package pokecube.adventures.blocks.genetics.cloner;

import net.minecraft.block.*;
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

import org.jetbrains.annotations.NotNull;

import pokecube.core.blocks.InteractableHorizontalBlock;

public class ClonerBlock extends InteractableHorizontalBlock implements IWaterLoggable
{
    private static final EnumProperty<ClonerBlockPart> HALF = EnumProperty.create("half", ClonerBlockPart.class);
    private static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;

    //Precise selection box
    private static final VoxelShape CLONER_BOTTOM = VoxelShapes.or(
            makeCuboidShape(0, 0, 0, 16, 12, 16),
            makeCuboidShape(0.74, 12, 0.74, 15.26, 13, 15.26),
            makeCuboidShape(1.13, 13, 1.02, 15.03, 16, 14.93))
      .simplify();

    private static final VoxelShape CLONER_TOP = VoxelShapes.or(
            makeCuboidShape(0, 12, 0, 16, 16, 16),
            makeCuboidShape(0.74, 11, 0.74, 15.26, 12, 15.26),
            makeCuboidShape(1.13, 0, 1.02, 15.03, 11, 14.93))
      .simplify();

    //Precise selection box
    @NotNull
    public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context)
    {
        ClonerBlockPart half = state.get(HALF);
        if (half == ClonerBlockPart.BOTTOM)
        {
            return CLONER_BOTTOM;
        }
        else {
            return CLONER_TOP;
        }
    }

    //Default States
    public ClonerBlock(Properties properties)
    {
        super(properties);
        this.setDefaultState(this.stateContainer.getBaseState()
                .with(HALF, ClonerBlockPart.BOTTOM)
                .with(HorizontalBlock.HORIZONTAL_FACING, Direction.NORTH)
                .with(WATERLOGGED, false));
    }

    //Places Cloner with both top and bottom pieces
    @Override
    public void onBlockPlacedBy(World world, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack)
    {
        if (placer != null)
        {
            IFluidState fluidState = world.getFluidState(pos.up());
            world.setBlockState(pos.up(),
              state.with(HALF, ClonerBlockPart.TOP)
                .with(WATERLOGGED,  fluidState.getFluid() == Fluids.WATER), 1);
        }
    }

    //Breaking Cloner breaks both parts and returns one item only
    public void onBlockHarvested(World world, @NotNull BlockPos pos, BlockState state, @NotNull PlayerEntity player)
    {
        Direction facing = state.get(HORIZONTAL_FACING);
        BlockPos clonerPos = getClonerPos(pos, state.get(HALF), facing);
        BlockState clonerBlockState = world.getBlockState(clonerPos);
        if (clonerBlockState.getBlock() == this && !pos.equals(clonerPos))
        {
            removeHalf(world, clonerPos, clonerBlockState);
        }
        BlockPos clonerPartPos = getClonerTopPos(clonerPos, facing);
        clonerBlockState = world.getBlockState(clonerPartPos);
        if (clonerBlockState.getBlock() == this && !pos.equals(clonerPartPos))
        {
            removeHalf(world, clonerPartPos, clonerBlockState);
        }
        super.onBlockHarvested(world, pos, state, player);
    }

    private BlockPos getClonerTopPos(BlockPos base, Direction facing)
    {
        switch (facing) {
            default:
                return base.up();
        }
    }

    private BlockPos getClonerPos(BlockPos pos, ClonerBlockPart part, Direction facing)
    {
        if (part == ClonerBlockPart.BOTTOM) return pos;
        switch (facing) {
            default:
                return pos.down();
        }
    }

    //Breaking the Cloner leaves water if underwater
    private void removeHalf(World world, BlockPos pos, BlockState state)
    {
        IFluidState fluidState = world.getFluidState(pos);
        if (fluidState.getFluid() == Fluids.WATER)
        {
            world.setBlockState(pos, fluidState.getBlockState(), 35);
        }
        else {
            world.setBlockState(pos, Blocks.AIR.getDefaultState(), 35);
        }
    }

    //Prevents the Cloner from replacing blocks above it and checks for water
    @Override
    public BlockState getStateForPlacement(BlockItemUseContext context)
    {
        IFluidState ifluidstate = context.getWorld().getFluidState(context.getPos());
        BlockPos pos = context.getPos();

        BlockPos clonerPos = getClonerTopPos(pos, context.getPlacementHorizontalFacing().getOpposite());
        if (pos.getY() < 255 &&
                clonerPos.getY() < 255 && context.getWorld().getBlockState(pos.up()).isReplaceable(context))
        {
            return this.getDefaultState()
                    .with(HorizontalBlock.HORIZONTAL_FACING, context.getPlacementHorizontalFacing().getOpposite())
                    .with(HALF, ClonerBlockPart.BOTTOM)
                    .with(WATERLOGGED, ifluidstate.isTagged(FluidTags.WATER) && ifluidstate.getLevel() == 8);
        }
        return null;
    }

    @NotNull
    @SuppressWarnings("deprecation")
    @Override
    public IFluidState getFluidState(BlockState state)
    {
        return state.get(WATERLOGGED) ? Fluids.WATER.getStillFluidState(false) : super.getFluidState(state);
    }

    protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder)
    {
        builder.add(HALF, HorizontalBlock.HORIZONTAL_FACING, WATERLOGGED);
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
    public boolean canRenderInLayer(BlockState state, BlockRenderLayer layer)
    {
        return (layer == BlockRenderLayer.CUTOUT) || (layer == BlockRenderLayer.TRANSLUCENT);
    }
}
