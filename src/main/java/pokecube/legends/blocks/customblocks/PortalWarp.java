package pokecube.legends.blocks.customblocks;

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
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.DirectionProperty;
import net.minecraft.state.EnumProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tags.FluidTags;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.shapes.IBooleanFunction;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import pokecube.core.PokecubeCore;
import pokecube.legends.blocks.BlockBase;
import pokecube.legends.init.function.PortalActiveFunction;
import pokecube.legends.tileentity.RingTile;

public class PortalWarp extends Rotates implements IWaterLoggable
{
    public static final EnumProperty<PortalWarpPart> PART        = EnumProperty.create("part", PortalWarpPart.class);
    public static final BooleanProperty              ACTIVE      = BooleanProperty.create("active");
    private static final BooleanProperty             WATERLOGGED = BlockStateProperties.WATERLOGGED;
    private static final DirectionProperty           FACING      = HorizontalBlock.HORIZONTAL_FACING;

    private static final Map<Direction, VoxelShape> PORTAL_TOP              = new HashMap<>();
    private static final Map<Direction, VoxelShape> PORTAL_TOP_LEFT         = new HashMap<>();
    private static final Map<Direction, VoxelShape> PORTAL_TOP_RIGHT        = new HashMap<>();
    private static final Map<Direction, VoxelShape> PORTAL_MIDDLE           = new HashMap<>();
    private static final Map<Direction, VoxelShape> PORTAL_MIDDLE_LEFT      = new HashMap<>();
    private static final Map<Direction, VoxelShape> PORTAL_MIDDLE_RIGHT     = new HashMap<>();
    private static final Map<Direction, VoxelShape> PORTAL_BOTTOM           = new HashMap<>();
    private static final Map<Direction, VoxelShape> PORTAL_BOTTOM_LEFT      = new HashMap<>();
    private static final Map<Direction, VoxelShape> PORTAL_BOTTOM_RIGHT     = new HashMap<>();
    private static final Map<Direction, VoxelShape> PORTAL_TOP_OFF          = new HashMap<>();
    private static final Map<Direction, VoxelShape> PORTAL_TOP_LEFT_OFF     = new HashMap<>();
    private static final Map<Direction, VoxelShape> PORTAL_TOP_RIGHT_OFF    = new HashMap<>();
    private static final Map<Direction, VoxelShape> PORTAL_MIDDLE_OFF       = new HashMap<>();
    private static final Map<Direction, VoxelShape> PORTAL_MIDDLE_LEFT_OFF  = new HashMap<>();
    private static final Map<Direction, VoxelShape> PORTAL_MIDDLE_RIGHT_OFF = new HashMap<>();
    private static final Map<Direction, VoxelShape> PORTAL_BOTTOM_OFF       = new HashMap<>();
    private static final Map<Direction, VoxelShape> PORTAL_BOTTOM_LEFT_OFF  = new HashMap<>();
    private static final Map<Direction, VoxelShape> PORTAL_BOTTOM_RIGHT_OFF = new HashMap<>();

    String infoname;

