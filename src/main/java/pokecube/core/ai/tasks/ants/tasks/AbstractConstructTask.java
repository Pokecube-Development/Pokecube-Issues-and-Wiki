package pokecube.core.ai.tasks.ants.tasks;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;

import com.google.common.collect.Maps;

import net.minecraft.block.BlockState;
import net.minecraft.entity.ai.brain.Brain;
import net.minecraft.entity.ai.brain.memory.MemoryModuleStatus;
import net.minecraft.entity.ai.brain.memory.MemoryModuleType;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.pathfinding.Path;
import net.minecraft.pathfinding.PathType;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.GlobalPos;
import pokecube.core.PokecubeCore;
import pokecube.core.ai.brain.RootTask;
import pokecube.core.ai.tasks.ants.AntTasks;
import pokecube.core.ai.tasks.ants.AntTasks.AntJob;
import pokecube.core.ai.tasks.ants.nest.Edge;
import pokecube.core.ai.tasks.ants.nest.Node;
import pokecube.core.ai.tasks.ants.nest.Part;
import pokecube.core.interfaces.IMoveConstants.AIRoutine;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.PokecubeMod;

public abstract class AbstractConstructTask extends AbstractWorkTask
{
    private static final Map<MemoryModuleType<?>, MemoryModuleStatus> mems = Maps.newHashMap();
    static
    {
        AbstractConstructTask.mems.put(AntTasks.JOB_INFO, MemoryModuleStatus.VALUE_PRESENT);
    }

    protected int progressTimer    = 0;
    protected int progressDistance = 0;

    protected Node n = null;
    protected Edge e = null;

    protected BlockPos work_pos  = null;
    protected BlockPos stand_pos = null;

    protected final AtomicInteger valids = new AtomicInteger(0);

    protected Predicate<BlockPos> hasEmptySpace = pos ->
    {
        for (final Direction dir : Direction.values())
        {
            final BlockPos pos2 = pos.offset(dir);
            final BlockState state = this.world.getBlockState(pos2);
            if (state.allowsMovement(this.world, pos2, PathType.LAND)) return true;
        }
        return false;
    };

    protected Predicate<BlockPos> canStand = p -> this.world.getBlockState(p).isSolid() && this.world.getBlockState(p
            .up()).allowsMovement(this.world, p, PathType.LAND);

    protected Predicate<BlockPos> canStandNear = pos -> BlockPos.getAllInBox(pos.add(-2, -2, -2), pos.add(2, 2, 2))
            .anyMatch(p2 -> p2.distanceSq(pos) < 9 && this.canStand.test(p2));;

    public AbstractConstructTask(final IPokemob pokemob, final Predicate<AntJob> job)
    {
        this(pokemob, AbstractConstructTask.mems, job);
    }

    public AbstractConstructTask(final IPokemob pokemob, final Map<MemoryModuleType<?>, MemoryModuleStatus> mems,
            final Predicate<AntJob> job)
    {
        super(pokemob, RootTask.merge(mems, AbstractConstructTask.mems), job);
    }

    @Override
    public final void reset()
    {
        this.progressTimer = 0;
        this.progressDistance = 0;
        this.n = null;
        this.e = null;
        this.work_pos = null;
        this.stand_pos = null;
        this.valids.set(0);
        final Brain<?> brain = this.entity.getBrain();
        brain.removeMemory(AntTasks.WORK_POS);
        brain.removeMemory(AntTasks.JOB_INFO);
        brain.setMemory(AntTasks.NO_WORK_TIME, -20);
    }

    protected final void endTask()
    {
        if (PokecubeMod.debug) PokecubeCore.LOGGER.debug("Need New Work Site " + this.progressTimer);
        if (this.progressTimer > 700) this.entity.getBrain().setMemory(AntTasks.GOING_HOME, true);
        this.reset();
    }

