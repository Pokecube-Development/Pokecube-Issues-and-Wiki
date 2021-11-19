package pokecube.legends.blocks.normalblocks;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.CampfireBlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import pokecube.legends.init.TileEntityInit;
import pokecube.legends.tileentity.InfectedCampfireBlockEntity;

public class InfectedCampfireBlock extends CampfireBlock
{
    public InfectedCampfireBlock(boolean smoke, int damage, BlockBehaviour.Properties properties)
    {
        super(smoke, damage, properties);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state)
    {
        return new InfectedCampfireBlockEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level world, BlockState state, BlockEntityType<T> type)
    {
        if (world.isClientSide)
        {
            return state.getValue(LIT) ? createTickerHelper(type, TileEntityInit.CAMPFIRE_ENTITY.get(), CampfireBlockEntity::particleTick) : null;
        } else
        {
            return state.getValue(LIT) ? createTickerHelper(type, TileEntityInit.CAMPFIRE_ENTITY.get(), CampfireBlockEntity::cookTick) :
                createTickerHelper(type, TileEntityInit.CAMPFIRE_ENTITY.get(), CampfireBlockEntity::cooldownTick);
        }
    }
}