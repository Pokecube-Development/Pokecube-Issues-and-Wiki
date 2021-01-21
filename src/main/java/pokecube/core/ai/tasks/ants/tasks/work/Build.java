package pokecube.core.ai.tasks.ants.tasks.work;

import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;

import com.google.common.collect.Maps;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.ai.brain.Brain;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.GlobalPos;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.items.IItemHandlerModifiable;
import pokecube.core.PokecubeCore;
import pokecube.core.ai.tasks.ants.AntTasks;
import pokecube.core.ai.tasks.ants.AntTasks.AntJob;
import pokecube.core.ai.tasks.ants.AntTasks.AntRoom;
import pokecube.core.ai.tasks.ants.nest.Edge;
import pokecube.core.ai.tasks.ants.nest.Node;
import pokecube.core.ai.tasks.ants.tasks.AbstractWorkTask;
import pokecube.core.interfaces.IMoveConstants.AIRoutine;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.PokecubeMod;
import pokecube.core.world.terrain.PokecubeTerrainChecker;

public class Build extends AbstractWorkTask
{
    public static interface IRoomHandler
    {
        default boolean validWall(final Node node, final ServerWorld world, final BlockPos pos,
                final AbstractWorkTask task)
        {
            final BlockState state = world.getBlockState(pos);
            final BlockPos mid = node.getCenter();
            final int dy = pos.getY() - mid.getY();
            final int dx = pos.getX() - mid.getX();
            final int dz = pos.getZ() - mid.getZ();

            final int roofY = (int) (node.inBounds.maxY + 1);

            final double dh2 = dx * dx + dz * dz;

            boolean valid = state.isOpaqueCube(world, pos) || state.getBlock() == Blocks.FARMLAND;
            switch (node.type)
            {
            case ENTRANCE:
                // Entrances keep entrances on the cardinal directions
                if ((dx == 0 || dz == 0) && dy >= 0 && dy <= 2)
                {
                    boolean edge = false;
                    for (final Edge e : node.edges)
                        if (e.isOnShell(pos))
                        {
                            edge = true;
                            break;
                        }
                    if (!edge) valid = world.isAirBlock(pos);
                }
                break;
            case FOOD:
                if (dx == 0 && dz == 0 && dy == -1) valid = world.getFluidState(pos).isTagged(FluidTags.WATER);
                else if (dy == -1 && dh2 <= 4) valid = state.getBlock() == Blocks.FARMLAND;
                break;
            default:
                break;
            }
            if (node.type != AntRoom.ENTRANCE) if (dx == 0 && dz == 0 && pos.getY() == roofY) valid = state
                    .getBlock() == Blocks.SHROOMLIGHT;
            if (!valid && task != null)
            {
                task.tryHarvest(pos, true);
                return this.validWall(node, world, pos, null);
            }
            return valid;
        }

        default void place(final Node node, final ServerWorld world, BlockState state, final BlockPos pos)
        {
            final BlockPos mid = node.getCenter();
            final int dy = pos.getY() - mid.getY();
            final int dx = pos.getX() - mid.getX();
            final int dz = pos.getZ() - mid.getZ();

            final int roofY = (int) (node.inBounds.maxY + 1);

            final double dh2 = dx * dx + dz * dz;

            switch (node.type)
            {
            case EGG:
                break;
            case ENTRANCE:
                if ((dx == 0 || dz == 0) && dy >= 0 && dy <= 2)
                {
                    boolean edge = false;
                    for (final Edge e : node.edges)
                        if (e.isOnShell(pos))
                        {
                            edge = true;
                            break;
                        }
                    if (!edge) return;
                }
                break;
            case FOOD:
                if (dx == 0 && dz == 0 && dy == -1)
                {
                    world.setBlockState(pos, Blocks.WATER.getDefaultState());
                    return;
                }
                else if (dy == -1 && dh2 <= 4)
                {
                    world.setBlockState(pos, Blocks.FARMLAND.getDefaultState());
                    return;
                }
                break;
            case NODE:
                break;
            default:
                break;
            }
            if (node.type != AntRoom.ENTRANCE) if (dx == 0 && dz == 0 && pos.getY() == roofY) state = Blocks.SHROOMLIGHT
                    .getDefaultState();
            world.setBlockState(pos, state);
        }
    }

    private static final IRoomHandler DEFAULT = new IRoomHandler()
    {
    };

    public static final Map<AntRoom, IRoomHandler> ROOMHANLDERS = Maps.newHashMap();

    ItemStack to_place = ItemStack.EMPTY;
    int       storeInd = -1;

    int build_timer = 0;

    Node n = null;
    Edge e = null;

    BlockPos build_pos = null;

    boolean going_to_nest = false;

    Predicate<BlockPos> canStand = pos -> BlockPos.getAllInBox(pos.add(-1, -2, -1), pos.add(1, 0, 1)).anyMatch(
            p -> this.world.getBlockState(p).isSolid());

    public Build(final IPokemob pokemob)
    {
        super(pokemob, j -> j == AntJob.BUILD);
    }