    // Precise selection box
    static
    {
        PortalWarp.PORTAL_TOP.put(Direction.NORTH, Block.makeCuboidShape(0, 0, 6.5, 16, 16, 9.5));
        PortalWarp.PORTAL_TOP.put(Direction.EAST, Block.makeCuboidShape(6.5, 0, 0, 9.5, 16, 16));
        PortalWarp.PORTAL_TOP.put(Direction.SOUTH, Block.makeCuboidShape(0, 0, 6.5, 16, 16, 9.5));
        PortalWarp.PORTAL_TOP.put(Direction.WEST, Block.makeCuboidShape(6.5, 0, 0, 9.5, 16, 16));

        //@formatter:off
        PortalWarp.PORTAL_TOP_OFF.put(Direction.NORTH,
          VoxelShapes.combineAndSimplify(Block.makeCuboidShape(0, 7.5, 6.5, 3, 9, 9.5),
            VoxelShapes.combineAndSimplify(Block.makeCuboidShape(13, 7.5, 6.5, 16, 9, 9.5),
              Block.makeCuboidShape(0, 9, 6.5, 16, 16, 9.5),
              IBooleanFunction.OR), IBooleanFunction.OR));
        PortalWarp.PORTAL_TOP_OFF.put(Direction.EAST,
          VoxelShapes.combineAndSimplify(Block.makeCuboidShape(6.5, 7.5, 0, 9.5, 9, 3),
            VoxelShapes.combineAndSimplify(Block.makeCuboidShape(6.5, 7.5, 13, 9.5, 9, 16),
              Block.makeCuboidShape(6.5, 9, 0, 9.5, 16, 16),
              IBooleanFunction.OR), IBooleanFunction.OR));
        PortalWarp.PORTAL_TOP_OFF.put(Direction.SOUTH,
          VoxelShapes.combineAndSimplify(Block.makeCuboidShape(0, 7.5, 6.5, 3, 9, 9.5),
            VoxelShapes.combineAndSimplify(Block.makeCuboidShape(13, 7.5, 6.5, 16, 9, 9.5),
              Block.makeCuboidShape(0, 9, 6.5, 16, 16, 9.5),
              IBooleanFunction.OR), IBooleanFunction.OR));
        PortalWarp.PORTAL_TOP_OFF.put(Direction.WEST,
          VoxelShapes.combineAndSimplify(Block.makeCuboidShape(6.5, 7.5, 0, 9.5, 9, 3),
            VoxelShapes.combineAndSimplify(Block.makeCuboidShape(6.5, 7.5, 13, 9.5, 9, 16),
              Block.makeCuboidShape(6.5, 9, 0, 9.5, 16, 16),
              IBooleanFunction.OR), IBooleanFunction.OR));

        PortalWarp.PORTAL_TOP_LEFT.put(Direction.NORTH,
          VoxelShapes.combineAndSimplify(Block.makeCuboidShape(0, 0, 6.5, 13.5, 4.5, 9.5),
            VoxelShapes.combineAndSimplify(Block.makeCuboidShape(0, 4.5, 6.5, 12, 7.5, 9.5),
              VoxelShapes.combineAndSimplify(Block.makeCuboidShape(0, 7.5, 6.5, 10.5, 9, 9.5),
                VoxelShapes.combineAndSimplify(Block.makeCuboidShape(0, 9, 6.5, 9, 10.5, 9.5),
                  VoxelShapes.combineAndSimplify(Block.makeCuboidShape(0, 10.5, 6.5, 7.5, 12, 9.5),
                    VoxelShapes.combineAndSimplify(Block.makeCuboidShape(0, 12, 6.5, 6, 13.5, 9.5),
                      VoxelShapes.combineAndSimplify(Block.makeCuboidShape(0, 13.5, 6.5, 4.5, 14.75, 9.5),
                        Block.makeCuboidShape(0, 14.75, 6.5, 1.5, 16, 9.5),
                        IBooleanFunction.OR), IBooleanFunction.OR), IBooleanFunction.OR),
                  IBooleanFunction.OR), IBooleanFunction.OR), IBooleanFunction.OR),
            IBooleanFunction.OR));
        PortalWarp.PORTAL_TOP_LEFT.put(Direction.EAST,
          VoxelShapes.combineAndSimplify(Block.makeCuboidShape(6.5, 0, 0, 9.5, 4.5, 13.5),
            VoxelShapes.combineAndSimplify(Block.makeCuboidShape(6.5, 4.5, 0, 9.5, 7.5, 12),
              VoxelShapes.combineAndSimplify(Block.makeCuboidShape(6.5, 7.5, 0, 9.5, 9, 10.5),
                VoxelShapes.combineAndSimplify(Block.makeCuboidShape(6.5, 9, 0, 9.5, 10.5, 9),
                  VoxelShapes.combineAndSimplify(Block.makeCuboidShape(6.5, 10.5, 0, 9.5, 12, 7.5),
                    VoxelShapes.combineAndSimplify(Block.makeCuboidShape(6.5, 12, 0, 9.5, 13.5, 6),
                      VoxelShapes.combineAndSimplify(Block.makeCuboidShape(6.5, 13.5, 0, 9.5, 14.75, 4.5),
                        Block.makeCuboidShape(6.5, 14.75, 0, 9.5, 16, 1.5),
                        IBooleanFunction.OR), IBooleanFunction.OR), IBooleanFunction.OR),
                  IBooleanFunction.OR), IBooleanFunction.OR), IBooleanFunction.OR),
            IBooleanFunction.OR));
        PortalWarp.PORTAL_TOP_LEFT.put(Direction.SOUTH,
          VoxelShapes.combineAndSimplify(Block.makeCuboidShape(2.5, 0, 6.5, 16, 4.5, 9.5),
            VoxelShapes.combineAndSimplify(Block.makeCuboidShape(4, 4.5, 6.5, 16, 7.5, 9.5),
              VoxelShapes.combineAndSimplify(Block.makeCuboidShape(5.5, 7.5, 6.5, 16, 9, 9.5),
                VoxelShapes.combineAndSimplify(Block.makeCuboidShape(7, 9, 6.5, 16, 10.5, 9.5),
                  VoxelShapes.combineAndSimplify(Block.makeCuboidShape(8.5, 10.5, 6.5, 16, 12, 9.5),
                    VoxelShapes.combineAndSimplify(Block.makeCuboidShape(10, 12, 6.5, 16, 13.5, 9.5),
                      VoxelShapes.combineAndSimplify(Block.makeCuboidShape(11.5, 13.5, 6.5, 16, 14.75, 9.5),
                        Block.makeCuboidShape(14.5, 14.75, 6.5, 16, 16, 9.5),
                        IBooleanFunction.OR), IBooleanFunction.OR), IBooleanFunction.OR),
                  IBooleanFunction.OR), IBooleanFunction.OR), IBooleanFunction.OR),
            IBooleanFunction.OR));
        PortalWarp.PORTAL_TOP_LEFT.put(Direction.WEST,
          VoxelShapes.combineAndSimplify(Block.makeCuboidShape(6.5, 0, 2.5, 9.5, 4.5, 16),
            VoxelShapes.combineAndSimplify(Block.makeCuboidShape(6.5, 4.5, 4, 9.5, 7.5, 16),
              VoxelShapes.combineAndSimplify(Block.makeCuboidShape(6.5, 7.5, 5.5, 9.5, 9, 16),
                VoxelShapes.combineAndSimplify(Block.makeCuboidShape(6.5, 9, 7, 9.5, 10.5, 16),
                  VoxelShapes.combineAndSimplify(Block.makeCuboidShape(6.5, 10.5, 8.5, 9.5, 12, 16),
                    VoxelShapes.combineAndSimplify(Block.makeCuboidShape(6.5, 12, 10, 9.5, 13.5, 16),
                      VoxelShapes.combineAndSimplify(Block.makeCuboidShape(6.5, 13.5, 11.5, 9.5, 14.75, 16),
                        Block.makeCuboidShape(6.5, 14.75, 14.5, 9.5, 16, 16),
                        IBooleanFunction.OR), IBooleanFunction.OR), IBooleanFunction.OR),
                  IBooleanFunction.OR), IBooleanFunction.OR), IBooleanFunction.OR),
            IBooleanFunction.OR));

        PortalWarp.PORTAL_TOP_LEFT_OFF.put(Direction.NORTH,
          VoxelShapes.combineAndSimplify(Block.makeCuboidShape(4.5, 1.5, 6.5, 13.5, 4.5, 9.5),
            VoxelShapes.combineAndSimplify(Block.makeCuboidShape(6, 0, 6.5, 13.5, 1.5, 9.5),
              VoxelShapes.combineAndSimplify(Block.makeCuboidShape(0, 6, 6.5, 12, 7.5, 9.5),
                VoxelShapes.combineAndSimplify(Block.makeCuboidShape(3, 4.5, 6.5, 12, 6, 9.5),
                  VoxelShapes.combineAndSimplify(Block.makeCuboidShape(0, 7.5, 6.5, 10.5, 9, 9.5),
                    VoxelShapes.combineAndSimplify(Block.makeCuboidShape(0, 9, 6.5, 9, 10.5, 9.5),
                      VoxelShapes.combineAndSimplify(Block.makeCuboidShape(0, 10.5, 6.5, 7.5, 12, 9.5),
                        VoxelShapes.combineAndSimplify(Block.makeCuboidShape(0, 12, 6.5, 6, 13.5, 9.5),
                          VoxelShapes.combineAndSimplify(Block.makeCuboidShape(0, 13.5, 6.5, 4.5, 14.75, 9.5),
                            Block.makeCuboidShape(0, 14.75, 6.5, 1.5, 16, 9.5),
                            IBooleanFunction.OR), IBooleanFunction.OR), IBooleanFunction.OR),
                      IBooleanFunction.OR), IBooleanFunction.OR), IBooleanFunction.OR),
                IBooleanFunction.OR), IBooleanFunction.OR), IBooleanFunction.OR));
        PortalWarp.PORTAL_TOP_LEFT_OFF.put(Direction.EAST,
          VoxelShapes.combineAndSimplify(Block.makeCuboidShape(6.5, 1.5, 4.5, 9.5, 4.5, 13.5),
            VoxelShapes.combineAndSimplify(Block.makeCuboidShape(6.5, 0, 6, 9.5, 1.5, 13.5),
              VoxelShapes.combineAndSimplify(Block.makeCuboidShape(6.5, 6, 0, 9.5, 7.5, 12),
                VoxelShapes.combineAndSimplify(Block.makeCuboidShape(6.5, 4.5, 3, 9.5, 6, 12),
                  VoxelShapes.combineAndSimplify(Block.makeCuboidShape(6.5, 7.5, 0, 9.5, 9, 10.5),
                    VoxelShapes.combineAndSimplify(Block.makeCuboidShape(6.5, 9, 0, 9.5, 10.5, 9),
                      VoxelShapes.combineAndSimplify(Block.makeCuboidShape(6.5, 10.5, 0, 9.5, 12, 7.5),
                        VoxelShapes.combineAndSimplify(Block.makeCuboidShape(6.5, 12, 0, 9.5, 13.5, 6),
                          VoxelShapes.combineAndSimplify(Block.makeCuboidShape(6.5, 13.5, 0, 9.5, 14.75, 4.5),
                            Block.makeCuboidShape(6.5, 14.75, 0, 9.5, 16, 1.5),
                            IBooleanFunction.OR), IBooleanFunction.OR), IBooleanFunction.OR),
                      IBooleanFunction.OR), IBooleanFunction.OR), IBooleanFunction.OR),
                IBooleanFunction.OR), IBooleanFunction.OR), IBooleanFunction.OR));
        PortalWarp.PORTAL_TOP_LEFT_OFF.put(Direction.SOUTH,
          VoxelShapes.combineAndSimplify(Block.makeCuboidShape(2.5, 1.5, 6.5, 11.5, 4.5, 9.5),
            VoxelShapes.combineAndSimplify(Block.makeCuboidShape(2.5, 0, 6.5, 10, 1.5, 9.5),
              VoxelShapes.combineAndSimplify(Block.makeCuboidShape(4, 6, 6.5, 16, 7.5, 9.5),
                VoxelShapes.combineAndSimplify(Block.makeCuboidShape(4, 4.5, 6.5, 13, 6, 9.5),
                  VoxelShapes.combineAndSimplify(Block.makeCuboidShape(5.5, 7.5, 6.5, 16, 9, 9.5),
                    VoxelShapes.combineAndSimplify(Block.makeCuboidShape(7, 9, 6.5, 16, 10.5, 9.5),
                      VoxelShapes.combineAndSimplify(Block.makeCuboidShape(8.5, 10.5, 6.5, 16, 12, 9.5),
                        VoxelShapes.combineAndSimplify(Block.makeCuboidShape(10, 12, 6.5, 16, 13.5, 9.5),
                          VoxelShapes.combineAndSimplify(Block.makeCuboidShape(11.5, 13.5, 6.5, 16, 14.75, 9.5),
                            Block.makeCuboidShape(14.5, 14.75, 6.5, 16, 16, 9.5),
                            IBooleanFunction.OR), IBooleanFunction.OR), IBooleanFunction.OR),
                      IBooleanFunction.OR), IBooleanFunction.OR), IBooleanFunction.OR),
                IBooleanFunction.OR), IBooleanFunction.OR), IBooleanFunction.OR));
        PortalWarp.PORTAL_TOP_LEFT_OFF.put(Direction.WEST,
          VoxelShapes.combineAndSimplify(Block.makeCuboidShape(6.5, 1.5, 2.5, 9.5, 4.5, 11.5),
            VoxelShapes.combineAndSimplify(Block.makeCuboidShape(6.5, 0, 2.5, 9.5, 1.5, 10),
              VoxelShapes.combineAndSimplify(Block.makeCuboidShape(6.5, 6, 4, 9.5, 7.5, 16),
                VoxelShapes.combineAndSimplify(Block.makeCuboidShape(6.5, 4.5, 4, 9.5, 6, 13),
                  VoxelShapes.combineAndSimplify(Block.makeCuboidShape(6.5, 7.5, 5.5, 9.5, 9, 16),
                    VoxelShapes.combineAndSimplify(Block.makeCuboidShape(6.5, 9, 7, 9.5, 10.5, 16),
                      VoxelShapes.combineAndSimplify(Block.makeCuboidShape(6.5, 10.5, 8.5, 9.5, 12, 16),
                        VoxelShapes.combineAndSimplify(Block.makeCuboidShape(6.5, 12, 10, 9.5, 13.5, 16),
                          VoxelShapes.combineAndSimplify(Block.makeCuboidShape(6.5, 13.5, 11.5, 9.5, 14.75, 16),
                            Block.makeCuboidShape(6.5, 14.75, 14.5, 9.5, 16, 16),
                            IBooleanFunction.OR), IBooleanFunction.OR), IBooleanFunction.OR),
                      IBooleanFunction.OR), IBooleanFunction.OR), IBooleanFunction.OR),
                IBooleanFunction.OR), IBooleanFunction.OR), IBooleanFunction.OR));

        PortalWarp.PORTAL_TOP_RIGHT.put(Direction.NORTH,
          VoxelShapes.combineAndSimplify(Block.makeCuboidShape(1.5, 0, 6.5, 16, 4.5, 9.5),
            VoxelShapes.combineAndSimplify(Block.makeCuboidShape(3, 4.5, 6.5, 16, 7.5, 9.5),
              VoxelShapes.combineAndSimplify(Block.makeCuboidShape(4.5, 7.5, 6.5, 16, 9, 9.5),
                VoxelShapes.combineAndSimplify(Block.makeCuboidShape(6, 9, 6.5, 16, 10.5, 9.5),
                  VoxelShapes.combineAndSimplify(Block.makeCuboidShape(7.5, 10.5, 6.5, 16, 12, 9.5),
                    VoxelShapes.combineAndSimplify(Block.makeCuboidShape(9, 12, 6.5, 16, 13.5, 9.5),
                      VoxelShapes.combineAndSimplify(Block.makeCuboidShape(11.75, 13.5, 6.5, 16, 14.75, 9.5),
                        Block.makeCuboidShape(14.5, 14.75, 6.5, 16, 16, 9.5),
                        IBooleanFunction.OR), IBooleanFunction.OR), IBooleanFunction.OR),
                  IBooleanFunction.OR), IBooleanFunction.OR), IBooleanFunction.OR),
            IBooleanFunction.OR));
        PortalWarp.PORTAL_TOP_RIGHT.put(Direction.EAST,
          VoxelShapes.combineAndSimplify(Block.makeCuboidShape(6.5, 0, 1.5, 9.5, 4.5, 16),
            VoxelShapes.combineAndSimplify(Block.makeCuboidShape(6.5, 4.5, 3, 9.5, 7.5, 16),
              VoxelShapes.combineAndSimplify(Block.makeCuboidShape(6.5, 7.5, 4.5, 9.5, 9, 16),
                VoxelShapes.combineAndSimplify(Block.makeCuboidShape(6.5, 9, 6, 9.5, 10.5, 16),
                  VoxelShapes.combineAndSimplify(Block.makeCuboidShape(6.5, 10.5, 7.5, 9.5, 12, 16),
                    VoxelShapes.combineAndSimplify(Block.makeCuboidShape(6.5, 12, 9, 9.5, 13.5, 16),
                      VoxelShapes.combineAndSimplify(Block.makeCuboidShape(6.5, 13.5, 11.75, 9.5, 14.75, 16),
                        Block.makeCuboidShape(6.5, 14.75, 14.5, 9.5, 16, 16),
                        IBooleanFunction.OR), IBooleanFunction.OR), IBooleanFunction.OR),
                  IBooleanFunction.OR), IBooleanFunction.OR), IBooleanFunction.OR),
            IBooleanFunction.OR));
        PortalWarp.PORTAL_TOP_RIGHT.put(Direction.SOUTH,
          VoxelShapes.combineAndSimplify(Block.makeCuboidShape(0, 0, 6.5, 14.5, 4.5, 9.5),
            VoxelShapes.combineAndSimplify(Block.makeCuboidShape(0, 4.5, 6.5, 13, 7.5, 9.5),
              VoxelShapes.combineAndSimplify(Block.makeCuboidShape(0, 7.5, 6.5, 11.5, 9, 9.5),
                VoxelShapes.combineAndSimplify(Block.makeCuboidShape(0, 9, 6.5, 10, 10.5, 9.5),
                  VoxelShapes.combineAndSimplify(Block.makeCuboidShape(0, 10.5, 6.5, 8.5, 12, 9.5),
                    VoxelShapes.combineAndSimplify(Block.makeCuboidShape(0, 12, 6.5, 7, 13.5, 9.5),
                      VoxelShapes.combineAndSimplify(Block.makeCuboidShape(0, 13.5, 6.5, 4.25, 14.75, 9.5),
                        Block.makeCuboidShape(0, 14.75, 6.5, 1.5, 16, 9.5),
                        IBooleanFunction.OR), IBooleanFunction.OR), IBooleanFunction.OR),
                  IBooleanFunction.OR), IBooleanFunction.OR), IBooleanFunction.OR),
            IBooleanFunction.OR));
        PortalWarp.PORTAL_TOP_RIGHT.put(Direction.WEST,
          VoxelShapes.combineAndSimplify(Block.makeCuboidShape(6.5, 0, 0, 9.5, 4.5, 14.5),
            VoxelShapes.combineAndSimplify(Block.makeCuboidShape(6.5, 4.5, 0, 9.5, 7.5, 13),
              VoxelShapes.combineAndSimplify(Block.makeCuboidShape(6.5, 7.5, 0, 9.5, 9, 11.5),
                VoxelShapes.combineAndSimplify(Block.makeCuboidShape(6.5, 9, 0, 9.5, 10.5, 10),
                  VoxelShapes.combineAndSimplify(Block.makeCuboidShape(6.5, 10.5, 0, 9.5, 12, 8.5),
                    VoxelShapes.combineAndSimplify(Block.makeCuboidShape(6.5, 12, 0, 9.5, 13.5, 7),
                      VoxelShapes.combineAndSimplify(Block.makeCuboidShape(6.5, 13.5, 0, 9.5, 14.75, 4.25),
                        Block.makeCuboidShape(6.5, 14.75, 0, 9.5, 16, 1.5),
                        IBooleanFunction.OR), IBooleanFunction.OR), IBooleanFunction.OR),
                  IBooleanFunction.OR), IBooleanFunction.OR), IBooleanFunction.OR),
            IBooleanFunction.OR));

        PortalWarp.PORTAL_TOP_RIGHT_OFF.put(Direction.NORTH,
          VoxelShapes.combineAndSimplify(Block.makeCuboidShape(1.5, 1.5, 6.5, 11.5, 4.5, 9.5),
            VoxelShapes.combineAndSimplify(Block.makeCuboidShape(1.5, 0, 6.5, 10, 1.5, 9.5),
              VoxelShapes.combineAndSimplify(Block.makeCuboidShape(3, 6, 6.5, 16, 7.5, 9.5),
                VoxelShapes.combineAndSimplify(Block.makeCuboidShape(3, 4.5, 6.5, 13, 6, 9.5),
                  VoxelShapes.combineAndSimplify(Block.makeCuboidShape(4.5, 7.5, 6.5, 16, 9, 9.5),
                    VoxelShapes.combineAndSimplify(Block.makeCuboidShape(6, 9, 6.5, 16, 10.5, 9.5),
                      VoxelShapes.combineAndSimplify(Block.makeCuboidShape(7.5, 10.5, 6.5, 16, 12, 9.5),
                        VoxelShapes.combineAndSimplify(Block.makeCuboidShape(9, 12, 6.5, 16, 13.5, 9.5),
                          VoxelShapes.combineAndSimplify(Block.makeCuboidShape(11.75, 13.5, 6.5, 16, 14.75, 9.5),
                            Block.makeCuboidShape(14.5, 14.75, 6.5, 16, 16, 9.5),
                            IBooleanFunction.OR), IBooleanFunction.OR), IBooleanFunction.OR),
                      IBooleanFunction.OR), IBooleanFunction.OR), IBooleanFunction.OR),
                IBooleanFunction.OR), IBooleanFunction.OR), IBooleanFunction.OR));
        PortalWarp.PORTAL_TOP_RIGHT_OFF.put(Direction.EAST,
          VoxelShapes.combineAndSimplify(Block.makeCuboidShape(6.5, 1.5, 1.5, 9.5, 4.5, 11.5),
            VoxelShapes.combineAndSimplify(Block.makeCuboidShape(6.5, 0, 1.5, 9.5, 1.5, 10),
              VoxelShapes.combineAndSimplify(Block.makeCuboidShape(6.5, 6, 3, 9.5, 7.5, 16),
                VoxelShapes.combineAndSimplify(Block.makeCuboidShape(6.5, 4.5, 3, 9.5, 6, 13),
                  VoxelShapes.combineAndSimplify(Block.makeCuboidShape(6.5, 7.5, 4.5, 9.5, 9, 16),
                    VoxelShapes.combineAndSimplify(Block.makeCuboidShape(6.5, 9, 6, 9.5, 10.5, 16),
                      VoxelShapes.combineAndSimplify(Block.makeCuboidShape(6.5, 10.5, 7.5, 9.5, 12, 16),
                        VoxelShapes.combineAndSimplify(Block.makeCuboidShape(6.5, 12, 9, 9.5, 13.5, 16),
                          VoxelShapes.combineAndSimplify(Block.makeCuboidShape(6.5, 13.5, 11.75, 9.5, 14.75, 16),
                            Block.makeCuboidShape(6.5, 14.75, 14.5, 9.5, 16, 16),
                            IBooleanFunction.OR), IBooleanFunction.OR), IBooleanFunction.OR),
                      IBooleanFunction.OR), IBooleanFunction.OR), IBooleanFunction.OR),
                IBooleanFunction.OR), IBooleanFunction.OR), IBooleanFunction.OR));
        PortalWarp.PORTAL_TOP_RIGHT_OFF.put(Direction.SOUTH,
          VoxelShapes.combineAndSimplify(Block.makeCuboidShape(4.5, 1.5, 6.5, 14.5, 4.5, 9.5),
            VoxelShapes.combineAndSimplify(Block.makeCuboidShape(6, 0, 6.5, 14.5, 1.5, 9.5),
              VoxelShapes.combineAndSimplify(Block.makeCuboidShape(0, 6, 6.5, 13, 7.5, 9.5),
                VoxelShapes.combineAndSimplify(Block.makeCuboidShape(3, 4.5, 6.5, 13, 6, 9.5),
                  VoxelShapes.combineAndSimplify(Block.makeCuboidShape(0, 7.5, 6.5, 11.5, 9, 9.5),
                    VoxelShapes.combineAndSimplify(Block.makeCuboidShape(0, 9, 6.5, 10, 10.5, 9.5),
                      VoxelShapes.combineAndSimplify(Block.makeCuboidShape(0, 10.5, 6.5, 8.5, 12, 9.5),
                        VoxelShapes.combineAndSimplify(Block.makeCuboidShape(0, 12, 6.5, 7, 13.5, 9.5),
                          VoxelShapes.combineAndSimplify(Block.makeCuboidShape(0, 13.5, 6.5, 4.25, 14.75, 9.5),
                            Block.makeCuboidShape(0, 14.75, 6.5, 1.5, 16, 9.5),
                            IBooleanFunction.OR), IBooleanFunction.OR), IBooleanFunction.OR),
                      IBooleanFunction.OR), IBooleanFunction.OR), IBooleanFunction.OR),
                IBooleanFunction.OR), IBooleanFunction.OR), IBooleanFunction.OR));
        PortalWarp.PORTAL_TOP_RIGHT_OFF.put(Direction.WEST,
          VoxelShapes.combineAndSimplify(Block.makeCuboidShape(6.5, 1.5, 4.5, 9.5, 4.5, 14.5),
            VoxelShapes.combineAndSimplify(Block.makeCuboidShape(6.5, 0, 6, 9.5, 1.5, 14.5),
              VoxelShapes.combineAndSimplify(Block.makeCuboidShape(6.5, 6, 0, 9.5, 7.5, 13),
                VoxelShapes.combineAndSimplify(Block.makeCuboidShape(6.5, 4.5, 3, 9.5, 6, 13),
                  VoxelShapes.combineAndSimplify(Block.makeCuboidShape(6.5, 7.5, 0, 9.5, 9, 11.5),
                    VoxelShapes.combineAndSimplify(Block.makeCuboidShape(6.5, 9, 0, 9.5, 10.5, 10),
                      VoxelShapes.combineAndSimplify(Block.makeCuboidShape(6.5, 10.5, 0, 9.5, 12, 8.5),
                        VoxelShapes.combineAndSimplify(Block.makeCuboidShape(6.5, 12, 0, 9.5, 13.5, 7),
                          VoxelShapes.combineAndSimplify(Block.makeCuboidShape(6.5, 13.5, 0, 9.5, 14.75, 4.25),
                            Block.makeCuboidShape(6.5, 14.75, 0, 9.5, 16, 1.5),
                            IBooleanFunction.OR), IBooleanFunction.OR), IBooleanFunction.OR),
                      IBooleanFunction.OR), IBooleanFunction.OR), IBooleanFunction.OR),
                IBooleanFunction.OR), IBooleanFunction.OR), IBooleanFunction.OR));

        PortalWarp.PORTAL_MIDDLE.put(Direction.NORTH,
          Block.makeCuboidShape(0, 0, 6.5, 16, 16, 9.5));
        PortalWarp.PORTAL_MIDDLE.put(Direction.EAST,
          Block.makeCuboidShape(6.5, 0, 0, 9.5, 16, 16));
        PortalWarp.PORTAL_MIDDLE.put(Direction.SOUTH,
          Block.makeCuboidShape(0, 0, 6.5, 16, 16, 9.5));
        PortalWarp.PORTAL_MIDDLE.put(Direction.WEST,
          Block.makeCuboidShape(6.5, 0, 0, 9.5, 16, 16));

        PortalWarp.PORTAL_MIDDLE_OFF.put(Direction.NORTH,
          Block.makeCuboidShape(0, 0, 0, 0, 0, 0));
        PortalWarp.PORTAL_MIDDLE_OFF.put(Direction.EAST,
          Block.makeCuboidShape(0, 0, 0, 0, 0, 0));
        PortalWarp.PORTAL_MIDDLE_OFF.put(Direction.SOUTH,
          Block.makeCuboidShape(0, 0, 0, 0, 0, 0));
        PortalWarp.PORTAL_MIDDLE_OFF.put(Direction.WEST,
          Block.makeCuboidShape(0, 0, 0, 0, 0, 0));

        PortalWarp.PORTAL_MIDDLE_LEFT.put(Direction.NORTH,
          VoxelShapes.combineAndSimplify(Block.makeCuboidShape(0, 1.5, 6.5, 15, 16, 9.5),
            Block.makeCuboidShape(0, 0, 6.5, 13.5, 1.5, 9.5),
            IBooleanFunction.OR));
        PortalWarp.PORTAL_MIDDLE_LEFT.put(Direction.EAST,
          VoxelShapes.combineAndSimplify(Block.makeCuboidShape(6.5, 1.5, 0, 9.5, 16, 15),
            Block.makeCuboidShape(6.5, 0, 0, 9.5, 1.5, 13.5),
            IBooleanFunction.OR));
        PortalWarp.PORTAL_MIDDLE_LEFT.put(Direction.SOUTH,
          VoxelShapes.combineAndSimplify(Block.makeCuboidShape(1, 1.5, 6.5, 16, 16, 9.5),
            Block.makeCuboidShape(2.5, 0, 6.5, 16, 1.5, 9.5),
            IBooleanFunction.OR));
        PortalWarp.PORTAL_MIDDLE_LEFT.put(Direction.WEST,
          VoxelShapes.combineAndSimplify(Block.makeCuboidShape(6.5, 1.5, 1, 9.5, 16, 16),
            Block.makeCuboidShape(6.5, 0, 2.5, 9.5, 1.5, 16),
            IBooleanFunction.OR));

        PortalWarp.PORTAL_MIDDLE_LEFT_OFF.put(Direction.NORTH,
          VoxelShapes.combineAndSimplify(Block.makeCuboidShape(7.5, 3, 6.5, 15, 14.5, 9.5),
            VoxelShapes.combineAndSimplify(Block.makeCuboidShape(6, 0, 6.5, 13.5, 1.5, 9.5),
              VoxelShapes.combineAndSimplify(Block.makeCuboidShape(6, 1.5, 6.5, 15, 3, 9.5),
                Block.makeCuboidShape(6, 14.5, 6.5, 15, 16, 9.5),
                IBooleanFunction.OR), IBooleanFunction.OR), IBooleanFunction.OR));
        PortalWarp.PORTAL_MIDDLE_LEFT_OFF.put(Direction.EAST,
          VoxelShapes.combineAndSimplify(Block.makeCuboidShape(6.5, 3, 7.5, 9.5, 14.5, 15),
            VoxelShapes.combineAndSimplify(Block.makeCuboidShape(6.5, 0, 6, 9.5, 1.5, 13.5),
              VoxelShapes.combineAndSimplify(Block.makeCuboidShape(6.5, 1.5, 6, 9.5, 3, 15),
                Block.makeCuboidShape(6.5, 14.5, 6, 9.5, 16, 15),
                IBooleanFunction.OR), IBooleanFunction.OR), IBooleanFunction.OR));
        PortalWarp.PORTAL_MIDDLE_LEFT_OFF.put(Direction.SOUTH,
          VoxelShapes.combineAndSimplify(Block.makeCuboidShape(1, 3, 6.5, 8.5, 14.5, 9.5),
            VoxelShapes.combineAndSimplify(Block.makeCuboidShape(2.5, 0, 6.5, 10, 1.5, 9.5),
              VoxelShapes.combineAndSimplify(Block.makeCuboidShape(1, 1.5, 6.5, 10, 3, 9.5),
                Block.makeCuboidShape(1, 14.5, 6.5, 10, 16, 9.5),
                IBooleanFunction.OR), IBooleanFunction.OR), IBooleanFunction.OR));
        PortalWarp.PORTAL_MIDDLE_LEFT_OFF.put(Direction.WEST,
          VoxelShapes.combineAndSimplify(Block.makeCuboidShape(6.5, 3, 1, 9.5, 14.5, 8.5),
            VoxelShapes.combineAndSimplify(Block.makeCuboidShape(6.5, 0, 2.5, 9.5, 1.5, 10),
              VoxelShapes.combineAndSimplify(Block.makeCuboidShape(6.5, 1.5, 1, 9.5, 3, 10),
                Block.makeCuboidShape(6.5, 14.5, 1, 9.5, 16, 10),
                IBooleanFunction.OR), IBooleanFunction.OR), IBooleanFunction.OR));

        PortalWarp.PORTAL_MIDDLE_RIGHT.put(Direction.NORTH,
          VoxelShapes.combineAndSimplify(Block.makeCuboidShape(0, 1.5, 6.5, 16, 16, 9.5),
            Block.makeCuboidShape(1.5, 0, 6.5, 16, 1.5, 9.5),
            IBooleanFunction.OR));
        PortalWarp.PORTAL_MIDDLE_RIGHT.put(Direction.EAST,
          VoxelShapes.combineAndSimplify(Block.makeCuboidShape(6.5, 1.5, 0, 9.5, 16, 16),
            Block.makeCuboidShape(6.5, 0, 1.5, 9.5, 1.5, 16),
            IBooleanFunction.OR));
        PortalWarp.PORTAL_MIDDLE_RIGHT.put(Direction.SOUTH,
          VoxelShapes.combineAndSimplify(Block.makeCuboidShape(0, 1.5, 6.5, 16, 16, 9.5),
            Block.makeCuboidShape(0, 0, 6.5, 14.5, 1.5, 9.5),
            IBooleanFunction.OR));
        PortalWarp.PORTAL_MIDDLE_RIGHT.put(Direction.WEST,
          VoxelShapes.combineAndSimplify(Block.makeCuboidShape(6.5, 1.5, 0, 9.5, 16, 16),
            Block.makeCuboidShape(6.5, 0, 0, 9.5, 1.5, 14.5),
            IBooleanFunction.OR));

        PortalWarp.PORTAL_MIDDLE_RIGHT_OFF.put(Direction.NORTH,
          VoxelShapes.combineAndSimplify(Block.makeCuboidShape(0, 3, 6.5, 8.6, 14.5, 9.5),
            VoxelShapes.combineAndSimplify(Block.makeCuboidShape(1.5, 0, 6.5, 10, 1.5, 9.5),
              VoxelShapes.combineAndSimplify(Block.makeCuboidShape(0, 1.5, 6.5, 10, 3, 9.5),
                Block.makeCuboidShape(0, 14.5, 6.5, 10, 16, 9.5),
                IBooleanFunction.OR), IBooleanFunction.OR), IBooleanFunction.OR));
        PortalWarp.PORTAL_MIDDLE_RIGHT_OFF.put(Direction.EAST,
          VoxelShapes.combineAndSimplify(Block.makeCuboidShape(6.5, 3, 0, 9.5, 14.5, 8.6),
            VoxelShapes.combineAndSimplify(Block.makeCuboidShape(6.5, 0, 1.5, 9.5, 1.5, 10),
              VoxelShapes.combineAndSimplify(Block.makeCuboidShape(6.5, 1.5, 0, 9.5, 3, 10),
                Block.makeCuboidShape(6.5, 14.5, 0, 9.5, 16, 10),
                IBooleanFunction.OR), IBooleanFunction.OR), IBooleanFunction.OR));
        PortalWarp.PORTAL_MIDDLE_RIGHT_OFF.put(Direction.SOUTH,
          VoxelShapes.combineAndSimplify(Block.makeCuboidShape(7.4, 3, 6.5, 16, 14.5, 9.5),
            VoxelShapes.combineAndSimplify(Block.makeCuboidShape(6, 0, 6.5, 14.5, 1.5, 9.5),
              VoxelShapes.combineAndSimplify(Block.makeCuboidShape(6, 1.5, 6.5, 16, 3, 9.5),
                Block.makeCuboidShape(6, 14.5, 6.5, 16, 16, 9.5),
                IBooleanFunction.OR), IBooleanFunction.OR), IBooleanFunction.OR));
        PortalWarp.PORTAL_MIDDLE_RIGHT_OFF.put(Direction.WEST,
          VoxelShapes.combineAndSimplify(Block.makeCuboidShape(6.5, 3, 7.4, 9.5, 14.5, 16),
            VoxelShapes.combineAndSimplify(Block.makeCuboidShape(6.5, 0, 6, 9.5, 1.5, 14.5),
              VoxelShapes.combineAndSimplify(Block.makeCuboidShape(6.5, 1.5, 6, 9.5, 3, 16),
                Block.makeCuboidShape(6.5, 14.5, 6, 9.5, 16, 16),
                IBooleanFunction.OR), IBooleanFunction.OR), IBooleanFunction.OR));

        PortalWarp.PORTAL_BOTTOM.put(Direction.NORTH, Block.makeCuboidShape(0, 0, 6.5, 16, 16, 9.5));
        PortalWarp.PORTAL_BOTTOM.put(Direction.EAST, Block.makeCuboidShape(6.5, 0, 0, 9.5, 16, 16));
        PortalWarp.PORTAL_BOTTOM.put(Direction.SOUTH, Block.makeCuboidShape(0, 0, 6.5, 16, 16, 9.5));
        PortalWarp.PORTAL_BOTTOM.put(Direction.WEST, Block.makeCuboidShape(6.5, 0, 0, 9.5, 16, 16));

        PortalWarp.PORTAL_BOTTOM_OFF.put(Direction.NORTH,
          VoxelShapes.combineAndSimplify(Block.makeCuboidShape(0, 0, 6.5, 16, 9, 9.5),
            VoxelShapes.combineAndSimplify(Block.makeCuboidShape(0, 9, 6.5, 3, 10.5, 9.5),
              Block.makeCuboidShape(13, 9, 6.5, 16, 10.5, 9.5),
              IBooleanFunction.OR), IBooleanFunction.OR));
        PortalWarp.PORTAL_BOTTOM_OFF.put(Direction.EAST,
          VoxelShapes.combineAndSimplify(Block.makeCuboidShape(6.5, 0, 0, 9.5, 9, 16),
            VoxelShapes.combineAndSimplify(Block.makeCuboidShape(6.5, 9, 0, 9.5, 10.5, 3),
              Block.makeCuboidShape(6.5, 9, 13, 9.5, 10.5, 16),
              IBooleanFunction.OR), IBooleanFunction.OR));
        PortalWarp.PORTAL_BOTTOM_OFF.put(Direction.SOUTH,
          VoxelShapes.combineAndSimplify(Block.makeCuboidShape(0, 0, 6.5, 16, 9, 9.5),
            VoxelShapes.combineAndSimplify(Block.makeCuboidShape(0, 9, 6.5, 3, 10.5, 9.5),
              Block.makeCuboidShape(13, 9, 6.5, 16, 10.5, 9.5),
              IBooleanFunction.OR), IBooleanFunction.OR));
        PortalWarp.PORTAL_BOTTOM_OFF.put(Direction.WEST,
          VoxelShapes.combineAndSimplify(Block.makeCuboidShape(6.5, 0, 0, 9.5, 9, 16),
            VoxelShapes.combineAndSimplify(Block.makeCuboidShape(6.5, 9, 0, 9.5, 10.5, 3),
              Block.makeCuboidShape(6.5, 9, 13, 9.5, 10.5, 16),
              IBooleanFunction.OR), IBooleanFunction.OR));

        PortalWarp.PORTAL_BOTTOM_LEFT.put(Direction.NORTH,
          VoxelShapes.combineAndSimplify(Block.makeCuboidShape(0, 1.5, 6.5, 3, 3, 9.5),
            VoxelShapes.combineAndSimplify(Block.makeCuboidShape(0, 3, 6.5, 4.5, 4.5, 9.5),
              VoxelShapes.combineAndSimplify(Block.makeCuboidShape(0, 4.5, 6.5, 6, 6, 9.5),
                VoxelShapes.combineAndSimplify(Block.makeCuboidShape(0, 6, 6.5, 7.5, 7.5, 9.5),
                  VoxelShapes.combineAndSimplify(Block.makeCuboidShape(0, 7.5, 6.5, 9, 9, 9.5),
                    VoxelShapes.combineAndSimplify(Block.makeCuboidShape(0, 9, 6.5, 10.5, 10.5, 9.5),
                      VoxelShapes.combineAndSimplify(Block.makeCuboidShape(0, 10.5, 6.5, 12, 13.5, 9.5),
                        Block.makeCuboidShape(0, 13.5, 6.5, 13.5, 16, 9.5),
                        IBooleanFunction.OR), IBooleanFunction.OR), IBooleanFunction.OR),
                  IBooleanFunction.OR), IBooleanFunction.OR), IBooleanFunction.OR),
            IBooleanFunction.OR));
        PortalWarp.PORTAL_BOTTOM_LEFT.put(Direction.EAST,
          VoxelShapes.combineAndSimplify(Block.makeCuboidShape(6.5, 1.5, 0, 9.5, 3, 3),
            VoxelShapes.combineAndSimplify(Block.makeCuboidShape(6.5, 3, 0, 9.5, 4.5, 4.5),
              VoxelShapes.combineAndSimplify(Block.makeCuboidShape(6.5, 4.5, 0, 9.5, 6, 6),
                VoxelShapes.combineAndSimplify(Block.makeCuboidShape(6.5, 6, 0, 9.5, 7.5, 7.5),
                  VoxelShapes.combineAndSimplify(Block.makeCuboidShape(6.5, 7.5, 0, 9.5, 9, 9),
                    VoxelShapes.combineAndSimplify(Block.makeCuboidShape(6.5, 9, 0, 9.5, 10.5, 10.5),
                      VoxelShapes.combineAndSimplify(Block.makeCuboidShape(6.5, 10.5, 0, 9.5, 13.5, 12),
                        Block.makeCuboidShape(6.5, 13.5, 0, 9.5, 16, 13.5),
                        IBooleanFunction.OR), IBooleanFunction.OR), IBooleanFunction.OR),
                  IBooleanFunction.OR), IBooleanFunction.OR), IBooleanFunction.OR),
            IBooleanFunction.OR));
        PortalWarp.PORTAL_BOTTOM_LEFT.put(Direction.SOUTH,
          VoxelShapes.combineAndSimplify(Block.makeCuboidShape(13, 1.5, 6.5, 16, 3, 9.5),
            VoxelShapes.combineAndSimplify(Block.makeCuboidShape(11.5, 3, 6.5, 16, 4.5, 9.5),
              VoxelShapes.combineAndSimplify(Block.makeCuboidShape(10, 4.5, 6.5, 16, 6, 9.5),
                VoxelShapes.combineAndSimplify(Block.makeCuboidShape(8.5, 6, 6.5, 16, 7.5, 9.5),
                  VoxelShapes.combineAndSimplify(Block.makeCuboidShape(7, 7.5, 6.5, 16, 9, 9.5),
                    VoxelShapes.combineAndSimplify(Block.makeCuboidShape(5.5, 9, 6.5, 16, 10.5, 9.5),
                      VoxelShapes.combineAndSimplify(Block.makeCuboidShape(4, 10.5, 6.5, 16, 13.5, 9.5),
                        Block.makeCuboidShape(2.5, 13.5, 6.5, 16, 16, 9.5),
                        IBooleanFunction.OR), IBooleanFunction.OR), IBooleanFunction.OR),
                  IBooleanFunction.OR), IBooleanFunction.OR), IBooleanFunction.OR),
            IBooleanFunction.OR));
        PortalWarp.PORTAL_BOTTOM_LEFT.put(Direction.WEST,
          VoxelShapes.combineAndSimplify(Block.makeCuboidShape(6.5, 1.5, 13, 9.5, 3, 16),
            VoxelShapes.combineAndSimplify(Block.makeCuboidShape(6.5, 3, 11.5, 9.5, 4.5, 16),
              VoxelShapes.combineAndSimplify(Block.makeCuboidShape(6.5, 4.5, 10, 9.5, 6, 16),
                VoxelShapes.combineAndSimplify(Block.makeCuboidShape(6.5, 6, 8.5, 9.5, 7.5, 16),
                  VoxelShapes.combineAndSimplify(Block.makeCuboidShape(6.5, 7.5, 7, 9.5, 9, 16),
                    VoxelShapes.combineAndSimplify(Block.makeCuboidShape(6.5, 9, 5.5, 9.5, 10.5, 16),
                      VoxelShapes.combineAndSimplify(Block.makeCuboidShape(6.5, 10.5, 4, 9.5, 13.5, 16),
                        Block.makeCuboidShape(6.5, 13.5, 2.5, 9.5, 16, 16),
                        IBooleanFunction.OR), IBooleanFunction.OR), IBooleanFunction.OR),
                  IBooleanFunction.OR), IBooleanFunction.OR), IBooleanFunction.OR),
            IBooleanFunction.OR));

        PortalWarp.PORTAL_BOTTOM_LEFT_OFF.put(Direction.NORTH,
          VoxelShapes.combineAndSimplify(Block.makeCuboidShape(0, 1.5, 6.5, 3, 3, 9.5),
            VoxelShapes.combineAndSimplify(Block.makeCuboidShape(0, 3, 6.5, 4.5, 4.5, 9.5),
              VoxelShapes.combineAndSimplify(Block.makeCuboidShape(0, 4.5, 6.5, 6, 6, 9.5),
                VoxelShapes.combineAndSimplify(Block.makeCuboidShape(0, 6, 6.5, 7.5, 7.5, 9.5),
                  VoxelShapes.combineAndSimplify(Block.makeCuboidShape(0, 7.5, 6.5, 9, 9, 9.5),
                    VoxelShapes.combineAndSimplify(Block.makeCuboidShape(0, 9, 6.5, 10.5, 10.5, 9.5),
                      VoxelShapes.combineAndSimplify(Block.makeCuboidShape(0, 10.5, 6.5, 12, 12, 9.5),
                        VoxelShapes.combineAndSimplify(Block.makeCuboidShape(3, 12, 6.5, 12, 13.5, 9.5),
                        Block.makeCuboidShape(4.5, 13.5, 6.5, 13.5, 16, 9.5),
                        IBooleanFunction.OR), IBooleanFunction.OR), IBooleanFunction.OR),
                  IBooleanFunction.OR), IBooleanFunction.OR), IBooleanFunction.OR),
            IBooleanFunction.OR), IBooleanFunction.OR));
        PortalWarp.PORTAL_BOTTOM_LEFT_OFF.put(Direction.EAST,
          VoxelShapes.combineAndSimplify(Block.makeCuboidShape(6.5, 1.5, 0, 9.5, 3, 3),
            VoxelShapes.combineAndSimplify(Block.makeCuboidShape(6.5, 3, 0, 9.5, 4.5, 4.5),
              VoxelShapes.combineAndSimplify(Block.makeCuboidShape(6.5, 4.5, 0, 9.5, 6, 6),
                VoxelShapes.combineAndSimplify(Block.makeCuboidShape(6.5, 6, 0, 9.5, 7.5, 7.5),
                  VoxelShapes.combineAndSimplify(Block.makeCuboidShape(6.5, 7.5, 0, 9.5, 9, 9),
                    VoxelShapes.combineAndSimplify(Block.makeCuboidShape(6.5, 9, 0, 9.5, 10.5, 10.5),
                      VoxelShapes.combineAndSimplify(Block.makeCuboidShape(6.5, 10.5, 0, 9.5, 12, 12),
                        VoxelShapes.combineAndSimplify(Block.makeCuboidShape(6.5, 12, 3, 9.5, 13.5, 12),
                          Block.makeCuboidShape(6.5, 13.5, 4.5, 9.5, 16, 13.5),
                          IBooleanFunction.OR), IBooleanFunction.OR), IBooleanFunction.OR),
                    IBooleanFunction.OR), IBooleanFunction.OR), IBooleanFunction.OR),
              IBooleanFunction.OR), IBooleanFunction.OR));
        PortalWarp.PORTAL_BOTTOM_LEFT_OFF.put(Direction.SOUTH,
          VoxelShapes.combineAndSimplify(Block.makeCuboidShape(13, 1.5, 6.5, 16, 3, 9.5),
            VoxelShapes.combineAndSimplify(Block.makeCuboidShape(11.5, 3, 6.5, 16, 4.5, 9.5),
              VoxelShapes.combineAndSimplify(Block.makeCuboidShape(10, 4.5, 6.5, 16, 6, 9.5),
                VoxelShapes.combineAndSimplify(Block.makeCuboidShape(8.5, 6, 6.5, 16, 7.5, 9.5),
                  VoxelShapes.combineAndSimplify(Block.makeCuboidShape(7, 7.5, 6.5, 16, 9, 9.5),
                    VoxelShapes.combineAndSimplify(Block.makeCuboidShape(5.5, 9, 6.5, 16, 10.5, 9.5),
                      VoxelShapes.combineAndSimplify(Block.makeCuboidShape(4, 10.5, 6.5, 16, 12, 9.5),
                        VoxelShapes.combineAndSimplify(Block.makeCuboidShape(4, 12, 6.5, 13, 13.5, 9.5),
                          Block.makeCuboidShape(2.5, 13.5, 6.5, 11.5, 16, 9.5),
                          IBooleanFunction.OR), IBooleanFunction.OR), IBooleanFunction.OR),
                    IBooleanFunction.OR), IBooleanFunction.OR), IBooleanFunction.OR),
              IBooleanFunction.OR), IBooleanFunction.OR));
        PortalWarp.PORTAL_BOTTOM_LEFT_OFF.put(Direction.WEST,
          VoxelShapes.combineAndSimplify(Block.makeCuboidShape(6.5, 1.5, 13, 9.5, 3, 16),
            VoxelShapes.combineAndSimplify(Block.makeCuboidShape(6.5, 3, 11.5, 9.5, 4.5, 16),
              VoxelShapes.combineAndSimplify(Block.makeCuboidShape(6.5, 4.5, 10, 9.5, 6, 16),
                VoxelShapes.combineAndSimplify(Block.makeCuboidShape(6.5, 6, 8.5, 9.5, 7.5, 16),
                  VoxelShapes.combineAndSimplify(Block.makeCuboidShape(6.5, 7.5, 7, 9.5, 9, 16),
                    VoxelShapes.combineAndSimplify(Block.makeCuboidShape(6.5, 9, 5.5, 9.5, 10.5, 16),
                      VoxelShapes.combineAndSimplify(Block.makeCuboidShape(6.5, 10.5, 4, 9.5, 12, 16),
                        VoxelShapes.combineAndSimplify(Block.makeCuboidShape(6.5, 12, 4, 9.5, 13.5, 13),
                          Block.makeCuboidShape(6.5, 13.5, 2.5, 9.5, 16, 11.5),
                          IBooleanFunction.OR), IBooleanFunction.OR), IBooleanFunction.OR),
                    IBooleanFunction.OR), IBooleanFunction.OR), IBooleanFunction.OR),
              IBooleanFunction.OR), IBooleanFunction.OR));

        PortalWarp.PORTAL_BOTTOM_RIGHT.put(Direction.NORTH,
          VoxelShapes.combineAndSimplify(Block.makeCuboidShape(13.25, 1.5, 6.5, 16, 3, 9.5),
            VoxelShapes.combineAndSimplify(Block.makeCuboidShape(10.5, 3, 6.5, 16, 4.5, 9.5),
              VoxelShapes.combineAndSimplify(Block.makeCuboidShape(9, 4.5, 6.5, 16, 6, 9.5),
                VoxelShapes.combineAndSimplify(Block.makeCuboidShape(7.5, 6, 6.5, 16, 7.5, 9.5),
                  VoxelShapes.combineAndSimplify(Block.makeCuboidShape(6, 7.5, 6.5, 16, 9, 9.5),
                    VoxelShapes.combineAndSimplify(Block.makeCuboidShape(4.5, 9, 6.5, 16, 10.5, 9.5),
                      VoxelShapes.combineAndSimplify(Block.makeCuboidShape(3, 10.5, 6.5, 16, 13.5, 9.5),
                        Block.makeCuboidShape(1.5, 13.5, 6.5, 16, 16, 9.5),
                        IBooleanFunction.OR), IBooleanFunction.OR), IBooleanFunction.OR),
                  IBooleanFunction.OR), IBooleanFunction.OR), IBooleanFunction.OR),
            IBooleanFunction.OR));
        PortalWarp.PORTAL_BOTTOM_RIGHT.put(Direction.EAST,
          VoxelShapes.combineAndSimplify(Block.makeCuboidShape(6.5, 1.5, 13.25, 9.5, 3, 16),
            VoxelShapes.combineAndSimplify(Block.makeCuboidShape(6.5, 3, 10.5, 9.5, 4.5, 16),
              VoxelShapes.combineAndSimplify(Block.makeCuboidShape(6.5, 4.5, 9, 9.5, 6, 16),
                VoxelShapes.combineAndSimplify(Block.makeCuboidShape(6.5, 6, 7.5, 9.5, 7.5, 16),
                  VoxelShapes.combineAndSimplify(Block.makeCuboidShape(6.5, 7.5, 6, 9.5, 9, 16),
                    VoxelShapes.combineAndSimplify(Block.makeCuboidShape(6.5, 9, 4.5, 9.5, 10.5, 16),
                      VoxelShapes.combineAndSimplify(Block.makeCuboidShape(6.5, 10.5, 3, 9.5, 13.5, 16),
                        Block.makeCuboidShape(6.5, 13.5, 1.5, 9.5, 16, 16),
                        IBooleanFunction.OR), IBooleanFunction.OR), IBooleanFunction.OR),
                  IBooleanFunction.OR), IBooleanFunction.OR), IBooleanFunction.OR),
            IBooleanFunction.OR));
        PortalWarp.PORTAL_BOTTOM_RIGHT.put(Direction.SOUTH,
          VoxelShapes.combineAndSimplify(Block.makeCuboidShape(0, 1.5, 6.5, 2.75, 3, 9.5),
            VoxelShapes.combineAndSimplify(Block.makeCuboidShape(0, 3, 6.5, 5.5, 4.5, 9.5),
              VoxelShapes.combineAndSimplify(Block.makeCuboidShape(0, 4.5, 6.5, 7, 6, 9.5),
                VoxelShapes.combineAndSimplify(Block.makeCuboidShape(0, 6, 6.5, 8.5, 7.5, 9.5),
                  VoxelShapes.combineAndSimplify(Block.makeCuboidShape(0, 7.5, 6.5, 10, 9, 9.5),
                    VoxelShapes.combineAndSimplify(Block.makeCuboidShape(0, 9, 6.5, 11.5, 10.5, 9.5),
                      VoxelShapes.combineAndSimplify(Block.makeCuboidShape(0, 10.5, 6.5, 13, 13.5, 9.5),
                        Block.makeCuboidShape(0, 13.5, 6.5, 14.5, 16, 9.5),
                        IBooleanFunction.OR), IBooleanFunction.OR), IBooleanFunction.OR),
                  IBooleanFunction.OR), IBooleanFunction.OR), IBooleanFunction.OR),
            IBooleanFunction.OR));
        PortalWarp.PORTAL_BOTTOM_RIGHT.put(Direction.WEST,
          VoxelShapes.combineAndSimplify(Block.makeCuboidShape(6.5, 1.5, 0, 9.5, 3, 2.75),
            VoxelShapes.combineAndSimplify(Block.makeCuboidShape(6.5, 3, 0, 9.5, 4.5, 5.5),
              VoxelShapes.combineAndSimplify(Block.makeCuboidShape(6.5, 4.5, 0, 9.5, 6, 7),
                VoxelShapes.combineAndSimplify(Block.makeCuboidShape(6.5, 6, 0, 9.5, 7.5, 8.5),
                  VoxelShapes.combineAndSimplify(Block.makeCuboidShape(6.5, 7.5, 0, 9.5, 9, 10),
                    VoxelShapes.combineAndSimplify(Block.makeCuboidShape(6.5, 9, 0, 9.5, 10.5, 11.5),
                      VoxelShapes.combineAndSimplify(Block.makeCuboidShape(6.5, 10.5, 0, 9.5, 13.5, 13),
                        Block.makeCuboidShape(6.5, 13.5, 0, 9.5, 16, 14.5),
                        IBooleanFunction.OR), IBooleanFunction.OR), IBooleanFunction.OR),
                  IBooleanFunction.OR), IBooleanFunction.OR), IBooleanFunction.OR),
            IBooleanFunction.OR));

        PortalWarp.PORTAL_BOTTOM_RIGHT_OFF.put(Direction.NORTH,
          VoxelShapes.combineAndSimplify(Block.makeCuboidShape(13.25, 1.5, 6.5, 16, 3, 9.5),
            VoxelShapes.combineAndSimplify(Block.makeCuboidShape(10.5, 3, 6.5, 16, 4.5, 9.5),
              VoxelShapes.combineAndSimplify(Block.makeCuboidShape(9, 4.5, 6.5, 16, 6, 9.5),
                VoxelShapes.combineAndSimplify(Block.makeCuboidShape(7.5, 6, 6.5, 16, 7.5, 9.5),
                  VoxelShapes.combineAndSimplify(Block.makeCuboidShape(6, 7.5, 6.5, 16, 9, 9.5),
                    VoxelShapes.combineAndSimplify(Block.makeCuboidShape(4.5, 9, 6.5, 16, 10.5, 9.5),
                      VoxelShapes.combineAndSimplify(Block.makeCuboidShape(3, 10.5, 6.5, 16, 12, 9.5),
                        VoxelShapes.combineAndSimplify(Block.makeCuboidShape(3, 12, 6.5, 13, 13.5, 9.5),
                        Block.makeCuboidShape(1.5, 13.5, 6.5, 11.5, 16, 9.5),
                        IBooleanFunction.OR), IBooleanFunction.OR), IBooleanFunction.OR),
                  IBooleanFunction.OR), IBooleanFunction.OR), IBooleanFunction.OR),
            IBooleanFunction.OR), IBooleanFunction.OR));
        PortalWarp.PORTAL_BOTTOM_RIGHT_OFF.put(Direction.EAST,
          VoxelShapes.combineAndSimplify(Block.makeCuboidShape(6.5, 1.5, 13.25, 9.5, 3, 16),
            VoxelShapes.combineAndSimplify(Block.makeCuboidShape(6.5, 3, 10.5, 9.5, 4.5, 16),
              VoxelShapes.combineAndSimplify(Block.makeCuboidShape(6.5, 4.5, 9, 9.5, 6, 16),
                VoxelShapes.combineAndSimplify(Block.makeCuboidShape(6.5, 6, 7.5, 9.5, 7.5, 16),
                  VoxelShapes.combineAndSimplify(Block.makeCuboidShape(6.5, 7.5, 6, 9.5, 9, 16),
                    VoxelShapes.combineAndSimplify(Block.makeCuboidShape(6.5, 9, 4.5, 9.5, 10.5, 16),
                      VoxelShapes.combineAndSimplify(Block.makeCuboidShape(6.5, 10.5, 3, 9.5, 12, 16),
                        VoxelShapes.combineAndSimplify(Block.makeCuboidShape(6.5, 12, 3, 9.5, 13.5, 13),
                          Block.makeCuboidShape(6.5, 13.5, 1.5, 9.5, 16, 11.5),
                          IBooleanFunction.OR), IBooleanFunction.OR), IBooleanFunction.OR),
                    IBooleanFunction.OR), IBooleanFunction.OR), IBooleanFunction.OR),
              IBooleanFunction.OR), IBooleanFunction.OR));
        PortalWarp.PORTAL_BOTTOM_RIGHT_OFF.put(Direction.SOUTH,
          VoxelShapes.combineAndSimplify(Block.makeCuboidShape(0, 1.5, 6.5, 2.75, 3, 9.5),
            VoxelShapes.combineAndSimplify(Block.makeCuboidShape(0, 3, 6.5, 5.5, 4.5, 9.5),
              VoxelShapes.combineAndSimplify(Block.makeCuboidShape(0, 4.5, 6.5, 7, 6, 9.5),
                VoxelShapes.combineAndSimplify(Block.makeCuboidShape(0, 6, 6.5, 8.5, 7.5, 9.5),
                  VoxelShapes.combineAndSimplify(Block.makeCuboidShape(0, 7.5, 6.5, 10, 9, 9.5),
                    VoxelShapes.combineAndSimplify(Block.makeCuboidShape(0, 9, 6.5, 11.5, 10.5, 9.5),
                      VoxelShapes.combineAndSimplify(Block.makeCuboidShape(0, 10.5, 6.5, 13, 12, 9.5),
                        VoxelShapes.combineAndSimplify(Block.makeCuboidShape(3, 12, 6.5, 13, 13.5, 9.5),
                          Block.makeCuboidShape(4.5, 13.5, 6.5, 14.5, 16, 9.5),
                          IBooleanFunction.OR), IBooleanFunction.OR), IBooleanFunction.OR),
                    IBooleanFunction.OR), IBooleanFunction.OR), IBooleanFunction.OR),
              IBooleanFunction.OR), IBooleanFunction.OR));
        PortalWarp.PORTAL_BOTTOM_RIGHT_OFF.put(Direction.WEST,
          VoxelShapes.combineAndSimplify(Block.makeCuboidShape(6.5, 1.5, 0, 9.5, 3, 2.75),
            VoxelShapes.combineAndSimplify(Block.makeCuboidShape(6.5, 3, 0, 9.5, 4.5, 5.5),
              VoxelShapes.combineAndSimplify(Block.makeCuboidShape(6.5, 4.5, 0, 9.5, 6, 7),
                VoxelShapes.combineAndSimplify(Block.makeCuboidShape(6.5, 6, 0, 9.5, 7.5, 8.5),
                  VoxelShapes.combineAndSimplify(Block.makeCuboidShape(6.5, 7.5, 0, 9.5, 9, 10),
                    VoxelShapes.combineAndSimplify(Block.makeCuboidShape(6.5, 9, 0, 9.5, 10.5, 11.5),
                      VoxelShapes.combineAndSimplify(Block.makeCuboidShape(6.5, 10.5, 0, 9.5, 12, 13),
                        VoxelShapes.combineAndSimplify(Block.makeCuboidShape(6.5, 12, 3, 9.5, 13.5, 13),
                          Block.makeCuboidShape(6.5, 13.5, 4.5, 9.5, 16, 14.5),
                          IBooleanFunction.OR), IBooleanFunction.OR), IBooleanFunction.OR),
                    IBooleanFunction.OR), IBooleanFunction.OR), IBooleanFunction.OR),
              IBooleanFunction.OR), IBooleanFunction.OR));
        //@formatter:on
    }

