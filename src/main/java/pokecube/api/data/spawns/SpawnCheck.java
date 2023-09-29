package pokecube.api.data.spawns;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.material.FluidState;
import pokecube.api.data.PokedexEntry;
import pokecube.core.utils.TimePeriod;
import pokecube.mixin.accessors.WorldGenRegionAccessor;
import pokecube.world.terrain.PokecubeTerrainChecker;
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

        public static Weather getForWorld(final Level world, final Vector3 location)
        {
            final boolean globalRain = world.isRaining();
            final BlockPos position = location.getPos();
            boolean outside = world.canSeeSky(position);
            outside = outside
                    && position.getY() + 1 > world.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, position).getY();
            if (!outside) return NONE;
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
    public final Weather weather;
    public final TerrainType terrain;
    public final boolean thundering;
    public final LevelAccessor world;
    public final ChunkAccess chunk;
    public final BlockPos pos;

    public SpawnCheck(final Vector3 location, final ServerLevelAccessor world)
    {
        this.world = world;
        this.pos = location.getPos();
        this.biome = location.getBiomeHolder(world);
        this.state = world.getBlockState(location.getPos());
        ServerLevel level;
        if (world instanceof ServerLevel) level = (ServerLevel) world;
        else level = ((WorldGenRegionAccessor) world).getServerLevel();
        this.chunk = ITerrainProvider.getChunk(level.dimension(), new ChunkPos(location.getPos()));
        final TerrainSegment t = TerrainManager.getInstance().getTerrian(world, location);
        this.type = t.getBiome(location);
        this.time = (float) TimePeriod.getTime(level);
        this.blockState = location.getBlockState(world);
        this.fluid = world.getFluidState(location.getPos());
        final int lightBlock = world.getMaxLocalRawBrightness(location.getPos());
        this.light = lightBlock / 15f;
        this.weather = Weather.getForWorld(level, location);
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
        return String.format(FMT, timeStr, (int) (light * 16), state.getMapColor(world, pos).col + "", biome.toString(),
                type.name, weather.toString(), thundering, terrain.toString(), pos);
    }
}
