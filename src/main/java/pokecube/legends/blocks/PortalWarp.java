package pokecube.legends.blocks;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.HorizontalBlock;
import net.minecraft.block.IWaterLoggable;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluids;
import net.minecraft.fluid.IFluidState;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.DirectionProperty;
import net.minecraft.state.EnumProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.shapes.IBooleanFunction;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import org.antlr.v4.runtime.misc.NotNull;
import javax.annotation.Nullable;
import pokecube.legends.PokecubeLegends;
import pokecube.legends.init.function.PortalActiveFunction;
import static net.minecraft.util.math.shapes.VoxelShapes.combineAndSimplify;

public class PortalWarp extends Rotates implements IWaterLoggable
{
    private static final EnumProperty<PortalWarpPart> PART = EnumProperty.create("part", PortalWarpPart.class);
    public static final BooleanProperty ACTIVE = BooleanProperty.create("active");
    private static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
    private static final DirectionProperty FACING = HorizontalBlock.HORIZONTAL_FACING;

    private static final Map<Direction, VoxelShape> PORTAL_TOP = new HashMap<>();
    private static final Map<Direction, VoxelShape> PORTAL_TOP_LEFT = new HashMap<>();
    private static final Map<Direction, VoxelShape> PORTAL_TOP_RIGHT = new HashMap<>();
    private static final Map<Direction, VoxelShape> PORTAL_MIDDLE = new HashMap<>();
    private static final Map<Direction, VoxelShape> PORTAL_MIDDLE_LEFT = new HashMap<>();
    private static final Map<Direction, VoxelShape> PORTAL_MIDDLE_RIGHT = new HashMap<>();
    private static final Map<Direction, VoxelShape> PORTAL_BOTTOM = new HashMap<>();
    private static final Map<Direction, VoxelShape> PORTAL_BOTTOM_LEFT = new HashMap<>();
    private static final Map<Direction, VoxelShape> PORTAL_BOTTOM_RIGHT = new HashMap<>();

    String infoname;

