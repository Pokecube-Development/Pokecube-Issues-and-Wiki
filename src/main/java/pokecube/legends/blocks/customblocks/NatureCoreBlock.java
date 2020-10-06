package pokecube.legends.blocks.customblocks;

import java.util.Random;

import javax.annotation.Nullable;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.HorizontalBlock;
import net.minecraft.block.IWaterLoggable;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluids;
import net.minecraft.fluid.FluidState;
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

public class NatureCoreBlock extends Rotates implements IWaterLoggable
{
    private static final EnumProperty<NatureCorePart> HALF        = EnumProperty.create("half", NatureCorePart.class);
    private static final BooleanProperty              WATERLOGGED = BlockStateProperties.WATERLOGGED;
    private static final DirectionProperty            FACING      = HorizontalBlock.HORIZONTAL_FACING;

    // Precise selection box
    private static final VoxelShape NATURE_CORE_TOP_NORTH = Block.makeCuboidShape(2.33, 0.21, 6.75, 12.96, 12.25, 9.5);
    private static final VoxelShape NATURE_CORE_TOP_EAST  = Block.makeCuboidShape(6.5, 0.21, 2.33, 9.25, 12.25, 12.96);
    private static final VoxelShape NATURE_CORE_TOP_SOUTH = Block.makeCuboidShape(3.04, 0.21, 6.5, 13.67, 12.25, 9.25);
    private static final VoxelShape NATURE_CORE_TOP_WEST  = Block.makeCuboidShape(6.75, 0.21, 3.04, 9.5, 12.25, 13.67);

    private static final VoxelShape NATURE_CORE_BOTTOM = VoxelShapes.or(Block.makeCuboidShape(0, 0, 4, 2, 2, 12), Block
            .makeCuboidShape(0, 14, 4, 2, 16, 12), Block.makeCuboidShape(2, 0, 4, 4, 4, 12), Block.makeCuboidShape(2,
                    12, 4, 4, 16, 12), Block.makeCuboidShape(4, 0, 0, 12, 2, 2), Block.makeCuboidShape(4, 0, 12, 12, 4,
                            14), Block.makeCuboidShape(4, 0, 14, 12, 2, 16), Block.makeCuboidShape(4, 0, 2, 12, 4, 4),
            Block.makeCuboidShape(4, 0, 4, 12, 16, 12), Block.makeCuboidShape(4, 12, 2, 12, 16, 4), Block
                    .makeCuboidShape(4, 12, 12, 12, 16, 14), Block.makeCuboidShape(4, 14, 0, 12, 16, 2), Block
                            .makeCuboidShape(4, 14, 14, 12, 16, 16), Block.makeCuboidShape(12, 0, 4, 14, 4, 12), Block
                                    .makeCuboidShape(14, 0, 4, 16, 2, 12), Block.makeCuboidShape(12, 12, 4, 14, 16, 12),
            Block.makeCuboidShape(14, 14, 4, 16, 16, 12)).simplify();

