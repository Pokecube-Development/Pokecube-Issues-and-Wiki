package pokecube.gimmicks.nests.tasks.ants.tasks;

import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;

import com.google.common.collect.Maps;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.GlobalPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.level.pathfinder.PathComputationType;
import pokecube.api.PokecubeAPI;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.entity.pokemob.ai.AIRoutine;
import pokecube.core.PokecubeCore;
import pokecube.core.ai.brain.MemoryModules;
import pokecube.gimmicks.nests.tasks.ants.AntTasks.AntJob;
import pokecube.gimmicks.nests.tasks.ants.nest.Edge;
import pokecube.gimmicks.nests.tasks.ants.nest.Node;
import pokecube.gimmicks.nests.tasks.ants.nest.Part;
import thut.api.entity.ai.RootTask;

public abstract class AbstractConstructTask extends AbstractWorkTask
{
    private static final Map<MemoryModuleType<?>, MemoryStatus> mems = Maps.newHashMap();
    static
    {
        AbstractConstructTask.mems.put(MemoryModules.JOB_INFO.get(), MemoryStatus.VALUE_PRESENT);
    }

    protected int progressTimer = 0;
    protected int progressDistance = 0;

    protected Node n = null;
    protected Edge e = null;

    protected BlockPos work_pos = null;

    final double ds2Max;
    final double dsMax;

    protected final AtomicInteger valids = new AtomicInteger(0);

    protected Predicate<BlockPos> hasEmptySpace;
    protected Predicate<BlockPos> canStand;
    protected Predicate<BlockPos> canStandNear;

    public AbstractConstructTask(final IPokemob pokemob, final Predicate<AntJob> job, final double range)
    {
        this(pokemob, AbstractConstructTask.mems, job, range);
    }

    public AbstractConstructTask(final IPokemob pokemob, final Map<MemoryModuleType<?>, MemoryStatus> mems,
            final Predicate<AntJob> job, final double range)
    {
        super(pokemob, RootTask.merge(mems, AbstractConstructTask.mems), job);
        this.dsMax = PokecubeCore.getConfig().debug_ai ? 64 : range;
        this.ds2Max = this.dsMax * this.dsMax;

        this.canStand = p -> PokecubeCore.getConfig().debug_ai || this.world.getBlockState(p).canOcclude()
                && this.world.getBlockState(p.above()).isPathfindable(this.world, p, PathComputationType.LAND);

        this.canStandNear = pos -> PokecubeCore.getConfig().debug_ai
                || BlockPos.betweenClosedStream(pos.offset(-2, -2, -2), pos.offset(2, 2, 2))
                        .anyMatch(p2 -> p2.distSqr(pos) < this.ds2Max && this.canStand.test(p2));

        this.hasEmptySpace = pos -> {
            if (PokecubeCore.getConfig().debug_ai) return true;
            for (final Direction dir : Direction.values())
            {
                final BlockPos pos2 = pos.relative(dir);
                final BlockState state = this.world.getBlockState(pos2);
                if (state.isPathfindable(this.world, pos2, PathComputationType.LAND)) return true;
            }
            return false;
        };
    }

    @Override
    public final void reset()
    {
        this.progressTimer = 0;
        this.progressDistance = 0;
        this.n = null;
        this.e = null;
        this.work_pos = null;
        this.valids.set(0);
        final Brain<?> brain = this.entity.getBrain();
        brain.eraseMemory(MemoryModules.WORK_POS.get());
        brain.eraseMemory(MemoryModules.JOB_INFO.get());
        brain.setMemory(MemoryModules.NO_WORK_TIMER.get(), -20);
    }

