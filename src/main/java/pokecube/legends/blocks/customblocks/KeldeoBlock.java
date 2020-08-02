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

public class KeldeoBlock extends Rotates implements IWaterLoggable
{
    private static final EnumProperty<KeldeoBlockPart> HALF          = EnumProperty.create("half",
            KeldeoBlockPart.class);
    private static final Map<Direction, VoxelShape>    KELDEO_TOP    = new HashMap<>();
    private static final Map<Direction, VoxelShape>    KELDEO_BOTTOM = new HashMap<>();
    private static final BooleanProperty               WATERLOGGED   = BlockStateProperties.WATERLOGGED;
    private static final DirectionProperty             FACING        = HorizontalBlock.HORIZONTAL_FACING;

    // Precise selection box
    static
    {
      //@formatter:off
    KeldeoBlock.KELDEO_TOP.put(Direction.NORTH,
      VoxelShapes.combineAndSimplify(Block.makeCuboidShape(10.5, 3, 7.5, 16, 15.25, 8.5),
      Block.makeCuboidShape(5.5, 0, 7.5, 11, 10.25, 8.5),
      IBooleanFunction.OR));
    KeldeoBlock.KELDEO_TOP.put(Direction.EAST,
      VoxelShapes.combineAndSimplify(Block.makeCuboidShape(7.5, 3, 10.5, 8.5, 15.25, 16),
        Block.makeCuboidShape(7.5, 0, 5.5, 8.5, 10.25, 11),
        IBooleanFunction.OR));
    KeldeoBlock.KELDEO_TOP.put(Direction.SOUTH,
      VoxelShapes.combineAndSimplify(Block.makeCuboidShape(0, 3, 7.5, 5.5, 15.25, 8.5),
        Block.makeCuboidShape(5, 0, 7.5, 10.5, 10.25, 8.5),
        IBooleanFunction.OR));
    KeldeoBlock.KELDEO_TOP.put(Direction.WEST,
      VoxelShapes.combineAndSimplify(Block.makeCuboidShape(7.5, 3, 0, 8.5, 15.25, 5.5),
        Block.makeCuboidShape(7.5, 0, 5, 8.5, 10.25, 10.5),
        IBooleanFunction.OR));

    KeldeoBlock.KELDEO_BOTTOM.put(Direction.NORTH,
      VoxelShapes.combineAndSimplify(Block.makeCuboidShape(8, 0, 4, 16, 4, 12),
        VoxelShapes.combineAndSimplify(Block.makeCuboidShape(4.5, 4, 5, 12.5, 12, 13),
          VoxelShapes.combineAndSimplify(Block.makeCuboidShape(0, 0, 4, 8, 4, 8),
            VoxelShapes.combineAndSimplify(Block.makeCuboidShape(6, 0, 12, 14, 4, 16),
              VoxelShapes.combineAndSimplify(Block.makeCuboidShape(2, 0, 0, 10, 4, 4),
                VoxelShapes.combineAndSimplify(Block.makeCuboidShape(4.75, 4, 1, 9.75, 8, 5),
                  VoxelShapes.combineAndSimplify(Block.makeCuboidShape(10, 0, 1, 13, 3, 4),
                    VoxelShapes.combineAndSimplify(Block.makeCuboidShape(12.5, 4, 6, 14.5, 7, 11),
                      VoxelShapes.combineAndSimplify(Block.makeCuboidShape(0.5, 4, 5, 4.5, 8, 11),
                        VoxelShapes.combineAndSimplify(Block.makeCuboidShape(2, 0, 12, 6, 4, 16),
                          VoxelShapes.combineAndSimplify(Block.makeCuboidShape(5, 12, 6, 11, 16, 10),
                            VoxelShapes.combineAndSimplify(Block.makeCuboidShape(6, 4, 12, 12, 8, 16),
                              Block.makeCuboidShape(0, 0, 8, 8, 4, 12),
                              IBooleanFunction.OR), IBooleanFunction.OR), IBooleanFunction.OR),
                        IBooleanFunction.OR), IBooleanFunction.OR), IBooleanFunction.OR),
                  IBooleanFunction.OR), IBooleanFunction.OR), IBooleanFunction.OR),
            IBooleanFunction.OR), IBooleanFunction.OR), IBooleanFunction.OR)
    );
    KeldeoBlock.KELDEO_BOTTOM.put(Direction.EAST,
      VoxelShapes.combineAndSimplify(Block.makeCuboidShape(4, 0, 8, 12, 4, 16),
        VoxelShapes.combineAndSimplify(Block.makeCuboidShape(3, 4, 4.5, 11, 12, 12.5),
          VoxelShapes.combineAndSimplify(Block.makeCuboidShape(8, 0, 0, 12, 4, 8),
            VoxelShapes.combineAndSimplify(Block.makeCuboidShape(0, 0, 6, 4, 4, 14),
              VoxelShapes.combineAndSimplify(Block.makeCuboidShape(12, 0, 2, 16, 4, 10),
                VoxelShapes.combineAndSimplify(Block.makeCuboidShape(11, 4, 4.75, 15, 8, 9.75),
                  VoxelShapes.combineAndSimplify(Block.makeCuboidShape(12, 0, 10, 15, 3, 13),
                    VoxelShapes.combineAndSimplify(Block.makeCuboidShape(5, 4, 12.5, 10, 7, 14.5),
                      VoxelShapes.combineAndSimplify(Block.makeCuboidShape(5, 4, 0.5, 11, 8, 4.5),
                        VoxelShapes.combineAndSimplify(Block.makeCuboidShape(0, 0, 2, 4, 4, 6),
                          VoxelShapes.combineAndSimplify(Block.makeCuboidShape(6, 12, 5, 10, 16, 11),
                            VoxelShapes.combineAndSimplify(Block.makeCuboidShape(0, 4, 6, 4, 8, 12),
                              Block.makeCuboidShape(4, 0, 0, 8, 4, 8),
                              IBooleanFunction.OR), IBooleanFunction.OR), IBooleanFunction.OR),
                        IBooleanFunction.OR), IBooleanFunction.OR), IBooleanFunction.OR),
                  IBooleanFunction.OR), IBooleanFunction.OR), IBooleanFunction.OR),
            IBooleanFunction.OR), IBooleanFunction.OR), IBooleanFunction.OR)
    );
    KeldeoBlock.KELDEO_BOTTOM.put(Direction.SOUTH,
      VoxelShapes.combineAndSimplify(Block.makeCuboidShape(0, 0, 4, 8, 4, 12),
        VoxelShapes.combineAndSimplify(Block.makeCuboidShape(3.5, 4, 3, 11.5, 12, 11),
          VoxelShapes.combineAndSimplify(Block.makeCuboidShape(8, 0, 8, 16, 4, 12),
            VoxelShapes.combineAndSimplify(Block.makeCuboidShape(2, 0, 0, 10, 4, 4),
              VoxelShapes.combineAndSimplify(Block.makeCuboidShape(6, 0, 12, 14, 4, 16),
                VoxelShapes.combineAndSimplify(Block.makeCuboidShape(6.25, 4, 11, 11.25, 8, 15),
                  VoxelShapes.combineAndSimplify(Block.makeCuboidShape(3, 0, 12, 6, 3, 15),
                    VoxelShapes.combineAndSimplify(Block.makeCuboidShape(1.5, 4, 5, 3.5, 7, 10),
                      VoxelShapes.combineAndSimplify(Block.makeCuboidShape(11.5, 4, 5, 15.5, 8, 11),
                        VoxelShapes.combineAndSimplify(Block.makeCuboidShape(10, 0, 0, 14, 4, 4),
                          VoxelShapes.combineAndSimplify(Block.makeCuboidShape(5, 12, 6, 11, 16, 10),
                            VoxelShapes.combineAndSimplify(Block.makeCuboidShape(4, 4, 0, 10, 8, 4),
                              Block.makeCuboidShape(8, 0, 4, 16, 4, 8),
                              IBooleanFunction.OR), IBooleanFunction.OR), IBooleanFunction.OR),
                        IBooleanFunction.OR), IBooleanFunction.OR), IBooleanFunction.OR),
                  IBooleanFunction.OR), IBooleanFunction.OR), IBooleanFunction.OR),
            IBooleanFunction.OR), IBooleanFunction.OR), IBooleanFunction.OR)
    );
    KeldeoBlock.KELDEO_BOTTOM.put(Direction.WEST,
      VoxelShapes.combineAndSimplify(Block.makeCuboidShape(4, 0, 0, 12, 4, 8),
        VoxelShapes.combineAndSimplify(Block.makeCuboidShape(5, 4, 3.5, 13, 12, 11.5),
          VoxelShapes.combineAndSimplify(Block.makeCuboidShape(4, 0, 8, 8, 4, 16),
            VoxelShapes.combineAndSimplify(Block.makeCuboidShape(12, 0, 2, 16, 4, 10),
              VoxelShapes.combineAndSimplify(Block.makeCuboidShape(0, 0, 6, 4, 4, 14),
                VoxelShapes.combineAndSimplify(Block.makeCuboidShape(1, 4, 6.25, 5, 8, 11.25),
                  VoxelShapes.combineAndSimplify(Block.makeCuboidShape(1, 0, 3, 4, 3, 6),
                    VoxelShapes.combineAndSimplify(Block.makeCuboidShape(6, 4, 1.5, 11, 7, 3.5),
                      VoxelShapes.combineAndSimplify(Block.makeCuboidShape(5, 4, 11.5, 11, 8, 15.5),
                        VoxelShapes.combineAndSimplify(Block.makeCuboidShape(12, 0, 10, 16, 4, 14),
                          VoxelShapes.combineAndSimplify(Block.makeCuboidShape(6, 12, 5, 10, 16, 11),
                            VoxelShapes.combineAndSimplify(Block.makeCuboidShape(12, 4, 4, 16, 8, 10),
                              Block.makeCuboidShape(8, 0, 8, 12, 4, 16),
                              IBooleanFunction.OR), IBooleanFunction.OR), IBooleanFunction.OR),
                        IBooleanFunction.OR), IBooleanFunction.OR), IBooleanFunction.OR),
                  IBooleanFunction.OR), IBooleanFunction.OR), IBooleanFunction.OR),
            IBooleanFunction.OR), IBooleanFunction.OR), IBooleanFunction.OR)
    );
    //@formatter:on
    }

