package pokecube.core.blocks;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.HorizontalBlock;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.state.StateContainer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.world.World;

public abstract class InteractableHorizontalBlock extends HorizontalBlock
{

    public InteractableHorizontalBlock(final Properties properties)
    {
        super(properties);
        this.setDefaultState(this.stateContainer.getBaseState().with(HorizontalBlock.HORIZONTAL_FACING,
                Direction.NORTH));
    }

    @Override
    protected void fillStateContainer(final StateContainer.Builder<Block, BlockState> builder)
    {
        builder.add(HorizontalBlock.HORIZONTAL_FACING);
    }

    @Override
    public BlockState getStateForPlacement(final BlockItemUseContext context)
    {
        return this.getDefaultState().with(HorizontalBlock.HORIZONTAL_FACING, context.getPlacementHorizontalFacing()
                .getOpposite());
    }

    @Override
    public boolean onBlockActivated(final BlockState state, final World world, final BlockPos pos,
            final PlayerEntity player, final Hand hand, final BlockRayTraceResult hit)
    {
        final TileEntity tile = world.getTileEntity(pos);
        if (tile instanceof InteractableTile) return ((InteractableTile) tile).onInteract(pos, player, hand, hit);
        return true;
    }

    @Override
    public void onEntityWalk(final World worldIn, final BlockPos pos, final Entity entityIn)
    {
        final TileEntity tile = worldIn.getTileEntity(pos);
        if (tile instanceof InteractableTile) ((InteractableTile) tile).onWalkedOn(entityIn);
    }

}
