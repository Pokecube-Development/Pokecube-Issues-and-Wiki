package pokecube.legends.blocks.customblocks;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import javax.annotation.Nullable;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import pokecube.core.PokecubeCore;
import pokecube.legends.blocks.BlockBase;
import pokecube.legends.init.function.PortalActiveFunction;
import pokecube.legends.tileentity.RingTile;
import thut.api.block.ITickTile;

public class PortalWarp extends Rotates implements SimpleWaterloggedBlock, EntityBlock
{
    public static final EnumProperty<PortalWarpPart> PART        = EnumProperty.create("part", PortalWarpPart.class);
    public static final BooleanProperty              ACTIVE      = BooleanProperty.create("active");
    private static final BooleanProperty             WATERLOGGED = BlockStateProperties.WATERLOGGED;
    private static final DirectionProperty           FACING      = HorizontalDirectionalBlock.FACING;

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
    //@formatter:off
    static
    {
        PortalWarp.PORTAL_TOP.put(Direction.NORTH,
            Block.box(0, 0, 6.5, 16, 16, 9.5));
        PortalWarp.PORTAL_TOP.put(Direction.EAST,
            Block.box(6.5, 0, 0, 9.5, 16, 16));
        PortalWarp.PORTAL_TOP.put(Direction.SOUTH,
            Block.box(0, 0, 6.5, 16, 16, 9.5));
        PortalWarp.PORTAL_TOP.put(Direction.WEST,
            Block.box(6.5, 0, 0, 9.5, 16, 16));

        PortalWarp.PORTAL_TOP_OFF.put(Direction.NORTH, Shapes.or(
            Block.box(0, 7.5, 6.5, 3, 9, 9.5),
            Block.box(13, 7.5, 6.5, 16, 9, 9.5),
            Block.box(0, 9, 6.5, 16, 16, 9.5)).optimize());
        PortalWarp.PORTAL_TOP_OFF.put(Direction.EAST, Shapes.or(
            Block.box(6.5, 7.5, 0, 9.5, 9, 3),
            Block.box(6.5, 7.5, 13, 9.5, 9, 16),
            Block.box(6.5, 9, 0, 9.5, 16, 16)).optimize());
        PortalWarp.PORTAL_TOP_OFF.put(Direction.SOUTH, Shapes.or(
            Block.box(0, 7.5, 6.5, 3, 9, 9.5),
            Block.box(13, 7.5, 6.5, 16, 9, 9.5),
            Block.box(0, 9, 6.5, 16, 16, 9.5)).optimize());
        PortalWarp.PORTAL_TOP_OFF.put(Direction.WEST, Shapes.or(
            Block.box(6.5, 7.5, 0, 9.5, 9, 3),
            Block.box(6.5, 7.5, 13, 9.5, 9, 16),
            Block.box(6.5, 9, 0, 9.5, 16, 16)).optimize());

        PortalWarp.PORTAL_TOP_LEFT.put(Direction.NORTH, Shapes.or(
            Block.box(0, 0, 6.5, 13.5, 4.5, 9.5),
            Block.box(0, 4.5, 6.5, 12, 7.5, 9.5),
            Block.box(0, 7.5, 6.5, 10.5, 9, 9.5),
            Block.box(0, 9, 6.5, 9, 10.5, 9.5),
            Block.box(0, 10.5, 6.5, 7.5, 12, 9.5),
            Block.box(0, 12, 6.5, 6, 13.5, 9.5),
            Block.box(0, 13.5, 6.5, 4.5, 14.75, 9.5),
            Block.box(0, 14.75, 6.5, 1.5, 16, 9.5)).optimize());
        PortalWarp.PORTAL_TOP_LEFT.put(Direction.EAST, Shapes.or(
            Block.box(6.5, 0, 0, 9.5, 4.5, 13.5),
            Block.box(6.5, 4.5, 0, 9.5, 7.5, 12),
            Block.box(6.5, 7.5, 0, 9.5, 9, 10.5),
            Block.box(6.5, 9, 0, 9.5, 10.5, 9),
            Block.box(6.5, 10.5, 0, 9.5, 12, 7.5),
            Block.box(6.5, 12, 0, 9.5, 13.5, 6),
            Block.box(6.5, 13.5, 0, 9.5, 14.75, 4.5),
            Block.box(6.5, 14.75, 0, 9.5, 16, 1.5)).optimize());
        PortalWarp.PORTAL_TOP_LEFT.put(Direction.SOUTH, Shapes.or(
            Block.box(2.5, 0, 6.5, 16, 4.5, 9.5),
            Block.box(4, 4.5, 6.5, 16, 7.5, 9.5),
            Block.box(5.5, 7.5, 6.5, 16, 9, 9.5),
            Block.box(7, 9, 6.5, 16, 10.5, 9.5),
            Block.box(8.5, 10.5, 6.5, 16, 12, 9.5),
            Block.box(10, 12, 6.5, 16, 13.5, 9.5),
            Block.box(11.5, 13.5, 6.5, 16, 14.75, 9.5),
            Block.box(14.5, 14.75, 6.5, 16, 16, 9.5)).optimize());
        PortalWarp.PORTAL_TOP_LEFT.put(Direction.WEST, Shapes.or(
            Block.box(6.5, 0, 2.5, 9.5, 4.5, 16),
            Block.box(6.5, 4.5, 4, 9.5, 7.5, 16),
            Block.box(6.5, 7.5, 5.5, 9.5, 9, 16),
            Block.box(6.5, 9, 7, 9.5, 10.5, 16),
            Block.box(6.5, 10.5, 8.5, 9.5, 12, 16),
            Block.box(6.5, 12, 10, 9.5, 13.5, 16),
            Block.box(6.5, 13.5, 11.5, 9.5, 14.75, 16),
            Block.box(6.5, 14.75, 14.5, 9.5, 16, 16)).optimize());

        PortalWarp.PORTAL_TOP_LEFT_OFF.put(Direction.NORTH, Shapes.or(
            Block.box(4.5, 1.5, 6.5, 13.5, 4.5, 9.5),
            Block.box(6, 0, 6.5, 13.5, 1.5, 9.5),
            Block.box(0, 6, 6.5, 12, 7.5, 9.5),
            Block.box(3, 4.5, 6.5, 12, 6, 9.5),
            Block.box(0, 7.5, 6.5, 10.5, 9, 9.5),
            Block.box(0, 9, 6.5, 9, 10.5, 9.5),
            Block.box(0, 10.5, 6.5, 7.5, 12, 9.5),
            Block.box(0, 12, 6.5, 6, 13.5, 9.5),
            Block.box(0, 13.5, 6.5, 4.5, 14.75, 9.5),
            Block.box(0, 14.75, 6.5, 1.5, 16, 9.5)).optimize());
        PortalWarp.PORTAL_TOP_LEFT_OFF.put(Direction.EAST, Shapes.or(
            Block.box(6.5, 1.5, 4.5, 9.5, 4.5, 13.5),
            Block.box(6.5, 0, 6, 9.5, 1.5, 13.5),
            Block.box(6.5, 6, 0, 9.5, 7.5, 12),
            Block.box(6.5, 4.5, 3, 9.5, 6, 12),
            Block.box(6.5, 7.5, 0, 9.5, 9, 10.5),
            Block.box(6.5, 9, 0, 9.5, 10.5, 9),
            Block.box(6.5, 10.5, 0, 9.5, 12, 7.5),
            Block.box(6.5, 12, 0, 9.5, 13.5, 6),
            Block.box(6.5, 13.5, 0, 9.5, 14.75, 4.5),
            Block.box(6.5, 14.75, 0, 9.5, 16, 1.5)).optimize());
        PortalWarp.PORTAL_TOP_LEFT_OFF.put(Direction.SOUTH, Shapes.or(
            Block.box(2.5, 1.5, 6.5, 11.5, 4.5, 9.5),
            Block.box(2.5, 0, 6.5, 10, 1.5, 9.5),
            Block.box(4, 6, 6.5, 16, 7.5, 9.5),
            Block.box(4, 4.5, 6.5, 13, 6, 9.5),
            Block.box(5.5, 7.5, 6.5, 16, 9, 9.5),
            Block.box(7, 9, 6.5, 16, 10.5, 9.5),
            Block.box(8.5, 10.5, 6.5, 16, 12, 9.5),
            Block.box(10, 12, 6.5, 16, 13.5, 9.5),
            Block.box(11.5, 13.5, 6.5, 16, 14.75, 9.5),
            Block.box(14.5, 14.75, 6.5, 16, 16, 9.5)).optimize());
        PortalWarp.PORTAL_TOP_LEFT_OFF.put(Direction.WEST, Shapes.or(
            Block.box(6.5, 1.5, 2.5, 9.5, 4.5, 11.5),
            Block.box(6.5, 0, 2.5, 9.5, 1.5, 10),
            Block.box(6.5, 6, 4, 9.5, 7.5, 16),
            Block.box(6.5, 4.5, 4, 9.5, 6, 13),
            Block.box(6.5, 7.5, 5.5, 9.5, 9, 16),
            Block.box(6.5, 9, 7, 9.5, 10.5, 16),
            Block.box(6.5, 10.5, 8.5, 9.5, 12, 16),
            Block.box(6.5, 12, 10, 9.5, 13.5, 16),
            Block.box(6.5, 13.5, 11.5, 9.5, 14.75, 16),
            Block.box(6.5, 14.75, 14.5, 9.5, 16, 16)).optimize());

        PortalWarp.PORTAL_TOP_RIGHT.put(Direction.NORTH, Shapes.or(
            Block.box(1.5, 0, 6.5, 16, 4.5, 9.5),
            Block.box(3, 4.5, 6.5, 16, 7.5, 9.5),
            Block.box(4.5, 7.5, 6.5, 16, 9, 9.5),
            Block.box(6, 9, 6.5, 16, 10.5, 9.5),
            Block.box(7.5, 10.5, 6.5, 16, 12, 9.5),
            Block.box(9, 12, 6.5, 16, 13.5, 9.5),
            Block.box(11.75, 13.5, 6.5, 16, 14.75, 9.5),
            Block.box(14.5, 14.75, 6.5, 16, 16, 9.5)).optimize());
        PortalWarp.PORTAL_TOP_RIGHT.put(Direction.EAST, Shapes.or(
            Block.box(6.5, 0, 1.5, 9.5, 4.5, 16),
            Block.box(6.5, 4.5, 3, 9.5, 7.5, 16),
            Block.box(6.5, 7.5, 4.5, 9.5, 9, 16),
            Block.box(6.5, 9, 6, 9.5, 10.5, 16),
            Block.box(6.5, 10.5, 7.5, 9.5, 12, 16),
            Block.box(6.5, 12, 9, 9.5, 13.5, 16),
            Block.box(6.5, 13.5, 11.75, 9.5, 14.75, 16),
            Block.box(6.5, 14.75, 14.5, 9.5, 16, 16)).optimize());
        PortalWarp.PORTAL_TOP_RIGHT.put(Direction.SOUTH, Shapes.or(
            Block.box(0, 0, 6.5, 14.5, 4.5, 9.5),
            Block.box(0, 4.5, 6.5, 13, 7.5, 9.5),
            Block.box(0, 7.5, 6.5, 11.5, 9, 9.5),
            Block.box(0, 9, 6.5, 10, 10.5, 9.5),
            Block.box(0, 10.5, 6.5, 8.5, 12, 9.5),
            Block.box(0, 12, 6.5, 7, 13.5, 9.5),
            Block.box(0, 13.5, 6.5, 4.25, 14.75, 9.5),
            Block.box(0, 14.75, 6.5, 1.5, 16, 9.5)).optimize());
        PortalWarp.PORTAL_TOP_RIGHT.put(Direction.WEST, Shapes.or(
            Block.box(6.5, 0, 0, 9.5, 4.5, 14.5),
            Block.box(6.5, 4.5, 0, 9.5, 7.5, 13),
            Block.box(6.5, 7.5, 0, 9.5, 9, 11.5),
            Block.box(6.5, 9, 0, 9.5, 10.5, 10),
            Block.box(6.5, 10.5, 0, 9.5, 12, 8.5),
            Block.box(6.5, 12, 0, 9.5, 13.5, 7),
            Block.box(6.5, 13.5, 0, 9.5, 14.75, 4.25),
            Block.box(6.5, 14.75, 0, 9.5, 16, 1.5)).optimize());

        PortalWarp.PORTAL_TOP_RIGHT_OFF.put(Direction.NORTH, Shapes.or(
            Block.box(1.5, 1.5, 6.5, 11.5, 4.5, 9.5),
            Block.box(1.5, 0, 6.5, 10, 1.5, 9.5),
            Block.box(3, 6, 6.5, 16, 7.5, 9.5),
            Block.box(3, 4.5, 6.5, 13, 6, 9.5),
            Block.box(4.5, 7.5, 6.5, 16, 9, 9.5),
            Block.box(6, 9, 6.5, 16, 10.5, 9.5),
            Block.box(7.5, 10.5, 6.5, 16, 12, 9.5),
            Block.box(9, 12, 6.5, 16, 13.5, 9.5),
            Block.box(11.75, 13.5, 6.5, 16, 14.75, 9.5),
            Block.box(14.5, 14.75, 6.5, 16, 16, 9.5)).optimize());
        PortalWarp.PORTAL_TOP_RIGHT_OFF.put(Direction.EAST, Shapes.or(
            Block.box(6.5, 1.5, 1.5, 9.5, 4.5, 11.5),
            Block.box(6.5, 0, 1.5, 9.5, 1.5, 10),
            Block.box(6.5, 6, 3, 9.5, 7.5, 16),
            Block.box(6.5, 4.5, 3, 9.5, 6, 13),
            Block.box(6.5, 7.5, 4.5, 9.5, 9, 16),
            Block.box(6.5, 9, 6, 9.5, 10.5, 16),
            Block.box(6.5, 10.5, 7.5, 9.5, 12, 16),
            Block.box(6.5, 12, 9, 9.5, 13.5, 16),
            Block.box(6.5, 13.5, 11.75, 9.5, 14.75, 16),
            Block.box(6.5, 14.75, 14.5, 9.5, 16, 16)).optimize());
        PortalWarp.PORTAL_TOP_RIGHT_OFF.put(Direction.SOUTH, Shapes.or(
            Block.box(4.5, 1.5, 6.5, 14.5, 4.5, 9.5),
            Block.box(6, 0, 6.5, 14.5, 1.5, 9.5),
            Block.box(0, 6, 6.5, 13, 7.5, 9.5),
            Block.box(3, 4.5, 6.5, 13, 6, 9.5),
            Block.box(0, 7.5, 6.5, 11.5, 9, 9.5),
            Block.box(0, 9, 6.5, 10, 10.5, 9.5),
            Block.box(0, 10.5, 6.5, 8.5, 12, 9.5),
            Block.box(0, 12, 6.5, 7, 13.5, 9.5),
            Block.box(0, 13.5, 6.5, 4.25, 14.75, 9.5),
            Block.box(0, 14.75, 6.5, 1.5, 16, 9.5)).optimize());
        PortalWarp.PORTAL_TOP_RIGHT_OFF.put(Direction.WEST, Shapes.or(
            Block.box(6.5, 1.5, 4.5, 9.5, 4.5, 14.5),
            Block.box(6.5, 0, 6, 9.5, 1.5, 14.5),
            Block.box(6.5, 6, 0, 9.5, 7.5, 13),
            Block.box(6.5, 4.5, 3, 9.5, 6, 13),
            Block.box(6.5, 7.5, 0, 9.5, 9, 11.5),
            Block.box(6.5, 9, 0, 9.5, 10.5, 10),
            Block.box(6.5, 10.5, 0, 9.5, 12, 8.5),
            Block.box(6.5, 12, 0, 9.5, 13.5, 7),
            Block.box(6.5, 13.5, 0, 9.5, 14.75, 4.25),
            Block.box(6.5, 14.75, 0, 9.5, 16, 1.5)).optimize());

        PortalWarp.PORTAL_MIDDLE.put(Direction.NORTH,
            Block.box(0, 0, 6.5, 16, 16, 9.5));
        PortalWarp.PORTAL_MIDDLE.put(Direction.EAST,
            Block.box(6.5, 0, 0, 9.5, 16, 16));
        PortalWarp.PORTAL_MIDDLE.put(Direction.SOUTH,
            Block.box(0, 0, 6.5, 16, 16, 9.5));
        PortalWarp.PORTAL_MIDDLE.put(Direction.WEST,
            Block.box(6.5, 0, 0, 9.5, 16, 16));

        PortalWarp.PORTAL_MIDDLE_OFF.put(Direction.NORTH,
            Block.box(0, 0, 0, 0, 0, 0));
        PortalWarp.PORTAL_MIDDLE_OFF.put(Direction.EAST,
            Block.box(0, 0, 0, 0, 0, 0));
        PortalWarp.PORTAL_MIDDLE_OFF.put(Direction.SOUTH,
            Block.box(0, 0, 0, 0, 0, 0));
        PortalWarp.PORTAL_MIDDLE_OFF.put(Direction.WEST,
            Block.box(0, 0, 0, 0, 0, 0));

        PortalWarp.PORTAL_MIDDLE_LEFT.put(Direction.NORTH, Shapes.or(
            Block.box(0, 1.5, 6.5, 15, 16, 9.5),
            Block.box(0, 0, 6.5, 13.5, 1.5, 9.5)).optimize());
        PortalWarp.PORTAL_MIDDLE_LEFT.put(Direction.EAST, Shapes.or(
            Block.box(6.5, 1.5, 0, 9.5, 16, 15),
            Block.box(6.5, 0, 0, 9.5, 1.5, 13.5)).optimize());
        PortalWarp.PORTAL_MIDDLE_LEFT.put(Direction.SOUTH, Shapes.or(
            Block.box(1, 1.5, 6.5, 16, 16, 9.5),
            Block.box(2.5, 0, 6.5, 16, 1.5, 9.5)).optimize());
        PortalWarp.PORTAL_MIDDLE_LEFT.put(Direction.WEST, Shapes.or(
            Block.box(6.5, 1.5, 1, 9.5, 16, 16),
            Block.box(6.5, 0, 2.5, 9.5, 1.5, 16)).optimize());

        PortalWarp.PORTAL_MIDDLE_LEFT_OFF.put(Direction.NORTH, Shapes.or(
            Block.box(7.5, 3, 6.5, 15, 14.5, 9.5),
            Block.box(6, 0, 6.5, 13.5, 1.5, 9.5),
            Block.box(6, 1.5, 6.5, 15, 3, 9.5),
            Block.box(6, 14.5, 6.5, 15, 16, 9.5)).optimize());
        PortalWarp.PORTAL_MIDDLE_LEFT_OFF.put(Direction.EAST, Shapes.or(
            Block.box(6.5, 3, 7.5, 9.5, 14.5, 15),
            Block.box(6.5, 0, 6, 9.5, 1.5, 13.5),
            Block.box(6.5, 1.5, 6, 9.5, 3, 15),
            Block.box(6.5, 14.5, 6, 9.5, 16, 15)).optimize());
        PortalWarp.PORTAL_MIDDLE_LEFT_OFF.put(Direction.SOUTH, Shapes.or(
            Block.box(1, 3, 6.5, 8.5, 14.5, 9.5),
            Block.box(2.5, 0, 6.5, 10, 1.5, 9.5),
            Block.box(1, 1.5, 6.5, 10, 3, 9.5),
            Block.box(1, 14.5, 6.5, 10, 16, 9.5)).optimize());
        PortalWarp.PORTAL_MIDDLE_LEFT_OFF.put(Direction.WEST, Shapes.or(
            Block.box(6.5, 3, 1, 9.5, 14.5, 8.5),
            Block.box(6.5, 0, 2.5, 9.5, 1.5, 10),
            Block.box(6.5, 1.5, 1, 9.5, 3, 10),
            Block.box(6.5, 14.5, 1, 9.5, 16, 10)).optimize());

        PortalWarp.PORTAL_MIDDLE_RIGHT.put(Direction.NORTH, Shapes.or(
            Block.box(0, 1.5, 6.5, 16, 16, 9.5),
            Block.box(1.5, 0, 6.5, 16, 1.5, 9.5)).optimize());
        PortalWarp.PORTAL_MIDDLE_RIGHT.put(Direction.EAST, Shapes.or(
            Block.box(6.5, 1.5, 0, 9.5, 16, 16),
            Block.box(6.5, 0, 1.5, 9.5, 1.5, 16)).optimize());
        PortalWarp.PORTAL_MIDDLE_RIGHT.put(Direction.SOUTH, Shapes.or(
            Block.box(0, 1.5, 6.5, 16, 16, 9.5),
            Block.box(0, 0, 6.5, 14.5, 1.5, 9.5)).optimize());
        PortalWarp.PORTAL_MIDDLE_RIGHT.put(Direction.WEST, Shapes.or(
            Block.box(6.5, 1.5, 0, 9.5, 16, 16),
            Block.box(6.5, 0, 0, 9.5, 1.5, 14.5)).optimize());

        PortalWarp.PORTAL_MIDDLE_RIGHT_OFF.put(Direction.NORTH, Shapes.or(
            Block.box(0, 3, 6.5, 8.6, 14.5, 9.5),
            Block.box(1.5, 0, 6.5, 10, 1.5, 9.5),
            Block.box(0, 1.5, 6.5, 10, 3, 9.5),
            Block.box(0, 14.5, 6.5, 10, 16, 9.5)).optimize());
        PortalWarp.PORTAL_MIDDLE_RIGHT_OFF.put(Direction.EAST, Shapes.or(
            Block.box(6.5, 3, 0, 9.5, 14.5, 8.6),
            Block.box(6.5, 0, 1.5, 9.5, 1.5, 10),
            Block.box(6.5, 1.5, 0, 9.5, 3, 10),
            Block.box(6.5, 14.5, 0, 9.5, 16, 10)).optimize());
        PortalWarp.PORTAL_MIDDLE_RIGHT_OFF.put(Direction.SOUTH, Shapes.or(
            Block.box(7.4, 3, 6.5, 16, 14.5, 9.5),
            Block.box(6, 0, 6.5, 14.5, 1.5, 9.5),
            Block.box(6, 1.5, 6.5, 16, 3, 9.5),
            Block.box(6, 14.5, 6.5, 16, 16, 9.5)).optimize());
        PortalWarp.PORTAL_MIDDLE_RIGHT_OFF.put(Direction.WEST, Shapes.or(
            Block.box(6.5, 3, 7.4, 9.5, 14.5, 16),
            Block.box(6.5, 0, 6, 9.5, 1.5, 14.5),
            Block.box(6.5, 1.5, 6, 9.5, 3, 16),
            Block.box(6.5, 14.5, 6, 9.5, 16, 16)).optimize());

        PortalWarp.PORTAL_BOTTOM.put(Direction.NORTH,
            Block.box(0, 0, 6.5, 16, 16, 9.5));
        PortalWarp.PORTAL_BOTTOM.put(Direction.EAST,
            Block.box(6.5, 0, 0, 9.5, 16, 16));
        PortalWarp.PORTAL_BOTTOM.put(Direction.SOUTH,
            Block.box(0, 0, 6.5, 16, 16, 9.5));
        PortalWarp.PORTAL_BOTTOM.put(Direction.WEST,
            Block.box(6.5, 0, 0, 9.5, 16, 16));

        PortalWarp.PORTAL_BOTTOM_OFF.put(Direction.NORTH, Shapes.or(
            Block.box(0, 0, 6.5, 16, 9, 9.5),
            Block.box(0, 9, 6.5, 3, 10.5, 9.5),
            Block.box(13, 9, 6.5, 16, 10.5, 9.5)).optimize());
        PortalWarp.PORTAL_BOTTOM_OFF.put(Direction.EAST, Shapes.or(
            Block.box(6.5, 0, 0, 9.5, 9, 16),
            Block.box(6.5, 9, 0, 9.5, 10.5, 3),
            Block.box(6.5, 9, 13, 9.5, 10.5, 16)).optimize());
        PortalWarp.PORTAL_BOTTOM_OFF.put(Direction.SOUTH, Shapes.or(
            Block.box(0, 0, 6.5, 16, 9, 9.5),
            Block.box(0, 9, 6.5, 3, 10.5, 9.5),
            Block.box(13, 9, 6.5, 16, 10.5, 9.5)).optimize());
        PortalWarp.PORTAL_BOTTOM_OFF.put(Direction.WEST, Shapes.or(
            Block.box(6.5, 0, 0, 9.5, 9, 16),
            Block.box(6.5, 9, 0, 9.5, 10.5, 3),
            Block.box(6.5, 9, 13, 9.5, 10.5, 16)).optimize());

        PortalWarp.PORTAL_BOTTOM_LEFT.put(Direction.NORTH, Shapes.or(
            Block.box(0, 1.5, 6.5, 3, 3, 9.5),
            Block.box(0, 3, 6.5, 4.5, 4.5, 9.5),
            Block.box(0, 4.5, 6.5, 6, 6, 9.5),
            Block.box(0, 6, 6.5, 7.5, 7.5, 9.5),
            Block.box(0, 7.5, 6.5, 9, 9, 9.5),
            Block.box(0, 9, 6.5, 10.5, 10.5, 9.5),
            Block.box(0, 10.5, 6.5, 12, 13.5, 9.5),
            Block.box(0, 13.5, 6.5, 13.5, 16, 9.5)).optimize());
        PortalWarp.PORTAL_BOTTOM_LEFT.put(Direction.EAST, Shapes.or(
            Block.box(6.5, 1.5, 0, 9.5, 3, 3),
            Block.box(6.5, 3, 0, 9.5, 4.5, 4.5),
            Block.box(6.5, 4.5, 0, 9.5, 6, 6),
            Block.box(6.5, 6, 0, 9.5, 7.5, 7.5),
            Block.box(6.5, 7.5, 0, 9.5, 9, 9),
            Block.box(6.5, 9, 0, 9.5, 10.5, 10.5),
            Block.box(6.5, 10.5, 0, 9.5, 13.5, 12),
            Block.box(6.5, 13.5, 0, 9.5, 16, 13.5)).optimize());
        PortalWarp.PORTAL_BOTTOM_LEFT.put(Direction.SOUTH, Shapes.or(
            Block.box(13, 1.5, 6.5, 16, 3, 9.5),
            Block.box(11.5, 3, 6.5, 16, 4.5, 9.5),
            Block.box(10, 4.5, 6.5, 16, 6, 9.5),
            Block.box(8.5, 6, 6.5, 16, 7.5, 9.5),
            Block.box(7, 7.5, 6.5, 16, 9, 9.5),
            Block.box(5.5, 9, 6.5, 16, 10.5, 9.5),
            Block.box(4, 10.5, 6.5, 16, 13.5, 9.5),
            Block.box(2.5, 13.5, 6.5, 16, 16, 9.5)).optimize());
        PortalWarp.PORTAL_BOTTOM_LEFT.put(Direction.WEST, Shapes.or(
            Block.box(6.5, 1.5, 13, 9.5, 3, 16),
            Block.box(6.5, 3, 11.5, 9.5, 4.5, 16),
            Block.box(6.5, 4.5, 10, 9.5, 6, 16),
            Block.box(6.5, 6, 8.5, 9.5, 7.5, 16),
            Block.box(6.5, 7.5, 7, 9.5, 9, 16),
            Block.box(6.5, 9, 5.5, 9.5, 10.5, 16),
            Block.box(6.5, 10.5, 4, 9.5, 13.5, 16),
            Block.box(6.5, 13.5, 2.5, 9.5, 16, 16)).optimize());

        PortalWarp.PORTAL_BOTTOM_LEFT_OFF.put(Direction.NORTH, Shapes.or(
            Block.box(0, 1.5, 6.5, 3, 3, 9.5),
            Block.box(0, 3, 6.5, 4.5, 4.5, 9.5),
            Block.box(0, 4.5, 6.5, 6, 6, 9.5),
            Block.box(0, 6, 6.5, 7.5, 7.5, 9.5),
            Block.box(0, 7.5, 6.5, 9, 9, 9.5),
            Block.box(0, 9, 6.5, 10.5, 10.5, 9.5),
            Block.box(0, 10.5, 6.5, 12, 12, 9.5),
            Block.box(3, 12, 6.5, 12, 13.5, 9.5),
            Block.box(4.5, 13.5, 6.5, 13.5, 16, 9.5)).optimize());
        PortalWarp.PORTAL_BOTTOM_LEFT_OFF.put(Direction.EAST, Shapes.or(
            Block.box(6.5, 1.5, 0, 9.5, 3, 3),
            Block.box(6.5, 3, 0, 9.5, 4.5, 4.5),
            Block.box(6.5, 4.5, 0, 9.5, 6, 6),
            Block.box(6.5, 6, 0, 9.5, 7.5, 7.5),
            Block.box(6.5, 7.5, 0, 9.5, 9, 9),
            Block.box(6.5, 9, 0, 9.5, 10.5, 10.5),
            Block.box(6.5, 10.5, 0, 9.5, 12, 12),
            Block.box(6.5, 12, 3, 9.5, 13.5, 12),
            Block.box(6.5, 13.5, 4.5, 9.5, 16, 13.5)).optimize());
        PortalWarp.PORTAL_BOTTOM_LEFT_OFF.put(Direction.SOUTH, Shapes.or(
            Block.box(13, 1.5, 6.5, 16, 3, 9.5),
            Block.box(11.5, 3, 6.5, 16, 4.5, 9.5),
            Block.box(10, 4.5, 6.5, 16, 6, 9.5),
            Block.box(8.5, 6, 6.5, 16, 7.5, 9.5),
            Block.box(7, 7.5, 6.5, 16, 9, 9.5),
            Block.box(5.5, 9, 6.5, 16, 10.5, 9.5),
            Block.box(4, 10.5, 6.5, 16, 12, 9.5),
            Block.box(4, 12, 6.5, 13, 13.5, 9.5),
            Block.box(2.5, 13.5, 6.5, 11.5, 16, 9.5)).optimize());
        PortalWarp.PORTAL_BOTTOM_LEFT_OFF.put(Direction.WEST, Shapes.or(
            Block.box(6.5, 1.5, 13, 9.5, 3, 16),
            Block.box(6.5, 3, 11.5, 9.5, 4.5, 16),
            Block.box(6.5, 4.5, 10, 9.5, 6, 16),
            Block.box(6.5, 6, 8.5, 9.5, 7.5, 16),
            Block.box(6.5, 7.5, 7, 9.5, 9, 16),
            Block.box(6.5, 9, 5.5, 9.5, 10.5, 16),
            Block.box(6.5, 10.5, 4, 9.5, 12, 16),
            Block.box(6.5, 12, 4, 9.5, 13.5, 13),
            Block.box(6.5, 13.5, 2.5, 9.5, 16, 11.5)).optimize());

        PortalWarp.PORTAL_BOTTOM_RIGHT.put(Direction.NORTH, Shapes.or(
            Block.box(13.25, 1.5, 6.5, 16, 3, 9.5),
            Block.box(10.5, 3, 6.5, 16, 4.5, 9.5),
            Block.box(9, 4.5, 6.5, 16, 6, 9.5),
            Block.box(7.5, 6, 6.5, 16, 7.5, 9.5),
            Block.box(6, 7.5, 6.5, 16, 9, 9.5),
            Block.box(4.5, 9, 6.5, 16, 10.5, 9.5),
            Block.box(3, 10.5, 6.5, 16, 13.5, 9.5),
            Block.box(1.5, 13.5, 6.5, 16, 16, 9.5)).optimize());
        PortalWarp.PORTAL_BOTTOM_RIGHT.put(Direction.EAST, Shapes.or(
            Block.box(6.5, 1.5, 13.25, 9.5, 3, 16),
            Block.box(6.5, 3, 10.5, 9.5, 4.5, 16),
            Block.box(6.5, 4.5, 9, 9.5, 6, 16),
            Block.box(6.5, 6, 7.5, 9.5, 7.5, 16),
            Block.box(6.5, 7.5, 6, 9.5, 9, 16),
            Block.box(6.5, 9, 4.5, 9.5, 10.5, 16),
            Block.box(6.5, 10.5, 3, 9.5, 13.5, 16),
            Block.box(6.5, 13.5, 1.5, 9.5, 16, 16)).optimize());
        PortalWarp.PORTAL_BOTTOM_RIGHT.put(Direction.SOUTH, Shapes.or(
            Block.box(0, 1.5, 6.5, 2.75, 3, 9.5),
            Block.box(0, 3, 6.5, 5.5, 4.5, 9.5),
            Block.box(0, 4.5, 6.5, 7, 6, 9.5),
            Block.box(0, 6, 6.5, 8.5, 7.5, 9.5),
            Block.box(0, 7.5, 6.5, 10, 9, 9.5),
            Block.box(0, 9, 6.5, 11.5, 10.5, 9.5),
            Block.box(0, 10.5, 6.5, 13, 13.5, 9.5),
            Block.box(0, 13.5, 6.5, 14.5, 16, 9.5)).optimize());
        PortalWarp.PORTAL_BOTTOM_RIGHT.put(Direction.WEST, Shapes.or(
            Block.box(6.5, 1.5, 0, 9.5, 3, 2.75),
            Block.box(6.5, 3, 0, 9.5, 4.5, 5.5),
            Block.box(6.5, 4.5, 0, 9.5, 6, 7),
            Block.box(6.5, 6, 0, 9.5, 7.5, 8.5),
            Block.box(6.5, 7.5, 0, 9.5, 9, 10),
            Block.box(6.5, 9, 0, 9.5, 10.5, 11.5),
            Block.box(6.5, 10.5, 0, 9.5, 13.5, 13),
            Block.box(6.5, 13.5, 0, 9.5, 16, 14.5)).optimize());

        PortalWarp.PORTAL_BOTTOM_RIGHT_OFF.put(Direction.NORTH, Shapes.or(
            Block.box(13.25, 1.5, 6.5, 16, 3, 9.5),
            Block.box(10.5, 3, 6.5, 16, 4.5, 9.5),
            Block.box(9, 4.5, 6.5, 16, 6, 9.5),
            Block.box(7.5, 6, 6.5, 16, 7.5, 9.5),
            Block.box(6, 7.5, 6.5, 16, 9, 9.5),
            Block.box(4.5, 9, 6.5, 16, 10.5, 9.5),
            Block.box(3, 10.5, 6.5, 16, 12, 9.5),
            Block.box(3, 12, 6.5, 13, 13.5, 9.5),
            Block.box(1.5, 13.5, 6.5, 11.5, 16, 9.5)).optimize());
        PortalWarp.PORTAL_BOTTOM_RIGHT_OFF.put(Direction.EAST, Shapes.or(
            Block.box(6.5, 1.5, 13.25, 9.5, 3, 16),
            Block.box(6.5, 3, 10.5, 9.5, 4.5, 16),
            Block.box(6.5, 4.5, 9, 9.5, 6, 16),
            Block.box(6.5, 6, 7.5, 9.5, 7.5, 16),
            Block.box(6.5, 7.5, 6, 9.5, 9, 16),
            Block.box(6.5, 9, 4.5, 9.5, 10.5, 16),
            Block.box(6.5, 10.5, 3, 9.5, 12, 16),
            Block.box(6.5, 12, 3, 9.5, 13.5, 13),
            Block.box(6.5, 13.5, 1.5, 9.5, 16, 11.5)).optimize());
        PortalWarp.PORTAL_BOTTOM_RIGHT_OFF.put(Direction.SOUTH, Shapes.or(
            Block.box(0, 1.5, 6.5, 2.75, 3, 9.5),
            Block.box(0, 3, 6.5, 5.5, 4.5, 9.5),
            Block.box(0, 4.5, 6.5, 7, 6, 9.5),
            Block.box(0, 6, 6.5, 8.5, 7.5, 9.5),
            Block.box(0, 7.5, 6.5, 10, 9, 9.5),
            Block.box(0, 9, 6.5, 11.5, 10.5, 9.5),
            Block.box(0, 10.5, 6.5, 13, 12, 9.5),
            Block.box(3, 12, 6.5, 13, 13.5, 9.5),
            Block.box(4.5, 13.5, 6.5, 14.5, 16, 9.5)).optimize());
        PortalWarp.PORTAL_BOTTOM_RIGHT_OFF.put(Direction.WEST, Shapes.or(
            Block.box(6.5, 1.5, 0, 9.5, 3, 2.75),
            Block.box(6.5, 3, 0, 9.5, 4.5, 5.5),
            Block.box(6.5, 4.5, 0, 9.5, 6, 7),
            Block.box(6.5, 6, 0, 9.5, 7.5, 8.5),
            Block.box(6.5, 7.5, 0, 9.5, 9, 10),
            Block.box(6.5, 9, 0, 9.5, 10.5, 11.5),
            Block.box(6.5, 10.5, 0, 9.5, 12, 13),
            Block.box(6.5, 12, 3, 9.5, 13.5, 13),
            Block.box(6.5, 13.5, 4.5, 9.5, 16, 14.5)).optimize());
    }
    // Precise selection box @formatter:on

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

