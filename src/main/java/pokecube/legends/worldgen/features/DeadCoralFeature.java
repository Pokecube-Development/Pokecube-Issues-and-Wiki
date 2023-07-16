package pokecube.legends.worldgen.features;

import java.util.List;
import java.util.Random;

import com.mojang.serialization.Codec;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.BaseCoralWallFanBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraftforge.registries.ForgeRegistries;

public abstract class DeadCoralFeature extends Feature<NoneFeatureConfiguration>
{
    // Tags
    public final static TagKey<Block> DEAD_CORAL_BLOCKS = TagKey.create(Registry.BLOCK_REGISTRY,
            new ResourceLocation("forge", "dead_coral_blocks"));
    public final static TagKey<Block> DEAD_CORALS = TagKey.create(Registry.BLOCK_REGISTRY,
            new ResourceLocation("forge", "dead_corals"));
    public final static TagKey<Block> DEAD_WALL_CORALS = TagKey.create(Registry.BLOCK_REGISTRY,
            new ResourceLocation("forge", "dead_wall_corals"));

    private Block getRandom(TagKey<Block> key, Random rand)
    {
        List<Block> list = ForgeRegistries.BLOCKS.tags().getTag(key).stream().toList();
        return list.get(rand.nextInt(list.size()));
    }

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
        final BlockState state = getRandom(DeadCoralFeature.DEAD_CORAL_BLOCKS, random).defaultBlockState();
        return this.placeFeature(world, random, pos, state);
    }

    public abstract boolean placeFeature(LevelAccessor world, Random random, BlockPos pos, BlockState state);

    public boolean placeDeadCoralBlock(final LevelAccessor world, final Random random, final BlockPos pos,
            final BlockState state)
    {
        final BlockPos posAbove = pos.above();
        final BlockState statePos = world.getBlockState(pos);
        if ((statePos.is(Blocks.WATER) || statePos.is(DeadCoralFeature.DEAD_CORALS))
                && world.getBlockState(posAbove).is(Blocks.WATER))
        {
            world.setBlock(pos, state, 3);
            if (random.nextFloat() < 0.25F)
                world.setBlock(posAbove, getRandom(DeadCoralFeature.DEAD_CORALS, random).defaultBlockState(), 2);

            for (final Direction direction : Direction.Plane.HORIZONTAL) if (random.nextFloat() < 0.2F)
            {
                final BlockPos posRelative = pos.relative(direction);
                if (world.getBlockState(posRelative).is(Blocks.WATER))
                {
                    BlockState stateRandom = getRandom(DeadCoralFeature.DEAD_WALL_CORALS, random).defaultBlockState();
                    if (stateRandom.hasProperty(BaseCoralWallFanBlock.FACING))
                        stateRandom = stateRandom.setValue(BaseCoralWallFanBlock.FACING, direction);
                    world.setBlock(posRelative, stateRandom, 2);
                }
            }
            return true;
        }
        else return false;
    }
}