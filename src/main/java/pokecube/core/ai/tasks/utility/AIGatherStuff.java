package pokecube.core.ai.tasks.utility;

import java.util.List;
import java.util.Random;

import com.google.common.base.Predicate;
import com.google.common.collect.Lists;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.CropsBlock;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.state.IProperty;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.common.IPlantable;
import pokecube.core.PokecubeCore;
import pokecube.core.ai.brain.BrainUtils;
import pokecube.core.ai.brain.sensors.NearBlocks.NearBlock;
import pokecube.core.ai.tasks.IRunnable;
import pokecube.core.interfaces.IMoveConstants.AIRoutine;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.PokecubeMod;
import pokecube.core.interfaces.pokemob.ai.GeneralStates;
import pokecube.core.interfaces.pokemob.ai.LogicStates;
import thut.api.item.ItemList;
import thut.api.maths.Vector3;
import thut.lib.ItemStackTools;

/**
 * This IAIRunnable gets the mob to look for and collect dropped items and
 * berries. It requires an AIStoreStuff to have located a suitable storage
 * before it will run.
 */
public class AIGatherStuff extends UtilTask
{
    /**
     * This manages the pokemobs replanting anything that they gather.
     *
     * @author Patrick
     */
    public static class ReplantTask implements IRunnable
    {
        final ItemStack  seeds;
        final BlockPos   pos;
        final BlockState oldState;

        final boolean selfPlacement;

        public ReplantTask(final ItemStack seeds, final BlockState old, final BlockPos pos)
        {
            this(seeds, old, pos, false);
        }

        public ReplantTask(final ItemStack seeds, final BlockState old, final BlockPos pos, final boolean selfPlacment)
        {
            this.seeds = seeds;
            this.pos = new BlockPos(pos);
            this.oldState = old;
            this.selfPlacement = selfPlacment;
        }

        @Override
        public boolean run(final World world)
        {
            if (this.seeds.isEmpty()) return false;
            final BlockPos down = this.pos.down();
            // Use the fakeplayer to plant it
            final PlayerEntity player = PokecubeMod.getFakePlayer(world);
            player.setPosition(this.pos.getX(), this.pos.getY(), this.pos.getZ());
            player.inventory.mainInventory.set(player.inventory.currentItem, this.seeds);
            final ItemUseContext context = new ItemUseContext(player, Hand.MAIN_HAND, new BlockRayTraceResult(new Vec3d(
                    0.5, 1, 0.5), Direction.UP, down, false));
            check:
            if (this.seeds.getItem() instanceof BlockItem && !this.selfPlacement)
            {
                final Block block = Block.getBlockFromItem(this.seeds.getItem());
                if (block != this.oldState.getBlock()) break check;

                final BlockState def = block.getDefaultState();
                boolean same = true;
                for (final IProperty<?> p : def.getProperties())
                {
                    if (!this.oldState.has(p))
                    {
                        same = false;
                        break;
                    }
                    if (this.oldState.get(p) != def.get(p))
                    {
                        same = false;
                        break;
                    }
                }
                if (same) return false;
            }

            // Attempt to plant it.
            final ActionResultType result = this.seeds.getItem().onItemUse(context);
            return result == ActionResultType.SUCCESS;
        }
    }

    public static int COOLDOWN_SEARCH  = 200;
    public static int COOLDOWN_COLLECT = 5;

    public static int COOLDOWN_PATH = 5;

    /**
     * All things which inherit from BlockCrops, if not on this list, will be
     * valid options if at max age.
     */
    public static final ResourceLocation BLACKLIST = new ResourceLocation(PokecubeCore.MODID, "harvest_blacklist");
    /**
     * This contains extra things to harvest
     */
    public static final ResourceLocation HARVEST   = new ResourceLocation(PokecubeCore.MODID, "harvest_extra");

    // Matcher used to determine if a block is a fruit or crop to be picked.
    private static final Predicate<BlockState> harvestMatcher = input ->
    {
        final boolean blacklisted = ItemList.is(AIGatherStuff.BLACKLIST, input);
        if (blacklisted) return false;
        final boolean fullCrop = input.getBlock() instanceof CropsBlock && input.get(
                CropsBlock.AGE) >= ((CropsBlock) input.getBlock()).getMaxAge();
        return fullCrop || ItemList.is(AIGatherStuff.HARVEST, input);
    };

    private static final Predicate<ItemEntity> deaditemmatcher = input -> !input.isAlive() || !input.addedToChunk
            || !input.isAddedToWorld();

    final double distance;

    List<NearBlock>  blocks = null;
    List<ItemEntity> items  = null;

    ItemEntity targetItem  = null;
    NearBlock  targetBlock = null;

    boolean hasRoom = true;

    int collectCooldown = 0;
    int pathCooldown    = 0;

    final AIStoreStuff storage;

    Vector3 seeking = Vector3.getNewVector();

    Vector3 v  = Vector3.getNewVector();
    Vector3 v1 = Vector3.getNewVector();

    public AIGatherStuff(final IPokemob mob, final double distance, final AIStoreStuff storage)
    {
        super(mob);
        this.distance = distance;
        this.storage = storage;
    }

    private boolean hasStuff()
    {
        if (this.targetItem != null && AIGatherStuff.deaditemmatcher.apply(this.targetItem)) this.targetItem = null;
        if (this.targetBlock != null && !AIGatherStuff.harvestMatcher.apply(this.entity.getEntityWorld().getBlockState(
                this.targetBlock.getPos()))) this.targetBlock = null;
        return this.targetItem != null || this.targetBlock != null;
    }

