package pokecube.legends.blocks;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import javax.annotation.Nullable;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.HorizontalBlock;
import net.minecraft.block.IWaterLoggable;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
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
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.shapes.IBooleanFunction;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import pokecube.legends.PokecubeLegends;
import pokecube.legends.worldgen.dimension.ModDimensions;
import pokecube.legends.worldgen.dimension.UltraSpaceModDimension;
import thut.api.terrain.TerrainManager;

public class UltraSpacePortal extends Rotates implements IWaterLoggable
{
    private static final EnumProperty<UltraSpacePortalPart> PART        = EnumProperty.create("part",
            UltraSpacePortalPart.class);
    private static final BooleanProperty                    WATERLOGGED = BlockStateProperties.WATERLOGGED;
    private static final DirectionProperty                  FACING      = HorizontalBlock.HORIZONTAL_FACING;

    private static final Map<Direction, VoxelShape> WORMHOLE_TOP          = new HashMap<>();
    private static final Map<Direction, VoxelShape> WORMHOLE_TOP_LEFT     = new HashMap<>();
    private static final Map<Direction, VoxelShape> WORMHOLE_TOP_RIGHT    = new HashMap<>();
    private static final Map<Direction, VoxelShape> WORMHOLE_MIDDLE       = new HashMap<>();
    private static final Map<Direction, VoxelShape> WORMHOLE_MIDDLE_LEFT  = new HashMap<>();
    private static final Map<Direction, VoxelShape> WORMHOLE_MIDDLE_RIGHT = new HashMap<>();
    private static final Map<Direction, VoxelShape> WORMHOLE_BOTTOM       = new HashMap<>();
    private static final Map<Direction, VoxelShape> WORMHOLE_BOTTOM_LEFT  = new HashMap<>();
    private static final Map<Direction, VoxelShape> WORMHOLE_BOTTOM_RIGHT = new HashMap<>();

    String  infoname;
    boolean hasTextInfo = true;

