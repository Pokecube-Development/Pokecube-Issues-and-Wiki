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
import pokecube.core.PokecubeCore;

import java.util.Random;
import java.util.function.BiConsumer;

public class RoundFoliagePlacer extends FoliagePlacer
{
    protected final IntProvider height;

    public static final Codec<RoundFoliagePlacer> CODEC = RecordCodecBuilder.create((type) -> {
        return roundParts(type).apply(type, RoundFoliagePlacer::new);
    });

    protected static <P extends RoundFoliagePlacer> P3<Mu<P>, IntProvider, IntProvider, IntProvider> roundParts(
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
        double ch = height / 2.0;
        double scale = Math.sqrt(2);
        for(int i = 0; i >= -height; --i)
        {
            int range = (int) ((Math.sqrt(ch - Math.abs(-i - ch)) + 1) * scale + 1);
            this.placeLeavesRow(level, blockSetter, random, treeConfig, foliageAttachment.pos(), range, offset + i, foliageAttachment.doubleTrunk());
            PokecubeCore.LOGGER.info("Range = {}, i = {}, ch = {}", range, i, ch);
            PokecubeCore.LOGGER.info("Formula = {}", (Math.sqrt(ch - Math.abs(-i - ch)) + 1));
        }
    }

    protected void placeLeavesRow(LevelSimulatedReader level, BiConsumer<BlockPos, BlockState> blockSetter,
                                  Random random, TreeConfiguration treeConfig, BlockPos pos, int range, int yOffset, boolean large)
    {
        int i = large ? 1 : 0;
        BlockPos.MutableBlockPos mutablePos = new BlockPos.MutableBlockPos();

        for(int j = -range; j <= range + i; ++j)
        {
            for(int k = -range; k <= range + i; ++k)
            {
                if(j * j + k * k <= range * range)
                {
                    if (!this.shouldSkipLocationSigned(random, j, yOffset, k, range, large))
                    {
                        mutablePos.setWithOffset(pos, j, yOffset, k);
                        tryPlaceLeaf(level, blockSetter, random, treeConfig, mutablePos);
                    }
                }
            }
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
        return localX == range && localZ == range && (random.nextInt(8) == 0 || localY == 0);
    }
}