    private void findStuff()
    {
        // Only mobs that are standing with homes should look for stuff.
        if (this.pokemob.getHome() == null || this.pokemob.getGeneralState(GeneralStates.TAMED) && this.pokemob
                .getLogicState(LogicStates.SITTING)) return;
        // This means we have stuff
        if (this.hasStuff()) return;

        if (this.items != null)
        {
            // Check for items to possibly gather.
            for (final ItemEntity e : this.items)
                if (!AIGatherStuff.deaditemmatcher.apply(e))
                {
                    this.targetItem = e;
                    return;
                }
            if (this.targetItem != null) return;
        }
        if (this.blocks != null && this.blocks.size() > 0)
        {
            this.targetBlock = this.blocks.get(0);
            return;
        }
        // Nothing found, enter cooldown.
        this.collectCooldown = AIGatherStuff.COOLDOWN_SEARCH;
    }

    private void gatherStuff()
    {
        if (!this.hasStuff()) return;

        final Vector3 stuffLoc = Vector3.getNewVector();
        if (this.targetItem != null) stuffLoc.set(this.targetItem);
        else stuffLoc.set(this.targetBlock.getPos());

        // Set path to the stuff found.
        final double speed = this.entity.getAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).getValue();
        this.setWalkTo(stuffLoc, speed, 0);

        // The stuff below is for collecting blocks, so we return after setting
        // path if it is an item we are after
        if (this.targetItem != null)
        {
            double diff = 1;
            diff = Math.max(diff, this.entity.getWidth());
            if (this.targetItem.getDistance(this.entity) < diff)
            {
                ItemStackTools.addItemStackToInventory(this.targetItem.getItem(), this.pokemob.getInventory(), 2);
                this.targetItem.remove();
            }
            this.reset();
            return;
        }
        double diff = 2;
        diff = Math.max(diff, this.entity.getWidth());
        final double dist = stuffLoc.distToEntity(this.entity);
        this.v.set(this.entity).subtractFrom(stuffLoc);
        final double dot = this.v.normalize().dot(Vector3.secondAxis);
        // This means that the item is directly above the pokemob, assume it
        // can pick up to 3 blocks upwards.
        if (dot < -0.9 && this.entity.onGround) diff = Math.max(3, diff);
        if (dist < diff)
        {
            final BlockState state = stuffLoc.getBlockState(this.entity.getEntityWorld());
            stuffLoc.setAir(this.world);
            final List<ItemStack> list = Block.getDrops(state, this.world, stuffLoc.getPos(), null);
            boolean replanted = false;
            // See if anything dropped was a seed for the thing we
            // picked.
            for (final ItemStack stack : list)
            {
                // If so, Replant it.
                if (!replanted) replanted = new ReplantTask(stack, state, stuffLoc.getPos()).run(this.world);
                new InventoryChange(this.entity, 2, stack, true).run(this.world);
            }
            if (!replanted) for (int i = 2; i < this.pokemob.getInventory().getSizeInventory(); i++)
            {
                final ItemStack stack = this.pokemob.getInventory().getStackInSlot(i);
                if (!stack.isEmpty() && stack.getItem() instanceof IPlantable)
                {
                    final IPlantable plantable = (IPlantable) stack.getItem();
                    final BlockState plantState = plantable.getPlant(this.world, stuffLoc.getPos().up());
                    if (plantState.getBlock() == state.getBlock() && !replanted)
                    {
                        replanted = new ReplantTask(stack, state, stuffLoc.getPos()).run(this.world);
                        break;
                    }
                }
            }
            this.reset();
        }
    }

    @Override
    public void reset()
    {
        this.targetItem = null;
        this.targetBlock = null;
    }

    @Override
    public void run()
    {
        this.findStuff();
    }

    @Override
    public boolean shouldRun()
    {
        // Check if gather is enabled first.
        if (!this.pokemob.isRoutineEnabled(AIRoutine.GATHER)) return false;

        // Dont run if the storage is currently trying to path somewhere
        if (this.storage.pathing) return false;

        // We are going after something.
        if (this.hasStuff()) return true;

        final boolean wildCheck = !PokecubeCore.getConfig().wildGather && !this.pokemob.getGeneralState(
                GeneralStates.TAMED);
        // Check if this should be doing something else instead, if so return
        // false.
        if (this.tameCheck() || BrainUtils.hasAttackTarget(this.entity) || wildCheck) return false;

        final int rate = this.pokemob.getGeneralState(GeneralStates.TAMED) ? PokecubeCore.getConfig().tameGatherDelay
                : PokecubeCore.getConfig().wildGatherDelay;
        final Random rand = new Random(this.pokemob.getRNGValue());
        // Check if it has a location, if so, apply a delay and return false if
        // not correct tick for this pokemob.
        if (this.pokemob.getHome() == null || this.entity.ticksExisted % rate != rand.nextInt(rate)) return false;

        final List<NearBlock> blocks = BrainUtils.getNearBlocks(this.entity);
        final List<ItemEntity> items = BrainUtils.getNearItems(this.entity);

        if (blocks != null)
        {
            this.blocks = Lists.newArrayList(blocks);
            this.blocks.removeIf(b -> !AIGatherStuff.harvestMatcher.apply(b.getState()));
        }
        // Only replace this if the new list is not null.
        if (items != null) this.items = items;

        if (this.blocks == null && this.items == null) return false;
        // check if pokemob has room in inventory for stuff, if so, return true.
        return this.storage.emptySlots > 0;
    }

    /**
     * Only tame pokemobs set to "stay" should run this AI.
     *
     * @return
     */
    private boolean tameCheck()
    {
        return this.pokemob.getGeneralState(GeneralStates.TAMED) && (!this.pokemob.getGeneralState(
                GeneralStates.STAYING) || !PokecubeCore.getConfig().tameGather);
    }

    @Override
    public void tick()
    {
        this.gatherStuff();
    }
}
