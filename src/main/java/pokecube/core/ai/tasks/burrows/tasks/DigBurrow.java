package pokecube.core.ai.tasks.burrows.tasks;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;

import com.google.common.collect.Maps;

import net.minecraft.block.BlockState;
import net.minecraft.entity.ai.brain.memory.MemoryModuleStatus;
import net.minecraft.entity.ai.brain.memory.MemoryModuleType;
import net.minecraft.pathfinding.Path;
import net.minecraft.pathfinding.PathType;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockPosWrapper;
import net.minecraft.util.math.MathHelper;
import pokecube.core.PokecubeCore;
import pokecube.core.ai.brain.BrainUtils;
import pokecube.core.ai.tasks.TaskBase;
import pokecube.core.ai.tasks.burrows.AbstractBurrowTask;
import pokecube.core.ai.tasks.burrows.BurrowTasks;
import pokecube.core.ai.tasks.burrows.burrow.Part;
import pokecube.core.ai.tasks.utility.UtilTask;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.PokecubeMod;

public class DigBurrow extends AbstractBurrowTask
{
    private static final Map<MemoryModuleType<?>, MemoryModuleStatus> mems = Maps.newHashMap();

    static
    {
        DigBurrow.mems.put(BurrowTasks.JOB_INFO, MemoryModuleStatus.VALUE_PRESENT);
    }
    protected int progressTimer = 0;

    boolean dig   = false;
    boolean build = false;

    BlockPos work_pos = null;

    final double ds2Max = 9;
    final double dsMax  = 3;

    protected Predicate<BlockPos> hasEmptySpace;
    protected Predicate<BlockPos> canStand;
    protected Predicate<BlockPos> canStandNear;

    public DigBurrow(final IPokemob pokemob)
    {
        super(pokemob, DigBurrow.mems);

        this.canStand = p -> PokecubeMod.debug || this.world.getBlockState(p).isSolid() && this.world.getBlockState(p
                .up()).allowsMovement(this.world, p, PathType.LAND);

        this.canStandNear = pos -> PokecubeMod.debug || BlockPos.getAllInBox(pos.add(-2, -2, -2), pos.add(2, 2, 2))
                .anyMatch(p2 -> p2.distanceSq(pos) < this.ds2Max && this.canStand.test(p2));

        this.hasEmptySpace = pos ->
        {
            if (PokecubeMod.debug) return true;
            for (final Direction dir : Direction.values())
            {
                final BlockPos pos2 = pos.offset(dir);
                final BlockState state = this.world.getBlockState(pos2);
                if (state.allowsMovement(this.world, pos2, PathType.LAND)) return true;
            }
            return false;
        };
    }

    @Override
    public void reset()
    {
        this.entity.getBrain().removeMemory(BurrowTasks.JOB_INFO);
        this.work_pos = null;
        this.progressTimer = 0;
    }

    private boolean checkDigSite()
    {
        if (this.work_pos != null) return true;
        final long time = this.world.getGameTime();

        final AtomicInteger valids = new AtomicInteger();
        valids.set(0);
        final Part part = this.burrow.hab.burrow;
        // Start with a check of if the pos is inside.
        Predicate<BlockPos> isValid = p -> part.shouldCheckDig(p, time);
        // If it is inside, and not diggable, we notify the node of the
        // dug spot, finally we check if there is space nearby to stand.
        isValid = isValid.and(p ->
        {
            if (UtilTask.diggable.test(this.world.getBlockState(p)))
            {
                valids.getAndIncrement();
                return this.hasEmptySpace.test(p);
            }
            return false;
        });
        final BlockPos pos = this.entity.getPosition();
        // Stream -> filter gets us only the valid postions.
        // Min then gets us the one closest to the ant.
        final Optional<BlockPos> valid = part.getDigBlocks().keySet().stream().filter(isValid).min((p1, p2) ->
        {
            final double d1 = p1.distanceSq(pos);
            final double d2 = p2.distanceSq(pos);
            return Double.compare(d1, d2);
        });
        if (valid.isPresent())
        {
            this.work_pos = valid.get().toImmutable();
            if (PokecubeMod.debug) PokecubeCore.LOGGER.debug("Found Dig Site!");
            return true;
        }
        if (valids.get() == 0) part.setDigDone(this.world.getGameTime() + 12000);
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
            final Path p = this.entity.getNavigator().getPath();
            final double dr = this.work_pos.distanceSq(this.entity.getPosition());
            final double dr2 = p == null ? dr : p.getFinalPathPoint().func_224759_a().distanceSq(this.work_pos);

            if (dr2 > this.ds2Max) this.setWalkTo(this.work_pos, 1, MathHelper.ceil(this.dsMax - 1));
            else if (this.progressTimer > 20) this.progressTimer = 20;

            if (this.progressTimer > 0 && dr < this.ds2Max)
            {
                this.tryHarvest(this.work_pos, true);
                BrainUtils.setLeapTarget(this.entity, new BlockPosWrapper(this.work_pos));
                // Mark it as done for the next few seconds or so
                part.markDug(this.work_pos, this.world.getGameTime() + 2400);
                this.progressTimer = -10;
                this.work_pos = null;
            }
            else if (this.progressTimer > 300)
            {
                this.progressTimer = -10;
                part.markDug(this.work_pos, this.world.getGameTime() + 120);
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
        this.dig = this.burrow.hab.burrow.shouldDig(this.world.getGameTime());
        this.build = this.burrow.hab.burrow.shouldBuild(this.world.getGameTime());
        return this.dig || this.build;
    }

}
