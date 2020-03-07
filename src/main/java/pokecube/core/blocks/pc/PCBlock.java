package pokecube.core.blocks.pc;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.HorizontalBlock;
import net.minecraft.block.IWaterLoggable;
import net.minecraft.block.material.Material;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.fluid.Fluids;
import net.minecraft.fluid.IFluidState;
import net.minecraft.inventory.container.SimpleNamedContainerProvider;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.DirectionProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tags.FluidTags;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.shapes.IBooleanFunction;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraftforge.common.ToolType;
import pokecube.core.inventory.pc.PCContainer;
import pokecube.core.inventory.pc.PCInventory;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import javax.annotation.Nullable;

import static net.minecraft.util.math.shapes.VoxelShapes.combineAndSimplify;

public class PCBlock extends HorizontalBlock implements IWaterLoggable
{

    private static final Map<Direction, VoxelShape> PC_TOP = new HashMap<>();
    private static final Map<Direction, VoxelShape> PC_BASE = new HashMap<>();
    private static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;

    public static final DirectionProperty FACING = HorizontalBlock.HORIZONTAL_FACING;
    final boolean                         top;
    final boolean                         needsBase;

    //Precise selection box
    static
    {
        PC_TOP.put(Direction.NORTH,
                combineAndSimplify(makeCuboidShape(0.34, 0.05, 5.61, 15.66, 16.81, 15.83),
                        makeCuboidShape(0.26, 0.05, 9.71, 15.74, 16.94, 11.72), IBooleanFunction.OR)
        );
        PC_TOP.put(Direction.EAST,
                combineAndSimplify(makeCuboidShape(0.17, 0.05, 0.34, 10.39, 16.81, 15.66),
                        makeCuboidShape(4.28, 0.05, 0.26, 6.29, 16.94, 15.74), IBooleanFunction.OR)
        );
        PC_TOP.put(Direction.SOUTH,
                combineAndSimplify(makeCuboidShape(0.34, 0.05, 0.17, 15.66, 16.81, 10.39),
                        makeCuboidShape(0.26, 0.05, 4.28, 15.74, 16.94, 6.29), IBooleanFunction.OR)
        );
        PC_TOP.put(Direction.WEST,
                combineAndSimplify(makeCuboidShape(5.61, 0.05, 0.34, 15.83, 16.81, 15.66),
                        makeCuboidShape(9.71, 0.05, 0.26, 11.72, 16.94, 15.74), IBooleanFunction.OR)
        );
        PC_BASE.put(Direction.NORTH,
                combineAndSimplify(makeCuboidShape(0.34, 0.05, 5.61, 15.66, 16.05, 15.83),
                        combineAndSimplify(makeCuboidShape(1.46, 0.04, 2.3, 14.54, 14.95, 6.62),
                                combineAndSimplify(makeCuboidShape(1.39, 12.06, 0.81, 14.61, 18.16, 7.47),
                                        makeCuboidShape(0.26, 0.05, 9.72, 15.74, 16.049, 11.72),
                                        IBooleanFunction.OR), IBooleanFunction.OR), IBooleanFunction.OR)
        );
        PC_BASE.put(Direction.EAST,
                combineAndSimplify(makeCuboidShape(0.17, 0.05, 0.34, 10.39, 16.05, 15.66),
                        combineAndSimplify(makeCuboidShape(9.38, 0.04, 1.46, 13.7, 14.95, 14.54),
                                combineAndSimplify(makeCuboidShape(8.53, 12.06, 1.39, 15.19, 18.16, 14.61),
                                        makeCuboidShape(4.28, 0.05, 0.26, 6.28, 16.049, 15.74),
                                        IBooleanFunction.OR), IBooleanFunction.OR), IBooleanFunction.OR)
        );
        PC_BASE.put(Direction.SOUTH,
                combineAndSimplify(makeCuboidShape(0.34, 0.05, 0.17, 15.66, 16.05, 10.39),
                        combineAndSimplify(makeCuboidShape(1.46, 0.04, 9.38, 14.54, 14.95, 13.7),
                                combineAndSimplify(makeCuboidShape(1.39, 12.06, 8.53, 14.61, 18.16, 15.19),
                                        makeCuboidShape(0.26, 0.05, 4.28, 15.74, 16.049, 6.28),
                                        IBooleanFunction.OR), IBooleanFunction.OR), IBooleanFunction.OR)
        );
        PC_BASE.put(Direction.WEST,
                combineAndSimplify(makeCuboidShape(5.61, 0.05, 0.34, 15.83, 16.05, 15.66),
                        combineAndSimplify(makeCuboidShape(2.3, 0.04, 1.46, 6.62, 14.95, 14.54),
                                combineAndSimplify(makeCuboidShape(0.81, 12.06, 1.39, 7.47, 18.16, 14.61),
                                        makeCuboidShape(9.72, 0.05, 0.26, 11.72, 16.049, 15.74),
                                        IBooleanFunction.OR), IBooleanFunction.OR), IBooleanFunction.OR)
        );
    }

    //Precise selection box
    public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context)
    {
        if (this.top)
        {
            return PC_TOP.get(state.get(FACING));
        }
        else
        {
            return PC_BASE.get(state.get(FACING));
        }
    }

    public PCBlock(final Properties properties, final boolean top)
    {
        this(properties, top, true);
    }

    //Default States
    public PCBlock(final Properties properties, final boolean top, final boolean needsBase)
    {
        super(Properties.create(Material.IRON)
                .hardnessAndResistance(3.0f, 5.0f)
                .harvestTool(ToolType.PICKAXE));
        this.top = top;
        this.needsBase = needsBase;
        this.setDefaultState(this.stateContainer.getBaseState()
                .with(FACING, Direction.NORTH)
                .with(WATERLOGGED, false));
    }

    @Override
    public TileEntity createTileEntity(final BlockState state, final IBlockReader world)
    {
        return new PCTile();
    }

    @Override
    protected void fillStateContainer(final StateContainer.Builder<Block, BlockState> builder)
    {
        builder.add(FACING, WATERLOGGED);
    }

    //Waterloggging on placement
    @Override
    public BlockState getStateForPlacement(final BlockItemUseContext context)
    {
        IFluidState ifluidstate = context.getWorld().getFluidState(context.getPos());
        return Objects.requireNonNull(super.getStateForPlacement(context))
           .with(FACING, context.getPlacementHorizontalFacing().getOpposite())
           .with(WATERLOGGED, ifluidstate.isTagged(FluidTags.WATER) && ifluidstate.getLevel() == 8);
    }

    @Override
    public boolean hasTileEntity(final BlockState state)
    {
        return true;
    }

    @Override
    public boolean onBlockActivated(final BlockState state, final World world, final BlockPos pos,
            final PlayerEntity player, final Hand hand, final BlockRayTraceResult hit)
    {
        if (this.top)
        {
            if (!this.needsBase || world.getBlockState(pos.down()).getBlock() instanceof PCBlock)
                if (player instanceof ServerPlayerEntity) player.openContainer(new SimpleNamedContainerProvider((id,
                        playerInventory, playerIn) -> new PCContainer(id, playerInventory, PCInventory.getPC(playerIn)),
                        player.getDisplayName()));
            return true;
        }
        else {
            return false;
        }
    }

    //Adds Waterlogging
    @SuppressWarnings("deprecation")
    @Override
    public IFluidState getFluidState(BlockState state)
    {
        return state.get(WATERLOGGED) ? Fluids.WATER.getStillFluidState(false) : super.getFluidState(state);
    }
}
