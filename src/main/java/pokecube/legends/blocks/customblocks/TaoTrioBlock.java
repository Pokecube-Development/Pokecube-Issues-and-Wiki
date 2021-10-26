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

public class TaoTrioBlock extends Rotates implements SimpleWaterloggedBlock
{
    private static final DirectionProperty          FACING      = HorizontalDirectionalBlock.FACING;
    private static final BooleanProperty            WATERLOGGED = BlockStateProperties.WATERLOGGED;

    // Precise selection box
    private static final VoxelShape TAO = Shapes.or(
        Block.box(1, 0, 1, 15, 2, 15),
        Block.box(2, 10, 2, 14, 13, 14),
        Block.box(5, 13, 5, 11, 16, 11),
        Block.box(3, 2, 3, 13, 10, 13)).optimize();

    // Precise selection box
    @Override
    public VoxelShape getShape(final BlockState state, final BlockGetter worldIn, final BlockPos pos,
            final CollisionContext context)
    {
        return TaoTrioBlock.TAO;
    }

    public TaoTrioBlock(final Properties props)
    {
        super(props);
        this.registerDefaultState(this.stateDefinition.any().setValue(TaoTrioBlock.FACING, Direction.NORTH).setValue(
        		TaoTrioBlock.WATERLOGGED, false));
    }
}