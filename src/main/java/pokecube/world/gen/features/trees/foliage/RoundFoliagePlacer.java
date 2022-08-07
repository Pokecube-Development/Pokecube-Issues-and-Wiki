package pokecube.world.gen.features.trees.foliage;

import com.mojang.datafixers.Products.P3;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.mojang.serialization.codecs.RecordCodecBuilder.Mu;
import net.minecraft.core.BlockPos;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.world.level.LevelSimulatedReader;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;
import net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacer;
import net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacerType;

import java.util.Random;
import java.util.function.BiConsumer;

public class RoundFoliagePlacer extends FoliagePlacer
{
    protected final IntProvider height;

    public static final Codec<RoundFoliagePlacer> CODEC = RecordCodecBuilder.create((type) -> {
        return palmParts(type).apply(type, RoundFoliagePlacer::new);
    });

    protected static <P extends RoundFoliagePlacer> P3<Mu<P>, IntProvider, IntProvider, IntProvider> palmParts(
            RecordCodecBuilder.Instance<P> instance)
    {
        return foliagePlacerParts(instance).and(IntProvider.codec(0, 16).fieldOf("height").forGetter((get) -> {
            return get.height;
        }));
    }

    public RoundFoliagePlacer(IntProvider radius, IntProvider offset, IntProvider height)
    {
        super(radius, offset);
        this.height = height;
    }

    @Override
    protected FoliagePlacerType<?> type()
    {
        return FoliagePlacerTypes.ROUND_FOLIAGE_PLACER.get();
    }

    @Override
    protected void createFoliage(LevelSimulatedReader level, BiConsumer<BlockPos, BlockState> blockSetter,
                                 Random random, TreeConfiguration treeConfig, int maxFreeTreeHeight,
                                 FoliagePlacer.FoliageAttachment foliageAttachment, int height, int radius, int offset)
    {
        for(int i = offset; i >= offset - height; --i) {
            int j = radius + (i != offset && i != offset - height ? 1 : 0);
            this.placeLeavesRow(level, blockSetter, random, treeConfig, foliageAttachment.pos(), j, i, foliageAttachment.doubleTrunk());
        }
    }

    @Override
    public int foliageHeight(Random random, int height, TreeConfiguration treeConfig)
    {
        return this.height.sample(random);
    }

    @Override
    protected boolean shouldSkipLocation(Random random, int localX, int localY, int localZ, int range, boolean large)
    {
        return localX == range && localZ == range && (random.nextInt(4) == 0 || localY == 0);
    }
}
