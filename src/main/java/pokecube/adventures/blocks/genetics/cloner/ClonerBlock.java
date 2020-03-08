package pokecube.adventures.blocks.genetics.cloner;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.HorizontalBlock;
import net.minecraft.block.IWaterLoggable;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluids;
import net.minecraft.fluid.IFluidState;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.EnumProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tags.FluidTags;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import pokecube.core.blocks.InteractableHorizontalBlock;

public class ClonerBlock extends InteractableHorizontalBlock implements IWaterLoggable
{
    private static final EnumProperty<ClonerBlockPart> HALF        = EnumProperty.create("half", ClonerBlockPart.class);
    private static final BooleanProperty               WATERLOGGED = BlockStateProperties.WATERLOGGED;

    // Precise selection box
    private static final VoxelShape CLONER_BOTTOM = VoxelShapes.or(Block.makeCuboidShape(0, 0, 0, 16, 12, 16), Block
            .makeCuboidShape(0.74, 12, 0.74, 15.26, 13, 15.26), Block.makeCuboidShape(1.13, 13, 1.02, 15.03, 16, 14.93))
            .simplify();

    private static final VoxelShape CLONER_TOP = VoxelShapes.or(Block.makeCuboidShape(0, 12, 0, 16, 16, 16), Block
            .makeCuboidShape(0.74, 11, 0.74, 15.26, 12, 15.26), Block.makeCuboidShape(1.13, 0, 1.02, 15.03, 11, 14.93))
            .simplify();

    // Precise selection box
    @Override
    public VoxelShape getShape(final BlockState state, final IBlockReader worldIn, final BlockPos pos,
            final ISelectionContext context)
    {
        final ClonerBlockPart half = state.get(ClonerBlock.HALF);
        if (half == ClonerBlockPart.BOTTOM) return ClonerBlock.CLONER_BOTTOM;
        else return ClonerBlock.CLONER_TOP;
    }

    // Default States
    public ClonerBlock(final Properties properties)
    {
        super(properties);
        this.setDefaultState(this.stateContainer.getBaseState().with(ClonerBlock.HALF, ClonerBlockPart.BOTTOM).with(
                HorizontalBlock.HORIZONTAL_FACING, Direction.NORTH).with(ClonerBlock.WATERLOGGED, false));
    }

    // Places Cloner with both top and bottom pieces
    @Override
    public void onBlockPlacedBy(final World world, final BlockPos pos, final BlockState state,
            final LivingEntity placer, final ItemStack stack)
    {
        if (placer != null)
        {
            final IFluidState fluidState = world.getFluidState(pos.up());
            world.setBlockState(pos.up(), state.with(ClonerBlock.HALF, ClonerBlockPart.TOP).with(
                    ClonerBlock.WATERLOGGED, fluidState.getFluid() == Fluids.WATER), 1);
        }
    }

    // Breaking Cloner breaks both parts and returns one item only
    @Override
    public void onBlockHarvested(final World world, final BlockPos pos, final BlockState state,
            final PlayerEntity player)
    {
        final Direction facing = state.get(HorizontalBlock.HORIZONTAL_FACING);
        final BlockPos clonerPos = this.getClonerPos(pos, state.get(ClonerBlock.HALF), facing);
        BlockState clonerBlockState = world.getBlockState(clonerPos);
        if (clonerBlockState.getBlock() == this && !pos.equals(clonerPos)) this.removeHalf(world, clonerPos,
                clonerBlockState);
        final BlockPos clonerPartPos = this.getClonerTopPos(clonerPos, facing);
        clonerBlockState = world.getBlockState(clonerPartPos);
        if (clonerBlockState.getBlock() == this && !pos.equals(clonerPartPos)) this.removeHalf(world, clonerPartPos,
                clonerBlockState);
        super.onBlockHarvested(world, pos, state, player);
    }

    private BlockPos getClonerTopPos(final BlockPos base, final Direction facing)
    {
        switch (facing)
        {
        default:
            return base.up();
        }
    }

    private BlockPos getClonerPos(final BlockPos pos, final ClonerBlockPart part, final Direction facing)
    {
        if (part == ClonerBlockPart.BOTTOM) return pos;
        switch (facing)
        {
        default:
            return pos.down();
        }
    }

    // Breaking the Cloner leaves water if underwater
    private void removeHalf(final World world, final BlockPos pos, final BlockState state)
    {
        final IFluidState fluidState = world.getFluidState(pos);
        if (fluidState.getFluid() == Fluids.WATER) world.setBlockState(pos, fluidState.getBlockState(), 35);
        else world.setBlockState(pos, Blocks.AIR.getDefaultState(), 35);
    }

    // Prevents the Cloner from replacing blocks above it and checks for water
    @Override
    public BlockState getStateForPlacement(final BlockItemUseContext context)
    {
        final IFluidState ifluidstate = context.getWorld().getFluidState(context.getPos());
        final BlockPos pos = context.getPos();

        final BlockPos clonerPos = this.getClonerTopPos(pos, context.getPlacementHorizontalFacing().getOpposite());
        if (pos.getY() < 255 && clonerPos.getY() < 255 && context.getWorld().getBlockState(pos.up()).isReplaceable(
                context)) return this.getDefaultState().with(HorizontalBlock.HORIZONTAL_FACING, context
                        .getPlacementHorizontalFacing().getOpposite()).with(ClonerBlock.HALF, ClonerBlockPart.BOTTOM)
                        .with(ClonerBlock.WATERLOGGED, ifluidstate.isTagged(FluidTags.WATER) && ifluidstate
                                .getLevel() == 8);
        return null;
    }

    @SuppressWarnings("deprecation")
    @Override
    public IFluidState getFluidState(final BlockState state)
    {
        return state.get(ClonerBlock.WATERLOGGED) ? Fluids.WATER.getStillFluidState(false) : super.getFluidState(state);
    }

    @Override
    protected void fillStateContainer(final StateContainer.Builder<Block, BlockState> builder)
    {
        builder.add(ClonerBlock.HALF, HorizontalBlock.HORIZONTAL_FACING, ClonerBlock.WATERLOGGED);
    }

    @Override
    public TileEntity createTileEntity(final BlockState state, final IBlockReader world)
    {
        return new ClonerTile();
    }

    @Override
    public boolean hasTileEntity(final BlockState state)
    {
        return true;
    }

    @Override
    public boolean canRenderInLayer(final BlockState state, final BlockRenderLayer layer)
    {
        return layer == BlockRenderLayer.CUTOUT || layer == BlockRenderLayer.TRANSLUCENT;
    }
}
