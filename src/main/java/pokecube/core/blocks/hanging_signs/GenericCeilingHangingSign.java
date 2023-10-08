package pokecube.core.blocks.hanging_signs;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.CeilingHangingSignBlock;
import net.minecraft.world.level.block.StandingSignBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.WoodType;
import pokecube.core.blocks.signs.GenericSignBlockEntity;

public class GenericCeilingHangingSign extends CeilingHangingSignBlock
{

    public GenericCeilingHangingSign(Properties properties, WoodType woodType)
    {
        super(properties, woodType);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state)
    {
        return GenericHangingSignBlockEntity.SIGN_TYPE.get().create(pos, state);
    }
}
