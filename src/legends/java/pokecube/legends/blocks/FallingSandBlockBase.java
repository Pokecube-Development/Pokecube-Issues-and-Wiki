package pokecube.legends.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.util.ColorRGBA;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.ColoredFallingBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

public class FallingSandBlockBase extends ColoredFallingBlock
{
    private final ColorRGBA dustColor;

    public FallingSandBlockBase(final ColorRGBA dustColor, final BlockBehaviour.Properties properties)
    {
        super(dustColor, properties);
        this.dustColor = dustColor;
    }

    @Override
    public int getDustColor(BlockState state, BlockGetter level, BlockPos pos) {
        return this.dustColor.rgba();
    }
}
