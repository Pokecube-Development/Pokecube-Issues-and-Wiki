package pokecube.core.blocks.pc;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.HorizontalBlock;
import net.minecraft.block.IWaterLoggable;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.fluid.Fluids;
import net.minecraft.fluid.IFluidState;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.DirectionProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tags.FluidTags;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.shapes.IBooleanFunction;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import pokecube.core.network.packets.PacketPC;

public class PCBlock extends HorizontalBlock implements IWaterLoggable
{

    private static final Map<Direction, VoxelShape> PC_TOP      = new HashMap<>();
    private static final Map<Direction, VoxelShape> PC_BASE     = new HashMap<>();
    private static final BooleanProperty            WATERLOGGED = BlockStateProperties.WATERLOGGED;

    public static final DirectionProperty FACING = HorizontalBlock.HORIZONTAL_FACING;
    final boolean                         top;
    final boolean                         needsBase;

    // Precise selection box
    static
    {// @formatter:off
        PCBlock.PC_TOP.put(Direction.NORTH,
                VoxelShapes.combineAndSimplify(Block.makeCuboidShape(0.34, 0.05, 5.61, 15.66, 16.81, 15.83),
                        Block.makeCuboidShape(0.26, 0.05, 9.71, 15.74, 16.94, 11.72), IBooleanFunction.OR)
        );
        PCBlock.PC_TOP.put(Direction.EAST,
                VoxelShapes.combineAndSimplify(Block.makeCuboidShape(0.17, 0.05, 0.34, 10.39, 16.81, 15.66),
                        Block.makeCuboidShape(4.28, 0.05, 0.26, 6.29, 16.94, 15.74), IBooleanFunction.OR)
        );
        PCBlock.PC_TOP.put(Direction.SOUTH,
                VoxelShapes.combineAndSimplify(Block.makeCuboidShape(0.34, 0.05, 0.17, 15.66, 16.81, 10.39),
                        Block.makeCuboidShape(0.26, 0.05, 4.28, 15.74, 16.94, 6.29), IBooleanFunction.OR)
        );
        PCBlock.PC_TOP.put(Direction.WEST,
                VoxelShapes.combineAndSimplify(Block.makeCuboidShape(5.61, 0.05, 0.34, 15.83, 16.81, 15.66),
                        Block.makeCuboidShape(9.71, 0.05, 0.26, 11.72, 16.94, 15.74), IBooleanFunction.OR)
        );
        PCBlock.PC_BASE.put(Direction.NORTH,
                VoxelShapes.combineAndSimplify(Block.makeCuboidShape(0.34, 0.05, 5.61, 15.66, 16.05, 15.83),
                        VoxelShapes.combineAndSimplify(Block.makeCuboidShape(1.46, 0.04, 2.3, 14.54, 14.95, 6.62),
                                VoxelShapes.combineAndSimplify(Block.makeCuboidShape(1.39, 12.06, 0.81, 14.61, 18.16, 7.47),
                                        Block.makeCuboidShape(0.26, 0.05, 9.72, 15.74, 16.049, 11.72),
                                        IBooleanFunction.OR), IBooleanFunction.OR), IBooleanFunction.OR)
        );
        PCBlock.PC_BASE.put(Direction.EAST,
                VoxelShapes.combineAndSimplify(Block.makeCuboidShape(0.17, 0.05, 0.34, 10.39, 16.05, 15.66),
                        VoxelShapes.combineAndSimplify(Block.makeCuboidShape(9.38, 0.04, 1.46, 13.7, 14.95, 14.54),
                                VoxelShapes.combineAndSimplify(Block.makeCuboidShape(8.53, 12.06, 1.39, 15.19, 18.16, 14.61),
                                        Block.makeCuboidShape(4.28, 0.05, 0.26, 6.28, 16.049, 15.74),
                                        IBooleanFunction.OR), IBooleanFunction.OR), IBooleanFunction.OR)
        );
        PCBlock.PC_BASE.put(Direction.SOUTH,
                VoxelShapes.combineAndSimplify(Block.makeCuboidShape(0.34, 0.05, 0.17, 15.66, 16.05, 10.39),
                        VoxelShapes.combineAndSimplify(Block.makeCuboidShape(1.46, 0.04, 9.38, 14.54, 14.95, 13.7),
                                VoxelShapes.combineAndSimplify(Block.makeCuboidShape(1.39, 12.06, 8.53, 14.61, 18.16, 15.19),
                                        Block.makeCuboidShape(0.26, 0.05, 4.28, 15.74, 16.049, 6.28),
                                        IBooleanFunction.OR), IBooleanFunction.OR), IBooleanFunction.OR)
        );
        PCBlock.PC_BASE.put(Direction.WEST,
                VoxelShapes.combineAndSimplify(Block.makeCuboidShape(5.61, 0.05, 0.34, 15.83, 16.05, 15.66),
                        VoxelShapes.combineAndSimplify(Block.makeCuboidShape(2.3, 0.04, 1.46, 6.62, 14.95, 14.54),
                                VoxelShapes.combineAndSimplify(Block.makeCuboidShape(0.81, 12.06, 1.39, 7.47, 18.16, 14.61),
                                        Block.makeCuboidShape(9.72, 0.05, 0.26, 11.72, 16.049, 15.74),
                                        IBooleanFunction.OR), IBooleanFunction.OR), IBooleanFunction.OR)
        );
    }// @formatter:on

    // Precise selection box
    @Override
    public VoxelShape getShape(final BlockState state, final IBlockReader worldIn, final BlockPos pos,
            final ISelectionContext context)
    {
        if (this.top) return PCBlock.PC_TOP.get(state.get(PCBlock.FACING));
        else return PCBlock.PC_BASE.get(state.get(PCBlock.FACING));
    }

    public PCBlock(final Properties properties, final boolean top)
    {
        this(properties, top, true);
    }

    // Default States
    public PCBlock(final Properties properties, final boolean top, final boolean needsBase)
    {
        super(properties);
        this.top = top;
        this.needsBase = needsBase;
        this.setDefaultState(this.stateContainer.getBaseState().with(PCBlock.FACING, Direction.NORTH).with(
                PCBlock.WATERLOGGED, false));
    }

    @Override
    public TileEntity createTileEntity(final BlockState state, final IBlockReader world)
    {
        return new PCTile();
    }

    @Override
    protected void fillStateContainer(final StateContainer.Builder<Block, BlockState> builder)
    {
        builder.add(PCBlock.FACING, PCBlock.WATERLOGGED);
    }

    // Waterloggging on placement
    @Override
    public BlockState getStateForPlacement(final BlockItemUseContext context)
    {
        final IFluidState ifluidstate = context.getWorld().getFluidState(context.getPos());
        return Objects.requireNonNull(super.getStateForPlacement(context)).with(PCBlock.FACING, context
                .getPlacementHorizontalFacing().getOpposite()).with(PCBlock.WATERLOGGED, ifluidstate.isTagged(
                        FluidTags.WATER) && ifluidstate.getLevel() == 8);
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
        if (this.top)
        {
            if (!this.needsBase || world.getBlockState(pos.down()).getBlock() instanceof PCBlock)
                if (player instanceof ServerPlayerEntity) PacketPC.sendOpenPacket(player, player.getUniqueID(), pos);
            return true;
        }
        else return false;
    }

    // Adds Waterlogging
    @SuppressWarnings("deprecation")
    @Override
    public IFluidState getFluidState(final BlockState state)
    {
        return state.get(PCBlock.WATERLOGGED) ? Fluids.WATER.getStillFluidState(false) : super.getFluidState(state);
    }
}
