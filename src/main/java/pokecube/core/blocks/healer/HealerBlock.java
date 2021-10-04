package pokecube.core.blocks.healer;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import pokecube.core.inventory.healer.HealerContainer;

public class HealerBlock extends HorizontalDirectionalBlock implements SimpleWaterloggedBlock
{
	private static final Map<Direction, VoxelShape> POKECENTER  = new HashMap<>();
    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
    public static final BooleanProperty   FIXED  = BooleanProperty.create("fixed");
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;

    // Precise selection box
    static
    {
    	HealerBlock.POKECENTER.put(Direction.NORTH, Shapes.or(
            Block.box(1, 0, 0, 15, 13, 16),
            Block.box(2, 13, 0, 14, 15, 1),
            Block.box(7, 13, 1, 9, 14, 15),
            Block.box(2, 13, 15, 14, 15, 16),
            Block.box(10, 13, 11, 13, 14, 14),
            Block.box(3, 13, 11, 6, 14, 14),
            Block.box(10, 13, 6.5, 13, 14, 9.5),
            Block.box(3, 13, 6.5, 6, 14, 9.5),
            Block.box(10, 13, 2, 13, 14, 5),
            Block.box(3, 13, 2, 6, 14, 5)).optimize());
    	HealerBlock.POKECENTER.put(Direction.EAST, Shapes.or(
            Block.box(0, 0, 1, 16, 13, 15),
            Block.box(15, 13, 2, 16, 15, 14),
            Block.box(1, 13, 7, 15, 14, 9),
            Block.box(0, 13, 2, 1, 15, 14),
            Block.box(2, 13, 10, 5, 14, 13),
            Block.box(2, 13, 3, 5, 14, 6),
            Block.box(6.5, 13, 10, 9.5, 14, 13),
            Block.box(6.5, 13, 3, 9.5, 14, 6),
            Block.box(11, 13, 10, 14, 14, 13),
            Block.box(11, 13, 3, 14, 14, 6)).optimize());
    	HealerBlock.POKECENTER.put(Direction.SOUTH, Shapes.or(
		    Block.box(1, 0, 0, 15, 13, 16),
            Block.box(2, 13, 0, 14, 15, 1),
            Block.box(7, 13, 1, 9, 14, 15),
            Block.box(2, 13, 15, 14, 15, 16),
            Block.box(10, 13, 11, 13, 14, 14),
            Block.box(3, 13, 11, 6, 14, 14),
            Block.box(10, 13, 6.5, 13, 14, 9.5),
            Block.box(3, 13, 6.5, 6, 14, 9.5),
            Block.box(10, 13, 2, 13, 14, 5),
            Block.box(3, 13, 2, 6, 14, 5)).optimize());
    	HealerBlock.POKECENTER.put(Direction.WEST, Shapes.or(
		    Block.box(0, 0, 1, 16, 13, 15),
            Block.box(15, 13, 2, 16, 15, 14),
            Block.box(1, 13, 7, 15, 14, 9),
            Block.box(0, 13, 2, 1, 15, 14),
            Block.box(2, 13, 10, 5, 14, 13),
            Block.box(2, 13, 3, 5, 14, 6),
            Block.box(6.5, 13, 10, 9.5, 14, 13),
            Block.box(6.5, 13, 3, 9.5, 14, 6),
            Block.box(11, 13, 10, 14, 14, 13),
            Block.box(11, 13, 3, 14, 14, 6)).optimize());
    }

    // Precise selection box
    @Override
    public VoxelShape getShape(final BlockState state, final BlockGetter worldIn, final BlockPos pos,
           final CollisionContext context)
    {
        return HealerBlock.POKECENTER.get(state.getValue(HealerBlock.FACING));
    }

    public HealerBlock(final Properties builder)
    {
        super(builder);
        this.registerDefaultState(this.stateDefinition.any().setValue(HealerBlock.FACING, Direction.NORTH).setValue(
                HealerBlock.FIXED, false).setValue(HealerBlock.WATERLOGGED, false));
    }

    @Override
    public BlockEntity createTileEntity(final BlockState state, final BlockGetter world)
    {
        return new HealerTile();
    }

    @Override
    protected void createBlockStateDefinition(final StateDefinition.Builder<Block, BlockState> builder)
    {
        builder.add(HealerBlock.FACING);
        builder.add(HealerBlock.FIXED);
        builder.add(HealerBlock.WATERLOGGED);
    }

    @Override
    public BlockState getStateForPlacement(final BlockPlaceContext context)
    {
        final boolean flag = context.getLevel().getFluidState(context.getClickedPos()).getType() == Fluids.WATER;
        return this.defaultBlockState().setValue(HealerBlock.FACING, context.getHorizontalDirection().getOpposite())
                .setValue(HealerBlock.FIXED, false).setValue(HealerBlock.WATERLOGGED, flag);
    }

    @Override
    @SuppressWarnings("deprecation")
    public BlockState updateShape(final BlockState state, final Direction facing, final BlockState facingState, final LevelAccessor world, final BlockPos currentPos,
            final BlockPos facingPos)
    {
        if (state.getValue(HealerBlock.WATERLOGGED)) world.getLiquidTicks().scheduleTick(currentPos, Fluids.WATER, Fluids.WATER.getTickDelay(world));
        return super.updateShape(state, facing, facingState, world, currentPos, facingPos);
    }

    @Override
    @SuppressWarnings("deprecation")
    public FluidState getFluidState(final BlockState state)
    {
        return state.getValue(HealerBlock.WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(state);
    }

    @Override
    public boolean hasTileEntity(final BlockState state)
    {
        return true;
    }

    @Override
    public InteractionResult use(final BlockState state, final Level world, final BlockPos pos,
            final Player player, final InteractionHand hand, final BlockHitResult hit)
    {
        player.openMenu(new SimpleMenuProvider((id, playerInventory, playerIn) -> new HealerContainer(id,
                playerInventory, ContainerLevelAccess.create(world, pos)), player.getDisplayName()));
        return InteractionResult.SUCCESS;
    }

}
