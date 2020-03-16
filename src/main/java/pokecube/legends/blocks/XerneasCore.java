package pokecube.legends.blocks;

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

public class XerneasCore extends Rotates implements IWaterLoggable
{
    private static final EnumProperty<XerneasCorePart> PART              = EnumProperty.create("part",
            XerneasCorePart.class);
    private static final Map<Direction, VoxelShape>    XERNEAS_TOP       = new HashMap<>();
    private static final Map<Direction, VoxelShape>    XERNEAS_TOP_LEFT  = new HashMap<>();
    private static final Map<Direction, VoxelShape>    XERNEAS_TOP_RIGHT = new HashMap<>();
    private static final Map<Direction, VoxelShape>    XERNEAS_BOTTOM    = new HashMap<>();
    private static final BooleanProperty               WATERLOGGED       = BlockStateProperties.WATERLOGGED;
    private static final DirectionProperty             FACING            = HorizontalBlock.HORIZONTAL_FACING;

    // Precise selection box
    static
    {
      //@formatter:off
    XerneasCore.XERNEAS_TOP.put(Direction.NORTH,
      VoxelShapes.combineAndSimplify(Block.makeCuboidShape(4.5, 0, 4.5, 11.5, 7, 11.5),
        VoxelShapes.combineAndSimplify(Block.makeCuboidShape(8.78, 1, 5.5, 15.28, 11.5, 10.5),
          VoxelShapes.combineAndSimplify(Block.makeCuboidShape(0.7, 1, 5.5, 7.2, 11.5, 10.5),
            VoxelShapes.combineAndSimplify(Block.makeCuboidShape(11.5, 0, 5.25, 16, 6.7, 10.75),
              VoxelShapes.combineAndSimplify(Block.makeCuboidShape(0, 0, 5.25, 4.5, 6.7, 10.75),
                VoxelShapes.combineAndSimplify(Block.makeCuboidShape(11, 9, 6, 15, 16, 10),
                  VoxelShapes.combineAndSimplify(Block.makeCuboidShape(1, 9, 6, 5, 16, 10),
                    VoxelShapes.combineAndSimplify(Block.makeCuboidShape(8.19, 11.5, 7, 11, 14.5, 9),
                      Block.makeCuboidShape(4.94, 11.5, 7, 7.75, 14.5, 9),
                      IBooleanFunction.OR), IBooleanFunction.OR), IBooleanFunction.OR),
                IBooleanFunction.OR), IBooleanFunction.OR), IBooleanFunction.OR),
          IBooleanFunction.OR), IBooleanFunction.OR));
    XerneasCore.XERNEAS_TOP.put(Direction.EAST,
      VoxelShapes.combineAndSimplify(Block.makeCuboidShape(4.5, 0, 4.5, 11.5, 7, 11.5),
        VoxelShapes.combineAndSimplify(Block.makeCuboidShape(5.5, 1, 8.78, 10.5, 11.5, 15.28),
          VoxelShapes.combineAndSimplify(Block.makeCuboidShape(5.5, 1, 0.7, 10.5, 11.5, 7.2),
            VoxelShapes.combineAndSimplify(Block.makeCuboidShape(5.25, 0, 11.5, 10.75, 6.7, 16),
              VoxelShapes.combineAndSimplify(Block.makeCuboidShape(5.25, 0, 0, 10.75, 6.7, 4.5),
                VoxelShapes.combineAndSimplify(Block.makeCuboidShape(6, 9, 11, 10, 16, 15),
                  VoxelShapes.combineAndSimplify(Block.makeCuboidShape(6, 9, 1, 10, 16, 5),
                    VoxelShapes.combineAndSimplify(Block.makeCuboidShape(7, 11.5, 8.19, 9, 14.5, 11),
                      Block.makeCuboidShape(7, 11.5, 4.94, 9, 14.5, 7.75),
                      IBooleanFunction.OR), IBooleanFunction.OR), IBooleanFunction.OR),
                IBooleanFunction.OR), IBooleanFunction.OR), IBooleanFunction.OR),
          IBooleanFunction.OR), IBooleanFunction.OR));
    XerneasCore.XERNEAS_TOP.put(Direction.SOUTH,
      VoxelShapes.combineAndSimplify(Block.makeCuboidShape(4.5, 0, 4.5, 11.5, 7, 11.5),
        VoxelShapes.combineAndSimplify(Block.makeCuboidShape(0.72, 1, 5.5, 7.22, 11.5, 10.5),
          VoxelShapes.combineAndSimplify(Block.makeCuboidShape(8.8, 1, 5.5, 15.3, 11.5, 10.5),
            VoxelShapes.combineAndSimplify(Block.makeCuboidShape(0, 0, 5.25, 4.5, 6.7, 10.75),
              VoxelShapes.combineAndSimplify(Block.makeCuboidShape(11.5, 0, 5.25, 16, 6.7, 10.75),
                VoxelShapes.combineAndSimplify(Block.makeCuboidShape(1, 9, 6, 5, 16, 10),
                  VoxelShapes.combineAndSimplify(Block.makeCuboidShape(11, 9, 6, 15, 16, 10),
                    VoxelShapes.combineAndSimplify(Block.makeCuboidShape(5, 11.5, 7, 7.81, 14.5, 9),
                      Block.makeCuboidShape(8.25, 11.5, 7, 11.06, 14.5, 9),
                      IBooleanFunction.OR), IBooleanFunction.OR), IBooleanFunction.OR),
                IBooleanFunction.OR), IBooleanFunction.OR), IBooleanFunction.OR),
          IBooleanFunction.OR), IBooleanFunction.OR));
    XerneasCore.XERNEAS_TOP.put(Direction.WEST,
      VoxelShapes.combineAndSimplify(Block.makeCuboidShape(4.5, 0, 4.5, 11.5, 7, 11.5),
        VoxelShapes.combineAndSimplify(Block.makeCuboidShape(5.5, 1, 0.72, 10.5, 11.5, 7.22),
          VoxelShapes.combineAndSimplify(Block.makeCuboidShape(5.5, 1, 8.8, 10.5, 11.5, 15.3),
            VoxelShapes.combineAndSimplify(Block.makeCuboidShape(5.25, 0, 0, 10.75, 6.7, 4.5),
              VoxelShapes.combineAndSimplify(Block.makeCuboidShape(5.25, 0, 11.5, 10.75, 6.7, 16),
                VoxelShapes.combineAndSimplify(Block.makeCuboidShape(6, 9, 1, 10, 16, 5),
                  VoxelShapes.combineAndSimplify(Block.makeCuboidShape(6, 9, 11, 10, 16, 15),
                    VoxelShapes.combineAndSimplify(Block.makeCuboidShape(7, 11.5, 5, 9, 14.5, 7.81),
                      Block.makeCuboidShape(7, 11.5, 8.25, 9, 14.5, 11.06),
                      IBooleanFunction.OR), IBooleanFunction.OR), IBooleanFunction.OR),
                IBooleanFunction.OR), IBooleanFunction.OR), IBooleanFunction.OR),
          IBooleanFunction.OR), IBooleanFunction.OR));

    XerneasCore.XERNEAS_TOP_LEFT.put(Direction.NORTH,
      VoxelShapes.combineAndSimplify(Block.makeCuboidShape(0, 0.5, 5.5, 12, 16, 10.5),
        VoxelShapes.combineAndSimplify(Block.makeCuboidShape(12, 5.25, 6.5, 19, 12.75, 9.5),
          Block.makeCuboidShape(0, 0, 5.25, 5.5, 6.64, 10.75),
          IBooleanFunction.OR), IBooleanFunction.OR));
    XerneasCore.XERNEAS_TOP_LEFT.put(Direction.EAST,
      VoxelShapes.combineAndSimplify(Block.makeCuboidShape(5.5, 0.5, 0, 10.5, 16, 12),
        VoxelShapes.combineAndSimplify(Block.makeCuboidShape(6.5, 5.25, 12, 9.5, 12.75, 19),
          Block.makeCuboidShape(5.25, 0, 0, 10.75, 6.64, 5.5),
          IBooleanFunction.OR), IBooleanFunction.OR));
    XerneasCore.XERNEAS_TOP_LEFT.put(Direction.SOUTH,
      VoxelShapes.combineAndSimplify(Block.makeCuboidShape(4, 0.5, 5.5, 16, 16, 10.5),
        VoxelShapes.combineAndSimplify(Block.makeCuboidShape(-3, 5.25, 6.5, 4, 12.75, 9.5),
          Block.makeCuboidShape(10.5, 0, 5.25, 16, 6.64, 10.75),
          IBooleanFunction.OR), IBooleanFunction.OR));
    XerneasCore.XERNEAS_TOP_LEFT.put(Direction.WEST,
      VoxelShapes.combineAndSimplify(Block.makeCuboidShape(5.5, 0.5, 4, 10.5, 16, 16),
        VoxelShapes.combineAndSimplify(Block.makeCuboidShape(6.5, 5.25, -3, 9.5, 12.75, 4),
          Block.makeCuboidShape(5.25, 0, 10.5, 10.75, 6.64, 16),
          IBooleanFunction.OR), IBooleanFunction.OR));

    XerneasCore.XERNEAS_TOP_RIGHT.put(Direction.NORTH,
      VoxelShapes.combineAndSimplify(Block.makeCuboidShape(4, 0.5, 5.5, 16, 16, 10.5),
        VoxelShapes.combineAndSimplify(Block.makeCuboidShape(-3, 5.25, 6.5, 4, 13.35, 9.5),
          Block.makeCuboidShape(10.5, 0, 5.25, 16, 6.64, 10.75),
          IBooleanFunction.OR), IBooleanFunction.OR));
    XerneasCore.XERNEAS_TOP_RIGHT.put(Direction.EAST,
      VoxelShapes.combineAndSimplify(Block.makeCuboidShape(5.5, 0.5, 4, 10.5, 16, 16),
        VoxelShapes.combineAndSimplify(Block.makeCuboidShape(6.5, 5.25, -3, 9.5, 13.35, 4),
          Block.makeCuboidShape(5.25, 0, 10.5, 10.75, 6.64, 16),
          IBooleanFunction.OR), IBooleanFunction.OR));
    XerneasCore.XERNEAS_TOP_RIGHT.put(Direction.SOUTH,
      VoxelShapes.combineAndSimplify(Block.makeCuboidShape(0, 0.5, 5.5, 12, 16, 10.5),
        VoxelShapes.combineAndSimplify(Block.makeCuboidShape(12, 5.25, 6.5, 19, 13.35, 9.5),
          Block.makeCuboidShape(0, 0, 5.25, 5.5, 6.64, 10.75),
          IBooleanFunction.OR), IBooleanFunction.OR));
    XerneasCore.XERNEAS_TOP_RIGHT.put(Direction.WEST,
      VoxelShapes.combineAndSimplify(Block.makeCuboidShape(5.5, 0.5, 0, 10.5, 16, 12),
        VoxelShapes.combineAndSimplify(Block.makeCuboidShape(6.5, 5.25, 12, 9.5, 13.35, 19),
          Block.makeCuboidShape(5.25, 0, 0, 10.75, 6.64, 5.5),
          IBooleanFunction.OR), IBooleanFunction.OR));

    XerneasCore.XERNEAS_BOTTOM.put(Direction.NORTH,
      VoxelShapes.combineAndSimplify(Block.makeCuboidShape(4, 0, 4, 12, 5, 12),
        VoxelShapes.combineAndSimplify(Block.makeCuboidShape(3, 0.25, 3, 7, 4.25, 7),
          VoxelShapes.combineAndSimplify(Block.makeCuboidShape(3, 0.25, 9, 7, 4.25, 13),
            VoxelShapes.combineAndSimplify(Block.makeCuboidShape(9, 0.25, 9, 13, 4.25, 13),
              VoxelShapes.combineAndSimplify(Block.makeCuboidShape(9, 0.25, 3, 13, 4.25, 7),
                VoxelShapes.combineAndSimplify(Block.makeCuboidShape(4, 0, 13, 6, 3, 15),
                  VoxelShapes.combineAndSimplify(Block.makeCuboidShape(4, 0, 1, 6, 3, 3),
                    VoxelShapes.combineAndSimplify(Block.makeCuboidShape(10, 0, 1, 12, 3, 3),
                      VoxelShapes.combineAndSimplify(Block.makeCuboidShape(1, 0, 10, 3, 3, 12),
                        VoxelShapes.combineAndSimplify(Block.makeCuboidShape(13, 0, 10, 15, 3, 12),
                          VoxelShapes.combineAndSimplify(Block.makeCuboidShape(1, 0, 4, 3, 3, 6),
                            VoxelShapes.combineAndSimplify(Block.makeCuboidShape(13, 0, 4, 15, 3, 6),
                              VoxelShapes.combineAndSimplify(Block.makeCuboidShape(10, 0, 13, 12, 3, 15),
                                VoxelShapes.combineAndSimplify(Block.makeCuboidShape(4, 0, 15, 6, 2, 16),
                                  VoxelShapes.combineAndSimplify(Block.makeCuboidShape(4, 0, 0, 6, 2, 1),
                                    VoxelShapes.combineAndSimplify(Block.makeCuboidShape(10, 0, 0, 12, 2, 1),
                                      VoxelShapes.combineAndSimplify(Block.makeCuboidShape(0, 0, 10, 1, 2, 12),
                                        VoxelShapes.combineAndSimplify(Block.makeCuboidShape(15, 0, 10, 16, 2, 12),
                                          VoxelShapes.combineAndSimplify(Block.makeCuboidShape(0, 0, 4, 1, 2, 6),
                                            VoxelShapes.combineAndSimplify(Block.makeCuboidShape(15, 0, 4, 16, 2, 6),
                                              VoxelShapes.combineAndSimplify(Block.makeCuboidShape(10, 0, 15, 12, 2, 16),
                                                VoxelShapes.combineAndSimplify(Block.makeCuboidShape(4.5, 5, 4.5, 11.5, 16, 11.5),
                                                  VoxelShapes.combineAndSimplify(Block.makeCuboidShape(0, 10.4, 5.25, 4.5, 16, 10.75),
                                                    Block.makeCuboidShape(11.5, 10.4, 5.25, 16, 16, 10.75),
                                                    IBooleanFunction.OR), IBooleanFunction.OR), IBooleanFunction.OR),
                                              IBooleanFunction.OR), IBooleanFunction.OR), IBooleanFunction.OR),
                                        IBooleanFunction.OR), IBooleanFunction.OR), IBooleanFunction.OR),
                                  IBooleanFunction.OR), IBooleanFunction.OR), IBooleanFunction.OR),
                            IBooleanFunction.OR), IBooleanFunction.OR), IBooleanFunction.OR),
                      IBooleanFunction.OR), IBooleanFunction.OR), IBooleanFunction.OR),
                IBooleanFunction.OR), IBooleanFunction.OR), IBooleanFunction.OR),
          IBooleanFunction.OR), IBooleanFunction.OR));
    XerneasCore.XERNEAS_BOTTOM.put(Direction.EAST,
      VoxelShapes.combineAndSimplify(Block.makeCuboidShape(4, 0, 4, 12, 5, 12),
        VoxelShapes.combineAndSimplify(Block.makeCuboidShape(3, 0.25, 3, 7, 4.25, 7),
          VoxelShapes.combineAndSimplify(Block.makeCuboidShape(3, 0.25, 9, 7, 4.25, 13),
            VoxelShapes.combineAndSimplify(Block.makeCuboidShape(9, 0.25, 9, 13, 4.25, 13),
              VoxelShapes.combineAndSimplify(Block.makeCuboidShape(9, 0.25, 3, 13, 4.25, 7),
                VoxelShapes.combineAndSimplify(Block.makeCuboidShape(4, 0, 13, 6, 3, 15),
                  VoxelShapes.combineAndSimplify(Block.makeCuboidShape(4, 0, 1, 6, 3, 3),
                    VoxelShapes.combineAndSimplify(Block.makeCuboidShape(10, 0, 1, 12, 3, 3),
                      VoxelShapes.combineAndSimplify(Block.makeCuboidShape(1, 0, 10, 3, 3, 12),
                        VoxelShapes.combineAndSimplify(Block.makeCuboidShape(13, 0, 10, 15, 3, 12),
                          VoxelShapes.combineAndSimplify(Block.makeCuboidShape(1, 0, 4, 3, 3, 6),
                            VoxelShapes.combineAndSimplify(Block.makeCuboidShape(13, 0, 4, 15, 3, 6),
                              VoxelShapes.combineAndSimplify(Block.makeCuboidShape(10, 0, 13, 12, 3, 15),
                                VoxelShapes.combineAndSimplify(Block.makeCuboidShape(4, 0, 15, 6, 2, 16),
                                  VoxelShapes.combineAndSimplify(Block.makeCuboidShape(4, 0, 0, 6, 2, 1),
                                    VoxelShapes.combineAndSimplify(Block.makeCuboidShape(10, 0, 0, 12, 2, 1),
                                      VoxelShapes.combineAndSimplify(Block.makeCuboidShape(0, 0, 10, 1, 2, 12),
                                        VoxelShapes.combineAndSimplify(Block.makeCuboidShape(15, 0, 10, 16, 2, 12),
                                          VoxelShapes.combineAndSimplify(Block.makeCuboidShape(0, 0, 4, 1, 2, 6),
                                            VoxelShapes.combineAndSimplify(Block.makeCuboidShape(15, 0, 4, 16, 2, 6),
                                              VoxelShapes.combineAndSimplify(Block.makeCuboidShape(10, 0, 15, 12, 2, 16),
                                                VoxelShapes.combineAndSimplify(Block.makeCuboidShape(4.5, 5, 4.5, 11.5, 16, 11.5),
                                                  VoxelShapes.combineAndSimplify(Block.makeCuboidShape(5.25, 10.4, 11.5, 10.75, 16, 16),
                                                    Block.makeCuboidShape(5.25, 10.4, 0, 10.75, 16, 4.5),
                                                    IBooleanFunction.OR), IBooleanFunction.OR), IBooleanFunction.OR),
                                              IBooleanFunction.OR), IBooleanFunction.OR), IBooleanFunction.OR),
                                        IBooleanFunction.OR), IBooleanFunction.OR), IBooleanFunction.OR),
                                  IBooleanFunction.OR), IBooleanFunction.OR), IBooleanFunction.OR),
                            IBooleanFunction.OR), IBooleanFunction.OR), IBooleanFunction.OR),
                      IBooleanFunction.OR), IBooleanFunction.OR), IBooleanFunction.OR),
                IBooleanFunction.OR), IBooleanFunction.OR), IBooleanFunction.OR),
          IBooleanFunction.OR), IBooleanFunction.OR));
    XerneasCore.XERNEAS_BOTTOM.put(Direction.SOUTH,
      VoxelShapes.combineAndSimplify(Block.makeCuboidShape(4, 0, 4, 12, 5, 12),
        VoxelShapes.combineAndSimplify(Block.makeCuboidShape(3, 0.25, 3, 7, 4.25, 7),
          VoxelShapes.combineAndSimplify(Block.makeCuboidShape(3, 0.25, 9, 7, 4.25, 13),
            VoxelShapes.combineAndSimplify(Block.makeCuboidShape(9, 0.25, 9, 13, 4.25, 13),
              VoxelShapes.combineAndSimplify(Block.makeCuboidShape(9, 0.25, 3, 13, 4.25, 7),
                VoxelShapes.combineAndSimplify(Block.makeCuboidShape(4, 0, 13, 6, 3, 15),
                  VoxelShapes.combineAndSimplify(Block.makeCuboidShape(4, 0, 1, 6, 3, 3),
                    VoxelShapes.combineAndSimplify(Block.makeCuboidShape(10, 0, 1, 12, 3, 3),
                      VoxelShapes.combineAndSimplify(Block.makeCuboidShape(1, 0, 10, 3, 3, 12),
                        VoxelShapes.combineAndSimplify(Block.makeCuboidShape(13, 0, 10, 15, 3, 12),
                          VoxelShapes.combineAndSimplify(Block.makeCuboidShape(1, 0, 4, 3, 3, 6),
                            VoxelShapes.combineAndSimplify(Block.makeCuboidShape(13, 0, 4, 15, 3, 6),
                              VoxelShapes.combineAndSimplify(Block.makeCuboidShape(10, 0, 13, 12, 3, 15),
                                VoxelShapes.combineAndSimplify(Block.makeCuboidShape(4, 0, 15, 6, 2, 16),
                                  VoxelShapes.combineAndSimplify(Block.makeCuboidShape(4, 0, 0, 6, 2, 1),
                                    VoxelShapes.combineAndSimplify(Block.makeCuboidShape(10, 0, 0, 12, 2, 1),
                                      VoxelShapes.combineAndSimplify(Block.makeCuboidShape(0, 0, 10, 1, 2, 12),
                                        VoxelShapes.combineAndSimplify(Block.makeCuboidShape(15, 0, 10, 16, 2, 12),
                                          VoxelShapes.combineAndSimplify(Block.makeCuboidShape(0, 0, 4, 1, 2, 6),
                                            VoxelShapes.combineAndSimplify(Block.makeCuboidShape(15, 0, 4, 16, 2, 6),
                                              VoxelShapes.combineAndSimplify(Block.makeCuboidShape(10, 0, 15, 12, 2, 16),
                                                VoxelShapes.combineAndSimplify(Block.makeCuboidShape(4.5, 5, 4.5, 11.5, 16, 11.5),
                                                  VoxelShapes.combineAndSimplify(Block.makeCuboidShape(0, 10.4, 5.25, 4.5, 16, 10.75),
                                                    Block.makeCuboidShape(11.5, 10.4, 5.25, 16, 16, 10.75),
                                                    IBooleanFunction.OR), IBooleanFunction.OR), IBooleanFunction.OR),
                                              IBooleanFunction.OR), IBooleanFunction.OR), IBooleanFunction.OR),
                                        IBooleanFunction.OR), IBooleanFunction.OR), IBooleanFunction.OR),
                                  IBooleanFunction.OR), IBooleanFunction.OR), IBooleanFunction.OR),
                            IBooleanFunction.OR), IBooleanFunction.OR), IBooleanFunction.OR),
                      IBooleanFunction.OR), IBooleanFunction.OR), IBooleanFunction.OR),
                IBooleanFunction.OR), IBooleanFunction.OR), IBooleanFunction.OR),
          IBooleanFunction.OR), IBooleanFunction.OR));
    XerneasCore.XERNEAS_BOTTOM.put(Direction.WEST,
      VoxelShapes.combineAndSimplify(Block.makeCuboidShape(4, 0, 4, 12, 5, 12),
        VoxelShapes.combineAndSimplify(Block.makeCuboidShape(3, 0.25, 3, 7, 4.25, 7),
          VoxelShapes.combineAndSimplify(Block.makeCuboidShape(3, 0.25, 9, 7, 4.25, 13),
            VoxelShapes.combineAndSimplify(Block.makeCuboidShape(9, 0.25, 9, 13, 4.25, 13),
              VoxelShapes.combineAndSimplify(Block.makeCuboidShape(9, 0.25, 3, 13, 4.25, 7),
                VoxelShapes.combineAndSimplify(Block.makeCuboidShape(4, 0, 13, 6, 3, 15),
                  VoxelShapes.combineAndSimplify(Block.makeCuboidShape(4, 0, 1, 6, 3, 3),
                    VoxelShapes.combineAndSimplify(Block.makeCuboidShape(10, 0, 1, 12, 3, 3),
                      VoxelShapes.combineAndSimplify(Block.makeCuboidShape(1, 0, 10, 3, 3, 12),
                        VoxelShapes.combineAndSimplify(Block.makeCuboidShape(13, 0, 10, 15, 3, 12),
                          VoxelShapes.combineAndSimplify(Block.makeCuboidShape(1, 0, 4, 3, 3, 6),
                            VoxelShapes.combineAndSimplify(Block.makeCuboidShape(13, 0, 4, 15, 3, 6),
                              VoxelShapes.combineAndSimplify(Block.makeCuboidShape(10, 0, 13, 12, 3, 15),
                                VoxelShapes.combineAndSimplify(Block.makeCuboidShape(4, 0, 15, 6, 2, 16),
                                  VoxelShapes.combineAndSimplify(Block.makeCuboidShape(4, 0, 0, 6, 2, 1),
                                    VoxelShapes.combineAndSimplify(Block.makeCuboidShape(10, 0, 0, 12, 2, 1),
                                      VoxelShapes.combineAndSimplify(Block.makeCuboidShape(0, 0, 10, 1, 2, 12),
                                        VoxelShapes.combineAndSimplify(Block.makeCuboidShape(15, 0, 10, 16, 2, 12),
                                          VoxelShapes.combineAndSimplify(Block.makeCuboidShape(0, 0, 4, 1, 2, 6),
                                            VoxelShapes.combineAndSimplify(Block.makeCuboidShape(15, 0, 4, 16, 2, 6),
                                              VoxelShapes.combineAndSimplify(Block.makeCuboidShape(10, 0, 15, 12, 2, 16),
                                                VoxelShapes.combineAndSimplify(Block.makeCuboidShape(4.5, 5, 4.5, 11.5, 16, 11.5),
                                                  VoxelShapes.combineAndSimplify(Block.makeCuboidShape(5.25, 10.4, 11.5, 10.75, 16, 16),
                                                  Block.makeCuboidShape(5.25, 10.4, 0, 10.75, 16, 4.5),
                                                  IBooleanFunction.OR), IBooleanFunction.OR), IBooleanFunction.OR),
                                            IBooleanFunction.OR), IBooleanFunction.OR), IBooleanFunction.OR),
                                      IBooleanFunction.OR), IBooleanFunction.OR), IBooleanFunction.OR),
                                IBooleanFunction.OR), IBooleanFunction.OR), IBooleanFunction.OR),
                          IBooleanFunction.OR), IBooleanFunction.OR), IBooleanFunction.OR),
                    IBooleanFunction.OR), IBooleanFunction.OR), IBooleanFunction.OR),
              IBooleanFunction.OR), IBooleanFunction.OR), IBooleanFunction.OR),
          IBooleanFunction.OR), IBooleanFunction.OR));
    //@formatter:on
    }