    private VoxelShape getShape(final PortalWarpPart part, final Direction dir, final boolean active)
    {
        if (part == PortalWarpPart.BOTTOM && active) return PortalWarp.PORTAL_BOTTOM.get(dir);
        else if (part == PortalWarpPart.BOTTOM_LEFT && active) return PortalWarp.PORTAL_BOTTOM_LEFT.get(dir);
        else if (part == PortalWarpPart.BOTTOM_RIGHT && active) return PortalWarp.PORTAL_BOTTOM_RIGHT.get(dir);
        else if (part == PortalWarpPart.MIDDLE && active) return PortalWarp.PORTAL_MIDDLE.get(dir);
        else if (part == PortalWarpPart.MIDDLE_LEFT && active) return PortalWarp.PORTAL_MIDDLE_LEFT.get(dir);
        else if (part == PortalWarpPart.MIDDLE_RIGHT && active) return PortalWarp.PORTAL_MIDDLE_RIGHT.get(dir);
        else if (part == PortalWarpPart.TOP_LEFT && active) return PortalWarp.PORTAL_TOP_LEFT.get(dir);
        else if (part == PortalWarpPart.TOP_RIGHT && active) return PortalWarp.PORTAL_TOP_RIGHT.get(dir);

        else if (part == PortalWarpPart.BOTTOM && !active) return PortalWarp.PORTAL_BOTTOM_OFF.get(dir);
        else if (part == PortalWarpPart.BOTTOM_LEFT && !active) return PortalWarp.PORTAL_BOTTOM_LEFT_OFF.get(dir);
        else if (part == PortalWarpPart.BOTTOM_RIGHT && !active) return PortalWarp.PORTAL_BOTTOM_RIGHT_OFF.get(dir);
        else if (part == PortalWarpPart.MIDDLE && !active) return PortalWarp.PORTAL_MIDDLE_OFF.get(dir);
        else if (part == PortalWarpPart.MIDDLE_LEFT && !active) return PortalWarp.PORTAL_MIDDLE_LEFT_OFF.get(dir);
        else if (part == PortalWarpPart.MIDDLE_RIGHT && !active) return PortalWarp.PORTAL_MIDDLE_RIGHT_OFF.get(dir);
        else if (part == PortalWarpPart.TOP_LEFT && !active) return PortalWarp.PORTAL_TOP_LEFT_OFF.get(dir);
        else if (part == PortalWarpPart.TOP_RIGHT && !active) return PortalWarp.PORTAL_TOP_RIGHT_OFF.get(dir);
        else if (part == PortalWarpPart.TOP && !active) return PortalWarp.PORTAL_TOP_OFF.get(dir);
        else return PortalWarp.PORTAL_TOP.get(dir);
    }

