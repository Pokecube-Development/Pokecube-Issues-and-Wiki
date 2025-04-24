package pokecube.core.ai.tasks.utility;

import com.google.common.base.Predicate;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
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
import net.neoforged.neoforge.common.util.TriState;
import net.neoforged.neoforge.items.IItemHandlerModifiable;
import pokecube.api.PokecubeAPI;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.entity.pokemob.PokemobCaps;
import pokecube.api.entity.pokemob.ai.AIRoutine;
import pokecube.api.entity.pokemob.ai.GeneralStates;
import pokecube.api.entity.pokemob.ai.LogicStates;
import pokecube.api.events.pokemobs.ai.HarvestCheckEvent;
import pokecube.core.PokecubeCore;
import pokecube.core.ai.brain.BrainUtils;
import pokecube.core.ai.brain.MemoryModules;
import pokecube.core.ai.brain.sensors.NearBlocks.NearBlock;
import pokecube.core.ai.tasks.IRunnable;
import pokecube.core.ai.tasks.PokemobBehaviour;
import pokecube.core.ai.tasks.TaskBase;
import pokecube.core.impl.PokecubeMod;
import pokecube.core.inventory.pokemob.PokemobInventory;
import pokecube.core.utils.mixin.IBlockItem;
import thut.api.entity.ai.VectorPosWrapper;
import thut.api.item.ItemList;
import thut.api.maths.Vector3;
import thut.lib.ItemStackTools;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;

/**
 * This IAIRunnable gets the mob to look for and collect dropped items and berries. It requires an AIStoreStuff to have
 * located a suitable storage before it will run.
 */
public class GatherItems extends PokemobBehaviour
{
    /**
     * All things which inherit from BlockCrops, if not on this list, will be valid options if at max age.
     */
    public static final ResourceLocation BLACKLIST = ResourceLocation.fromNamespaceAndPath(PokecubeCore.MODID,
            "harvest_blacklist");
    /**
     * This contains extra things to harvest
     */
    public static final ResourceLocation HARVEST = ResourceLocation.fromNamespaceAndPath(PokecubeCore.MODID,
            "harvest_extra");

    private static final Predicate<BlockState> fullCropNormal = input -> input.getBlock() instanceof CropBlock crop
            && input.hasProperty(CropBlock.AGE) && input.getValue(CropBlock.AGE) >= crop.getMaxAge();

    private static final Predicate<BlockState> fullCropBeet = input -> input.getBlock() instanceof CropBlock crop
            && input.hasProperty(BeetrootBlock.AGE) && input.getValue(BeetrootBlock.AGE) >= crop.getMaxAge();

    private static final Predicate<BlockState> fullCropNetherWart = input -> input.getBlock() instanceof NetherWartBlock
            && input.hasProperty(NetherWartBlock.AGE) && input.getValue(NetherWartBlock.AGE) >= 3;

    private static final Predicate<BlockState> sweetBerry = input -> input.getBlock() instanceof SweetBerryBushBlock
            && input.getValue(SweetBerryBushBlock.AGE) > 1;

    public static final Predicate<ItemEntity> deaditemmatcher = input -> !input.isAlive() || !input.isAddedToLevel();

    // Matcher used to determine if a block is a fruit or crop to be picked.
    public static final Predicate<BlockState> harvestMatcher = input -> {
        final boolean blacklisted = ItemList.is(GatherItems.BLACKLIST, input);
        if (blacklisted) return false;
        final boolean fullCrop = GatherItems.fullCropNormal.test(input) || GatherItems.fullCropBeet.test(input)
                || GatherItems.fullCropNetherWart.test(input);
        return fullCrop || ItemList.is(GatherItems.HARVEST, input);
    };

    public static record HarvestContext(ServerLevel level, BlockState state, BlockPos pos,
            IItemHandlerModifiable destination, boolean isPokemobInventory)
    {}

    public static interface IHarvester
    {
        default boolean isHarvestable(Mob entity, IPokemob pokemob, HarvestContext context)
        {
            final HarvestCheckEvent event = new HarvestCheckEvent(pokemob, context.state(), context.pos());
            PokecubeAPI.POKEMOB_BUS.post(event);
            return event.getResult() == TriState.TRUE || (event.getResult() != TriState.FALSE
                    && GatherItems.harvestMatcher.apply(context.state()));
        }