    @Override
    public void reset()
    {
        this.to_place = ItemStack.EMPTY;
        this.going_to_nest = false;
        this.storeInd = -1;
        this.build_timer = 0;
        this.n = null;
        this.e = null;
        this.build_pos = null;
        final Brain<?> brain = this.entity.getBrain();
        brain.removeMemory(AntTasks.WORK_POS);
        brain.removeMemory(AntTasks.JOB_INFO);
        brain.setMemory(AntTasks.NO_WORK_TIME, -20);
    }

    private void endTask(final boolean completed)
    {
        if (PokecubeMod.debug) PokecubeCore.LOGGER.debug("Need New Build Site");
        if (this.build_timer > 400) this.entity.getBrain().setMemory(AntTasks.GOING_HOME, true);
        if (completed)
        {
            if (this.n != null) this.n.build_done = this.world.getGameTime() + 2400;
            if (this.e != null) this.e.build_done = this.world.getGameTime() + 2400;
        }
        this.reset();
    }

    private boolean checkJob()
    {
        final Brain<?> brain = this.entity.getBrain();

        boolean edge = this.e != null;
        boolean node = this.n != null;

        if (edge && this.e.getTree() == null)
        {
            PokecubeCore.LOGGER.error("No Edge Tree!");
            this.reset();
            return false;
        }

        if (node && this.n.getTree() == null)
        {
            PokecubeCore.LOGGER.error("No Node Tree!");
            this.reset();
            return false;
        }

        if (!(edge || node))
        {
            final CompoundNBT tag = brain.getMemory(AntTasks.JOB_INFO).get();
            edge = tag.getString("type").equals("edge");
            node = tag.getString("type").equals("node");
            final CompoundNBT data = tag.getCompound("data");
            if (edge)
            {
                this.e = new Edge();
                this.e.deserializeNBT(data);
                if (this.e.node1 == null || this.e.node2 == null)
                {
                    tag.remove("type");
                    tag.remove("data");
                    PokecubeCore.LOGGER.error("Corrupted Dig Edge Info!");
                    this.reset();
                    return false;
                }
                this.e.node1 = this.nest.hab.rooms.map.get(this.e.node1.getCenter());
                this.e.node2 = this.nest.hab.rooms.map.get(this.e.node2.getCenter());
                this.e.setTree(this.e.node1.getTree());
                if (this.e.getTree() == null)
                {
                    tag.remove("type");
                    tag.remove("data");
                    PokecubeCore.LOGGER.error("No Edge Tree!");
                    this.reset();
                    return false;
                }
            }
            if (node)
            {
                this.n = new Node();
                try
                {
                    this.n.deserializeNBT(data);
                    this.n = this.nest.hab.rooms.map.get(this.n.getCenter());
                    if (this.n.getTree() == null)
                    {
                        tag.remove("type");
                        tag.remove("data");
                        PokecubeCore.LOGGER.error("No Node Tree!");
                        this.reset();
                        return false;
                    }
                }
                catch (final Exception e1)
                {
                    e1.printStackTrace();
                    tag.remove("type");
                    tag.remove("data");
                    PokecubeCore.LOGGER.error("Corrupted Dig Node Info!");
                    this.reset();
                    return false;
                }
            }
        }
        if (!(edge || node))
        {
            PokecubeCore.LOGGER.debug("Invalid Dig Info!");
            this.reset();
            return false;
        }
        return true;
    }

    private boolean selectJobSite()
    {
        final Brain<?> brain = this.entity.getBrain();
        final Optional<GlobalPos> room = brain.getMemory(AntTasks.WORK_POS);
        final long time = this.world.getGameTime();
        select:
        if (this.build_pos == null)
        {
            final boolean edge = this.e != null;
            if (edge)
            {
                if (!this.e.shouldBuild(time))
                {
                    if (this.e.node1.shouldBuild(time))
                    {
                        this.n = this.e.node1;
                        this.e = null;
                        return false;
                    }
                    if (this.e.node2.shouldBuild(time))
                    {
                        this.n = this.e.node2;
                        this.e = null;
                        return false;
                    }
                    this.endTask(false);
                    return false;
                }
                final Node node = this.e.node1;
                final AntRoom type = this.n == null ? AntRoom.NODE : this.n.type;
                final IRoomHandler handler = Build.ROOMHANLDERS.getOrDefault(type, Build.DEFAULT);
                final AxisAlignedBB box = new AxisAlignedBB(this.e.end1, this.e.end2).grow(1);
                // Start with a check of if the pos is inside.
                Predicate<BlockPos> isValid = pos -> this.e.getTree().isOnShell(pos);
                // If it is inside, and not diggable, we notify the node of the
                // dug spot, finally we check if there is space nearby to stand.
                isValid = isValid.and(pos -> !handler.validWall(node, this.world, pos, this)).and(this.canStand);
                final Optional<BlockPos> pos = BlockPos.getAllInBox(box).filter(isValid).findAny();
                if (pos.isPresent())
                {
                    this.build_pos = pos.get();
                    this.build_timer = -600;
                    break select;
                }
                else this.e.build_done = time + 2400;
            }
            else
            {
                if (!this.n.shouldBuild(time))
                {
                    for (final Edge e : this.n.edges)
                        if (e.shouldBuild(time))
                        {
                            this.e = e;
                            this.n = null;
                            return false;
                        }
                    this.endTask(false);
                    return false;
                }
                final IRoomHandler handler = Build.ROOMHANLDERS.getOrDefault(this.n.type, Build.DEFAULT);
                final AxisAlignedBB box = this.n.outBounds;
                // Start with a check of if the pos is inside.
                Predicate<BlockPos> isValid = pos -> this.n.getTree().isOnShell(pos);
                // If it is inside, and not diggable, we notify the node of the
                // dug spot, finally we check if there is space nearby to stand.
                isValid = isValid.and(pos -> !handler.validWall(this.n, this.world, pos, this)).and(this.canStand);

                final Optional<BlockPos> pos = BlockPos.getAllInBox(box).filter(isValid).findAny();
                if (pos.isPresent())
                {
                    this.build_pos = pos.get();
                    this.build_timer = -600;
                    break select;
                }
                else this.n.build_done = time + 2400;
            }
        }
        this.build_timer++;
        if (this.build_pos == null)
        {
            this.setWalkTo(room.get().getPos(), 1, 1);
            // If we took too long. lets give up
            if (this.build_timer > 600) this.endTask(true);
        }
        return this.build_pos != null;
    }

