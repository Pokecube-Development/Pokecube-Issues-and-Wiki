package pokecube.core.moves.world;

import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.neoforged.neoforge.common.CommonHooks;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.moves.MoveEntry;
import pokecube.api.moves.utils.IMoveWorldEffect;
import pokecube.core.PokecubeCore;
import pokecube.core.eventhandlers.MoveEventsHandler;
import thut.api.maths.Vector3;

public class DefaultAction implements IMoveWorldEffect
{
    public static Direction getHitDirection(Level level, BlockHitResult blockHit)
    {
        // TODO the shape below can be refined further for more proper internal hitbox stuff
        Direction direction = blockHit.getDirection();
        var state = level.getBlockState(blockHit.getBlockPos());
        var shape = state.getShape(level, blockHit.getBlockPos());
        Vector3 test = new Vector3(blockHit.getLocation()).subtractFrom(new Vector3(blockHit.getBlockPos()));
        var dot = test.y;
        var max = shape.max(Direction.Axis.Y);
        // Top face
        if (dot < 1e-4 || dot > max - 1e-4)
        {
            direction = dot > max/2 ? Direction.UP : Direction.DOWN;
        }
        dot = test.x;
        max = shape.max(Direction.Axis.X);
        // Top face
        if (dot < 1e-4 || dot > max - 1e-4)
        {
            direction = dot > max/2? Direction.EAST : Direction.WEST;
        }
        dot = test.z;
        max = shape.max(Direction.Axis.Z);
        // Top face
        if (dot < 1e-4 || dot > max - 1e-4)
        {
            direction = dot > max/2 ? Direction.SOUTH : Direction.NORTH;
        }
        return direction;
    }

    MoveEntry move;

    public DefaultAction(final MoveEntry move)
    {
        this.move = move;
    }

    @Override
    public boolean applyOutOfCombat(IPokemob user, Vector3 location, HitResult hit)
    {
        if (!PokecubeCore.getConfig().defaultInteractAction) return false;
        var player = MoveEventsHandler.getRelevantPlayer(user);
        BlockHitResult hitResult = new BlockHitResult(location.toVec3d(), Direction.DOWN, location.getPos(), true);
        var event = CommonHooks.onRightClickBlock(player, InteractionHand.MAIN_HAND, location.getPos(), hitResult);
        if (!event.isCanceled())
        {
            var state = location.getBlockState(user.getTrackedEntity().level());
            var result = state.useWithoutItem(player.level(), player, hitResult);
            return result.indicateItemUse();
        }
        return false;
    }

    @Override
    public String getMoveName()
    {
        return this.move.name;
    }

    @Override
    public boolean isValid()
    {
        return move.power > 0;
    }
}
