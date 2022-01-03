package thut.concrete.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import thut.api.block.ITickTile;
import thut.api.block.flowing.FlowingBlock;
import thut.concrete.block.entity.VolcanoEntity;

public class VolcanoBlock extends Block implements EntityBlock
{
    public static final IntegerProperty VISCOSITY = FlowingBlock.VISCOSITY;

    public VolcanoBlock(Properties p_49795_)
    {
        super(p_49795_);
        this.registerDefaultState(this.stateDefinition.any().setValue(VISCOSITY, 4));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder)
    {
        builder.add(VISCOSITY);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos p_153215_, BlockState p_153216_)
    {
        return new VolcanoEntity(p_153215_, p_153216_);
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(final Level world, final BlockState state,
            final BlockEntityType<T> type)
    {
        return ITickTile.getTicker(world, state, type);
    }
}