    // Precise selection box
    @Override
    public VoxelShape getShape(final BlockState state, final IBlockReader worldIn, final BlockPos pos,
            final ISelectionContext context)
    {
        final KeldeoBlockPart half = state.get(KeldeoBlock.HALF);
        if (half == KeldeoBlockPart.BOTTOM) return KeldeoBlock.KELDEO_BOTTOM.get(state.get(KeldeoBlock.FACING));
        else return KeldeoBlock.KELDEO_TOP.get(state.get(KeldeoBlock.FACING));
    }

    public KeldeoBlock(final String name, final Properties props)
    {
        super(name, props);
        this.setDefaultState(this.stateContainer.getBaseState().with(KeldeoBlock.FACING, Direction.NORTH).with(
                KeldeoBlock.WATERLOGGED, false).with(KeldeoBlock.HALF, KeldeoBlockPart.BOTTOM));
    }

    // Places Keldeo Spawner with both top and bottom pieces
    @Override
    public void onBlockPlacedBy(final World world, final BlockPos pos, final BlockState state,
            @Nullable final LivingEntity entity, final ItemStack stack)
    {
        if (entity != null)
        {
            final IFluidState fluidState = world.getFluidState(pos.up());
            world.setBlockState(pos.up(), state.with(KeldeoBlock.HALF, KeldeoBlockPart.TOP).with(
                    KeldeoBlock.WATERLOGGED, fluidState.getFluid() == Fluids.WATER), 3);
        }
    }

