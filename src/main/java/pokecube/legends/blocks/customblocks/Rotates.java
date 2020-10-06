package pokecube.legends.blocks.customblocks;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.HorizontalBlock;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.fluid.Fluids;
import net.minecraft.fluid.FluidState;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.DirectionProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Direction;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;
import net.minecraftforge.common.ToolType;
import pokecube.legends.blocks.BlockBase;

public class Rotates extends BlockBase
{
    private static final BooleanProperty   WATERLOGGED = BlockStateProperties.WATERLOGGED;
    private static final DirectionProperty FACING      = HorizontalBlock.HORIZONTAL_FACING;

    public Rotates(final String name, final Material material, final float hardness, final float resistance,
            final SoundType sound, final ToolType tool)
    {
        super(name, material, hardness, resistance, sound, tool);
        this.setDefaultState(this.stateContainer.getBaseState().with(Rotates.FACING, Direction.NORTH).with(
                Rotates.WATERLOGGED, false));
    }

    public Rotates(final String name, final Material material, final ToolType tool, final int level)
    {
        super(name, material, tool, level);
        this.setDefaultState(this.stateContainer.getBaseState().with(Rotates.FACING, Direction.NORTH).with(
                Rotates.WATERLOGGED, false));
    }

    public Rotates(final String name, final Properties props)
    {
        super(name, props);
        this.setDefaultState(this.stateContainer.getBaseState().with(Rotates.FACING, Direction.NORTH).with(
                Rotates.WATERLOGGED, false));
    }

    @Override
    protected void fillStateContainer(final StateContainer.Builder<Block, BlockState> builder)
    {
        builder.add(Rotates.FACING, Rotates.WATERLOGGED);
    }

    @Override
    public BlockState getStateForPlacement(final BlockItemUseContext context)
    {
        final FluidState ifluidstate = context.getWorld().getFluidState(context.getPos());
        return this.getDefaultState().with(Rotates.FACING, context.getPlacementHorizontalFacing().getOpposite()).with(
                Rotates.WATERLOGGED, ifluidstate.isTagged(FluidTags.WATER) && ifluidstate.getLevel() == 8);
    }

    // Adds Waterlogging
    @SuppressWarnings("deprecation")
    @Override
    public FluidState getFluidState(final BlockState state)
    {
        return state.get(Rotates.WATERLOGGED) ? Fluids.WATER.getStillFluidState(false) : super.getFluidState(state);
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
        return state.with(Rotates.FACING, rot.rotate(state.get(Rotates.FACING)));
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
        return state.rotate(mirrorIn.toRotation(state.get(Rotates.FACING)));
    }
}
