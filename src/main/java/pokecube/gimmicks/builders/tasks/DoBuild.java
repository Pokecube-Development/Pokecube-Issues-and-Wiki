package pokecube.gimmicks.builders.tasks;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap.Types;
import net.minecraftforge.items.IItemHandlerModifiable;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.core.ai.tasks.utility.StoreTask;
import pokecube.core.ai.tasks.utility.UtilTask;
import pokecube.gimmicks.builders.builders.StructureBuilder;
import pokecube.gimmicks.builders.builders.StructureBuilder.PlaceInfo;
import thut.api.maths.Vector3;
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
    StructureBuilder builder;

    boolean findingSpot = false;
    boolean gettingPart = false;

    boolean makeBorder = false;

    PlaceInfo nextPlace = null;
    BlockPos nextClear = null;
    int pathTimeout = -1;
    int last_check = -1;

    Vector3 seeking = new Vector3();

    Vector3 v = new Vector3();
    Vector3 v1 = new Vector3();

    List<Integer> ys = null;

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
        ys = null;
        makeBorder = true;
    }

    private boolean checkValid(ItemStack stack, List<ItemStack> requested)
    {
        for (ItemStack v : requested) if (ItemStack.isSameItem(v, stack)) return true;
        return false;
    }

    public void setBuilder(StructureBuilder builder, ServerLevel level)
    {
        var pair = storage.getInventory(level, storage.storageLoc, Direction.UP);
        if (pair == null)
        {
            reset();
            return;
        }
        builder.checkBlueprint(level);
        builder.provideBoM();
        this.builder = builder;
        ys = new ArrayList<>(builder.removeOrder.keySet());
        last_check = builder.passes;
        hasInstructions = true;
        makeBorder = true;
        builder.creative = pokemob.getOwner() instanceof ServerPlayer player && player.isCreative();
        if (builder.done) reset();
    }

    @Override
    public void run()
    {
        var storeLoc = storage.storageLoc;
        if (storeLoc == null || !(entity.level instanceof ServerLevel level)) return;

        if (builder != null && (builder._template == null || builder.done)) builder.checkBlueprint(level);

        if (builder == null || builder._template == null)
        {
            reset();
            return;
        }

        if (last_check != builder.passes)
        {
            ys = new ArrayList<>(builder.removeOrder.keySet());
            last_check = builder.passes;
        }

        if (entity.tickCount % 40 == 0) builder.checkBoM();

        IItemHandlerModifiable itemhandler = builder.itemSource;

        if (nextClear == null) nextClear = builder.nextRemoval(ys, level);
        boolean isClear = nextClear == null;
        pathTimeout--;

        if (!isClear)
        {
            double diff = 5;
            diff = Math.max(diff, this.entity.getBbWidth());
            if (entity.getOnPos().distSqr(nextClear) > diff)
            {
                this.setWalkTo(nextClear, 1, 0);
                if (pathTimeout < 0) pathTimeout = 40;
                builder.pendingClear.add(nextClear);
            }
            if (pathTimeout < 20 || builder.creative)
            {
                if (!storage.canBreak(level, nextClear))
                {
                    // Notify that we can't actually break this.
                    double size = pokemob.getMobSizes().mag();
                    double x = this.entity.getX();
                    double y = this.entity.getY();
                    double z = this.entity.getZ();

                    Random r = ThutCore.newRandom();
                    for (int l = 0; l < 2; l++)
                    {
                        double i = r.nextGaussian() * size;
                        double j = r.nextGaussian() * size;
                        double k = r.nextGaussian() * size;
                        level.sendParticles(ParticleTypes.ANGRY_VILLAGER, x + i, y + j, z + k, 1, 0, 0, 0, 0);
                    }
                }
                else
                {
                    BlockState state = level.getBlockState(nextClear);
                    final List<ItemStack> list = Block.getDrops(state, level, nextClear,
                            level.getBlockEntity(nextClear));
                    list.removeIf(stack -> ItemStackTools.addItemStackToInventory(stack, builder.itemSource, 1));
                    list.forEach(c -> {
                        int x = nextClear.getX();
                        int z = nextClear.getZ();
                        ItemEntity item = new ItemEntity(level, x + 0.5, nextClear.getY() + 0.5, z + 0.5, c);
                        level.addFreshEntity(item);
                    });
                    level.destroyBlock(nextClear, false);
                    builder.pendingClear.remove(nextClear);
                    nextClear = null;

                    // TODO ensure this walks to storage first, and only when
                    // nearly full.
                    storage.doStorageCheck(itemhandler);
                }
            }
        }
        else if (makeBorder)
        {
            var origin = builder.origin;
            var size = builder._template.getSize();

            // Mark corners
            BlockPos p1 = origin.offset(-1, 0, -1);
            BlockPos p2 = origin.offset(size.getX() + 1, 0, size.getZ() + 1);
            BlockPos p3 = origin.offset(size.getX() + 1, 0, -1);
            BlockPos p4 = origin.offset(-1, 0, size.getZ() + 1);

            level.setBlockAndUpdate(level.getHeightmapPos(Types.MOTION_BLOCKING, p1), Blocks.TORCH.defaultBlockState());
            level.setBlockAndUpdate(level.getHeightmapPos(Types.MOTION_BLOCKING, p2), Blocks.TORCH.defaultBlockState());
            level.setBlockAndUpdate(level.getHeightmapPos(Types.MOTION_BLOCKING, p3), Blocks.TORCH.defaultBlockState());
            level.setBlockAndUpdate(level.getHeightmapPos(Types.MOTION_BLOCKING, p4), Blocks.TORCH.defaultBlockState());

            makeBorder = false;
        }
        else if (findingSpot)
        {
            if (nextPlace == null)
            {
                nextPlace = builder.getNextPlacement(level);
                if (nextPlace != null) builder.pendingBuild.add(nextPlace.info().pos());
            }
            if (nextPlace != null)
            {
                var pos = nextPlace.info().pos();
                double diff = 5;
                diff = Math.max(diff, this.entity.getBbWidth());
                if (entity.getOnPos().distSqr(pos) > diff)
                {
                    this.setWalkTo(pos, 1, 0);
                    if (pathTimeout < 0) pathTimeout = 40;
                }
                if (pathTimeout < 20 || builder.creative)
                {
                    builder.tryPlace(nextPlace, level);
                    nextPlace = null;

                    gettingPart = true;
                    findingSpot = false;

                    // Check if we still have item for next one, if so, then
                    // immediately continue
                    List<ItemStack> requested = new ArrayList<>();
                    int n = 0;
                    needed_check:
                    for (int i = 0, max = builder.placeOrder.size(); i < max; i++)
                    {
                        ItemStack needed = builder.neededItems.get(builder.placeOrder.get(i).pos());
                        if (needed == null || needed.isEmpty()) continue;
                        if (++n > 3) break;
                        for (var stack : requested) if (ItemStack.isSameItem(stack, needed)) continue needed_check;
                        needed = needed.copy();
                        needed.setCount(Math.min(5, needed.getCount()));
                        requested.add(needed);
                    }

                    for (int i = 2; i < itemhandler.getSlots(); i++)
                    {
                        ItemStack stack = itemhandler.getStackInSlot(i);
                        for (var stack2 : requested) if (ItemStack.isSameItem(stack, stack2))
                        {
                            requested.remove(stack2);
                            break;
                        }
                    }

                    if (requested.isEmpty())
                    {
                        gettingPart = false;
                        findingSpot = true;
                    }
                }
            }
            else
            {
                if (builder.passes++ < 3)
                {
                    builder._template = null;
                }
                else reset();
            }
        }
        else if (gettingPart)
        {
            if (builder.creative)
            {
                gettingPart = false;
                findingSpot = true;
                return;
            }

            double diff = 1;
            diff = Math.max(diff, this.entity.getBbWidth());
            if (entity.getOnPos().distSqr(storeLoc) > diff)
            {
                this.setWalkTo(storeLoc, 1, 0);
                if (pathTimeout < 0) pathTimeout = 40;
            }
            if (pathTimeout < 20)
            {
                var pair = storage.getInventory(level, storage.storageLoc, Direction.UP);
                int bak = storage.emptySlots;
                storage.emptySlots = 0;
                storage.doStorageCheck(itemhandler);
                storage.emptySlots = bak;
                var container = pair.getFirst();

                List<ItemStack> requested = new ArrayList<>();
                int n = 0;
                needed_check:
                for (int i = 0, max = builder.placeOrder.size(); i < max; i++)
                {
                    ItemStack needed = builder.neededItems.get(builder.placeOrder.get(i).pos());
                    if (needed == null || needed.isEmpty()) continue;
                    if (++n > 3) break;
                    for (var stack : requested) if (ItemStack.isSameItem(stack, needed)) continue needed_check;
                    needed = needed.copy();
                    needed.setCount(Math.min(5, needed.getCount()));
                    requested.add(needed);
                }

                if (container != null) for (int i = 0; i < container.getSlots(); i++)
                {
                    ItemStack stack = container.getStackInSlot(i);
                    if (checkValid(stack, requested))
                    {
                        for (var stack2 : requested) if (ItemStack.isSameItem(stack, stack2))
                        {
                            requested.remove(stack2);
                            break;
                        }
                        container.setStackInSlot(i, ItemStack.EMPTY);
                        ItemStackTools.addItemStackToInventory(stack, itemhandler, 2);
                    }
                }
                if (requested.size() > 0)
                {
                    // need item, request it.
                    builder.provideBoM();

                    double size = pokemob.getMobSizes().mag();
                    double x = this.entity.getX();
                    double y = this.entity.getY();
                    double z = this.entity.getZ();

                    Random r = ThutCore.newRandom();
                    for (int l = 0; l < 2; l++)
                    {
                        double i = r.nextGaussian() * size;
                        double j = r.nextGaussian() * size;
                        double k = r.nextGaussian() * size;
                        level.sendParticles(ParticleTypes.ANGRY_VILLAGER, x + i, y + j, z + k, 1, 0, 0, 0, 0);
                    }
                }
                else
                {
                    gettingPart = false;
                    findingSpot = true;
                }
            }
        }
        else
        {
            gettingPart = true;
        }
    }

    @Override
    public boolean shouldRun()
    {
        return hasInstructions;
    }

    @Override
    public boolean loadThrottle()
    {
        return false;
    }
}
