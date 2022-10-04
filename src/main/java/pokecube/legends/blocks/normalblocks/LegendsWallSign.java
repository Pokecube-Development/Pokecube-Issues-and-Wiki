package pokecube.legends.blocks.normalblocks;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.WoodType;
import pokecube.core.blocks.signs.GenericWallSign;
import pokecube.legends.init.TileEntityInit;

public class LegendsWallSign extends GenericWallSign
{
    public LegendsWallSign(Properties properties, WoodType woodType)
    {
        super(properties, woodType);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state)
    {
        return TileEntityInit.SIGN_ENTITY.get().create(pos, state);
    }
}
