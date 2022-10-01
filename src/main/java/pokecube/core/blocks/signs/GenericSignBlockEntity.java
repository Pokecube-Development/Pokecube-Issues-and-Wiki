package pokecube.core.blocks.signs;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import pokecube.core.PokecubeItems;

public class GenericSignBlockEntity extends SignBlockEntity
{
    public GenericSignBlockEntity(BlockPos pos, BlockState state)
    {
        super(pos, state);
    }

    @Override
    public BlockEntityType<?> getType()
    {
        return PokecubeItems.SIGN_TYPE.get();
    }
}