    // Precise selection box
    @Override
    public VoxelShape getShape(final BlockState state, final IBlockReader worldIn, final BlockPos pos,
            final ISelectionContext context)
    {
        final PortalWarpPart part = state.get(PortalWarp.PART);
        final Direction dir = state.get(PortalWarp.FACING);
        final boolean active = state.get(PortalWarp.ACTIVE);
        VoxelShape s = this.getShape(part, dir, active);
        if (s == null)
        {
            s = VoxelShapes.empty();
            PokecubeCore.LOGGER.error("Error with hitbox for {}, {}, {}", part, dir, active);
        }

        return s;
    }

    public PortalWarp(final String name, final Properties props)
    {
        super(name, props);
        this.setDefaultState(this.stateContainer.getBaseState().with(PortalWarp.FACING, Direction.NORTH).with(
                PortalWarp.WATERLOGGED, false).with(PortalWarp.PART, PortalWarpPart.BOTTOM).with(PortalWarp.ACTIVE,
                        true));
    }

    @Override
    public TileEntity createTileEntity(final BlockState state, final IBlockReader world)
    {
        return new RingTile();
    }

    @Override
    public boolean hasTileEntity(final BlockState state)
    {
        final PortalWarpPart part = state.get(PortalWarp.PART);
        return part == PortalWarpPart.MIDDLE;
    }