    // Precise selection box
    static
    {
        //@formatter:off
        UltraSpacePortal.WORMHOLE_TOP.put(Direction.NORTH,
          VoxelShapes.combineAndSimplify(Block.makeCuboidShape(4.875, 13, 7.25, 11.125, 14.5, 8.75),
            VoxelShapes.combineAndSimplify(Block.makeCuboidShape(5.65625, 7.75, 9.1875, 10.34375, 8.875, 10.3125),
              VoxelShapes.combineAndSimplify(Block.makeCuboidShape(6.24219, 3.8125, 10.76562, 9.75781, 4.65625, 11.60937),
                VoxelShapes.combineAndSimplify(Block.makeCuboidShape(0, 0, 10.76562, 16, 3.8125, 11.60937),
                  VoxelShapes.combineAndSimplify(Block.makeCuboidShape(0, 0, 7.25, 16, 13, 8.75),
                    Block.makeCuboidShape(0, 0, 9.1875, 16, 7.75, 10.3125),
                    IBooleanFunction.OR), IBooleanFunction.OR), IBooleanFunction.OR),
              IBooleanFunction.OR), IBooleanFunction.OR));
        UltraSpacePortal.WORMHOLE_TOP.put(Direction.EAST,
          VoxelShapes.combineAndSimplify(Block.makeCuboidShape(7.25, 13, 4.875, 8.75, 14.5, 11.125),
            VoxelShapes.combineAndSimplify(Block.makeCuboidShape(5.6875, 7.75, 5.65625, 6.8125, 8.875, 10.34375),
              VoxelShapes.combineAndSimplify(Block.makeCuboidShape(4.39063, 3.8125, 6.24219, 5.23438, 4.65625, 9.75781),
                VoxelShapes.combineAndSimplify(Block.makeCuboidShape(4.39063, 0, 0, 5.23438, 3.8125, 16),
                  VoxelShapes.combineAndSimplify(Block.makeCuboidShape(7.25, 0, 0, 8.75, 13, 16),
                    Block.makeCuboidShape(5.6875, 0, 0, 6.8125, 7.75, 16),
                    IBooleanFunction.OR), IBooleanFunction.OR), IBooleanFunction.OR),
              IBooleanFunction.OR), IBooleanFunction.OR));
        UltraSpacePortal.WORMHOLE_TOP.put(Direction.SOUTH,
          VoxelShapes.combineAndSimplify(Block.makeCuboidShape(4.875, 13, 7.25, 11.125, 14.5, 8.75),
            VoxelShapes.combineAndSimplify(Block.makeCuboidShape(5.65625, 7.75, 5.6875, 10.34375, 8.875, 6.8125),
              VoxelShapes.combineAndSimplify(Block.makeCuboidShape(6.24219, 3.8125, 4.39063, 9.75781, 4.65625, 5.23438),
                VoxelShapes.combineAndSimplify(Block.makeCuboidShape(0, 0, 4.39063, 16, 3.8125, 5.23438),
                  VoxelShapes.combineAndSimplify(Block.makeCuboidShape(0, 0, 7.25, 16, 13, 8.75),
                    Block.makeCuboidShape(0, 0, 5.6875, 16, 7.75, 6.8125),
                    IBooleanFunction.OR), IBooleanFunction.OR), IBooleanFunction.OR),
              IBooleanFunction.OR), IBooleanFunction.OR));
        UltraSpacePortal.WORMHOLE_TOP.put(Direction.WEST,
          VoxelShapes.combineAndSimplify(Block.makeCuboidShape(7.25, 13, 4.875, 8.75, 14.5, 11.125),
            VoxelShapes.combineAndSimplify(Block.makeCuboidShape(9.1875, 7.75, 5.65625, 10.3125, 8.875, 10.34375),
              VoxelShapes.combineAndSimplify(Block.makeCuboidShape(10.76562, 3.8125, 6.24219, 11.60937, 4.65625, 9.75781),
                VoxelShapes.combineAndSimplify(Block.makeCuboidShape(10.76562, 0, 0, 11.60937, 3.8125, 16),
                  VoxelShapes.combineAndSimplify(Block.makeCuboidShape(7.25, 0, 0, 8.75, 13, 16),
                    Block.makeCuboidShape(9.1875, 0, 0, 10.3125, 7.75, 16),
                    IBooleanFunction.OR), IBooleanFunction.OR), IBooleanFunction.OR),
              IBooleanFunction.OR), IBooleanFunction.OR));

        UltraSpacePortal.WORMHOLE_TOP_LEFT.put(Direction.NORTH,
          VoxelShapes.combineAndSimplify(Block.makeCuboidShape(0, 11.5, 7.25, 2.75, 13, 8.75),
            VoxelShapes.combineAndSimplify(Block.makeCuboidShape(0, 6.625, 9.1875, 0.0625, 7.75, 10.3125),
              VoxelShapes.combineAndSimplify(Block.makeCuboidShape(0, 10, 7.25, 5.5, 11.5, 8.75),
                VoxelShapes.combineAndSimplify(Block.makeCuboidShape(0, 5.5, 9.1875, 2.125, 6.625, 10.3125),
                  VoxelShapes.combineAndSimplify(Block.makeCuboidShape(0, 8.5, 7.25, 7, 10, 8.75),
                    VoxelShapes.combineAndSimplify(Block.makeCuboidShape(0, 4.375, 9.1875, 3.25, 5.5, 10.3125),
                      VoxelShapes.combineAndSimplify(Block.makeCuboidShape(0, 1.28125, 10.76562, 0.4375, 2.125, 11.60937),
                        VoxelShapes.combineAndSimplify(Block.makeCuboidShape(0, 7, 7.25, 8.5, 8.5, 8.75),
                          VoxelShapes.combineAndSimplify(Block.makeCuboidShape(0, 3.25, 9.1875, 4.375, 4.375, 10.3125),
                            VoxelShapes.combineAndSimplify(Block.makeCuboidShape(0, 0.4375, 10.76562, 1.28125, 1.28125, 11.60937),
                              VoxelShapes.combineAndSimplify(Block.makeCuboidShape(0, 5.5, 7.25, 10, 7, 8.75),
                                VoxelShapes.combineAndSimplify(Block.makeCuboidShape(0, 2.125, 9.1875, 5.5, 3.25, 10.3125),
                                  VoxelShapes.combineAndSimplify(Block.makeCuboidShape(0, 0, 10.76562, 2.125, 0.4375, 11.60937),
                                    VoxelShapes.combineAndSimplify(Block.makeCuboidShape(0, 4, 7.25, 11.5, 5.5, 8.75),
                                      VoxelShapes.combineAndSimplify(Block.makeCuboidShape(0, 1, 9.1875, 6.625, 2.125, 10.3125),
                                        VoxelShapes.combineAndSimplify(Block.makeCuboidShape(0, 2.65, 7.25, 13, 4, 8.75),
                                          VoxelShapes.combineAndSimplify(Block.makeCuboidShape(0, 0, 9.1875, 7.75, 1, 10.3125),
                                            Block.makeCuboidShape(0, 0, 7.25, 14.5, 2.65, 8.75),
                                            IBooleanFunction.OR), IBooleanFunction.OR), IBooleanFunction.OR),
                                          IBooleanFunction.OR), IBooleanFunction.OR), IBooleanFunction.OR),
                                    IBooleanFunction.OR), IBooleanFunction.OR), IBooleanFunction.OR),
                              IBooleanFunction.OR), IBooleanFunction.OR), IBooleanFunction.OR),
                        IBooleanFunction.OR), IBooleanFunction.OR), IBooleanFunction.OR),
                  IBooleanFunction.OR), IBooleanFunction.OR));
        UltraSpacePortal.WORMHOLE_TOP_LEFT.put(Direction.EAST,
          VoxelShapes.combineAndSimplify(Block.makeCuboidShape(7.25, 11.5, 0, 8.75, 13, 2.75),
            VoxelShapes.combineAndSimplify(Block.makeCuboidShape(5.6875, 6.625, 0, 6.8125, 7.75, 0.0625),
              VoxelShapes.combineAndSimplify(Block.makeCuboidShape(7.25, 10, 0, 8.75, 11.5, 5.5),
                VoxelShapes.combineAndSimplify(Block.makeCuboidShape(5.6875, 5.5, 0, 6.8125, 6.625, 2.125),
                  VoxelShapes.combineAndSimplify(Block.makeCuboidShape(7.25, 8.5, 0, 8.75, 10, 7),
                    VoxelShapes.combineAndSimplify(Block.makeCuboidShape(5.6875, 4.375, 0, 6.8125, 5.5, 3.25),
                      VoxelShapes.combineAndSimplify(Block.makeCuboidShape(4.39063, 1.28125, 0, 5.23438, 2.125, 0.4375),
                        VoxelShapes.combineAndSimplify(Block.makeCuboidShape(7.25, 7, 0, 8.75, 8.5, 8.5),
                          VoxelShapes.combineAndSimplify(Block.makeCuboidShape(5.6875, 3.25, 0, 6.8125, 4.375, 4.375),
                            VoxelShapes.combineAndSimplify(Block.makeCuboidShape(4.39063, 0.4375, 0, 5.23438, 1.28125, 1.28125),
                              VoxelShapes.combineAndSimplify(Block.makeCuboidShape(7.25, 5.5, 0, 8.75, 7, 10),
                                VoxelShapes.combineAndSimplify(Block.makeCuboidShape(5.6875, 2.125, 0, 6.8125, 3.25, 5.5),
                                  VoxelShapes.combineAndSimplify(Block.makeCuboidShape(4.39063, 0, 0, 5.23438, 0.4375, 2.125),
                                    VoxelShapes.combineAndSimplify(Block.makeCuboidShape(7.25, 4, 0, 8.75, 5.5, 11.5),
                                      VoxelShapes.combineAndSimplify(Block.makeCuboidShape(5.6875, 1, 0, 6.8125, 2.125, 6.625),
                                        VoxelShapes.combineAndSimplify(Block.makeCuboidShape(7.25, 2.65, 0, 8.75, 4, 13),
                                          VoxelShapes.combineAndSimplify(Block.makeCuboidShape(5.6875, 0, 0, 6.8125, 1, 7.75),
                                            Block.makeCuboidShape(7.25, 0, 0, 8.75, 2.65, 14.5),
                                            IBooleanFunction.OR), IBooleanFunction.OR), IBooleanFunction.OR),
                                      IBooleanFunction.OR), IBooleanFunction.OR), IBooleanFunction.OR),
                                IBooleanFunction.OR), IBooleanFunction.OR), IBooleanFunction.OR),
                          IBooleanFunction.OR), IBooleanFunction.OR), IBooleanFunction.OR),
                    IBooleanFunction.OR), IBooleanFunction.OR), IBooleanFunction.OR),
              IBooleanFunction.OR), IBooleanFunction.OR));
        UltraSpacePortal.WORMHOLE_TOP_LEFT.put(Direction.SOUTH,
          VoxelShapes.combineAndSimplify(Block.makeCuboidShape(13.25, 11.5, 7.25, 16, 13, 8.75),
            VoxelShapes.combineAndSimplify(Block.makeCuboidShape(15.9375, 6.625, 5.6875, 16, 7.75, 6.8125),
              VoxelShapes.combineAndSimplify(Block.makeCuboidShape(10.5, 10, 7.25, 16, 11.5, 8.75),
                VoxelShapes.combineAndSimplify(Block.makeCuboidShape(13.875, 5.5, 5.6875, 16, 6.625, 6.8125),
                  VoxelShapes.combineAndSimplify(Block.makeCuboidShape(9, 8.5, 7.25, 16, 10, 8.75),
                    VoxelShapes.combineAndSimplify(Block.makeCuboidShape(12.75, 4.375, 5.6875, 16, 5.5, 6.8125),
                      VoxelShapes.combineAndSimplify(Block.makeCuboidShape(15.5625, 1.28125, 4.39063, 16, 2.125, 5.23438),
                        VoxelShapes.combineAndSimplify(Block.makeCuboidShape(7.5, 7, 7.25, 16, 8.5, 8.75),
                          VoxelShapes.combineAndSimplify(Block.makeCuboidShape(11.625, 3.25, 5.6875, 16, 4.375, 6.8125),
                            VoxelShapes.combineAndSimplify(Block.makeCuboidShape(14.71875, 0.4375, 4.39063, 16, 1.28125, 5.23438),
                              VoxelShapes.combineAndSimplify(Block.makeCuboidShape(6, 5.5, 7.25, 16, 7, 8.75),
                                VoxelShapes.combineAndSimplify(Block.makeCuboidShape(10.5, 2.125, 5.6875, 16, 3.25, 6.8125),
                                  VoxelShapes.combineAndSimplify(Block.makeCuboidShape(13.875, 0, 4.39063, 16, 0.4375, 5.23438),
                                    VoxelShapes.combineAndSimplify(Block.makeCuboidShape(4.5, 4, 7.25, 16, 5.5, 8.75),
                                      VoxelShapes.combineAndSimplify(Block.makeCuboidShape(9.375, 1, 5.6875, 16, 2.125, 6.8125),
                                        VoxelShapes.combineAndSimplify(Block.makeCuboidShape(3, 2.65, 7.25, 16, 4, 8.75),
                                          VoxelShapes.combineAndSimplify(Block.makeCuboidShape(8.25, 0, 5.6875, 16, 1, 6.8125),
                                            Block.makeCuboidShape(1.5, 0, 7.25, 16, 2.65, 8.75),
                                            IBooleanFunction.OR), IBooleanFunction.OR), IBooleanFunction.OR),
                                            IBooleanFunction.OR), IBooleanFunction.OR), IBooleanFunction.OR),
                                      IBooleanFunction.OR), IBooleanFunction.OR), IBooleanFunction.OR),
                                IBooleanFunction.OR), IBooleanFunction.OR), IBooleanFunction.OR),
                          IBooleanFunction.OR), IBooleanFunction.OR), IBooleanFunction.OR),
                    IBooleanFunction.OR), IBooleanFunction.OR));
        UltraSpacePortal.WORMHOLE_TOP_LEFT.put(Direction.WEST,
          VoxelShapes.combineAndSimplify(Block.makeCuboidShape(7.25, 11.5, 13.25, 8.75, 13, 16),
            VoxelShapes.combineAndSimplify(Block.makeCuboidShape(9.1875, 6.625, 15.9375, 10.3125, 7.75, 16),
              VoxelShapes.combineAndSimplify(Block.makeCuboidShape(7.25, 10, 10.5, 8.75, 11.5, 16),
                VoxelShapes.combineAndSimplify(Block.makeCuboidShape(9.1875, 5.5, 13.875, 10.3125, 6.625, 16),
                  VoxelShapes.combineAndSimplify(Block.makeCuboidShape(7.25, 8.5, 9, 8.75, 10, 16),
                    VoxelShapes.combineAndSimplify(Block.makeCuboidShape(9.1875, 4.375, 12.75, 10.3125, 5.5, 16),
                      VoxelShapes.combineAndSimplify(Block.makeCuboidShape(10.76562, 1.28125, 15.5625, 11.60937, 2.125, 16),
                        VoxelShapes.combineAndSimplify(Block.makeCuboidShape(7.25, 7, 7.5, 8.75, 8.5, 16),
                          VoxelShapes.combineAndSimplify(Block.makeCuboidShape(9.1875, 3.25, 11.625, 10.3125, 4.375, 16),
                            VoxelShapes.combineAndSimplify(Block.makeCuboidShape(10.76562, 0.4375, 14.71875, 11.60937, 1.28125, 16),
                              VoxelShapes.combineAndSimplify(Block.makeCuboidShape(7.25, 5.5, 6, 8.75, 7, 16),
                                VoxelShapes.combineAndSimplify(Block.makeCuboidShape(9.1875, 2.125, 10.5, 10.3125, 3.25, 16),
                                  VoxelShapes.combineAndSimplify(Block.makeCuboidShape(10.76562, 0, 13.875, 11.60937, 0.4375, 16),
                                    VoxelShapes.combineAndSimplify(Block.makeCuboidShape(7.25, 4, 4.5, 8.75, 5.5, 16),
                                      VoxelShapes.combineAndSimplify(Block.makeCuboidShape(9.1875, 1, 9.375, 10.3125, 2.125, 16),
                                        VoxelShapes.combineAndSimplify(Block.makeCuboidShape(7.25, 2.65, 3, 8.75, 4, 16),
                                          VoxelShapes.combineAndSimplify(Block.makeCuboidShape(9.1875, 0, 8.25, 10.3125, 1, 16),
                                            Block.makeCuboidShape(7.25, 0, 1.5, 8.75, 2.65, 16),
                                            IBooleanFunction.OR), IBooleanFunction.OR), IBooleanFunction.OR),
                                      IBooleanFunction.OR), IBooleanFunction.OR), IBooleanFunction.OR),
                                IBooleanFunction.OR), IBooleanFunction.OR), IBooleanFunction.OR),
                          IBooleanFunction.OR), IBooleanFunction.OR), IBooleanFunction.OR),
                    IBooleanFunction.OR), IBooleanFunction.OR), IBooleanFunction.OR),
              IBooleanFunction.OR), IBooleanFunction.OR));

        UltraSpacePortal.WORMHOLE_TOP_RIGHT.put(Direction.NORTH,
          VoxelShapes.combineAndSimplify(Block.makeCuboidShape(13.25, 11.5, 7.25, 16, 13, 8.75),
            VoxelShapes.combineAndSimplify(Block.makeCuboidShape(15.9375, 6.625, 9.1875, 16, 7.75, 10.3125),
              VoxelShapes.combineAndSimplify(Block.makeCuboidShape(10.5, 10, 7.25, 16, 11.5, 8.75),
                VoxelShapes.combineAndSimplify(Block.makeCuboidShape(13.875, 5.5, 9.1875, 16, 6.625, 10.3125),
                  VoxelShapes.combineAndSimplify(Block.makeCuboidShape(9, 8.5, 7.25, 16, 10, 8.75),
                    VoxelShapes.combineAndSimplify(Block.makeCuboidShape(12.75, 4.375, 9.1875, 16, 5.5, 10.3125),
                      VoxelShapes.combineAndSimplify(Block.makeCuboidShape(15.5625, 1.28125, 10.76562, 16, 2.125, 11.60937),
                        VoxelShapes.combineAndSimplify(Block.makeCuboidShape(7.5, 7, 7.25, 16, 8.5, 8.75),
                          VoxelShapes.combineAndSimplify(Block.makeCuboidShape(11.625, 3.25, 9.1875, 16, 4.375, 10.3125),
                            VoxelShapes.combineAndSimplify(Block.makeCuboidShape(14.7187, 0.4375, 10.76562, 16, 1.28125, 11.60937),
                              VoxelShapes.combineAndSimplify(Block.makeCuboidShape(6, 5.5, 7.25, 16, 7, 8.75),
                                VoxelShapes.combineAndSimplify(Block.makeCuboidShape(10.5, 2.125, 9.1875, 16, 3.25, 10.3125),
                                  VoxelShapes.combineAndSimplify(Block.makeCuboidShape(13.875, 0, 10.76562, 16, 0.4375, 11.60937),
                                    VoxelShapes.combineAndSimplify(Block.makeCuboidShape(4.5, 4, 7.25, 16, 5.5, 8.75),
                                      VoxelShapes.combineAndSimplify(Block.makeCuboidShape(9.375, 1, 9.1875, 16, 2.125, 10.3125),
                                        VoxelShapes.combineAndSimplify(Block.makeCuboidShape(3, 2.65, 7.25, 16, 4, 8.75),
                                          VoxelShapes.combineAndSimplify(Block.makeCuboidShape(8.25, 0, 9.1875, 16, 1, 10.3125),
                                            Block.makeCuboidShape(1.5, 0, 7.25, 16, 2.65, 8.75),
                                            IBooleanFunction.OR), IBooleanFunction.OR), IBooleanFunction.OR),
                                      IBooleanFunction.OR), IBooleanFunction.OR), IBooleanFunction.OR),
                                IBooleanFunction.OR), IBooleanFunction.OR), IBooleanFunction.OR),
                          IBooleanFunction.OR), IBooleanFunction.OR), IBooleanFunction.OR),
                    IBooleanFunction.OR), IBooleanFunction.OR), IBooleanFunction.OR),
              IBooleanFunction.OR), IBooleanFunction.OR));
        UltraSpacePortal.WORMHOLE_TOP_RIGHT.put(Direction.EAST,
          VoxelShapes.combineAndSimplify(Block.makeCuboidShape(7.25, 11.5, 13.25, 8.75, 13, 16),
            VoxelShapes.combineAndSimplify(Block.makeCuboidShape(5.6875, 6.625, 15.9375, 6.8125, 7.75, 16),
              VoxelShapes.combineAndSimplify(Block.makeCuboidShape(7.25, 10, 10.5, 8.75, 11.5, 16),
                VoxelShapes.combineAndSimplify(Block.makeCuboidShape(5.6875, 5.5, 13.875, 6.8125, 6.625, 16),
                  VoxelShapes.combineAndSimplify(Block.makeCuboidShape(7.25, 8.5, 9, 8.75, 10, 16),
                    VoxelShapes.combineAndSimplify(Block.makeCuboidShape(5.6875, 4.375, 12.75, 6.8125, 5.5, 16),
                      VoxelShapes.combineAndSimplify(Block.makeCuboidShape(4.39063, 1.28125, 15.5625, 5.23438, 2.125, 16),
                        VoxelShapes.combineAndSimplify(Block.makeCuboidShape(7.25, 7, 7.5, 8.75, 8.5, 16),
                          VoxelShapes.combineAndSimplify(Block.makeCuboidShape(5.6875, 3.25, 11.625, 6.8125, 4.375, 16),
                            VoxelShapes.combineAndSimplify(Block.makeCuboidShape(4.39063, 0.4375, 14.7187, 5.23438, 1.28125, 16),
                              VoxelShapes.combineAndSimplify(Block.makeCuboidShape(7.25, 5.5, 6, 8.75, 7, 16),
                                VoxelShapes.combineAndSimplify(Block.makeCuboidShape(5.6875, 2.125, 10.5, 6.8125, 3.25, 16),
                                  VoxelShapes.combineAndSimplify(Block.makeCuboidShape(4.39063, 0, 13.875, 5.23438, 0.4375, 16),
                                    VoxelShapes.combineAndSimplify(Block.makeCuboidShape(7.25, 4, 4.5, 8.75, 5.5, 16),
                                      VoxelShapes.combineAndSimplify(Block.makeCuboidShape(5.6875, 1, 9.375, 6.8125, 2.125, 16),
                                        VoxelShapes.combineAndSimplify(Block.makeCuboidShape(7.25, 2.65, 3, 8.75, 4, 16),
                                          VoxelShapes.combineAndSimplify(Block.makeCuboidShape(5.6875, 0, 8.25, 6.8125, 1, 16),
                                            Block.makeCuboidShape(7.25, 0, 1.5, 8.75, 2.65, 16),
                                            IBooleanFunction.OR), IBooleanFunction.OR), IBooleanFunction.OR),
                                      IBooleanFunction.OR), IBooleanFunction.OR), IBooleanFunction.OR),
                                IBooleanFunction.OR), IBooleanFunction.OR), IBooleanFunction.OR),
                          IBooleanFunction.OR), IBooleanFunction.OR), IBooleanFunction.OR),
                    IBooleanFunction.OR), IBooleanFunction.OR), IBooleanFunction.OR),
              IBooleanFunction.OR), IBooleanFunction.OR));
        UltraSpacePortal.WORMHOLE_TOP_RIGHT.put(Direction.SOUTH,
          VoxelShapes.combineAndSimplify(Block.makeCuboidShape(0, 11.5, 7.25, 2.75, 13, 8.75),
            VoxelShapes.combineAndSimplify(Block.makeCuboidShape(0, 6.625, 5.6875, 0.0625, 7.75, 6.8125),
              VoxelShapes.combineAndSimplify(Block.makeCuboidShape(0, 10, 7.25, 5.5, 11.5, 8.75),
                VoxelShapes.combineAndSimplify(Block.makeCuboidShape(0, 5.5, 5.6875, 2.125, 6.625, 6.8125),
                  VoxelShapes.combineAndSimplify(Block.makeCuboidShape(0, 8.5, 7.25, 7, 10, 8.75),
                    VoxelShapes.combineAndSimplify(Block.makeCuboidShape(0, 4.375, 5.6875, 3.25, 5.5, 6.8125),
                      VoxelShapes.combineAndSimplify(Block.makeCuboidShape(0, 1.28125, 4.39063, 0.4375, 2.125, 5.23438),
                        VoxelShapes.combineAndSimplify(Block.makeCuboidShape(0, 7, 7.25, 8.5, 8.5, 8.75),
                          VoxelShapes.combineAndSimplify(Block.makeCuboidShape(0, 3.25, 5.6875, 4.375, 4.375, 6.8125),
                            VoxelShapes.combineAndSimplify(Block.makeCuboidShape(0, 0.4375, 4.39063, 1.2813, 1.28125, 5.23438),
                              VoxelShapes.combineAndSimplify(Block.makeCuboidShape(0, 5.5, 7.25, 10, 7, 8.75),
                                VoxelShapes.combineAndSimplify(Block.makeCuboidShape(0, 2.125, 5.6875, 5.5, 3.25, 6.8125),
                                  VoxelShapes.combineAndSimplify(Block.makeCuboidShape(0, 0, 4.39063, 2.125, 0.4375, 5.23438),
                                    VoxelShapes.combineAndSimplify(Block.makeCuboidShape(0, 4, 7.25, 11.5, 5.5, 8.75),
                                      VoxelShapes.combineAndSimplify(Block.makeCuboidShape(0, 1, 5.6875, 6.625, 2.125, 6.8125),
                                        VoxelShapes.combineAndSimplify(Block.makeCuboidShape(0, 2.65, 7.25, 13, 4, 8.75),
                                          VoxelShapes.combineAndSimplify(Block.makeCuboidShape(0, 0, 5.6875, 7.75, 1, 6.8125),
                                            Block.makeCuboidShape(0, 0, 7.25, 14.5, 2.65, 8.75),
                                            IBooleanFunction.OR), IBooleanFunction.OR), IBooleanFunction.OR),
                                      IBooleanFunction.OR), IBooleanFunction.OR), IBooleanFunction.OR),
                                IBooleanFunction.OR), IBooleanFunction.OR), IBooleanFunction.OR),
                          IBooleanFunction.OR), IBooleanFunction.OR), IBooleanFunction.OR),
                    IBooleanFunction.OR), IBooleanFunction.OR), IBooleanFunction.OR),
              IBooleanFunction.OR), IBooleanFunction.OR));
        UltraSpacePortal.WORMHOLE_TOP_RIGHT.put(Direction.WEST,
          VoxelShapes.combineAndSimplify(Block.makeCuboidShape(7.25, 11.5, 0, 8.75, 13, 2.75),
            VoxelShapes.combineAndSimplify(Block.makeCuboidShape(9.1875, 6.625, 0, 10.3125, 7.75, 0.0625),
              VoxelShapes.combineAndSimplify(Block.makeCuboidShape(7.25, 10, 0, 8.75, 11.5, 5.5),
                VoxelShapes.combineAndSimplify(Block.makeCuboidShape(9.1875, 5.5, 0, 10.3125, 6.625, 2.125),
                  VoxelShapes.combineAndSimplify(Block.makeCuboidShape(7.25, 8.5, 0, 8.75, 10, 7),
                    VoxelShapes.combineAndSimplify(Block.makeCuboidShape(9.1875, 4.375, 0, 10.3125, 5.5, 3.25),
                      VoxelShapes.combineAndSimplify(Block.makeCuboidShape(10.76562, 1.28125, 0, 11.60937, 2.125, 0.4375),
                        VoxelShapes.combineAndSimplify(Block.makeCuboidShape(7.25, 7, 0, 8.75, 8.5, 8.5),
                          VoxelShapes.combineAndSimplify(Block.makeCuboidShape(9.1875, 3.25, 0, 10.3125, 4.375, 4.375),
                            VoxelShapes.combineAndSimplify(Block.makeCuboidShape(10.76562, 0.4375, 0, 11.60937, 1.28125, 1.2813),
                              VoxelShapes.combineAndSimplify(Block.makeCuboidShape(7.25, 5.5, 0, 8.75, 7, 10),
                                VoxelShapes.combineAndSimplify(Block.makeCuboidShape(9.1875, 2.125, 0, 10.3125, 3.25, 5.5),
                                  VoxelShapes.combineAndSimplify(Block.makeCuboidShape(10.76562, 0, 0, 11.60937, 0.4375, 2.125),
                                    VoxelShapes.combineAndSimplify(Block.makeCuboidShape(7.25, 4, 0, 8.75, 5.5, 11.5),
                                      VoxelShapes.combineAndSimplify(Block.makeCuboidShape(9.1875, 1, 0, 10.3125, 2.125, 6.625),
                                        VoxelShapes.combineAndSimplify(Block.makeCuboidShape(7.25, 2.65, 0, 8.75, 4, 13),
                                          VoxelShapes.combineAndSimplify(Block.makeCuboidShape(9.1875, 0, 0, 10.3125, 1, 7.75),
                                            Block.makeCuboidShape(7.25, 0, 0, 8.75, 2.65, 14.5),
                                            IBooleanFunction.OR), IBooleanFunction.OR), IBooleanFunction.OR),
                                      IBooleanFunction.OR), IBooleanFunction.OR), IBooleanFunction.OR),
                                IBooleanFunction.OR), IBooleanFunction.OR), IBooleanFunction.OR),
                          IBooleanFunction.OR), IBooleanFunction.OR), IBooleanFunction.OR),
                    IBooleanFunction.OR), IBooleanFunction.OR), IBooleanFunction.OR),
              IBooleanFunction.OR), IBooleanFunction.OR));

        UltraSpacePortal.WORMHOLE_MIDDLE.put(Direction.NORTH,
          VoxelShapes.combineAndSimplify(Block.makeCuboidShape(0, 0, 7.25, 16, 1.5, 8.75),
            VoxelShapes.combineAndSimplify(Block.makeCuboidShape(0, 0, 9.1875, 16, 3.125, 10.3125),
              VoxelShapes.combineAndSimplify(Block.makeCuboidShape(0, 0, 10.76562, 16, 4.3438, 11.60937),
                VoxelShapes.combineAndSimplify(Block.makeCuboidShape(9.6, 1.5, 7.25, 16, 3, 8.75),
                  VoxelShapes.combineAndSimplify(Block.makeCuboidShape(9.2, 3.125, 9.1875, 16, 4.25, 10.3125),
                    VoxelShapes.combineAndSimplify(Block.makeCuboidShape(8.9, 4.34375, 10.76562, 16, 5.1875, 11.60937),
                      VoxelShapes.combineAndSimplify(Block.makeCuboidShape(9.6, 12, 7.25, 16, 13.5, 8.75),
                        VoxelShapes.combineAndSimplify(Block.makeCuboidShape(9.2, 11, 9.1875, 16, 12.125, 10.3125),
                          VoxelShapes.combineAndSimplify(Block.makeCuboidShape(8.9, 10.25, 10.76562, 16, 11.09375, 11.60937),
                            VoxelShapes.combineAndSimplify(Block.makeCuboidShape(12.8, 3, 7.25, 16, 4.5, 8.75),
                              VoxelShapes.combineAndSimplify(Block.makeCuboidShape(11.6, 4.25, 9.1875, 16, 5.375, 10.3125),
                                VoxelShapes.combineAndSimplify(Block.makeCuboidShape(10.7, 5.1875, 10.76562, 16, 6.03125, 11.60937),
                                  VoxelShapes.combineAndSimplify(Block.makeCuboidShape(12.8, 10.5, 7.25, 16, 12, 8.75),
                                    VoxelShapes.combineAndSimplify(Block.makeCuboidShape(11.6, 9.875, 9.1875, 16, 11, 10.3125),
                                      VoxelShapes.combineAndSimplify(Block.makeCuboidShape(10.7, 9.40625, 10.76562, 16, 10.25, 11.60937),
                                        VoxelShapes.combineAndSimplify(Block.makeCuboidShape(14.4, 4.5, 7.25, 16, 10.5, 8.75),
                                          VoxelShapes.combineAndSimplify(Block.makeCuboidShape(12.8, 5.375, 9.1875, 16, 9.875, 10.3125),
                                            VoxelShapes.combineAndSimplify(Block.makeCuboidShape(11.6, 6.03125, 10.76562, 16, 9.40625, 11.60937),
                                              VoxelShapes.combineAndSimplify(Block.makeCuboidShape(0, 4.5, 7.25, 1.6, 10.5, 8.75),
                                                VoxelShapes.combineAndSimplify(Block.makeCuboidShape(0, 5.375, 9.1875, 3.2, 9.875, 10.3125),
                                                  VoxelShapes.combineAndSimplify(Block.makeCuboidShape(0, 6.03125, 10.76562, 4.4, 9.40625, 11.60937),
                                                    VoxelShapes.combineAndSimplify(Block.makeCuboidShape(0, 3, 7.25, 3.2, 4.5, 8.75),
                                                      VoxelShapes.combineAndSimplify(Block.makeCuboidShape(0, 4.25, 9.1875, 4.4, 5.375, 10.3125),
                                                        VoxelShapes.combineAndSimplify(Block.makeCuboidShape(0, 5.1875, 10.76562, 5.3, 6.03125, 11.60937),
                                                          VoxelShapes.combineAndSimplify(Block.makeCuboidShape(0, 10.5, 7.25, 3.2, 12, 8.75),
                                                            VoxelShapes.combineAndSimplify(Block.makeCuboidShape(0, 9.875, 9.1875, 4.4, 11, 10.3125),
                                                              VoxelShapes.combineAndSimplify(Block.makeCuboidShape(0, 9.40625, 10.76562, 5.3, 10.25, 11.60937),
                                                                VoxelShapes.combineAndSimplify(Block.makeCuboidShape(0, 1.5, 7.25, 6.4, 3, 8.75),
                                                                  VoxelShapes.combineAndSimplify(Block.makeCuboidShape(0, 3.125, 9.1875, 6.8, 4.25, 10.3125),
                                                                    VoxelShapes.combineAndSimplify(Block.makeCuboidShape(0, 4.34375, 10.76562, 7.1, 5.1875, 11.60937),
                                                                      VoxelShapes.combineAndSimplify(Block.makeCuboidShape(0, 12, 7.25, 6.4, 13.5, 8.75),
                                                                        VoxelShapes.combineAndSimplify(Block.makeCuboidShape(0, 11, 9.1875, 6.8, 12.125, 10.3125),
                                                                          VoxelShapes.combineAndSimplify(Block.makeCuboidShape(0, 10.25, 10.76562, 7.1, 11.09375, 11.60937),
                                                                            VoxelShapes.combineAndSimplify(Block.makeCuboidShape(0, 13.5, 7.25, 16, 16, 8.75),
                                                                              VoxelShapes.combineAndSimplify(Block.makeCuboidShape(0, 12.125, 9.1875, 16, 16, 10.3125),
                                                                                Block.makeCuboidShape(0, 11.09375, 10.76562, 16, 15.99995, 11.60937),
                                                                                IBooleanFunction.OR), IBooleanFunction.OR), IBooleanFunction.OR),
                                                                          IBooleanFunction.OR), IBooleanFunction.OR), IBooleanFunction.OR),
                                                                    IBooleanFunction.OR), IBooleanFunction.OR), IBooleanFunction.OR),
                                                              IBooleanFunction.OR), IBooleanFunction.OR), IBooleanFunction.OR),
                                                        IBooleanFunction.OR), IBooleanFunction.OR), IBooleanFunction.OR),
                                                  IBooleanFunction.OR), IBooleanFunction.OR), IBooleanFunction.OR),
                                            IBooleanFunction.OR), IBooleanFunction.OR), IBooleanFunction.OR),
                                      IBooleanFunction.OR), IBooleanFunction.OR), IBooleanFunction.OR),
                                IBooleanFunction.OR), IBooleanFunction.OR), IBooleanFunction.OR),
                          IBooleanFunction.OR), IBooleanFunction.OR), IBooleanFunction.OR),
                    IBooleanFunction.OR), IBooleanFunction.OR), IBooleanFunction.OR),
              IBooleanFunction.OR), IBooleanFunction.OR));
        UltraSpacePortal.WORMHOLE_MIDDLE.put(Direction.EAST,
          VoxelShapes.combineAndSimplify(Block.makeCuboidShape(7.25, 0, 0, 8.75, 1.5, 16),
            VoxelShapes.combineAndSimplify(Block.makeCuboidShape(5.6875, 0, 0, 6.8125, 3.125, 16),
              VoxelShapes.combineAndSimplify(Block.makeCuboidShape(4.39063, 0, 0, 5.23438, 4.3438, 16),
                VoxelShapes.combineAndSimplify(Block.makeCuboidShape(7.25, 1.5, 9.6, 8.75, 3, 16),
                  VoxelShapes.combineAndSimplify(Block.makeCuboidShape(5.6875, 3.125, 9.2, 6.8125, 4.25, 16),
                    VoxelShapes.combineAndSimplify(Block.makeCuboidShape(4.39063, 4.34375, 8.9, 5.23438, 5.1875, 16),
                      VoxelShapes.combineAndSimplify(Block.makeCuboidShape(7.25, 12, 9.6, 8.75, 13.5, 16),
                        VoxelShapes.combineAndSimplify(Block.makeCuboidShape(5.6875, 11, 9.2, 6.8125, 12.125, 16),
                          VoxelShapes.combineAndSimplify(Block.makeCuboidShape(4.39063, 10.25, 8.9, 5.23438, 11.09375, 16),
                            VoxelShapes.combineAndSimplify(Block.makeCuboidShape(7.25, 3, 12.8, 8.75, 4.5, 16),
                              VoxelShapes.combineAndSimplify(Block.makeCuboidShape(5.6875, 4.25, 11.6, 6.8125, 5.375, 16),
                                VoxelShapes.combineAndSimplify(Block.makeCuboidShape(4.39063, 5.1875, 10.7, 5.23438, 6.03125, 16),
                                  VoxelShapes.combineAndSimplify(Block.makeCuboidShape(7.25, 10.5, 12.8, 8.75, 12, 16),
                                    VoxelShapes.combineAndSimplify(Block.makeCuboidShape(5.6875, 9.875, 11.6, 6.8125, 11, 16),
                                      VoxelShapes.combineAndSimplify(Block.makeCuboidShape(4.39063, 9.40625, 10.7, 5.23438, 10.25, 16),
                                        VoxelShapes.combineAndSimplify(Block.makeCuboidShape(7.25, 4.5, 14.4, 8.75, 10.5, 16),
                                          VoxelShapes.combineAndSimplify(Block.makeCuboidShape(5.6875, 5.375, 12.8, 6.8125, 9.875, 16),
                                            VoxelShapes.combineAndSimplify(Block.makeCuboidShape(4.39063, 6.03125, 11.6, 5.23438, 9.40625, 16),
                                              VoxelShapes.combineAndSimplify(Block.makeCuboidShape(7.25, 4.5, 0, 8.75, 10.5, 1.6),
                                                VoxelShapes.combineAndSimplify(Block.makeCuboidShape(5.6875, 5.375, 0, 6.8125, 9.875, 3.2),
                                                  VoxelShapes.combineAndSimplify(Block.makeCuboidShape(4.39063, 6.03125, 0, 5.23438, 9.40625, 4.4),
                                                    VoxelShapes.combineAndSimplify(Block.makeCuboidShape(7.25, 3, 0, 8.75, 4.5, 3.2),
                                                      VoxelShapes.combineAndSimplify(Block.makeCuboidShape(5.6875, 4.25, 0, 6.8125, 5.375, 4.4),
                                                        VoxelShapes.combineAndSimplify(Block.makeCuboidShape(4.39063, 5.1875, 0, 5.23438, 6.03125, 5.3),
                                                          VoxelShapes.combineAndSimplify(Block.makeCuboidShape(7.25, 10.5, 0, 8.75, 12, 3.2),
                                                            VoxelShapes.combineAndSimplify(Block.makeCuboidShape(5.6875, 9.875, 0, 6.8125, 11, 4.4),
                                                              VoxelShapes.combineAndSimplify(Block.makeCuboidShape(4.39063, 9.40625, 0, 5.23438, 10.25, 5.3),
                                                                VoxelShapes.combineAndSimplify(Block.makeCuboidShape(7.25, 1.5, 0, 8.75, 3, 6.4),
                                                                  VoxelShapes.combineAndSimplify(Block.makeCuboidShape(5.6875, 3.125, 0, 6.8125, 4.25, 6.8),
                                                                    VoxelShapes.combineAndSimplify(Block.makeCuboidShape(4.39063, 4.34375, 0, 5.23438, 5.1875, 7.1),
                                                                      VoxelShapes.combineAndSimplify(Block.makeCuboidShape(7.25, 12, 0, 8.75, 13.5, 6.4),
                                                                        VoxelShapes.combineAndSimplify(Block.makeCuboidShape(5.6875, 11, 0, 6.8125, 12.125, 6.8),
                                                                          VoxelShapes.combineAndSimplify(Block.makeCuboidShape(4.39063, 10.25, 0, 5.23438, 11.09375, 7.1),
                                                                            VoxelShapes.combineAndSimplify(Block.makeCuboidShape(7.25, 13.5, 0, 8.75, 16, 16),
                                                                              VoxelShapes.combineAndSimplify(Block.makeCuboidShape(5.6875, 12.125, 0, 6.8125, 16, 16),
                                                                                Block.makeCuboidShape(4.39063, 11.09375, 0, 5.23438, 15.99995, 16),
                                                                                IBooleanFunction.OR), IBooleanFunction.OR), IBooleanFunction.OR),
                                                                          IBooleanFunction.OR), IBooleanFunction.OR), IBooleanFunction.OR),
                                                                    IBooleanFunction.OR), IBooleanFunction.OR), IBooleanFunction.OR),
                                                              IBooleanFunction.OR), IBooleanFunction.OR), IBooleanFunction.OR),
                                                        IBooleanFunction.OR), IBooleanFunction.OR), IBooleanFunction.OR),
                                                  IBooleanFunction.OR), IBooleanFunction.OR), IBooleanFunction.OR),
                                            IBooleanFunction.OR), IBooleanFunction.OR), IBooleanFunction.OR),
                                      IBooleanFunction.OR), IBooleanFunction.OR), IBooleanFunction.OR),
                                IBooleanFunction.OR), IBooleanFunction.OR), IBooleanFunction.OR),
                          IBooleanFunction.OR), IBooleanFunction.OR), IBooleanFunction.OR),
                    IBooleanFunction.OR), IBooleanFunction.OR), IBooleanFunction.OR),
              IBooleanFunction.OR), IBooleanFunction.OR));
        UltraSpacePortal.WORMHOLE_MIDDLE.put(Direction.SOUTH,
          VoxelShapes.combineAndSimplify(Block.makeCuboidShape(0, 0, 7.25, 16, 1.5, 8.75),
            VoxelShapes.combineAndSimplify(Block.makeCuboidShape(0, 0, 5.6875, 16, 3.125, 6.8125),
              VoxelShapes.combineAndSimplify(Block.makeCuboidShape(0, 0, 4.39063, 16, 4.3438, 5.23438),
                VoxelShapes.combineAndSimplify(Block.makeCuboidShape(0, 1.5, 7.25, 6.4, 3, 8.75),
                  VoxelShapes.combineAndSimplify(Block.makeCuboidShape(0, 3.125, 5.6875, 6.8, 4.25, 6.8125),
                    VoxelShapes.combineAndSimplify(Block.makeCuboidShape(0, 4.34375, 4.39063, 7.1, 5.1875, 5.23438),
                      VoxelShapes.combineAndSimplify(Block.makeCuboidShape(0, 12, 7.25, 6.4, 13.5, 8.75),
                        VoxelShapes.combineAndSimplify(Block.makeCuboidShape(0, 11, 5.6875, 6.8, 12.125, 6.8125),
                          VoxelShapes.combineAndSimplify(Block.makeCuboidShape(0, 10.25, 4.39063, 7.1, 11.09375, 5.23438),
                            VoxelShapes.combineAndSimplify(Block.makeCuboidShape(0, 3, 7.25, 3.2, 4.5, 8.75),
                              VoxelShapes.combineAndSimplify(Block.makeCuboidShape(0, 4.25, 5.6875, 4.4, 5.375, 6.8125),
                                VoxelShapes.combineAndSimplify(Block.makeCuboidShape(0, 5.1875, 4.39063, 5.3, 6.03125, 5.23438),
                                  VoxelShapes.combineAndSimplify(Block.makeCuboidShape(0, 10.5, 7.25, 3.2, 12, 8.75),
                                    VoxelShapes.combineAndSimplify(Block.makeCuboidShape(0, 9.875, 5.6875, 4.4, 11, 6.8125),
                                      VoxelShapes.combineAndSimplify(Block.makeCuboidShape(0, 9.40625, 4.39063, 5.3, 10.25, 5.23438),
                                        VoxelShapes.combineAndSimplify(Block.makeCuboidShape(0, 4.5, 7.25, 1.6, 10.5, 8.75),
                                          VoxelShapes.combineAndSimplify(Block.makeCuboidShape(0, 5.375, 5.6875, 3.2, 9.875, 6.8125),
                                            VoxelShapes.combineAndSimplify(Block.makeCuboidShape(0, 6.03125, 4.39063, 4.4, 9.40625, 5.23438),
                                              VoxelShapes.combineAndSimplify(Block.makeCuboidShape(14.4, 4.5, 7.25, 16, 10.5, 8.75),
                                                VoxelShapes.combineAndSimplify(Block.makeCuboidShape(12.8, 5.375, 5.6875, 16, 9.875, 6.8125),
                                                  VoxelShapes.combineAndSimplify(Block.makeCuboidShape(11.6, 6.03125, 4.39063, 16, 9.40625, 5.23438),
                                                    VoxelShapes.combineAndSimplify(Block.makeCuboidShape(12.8, 3, 7.25, 16, 4.5, 8.75),
                                                      VoxelShapes.combineAndSimplify(Block.makeCuboidShape(11.6, 4.25, 5.6875, 16, 5.375, 6.8125),
                                                        VoxelShapes.combineAndSimplify(Block.makeCuboidShape(10.7, 5.1875, 4.39063, 16, 6.03125, 5.23438),
                                                          VoxelShapes.combineAndSimplify(Block.makeCuboidShape(12.8, 10.5, 7.25, 16, 12, 8.75),
                                                            VoxelShapes.combineAndSimplify(Block.makeCuboidShape(11.6, 9.875, 5.6875, 16, 11, 6.8125),
                                                              VoxelShapes.combineAndSimplify(Block.makeCuboidShape(10.7, 9.40625, 4.39063, 16, 10.25, 5.23438),
                                                                VoxelShapes.combineAndSimplify(Block.makeCuboidShape(9.6, 1.5, 7.25, 16, 3, 8.75),
                                                                  VoxelShapes.combineAndSimplify(Block.makeCuboidShape(9.2, 3.125, 5.6875, 16, 4.25, 6.8125),
                                                                    VoxelShapes.combineAndSimplify(Block.makeCuboidShape(8.9, 4.34375, 4.39063, 16, 5.1875, 5.23438),
                                                                      VoxelShapes.combineAndSimplify(Block.makeCuboidShape(9.6, 12, 7.25, 16, 13.5, 8.75),
                                                                        VoxelShapes.combineAndSimplify(Block.makeCuboidShape(9.2, 11, 5.6875, 16, 12.125, 6.8125),
                                                                          VoxelShapes.combineAndSimplify(Block.makeCuboidShape(8.9, 10.25, 4.39063, 16, 11.09375, 5.23438),
                                                                            VoxelShapes.combineAndSimplify(Block.makeCuboidShape(0, 13.5, 7.25, 16, 16, 8.75),
                                                                              VoxelShapes.combineAndSimplify(Block.makeCuboidShape(0, 12.125, 5.6875, 16, 16, 6.8125),
                                                                                Block.makeCuboidShape(0, 11.09375, 4.39063, 16, 15.99995, 5.23438),
                                                                                IBooleanFunction.OR), IBooleanFunction.OR), IBooleanFunction.OR),
                                                                          IBooleanFunction.OR), IBooleanFunction.OR), IBooleanFunction.OR),
                                                                    IBooleanFunction.OR), IBooleanFunction.OR), IBooleanFunction.OR),
                                                              IBooleanFunction.OR), IBooleanFunction.OR), IBooleanFunction.OR),
                                                        IBooleanFunction.OR), IBooleanFunction.OR), IBooleanFunction.OR),
                                                  IBooleanFunction.OR), IBooleanFunction.OR), IBooleanFunction.OR),
                                            IBooleanFunction.OR), IBooleanFunction.OR), IBooleanFunction.OR),
                                      IBooleanFunction.OR), IBooleanFunction.OR), IBooleanFunction.OR),
                                IBooleanFunction.OR), IBooleanFunction.OR), IBooleanFunction.OR),
                          IBooleanFunction.OR), IBooleanFunction.OR), IBooleanFunction.OR),
                    IBooleanFunction.OR), IBooleanFunction.OR), IBooleanFunction.OR),
              IBooleanFunction.OR), IBooleanFunction.OR));
        UltraSpacePortal.WORMHOLE_MIDDLE.put(Direction.WEST,
          VoxelShapes.combineAndSimplify(Block.makeCuboidShape(7.25, 0, 0, 8.75, 1.5, 16),
            VoxelShapes.combineAndSimplify(Block.makeCuboidShape(9.1875, 0, 0, 10.3125, 3.125, 16),
              VoxelShapes.combineAndSimplify(Block.makeCuboidShape(10.76562, 0, 0, 11.60937, 4.3438, 16),
                VoxelShapes.combineAndSimplify(Block.makeCuboidShape(7.25, 1.5, 0, 8.75, 3, 6.4),
                  VoxelShapes.combineAndSimplify(Block.makeCuboidShape(9.1875, 3.125, 0, 10.3125, 4.25, 6.8),
                    VoxelShapes.combineAndSimplify(Block.makeCuboidShape(10.76562, 4.34375, 0, 11.60937, 5.1875, 7.1),
                      VoxelShapes.combineAndSimplify(Block.makeCuboidShape(7.25, 12, 0, 8.75, 13.5, 6.4),
                        VoxelShapes.combineAndSimplify(Block.makeCuboidShape(9.1875, 11, 0, 10.3125, 12.125, 6.8),
                          VoxelShapes.combineAndSimplify(Block.makeCuboidShape(10.76562, 10.25, 0, 11.60937, 11.09375, 7.1),
                            VoxelShapes.combineAndSimplify(Block.makeCuboidShape(7.25, 3, 0, 8.75, 4.5, 3.2),
                              VoxelShapes.combineAndSimplify(Block.makeCuboidShape(9.1875, 4.25, 0, 10.3125, 5.375, 4.4),
                                VoxelShapes.combineAndSimplify(Block.makeCuboidShape(10.76562, 5.1875, 0, 11.60937, 6.03125, 5.3),
                                  VoxelShapes.combineAndSimplify(Block.makeCuboidShape(7.25, 10.5, 0, 8.75, 12, 3.2),
                                    VoxelShapes.combineAndSimplify(Block.makeCuboidShape(9.1875, 9.875, 0, 10.3125, 11, 4.4),
                                      VoxelShapes.combineAndSimplify(Block.makeCuboidShape(10.76562, 9.40625, 0, 11.60937, 10.25, 5.3),
                                        VoxelShapes.combineAndSimplify(Block.makeCuboidShape(7.25, 4.5, 0, 8.75, 10.5, 1.6),
                                          VoxelShapes.combineAndSimplify(Block.makeCuboidShape(9.1875, 5.375, 0, 10.3125, 9.875, 3.2),
                                            VoxelShapes.combineAndSimplify(Block.makeCuboidShape(10.76562, 6.03125, 0, 11.60937, 9.40625, 4.4),
                                              VoxelShapes.combineAndSimplify(Block.makeCuboidShape(7.25, 4.5, 14.4, 8.75, 10.5, 16),
                                                VoxelShapes.combineAndSimplify(Block.makeCuboidShape(9.1875, 5.375, 12.8, 10.3125, 9.875, 16),
                                                  VoxelShapes.combineAndSimplify(Block.makeCuboidShape(10.76562, 6.03125, 11.6, 11.60937, 9.40625, 16),
                                                    VoxelShapes.combineAndSimplify(Block.makeCuboidShape(7.25, 3, 12.8, 8.75, 4.5, 16),
                                                      VoxelShapes.combineAndSimplify(Block.makeCuboidShape(10.76562, 5.1875, 10.7, 11.60937, 6.03125, 16),
                                                        VoxelShapes.combineAndSimplify(Block.makeCuboidShape(7.25, 10.5, 12.8, 8.75, 12, 16),
                                                          VoxelShapes.combineAndSimplify(Block.makeCuboidShape(9.1875, 9.875, 11.6, 10.3125, 11, 16),
                                                            VoxelShapes.combineAndSimplify(Block.makeCuboidShape(10.76562, 9.40625, 10.7, 11.60937, 10.25, 16),
                                                              VoxelShapes.combineAndSimplify(Block.makeCuboidShape(7.25, 1.5, 9.6, 8.75, 3, 16),
                                                                VoxelShapes.combineAndSimplify(Block.makeCuboidShape(9.1875, 3.125, 9.2, 10.3125, 4.25, 16),
                                                                  VoxelShapes.combineAndSimplify(Block.makeCuboidShape(10.76562, 4.34375, 8.9, 11.60937, 5.1875, 16),
                                                                    VoxelShapes.combineAndSimplify(Block.makeCuboidShape(7.25, 12, 9.6, 8.75, 13.5, 16),
                                                                      VoxelShapes.combineAndSimplify(Block.makeCuboidShape(9.1875, 11, 9.2, 10.3125, 12.125, 16),
                                                                        VoxelShapes.combineAndSimplify(Block.makeCuboidShape(10.76562, 10.25, 8.9, 11.60937, 11.09375, 16),
                                                                          VoxelShapes.combineAndSimplify(Block.makeCuboidShape(7.25, 13.5, 0, 8.75, 16, 16),
                                                                            VoxelShapes.combineAndSimplify(Block.makeCuboidShape(9.1875, 12.125, 0, 10.3125, 16, 16),
                                                                              VoxelShapes.combineAndSimplify(Block.makeCuboidShape(10.76562, 11.09375, 0, 11.60937, 15.99995, 16),
                                                                                Block.makeCuboidShape(9.1875, 4.25, 11.6, 10.3125, 5.375, 16),
                                                                                IBooleanFunction.OR), IBooleanFunction.OR), IBooleanFunction.OR),
                                                                          IBooleanFunction.OR), IBooleanFunction.OR), IBooleanFunction.OR),
                                                                    IBooleanFunction.OR), IBooleanFunction.OR), IBooleanFunction.OR),
                                                              IBooleanFunction.OR), IBooleanFunction.OR), IBooleanFunction.OR),
                                                        IBooleanFunction.OR), IBooleanFunction.OR), IBooleanFunction.OR),
                                                  IBooleanFunction.OR), IBooleanFunction.OR), IBooleanFunction.OR),
                                            IBooleanFunction.OR), IBooleanFunction.OR), IBooleanFunction.OR),
                                      IBooleanFunction.OR), IBooleanFunction.OR), IBooleanFunction.OR),
                                IBooleanFunction.OR), IBooleanFunction.OR), IBooleanFunction.OR),
                          IBooleanFunction.OR), IBooleanFunction.OR), IBooleanFunction.OR),
                    IBooleanFunction.OR), IBooleanFunction.OR), IBooleanFunction.OR),
              IBooleanFunction.OR), IBooleanFunction.OR));

        UltraSpacePortal.WORMHOLE_MIDDLE_LEFT.put(Direction.NORTH,
          VoxelShapes.combineAndSimplify(Block.makeCuboidShape(0, 0, 7.25, 14.5, 1.5, 8.75),
            VoxelShapes.combineAndSimplify(Block.makeCuboidShape(0, 0, 9.1875, 8.875, 3.125, 10.3125),
              VoxelShapes.combineAndSimplify(Block.makeCuboidShape(0, 0, 10.76562, 4.6563, 4.3438, 11.60937),
                VoxelShapes.combineAndSimplify(Block.makeCuboidShape(0, 13.5, 7.25, 14.5, 16, 8.75),
                  VoxelShapes.combineAndSimplify(Block.makeCuboidShape(0, 12.125, 9.1875, 8.875, 16, 10.3125),
                    VoxelShapes.combineAndSimplify(Block.makeCuboidShape(0, 11.09375, 10.76562, 4.6563, 15.99995, 11.60937),
                      VoxelShapes.combineAndSimplify(Block.makeCuboidShape(0, 1.5, 7.25, 16, 4.5, 8.75),
                        VoxelShapes.combineAndSimplify(Block.makeCuboidShape(0, 3.125, 9.1875, 10, 5.375, 10.3125),
                          VoxelShapes.combineAndSimplify(Block.makeCuboidShape(0, 4.34375, 10.76562, 5.5, 6.03125, 11.60937),
                            VoxelShapes.combineAndSimplify(Block.makeCuboidShape(0, 10.5, 7.25, 16, 13.5, 8.75),
                              VoxelShapes.combineAndSimplify(Block.makeCuboidShape(0, 9.875, 9.1875, 10, 12.125, 10.3125),
                                VoxelShapes.combineAndSimplify(Block.makeCuboidShape(0, 9.40625, 10.76562, 5.5, 11.09375, 11.60937),
                                  VoxelShapes.combineAndSimplify(Block.makeCuboidShape(0, 4.5, 7.25, 16, 10.5, 8.75),
                                    VoxelShapes.combineAndSimplify(Block.makeCuboidShape(0, 5.375, 9.1875, 10, 9.875, 10.3125),
                                      Block.makeCuboidShape(0, 6.03125, 10.76562, 5.5, 9.40625, 11.60937),
                                      IBooleanFunction.OR), IBooleanFunction.OR), IBooleanFunction.OR),
                                IBooleanFunction.OR), IBooleanFunction.OR), IBooleanFunction.OR),
                          IBooleanFunction.OR), IBooleanFunction.OR), IBooleanFunction.OR),
                    IBooleanFunction.OR), IBooleanFunction.OR), IBooleanFunction.OR),
              IBooleanFunction.OR), IBooleanFunction.OR));
        UltraSpacePortal.WORMHOLE_MIDDLE_LEFT.put(Direction.EAST,
          VoxelShapes.combineAndSimplify(Block.makeCuboidShape(7.25, 0, 0, 8.75, 1.5, 14.5),
            VoxelShapes.combineAndSimplify(Block.makeCuboidShape(5.6875, 0, 0, 6.8125, 3.125, 8.875),
              VoxelShapes.combineAndSimplify(Block.makeCuboidShape(4.39063, 0, 0, 5.23438, 4.3438, 4.6563),
                VoxelShapes.combineAndSimplify(Block.makeCuboidShape(7.25, 13.5, 0, 8.75, 16, 14.5),
                  VoxelShapes.combineAndSimplify(Block.makeCuboidShape(5.6875, 12.125, 0, 6.8125, 16, 8.875),
                    VoxelShapes.combineAndSimplify(Block.makeCuboidShape(4.39063, 11.09375, 0, 5.23438, 15.99995, 4.6563),
                      VoxelShapes.combineAndSimplify(Block.makeCuboidShape(7.25, 1.5, 0, 8.75, 4.5, 16),
                        VoxelShapes.combineAndSimplify(Block.makeCuboidShape(5.6875, 3.125, 0, 6.8125, 5.375, 10),
                          VoxelShapes.combineAndSimplify(Block.makeCuboidShape(4.39063, 4.34375, 0, 5.23438, 6.03125, 5.5),
                            VoxelShapes.combineAndSimplify(Block.makeCuboidShape(7.25, 10.5, 0, 8.75, 13.5, 16),
                              VoxelShapes.combineAndSimplify(Block.makeCuboidShape(5.6875, 9.875, 0, 6.8125, 12.125, 10),
                                VoxelShapes.combineAndSimplify(Block.makeCuboidShape(4.39063, 9.40625, 0, 5.23438, 11.09375, 5.5),
                                  VoxelShapes.combineAndSimplify(Block.makeCuboidShape(7.25, 4.5, 0, 8.75, 10.5, 16),
                                    VoxelShapes.combineAndSimplify(Block.makeCuboidShape(5.6875, 5.375, 0, 6.8125, 9.875, 10),
                                      Block.makeCuboidShape(4.39063, 6.03125, 0, 5.23438, 9.40625, 5.5),
                                      IBooleanFunction.OR), IBooleanFunction.OR), IBooleanFunction.OR),
                                IBooleanFunction.OR), IBooleanFunction.OR), IBooleanFunction.OR),
                          IBooleanFunction.OR), IBooleanFunction.OR), IBooleanFunction.OR),
                    IBooleanFunction.OR), IBooleanFunction.OR), IBooleanFunction.OR),
              IBooleanFunction.OR), IBooleanFunction.OR));
        UltraSpacePortal.WORMHOLE_MIDDLE_LEFT.put(Direction.SOUTH,
          VoxelShapes.combineAndSimplify(Block.makeCuboidShape(1.5, 0, 7.25, 16, 1.5, 8.75),
            VoxelShapes.combineAndSimplify(Block.makeCuboidShape(7.125, 0, 5.6875, 16, 3.125, 6.8125),
              VoxelShapes.combineAndSimplify(Block.makeCuboidShape(11.3437, 0, 4.39063, 16, 4.3438, 5.23438),
                VoxelShapes.combineAndSimplify(Block.makeCuboidShape(1.5, 13.5, 7.25, 16, 16, 8.75),
                  VoxelShapes.combineAndSimplify(Block.makeCuboidShape(7.125, 12.125, 5.6875, 16, 16, 6.8125),
                    VoxelShapes.combineAndSimplify(Block.makeCuboidShape(11.3437, 11.09375, 4.39063, 16, 15.99995, 5.23438),
                      VoxelShapes.combineAndSimplify(Block.makeCuboidShape(0, 1.5, 7.25, 16, 4.5, 8.75),
                        VoxelShapes.combineAndSimplify(Block.makeCuboidShape(6, 3.125, 5.6875, 16, 5.375, 6.8125),
                          VoxelShapes.combineAndSimplify(Block.makeCuboidShape(10.5, 4.34375, 4.39063, 16, 6.03125, 5.23438),
                            VoxelShapes.combineAndSimplify(Block.makeCuboidShape(0, 10.5, 7.25, 16, 13.5, 8.75),
                              VoxelShapes.combineAndSimplify(Block.makeCuboidShape(6, 9.875, 5.6875, 16, 12.125, 6.8125),
                                VoxelShapes.combineAndSimplify(Block.makeCuboidShape(10.5, 9.40625, 4.39063, 16, 11.09375, 5.23438),
                                  VoxelShapes.combineAndSimplify(Block.makeCuboidShape(0, 4.5, 7.25, 16, 10.5, 8.75),
                                    VoxelShapes.combineAndSimplify(Block.makeCuboidShape(6, 5.375, 5.6875, 16, 9.875, 6.8125),
                                      Block.makeCuboidShape(10.5, 6.03125, 4.39063, 16, 9.40625, 5.23438),
                                      IBooleanFunction.OR), IBooleanFunction.OR), IBooleanFunction.OR),
                                IBooleanFunction.OR), IBooleanFunction.OR), IBooleanFunction.OR),
                          IBooleanFunction.OR), IBooleanFunction.OR), IBooleanFunction.OR),
                    IBooleanFunction.OR), IBooleanFunction.OR), IBooleanFunction.OR),
              IBooleanFunction.OR), IBooleanFunction.OR));
        UltraSpacePortal.WORMHOLE_MIDDLE_LEFT.put(Direction.WEST,
          VoxelShapes.combineAndSimplify(Block.makeCuboidShape(7.25, 0, 1.5, 8.75, 1.5, 16),
            VoxelShapes.combineAndSimplify(Block.makeCuboidShape(9.1875, 0, 7.125, 10.3125, 3.125, 16),
              VoxelShapes.combineAndSimplify(Block.makeCuboidShape(10.76562, 0, 11.3437, 11.60937, 4.3438, 16),
                VoxelShapes.combineAndSimplify(Block.makeCuboidShape(7.25, 13.5, 1.5, 8.75, 16, 16),
                  VoxelShapes.combineAndSimplify(Block.makeCuboidShape(9.1875, 12.125, 7.125, 10.3125, 16, 16),
                    VoxelShapes.combineAndSimplify(Block.makeCuboidShape(10.76562, 11.09375, 11.3437, 11.60937, 15.99995, 16),
                      VoxelShapes.combineAndSimplify(Block.makeCuboidShape(7.25, 1.5, 0, 8.75, 4.5, 16),
                        VoxelShapes.combineAndSimplify(Block.makeCuboidShape(9.1875, 3.125, 6, 10.3125, 5.375, 16),
                          VoxelShapes.combineAndSimplify(Block.makeCuboidShape(10.76562, 4.34375, 10.5, 11.60937, 6.03125, 16),
                            VoxelShapes.combineAndSimplify(Block.makeCuboidShape(7.25, 10.5, 0, 8.75, 13.5, 16),
                              VoxelShapes.combineAndSimplify(Block.makeCuboidShape(9.1875, 9.875, 6, 10.3125, 12.125, 16),
                                VoxelShapes.combineAndSimplify(Block.makeCuboidShape(10.76562, 9.40625, 10.5, 11.60937, 11.09375, 16),
                                  VoxelShapes.combineAndSimplify(Block.makeCuboidShape(7.25, 4.5, 0, 8.75, 10.5, 16),
                                    VoxelShapes.combineAndSimplify(Block.makeCuboidShape(9.1875, 5.375, 6, 10.3125, 9.875, 16),
                                      Block.makeCuboidShape(10.76562, 6.03125, 10.5, 11.60937, 9.40625, 16),
                                      IBooleanFunction.OR), IBooleanFunction.OR), IBooleanFunction.OR),
                                IBooleanFunction.OR), IBooleanFunction.OR), IBooleanFunction.OR),
                          IBooleanFunction.OR), IBooleanFunction.OR), IBooleanFunction.OR),
                    IBooleanFunction.OR), IBooleanFunction.OR), IBooleanFunction.OR),
              IBooleanFunction.OR), IBooleanFunction.OR));

        UltraSpacePortal.WORMHOLE_MIDDLE_RIGHT.put(Direction.NORTH,
          VoxelShapes.combineAndSimplify(Block.makeCuboidShape(1.5, 0, 7.25, 16, 1.5, 8.75),
            VoxelShapes.combineAndSimplify(Block.makeCuboidShape(7.125, 0, 9.1875, 16, 3.125, 10.3125),
              VoxelShapes.combineAndSimplify(Block.makeCuboidShape(11.3437, 0, 10.76562, 16, 4.3438, 11.60937),
                VoxelShapes.combineAndSimplify(Block.makeCuboidShape(1.5, 13.5, 7.25, 16, 16, 8.75),
                  VoxelShapes.combineAndSimplify(Block.makeCuboidShape(7.125, 12.125, 9.1875, 16, 16, 10.3125),
                    VoxelShapes.combineAndSimplify(Block.makeCuboidShape(11.3437, 11.09375, 10.76562, 16, 15.99995, 11.60937),
                      VoxelShapes.combineAndSimplify(Block.makeCuboidShape(0, 1.5, 7.25, 16, 4.5, 8.75),
                        VoxelShapes.combineAndSimplify(Block.makeCuboidShape(6, 3.125, 9.1875, 16, 5.375, 10.3125),
                          VoxelShapes.combineAndSimplify(Block.makeCuboidShape(10.5, 4.34375, 10.76562, 16, 6.03125, 11.60937),
                            VoxelShapes.combineAndSimplify(Block.makeCuboidShape(0, 10.5, 7.25, 16, 13.5, 8.75),
                              VoxelShapes.combineAndSimplify(Block.makeCuboidShape(6, 9.875, 9.1875, 16, 12.125, 10.3125),
                                VoxelShapes.combineAndSimplify(Block.makeCuboidShape(10.5, 9.40625, 10.76562, 16, 11.09375, 11.60937),
                                  VoxelShapes.combineAndSimplify(Block.makeCuboidShape(0, 4.5, 7.25, 16, 10.5, 8.75),
                                    VoxelShapes.combineAndSimplify(Block.makeCuboidShape(6, 5.375, 9.1875, 16, 9.875, 10.3125),
                                      Block.makeCuboidShape(10.5, 6.03125, 10.76562, 16, 9.40625, 11.60937),
                                      IBooleanFunction.OR), IBooleanFunction.OR), IBooleanFunction.OR),
                                IBooleanFunction.OR), IBooleanFunction.OR), IBooleanFunction.OR),
                          IBooleanFunction.OR), IBooleanFunction.OR), IBooleanFunction.OR),
                    IBooleanFunction.OR), IBooleanFunction.OR), IBooleanFunction.OR),
              IBooleanFunction.OR), IBooleanFunction.OR));
        UltraSpacePortal.WORMHOLE_MIDDLE_RIGHT.put(Direction.EAST,
          VoxelShapes.combineAndSimplify(Block.makeCuboidShape(7.25, 0, 1.5, 8.75, 1.5, 16),
            VoxelShapes.combineAndSimplify(Block.makeCuboidShape(5.6875, 0, 7.125, 6.8125, 3.125, 16),
              VoxelShapes.combineAndSimplify(Block.makeCuboidShape(4.39063, 0, 11.3437, 5.23438, 4.3438, 16),
                VoxelShapes.combineAndSimplify(Block.makeCuboidShape(7.25, 13.5, 1.5, 8.75, 16, 16),
                  VoxelShapes.combineAndSimplify(Block.makeCuboidShape(5.6875, 12.125, 7.125, 6.8125, 16, 16),
                    VoxelShapes.combineAndSimplify(Block.makeCuboidShape(4.39063, 11.09375, 11.3437, 5.23438, 15.99995, 16),
                      VoxelShapes.combineAndSimplify(Block.makeCuboidShape(7.25, 1.5, 0, 8.75, 4.5, 16),
                        VoxelShapes.combineAndSimplify(Block.makeCuboidShape(5.6875, 3.125, 6, 6.8125, 5.375, 16),
                          VoxelShapes.combineAndSimplify(Block.makeCuboidShape(4.39063, 4.34375, 10.5, 5.23438, 6.03125, 16),
                            VoxelShapes.combineAndSimplify(Block.makeCuboidShape(7.25, 10.5, 0, 8.75, 13.5, 16),
                              VoxelShapes.combineAndSimplify(Block.makeCuboidShape(5.6875, 9.875, 6, 6.8125, 12.125, 16),
                                VoxelShapes.combineAndSimplify(Block.makeCuboidShape(4.39063, 9.40625, 10.5, 5.23438, 11.09375, 16),
                                  VoxelShapes.combineAndSimplify(Block.makeCuboidShape(7.25, 4.5, 0, 8.75, 10.5, 16),
                                    VoxelShapes.combineAndSimplify(Block.makeCuboidShape(5.6875, 5.375, 6, 6.8125, 9.875, 16),
                                      Block.makeCuboidShape(4.39063, 6.03125, 10.5, 5.23438, 9.40625, 16),
                                      IBooleanFunction.OR), IBooleanFunction.OR), IBooleanFunction.OR),
                                IBooleanFunction.OR), IBooleanFunction.OR), IBooleanFunction.OR),
                          IBooleanFunction.OR), IBooleanFunction.OR), IBooleanFunction.OR),
                    IBooleanFunction.OR), IBooleanFunction.OR), IBooleanFunction.OR),
              IBooleanFunction.OR), IBooleanFunction.OR));
        UltraSpacePortal.WORMHOLE_MIDDLE_RIGHT.put(Direction.SOUTH,
          VoxelShapes.combineAndSimplify(Block.makeCuboidShape(0, 0, 7.25, 14.5, 1.5, 8.75),
            VoxelShapes.combineAndSimplify(Block.makeCuboidShape(0, 0, 5.6875, 8.875, 3.125, 6.8125),
              VoxelShapes.combineAndSimplify(Block.makeCuboidShape(0, 0, 4.39063, 4.6563, 4.3438, 5.23438),
                VoxelShapes.combineAndSimplify(Block.makeCuboidShape(0, 13.5, 7.25, 14.5, 16, 8.75),
                  VoxelShapes.combineAndSimplify(Block.makeCuboidShape(0, 12.125, 5.6875, 8.875, 16, 6.8125),
                    VoxelShapes.combineAndSimplify(Block.makeCuboidShape(0, 11.09375, 4.39063, 4.6563, 15.99995, 5.23438),
                      VoxelShapes.combineAndSimplify(Block.makeCuboidShape(0, 1.5, 7.25, 16, 4.5, 8.75),
                        VoxelShapes.combineAndSimplify(Block.makeCuboidShape(0, 3.125, 5.6875, 10, 5.375, 6.8125),
                          VoxelShapes.combineAndSimplify(Block.makeCuboidShape(0, 4.34375, 4.39063, 5.5, 6.03125, 5.23438),
                            VoxelShapes.combineAndSimplify(Block.makeCuboidShape(0, 10.5, 7.25, 16, 13.5, 8.75),
                              VoxelShapes.combineAndSimplify(Block.makeCuboidShape(0, 9.875, 5.6875, 10, 12.125, 6.8125),
                                VoxelShapes.combineAndSimplify(Block.makeCuboidShape(0, 9.40625, 4.39063, 5.5, 11.09375, 5.23438),
                                  VoxelShapes.combineAndSimplify(Block.makeCuboidShape(0, 4.5, 7.25, 16, 10.5, 8.75),
                                    VoxelShapes.combineAndSimplify(Block.makeCuboidShape(0, 5.375, 5.6875, 10, 9.875, 6.8125),
                                      Block.makeCuboidShape(0, 6.03125, 4.39063, 5.5, 9.40625, 5.23438),
                                      IBooleanFunction.OR), IBooleanFunction.OR), IBooleanFunction.OR),
                                IBooleanFunction.OR), IBooleanFunction.OR), IBooleanFunction.OR),
                          IBooleanFunction.OR), IBooleanFunction.OR), IBooleanFunction.OR),
                    IBooleanFunction.OR), IBooleanFunction.OR), IBooleanFunction.OR),
              IBooleanFunction.OR), IBooleanFunction.OR));
        UltraSpacePortal.WORMHOLE_MIDDLE_RIGHT.put(Direction.WEST,
          VoxelShapes.combineAndSimplify(Block.makeCuboidShape(7.25, 0, 0, 8.75, 1.5, 14.5),
            VoxelShapes.combineAndSimplify(Block.makeCuboidShape(9.1875, 0, 0, 10.3125, 3.125, 8.875),
              VoxelShapes.combineAndSimplify(Block.makeCuboidShape(10.76562, 0, 0, 11.60937, 4.3438, 4.6563),
                VoxelShapes.combineAndSimplify(Block.makeCuboidShape(7.25, 13.5, 0, 8.75, 16, 14.5),
                  VoxelShapes.combineAndSimplify(Block.makeCuboidShape(9.1875, 12.125, 0, 10.3125, 16, 8.875),
                    VoxelShapes.combineAndSimplify(Block.makeCuboidShape(10.76562, 11.09375, 0, 11.60937, 15.99995, 4.6563),
                      VoxelShapes.combineAndSimplify(Block.makeCuboidShape(7.25, 1.5, 0, 8.75, 4.5, 16),
                        VoxelShapes.combineAndSimplify(Block.makeCuboidShape(9.1875, 3.125, 0, 10.3125, 5.375, 10),
                          VoxelShapes.combineAndSimplify(Block.makeCuboidShape(10.76562, 4.34375, 0, 11.60937, 6.03125, 5.5),
                            VoxelShapes.combineAndSimplify(Block.makeCuboidShape(7.25, 10.5, 0, 8.75, 13.5, 16),
                              VoxelShapes.combineAndSimplify(Block.makeCuboidShape(9.1875, 9.875, 0, 10.3125, 12.125, 10),
                                VoxelShapes.combineAndSimplify(Block.makeCuboidShape(10.76562, 9.40625, 0, 11.60937, 11.09375, 5.5),
                                  VoxelShapes.combineAndSimplify(Block.makeCuboidShape(7.25, 4.5, 0, 8.75, 10.5, 16),
                                    VoxelShapes.combineAndSimplify(Block.makeCuboidShape(9.1875, 5.375, 0, 10.3125, 9.875, 10),
                                      Block.makeCuboidShape(10.76562, 6.03125, 0, 11.60937, 9.40625, 5.5),
                                      IBooleanFunction.OR), IBooleanFunction.OR), IBooleanFunction.OR),
                                IBooleanFunction.OR), IBooleanFunction.OR), IBooleanFunction.OR),
                          IBooleanFunction.OR), IBooleanFunction.OR), IBooleanFunction.OR),
                    IBooleanFunction.OR), IBooleanFunction.OR), IBooleanFunction.OR),
              IBooleanFunction.OR), IBooleanFunction.OR));

        UltraSpacePortal.WORMHOLE_BOTTOM.put(Direction.NORTH,
          VoxelShapes.combineAndSimplify(Block.makeCuboidShape(4.875, 0, 7.25, 11.125, 1.5, 8.75),
            VoxelShapes.combineAndSimplify(Block.makeCuboidShape(5.65625, 6, 9.1875, 10.34375, 7.125, 10.3125),
              VoxelShapes.combineAndSimplify(Block.makeCuboidShape(6.24219, 10.5, 10.76562, 9.75781, 11.34375, 11.60937),
                VoxelShapes.combineAndSimplify(Block.makeCuboidShape(0, 1.5, 7.25, 16, 16, 8.75),
                  VoxelShapes.combineAndSimplify(Block.makeCuboidShape(0, 7.125, 9.1875, 16, 16, 10.3125),
                    Block.makeCuboidShape(0, 11.34375, 10.76562, 16, 15.99995, 11.60937),
                    IBooleanFunction.OR), IBooleanFunction.OR), IBooleanFunction.OR),
              IBooleanFunction.OR), IBooleanFunction.OR));
        UltraSpacePortal.WORMHOLE_BOTTOM.put(Direction.EAST,
          VoxelShapes.combineAndSimplify(Block.makeCuboidShape(7.25, 0, 4.875, 8.75, 1.5, 11.125),
            VoxelShapes.combineAndSimplify(Block.makeCuboidShape(5.6875, 6, 5.65625, 6.8125, 7.125, 10.34375),
              VoxelShapes.combineAndSimplify(Block.makeCuboidShape(4.39063, 10.5, 6.24219, 5.23438, 11.34375, 9.75781),
                VoxelShapes.combineAndSimplify(Block.makeCuboidShape(7.25, 1.5, 0, 8.75, 16, 16),
                  VoxelShapes.combineAndSimplify(Block.makeCuboidShape(5.6875, 7.125, 0, 6.8125, 16, 16),
                    Block.makeCuboidShape(4.39063, 11.34375, 0, 5.23438, 15.99995, 16),
                    IBooleanFunction.OR), IBooleanFunction.OR), IBooleanFunction.OR),
              IBooleanFunction.OR), IBooleanFunction.OR));
        UltraSpacePortal.WORMHOLE_BOTTOM.put(Direction.SOUTH,
          VoxelShapes.combineAndSimplify(Block.makeCuboidShape(4.875, 0, 7.25, 11.125, 1.5, 8.75),
            VoxelShapes.combineAndSimplify(Block.makeCuboidShape(5.65625, 6, 5.6875, 10.34375, 7.125, 6.8125),
              VoxelShapes.combineAndSimplify(Block.makeCuboidShape(6.24219, 10.5, 4.39063, 9.75781, 11.34375, 5.23438),
                VoxelShapes.combineAndSimplify(Block.makeCuboidShape(0, 1.5, 7.25, 16, 16, 8.75),
                  VoxelShapes.combineAndSimplify(Block.makeCuboidShape(0, 7.125, 5.6875, 16, 16, 6.8125),
                    Block.makeCuboidShape(0, 11.34375, 4.39063, 16, 15.99995, 5.23438),
                    IBooleanFunction.OR), IBooleanFunction.OR), IBooleanFunction.OR),
              IBooleanFunction.OR), IBooleanFunction.OR));
        UltraSpacePortal.WORMHOLE_BOTTOM.put(Direction.WEST,
          VoxelShapes.combineAndSimplify(Block.makeCuboidShape(7.25, 0, 4.875, 8.75, 1.5, 11.125),
            VoxelShapes.combineAndSimplify(Block.makeCuboidShape(9.1875, 6, 5.65625, 10.3125, 7.125, 10.34375),
              VoxelShapes.combineAndSimplify(Block.makeCuboidShape(10.76562, 10.5, 6.24219, 11.60937, 11.34375, 9.75781),
                VoxelShapes.combineAndSimplify(Block.makeCuboidShape(7.25, 1.5, 0, 8.75, 16, 16),
                  VoxelShapes.combineAndSimplify(Block.makeCuboidShape(9.1875, 7.125, 0, 10.3125, 16, 16),
                    Block.makeCuboidShape(10.76562, 11.34375, 0, 11.60937, 15.99995, 16),
                    IBooleanFunction.OR), IBooleanFunction.OR), IBooleanFunction.OR),
              IBooleanFunction.OR), IBooleanFunction.OR));

        UltraSpacePortal.WORMHOLE_BOTTOM_LEFT.put(Direction.NORTH,
          VoxelShapes.combineAndSimplify(Block.makeCuboidShape(0, 1.5, 7.25, 2.75, 3, 8.75),
            VoxelShapes.combineAndSimplify(Block.makeCuboidShape(0, 7.125, 9.1875, 0.0625, 8.25, 10.3125),
              VoxelShapes.combineAndSimplify(Block.makeCuboidShape(0, 3, 7.25, 5.5, 4.5, 8.75),
                VoxelShapes.combineAndSimplify(Block.makeCuboidShape(0, 8.25, 9.1875, 2.125, 9.375, 10.3125),
                  VoxelShapes.combineAndSimplify(Block.makeCuboidShape(0, 4.5, 7.25, 7, 6, 8.75),
                    VoxelShapes.combineAndSimplify(Block.makeCuboidShape(0, 9.375, 9.1875, 3.25, 10.5, 10.3125),
                      VoxelShapes.combineAndSimplify(Block.makeCuboidShape(0, 13.03125, 10.76562, 0.4375, 13.875, 11.60937),
                        VoxelShapes.combineAndSimplify(Block.makeCuboidShape(0, 6, 7.25, 8.5, 7.5, 8.75),
                          VoxelShapes.combineAndSimplify(Block.makeCuboidShape(0, 10.5, 9.1875, 4.375, 11.625, 10.3125),
                            VoxelShapes.combineAndSimplify(Block.makeCuboidShape(0, 13.875, 10.76562, 1.28125, 14.71875, 11.60937),
                              VoxelShapes.combineAndSimplify(Block.makeCuboidShape(0, 7.5, 7.25, 10, 9, 8.75),
                                VoxelShapes.combineAndSimplify(Block.makeCuboidShape(0, 11.625, 9.1875, 5.5, 12.75, 10.3125),
                                  VoxelShapes.combineAndSimplify(Block.makeCuboidShape(0, 14.71875, 10.76562, 2.125, 15.5625, 11.60937),
                                    VoxelShapes.combineAndSimplify(Block.makeCuboidShape(0, 9, 7.25, 11.5, 10.5, 8.75),
                                      VoxelShapes.combineAndSimplify(Block.makeCuboidShape(0, 12.75, 9.1875, 6.625, 13.875, 10.3125),
                                        VoxelShapes.combineAndSimplify(Block.makeCuboidShape(0, 15.5625, 10.76562, 2.96875, 16, 11.60937),
                                          VoxelShapes.combineAndSimplify(Block.makeCuboidShape(0, 10.5, 7.25, 13, 12, 8.75),
                                            VoxelShapes.combineAndSimplify(Block.makeCuboidShape(0, 13.875, 9.1875, 7.75, 15, 10.3125),
                                              VoxelShapes.combineAndSimplify(Block.makeCuboidShape(0, 12, 7.25, 14.5, 16, 8.75),
                                                Block.makeCuboidShape(0, 15, 9.1875, 8.875, 16, 10.3125),
                                                IBooleanFunction.OR), IBooleanFunction.OR), IBooleanFunction.OR),
                                          IBooleanFunction.OR), IBooleanFunction.OR), IBooleanFunction.OR),
                                    IBooleanFunction.OR), IBooleanFunction.OR), IBooleanFunction.OR),
                              IBooleanFunction.OR), IBooleanFunction.OR), IBooleanFunction.OR),
                        IBooleanFunction.OR), IBooleanFunction.OR), IBooleanFunction.OR),
                  IBooleanFunction.OR), IBooleanFunction.OR), IBooleanFunction.OR),
            IBooleanFunction.OR));
        UltraSpacePortal.WORMHOLE_BOTTOM_LEFT.put(Direction.EAST,
          VoxelShapes.combineAndSimplify(Block.makeCuboidShape(7.25, 1.5, 0, 8.75, 3, 2.75),
            VoxelShapes.combineAndSimplify(Block.makeCuboidShape(5.6875, 7.125, 0, 6.8125, 8.25, 0.0625),
              VoxelShapes.combineAndSimplify(Block.makeCuboidShape(7.25, 3, 0, 8.75, 4.5, 5.5),
                VoxelShapes.combineAndSimplify(Block.makeCuboidShape(5.6875, 8.25, 0, 6.8125, 9.375, 2.125),
                  VoxelShapes.combineAndSimplify(Block.makeCuboidShape(7.25, 4.5, 0, 8.75, 6, 7),
                    VoxelShapes.combineAndSimplify(Block.makeCuboidShape(5.6875, 9.375, 0, 6.8125, 10.5, 3.25),
                      VoxelShapes.combineAndSimplify(Block.makeCuboidShape(4.39063, 13.03125, 0, 5.23438, 13.875, 0.4375),
                        VoxelShapes.combineAndSimplify(Block.makeCuboidShape(7.25, 6, 0, 8.75, 7.5, 8.5),
                          VoxelShapes.combineAndSimplify(Block.makeCuboidShape(5.6875, 10.5, 0, 6.8125, 11.625, 4.375),
                            VoxelShapes.combineAndSimplify(Block.makeCuboidShape(4.39063, 13.875, 0, 5.23438, 14.71875, 1.28125),
                              VoxelShapes.combineAndSimplify(Block.makeCuboidShape(7.25, 7.5, 0, 8.75, 9, 10),
                                VoxelShapes.combineAndSimplify(Block.makeCuboidShape(5.6875, 11.625, 0, 6.8125, 12.75, 5.5),
                                  VoxelShapes.combineAndSimplify(Block.makeCuboidShape(4.39063, 14.71875, 0, 5.23438, 15.5625, 2.125),
                                    VoxelShapes.combineAndSimplify(Block.makeCuboidShape(7.25, 9, 0, 8.75, 10.5, 11.5),
                                      VoxelShapes.combineAndSimplify(Block.makeCuboidShape(5.6875, 12.75, 0, 6.8125, 13.875, 6.625),
                                        VoxelShapes.combineAndSimplify(Block.makeCuboidShape(4.39063, 15.5625, 0, 5.23438, 16, 2.96875),
                                          VoxelShapes.combineAndSimplify(Block.makeCuboidShape(7.25, 10.5, 0, 8.75, 12, 13),
                                            VoxelShapes.combineAndSimplify(Block.makeCuboidShape(5.6875, 13.875, 0, 6.8125, 15, 7.75),
                                              VoxelShapes.combineAndSimplify(Block.makeCuboidShape(7.25, 12, 0, 8.75, 16, 14.5),
                                                Block.makeCuboidShape(5.6875, 15, 0, 6.8125, 16, 8.875),
                                                IBooleanFunction.OR), IBooleanFunction.OR), IBooleanFunction.OR),
                                          IBooleanFunction.OR), IBooleanFunction.OR), IBooleanFunction.OR),
                                    IBooleanFunction.OR), IBooleanFunction.OR), IBooleanFunction.OR),
                              IBooleanFunction.OR), IBooleanFunction.OR), IBooleanFunction.OR),
                        IBooleanFunction.OR), IBooleanFunction.OR), IBooleanFunction.OR),
                  IBooleanFunction.OR), IBooleanFunction.OR), IBooleanFunction.OR),
            IBooleanFunction.OR));
        UltraSpacePortal.WORMHOLE_BOTTOM_LEFT.put(Direction.SOUTH,
          VoxelShapes.combineAndSimplify(Block.makeCuboidShape(13.25, 1.5, 7.25, 16, 3, 8.75),
            VoxelShapes.combineAndSimplify(Block.makeCuboidShape(15.9375, 7.125, 5.6875, 16, 8.25, 6.8125),
              VoxelShapes.combineAndSimplify(Block.makeCuboidShape(10.5, 3, 7.25, 16, 4.5, 8.75),
                VoxelShapes.combineAndSimplify(Block.makeCuboidShape(13.875, 8.25, 5.6875, 16, 9.375, 6.8125),
                  VoxelShapes.combineAndSimplify(Block.makeCuboidShape(9, 4.5, 7.25, 16, 6, 8.75),
                    VoxelShapes.combineAndSimplify(Block.makeCuboidShape(12.75, 9.375, 5.6875, 16, 10.5, 6.8125),
                      VoxelShapes.combineAndSimplify(Block.makeCuboidShape(15.5625, 13.03125, 4.39063, 16, 13.875, 5.23438),
                        VoxelShapes.combineAndSimplify(Block.makeCuboidShape(7.5, 6, 7.25, 16, 7.5, 8.75),
                          VoxelShapes.combineAndSimplify(Block.makeCuboidShape(11.625, 10.5, 5.6875, 16, 11.625, 6.8125),
                            VoxelShapes.combineAndSimplify(Block.makeCuboidShape(14.71875, 13.875, 4.39063, 16, 14.71875, 5.23438),
                              VoxelShapes.combineAndSimplify(Block.makeCuboidShape(6, 7.5, 7.25, 16, 9, 8.75),
                                VoxelShapes.combineAndSimplify(Block.makeCuboidShape(10.5, 11.625, 5.6875, 16, 12.75, 6.8125),
                                  VoxelShapes.combineAndSimplify(Block.makeCuboidShape(13.875, 14.71875, 4.39063, 16, 15.5625, 5.23438),
                                    VoxelShapes.combineAndSimplify(Block.makeCuboidShape(4.5, 9, 7.25, 16, 10.5, 8.75),
                                      VoxelShapes.combineAndSimplify(Block.makeCuboidShape(9.375, 12.75, 5.6875, 16, 13.875, 6.8125),
                                        VoxelShapes.combineAndSimplify(Block.makeCuboidShape(13.03125, 15.5625, 4.39063, 16, 16, 5.23438),
                                          VoxelShapes.combineAndSimplify(Block.makeCuboidShape(3, 10.5, 7.25, 16, 12, 8.75),
                                            VoxelShapes.combineAndSimplify(Block.makeCuboidShape(8.25, 13.875, 5.6875, 16, 15, 6.8125),
                                              VoxelShapes.combineAndSimplify(Block.makeCuboidShape(1.5, 12, 7.25, 16, 16, 8.75),
                                                Block.makeCuboidShape(7.125, 15, 5.6875, 16, 16, 6.8125),
                                                IBooleanFunction.OR), IBooleanFunction.OR), IBooleanFunction.OR),
                                          IBooleanFunction.OR), IBooleanFunction.OR), IBooleanFunction.OR),
                                    IBooleanFunction.OR), IBooleanFunction.OR), IBooleanFunction.OR),
                              IBooleanFunction.OR), IBooleanFunction.OR), IBooleanFunction.OR),
                        IBooleanFunction.OR), IBooleanFunction.OR), IBooleanFunction.OR),
                  IBooleanFunction.OR), IBooleanFunction.OR), IBooleanFunction.OR),
            IBooleanFunction.OR));
        UltraSpacePortal.WORMHOLE_BOTTOM_LEFT.put(Direction.WEST,
          VoxelShapes.combineAndSimplify(Block.makeCuboidShape(7.25, 1.5, 13.25, 8.75, 3, 16),
            VoxelShapes.combineAndSimplify(Block.makeCuboidShape(9.1875, 7.125, 15.9375, 10.3125, 8.25, 16),
              VoxelShapes.combineAndSimplify(Block.makeCuboidShape(7.25, 3, 10.5, 8.75, 4.5, 16),
                VoxelShapes.combineAndSimplify(Block.makeCuboidShape(9.1875, 8.25, 13.875, 10.3125, 9.375, 16),
                  VoxelShapes.combineAndSimplify(Block.makeCuboidShape(7.25, 4.5, 9, 8.75, 6, 16),
                    VoxelShapes.combineAndSimplify(Block.makeCuboidShape(9.1875, 9.375, 12.75, 10.3125, 10.5, 16),
                      VoxelShapes.combineAndSimplify(Block.makeCuboidShape(10.76562, 13.03125, 15.5625, 11.60937, 13.875, 16),
                        VoxelShapes.combineAndSimplify(Block.makeCuboidShape(7.25, 6, 7.5, 8.75, 7.5, 16),
                          VoxelShapes.combineAndSimplify(Block.makeCuboidShape(9.1875, 10.5, 11.625, 10.3125, 11.625, 16),
                            VoxelShapes.combineAndSimplify(Block.makeCuboidShape(10.76562, 13.875, 14.71875, 11.60937, 14.71875, 16),
                              VoxelShapes.combineAndSimplify(Block.makeCuboidShape(7.25, 7.5, 6, 8.75, 9, 16),
                                VoxelShapes.combineAndSimplify(Block.makeCuboidShape(9.1875, 11.625, 10.5, 10.3125, 12.75, 16),
                                  VoxelShapes.combineAndSimplify(Block.makeCuboidShape(10.76562, 14.71875, 13.875, 11.60937, 15.5625, 16),
                                    VoxelShapes.combineAndSimplify(Block.makeCuboidShape(7.25, 9, 4.5, 8.75, 10.5, 16),
                                      VoxelShapes.combineAndSimplify(Block.makeCuboidShape(9.1875, 12.75, 9.375, 10.3125, 13.875, 16),
                                        VoxelShapes.combineAndSimplify(Block.makeCuboidShape(10.76562, 15.5625, 13.03125, 11.60937, 16, 16),
                                          VoxelShapes.combineAndSimplify(Block.makeCuboidShape(7.25, 10.5, 3, 8.75, 12, 16),
                                            VoxelShapes.combineAndSimplify(Block.makeCuboidShape(9.1875, 13.875, 8.25, 10.3125, 15, 16),
                                              VoxelShapes.combineAndSimplify(Block.makeCuboidShape(7.25, 12, 1.5, 8.75, 16, 16),
                                                Block.makeCuboidShape(9.1875, 15, 7.125, 10.3125, 16, 16),
                                                IBooleanFunction.OR), IBooleanFunction.OR), IBooleanFunction.OR),
                                          IBooleanFunction.OR), IBooleanFunction.OR), IBooleanFunction.OR),
                                    IBooleanFunction.OR), IBooleanFunction.OR), IBooleanFunction.OR),
                              IBooleanFunction.OR), IBooleanFunction.OR), IBooleanFunction.OR),
                        IBooleanFunction.OR), IBooleanFunction.OR), IBooleanFunction.OR),
                  IBooleanFunction.OR), IBooleanFunction.OR), IBooleanFunction.OR),
            IBooleanFunction.OR));

        UltraSpacePortal.WORMHOLE_BOTTOM_RIGHT.put(Direction.NORTH,
          VoxelShapes.combineAndSimplify(Block.makeCuboidShape(13.25, 1.5, 7.25, 16, 3, 8.75),
            VoxelShapes.combineAndSimplify(Block.makeCuboidShape(15.9375, 7.125, 9.1875, 16, 8.25, 10.3125),
              VoxelShapes.combineAndSimplify(Block.makeCuboidShape(10.5, 3, 7.25, 16, 4.5, 8.75),
                VoxelShapes.combineAndSimplify(Block.makeCuboidShape(13.875, 8.25, 9.1875, 16, 9.375, 10.3125),
                  VoxelShapes.combineAndSimplify(Block.makeCuboidShape(9, 4.5, 7.25, 16, 6, 8.75),
                    VoxelShapes.combineAndSimplify(Block.makeCuboidShape(12.75, 9.375, 9.1875, 16, 10.5, 10.3125),
                      VoxelShapes.combineAndSimplify(Block.makeCuboidShape(15.5625, 13.03125, 10.76562, 16, 13.875, 11.60937),
                        VoxelShapes.combineAndSimplify(Block.makeCuboidShape(7.5, 6, 7.25, 16, 7.5, 8.75),
                          VoxelShapes.combineAndSimplify(Block.makeCuboidShape(11.625, 10.5, 9.1875, 16, 11.625, 10.3125),
                            VoxelShapes.combineAndSimplify(Block.makeCuboidShape(14.7187, 13.875, 10.76562, 16, 14.71875, 11.60937),
                              VoxelShapes.combineAndSimplify(Block.makeCuboidShape(6, 7.5, 7.25, 16, 9, 8.75),
                                VoxelShapes.combineAndSimplify(Block.makeCuboidShape(10.5, 11.625, 9.1875, 16, 12.75, 10.3125),
                                  VoxelShapes.combineAndSimplify(Block.makeCuboidShape(13.875, 14.71875, 10.76562, 16, 15.5625, 11.60937),
                                    VoxelShapes.combineAndSimplify(Block.makeCuboidShape(4.5, 9, 7.25, 16, 10.5, 8.75),
                                      VoxelShapes.combineAndSimplify(Block.makeCuboidShape(9.375, 12.75, 9.1875, 16, 13.875, 10.3125),
                                        VoxelShapes.combineAndSimplify(Block.makeCuboidShape(13.0312, 15.5625, 10.76562, 16, 16, 11.60937),
                                          VoxelShapes.combineAndSimplify(Block.makeCuboidShape(3, 10.5, 7.25, 16, 12, 8.75),
                                            VoxelShapes.combineAndSimplify(Block.makeCuboidShape(8.25, 13.875, 9.1875, 16, 15, 10.3125),
                                              VoxelShapes.combineAndSimplify(Block.makeCuboidShape(1.5, 12, 7.25, 16, 16, 8.75),
                                                Block.makeCuboidShape(7.125, 15, 9.1875, 16, 16, 10.3125),
                                                IBooleanFunction.OR), IBooleanFunction.OR), IBooleanFunction.OR),
                                          IBooleanFunction.OR), IBooleanFunction.OR), IBooleanFunction.OR),
                                    IBooleanFunction.OR), IBooleanFunction.OR), IBooleanFunction.OR),
                              IBooleanFunction.OR), IBooleanFunction.OR), IBooleanFunction.OR),
                        IBooleanFunction.OR), IBooleanFunction.OR), IBooleanFunction.OR),
                  IBooleanFunction.OR), IBooleanFunction.OR), IBooleanFunction.OR),
            IBooleanFunction.OR));
        UltraSpacePortal.WORMHOLE_BOTTOM_RIGHT.put(Direction.EAST,
          VoxelShapes.combineAndSimplify(Block.makeCuboidShape(7.25, 1.5, 13.25, 8.75, 3, 16),
            VoxelShapes.combineAndSimplify(Block.makeCuboidShape(5.6875, 7.125, 15.9375, 6.8125, 8.25, 16),
              VoxelShapes.combineAndSimplify(Block.makeCuboidShape(7.25, 3, 10.5, 8.75, 4.5, 16),
                VoxelShapes.combineAndSimplify(Block.makeCuboidShape(5.6875, 8.25, 13.875, 6.8125, 9.375, 16),
                  VoxelShapes.combineAndSimplify(Block.makeCuboidShape(7.25, 4.5, 9, 8.75, 6, 16),
                    VoxelShapes.combineAndSimplify(Block.makeCuboidShape(5.6875, 9.375, 12.75, 6.8125, 10.5, 16),
                      VoxelShapes.combineAndSimplify(Block.makeCuboidShape(4.39063, 13.03125, 15.5625, 5.23438, 13.875, 16),
                        VoxelShapes.combineAndSimplify(Block.makeCuboidShape(7.25, 6, 7.5, 8.75, 7.5, 16),
                          VoxelShapes.combineAndSimplify(Block.makeCuboidShape(5.6875, 10.5, 11.625, 6.8125, 11.625, 16),
                            VoxelShapes.combineAndSimplify(Block.makeCuboidShape(4.39063, 13.875, 14.7187, 5.23438, 14.71875, 16),
                              VoxelShapes.combineAndSimplify(Block.makeCuboidShape(7.25, 7.5, 6, 8.75, 9, 16),
                                VoxelShapes.combineAndSimplify(Block.makeCuboidShape(5.6875, 11.625, 10.5, 6.8125, 12.75, 16),
                                  VoxelShapes.combineAndSimplify(Block.makeCuboidShape(4.39063, 14.71875, 13.875, 5.23438, 15.5625, 16),
                                    VoxelShapes.combineAndSimplify(Block.makeCuboidShape(7.25, 9, 4.5, 8.75, 10.5, 16),
                                      VoxelShapes.combineAndSimplify(Block.makeCuboidShape(5.6875, 12.75, 9.375, 6.8125, 13.875, 16),
                                        VoxelShapes.combineAndSimplify(Block.makeCuboidShape(4.39063, 15.5625, 13.0312, 5.23438, 16, 16),
                                          VoxelShapes.combineAndSimplify(Block.makeCuboidShape(7.25, 10.5, 3, 8.75, 12, 16),
                                            VoxelShapes.combineAndSimplify(Block.makeCuboidShape(5.6875, 13.875, 8.25, 6.8125, 15, 16),
                                              VoxelShapes.combineAndSimplify(Block.makeCuboidShape(7.25, 12, 1.5, 8.75, 16, 16),
                                                Block.makeCuboidShape(5.6875, 15, 7.125, 6.8125, 16, 16),
                                                IBooleanFunction.OR), IBooleanFunction.OR), IBooleanFunction.OR),
                                          IBooleanFunction.OR), IBooleanFunction.OR), IBooleanFunction.OR),
                                    IBooleanFunction.OR), IBooleanFunction.OR), IBooleanFunction.OR),
                              IBooleanFunction.OR), IBooleanFunction.OR), IBooleanFunction.OR),
                        IBooleanFunction.OR), IBooleanFunction.OR), IBooleanFunction.OR),
                  IBooleanFunction.OR), IBooleanFunction.OR), IBooleanFunction.OR),
            IBooleanFunction.OR));
        UltraSpacePortal.WORMHOLE_BOTTOM_RIGHT.put(Direction.SOUTH,
          VoxelShapes.combineAndSimplify(Block.makeCuboidShape(0, 1.5, 7.25, 2.75, 3, 8.75),
            VoxelShapes.combineAndSimplify(Block.makeCuboidShape(0, 7.125, 5.6875, 0.0625, 8.25, 6.8125),
              VoxelShapes.combineAndSimplify(Block.makeCuboidShape(0, 3, 7.25, 5.5, 4.5, 8.75),
                VoxelShapes.combineAndSimplify(Block.makeCuboidShape(0, 8.25, 5.6875, 2.125, 9.375, 6.8125),
                  VoxelShapes.combineAndSimplify(Block.makeCuboidShape(0, 4.5, 7.25, 7, 6, 8.75),
                    VoxelShapes.combineAndSimplify(Block.makeCuboidShape(0, 9.375, 5.6875, 3.25, 10.5, 6.8125),
                      VoxelShapes.combineAndSimplify(Block.makeCuboidShape(0, 13.03125, 4.39063, 0.4375, 13.875, 5.23438),
                        VoxelShapes.combineAndSimplify(Block.makeCuboidShape(0, 6, 7.25, 8.5, 7.5, 8.75),
                          VoxelShapes.combineAndSimplify(Block.makeCuboidShape(0, 10.5, 5.6875, 4.375, 11.625, 6.8125),
                            VoxelShapes.combineAndSimplify(Block.makeCuboidShape(0, 13.875, 4.39063, 1.2813, 14.71875, 5.23438),
                              VoxelShapes.combineAndSimplify(Block.makeCuboidShape(0, 7.5, 7.25, 10, 9, 8.75),
                                VoxelShapes.combineAndSimplify(Block.makeCuboidShape(0, 11.625, 5.6875, 5.5, 12.75, 6.8125),
                                  VoxelShapes.combineAndSimplify(Block.makeCuboidShape(0, 14.71875, 4.39063, 2.125, 15.5625, 5.23438),
                                    VoxelShapes.combineAndSimplify(Block.makeCuboidShape(0, 9, 7.25, 11.5, 10.5, 8.75),
                                      VoxelShapes.combineAndSimplify(Block.makeCuboidShape(0, 12.75, 5.6875, 6.625, 13.875, 6.8125),
                                        VoxelShapes.combineAndSimplify(Block.makeCuboidShape(0, 15.5625, 4.39063, 2.9688, 16, 5.23438),
                                          VoxelShapes.combineAndSimplify(Block.makeCuboidShape(0, 10.5, 7.25, 13, 12, 8.75),
                                            VoxelShapes.combineAndSimplify(Block.makeCuboidShape(0, 13.875, 5.6875, 7.75, 15, 6.8125),
                                              VoxelShapes.combineAndSimplify(Block.makeCuboidShape(0, 12, 7.25, 14.5, 16, 8.75),
                                                Block.makeCuboidShape(0, 15, 5.6875, 8.875, 16, 6.8125),
                                                IBooleanFunction.OR), IBooleanFunction.OR), IBooleanFunction.OR),
                                          IBooleanFunction.OR), IBooleanFunction.OR), IBooleanFunction.OR),
                                    IBooleanFunction.OR), IBooleanFunction.OR), IBooleanFunction.OR),
                              IBooleanFunction.OR), IBooleanFunction.OR), IBooleanFunction.OR),
                        IBooleanFunction.OR), IBooleanFunction.OR), IBooleanFunction.OR),
                  IBooleanFunction.OR), IBooleanFunction.OR), IBooleanFunction.OR),
            IBooleanFunction.OR));
        UltraSpacePortal.WORMHOLE_BOTTOM_RIGHT.put(Direction.WEST,
          VoxelShapes.combineAndSimplify(Block.makeCuboidShape(7.25, 1.5, 0, 8.75, 3, 2.75),
            VoxelShapes.combineAndSimplify(Block.makeCuboidShape(9.1875, 7.125, 0, 10.3125, 8.25, 0.0625),
              VoxelShapes.combineAndSimplify(Block.makeCuboidShape(7.25, 3, 0, 8.75, 4.5, 5.5),
                VoxelShapes.combineAndSimplify(Block.makeCuboidShape(9.1875, 8.25, 0, 10.3125, 9.375, 2.125),
                  VoxelShapes.combineAndSimplify(Block.makeCuboidShape(7.25, 4.5, 0, 8.75, 6, 7),
                    VoxelShapes.combineAndSimplify(Block.makeCuboidShape(9.1875, 9.375, 0, 10.3125, 10.5, 3.25),
                      VoxelShapes.combineAndSimplify(Block.makeCuboidShape(10.76562, 13.03125, 0, 11.60937, 13.875, 0.4375),
                        VoxelShapes.combineAndSimplify(Block.makeCuboidShape(7.25, 6, 0, 8.75, 7.5, 8.5),
                          VoxelShapes.combineAndSimplify(Block.makeCuboidShape(9.1875, 10.5, 0, 10.3125, 11.625, 4.375),
                            VoxelShapes.combineAndSimplify(Block.makeCuboidShape(10.76562, 13.875, 0, 11.60937, 14.71875, 1.2813),
                              VoxelShapes.combineAndSimplify(Block.makeCuboidShape(7.25, 7.5, 0, 8.75, 9, 10),
                                VoxelShapes.combineAndSimplify(Block.makeCuboidShape(9.1875, 11.625, 0, 10.3125, 12.75, 5.5),
                                  VoxelShapes.combineAndSimplify(Block.makeCuboidShape(10.76562, 14.71875, 0, 11.60937, 15.5625, 2.125),
                                    VoxelShapes.combineAndSimplify(Block.makeCuboidShape(7.25, 9, 0, 8.75, 10.5, 11.5),
                                      VoxelShapes.combineAndSimplify(Block.makeCuboidShape(9.1875, 12.75, 0, 10.3125, 13.875, 6.625),
                                        VoxelShapes.combineAndSimplify(Block.makeCuboidShape(10.76562, 15.5625, 0, 11.60937, 16, 2.9688),
                                          VoxelShapes.combineAndSimplify(Block.makeCuboidShape(7.25, 10.5, 0, 8.75, 12, 13),
                                            VoxelShapes.combineAndSimplify(Block.makeCuboidShape(9.1875, 13.875, 0, 10.3125, 15, 7.75),
                                              VoxelShapes.combineAndSimplify(Block.makeCuboidShape(7.25, 12, 0, 8.75, 16, 14.5),
                                                Block.makeCuboidShape(9.1875, 15, 0, 10.3125, 16, 8.875),
                                                IBooleanFunction.OR), IBooleanFunction.OR), IBooleanFunction.OR),
                                          IBooleanFunction.OR), IBooleanFunction.OR), IBooleanFunction.OR),
                                    IBooleanFunction.OR), IBooleanFunction.OR), IBooleanFunction.OR),
                              IBooleanFunction.OR), IBooleanFunction.OR), IBooleanFunction.OR),
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
        final UltraSpacePortalPart part = state.get(UltraSpacePortal.PART);
        if (part == UltraSpacePortalPart.BOTTOM) return UltraSpacePortal.WORMHOLE_BOTTOM.get(state.get(
                UltraSpacePortal.FACING));
        else if (part == UltraSpacePortalPart.BOTTOM_LEFT) return UltraSpacePortal.WORMHOLE_BOTTOM_LEFT.get(state.get(
                UltraSpacePortal.FACING));
        else if (part == UltraSpacePortalPart.BOTTOM_RIGHT) return UltraSpacePortal.WORMHOLE_BOTTOM_RIGHT.get(state.get(
                UltraSpacePortal.FACING));
        else if (part == UltraSpacePortalPart.MIDDLE) return UltraSpacePortal.WORMHOLE_MIDDLE.get(state.get(
                UltraSpacePortal.FACING));
        else if (part == UltraSpacePortalPart.MIDDLE_LEFT) return UltraSpacePortal.WORMHOLE_MIDDLE_LEFT.get(state.get(
                UltraSpacePortal.FACING));
        else if (part == UltraSpacePortalPart.MIDDLE_RIGHT) return UltraSpacePortal.WORMHOLE_MIDDLE_RIGHT.get(state.get(
                UltraSpacePortal.FACING));
        else if (part == UltraSpacePortalPart.TOP_LEFT) return UltraSpacePortal.WORMHOLE_TOP_LEFT.get(state.get(
                UltraSpacePortal.FACING));
        else if (part == UltraSpacePortalPart.TOP_RIGHT) return UltraSpacePortal.WORMHOLE_TOP_RIGHT.get(state.get(
                UltraSpacePortal.FACING));
        else return UltraSpacePortal.WORMHOLE_TOP.get(state.get(UltraSpacePortal.FACING));
    }

    public void place(final World world, final BlockPos pos, final Direction direction)
    {
        final BlockState state = this.getDefaultState().with(UltraSpacePortal.PART, UltraSpacePortalPart.BOTTOM).with(
                UltraSpacePortal.FACING, direction);
        world.setBlockState(pos, state, 3);
        this.place(world, pos, state, direction.getOpposite());
    }

    public void place(final World world, final BlockPos pos, final BlockState state, final Direction direction)
    {
        final BlockPos ultraSpacePortalTopLeftPos = this.getUltraSpacePortalTopLeftPos(pos, direction);
        final BlockPos ultraSpacePortalTopRightPos = this.getUltraSpacePortalTopRightPos(pos, direction);
        final BlockPos ultraSpacePortalMiddleLeftPos = this.getUltraSpacePortalMiddleLeftPos(pos, direction);
        final BlockPos ultraSpacePortalMiddleRightPos = this.getUltraSpacePortalMiddleRightPos(pos, direction);
        final BlockPos ultraSpacePortalBottomLeftPos = this.getUltraSpacePortalBottomLeftPos(pos, direction);
        final BlockPos ultraSpacePortalBottomRightPos = this.getUltraSpacePortalBottomRightPos(pos, direction);

        final IFluidState topFluidState = world.getFluidState(pos.up(2));
        final IFluidState topWestFluidState = world.getFluidState(pos.up(2).west());
        final IFluidState topEastFluidState = world.getFluidState(pos.up(2).east());
        final IFluidState middleFluidState = world.getFluidState(pos.up());
        final IFluidState middleWestFluidState = world.getFluidState(pos.up().west());
        final IFluidState middleEastFluidState = world.getFluidState(pos.up().east());
        final IFluidState bottomWestFluidState = world.getFluidState(pos.west());
        final IFluidState bottomEastFluidState = world.getFluidState(pos.east());

        world.setBlockState(ultraSpacePortalBottomLeftPos, state.with(UltraSpacePortal.PART,
                UltraSpacePortalPart.BOTTOM_LEFT).with(UltraSpacePortal.WATERLOGGED, bottomWestFluidState
                        .getFluid() == Fluids.WATER), 3);
        world.setBlockState(ultraSpacePortalBottomRightPos, state.with(UltraSpacePortal.PART,
                UltraSpacePortalPart.BOTTOM_RIGHT).with(UltraSpacePortal.WATERLOGGED, bottomEastFluidState
                        .getFluid() == Fluids.WATER), 3);
        world.setBlockState(pos.up(), state.with(UltraSpacePortal.PART, UltraSpacePortalPart.MIDDLE).with(
                UltraSpacePortal.WATERLOGGED, middleFluidState.getFluid() == Fluids.WATER), 3);
        world.setBlockState(ultraSpacePortalMiddleLeftPos, state.with(UltraSpacePortal.PART,
                UltraSpacePortalPart.MIDDLE_LEFT).with(UltraSpacePortal.WATERLOGGED, middleWestFluidState
                        .getFluid() == Fluids.WATER), 3);
        world.setBlockState(ultraSpacePortalMiddleRightPos, state.with(UltraSpacePortal.PART,
                UltraSpacePortalPart.MIDDLE_RIGHT).with(UltraSpacePortal.WATERLOGGED, middleEastFluidState
                        .getFluid() == Fluids.WATER), 3);
        world.setBlockState(pos.up(2), state.with(UltraSpacePortal.PART, UltraSpacePortalPart.TOP).with(
                UltraSpacePortal.WATERLOGGED, topFluidState.getFluid() == Fluids.WATER), 3);
        world.setBlockState(ultraSpacePortalTopLeftPos, state.with(UltraSpacePortal.PART, UltraSpacePortalPart.TOP_LEFT)
                .with(UltraSpacePortal.WATERLOGGED, topWestFluidState.getFluid() == Fluids.WATER), 3);
        world.setBlockState(ultraSpacePortalTopRightPos, state.with(UltraSpacePortal.PART,
                UltraSpacePortalPart.TOP_RIGHT).with(UltraSpacePortal.WATERLOGGED, topEastFluidState
                        .getFluid() == Fluids.WATER), 3);
    }

    // Places Ultra Space Portal with both top and bottom pieces
    @Override
    public void onBlockPlacedBy(final World world, final BlockPos pos, final BlockState state,
            @Nullable final LivingEntity entity, final ItemStack stack)
    {
        if (entity != null) this.place(world, pos, state, entity.getHorizontalFacing());
    }

    public void remove(final World world, final BlockPos pos, final BlockState state)
    {
        final Direction facing = state.get(UltraSpacePortal.FACING);

        final BlockPos ultraSpacePortalPos = this.getUltraSpacePortalPos(pos, state.get(UltraSpacePortal.PART), facing);
        BlockState UltraSpacePortalBlockState = world.getBlockState(ultraSpacePortalPos);
        if (UltraSpacePortalBlockState.getBlock() == this && !pos.equals(ultraSpacePortalPos)) this.removePart(world,
                ultraSpacePortalPos, UltraSpacePortalBlockState);

        BlockPos ultraSpacePortalPartPos = this.getUltraSpacePortalTopPos(ultraSpacePortalPos, facing);
        UltraSpacePortalBlockState = world.getBlockState(ultraSpacePortalPartPos);
        if (UltraSpacePortalBlockState.getBlock() == this && !pos.equals(ultraSpacePortalPartPos)) this.removePart(
                world, ultraSpacePortalPartPos, UltraSpacePortalBlockState);

        ultraSpacePortalPartPos = this.getUltraSpacePortalTopLeftPos(ultraSpacePortalPos, facing);
        UltraSpacePortalBlockState = world.getBlockState(ultraSpacePortalPartPos);
        if (UltraSpacePortalBlockState.getBlock() == this && !pos.equals(ultraSpacePortalPartPos)) this.removePart(
                world, ultraSpacePortalPartPos, UltraSpacePortalBlockState);

        ultraSpacePortalPartPos = this.getUltraSpacePortalTopRightPos(ultraSpacePortalPos, facing);
        UltraSpacePortalBlockState = world.getBlockState(ultraSpacePortalPartPos);
        if (UltraSpacePortalBlockState.getBlock() == this && !pos.equals(ultraSpacePortalPartPos)) this.removePart(
                world, ultraSpacePortalPartPos, UltraSpacePortalBlockState);

        ultraSpacePortalPartPos = this.getUltraSpacePortalMiddlePos(ultraSpacePortalPos, facing);
        UltraSpacePortalBlockState = world.getBlockState(ultraSpacePortalPartPos);
        if (UltraSpacePortalBlockState.getBlock() == this && !pos.equals(ultraSpacePortalPartPos)) this.removePart(
                world, ultraSpacePortalPartPos, UltraSpacePortalBlockState);

        ultraSpacePortalPartPos = this.getUltraSpacePortalMiddleLeftPos(ultraSpacePortalPos, facing);
        UltraSpacePortalBlockState = world.getBlockState(ultraSpacePortalPartPos);
        if (UltraSpacePortalBlockState.getBlock() == this && !pos.equals(ultraSpacePortalPartPos)) this.removePart(
                world, ultraSpacePortalPartPos, UltraSpacePortalBlockState);

        ultraSpacePortalPartPos = this.getUltraSpacePortalMiddleRightPos(ultraSpacePortalPos, facing);
        UltraSpacePortalBlockState = world.getBlockState(ultraSpacePortalPartPos);
        if (UltraSpacePortalBlockState.getBlock() == this && !pos.equals(ultraSpacePortalPartPos)) this.removePart(
                world, ultraSpacePortalPartPos, UltraSpacePortalBlockState);

        ultraSpacePortalPartPos = this.getUltraSpacePortalBottomLeftPos(ultraSpacePortalPos, facing);
        UltraSpacePortalBlockState = world.getBlockState(ultraSpacePortalPartPos);
        if (UltraSpacePortalBlockState.getBlock() == this && !pos.equals(ultraSpacePortalPartPos)) this.removePart(
                world, ultraSpacePortalPartPos, UltraSpacePortalBlockState);

        ultraSpacePortalPartPos = this.getUltraSpacePortalBottomRightPos(ultraSpacePortalPos, facing);
        UltraSpacePortalBlockState = world.getBlockState(ultraSpacePortalPartPos);
        if (UltraSpacePortalBlockState.getBlock() == this && !pos.equals(ultraSpacePortalPartPos)) this.removePart(
                world, ultraSpacePortalPartPos, UltraSpacePortalBlockState);
    }

    // Breaking Ultra Space Portal breaks both parts and returns one item only
    @Override
    public void onBlockHarvested(final World world, final BlockPos pos, final BlockState state,
            final PlayerEntity player)
    {
        this.remove(world, pos, state);
        super.onBlockHarvested(world, pos, state, player);
    }

    private BlockPos getUltraSpacePortalTopPos(final BlockPos base, final Direction facing)
    {
        switch (facing)
        {
        case NORTH:
            return base.up(2);
        case EAST:
            return base.up(2);
        case SOUTH:
            return base.up(2);
        case WEST:
            return base.up(2);
        default:
            return base.up(2);
        }
    }

    private BlockPos getUltraSpacePortalTopLeftPos(final BlockPos base, final Direction facing)
    {
        switch (facing)
        {
        case NORTH:
            return base.up(2).west();
        case EAST:
            return base.up(2).north();
        case SOUTH:
            return base.up(2).east();
        case WEST:
            return base.up(2).south();
        default:
            return base.up(2).east();
        }
    }

    private BlockPos getUltraSpacePortalTopRightPos(final BlockPos base, final Direction facing)
    {
        switch (facing)
        {
        case NORTH:
            return base.up(2).east();
        case EAST:
            return base.up(2).south();
        case SOUTH:
            return base.up(2).west();
        case WEST:
            return base.up(2).north();
        default:
            return base.up(2).west();
        }
    }

    private BlockPos getUltraSpacePortalMiddlePos(final BlockPos base, final Direction facing)
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

    private BlockPos getUltraSpacePortalMiddleLeftPos(final BlockPos base, final Direction facing)
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

    private BlockPos getUltraSpacePortalMiddleRightPos(final BlockPos base, final Direction facing)
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

    private BlockPos getUltraSpacePortalBottomLeftPos(final BlockPos base, final Direction facing)
    {
        switch (facing)
        {
        case NORTH:
            return base.west();
        case EAST:
            return base.north();
        case SOUTH:
            return base.east();
        case WEST:
            return base.south();
        default:
            return base.east();
        }
    }

    private BlockPos getUltraSpacePortalBottomRightPos(final BlockPos base, final Direction facing)
    {
        switch (facing)
        {
        case NORTH:
            return base.east();
        case EAST:
            return base.south();
        case SOUTH:
            return base.west();
        case WEST:
            return base.north();
        default:
            return base.west();
        }
    }

    private BlockPos getUltraSpacePortalPos(final BlockPos pos, final UltraSpacePortalPart part, final Direction facing)
    {
        if (part == UltraSpacePortalPart.BOTTOM) return pos;
        switch (facing)
        {
        case NORTH:
            switch (part)
            {
            case TOP:
                return pos.down(2);
            case TOP_LEFT:
                return pos.down(2).west();
            case TOP_RIGHT:
                return pos.down(2).east();
            case MIDDLE:
                return pos.down();
            case MIDDLE_LEFT:
                return pos.down().west();
            case MIDDLE_RIGHT:
                return pos.down().east();
            case BOTTOM_LEFT:
                return pos.west();
            case BOTTOM_RIGHT:
                return pos.east();
            default:
                return null;
            }
        case EAST:
            switch (part)
            {
            case TOP:
                return pos.down(2);
            case TOP_LEFT:
                return pos.down(2).north();
            case TOP_RIGHT:
                return pos.down(2).south();
            case MIDDLE:
                return pos.down();
            case MIDDLE_LEFT:
                return pos.down().north();
            case MIDDLE_RIGHT:
                return pos.down().south();
            case BOTTOM_LEFT:
                return pos.north();
            case BOTTOM_RIGHT:
                return pos.south();
            default:
                return null;
            }
        case SOUTH:
            switch (part)
            {
            case TOP:
                return pos.down(2);
            case TOP_LEFT:
                return pos.down(2).east();
            case TOP_RIGHT:
                return pos.down(2).west();
            case MIDDLE:
                return pos.down();
            case MIDDLE_LEFT:
                return pos.down().east();
            case MIDDLE_RIGHT:
                return pos.down().west();
            case BOTTOM_LEFT:
                return pos.east();
            case BOTTOM_RIGHT:
                return pos.west();
            default:
                return null;
            }
        case WEST:
            switch (part)
            {
            case TOP:
                return pos.down(2);
            case TOP_LEFT:
                return pos.down(2).south();
            case TOP_RIGHT:
                return pos.down(2).north();
            case MIDDLE:
                return pos.down();
            case MIDDLE_LEFT:
                return pos.down().south();
            case MIDDLE_RIGHT:
                return pos.down().north();
            case BOTTOM_LEFT:
                return pos.south();
            case BOTTOM_RIGHT:
                return pos.north();
            default:
                return null;
            }
        default:
            return null;
        }
    }

    // Breaking the Ultra Space Portal leaves water if underwater
    private void removePart(final World world, final BlockPos pos, final BlockState state)
    {
        final IFluidState fluidState = world.getFluidState(pos);
        if (fluidState.getFluid() == Fluids.WATER) world.setBlockState(pos, fluidState.getBlockState(), 35);
        else world.setBlockState(pos, Blocks.AIR.getDefaultState(), 35);
    }

    // Prevents the Ultra Space Portal from replacing blocks above it and checks
    // for water
    @Override
    public BlockState getStateForPlacement(final BlockItemUseContext context)
    {
        final IFluidState ifluidstate = context.getWorld().getFluidState(context.getPos());
        final BlockPos pos = context.getPos();

        final BlockPos ultraSpacePortalTopPos = this.getUltraSpacePortalTopPos(pos, context
                .getPlacementHorizontalFacing().getOpposite());
        final BlockPos ultraSpacePortalTopLeftPos = this.getUltraSpacePortalTopLeftPos(pos, context
                .getPlacementHorizontalFacing().getOpposite());
        final BlockPos ultraSpacePortalTopRightPos = this.getUltraSpacePortalTopRightPos(pos, context
                .getPlacementHorizontalFacing().getOpposite());

        final BlockPos ultraSpacePortalMiddlePos = this.getUltraSpacePortalMiddlePos(pos, context
                .getPlacementHorizontalFacing().getOpposite());
        final BlockPos ultraSpacePortalMiddleLeftPos = this.getUltraSpacePortalMiddleLeftPos(pos, context
                .getPlacementHorizontalFacing().getOpposite());
        final BlockPos ultraSpacePortalMiddleRightPos = this.getUltraSpacePortalMiddleRightPos(pos, context
                .getPlacementHorizontalFacing().getOpposite());

        final BlockPos ultraSpacePortalBottomLeftPos = this.getUltraSpacePortalBottomLeftPos(pos, context
                .getPlacementHorizontalFacing().getOpposite());
        final BlockPos ultraSpacePortalBottomRightPos = this.getUltraSpacePortalBottomRightPos(pos, context
                .getPlacementHorizontalFacing().getOpposite());

        if (pos.getY() < 255 && ultraSpacePortalTopPos.getY() < 255 && context.getWorld().getBlockState(pos.up(2))
                .isReplaceable(context) && ultraSpacePortalTopLeftPos.getY() < 255 && context.getWorld().getBlockState(
                        ultraSpacePortalTopLeftPos).isReplaceable(context) && ultraSpacePortalTopRightPos.getY() < 255
                && context.getWorld().getBlockState(ultraSpacePortalTopRightPos).isReplaceable(context)
                && ultraSpacePortalMiddlePos.getY() < 255 && context.getWorld().getBlockState(pos.up()).isReplaceable(
                        context) && ultraSpacePortalMiddleLeftPos.getY() < 255 && context.getWorld().getBlockState(
                                ultraSpacePortalMiddleLeftPos).isReplaceable(context) && ultraSpacePortalMiddleRightPos
                                        .getY() < 255 && context.getWorld().getBlockState(
                                                ultraSpacePortalMiddleRightPos).isReplaceable(context)
                && ultraSpacePortalBottomLeftPos.getY() < 255 && context.getWorld().getBlockState(
                        ultraSpacePortalBottomLeftPos).isReplaceable(context) && ultraSpacePortalBottomRightPos
                                .getY() < 255 && context.getWorld().getBlockState(ultraSpacePortalBottomRightPos)
                                        .isReplaceable(context)) return this.getDefaultState().with(
                                                UltraSpacePortal.FACING, context.getPlacementHorizontalFacing()
                                                        .getOpposite()).with(UltraSpacePortal.PART,
                                                                UltraSpacePortalPart.BOTTOM).with(
                                                                        UltraSpacePortal.WATERLOGGED, ifluidstate
                                                                                .isTagged(FluidTags.WATER)
                                                                                && ifluidstate.getLevel() == 8);
        return null;
    }

    @Override
    protected void fillStateContainer(final StateContainer.Builder<Block, BlockState> builder)
    {
        builder.add(UltraSpacePortal.PART, UltraSpacePortal.FACING, UltraSpacePortal.WATERLOGGED);
    }

    public UltraSpacePortal(final String name, final Properties props)
    {
        super(name, props.tickRandomly());
        this.setDefaultState(this.stateContainer.getBaseState().with(UltraSpacePortal.FACING, Direction.NORTH).with(
                UltraSpacePortal.WATERLOGGED, false).with(UltraSpacePortal.PART, UltraSpacePortalPart.BOTTOM));
    }

    // Time for Despawn
    @Override
    public int tickRate(final IWorldReader world)
    {
        return PokecubeLegends.config.portalDwellTime;
    }

    @Override
    public BlockBase setInfoBlockName(final String infoname)
    {
        this.infoname = infoname;
        return this;
    }

    @Override
    public BlockBase noInfoBlock()
    {
        this.hasTextInfo = false;
        return this;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void addInformation(final ItemStack stack, final IBlockReader worldIn, final List<ITextComponent> tooltip,
            final ITooltipFlag flagIn)
    {
        if (!this.hasTextInfo) return;
        String message;
        if (Screen.hasShiftDown()) message = I18n.format("legendblock." + this.infoname + ".tooltip");
        else message = I18n.format("pokecube.tooltip.advanced");
        tooltip.add(new TranslationTextComponent(message));
    }

    @Override
    public void randomTick(final BlockState state, final World worldIn, final BlockPos pos, final Random random)
    {
        // Only remove this if the nearby area is actually loaded.
        if (!TerrainManager.isAreaLoaded(worldIn, pos, 8)) return;

        this.remove(worldIn, pos, state);
        worldIn.setBlockState(pos, Blocks.AIR.getDefaultState());
        worldIn.playSound(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, SoundEvents.ENTITY_ENDERMAN_TELEPORT,
                SoundCategory.BLOCKS, 0.5F, random.nextFloat() * 0.4F + 0.8F, false);
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void animateTick(final BlockState stateIn, final World world, final BlockPos pos, final Random random)
    {
        if (random.nextInt(100) == 0) world.playSound(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                SoundEvents.AMBIENT_CAVE, SoundCategory.BLOCKS, 0.5F, random.nextFloat() * 0.4F + 0.8F, false);
    }

    @Override
    public void onEntityCollision(final BlockState state, final World worldIn, final BlockPos pos, final Entity entity)
    {
        if (!(entity instanceof ServerPlayerEntity)) return;
        if (entity.dimension == DimensionType.OVERWORLD) UltraSpaceModDimension.sentToUltraspace(
                (ServerPlayerEntity) entity);
        else if (entity.dimension == ModDimensions.DIMENSION_TYPE) UltraSpaceModDimension.sendToOverworld(
                (ServerPlayerEntity) entity);
    }

    @Override
    public boolean onBlockActivated(final BlockState state, final World world, final BlockPos pos,
            final PlayerEntity entity, final Hand hand, final BlockRayTraceResult hit)
    {
        final DimensionType dim = entity.dimension;
        if (!(entity instanceof ServerPlayerEntity)) return dim == DimensionType.OVERWORLD
                || dim == ModDimensions.DIMENSION_TYPE;
        if (dim == DimensionType.OVERWORLD)
        {
            UltraSpaceModDimension.sentToUltraspace((ServerPlayerEntity) entity);
            return true;
        }
        else if (dim == ModDimensions.DIMENSION_TYPE)
        {
            UltraSpaceModDimension.sendToOverworld((ServerPlayerEntity) entity);
            return true;
        }
        return false;
    }

    @Override
    public boolean canRenderInLayer(final BlockState state, final BlockRenderLayer layer)
    {
        return layer == BlockRenderLayer.CUTOUT;
    }
}
