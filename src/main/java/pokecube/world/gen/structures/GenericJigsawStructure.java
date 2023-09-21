package pokecube.world.gen.structures;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import com.google.common.collect.Lists;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureSet;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;
import pokecube.api.PokecubeAPI;
import pokecube.api.data.spawns.SpawnBiomeMatcher;
import pokecube.core.PokecubeCore;
import pokecube.core.utils.PokecubeSerializer;
import pokecube.world.gen.structures.GenericJigsawStructure.AvoidanceSettings.AvoidanceEntry;
import pokecube.world.gen.structures.utils.ExpandedJigsawPacement;
import thut.lib.RegHelper;

public class GenericJigsawStructure extends Structure
{
    public static final Codec<GenericJigsawStructure> CODEC = RecordCodecBuilder.create((instance) -> {
        return instance
                .group(//@formatter:off
                Structure.settingsCodec(instance),
                Codec.STRING.fieldOf("start_pool").orElse("").forGetter(s -> s.name),
                StructureTemplatePool.CODEC.fieldOf("start_pool").forGetter(s -> s.startPool),
                Codec.intRange(0, 30).fieldOf("size").forGetter(s -> s.max_depth),
                YSettings.CODEC.fieldOf("y_settings").orElse(YSettings.DEFAULT).forGetter(s -> s.y_settings),
                ClearanceSettings.CODEC.fieldOf("clearances").orElse(ClearanceSettings.DEFAULT).forGetter(s -> s.clearances),
                Codec.STRING.listOf().fieldOf("required_parts").orElse(new ArrayList<>()).forGetter(s -> s.required_parts),
                Codec.STRING.fieldOf("spawn_preset").orElse("").forGetter(s -> s._spawn_preset),
                Codec.STRING.fieldOf("spawn_blacklist").orElse("").forGetter(s -> s._spawn_blacklist),
                Codec.STRING.fieldOf("biome_type").orElse("none").forGetter(s -> s.biome_type),
                Heightmap.Types.CODEC.fieldOf("height_type").orElse(Heightmap.Types.WORLD_SURFACE_WG).forGetter(structure -> structure.height_type),
                ResourceKey.codec(RegHelper.STRUCTURE_SET_REGISTRY).listOf().fieldOf("structures_to_avoid").orElse(new ArrayList<>()).forGetter(config -> config.structures_to_avoid),
                Codec.INT.fieldOf("avoid_range").orElse(4).forGetter(s -> s.avoid_range),
                Codec.INT.fieldOf("biome_room").orElse(2).forGetter(s -> s.biome_room), 
                AvoidanceSettings.CODEC.fieldOf("avoidances").orElse(AvoidanceSettings.DEFAULT).forGetter(s -> s.avoidances)
                      //@formatter:on
        ).apply(instance, GenericJigsawStructure::new);
    });

    public static class YSettings
    {
        public static final YSettings DEFAULT = new YSettings(0, 0, Integer.MIN_VALUE, Integer.MAX_VALUE,
                Integer.MAX_VALUE, Integer.MIN_VALUE, 0, "surface");

        public static final Codec<YSettings> CODEC = RecordCodecBuilder.create((instance) -> {
            return instance
                    .group(Codec.INT.fieldOf("vertical_offset").orElse(0).forGetter(s -> s.vertical_offset),
                            Codec.INT.fieldOf("y_check_radius").orElse(0).forGetter(s -> s.y_check_radius),
                            Codec.INT.fieldOf("min_y").orElse(Integer.MIN_VALUE).forGetter(s -> s.min_y),
                            Codec.INT.fieldOf("max_y").orElse(Integer.MAX_VALUE).forGetter(s -> s.max_y),
                            Codec.INT.fieldOf("max_dy").orElse(Integer.MAX_VALUE).forGetter(s -> s.max_dy),
                            Codec.INT.fieldOf("fixed_y").orElse(Integer.MIN_VALUE).forGetter(s -> s.fixed_y),
                            Codec.INT.fieldOf("dy_offset").orElse(Integer.MAX_VALUE).forGetter(s -> s.dy_offset),
                            Codec.STRING.fieldOf("surface_type").orElse("surface").forGetter(s -> s.surface_type))
                    .apply(instance, YSettings::new);
        });

