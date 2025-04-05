package pokecube.core.blocks.hanging_signs;

import java.util.function.Supplier;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.HangingSignBlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class GenericHangingSignBlockEntity extends HangingSignBlockEntity
{
    public static Supplier<BlockEntityType<GenericHangingSignBlockEntity>> SIGN_TYPE;

    public GenericHangingSignBlockEntity(BlockPos pos, BlockState state)
    {
        super(pos, state);
    }

    @Override
    public BlockEntityType<?> getType()
    {
        return GenericHangingSignBlockEntity.SIGN_TYPE.get();
    }
}