    // Precise selection box
    @Override
    public VoxelShape getShape(final BlockState state, final IBlockReader worldIn, final BlockPos pos,
            final ISelectionContext context)
    {
        final XerneasCorePart part = state.get(XerneasCore.PART);
        if (part == XerneasCorePart.BOTTOM) return XerneasCore.XERNEAS_BOTTOM.get(state.get(XerneasCore.FACING));
        else if (part == XerneasCorePart.TOP_LEFT) return XerneasCore.XERNEAS_TOP_LEFT.get(state.get(
                XerneasCore.FACING));
        else if (part == XerneasCorePart.TOP_RIGHT) return XerneasCore.XERNEAS_TOP_RIGHT.get(state.get(
                XerneasCore.FACING));
        else return XerneasCore.XERNEAS_TOP.get(state.get(XerneasCore.FACING));
    }

    public XerneasCore(final String name, final Properties props)
    {
        super(name, props);
        this.setDefaultState(this.stateContainer.getBaseState().with(XerneasCore.FACING, Direction.NORTH).with(
                XerneasCore.WATERLOGGED, false).with(XerneasCore.PART, XerneasCorePart.BOTTOM));
    }

    // Places Xerneas Core Spawner with both top and bottom pieces
    @Override
    public void onBlockPlacedBy(final World world, final BlockPos pos, final BlockState state,
            @Nullable final LivingEntity entity, final ItemStack stack)
    {
        if (entity != null)
        {
            final BlockPos xerneasCoreTopLeftPos = this.getXerneasCoreTopLeftPos(pos, entity.getHorizontalFacing());
            final BlockPos xerneasCoreTopRightPos = this.getXerneasCoreTopRightPos(pos, entity.getHorizontalFacing());

            final IFluidState fluidState = world.getFluidState(pos.up());
            final IFluidState westFluidState = world.getFluidState(pos.up().west());
            final IFluidState eastFluidState = world.getFluidState(pos.up().east());

            world.setBlockState(pos.up(), state.with(XerneasCore.PART, XerneasCorePart.TOP).with(
                    XerneasCore.WATERLOGGED, fluidState.getFluid() == Fluids.WATER), 3);
            world.setBlockState(xerneasCoreTopLeftPos, state.with(XerneasCore.PART, XerneasCorePart.TOP_LEFT).with(
                    XerneasCore.WATERLOGGED, westFluidState.getFluid() == Fluids.WATER), 3);
            world.setBlockState(xerneasCoreTopRightPos, state.with(XerneasCore.PART, XerneasCorePart.TOP_RIGHT).with(
                    XerneasCore.WATERLOGGED, eastFluidState.getFluid() == Fluids.WATER), 3);
        }
    }

