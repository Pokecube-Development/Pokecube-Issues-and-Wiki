package thut.api.block;

import java.util.Random;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class DustBlock extends Block
{
    public static final IntegerProperty LEVEL = BlockStateProperties.LEVEL;

    public static final VoxelShape[] SHAPES = new VoxelShape[16];
    static
    {
        for (int i = 0; i < 16; i++)
        {
            SHAPES[i] = Block.box(0.0D, 0.0D, 0.0D, 16.0D, (i + 1) * 16.0D, 16.0D);
        }
    }

    public DustBlock(Properties properties)
    {
        super(properties);
    }

    public boolean isRandomlyTicking(BlockState p_54732_)
    {
        return true;
    }

    @Override
    public void randomTick(BlockState p_60551_, ServerLevel p_60552_, BlockPos p_60553_, Random p_60554_)
    {
        
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter p_60556_, BlockPos p_60557_, CollisionContext p_60558_)
    {
        int level = state.getValue(LEVEL);
        return SHAPES[level];
    }
}
