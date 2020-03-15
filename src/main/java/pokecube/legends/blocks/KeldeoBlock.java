package pokecube.legends.blocks;

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
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import org.antlr.v4.runtime.misc.NotNull;
import java.util.HashMap;
import java.util.Map;
import static net.minecraft.util.math.shapes.VoxelShapes.combineAndSimplify;

public class KeldeoBlock extends Rotates implements IWaterLoggable
{
  private static final EnumProperty<KeldeoBlockPart> HALF = EnumProperty.create("half", KeldeoBlockPart.class);
  private static final Map<Direction, VoxelShape> KELDEO_TOP = new HashMap<>();
  private static final Map<Direction, VoxelShape> KELDEO_BOTTOM = new HashMap<>();
  private static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
  private static final DirectionProperty FACING = HorizontalBlock.HORIZONTAL_FACING;

  //Precise selection box
  static
  {
    KELDEO_TOP.put(Direction.NORTH,
      combineAndSimplify(makeCuboidShape(10.5, 3, 7.5, 16, 15.25, 8.5),
      makeCuboidShape(5.5, 0, 7.5, 11, 10.25, 8.5),
      IBooleanFunction.OR));
    KELDEO_TOP.put(Direction.EAST,
      combineAndSimplify(makeCuboidShape(7.5, 3, 10.5, 8.5, 15.25, 16),
        makeCuboidShape(7.5, 0, 5.5, 8.5, 10.25, 11),
        IBooleanFunction.OR));
    KELDEO_TOP.put(Direction.SOUTH,
      combineAndSimplify(makeCuboidShape(0, 3, 7.5, 5.5, 15.25, 8.5),
        makeCuboidShape(5, 0, 7.5, 10.5, 10.25, 8.5),
        IBooleanFunction.OR));
    KELDEO_TOP.put(Direction.WEST,
      combineAndSimplify(makeCuboidShape(7.5, 3, 0, 8.5, 15.25, 5.5),
        makeCuboidShape(7.5, 0, 5, 8.5, 10.25, 10.5),
        IBooleanFunction.OR));

    KELDEO_BOTTOM.put(Direction.NORTH,
      combineAndSimplify(makeCuboidShape(8, 0, 4, 16, 4, 12),
        combineAndSimplify(makeCuboidShape(4.5, 4, 5, 12.5, 12, 13),
          combineAndSimplify(makeCuboidShape(0, 0, 4, 8, 4, 8),
            combineAndSimplify(makeCuboidShape(6, 0, 12, 14, 4, 16),
              combineAndSimplify(makeCuboidShape(2, 0, 0, 10, 4, 4),
                combineAndSimplify(makeCuboidShape(4.75, 4, 1, 9.75, 8, 5),
                  combineAndSimplify(makeCuboidShape(10, 0, 1, 13, 3, 4),
                    combineAndSimplify(makeCuboidShape(12.5, 4, 6, 14.5, 7, 11),
                      combineAndSimplify(makeCuboidShape(0.5, 4, 5, 4.5, 8, 11),
                        combineAndSimplify(makeCuboidShape(2, 0, 12, 6, 4, 16),
                          combineAndSimplify(makeCuboidShape(5, 12, 6, 11, 16, 10),
                            combineAndSimplify(makeCuboidShape(6, 4, 12, 12, 8, 16),
                              makeCuboidShape(0, 0, 8, 8, 4, 12),
                              IBooleanFunction.OR), IBooleanFunction.OR), IBooleanFunction.OR),
                        IBooleanFunction.OR), IBooleanFunction.OR), IBooleanFunction.OR),
                  IBooleanFunction.OR), IBooleanFunction.OR), IBooleanFunction.OR),
            IBooleanFunction.OR), IBooleanFunction.OR), IBooleanFunction.OR)
    );
    KELDEO_BOTTOM.put(Direction.EAST,
      combineAndSimplify(makeCuboidShape(4, 0, 8, 12, 4, 16),
        combineAndSimplify(makeCuboidShape(3, 4, 4.5, 11, 12, 12.5),
          combineAndSimplify(makeCuboidShape(8, 0, 0, 12, 4, 8),
            combineAndSimplify(makeCuboidShape(0, 0, 6, 4, 4, 14),
              combineAndSimplify(makeCuboidShape(12, 0, 2, 16, 4, 10),
                combineAndSimplify(makeCuboidShape(11, 4, 4.75, 15, 8, 9.75),
                  combineAndSimplify(makeCuboidShape(12, 0, 10, 15, 3, 13),
                    combineAndSimplify(makeCuboidShape(5, 4, 12.5, 10, 7, 14.5),
                      combineAndSimplify(makeCuboidShape(5, 4, 0.5, 11, 8, 4.5),
                        combineAndSimplify(makeCuboidShape(0, 0, 2, 4, 4, 6),
                          combineAndSimplify(makeCuboidShape(6, 12, 5, 10, 16, 11),
                            combineAndSimplify(makeCuboidShape(0, 4, 6, 4, 8, 12),
                              makeCuboidShape(4, 0, 0, 8, 4, 8),
                              IBooleanFunction.OR), IBooleanFunction.OR), IBooleanFunction.OR),
                        IBooleanFunction.OR), IBooleanFunction.OR), IBooleanFunction.OR),
                  IBooleanFunction.OR), IBooleanFunction.OR), IBooleanFunction.OR),
            IBooleanFunction.OR), IBooleanFunction.OR), IBooleanFunction.OR)
    );
    KELDEO_BOTTOM.put(Direction.SOUTH,
      combineAndSimplify(makeCuboidShape(0, 0, 4, 8, 4, 12),
        combineAndSimplify(makeCuboidShape(3.5, 4, 3, 11.5, 12, 11),
          combineAndSimplify(makeCuboidShape(8, 0, 8, 16, 4, 12),
            combineAndSimplify(makeCuboidShape(2, 0, 0, 10, 4, 4),
              combineAndSimplify(makeCuboidShape(6, 0, 12, 14, 4, 16),
                combineAndSimplify(makeCuboidShape(6.25, 4, 11, 11.25, 8, 15),
                  combineAndSimplify(makeCuboidShape(3, 0, 12, 6, 3, 15),
                    combineAndSimplify(makeCuboidShape(1.5, 4, 5, 3.5, 7, 10),
                      combineAndSimplify(makeCuboidShape(11.5, 4, 5, 15.5, 8, 11),
                        combineAndSimplify(makeCuboidShape(10, 0, 0, 14, 4, 4),
                          combineAndSimplify(makeCuboidShape(5, 12, 6, 11, 16, 10),
                            combineAndSimplify(makeCuboidShape(4, 4, 0, 10, 8, 4),
                              makeCuboidShape(8, 0, 4, 16, 4, 8),
                              IBooleanFunction.OR), IBooleanFunction.OR), IBooleanFunction.OR),
                        IBooleanFunction.OR), IBooleanFunction.OR), IBooleanFunction.OR),
                  IBooleanFunction.OR), IBooleanFunction.OR), IBooleanFunction.OR),
            IBooleanFunction.OR), IBooleanFunction.OR), IBooleanFunction.OR)
    );
    KELDEO_BOTTOM.put(Direction.WEST,
      combineAndSimplify(makeCuboidShape(4, 0, 0, 12, 4, 8),
        combineAndSimplify(makeCuboidShape(5, 4, 3.5, 13, 12, 11.5),
          combineAndSimplify(makeCuboidShape(4, 0, 8, 8, 4, 16),
            combineAndSimplify(makeCuboidShape(12, 0, 2, 16, 4, 10),
              combineAndSimplify(makeCuboidShape(0, 0, 6, 4, 4, 14),
                combineAndSimplify(makeCuboidShape(1, 4, 6.25, 5, 8, 11.25),
                  combineAndSimplify(makeCuboidShape(1, 0, 3, 4, 3, 6),
                    combineAndSimplify(makeCuboidShape(6, 4, 1.5, 11, 7, 3.5),
                      combineAndSimplify(makeCuboidShape(5, 4, 11.5, 11, 8, 15.5),
                        combineAndSimplify(makeCuboidShape(12, 0, 10, 16, 4, 14),
                          combineAndSimplify(makeCuboidShape(6, 12, 5, 10, 16, 11),
                            combineAndSimplify(makeCuboidShape(12, 4, 4, 16, 8, 10),
                              makeCuboidShape(8, 0, 8, 12, 4, 16),
                              IBooleanFunction.OR), IBooleanFunction.OR), IBooleanFunction.OR),
                        IBooleanFunction.OR), IBooleanFunction.OR), IBooleanFunction.OR),
                  IBooleanFunction.OR), IBooleanFunction.OR), IBooleanFunction.OR),
            IBooleanFunction.OR), IBooleanFunction.OR), IBooleanFunction.OR)
    );
  }

