package pokecube.legends.tileentity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.Clearable;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.CampfireBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import pokecube.legends.init.TileEntityInit;

public class InfectedCampfireBlockEntity extends CampfireBlockEntity implements Clearable
{
    public InfectedCampfireBlockEntity(BlockPos pos, BlockState state)
    {
        super(pos, state);
    }

    @Override
    public BlockEntityType<?> getType() {
        return TileEntityInit.CAMPFIRE_ENTITY.get();
    }
}