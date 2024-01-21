package pokecube.gimmicks.builders.tasks;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.function.Consumer;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.items.IItemHandlerModifiable;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.entity.pokemob.ai.AIRoutine;
import pokecube.api.entity.pokemob.ai.LogicStates;
import pokecube.core.ai.tasks.utility.StoreTask;
import pokecube.core.ai.tasks.utility.UtilTask;
import pokecube.core.inventory.pokemob.PokemobInventory;
import pokecube.gimmicks.builders.BuilderTasks;
import pokecube.gimmicks.builders.builders.BuilderManager.BuilderClearer;
import pokecube.gimmicks.builders.builders.IBlocksBuilder;
import pokecube.gimmicks.builders.builders.IBlocksBuilder.BoMRecord;
import pokecube.gimmicks.builders.builders.IBlocksBuilder.CanPlace;
import pokecube.gimmicks.builders.builders.IBlocksBuilder.PlaceInfo;
import pokecube.gimmicks.builders.builders.IBlocksClearer;
import pokecube.gimmicks.builders.builders.StructureBuilder;
import thut.api.ThutCaps;
import thut.api.maths.Vector3;
import thut.api.world.StructureTemplateTools;
import thut.core.common.ThutCore;
import thut.lib.ItemStackTools;

/**
 * This IAIRunnable builds a structure based on the set
 * {@link StructureBuilder}, and then provides feedback on it via a book in the
 * offhand slot.
 */
public class DoBuild extends UtilTask
{
    public static final String KEY = "build_buildings";

    public final StoreTask storage;

    boolean hasInstructions = false;
    ItemStack last = ItemStack.EMPTY;

    BuilderClearer build;

    IBlocksBuilder builder;
    IBlocksClearer clearer;

    IItemHandlerModifiable ourInventory;

    boolean findingSpot = false;
    boolean gettingPart = false;

    PlaceInfo nextPlace = null;
    BlockPos nextClear = null;
    int pathTimeout = -1;

    Vector3 seeking = new Vector3();

    Vector3 v = new Vector3();
    Vector3 v1 = new Vector3();

    BoMRecord BoM = null;

    public DoBuild(IPokemob pokemob, StoreTask storage)
    {
        super(pokemob);
        this.storage = storage;
    }

    @Override
    public String getIdentifier()
    {
        return KEY;
    }

    @Override
    public void reset()
    {
        hasInstructions = false;
        builder = null;
        clearer = null;
        this.build = null;
    }

    private boolean checkValid(ItemStack stack, List<ItemStack> requested)
    {
        for (ItemStack v : requested) if (ItemStack.isSame(v, stack)) return true;
        return false;
    }

    public void setBuilder(BuilderClearer build, ServerLevel level)
    {
        if (build == this.build) return;

        var pair = storage.getInventory(level, storage.storageLoc, Direction.UP);
        if (pair == null || build == null)
        {
            reset();
            return;
        }
        this.builder = build.builder();
        this.clearer = build.clearer();

        if (this.BoM == null) this.BoM = new BoMRecord(() -> pair.getFirst().getStackInSlot(0),
                _book -> pair.getFirst().setStackInSlot(0, _book));

        if (this.builder != null)
        {
            builder.update(level);
            builder.provideBoM(this.BoM, true);
            hasInstructions = true;
            builder.setCreative(pokemob.getOwner() instanceof ServerPlayer player && player.isCreative());
            if (!builder.validBuilder()) reset();
        }
    }

    private boolean blocksClear(ServerLevel level)
    {
        if (clearer == null) return true;
        if (nextClear == null) nextClear = clearer.nextRemoval();
        boolean isClear = nextClear == null;
        pathTimeout--;

        // First check for blocks to remove
        if (!isClear)
        {
//            if (entity.tickCount % 20 == 0) System.out.println("Clearing " + nextClear);
            double diff = 5;
            diff = Math.max(diff, this.entity.getBbWidth());
            if (entity.getOnPos().distSqr(nextClear) > diff)
            {
                this.setWalkTo(nextClear, 1, 0);
                if (pathTimeout < 0) pathTimeout = 150;
                clearer.markPendingClear(nextClear);
            }
            if (pathTimeout < 20 || clearer.isCreative())
            {
                if (!storage.canBreak(level, nextClear))
                {
                    // Notify that we can't actually break this.
                    double size = 0.1;
                    double x = this.entity.getX();
                    double y = this.entity.getY() + entity.getBbHeight();
                    double z = this.entity.getZ();
                    Random r = ThutCore.newRandom();
                    double i = r.nextGaussian() * size;
                    double j = r.nextGaussian() * size;
                    double k = r.nextGaussian() * size;
                    level.sendParticles(ParticleTypes.ANGRY_VILLAGER, x + i, y + j, z + k, 1, 0, 0, 0, 0);
                }
                else
                {
                    BlockState state = level.getBlockState(nextClear);

                    if (!clearer.isCreative())
                    {
                        int minSlot = 0;
                        int maxSlot = storage.getTaskInventory().getSlots();
                        if (storage.getTaskInventory() == storage.getPokeInventory())
                        {
                            minSlot = 2;
                            maxSlot = PokemobInventory.MAIN_INVENTORY_SIZE;
                        }
                        int startSlot = minSlot;
                        int endSlot = maxSlot;
                        Consumer<ItemStack> dropHandler = stack -> {
                            if (!ItemStackTools.addItemStackToInventory(stack, storage.getTaskInventory(), startSlot,
                                    endSlot))
                            {
                                double x = nextClear.getX() + 0.5;
                                double y = nextClear.getY() + 0.5;
                                double z = nextClear.getZ() + 0.5;
                                ItemEntity item = new ItemEntity(level, x, y, z, stack);
                                level.addFreshEntity(item);
                            }
                        };
                        // If we are not creative, we drop the items, and
                        // attempt to add to inventory, or drop in world
                        // otherwise.
                        final List<ItemStack> list = Block.getDrops(state, level, nextClear,
                                level.getBlockEntity(nextClear));
                        list.forEach(dropHandler);
                    }

                    // We destroy the block
                    level.destroyBlock(nextClear, false);
                    // Then remove the mutex flag for this location
                    clearer.markCleared(nextClear);
                    nextClear = null;;

                    if (!clearer.isCreative())
                    {
                        // Now we check if we should go store items or not.
                        storage.doStorageCheck(storage.getTaskInventory());
                        pokemob.setRoutineState(AIRoutine.STORE, true);
                        pokemob.setRoutineState(AIRoutine.GATHER, false);
                    }
                }
            }
            return false;
        }
        return true;
    }

