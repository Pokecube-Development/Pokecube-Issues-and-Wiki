package pokecube.core.ai.tasks.utility;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;

import com.google.common.base.Predicate;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BeetrootBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.NetherWartBlock;
import net.minecraft.world.level.block.SweetBerryBushBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.IPlantable;
import net.minecraftforge.eventbus.api.Event.Result;
import pokecube.core.PokecubeCore;
import pokecube.core.ai.brain.BrainUtils;
import pokecube.core.ai.brain.sensors.NearBlocks.NearBlock;
import pokecube.core.ai.tasks.IRunnable;
import pokecube.core.events.HarvestCheckEvent;
import pokecube.core.interfaces.IMoveConstants.AIRoutine;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.PokecubeMod;
import pokecube.core.interfaces.pokemob.ai.GeneralStates;
import pokecube.core.interfaces.pokemob.ai.LogicStates;
import thut.api.entity.ai.VectorPosWrapper;
import thut.api.item.ItemList;
import thut.api.maths.Vector3;
import thut.lib.ItemStackTools;

/**
 * This IAIRunnable gets the mob to look for and collect dropped items and
 * berries. It requires an AIStoreStuff to have located a suitable storage
 * before it will run.
 */
public class GatherTask extends UtilTask
{
    /**
     * All things which inherit from BlockCrops, if not on this list, will be
     * valid options if at max age.
     */
    public static final ResourceLocation BLACKLIST = new ResourceLocation(PokecubeCore.MODID, "harvest_blacklist");
    /**
     * This contains extra things to harvest
     */
    public static final ResourceLocation HARVEST = new ResourceLocation(PokecubeCore.MODID, "harvest_extra");

    private static final Predicate<BlockState> fullCropNormal = input -> input.getBlock() instanceof CropBlock
            && input.hasProperty(CropBlock.AGE)
            && input.getValue(CropBlock.AGE) >= ((CropBlock) input.getBlock()).getMaxAge();

    private static final Predicate<BlockState> fullCropBeet = input -> input.getBlock() instanceof CropBlock
            && input.hasProperty(BeetrootBlock.AGE)
            && input.getValue(BeetrootBlock.AGE) >= ((CropBlock) input.getBlock()).getMaxAge();

    private static final Predicate<BlockState> fullCropNetherWart = input -> input.getBlock() instanceof NetherWartBlock
            && input.hasProperty(NetherWartBlock.AGE) && input.getValue(NetherWartBlock.AGE) >= 3;

    private static final Predicate<BlockState> sweetBerry = input -> input.getBlock() instanceof SweetBerryBushBlock
            && input.getValue(SweetBerryBushBlock.AGE) > 1;

    private static final Predicate<ItemEntity> deaditemmatcher = input -> !input.isAlive() || !input.isAddedToWorld();

    // Matcher used to determine if a block is a fruit or crop to be picked.
    private static final Predicate<BlockState> harvestMatcher = input -> {
        final boolean blacklisted = ItemList.is(GatherTask.BLACKLIST, input);
        if (blacklisted) return false;
        final boolean fullCrop = GatherTask.fullCropNormal.test(input) || GatherTask.fullCropBeet.test(input)
                || GatherTask.fullCropNetherWart.test(input);
        return fullCrop || ItemList.is(GatherTask.HARVEST, input);
    };

    public static interface IHarvester
    {
        default boolean isHarvestable(final Mob entity, final IPokemob pokemob, final BlockState state,
                final BlockPos pos, final ServerLevel world)
        {
            final HarvestCheckEvent event = new HarvestCheckEvent(pokemob, state, pos);
            PokecubeCore.POKEMOB_BUS.post(event);
            final boolean gatherable = event.getResult() == Result.ALLOW ? true
                    : event.getResult() == Result.DENY ? false : GatherTask.harvestMatcher.apply(state);
            return gatherable;
        }

