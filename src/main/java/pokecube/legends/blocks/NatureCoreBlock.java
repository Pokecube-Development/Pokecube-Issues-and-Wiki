package pokecube.legends.blocks;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

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
import net.minecraft.fluid.IFluidState;
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

import org.antlr.v4.runtime.misc.NotNull;
import javax.annotation.Nullable;

public class NatureCoreBlock extends Rotates implements IWaterLoggable
{
    private static final EnumProperty<NatureCorePart> HALF = EnumProperty.create("half", NatureCorePart.class);
    private static final Map<Direction, VoxelShape> NATURE_CORE_TOP = new HashMap<>();
    private static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
    private static final DirectionProperty FACING = HorizontalBlock.HORIZONTAL_FACING;

    //Precise selection box
    private static final VoxelShape NATURE_CORE_TOP_NORTH = Block
      .makeCuboidShape(2.33, 0.21, 6.75, 12.96, 12.25, 9.5);
    private static final VoxelShape NATURE_CORE_TOP_EAST = Block
      .makeCuboidShape(6.5, 0.21, 2.33, 9.25, 12.25, 12.96);
    private static final VoxelShape NATURE_CORE_TOP_SOUTH = Block
      .makeCuboidShape(3.04, 0.21, 6.5, 13.67, 12.25, 9.25);
    private static final VoxelShape NATURE_CORE_TOP_WEST = Block
      .makeCuboidShape(6.75, 0.21, 3.04, 9.5, 12.25, 13.67);

    private static final VoxelShape NATURE_CORE_BOTTOM = VoxelShapes.or(
      makeCuboidShape(0, 0, 4, 2, 2, 12),
      makeCuboidShape(0, 14, 4, 2, 16, 12),
      makeCuboidShape(2, 0, 4, 4, 4, 12),
      makeCuboidShape(2, 12, 4, 4, 16, 12),
      makeCuboidShape(4, 0, 0, 12, 2, 2),
      makeCuboidShape(4, 0, 12, 12, 4, 14),
      makeCuboidShape(4, 0, 14, 12, 2, 16),
      makeCuboidShape(4, 0, 2, 12, 4, 4),
      makeCuboidShape(4, 0, 4, 12, 16, 12),
      makeCuboidShape(4, 12, 2, 12, 16, 4),
      makeCuboidShape(4, 12, 12, 12, 16, 14),
      makeCuboidShape(4, 14, 0, 12, 16, 2),
      makeCuboidShape(4, 14, 14, 12, 16, 16),
      makeCuboidShape(12, 0, 4, 14, 4, 12),
      makeCuboidShape(14, 0, 4, 16, 2, 12),
      makeCuboidShape(12, 12, 4, 14, 16, 12),
      makeCuboidShape(14, 14, 4, 16, 16, 12))
      .simplify();

    //Precise selection box
    public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context)
    {
        NatureCorePart half = state.get(HALF);
        if (half == NatureCorePart.BOTTOM)
        {
            return NATURE_CORE_BOTTOM;
        }
        else
        {
            //return NATURE_CORE_TOP.get(state.get(FACING));
            switch(state.get(FACING))
            {
                case NORTH:
                    return NATURE_CORE_TOP_NORTH;
                case EAST:
                    return NATURE_CORE_TOP_EAST;
                case SOUTH:
                    return NATURE_CORE_TOP_SOUTH;
                case WEST:
                    return NATURE_CORE_TOP_WEST;
                default:
                    return NATURE_CORE_TOP_NORTH;
            }
        }
    }

    //Default States
    public NatureCoreBlock(final String name, final Material material, final float hardness, final float resistance,
                           final SoundType sound)
    {
        super(name, material, hardness, resistance, sound);
        this.setDefaultState(this.stateContainer.getBaseState()
          .with(HALF, NatureCorePart.BOTTOM)
          .with(FACING, Direction.NORTH)
          .with(WATERLOGGED, false));
    }

    //Places Nature Core Spawner with both top and bottom pieces
    @Override
    public void onBlockPlacedBy(World world, BlockPos pos, BlockState state, @Nullable LivingEntity entity, ItemStack stack)
    {
        if (entity != null)
        {
            IFluidState fluidState = world.getFluidState(pos.up());
            world.setBlockState(pos.up(),
              state.with(HALF, NatureCorePart.TOP)
                .with(WATERLOGGED,  fluidState.getFluid() == Fluids.WATER), 3);
        }
    }

    //Breaking Nature Core Spawner breaks both parts and returns one item only
    public void onBlockHarvested(World world, @NotNull BlockPos pos, BlockState state, @NotNull PlayerEntity player)
    {
        Direction facing = state.get(FACING);
        BlockPos natureCorePos = getNatureCorePos(pos, state.get(HALF), facing);
        BlockState NatureCoreBlockState = world.getBlockState(natureCorePos);
        if (NatureCoreBlockState.getBlock() == this && !pos.equals(natureCorePos))
        {
            removeHalf(world, natureCorePos, NatureCoreBlockState);
        }
        BlockPos natureCorePartPos = getNatureCoreTopPos(natureCorePos, facing);
        NatureCoreBlockState = world.getBlockState(natureCorePartPos);
        if (NatureCoreBlockState.getBlock() == this && !pos.equals(natureCorePartPos))
        {
            removeHalf(world, natureCorePartPos, NatureCoreBlockState);
        }
        super.onBlockHarvested(world, pos, state, player);
    }

    private BlockPos getNatureCoreTopPos(BlockPos base, Direction facing)
    {
        switch (facing) {
            default:
                return base.up();
        }
    }

    private BlockPos getNatureCorePos(BlockPos pos, NatureCorePart part, Direction facing)
    {
        if (part == NatureCorePart.BOTTOM) return pos;
        switch (facing) {
            default:
                return pos.down();
        }
    }

    //Breaking the Nature Core Spawner leaves water if underwater
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

    //Prevents the Nature Core Spawner from replacing blocks above it and checks for water
    @Override
    public BlockState getStateForPlacement(BlockItemUseContext context)
    {
        IFluidState ifluidstate = context.getWorld().getFluidState(context.getPos());
        BlockPos pos = context.getPos();

        BlockPos natureCorePos = getNatureCoreTopPos(pos, context.getPlacementHorizontalFacing().getOpposite());
        if (pos.getY() < 255 &&
          natureCorePos.getY() < 255 && context.getWorld().getBlockState(pos.up()).isReplaceable(context))
        {
            return  this.getDefaultState()
              .with(FACING, context.getPlacementHorizontalFacing().getOpposite())
              .with(HALF, NatureCorePart.BOTTOM)
              .with(WATERLOGGED, ifluidstate.isTagged(FluidTags.WATER) && ifluidstate.getLevel() == 8);
        }
        return null;
    }

    @Override
    protected void fillStateContainer(final StateContainer.Builder<Block, BlockState> builder)
    {
        builder.add(HALF, HorizontalBlock.HORIZONTAL_FACING, WATERLOGGED);
    }

    public NatureCoreBlock(final String name, final Properties props)
    {
        super(name, props.tickRandomly());
    }

    @Override
    public void randomTick(final BlockState state, final World worldIn, final BlockPos pos, final Random random)
    {
        if (random.nextInt(100) == 0) worldIn.playSound(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
          SoundEvents.AMBIENT_CAVE, SoundCategory.BLOCKS, 0.5F, random.nextFloat() * 0.4F + 0.8F, false);
    }
}