    private void takeFromContainer(IItemHandlerModifiable container, List<ItemStack> requested, int minSlot,
            int maxSlot, int depth)
    {
        if (requested.isEmpty()) return;
        List<IItemHandlerModifiable> subs = new ArrayList<>();
        for (int i = 0; i < container.getSlots(); i++)
        {
            ItemStack stack = container.getStackInSlot(i);
            if (checkValid(stack, requested))
            {
                boolean collected = true;
                if (stack.getCount() > 5)
                {
                    var split = stack.split(5);
                    container.setStackInSlot(i, stack);
                    if (!ItemStackTools.addItemStackToInventory(split, storage.getTaskInventory(), minSlot, maxSlot))
                    {
                        stack.grow(5);
                        collected = false;
                    }
                }
                else
                {
                    container.setStackInSlot(i, ItemStack.EMPTY);
                    if (!ItemStackTools.addItemStackToInventory(stack, storage.getTaskInventory(), minSlot, maxSlot))
                    {
                        container.setStackInSlot(i, stack);
                        collected = false;
                    }
                }
                if (collected)
                {
                    for (var stack2 : requested) if (ItemStack.isSame(stack, stack2))
                    {
                        requested.remove(stack2);
                        if (requested.isEmpty()) return;
                        break;
                    }
                }
                continue;
            }
            var tmp = ThutCaps.getInventory(stack);
            if (tmp instanceof IItemHandlerModifiable subContainer && depth == 0)
            {
                subs.add(subContainer);
            }
        }
        for (var subContainer : subs)
        {
            takeFromContainer(subContainer, requested, minSlot, maxSlot, depth + 1);
            if (requested.isEmpty()) return;
        }
    }