        public final int vertical_offset;
        public final int y_check_radius;
        public final int min_y;
        public final int max_y;
        public final int max_dy;
        public final int fixed_y;
        public final int dy_offset;
        public final String surface_type;

        public YSettings(int vertical_offset, int y_check_radius, int min_y, int max_y, int max_dy, int fixed_y,
                int dy_offset, String surface_type)
        {
            this.vertical_offset = vertical_offset;
            this.y_check_radius = y_check_radius;
            this.min_y = min_y;
            this.max_y = max_y;
            this.max_dy = max_dy;
            this.fixed_y = fixed_y;
            this.surface_type = surface_type;
            this.dy_offset = dy_offset;
        }
    }

    public static class ClearanceSettings
    {
        public static final ClearanceSettings DEFAULT = new ClearanceSettings(0, 0, 100);

        public static final Codec<ClearanceSettings> CODEC = RecordCodecBuilder.create((instance) -> {
            return instance
                    .group(Codec.INT.fieldOf("h_clearance").orElse(0).forGetter(s -> s.h_clearance),
                            Codec.INT.fieldOf("v_clearance").orElse(0).forGetter(s -> s.v_clearance),
                            Codec.INT.fieldOf("max_distance_from_center").orElse(100)
                                    .forGetter(s -> s.max_distance_from_center))
                    .apply(instance, ClearanceSettings::new);
        });

        public int h_clearance;
        public int v_clearance;
        public int max_distance_from_center;