    // Breaking Xerneas Core Spawner breaks both parts and returns one item only
    @Override
    public void onBlockHarvested(final World world, final BlockPos pos, final BlockState state,
            final PlayerEntity player)
    {
        final Direction facing = state.get(XerneasCore.FACING);

        final BlockPos xerneasCorePos = this.getXerneasCorePos(pos, state.get(XerneasCore.PART), facing);
        BlockState XerneasCoreBlockState = world.getBlockState(xerneasCorePos);
        if (XerneasCoreBlockState.getBlock() == this && !pos.equals(xerneasCorePos)) this.removePart(world,
                xerneasCorePos, XerneasCoreBlockState);

        BlockPos xerneasCorePartPos = this.getXerneasCoreTopPos(xerneasCorePos, facing);
        XerneasCoreBlockState = world.getBlockState(xerneasCorePartPos);
        if (XerneasCoreBlockState.getBlock() == this && !pos.equals(xerneasCorePartPos)) this.removePart(world,
                xerneasCorePartPos, XerneasCoreBlockState);

        xerneasCorePartPos = this.getXerneasCoreTopLeftPos(xerneasCorePos, facing);
        XerneasCoreBlockState = world.getBlockState(xerneasCorePartPos);
        if (XerneasCoreBlockState.getBlock() == this && !pos.equals(xerneasCorePartPos)) this.removePart(world,
                xerneasCorePartPos, XerneasCoreBlockState);

        xerneasCorePartPos = this.getXerneasCoreTopRightPos(xerneasCorePos, facing);
        XerneasCoreBlockState = world.getBlockState(xerneasCorePartPos);
        if (XerneasCoreBlockState.getBlock() == this && !pos.equals(xerneasCorePartPos)) this.removePart(world,
                xerneasCorePartPos, XerneasCoreBlockState);
        super.onBlockHarvested(world, pos, state, player);
    }

