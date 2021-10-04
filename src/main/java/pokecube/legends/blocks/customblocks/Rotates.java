package pokecube.legends.blocks.customblocks;

import com.minecolonies.api.util.constant.ToolType;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.material.MaterialColor;
import pokecube.legends.blocks.BlockBase;

public class Rotates extends BlockBase implements SimpleWaterloggedBlock
{
    private static final BooleanProperty   WATERLOGGED = BlockStateProperties.WATERLOGGED;
    private static final DirectionProperty FACING      = HorizontalDirectionalBlock.FACING;

    public Rotates(final String name, final Material material, final MaterialColor color, final float hardness, final float resistance,
            final SoundType sound, final ToolType tool, final int harvest, final boolean hasDrop)
    {
    	super(name, material, color, hardness, resistance, sound, tool, harvest, hasDrop);
        this.registerDefaultState(this.stateDefinition.any().setValue(Rotates.FACING, Direction.NORTH).setValue(
                Rotates.WATERLOGGED, false));
    }
    
    public Rotates(final Material material, final MaterialColor color, final float hardness, final float resistance,
            final SoundType sound, final ToolType tool, final int harvest, final boolean hasDrop)
    {
    	super(material, color, hardness, resistance, sound, tool, harvest, hasDrop);
        this.registerDefaultState(this.stateDefinition.any().setValue(Rotates.FACING, Direction.NORTH).setValue(
                Rotates.WATERLOGGED, false));
    }

    public Rotates(String name, Properties props) {
    	super(name, props);
    	this.registerDefaultState(this.stateDefinition.any().setValue(Rotates.FACING, Direction.NORTH).setValue(
                Rotates.WATERLOGGED, false));
	}
    
    public Rotates( Properties props) {
    	super(props);
    	this.registerDefaultState(this.stateDefinition.any().setValue(Rotates.FACING, Direction.NORTH).setValue(
                Rotates.WATERLOGGED, false));
	}

	@Override
    protected void createBlockStateDefinition(final StateDefinition.Builder<Block, BlockState> builder)
    {
        builder.add(Rotates.FACING, Rotates.WATERLOGGED);
    }

    @Override
    public BlockState getStateForPlacement(final BlockPlaceContext context)
    {
        final FluidState ifluidstate = context.getLevel().getFluidState(context.getClickedPos());
        return this.defaultBlockState().setValue(Rotates.FACING, context.getHorizontalDirection().getOpposite()).setValue(
                Rotates.WATERLOGGED, ifluidstate.is(FluidTags.WATER) && ifluidstate.getAmount() == 8);
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
    
    @Override
    @SuppressWarnings("deprecation")
    public BlockState updateShape(final BlockState state, final Direction facing, final BlockState facingState, final LevelAccessor world, final BlockPos currentPos,
                                  final BlockPos facingPos)
    {
        if (state.getValue(Rotates.WATERLOGGED)) world.getLiquidTicks().scheduleTick(currentPos, Fluids.WATER, Fluids.WATER.getTickDelay(world));
        return super.updateShape(state, facing, facingState, world, currentPos, facingPos);
    }
}
