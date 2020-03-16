package pokecube.legends.blocks;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.HorizontalBlock;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.fluid.Fluids;
import net.minecraft.fluid.IFluidState;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.DirectionProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Direction;
import net.minecraftforge.common.ToolType;

public class Rotates extends BlockBase
{
    private static final BooleanProperty   WATERLOGGED = BlockStateProperties.WATERLOGGED;
    private static final DirectionProperty FACING      = HorizontalBlock.HORIZONTAL_FACING;

    public Rotates(final String name, final Material material, final float hardness, final float resistance,
            final SoundType sound)
    {
        super(name, material, hardness, resistance, sound);
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
        final IFluidState ifluidstate = context.getWorld().getFluidState(context.getPos());
        return this.getDefaultState().with(Rotates.FACING, context.getPlacementHorizontalFacing().getOpposite()).with(
                Rotates.WATERLOGGED, ifluidstate.isTagged(FluidTags.WATER) && ifluidstate.getLevel() == 8);
    }

    // Adds Waterlogging
    @SuppressWarnings("deprecation")
    @Override
    public IFluidState getFluidState(final BlockState state)
    {
        return state.get(Rotates.WATERLOGGED) ? Fluids.WATER.getStillFluidState(false) : super.getFluidState(state);
    }
}