        default void harvest(final Mob entity, final IPokemob pokemob, final BlockState state, final BlockPos pos,
                final ServerLevel world)
        {
            world.setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());
            final List<ItemStack> list = Block.getDrops(state, world, pos, null);
            boolean replanted = false;
            // See if anything dropped was a seed for the thing we
            // picked.
            for (final ItemStack stack : list)
            {
                // If so, Replant it.
                if (!replanted) replanted = new ReplantTask(stack, state, pos).run(world);
                new InventoryChange(entity, 2, stack, true).run(world);
            }
            if (!replanted) for (int i = 2; i < pokemob.getInventory().getContainerSize(); i++)
            {
                final ItemStack stack = pokemob.getInventory().getItem(i);
                if (!stack.isEmpty() && stack.getItem() instanceof IPlantable)
                {
                    final IPlantable plantable = (IPlantable) stack.getItem();
                    final BlockState plantState = plantable.getPlant(world, pos.above());
                    if (plantState.getBlock() == state.getBlock() && !replanted)
                    {
                        replanted = new ReplantTask(stack, state, pos).run(world);
                        break;
                    }
                }
            }
        }
    }

    /**
     * This manages the pokemobs replanting anything that they gather.
     *
     * @author Patrick
     */
    public static class ReplantTask implements IRunnable
    {
        final ItemStack seeds;
        final BlockPos pos;
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
        public boolean run(final Level world)
        {
            if (this.seeds.isEmpty()) return false;
            final BlockPos down = this.pos.below();
            // Use the fakeplayer to plant it
            final Player player = PokecubeMod.getFakePlayer(world);
            player.setPos(this.pos.getX(), this.pos.getY(), this.pos.getZ());
            player.getInventory().items.set(player.getInventory().selected, this.seeds);
            final UseOnContext context = new UseOnContext(player, InteractionHand.MAIN_HAND,
                    new BlockHitResult(new Vec3(0.5, 1, 0.5), Direction.UP, down, false));
            check:
            if (this.seeds.getItem() instanceof BlockItem && !this.selfPlacement)
            {
                final Block block = Block.byItem(this.seeds.getItem());
                if (block != this.oldState.getBlock()) break check;

                final BlockState def = block.defaultBlockState();
                boolean same = true;
                for (final Property<?> p : def.getProperties())
                {
                    if (!this.oldState.hasProperty(p))
                    {
                        same = false;
                        break;
                    }
                    if (this.oldState.getValue(p) != def.getValue(p))
                    {
                        same = false;
                        break;
                    }
                }
                if (same) return false;
            }

            // Attempt to plant it.
            final InteractionResult result = this.seeds.getItem().useOn(context);
            return result == InteractionResult.SUCCESS;
        }
    }

    public static int COOLDOWN_SEARCH = 200;
    public static int COOLDOWN_COLLECT = 5;

    public static int COOLDOWN_PATH = 5;

    public static Map<ResourceLocation, IHarvester> REGISTRY = Maps.newHashMap();

    static
    {
        GatherTask.REGISTRY.put(new ResourceLocation("pokecube:crops"), new IHarvester()
        {
        });
        GatherTask.REGISTRY.put(new ResourceLocation("pokecube:sweet_berries"), new IHarvester()
        {
            @Override
            public void harvest(final Mob entity, final IPokemob pokemob, final BlockState state, final BlockPos pos,
                    final ServerLevel world)
            {
                final int i = state.getValue(SweetBerryBushBlock.AGE);
                final boolean flag = i == 3;
                world.setBlockAndUpdate(pos, state.setValue(SweetBerryBushBlock.AGE, 1));
                final int j = 1 + world.random.nextInt(2);
                final ItemStack stack = new ItemStack(Items.SWEET_BERRIES, j + (flag ? 1 : 0));
                world.playSound((Player) null, pos, SoundEvents.SWEET_BERRY_BUSH_PICK_BERRIES, SoundSource.BLOCKS, 1.0F,
                        0.8F + world.random.nextFloat() * 0.4F);
                new InventoryChange(entity, 2, stack, true).run(world);
            }

            @Override
            public boolean isHarvestable(final Mob entity, final IPokemob pokemob, final BlockState state,
                    final BlockPos pos, final ServerLevel world)
            {
                final HarvestCheckEvent event = new HarvestCheckEvent(pokemob, state, pos);
                PokecubeCore.POKEMOB_BUS.post(event);
                final boolean gatherable = event.getResult() == Result.ALLOW ? true
                        : event.getResult() == Result.DENY ? false : GatherTask.sweetBerry.apply(state);
                return gatherable;
            }
        });
    }

    final double distance;

    List<NearBlock> blocks = null;
    List<ItemEntity> items = null;

    public ItemEntity targetItem = null;
    public NearBlock targetBlock = null;

    private ItemStack heldItem = ItemStack.EMPTY;
    private List<ResourceLocation> keys = Lists.newArrayList();

    boolean hasRoom = true;

    int collectCooldown = 0;
    int pathCooldown = 0;

    final StoreTask storage;

    ResourceLocation currentHarvester = null;

    Vector3 seeking = Vector3.getNewVector();

    Vector3 v = Vector3.getNewVector();
    Vector3 v1 = Vector3.getNewVector();

    public GatherTask(final IPokemob mob, final double distance, final StoreTask storage)
    {
        super(mob);
        this.distance = distance;
        this.storage = storage;
    }

    private void checkHeldItem()
    {
        ItemStack stack = pokemob.getHeldItem();
        if (stack != this.heldItem)
        {
            this.heldItem = stack;
            keys.clear();
            if (stack.hasTag() && stack.getTag().contains("pages") && stack.getTag().get("pages") instanceof ListTag)
            {
                final ListTag pages = (ListTag) stack.getTag().get("pages");
                try
                {
                    final Component comp = Component.Serializer.fromJson(pages.getString(0));
                    boolean isFilter = false;
                    for (final String line : comp.getString().split("\n"))
                    {
                        if (line.toLowerCase(Locale.ROOT).contains("item filters"))
                        {
                            isFilter = true;
                            continue;
                        }
                        if (isFilter)
                        {
                            ResourceLocation res = new ResourceLocation(line);
                            keys.add(res);
                        }
                    }
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
        }
    }

    private boolean checkValid(Object item_or_block)
    {
        checkHeldItem();
        for (ResourceLocation l : keys) if (ItemList.is(l, item_or_block)) return true;
        return keys.isEmpty();
    }

    private boolean isValidItem(ItemEntity item)
    {
        return checkValid(item.getItem());
    }

    private boolean isValidBlock(NearBlock block)
    {
        if (!checkValid(block.getState())) return false;
        boolean canHarvest = false;
        for (final Entry<ResourceLocation, IHarvester> entry : GatherTask.REGISTRY.entrySet())
        {
            canHarvest = entry.getValue().isHarvestable(this.entity, this.pokemob, block.getState(), block.getPos(),
                    this.world);
            if (canHarvest) break;
        }
        return canHarvest;
    }

    private boolean hasStuff()
    {
        if (this.targetItem != null && GatherTask.deaditemmatcher.apply(this.targetItem)) this.targetItem = null;
        if (this.targetBlock != null)
        {
            final BlockState state = this.entity.getCommandSenderWorld().getBlockState(this.targetBlock.getPos());
            final HarvestCheckEvent event = new HarvestCheckEvent(this.pokemob, state, this.targetBlock.getPos());
            PokecubeCore.POKEMOB_BUS.post(event);
            final boolean gatherable = event.getResult() == Result.ALLOW ? true
                    : event.getResult() == Result.DENY ? false : GatherTask.harvestMatcher.apply(state);
            if (!gatherable) this.targetBlock = null;
        }
        return this.targetItem != null || this.targetBlock != null;
    }

    private void findStuff()
    {
        // Only mobs that are standing with homes should look for stuff.
        if (this.pokemob.getHome() == null
                || this.pokemob.getGeneralState(GeneralStates.TAMED) && this.pokemob.getLogicState(LogicStates.SITTING))
            return;
        // This means we have stuff
        if (this.hasStuff()) return;

        if (this.items != null)
        {
            // Check for items to possibly gather.
            for (final ItemEntity e : this.items) if (!GatherTask.deaditemmatcher.apply(e))
            {
                this.targetItem = e;
                return;
            }
            if (this.targetItem != null) return;
        }
        if (this.blocks != null && this.blocks.size() > 0)
        {
            this.targetBlock = this.blocks.get(0);

            this.currentHarvester = null;
            for (final Entry<ResourceLocation, IHarvester> entry : GatherTask.REGISTRY.entrySet())
            {
                final boolean canHarvest = entry.getValue().isHarvestable(this.entity, this.pokemob,
                        this.targetBlock.getState(), this.targetBlock.getPos(), this.world);
                if (canHarvest)
                {
                    this.currentHarvester = entry.getKey();
                    break;
                }
            }
            return;
        }
        // Nothing found, enter cooldown.
        this.collectCooldown = GatherTask.COOLDOWN_SEARCH;
    }

    private void gatherStuff()
    {
        if (!this.hasStuff()) return;

        final Vector3 stuffLoc = Vector3.getNewVector();
        if (this.targetItem != null) stuffLoc.set(this.targetItem);
        else stuffLoc.set(this.targetBlock.getPos());

        // Set path to the stuff found.
        final double speed = 1;

        // The stuff below is for collecting blocks, so we return after setting
        // path if it is an item we are after
        if (this.targetItem != null)
        {
            double diff = 1;
            diff = Math.max(diff, this.entity.getBbWidth());
            if (this.targetItem.distanceTo(this.entity) < diff)
            {
                ItemStackTools.addItemStackToInventory(this.targetItem.getItem(), this.pokemob.getInventory(), 2);
                this.targetItem.discard();
            }
            else this.setWalkTo(stuffLoc, speed, 0);
            this.reset();
            return;
        }
        double diff = 2.5;
        diff = Math.max(diff, this.entity.getBbWidth());
        final double dist = stuffLoc.distToEntity(this.entity);
        this.v.set(this.entity).subtractFrom(stuffLoc);
        final double dy = this.v.y;
        final double dot = this.v.normalize().dot(Vector3.secondAxis);

        final boolean air = this.pokemob.floats() || this.pokemob.flys();
        final boolean groundShouldJump = this.entity.isOnGround() && !air && dot < -0.8 && dy < -1.8;
        final boolean flyShouldJump = !groundShouldJump && air && dist < 4;

        // This means that the item is directly above the pokemob, try to jump
        // to get closer
        final boolean jump = flyShouldJump || groundShouldJump;
        if (jump) BrainUtils.setLeapTarget(this.entity, new VectorPosWrapper(stuffLoc));

        if (dist < diff)
        {
            final BlockState state = stuffLoc.getBlockState(this.entity.getCommandSenderWorld());
            if (this.currentHarvester != null)
            {
                final IHarvester harvest = GatherTask.REGISTRY.get(this.currentHarvester);
                if (harvest.isHarvestable(this.entity, this.pokemob, state, stuffLoc.getPos(), this.world))
                    harvest.harvest(this.entity, this.pokemob, state, stuffLoc.getPos(), this.world);
            }
            this.reset();
        }
        else if (!jump)
        {
            this.setWalkTo(stuffLoc, speed, 0);
        }
    }

    @Override
    public void reset()
    {
        this.targetItem = null;
        this.targetBlock = null;
        this.currentHarvester = null;
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

        final boolean wildCheck = !PokecubeCore.getConfig().wildGather
                && !this.pokemob.getGeneralState(GeneralStates.TAMED);
        // Check if this should be doing something else instead, if so return
        // false.
        if (this.tameCheck() || BrainUtils.hasAttackTarget(this.entity) || wildCheck) return false;

        final int rate = this.pokemob.getGeneralState(GeneralStates.TAMED) ? PokecubeCore.getConfig().tameGatherDelay
                : PokecubeCore.getConfig().wildGatherDelay;
        final Random rand = new Random(this.pokemob.getRNGValue());
        // Check if it has a location, if so, apply a delay and return false if
        // not correct tick for this pokemob.
        if (this.pokemob.getHome() == null || this.entity.tickCount % rate != rand.nextInt(rate)) return false;

        final List<NearBlock> blocks = BrainUtils.getNearBlocks(this.entity);
        final List<ItemEntity> items = BrainUtils.getNearItems(this.entity);

        final BlockPos home = this.pokemob.getHome();
        final float dist = this.pokemob.getHomeDistance() * this.pokemob.getHomeDistance();

        final Predicate<BlockPos> inRange = p -> home != null ? home.distSqr(p) < dist : true;

        if (blocks != null)
        {
            this.blocks = Lists.newArrayList(blocks);
            this.blocks.removeIf(b -> {
                if (!inRange.test(b.getPos())) return true;
                return !isValidBlock(b);
            });
            if (this.blocks.isEmpty()) this.blocks = null;
        }
        // Only replace this if the new list is not null.
        if (items != null)
        {
            this.items = Lists.newArrayList(items);
            this.items.removeIf(b -> !inRange.test(b.blockPosition()) || !isValidItem(b));
            if (this.items.isEmpty()) this.items = null;
        }

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
        return this.pokemob.getGeneralState(GeneralStates.TAMED)
                && (!this.pokemob.getGeneralState(GeneralStates.STAYING) || !PokecubeCore.getConfig().tameGather);
    }

    @Override
    public void tick()
    {
        this.gatherStuff();
    }
}
