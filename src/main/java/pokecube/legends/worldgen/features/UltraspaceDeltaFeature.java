package pokecube.legends.worldgen.features;

import java.util.Random;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Codec;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.DeltaFeatureConfiguration;
import pokecube.legends.init.BlockInit;

public class UltraspaceDeltaFeature extends Feature<DeltaFeatureConfiguration>
{
    public static ImmutableList<Block> CANNOT_REPLACE   = ImmutableList.of(BlockInit.AGED_LEAVES.get(), BlockInit.ASH.get(),
            BlockInit.CORRUPTED_LEAVES.get(), BlockInit.DISTORTIC_LEAVES.get(), BlockInit.DYNA_LEAVES_PASTEL_PINK.get(),
            BlockInit.DYNA_LEAVES_PINK.get(), BlockInit.DYNA_LEAVES_RED.get(), BlockInit.INVERTED_LEAVES.get(),
            BlockInit.MIRAGE_LEAVES.get(), BlockInit.TEMPORAL_LEAVES.get(), Blocks.BEDROCK, Blocks.CHEST, Blocks.LAVA,
            Blocks.NETHER_BRICKS, Blocks.NETHER_BRICK_FENCE, Blocks.NETHER_BRICK_STAIRS, Blocks.NETHER_WART,
            Blocks.SPAWNER, Blocks.WATER);
    private static final Direction[]   DIRECTIONS       = Direction.values();
    private static final double        RIM_SPAWN_CHANCE = 0.9D;

    public UltraspaceDeltaFeature(final Codec<DeltaFeatureConfiguration> config)
    {
        super(config);
    }

    @Override
    public boolean place(final FeaturePlaceContext<DeltaFeatureConfiguration> context)
    {
        boolean flag = false;
        final Random random = context.random();
        final WorldGenLevel world = context.level();
        final DeltaFeatureConfiguration deltaConfig = context.config();
        final BlockPos pos = context.origin();
        final boolean flag1 = random.nextDouble() < UltraspaceDeltaFeature.RIM_SPAWN_CHANCE;
        final int i = flag1 ? deltaConfig.rimSize().sample(random) : 0;
        final int j = flag1 ? deltaConfig.rimSize().sample(random) : 0;
        final boolean flag2 = flag1 && i != 0 && j != 0;
        final int k = deltaConfig.size().sample(random);
        final int l = deltaConfig.size().sample(random);
        final int i1 = Math.max(k, l);

        for (final BlockPos pos1 : BlockPos.withinManhattan(pos, k, 0, l))
        {
            if (pos1.distManhattan(pos) > i1) break;

            if (UltraspaceDeltaFeature.isClear(world, pos1, deltaConfig))
            {
                if (flag2)
                {
                    flag = true;
                    this.setBlock(world, pos1, deltaConfig.rim());
                }

                final BlockPos pos2 = pos1.offset(i, 0, j);
                if (UltraspaceDeltaFeature.isClear(world, pos2, deltaConfig))
                {
                    flag = true;
                    this.setBlock(world, pos2, deltaConfig.contents());
                }
            }
        }
        return flag;
    }

    public static boolean isClear(final LevelAccessor world, final BlockPos pos, final DeltaFeatureConfiguration config)
    {
        final BlockState state = world.getBlockState(pos);
        if (state.is(config.contents().getBlock())) return false;
        else if (UltraspaceDeltaFeature.CANNOT_REPLACE.contains(state.getBlock())) return false;
        else
        {
            for (final Direction direction : UltraspaceDeltaFeature.DIRECTIONS)
            {
                final boolean flag = world.getBlockState(pos.relative(direction)).isAir();
                if (flag && direction != Direction.UP || !flag && direction == Direction.UP) return false;
            }
            return true;
        }
    }
}