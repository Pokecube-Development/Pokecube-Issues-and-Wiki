package pokecube.legends.blocks.customblocks;

import net.minecraft.block.*;
import net.minecraft.block.material.Material;
import net.minecraft.block.material.MaterialColor;
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
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.ToolType;

import javax.annotation.Nullable;
import java.util.Random;

public class NatureCoreBlock extends Rotates implements IWaterLoggable
{
    private static final EnumProperty<NatureCorePart> HALF        = EnumProperty.create("half", NatureCorePart.class);
    private static final BooleanProperty              WATERLOGGED = BlockStateProperties.WATERLOGGED;
    private static final DirectionProperty            FACING      = HorizontalBlock.FACING;

    // Precise selection box
    private static final VoxelShape NATURE_CORE_TOP_NORTH =
            Block.box(2, 1, 6, 14, 14, 10);
    private static final VoxelShape NATURE_CORE_TOP_EAST  =
            Block.box(6, 1, 2, 10, 14, 14);
    private static final VoxelShape NATURE_CORE_TOP_SOUTH =
            Block.box(2, 1, 6, 14, 14, 10);
    private static final VoxelShape NATURE_CORE_TOP_WEST  =
            Block.box(6, 1, 2, 10, 14, 14);

    private static final VoxelShape NATURE_CORE_BOTTOM = VoxelShapes.or(
            Block.box(0, 0, 4, 2, 2, 12),
            Block.box(0, 14, 4, 2, 16, 12),
            Block.box(2, 0, 4, 4, 4, 12),
            Block.box(2, 12, 4, 4, 16, 12),
            Block.box(4, 0, 0, 12, 2, 2),
            Block.box(4, 0, 12, 12, 4, 14),
            Block.box(4, 0, 14, 12, 2, 16),
            Block.box(4, 0, 2, 12, 4, 4),
            Block.box(4, 0, 4, 12, 16, 12),
            Block.box(4, 12, 2, 12, 16, 4),
            Block.box(4, 12, 12, 12, 16, 14),
            Block.box(4, 14, 0, 12, 16, 2),
            Block.box(4, 14, 14, 12, 16, 16),
            Block.box(12, 0, 4, 14, 4, 12),
            Block.box(14, 0, 4, 16, 2, 12),
            Block.box(12, 12, 4, 14, 16, 12),
            Block.box(14, 14, 4, 16, 16, 12)).optimize();

    // Precise selection box
    @Override
    public VoxelShape getShape(final BlockState state, final IBlockReader worldIn, final BlockPos pos,
            final ISelectionContext context)
    {
        final NatureCorePart half = state.getValue(NatureCoreBlock.HALF);
        if (half == NatureCorePart.BOTTOM) return NatureCoreBlock.NATURE_CORE_BOTTOM;
        else // return NATURE_CORE_TOP.get(state.get(FACING));
            switch (state.getValue(NatureCoreBlock.FACING))
            {
            case NORTH:
            return NatureCoreBlock.NATURE_CORE_TOP_NORTH;
            case EAST:
            return NatureCoreBlock.NATURE_CORE_TOP_EAST;
            case SOUTH:
            return NatureCoreBlock.NATURE_CORE_TOP_SOUTH;
            case WEST:
            return NatureCoreBlock.NATURE_CORE_TOP_WEST;
            default:
            return NatureCoreBlock.NATURE_CORE_TOP_NORTH;
            }
    }

    // Default States
    public NatureCoreBlock(final String name, final Material material, final MaterialColor color, final float hardness, final float resistance,
            final SoundType sound, final ToolType tool, final int harvest)
    {
        super(name, material, color, hardness, resistance, sound, tool, harvest);
        this.registerDefaultState(this.stateDefinition.any().setValue(NatureCoreBlock.HALF, NatureCorePart.BOTTOM).setValue(
                NatureCoreBlock.FACING, Direction.NORTH).setValue(NatureCoreBlock.WATERLOGGED, false));
    }

