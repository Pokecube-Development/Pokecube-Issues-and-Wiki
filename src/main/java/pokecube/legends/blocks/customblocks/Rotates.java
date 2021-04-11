package pokecube.legends.blocks.customblocks;

import net.minecraft.block.*;
import net.minecraft.block.material.Material;
import net.minecraft.block.material.MaterialColor;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.DirectionProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Direction;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;
import net.minecraftforge.common.ToolType;
import pokecube.legends.blocks.BlockBase;

public class Rotates extends BlockBase implements IWaterLoggable
{
    private static final BooleanProperty   WATERLOGGED = BlockStateProperties.WATERLOGGED;
    private static final DirectionProperty FACING      = HorizontalBlock.FACING;

    public Rotates(final String name, final Material material, final MaterialColor color, final float hardness, final float resistance,
            final SoundType sound, final ToolType tool, final int harvest)
    {
    	super(name, material, color, hardness, resistance, sound, tool, harvest);
        this.registerDefaultState(this.stateDefinition.any().setValue(Rotates.FACING, Direction.NORTH).setValue(
                Rotates.WATERLOGGED, false));
    }

    public Rotates(final String name, final Material material, final MaterialColor color, final ToolType tool, final int level)
    {
        super(name, material, color, tool, level);
        this.registerDefaultState(this.stateDefinition.any().setValue(Rotates.FACING, Direction.NORTH).setValue(
                Rotates.WATERLOGGED, false));
    }

    public Rotates(final String name, final Properties props)
    {
        super(name, props);
        this.registerDefaultState(this.stateDefinition.any().setValue(Rotates.FACING, Direction.NORTH).setValue(
                Rotates.WATERLOGGED, false));
    }

    @Override
    protected void createBlockStateDefinition(final StateContainer.Builder<Block, BlockState> builder)
    {
        builder.add(Rotates.FACING, Rotates.WATERLOGGED);
    }

    @Override
    public BlockState getStateForPlacement(final BlockItemUseContext context)
    {
        final FluidState ifluidstate = context.getLevel().getFluidState(context.getClickedPos());
        return this.defaultBlockState().setValue(Rotates.FACING, context.getHorizontalDirection().getOpposite()).setValue(
                Rotates.WATERLOGGED, ifluidstate.is(FluidTags.WATER) && ifluidstate.getAmount() == 8);
    }

    @Override
    @SuppressWarnings("deprecation")
    public BlockState updateShape(final BlockState state, final Direction facing, final BlockState facingState, final IWorld world, final BlockPos currentPos,
                                  final BlockPos facingPos)
    {
        if (state.getValue(Rotates.WATERLOGGED)) world.getLiquidTicks().scheduleTick(currentPos, Fluids.WATER, Fluids.WATER.getTickDelay(world));
        return super.updateShape(state, facing, facingState, world, currentPos, facingPos);
    }

    // Adds Waterlogging
    @SuppressWarnings("deprecation")
    @Override
    public FluidState getFluidState(final BlockState state)
    {
        return state.getValue(Rotates.WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(state);
    }

    /**
     * Returns the blockstate with the given rotation from the passed
     * blockstate. If inapplicable, returns the passed
     * blockstate.
     *
     * @deprecated call via {@link IBlockState#withRotation(Rotation)} whenever
     *             possible. Implementing/overriding is
     *             fine.
     */
    @Deprecated
    @Override
    public BlockState rotate(final BlockState state, final Rotation rot)
    {
        return state.setValue(Rotates.FACING, rot.rotate(state.getValue(Rotates.FACING)));
    }

    /**
     * Returns the blockstate with the given mirror of the passed blockstate. If
     * inapplicable, returns the passed
     * blockstate.
     *
     * @deprecated call via {@link IBlockState#withMirror(Mirror)} whenever
     *             possible. Implementing/overriding is fine.
     */
    @Deprecated
    @Override
    public BlockState mirror(final BlockState state, final Mirror mirrorIn)
    {
        return state.rotate(mirrorIn.getRotation(state.getValue(Rotates.FACING)));
    }
}