        default void harvest(Mob entity, IPokemob pokemob, HarvestContext context)
        {
            context.level().setBlockAndUpdate(context.pos(), Blocks.AIR.defaultBlockState());
            final List<ItemStack> list = Block.getDrops(context.state(), context.level(), context.pos(),
                    context.level().getBlockEntity(context.pos()));
            boolean replanted = false;

            int startSlot = context.isPokemobInventory() ? 2 : 0;
            int endSlot = context.isPokemobInventory()
                    ? PokemobInventory.MAIN_INVENTORY_SIZE
                    : context.destination().getSlots();
            // See if anything dropped was a seed for the thing we
            // picked.
            for (final ItemStack stack : list)
            {
                // If so, Replant it.
                if (!replanted) replanted = new ReplantTask(stack, context.state(), context.pos()).run(context.level());
                new TaskBase.InventoryChange(entity, startSlot, stack, true).run(context.level());
            }
            if (!replanted) for (int i = startSlot; i < endSlot; i++)
            {
                final ItemStack stack = pokemob.getInventory().getItem(i);
                if (!stack.isEmpty() && stack.getItem() instanceof IBlockItem plantable)
                {
                    Vec3 mid = context.pos().getBottomCenter();
                    BlockHitResult hit = new BlockHitResult(mid, Direction.DOWN, context.pos(), true);
                    BlockPlaceContext _context = new BlockPlaceContext(context.level(), null, InteractionHand.MAIN_HAND,
                            stack, hit);
                    final BlockState plantState = plantable.getPlacement(_context);
                    if (plantState.getBlock() == context.state().getBlock() && !replanted)
                    {
                        new ReplantTask(stack, context.state(), context.pos()).run(context.level());
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
            if (this.seeds.getItem() instanceof BlockItem item && !this.selfPlacement)
            {
                final Block block = item.getBlock();
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

    public static Map<ResourceLocation, IHarvester> REGISTRY = Maps.newHashMap();

    static
    {
        GatherItems.REGISTRY.put(ResourceLocation.parse("pokecube:crops"), new IHarvester()
        {});
        GatherItems.REGISTRY.put(ResourceLocation.parse("pokecube:sweet_berries"), new IHarvester()
        {
            @Override
            public void harvest(final Mob entity, final IPokemob pokemob, HarvestContext context)
            {
                final int i = context.state().getValue(SweetBerryBushBlock.AGE);
                final boolean flag = i == 3;
                context.level().setBlockAndUpdate(context.pos(), context.state().setValue(SweetBerryBushBlock.AGE, 1));
                final int j = 1 + context.level().random.nextInt(2);
                final ItemStack stack = new ItemStack(Items.SWEET_BERRIES, j + (flag ? 1 : 0));
                context.level()
                        .playSound(null, context.pos(), SoundEvents.SWEET_BERRY_BUSH_PICK_BERRIES, SoundSource.BLOCKS,
                                1.0F, 0.8F + context.level().random.nextFloat() * 0.4F);
                new TaskBase.InventoryChange(entity, 2, stack, true).run(context.level());
            }

            @Override
            public boolean isHarvestable(final Mob entity, final IPokemob pokemob, HarvestContext context)
            {
                final HarvestCheckEvent event = new HarvestCheckEvent(pokemob, context.state(), context.pos());
                PokecubeAPI.POKEMOB_BUS.post(event);
                return event.getResult() == TriState.TRUE
                        || event.getResult() != TriState.FALSE && GatherItems.sweetBerry.apply(context.state());
            }
        });
    }

    public static class GatherDetails
    {
        public List<NearBlock> blocks = null;
        public List<ItemEntity> items = null;

        public ItemEntity targetItem = null;
        public NearBlock targetBlock = null;

        public int collectCooldown = 0;

        public ResourceLocation currentHarvester = null;
    }

    private static final Map<MemoryModuleType<?>, MemoryStatus> MEMS = Maps.newHashMap();

    static
    {
        MEMS.put(MemoryModules.GATHER_DETAILS.get(), MemoryStatus.REGISTERED);
        MEMS.put(MemoryModules.ATTACKTARGET.get(), MemoryStatus.VALUE_ABSENT);
    }

    final double distance;

    public GatherItems(final double distance)
    {
        super(MEMS);
        this.distance = distance;
    }

    private boolean isValidItem(ItemEntity item, StoreItems storage)
    {
        return storage.checkValid(item.getItem());
    }

    private boolean isValidBlock(NearBlock block, StoreItems storage)
    {
        if (!storage.checkValid(block.getState())) return false;
        boolean canHarvest = false;
        HarvestContext context = new HarvestContext(storage.level, block.getState(), block.getPos(),
                storage.getTaskInventory(), storage.getTaskInventory() == storage.getPokeInventory());
        for (final Entry<ResourceLocation, IHarvester> entry : GatherItems.REGISTRY.entrySet())
        {
            canHarvest = entry.getValue().isHarvestable(storage.entity, storage.pokemob, context);
            if (canHarvest) break;
        }
        return canHarvest;
    }

    private boolean hasStuff(StoreItems storage, GatherDetails details)
    {
        if (details.targetItem != null && GatherItems.deaditemmatcher.apply(details.targetItem))
            details.targetItem = null;
        if (details.targetBlock != null)
        {
            final BlockState state = storage.entity.level().getBlockState(details.targetBlock.getPos());
            final HarvestCheckEvent event = new HarvestCheckEvent(storage.pokemob, state, details.targetBlock.getPos());
            PokecubeAPI.POKEMOB_BUS.post(event);
            final boolean gatherable = event.getResult() == TriState.TRUE
                    || event.getResult() != TriState.FALSE && GatherItems.harvestMatcher.apply(state);
            if (!gatherable) details.targetBlock = null;
        }
        return details.targetItem != null || details.targetBlock != null;
    }

    private void findStuff(StoreItems storage, GatherDetails details)
    {
        // Only mobs that are standing with homes should look for stuff.
        if (storage.pokemob.getHome() == null
                || storage.pokemob.getGeneralState(GeneralStates.TAMED) && storage.pokemob.getLogicState(
                LogicStates.SITTING)) return;
        // This means we have stuff
        if (this.hasStuff(storage, details)) return;

        if (details.items != null)
        {
            // Check for items to possibly gather.
            for (final ItemEntity e : details.items)
                if (!GatherItems.deaditemmatcher.apply(e))
                {
                    details.targetItem = e;
                    return;
                }
            if (details.targetItem != null) return;
        }
        if (details.blocks != null && !details.blocks.isEmpty())
        {
            details.targetBlock = details.blocks.getFirst();

            details.currentHarvester = null;
            HarvestContext context = new HarvestContext(storage.level, details.targetBlock.getState(),
                    details.targetBlock.getPos(), storage.getTaskInventory(),
                    storage.getTaskInventory() == storage.getPokeInventory());
            for (final Entry<ResourceLocation, IHarvester> entry : GatherItems.REGISTRY.entrySet())
            {
                final boolean canHarvest = entry.getValue().isHarvestable(storage.entity, storage.pokemob, context);
                if (canHarvest)
                {
                    details.currentHarvester = entry.getKey();
                    break;
                }
            }
            return;
        }
        // Nothing found, enter cooldown.
        details.collectCooldown = GatherItems.COOLDOWN_SEARCH;
    }

    private void gatherStuff(StoreItems storage, GatherDetails details)
    {
        if (!this.hasStuff(storage, details)) return;

        final Vector3 stuffLoc = new Vector3();
        if (details.targetItem != null) stuffLoc.set(details.targetItem);
        else stuffLoc.set(details.targetBlock.getPos());

        // Set path to the stuff found.
        final double speed = 1;

        // The stuff below is for collecting blocks, so we return after setting
        // path if it is an item we are after
        if (details.targetItem != null)
        {
            double diff = 1;
            diff = Math.max(diff, storage.entity.getBbWidth());

            int minSlot = 0;
            int maxSlot = storage.getTaskInventory().getSlots();

            if (storage.getTaskInventory() == storage.getPokeInventory())
            {
                minSlot = 2;
                maxSlot = PokemobInventory.MAIN_INVENTORY_SIZE;
            }

            if (details.targetItem.distanceTo(storage.entity) < diff && ItemStackTools.addItemStackToInventory(
                    details.targetItem.getItem(), storage.getTaskInventory(), minSlot, maxSlot))
            {
                details.targetItem.discard();
            }
            else this.setWalkTo(storage.entity, stuffLoc, speed, 0);
            this.reset(storage.entity);
            return;
        }
        double diff = 2.5;
        diff = Math.max(diff, storage.entity.getBbWidth());
        final double dist = stuffLoc.distToEntity(storage.entity);
        Vector3 v = new Vector3(storage.entity).subtractFrom(stuffLoc);
        final double dy = v.y;
        final double dot = v.normalize().dot(Vector3.secondAxis);

        final boolean air = storage.pokemob.floats() || storage.pokemob.flys();
        final boolean groundShouldJump = storage.entity.onGround() && !air && dot < -0.8 && dy < -1.8;
        final boolean flyShouldJump = !groundShouldJump && air && dist < 4;

        // This means that the item is directly above the pokemob, try to jump
        // to get closer
        final boolean jump = flyShouldJump || groundShouldJump;
        if (jump) BrainUtils.setLeapTarget(storage.entity, new VectorPosWrapper(stuffLoc));

        if (dist < diff)
        {
            final BlockState state = stuffLoc.getBlockState(storage.entity.level());
            if (details.currentHarvester != null)
            {
                HarvestContext context = new HarvestContext(storage.level, state, stuffLoc.getPos(),
                        storage.getTaskInventory(), storage.getTaskInventory() == storage.getPokeInventory());
                final IHarvester harvest = GatherItems.REGISTRY.get(details.currentHarvester);
                if (harvest.isHarvestable(storage.entity, storage.pokemob, context))
                    harvest.harvest(storage.entity, storage.pokemob, context);
            }
            this.reset(storage.entity);
        }
        else if (!jump)
        {
            this.setWalkTo(storage.entity, stuffLoc, speed, 0);
        }
    }

    @Override
    protected void stop(final ServerLevel level, final Mob entityIn, final long gameTimeIn)
    {
        entityIn.getBrain().eraseMemory(MemoryModules.GATHER_DETAILS.get());
    }

    @Override
    protected boolean timedOut(long gameTime)
    {
        return false;
    }

    @Override
    protected void tick(ServerLevel level, Mob owner, long gameTime)
    {
        var storage = owner.getData(StoreItems.StoreBehaviour.TYPE);
        GatherDetails details;
        var detailsOpt = owner.getBrain().getMemory(MemoryModules.GATHER_DETAILS.get());
        if (detailsOpt.isEmpty())
        {
            owner.getBrain().setMemory(MemoryModules.GATHER_DETAILS.get(), details = new GatherDetails());
        }
        else details = detailsOpt.get();
        this.findStuff(storage, details);
        this.gatherStuff(storage, details);
    }

    @Override
    public boolean shouldRun(Mob entity)
    {
        IPokemob pokemob = PokemobCaps.getPokemobFor(entity);
        if (pokemob == null) return false;
        var storage = entity.getData(StoreItems.StoreBehaviour.TYPE);
        storage.bind(entity);
        // Check if gather is enabled first.
        if (!pokemob.isRoutineEnabled(AIRoutine.GATHER)) return false;

        // Dont run if the storage is currently trying to path somewhere
        if (storage.pathing) return false;

        GatherDetails details;
        var detailsOpt = entity.getBrain().getMemory(MemoryModules.GATHER_DETAILS.get());
        if (detailsOpt.isEmpty())
        {
            entity.getBrain().setMemory(MemoryModules.GATHER_DETAILS.get(), details = new GatherDetails());
        }
        else details = detailsOpt.get();

        // We are going after something.
        if (this.hasStuff(storage, details)) return true;

        final boolean wildCheck = !PokecubeCore.getConfig().wildGather && !pokemob.getGeneralState(GeneralStates.TAMED);
        // Check if this should be doing something else instead, if so return
        // false.
        if (this.tameCheck(pokemob) || BrainUtils.hasAttackTarget(entity) || wildCheck) return false;

        final int rate = pokemob.getGeneralState(GeneralStates.TAMED)
                ? PokecubeCore.getConfig().tameGatherDelay
                : PokecubeCore.getConfig().wildGatherDelay;
        final Random rand = new Random(pokemob.getRNGValue());
        // Check if it has a location, if so, apply a delay and return false if
        // not correct tick for this pokemob.
        if (pokemob.getHome() == null || entity.tickCount % rate != rand.nextInt(rate)) return false;

        final List<NearBlock> blocks = BrainUtils.getNearBlocks(entity);
        final List<ItemEntity> items = BrainUtils.getNearItems(entity);

        final BlockPos home = pokemob.getHome();
        final float dist = pokemob.getHomeDistance() * pokemob.getHomeDistance();

        final Predicate<BlockPos> inRange = p -> home == null || home.distSqr(p) < dist;

        if (blocks != null)
        {
            details.blocks = Lists.newArrayList(blocks);
            details.blocks.removeIf(b -> {
                if (!inRange.test(b.getPos())) return true;
                return !isValidBlock(b, storage);
            });
            if (details.blocks.isEmpty()) details.blocks = null;
        }
        // Only replace this if the new list is not null.
        if (items != null)
        {
            details.items = Lists.newArrayList(items);
            details.items.removeIf(b -> !inRange.test(b.blockPosition()) || !isValidItem(b, storage));
            if (details.items.isEmpty()) details.items = null;
        }

        if (details.blocks == null && details.items == null) return false;
        // check if pokemob has room in inventory for stuff, if so, return true.
        return storage.emptySlots > 0;
    }

    /**
     * Only tame pokemobs set to "stay" should run this AI.
     */
    private boolean tameCheck(IPokemob pokemob)
    {
        return pokemob.getGeneralState(GeneralStates.TAMED) && (!pokemob.getGeneralState(GeneralStates.STAYING)
                || !PokecubeCore.getConfig().tameGather);
    }
}
