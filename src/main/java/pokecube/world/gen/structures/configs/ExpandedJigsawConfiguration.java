package pokecube.world.gen.structures.configs;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.configurations.JigsawConfiguration;
import net.minecraft.world.level.levelgen.structure.StructureSet;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;

public class ExpandedJigsawConfiguration extends JigsawConfiguration
{
    private static final Supplier<Heightmap.Types[]> HM = Heightmap.Types::values;
    private static final Function<String, Heightmap.Types> NM = Heightmap.Types::getFromKey;

    public static final Codec<ExpandedJigsawConfiguration> CODEC = RecordCodecBuilder.create((instance) -> {
        return instance
                .group(StructureTemplatePool.CODEC.fieldOf("start_pool")
                        .forGetter(ExpandedJigsawConfiguration::startPool),
                        Codec.intRange(0, 30).fieldOf("size").forGetter(ExpandedJigsawConfiguration::maxDepth),
                        Codec.INT.fieldOf("vertical_offset").orElse(0).forGetter(s -> s.vertical_offset),
                        Codec.STRING.listOf().fieldOf("required_parts").orElse(new ArrayList<>())
                                .forGetter(s -> s.required_parts),
                        StringRepresentable.fromEnum(HM, NM).fieldOf("height_type")
                                .orElse(Heightmap.Types.WORLD_SURFACE_WG).forGetter(structure -> structure.height_type),
                        ResourceKey.codec(Registry.STRUCTURE_SET_REGISTRY).listOf().fieldOf("structures_to_avoid")
                                .orElse(new ArrayList<>()).forGetter(config -> config.structures_to_avoid),
                        Codec.INT.fieldOf("avoid_range").orElse(0).forGetter(s -> s.avoid_range),
                        Codec.INT.fieldOf("biome_room").orElse(0).forGetter(s -> s.biome_room))
                .apply(instance, ExpandedJigsawConfiguration::new);
    });

    public final List<String> required_parts;
    public final int vertical_offset;
    public final Heightmap.Types height_type;

    public final List<ResourceKey<StructureSet>> structures_to_avoid;
    public final int avoid_range;

    public final int biome_room;

    public ExpandedJigsawConfiguration(Holder<StructureTemplatePool> start_pool, int maxDepth, int vertical_offset,
            List<String> required_parts, Heightmap.Types height_type,
            final List<ResourceKey<StructureSet>> structures_to_avoid, final int avoid_range, final int biome_room)
    {
        super(start_pool, maxDepth);
        this.vertical_offset = vertical_offset;
        this.required_parts = required_parts;
        this.height_type = height_type;
        this.structures_to_avoid = structures_to_avoid;
        this.avoid_range = avoid_range;
        this.biome_room = biome_room;
    }
}
