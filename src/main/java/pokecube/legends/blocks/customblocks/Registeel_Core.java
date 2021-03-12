package pokecube.legends.blocks.customblocks;

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
import pokecube.legends.blocks.BlockBase;

public class Registeel_Core extends BlockBase
{

    public static final DirectionProperty FACING = HorizontalBlock.FACING;

    public Registeel_Core(final String name, final Material material, final MaterialColor color, final float hardnessresistance,
            final SoundType sound, final ToolType tool, final int harvest)
    {
        super(name, material, color, hardnessresistance, sound, tool, harvest);
        this.registerDefaultState(this.stateDefinition.any().setValue(Registeel_Core.FACING, Direction.NORTH));
    }

    @Override
    protected void createBlockStateDefinition(final StateContainer.Builder<Block, BlockState> builder)
    {
        builder.add(Registeel_Core.FACING);
    }

    @Override
    public BlockState rotate(final BlockState state, final Rotation rot)
    {
        return state.setValue(Registeel_Core.FACING, rot.rotate(state.getValue(Registeel_Core.FACING)));
    }

    @Override
    @SuppressWarnings("deprecation")
    public BlockState mirror(final BlockState state, final Mirror mirrorIn)
    {
        return state.rotate(mirrorIn.getRotation(state.getValue(Registeel_Core.FACING)));
    }

    @Override
    public BlockState getStateForPlacement(final BlockItemUseContext context)
    {
        return this.defaultBlockState().setValue(Registeel_Core.FACING, context.getHorizontalDirection().getOpposite());
    }
}
