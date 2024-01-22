package pokecube.core.blocks.hanging_signs;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.WallHangingSignBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.WoodType;

public class GenericWallHangingSign extends WallHangingSignBlock
{
    public GenericWallHangingSign(WoodType woodType, Properties properties)
    {
        super(properties, woodType);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state)
    {
        return GenericHangingSignBlockEntity.SIGN_TYPE.get().create(pos, state);
    }
}
