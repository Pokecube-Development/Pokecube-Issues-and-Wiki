package pokecube.core.ai.tasks.utility;

import java.util.Map;

import com.google.common.collect.Maps;

import net.minecraft.core.Vec3i;
import net.minecraft.world.entity.ai.behavior.PositionTracker;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.ClipContext.Block;
import net.minecraft.world.level.ClipContext.Fluid;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.HitResult.Type;
import net.minecraft.world.phys.Vec3;
import pokecube.core.PokecubeCore;
import pokecube.core.ai.brain.BrainUtils;
import pokecube.core.ai.brain.MemoryModules;
import pokecube.core.interfaces.IMoveConstants;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.Move_Base;
import pokecube.core.moves.MovesUtils;
import thut.api.entity.ai.VectorPosWrapper;
import thut.api.maths.Vector3;

public class UseMoveTask extends UtilTask
{
    private static final Map<MemoryModuleType<?>, MemoryStatus> MEMS = Maps.newHashMap();

    static
    {
        UseMoveTask.MEMS.put(MemoryModules.MOVE_TARGET, MemoryStatus.VALUE_PRESENT);
    }

    private boolean running    = false;
    private boolean checkRange = false;

    PositionTracker pos = null;

    Vector3 destination = Vector3.getNewVector();

    double speed;

    public UseMoveTask(final IPokemob pokemob)
    {
        super(pokemob, UseMoveTask.MEMS);
    }

    @Override
    public void reset()
    {
        this.running = false;
    }

    @Override
    public void run()
    {
        this.destination.set(this.pos.currentPosition());
        final Move_Base move = MovesUtils.getMoveFromName(this.pokemob.getMove(this.pokemob.getMoveIndex()));

        if (move == null)
        {
            BrainUtils.clearMoveUseTarget(this.entity);
            return;
        }

        if (!this.running)
        {
            this.speed = 1;

            final boolean self = (move.getAttackCategory() & IMoveConstants.CATEGORY_SELF) != 0;
            // Apply self moves directly.
            if (self)
            {
                this.pokemob.executeMove(null, this.destination, 0);
                return;
            }
            final boolean ranged = (move.getAttackCategory() & IMoveConstants.CATEGORY_DISTANCE) != 0;
            if (ranged && !this.checkRange)
            {
                final double dist = this.destination.distToEntity(this.entity);
                // If in range, divert to main thread to see if visible.
                if (dist < PokecubeCore.getConfig().rangedAttackDistance)
                {
                    this.checkRange = true;
                    return;
                }
            }
            this.setWalkTo(this.destination, this.speed, 0);
            this.running = true;
        }
        // Look at your destination
        BrainUtils.lookAt(this.entity, this.destination);
        final Vector3 loc = Vector3.getNewVector().set(this.entity, false);
        final double dist = loc.distToSq(this.destination);
        double var1 = 4;

        final boolean rangedMove = (move.getAttackCategory() & IMoveConstants.CATEGORY_DISTANCE) > 0;

        if (!this.checkRange && rangedMove)
        {
            var1 = PokecubeCore.getConfig().rangedAttackDistance * PokecubeCore.getConfig().rangedAttackDistance;
            // Divert ranged moves to main thread for visiblity checks.
            this.checkRange = true;
        }
        if (!rangedMove) // Leap at the target location
            BrainUtils.setLeapTarget(this.entity, new VectorPosWrapper(this.destination));

        if (!this.checkRange && dist < var1) // If in range, apply the move
            this.pokemob.executeMove(null, this.destination, 0);

    }

    @Override
    public boolean shouldRun()
    {
        return (this.pos = BrainUtils.getMoveUseTarget(this.entity)) != null;
    }

    @Override
    public boolean loadThrottle()
    {
        return false;
    }

    @Override
    public void tick()
    {
        // If the move was ranged, check that it is visible, if so, execute
        // move, otherwise path to location.
        if (this.checkRange)
        {
            ClipContext context = new ClipContext(this.entity.position(), new Vec3(this.destination.x,
                    this.destination.y, this.destination.z), Block.COLLIDER, Fluid.NONE, this.entity);
            HitResult trace = this.world.clip(context);
            BlockHitResult result = null;

            // Adjust destination accordingly based on side hit, since it is
            // normally center of block.
            if (trace.getType() == Type.BLOCK)
            {
                result = (BlockHitResult) trace;
                final Vec3i dir = result.getDirection().getNormal();
                // Make a new location that is shifted to closer to edge of
                // the block for the visiblity checks.
                final Vector3 loc = this.destination.copy();
                if (loc.x % 1 == 0.5) loc.x += dir.getX() * 0.49;
                if (loc.y % 1 == 0.5) loc.y += dir.getY() * 0.49;
                if (loc.z % 1 == 0.5) loc.z += dir.getZ() * 0.49;
                result = null;
                context = new ClipContext(this.entity.position(), new Vec3(loc.x, loc.y, loc.z),
                        Block.COLLIDER, Fluid.NONE, this.entity);
                // Raytrace against shifted location.
                trace = this.world.clip(context);
                if (trace.getType() == Type.BLOCK) result = (BlockHitResult) trace;
            }

            // Apply move directly from here.
            if (result == null || result.getBlockPos().equals(this.destination.getPos()))
            {
                this.pokemob.executeMove(null, this.destination, 0);
                this.running = false;
            }
            else
            {
                // Set destination and wait for move to be checked again.
                this.running = true;
                this.setWalkTo(this.destination, this.speed, 0);
            }
            this.checkRange = false;
        }
    }

}