    private BlockPos getXerneasCoreTopPos(final BlockPos base, final Direction facing)
    {
        switch (facing)
        {
        case NORTH:
            return base.up();
        case EAST:
            return base.up();
        case SOUTH:
            return base.up();
        case WEST:
            return base.up();
        default:
            return base.up();
        }
    }

    private BlockPos getXerneasCoreTopLeftPos(final BlockPos base, final Direction facing)
    {
        switch (facing)
        {
        case NORTH:
            return base.up().west();
        case EAST:
            return base.up().north();
        case SOUTH:
            return base.up().east();
        case WEST:
            return base.up().south();
        default:
            return base.up().east();
        }
    }

    private BlockPos getXerneasCoreTopRightPos(final BlockPos base, final Direction facing)
    {
        switch (facing)
        {
        case NORTH:
            return base.up().east();
        case EAST:
            return base.up().south();
        case SOUTH:
            return base.up().west();
        case WEST:
            return base.up().north();
        default:
            return base.up().west();
        }
    }

    private BlockPos getXerneasCorePos(final BlockPos pos, final XerneasCorePart part, final Direction facing)
    {
        if (part == XerneasCorePart.BOTTOM) return pos;
        switch (facing)
        {
        case NORTH:
            switch (part)
            {
            case TOP:
                return pos.down();
            case TOP_LEFT:
                return pos.down().west();
            case TOP_RIGHT:
                return pos.down().east();
            default:
                return null;
            }
        case EAST:
            switch (part)
            {
            case TOP:
                return pos.down();
            case TOP_LEFT:
                return pos.down().north();
            case TOP_RIGHT:
                return pos.down().south();
            default:
                return null;
            }
        case SOUTH:
            switch (part)
            {
            case TOP:
                return pos.down();
            case TOP_LEFT:
                return pos.down().east();
            case TOP_RIGHT:
                return pos.down().west();
            default:
                return null;
            }
        case WEST:
            switch (part)
            {
            case TOP:
                return pos.down();
            case TOP_LEFT:
                return pos.down().south();
            case TOP_RIGHT:
                return pos.down().north();
            default:
                return null;
            }
        default:
            return null;
        }
    }

