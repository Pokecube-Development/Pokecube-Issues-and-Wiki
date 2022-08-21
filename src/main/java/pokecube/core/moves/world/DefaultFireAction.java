package pokecube.core.moves.world;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.SnowLayerBlock;
import net.minecraft.world.level.block.state.BlockState;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.moves.Move_Base;
import pokecube.core.PokecubeCore;
import pokecube.core.eventhandlers.MoveEventsHandler;
import pokecube.core.eventhandlers.MoveEventsHandler.UseContext;
import thut.api.maths.Vector3;

public class DefaultFireAction extends DefaultAction
{

    public DefaultFireAction(Move_Base move)
    {
        super(move);
    }

    @Override
    /**
     * This will have the following effects, for fire type moves: Ignite
     * flamable blocks Melt snow If strong, melt obsidian to lava If none of the
     * above, attempt to cook items nearby
     */
    public boolean applyEffect(IPokemob user, Vector3 location)
    {
        if (move.getPWR() <= 0 || !PokecubeCore.getConfig().defaultFireActions) return false;
        final Level world = user.getEntity().getLevel();
        final UseContext context = MoveEventsHandler.getContext(world, user, Blocks.LAVA.defaultBlockState(), location);
        final BlockState state = context.getHitState();
        Block block = state.getBlock();
        final BlockPos hitPos = context.getHitPos();
        final BlockPos prevPos = context.getClickedPos();
        final BlockPos placePos = prevPos;
        final boolean light = BaseFireBlock.canBePlacedAt(world, placePos, context.getHorizontalDirection());
        final BlockState prev = world.getBlockState(prevPos);

        final boolean smelted = MoveEventsHandler.attemptSmelt(user, location);
        // First try to smelt items
        if (smelted) return true;

        // Things below here all actually damage blocks, so check this.
        if (!MoveEventsHandler.canAffectBlock(user, location, move.getName())) return false;

        // Melt Snow
        if (block == Blocks.SNOW_BLOCK)
        {
            world.setBlockAndUpdate(hitPos, Blocks.WATER.defaultBlockState());
            return true;
        }
        // Melt Snow
        else if (block == Blocks.SNOW)
        {
            final int level = state.getValue(SnowLayerBlock.LAYERS);
            world.setBlockAndUpdate(hitPos, Blocks.WATER.defaultBlockState().setValue(LiquidBlock.LEVEL, level));
            return true;
        }
        // Melt Ice
        else if (block == Blocks.ICE)
        {
            world.setBlockAndUpdate(hitPos, Blocks.WATER.defaultBlockState());
            return true;
        }
        block = prev.getBlock();

        // Melt Snow
        if (block == Blocks.SNOW_BLOCK)
        {
            world.setBlockAndUpdate(prevPos, Blocks.WATER.defaultBlockState());
            return true;
        }
        // Melt Snow
        else if (block == Blocks.SNOW)
        {
            final int level = prev.getValue(SnowLayerBlock.LAYERS);
            world.setBlockAndUpdate(prevPos, Blocks.WATER.defaultBlockState().setValue(LiquidBlock.LEVEL, level));
            return true;
        }
        // Melt Ice
        else if (block == Blocks.ICE)
        {
            world.setBlockAndUpdate(prevPos, Blocks.WATER.defaultBlockState());
            return true;
        }

        // Start fires
        if (light && move.getPWR() < MoveEventsHandler.FIRESTRONG)
        {
            final BlockState fire = BaseFireBlock.getState(world, placePos);
            world.setBlockAndUpdate(placePos, fire);
            return true;
        }
        if (move.getPWR() < MoveEventsHandler.FIRESTRONG) return false;

        block = state.getBlock();
        // Melt obsidian
        if (block == Blocks.OBSIDIAN)
        {
            world.setBlockAndUpdate(hitPos, Blocks.LAVA.defaultBlockState());
            return true;
        }
        // Evapourate water
        else if (block == Blocks.WATER)
        {
            world.setBlockAndUpdate(hitPos, Blocks.AIR.defaultBlockState());
            return true;
        }
        block = prev.getBlock();
        if (block == Blocks.OBSIDIAN)
        {
            world.setBlockAndUpdate(hitPos, Blocks.LAVA.defaultBlockState());
            return true;
        }
        // Evapourate water
        else if (block == Blocks.WATER)
        {
            world.setBlockAndUpdate(hitPos, Blocks.AIR.defaultBlockState());
            return true;
        }
        // Start fires
        else if (light)
        {
            final BlockState fire = BaseFireBlock.getState(world, placePos);
            world.setBlockAndUpdate(placePos, fire);
            return true;
        }
        return false;
    }

}