    /*
     * Make Portal in Move
     */
    public void place(final World world, final BlockPos pos, final Direction direction)
    {
        final BlockState state = this.getDefaultState().with(PortalWarp.PART, PortalWarpPart.BOTTOM).with(
                PortalWarp.FACING, direction);
        world.setBlockState(pos, state, 3);
        this.place(world, pos, state, direction.getOpposite());
    }

    public void place(final World world, final BlockPos pos, final BlockState state, final Direction direction)
    {
        final BlockPos portalWarpTopLeftPos = this.getPortalWarpTopLeftPos(pos, direction);
        final BlockPos portalWarpTopRightPos = this.getPortalWarpTopRightPos(pos, direction);
        final BlockPos portalWarpMiddleLeftPos = this.getPortalWarpMiddleLeftPos(pos, direction);
        final BlockPos portalWarpMiddleRightPos = this.getPortalWarpMiddleRightPos(pos, direction);
        final BlockPos portalWarpBottomLeftPos = this.getPortalWarpBottomLeftPos(pos, direction);
        final BlockPos portalWarpBottomRightPos = this.getPortalWarpBottomRightPos(pos, direction);

        final FluidState topFluidState = world.getFluidState(pos.up(2));
        final FluidState topWestFluidState = world.getFluidState(pos.up(2).west());
        final FluidState topEastFluidState = world.getFluidState(pos.up(2).east());
        final FluidState middleFluidState = world.getFluidState(pos.up());
        final FluidState middleWestFluidState = world.getFluidState(pos.up().west());
        final FluidState middleEastFluidState = world.getFluidState(pos.up().east());
        final FluidState bottomWestFluidState = world.getFluidState(pos.west());
        final FluidState bottomEastFluidState = world.getFluidState(pos.east());

        world.setBlockState(portalWarpBottomLeftPos, state.with(PortalWarp.PART, PortalWarpPart.BOTTOM_LEFT).with(
                PortalWarp.WATERLOGGED, bottomWestFluidState.getFluid() == Fluids.WATER), 3);
        world.setBlockState(portalWarpBottomRightPos, state.with(PortalWarp.PART, PortalWarpPart.BOTTOM_RIGHT).with(
                PortalWarp.WATERLOGGED, bottomEastFluidState.getFluid() == Fluids.WATER), 3);
        world.setBlockState(pos.up(), state.with(PortalWarp.PART, PortalWarpPart.MIDDLE).with(PortalWarp.WATERLOGGED,
                middleFluidState.getFluid() == Fluids.WATER), 3);
        world.setBlockState(portalWarpMiddleLeftPos, state.with(PortalWarp.PART, PortalWarpPart.MIDDLE_LEFT).with(
                PortalWarp.WATERLOGGED, middleWestFluidState.getFluid() == Fluids.WATER), 3);
        world.setBlockState(portalWarpMiddleRightPos, state.with(PortalWarp.PART, PortalWarpPart.MIDDLE_RIGHT).with(
                PortalWarp.WATERLOGGED, middleEastFluidState.getFluid() == Fluids.WATER), 3);
        world.setBlockState(pos.up(2), state.with(PortalWarp.PART, PortalWarpPart.TOP).with(PortalWarp.WATERLOGGED,
                topFluidState.getFluid() == Fluids.WATER), 3);
        world.setBlockState(portalWarpTopLeftPos, state.with(PortalWarp.PART, PortalWarpPart.TOP_LEFT).with(
                PortalWarp.WATERLOGGED, topWestFluidState.getFluid() == Fluids.WATER), 3);
        world.setBlockState(portalWarpTopRightPos, state.with(PortalWarp.PART, PortalWarpPart.TOP_RIGHT).with(
                PortalWarp.WATERLOGGED, topEastFluidState.getFluid() == Fluids.WATER), 3);
    }

