package pokecube.core.database.spawns;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biome.BiomeCategory;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.material.Material;
import pokecube.core.database.PokedexEntry;
import thut.api.maths.Vector3;
import thut.api.terrain.BiomeDatabase;
import thut.api.terrain.BiomeType;
import thut.api.terrain.ITerrainProvider;
import thut.api.terrain.TerrainManager;
import thut.api.terrain.TerrainSegment;

public class SpawnCheck
{
    public static enum Weather
    {
        SUN, CLOUD, RAIN, SNOW, NONE;

        public static Weather getForWorld(final Level world, final Vector3 location)
        {
            final boolean globalRain = world.isRaining();
            final BlockPos position = location.getPos();
            boolean outside = world.canSeeSky(position);
            outside = outside
                    && world.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, position).getY() > position.getY();
            if (!outside) return NONE;
            if (globalRain)
            {
                final Biome.Precipitation type = world.getBiome(position).getPrecipitation();
                switch (type)
                {
                case NONE:
                    return CLOUD;
                case RAIN:
                    return RAIN;
                case SNOW:
                    return SNOW;
                default:
                    break;
                }
            }
            return SUN;
        }
    }

    public static enum MatchResult
    {
        PASS, SUCCEED, FAIL;
    }

    private static final String FMT = "{time: %s, light: %d, material: %s, " + "biome: %s, type: %s, category: %s, "
            + "weather: %s(%b), location: %s }";

    public final boolean day;
    public final boolean dusk;
    public final boolean dawn;
    public final boolean night;
    public final Material material;
    public final float light;
    public final ResourceKey<Biome> biome;
    public final BiomeType type;
    public final Weather weather;
    public final BiomeCategory cat;
    public final boolean thundering;
    public final LevelAccessor world;
    public final ChunkAccess chunk;
    public final Vector3 location;

    public SpawnCheck(final Vector3 location, final LevelAccessor world, final Biome biome)
    {
        this.world = world;
        this.location = location;
        this.biome = BiomeDatabase.getKey(biome);
        this.day = this.dusk = this.dawn = this.night = true;
        this.weather = Weather.NONE;
        this.thundering = false;
        this.light = 1f;
        this.type = BiomeType.NONE;
        this.material = Material.AIR;
        this.chunk = world.getChunk(location.intX() >> 4, location.intZ() >> 4, ChunkStatus.EMPTY, false);
        this.cat = biome.getBiomeCategory();
    }

    public SpawnCheck(final Vector3 location, final LevelAccessor world)
    {
        this.world = world;
        this.location = location;
        final Biome biome = location.getBiome(world);
        this.biome = BiomeDatabase.getKey(biome);
        this.cat = biome.getBiomeCategory();
        this.material = location.getBlockMaterial(world);
        this.chunk = ITerrainProvider.getChunk(((Level) world).dimension(), new ChunkPos(location.getPos()));
        final TerrainSegment t = TerrainManager.getInstance().getTerrian(world, location);
        this.type = t.getBiome(location);
        // TODO better way to choose current time.
        final double time = ((ServerLevel) world).getDayTime() / 24000.0;
        final int lightBlock = world.getMaxLocalRawBrightness(location.getPos());
        this.light = lightBlock / 15f;
        final Level w = (ServerLevel) world;
        this.weather = Weather.getForWorld(w, location);
        this.thundering = this.weather == Weather.RAIN && w.isThundering();
        this.day = PokedexEntry.day.contains(time);
        this.dusk = PokedexEntry.dusk.contains(time);
        this.dawn = PokedexEntry.dawn.contains(time);
        this.night = PokedexEntry.night.contains(time);
    }

    @Override
    public String toString()
    {
        String timeStr = day ? "day" : night ? "night" : dusk ? "dusk" : "dawn";
        return String.format(FMT, timeStr, (int) (light * 16), material.getColor().col + "", biome.toString(),
                type.name, cat.toString(), weather.toString(), thundering, location.getPos().toString());
    }
}
