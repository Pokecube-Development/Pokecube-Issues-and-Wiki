package pokecube.world.gen.structures.configs;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

import com.google.common.collect.Lists;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.configurations.JigsawConfiguration;
import net.minecraft.world.level.levelgen.structure.StructureSet;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;
import pokecube.api.data.spawns.SpawnBiomeMatcher;

public class ExpandedJigsawConfiguration extends JigsawConfiguration
{
    private static final Supplier<Heightmap.Types[]> HM = Heightmap.Types::values;
    private static final Function<String, Heightmap.Types> NM = Heightmap.Types::getFromKey;

    public static final Codec<ExpandedJigsawConfiguration> CODEC = RecordCodecBuilder.create((instance) -> {
        return instance
                .group(StructureTemplatePool.CODEC.fieldOf("start_pool")
                        .forGetter(ExpandedJigsawConfiguration::startPool),
                        Codec.intRange(0, 30).fieldOf("size").forGetter(ExpandedJigsawConfiguration::maxDepth),
                        YSettings.CODEC.fieldOf("y_settings").orElse(YSettings.DEFAULT).forGetter(s -> s.y_settings),
                        ClearanceSettings.CODEC.fieldOf("clearances").orElse(ClearanceSettings.DEFAULT)
                                .forGetter(s -> s.clearances),
                        Codec.STRING.listOf().fieldOf("required_parts").orElse(new ArrayList<>())
                                .forGetter(s -> s.required_parts),
                        Codec.STRING.fieldOf("spawn_preset").orElse("").forGetter(s -> s._spawn_preset),
                        Codec.STRING.fieldOf("spawn_blacklist").orElse("").forGetter(s -> s._spawn_blacklist),
                        Codec.STRING.fieldOf("biome_type").orElse("none").forGetter(s -> s.biome_type),
                        StringRepresentable.fromEnum(HM, NM).fieldOf("height_type")
                                .orElse(Heightmap.Types.WORLD_SURFACE_WG).forGetter(structure -> structure.height_type),
                        ResourceKey.codec(Registry.STRUCTURE_SET_REGISTRY).listOf().fieldOf("structures_to_avoid")
                                .orElse(new ArrayList<>()).forGetter(config -> config.structures_to_avoid),
                        Codec.INT.fieldOf("avoid_range").orElse(4).forGetter(s -> s.avoid_range),
                        Codec.INT.fieldOf("biome_room").orElse(2).forGetter(s -> s.biome_room), AvoidanceSettings.CODEC
                                .fieldOf("avoidances").orElse(AvoidanceSettings.DEFAULT).forGetter(s -> s.avoidances))
                .apply(instance, ExpandedJigsawConfiguration::new);
    });

    public static class YSettings
    {
        public static final YSettings DEFAULT = new YSettings(0, 0, Integer.MIN_VALUE, Integer.MAX_VALUE,
                Integer.MAX_VALUE, 0, "surface");

        public static final Codec<YSettings> CODEC = RecordCodecBuilder.create((instance) -> {
            return instance
                    .group(Codec.INT.fieldOf("vertical_offset").orElse(0).forGetter(s -> s.vertical_offset),
                            Codec.INT.fieldOf("y_check_radius").orElse(1).forGetter(s -> s.y_check_radius),
                            Codec.INT.fieldOf("min_y").orElse(Integer.MIN_VALUE).forGetter(s -> s.min_y),
                            Codec.INT.fieldOf("max_y").orElse(Integer.MAX_VALUE).forGetter(s -> s.max_y),
                            Codec.INT.fieldOf("max_dy").orElse(Integer.MAX_VALUE).forGetter(s -> s.max_dy),
                            Codec.INT.fieldOf("dy_offset").orElse(Integer.MAX_VALUE).forGetter(s -> s.dy_offset),
                            Codec.STRING.fieldOf("surface_type").orElse("surface").forGetter(s -> s.surface_type))
                    .apply(instance, YSettings::new);
        });

        public final int vertical_offset;
        public final int y_check_radius;
        public final int min_y;
        public final int max_y;
        public final int max_dy;
        public final int dy_offset;
        public final String surface_type;

        public YSettings(int vertical_offset, int y_check_radius, int min_y, int max_y, int max_dy, int dy_offset,
                String surface_type)
        {
            this.vertical_offset = vertical_offset;
            this.y_check_radius = y_check_radius;
            this.min_y = min_y;
            this.max_y = max_y;
            this.max_dy = max_dy;
            this.surface_type = surface_type;
            this.dy_offset = dy_offset;
        }
    }

    public static class ClearanceSettings
    {
        public static final ClearanceSettings DEFAULT = new ClearanceSettings(0, 0);

