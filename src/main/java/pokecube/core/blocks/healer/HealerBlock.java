package pokecube.core.blocks.healer;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.HorizontalBlock;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.container.SimpleNamedContainerProvider;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.DirectionProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.IWorldPosCallable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import pokecube.core.inventory.healer.HealerContainer;

public class HealerBlock extends HorizontalBlock
{
    public static final DirectionProperty FACING = HorizontalBlock.HORIZONTAL_FACING;
    public static final BooleanProperty   FIXED  = BooleanProperty.create("fixed");
    public static final VoxelShape        SHAPE  = VoxelShapes.create(0.01, 0.01, 0.01, 0.99, 0.99, 0.99);

    public HealerBlock(final Properties builder)
    {
        super(builder);
        this.setDefaultState(this.stateContainer.getBaseState().with(HealerBlock.FACING, Direction.NORTH).with(
                HealerBlock.FIXED, false));
    }

    @Override
    public TileEntity createTileEntity(final BlockState state, final IBlockReader world)
    {
        return new HealerTile();
    }

    @Override
    protected void fillStateContainer(final StateContainer.Builder<Block, BlockState> builder)
    {
        builder.add(HealerBlock.FACING);
        builder.add(HealerBlock.FIXED);
    }

    @Override
    public VoxelShape getShape(final BlockState state, final IBlockReader worldIn, final BlockPos pos,
            final ISelectionContext context)
    {
        return HealerBlock.SHAPE;
    }

    @Override
    public BlockState getStateForPlacement(final BlockItemUseContext context)
    {
        return this.getDefaultState().with(HealerBlock.FACING, context.getPlacementHorizontalFacing().getOpposite())
                .with(HealerBlock.FIXED, false);
    }

    @Override
    public boolean hasTileEntity(final BlockState state)
    {
        return true;
    }

    @Override
    public boolean onBlockActivated(final BlockState state, final World world, final BlockPos pos,
            final PlayerEntity player, final Hand hand, final BlockRayTraceResult hit)
    {
        player.openContainer(new SimpleNamedContainerProvider((id, playerInventory, playerIn) -> new HealerContainer(id,
                playerInventory, IWorldPosCallable.of(world, pos)), player.getDisplayName()));
        return true;
    }

}
