package pokecube.adventures.blocks.genetics.cloner;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.HorizontalBlock;
import net.minecraft.block.IWaterLoggable;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.EnumProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tags.FluidTags;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;
import pokecube.core.blocks.InteractableHorizontalBlock;

public class ClonerBlock extends InteractableHorizontalBlock implements IWaterLoggable
{
    public static final EnumProperty<ClonerBlockPart> HALF        = EnumProperty.create("half", ClonerBlockPart.class);
    public static final BooleanProperty               WATERLOGGED = BlockStateProperties.WATERLOGGED;

    // Precise selection box
    private static final VoxelShape CLONER_BOTTOM = VoxelShapes.or(
            Block.box(0, 0, 0, 16, 12, 16),
            Block.box(0.5, 12, 0.5, 15.5, 13, 15.5),
            Block.box(1, 13, 1, 15, 16, 15)).optimize();

    private static final VoxelShape CLONER_TOP = VoxelShapes.or(
            Block.box(0, 12, 0, 16, 16, 16),
            Block.box(0.5, 11, 0.5, 15.5, 12, 15.5),
            Block.box(1, 0, 1, 15, 11, 15)).optimize();

    // Precise selection box
    @Override
    public VoxelShape getShape(final BlockState state, final IBlockReader worldIn, final BlockPos pos,
            final ISelectionContext context)
    {
        final ClonerBlockPart half = state.getValue(ClonerBlock.HALF);
        if (half == ClonerBlockPart.BOTTOM) return ClonerBlock.CLONER_BOTTOM;
        else return ClonerBlock.CLONER_TOP;
    }

    // Default States
    public ClonerBlock(final Properties properties)
    {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(ClonerBlock.HALF, ClonerBlockPart.BOTTOM).setValue(
                HorizontalBlock.FACING, Direction.NORTH).setValue(ClonerBlock.WATERLOGGED, false));
    }

    // Places Cloner with both top and bottom pieces
    @Override
    public void setPlacedBy(final World world, final BlockPos pos, final BlockState state,
            final LivingEntity placer, final ItemStack stack)
    {
        if (placer != null)
        {
            final FluidState fluidState = world.getFluidState(pos.above());
            world.setBlock(pos.above(), state.setValue(ClonerBlock.HALF, ClonerBlockPart.TOP).setValue(
                    ClonerBlock.WATERLOGGED, fluidState.getType() == Fluids.WATER), 1);
        }
    }

    // Breaking Cloner breaks both parts and returns one item only
    @Override
    public void playerWillDestroy(final World world, final BlockPos pos, final BlockState state,
            final PlayerEntity player)
    {
        final Direction facing = state.getValue(HorizontalBlock.FACING);
        final BlockPos clonerPos = this.getClonerPos(pos, state.getValue(ClonerBlock.HALF), facing);
        BlockState clonerBlockState = world.getBlockState(clonerPos);
        if (clonerBlockState.getBlock() == this && !pos.equals(clonerPos)) this.removeHalf(world, clonerPos,
                clonerBlockState, player);
        final BlockPos clonerPartPos = this.getClonerTopPos(clonerPos, facing);
        clonerBlockState = world.getBlockState(clonerPartPos);
        if (clonerBlockState.getBlock() == this && !pos.equals(clonerPartPos)) this.removeHalf(world, clonerPartPos,
                clonerBlockState, player);
        super.playerWillDestroy(world, pos, state, player);
    }

    // Breaking the Cloner leaves water if underwater
    private void removeHalf(final World world, final BlockPos pos, final BlockState state, PlayerEntity player)
    {
        BlockState blockstate = world.getBlockState(pos);
        final FluidState fluidState = world.getFluidState(pos);
        if (fluidState.getType() == Fluids.WATER) world.setBlock(pos, fluidState.createLegacyBlock(), 35);
        else
        {
            world.setBlock(pos, Blocks.AIR.defaultBlockState(), 35);
            world.levelEvent(player, 2001, pos, Block.getId(blockstate));
        }
    }

    private BlockPos getClonerTopPos(final BlockPos base, final Direction facing)
    {
        switch (facing)
        {
        default:
            return base.above();
        }
    }

    private BlockPos getClonerPos(final BlockPos pos, final ClonerBlockPart part, final Direction facing)
    {
        if (part == ClonerBlockPart.BOTTOM) return pos;
        switch (facing)
        {
        default:
            return pos.below();
        }
    }

    // Prevents the Cloner from replacing blocks above it and checks for water
    @Override
    public BlockState getStateForPlacement(final BlockItemUseContext context)
    {
        final FluidState ifluidstate = context.getLevel().getFluidState(context.getClickedPos());
        final BlockPos pos = context.getClickedPos();

        final BlockPos clonerPos = this.getClonerTopPos(pos, context.getHorizontalDirection().getOpposite());
        if (pos.getY() < 255 && clonerPos.getY() < 255 && context.getLevel().getBlockState(pos.above()).canBeReplaced(
                context)) return this.defaultBlockState().setValue(HorizontalBlock.FACING, context
                        .getHorizontalDirection().getOpposite()).setValue(ClonerBlock.HALF, ClonerBlockPart.BOTTOM)
                        .setValue(ClonerBlock.WATERLOGGED, ifluidstate.is(FluidTags.WATER) && ifluidstate
                                .getAmount() == 8);
        return null;
    }

    @Override
    @SuppressWarnings("deprecation")
    public BlockState updateShape(final BlockState state, final Direction facing, final BlockState facingState, final IWorld world, final BlockPos currentPos,
                                  final BlockPos facingPos)
    {
        if (state.getValue(ClonerBlock.WATERLOGGED)) world.getLiquidTicks().scheduleTick(currentPos, Fluids.WATER, Fluids.WATER.getTickDelay(world));
        return super.updateShape(state, facing, facingState, world, currentPos, facingPos);
    }

    @SuppressWarnings("deprecation")
    @Override
    public FluidState getFluidState(final BlockState state)
    {
        return state.getValue(ClonerBlock.WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(state);
    }

    @Override
    protected void createBlockStateDefinition(final StateContainer.Builder<Block, BlockState> builder)
    {
        builder.add(ClonerBlock.HALF, HorizontalBlock.FACING, ClonerBlock.WATERLOGGED);
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
    public float[] getBeaconColorMultiplier(BlockState state, IWorldReader world, BlockPos pos, BlockPos beaconPos) {
        return new float[]{0.62f, 0.85f, 1.00f};
    }
}