    //Precise selection box
    static {
        PORTAL_TOP.put(Direction.NORTH,
          makeCuboidShape(0, 0, 6.5, 16, 15, 9.5));
        PORTAL_TOP.put(Direction.EAST,
          makeCuboidShape(6.5, 0, 0, 9.5, 15, 16));
        PORTAL_TOP.put(Direction.SOUTH,
          makeCuboidShape(0, 0, 6.5, 16, 15, 9.5));
        PORTAL_TOP.put(Direction.WEST,
          makeCuboidShape(6.5, 0, 0, 9.5, 15, 16));

        PORTAL_TOP_LEFT.put(Direction.NORTH,
          combineAndSimplify(makeCuboidShape(0, 0, 6.5, 13.5, 3, 9.5),
            combineAndSimplify(makeCuboidShape(0, 3, 6.5, 12, 6, 9.5),
              combineAndSimplify(makeCuboidShape(0, 6, 6.5, 10.5, 7.5, 9.5),
                combineAndSimplify(makeCuboidShape(0, 7.5, 6.5, 9, 9, 9.5),
                  combineAndSimplify(makeCuboidShape(0, 9, 6.5, 7.5, 10.5, 9.5),
                    combineAndSimplify(makeCuboidShape(0, 10.5, 6.5, 6, 12, 9.5),
                      combineAndSimplify(makeCuboidShape(0, 12, 6.5, 3, 13.5, 9.5),
                        makeCuboidShape(0, 13.5, 6.5, 1.5, 15, 9.5),
              IBooleanFunction.OR), IBooleanFunction.OR), IBooleanFunction.OR),
                  IBooleanFunction.OR), IBooleanFunction.OR), IBooleanFunction.OR),
            IBooleanFunction.OR));
        PORTAL_TOP_LEFT.put(Direction.EAST,
          combineAndSimplify(makeCuboidShape(6.5, 0, 0, 9.5, 3, 13.5),
            combineAndSimplify(makeCuboidShape(6.5, 3, 0, 9.5, 6, 12),
              combineAndSimplify(makeCuboidShape(6.5, 6, 0, 9.5, 7.5, 10.5),
                combineAndSimplify(makeCuboidShape(6.5, 7.5, 0, 9.5, 9, 9),
                  combineAndSimplify(makeCuboidShape(6.5, 9, 0, 9.5, 10.5, 7.5),
                    combineAndSimplify(makeCuboidShape(6.5, 10.5, 0, 9.5, 12, 6),
                      combineAndSimplify(makeCuboidShape(6.5, 12, 0, 9.5, 13.5, 3),
                        makeCuboidShape(6.5, 13.5, 0, 9.5, 15, 1.5),
                        IBooleanFunction.OR), IBooleanFunction.OR), IBooleanFunction.OR),
                  IBooleanFunction.OR), IBooleanFunction.OR), IBooleanFunction.OR),
            IBooleanFunction.OR));
        PORTAL_TOP_LEFT.put(Direction.SOUTH,
          combineAndSimplify(makeCuboidShape(2.5, 0, 6.5, 16, 3, 9.5),
            combineAndSimplify(makeCuboidShape(4, 3, 6.5, 16, 6, 9.5),
              combineAndSimplify(makeCuboidShape(5.5, 6, 6.5, 16, 7.5, 9.5),
                combineAndSimplify(makeCuboidShape(7, 7.5, 6.5, 16, 9, 9.5),
                  combineAndSimplify(makeCuboidShape(8.5, 9, 6.5, 16, 10.5, 9.5),
                    combineAndSimplify(makeCuboidShape(10, 10.5, 6.5, 16, 12, 9.5),
                      combineAndSimplify(makeCuboidShape(13, 12, 6.5, 16, 13.5, 9.5),
                        makeCuboidShape(14.5, 13.5, 6.5, 16, 15, 9.5),
                        IBooleanFunction.OR), IBooleanFunction.OR), IBooleanFunction.OR),
                  IBooleanFunction.OR), IBooleanFunction.OR), IBooleanFunction.OR),
            IBooleanFunction.OR));
        PORTAL_TOP_LEFT.put(Direction.WEST,
          combineAndSimplify(makeCuboidShape(6.5, 0, 2.5, 9.5, 3, 16),
            combineAndSimplify(makeCuboidShape(6.5, 3, 4, 9.5, 6, 16),
              combineAndSimplify(makeCuboidShape(6.5, 6, 5.5, 9.5, 7.5, 16),
                combineAndSimplify(makeCuboidShape(6.5, 7.5, 7, 9.5, 9, 16),
                  combineAndSimplify(makeCuboidShape(6.5, 9, 8.5, 9.5, 10.5, 16),
                    combineAndSimplify(makeCuboidShape(6.5, 10.5, 10, 9.5, 12, 16),
                      combineAndSimplify(makeCuboidShape(6.5, 12, 13, 9.5, 13.5, 16),
                        makeCuboidShape(6.5, 13.5, 14.5, 9.5, 15, 16),
                        IBooleanFunction.OR), IBooleanFunction.OR), IBooleanFunction.OR),
                  IBooleanFunction.OR), IBooleanFunction.OR), IBooleanFunction.OR),
            IBooleanFunction.OR));

        PORTAL_TOP_RIGHT.put(Direction.NORTH,
          combineAndSimplify(makeCuboidShape(2.5, 0, 6.5, 16, 3, 9.5),
            combineAndSimplify(makeCuboidShape(4, 3, 6.5, 16, 6, 9.5),
              combineAndSimplify(makeCuboidShape(5.5, 6, 6.5, 16, 7.5, 9.5),
                combineAndSimplify(makeCuboidShape(7, 7.5, 6.5, 16, 9, 9.5),
                  combineAndSimplify(makeCuboidShape(8.5, 9, 6.5, 16, 10.5, 9.5),
                    combineAndSimplify(makeCuboidShape(10, 10.5, 6.5, 16, 12, 9.5),
                      combineAndSimplify(makeCuboidShape(13, 12, 6.5, 16, 13.5, 9.5),
                        makeCuboidShape(14.5, 13.5, 6.5, 16, 15, 9.5),
                        IBooleanFunction.OR), IBooleanFunction.OR), IBooleanFunction.OR),
                  IBooleanFunction.OR), IBooleanFunction.OR), IBooleanFunction.OR),
            IBooleanFunction.OR));
        PORTAL_TOP_RIGHT.put(Direction.EAST,
          combineAndSimplify(makeCuboidShape(6.5, 0, 2.5, 9.5, 3, 16),
            combineAndSimplify(makeCuboidShape(6.5, 3, 4, 9.5, 6, 16),
              combineAndSimplify(makeCuboidShape(6.5, 6, 5.5, 9.5, 7.5, 16),
                combineAndSimplify(makeCuboidShape(6.5, 7.5, 7, 9.5, 9, 16),
                  combineAndSimplify(makeCuboidShape(6.5, 9, 8.5, 9.5, 10.5, 16),
                    combineAndSimplify(makeCuboidShape(6.5, 10.5, 10, 9.5, 12, 16),
                      combineAndSimplify(makeCuboidShape(6.5, 12, 13, 9.5, 13.5, 16),
                        makeCuboidShape(6.5, 13.5, 14.5, 9.5, 15, 16),
                        IBooleanFunction.OR), IBooleanFunction.OR), IBooleanFunction.OR),
                  IBooleanFunction.OR), IBooleanFunction.OR), IBooleanFunction.OR),
            IBooleanFunction.OR));
        PORTAL_TOP_RIGHT.put(Direction.SOUTH,
          combineAndSimplify(makeCuboidShape(0, 0, 6.5, 13.5, 3, 9.5),
            combineAndSimplify(makeCuboidShape(0, 3, 6.5, 12, 6, 9.5),
              combineAndSimplify(makeCuboidShape(0, 6, 6.5, 10.5, 7.5, 9.5),
                combineAndSimplify(makeCuboidShape(0, 7.5, 6.5, 9, 9, 9.5),
                  combineAndSimplify(makeCuboidShape(0, 9, 6.5, 7.5, 10.5, 9.5),
                    combineAndSimplify(makeCuboidShape(0, 10.5, 6.5, 6, 12, 9.5),
                      combineAndSimplify(makeCuboidShape(0, 12, 6.5, 3, 13.5, 9.5),
                        makeCuboidShape(0, 13.5, 6.5, 1.5, 15, 9.5),
                        IBooleanFunction.OR), IBooleanFunction.OR), IBooleanFunction.OR),
                  IBooleanFunction.OR), IBooleanFunction.OR), IBooleanFunction.OR),
            IBooleanFunction.OR));
        PORTAL_TOP_RIGHT.put(Direction.WEST,
          combineAndSimplify(makeCuboidShape(6.5, 0, 0, 9.5, 3, 13.5),
            combineAndSimplify(makeCuboidShape(6.5, 3, 0, 9.5, 6, 12),
              combineAndSimplify(makeCuboidShape(6.5, 6, 0, 9.5, 7.5, 10.5),
                combineAndSimplify(makeCuboidShape(6.5, 7.5, 0, 9.5, 9, 9),
                  combineAndSimplify(makeCuboidShape(6.5, 9, 0, 9.5, 10.5, 7.5),
                    combineAndSimplify(makeCuboidShape(6.5, 10.5, 0, 9.5, 12, 6),
                      combineAndSimplify(makeCuboidShape(6.5, 12, 0, 9.5, 13.5, 3),
                        makeCuboidShape(6.5, 13.5, 0, 9.5, 15, 1.5),
                        IBooleanFunction.OR), IBooleanFunction.OR), IBooleanFunction.OR),
                  IBooleanFunction.OR), IBooleanFunction.OR), IBooleanFunction.OR),
            IBooleanFunction.OR));

        PORTAL_MIDDLE.put(Direction.NORTH,
          makeCuboidShape(0, 0, 6.5, 16, 16, 9.5));
        PORTAL_MIDDLE.put(Direction.EAST,
          makeCuboidShape(6.5, 0, 0, 9.5, 16, 16));
        PORTAL_MIDDLE.put(Direction.SOUTH,
          makeCuboidShape(0, 0, 6.5, 16, 16, 9.5));
        PORTAL_MIDDLE.put(Direction.WEST,
          makeCuboidShape(6.5, 0, 0, 9.5, 16, 16));

        PORTAL_MIDDLE_LEFT.put(Direction.NORTH,
          combineAndSimplify(makeCuboidShape(0, 0, 6.5, 15, 14.5, 9.5),
              makeCuboidShape(0, 14.5, 6.5, 13.5, 16, 9.5),
              IBooleanFunction.OR));
        PORTAL_MIDDLE_LEFT.put(Direction.EAST,
          combineAndSimplify(makeCuboidShape(6.5, 0, 0, 9.5, 14.5, 15),
            makeCuboidShape(6.5, 14.5, 0, 9.5, 16, 13.5),
            IBooleanFunction.OR));
        PORTAL_MIDDLE_LEFT.put(Direction.SOUTH,
          combineAndSimplify(makeCuboidShape(1, 0, 6.5, 16, 14.5, 9.5),
            makeCuboidShape(2.5, 14.5, 6.5, 16, 16, 9.5),
            IBooleanFunction.OR));
        PORTAL_MIDDLE_LEFT.put(Direction.WEST,
          combineAndSimplify(makeCuboidShape(6.5, 0, 1, 9.5, 14.5, 16),
            makeCuboidShape(6.5, 14.5, 2.5, 9.5, 16, 16),
            IBooleanFunction.OR));

        PORTAL_MIDDLE_RIGHT.put(Direction.NORTH,
          combineAndSimplify(makeCuboidShape(1, 0, 6.5, 16, 14.5, 9.5),
            makeCuboidShape(2.5, 14.5, 6.5, 16, 16, 9.5),
            IBooleanFunction.OR));
        PORTAL_MIDDLE_RIGHT.put(Direction.EAST,
          combineAndSimplify(makeCuboidShape(6.5, 0, 1, 9.5, 14.5, 16),
            makeCuboidShape(6.5, 14.5, 2.5, 9.5, 16, 16),
            IBooleanFunction.OR));
        PORTAL_MIDDLE_RIGHT.put(Direction.SOUTH,
          combineAndSimplify(makeCuboidShape(0, 0, 6.5, 15, 14.5, 9.5),
            makeCuboidShape(0, 14.5, 6.5, 13.5, 16, 9.5),
            IBooleanFunction.OR));
        PORTAL_MIDDLE_RIGHT.put(Direction.WEST,
          combineAndSimplify(makeCuboidShape(6.5, 0, 0, 9.5, 14.5, 15),
            makeCuboidShape(6.5, 14.5, 0, 9.5, 16, 13.5),
            IBooleanFunction.OR));

        PORTAL_BOTTOM.put(Direction.NORTH,
          makeCuboidShape(0, 0, 6.5, 16, 16, 9.5));
        PORTAL_BOTTOM.put(Direction.EAST,
          makeCuboidShape(6.5, 0, 0, 9.5, 16, 16));
        PORTAL_BOTTOM.put(Direction.SOUTH,
          makeCuboidShape(0, 0, 6.5, 16, 16, 9.5));
        PORTAL_BOTTOM.put(Direction.WEST,
          makeCuboidShape(6.5, 0, 0, 9.5, 16, 16));

        PORTAL_BOTTOM_LEFT.put(Direction.NORTH,
          combineAndSimplify(makeCuboidShape(0, 0, 6.5, 1.5, 1.5, 9.5),
            combineAndSimplify(makeCuboidShape(0, 1.5, 6.5, 3, 3, 9.5),
              combineAndSimplify(makeCuboidShape(0, 3, 6.5, 6, 4.5, 9.5),
                combineAndSimplify(makeCuboidShape(0, 4.5, 6.5, 7.5, 6, 9.5),
                  combineAndSimplify(makeCuboidShape(0, 6, 6.5, 9, 7.5, 9.5),
                    combineAndSimplify(makeCuboidShape(0, 7.5, 6.5, 10.5, 9, 9.5),
                      combineAndSimplify(makeCuboidShape(0, 9, 6.5, 12, 12, 9.5),
                        makeCuboidShape(0, 12, 6.5, 13.5, 16, 9.5),
                        IBooleanFunction.OR), IBooleanFunction.OR), IBooleanFunction.OR),
                  IBooleanFunction.OR), IBooleanFunction.OR), IBooleanFunction.OR),
            IBooleanFunction.OR));
        PORTAL_BOTTOM_LEFT.put(Direction.EAST,
          combineAndSimplify(makeCuboidShape(6.5, 0, 0, 9.5, 1.5, 1.5),
            combineAndSimplify(makeCuboidShape(6.5, 1.5, 0, 9.5, 3, 3),
              combineAndSimplify(makeCuboidShape(6.5, 3, 0, 9.5, 4.5, 6),
                combineAndSimplify(makeCuboidShape(6.5, 4.5, 0, 9.5, 6, 7.5),
                  combineAndSimplify(makeCuboidShape(6.5, 6, 0, 9.5, 7.5, 9),
                    combineAndSimplify(makeCuboidShape(6.5, 7.5, 0, 9.5, 9, 10.5),
                      combineAndSimplify(makeCuboidShape(6.5, 9, 0, 9.5, 12, 12),
                        makeCuboidShape(6.5, 12, 0, 9.5, 16, 13.5),
                        IBooleanFunction.OR), IBooleanFunction.OR), IBooleanFunction.OR),
                  IBooleanFunction.OR), IBooleanFunction.OR), IBooleanFunction.OR),
            IBooleanFunction.OR));
        PORTAL_BOTTOM_LEFT.put(Direction.SOUTH,
          combineAndSimplify(makeCuboidShape(14.5, 0, 6.5, 16, 1.5, 9.5),
            combineAndSimplify(makeCuboidShape(13, 1.5, 6.5, 16, 3, 9.5),
              combineAndSimplify(makeCuboidShape(10, 3, 6.5, 16, 4.5, 9.5),
                combineAndSimplify(makeCuboidShape(8.5, 4.5, 6.5, 16, 6, 9.5),
                  combineAndSimplify(makeCuboidShape(7, 6, 6.5, 16, 7.5, 9.5),
                    combineAndSimplify(makeCuboidShape(5.5, 7.5, 6.5, 16, 9, 9.5),
                      combineAndSimplify(makeCuboidShape(4, 9, 6.5, 16, 12, 9.5),
                        makeCuboidShape(2.5, 12, 6.5, 16, 16, 9.5),
                        IBooleanFunction.OR), IBooleanFunction.OR), IBooleanFunction.OR),
                  IBooleanFunction.OR), IBooleanFunction.OR), IBooleanFunction.OR),
            IBooleanFunction.OR));
        PORTAL_BOTTOM_LEFT.put(Direction.WEST,
          combineAndSimplify(makeCuboidShape(6.5, 0, 14.5, 9.5, 1.5, 16),
            combineAndSimplify(makeCuboidShape(6.5, 1.5, 13, 9.5, 3, 16),
              combineAndSimplify(makeCuboidShape(6.5, 3, 10, 9.5, 4.5, 16),
                combineAndSimplify(makeCuboidShape(6.5, 4.5, 8.5, 9.5, 6, 16),
                  combineAndSimplify(makeCuboidShape(6.5, 6, 7, 9.5, 7.5, 16),
                    combineAndSimplify(makeCuboidShape(6.5, 7.5, 5.5, 9.5, 9, 16),
                      combineAndSimplify(makeCuboidShape(6.5, 9, 4, 9.5, 12, 16),
                        makeCuboidShape(6.5, 12, 2.5, 9.5, 16, 16),
                        IBooleanFunction.OR), IBooleanFunction.OR), IBooleanFunction.OR),
                  IBooleanFunction.OR), IBooleanFunction.OR), IBooleanFunction.OR),
            IBooleanFunction.OR));

        PORTAL_BOTTOM_RIGHT.put(Direction.NORTH,
          combineAndSimplify(makeCuboidShape(14.5, 0, 6.5, 16, 1.5, 9.5),
            combineAndSimplify(makeCuboidShape(13, 1.5, 6.5, 16, 3, 9.5),
              combineAndSimplify(makeCuboidShape(10, 3, 6.5, 16, 4.5, 9.5),
                combineAndSimplify(makeCuboidShape(8.5, 4.5, 6.5, 16, 6, 9.5),
                  combineAndSimplify(makeCuboidShape(7, 6, 6.5, 16, 7.5, 9.5),
                    combineAndSimplify(makeCuboidShape(5.5, 7.5, 6.5, 16, 9, 9.5),
                      combineAndSimplify(makeCuboidShape(4, 9, 6.5, 16, 12, 9.5),
                        makeCuboidShape(2.5, 12, 6.5, 16, 16, 9.5),
                        IBooleanFunction.OR), IBooleanFunction.OR), IBooleanFunction.OR),
                  IBooleanFunction.OR), IBooleanFunction.OR), IBooleanFunction.OR),
            IBooleanFunction.OR));
        PORTAL_BOTTOM_RIGHT.put(Direction.EAST,
          combineAndSimplify(makeCuboidShape(6.5, 0, 14.5, 9.5, 1.5, 16),
            combineAndSimplify(makeCuboidShape(6.5, 1.5, 13, 9.5, 3, 16),
              combineAndSimplify(makeCuboidShape(6.5, 3, 10, 9.5, 4.5, 16),
                combineAndSimplify(makeCuboidShape(6.5, 4.5, 8.5, 9.5, 6, 16),
                  combineAndSimplify(makeCuboidShape(6.5, 6, 7, 9.5, 7.5, 16),
                    combineAndSimplify(makeCuboidShape(6.5, 7.5, 5.5, 9.5, 9, 16),
                      combineAndSimplify(makeCuboidShape(6.5, 9, 4, 9.5, 12, 16),
                        makeCuboidShape(6.5, 12, 2.5, 9.5, 16, 16),
                        IBooleanFunction.OR), IBooleanFunction.OR), IBooleanFunction.OR),
                  IBooleanFunction.OR), IBooleanFunction.OR), IBooleanFunction.OR),
            IBooleanFunction.OR));
        PORTAL_BOTTOM_RIGHT.put(Direction.SOUTH,
          combineAndSimplify(makeCuboidShape(0, 0, 6.5, 1.5, 1.5, 9.5),
            combineAndSimplify(makeCuboidShape(0, 1.5, 6.5, 3, 3, 9.5),
              combineAndSimplify(makeCuboidShape(0, 3, 6.5, 6, 4.5, 9.5),
                combineAndSimplify(makeCuboidShape(0, 4.5, 6.5, 7.5, 6, 9.5),
                  combineAndSimplify(makeCuboidShape(0, 6, 6.5, 9, 7.5, 9.5),
                    combineAndSimplify(makeCuboidShape(0, 7.5, 6.5, 10.5, 9, 9.5),
                      combineAndSimplify(makeCuboidShape(0, 9, 6.5, 12, 12, 9.5),
                        makeCuboidShape(0, 12, 6.5, 13.5, 16, 9.5),
                        IBooleanFunction.OR), IBooleanFunction.OR), IBooleanFunction.OR),
                  IBooleanFunction.OR), IBooleanFunction.OR), IBooleanFunction.OR),
            IBooleanFunction.OR));
        PORTAL_BOTTOM_RIGHT.put(Direction.WEST,
          combineAndSimplify(makeCuboidShape(6.5, 0, 0, 9.5, 1.5, 1.5),
            combineAndSimplify(makeCuboidShape(6.5, 1.5, 0, 9.5, 3, 3),
              combineAndSimplify(makeCuboidShape(6.5, 3, 0, 9.5, 4.5, 6),
                combineAndSimplify(makeCuboidShape(6.5, 4.5, 0, 9.5, 6, 7.5),
                  combineAndSimplify(makeCuboidShape(6.5, 6, 0, 9.5, 7.5, 9),
                    combineAndSimplify(makeCuboidShape(6.5, 7.5, 0, 9.5, 9, 10.5),
                      combineAndSimplify(makeCuboidShape(6.5, 9, 0, 9.5, 12, 12),
                        makeCuboidShape(6.5, 12, 0, 9.5, 16, 13.5),
                        IBooleanFunction.OR), IBooleanFunction.OR), IBooleanFunction.OR),
                  IBooleanFunction.OR), IBooleanFunction.OR), IBooleanFunction.OR),
            IBooleanFunction.OR));
    }