    private final boolean checkJob()
    {
        if (this.storage.firstEmpty == -1)
        {
            this.progressTimer = 1000;
            this.endTask();
            return false;
        }
        // First check if we have items to place, if not, go pick them up,
        // return true while doing that.
        this.pokemob.setRoutineState(AIRoutine.STORE, true);
        this.storage.storageLoc = this.nest.nest.getPos();
        this.storage.berryLoc = this.nest.nest.getPos();

        final Brain<?> brain = this.entity.getBrain();

        boolean edge = this.e != null;
        boolean node = this.n != null;

        if (edge && this.e.getTree() == null)
        {
            PokecubeCore.LOGGER.error("No Edge Tree! " + this.job + " " + this.e);
            this.reset();
            return false;
        }

        if (node && this.n.getTree() == null)
        {
            PokecubeCore.LOGGER.error("No Node Tree!" + this.job + " " + this.n);
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

    protected abstract boolean selectJobSite();

    protected abstract void doWork();

    @Override
    public final void run()
    {
        if (!this.checkJob()) return;
        final Part part = this.e == null ? this.n : this.e;
        if (PokecubeMod.debug) this.pokemob.setPokemonNickname(this.job + " " + part);
        this.progressTimer++;
        if (!this.selectJobSite())
        {
            // We give up
            if (this.progressTimer > 700) this.endTask();
            return;
        }

        final Brain<?> brain = this.entity.getBrain();
        final GlobalPos pos = GlobalPos.getPosition(this.world.getDimensionKey(), this.work_pos);
        brain.setMemory(AntTasks.WORK_POS, pos);

        final Path p = this.entity.getNavigator().getPath();

        if (this.stand_pos == null || this.stand_pos.distanceSq(this.work_pos) > 9)
        {
            final Optional<BlockPos> test = BlockPos.getAllInBox(this.work_pos.add(-2, -2, -2), this.work_pos.add(2, 2,
                    2)).filter(p2 -> p2.distanceSq(this.work_pos) < 9 && this.canStand.test(p2)).findAny();
            if (test.isPresent()) this.stand_pos = test.get().toImmutable();
            else this.stand_pos = this.work_pos;
            if (this.stand_pos == this.work_pos) PokecubeCore.LOGGER.warn("Invalid stand pos! " + this.job);
        }
        final double dr = this.stand_pos.distanceSq(this.entity.getPosition());
        final double dr2 = p == null ? dr : p.getFinalPathPoint().func_224759_a().distanceSq(this.stand_pos);

        if (PokecubeMod.debug) this.pokemob.setPokemonNickname(this.job + " WORK! (" + dr + "/" + dr2 + ") "
                + this.stand_pos);

        this.setWalkTo(this.stand_pos, 1, 2);
        if (dr > 9 && this.progressTimer > 400 && this.entity.getNavigator().hasPath())
        {
            final BlockPos targ = p.getTarget();
            final BlockPos end = p.getFinalPathPoint().func_224759_a();
            final int dist = end.manhattanDistance(targ);
            if (dist > 9)
            {
                BlockPos p2 = null;
                if (this.n != null)
                {
                    p2 = this.n.getCenter();
                    for (final Edge e : this.n.edges)
                    {
                        if (e.started && e.node1 == this.n)
                        {
                            p2 = e.getEnd1();
                            break;
                        }
                        if (e.started && e.node2 == this.n)
                        {
                            p2 = e.getEnd2();
                            break;
                        }
                    }
                }
                else if (this.e != null) if (this.e.node1.started) p2 = this.e.getEnd1();
                else if (this.e.node2.started) p2 = this.e.getEnd2();
                if (p2 != null)
                {
                    this.setWalkTo(p2, 1, 1);
                    this.entity.getBrain().setMemory(AntTasks.WORK_POS, GlobalPos.getPosition(this.world
                            .getDimensionKey(), p2));
                    if (this.progressTimer > 600)
                    {
                        this.entity.setPosition(p2.getX(), p2.getY(), p2.getZ());
                        this.progressTimer = 0;
                    }
                }
            }
            if (this.progressDistance == 0) this.progressDistance = dist;
        }

        final double dsMax = 16;

        if (this.progressTimer > 0 && dr < dsMax)
        {
            this.progressTimer = -10;
            this.doWork();
            if (PokecubeMod.debug) PokecubeCore.LOGGER.debug("Work Done! " + this.job);
            this.pokemob.setPokemonNickname(this.job + " IDLE");
            this.work_pos = null;
            this.stand_pos = null;
            this.progressDistance = 0;
        }
    }

}
