package pokecube.legends.blocks.normalblocks;

import java.util.Iterator;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.state.BlockState;
import pokecube.legends.Reference;
import pokecube.legends.init.BlockInit;
import thut.lib.RegHelper;

public class DistorticStoneBlock extends Block implements BonemealableBlock
{
    // Tag
    public static final TagKey<Block> DISTORTIC_GRASS_SPREADABLE = TagKey.create(RegHelper.BLOCK_REGISTRY,
            new ResourceLocation(Reference.ID, "distortic_grass_spreadable"));
    public DistorticStoneBlock(final Properties properties)
    {
        super(properties);
    }

    @Override
    public boolean isValidBonemealTarget(final LevelReader worldReader, final BlockPos pos, final BlockState state,
                                         final boolean valid)
    {
        if (!worldReader.getBlockState(pos.above()).propagatesSkylightDown(worldReader, pos)) return false;
        else
        {
            final Iterator<BlockPos> var5 = BlockPos.betweenClosed(pos.offset(-1, -1, -1), pos.offset(1, 1, 1))
                    .iterator();

            BlockPos blockpos;
            do
            {
                if (!var5.hasNext()) return false;

                blockpos = var5.next();
            }
            while (!worldReader.getBlockState(blockpos).is(DISTORTIC_GRASS_SPREADABLE));

            return true;
        }
    }

    @Override
    public boolean isBonemealSuccess(final Level world, final RandomSource random, final BlockPos pos, final BlockState state)
    {
        return true;
    }

    @Override
    public void performBonemeal(final ServerLevel world, final RandomSource random, final BlockPos pos,
            final BlockState state)
    {
        boolean valid = false;
        final Iterator<BlockPos> var7 = BlockPos.betweenClosed(pos.offset(-1, -1, -1), pos.offset(1, 1, 1)).iterator();

        while (var7.hasNext())
        {
            final BlockPos blockpos = var7.next();
            final BlockState state1 = world.getBlockState(blockpos);
            if (state1.is(BlockInit.DISTORTIC_GRASS_BLOCK.get())) valid = true;

            if (valid) break;
        }

        if (valid) world.setBlock(pos, BlockInit.DISTORTIC_GRASS_BLOCK.get().defaultBlockState().setValue(
                CorruptedGrassBlock.SNOWY, world.getBlockState(pos.above()).is(Blocks.SNOW)), 3);
    }
}
