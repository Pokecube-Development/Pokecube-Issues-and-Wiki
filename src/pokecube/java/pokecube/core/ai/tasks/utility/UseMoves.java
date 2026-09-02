package pokecube.core.ai.tasks.utility;

import com.google.common.collect.Maps;
import net.minecraft.core.Vec3i;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.ClipContext.Block;
import net.minecraft.world.level.ClipContext.Fluid;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult.Type;
import net.minecraft.world.phys.Vec3;
import pokecube.api.data.moves.MoveApplicationRegistry;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.entity.pokemob.PokemobCaps;
import pokecube.api.moves.MoveEntry;
import pokecube.api.moves.utils.MoveApplication;
import pokecube.core.PokecubeCore;
import pokecube.core.ai.brain.BrainUtils;
import pokecube.core.ai.brain.MemoryModules;
import pokecube.core.ai.tasks.IMoveUseAI;
import thut.api.entity.ai.VectorPosWrapper;
import thut.api.maths.Vector3;

import java.util.Map;

public class UseMoves extends UtilBehaviour implements IMoveUseAI
{
    private static final Map<MemoryModuleType<?>, MemoryStatus> MEMS = Maps.newHashMap();

    static
    {
        MEMS.put(MemoryModules.MOVE_TARGET.get(), MemoryStatus.VALUE_PRESENT);
    }

    double speed;

    public UseMoves()
    {
        super(UseMoves.MEMS);
    }

    @Override
    public void reset(Mob entityIn)
    {
        entityIn.getBrain().eraseMemory(MemoryModules.MOVE_TARGET.get());
    }

    @Override
    protected void tick(ServerLevel level, Mob entity, long gameTime)
    {
        boolean running = false;
        boolean checkRange = false;

        IPokemob pokemob = PokemobCaps.getPokemobFor(entity);
        var pos = BrainUtils.getMoveUseTarget(entity).currentPosition();
        Vector3 destination = new Vector3(pos);
        final MoveEntry move = pokemob.getSelectedMove();

        if (!running)
        {
            MoveApplication toApply = new MoveApplication(move, pokemob, null);
            this.speed = 1;

            final boolean self = MoveApplicationRegistry.getValidator(move).test(toApply);
            // Apply self moves directly.
            if (self)
            {
                pokemob.executeMove(null, destination, 0);
                this.clearUseMove(pokemob);
                return;
            }
            final boolean ranged = move.isRanged(pokemob);
            if (ranged && !checkRange)
            {
                final double dist = destination.distToEntity(entity);
                // If in range, divert to main thread to see if visible.
                if (dist < PokecubeCore.getConfig().rangedAttackDistance)
                {
                    checkRange = true;
                }
            }
            if (!checkRange) this.setWalkTo(entity, destination, this.speed, 0);
        }
        // Look at your destination
        BrainUtils.lookAt(entity, destination);
        Vector3 loc = new Vector3().set(entity, false);
        final double dist = loc.distToSq(destination);
        double var1 = 4;

        final boolean rangedMove = move.isRanged(pokemob);

        if (!checkRange && rangedMove)
        {
            var1 = PokecubeCore.getConfig().rangedAttackDistance * PokecubeCore.getConfig().rangedAttackDistance;
            // Divert ranged moves to main thread for visiblity checks.
            checkRange = true;
        }
        if (!rangedMove) // Leap at the target location
            BrainUtils.setLeapTarget(entity, new VectorPosWrapper(destination));

        if (!checkRange && dist < var1) // If in range, apply the move
        {
            pokemob.executeMove(null, destination, 0);
            this.clearUseMove(pokemob);
        }

        // If the move was ranged, check that it is visible, if so, execute
        // move, otherwise path to location.
        if (checkRange)
        {
            ClipContext context = new ClipContext(entity.position(),
                    new Vec3(destination.x, destination.y, destination.z), Block.COLLIDER, Fluid.NONE, entity);
            BlockHitResult trace = level.clip(context);
            BlockHitResult result = null;

            // Adjust destination accordingly based on side hit, since it is
            // normally center of block.
            if (trace.getType() == Type.BLOCK)
            {
                result = trace;
                final Vec3i dir = result.getDirection().getNormal();
                // Make a new location that is shifted to closer to edge of
                // the block for the visiblity checks.
                loc = destination.copy();
                if (loc.x % 1 == 0.5) loc.x += dir.getX() * 0.49;
                if (loc.y % 1 == 0.5) loc.y += dir.getY() * 0.49;
                if (loc.z % 1 == 0.5) loc.z += dir.getZ() * 0.49;
                result = null;
                context = new ClipContext(entity.position(), new Vec3(loc.x, loc.y, loc.z), Block.COLLIDER, Fluid.NONE,
                        entity);
                // Raytrace against shifted location.
                trace = level.clip(context);
                if (trace.getType() == Type.BLOCK) result = trace;
            }

            // Apply move directly from here.
            if (result == null || result.getBlockPos().equals(destination.getPos()))
            {
                pokemob.executeMove(null, destination, 0);
            }
            else
            {
                // Set destination and wait for move to be checked again.
                this.setWalkTo(entity, destination, this.speed, 0);
            }
        }

    }

    @Override
    public boolean shouldRun(Mob entity)
    {
        return BrainUtils.getMoveUseTarget(entity) != null;
    }

}