    // Breaking the Xerneas Core Spawner leaves water if underwater
    private void removePart(final World world, final BlockPos pos, final BlockState state)
    {
        final IFluidState fluidState = world.getFluidState(pos);
        if (fluidState.getFluid() == Fluids.WATER) world.setBlockState(pos, fluidState.getBlockState(), 35);
        else world.setBlockState(pos, Blocks.AIR.getDefaultState(), 35);
    }

    // Prevents the Xerneas Core Spawner from replacing blocks above it and
    // checks for water
    @Override
    public BlockState getStateForPlacement(final BlockItemUseContext context)
    {
        final IFluidState ifluidstate = context.getWorld().getFluidState(context.getPos());
        final BlockPos pos = context.getPos();

        final BlockPos xerneasCorePos = this.getXerneasCoreTopPos(pos, context.getPlacementHorizontalFacing()
                .getOpposite());
        final BlockPos xerneasCoreLeftPos = this.getXerneasCoreTopLeftPos(pos, context.getPlacementHorizontalFacing()
                .getOpposite());
        final BlockPos xerneasCoreRightPos = this.getXerneasCoreTopRightPos(pos, context.getPlacementHorizontalFacing()
                .getOpposite());
        if (pos.getY() < 255 && xerneasCorePos.getY() < 255 && context.getWorld().getBlockState(pos.up()).isReplaceable(
                context) && xerneasCoreLeftPos.getY() < 255 && context.getWorld().getBlockState(xerneasCoreLeftPos)
                        .isReplaceable(context) && xerneasCoreRightPos.getY() < 255 && context.getWorld().getBlockState(
                                xerneasCoreRightPos).isReplaceable(context)) return this.getDefaultState().with(
                                        XerneasCore.FACING, context.getPlacementHorizontalFacing().getOpposite()).with(
                                                XerneasCore.PART, XerneasCorePart.BOTTOM).with(XerneasCore.WATERLOGGED,
                                                        ifluidstate.isTagged(FluidTags.WATER) && ifluidstate
                                                                .getLevel() == 8);
        return null;
    }

    @Override
    protected void fillStateContainer(final StateContainer.Builder<Block, BlockState> builder)
    {
        builder.add(XerneasCore.PART, XerneasCore.FACING, XerneasCore.WATERLOGGED);
    }
}