    // Breaking Keldeo Spawner breaks both parts and returns one item only
    @Override
    public void onBlockHarvested(final World world, final BlockPos pos, final BlockState state,
            final PlayerEntity player)
    {
        final Direction facing = state.get(KeldeoBlock.FACING);
        final BlockPos keldeoPos = this.getKeldeoPos(pos, state.get(KeldeoBlock.HALF), facing);
        BlockState KeldeoBlockState = world.getBlockState(keldeoPos);
        if (KeldeoBlockState.getBlock() == this && !pos.equals(keldeoPos)) this.removeHalf(world, keldeoPos,
                KeldeoBlockState);
        final BlockPos keldeoPartPos = this.getKeldeoTopPos(keldeoPos, facing);
        KeldeoBlockState = world.getBlockState(keldeoPartPos);
        if (KeldeoBlockState.getBlock() == this && !pos.equals(keldeoPartPos)) this.removeHalf(world, keldeoPartPos,
                KeldeoBlockState);
        super.onBlockHarvested(world, pos, state, player);
    }

    private BlockPos getKeldeoTopPos(final BlockPos base, final Direction facing)
    {
        switch (facing)
        {
        default:
            return base.up();
        }
    }

    private BlockPos getKeldeoPos(final BlockPos pos, final KeldeoBlockPart part, final Direction facing)
    {
        if (part == KeldeoBlockPart.BOTTOM) return pos;
        switch (facing)
        {
        default:
            return pos.down();
        }
    }

    // Breaking the Keldeo Spawner leaves water if underwater
    private void removeHalf(final World world, final BlockPos pos, final BlockState state)
    {
        final IFluidState fluidState = world.getFluidState(pos);
        if (fluidState.getFluid() == Fluids.WATER) world.setBlockState(pos, fluidState.getBlockState(), 35);
        else world.setBlockState(pos, Blocks.AIR.getDefaultState(), 35);
    }

    // Prevents the Keldeo Spawner from replacing blocks above it and checks for
    // water
    @Override
    public BlockState getStateForPlacement(final BlockItemUseContext context)
    {
        final IFluidState ifluidstate = context.getWorld().getFluidState(context.getPos());
        final BlockPos pos = context.getPos();

        final BlockPos keldeoPos = this.getKeldeoTopPos(pos, context.getPlacementHorizontalFacing().getOpposite());
        if (pos.getY() < 255 && keldeoPos.getY() < 255 && context.getWorld().getBlockState(pos.up()).isReplaceable(
                context)) return this.getDefaultState().with(KeldeoBlock.FACING, context.getPlacementHorizontalFacing()
                        .getOpposite()).with(KeldeoBlock.HALF, KeldeoBlockPart.BOTTOM).with(KeldeoBlock.WATERLOGGED,
                                ifluidstate.isTagged(FluidTags.WATER) && ifluidstate.getLevel() == 8);
        return null;
    }

    @Override
    protected void fillStateContainer(final StateContainer.Builder<Block, BlockState> builder)
    {
        builder.add(KeldeoBlock.HALF, KeldeoBlock.FACING, KeldeoBlock.WATERLOGGED);
    }
}