  //Precise selection box
  public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context)
  {
    KeldeoBlockPart half = state.get(HALF);
    if (half == KeldeoBlockPart.BOTTOM)
    {
      return KELDEO_BOTTOM.get(state.get(FACING));
    }
    else
    {
      return KELDEO_TOP.get(state.get(FACING));
    }
  }

  public KeldeoBlock(final String name, final Properties props)
  {
    super(name, props);
    this.setDefaultState(this.stateContainer.getBaseState()
      .with(FACING, Direction.NORTH)
      .with(WATERLOGGED, false)
      .with(HALF, KeldeoBlockPart.BOTTOM));
  }

  //Places Keldeo Spawner with both top and bottom pieces
  @Override
  public void onBlockPlacedBy(World world, BlockPos pos, BlockState state, @Nullable LivingEntity entity, ItemStack stack)
  {
    if (entity != null)
    {
      IFluidState fluidState = world.getFluidState(pos.up());
      world.setBlockState(pos.up(),
        state.with(HALF, KeldeoBlockPart.TOP)
          .with(WATERLOGGED,  fluidState.getFluid() == Fluids.WATER), 3);
    }
  }

  //Breaking Keldeo Spawner breaks both parts and returns one item only
  public void onBlockHarvested(World world, @NotNull BlockPos pos, BlockState state, @NotNull PlayerEntity player)
  {
    Direction facing = state.get(FACING);
    BlockPos keldeoPos = getKeldeoPos(pos, state.get(HALF), facing);
    BlockState KeldeoBlockState = world.getBlockState(keldeoPos);
    if (KeldeoBlockState.getBlock() == this && !pos.equals(keldeoPos))
    {
      removeHalf(world, keldeoPos, KeldeoBlockState);
    }
    BlockPos keldeoPartPos = getKeldeoTopPos(keldeoPos, facing);
    KeldeoBlockState = world.getBlockState(keldeoPartPos);
    if (KeldeoBlockState.getBlock() == this && !pos.equals(keldeoPartPos))
    {
      removeHalf(world, keldeoPartPos, KeldeoBlockState);
    }
    super.onBlockHarvested(world, pos, state, player);
  }

  private BlockPos getKeldeoTopPos(BlockPos base, Direction facing)
  {
    switch (facing)
    {
      default:
        return base.up();
    }
  }

  private BlockPos getKeldeoPos(BlockPos pos, KeldeoBlockPart part, Direction facing)
  {
    if (part == KeldeoBlockPart.BOTTOM) return pos;
    switch (facing)
    {
      default:
        return pos.down();
    }
  }

  //Breaking the Keldeo Spawner leaves water if underwater
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

  //Prevents the Keldeo Spawner from replacing blocks above it and checks for water
  @Override
  public BlockState getStateForPlacement(BlockItemUseContext context)
  {
    final IFluidState ifluidstate = context.getWorld().getFluidState(context.getPos());
    final BlockPos pos = context.getPos();

    final BlockPos keldeoPos = getKeldeoTopPos(pos, context.getPlacementHorizontalFacing().getOpposite());
    if (pos.getY() < 255 &&
      keldeoPos.getY() < 255 &&
      context.getWorld().getBlockState(pos.up()).isReplaceable(context))
      return this.getDefaultState()
        .with(FACING, context.getPlacementHorizontalFacing().getOpposite())
        .with(HALF, KeldeoBlockPart.BOTTOM)
        .with(WATERLOGGED, ifluidstate.isTagged(FluidTags.WATER) && ifluidstate.getLevel() == 8);
    return null;
  }

  @Override
  protected void fillStateContainer(final StateContainer.Builder<Block, BlockState> builder)
  {
      builder.add(HALF, FACING, WATERLOGGED);
  }
}