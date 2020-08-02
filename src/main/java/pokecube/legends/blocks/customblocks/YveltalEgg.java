package pokecube.legends.blocks.customblocks;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nullable;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.HorizontalBlock;
import net.minecraft.block.IWaterLoggable;
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
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.IBooleanFunction;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;

public class YveltalEgg extends Rotates implements IWaterLoggable
{
    private static final EnumProperty<YveltalEggPart> HALF           = EnumProperty.create("half",
            YveltalEggPart.class);
    private static final Map<Direction, VoxelShape>   YVELTAL_TOP    = new HashMap<>();
    private static final Map<Direction, VoxelShape>   YVELTAL_BOTTOM = new HashMap<>();
    private static final BooleanProperty              WATERLOGGED    = BlockStateProperties.WATERLOGGED;
    private static final DirectionProperty            FACING         = HorizontalBlock.HORIZONTAL_FACING;

    // Precise selection box
    static
    {
      //@formatter:off
    YveltalEgg.YVELTAL_TOP.put(Direction.NORTH,
      VoxelShapes.combineAndSimplify(Block.makeCuboidShape(1, 0, 3, 15, 3, 13),
        VoxelShapes.combineAndSimplify(Block.makeCuboidShape(4, 0, 2, 12, 5, 14),
          VoxelShapes.combineAndSimplify(Block.makeCuboidShape(3, 3, 4, 13, 6, 12),
            VoxelShapes.combineAndSimplify(Block.makeCuboidShape(1.1, 3, 6, 14.85, 9.5, 10),
              VoxelShapes.combineAndSimplify(Block.makeCuboidShape(2.5, 3, 2, 13.5, 8, 6),
                Block.makeCuboidShape(2.5, 3, 10, 13.5, 8, 14),
                IBooleanFunction.OR), IBooleanFunction.OR), IBooleanFunction.OR),
          IBooleanFunction.OR), IBooleanFunction.OR));
    YveltalEgg.YVELTAL_TOP.put(Direction.EAST,
      VoxelShapes.combineAndSimplify(Block.makeCuboidShape(3, 0, 1, 13, 3, 15),
        VoxelShapes.combineAndSimplify(Block.makeCuboidShape(2, 0, 4, 14, 5, 12),
          VoxelShapes.combineAndSimplify(Block.makeCuboidShape(4, 3, 3, 12, 6, 13),
            VoxelShapes.combineAndSimplify(Block.makeCuboidShape(6, 3, 1.1, 10, 9.5, 14.85),
              VoxelShapes.combineAndSimplify(Block.makeCuboidShape(10, 3, 2.5, 14, 8, 13.5),
                Block.makeCuboidShape(2, 3, 2.5, 6, 8, 13.5),
                IBooleanFunction.OR), IBooleanFunction.OR), IBooleanFunction.OR),
          IBooleanFunction.OR), IBooleanFunction.OR));
    YveltalEgg.YVELTAL_TOP.put(Direction.SOUTH,
      VoxelShapes.combineAndSimplify(Block.makeCuboidShape(1, 0, 3, 15, 3, 13),
        VoxelShapes.combineAndSimplify(Block.makeCuboidShape(4, 0, 2, 12, 5, 14),
          VoxelShapes.combineAndSimplify(Block.makeCuboidShape(3, 3, 4, 13, 6, 12),
            VoxelShapes.combineAndSimplify(Block.makeCuboidShape(1.15, 3, 6, 14.9, 9.5, 10),
              VoxelShapes.combineAndSimplify(Block.makeCuboidShape(2.5, 3, 2, 13.5, 8, 6),
                Block.makeCuboidShape(2.5, 3, 10, 13.5, 8, 14),
                IBooleanFunction.OR), IBooleanFunction.OR), IBooleanFunction.OR),
          IBooleanFunction.OR), IBooleanFunction.OR));
    YveltalEgg.YVELTAL_TOP.put(Direction.WEST,
      VoxelShapes.combineAndSimplify(Block.makeCuboidShape(3, 0, 1, 13, 3, 15),
        VoxelShapes.combineAndSimplify(Block.makeCuboidShape(2, 0, 4, 14, 5, 12),
          VoxelShapes.combineAndSimplify(Block.makeCuboidShape(4, 3, 3, 12, 6, 13),
            VoxelShapes.combineAndSimplify(Block.makeCuboidShape(6, 3, 1.15, 10, 9.5, 14.9),
              VoxelShapes.combineAndSimplify(Block.makeCuboidShape(10, 3, 2.5, 14, 8, 13.5),
                Block.makeCuboidShape(2, 3, 2.5, 6, 8, 13.5),
                IBooleanFunction.OR), IBooleanFunction.OR), IBooleanFunction.OR),
          IBooleanFunction.OR), IBooleanFunction.OR));

    YveltalEgg.YVELTAL_BOTTOM.put(Direction.NORTH,
      VoxelShapes.combineAndSimplify(Block.makeCuboidShape(4, 0, 2, 12, 4, 14),
        VoxelShapes.combineAndSimplify(Block.makeCuboidShape(3, 4, 1, 13, 15, 15),
          VoxelShapes.combineAndSimplify(Block.makeCuboidShape(1, 7, 3, 15, 16, 13),
            VoxelShapes.combineAndSimplify(Block.makeCuboidShape(4, 15, 2, 12, 16, 14),
              VoxelShapes.combineAndSimplify(Block.makeCuboidShape(11.9, 3.32, 4, 14.9, 7.32, 12),
                VoxelShapes.combineAndSimplify(Block.makeCuboidShape(1.1, 3.32, 4, 4.1, 7.32, 12),
                  VoxelShapes.combineAndSimplify(Block.makeCuboidShape(10.25, 2, 5, 13.25, 4, 11),
                              Block.makeCuboidShape(2.75, 2, 5, 5.75, 4, 11),
                              IBooleanFunction.OR), IBooleanFunction.OR), IBooleanFunction.OR),
                        IBooleanFunction.OR), IBooleanFunction.OR), IBooleanFunction.OR),
                  IBooleanFunction.OR));
    YveltalEgg.YVELTAL_BOTTOM.put(Direction.EAST,
      VoxelShapes.combineAndSimplify(Block.makeCuboidShape(2, 0, 4, 14, 4, 12),
        VoxelShapes.combineAndSimplify(Block.makeCuboidShape(1, 4, 3, 15, 15, 13),
          VoxelShapes.combineAndSimplify(Block.makeCuboidShape(3, 7, 1, 13, 16, 15),
            VoxelShapes.combineAndSimplify(Block.makeCuboidShape(2, 15, 4, 14, 16, 12),
              VoxelShapes.combineAndSimplify(Block.makeCuboidShape(4, 3.32, 11.9, 12, 7.32, 14.9),
                VoxelShapes.combineAndSimplify(Block.makeCuboidShape(4, 3.32, 1.1, 12, 7.32, 4.1),
                  VoxelShapes.combineAndSimplify(Block.makeCuboidShape(5, 2, 10.25, 11, 4, 13.25),
                    Block.makeCuboidShape(5, 2, 2.75, 11, 4, 5.75),
                    IBooleanFunction.OR), IBooleanFunction.OR), IBooleanFunction.OR),
              IBooleanFunction.OR), IBooleanFunction.OR), IBooleanFunction.OR),
        IBooleanFunction.OR));
    YveltalEgg.YVELTAL_BOTTOM.put(Direction.SOUTH,
      VoxelShapes.combineAndSimplify(Block.makeCuboidShape(4, 0, 2, 12, 4, 14),
        VoxelShapes.combineAndSimplify(Block.makeCuboidShape(3, 4, 1, 13, 15, 15),
          VoxelShapes.combineAndSimplify(Block.makeCuboidShape(1, 7, 3, 15, 16, 13),
            VoxelShapes.combineAndSimplify(Block.makeCuboidShape(4, 15, 2, 12, 16, 14),
              VoxelShapes.combineAndSimplify(Block.makeCuboidShape(11.9, 3.32, 4, 14.9, 7.32, 12),
                VoxelShapes.combineAndSimplify(Block.makeCuboidShape(1.1, 3.32, 4, 4.1, 7.32, 12),
                  VoxelShapes.combineAndSimplify(Block.makeCuboidShape(10.25, 2, 5, 13.25, 4, 11),
                    Block.makeCuboidShape(2.75, 2, 5, 5.75, 4, 11),
                    IBooleanFunction.OR), IBooleanFunction.OR), IBooleanFunction.OR),
              IBooleanFunction.OR), IBooleanFunction.OR), IBooleanFunction.OR),
        IBooleanFunction.OR));
    YveltalEgg.YVELTAL_BOTTOM.put(Direction.WEST,
      VoxelShapes.combineAndSimplify(Block.makeCuboidShape(2, 0, 4, 14, 4, 12),
        VoxelShapes.combineAndSimplify(Block.makeCuboidShape(1, 4, 3, 15, 15, 13),
          VoxelShapes.combineAndSimplify(Block.makeCuboidShape(3, 7, 1, 13, 16, 15),
            VoxelShapes.combineAndSimplify(Block.makeCuboidShape(2, 15, 4, 14, 16, 12),
              VoxelShapes.combineAndSimplify(Block.makeCuboidShape(4, 3.32, 11.9, 12, 7.32, 14.9),
                VoxelShapes.combineAndSimplify(Block.makeCuboidShape(4, 3.32, 1.1, 12, 7.32, 4.1),
                  VoxelShapes.combineAndSimplify(Block.makeCuboidShape(5, 2, 10.25, 11, 4, 13.25),
                    Block.makeCuboidShape(5, 2, 2.75, 11, 4, 5.75),
                    IBooleanFunction.OR), IBooleanFunction.OR), IBooleanFunction.OR),
              IBooleanFunction.OR), IBooleanFunction.OR), IBooleanFunction.OR),
        IBooleanFunction.OR));
    //@formatter:on
    }

