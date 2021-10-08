package pokecube.legends.blocks.customblocks;

import java.util.HashMap;
import java.util.Map;

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
import pokecube.legends.init.BlockInit;

public class VictiniBlock extends Rotates implements SimpleWaterloggedBlock
{
    private static final EnumProperty<VictiniBlockPart> HALF           = EnumProperty.create("half",
            VictiniBlockPart.class);
    private static final Map<Direction, VoxelShape>     VICTINI_TOP    = new HashMap<>();
    private static final Map<Direction, VoxelShape>     VICTINI_BOTTOM = new HashMap<>();
    private static final BooleanProperty                WATERLOGGED    = BlockStateProperties.WATERLOGGED;
    private static final DirectionProperty              FACING         = HorizontalDirectionalBlock.FACING;

    // Precise selection box @formatter:off
    static
    {
        VictiniBlock.VICTINI_TOP.put(Direction.NORTH, Shapes.or(
            Block.box(13, 3, 3.5, 16, 4, 4),
            Block.box(12, 2, 3.5, 15, 3, 4),
            Block.box(11, 1, 3.5, 14, 2, 4),
            Block.box(10, 0, 3.5, 13, 1, 4),
            Block.box(3, 0, 3.5, 6, 1, 4),
            Block.box(2, 1, 3.5, 5, 2, 4),
            Block.box(1, 2, 3.5, 4, 3, 4),
            Block.box(0, 3, 3.5, 3, 4, 4)).optimize());
        VictiniBlock.VICTINI_TOP.put(Direction.EAST, Shapes.or(
            Block.box(12, 3, 13, 12.5, 4, 16),
            Block.box(12, 2, 12, 12.5, 3, 15),
            Block.box(12, 1, 11, 12.5, 2, 14),
            Block.box(12, 0, 10, 12.5, 1, 13),
            Block.box(12, 0, 3, 12.5, 1, 6),
            Block.box(12, 1, 2, 12.5, 2, 5),
            Block.box(12, 2, 1, 12.5, 3, 4),
            Block.box(12, 3, 0, 12.5, 4, 3)).optimize());
        VictiniBlock.VICTINI_TOP.put(Direction.SOUTH, Shapes.or(
            Block.box(0, 3, 12, 3, 4, 12.5),
            Block.box(1, 2, 12, 4, 3, 12.5),
            Block.box(2, 1, 12, 5, 2, 12.5),
            Block.box(3, 0, 12, 6, 1, 12.5),
            Block.box(10, 0, 12, 13, 1, 12.5),
            Block.box(11, 1, 12, 14, 2, 12.5),
            Block.box(12, 2, 12, 15, 3, 12.5),
            Block.box(13, 3, 12, 16, 4, 12.5)).optimize());
        VictiniBlock.VICTINI_TOP.put(Direction.WEST, Shapes.or(
            Block.box(3.5, 3, 0, 4, 4, 3),
            Block.box(3.5, 2, 1, 4, 3, 4),
            Block.box(3.5, 1, 2, 4, 2, 5),
            Block.box(3.5, 0, 3, 4, 1, 6),
            Block.box(3.5, 0, 10, 4, 1, 13),
            Block.box(3.5, 1, 11, 4, 2, 14),
            Block.box(3.5, 2, 12, 4, 3, 15),
            Block.box(3.5, 3, 13, 4, 4, 16)).optimize());
        VictiniBlock.VICTINI_BOTTOM.put(Direction.NORTH, Shapes.or(
            Block.box(13.5, 7, 7, 15.5, 15, 9),
            Block.box(12, 13, 7, 13.5, 15, 9),
            Block.box(2.5, 13, 7, 4, 15, 9),
            Block.box(4, 0, 4, 12, 2, 12),
            Block.box(6, 6.5, 6, 10, 8.5, 10),
            Block.box(4, 6, 10, 12, 16, 12),
            Block.box(10, 6, 6, 12, 16, 10),
            Block.box(4, 6, 6, 6, 16, 10),
            Block.box(4, 6, 4, 12, 16, 6),
            Block.box(4, 14, 3.5, 12, 15, 4),
            Block.box(5, 13, 3.5, 11, 14, 4),
            Block.box(9, 15, 3.5, 12, 16, 4),
            Block.box(6, 12, 3.5, 10, 13, 4),
            Block.box(6.5, 2, 6.5, 9.5, 6, 9.5),
            Block.box(7, 11, 3.5, 9, 12, 4),
            Block.box(4, 15, 3.5, 7, 16, 4),
            Block.box(0.5, 7, 7, 2.5, 15, 9),
            Block.box(2.5, 7, 7, 4, 9, 9),
            Block.box(12, 7, 7, 13.5, 9, 9)).optimize());
        VictiniBlock.VICTINI_BOTTOM.put(Direction.EAST, Shapes.or(
            Block.box(7, 7, 13.5, 9, 15, 15.5),
            Block.box(7, 13, 12, 9, 15, 13.5),
            Block.box(7, 13, 2.5, 9, 15, 4),
            Block.box(4, 0, 4, 12, 2, 12),
            Block.box(6, 6.5, 6, 10, 8.5, 10),
            Block.box(4, 6, 4, 6, 16, 12),
            Block.box(6, 6, 10, 10, 16, 12),
            Block.box(6, 6, 4, 10, 16, 6),
            Block.box(10, 6, 4, 12, 16, 12),
            Block.box(12, 14, 4, 12.5, 15, 12),
            Block.box(12, 13, 5, 12.5, 14, 11),
            Block.box(12, 15, 9, 12.5, 16, 12),
            Block.box(12, 12, 6, 12.5, 13, 10),
            Block.box(6.5, 2, 6.5, 9.5, 6, 9.5),
            Block.box(12, 11, 7, 12.5, 12, 9),
            Block.box(12, 15, 4, 12.5, 16, 7),
            Block.box(7, 7, 0.5, 9, 15, 2.5),
            Block.box(7, 7, 2.5, 9, 9, 4),
            Block.box(7, 7, 12, 9, 9, 13.5)).optimize());
        VictiniBlock.VICTINI_BOTTOM.put(Direction.SOUTH, Shapes.or(
            Block.box(0.5, 7, 7, 2.5, 15, 9),
            Block.box(2.5, 13, 7, 4, 15, 9),
            Block.box(12, 13, 7, 13.5, 15, 9),
            Block.box(4, 0, 4, 12, 2, 12),
            Block.box(6, 6.5, 6, 10, 8.5, 10),
            Block.box(4, 6, 4, 12, 16, 6),
            Block.box(4, 6, 6, 6, 16, 10),
            Block.box(10, 6, 6, 12, 16, 10),
            Block.box(4, 6, 10, 12, 16, 12),
            Block.box(4, 14, 12, 12, 15, 12.5),
            Block.box(5, 13, 12, 11, 14, 12.5),
            Block.box(4, 15, 12, 7, 16, 12.5),
            Block.box(6, 12, 12, 10, 13, 12.5),
            Block.box(6.5, 2, 6.5, 9.5, 6, 9.5),
            Block.box(7, 11, 12, 9, 12, 12.5),
            Block.box(9, 15, 12, 12, 16, 12.5),
            Block.box(13.5, 7, 7, 15.5, 15, 9),
            Block.box(12, 7, 7, 13.5, 9, 9),
            Block.box(2.5, 7, 7, 4, 9, 9)).optimize());
        VictiniBlock.VICTINI_BOTTOM.put(Direction.WEST, Shapes.or(
            Block.box(7, 7, 0.5, 9, 15, 2.5),
            Block.box(7, 13, 2.5, 9, 15, 4),
            Block.box(7, 13, 12, 9, 15, 13.5),
            Block.box(4, 0, 4, 12, 2, 12),
            Block.box(6, 6.5, 6, 10, 8.5, 10),
            Block.box(10, 6, 4, 12, 16, 12),
            Block.box(6, 6, 4, 10, 16, 6),
            Block.box(6, 6, 10, 10, 16, 12),
            Block.box(4, 6, 4, 6, 16, 12),
            Block.box(3.5, 14, 4, 4, 15, 12),
            Block.box(3.5, 13, 5, 4, 14, 11),
            Block.box(3.5, 15, 4, 4, 16, 7),
            Block.box(3.5, 12, 6, 4, 13, 10),
            Block.box(6.5, 2, 6.5, 9.5, 6, 9.5),
            Block.box(3.5, 11, 7, 4, 12, 9),
            Block.box(3.5, 15, 9, 4, 16, 12),
            Block.box(7, 7, 13.5, 9, 15, 15.5),
            Block.box(7, 7, 12, 9, 9, 13.5),
            Block.box(7, 7, 2.5, 9, 9, 4)).optimize());
    }
    // Precise selection box @formatter:on

