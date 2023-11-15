package pokecube.api.data.spawns;

import java.util.Set;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Material;
import pokecube.api.data.PokedexEntry;
import pokecube.core.utils.TimePeriod;
import pokecube.mixin.accessors.WorldGenRegionAccessor;
import pokecube.world.terrain.PokecubeTerrainChecker;
import thut.api.level.structures.NamedVolumes.INamedStructure;
import thut.api.level.terrain.BiomeType;
import thut.api.level.terrain.ITerrainProvider;
import thut.api.level.terrain.TerrainManager;
import thut.api.level.terrain.TerrainSegment;
import thut.api.maths.Vector3;

public class SpawnCheck
{
    public static enum Weather
    {
        SUN, CLOUD, RAIN, SNOW, NONE;

        public static Weather getForWorld(final Level world, final Vector3 location, boolean onlyOutside)
        {
            final boolean globalRain = world.isRaining();
            final BlockPos position = location.getPos();
            if (onlyOutside)
            {
                boolean outside = world.canSeeSky(position);
                outside = outside && position.getY() + 1 > world
                        .getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, position).getY();
                if (!outside) return NONE;
            }
            if (globalRain)
            {
                final Biome.Precipitation type = world.getBiome(position).value().getPrecipitation();
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

    public static enum TerrainType
    {
        FLAT, HILLS;
    }

    private static final String FMT = "{time: %s, light: %d, material: %s, " + "biome: %s, type: %s, "
            + "weather: %s(%b), terrain: %s, location: %s }";

    public final boolean day;
    public final boolean dusk;
    public final boolean dawn;
    public final boolean night;
    public final Material material;
    public final float light;
    public final float time;
    public final Holder<Biome> biome;
    public final BlockState blockState;
    public final FluidState fluid;
    public final BiomeType type;
    // This weather can be "NONE" if the location is not outside
    public final Weather weather;
    // This weather shouldn't ever be "NONE"
    public final Weather outsideWeather;
    public final TerrainType terrain;
    public final boolean thundering;
    public final LevelAccessor world;
    public final ChunkAccess chunk;
    public final Vector3 location;
    // These are only looked up if needed, but then cached for further uses of
    // the spawnCheck
    public Set<INamedStructure> namedStructures = null;

    public SpawnCheck(final Vector3 location, final LevelAccessor world)
    {
        this.world = world;
        this.location = location;
        final Holder<Biome> biome = location.getBiomeHolder(world);
        this.biome = biome;
        this.material = location.getBlockMaterial(world);
        Level level;
        if (world instanceof Level l) level = l;
        else level = ((WorldGenRegionAccessor) world).getServerLevel();
        this.chunk = ITerrainProvider.getChunk(level.dimension(), new ChunkPos(location.getPos()));
        final TerrainSegment t = TerrainManager.getInstance().getTerrian(world, location);
        this.type = t.getBiome(location);
        this.time = (float) TimePeriod.getTime(level);
        this.blockState = location.getBlockState(world);
        this.fluid = world.getFluidState(location.getPos());
        final int lightBlock = world.getMaxLocalRawBrightness(location.getPos());
        this.light = lightBlock / 15f;
        this.weather = Weather.getForWorld(level, location, true);
        this.outsideWeather = weather == Weather.NONE ? Weather.getForWorld(level, location, false) : weather;
        this.thundering = this.weather == Weather.RAIN && level.isThundering();
        this.day = PokedexEntry.day.contains(time);
        this.dusk = PokedexEntry.dusk.contains(time);
        this.dawn = PokedexEntry.dawn.contains(time);
        this.night = PokedexEntry.night.contains(time);
        this.terrain = PokecubeTerrainChecker.getTerrain(location, world);
    }

    @Override
    public String toString()
    {
        String timeStr = day ? "day" : night ? "night" : dusk ? "dusk" : "dawn";
        return String.format(FMT, timeStr, (int) (light * 16), material.getColor().col + "", biome.toString(),
                type.name, weather.toString(), thundering, terrain.toString(), location.getPos().toString());
    }
}
