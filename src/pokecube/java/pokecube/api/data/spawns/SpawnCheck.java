package pokecube.api.data.spawns;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.material.FluidState;
import pokecube.api.data.PokedexEntry;
import pokecube.api.events.pokemobs.SpawnEvent;
import pokecube.core.utils.TimePeriod;
import pokecube.world.terrain.PokecubeTerrainChecker;
import thut.api.level.structures.NamedVolumes.INamedVolume;
import thut.api.level.terrain.BiomeType;
import thut.api.level.terrain.TerrainManager;
import thut.api.level.terrain.TerrainSegment;
import thut.api.maths.Vector3;
import thut.core.common.ThutCore;

import java.util.Set;

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
                outside = outside && position.getY() + 1 > world.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING,
                        position).getY();
                if (!outside) return NONE;
            }
            if (globalRain)
            {
                final Biome.Precipitation type = world.getBiome(position).value().getPrecipitationAt(position);
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
    public final BlockState state;
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
    public final BlockPos pos;
    public final Holder<DimensionType> dimensionType;
    public final Holder<Level> dimension;
    // These are only looked up if needed, but then cached for further uses of
    // the spawnCheck
    public Set<INamedVolume> namedStructures = null;
    /**
     * RNG seed for shuffling, etc. If this is zero,
     */
    private long RNGSeed = 0;

    public SpawnCheck(SpawnEvent.SpawnContext context)
    {
        this(context.location(), context.level(), context.time());
    }

    public SpawnCheck(final Vector3 location, final LevelAccessor world)
    {
        this(location, world, world.dayTime());
    }

    public SpawnCheck(final Vector3 location, final LevelAccessor world, long time)
    {
        this.world = world;
        this.pos = location.getPos();
        this.biome = location.getBiomeHolder(world);
        this.state = world.getBlockState(location.getPos());
        this.chunk = world.getChunk(pos);
        Level level;
        if (world instanceof Level l) level = l;
        else level = ((WorldGenRegion) world).getLevel();
        final TerrainSegment t = TerrainManager.getInstance().getTerrian(world, location);
        this.dimensionType = level.dimensionTypeRegistration();
        this.dimension = level.holderOrThrow(level.dimension());
        this.type = t.getBiome(location);
        this.time = (float) TimePeriod.getTime(time);
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

    public long getRNGSeed()
    {
        if(RNGSeed==0) RNGSeed = ThutCore.newRandom().nextLong();
        return RNGSeed;
    }

    public void setRNGSeed(long RNGSeed)
    {
        this.RNGSeed = RNGSeed;
    }

    @Override
    public String toString()
    {
        String timeStr = day ? "day" : night ? "night" : dusk ? "dusk" : "dawn";
        return String.format(FMT, timeStr, (int) (light * 16), state.getMapColor(world, pos).col + "", biome.toString(),
                type.name, weather.toString(), thundering, terrain.toString(), pos);
    }
}
