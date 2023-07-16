package pokecube.legends.blocks.plants;

import java.util.Random;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.AzaleaBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import pokecube.legends.worldgen.trees.DynaTreeGrower;

public class DynaShrubBlock extends AzaleaBlock implements BonemealableBlock
{
    private static final DynaTreeGrower TREE_GROWER = new DynaTreeGrower();
    private static final VoxelShape SHAPE = Shapes.or(Block.box(1.0D, 5.0D, 1.0D, 15.0D, 16.0D, 15.0D), Block.box(6.0D, 0.0D, 6.0D, 10.0D, 8.0D, 10.0D));

    public DynaShrubBlock(final BlockBehaviour.Properties properties)
    {
       super(properties);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter block, BlockPos pos, CollisionContext context)
    {
       return SHAPE;
    }

    @Override
    public void performBonemeal(ServerLevel world, Random random, BlockPos pos, BlockState state)
    {
       TREE_GROWER.growTree(world, world.getChunkSource().getGenerator(), pos, state, random);
    }
}