        public ClearanceSettings(int h_clearance, int v_clearance, int max_distance_from_center)
        {
            this.h_clearance = h_clearance;
            this.v_clearance = v_clearance;
            this.max_distance_from_center = max_distance_from_center;
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

    public final Holder<StructureTemplatePool> startPool;
    public final int max_depth;

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

    public GenericJigsawStructure(//@formatter:off
            Structure.StructureSettings config,
            String pool_name,
            Holder<StructureTemplatePool> start_pool, 
            int maxDepth, 
            YSettings y_settings,
            ClearanceSettings clearances, 
            List<String> required_parts, 
            String _spawn_preset, 
            String _spawn_blacklist,
            final String biome_type, 
            Heightmap.Types height_type,
            final List<ResourceKey<StructureSet>> structures_to_avoid, 
            final int avoid_range, 
            final int biome_room,
            AvoidanceSettings avoidances)
    //@formatter:on
    {
        // Create the pieces layout of the structure and give it to the game
        super(config);
        this.startPool = start_pool;
        this.max_depth = maxDepth;

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
        this.name = pool_name;
        this.avoidances = avoidances;

        if (!_spawn_blacklist.isBlank())
        {
            PokecubeAPI.LOGGER.warn("Warning, spawn blacklist is not used anymore! Use Holdersets instead! {}",
                    pool_name);
        }
        if (!_spawn_preset.isBlank())
        {
            PokecubeAPI.LOGGER.warn("Warning, spawn preset is not used anymore! Use Holdersets instead! {}", pool_name);
        }

        this.underground = "underground".equals(y_settings.surface_type);
        this.air = "air".equals(y_settings.surface_type);
    }

    private boolean tooClose(GenerationContext context)
    {
        ChunkPos pos = context.chunkPos();
        Level level = ExpandedJigsawPacement.getForGen(context);
        List<AvoidanceEntry> avoidances = this.avoidances.avoidances;
        BlockPos bpos = pos.getMiddleBlockPosition(0);
        for (var avoid : avoidances)
        {
            if (avoid.distance > 0 && !avoid.name.isBlank()) if (!PokecubeSerializer.getInstance()
                    .shouldPlace(avoid.name, bpos, level.dimension(), avoid.distance * 16))
            {
                if (PokecubeCore.getConfig().debug_misc)
                    PokecubeAPI.logDebug(this.avoidances.flags + " Conflicts with " + avoid.name);
                return true;
            }
        }
        return false;
    }

    public void markPlaced(GenerationContext context)
    {
        List<String> flags = this.avoidances.flags;
        if (flags.isEmpty()) return;
        ChunkPos pos = context.chunkPos();
        Level level = ExpandedJigsawPacement.getForGen(context);
        BlockPos bpos = pos.getMiddleBlockPosition(0);
        if (PokecubeCore.getConfig().debug_misc) PokecubeAPI.logDebug(this.avoidances.flags + " " + level.dimension());
        for (String flag : flags) PokecubeSerializer.getInstance().place(flag.strip(), bpos, level.dimension());
    }

    private boolean isFeatureChunk(GenerationContext context)
    {
        ChunkGenerator generator = context.chunkGenerator();
        BiomeSource biomes = context.biomeSource();
        ChunkPos pos = context.chunkPos();
        var rng = context.randomState();

        if (tooClose(context)) return false;
        ServerLevel level = ExpandedJigsawPacement.getForGen(context);
        var generatorState = level.getChunkSource().getGeneratorState();

        // Check if we need to avoid any structures.
        for (ResourceKey<StructureSet> key : this.structures_to_avoid)
        {
            Holder<StructureSet> set;
            try
            {
                // Skip if the key is null, this generally means the listed
                // structure to avoid was removed via a datapack!
                set = context.registryAccess().registryOrThrow(RegHelper.STRUCTURE_SET_REGISTRY).getHolderOrThrow(key);
            }
            catch (Exception e)
            {
                continue;
            }
            if (generatorState.hasStructureChunkInRange(set, pos.x, pos.z, this.avoid_range))
            {
                if (PokecubeCore.getConfig().debug_misc)
                    PokecubeAPI.logDebug("Skipping generation of {} due to conflict with {}",
                            this.startPool.value().getClass().getName(), key);
                return false;
            }
        }

        // Check if we have enough biome room around us.
        if (this.biome_room > 0)
        {
            BlockPos p = pos.getMiddleBlockPosition(0);
            int y = generator.getBaseHeight(p.getX(), p.getZ(), this.height_type, context.heightAccessor(), rng);
            Set<Holder<Biome>> biome_set = biomes.getBiomesWithin(p.getX(), y, p.getZ(), this.biome_room,
                    rng.sampler());
            for (var holder : biome_set) if (!context.validBiome().test(holder)) return false;
        }

        // Check the settings for max slope and other height bounds
        int max_y = Integer.MIN_VALUE;
        int min_y = Integer.MAX_VALUE;
        if (!this.underground)
        {
            for (int x = pos.x - this.y_settings.y_check_radius; x <= pos.x + this.y_settings.y_check_radius; x++)
                for (int z = pos.z - this.y_settings.y_check_radius; z <= pos.z + this.y_settings.y_check_radius; z++)
            {
                int height = context.chunkGenerator().getBaseHeight((x << 4) + 7, (z << 4) + 7, this.height_type,
                        context.heightAccessor(), rng);
                max_y = Math.max(max_y, height);
                min_y = Math.min(min_y, height);
                if (min_y < this.y_settings.min_y) return false;
                if (max_y > this.y_settings.max_y) return false;
            }
            if (max_y - min_y > this.y_settings.max_dy)
            {
                return false;
            }
        }
        return true;
    }

    @Override
    public Optional<GenerationStub> findGenerationPoint(GenerationContext context)
    {
        if (!this.isFeatureChunk(context))
        {
            return Optional.empty();
        }
        BlockPos blockpos = context.chunkPos().getMiddleBlockPosition(0);
        return ExpandedJigsawPacement.addPieces(this, context, blockpos, false, true);
    }

    @Override
    public StructureType<?> type()
    {
        return PokecubeStructures.STRUCTURES.get();
    }
}
