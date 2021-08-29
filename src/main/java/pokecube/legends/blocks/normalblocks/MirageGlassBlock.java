package pokecube.legends.blocks.normalblocks;

import net.minecraft.block.BlockState;
import net.minecraft.item.DyeColor;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorldReader;
import pokecube.legends.blocks.GlassBlockBase;

public class MirageGlassBlock extends GlassBlockBase
{

    public MirageGlassBlock(DyeColor color, final Properties properties)
    {
        super(color, properties);
    }

    @Override
    public float[] getBeaconColorMultiplier(BlockState state, IWorldReader world, BlockPos pos, BlockPos beaconPos) {
        return new float[]{0.00f, 0.95f, 1.00f};
    }
}