    // Places Nature Core Spawner with both top and bottom pieces
    @Override
    public void setPlacedBy(final World world, final BlockPos pos, final BlockState state,
            @Nullable final LivingEntity entity, final ItemStack stack)
    {
        if (entity != null)
        {
            final FluidState fluidState = world.getFluidState(pos.above());
            world.setBlock(pos.above(), state.setValue(NatureCoreBlock.HALF, NatureCorePart.TOP).setValue(
                    NatureCoreBlock.WATERLOGGED, fluidState.getType() == Fluids.WATER), 3);
        }
    }

    // Breaking Nature Core Spawner breaks both parts and returns one item only
    @Override
    public void playerWillDestroy(final World world, final BlockPos pos, final BlockState state,
            final PlayerEntity player)
    {
        final Direction facing = state.getValue(NatureCoreBlock.FACING);
        final BlockPos natureCorePos = this.getNatureCorePos(pos, state.getValue(NatureCoreBlock.HALF), facing);
        BlockState NatureCoreBlockState = world.getBlockState(natureCorePos);
        if (NatureCoreBlockState.getBlock() == this && !pos.equals(natureCorePos)) this.removeHalf(world, natureCorePos,
                NatureCoreBlockState);
        final BlockPos natureCorePartPos = this.getNatureCoreTopPos(natureCorePos, facing);
        NatureCoreBlockState = world.getBlockState(natureCorePartPos);
        if (NatureCoreBlockState.getBlock() == this && !pos.equals(natureCorePartPos)) this.removeHalf(world,
                natureCorePartPos, NatureCoreBlockState);
        super.playerWillDestroy(world, pos, state, player);
    }

    private BlockPos getNatureCoreTopPos(final BlockPos base, final Direction facing)
    {
        switch (facing)
        {
        default:
            return base.above();
        }
    }

    private BlockPos getNatureCorePos(final BlockPos pos, final NatureCorePart part, final Direction facing)
    {
        if (part == NatureCorePart.BOTTOM) return pos;
        switch (facing)
        {
        default:
            return pos.below();
        }
    }

    // Breaking the Nature Core Spawner leaves water if underwater
    private void removeHalf(final World world, final BlockPos pos, final BlockState state)
    {
        final FluidState fluidState = world.getFluidState(pos);
        if (fluidState.getType() == Fluids.WATER) world.setBlock(pos, fluidState.createLegacyBlock(), 35);
        else world.setBlock(pos, Blocks.AIR.defaultBlockState(), 35);
    }

    // Prevents the Nature Core Spawner from replacing blocks above it and
    // checks for water
    @Override
    public BlockState getStateForPlacement(final BlockItemUseContext context)
    {
        final FluidState ifluidstate = context.getLevel().getFluidState(context.getClickedPos());
        final BlockPos pos = context.getClickedPos();

        final BlockPos natureCorePos = this.getNatureCoreTopPos(pos, context.getHorizontalDirection()
                .getOpposite());
        if (pos.getY() < 255 && natureCorePos.getY() < 255 && context.getLevel().getBlockState(pos.above()).canBeReplaced(
                context)) return this.defaultBlockState().setValue(NatureCoreBlock.FACING, context
                        .getHorizontalDirection().getOpposite()).setValue(NatureCoreBlock.HALF, NatureCorePart.BOTTOM)
                        .setValue(NatureCoreBlock.WATERLOGGED, ifluidstate.is(FluidTags.WATER) && ifluidstate
                                .getAmount() == 8);
        return null;
    }

    @Override
    protected void createBlockStateDefinition(final StateContainer.Builder<Block, BlockState> builder)
    {
        builder.add(NatureCoreBlock.HALF, HorizontalBlock.FACING, NatureCoreBlock.WATERLOGGED);
    }

    public NatureCoreBlock(final String name, final Properties props)
    {
        super(name, props.randomTicks());
    }

    @Override
    public void randomTick(final BlockState state, final ServerWorld worldIn, final BlockPos pos, final Random random)
    {
        if (random.nextInt(100) == 0) worldIn.playLocalSound(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                SoundEvents.AMBIENT_CAVE, SoundCategory.BLOCKS, 0.5F, random.nextFloat() * 0.4F + 0.8F, false);
    }
}