    //Precise selection box
    public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
        PortalWarpPart part = state.get(PART);
        if (part == PortalWarpPart.BOTTOM) {
            return PORTAL_BOTTOM.get(state.get(FACING));
        } else if (part == PortalWarpPart.BOTTOM_LEFT) {
            return PORTAL_BOTTOM_LEFT.get(state.get(FACING));
        } else if (part == PortalWarpPart.BOTTOM_RIGHT) {
            return PORTAL_BOTTOM_RIGHT.get(state.get(FACING));
        } else if (part == PortalWarpPart.MIDDLE) {
            return PORTAL_MIDDLE.get(state.get(FACING));
        } else if (part == PortalWarpPart.MIDDLE_LEFT) {
            return PORTAL_MIDDLE_LEFT.get(state.get(FACING));
        } else if (part == PortalWarpPart.MIDDLE_RIGHT) {
            return PORTAL_MIDDLE_RIGHT.get(state.get(FACING));
        } else if (part == PortalWarpPart.TOP_LEFT) {
            return PORTAL_TOP_LEFT.get(state.get(FACING));
        } else if (part == PortalWarpPart.TOP_RIGHT) {
            return PORTAL_TOP_RIGHT.get(state.get(FACING));
        } else {
            return PORTAL_TOP.get(state.get(FACING));
        }
    }

    public PortalWarp(final String name, final Properties props) {
        super(name, props.tickRandomly());
        this.setDefaultState(this.stateContainer.getBaseState()
          .with(FACING, Direction.NORTH)
          .with(WATERLOGGED, false)
          .with(PART, PortalWarpPart.BOTTOM)
          .with(PortalWarp.ACTIVE, true));
    }

    //Places Portal with both top and bottom pieces
    @Override
    public void onBlockPlacedBy(World world, BlockPos pos, BlockState state, @Nullable LivingEntity entity, ItemStack stack) {
        if (entity != null) {
            BlockPos portalWarpTopLeftPos = getPortalWarpTopLeftPos(pos, entity.getHorizontalFacing());
            BlockPos portalWarpTopRightPos = getPortalWarpTopRightPos(pos, entity.getHorizontalFacing());
            BlockPos portalWarpMiddleLeftPos = getPortalWarpMiddleLeftPos(pos, entity.getHorizontalFacing());
            BlockPos portalWarpMiddleRightPos = getPortalWarpMiddleRightPos(pos, entity.getHorizontalFacing());
            BlockPos portalWarpBottomLeftPos = getPortalWarpBottomLeftPos(pos, entity.getHorizontalFacing());
            BlockPos portalWarpBottomRightPos = getPortalWarpBottomRightPos(pos, entity.getHorizontalFacing());

            IFluidState topFluidState = world.getFluidState(pos.up(2));
            IFluidState topWestFluidState = world.getFluidState(pos.up(2).west());
            IFluidState topEastFluidState = world.getFluidState(pos.up(2).east());
            IFluidState middleFluidState = world.getFluidState(pos.up());
            IFluidState middleWestFluidState = world.getFluidState(pos.up().west());
            IFluidState middleEastFluidState = world.getFluidState(pos.up().east());
            IFluidState bottomWestFluidState = world.getFluidState(pos.west());
            IFluidState bottomEastFluidState = world.getFluidState(pos.east());

            world.setBlockState(portalWarpBottomLeftPos,
              state.with(PART, PortalWarpPart.BOTTOM_LEFT)
                .with(WATERLOGGED, bottomWestFluidState.getFluid() == Fluids.WATER), 3);
            world.setBlockState(portalWarpBottomRightPos,
              state.with(PART, PortalWarpPart.BOTTOM_RIGHT)
                .with(WATERLOGGED, bottomEastFluidState.getFluid() == Fluids.WATER), 3);
            world.setBlockState(pos.up(),
              state.with(PART, PortalWarpPart.MIDDLE)
                .with(WATERLOGGED, middleFluidState.getFluid() == Fluids.WATER), 3);
            world.setBlockState(portalWarpMiddleLeftPos,
              state.with(PART, PortalWarpPart.MIDDLE_LEFT)
                .with(WATERLOGGED, middleWestFluidState.getFluid() == Fluids.WATER), 3);
            world.setBlockState(portalWarpMiddleRightPos,
              state.with(PART, PortalWarpPart.MIDDLE_RIGHT)
                .with(WATERLOGGED, middleEastFluidState.getFluid() == Fluids.WATER), 3);
            world.setBlockState(pos.up(2),
              state.with(PART, PortalWarpPart.TOP)
                .with(WATERLOGGED, topFluidState.getFluid() == Fluids.WATER), 3);
            world.setBlockState(portalWarpTopLeftPos,
              state.with(PART, PortalWarpPart.TOP_LEFT)
                .with(WATERLOGGED, topWestFluidState.getFluid() == Fluids.WATER), 3);
            world.setBlockState(portalWarpTopRightPos,
              state.with(PART, PortalWarpPart.TOP_RIGHT)
                .with(WATERLOGGED, topEastFluidState.getFluid() == Fluids.WATER), 3);
        }
    }

    //Breaking Portal breaks both parts and returns one item only
    public void onBlockHarvested(World world, @NotNull BlockPos pos, BlockState state, @NotNull PlayerEntity player) {
        Direction facing = state.get(FACING);

        BlockPos portalWarpPos = getPortalWarpPos(pos, state.get(PART), facing);
        BlockState PortalWarpBlockState = world.getBlockState(portalWarpPos);
        if (PortalWarpBlockState.getBlock() == this && !pos.equals(portalWarpPos)) {
            removePart(world, portalWarpPos, PortalWarpBlockState);
        }

        BlockPos portalWarpPartPos = getPortalWarpTopPos(portalWarpPos, facing);
        PortalWarpBlockState = world.getBlockState(portalWarpPartPos);
        if (PortalWarpBlockState.getBlock() == this && !pos.equals(portalWarpPartPos)) {
            removePart(world, portalWarpPartPos, PortalWarpBlockState);
        }

        portalWarpPartPos = getPortalWarpTopLeftPos(portalWarpPos, facing);
        PortalWarpBlockState = world.getBlockState(portalWarpPartPos);
        if (PortalWarpBlockState.getBlock() == this && !pos.equals(portalWarpPartPos)) {
            removePart(world, portalWarpPartPos, PortalWarpBlockState);
        }

        portalWarpPartPos = getPortalWarpTopRightPos(portalWarpPos, facing);
        PortalWarpBlockState = world.getBlockState(portalWarpPartPos);
        if (PortalWarpBlockState.getBlock() == this && !pos.equals(portalWarpPartPos)) {
            removePart(world, portalWarpPartPos, PortalWarpBlockState);
        }

        portalWarpPartPos = getPortalWarpMiddlePos(portalWarpPos, facing);
        PortalWarpBlockState = world.getBlockState(portalWarpPartPos);
        if (PortalWarpBlockState.getBlock() == this && !pos.equals(portalWarpPartPos)) {
            removePart(world, portalWarpPartPos, PortalWarpBlockState);
        }

        portalWarpPartPos = getPortalWarpMiddleLeftPos(portalWarpPos, facing);
        PortalWarpBlockState = world.getBlockState(portalWarpPartPos);
        if (PortalWarpBlockState.getBlock() == this && !pos.equals(portalWarpPartPos)) {
            removePart(world, portalWarpPartPos, PortalWarpBlockState);
        }

        portalWarpPartPos = getPortalWarpMiddleRightPos(portalWarpPos, facing);
        PortalWarpBlockState = world.getBlockState(portalWarpPartPos);
        if (PortalWarpBlockState.getBlock() == this && !pos.equals(portalWarpPartPos)) {
            removePart(world, portalWarpPartPos, PortalWarpBlockState);
        }

        portalWarpPartPos = getPortalWarpBottomLeftPos(portalWarpPos, facing);
        PortalWarpBlockState = world.getBlockState(portalWarpPartPos);
        if (PortalWarpBlockState.getBlock() == this && !pos.equals(portalWarpPartPos)) {
            removePart(world, portalWarpPartPos, PortalWarpBlockState);
        }

        portalWarpPartPos = getPortalWarpBottomRightPos(portalWarpPos, facing);
        PortalWarpBlockState = world.getBlockState(portalWarpPartPos);
        if (PortalWarpBlockState.getBlock() == this && !pos.equals(portalWarpPartPos)) {
            removePart(world, portalWarpPartPos, PortalWarpBlockState);
        }
        super.onBlockHarvested(world, pos, state, player);
    }

    private BlockPos getPortalWarpTopPos(BlockPos base, Direction facing) {
        switch (facing) {
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

    private BlockPos getPortalWarpTopLeftPos(BlockPos base, Direction facing) {
        switch (facing) {
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

    private BlockPos getPortalWarpTopRightPos(BlockPos base, Direction facing) {
        switch (facing) {
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

    private BlockPos getPortalWarpMiddlePos(BlockPos base, Direction facing) {
        switch (facing) {
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

    private BlockPos getPortalWarpMiddleLeftPos(BlockPos base, Direction facing) {
        switch (facing) {
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

    private BlockPos getPortalWarpMiddleRightPos(BlockPos base, Direction facing) {
        switch (facing) {
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

    private BlockPos getPortalWarpBottomLeftPos(BlockPos base, Direction facing) {
        switch (facing) {
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

    private BlockPos getPortalWarpBottomRightPos(BlockPos base, Direction facing) {
        switch (facing) {
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

    private BlockPos getPortalWarpPos(BlockPos pos, PortalWarpPart part, Direction facing) {
        if (part == PortalWarpPart.BOTTOM) return pos;
        switch (facing) {
            case NORTH:
                switch (part) {
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
                switch (part) {
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
                switch (part) {
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
                switch (part) {
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

    //Breaking the Portal leaves water if underwater
    private void removePart(World world, BlockPos pos, BlockState state) {
        IFluidState fluidState = world.getFluidState(pos);
        if (fluidState.getFluid() == Fluids.WATER) {
            world.setBlockState(pos, fluidState.getBlockState(), 35);
        } else {
            world.setBlockState(pos, Blocks.AIR.getDefaultState(), 35);
        }
    }

    //Prevents the Portal from replacing blocks above it and checks for water
    @Override
    public BlockState getStateForPlacement(BlockItemUseContext context) {
        final IFluidState ifluidstate = context.getWorld().getFluidState(context.getPos());
        final BlockPos pos = context.getPos();

        final BlockPos portalWarpTopPos = getPortalWarpTopPos(pos, context.getPlacementHorizontalFacing().getOpposite());
        final BlockPos portalWarpTopLeftPos = getPortalWarpTopLeftPos(pos, context.getPlacementHorizontalFacing().getOpposite());
        final BlockPos portalWarpTopRightPos = getPortalWarpTopRightPos(pos, context.getPlacementHorizontalFacing().getOpposite());

        final BlockPos portalWarpMiddlePos = getPortalWarpMiddlePos(pos, context.getPlacementHorizontalFacing().getOpposite());
        final BlockPos portalWarpMiddleLeftPos = getPortalWarpMiddleLeftPos(pos, context.getPlacementHorizontalFacing().getOpposite());
        final BlockPos portalWarpMiddleRightPos = getPortalWarpMiddleRightPos(pos, context.getPlacementHorizontalFacing().getOpposite());

        final BlockPos portalWarpBottomLeftPos = getPortalWarpBottomLeftPos(pos, context.getPlacementHorizontalFacing().getOpposite());
        final BlockPos portalWarpBottomRightPos = getPortalWarpBottomRightPos(pos, context.getPlacementHorizontalFacing().getOpposite());

        if (pos.getY() < 255 &&
          portalWarpTopPos.getY() < 255 &&
          context.getWorld().getBlockState(pos.up(2)).isReplaceable(context) &&
          portalWarpTopLeftPos.getY() < 255 &&
          context.getWorld().getBlockState(portalWarpTopLeftPos).isReplaceable(context) &&
          portalWarpTopRightPos.getY() < 255 &&
          context.getWorld().getBlockState(portalWarpTopRightPos).isReplaceable(context) &&
          portalWarpMiddlePos.getY() < 255 &&
          context.getWorld().getBlockState(pos.up()).isReplaceable(context) &&
          portalWarpMiddleLeftPos.getY() < 255 &&
          context.getWorld().getBlockState(portalWarpMiddleLeftPos).isReplaceable(context) &&
          portalWarpMiddleRightPos.getY() < 255 &&
          context.getWorld().getBlockState(portalWarpMiddleRightPos).isReplaceable(context) &&
          portalWarpBottomLeftPos.getY() < 255 &&
          context.getWorld().getBlockState(portalWarpBottomLeftPos).isReplaceable(context) &&
          portalWarpBottomRightPos.getY() < 255 &&
          context.getWorld().getBlockState(portalWarpBottomRightPos).isReplaceable(context))
            return this.getDefaultState()
              .with(FACING, context.getPlacementHorizontalFacing().getOpposite())
              .with(PART, PortalWarpPart.BOTTOM)
              .with(WATERLOGGED, ifluidstate.isTagged(FluidTags.WATER) && ifluidstate.getLevel() == 8)
              .with(PortalWarp.ACTIVE, true);
        return null;
    }

    @Override
    protected void fillStateContainer(final StateContainer.Builder<Block, BlockState> builder) {
        builder.add(PART, FACING, WATERLOGGED, PortalWarp.ACTIVE);
    }

    // time for spawn
    @Override
    public int tickRate(final IWorldReader world) {
        return PokecubeLegends.config.ticksPerMirageSpawn;
    }

    @Override
    public BlockBase setInfoBlockName(final String infoname) {
        this.infoname = infoname;
        return this;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void addInformation(final ItemStack stack, final IBlockReader worldIn, final List<ITextComponent> tooltip,
                               final ITooltipFlag flagIn) {
        String message;
        if (Screen.hasShiftDown())
            message = I18n.format("legendblock." + this.infoname + ".tooltip");
        else message = I18n.format("pokecube.tooltip.advanced");
        tooltip.add(new TranslationTextComponent(message));
    }

    public void randomTick(final BlockState state, final World worldIn, final BlockPos pos, final Random random)
    {
        //PortalWarpPart part = state.get(PART);
        if (state.get(PortalWarp.ACTIVE)) return;
        worldIn.setBlockState(pos, state.with(PortalWarp.ACTIVE, true));
    }

    @Override
    public boolean onBlockActivated(final BlockState state, final World worldIn, final BlockPos pos,
                                    final PlayerEntity entity, final Hand hand, final BlockRayTraceResult hit)
    {
        PortalWarpPart part = state.get(PART);
        if (!state.get(PortalWarp.ACTIVE)) return false;
        if (worldIn instanceof ServerWorld && part == PortalWarpPart.MIDDLE)
            PortalActiveFunction.executeProcedure(pos, state, (ServerWorld) worldIn);
        return true;
    }


    @OnlyIn(Dist.CLIENT)
    @Override
    public void animateTick(final BlockState state, final World world, final BlockPos pos, final Random random)
    {
        super.animateTick(state, world, pos, random);
        final int x = pos.getX();
        final int y = pos.getY();
        final int z = pos.getZ();
        for (int l = 0; l < 4; ++l) {
            final double d0 = x + random.nextFloat();
            final double d1 = y + random.nextFloat();
            final double d2 = z + random.nextFloat();
            final double d3 = (random.nextFloat() - 0.5D) * 0.6;
            final double d4 = (random.nextFloat() - 0.5D) * 0.6;
            final double d5 = (random.nextFloat() - 0.5D) * 0.6;
            world.addParticle(ParticleTypes.PORTAL, d0, d1, d2, d3, d4, d5);
        }
    }

    @Override
    public boolean canRenderInLayer(final BlockState state, final BlockRenderLayer layer) {
        return layer == BlockRenderLayer.CUTOUT;
    }
}
