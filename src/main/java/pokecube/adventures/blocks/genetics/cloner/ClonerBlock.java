package pokecube.adventures.blocks.genetics.cloner;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import pokecube.core.blocks.InteractableHorizontalBlock;

public class ClonerBlock extends InteractableHorizontalBlock implements SimpleWaterloggedBlock
{
    public static final EnumProperty<ClonerBlockPart> HALF        = EnumProperty.create("half", ClonerBlockPart.class);
    public static final BooleanProperty               WATERLOGGED = BlockStateProperties.WATERLOGGED;

    // Precise selection box @formatter:off
    private static final VoxelShape CLONER_BOTTOM = Shapes.or(
            Block.box(0, 0, 0, 16, 12, 16),
            Block.box(0.5, 12, 0.5, 15.5, 13, 15.5),
            Block.box(1, 13, 1, 15, 16, 15)).optimize();

    private static final VoxelShape CLONER_TOP = Shapes.or(
            Block.box(0, 12, 0, 16, 16, 16),
            Block.box(0.5, 11, 0.5, 15.5, 12, 15.5),
            Block.box(1, 0, 1, 15, 11, 15)).optimize();
    // Precise selection box @formatter:on

    @Override
    public VoxelShape getShape(final BlockState state, final BlockGetter worldIn, final BlockPos pos,
            final CollisionContext context)
    {
        final ClonerBlockPart half = state.getValue(ClonerBlock.HALF);
        if (half == ClonerBlockPart.BOTTOM) return ClonerBlock.CLONER_BOTTOM;
        else return ClonerBlock.CLONER_TOP;
    }

    // Default States
    public ClonerBlock(final Properties properties)
    {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(ClonerBlock.HALF, ClonerBlockPart.BOTTOM)
                .setValue(HorizontalDirectionalBlock.FACING, Direction.NORTH).setValue(ClonerBlock.WATERLOGGED, false));
    }

    // Places Cloner with both top and bottom pieces
    @Override
    public void setPlacedBy(final Level world, final BlockPos pos, final BlockState state, final LivingEntity placer,
            final ItemStack stack)
    {
        if (placer != null)
        {
            final FluidState fluidState = world.getFluidState(pos.above());
            world.setBlock(pos.above(), state.setValue(ClonerBlock.HALF, ClonerBlockPart.TOP).setValue(
                    ClonerBlock.WATERLOGGED, fluidState.getType() == Fluids.WATER), 1);
            final BlockEntity tile = world.getBlockEntity(pos.above());
            if (tile != null)
            {
                // Refresh the block state for the tile, incase it wasn't set
                // properly and is needed.
                tile.clearCache();
                tile.getBlockState();
            }

        }
        super.setPlacedBy(world, pos, state, placer, stack);
    }

    @Override
    public InteractionResult use(final BlockState state, final Level world, final BlockPos pos,
            final Player player, final InteractionHand hand, final BlockHitResult hit)
    {
        return super.use(state, world, pos, player, hand, hit);
    }

    // Breaking Cloner breaks both parts and returns one item only
    @Override
    public void playerWillDestroy(final Level world, final BlockPos pos, final BlockState state,
            final Player player)
    {
        final Direction facing = state.getValue(HorizontalDirectionalBlock.FACING);
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
    private void removeHalf(final Level world, final BlockPos pos, final BlockState state, final Player player)
    {
        final BlockState blockstate = world.getBlockState(pos);
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
    public BlockState getStateForPlacement(final BlockPlaceContext context)
    {
        final FluidState ifluidstate = context.getLevel().getFluidState(context.getClickedPos());
        final BlockPos pos = context.getClickedPos();

        final BlockPos clonerPos = this.getClonerTopPos(pos, context.getHorizontalDirection().getOpposite());
        if (pos.getY() < 255 && clonerPos.getY() < 255 && context.getLevel().getBlockState(pos.above()).canBeReplaced(
                context)) return this.defaultBlockState().setValue(HorizontalDirectionalBlock.FACING, context
                        .getHorizontalDirection().getOpposite()).setValue(ClonerBlock.HALF, ClonerBlockPart.BOTTOM)
                        .setValue(ClonerBlock.WATERLOGGED, ifluidstate.is(FluidTags.WATER) && ifluidstate
                                .getAmount() == 8);
        return null;
    }

    @Override
    @SuppressWarnings("deprecation")
    public BlockState updateShape(final BlockState state, final Direction facing, final BlockState facingState,
            final LevelAccessor world, final BlockPos currentPos, final BlockPos facingPos)
    {
        if (state.getValue(ClonerBlock.WATERLOGGED)) world.getLiquidTicks().scheduleTick(currentPos, Fluids.WATER,
                Fluids.WATER.getTickDelay(world));
        return super.updateShape(state, facing, facingState, world, currentPos, facingPos);
    }

    @SuppressWarnings("deprecation")
    @Override
    public FluidState getFluidState(final BlockState state)
    {
        return state.getValue(ClonerBlock.WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(state);
    }

    @Override
    protected void createBlockStateDefinition(final StateDefinition.Builder<Block, BlockState> builder)
    {
        builder.add(ClonerBlock.HALF, HorizontalDirectionalBlock.FACING, ClonerBlock.WATERLOGGED);
    }

    @Override
    public BlockEntity createTileEntity(final BlockState state, final BlockGetter world)
    {
        return new ClonerTile();
    }

    @Override
    public boolean hasTileEntity(final BlockState state)
    {
        return true;// state.getValue(ClonerBlock.HALF) ==
                    // ClonerBlockPart.BOTTOM;
    }

    @Override
    public float[] getBeaconColorMultiplier(final BlockState state, final LevelReader world, final BlockPos pos,
            final BlockPos beaconPos)
    {
        return new float[] { 0.62f, 0.85f, 1.00f };
    }
}
