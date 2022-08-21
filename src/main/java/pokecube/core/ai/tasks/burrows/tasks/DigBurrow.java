package pokecube.core.ai.tasks.burrows.tasks;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;

import com.google.common.collect.Maps;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.ai.behavior.BlockPosTracker;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.level.pathfinder.PathComputationType;
import pokecube.api.PokecubeAPI;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.core.ai.brain.BrainUtils;
import pokecube.core.ai.brain.MemoryModules;
import pokecube.core.ai.tasks.TaskBase;
import pokecube.core.ai.tasks.burrows.AbstractBurrowTask;
import pokecube.core.ai.tasks.burrows.burrow.Part;
import pokecube.core.ai.tasks.utility.UtilTask;
import pokecube.core.impl.PokecubeMod;
import thut.api.Tracker;

public class DigBurrow extends AbstractBurrowTask
{
    private static final Map<MemoryModuleType<?>, MemoryStatus> mems = Maps.newHashMap();

    static
    {
        DigBurrow.mems.put(MemoryModules.JOB_INFO.get(), MemoryStatus.VALUE_PRESENT);
    }
    protected int progressTimer = 0;

    boolean dig = false;
    boolean build = false;

    BlockPos work_pos = null;

    final double ds2Max = 9;
    final double dsMax = 3;

    protected Predicate<BlockPos> hasEmptySpace;
    protected Predicate<BlockPos> canStand;
    protected Predicate<BlockPos> canStandNear;

    public DigBurrow(final IPokemob pokemob)
    {
        super(pokemob, DigBurrow.mems);

        this.canStand = p -> PokecubeMod.debug || this.world.getBlockState(p).canOcclude()
                && this.world.getBlockState(p.above()).isPathfindable(this.world, p, PathComputationType.LAND);

        this.canStandNear = pos -> PokecubeMod.debug
                || BlockPos.betweenClosedStream(pos.offset(-2, -2, -2), pos.offset(2, 2, 2))
                        .anyMatch(p2 -> p2.distSqr(pos) < this.ds2Max && this.canStand.test(p2));

        this.hasEmptySpace = pos -> {
            if (PokecubeMod.debug) return true;
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
    public void reset()
    {
        this.entity.getBrain().eraseMemory(MemoryModules.JOB_INFO.get());
        this.work_pos = null;
        this.progressTimer = 0;
    }

    private boolean checkDigSite()
    {
        if (this.work_pos != null) return true;
        final long time = Tracker.instance().getTick();

        final AtomicInteger valids = new AtomicInteger();
        valids.set(0);
        final Part part = this.burrow.hab.burrow;
        // Start with a check of if the pos is inside.
        Predicate<BlockPos> isValid = p -> part.shouldCheckDig(p, time);
        // If it is inside, and not diggable, we notify the node of the
        // dug spot, finally we check if there is space nearby to stand.
        isValid = isValid.and(p -> {
            if (UtilTask.diggable.test(this.world.getBlockState(p)))
            {
                valids.getAndIncrement();
                return this.hasEmptySpace.test(p);
            }
            return false;
        });
        final BlockPos pos = this.entity.blockPosition();
        // Stream -> filter gets us only the valid postions.
        // Min then gets us the one closest to the ant.
        final Optional<BlockPos> valid = part.getDigBlocks().keySet().stream().filter(isValid).min((p1, p2) -> {
            final double d1 = p1.distSqr(pos);
            final double d2 = p2.distSqr(pos);
            return Double.compare(d1, d2);
        });
        if (valid.isPresent())
        {
            this.work_pos = valid.get().immutable();
            if (PokecubeMod.debug) PokecubeAPI.LOGGER.debug("Found Dig Site!");
            return true;
        }
        if (valids.get() == 0) part.setDigDone(Tracker.instance().getTick() + 12000);
        return this.work_pos != null;
    }

    @Override
    public void run()
    {
        if (this.dig)
        {
            this.progressTimer++;
            final Part part = this.burrow.hab.burrow;
            if (!this.checkDigSite()) return;
            final Path p = this.entity.getNavigation().getPath();
            final double dr = this.work_pos.distSqr(this.entity.blockPosition());
            final double dr2 = p == null ? dr : p.getEndNode().asBlockPos().distSqr(this.work_pos);

            if (dr2 > this.ds2Max) this.setWalkTo(this.work_pos, 1, Mth.ceil(this.dsMax - 1));
            else if (this.progressTimer > 20) this.progressTimer = 20;

            if (this.progressTimer > 0 && dr < this.ds2Max)
            {
                this.tryHarvest(this.work_pos, true);
                BrainUtils.setLeapTarget(this.entity, new BlockPosTracker(this.work_pos));
                // Mark it as done for the next few seconds or so
                part.markDug(this.work_pos, Tracker.instance().getTick() + 2400);
                this.progressTimer = -10;
                this.work_pos = null;
            }
            else if (this.progressTimer > 300)
            {
                this.progressTimer = -10;
                part.markDug(this.work_pos, Tracker.instance().getTick() + 120);
                this.work_pos = null;
            }
        }
        else
        {

        }
    }

    @Override
    protected boolean doTask()
    {
        if (!TaskBase.canMove(this.pokemob)) return false;
        final long now = Tracker.instance().getTick();
        this.dig = this.burrow.hab.burrow.shouldDig(now);
        this.build = this.burrow.hab.burrow.shouldBuild(now);
        return this.dig || this.build;
    }

}
