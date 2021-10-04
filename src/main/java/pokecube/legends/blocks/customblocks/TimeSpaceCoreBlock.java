package pokecube.legends.blocks.customblocks;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import javax.annotation.Nullable;

import com.minecolonies.api.util.constant.ToolType;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
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
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.material.MaterialColor;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class TimeSpaceCoreBlock extends Rotates implements SimpleWaterloggedBlock
{
    private static final EnumProperty<TimeSpaceCorePart> HALF           = EnumProperty.create("half",
            TimeSpaceCorePart.class);
    private static final Map<Direction, VoxelShape>      TIME_SPACE_TOP = new HashMap<>();
    private static final BooleanProperty                 WATERLOGGED    = BlockStateProperties.WATERLOGGED;
    private static final DirectionProperty               FACING         = HorizontalDirectionalBlock.FACING;

    // Precise selection box
    private static final VoxelShape TIME_SPACE_BOTTOM = Shapes.or(
            Block.box(4, 0, 4, 12, 16, 12),
            Block.box(0, 0, 4, 4, 2, 12),
            Block.box(2, 2, 4, 4, 4, 12),
            Block.box(4, 0, 0, 12, 2, 4),
            Block.box(4, 2, 12, 12, 4, 14),
            Block.box(4, 0, 12, 12, 2, 16),
            Block.box(4, 2, 2, 12, 4, 4),
            Block.box(12, 2, 4, 14, 4, 12),
            Block.box(12, 0, 4, 16, 2, 12)).optimize();

    static
    {
        TIME_SPACE_TOP.put(Direction.NORTH, Shapes.or(
                Block.box(7, 1, 7.5, 9, 2, 8.5),
                Block.box(5, 2, 7.5, 11, 3, 8.5),
                Block.box(4, 3, 7.5, 12, 4, 8.5),
                Block.box(3, 4, 7.5, 13, 6, 8.5),
                Block.box(2, 6, 7.5, 14, 11, 8.5),
                Block.box(3, 11, 7.5, 13, 13, 8.5),
                Block.box(4, 13, 7.5, 12, 14, 8.5),
                Block.box(5, 14, 7.5, 11, 15, 8.5),
                Block.box(7, 15, 7.5, 9, 16, 8.5)).optimize());
        TIME_SPACE_TOP.put(Direction.EAST, Shapes.or(
                Block.box(7.5, 1, 7, 8.5, 2, 9),
                Block.box(7.5, 2, 5, 8.5, 3, 11),
                Block.box(7.5, 3, 4, 8.5, 4, 12),
                Block.box(7.5, 4, 3, 8.5, 6, 13),
                Block.box(7.5, 6, 2, 8.5, 11, 14),
                Block.box(7.5, 11, 3, 8.5, 13, 13),
                Block.box(7.5, 13, 4, 8.5, 14, 12),
                Block.box(7.5, 14, 5, 8.5, 15, 11),
                Block.box(7.5, 15, 7, 8.5, 16, 9)).optimize());
        TIME_SPACE_TOP.put(Direction.SOUTH, Shapes.or(
                Block.box(7, 1, 7.5, 9, 2, 8.5),
                Block.box(5, 2, 7.5, 11, 3, 8.5),
                Block.box(4, 3, 7.5, 12, 4, 8.5),
                Block.box(3, 4, 7.5, 13, 6, 8.5),
                Block.box(2, 6, 7.5, 14, 11, 8.5),
                Block.box(3, 11, 7.5, 13, 13, 8.5),
                Block.box(4, 13, 7.5, 12, 14, 8.5),
                Block.box(5, 14, 7.5, 11, 15, 8.5),
                Block.box(7, 15, 7.5, 9, 16, 8.5)).optimize());
        TIME_SPACE_TOP.put(Direction.WEST, Shapes.or(
                Block.box(7.5, 1, 7, 8.5, 2, 9),
                Block.box(7.5, 2, 5, 8.5, 3, 11),
                Block.box(7.5, 3, 4, 8.5, 4, 12),
                Block.box(7.5, 4, 3, 8.5, 6, 13),
                Block.box(7.5, 6, 2, 8.5, 11, 14),
                Block.box(7.5, 11, 3, 8.5, 13, 13),
                Block.box(7.5, 13, 4, 8.5, 14, 12),
                Block.box(7.5, 14, 5, 8.5, 15, 11),
                Block.box(7.5, 15, 7, 8.5, 16, 9)).optimize());
    }

    // Precise selection box
    @Override
    public VoxelShape getShape(final BlockState state, final BlockGetter worldIn, final BlockPos pos,
            final CollisionContext context)
    {
        final TimeSpaceCorePart half = state.getValue(TimeSpaceCoreBlock.HALF);
        if (half == TimeSpaceCorePart.BOTTOM) return TimeSpaceCoreBlock.TIME_SPACE_BOTTOM;
        else return TimeSpaceCoreBlock.TIME_SPACE_TOP.get(state.getValue(TimeSpaceCoreBlock.FACING));
    }

    // Default States
    public TimeSpaceCoreBlock(final String name, final Material material, final MaterialColor color, final float hardness, final float resistance,
                              final SoundType sound, final ToolType tool, final int harvest, boolean isDrop)
    {
        super(name, material, color, hardness, resistance, sound, tool, harvest, isDrop);
        this.registerDefaultState(this.stateDefinition.any().setValue(TimeSpaceCoreBlock.HALF, TimeSpaceCorePart.BOTTOM)
                .setValue(TimeSpaceCoreBlock.FACING, Direction.NORTH).setValue(TimeSpaceCoreBlock.WATERLOGGED, false));
    }

    // Places Time & Space Spawner with both top and bottom pieces
    @Override
    public void setPlacedBy(final Level world, final BlockPos pos, final BlockState state,
            @Nullable final LivingEntity entity, final ItemStack stack)
    {
        if (entity != null)
        {
            final FluidState fluidState = world.getFluidState(pos.above());
            world.setBlock(pos.above(), state.setValue(TimeSpaceCoreBlock.HALF, TimeSpaceCorePart.TOP).setValue(
                    TimeSpaceCoreBlock.WATERLOGGED, fluidState.getType() == Fluids.WATER), 3);
        }
    }

    // Breaking Time & Space Spawner breaks both parts and returns one item only
    @Override
    public void playerWillDestroy(final Level world, final BlockPos pos, final BlockState state,
            final Player player)
    {
        final Direction facing = state.getValue(TimeSpaceCoreBlock.FACING);
        final BlockPos timeSpacePos = this.getTimeSpacePos(pos, state.getValue(TimeSpaceCoreBlock.HALF), facing);
        BlockState TimeSpaceBlockState = world.getBlockState(timeSpacePos);
        if (TimeSpaceBlockState.getBlock() == this && !pos.equals(timeSpacePos)) this.removeHalf(world, timeSpacePos,
                TimeSpaceBlockState, player);
        final BlockPos timeSpacePartPos = this.getTimeSpaceTopPos(timeSpacePos, facing);
        TimeSpaceBlockState = world.getBlockState(timeSpacePartPos);
        if (TimeSpaceBlockState.getBlock() == this && !pos.equals(timeSpacePartPos)) this.removeHalf(world,
                timeSpacePartPos, TimeSpaceBlockState, player);
        super.playerWillDestroy(world, pos, state, player);
    }

    private BlockPos getTimeSpaceTopPos(final BlockPos base, final Direction facing)
    {
        switch (facing)
        {
        default:
            return base.above();
        }
    }

    private BlockPos getTimeSpacePos(final BlockPos pos, final TimeSpaceCorePart part, final Direction facing)
    {
        if (part == TimeSpaceCorePart.BOTTOM) return pos;
        switch (facing)
        {
        default:
            return pos.below();
        }
    }

    // Breaking the Time & Space Spawner leaves water if underwater
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

    // Prevents the Time & Space Spawner from replacing blocks above it and
    // checks for water
    @Override
    public BlockState getStateForPlacement(final BlockPlaceContext context)
    {
        final FluidState ifluidstate = context.getLevel().getFluidState(context.getClickedPos());
        final BlockPos pos = context.getClickedPos();

        final BlockPos timeSpacePos = this.getTimeSpaceTopPos(pos, context.getHorizontalDirection()
                .getOpposite());
        if (pos.getY() < 255 && timeSpacePos.getY() < 255 && context.getLevel().getBlockState(pos.above()).canBeReplaced(
                context)) return this.defaultBlockState().setValue(TimeSpaceCoreBlock.FACING, context
                        .getHorizontalDirection().getOpposite()).setValue(TimeSpaceCoreBlock.HALF,
                                TimeSpaceCorePart.BOTTOM).setValue(TimeSpaceCoreBlock.WATERLOGGED, ifluidstate.is(
                                        FluidTags.WATER) && ifluidstate.getAmount() == 8);
        return null;
    }

    @Override
    protected void createBlockStateDefinition(final StateDefinition.Builder<Block, BlockState> builder)
    {
        builder.add(TimeSpaceCoreBlock.HALF, HorizontalDirectionalBlock.FACING, TimeSpaceCoreBlock.WATERLOGGED);
    }

    public TimeSpaceCoreBlock(final Properties props)
    {
        super(props.randomTicks());
    }

    @Override
    public void randomTick(final BlockState state, final ServerLevel worldIn, final BlockPos pos, final Random random)
    {
        if (random.nextInt(100) == 0) worldIn.playLocalSound(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                SoundEvents.END_PORTAL_SPAWN, SoundSource.BLOCKS, 0.5F, random.nextFloat() * 0.4F + 0.8F,
                false);
    }
}