    @Override
    public void run()
    {
        if (this.storage.firstEmpty == -1) return;

        if (!this.checkJob()) return;
        if (!this.selectJobSite()) return;

        this.pokemob.setRoutineState(AIRoutine.STORE, true);
        this.storage.storageLoc = this.nest.nest.getPos();
        this.storage.berryLoc = this.nest.nest.getPos();

        // First. we need to check our own inventory, see if we have any blocks.
        // If so, we will place one of those. Otherwise, we need to go to the
        // nest, and pick one up from there.
        if (this.to_place.isEmpty())
        {
            if (this.going_to_nest)
            {
                final BlockPos pos = this.nest.nest.getPos();
                if (pos.distanceSq(this.entity.getPosition()) > 9) this.setWalkTo(pos, 1, 1);
                else
                {
                    final IItemHandlerModifiable inv = this.storage.getInventory(this.world, pos,
                            this.storage.storageFace);
                    boolean found = false;
                    for (int i = 2; i < this.pokemob.getInventory().getSizeInventory(); i++)
                    {

                        final ItemStack stack = this.pokemob.getInventory().getStackInSlot(i);
                        if (!stack.isEmpty() && stack.getItem() instanceof BlockItem)
                        {
                            final BlockItem item = (BlockItem) stack.getItem();
                            if (!PokecubeTerrainChecker.isTerrain(item.getBlock().getDefaultState())) continue;
                            this.storeInd = i;
                            this.to_place = stack;
                            found = true;
                        }
                    }
                    if (!found)
                    {
                        for (int i = 0; i < inv.getSlots(); i++)
                        {
                            final ItemStack stack = inv.getStackInSlot(i);
                            if (!stack.isEmpty() && stack.getItem() instanceof BlockItem)
                            {
                                final BlockItem item = (BlockItem) stack.getItem();
                                if (!PokecubeTerrainChecker.isTerrain(item.getBlock().getDefaultState())) continue;
                                this.to_place = inv.extractItem(i, Math.min(stack.getCount(), 5), false);
                                this.storeInd = this.storage.firstEmpty;
                                this.pokemob.getInventory().setInventorySlotContents(this.storage.firstEmpty,
                                        this.to_place);
                                return;
                            }
                        }
                        this.to_place = new ItemStack(Blocks.PODZOL, 5);
                        this.storeInd = this.storage.firstEmpty;
                        this.pokemob.getInventory().setInventorySlotContents(this.storage.firstEmpty, this.to_place);
                    }
                }
            }
            this.going_to_nest = this.to_place.isEmpty();
            if (this.going_to_nest) return;
        }
        final BlockPos pos = this.build_pos;
        if (pos.distanceSq(this.entity.getPosition()) > 10) this.setWalkTo(pos, 1, 1);
        if (!this.to_place.isEmpty() && this.to_place.getItem() instanceof BlockItem && this.storeInd != -1)
        {
            final BlockItem item = (BlockItem) this.to_place.getItem();
            final BlockState state = item.getBlock().getDefaultState();
            if (this.n != null)
            {
                final IRoomHandler handler = Build.ROOMHANLDERS.getOrDefault(this.n.type, Build.DEFAULT);
                if (!handler.validWall(this.n, this.world, pos, this))
                {
                    handler.place(this.n, this.world, state, pos);
                    if (!this.world.isAirBlock(pos)) this.to_place.shrink(1);
                }
            }
            else
            {
                this.world.setBlockState(pos, state);
                if (!this.world.isAirBlock(pos)) this.to_place.shrink(1);
            }
            this.pokemob.getInventory().setInventorySlotContents(this.storeInd, this.to_place);
        }
        this.build_pos = null;
    }
}