    public void remove(final World world, final BlockPos pos, final BlockState state)
    {
        final Direction facing = state.get(PortalWarp.FACING);

        final BlockPos portalWarpPos = this.getPortalWarpPos(pos, state.get(PortalWarp.PART), facing);
        BlockState portalWarpBlockState = world.getBlockState(portalWarpPos);
        if (portalWarpBlockState.getBlock() == this && !pos.equals(portalWarpPos)) this.removePart(world, portalWarpPos,
                portalWarpBlockState);

        BlockPos portalWarpPartPos = this.getPortalWarpTopPos(portalWarpPos, facing);
        portalWarpBlockState = world.getBlockState(portalWarpPartPos);
        if (portalWarpBlockState.getBlock() == this && !pos.equals(portalWarpPartPos)) this.removePart(world,
                portalWarpPartPos, portalWarpBlockState);

        portalWarpPartPos = this.getPortalWarpTopLeftPos(portalWarpPos, facing);
        portalWarpBlockState = world.getBlockState(portalWarpPartPos);
        if (portalWarpBlockState.getBlock() == this && !pos.equals(portalWarpPartPos)) this.removePart(world,
                portalWarpPartPos, portalWarpBlockState);

        portalWarpPartPos = this.getPortalWarpTopRightPos(portalWarpPos, facing);
        portalWarpBlockState = world.getBlockState(portalWarpPartPos);
        if (portalWarpBlockState.getBlock() == this && !pos.equals(portalWarpPartPos)) this.removePart(world,
                portalWarpPartPos, portalWarpBlockState);

        portalWarpPartPos = this.getPortalWarpMiddlePos(portalWarpPos, facing);
        portalWarpBlockState = world.getBlockState(portalWarpPartPos);
        if (portalWarpBlockState.getBlock() == this && !pos.equals(portalWarpPartPos)) this.removePart(world,
                portalWarpPartPos, portalWarpBlockState);

        portalWarpPartPos = this.getPortalWarpMiddleLeftPos(portalWarpPos, facing);
        portalWarpBlockState = world.getBlockState(portalWarpPartPos);
        if (portalWarpBlockState.getBlock() == this && !pos.equals(portalWarpPartPos)) this.removePart(world,
                portalWarpPartPos, portalWarpBlockState);

        portalWarpPartPos = this.getPortalWarpMiddleRightPos(portalWarpPos, facing);
        portalWarpBlockState = world.getBlockState(portalWarpPartPos);
        if (portalWarpBlockState.getBlock() == this && !pos.equals(portalWarpPartPos)) this.removePart(world,
                portalWarpPartPos, portalWarpBlockState);

        portalWarpPartPos = this.getPortalWarpBottomLeftPos(portalWarpPos, facing);
        portalWarpBlockState = world.getBlockState(portalWarpPartPos);
        if (portalWarpBlockState.getBlock() == this && !pos.equals(portalWarpPartPos)) this.removePart(world,
                portalWarpPartPos, portalWarpBlockState);

        portalWarpPartPos = this.getPortalWarpBottomRightPos(portalWarpPos, facing);
        portalWarpBlockState = world.getBlockState(portalWarpPartPos);
        if (portalWarpBlockState.getBlock() == this && !pos.equals(portalWarpPartPos)) this.removePart(world,
                portalWarpPartPos, portalWarpBlockState);
    }
    /*
     * end
     */