    @Override
    public VoxelShape getShape(final BlockState state, final BlockGetter worldIn, final BlockPos pos,
            final CollisionContext context)
    {
        final VictiniBlockPart half = state.getValue(VictiniBlock.HALF);
        if (half == VictiniBlockPart.BOTTOM) return VictiniBlock.VICTINI_BOTTOM.get(state.getValue(
                VictiniBlock.FACING));
        else return VictiniBlock.VICTINI_TOP.get(state.getValue(VictiniBlock.FACING));
    }

    public VictiniBlock(final Properties props)
    {
        super(props);
        this.registerDefaultState(this.stateDefinition.any().setValue(VictiniBlock.FACING, Direction.NORTH).setValue(
                VictiniBlock.WATERLOGGED, false).setValue(VictiniBlock.HALF, VictiniBlockPart.BOTTOM));
    }

    // Places Victini Spawner with both top and bottom pieces
    @Override
    public void setPlacedBy(final Level world, final BlockPos pos, final BlockState state,
            @Nullable final LivingEntity entity, final ItemStack stack)
    {
        if (entity != null)
        {
            final FluidState fluidState = world.getFluidState(pos.above());
            world.setBlock(pos.above(), state.setValue(VictiniBlock.HALF, VictiniBlockPart.TOP).setValue(
                    VictiniBlock.WATERLOGGED, fluidState.getType() == Fluids.WATER), 3);
        }
    }

