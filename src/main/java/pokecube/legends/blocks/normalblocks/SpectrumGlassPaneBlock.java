package pokecube.legends.blocks.normalblocks;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.BeaconBeamBlock;
import net.minecraft.world.level.block.StainedGlassPaneBlock;
import net.minecraft.world.level.block.state.BlockState;
import pokecube.legends.blocks.GlassBlockBase;

public class SpectrumGlassPaneBlock extends StainedGlassPaneBlock implements BeaconBeamBlock
{
    public SpectrumGlassPaneBlock(DyeColor color, final Properties properties)
    {
        super(color, properties);
    }

    @Override
    public float[] getBeaconColorMultiplier(BlockState state, LevelReader world, BlockPos pos, BlockPos beaconPos)
    {
        return new float[]{0.97f, 0.45f, 0.24f};
    }
}