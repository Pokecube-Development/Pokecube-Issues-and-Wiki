package pokecube.core.blocks.healer;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.HorizontalBlock;
import net.minecraft.block.IWaterLoggable;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.inventory.container.SimpleNamedContainerProvider;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.DirectionProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.IWorldPosCallable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.shapes.IBooleanFunction;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import pokecube.core.inventory.healer.HealerContainer;

public class HealerBlock extends HorizontalBlock implements IWaterLoggable
{
	private static final Map<Direction, VoxelShape> HEALER_MACHINE  = new HashMap<>();
    public static final DirectionProperty FACING = HorizontalBlock.FACING;
    public static final BooleanProperty   FIXED  = BooleanProperty.create("fixed");
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;

    // Precise selection box
    static
    {// @formatter:off
    	HealerBlock.HEALER_MACHINE.put(Direction.NORTH,
          VoxelShapes.join(Block.box(1, 0, 0, 15, 13, 16),
            VoxelShapes.join(Block.box(3, 13, 0, 13, 15, 1),
              VoxelShapes.join(Block.box(7, 13, 1, 9, 14, 15),
                VoxelShapes.join(Block.box(3, 13, 15, 13, 15, 16),
                  VoxelShapes.join(Block.box(10, 13, 11, 13, 14, 14),
                    VoxelShapes.join(Block.box(3, 13, 11, 6, 14, 14),
                      VoxelShapes.join(Block.box(10, 13, 6.5, 13, 14, 9.5),
                        VoxelShapes.join(Block.box(3, 13, 6.5, 6, 14, 9.5),
                            VoxelShapes.join(Block.box(10, 13, 2, 13, 14, 5),
                              Block.box(3, 13, 2, 6, 14, 5),
                                IBooleanFunction.OR), IBooleanFunction.OR), IBooleanFunction.OR),
                          IBooleanFunction.OR), IBooleanFunction.OR), IBooleanFunction.OR),
                    IBooleanFunction.OR), IBooleanFunction.OR), IBooleanFunction.OR)
        );
    	HealerBlock.HEALER_MACHINE.put(Direction.EAST,
          VoxelShapes.join(Block.box(0, 0, 1, 16, 13, 15),
            VoxelShapes.join(Block.box(15, 13, 3, 16, 15, 13),
              VoxelShapes.join(Block.box(1, 13, 7, 15, 14, 9),
                VoxelShapes.join(Block.box(0, 13, 3, 1, 15, 13),
                  VoxelShapes.join(Block.box(2, 13, 10, 5, 14, 13),
                    VoxelShapes.join(Block.box(2, 13, 3, 5, 14, 6),
                      VoxelShapes.join(Block.box(6.5, 13, 10, 9.5, 14, 13),
                        VoxelShapes.join(Block.box(6.5, 13, 3, 9.5, 14, 6),
                            VoxelShapes.join(Block.box(11, 13, 10, 14, 14, 13),
                              Block.box(11, 13, 3, 14, 14, 6),
                                IBooleanFunction.OR), IBooleanFunction.OR), IBooleanFunction.OR),
                          IBooleanFunction.OR), IBooleanFunction.OR), IBooleanFunction.OR),
                    IBooleanFunction.OR), IBooleanFunction.OR), IBooleanFunction.OR)
        );
    	HealerBlock.HEALER_MACHINE.put(Direction.SOUTH,
		VoxelShapes.join(Block.box(1, 0, 0, 15, 13, 16),
        VoxelShapes.join(Block.box(3, 13, 0, 13, 15, 1),
          VoxelShapes.join(Block.box(7, 13, 1, 9, 14, 15),
            VoxelShapes.join(Block.box(3, 13, 15, 13, 15, 16),
              VoxelShapes.join(Block.box(10, 13, 11, 13, 14, 14),
                VoxelShapes.join(Block.box(3, 13, 11, 6, 14, 14),
                  VoxelShapes.join(Block.box(10, 13, 6.5, 13, 14, 9.5),
                    VoxelShapes.join(Block.box(3, 13, 6.5, 6, 14, 9.5),
                        VoxelShapes.join(Block.box(10, 13, 2, 13, 14, 5),
                        	Block.box(3, 13, 2, 6, 14, 5),
                            IBooleanFunction.OR), IBooleanFunction.OR), IBooleanFunction.OR),
                      IBooleanFunction.OR), IBooleanFunction.OR), IBooleanFunction.OR),
                IBooleanFunction.OR), IBooleanFunction.OR), IBooleanFunction.OR)
        );
    	HealerBlock.HEALER_MACHINE.put(Direction.WEST,
		VoxelShapes.join(Block.box(0, 0, 1, 16, 13, 15),
        VoxelShapes.join(Block.box(15, 13, 3, 16, 15, 13),
          VoxelShapes.join(Block.box(1, 13, 7, 15, 14, 9),
            VoxelShapes.join(Block.box(0, 13, 3, 1, 15, 13),
              VoxelShapes.join(Block.box(2, 13, 10, 5, 14, 13),
                VoxelShapes.join(Block.box(2, 13, 3, 5, 14, 6),
                  VoxelShapes.join(Block.box(6.5, 13, 10, 9.5, 14, 13),
                    VoxelShapes.join(Block.box(6.5, 13, 3, 9.5, 14, 6),
                        VoxelShapes.join(Block.box(11, 13, 10, 14, 14, 13),
                          Block.box(11, 13, 3, 14, 14, 6),
                            IBooleanFunction.OR), IBooleanFunction.OR), IBooleanFunction.OR),
                      IBooleanFunction.OR), IBooleanFunction.OR), IBooleanFunction.OR),
                IBooleanFunction.OR), IBooleanFunction.OR), IBooleanFunction.OR)
        );
    }// @formatter:on