    @Override
    public VoxelShape getShape(final BlockState state, final BlockGetter worldIn, final BlockPos pos,
            final CollisionContext context)
    {
        final PortalWarpPart part = state.getValue(PortalWarp.PART);
        final Direction dir = state.getValue(PortalWarp.FACING);
        final boolean active = state.getValue(PortalWarp.ACTIVE);
        VoxelShape s = this.getShape(part, dir, active);
        if (s == null)
        {
            s = Shapes.empty();
            PokecubeCore.LOGGER.error("Error with hitbox for {}, {}, {}", part, dir, active);
        }

        return s;
    }

    public PortalWarp(final String name, final Properties props)
    {
        super(name, props);
        this.registerDefaultState(this.stateDefinition.any().setValue(PortalWarp.FACING, Direction.NORTH).setValue(
                PortalWarp.WATERLOGGED, false).setValue(PortalWarp.PART, PortalWarpPart.BOTTOM).setValue(
                        PortalWarp.ACTIVE, true));
    }

    @Override
    public BlockEntity newBlockEntity(final BlockPos pos, final BlockState state)
    {
        // TODO Auto-generated method stub
        return new RingTile(pos, state);
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(final Level world, final BlockState state,
            final BlockEntityType<T> type)
    {
        final PortalWarpPart part = state.getValue(PortalWarp.PART);
        return part == PortalWarpPart.MIDDLE ? ITickTile.getTicker(world, state, type) : null;
    }

    /*
     * Make Portal in Move
     */
    public void place(final Level world, final BlockPos pos, final Direction direction)
    {
        final BlockState state = this.defaultBlockState().setValue(PortalWarp.PART, PortalWarpPart.BOTTOM).setValue(
                PortalWarp.FACING, direction);
        world.setBlock(pos, state, 3);
        this.place(world, pos, state, direction.getOpposite());
    }

    public void place(final Level world, final BlockPos pos, final BlockState state, final Direction direction)
    {
        final BlockPos portalWarpTopLeftPos = this.getPortalWarpTopLeftPos(pos, direction);
        final BlockPos portalWarpTopRightPos = this.getPortalWarpTopRightPos(pos, direction);
        final BlockPos portalWarpMiddleLeftPos = this.getPortalWarpMiddleLeftPos(pos, direction);
        final BlockPos portalWarpMiddleRightPos = this.getPortalWarpMiddleRightPos(pos, direction);
        final BlockPos portalWarpBottomLeftPos = this.getPortalWarpBottomLeftPos(pos, direction);
        final BlockPos portalWarpBottomRightPos = this.getPortalWarpBottomRightPos(pos, direction);

        final FluidState topFluidState = world.getFluidState(pos.above(2));
        final FluidState topWestFluidState = world.getFluidState(pos.above(2).west());
        final FluidState topEastFluidState = world.getFluidState(pos.above(2).east());
        final FluidState middleFluidState = world.getFluidState(pos.above());
        final FluidState middleWestFluidState = world.getFluidState(pos.above().west());
        final FluidState middleEastFluidState = world.getFluidState(pos.above().east());
        final FluidState bottomWestFluidState = world.getFluidState(pos.west());
        final FluidState bottomEastFluidState = world.getFluidState(pos.east());

        world.setBlock(portalWarpBottomLeftPos, state.setValue(PortalWarp.PART, PortalWarpPart.BOTTOM_LEFT).setValue(
                PortalWarp.WATERLOGGED, bottomWestFluidState.getType() == Fluids.WATER), 3);
        world.setBlock(portalWarpBottomRightPos, state.setValue(PortalWarp.PART, PortalWarpPart.BOTTOM_RIGHT).setValue(
                PortalWarp.WATERLOGGED, bottomEastFluidState.getType() == Fluids.WATER), 3);
        world.setBlock(pos.above(), state.setValue(PortalWarp.PART, PortalWarpPart.MIDDLE).setValue(
                PortalWarp.WATERLOGGED, middleFluidState.getType() == Fluids.WATER), 3);
        world.setBlock(portalWarpMiddleLeftPos, state.setValue(PortalWarp.PART, PortalWarpPart.MIDDLE_LEFT).setValue(
                PortalWarp.WATERLOGGED, middleWestFluidState.getType() == Fluids.WATER), 3);
        world.setBlock(portalWarpMiddleRightPos, state.setValue(PortalWarp.PART, PortalWarpPart.MIDDLE_RIGHT).setValue(
                PortalWarp.WATERLOGGED, middleEastFluidState.getType() == Fluids.WATER), 3);
        world.setBlock(pos.above(2), state.setValue(PortalWarp.PART, PortalWarpPart.TOP).setValue(
                PortalWarp.WATERLOGGED, topFluidState.getType() == Fluids.WATER), 3);
        world.setBlock(portalWarpTopLeftPos, state.setValue(PortalWarp.PART, PortalWarpPart.TOP_LEFT).setValue(
                PortalWarp.WATERLOGGED, topWestFluidState.getType() == Fluids.WATER), 3);
        world.setBlock(portalWarpTopRightPos, state.setValue(PortalWarp.PART, PortalWarpPart.TOP_RIGHT).setValue(
                PortalWarp.WATERLOGGED, topEastFluidState.getType() == Fluids.WATER), 3);
    }

    public void remove(final Level world, final BlockPos pos, final BlockState state)
    {
        final Direction facing = state.getValue(PortalWarp.FACING);

        final BlockPos portalWarpPos = this.getPortalWarpPos(pos, state.getValue(PortalWarp.PART), facing);
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
    public void setPlacedBy(final Level world, final BlockPos pos, final BlockState state,
            @Nullable final LivingEntity entity, final ItemStack stack)
    {
        if (entity != null)
        {
            final BlockPos portalWarpTopLeftPos = this.getPortalWarpTopLeftPos(pos, entity.getDirection());
            final BlockPos portalWarpTopRightPos = this.getPortalWarpTopRightPos(pos, entity.getDirection());
            final BlockPos portalWarpMiddleLeftPos = this.getPortalWarpMiddleLeftPos(pos, entity.getDirection());
            final BlockPos portalWarpMiddleRightPos = this.getPortalWarpMiddleRightPos(pos, entity.getDirection());
            final BlockPos portalWarpBottomLeftPos = this.getPortalWarpBottomLeftPos(pos, entity.getDirection());
            final BlockPos portalWarpBottomRightPos = this.getPortalWarpBottomRightPos(pos, entity.getDirection());

            final FluidState topFluidState = world.getFluidState(pos.above(2));
            final FluidState topLeftFluidState = world.getFluidState(portalWarpTopLeftPos);
            final FluidState topRightFluidState = world.getFluidState(portalWarpTopRightPos);
            final FluidState middleFluidState = world.getFluidState(pos.above());
            final FluidState middleLeftFluidState = world.getFluidState(portalWarpMiddleLeftPos);
            final FluidState middleRightFluidState = world.getFluidState(portalWarpMiddleRightPos);
            final FluidState bottomLeftFluidState = world.getFluidState(portalWarpBottomLeftPos);
            final FluidState bottomRightFluidState = world.getFluidState(portalWarpBottomRightPos);

            world.setBlock(portalWarpBottomLeftPos, state.setValue(PortalWarp.PART, PortalWarpPart.BOTTOM_LEFT)
                    .setValue(PortalWarp.WATERLOGGED, bottomLeftFluidState.getType() == Fluids.WATER), 3);
            world.setBlock(portalWarpBottomRightPos, state.setValue(PortalWarp.PART, PortalWarpPart.BOTTOM_RIGHT)
                    .setValue(PortalWarp.WATERLOGGED, bottomRightFluidState.getType() == Fluids.WATER), 3);
            world.setBlock(pos.above(), state.setValue(PortalWarp.PART, PortalWarpPart.MIDDLE).setValue(
                    PortalWarp.WATERLOGGED, middleFluidState.getType() == Fluids.WATER), 3);
            world.setBlock(portalWarpMiddleLeftPos, state.setValue(PortalWarp.PART, PortalWarpPart.MIDDLE_LEFT)
                    .setValue(PortalWarp.WATERLOGGED, middleLeftFluidState.getType() == Fluids.WATER), 3);
            world.setBlock(portalWarpMiddleRightPos, state.setValue(PortalWarp.PART, PortalWarpPart.MIDDLE_RIGHT)
                    .setValue(PortalWarp.WATERLOGGED, middleRightFluidState.getType() == Fluids.WATER), 3);
            world.setBlock(pos.above(2), state.setValue(PortalWarp.PART, PortalWarpPart.TOP).setValue(
                    PortalWarp.WATERLOGGED, topFluidState.getType() == Fluids.WATER), 3);
            world.setBlock(portalWarpTopLeftPos, state.setValue(PortalWarp.PART, PortalWarpPart.TOP_LEFT).setValue(
                    PortalWarp.WATERLOGGED, topLeftFluidState.getType() == Fluids.WATER), 3);
            world.setBlock(portalWarpTopRightPos, state.setValue(PortalWarp.PART, PortalWarpPart.TOP_RIGHT).setValue(
                    PortalWarp.WATERLOGGED, topRightFluidState.getType() == Fluids.WATER), 3);
        }
    }

    // Breaking Portal breaks both parts and returns one item only
    @Override
    public void playerWillDestroy(final Level world, final BlockPos pos, final BlockState state, final Player player)
    {
        final Direction facing = state.getValue(PortalWarp.FACING);

        final BlockPos portalWarpPos = this.getPortalWarpPos(pos, state.getValue(PortalWarp.PART), facing);
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
        super.playerWillDestroy(world, pos, state, player);
    }

    private BlockPos getPortalWarpTopPos(final BlockPos base, final Direction facing)
    {
        switch (facing)
        {
        case NORTH:
            return base.above(2);
        case EAST:
            return base.above(2);
        case SOUTH:
            return base.above(2);
        case WEST:
            return base.above(2);
        default:
            return base.above(2);
        }
    }

    private BlockPos getPortalWarpTopLeftPos(final BlockPos base, final Direction facing)
    {
        switch (facing)
        {
        case NORTH:
            return base.above(2).west();
        case EAST:
            return base.above(2).north();
        case SOUTH:
            return base.above(2).east();
        case WEST:
            return base.above(2).south();
        default:
            return base.above(2).east();
        }
    }

    private BlockPos getPortalWarpTopRightPos(final BlockPos base, final Direction facing)
    {
        switch (facing)
        {
        case NORTH:
            return base.above(2).east();
        case EAST:
            return base.above(2).south();
        case SOUTH:
            return base.above(2).west();
        case WEST:
            return base.above(2).north();
        default:
            return base.above(2).west();
        }
    }

    private BlockPos getPortalWarpMiddlePos(final BlockPos base, final Direction facing)
    {
        switch (facing)
        {
        case NORTH:
            return base.above();
        case EAST:
            return base.above();
        case SOUTH:
            return base.above();
        case WEST:
            return base.above();
        default:
            return base.above();
        }
    }

    private BlockPos getPortalWarpMiddleLeftPos(final BlockPos base, final Direction facing)
    {
        switch (facing)
        {
        case NORTH:
            return base.above().west();
        case EAST:
            return base.above().north();
        case SOUTH:
            return base.above().east();
        case WEST:
            return base.above().south();
        default:
            return base.above().east();
        }
    }

    private BlockPos getPortalWarpMiddleRightPos(final BlockPos base, final Direction facing)
    {
        switch (facing)
        {
        case NORTH:
            return base.above().east();
        case EAST:
            return base.above().south();
        case SOUTH:
            return base.above().west();
        case WEST:
            return base.above().north();
        default:
            return base.above().west();
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
                return pos.below(2);
            case TOP_LEFT:
                return pos.below(2).west();
            case TOP_RIGHT:
                return pos.below(2).east();
            case MIDDLE:
                return pos.below();
            case MIDDLE_LEFT:
                return pos.below().west();
            case MIDDLE_RIGHT:
                return pos.below().east();
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
                return pos.below(2);
            case TOP_LEFT:
                return pos.below(2).north();
            case TOP_RIGHT:
                return pos.below(2).south();
            case MIDDLE:
                return pos.below();
            case MIDDLE_LEFT:
                return pos.below().north();
            case MIDDLE_RIGHT:
                return pos.below().south();
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
                return pos.below(2);
            case TOP_LEFT:
                return pos.below(2).east();
            case TOP_RIGHT:
                return pos.below(2).west();
            case MIDDLE:
                return pos.below();
            case MIDDLE_LEFT:
                return pos.below().east();
            case MIDDLE_RIGHT:
                return pos.below().west();
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
                return pos.below(2);
            case TOP_LEFT:
                return pos.below(2).south();
            case TOP_RIGHT:
                return pos.below(2).north();
            case MIDDLE:
                return pos.below();
            case MIDDLE_LEFT:
                return pos.below().south();
            case MIDDLE_RIGHT:
                return pos.below().north();
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

    public void setActiveState(final Level world, final BlockPos pos, final BlockState state, final boolean active)
    {
        final Direction facing = state.getValue(PortalWarp.FACING);

        final BlockPos portalWarpPos = this.getPortalWarpPos(pos, state.getValue(PortalWarp.PART), facing);
        BlockState PortalWarpBlockState = world.getBlockState(portalWarpPos);
        if (PortalWarpBlockState.getBlock() == this && !pos.equals(portalWarpPos)) world.setBlockAndUpdate(
                portalWarpPos, PortalWarpBlockState.setValue(PortalWarp.ACTIVE, active));

        BlockPos portalWarpPartPos = this.getPortalWarpTopPos(portalWarpPos, facing);
        PortalWarpBlockState = world.getBlockState(portalWarpPartPos);
        if (PortalWarpBlockState.getBlock() == this && !pos.equals(portalWarpPartPos)) world.setBlockAndUpdate(
                portalWarpPartPos, PortalWarpBlockState.setValue(PortalWarp.ACTIVE, active));

        portalWarpPartPos = this.getPortalWarpTopLeftPos(portalWarpPos, facing);
        PortalWarpBlockState = world.getBlockState(portalWarpPartPos);
        if (PortalWarpBlockState.getBlock() == this && !pos.equals(portalWarpPartPos)) world.setBlockAndUpdate(
                portalWarpPartPos, PortalWarpBlockState.setValue(PortalWarp.ACTIVE, active));

        portalWarpPartPos = this.getPortalWarpTopRightPos(portalWarpPos, facing);
        PortalWarpBlockState = world.getBlockState(portalWarpPartPos);
        if (PortalWarpBlockState.getBlock() == this && !pos.equals(portalWarpPartPos)) world.setBlockAndUpdate(
                portalWarpPartPos, PortalWarpBlockState.setValue(PortalWarp.ACTIVE, active));

        portalWarpPartPos = this.getPortalWarpMiddlePos(portalWarpPos, facing);
        PortalWarpBlockState = world.getBlockState(portalWarpPartPos);
        if (PortalWarpBlockState.getBlock() == this && !pos.equals(portalWarpPartPos)) world.setBlockAndUpdate(
                portalWarpPartPos, PortalWarpBlockState.setValue(PortalWarp.ACTIVE, active));

        portalWarpPartPos = this.getPortalWarpMiddleLeftPos(portalWarpPos, facing);
        PortalWarpBlockState = world.getBlockState(portalWarpPartPos);
        if (PortalWarpBlockState.getBlock() == this && !pos.equals(portalWarpPartPos)) world.setBlockAndUpdate(
                portalWarpPartPos, PortalWarpBlockState.setValue(PortalWarp.ACTIVE, active));

        portalWarpPartPos = this.getPortalWarpMiddleRightPos(portalWarpPos, facing);
        PortalWarpBlockState = world.getBlockState(portalWarpPartPos);
        if (PortalWarpBlockState.getBlock() == this && !pos.equals(portalWarpPartPos)) world.setBlockAndUpdate(
                portalWarpPartPos, PortalWarpBlockState.setValue(PortalWarp.ACTIVE, active));

        portalWarpPartPos = this.getPortalWarpBottomLeftPos(portalWarpPos, facing);
        PortalWarpBlockState = world.getBlockState(portalWarpPartPos);
        if (PortalWarpBlockState.getBlock() == this && !pos.equals(portalWarpPartPos)) world.setBlockAndUpdate(
                portalWarpPartPos, PortalWarpBlockState.setValue(PortalWarp.ACTIVE, active));

        portalWarpPartPos = this.getPortalWarpBottomRightPos(portalWarpPos, facing);
        PortalWarpBlockState = world.getBlockState(portalWarpPartPos);
        if (PortalWarpBlockState.getBlock() == this && !pos.equals(portalWarpPartPos)) world.setBlockAndUpdate(
                portalWarpPartPos, PortalWarpBlockState.setValue(PortalWarp.ACTIVE, active));
    }

    // Breaking the Portal leaves water if underwater
    private void removePart(final Level world, final BlockPos pos, final BlockState state)
    {
        final FluidState fluidState = world.getFluidState(pos);
        if (fluidState.getType() == Fluids.WATER) world.setBlock(pos, fluidState.createLegacyBlock(), 35);
        else world.setBlock(pos, Blocks.AIR.defaultBlockState(), 35);
    }

    // Prevents the Portal from replacing blocks above it and checks for water
    @Override
    public BlockState getStateForPlacement(final BlockPlaceContext context)
    {
        final FluidState ifluidstate = context.getLevel().getFluidState(context.getClickedPos());
        final BlockPos pos = context.getClickedPos();

        final BlockPos portalWarpTopPos = this.getPortalWarpTopPos(pos, context.getHorizontalDirection().getOpposite());
        final BlockPos portalWarpTopLeftPos = this.getPortalWarpTopLeftPos(pos, context.getHorizontalDirection()
                .getOpposite());
        final BlockPos portalWarpTopRightPos = this.getPortalWarpTopRightPos(pos, context.getHorizontalDirection()
                .getOpposite());

        final BlockPos portalWarpMiddlePos = this.getPortalWarpMiddlePos(pos, context.getHorizontalDirection()
                .getOpposite());
        final BlockPos portalWarpMiddleLeftPos = this.getPortalWarpMiddleLeftPos(pos, context.getHorizontalDirection()
                .getOpposite());
        final BlockPos portalWarpMiddleRightPos = this.getPortalWarpMiddleRightPos(pos, context.getHorizontalDirection()
                .getOpposite());

        final BlockPos portalWarpBottomLeftPos = this.getPortalWarpBottomLeftPos(pos, context.getHorizontalDirection()
                .getOpposite());
        final BlockPos portalWarpBottomRightPos = this.getPortalWarpBottomRightPos(pos, context.getHorizontalDirection()
                .getOpposite());

        if (pos.getY() < 255 && portalWarpTopPos.getY() < 255 && context.getLevel().getBlockState(pos.above(2))
                .canBeReplaced(context) && portalWarpTopLeftPos.getY() < 255 && context.getLevel().getBlockState(
                        portalWarpTopLeftPos).canBeReplaced(context) && portalWarpTopRightPos.getY() < 255 && context
                                .getLevel().getBlockState(portalWarpTopRightPos).canBeReplaced(context)
                && portalWarpMiddlePos.getY() < 255 && context.getLevel().getBlockState(pos.above()).canBeReplaced(
                        context) && portalWarpMiddleLeftPos.getY() < 255 && context.getLevel().getBlockState(
                                portalWarpMiddleLeftPos).canBeReplaced(context) && portalWarpMiddleRightPos.getY() < 255
                && context.getLevel().getBlockState(portalWarpMiddleRightPos).canBeReplaced(context)
                && portalWarpBottomLeftPos.getY() < 255 && context.getLevel().getBlockState(portalWarpBottomLeftPos)
                        .canBeReplaced(context) && portalWarpBottomRightPos.getY() < 255 && context.getLevel()
                                .getBlockState(portalWarpBottomRightPos).canBeReplaced(context)) return this
                                        .defaultBlockState().setValue(PortalWarp.FACING, context
                                                .getHorizontalDirection().getOpposite()).setValue(PortalWarp.PART,
                                                        PortalWarpPart.BOTTOM).setValue(PortalWarp.WATERLOGGED,
                                                                ifluidstate.is(FluidTags.WATER) && ifluidstate
                                                                        .getAmount() == 8).setValue(PortalWarp.ACTIVE,
                                                                                true);
        return null;
    }

    @Override
    protected void createBlockStateDefinition(final StateDefinition.Builder<Block, BlockState> builder)
    {
        builder.add(PortalWarp.PART, PortalWarp.FACING, PortalWarp.WATERLOGGED, PortalWarp.ACTIVE);
    }

    @Override
    public BlockBase setToolTip(final String infoname)
    {
        this.infoname = infoname;
        return this;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void appendHoverText(final ItemStack stack, final BlockGetter worldIn, final List<Component> tooltip,
            final TooltipFlag flagIn)
    {
        String message;
        if (Screen.hasShiftDown()) message = I18n.get("legendblock." + this.infoname + ".tooltip");
        else message = I18n.get("pokecube.tooltip.advanced");
        tooltip.add(new TranslatableComponent(message));
    }

    @Override
    public InteractionResult use(final BlockState state, final Level worldIn, final BlockPos pos, final Player entity,
            final InteractionHand hand, final BlockHitResult hit)
    {
        if (!state.getValue(PortalWarp.ACTIVE)) return InteractionResult.FAIL;
        if (worldIn instanceof ServerLevel)
        {
            PortalActiveFunction.executeProcedure(pos, state, (ServerLevel) worldIn);
            this.setActiveState(worldIn, pos, state, false);
            final Direction facing = state.getValue(PortalWarp.FACING);
            final BlockPos middle = this.getPortalWarpPos(pos, state.getValue(PortalWarp.PART), facing).above();
            final BlockEntity tile = worldIn.getBlockEntity(middle);
            if (tile instanceof RingTile) ((RingTile) tile).activatePortal();
        }
        return InteractionResult.SUCCESS;
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void animateTick(final BlockState state, final Level world, final BlockPos pos, final Random random)
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