    // Places Portal with both top and bottom pieces
    @Override
    public void onBlockPlacedBy(final World world, final BlockPos pos, final BlockState state,
            @Nullable final LivingEntity entity, final ItemStack stack)
    {
        if (entity != null)
        {
            final BlockPos portalWarpTopLeftPos = this.getPortalWarpTopLeftPos(pos, entity.getHorizontalFacing());
            final BlockPos portalWarpTopRightPos = this.getPortalWarpTopRightPos(pos, entity.getHorizontalFacing());
            final BlockPos portalWarpMiddleLeftPos = this.getPortalWarpMiddleLeftPos(pos, entity.getHorizontalFacing());
            final BlockPos portalWarpMiddleRightPos = this.getPortalWarpMiddleRightPos(pos, entity
                    .getHorizontalFacing());
            final BlockPos portalWarpBottomLeftPos = this.getPortalWarpBottomLeftPos(pos, entity.getHorizontalFacing());
            final BlockPos portalWarpBottomRightPos = this.getPortalWarpBottomRightPos(pos, entity
                    .getHorizontalFacing());

            final FluidState topFluidState = world.getFluidState(pos.up(2));
            final FluidState topWestFluidState = world.getFluidState(pos.up(2).west());
            final FluidState topEastFluidState = world.getFluidState(pos.up(2).east());
            final FluidState middleFluidState = world.getFluidState(pos.up());
            final FluidState middleWestFluidState = world.getFluidState(pos.up().west());
            final FluidState middleEastFluidState = world.getFluidState(pos.up().east());
            final FluidState bottomWestFluidState = world.getFluidState(pos.west());
            final FluidState bottomEastFluidState = world.getFluidState(pos.east());

            world.setBlockState(portalWarpBottomLeftPos, state.with(PortalWarp.PART, PortalWarpPart.BOTTOM_LEFT).with(
                    PortalWarp.WATERLOGGED, bottomWestFluidState.getFluid() == Fluids.WATER), 3);
            world.setBlockState(portalWarpBottomRightPos, state.with(PortalWarp.PART, PortalWarpPart.BOTTOM_RIGHT).with(
                    PortalWarp.WATERLOGGED, bottomEastFluidState.getFluid() == Fluids.WATER), 3);
            world.setBlockState(pos.up(), state.with(PortalWarp.PART, PortalWarpPart.MIDDLE).with(
                    PortalWarp.WATERLOGGED, middleFluidState.getFluid() == Fluids.WATER), 3);
            world.setBlockState(portalWarpMiddleLeftPos, state.with(PortalWarp.PART, PortalWarpPart.MIDDLE_LEFT).with(
                    PortalWarp.WATERLOGGED, middleWestFluidState.getFluid() == Fluids.WATER), 3);
            world.setBlockState(portalWarpMiddleRightPos, state.with(PortalWarp.PART, PortalWarpPart.MIDDLE_RIGHT).with(
                    PortalWarp.WATERLOGGED, middleEastFluidState.getFluid() == Fluids.WATER), 3);
            world.setBlockState(pos.up(2), state.with(PortalWarp.PART, PortalWarpPart.TOP).with(PortalWarp.WATERLOGGED,
                    topFluidState.getFluid() == Fluids.WATER), 3);
            world.setBlockState(portalWarpTopLeftPos, state.with(PortalWarp.PART, PortalWarpPart.TOP_LEFT).with(
                    PortalWarp.WATERLOGGED, topWestFluidState.getFluid() == Fluids.WATER), 3);
            world.setBlockState(portalWarpTopRightPos, state.with(PortalWarp.PART, PortalWarpPart.TOP_RIGHT).with(
                    PortalWarp.WATERLOGGED, topEastFluidState.getFluid() == Fluids.WATER), 3);
        }
    }

    // Breaking Portal breaks both parts and returns one item only
    @Override
    public void onBlockHarvested(final World world, final BlockPos pos, final BlockState state,
            final PlayerEntity player)
    {
        final Direction facing = state.get(PortalWarp.FACING);

        final BlockPos portalWarpPos = this.getPortalWarpPos(pos, state.get(PortalWarp.PART), facing);
        BlockState PortalWarpBlockState = world.getBlockState(portalWarpPos);
        if (PortalWarpBlockState.getBlock() == this && !pos.equals(portalWarpPos)) this.removePart(world, portalWarpPos,
                PortalWarpBlockState);

        BlockPos portalWarpPartPos = this.getPortalWarpTopPos(portalWarpPos, facing);
        PortalWarpBlockState = world.getBlockState(portalWarpPartPos);
        if (PortalWarpBlockState.getBlock() == this && !pos.equals(portalWarpPartPos)) this.removePart(world,
                portalWarpPartPos, PortalWarpBlockState);

        portalWarpPartPos = this.getPortalWarpTopLeftPos(portalWarpPos, facing);
        PortalWarpBlockState = world.getBlockState(portalWarpPartPos);
        if (PortalWarpBlockState.getBlock() == this && !pos.equals(portalWarpPartPos)) this.removePart(world,
                portalWarpPartPos, PortalWarpBlockState);

        portalWarpPartPos = this.getPortalWarpTopRightPos(portalWarpPos, facing);
        PortalWarpBlockState = world.getBlockState(portalWarpPartPos);
        if (PortalWarpBlockState.getBlock() == this && !pos.equals(portalWarpPartPos)) this.removePart(world,
                portalWarpPartPos, PortalWarpBlockState);

        portalWarpPartPos = this.getPortalWarpMiddlePos(portalWarpPos, facing);
        PortalWarpBlockState = world.getBlockState(portalWarpPartPos);
        if (PortalWarpBlockState.getBlock() == this && !pos.equals(portalWarpPartPos)) this.removePart(world,
                portalWarpPartPos, PortalWarpBlockState);

        portalWarpPartPos = this.getPortalWarpMiddleLeftPos(portalWarpPos, facing);
        PortalWarpBlockState = world.getBlockState(portalWarpPartPos);
        if (PortalWarpBlockState.getBlock() == this && !pos.equals(portalWarpPartPos)) this.removePart(world,
                portalWarpPartPos, PortalWarpBlockState);

        portalWarpPartPos = this.getPortalWarpMiddleRightPos(portalWarpPos, facing);
        PortalWarpBlockState = world.getBlockState(portalWarpPartPos);
        if (PortalWarpBlockState.getBlock() == this && !pos.equals(portalWarpPartPos)) this.removePart(world,
                portalWarpPartPos, PortalWarpBlockState);

        portalWarpPartPos = this.getPortalWarpBottomLeftPos(portalWarpPos, facing);
        PortalWarpBlockState = world.getBlockState(portalWarpPartPos);
        if (PortalWarpBlockState.getBlock() == this && !pos.equals(portalWarpPartPos)) this.removePart(world,
                portalWarpPartPos, PortalWarpBlockState);

        portalWarpPartPos = this.getPortalWarpBottomRightPos(portalWarpPos, facing);
        PortalWarpBlockState = world.getBlockState(portalWarpPartPos);
        if (PortalWarpBlockState.getBlock() == this && !pos.equals(portalWarpPartPos)) this.removePart(world,
                portalWarpPartPos, PortalWarpBlockState);
        super.onBlockHarvested(world, pos, state, player);
    }

