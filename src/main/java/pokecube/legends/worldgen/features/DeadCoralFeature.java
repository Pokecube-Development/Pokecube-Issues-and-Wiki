package pokecube.legends.worldgen.features;

import java.util.Random;

import com.mojang.serialization.Codec;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.Tag;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.BaseCoralWallFanBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

public abstract class DeadCoralFeature extends Feature<NoneFeatureConfiguration>
{
    // Tags
    public final static Tag.Named<Block> DEAD_CORAL_BLOCKS = BlockTags.createOptional(new ResourceLocation("forge",
            "dead_coral_blocks"));
    public final static Tag.Named<Block> DEAD_CORALS       = BlockTags.createOptional(new ResourceLocation("forge",
            "dead_corals"));
    public final static Tag.Named<Block> DEAD_WALL_CORALS  = BlockTags.createOptional(new ResourceLocation("forge",
            "dead_wall_corals"));

    public DeadCoralFeature(final Codec<NoneFeatureConfiguration> config)
    {
        super(config);
    }

    @Override
    public boolean place(final FeaturePlaceContext<NoneFeatureConfiguration> context)
    {
        final Random random = context.random();
        final WorldGenLevel world = context.level();
        final BlockPos pos = context.origin();
        final BlockState state = DeadCoralFeature.DEAD_CORAL_BLOCKS.getRandomElement(random).defaultBlockState();
        return this.placeFeature(world, random, pos, state);
    }

    public abstract boolean placeFeature(LevelAccessor world, Random random, BlockPos pos, BlockState state);

    public boolean placeDeadCoralBlock(final LevelAccessor world, final Random random, final BlockPos pos,
            final BlockState state)
    {
        final BlockPos posAbove = pos.above();
        final BlockState statePos = world.getBlockState(pos);
        if ((statePos.is(Blocks.WATER) || statePos.is(DeadCoralFeature.DEAD_CORALS)) && world.getBlockState(posAbove).is(
                Blocks.WATER))
        {
            world.setBlock(pos, state, 3);
            if (random.nextFloat() < 0.25F) world.setBlock(posAbove, DeadCoralFeature.DEAD_CORALS.getRandomElement(random)
                    .defaultBlockState(), 2);

            for (final Direction direction : Direction.Plane.HORIZONTAL)
                if (random.nextFloat() < 0.2F)
                {
                    final BlockPos posRelative = pos.relative(direction);
                    if (world.getBlockState(posRelative).is(Blocks.WATER))
                    {
                        BlockState stateRandom = DeadCoralFeature.DEAD_WALL_CORALS.getRandomElement(random)
                                .defaultBlockState();
                        if (stateRandom.hasProperty(BaseCoralWallFanBlock.FACING)) stateRandom = stateRandom.setValue(
                                BaseCoralWallFanBlock.FACING, direction);
                        world.setBlock(posRelative, stateRandom, 2);
                    }
                }
            return true;
        }
        else return false;
    }
}