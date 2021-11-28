package pokecube.legends.blocks.customblocks;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class HeatranBlock extends Rotates implements SimpleWaterloggedBlock
{
    private static final DirectionProperty          FACING      = HorizontalDirectionalBlock.FACING;
    private static final BooleanProperty            WATERLOGGED = BlockStateProperties.WATERLOGGED;

    // Precise selection box
    private static final VoxelShape HEATRAN = Shapes.or(
			Block.box(3, 6, 3, 13, 10, 13),
			Block.box(2, 10, 2, 14, 12, 14),
			Block.box(2, 4, 2, 14, 6, 14),
			Block.box(1, 12, 1, 15, 14, 15),
			Block.box(1, 2, 1, 15, 4, 15),
			Block.box(0, 14, 0, 16, 16, 16),
			Block.box(0, 0, 0, 16, 2, 16)).optimize();

    @Override
    public VoxelShape getShape(final BlockState state, final BlockGetter worldIn, final BlockPos pos,
            final CollisionContext context)
    {
        return HeatranBlock.HEATRAN;
    }

    public HeatranBlock(final Properties props)
    {
        super(props);
        this.registerDefaultState(this.stateDefinition.any().setValue(HeatranBlock.FACING, Direction.NORTH).setValue(
        		HeatranBlock.WATERLOGGED, false));
    }
}