    private boolean checkSupplies(ServerLevel level)
    {
//        if (entity.tickCount % 20 == 0) System.out.println("Supplies? ");
        // If we are trying to get to the place to build, first select the
        // spot.
        if (nextPlace == null)
        {
            nextPlace = builder.getNextPlacement(storage.getTaskInventory());
            if (nextPlace != null)
            {
                builder.markPendingBuild(nextPlace.info().pos);
                // Skip invalid placements directly, the tryPlace here should
                // internally mark the spot as "done"
                if (nextPlace.valid() == CanPlace.NO)
                {
                    builder.tryPlace(nextPlace, storage.getTaskInventory());
                    nextPlace = null;
                }
            }
            if (nextPlace == null) return false;
        }

        // This means we already checked, this will be unset right before
        // building to ensure we still have items.
        if (findingSpot) return true;

        // Creative always has the needed items
        if (builder.isCreative())
        {
            gettingPart = false;
            findingSpot = true;
            return true;
        }

        // Check if we still have item for next one, if so, then
        // immediately continue
        List<ItemStack> requested = new ArrayList<>();
        builder.getNextNeeded(requested, 3);

        if (nextPlace != null)
        {
            ItemStack stack = StructureTemplateTools.getForInfo(nextPlace.info());
            if (!stack.isEmpty())
            {
                boolean has = false;
                for (var stack2 : requested)
                {
                    if (ItemStack.isSame(stack2, stack))
                    {
                        has = true;
                        break;
                    }
                }
                if (!has) requested.add(0, stack);
            }
        }

        for (int i = 0; i < storage.getTaskInventory().getSlots(); i++)
        {
            ItemStack stack = storage.getTaskInventory().getStackInSlot(i);
            for (var stack2 : requested) if (ItemStack.isSame(stack, stack2))
            {
                requested.remove(stack2);
                break;
            }
        }
//        if (entity.tickCount % 20 == 0) System.out.println(requested);

        // This means we already had the next set of items on the list.
        if (requested.isEmpty())
        {
            gettingPart = false;
            findingSpot = true;
            return true;
        }

        // Otherwise we need to go collect them
        double diff = 1;
        var storeLoc = storage.storageLoc;
        diff = Math.max(diff, this.entity.getBbWidth());
        if (entity.getOnPos().distSqr(storeLoc) > diff)
        {
            this.setWalkTo(storeLoc, 1, 0);
            if (pathTimeout < 0) pathTimeout = 150;
        }

        if (pathTimeout < 20)
        {
            var pair = storage.getInventory(level, storage.storageLoc, Direction.UP);
            var container = pair.getFirst();

            int minSlot = 0;
            int maxSlot = storage.getTaskInventory().getSlots();
            if (storage.getTaskInventory() == storage.getPokeInventory())
            {
                minSlot = 2;
                maxSlot = PokemobInventory.MAIN_INVENTORY_SIZE;
            }

            if (container != null) takeFromContainer(container, requested, minSlot, maxSlot, 0);

            if (requested.size() > 0)
            {
//                System.out.println(requested);
                // need item, request it.
                builder.provideBoM(this.BoM, true);
                if (entity.tickCount % 10 == 0)
                {
                    double size = 0.1;
                    double x = this.entity.getX();
                    double y = this.entity.getY() + this.entity.getBbHeight();
                    double z = this.entity.getZ();

                    Random r = ThutCore.newRandom();
                    double i = r.nextGaussian() * size;
                    double j = r.nextGaussian() * size;
                    double k = r.nextGaussian() * size;
                    level.sendParticles(ParticleTypes.ANGRY_VILLAGER, x + i, y + j, z + k, 1, 0, 0, 0, 0);
                }
                if (storeLoc.distManhattan(entity.getOnPos()) > 3)
                {
                    // Path to it if too far.
                    setWalkTo(storeLoc, 1, 1);
                }
                else
                {
                    // Otherwise just sit down.
                    pokemob.setLogicState(LogicStates.SITTING, true);

                    // Also dump items
                    storage.doStorageCheck(storage.getTaskInventory());
                }
            }
            else
            {
                gettingPart = false;
                findingSpot = true;
                return true;
            }
        }
        return false;
    }

    private void buildBlocks(ServerLevel level)
    {
//        if (entity.tickCount % 20 == 0) System.out.println("Building");
        // This is always called after checkSupplies, which would have set this.
        if (nextPlace == null)
        {
            this.reset();
//            System.out.println("Reset :(");
            return;
        }

        // Skip invalid placements directly, the tryPlace here should internally
        // mark the spot as "done"
        if (nextPlace.valid() == CanPlace.NO)
        {
            builder.tryPlace(nextPlace, storage.getTaskInventory());
            nextPlace = null;
            return;
        }

        var pos = nextPlace.info().pos;
        double diff = 5;
        diff = Math.max(diff, this.entity.getBbWidth());
        if (entity.getOnPos().distSqr(pos) > diff)
        {
            this.setWalkTo(pos, 1, 0);
            if (pathTimeout < 0) pathTimeout = 150;
        }
        if (pathTimeout < 20 || builder.isCreative())
        {
            gettingPart = true;
            findingSpot = false;
            if (checkSupplies(level))
            {
                builder.tryPlace(nextPlace, storage.getTaskInventory());
                nextPlace = null;
            }
        }
    }

    @Override
    public void run()
    {
        var storeLoc = storage.storageLoc;
        // Only run if we actually have storage (and are server side)
        if (storeLoc == null || !(entity.level instanceof ServerLevel level)) return;

        // Refresh blueprint if needed
        if (builder != null && !builder.validBuilder()) builder.update(level);

        // If refresh failed, we are done, so reset.
        if (builder == null || !builder.validBuilder())
        {
            reset();
            return;
        }

        if (storage.pathing)
        {
//            System.out.println("Dumping items!");
//            return;
        }

        builder.markPendingBuild(storeLoc);
        if (clearer != null) clearer.markPendingClear(storeLoc);

        if (entity.tickCount % 40 == 0) builder.checkBoM(this.BoM);

        pathTimeout--;

        // first check if the blocks are clear, if so, return.
        if (!blocksClear(level))
        {
//            if (entity.tickCount % 20 == 0) System.out.println("Clearing Blocks Still");
            return;
        }

        // Check that we have needed supplies
        if (!checkSupplies(level))
        {
//            if (entity.tickCount % 20 == 0) System.out.println("Need Supplies");
            return;
        }

//        if (entity.tickCount % 20 == 0) System.out.println("Building!");
        buildBlocks(level);
    }

    @Override
    public boolean shouldRun()
    {
        return hasInstructions && pokemob.isRoutineEnabled(BuilderTasks.BUILD);
    }

    @Override
    public boolean loadThrottle()
    {
        return false;
    }
}