        public static final Codec<ClearanceSettings> CODEC = RecordCodecBuilder.create((instance) -> {
            return instance
                    .group(Codec.INT.fieldOf("h_clearance").orElse(0).forGetter(s -> s.h_clearance),
                            Codec.INT.fieldOf("v_clearance").orElse(0).forGetter(s -> s.v_clearance))
                    .apply(instance, ClearanceSettings::new);
        });

        public int h_clearance;
        public int v_clearance;

        public ClearanceSettings(int h_clearance, int v_clearance)
        {
            this.h_clearance = h_clearance;
            this.v_clearance = v_clearance;
        }
    }

    public static class AvoidanceSettings
    {
        public static class AvoidanceEntry
        {
            public static final Codec<AvoidanceEntry> CODEC = RecordCodecBuilder.create((instance) -> {
                return instance
                        .group(Codec.INT.fieldOf("distance").orElse(0).forGetter(s -> s.distance),
                                Codec.STRING.fieldOf("name").orElse("").forGetter(s -> s.name))
                        .apply(instance, AvoidanceEntry::new);
            });

            public int distance;
            public String name;

            public AvoidanceEntry(int distance, String name)
            {
                this.distance = distance;
                this.name = name;
            }
        }

        public static final AvoidanceSettings DEFAULT = new AvoidanceSettings(Lists.newArrayList(),
                Lists.newArrayList());

        public static final Codec<AvoidanceSettings> CODEC = RecordCodecBuilder.create((instance) -> {
            return instance.group(
                    Codec.list(AvoidanceEntry.CODEC).fieldOf("avoidances").orElse(Lists.newArrayList())
                            .forGetter(s -> s.avoidances),
                    Codec.list(Codec.STRING).fieldOf("flags").orElse(Lists.newArrayList()).forGetter(s -> s.flags))
                    .apply(instance, AvoidanceSettings::new);
        });

        public List<AvoidanceEntry> avoidances;
        public List<String> flags;

        public AvoidanceSettings(List<AvoidanceEntry> avoidances, List<String> flags)
        {
            this.avoidances = avoidances;
            this.flags = flags;
        }
    }

    public final List<String> required_parts;
    public final Heightmap.Types height_type;

    public final String biome_type;

    private final String _spawn_preset;
    private final String _spawn_blacklist;

    public final List<ResourceKey<StructureSet>> structures_to_avoid;
    public final int avoid_range;

    public final int biome_room;

    public final YSettings y_settings;
    public final ClearanceSettings clearances;

    public final boolean underground;
    public final boolean air;

    public final String name;

    public final AvoidanceSettings avoidances;

    public List<SpawnBiomeMatcher> _needed = Lists.newArrayList();
    public List<SpawnBiomeMatcher> _banned = Lists.newArrayList();

    public ExpandedJigsawConfiguration(Holder<StructureTemplatePool> start_pool, int maxDepth, YSettings y_settings,
            ClearanceSettings clearances, List<String> required_parts, String _spawn_preset, String _spawn_blacklist,
            final String biome_type, Heightmap.Types height_type,
            final List<ResourceKey<StructureSet>> structures_to_avoid, final int avoid_range, final int biome_room,
            AvoidanceSettings avoidances)
    {
        super(start_pool, maxDepth);
        this.y_settings = y_settings;
        this.clearances = clearances;
        this.required_parts = required_parts;
        this.height_type = height_type;
        this.structures_to_avoid = structures_to_avoid;
        this.avoid_range = avoid_range;
        this.biome_room = biome_room;
        this._spawn_preset = _spawn_preset;
        this._spawn_blacklist = _spawn_blacklist;
        this.biome_type = biome_type;
        this.name = start_pool.value().getName().toString();
        this.avoidances = avoidances;

        if (!_spawn_blacklist.isBlank())
        {
            String[] opts = _spawn_blacklist.split(",");
            for (String s : opts) _banned.add(SpawnBiomeMatcher.get(s));
        }
        if (!_spawn_preset.isBlank())
        {
            String[] opts = _spawn_preset.split(",");
            for (String s : opts) _needed.add(SpawnBiomeMatcher.get(s));
        }

        this.underground = "underground".equals(y_settings.surface_type);
        this.air = "air".equals(y_settings.surface_type);
    }

    public boolean hasValidator()
    {
        return !_needed.isEmpty() || !_banned.isEmpty();
    }

    public boolean isValid(Holder<Biome> holder)
    {
        for (SpawnBiomeMatcher m : _needed) if (!m.checkBiome(holder)) return false;
        for (SpawnBiomeMatcher m : _banned) if (m.checkBiome(holder)) return false;
        return true;
    }
}