    // Precise selection box
    @Override
    public VoxelShape getShape(final BlockState state, final IBlockReader worldIn, final BlockPos pos,
            final ISelectionContext context)
    {
        final YveltalEggPart half = state.get(YveltalEgg.HALF);
        if (half == YveltalEggPart.BOTTOM) return YveltalEgg.YVELTAL_BOTTOM.get(state.get(YveltalEgg.FACING));
        else return YveltalEgg.YVELTAL_TOP.get(state.get(YveltalEgg.FACING));
    }

    public YveltalEgg(final String name, final Properties props)
    {
        super(name, props);
        this.setDefaultState(this.stateContainer.getBaseState().with(YveltalEgg.FACING, Direction.NORTH).with(
                YveltalEgg.WATERLOGGED, false).with(YveltalEgg.HALF, YveltalEggPart.BOTTOM));
    }

    // Places Yveltal Egg Spawner with both top and bottom pieces
    @Override
    public void onBlockPlacedBy(final World world, final BlockPos pos, final BlockState state,
            @Nullable final LivingEntity entity, final ItemStack stack)
    {
        if (entity != null)
        {
            final IFluidState fluidState = world.getFluidState(pos.up());
            world.setBlockState(pos.up(), state.with(YveltalEgg.HALF, YveltalEggPart.TOP).with(YveltalEgg.WATERLOGGED,
                    fluidState.getFluid() == Fluids.WATER), 3);
        }
    }

