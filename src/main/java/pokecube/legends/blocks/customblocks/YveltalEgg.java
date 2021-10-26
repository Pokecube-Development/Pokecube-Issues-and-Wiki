package pokecube.legends.blocks.customblocks;

import javax.annotation.Nullable;

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
import net.minecraft.state.DirectionProperty;
import net.minecraft.state.EnumProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;

public class YveltalEgg extends Rotates implements IWaterLoggable
{
    private static final EnumProperty<YveltalEggPart> HALF           = EnumProperty.create("half",
            YveltalEggPart.class);
    private static final BooleanProperty              WATERLOGGED    = BlockStateProperties.WATERLOGGED;
    private static final DirectionProperty            FACING         = HorizontalBlock.FACING;

    // Precise selection box
    private static final VoxelShape YVELTAL_TOP = VoxelShapes.or(
        Block.box(1, 0, 1, 15, 6, 15),
        Block.box(2, 6, 2, 14, 10, 14)).optimize();

    private static final VoxelShape YVELTAL_BOTTOM = VoxelShapes.or(
        Block.box(2, 0, 2, 14, 4, 14),
        Block.box(1, 4, 1, 15, 16, 15)).optimize();

    // Precise selection box
    @Override
    public VoxelShape getShape(final BlockState state, final IBlockReader worldIn, final BlockPos pos,
            final ISelectionContext context)
    {
        final YveltalEggPart half = state.getValue(YveltalEgg.HALF);
        if (half == YveltalEggPart.BOTTOM) {
            return YveltalEgg.YVELTAL_BOTTOM;
        } else
        {
            return YveltalEgg.YVELTAL_TOP;
        }
    }

    public YveltalEgg(final Properties props)
    {
        super(props);
        this.registerDefaultState(this.stateDefinition.any().setValue(YveltalEgg.FACING, Direction.NORTH).setValue(
                YveltalEgg.WATERLOGGED, false).setValue(YveltalEgg.HALF, YveltalEggPart.BOTTOM));
    }

    // Places Yveltal Egg Spawner with both top and bottom pieces
    @Override
    public void setPlacedBy(final World world, final BlockPos pos, final BlockState state,
            @Nullable final LivingEntity entity, final ItemStack stack)
    {
        if (entity != null)
        {
            final FluidState fluidState = world.getFluidState(pos.above());
            world.setBlock(pos.above(), state.setValue(YveltalEgg.HALF, YveltalEggPart.TOP).setValue(YveltalEgg.WATERLOGGED,
                    fluidState.getType() == Fluids.WATER), 3);
        }
    }

    // Breaking Yveltal Egg Spawner breaks both parts and returns one item only
    @Override
    public void playerWillDestroy(final World world, final BlockPos pos, final BlockState state,
            final PlayerEntity player)
    {
        final Direction facing = state.getValue(YveltalEgg.FACING);
        final BlockPos yveltalEggPos = this.getYveltalEggPos(pos, state.getValue(YveltalEgg.HALF), facing);
        BlockState YveltalEggBlockState = world.getBlockState(yveltalEggPos);
        if (YveltalEggBlockState.getBlock() == this && !pos.equals(yveltalEggPos)) this.removeHalf(world, yveltalEggPos,
                YveltalEggBlockState, player);
        final BlockPos yveltalEggPartPos = this.getYveltalEggTopPos(yveltalEggPos, facing);
        YveltalEggBlockState = world.getBlockState(yveltalEggPartPos);
        if (YveltalEggBlockState.getBlock() == this && !pos.equals(yveltalEggPartPos)) this.removeHalf(world,
                yveltalEggPartPos, YveltalEggBlockState, player);
        super.playerWillDestroy(world, pos, state, player);
    }

    private BlockPos getYveltalEggTopPos(final BlockPos base, final Direction facing)
    {
        switch (facing)
        {
        default:
            return base.above();
        }
    }

    private BlockPos getYveltalEggPos(final BlockPos pos, final YveltalEggPart part, final Direction facing)
    {
        if (part == YveltalEggPart.BOTTOM) return pos;
        switch (facing)
        {
        default:
            return pos.below();
        }
    }

    // Breaking the Yveltal Egg Spawner leaves water if underwater
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

    // Prevents the Yveltal Egg Spawner from replacing blocks above it and
    // checks for water
    @Override
    public BlockState getStateForPlacement(final BlockItemUseContext context)
    {
        final FluidState ifluidstate = context.getLevel().getFluidState(context.getClickedPos());
        final BlockPos pos = context.getClickedPos();

        final BlockPos yveltalEggPos = this.getYveltalEggTopPos(pos, context.getHorizontalDirection()
                .getOpposite());
        if (pos.getY() < 255 && yveltalEggPos.getY() < 255 && context.getLevel().getBlockState(pos.above()).canBeReplaced(
                context)) return this.defaultBlockState().setValue(YveltalEgg.FACING, context.getHorizontalDirection()
                        .getOpposite()).setValue(YveltalEgg.HALF, YveltalEggPart.BOTTOM).setValue(YveltalEgg.WATERLOGGED,
                                ifluidstate.is(FluidTags.WATER) && ifluidstate.getAmount() == 8);
        return null;
    }

    @Override
    protected void createBlockStateDefinition(final StateContainer.Builder<Block, BlockState> builder)
    {
        builder.add(YveltalEgg.HALF, YveltalEgg.FACING, YveltalEgg.WATERLOGGED);
    }
}