    // Precise selection box
    @Override
    public VoxelShape getShape(final BlockState state, final IBlockReader worldIn, final BlockPos pos,
            final ISelectionContext context)
    {
        return HealerBlock.HEALER_MACHINE.get(state.getValue(HealerBlock.FACING));
    }

    public HealerBlock(final Properties builder)
    {
        super(builder);
        this.registerDefaultState(this.stateDefinition.any().setValue(HealerBlock.FACING, Direction.NORTH).setValue(
                HealerBlock.FIXED, false).setValue(HealerBlock.WATERLOGGED, false));
    }

    @Override
    public TileEntity createTileEntity(final BlockState state, final IBlockReader world)
    {
        return new HealerTile();
    }

    @Override
    protected void createBlockStateDefinition(final StateContainer.Builder<Block, BlockState> builder)
    {
        builder.add(HealerBlock.FACING);
        builder.add(HealerBlock.FIXED);
        builder.add(HealerBlock.WATERLOGGED);
    }

    @Override
    public BlockState getStateForPlacement(final BlockItemUseContext context)
    {
        final boolean flag = context.getLevel().getFluidState(context.getClickedPos()).getType() == Fluids.WATER;
        return this.defaultBlockState().setValue(HealerBlock.FACING, context.getHorizontalDirection().getOpposite())
                .setValue(HealerBlock.FIXED, false).setValue(HealerBlock.WATERLOGGED, flag);
    }

    @Override
    @SuppressWarnings("deprecation")
    public BlockState updateShape(final BlockState state, final Direction facing, final BlockState facingState, final IWorld world, final BlockPos currentPos,
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
    public ActionResultType use(final BlockState state, final World world, final BlockPos pos,
            final PlayerEntity player, final Hand hand, final BlockRayTraceResult hit)
    {
        player.openMenu(new SimpleNamedContainerProvider((id, playerInventory, playerIn) -> new HealerContainer(id,
                playerInventory, IWorldPosCallable.create(world, pos)), player.getDisplayName()));
        return ActionResultType.SUCCESS;
    }

}
