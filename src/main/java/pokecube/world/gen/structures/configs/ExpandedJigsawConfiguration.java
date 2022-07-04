package pokecube.world.gen.structures.configs;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.core.Holder;
import net.minecraft.world.level.levelgen.feature.configurations.JigsawConfiguration;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;

public class ExpandedJigsawConfiguration extends JigsawConfiguration
{
    public static final Codec<ExpandedJigsawConfiguration> CODEC = RecordCodecBuilder.create((instance) -> {
        return instance
                .group(StructureTemplatePool.CODEC.fieldOf("start_pool").forGetter(ExpandedJigsawConfiguration::startPool),
                        Codec.intRange(0, 30).fieldOf("size").forGetter(ExpandedJigsawConfiguration::maxDepth))
                .apply(instance, ExpandedJigsawConfiguration::new);
    });

    public ExpandedJigsawConfiguration(Holder<StructureTemplatePool> start_pool, int maxDepth)
    {
        super(start_pool, maxDepth);
    }

}