    private BlockPos getPortalWarpTopPos(final BlockPos base, final Direction facing)
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

    private BlockPos getPortalWarpTopLeftPos(final BlockPos base, final Direction facing)
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

    private BlockPos getPortalWarpTopRightPos(final BlockPos base, final Direction facing)
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

    private BlockPos getPortalWarpMiddlePos(final BlockPos base, final Direction facing)
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

    private BlockPos getPortalWarpMiddleLeftPos(final BlockPos base, final Direction facing)
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

    private BlockPos getPortalWarpMiddleRightPos(final BlockPos base, final Direction facing)
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

    private BlockPos getPortalWarpBottomLeftPos(final BlockPos base, final Direction facing)
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

    private BlockPos getPortalWarpBottomRightPos(final BlockPos base, final Direction facing)
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

    private BlockPos getPortalWarpPos(final BlockPos pos, final PortalWarpPart part, final Direction facing)
    {
        if (part == PortalWarpPart.BOTTOM) return pos;
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

    public void setActiveState(final World world, final BlockPos pos, final BlockState state, final boolean active)
    {
        final Direction facing = state.get(PortalWarp.FACING);

        final BlockPos portalWarpPos = this.getPortalWarpPos(pos, state.get(PortalWarp.PART), facing);
        BlockState PortalWarpBlockState = world.getBlockState(portalWarpPos);
        if (PortalWarpBlockState.getBlock() == this && !pos.equals(portalWarpPos)) world.setBlockState(portalWarpPos,
                PortalWarpBlockState.with(PortalWarp.ACTIVE, active));

        BlockPos portalWarpPartPos = this.getPortalWarpTopPos(portalWarpPos, facing);
        PortalWarpBlockState = world.getBlockState(portalWarpPartPos);
        if (PortalWarpBlockState.getBlock() == this && !pos.equals(portalWarpPartPos)) world.setBlockState(
                portalWarpPartPos, PortalWarpBlockState.with(PortalWarp.ACTIVE, active));

        portalWarpPartPos = this.getPortalWarpTopLeftPos(portalWarpPos, facing);
        PortalWarpBlockState = world.getBlockState(portalWarpPartPos);
        if (PortalWarpBlockState.getBlock() == this && !pos.equals(portalWarpPartPos)) world.setBlockState(
                portalWarpPartPos, PortalWarpBlockState.with(PortalWarp.ACTIVE, active));

        portalWarpPartPos = this.getPortalWarpTopRightPos(portalWarpPos, facing);
        PortalWarpBlockState = world.getBlockState(portalWarpPartPos);
        if (PortalWarpBlockState.getBlock() == this && !pos.equals(portalWarpPartPos)) world.setBlockState(
                portalWarpPartPos, PortalWarpBlockState.with(PortalWarp.ACTIVE, active));

        portalWarpPartPos = this.getPortalWarpMiddlePos(portalWarpPos, facing);
        PortalWarpBlockState = world.getBlockState(portalWarpPartPos);
        if (PortalWarpBlockState.getBlock() == this && !pos.equals(portalWarpPartPos)) world.setBlockState(
                portalWarpPartPos, PortalWarpBlockState.with(PortalWarp.ACTIVE, active));

        portalWarpPartPos = this.getPortalWarpMiddleLeftPos(portalWarpPos, facing);
        PortalWarpBlockState = world.getBlockState(portalWarpPartPos);
        if (PortalWarpBlockState.getBlock() == this && !pos.equals(portalWarpPartPos)) world.setBlockState(
                portalWarpPartPos, PortalWarpBlockState.with(PortalWarp.ACTIVE, active));

        portalWarpPartPos = this.getPortalWarpMiddleRightPos(portalWarpPos, facing);
        PortalWarpBlockState = world.getBlockState(portalWarpPartPos);
        if (PortalWarpBlockState.getBlock() == this && !pos.equals(portalWarpPartPos)) world.setBlockState(
                portalWarpPartPos, PortalWarpBlockState.with(PortalWarp.ACTIVE, active));

        portalWarpPartPos = this.getPortalWarpBottomLeftPos(portalWarpPos, facing);
        PortalWarpBlockState = world.getBlockState(portalWarpPartPos);
        if (PortalWarpBlockState.getBlock() == this && !pos.equals(portalWarpPartPos)) world.setBlockState(
                portalWarpPartPos, PortalWarpBlockState.with(PortalWarp.ACTIVE, active));

        portalWarpPartPos = this.getPortalWarpBottomRightPos(portalWarpPos, facing);
        PortalWarpBlockState = world.getBlockState(portalWarpPartPos);
        if (PortalWarpBlockState.getBlock() == this && !pos.equals(portalWarpPartPos)) world.setBlockState(
                portalWarpPartPos, PortalWarpBlockState.with(PortalWarp.ACTIVE, active));
    }

    // Breaking the Portal leaves water if underwater
    private void removePart(final World world, final BlockPos pos, final BlockState state)
    {
        final FluidState fluidState = world.getFluidState(pos);
        if (fluidState.getFluid() == Fluids.WATER) world.setBlockState(pos, fluidState.getBlockState(), 35);
        else world.setBlockState(pos, Blocks.AIR.getDefaultState(), 35);
    }

    // Prevents the Portal from replacing blocks above it and checks for water
    @Override
    public BlockState getStateForPlacement(final BlockItemUseContext context)
    {
        final FluidState ifluidstate = context.getWorld().getFluidState(context.getPos());
        final BlockPos pos = context.getPos();

        final BlockPos portalWarpTopPos = this.getPortalWarpTopPos(pos, context.getPlacementHorizontalFacing()
                .getOpposite());
        final BlockPos portalWarpTopLeftPos = this.getPortalWarpTopLeftPos(pos, context.getPlacementHorizontalFacing()
                .getOpposite());
        final BlockPos portalWarpTopRightPos = this.getPortalWarpTopRightPos(pos, context.getPlacementHorizontalFacing()
                .getOpposite());

        final BlockPos portalWarpMiddlePos = this.getPortalWarpMiddlePos(pos, context.getPlacementHorizontalFacing()
                .getOpposite());
        final BlockPos portalWarpMiddleLeftPos = this.getPortalWarpMiddleLeftPos(pos, context
                .getPlacementHorizontalFacing().getOpposite());
        final BlockPos portalWarpMiddleRightPos = this.getPortalWarpMiddleRightPos(pos, context
                .getPlacementHorizontalFacing().getOpposite());

        final BlockPos portalWarpBottomLeftPos = this.getPortalWarpBottomLeftPos(pos, context
                .getPlacementHorizontalFacing().getOpposite());
        final BlockPos portalWarpBottomRightPos = this.getPortalWarpBottomRightPos(pos, context
                .getPlacementHorizontalFacing().getOpposite());

        if (pos.getY() < 255 && portalWarpTopPos.getY() < 255 && context.getWorld().getBlockState(pos.up(2))
                .isReplaceable(context) && portalWarpTopLeftPos.getY() < 255 && context.getWorld().getBlockState(
                        portalWarpTopLeftPos).isReplaceable(context) && portalWarpTopRightPos.getY() < 255 && context
                                .getWorld().getBlockState(portalWarpTopRightPos).isReplaceable(context)
                && portalWarpMiddlePos.getY() < 255 && context.getWorld().getBlockState(pos.up()).isReplaceable(context)
                && portalWarpMiddleLeftPos.getY() < 255 && context.getWorld().getBlockState(portalWarpMiddleLeftPos)
                        .isReplaceable(context) && portalWarpMiddleRightPos.getY() < 255 && context.getWorld()
                                .getBlockState(portalWarpMiddleRightPos).isReplaceable(context)
                && portalWarpBottomLeftPos.getY() < 255 && context.getWorld().getBlockState(portalWarpBottomLeftPos)
                        .isReplaceable(context) && portalWarpBottomRightPos.getY() < 255 && context.getWorld()
                                .getBlockState(portalWarpBottomRightPos).isReplaceable(context)) return this
                                        .getDefaultState().with(PortalWarp.FACING, context
                                                .getPlacementHorizontalFacing().getOpposite()).with(PortalWarp.PART,
                                                        PortalWarpPart.BOTTOM).with(PortalWarp.WATERLOGGED, ifluidstate
                                                                .isTagged(FluidTags.WATER) && ifluidstate
                                                                        .getLevel() == 8).with(PortalWarp.ACTIVE, true);
        return null;
    }

    @Override
    protected void fillStateContainer(final StateContainer.Builder<Block, BlockState> builder)
    {
        builder.add(PortalWarp.PART, PortalWarp.FACING, PortalWarp.WATERLOGGED, PortalWarp.ACTIVE);
    }

    @Override
    public BlockBase setInfoBlockName(final String infoname)
    {
        this.infoname = infoname;
        return this;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void addInformation(final ItemStack stack, final IBlockReader worldIn, final List<ITextComponent> tooltip,
            final ITooltipFlag flagIn)
    {
        String message;
        if (Screen.hasShiftDown()) message = I18n.format("legendblock." + this.infoname + ".tooltip");
        else message = I18n.format("pokecube.tooltip.advanced");
        tooltip.add(new TranslationTextComponent(message));
    }

    @Override
    public ActionResultType onBlockActivated(final BlockState state, final World worldIn, final BlockPos pos,
            final PlayerEntity entity, final Hand hand, final BlockRayTraceResult hit)
    {
        if (!state.get(PortalWarp.ACTIVE)) return ActionResultType.FAIL;
        if (worldIn instanceof ServerWorld)
        {
            PortalActiveFunction.executeProcedure(pos, state, (ServerWorld) worldIn);
            this.setActiveState(worldIn, pos, state, false);
            final Direction facing = state.get(PortalWarp.FACING);
            final BlockPos middle = this.getPortalWarpPos(pos, state.get(PortalWarp.PART), facing).up();
            final TileEntity tile = worldIn.getTileEntity(middle);
            if (tile instanceof RingTile) ((RingTile) tile).activatePortal();
        }
        return ActionResultType.SUCCESS;
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void animateTick(final BlockState state, final World world, final BlockPos pos, final Random random)
    {
        super.animateTick(state, world, pos, random);
        final int x = pos.getX();
        final int y = pos.getY();
        final int z = pos.getZ();
        for (int l = 0; l < 4; ++l)
        {
            final double d0 = x + random.nextFloat();
            final double d1 = y + random.nextFloat();
            final double d2 = z + random.nextFloat();
            final double d3 = (random.nextFloat() - 0.5D) * 0.6;
            final double d4 = (random.nextFloat() - 0.5D) * 0.6;
            final double d5 = (random.nextFloat() - 0.5D) * 0.6;
            world.addParticle(ParticleTypes.PORTAL, d0, d1, d2, d3, d4, d5);
        }
    }
}
