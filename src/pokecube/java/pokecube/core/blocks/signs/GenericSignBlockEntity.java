package pokecube.core.blocks.signs;

import java.util.function.Supplier;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class GenericSignBlockEntity extends SignBlockEntity
{
    public static Supplier<BlockEntityType<GenericSignBlockEntity>> SIGN_TYPE;

    public GenericSignBlockEntity(BlockPos pos, BlockState state)
    {
        super(pos, state);
    }

    @Override
    public BlockEntityType<?> getType()
    {
        return GenericSignBlockEntity.SIGN_TYPE.get();
    }
}
