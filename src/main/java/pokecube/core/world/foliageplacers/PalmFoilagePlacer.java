package pokecube.core.world.foliageplacers;

import com.mojang.datafixers.Products.P3;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.mojang.serialization.codecs.RecordCodecBuilder.Mu;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.world.level.LevelSimulatedReader;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;
import net.minecraft.world.level.levelgen.feature.foliageplacers.BlobFoliagePlacer;
import net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacer;
import net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacerType;
import pokecube.core.handlers.FoliagePlacerTypes;

import java.util.Random;
import java.util.function.BiConsumer;

public class PalmFoilagePlacer extends FoliagePlacer {

    protected final int height;

    public static final Codec<PalmFoilagePlacer> CODEC = RecordCodecBuilder.create((type) ->
    {
        return palmParts(type).apply(type, PalmFoilagePlacer::new);
    });

    protected static <P extends PalmFoilagePlacer> P3<Mu<P>, IntProvider, IntProvider, Integer> palmParts(RecordCodecBuilder.Instance<P> instance)
    {
        return foliagePlacerParts(instance).and(Codec.intRange(0, 16).fieldOf("height").forGetter((p_68412_) ->
        {
            return p_68412_.height;
        }));
    }

    public PalmFoilagePlacer(IntProvider radius, IntProvider offset, int height) {
        super(radius, offset);
        this.height = height;
    }

    @Override
    protected FoliagePlacerType<?> type() {
        return FoliagePlacerTypes.PALM_FOLIAGE_PLACER.get();
    }

    @Override
    protected void createFoliage(LevelSimulatedReader level, BiConsumer<BlockPos, BlockState> blockSetter, Random random,
                                 TreeConfiguration treeConfig, int treeHeight, FoliagePlacer.FoliageAttachment foliageAttachment, int radius, int offset, int height)
    {
        for(int i = height; i >= height - radius; --i)
        {
            int j = Math.max(offset + foliageAttachment.radiusOffset() - 1 - i / 2, 0);
            placeLeavesRow(level, blockSetter, random, treeConfig, foliageAttachment.pos(), j, i, foliageAttachment.doubleTrunk());
        }
    }

    protected void placeLeavesRow(LevelSimulatedReader level, BiConsumer<BlockPos, BlockState> blockSetter, Random random,
                                  TreeConfiguration treeConfig, BlockPos pos, int range, int yOffset, boolean large)
    {
        int i = large ? 1 : 0;
        BlockPos.MutableBlockPos mutablePos = new BlockPos.MutableBlockPos();

        for(int j = -range; j <= range + i; ++j)
        {
            for(int k = -range; k <= range + i; ++k)
            {
                if (!this.shouldSkipLocationSigned(random, j, yOffset, k, range, large))
                {
                    mutablePos.setWithOffset(pos, j, yOffset, k);
                    tryPlaceLeaf(level, blockSetter, random, treeConfig, mutablePos);
                }
            }
        }
    }

    @Override
    public int foliageHeight(Random random, int i, TreeConfiguration treeConfig)
    {
        return this.height;
    }

    @Override
    protected boolean shouldSkipLocation(Random random, int p_68417_, int p_68418_, int p_68419_, int p_68420_, boolean p_68421_)
    {
        return p_68417_ == p_68420_ && p_68419_ == p_68420_ && (random.nextInt(4) == 0 || p_68418_ == 0);
    }
}
