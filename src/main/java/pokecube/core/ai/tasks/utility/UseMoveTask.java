package pokecube.core.ai.tasks.utility;

import java.util.Map;

import com.google.common.collect.Maps;

import net.minecraft.entity.ai.brain.memory.MemoryModuleStatus;
import net.minecraft.entity.ai.brain.memory.MemoryModuleType;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.IPosWrapper;
import net.minecraft.util.math.RayTraceContext;
import net.minecraft.util.math.RayTraceContext.BlockMode;
import net.minecraft.util.math.RayTraceContext.FluidMode;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.RayTraceResult.Type;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3i;
import pokecube.core.PokecubeCore;
import pokecube.core.ai.brain.BrainUtils;
import pokecube.core.ai.brain.MemoryModules;
import pokecube.core.interfaces.IMoveConstants;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.Move_Base;
import pokecube.core.interfaces.pokemob.ai.CombatStates;
import pokecube.core.moves.MovesUtils;
import thut.api.maths.Vector3;

public class UseMoveTask extends UtilTask
{
    private static final Map<MemoryModuleType<?>, MemoryModuleStatus> MEMS = Maps.newHashMap();

    static
    {
        UseMoveTask.MEMS.put(MemoryModules.MOVE_TARGET, MemoryModuleStatus.VALUE_PRESENT);
    }

    private boolean running    = false;
    private boolean checkRange = false;

    IPosWrapper pos = null;

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
        this.destination.set(this.pos.getPos());
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
            this.pokemob.setCombatState(CombatStates.LEAPING, true);

        if (!this.checkRange && dist < var1) // If in range, apply the move
            this.pokemob.executeMove(null, this.destination, 0);

    }

    @Override
    public boolean shouldRun()
    {
        return (this.pos = BrainUtils.getMoveUseTarget(this.entity)) != null;
    }

    @Override
    public void tick()
    {
        // If the move was ranged, check that it is visible, if so, execute
        // move, otherwise path to location.
        if (this.checkRange)
        {
            RayTraceContext context = new RayTraceContext(this.entity.getPositionVec(), new Vector3d(this.destination.x,
                    this.destination.y, this.destination.z), BlockMode.COLLIDER, FluidMode.NONE, this.entity);
            RayTraceResult trace = this.world.rayTraceBlocks(context);
            BlockRayTraceResult result = null;

            // Adjust destination accordingly based on side hit, since it is
            // normally center of block.
            if (trace.getType() == Type.BLOCK)
            {
                result = (BlockRayTraceResult) trace;
                final Vector3i dir = result.getFace().getDirectionVec();
                // Make a new location that is shifted to closer to edge of
                // the block for the visiblity checks.
                final Vector3 loc = this.destination.copy();
                if (loc.x % 1 == 0.5) loc.x += dir.getX() * 0.49;
                if (loc.y % 1 == 0.5) loc.y += dir.getY() * 0.49;
                if (loc.z % 1 == 0.5) loc.z += dir.getZ() * 0.49;
                result = null;
                context = new RayTraceContext(this.entity.getPositionVec(), new Vector3d(loc.x, loc.y, loc.z),
                        BlockMode.COLLIDER, FluidMode.NONE, this.entity);
                // Raytrace against shifted location.
                trace = this.world.rayTraceBlocks(context);
                if (trace.getType() == Type.BLOCK) result = (BlockRayTraceResult) trace;
            }

            // Apply move directly from here.
            if (result == null || result.getPos().equals(this.destination.getPos()))
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
