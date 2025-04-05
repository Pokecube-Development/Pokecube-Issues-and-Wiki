package pokecube.core.blocks.hanging_signs;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.CeilingHangingSignBlock;
import net.minecraft.world.level.block.SignBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.WoodType;

public class GenericCeilingHangingSign extends CeilingHangingSignBlock
{
    public static final MapCodec<CeilingHangingSignBlock> CODEC = RecordCodecBuilder.mapCodec(p_308810_ -> p_308810_
            .group(WoodType.CODEC.fieldOf("wood_type").forGetter(SignBlock::type), propertiesCodec())
            .apply(p_308810_, GenericCeilingHangingSign::new));

    @Override
    public MapCodec<CeilingHangingSignBlock> codec()
    {
        return CODEC;
    }

    public GenericCeilingHangingSign(WoodType woodType, Properties properties)
    {
        super(woodType, properties);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state)
    {
        return GenericHangingSignBlockEntity.SIGN_TYPE.get().create(pos, state);
    }
}
