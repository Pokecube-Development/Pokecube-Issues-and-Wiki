package pokecube.legends.blocks;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.HorizontalBlock;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.material.MaterialColor;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.state.DirectionProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.util.Direction;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;
import net.minecraftforge.common.ToolType;

public class FaceBlock_Base extends BlockBase {

	public static final DirectionProperty FACING = HorizontalBlock.FACING;

	public FaceBlock_Base(final String name, final Material material, final MaterialColor color, 
			final float hardness, final float resistance, final SoundType sound, final ToolType tool, final int harvest, final boolean hasDrop) {
		super(name, material, color, hardness, resistance, sound, tool, harvest, hasDrop);
		this.registerDefaultState(this.stateDefinition.any().setValue(FaceBlock_Base.FACING, Direction.NORTH));
	}
	
	public FaceBlock_Base(final Material material, final MaterialColor color, 
			final float hardness, final float resistance, final SoundType sound, final ToolType tool, final int harvest, final boolean hasDrop) {
		super(material, color, hardness, resistance, sound, tool, harvest, hasDrop);
		this.registerDefaultState(this.stateDefinition.any().setValue(FaceBlock_Base.FACING, Direction.NORTH));
	}

	@Override
    protected void createBlockStateDefinition(final StateContainer.Builder<Block, BlockState> builder) {
		builder.add(FaceBlock_Base.FACING);
	}

	@Override
    public BlockState rotate(final BlockState state, final Rotation rot) {
		return state.setValue(FaceBlock_Base.FACING, rot.rotate(state.getValue(FaceBlock_Base.FACING)));
	}

    @Override
    @SuppressWarnings("deprecation")
	public BlockState mirror(final BlockState state, final Mirror mirrorIn) {
		return state.rotate(mirrorIn.getRotation(state.getValue(FaceBlock_Base.FACING)));
	}

	@Override
	public BlockState getStateForPlacement(final BlockItemUseContext context) {
		return this.defaultBlockState().setValue(FaceBlock_Base.FACING, context.getHorizontalDirection().getOpposite());
	}
}