    // Breaking Yveltal Egg Spawner breaks both parts and returns one item only
    @Override
    public void onBlockHarvested(final World world, final BlockPos pos, final BlockState state,
            final PlayerEntity player)
    {
        final Direction facing = state.get(YveltalEgg.FACING);
        final BlockPos yveltalEggPos = this.getYveltalEggPos(pos, state.get(YveltalEgg.HALF), facing);
        BlockState YveltalEggBlockState = world.getBlockState(yveltalEggPos);
        if (YveltalEggBlockState.getBlock() == this && !pos.equals(yveltalEggPos)) this.removeHalf(world, yveltalEggPos,
                YveltalEggBlockState);
        final BlockPos yveltalEggPartPos = this.getYveltalEggTopPos(yveltalEggPos, facing);
        YveltalEggBlockState = world.getBlockState(yveltalEggPartPos);
        if (YveltalEggBlockState.getBlock() == this && !pos.equals(yveltalEggPartPos)) this.removeHalf(world,
                yveltalEggPartPos, YveltalEggBlockState);
        super.onBlockHarvested(world, pos, state, player);
    }

    private BlockPos getYveltalEggTopPos(final BlockPos base, final Direction facing)
    {
        switch (facing)
        {
        default:
            return base.up();
        }
    }

    private BlockPos getYveltalEggPos(final BlockPos pos, final YveltalEggPart part, final Direction facing)
    {
        if (part == YveltalEggPart.BOTTOM) return pos;
        switch (facing)
        {
        default:
            return pos.down();
        }
    }

    // Breaking the Yveltal Egg Spawner leaves water if underwater
    private void removeHalf(final World world, final BlockPos pos, final BlockState state)
    {
        final IFluidState fluidState = world.getFluidState(pos);
        if (fluidState.getFluid() == Fluids.WATER) world.setBlockState(pos, fluidState.getBlockState(), 35);
        else world.setBlockState(pos, Blocks.AIR.getDefaultState(), 35);
    }

    // Prevents the Yveltal Egg Spawner from replacing blocks above it and
    // checks for water
    @Override
    public BlockState getStateForPlacement(final BlockItemUseContext context)
    {
        final IFluidState ifluidstate = context.getWorld().getFluidState(context.getPos());
        final BlockPos pos = context.getPos();

        final BlockPos yveltalEggPos = this.getYveltalEggTopPos(pos, context.getPlacementHorizontalFacing()
                .getOpposite());
        if (pos.getY() < 255 && yveltalEggPos.getY() < 255 && context.getWorld().getBlockState(pos.up()).isReplaceable(
                context)) return this.getDefaultState().with(YveltalEgg.FACING, context.getPlacementHorizontalFacing()
                        .getOpposite()).with(YveltalEgg.HALF, YveltalEggPart.BOTTOM).with(YveltalEgg.WATERLOGGED,
                                ifluidstate.isTagged(FluidTags.WATER) && ifluidstate.getLevel() == 8);
        return null;
    }

    @Override
    protected void fillStateContainer(final StateContainer.Builder<Block, BlockState> builder)
    {
        builder.add(YveltalEgg.HALF, YveltalEgg.FACING, YveltalEgg.WATERLOGGED);
    }
}