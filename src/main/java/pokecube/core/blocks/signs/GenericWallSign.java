package pokecube.core.blocks.signs;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.StandingSignBlock;
import net.minecraft.world.level.block.WallSignBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.WoodType;
import pokecube.core.PokecubeItems;

public class GenericWallSign extends WallSignBlock
{
    public GenericWallSign(Properties properties, WoodType woodType)
    {
        super(properties, woodType);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state)
    {
        return PokecubeItems.SIGN_TYPE.get().create(pos, state);
    }
}
