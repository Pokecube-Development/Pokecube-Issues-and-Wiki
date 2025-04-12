package pokecube.legends.blocks;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.util.ColorRGBA;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.ColoredFallingBlock;
import net.minecraft.world.level.block.FallingBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

public class FallingBlockBase extends FallingBlock
{
    public static final MapCodec<FallingBlockBase> CODEC = RecordCodecBuilder.mapCodec(
            instance -> instance.group(ColorRGBA.CODEC.fieldOf("falling_dust_color").forGetter(b -> b.dustColor),
                    propertiesCodec()).apply(instance, FallingBlockBase::new));

    @Override
    protected MapCodec<? extends FallingBlock> codec()
    {
        return CODEC;
    }

    private final ColorRGBA dustColor;

    public FallingBlockBase(final ColorRGBA dustColor, final BlockBehaviour.Properties properties)
    {
        super(properties);
        this.dustColor = dustColor;
    }

    @Override
    public int getDustColor(BlockState state, BlockGetter level, BlockPos pos)
    {
        return this.dustColor.rgba();
    }
}
