package pokecube.core.moves.world;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.moves.MoveEntry;
import pokecube.api.utils.PokeType;
import pokecube.core.PokecubeCore;
import pokecube.core.eventhandlers.MoveEventsHandler;
import pokecube.core.eventhandlers.MoveEventsHandler.UseContext;
import thut.api.maths.Vector3;

public class DefaultIceAction extends DefaultAction
{

    public DefaultIceAction(MoveEntry move)
    {
        super(move);
    }

    /**
     * This will have the following effects, for ice type moves: Place snow Freeze water
     */
    @Override
    public boolean applyOutOfCombat(IPokemob user, Vector3 location, HitResult hit)
    {
        if (!PokecubeCore.getConfig().defaultIceActions) return false;

        if(!(hit instanceof BlockHitResult blockHit)) return false;
        // Things below here all actually damage blocks, so check this.
        if (!MoveEventsHandler.canAffectBlock(user, location, move.getName())) return false;

        final Level world = user.getEntity().level();
        BlockPos pos = blockHit.getBlockPos().immutable();
        BlockState state = world.getBlockState(pos);
        Block block = state.getBlock();
        // // First attempt to freeze the water if we hit water
        if (block == Blocks.WATER && state.getValue(LiquidBlock.LEVEL) == 0)
        {
            world.setBlockAndUpdate(pos, Blocks.ICE.defaultBlockState());
            return true;
        }

        Direction direction = getHitDirection(world, blockHit);
        BlockPos prevPos = pos.relative(direction);
        state = world.getBlockState(prevPos);
        Block prevBlock = state.getBlock();
        // Now try if we hit something behind water
        if (prevBlock == Blocks.WATER && state.getValue(LiquidBlock.LEVEL) == 0)
        {
            world.setBlockAndUpdate(prevPos, Blocks.ICE.defaultBlockState());
            return true;
        }
        var newHit = blockHit.withDirection(direction);
        final UseContext context = MoveEventsHandler.getContext(world, user, Blocks.SNOW.defaultBlockState(), newHit);
        // Otherwise update from the context
        // Finally try right clicking with it
        final InteractionResult result = context.getItemInHand().useOn(context);
        return result == InteractionResult.SUCCESS;
    }

    @Override
    public boolean isValid()
    {
        return move.getType(null) == PokeType.getType("ice") && !move.isContact(null) && move.power > 0;
    }

}
