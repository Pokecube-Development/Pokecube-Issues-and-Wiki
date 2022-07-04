package pokecube.legends.blocks.plants;

import java.util.Random;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.DoublePlantBlock;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.SmallDripleafBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import pokecube.legends.Reference;

public class SmallContaminatedDripleafBlock extends SmallDripleafBlock
        implements BonemealableBlock, SimpleWaterloggedBlock
{
    // Tag
    public static final TagKey<Block> SMALL_CONTAMINATED_DRIPLEAF_PLACEABLE = TagKey.create(Registry.BLOCK_REGISTRY,
            new ResourceLocation(Reference.ID, "small_contaminated_dripleaf_placeable"));
    protected static final VoxelShape SHAPE_UPPER = Block.box(2.0D, 0.0D, 2.0D, 14.0D, 13.0D, 14.0D);
    protected static final VoxelShape SHAPE_LOWER = Block.box(4.0D, 0.0D, 4.0D, 12.0D, 16.0D, 12.0D);

    public SmallContaminatedDripleafBlock(BlockBehaviour.Properties properties)
    {
        super(properties);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter block, BlockPos pos, CollisionContext context)
    {
        Vec3 vec3 = state.getOffset(block, pos);
        final DoubleBlockHalf half = state.getValue(HALF);

        if (half == DoubleBlockHalf.LOWER)
        {
            return SHAPE_LOWER.move(vec3.x, vec3.y, vec3.z);
        }
        else return SHAPE_UPPER.move(vec3.x, vec3.y, vec3.z);
    }

    @Override
    protected boolean mayPlaceOn(BlockState state, BlockGetter block, BlockPos pos)
    {
        return state.is(SMALL_CONTAMINATED_DRIPLEAF_PLACEABLE)
                || block.getFluidState(pos.above()).isSourceOfType(Fluids.WATER) && super.mayPlaceOn(state, block, pos);
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader world, BlockPos pos)
    {
        if (state.getValue(HALF) == DoubleBlockHalf.UPPER)
        {
            return super.canSurvive(state, world, pos);
        }
        else
        {
            BlockPos pos1 = pos.below();
            BlockState state1 = world.getBlockState(pos1);
            return this.mayPlaceOn(state1, world, pos1);
        }
    }

    @Override
    public void performBonemeal(ServerLevel world, Random random, BlockPos pos, BlockState state)
    {
        if (state.getValue(DoublePlantBlock.HALF) == DoubleBlockHalf.LOWER)
        {
            BlockPos posAbove = pos.above();
            world.setBlock(posAbove, world.getFluidState(posAbove).createLegacyBlock(), 18);
            BigContaminatedDripleafBlock.placeWithRandomHeight(world, random, pos, state.getValue(FACING));
        }
        else
        {
            BlockPos posBelow = pos.below();
            this.performBonemeal(world, random, posBelow, world.getBlockState(posBelow));
        }
    }
}