    // Precise selection box
    @Override
    public VoxelShape getShape(final BlockState state, final IBlockReader worldIn, final BlockPos pos,
            final ISelectionContext context)
    {
        final NatureCorePart half = state.get(NatureCoreBlock.HALF);
        if (half == NatureCorePart.BOTTOM) return NatureCoreBlock.NATURE_CORE_BOTTOM;
        else // return NATURE_CORE_TOP.get(state.get(FACING));
            switch (state.get(NatureCoreBlock.FACING))
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
    public NatureCoreBlock(final String name, final Material material, final float hardness, final float resistance,
            final SoundType sound, final ToolType tool)
    {
        super(name, material, hardness, resistance, sound, tool);
        this.setDefaultState(this.stateContainer.getBaseState().with(NatureCoreBlock.HALF, NatureCorePart.BOTTOM).with(
                NatureCoreBlock.FACING, Direction.NORTH).with(NatureCoreBlock.WATERLOGGED, false));
    }

    // Places Nature Core Spawner with both top and bottom pieces
    @Override
    public void onBlockPlacedBy(final World world, final BlockPos pos, final BlockState state,
            @Nullable final LivingEntity entity, final ItemStack stack)
    {
        if (entity != null)
        {
            final FluidState fluidState = world.getFluidState(pos.up());
            world.setBlockState(pos.up(), state.with(NatureCoreBlock.HALF, NatureCorePart.TOP).with(
                    NatureCoreBlock.WATERLOGGED, fluidState.getFluid() == Fluids.WATER), 3);
        }
    }

    // Breaking Nature Core Spawner breaks both parts and returns one item only
    @Override
    public void onBlockHarvested(final World world, final BlockPos pos, final BlockState state,
            final PlayerEntity player)
    {
        final Direction facing = state.get(NatureCoreBlock.FACING);
        final BlockPos natureCorePos = this.getNatureCorePos(pos, state.get(NatureCoreBlock.HALF), facing);
        BlockState NatureCoreBlockState = world.getBlockState(natureCorePos);
        if (NatureCoreBlockState.getBlock() == this && !pos.equals(natureCorePos)) this.removeHalf(world, natureCorePos,
                NatureCoreBlockState);
        final BlockPos natureCorePartPos = this.getNatureCoreTopPos(natureCorePos, facing);
        NatureCoreBlockState = world.getBlockState(natureCorePartPos);
        if (NatureCoreBlockState.getBlock() == this && !pos.equals(natureCorePartPos)) this.removeHalf(world,
                natureCorePartPos, NatureCoreBlockState);
        super.onBlockHarvested(world, pos, state, player);
    }

    private BlockPos getNatureCoreTopPos(final BlockPos base, final Direction facing)
    {
        switch (facing)
        {
        default:
            return base.up();
        }
    }

    private BlockPos getNatureCorePos(final BlockPos pos, final NatureCorePart part, final Direction facing)
    {
        if (part == NatureCorePart.BOTTOM) return pos;
        switch (facing)
        {
        default:
            return pos.down();
        }
    }

    // Breaking the Nature Core Spawner leaves water if underwater
    private void removeHalf(final World world, final BlockPos pos, final BlockState state)
    {
        final FluidState fluidState = world.getFluidState(pos);
        if (fluidState.getFluid() == Fluids.WATER) world.setBlockState(pos, fluidState.getBlockState(), 35);
        else world.setBlockState(pos, Blocks.AIR.getDefaultState(), 35);
    }

    // Prevents the Nature Core Spawner from replacing blocks above it and
    // checks for water
    @Override
    public BlockState getStateForPlacement(final BlockItemUseContext context)
    {
        final FluidState ifluidstate = context.getWorld().getFluidState(context.getPos());
        final BlockPos pos = context.getPos();

        final BlockPos natureCorePos = this.getNatureCoreTopPos(pos, context.getPlacementHorizontalFacing()
                .getOpposite());
        if (pos.getY() < 255 && natureCorePos.getY() < 255 && context.getWorld().getBlockState(pos.up()).isReplaceable(
                context)) return this.getDefaultState().with(NatureCoreBlock.FACING, context
                        .getPlacementHorizontalFacing().getOpposite()).with(NatureCoreBlock.HALF, NatureCorePart.BOTTOM)
                        .with(NatureCoreBlock.WATERLOGGED, ifluidstate.isTagged(FluidTags.WATER) && ifluidstate
                                .getLevel() == 8);
        return null;
    }

    @Override
    protected void fillStateContainer(final StateContainer.Builder<Block, BlockState> builder)
    {
        builder.add(NatureCoreBlock.HALF, HorizontalBlock.HORIZONTAL_FACING, NatureCoreBlock.WATERLOGGED);
    }

    public NatureCoreBlock(final String name, final Properties props)
    {
        super(name, props.tickRandomly());
    }

    @Override
    public void randomTick(final BlockState state, final ServerWorld worldIn, final BlockPos pos, final Random random)
    {
        if (random.nextInt(100) == 0) worldIn.playSound(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                SoundEvents.AMBIENT_CAVE, SoundCategory.BLOCKS, 0.5F, random.nextFloat() * 0.4F + 0.8F, false);
    }
}
