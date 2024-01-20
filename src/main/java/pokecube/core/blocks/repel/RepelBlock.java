package pokecube.core.blocks.repel;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import pokecube.core.blocks.InteractableHorizontalBlock;

public class RepelBlock extends InteractableHorizontalBlock implements EntityBlock
{
    public static final BooleanProperty POWERED = BlockStateProperties.POWERED;

    public RepelBlock(final Properties properties)
    {
        super(properties);
    }

    @Override
    protected void createBlockStateDefinition(final StateDefinition.Builder<Block, BlockState> builder)
    {
        builder.add(FACING, POWERED);
    }

    @Override
    public BlockEntity newBlockEntity(final BlockPos pos, final BlockState state)
    {
        return new RepelTile(pos, state);
    }

    @Override
    public void neighborChanged(final BlockState state, final Level worldIn, final BlockPos pos, final Block blockIn,
            final BlockPos fromPos, final boolean isMoving)
    {
        final int power = worldIn.getBestNeighborSignal(pos);
        final BlockEntity tile = worldIn.getBlockEntity(pos);
        if (!(tile instanceof RepelTile repel)) return;
        if (power != 0)
        {
            repel.enabled = false;
            repel.removeForbiddenSpawningCoord();
            worldIn.setBlock(pos, state.setValue(POWERED, Boolean.FALSE), 3);
        }
        else
        {
            repel.enabled = true;
            repel.addForbiddenSpawningCoord();
            worldIn.setBlock(pos, state.setValue(POWERED, Boolean.TRUE), 3);
        }
    }

    @Override
    public BlockState getStateForPlacement(final BlockPlaceContext context)
    {
        return this.defaultBlockState().setValue(POWERED, Boolean.TRUE)
                .setValue(HorizontalDirectionalBlock.FACING, context.getHorizontalDirection().getOpposite());
    }
}
