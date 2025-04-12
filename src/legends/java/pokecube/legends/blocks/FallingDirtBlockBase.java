package pokecube.legends.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.util.ColorRGBA;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.ColoredFallingBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

public class FallingDirtBlockBase extends ColoredFallingBlock
{
    private final ColorRGBA dustColor;

    public FallingDirtBlockBase(ColorRGBA color, BlockBehaviour.Properties properties)
    {
        super(color, properties);
        this.dustColor = color;
    }

    @Override
    public int getDustColor(BlockState state, BlockGetter level, BlockPos pos) {
        return this.dustColor.rgba();
    }
}
