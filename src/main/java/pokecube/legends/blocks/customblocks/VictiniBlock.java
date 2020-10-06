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
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.IBooleanFunction;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import pokecube.legends.init.BlockInit;

public class VictiniBlock extends Rotates implements IWaterLoggable
{
    private static final EnumProperty<VictiniBlockPart> HALF           = EnumProperty.create("half",
            VictiniBlockPart.class);
    private static final Map<Direction, VoxelShape>     VICTINI_TOP    = new HashMap<>();
    private static final Map<Direction, VoxelShape>     VICTINI_BOTTOM = new HashMap<>();
    private static final BooleanProperty                WATERLOGGED    = BlockStateProperties.WATERLOGGED;
    private static final DirectionProperty              FACING         = HorizontalBlock.HORIZONTAL_FACING;

    // Precise selection box
    static
    {
      //@formatter:off
    VictiniBlock.VICTINI_TOP.put(Direction.NORTH,
      VoxelShapes.combineAndSimplify(Block.makeCuboidShape(13, 3, 3.5, 16, 4, 4),
        VoxelShapes.combineAndSimplify(Block.makeCuboidShape(12, 2, 3.5, 15, 3, 4),
          VoxelShapes.combineAndSimplify(Block.makeCuboidShape(11, 1, 3.5, 14, 2, 4),
            VoxelShapes.combineAndSimplify(Block.makeCuboidShape(10, 0, 3.5, 13, 1, 4),
              VoxelShapes.combineAndSimplify(Block.makeCuboidShape(3, 0, 3.5, 6, 1, 4),
                VoxelShapes.combineAndSimplify(Block.makeCuboidShape(2, 1, 3.5, 5, 2, 4),
                  VoxelShapes.combineAndSimplify(Block.makeCuboidShape(1, 2, 3.5, 4, 3, 4),
                    Block.makeCuboidShape(0, 3, 3.5, 3, 4, 4),
                    IBooleanFunction.OR), IBooleanFunction.OR), IBooleanFunction.OR),
              IBooleanFunction.OR), IBooleanFunction.OR), IBooleanFunction.OR),
        IBooleanFunction.OR)
    );
    VictiniBlock.VICTINI_TOP.put(Direction.EAST,
      VoxelShapes.combineAndSimplify(Block.makeCuboidShape(12, 3, 13, 12.5, 4, 16),
        VoxelShapes.combineAndSimplify(Block.makeCuboidShape(12, 2, 12, 12.5, 3, 15),
          VoxelShapes.combineAndSimplify(Block.makeCuboidShape(12, 1, 11, 12.5, 2, 14),
            VoxelShapes.combineAndSimplify(Block.makeCuboidShape(12, 0, 10, 12.5, 1, 13),
              VoxelShapes.combineAndSimplify(Block.makeCuboidShape(12, 0, 3, 12.5, 1, 6),
                VoxelShapes.combineAndSimplify(Block.makeCuboidShape(12, 1, 2, 12.5, 2, 5),
                  VoxelShapes.combineAndSimplify(Block.makeCuboidShape(12, 2, 1, 12.5, 3, 4),
                    Block.makeCuboidShape(12, 3, 0, 12.5, 4, 3),
                    IBooleanFunction.OR), IBooleanFunction.OR), IBooleanFunction.OR),
              IBooleanFunction.OR), IBooleanFunction.OR), IBooleanFunction.OR),
        IBooleanFunction.OR)
    );
    VictiniBlock.VICTINI_TOP.put(Direction.SOUTH,
      VoxelShapes.combineAndSimplify(Block.makeCuboidShape(0, 3, 12, 3, 4, 12.5),
        VoxelShapes.combineAndSimplify(Block.makeCuboidShape(1, 2, 12, 4, 3, 12.5),
          VoxelShapes.combineAndSimplify(Block.makeCuboidShape(2, 1, 12, 5, 2, 12.5),
            VoxelShapes.combineAndSimplify(Block.makeCuboidShape(3, 0, 12, 6, 1, 12.5),
              VoxelShapes.combineAndSimplify(Block.makeCuboidShape(10, 0, 12, 13, 1, 12.5),
                VoxelShapes.combineAndSimplify(Block.makeCuboidShape(11, 1, 12, 14, 2, 12.5),
                  VoxelShapes.combineAndSimplify(Block.makeCuboidShape(12, 2, 12, 15, 3, 12.5),
                    Block.makeCuboidShape(13, 3, 12, 16, 4, 12.5),
                    IBooleanFunction.OR), IBooleanFunction.OR), IBooleanFunction.OR),
              IBooleanFunction.OR), IBooleanFunction.OR), IBooleanFunction.OR),
        IBooleanFunction.OR)
    );
    VictiniBlock.VICTINI_TOP.put(Direction.WEST,
      VoxelShapes.combineAndSimplify(Block.makeCuboidShape(3.5, 3, 0, 4, 4, 3),
        VoxelShapes.combineAndSimplify(Block.makeCuboidShape(3.5, 2, 1, 4, 3, 4),
          VoxelShapes.combineAndSimplify(Block.makeCuboidShape(3.5, 1, 2, 4, 2, 5),
            VoxelShapes.combineAndSimplify(Block.makeCuboidShape(3.5, 0, 3, 4, 1, 6),
              VoxelShapes.combineAndSimplify(Block.makeCuboidShape(3.5, 0, 10, 4, 1, 13),
                VoxelShapes.combineAndSimplify(Block.makeCuboidShape(3.5, 1, 11, 4, 2, 14),
                  VoxelShapes.combineAndSimplify(Block.makeCuboidShape(3.5, 2, 12, 4, 3, 15),
                    Block.makeCuboidShape(3.5, 3, 13, 4, 4, 16),
                    IBooleanFunction.OR), IBooleanFunction.OR), IBooleanFunction.OR),
              IBooleanFunction.OR), IBooleanFunction.OR), IBooleanFunction.OR),
        IBooleanFunction.OR)
    );
    VictiniBlock.VICTINI_BOTTOM.put(Direction.NORTH,
      VoxelShapes.combineAndSimplify(Block.makeCuboidShape(13.5, 7, 7, 15.5, 15, 9),
        VoxelShapes.combineAndSimplify(Block.makeCuboidShape(12, 13.5, 7, 13.5, 15, 9),
          VoxelShapes.combineAndSimplify(Block.makeCuboidShape(2.5, 13.5, 7, 4, 15, 9),
            VoxelShapes.combineAndSimplify(Block.makeCuboidShape(4, 0, 4, 12, 2, 12),
              VoxelShapes.combineAndSimplify(Block.makeCuboidShape(6, 6.5, 6, 10, 8.5, 10),
                VoxelShapes.combineAndSimplify(Block.makeCuboidShape(4, 6, 10, 12, 16, 12),
                  VoxelShapes.combineAndSimplify(Block.makeCuboidShape(10, 6, 6, 12, 16, 10),
                    VoxelShapes.combineAndSimplify(Block.makeCuboidShape(4, 6, 6, 6, 16, 10),
                      VoxelShapes.combineAndSimplify(Block.makeCuboidShape(4, 6, 4, 12, 16, 6),
                        VoxelShapes.combineAndSimplify(Block.makeCuboidShape(4, 14, 3.5, 12, 15, 4),
                          VoxelShapes.combineAndSimplify(Block.makeCuboidShape(5, 13, 3.5, 11, 14, 4),
                            VoxelShapes.combineAndSimplify(Block.makeCuboidShape(9, 15, 3.5, 12, 16, 4),
                              VoxelShapes.combineAndSimplify(Block.makeCuboidShape(6, 12, 3.5, 10, 13, 4),
                                VoxelShapes.combineAndSimplify(Block.makeCuboidShape(6.5, 2, 6.5, 9.5, 6, 9.5),
                                  VoxelShapes.combineAndSimplify(Block.makeCuboidShape(7, 11, 3.5, 9, 12, 4),
                                    VoxelShapes.combineAndSimplify(Block.makeCuboidShape(4, 15, 3.5, 7, 16, 4),
                                      VoxelShapes.combineAndSimplify(Block.makeCuboidShape(0.5, 7, 7, 2.5, 15, 9),
                                        VoxelShapes.combineAndSimplify(Block.makeCuboidShape(2.5, 7, 7, 4, 8.5, 9),
                                          Block.makeCuboidShape(12, 7, 7, 13.5, 8.5, 9),
                                          IBooleanFunction.OR), IBooleanFunction.OR), IBooleanFunction.OR),
                                    IBooleanFunction.OR), IBooleanFunction.OR), IBooleanFunction.OR),
                              IBooleanFunction.OR), IBooleanFunction.OR), IBooleanFunction.OR),
                        IBooleanFunction.OR), IBooleanFunction.OR), IBooleanFunction.OR),
                  IBooleanFunction.OR), IBooleanFunction.OR), IBooleanFunction.OR),
            IBooleanFunction.OR), IBooleanFunction.OR), IBooleanFunction.OR)
    );
    VictiniBlock.VICTINI_BOTTOM.put(Direction.EAST,
      VoxelShapes.combineAndSimplify(Block.makeCuboidShape(7, 7, 13.5, 9, 15, 15.5),
        VoxelShapes.combineAndSimplify(Block.makeCuboidShape(7, 13.5, 12, 9, 15, 13.5),
          VoxelShapes.combineAndSimplify(Block.makeCuboidShape(7, 13.5, 2.5, 9, 15, 4),
            VoxelShapes.combineAndSimplify(Block.makeCuboidShape(4, 0, 4, 12, 2, 12),
              VoxelShapes.combineAndSimplify(Block.makeCuboidShape(6, 6.5, 6, 10, 8.5, 10),
                VoxelShapes.combineAndSimplify(Block.makeCuboidShape(4, 6, 4, 6, 16, 12),
                  VoxelShapes.combineAndSimplify(Block.makeCuboidShape(6, 6, 10, 10, 16, 12),
                    VoxelShapes.combineAndSimplify(Block.makeCuboidShape(6, 6, 4, 10, 16, 6),
                      VoxelShapes.combineAndSimplify(Block.makeCuboidShape(10, 6, 4, 12, 16, 12),
                        VoxelShapes.combineAndSimplify(Block.makeCuboidShape(12, 14, 4, 12.5, 15, 12),
                          VoxelShapes.combineAndSimplify(Block.makeCuboidShape(12, 13, 5, 12.5, 14, 11),
                            VoxelShapes.combineAndSimplify(Block.makeCuboidShape(12, 15, 9, 12.5, 16, 12),
                              VoxelShapes.combineAndSimplify(Block.makeCuboidShape(12, 12, 6, 12.5, 13, 10),
                                VoxelShapes.combineAndSimplify(Block.makeCuboidShape(6.5, 2, 6.5, 9.5, 6, 9.5),
                                  VoxelShapes.combineAndSimplify(Block.makeCuboidShape(12, 11, 7, 12.5, 12, 9),
                                    VoxelShapes.combineAndSimplify(Block.makeCuboidShape(12, 15, 4, 12.5, 16, 7),
                                      VoxelShapes.combineAndSimplify(Block.makeCuboidShape(7, 7, 0.5, 9, 15, 2.5),
                                        VoxelShapes.combineAndSimplify(Block.makeCuboidShape(7, 7, 2.5, 9, 8.5, 4),
                                          Block.makeCuboidShape(7, 7, 12, 9, 8.5, 13.5),
                                          IBooleanFunction.OR), IBooleanFunction.OR), IBooleanFunction.OR),
                                    IBooleanFunction.OR), IBooleanFunction.OR), IBooleanFunction.OR),
                              IBooleanFunction.OR), IBooleanFunction.OR), IBooleanFunction.OR),
                        IBooleanFunction.OR), IBooleanFunction.OR), IBooleanFunction.OR),
                  IBooleanFunction.OR), IBooleanFunction.OR), IBooleanFunction.OR),
            IBooleanFunction.OR), IBooleanFunction.OR), IBooleanFunction.OR)
    );
    VictiniBlock.VICTINI_BOTTOM.put(Direction.SOUTH,
      VoxelShapes.combineAndSimplify(Block.makeCuboidShape(0.5, 7, 7, 2.5, 15, 9),
        VoxelShapes.combineAndSimplify(Block.makeCuboidShape(2.5, 13.5, 7, 4, 15, 9),
          VoxelShapes.combineAndSimplify(Block.makeCuboidShape(12, 13.5, 7, 13.5, 15, 9),
            VoxelShapes.combineAndSimplify(Block.makeCuboidShape(4, 0, 4, 12, 2, 12),
              VoxelShapes.combineAndSimplify(Block.makeCuboidShape(6, 6.5, 6, 10, 8.5, 10),
                VoxelShapes.combineAndSimplify(Block.makeCuboidShape(4, 6, 4, 12, 16, 6),
                  VoxelShapes.combineAndSimplify(Block.makeCuboidShape(4, 6, 6, 6, 16, 10),
                    VoxelShapes.combineAndSimplify(Block.makeCuboidShape(10, 6, 6, 12, 16, 10),
                      VoxelShapes.combineAndSimplify(Block.makeCuboidShape(4, 6, 10, 12, 16, 12),
                        VoxelShapes.combineAndSimplify(Block.makeCuboidShape(4, 14, 12, 12, 15, 12.5),
                          VoxelShapes.combineAndSimplify(Block.makeCuboidShape(5, 13, 12, 11, 14, 12.5),
                            VoxelShapes.combineAndSimplify(Block.makeCuboidShape(4, 15, 12, 7, 16, 12.5),
                              VoxelShapes.combineAndSimplify(Block.makeCuboidShape(6, 12, 12, 10, 13, 12.5),
                                VoxelShapes.combineAndSimplify(Block.makeCuboidShape(6.5, 2, 6.5, 9.5, 6, 9.5),
                                  VoxelShapes.combineAndSimplify(Block.makeCuboidShape(7, 11, 12, 9, 12, 12.5),
                                    VoxelShapes.combineAndSimplify(Block.makeCuboidShape(9, 15, 12, 12, 16, 12.5),
                                      VoxelShapes.combineAndSimplify(Block.makeCuboidShape(13.5, 7, 7, 15.5, 15, 9),
                                        VoxelShapes.combineAndSimplify(Block.makeCuboidShape(12, 7, 7, 13.5, 8.5, 9),
                                          Block.makeCuboidShape(2.5, 7, 7, 4, 8.5, 9),
                                          IBooleanFunction.OR), IBooleanFunction.OR), IBooleanFunction.OR),
                                    IBooleanFunction.OR), IBooleanFunction.OR), IBooleanFunction.OR),
                              IBooleanFunction.OR), IBooleanFunction.OR), IBooleanFunction.OR),
                        IBooleanFunction.OR), IBooleanFunction.OR), IBooleanFunction.OR),
                  IBooleanFunction.OR), IBooleanFunction.OR), IBooleanFunction.OR),
            IBooleanFunction.OR), IBooleanFunction.OR), IBooleanFunction.OR)
    );
    VictiniBlock.VICTINI_BOTTOM.put(Direction.WEST,
      VoxelShapes.combineAndSimplify(Block.makeCuboidShape(7, 7, 0.5, 9, 15, 2.5),
        VoxelShapes.combineAndSimplify(Block.makeCuboidShape(7, 13.5, 2.5, 9, 15, 4),
          VoxelShapes.combineAndSimplify(Block.makeCuboidShape(7, 13.5, 12, 9, 15, 13.5),
            VoxelShapes.combineAndSimplify(Block.makeCuboidShape(4, 0, 4, 12, 2, 12),
              VoxelShapes.combineAndSimplify(Block.makeCuboidShape(6, 6.5, 6, 10, 8.5, 10),
                VoxelShapes.combineAndSimplify(Block.makeCuboidShape(10, 6, 4, 12, 16, 12),
                  VoxelShapes.combineAndSimplify(Block.makeCuboidShape(6, 6, 4, 10, 16, 6),
                    VoxelShapes.combineAndSimplify(Block.makeCuboidShape(6, 6, 10, 10, 16, 12),
                      VoxelShapes.combineAndSimplify(Block.makeCuboidShape(4, 6, 4, 6, 16, 12),
                        VoxelShapes.combineAndSimplify(Block.makeCuboidShape(3.5, 14, 4, 4, 15, 12),
                          VoxelShapes.combineAndSimplify(Block.makeCuboidShape(3.5, 13, 5, 4, 14, 11),
                            VoxelShapes.combineAndSimplify(Block.makeCuboidShape(3.5, 15, 4, 4, 16, 7),
                              VoxelShapes.combineAndSimplify(Block.makeCuboidShape(3.5, 12, 6, 4, 13, 10),
                                VoxelShapes.combineAndSimplify(Block.makeCuboidShape(6.5, 2, 6.5, 9.5, 6, 9.5),
                                  VoxelShapes.combineAndSimplify(Block.makeCuboidShape(3.5, 11, 7, 4, 12, 9),
                                    VoxelShapes.combineAndSimplify(Block.makeCuboidShape(3.5, 15, 9, 4, 16, 12),
                                      VoxelShapes.combineAndSimplify(Block.makeCuboidShape(7, 7, 13.5, 9, 15, 15.5),
                                        VoxelShapes.combineAndSimplify(Block.makeCuboidShape(7, 7, 12, 9, 8.5, 13.5),
                                          Block.makeCuboidShape(7, 7, 2.5, 9, 8.5, 4),
                                          IBooleanFunction.OR), IBooleanFunction.OR), IBooleanFunction.OR),
                                    IBooleanFunction.OR), IBooleanFunction.OR), IBooleanFunction.OR),
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
        final VictiniBlockPart half = state.get(VictiniBlock.HALF);
        if (half == VictiniBlockPart.BOTTOM) return VictiniBlock.VICTINI_BOTTOM.get(state.get(VictiniBlock.FACING));
        else return VictiniBlock.VICTINI_TOP.get(state.get(VictiniBlock.FACING));
    }

    public VictiniBlock(final String name, final Properties props)
    {
        super(name, props);
        this.setDefaultState(this.stateContainer.getBaseState().with(VictiniBlock.FACING, Direction.NORTH).with(
                VictiniBlock.WATERLOGGED, false).with(VictiniBlock.HALF, VictiniBlockPart.BOTTOM));
    }

    // Places Victini Spawner with both top and bottom pieces
    @Override
    public void onBlockPlacedBy(final World world, final BlockPos pos, final BlockState state,
            @Nullable final LivingEntity entity, final ItemStack stack)
    {
        if (entity != null)
        {
            final FluidState fluidState = world.getFluidState(pos.up());
            world.setBlockState(pos.up(), state.with(VictiniBlock.HALF, VictiniBlockPart.TOP).with(
                    VictiniBlock.WATERLOGGED, fluidState.getFluid() == Fluids.WATER), 3);
        }
    }

    // Breaking Victini Spawner breaks both parts and returns one item only
    @Override
    public void onBlockHarvested(final World world, final BlockPos pos, final BlockState state,
            final PlayerEntity player)
    {
        final Direction facing = state.get(VictiniBlock.FACING);
        final BlockPos victiniPos = this.getVictiniPos(pos, state.get(VictiniBlock.HALF), facing);
        BlockState VictiniBlockState = world.getBlockState(victiniPos);
        if (VictiniBlockState.getBlock() == this && !pos.equals(victiniPos) && this.getBlock() == BlockInit.VICTINI_CORE
                .get()) this.removeHalf(world, victiniPos, VictiniBlockState);
        final BlockPos victiniPartPos = this.getVictiniTopPos(victiniPos, facing);
        VictiniBlockState = world.getBlockState(victiniPartPos);
        if (VictiniBlockState.getBlock() == this && !pos.equals(victiniPartPos) && this
                .getBlock() == BlockInit.VICTINI_CORE.get()) this.removeHalf(world, victiniPartPos, VictiniBlockState);
        super.onBlockHarvested(world, pos, state, player);
    }

    private BlockPos getVictiniTopPos(final BlockPos base, final Direction facing)
    {
        switch (facing)
        {
        default:
            return base.up();
        }
    }

    private BlockPos getVictiniPos(final BlockPos pos, final VictiniBlockPart part, final Direction facing)
    {
        if (part == VictiniBlockPart.BOTTOM) return pos;
        switch (facing)
        {
        default:
            return pos.down();
        }
    }

    // Breaking the Victini Spawner leaves water if underwater
    private void removeHalf(final World world, final BlockPos pos, final BlockState state)
    {
        final FluidState fluidState = world.getFluidState(pos);
        if (fluidState.getFluid() == Fluids.WATER) world.setBlockState(pos, fluidState.getBlockState(), 35);
        else world.setBlockState(pos, Blocks.AIR.getDefaultState(), 35);
    }

    // Prevents the Victini Spawner from replacing blocks above it and checks
    // for water
    @Override
    public BlockState getStateForPlacement(final BlockItemUseContext context)
    {
        final FluidState ifluidstate = context.getWorld().getFluidState(context.getPos());
        final BlockPos pos = context.getPos();

        final BlockPos victiniPos = this.getVictiniTopPos(pos, context.getPlacementHorizontalFacing().getOpposite());
        if (pos.getY() < 255 && victiniPos.getY() < 255 && context.getWorld().getBlockState(pos.up()).isReplaceable(
                context)) return this.getDefaultState().with(VictiniBlock.FACING, context.getPlacementHorizontalFacing()
                        .getOpposite()).with(VictiniBlock.HALF, VictiniBlockPart.BOTTOM).with(VictiniBlock.WATERLOGGED,
                                ifluidstate.isTagged(FluidTags.WATER) && ifluidstate.getLevel() == 8);
        return null;
    }

    @Override
    protected void fillStateContainer(final StateContainer.Builder<Block, BlockState> builder)
    {
        builder.add(VictiniBlock.HALF, VictiniBlock.FACING, VictiniBlock.WATERLOGGED);
    }
}