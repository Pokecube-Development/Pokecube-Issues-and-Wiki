package pokecube.legends.blocks;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.HorizontalBlock;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.state.StateContainer;
import net.minecraft.util.Direction;
import net.minecraftforge.common.ToolType;

public class Rotates extends BlockBase
{

    public Rotates(final String name, final Material material, final float hardnessresistance,
            final SoundType sound)
    {
        super(name, material, hardnessresistance, sound);
        this.setDefaultState(this.stateContainer.getBaseState().with(HorizontalBlock.HORIZONTAL_FACING,
                Direction.NORTH));
    }

    public Rotates(final String name, final Material material, final float hardness, final float resistance,
            final SoundType sound)
    {
        super(name, material, hardness, resistance, sound);
        this.setDefaultState(this.stateContainer.getBaseState().with(HorizontalBlock.HORIZONTAL_FACING,
                Direction.NORTH));
    }

    public Rotates(final String name, final Material material, final ToolType tool, final int level)
    {
        super(name, material, tool, level);
        this.setDefaultState(this.stateContainer.getBaseState().with(HorizontalBlock.HORIZONTAL_FACING,
                Direction.NORTH));
    }

    public Rotates(final String name, final Properties props)
    {
        super(name, props);
        this.setDefaultState(this.stateContainer.getBaseState().with(HorizontalBlock.HORIZONTAL_FACING,
                Direction.NORTH));
    }

    @Override
    protected void fillStateContainer(final StateContainer.Builder<Block, BlockState> builder)
    {
        builder.add(HorizontalBlock.HORIZONTAL_FACING);
    }

    @Override
    public BlockState getStateForPlacement(final BlockItemUseContext context)
    {
        return this.getDefaultState().with(HorizontalBlock.HORIZONTAL_FACING, context.getPlacementHorizontalFacing()
                .getOpposite());
    }

}
