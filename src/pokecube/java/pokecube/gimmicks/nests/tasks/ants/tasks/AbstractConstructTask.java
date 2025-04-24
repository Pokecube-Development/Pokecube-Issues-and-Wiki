package pokecube.gimmicks.nests.tasks.ants.tasks;

import com.google.common.collect.Maps;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.GlobalPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.level.pathfinder.PathComputationType;
import pokecube.api.PokecubeAPI;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.entity.pokemob.PokemobCaps;
import pokecube.api.entity.pokemob.ai.AIRoutine;
import pokecube.core.PokecubeCore;
import pokecube.core.ai.brain.MemoryModules;
import pokecube.core.ai.tasks.utility.StoreItems;
import pokecube.gimmicks.nests.tasks.ants.AntTasks.AntJob;
import pokecube.gimmicks.nests.tasks.ants.nest.Edge;
import pokecube.gimmicks.nests.tasks.ants.nest.Node;
import pokecube.gimmicks.nests.tasks.ants.nest.Part;
import thut.api.entity.ai.RootTask;

import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiFunction;
import java.util.function.Predicate;

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

    protected BiFunction<ServerLevel, BlockPos, Boolean> hasEmptySpace;
    protected BiFunction<ServerLevel, BlockPos, Boolean> canStand;
    protected BiFunction<ServerLevel, BlockPos, Boolean> canStandNear;

    public AbstractConstructTask(final Predicate<AntJob> job, final double range)
    {
        this(AbstractConstructTask.mems, job, range);
    }

    public AbstractConstructTask(final Map<MemoryModuleType<?>, MemoryStatus> mems, final Predicate<AntJob> job,
            final double range)
    {
        super(RootTask.merge(mems, AbstractConstructTask.mems), job);
        this.dsMax = PokecubeCore.getConfig().debug_ai ? 64 : range;
        this.ds2Max = this.dsMax * this.dsMax;

        this.canStand = (level, pos) -> PokecubeCore.getConfig().debug_ai
                || level.getBlockState(pos).canOcclude() && level.getBlockState(pos.above())
                .isPathfindable(PathComputationType.LAND);

        this.canStandNear = (level, pos) -> PokecubeCore.getConfig().debug_ai || BlockPos.betweenClosedStream(
                        pos.offset(-2, -2, -2), pos.offset(2, 2, 2))
                .anyMatch(p2 -> p2.distSqr(pos) < this.ds2Max && this.canStand.apply(level, p2));

        this.hasEmptySpace = (level, pos) -> {
            if (PokecubeCore.getConfig().debug_ai) return true;
            for (final Direction dir : Direction.values())
            {
                final BlockPos pos2 = pos.relative(dir);
                final BlockState state = level.getBlockState(pos2);
                if (state.isPathfindable(PathComputationType.LAND)) return true;
            }
            return false;
        };
    }

    @Override
    public final void reset(Mob entity)
    {
        this.progressTimer = 0;
        this.progressDistance = 0;
        this.n = null;
        this.e = null;
        this.work_pos = null;
        this.valids.set(0);
        final Brain<?> brain = entity.getBrain();
        brain.eraseMemory(MemoryModules.WORK_POS.get());
        brain.eraseMemory(MemoryModules.JOB_INFO.get());
        brain.setMemory(MemoryModules.NO_WORK_TIMER.get(), -20);
    }

    protected final void endTask(Mob entity)
    {
        if (PokecubeCore.getConfig().debug_ai) PokecubeAPI.logInfo("Need New Work Site " + this.progressTimer);
        if (this.progressTimer > 700) entity.getBrain().setMemory(MemoryModules.GOING_HOME.get(), true);
        this.reset(entity);
    }

    private boolean checkJob(IPokemob pokemob, StoreItems storage)
    {
        if (storage.firstEmpty == -1)
        {
            this.progressTimer = 1000;
            this.endTask(pokemob.getEntity());
            return false;
        }
        // First check if we have items to place, if not, go pick them up,
        // return true while doing that.
        pokemob.setRoutineState(AIRoutine.STORE, true);
        storage.storageLoc = this.nest.nest.getBlockPos();
        storage.berryLoc = this.nest.nest.getBlockPos();

        final Brain<?> brain = pokemob.getEntity().getBrain();

        boolean edge = this.e != null;
        boolean node = this.n != null;

        if (edge && this.e.getTree() == null)
        {
            PokecubeAPI.LOGGER.error("No Edge Tree! {} {}", this.job, this.e);
            this.reset(pokemob.getEntity());
            return false;
        }

        if (node && this.n.getTree() == null)
        {
            PokecubeAPI.LOGGER.error("No Node Tree!{} {}", this.job, this.n);
            this.reset(pokemob.getEntity());
            return false;
        }

        var reg = pokemob.getEntity().registryAccess();

        if (!(edge || node))
        {
            final CompoundTag tag = brain.getMemory(MemoryModules.JOB_INFO.get()).get();
            edge = tag.getString("type").equals("edge");
            node = tag.getString("type").equals("node");
            final CompoundTag data = tag.getCompound("data");
            if (edge)
            {
                this.e = new Edge();
                this.e.deserializeNBT(reg, data);
                if (this.e.node1 == null || this.e.node2 == null)
                {
                    tag.remove("type");
                    tag.remove("data");
                    PokecubeAPI.LOGGER.error("Corrupted Dig Edge Info!");
                    this.reset(pokemob.getEntity());
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
                    this.reset(pokemob.getEntity());
                    return false;
                }
            }
            if (node)
            {
                this.n = new Node();
                try
                {
                    this.n.deserializeNBT(reg, data);
                    this.n = this.nest.hab.rooms.map.get(this.n.getCenter());
                    if (this.n.getTree() == null)
                    {
                        tag.remove("type");
                        tag.remove("data");
                        PokecubeAPI.LOGGER.error("No Node Tree!");
                        this.reset(pokemob.getEntity());
                        return false;
                    }
                }
                catch (final Exception e1)
                {
                    e1.printStackTrace();
                    tag.remove("type");
                    tag.remove("data");
                    PokecubeAPI.LOGGER.error("Corrupted Dig Node Info!");
                    this.reset(pokemob.getEntity());
                    return false;
                }
            }
        }
        if (!(edge || node))
        {
            if (PokecubeCore.getConfig().debug_ai) PokecubeAPI.logInfo("Invalid Dig Info!");
            this.reset(pokemob.getEntity());
            return false;
        }
        return true;
    }

    protected abstract boolean selectJobSite(IPokemob pokemob, StoreItems storage);

    protected abstract void doWork(Mob owner);

    protected void onTimeout(final Part part, ServerLevel level)
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
    protected void tick(final ServerLevel level, final Mob entity, final long gameTime)
    {
        var storage = entity.getData(StoreItems.StoreBehaviour.TYPE);
        var pokemob = PokemobCaps.getPokemobFor(entity);
        if (!this.checkJob(pokemob, storage)) return;
        final Part part = this.e == null ? this.n : this.e;
        if (PokecubeCore.getConfig().debug_ai) pokemob.setPokemonNickname(this.job + " " + part);
        this.progressTimer++;
        if (!this.selectJobSite(pokemob, storage))
        {
            // We give up
            if (this.progressTimer > 700) this.endTask(entity);
            return;
        }

        final Brain<?> brain = entity.getBrain();
        final GlobalPos pos = GlobalPos.of(level.dimension(), this.work_pos);
        brain.setMemory(MemoryModules.WORK_POS.get(), pos);

        final Path p = entity.getNavigation().getPath();

        final double dr = this.work_pos.distSqr(entity.blockPosition());
        final double dr2 = p == null ? dr : p.getEndNode().asBlockPos().distSqr(this.work_pos);

        if (PokecubeCore.getConfig().debug_ai)
            pokemob.setPokemonNickname(this.job + " WORK! (" + dr + "/" + dr2 + ") " + this.ds2Max);

        if (dr2 > this.ds2Max) this.setWalkTo(entity, this.work_pos, 1, Mth.ceil(this.dsMax - 1));
        else if (this.progressTimer > 20) this.progressTimer = 20;

        if (this.shouldGiveUp(dr2))
        {
            this.onTimeout(part, level);
            return;
        }

        if (this.progressTimer > 0 && dr < this.ds2Max)
        {
            this.progressTimer = -10;
            this.doWork(entity);
            if (PokecubeCore.getConfig().debug_ai)
                PokecubeAPI.logInfo("Work Done! " + this.job + " " + this.n + " " + this.e);
            if (PokecubeCore.getConfig().debug_ai) pokemob.setPokemonNickname(this.job + " IDLE");
            this.work_pos = null;
            this.progressDistance = 0;
        }
    }

}
