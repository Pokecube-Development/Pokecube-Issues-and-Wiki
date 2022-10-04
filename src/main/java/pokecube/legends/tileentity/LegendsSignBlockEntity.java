package pokecube.legends.tileentity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import pokecube.core.blocks.signs.GenericSignBlockEntity;
import pokecube.legends.init.TileEntityInit;

public class LegendsSignBlockEntity extends GenericSignBlockEntity
{
    public LegendsSignBlockEntity(BlockPos pos, BlockState state)
    {
        super(pos, state);
    }

    @Override
    public BlockEntityType<?> getType()
    {
        return TileEntityInit.SIGN_ENTITY.get();
    }
}
