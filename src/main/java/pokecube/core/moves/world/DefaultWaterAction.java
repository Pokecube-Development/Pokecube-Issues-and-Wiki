package pokecube.core.moves.world;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FarmBlock;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.moves.Move_Base;
import pokecube.core.PokecubeCore;
import pokecube.core.eventhandlers.MoveEventsHandler;
import pokecube.core.eventhandlers.MoveEventsHandler.UseContext;
import thut.api.maths.Vector3;

public class DefaultWaterAction extends DefaultAction
{

    public DefaultWaterAction(Move_Base move)
    {
        super(move);
    }

    @Override
    /**
     * This will have the following effects, for water type moves: Extinguish
     * fires if strong: turn lava to obsidian water farmland
     */
    public boolean applyEffect(IPokemob user, Vector3 location)
    {
        if (!PokecubeCore.getConfig().defaultWaterActions) return false;
        if (move.isSelfMove()) return false;
        final Level world = user.getEntity().getLevel();
        final UseContext context = MoveEventsHandler.getContext(world, user, Blocks.WATER.defaultBlockState(),
                location);
        final BlockState state = context.getHitState();
        final Block block = state.getBlock();
        final BlockPos hitPos = context.getHitPos();
        // Put out fires
        if (block == Blocks.FIRE)
        {
            location.setAir(world);
            return true;
        }
        if (state.getProperties().contains(FarmBlock.MOISTURE))
        {
            final int level = state.getValue(FarmBlock.MOISTURE);
            if (level < 7)
            {
                world.setBlockAndUpdate(hitPos, state.setValue(FarmBlock.MOISTURE, 7));
                return true;
            }
        }
        if (move.getPWR() < MoveEventsHandler.WATERSTRONG) return false;

        // Things below here all actually damage blocks, so check this.
        if (!MoveEventsHandler.canAffectBlock(user, location, move.getName())) return false;

        // Freeze lava
        if (block == Blocks.LAVA)
        {
            final int level = state.getValue(LiquidBlock.LEVEL);
            final BlockState replacement = level == 0 ? Blocks.OBSIDIAN.defaultBlockState()
                    : Blocks.STONE.defaultBlockState();
            world.setBlockAndUpdate(hitPos, replacement);
            return true;
        }
        final BlockPos prevPos = context.getClickedPos();
        final BlockState prev = world.getBlockState(prevPos);

        // Freeze lava
        if (prev.getBlock() == Blocks.LAVA)
        {
            final int level = state.getValue(LiquidBlock.LEVEL);
            final BlockState replacement = level == 0 ? Blocks.OBSIDIAN.defaultBlockState()
                    : Blocks.STONE.defaultBlockState();
            world.setBlockAndUpdate(prevPos, replacement);
            return true;
        }

        // Attempt to place some water
        if (prev.canBeReplaced(context))
            world.setBlockAndUpdate(prevPos, Blocks.WATER.defaultBlockState().setValue(LiquidBlock.LEVEL, 2));
        return false;
    }

}
