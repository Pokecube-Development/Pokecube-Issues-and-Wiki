package pokecube.core.blocks.hanging_signs;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.registries.RegistryObject;

public class GenericHangingSignBlockEntity extends SignBlockEntity
{
    public static RegistryObject<BlockEntityType<GenericHangingSignBlockEntity>> SIGN_TYPE;

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