    // Breaking Victini Spawner breaks both parts and returns one item only
    @Override
    public void playerWillDestroy(final Level world, final BlockPos pos, final BlockState state, final Player player)
    {
        final Direction facing = state.getValue(VictiniBlock.FACING);
        final BlockPos victiniPos = this.getVictiniPos(pos, state.getValue(VictiniBlock.HALF), facing);
        BlockState VictiniBlockState = world.getBlockState(victiniPos);
        if (VictiniBlockState.getBlock() == this && !pos.equals(victiniPos) && this.asBlock() == BlockInit.VICTINI_CORE
                .get()) this.removeHalf(world, victiniPos, VictiniBlockState, player);
        final BlockPos victiniPartPos = this.getVictiniTopPos(victiniPos, facing);
        VictiniBlockState = world.getBlockState(victiniPartPos);
        if (VictiniBlockState.getBlock() == this && !pos.equals(victiniPartPos) && this
                .asBlock() == BlockInit.VICTINI_CORE.get()) this.removeHalf(world, victiniPartPos, VictiniBlockState,
                        player);
        super.playerWillDestroy(world, pos, state, player);
    }

    private BlockPos getVictiniTopPos(final BlockPos base, final Direction facing)
    {
        switch (facing)
        {
        default:
            return base.above();
        }
    }

    private BlockPos getVictiniPos(final BlockPos pos, final VictiniBlockPart part, final Direction facing)
    {
        if (part == VictiniBlockPart.BOTTOM) return pos;
        switch (facing)
        {
        default:
            return pos.below();
        }
    }

    // Breaking the Victini Spawner leaves water if underwater
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

    // Prevents the Victini Spawner from replacing blocks above it and checks
    // for water
    @Override
    public BlockState getStateForPlacement(final BlockPlaceContext context)
    {
        final FluidState ifluidstate = context.getLevel().getFluidState(context.getClickedPos());
        final BlockPos pos = context.getClickedPos();

        final BlockPos victiniPos = this.getVictiniTopPos(pos, context.getHorizontalDirection().getOpposite());
        if (pos.getY() < 255 && victiniPos.getY() < 255 && context.getLevel().getBlockState(pos.above()).canBeReplaced(
                context)) return this.defaultBlockState().setValue(VictiniBlock.FACING, context.getHorizontalDirection()
                        .getOpposite()).setValue(VictiniBlock.HALF, VictiniBlockPart.BOTTOM).setValue(
                                VictiniBlock.WATERLOGGED, ifluidstate.is(FluidTags.WATER) && ifluidstate
                                        .getAmount() == 8);
        return null;
    }

    @Override
    protected void createBlockStateDefinition(final StateDefinition.Builder<Block, BlockState> builder)
    {
        builder.add(VictiniBlock.HALF, VictiniBlock.FACING, VictiniBlock.WATERLOGGED);
    }
}