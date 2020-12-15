package pokecube.legends.blocks.customblocks;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.HorizontalBlock;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.state.DirectionProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.util.Direction;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;
import net.minecraftforge.common.ToolType;
import pokecube.legends.blocks.BlockBase;

public class Regieleki_Core extends BlockBase {

	public static final DirectionProperty FACING = HorizontalBlock.HORIZONTAL_FACING;

	public Regieleki_Core(final String name, final Material material, final float hardnessresistance, final SoundType sound, final ToolType tool, final int harvest) {
		super(name, material, hardnessresistance, sound, tool, harvest);
		this.setDefaultState(this.stateContainer.getBaseState().with(Regieleki_Core.FACING, Direction.NORTH));
	}

	@Override
    protected void fillStateContainer(final StateContainer.Builder<Block, BlockState> builder) {
		builder.add(Regieleki_Core.FACING);
	}

	@Override
    public BlockState rotate(final BlockState state, final Rotation rot) {
		return state.with(Regieleki_Core.FACING, rot.rotate(state.get(Regieleki_Core.FACING)));
	}

    @Override
    @SuppressWarnings("deprecation")
	public BlockState mirror(final BlockState state, final Mirror mirrorIn) {
		return state.rotate(mirrorIn.toRotation(state.get(Regieleki_Core.FACING)));
	}

	@Override
	public BlockState getStateForPlacement(final BlockItemUseContext context) {
		return this.getDefaultState().with(Regieleki_Core.FACING, context.getPlacementHorizontalFacing().getOpposite());
	}
}
