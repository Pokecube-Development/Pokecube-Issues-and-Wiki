package pokecube.legends.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.FallingBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

public class FallingBlockBase extends FallingBlock
{
    private final int dustColor;

    public FallingBlockBase(final int color, final BlockBehaviour.Properties properties)
    {
        super(properties);
        this.dustColor = color;
    }

    @Override
    public int getDustColor(final BlockState state, final BlockGetter block, final BlockPos pos)
    {
        return this.dustColor;
    }
}