    protected final void endTask()
    {
        if (PokecubeCore.getConfig().debug_ai) PokecubeAPI.logInfo("Need New Work Site " + this.progressTimer);
        if (this.progressTimer > 700) this.entity.getBrain().setMemory(MemoryModules.GOING_HOME.get(), true);
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
        this.storage.storageLoc = this.nest.nest.getBlockPos();
        this.storage.berryLoc = this.nest.nest.getBlockPos();

        final Brain<?> brain = this.entity.getBrain();

        boolean edge = this.e != null;
        boolean node = this.n != null;

        if (edge && this.e.getTree() == null)
        {
            PokecubeAPI.LOGGER.error("No Edge Tree! " + this.job + " " + this.e);
            this.reset();
            return false;
        }

        if (node && this.n.getTree() == null)
        {
            PokecubeAPI.LOGGER.error("No Node Tree!" + this.job + " " + this.n);
            this.reset();
            return false;
        }

        if (!(edge || node))
        {
            final CompoundTag tag = brain.getMemory(MemoryModules.JOB_INFO.get()).get();
            edge = tag.getString("type").equals("edge");
            node = tag.getString("type").equals("node");
            final CompoundTag data = tag.getCompound("data");
            if (edge)
            {
                this.e = new Edge();
                this.e.deserializeNBT(data);
                if (this.e.node1 == null || this.e.node2 == null)
                {
                    tag.remove("type");
                    tag.remove("data");
                    PokecubeAPI.LOGGER.error("Corrupted Dig Edge Info!");
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
                    PokecubeAPI.LOGGER.error("No Edge Tree!");
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
                        PokecubeAPI.LOGGER.error("No Node Tree!");
                        this.reset();
                        return false;
                    }
                }
                catch (final Exception e1)
                {
                    e1.printStackTrace();
                    tag.remove("type");
                    tag.remove("data");
                    PokecubeAPI.LOGGER.error("Corrupted Dig Node Info!");
                    this.reset();
                    return false;
                }
            }
        }
        if (!(edge || node))
        {
            if (PokecubeCore.getConfig().debug_ai) PokecubeAPI.logInfo("Invalid Dig Info!");
            this.reset();
            return false;
        }
        return true;
    }

    protected abstract boolean selectJobSite();

    protected abstract void doWork();

    protected void onTimeout(final Part part)
    {
        // final List<Node> entrances =
        // this.nest.hab.getRooms(AntRoom.ENTRANCE);
        // if (entrances.isEmpty()) return;
        //
        // final Node room = entrances.get(0);
        // this.setWalkTo(room.getCenter(), 1, 2);

        this.work_pos = null;
        this.valids.set(0);
        this.progressTimer = 0;
    }

    protected boolean shouldGiveUp(final double pathDistFromEnd)
    {
        return false;
    }

    @Override
    public final void run()
    {
        if (!this.checkJob()) return;
        final Part part = this.e == null ? this.n : this.e;
        if (PokecubeCore.getConfig().debug_ai) this.pokemob.setPokemonNickname(this.job + " " + part);
        this.progressTimer++;
        if (!this.selectJobSite())
        {
            // We give up
            if (this.progressTimer > 700) this.endTask();
            return;
        }

        final Brain<?> brain = this.entity.getBrain();
        final GlobalPos pos = GlobalPos.of(this.world.dimension(), this.work_pos);
        brain.setMemory(MemoryModules.WORK_POS.get(), pos);

        final Path p = this.entity.getNavigation().getPath();

        final double dr = this.work_pos.distSqr(this.entity.blockPosition());
        final double dr2 = p == null ? dr : p.getEndNode().asBlockPos().distSqr(this.work_pos);

        if (PokecubeCore.getConfig().debug_ai)
            this.pokemob.setPokemonNickname(this.job + " WORK! (" + dr + "/" + dr2 + ") " + this.ds2Max);

        if (dr2 > this.ds2Max) this.setWalkTo(this.work_pos, 1, Mth.ceil(this.dsMax - 1));
        else if (this.progressTimer > 20) this.progressTimer = 20;

        if (this.shouldGiveUp(dr2))
        {
            this.onTimeout(part);
            return;
        }

        if (this.progressTimer > 0 && dr < this.ds2Max)
        {
            this.progressTimer = -10;
            this.doWork();
            if (PokecubeCore.getConfig().debug_ai)
                PokecubeAPI.logInfo("Work Done! " + this.job + " " + this.n + " " + this.e);
            if (PokecubeCore.getConfig().debug_ai) this.pokemob.setPokemonNickname(this.job + " IDLE");
            this.work_pos = null;
            this.progressDistance = 0;
        }
    }

}
