package pokecube.core.moves.world;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.moves.MoveEntry;
import pokecube.api.utils.PokeType;
import pokecube.core.PokecubeCore;
import pokecube.core.eventhandlers.MoveEventsHandler;
import thut.api.maths.Vector3;

public class DefaultElectricAction extends DefaultAction
{

    public static int ELECTRICSTRONG = 100;

    public DefaultElectricAction(MoveEntry move)
    {
        super(move);
    }

    /**
     * This will have the following effects, for "Strong" electric type moves:
     * Melt sand to glass
     */
    @Override
    public boolean applyOutOfCombat(IPokemob user, Vector3 location, HitResult hit)
    {
        if (move.getPWR() < ELECTRICSTRONG || !PokecubeCore.getConfig().defaultElectricActions) return false;
        // Things below here all actually damage blocks, so check this.
        if(!(hit instanceof BlockHitResult blockHit)) return false;
        if (!MoveEventsHandler.canAffectBlock(user, location, move.getName())) return false;
        final Level world = user.getEntity().level();
        BlockPos pos = blockHit.getBlockPos();
        final BlockState state = world.getBlockState(pos);
        final Block block = state.getBlock();
        if (block == Blocks.SAND)
        {
            world.setBlockAndUpdate(pos, Blocks.GLASS.defaultBlockState());
            return true;
        }
        return false;
    }

    @Override
    public boolean isValid()
    {
        return move.getType(null) == PokeType.getType("electric");
    }

}
