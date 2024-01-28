package pokecube.core.blocks.signs;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.StandingSignBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.WoodType;

public class GenericStandingSign extends StandingSignBlock
{

    public GenericStandingSign(WoodType woodType, Properties properties)
    {
        super(properties, woodType);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state)
    {
        return GenericSignBlockEntity.SIGN_TYPE.get().create(pos, state);
    }
}
