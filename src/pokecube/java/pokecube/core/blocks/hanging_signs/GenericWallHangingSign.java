package pokecube.core.blocks.hanging_signs;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.SignBlock;
import net.minecraft.world.level.block.WallHangingSignBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.WoodType;

public class GenericWallHangingSign extends WallHangingSignBlock
{
    public static final MapCodec<WallHangingSignBlock> CODEC = RecordCodecBuilder.mapCodec(p_308846_ -> p_308846_
            .group(WoodType.CODEC.fieldOf("wood_type").forGetter(SignBlock::type), propertiesCodec())
            .apply(p_308846_, GenericWallHangingSign::new));

    @Override
    public MapCodec<WallHangingSignBlock> codec()
    {
        return CODEC;
    }

    public GenericWallHangingSign(WoodType woodType, Properties properties)
    {
        super(woodType, properties);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state)
    {
        return GenericHangingSignBlockEntity.SIGN_TYPE.get().create(pos, state);
    }
}
