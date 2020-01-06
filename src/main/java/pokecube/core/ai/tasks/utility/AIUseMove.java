package pokecube.core.ai.tasks.utility;

import net.minecraft.pathfinding.Path;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceContext;
import net.minecraft.util.math.RayTraceContext.BlockMode;
import net.minecraft.util.math.RayTraceContext.FluidMode;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.RayTraceResult.Type;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import pokecube.core.PokecubeCore;
import pokecube.core.ai.tasks.AIBase;
import pokecube.core.interfaces.IMoveConstants;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.Move_Base;
import pokecube.core.interfaces.pokemob.ai.CombatStates;
import pokecube.core.moves.MovesUtils;
import thut.api.maths.Vector3;

public class AIUseMove extends AIBase
{
    private boolean running    = false;
    private boolean checkRange = false;
    double          speed;

    public AIUseMove(final IPokemob pokemob)
    {
        super(pokemob);
    }

    @Override
    public void reset()
    {
    }

    @Override
    public void run()
    {
        final Vector3 destination = this.pokemob.getTargetPos();
        final Move_Base move = MovesUtils.getMoveFromName(this.pokemob.getMove(this.pokemob.getMoveIndex()));

        if (!this.running)
        {
            this.speed = this.pokemob.getMovementSpeed();
            this.pokemob.setCombatState(CombatStates.NEWEXECUTEMOVE, false);
            if (move == null) return;
            // No destination given, just apply the move directly.
            if (destination == null)
            {
                this.addMoveInfo(this.pokemob, null, null, 0);
                this.pokemob.setCombatState(CombatStates.EXECUTINGMOVE, false);
                return;
            }
            else
            {
                final boolean self = (move.getAttackCategory() & IMoveConstants.CATEGORY_SELF) != 0;
                // Apply self moves directly.
                if (self)
                {
                    this.addMoveInfo(this.pokemob, this.entity, null, 0);
                    return;
                }
                final boolean ranged = (move.getAttackCategory() & IMoveConstants.CATEGORY_DISTANCE) != 0;
                if (ranged && !this.checkRange)
                {
                    final double dist = destination.distToEntity(this.entity);
                    // If in range, divert to main thread to see if visible.
                    if (dist < PokecubeCore.getConfig().rangedAttackDistance)
                    {
                        this.checkRange = true;
                        return;
                    }
                }
                // Try to path to where the move is needed.
                this.pokemob.setCombatState(CombatStates.EXECUTINGMOVE, true);
                final Path path = this.entity.getNavigator().func_225466_a(destination.x, destination.y, destination.z,
                        0);
                this.addEntityPath(this.entity, path, this.speed);
            }
            this.running = true;
        }
        // Look at your destination
        this.entity.getLookController().setLookPosition(destination.x, destination.y, destination.z, 10, this.entity
                .getVerticalFaceSpeed());
        final Vector3 loc = Vector3.getNewVector().set(this.entity, false);
        final double dist = loc.distToSq(destination);
        double var1 = 16;
        if (move == null)
        {
            this.running = false;
            this.pokemob.setCombatState(CombatStates.EXECUTINGMOVE, false);
            return;
        }
        if (!this.checkRange && (move.getAttackCategory() & IMoveConstants.CATEGORY_DISTANCE) > 0)
        {
            var1 = PokecubeCore.getConfig().rangedAttackDistance * PokecubeCore.getConfig().rangedAttackDistance;
            // Divert ranged moves to main thread for visiblity checks.
            this.checkRange = true;
        }
        if (!this.checkRange && dist < var1)
        {
            // If in range, apply the move and reset tasks
            this.addMoveInfo(this.pokemob, null, destination, 0);
            this.addEntityPath(this.entity, null, this.speed);
            this.pokemob.setCombatState(CombatStates.EXECUTINGMOVE, false);
            // Leap at the target location
            this.pokemob.setCombatState(CombatStates.LEAPING, true);
            this.running = false;
        }

    }

    @Override
    public boolean shouldRun()
    {
        return this.running || this.pokemob.getCombatState(CombatStates.NEWEXECUTEMOVE) && !this.pokemob.getCombatState(
                CombatStates.ANGRY) && this.pokemob.getAttackCooldown() <= 0;
    }

    @Override
    public void tick()
    {

        // If the move was ranged, check that it is visible, if so, execute
        // move, otherwise path to location.
        if (this.checkRange)
        {
            final Vector3 destination = this.pokemob.getTargetPos();
            if (destination != null)
            {
                RayTraceContext context = new RayTraceContext(this.entity.getPositionVector(), new Vec3d(destination.x,
                        destination.y, destination.z), BlockMode.COLLIDER, FluidMode.NONE, this.entity);
                RayTraceResult trace = this.world.rayTraceBlocks(context);
                BlockRayTraceResult result = null;

                // Adjust destination accordingly based on side hit, since it is
                // normally center of block.
                if (trace.getType() == Type.BLOCK)
                {
                    result = (BlockRayTraceResult) trace;
                    final Vec3i dir = result.getFace().getDirectionVec();
                    // Make a new location that is shifted to closer to edge of
                    // the block for the visiblity checks.
                    final Vector3 loc = destination.copy();
                    if (loc.x % 1 == 0.5) loc.x += dir.getX() * 0.49;
                    if (loc.y % 1 == 0.5) loc.y += dir.getY() * 0.49;
                    if (loc.z % 1 == 0.5) loc.z += dir.getZ() * 0.49;
                    result = null;
                    context = new RayTraceContext(this.entity.getPositionVector(), new Vec3d(loc.x, loc.y, loc.z),
                            BlockMode.COLLIDER, FluidMode.NONE, this.entity);
                    // Raytrace against shifted location.
                    trace = this.world.rayTraceBlocks(context);
                    if (trace.getType() == Type.BLOCK) result = (BlockRayTraceResult) trace;
                }

                // Apply move directly from here.
                if (result == null || result.getPos().equals(destination.getPos()))
                {
                    this.addMoveInfo(this.pokemob, null, destination, 0);
                    this.addEntityPath(this.entity, null, this.speed);
                    this.pokemob.setCombatState(CombatStates.EXECUTINGMOVE, false);
                    this.running = false;
                }
                else
                {
                    // Set destination and wait for move to be checked again.
                    this.running = true;
                    this.pokemob.setCombatState(CombatStates.EXECUTINGMOVE, true);
                    final Path path = this.entity.getNavigator().func_225466_a(destination.x, destination.y,
                            destination.z, 0);
                    this.addEntityPath(this.entity, path, this.speed);
                }
            }
            this.checkRange = false;
        }
    }

}
