package pokecube.world.gen.features.trees.foliage;

import java.util.List;
import java.util.Random;
import java.util.function.BiConsumer;

import com.google.common.collect.Lists;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.core.BlockPos;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.world.level.LevelSimulatedReader;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;
import net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacer;
import net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacerType;

public class CustomShapeFoliagePlacer extends FoliagePlacer
{

    public static class LeafPos
    {
        public static final Codec<LeafPos> CODEC = RecordCodecBuilder.create((instance) -> {
            return instance.group(Codec.INT.fieldOf("x").orElse(0).forGetter(s -> s.x),
                    Codec.INT.fieldOf("y").orElse(0).forGetter(s -> s.y),
                    Codec.INT.fieldOf("z").orElse(0).forGetter(s -> s.z)).apply(instance, LeafPos::new);
        });

        final int x;
        final int y;
        final int z;

        public LeafPos(int x, int y, int z)
        {
            this.x = x;
            this.y = y;
            this.z = z;
        }
    }

    public static final Codec<CustomShapeFoliagePlacer> CODEC = RecordCodecBuilder.create((type) -> {
        return foliagePlacerParts(type).and(IntProvider.codec(0, 16).fieldOf("height").forGetter(l -> l.height))
                .and(Codec.list(LeafPos.CODEC).fieldOf("leaves").orElse(Lists.newArrayList()).forGetter(l -> l.leaves))
                .apply(type, CustomShapeFoliagePlacer::new);
    });
    protected final IntProvider height;
    protected final List<LeafPos> leaves;

    public CustomShapeFoliagePlacer(IntProvider radius, IntProvider offset, IntProvider height, List<LeafPos> leaves)
    {
        super(radius, offset);
        this.height = height;
        this.leaves = leaves;
    }

    @Override
    protected FoliagePlacerType<?> type()
    {
        return FoliagePlacerTypes.CUSTOM_SHAPE_FOLIAGE_PLACER.get();
    }

    @Override
    protected void createFoliage(LevelSimulatedReader level, BiConsumer<BlockPos, BlockState> blockSetter,
            Random random, TreeConfiguration treeConfig, int maxFreeTreeHeight,
            FoliagePlacer.FoliageAttachment foliageAttachment, int height, int radius, int offset)
    {
        for (int yOffset = offset; yOffset >= offset - height; --yOffset)
        {
            int range = Math.max(radius + foliageAttachment.radiusOffset() - 1 - yOffset, 0);
            placeLeafSegment(level, blockSetter, random, treeConfig, foliageAttachment.pos(), offset, range, yOffset,
                    foliageAttachment.doubleTrunk());
        }
    }

    protected void placeLeafSegment(LevelSimulatedReader level, BiConsumer<BlockPos, BlockState> blockSetter,
            Random random, TreeConfiguration treeConfig, BlockPos pos, int offset, int range, int yOffset,
            boolean large)
    {
        BlockPos.MutableBlockPos mutablePos = new BlockPos.MutableBlockPos();
        this.leaves.forEach(leaf -> {
            if (!this.shouldSkipLocationSigned(random, leaf.x, leaf.y + yOffset, leaf.z, range, large))
            {
                mutablePos.setWithOffset(pos, leaf.x, leaf.y + yOffset, leaf.z);
                tryPlaceLeaf(level, blockSetter, random, treeConfig, mutablePos);
            }
        });
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
