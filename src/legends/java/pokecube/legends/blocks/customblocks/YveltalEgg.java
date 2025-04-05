package pokecube.legends.blocks.customblocks;

import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class YveltalEgg extends Rotates implements SimpleWaterloggedBlock
{
    private static final EnumProperty<YveltalEggPart> HALF           = EnumProperty.create("half",
            YveltalEggPart.class);
    private static final BooleanProperty              WATERLOGGED    = BlockStateProperties.WATERLOGGED;
    private static final DirectionProperty            FACING         = HorizontalDirectionalBlock.FACING;

    // Precise selection box
    private static final VoxelShape YVELTAL_TOP = Shapes.or(
        Block.box(1, 0, 1, 15, 6, 15),
        Block.box(2, 6, 2, 14, 10, 14)).optimize();

    private static final VoxelShape YVELTAL_BOTTOM = Shapes.or(
        Block.box(2, 0, 2, 14, 4, 14),
        Block.box(1, 4, 1, 15, 16, 15)).optimize();

    // Precise selection box
    @Override
    public VoxelShape getShape(final BlockState state, final BlockGetter worldIn, final BlockPos pos,
            final CollisionContext context)
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
    public void setPlacedBy(final Level world, final BlockPos pos, final BlockState state,
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
    public void playerWillDestroy(final Level world, final BlockPos pos, final BlockState state,
            final Player player)
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
    private void removeHalf(final Level world, final BlockPos pos, final BlockState state, Player player)
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
    public BlockState getStateForPlacement(final BlockPlaceContext context)
    {
        final FluidState fluidState = context.getLevel().getFluidState(context.getClickedPos());
        final Direction direction = context.getHorizontalDirection().getOpposite();
        final BlockPos pos = context.getClickedPos();
        final Level world = context.getLevel();

        final BlockPos yveltalEggPos = this.getYveltalEggTopPos(pos, direction);
        
        if (pos.getY() < world.getMaxBuildHeight() && yveltalEggPos.getY() < world.getMaxBuildHeight()
                && context.getLevel().getBlockState(pos.above()).canBeReplaced(context))
            return this.defaultBlockState().setValue(FACING, direction).setValue(HALF, YveltalEggPart.BOTTOM)
                    .setValue(WATERLOGGED, fluidState.is(FluidTags.WATER) && fluidState.getAmount() == 8);
        return null;
    }

    @Override
    protected void createBlockStateDefinition(final StateDefinition.Builder<Block, BlockState> builder)
    {
        builder.add(YveltalEgg.HALF, YveltalEgg.FACING, YveltalEgg.WATERLOGGED);
    }
}