package pokecube.core.handlers.events;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.nfunk.jep.JEP;

import com.google.common.base.Predicate;
import com.google.common.collect.Sets;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntitySpawnPlacementRegistry.PlacementType;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.EntityPredicates;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Difficulty;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.gen.Heightmap.Type;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.spawner.AbstractSpawner;
import net.minecraft.world.spawner.WorldEntitySpawner;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.ForgeEventFactory;
import pokecube.core.PokecubeCore;
import pokecube.core.commands.Pokemake;
import pokecube.core.database.PokedexEntry;
import pokecube.core.database.PokedexEntry.SpawnData;
import pokecube.core.database.PokedexEntryLoader;
import pokecube.core.database.SpawnBiomeMatcher;
import pokecube.core.events.pokemob.SpawnEvent;
import pokecube.core.events.pokemob.SpawnEvent.Function;
import pokecube.core.events.pokemob.SpawnEvent.Variance;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.PokecubeMod;
import pokecube.core.interfaces.capabilities.CapabilityPokemob;
import pokecube.core.utils.ChunkCoordinate;
import pokecube.core.utils.PokecubeSerializer;
import pokecube.core.utils.PokemobTracker;
import pokecube.core.utils.Tools;
import pokecube.core.world.terrain.PokecubeTerrainChecker;
import thut.api.boom.ExplosionCustom;
import thut.api.maths.Vector3;
import thut.api.maths.Vector4;
import thut.api.terrain.BiomeType;
import thut.api.terrain.TerrainManager;
import thut.api.terrain.TerrainSegment;

/** @author Manchou Heavily modified by Thutmose */
public final class SpawnHandler
{

    public static class ForbiddenEntry
    {
        public final int             range;
        public final ForbidReason    reason;
        public final ChunkCoordinate origin;

        public ForbiddenEntry(final int range, final ForbidReason reason, final ChunkCoordinate origin)
        {
            this.range = range;
            this.reason = reason;
            this.origin = origin;
        }
    }

    public static class ForbidReason
    {
        public static final ForbidReason NONE, REPEL, NEST;

        static
        {
            NONE = new ForbidReason("pokecube:none");
            REPEL = new ForbidReason("pokecube:repel");
            NEST = new ForbidReason("pokecube:nest");
        }

        public final ResourceLocation name;

        public ForbidReason(final String name)
        {
            this.name = new ResourceLocation(name);
        }

        @Override
        public String toString()
        {
            return this.name.toString();
        }

        @Override
        public int hashCode()
        {
            return this.name.hashCode();
        }

        @Override
        public boolean equals(final Object obj)
        {
            if (obj instanceof ForbidReason) return ((ForbidReason) obj).name.equals(this.name);
            return false;
        }
    }

    private static Object2ObjectOpenHashMap<DimensionType, Function> functions = new Object2ObjectOpenHashMap<>();
    public static final Object2ObjectOpenHashMap<DimensionType, JEP> parsers   = new Object2ObjectOpenHashMap<>();

    public static Variance DEFAULT_VARIANCE = new Variance();

    private static final Map<ChunkCoordinate, ForbiddenEntry> forbiddenSpawningCoords = new HashMap<>();

    public static HashMap<BiomeType, Variance> subBiomeLevels = new HashMap<>();

    public static boolean doSpawns         = true;
    public static boolean onlySubbiomes    = false;
    public static boolean refreshSubbiomes = false;

    public static HashSet<DimensionType> dimensionBlacklist = Sets.newHashSet();
    public static HashSet<DimensionType> dimensionWhitelist = Sets.newHashSet();

    public static Predicate<Integer> biomeToRefresh = input ->
    {
        if (input == -1 || SpawnHandler.refreshSubbiomes) return true;
        return input == BiomeType.SKY.getType() || input == BiomeType.CAVE.getType() || input == BiomeType.CAVE_WATER
                .getType() || input == BiomeType.ALL.getType() || input == PokecubeTerrainChecker.INSIDE.getType()
                || input == BiomeType.NONE.getType();
    };

    private static Vector3 vec1        = Vector3.getNewVector();
    private static Vector3 temp        = Vector3.getNewVector();
    public static double   MAX_DENSITY = 1;
    public static int      MAXNUM      = 10;
    public static boolean  lvlCap      = false;
    public static boolean  expFunction = false;
    public static int      capLevel    = 50;

    public static boolean addForbiddenSpawningCoord(final BlockPos pos, final int dimensionId, final int distance)
    {
        return SpawnHandler.addForbiddenSpawningCoord(pos.getX(), pos.getY(), pos.getZ(), dimensionId, distance,
                ForbidReason.REPEL);
    }

    public static boolean addForbiddenSpawningCoord(final int x, final int y, final int z, final int dim,
            final int range, final ForbidReason reason)
    {
        final ChunkCoordinate coord = new ChunkCoordinate(x, y, z, dim);
        if (SpawnHandler.forbiddenSpawningCoords.containsKey(coord)) return false;
        SpawnHandler.forbiddenSpawningCoords.put(coord, new ForbiddenEntry(range, reason, coord));
        return true;
    }

    public static boolean canPokemonSpawnHere(final Vector3 location, final World world, final PokedexEntry entry)
    {
        if (!SpawnHandler.canSpawn(entry.getSpawnData(), location, world, true)) return false;
        final EntityType<?> entityTypeIn = entry.getEntityType();
        if (entityTypeIn == null) return false;
        if (WorldEntitySpawner.canSpawnAtBody(PlacementType.ON_GROUND, world, location.getPos(), entityTypeIn))
            return true;
        if (entry.swims()) if (WorldEntitySpawner.canSpawnAtBody(PlacementType.IN_WATER, world, location.getPos(),
                entityTypeIn)) return true;
        return false;
    }

    public static boolean canSpawn(final SpawnData data, final Vector3 v, final World world,
            final boolean respectDensity)
    {
        if (data == null) return false;
        if (respectDensity)
        {
            final int count = PokemobTracker.countPokemobs(world, v, PokecubeCore.getConfig().maxSpawnRadius);
            if (count > PokecubeCore.getConfig().mobSpawnNumber * PokecubeCore.getConfig().mobDensityMultiplier)
                return false;
        }
        return data.isValid(world, v);
    }

    public static boolean canSpawnInWorld(final IWorld world)
    {
        if (world == null) return true;
        if (world.getDifficulty() == Difficulty.PEACEFUL || !SpawnHandler.doSpawns) return false;
        if (SpawnHandler.dimensionBlacklist.contains(world.getDimension().getType())) return false;
        if (PokecubeCore.getConfig().spawnWhitelisted && !SpawnHandler.dimensionWhitelist.contains(world.getDimension()
                .getType())) return false;
        return true;
    }

    public static boolean checkNoSpawnerInArea(final IWorld world, final int x, final int y, final int z)
    {
        final ForbidReason reason = SpawnHandler.getNoSpawnReason(world, x, y, z);
        return reason == ForbidReason.NONE;
    }

    public static void clear()
    {
        SpawnHandler.forbiddenSpawningCoords.clear();
    }

    public static MobEntity creatureSpecificInit(final MobEntity MobEntity, final World world, final double posX,
            final double posY, final double posZ, final Vector3 spawnPoint, final int overrideLevel,
            final Variance variance)
    {
        final AbstractSpawner spawner = new AbstractSpawner()
        {
            @Override
            public void broadcastEvent(final int id)
            {
            }

            @Override
            public BlockPos getSpawnerPosition()
            {
                return spawnPoint.getPos();
            }

            @Override
            public World getWorld()
            {
                return world;
            }
        };
        if (ForgeEventFactory.doSpecialSpawn(MobEntity, world, (float) posX, (float) posY, (float) posZ, spawner,
                SpawnReason.NATURAL)) return null;
        IPokemob pokemob = CapabilityPokemob.getPokemobFor(MobEntity);
        if (pokemob != null)
        {

            final long time = System.nanoTime();
            int maxXP = 10;
            int level = 1;
            if (SpawnHandler.expFunction && overrideLevel == -1)
            {
                maxXP = SpawnHandler.getSpawnXp(world, spawnPoint, pokemob.getPokedexEntry(), variance, overrideLevel);
                final SpawnEvent.Level event = new SpawnEvent.Level(pokemob.getPokedexEntry(), spawnPoint, world, Tools
                        .levelToXp(pokemob.getPokedexEntry().getEvolutionMode(), maxXP), variance);
                PokecubeCore.POKEMOB_BUS.post(event);
                level = event.getLevel();
            }
            else if (overrideLevel == -1) level = SpawnHandler.getSpawnLevel(world, Vector3.getNewVector().set(posX,
                    posY, posZ), pokemob.getPokedexEntry(), variance, overrideLevel);
            else
            {
                final SpawnEvent.Level event = new SpawnEvent.Level(pokemob.getPokedexEntry(), spawnPoint, world,
                        overrideLevel, variance);
                PokecubeCore.POKEMOB_BUS.post(event);
                level = event.getLevel();
            }
            maxXP = Tools.levelToXp(pokemob.getPokedexEntry().getEvolutionMode(), level);
            pokemob.getEntity().getPersistentData().putInt("spawnExp", maxXP);
            pokemob = pokemob.spawnInit();
            final double dt = (System.nanoTime() - time) / 10e3D;
            if (PokecubeMod.debug && dt > 100)
            {
                SpawnHandler.temp.set(SpawnHandler.temp.set(posX, posY, posZ).getPos());
                final String toLog = "location: %1$s took: %2$s\u00B5s to spawn Init for %3$s";
                PokecubeCore.LOGGER.info(String.format(toLog, SpawnHandler.temp, dt, pokemob.getDisplayName()
                        .getFormattedText()));
            }
            return pokemob.getEntity();
        }
        return null;
    }

    public static ForbiddenEntry getForbiddenEntry(final IWorld world, final int x, final int y, final int z)
    {
        final ArrayList<ChunkCoordinate> coords = new ArrayList<>(SpawnHandler.forbiddenSpawningCoords.keySet());
        for (final ChunkCoordinate coord : coords)
        {
            final ForbiddenEntry entry = SpawnHandler.forbiddenSpawningCoords.get(coord);
            final int tolerance = entry.range;
            final int dx = Math.abs(x - coord.getX());
            final int dy = Math.abs(y - coord.getY());
            final int dz = Math.abs(z - coord.getZ());
            if (world.getDimension().getType().getId() == coord.dim && dx <= tolerance && dz <= tolerance
                    && dy <= tolerance) return entry;
        }
        return null;
    }

    public static ForbidReason getNoSpawnReason(final World world, final BlockPos pos)
    {
        return SpawnHandler.getNoSpawnReason(world, pos.getX(), pos.getY(), pos.getZ());
    }

    public static ForbidReason getNoSpawnReason(final IWorld world, final int x, final int y, final int z)
    {
        final ForbiddenEntry entry = SpawnHandler.getForbiddenEntry(world, x, y, z);
        return entry == null ? ForbidReason.NONE : entry.reason;
    }

    public static Vector3 getRandomPointNear(final Entity player, final int range)
    {
        if (player == null) return null;
        final World world = player.getEntityWorld();
        final Vector3 v = SpawnHandler.vec1.set(player);

        final Random rand = new Random();
        // SElect random gaussians from here.
        double x = rand.nextGaussian() * range;
        double z = rand.nextGaussian() * range;

        // Cap x and z to distance.
        if (Math.abs(x) > range) x = Math.signum(x) * range;
        if (Math.abs(z) > range) z = Math.signum(z) * range;

        // Don't select distances too far up/down from current.
        final double y = Math.min(Math.max(-5, rand.nextGaussian() * 10), 10);
        v.addTo(x, y, z);
        v.set(v.getPos()).addTo(0.5, 0.5, 0.5);

        // Don't select unloaded areas.
        if (!TerrainManager.isAreaLoaded(world, v, 8)) return null;

        // Find surface
        final Vector3 temp1 = SpawnHandler.getSpawnSurface(world, v, 10);
        if (!(temp1.isClearOfBlocks(world) || temp1.offset(Direction.UP).isClearOfBlocks(world))) return null;
        return temp1;
    }

    public static PokedexEntry getSpawnForLoc(final World world, final Vector3 pos)
    {
        SpawnEvent.Pick event = new SpawnEvent.Pick.Pre(null, pos, world);
        PokecubeCore.POKEMOB_BUS.post(event);
        PokedexEntry dbe = event.getPicked();
        if (dbe == null) return null;
        event = new SpawnEvent.Pick.Post(dbe, pos, world);
        PokecubeCore.POKEMOB_BUS.post(event);
        dbe = event.getPicked();
        if (event.getLocation() == null) pos.set(0, Double.NaN);
        else pos.set(event.getLocation());
        return dbe;
    }

    public static int getSpawnLevel(final IWorld world, final Vector3 location, final PokedexEntry pokemon)
    {
        return SpawnHandler.getSpawnLevel(world, location, pokemon, SpawnHandler.DEFAULT_VARIANCE, -1);
    }

    public static int getSpawnLevel(final IWorld world, final Vector3 location, final PokedexEntry pokemon,
            Variance variance, final int baseLevel)
    {
        int spawnLevel = baseLevel;

        final TerrainSegment t = TerrainManager.getInstance().getTerrian(world, location);
        final int b = t.getBiome(location);
        final BiomeType type = BiomeType.getType(b);
        if (variance == null) if (SpawnHandler.subBiomeLevels.containsKey(type)) variance = SpawnHandler.subBiomeLevels
                .get(type);
        else variance = SpawnHandler.DEFAULT_VARIANCE;
        if (spawnLevel == -1) if (SpawnHandler.subBiomeLevels.containsKey(type))
        {
            variance = SpawnHandler.subBiomeLevels.get(type);
            spawnLevel = variance.apply(baseLevel);
        }

        if (spawnLevel == baseLevel)
        {
            spawnLevel = SpawnHandler.parse(world, location);
            variance = variance == null ? SpawnHandler.DEFAULT_VARIANCE : variance;
            spawnLevel = variance.apply(spawnLevel);
        }
        final SpawnEvent.Level event = new SpawnEvent.Level(pokemon, location, world, spawnLevel, variance);
        PokecubeCore.POKEMOB_BUS.post(event);
        return event.getLevel();
    }

    public static Vector3 getSpawnSurface(final World world, final Vector3 loc, final int range)
    {
        int tries = 0;
        BlockState state;
        while (tries++ <= range)
        {
            state = loc.getBlockState(world);
            if (state.getMaterial() == Material.WATER) return loc.copy();
            final boolean clear = loc.isClearOfBlocks(world);
            if (clear && !loc.offsetBy(Direction.DOWN).isClearOfBlocks(world)) return loc.copy();
            loc.offsetBy(Direction.DOWN);
        }
        return loc.copy();
    }

    public static int getSpawnXp(final World world, final Vector3 location, final PokedexEntry pokemon)
    {
        int maxXp = 10;
        if (!SpawnHandler.expFunction) return Tools.levelToXp(pokemon.getEvolutionMode(), SpawnHandler.getSpawnLevel(
                world, location, pokemon));
        final TerrainSegment t = TerrainManager.getInstance().getTerrian(world, location);
        final int b = t.getBiome(location);
        final BiomeType type = BiomeType.getType(b);
        if (SpawnHandler.subBiomeLevels.containsKey(type))
        {
            final int level = SpawnHandler.subBiomeLevels.get(type).apply(-1);
            maxXp = Math.max(10, Tools.levelToXp(pokemon.getEvolutionMode(), level));
            return maxXp;
        }
        maxXp = SpawnHandler.parse(world, location);
        maxXp = Math.max(maxXp, 10);
        int level = Tools.xpToLevel(pokemon.getEvolutionMode(), maxXp);
        final Variance variance = SpawnHandler.DEFAULT_VARIANCE;
        level = variance.apply(level);
        level = Math.max(1, level);
        return Tools.levelToXp(pokemon.getEvolutionMode(), level);
    }

    public static int getSpawnXp(final World world, final Vector3 location, final PokedexEntry pokemon,
            Variance variance, final int baseLevel)
    {
        int maxXp = 10;
        if (!SpawnHandler.expFunction) return Tools.levelToXp(pokemon.getEvolutionMode(), SpawnHandler.getSpawnLevel(
                world, location, pokemon, variance, baseLevel));
        final TerrainSegment t = TerrainManager.getInstance().getTerrian(world, location);
        final int b = t.getBiome(location);
        final BiomeType type = BiomeType.getType(b);
        if (SpawnHandler.subBiomeLevels.containsKey(type))
        {
            final int level = SpawnHandler.subBiomeLevels.get(type).apply(baseLevel);
            maxXp = Math.max(10, Tools.levelToXp(pokemon.getEvolutionMode(), level));
            return maxXp;
        }
        maxXp = SpawnHandler.parse(world, location);
        maxXp = Math.max(maxXp, 10);
        int level = Tools.xpToLevel(pokemon.getEvolutionMode(), maxXp);
        variance = variance == null ? SpawnHandler.DEFAULT_VARIANCE : variance;
        level = variance.apply(level);
        level = Math.max(1, level);
        return Tools.levelToXp(pokemon.getEvolutionMode(), level);
    }

    public static boolean isPointValidForSpawn(final World world, final Vector3 point, final PokedexEntry dbe)
    {
        final int i = point.intX();
        final int j = point.intY();
        final int k = point.intZ();
        if (!SpawnHandler.checkNoSpawnerInArea(world, i, j, k)) return false;
        final boolean validLocation = SpawnHandler.canPokemonSpawnHere(point, world, dbe);
        return validLocation;
    }

    public static void loadFunctionFromString(final String args)
    {
        final Function func = PokedexEntryLoader.gson.fromJson(args, Function.class);
        final DimensionType dim = DimensionType.byName(new ResourceLocation(func.dim));
        SpawnHandler.functions.put(dim, func);
        SpawnHandler.parsers.put(dim, SpawnHandler.initJEP(new JEP(), func.func, func.radial));
    }

    public static JEP getParser(final DimensionType dim)
    {
        if (SpawnHandler.functions.isEmpty()) SpawnHandler.initSpawnFunctions();
        return SpawnHandler.parsers.getOrDefault(dim, SpawnHandler.parsers.get(DimensionType.OVERWORLD));
    }

    public static Function getFunction(final DimensionType dim)
    {
        if (SpawnHandler.functions.isEmpty()) SpawnHandler.initSpawnFunctions();
        return SpawnHandler.functions.getOrDefault(dim, SpawnHandler.functions.get(DimensionType.OVERWORLD));
    }

    public static void initSpawnFunctions()
    {
        for (final String s : PokecubeCore.getConfig().dimensionSpawnLevels)
            SpawnHandler.loadFunctionFromString(s);
    }

    public static void makeMeteor(final World world, final Vector3 location, final float power)
    {
        if (power > 0)
        {
            final ExplosionCustom boom = new ExplosionCustom(world, null, location, (float) (power * PokecubeCore
                    .getConfig().meteorScale)).setMeteor(true).setMaxRadius(PokecubeCore.getConfig().meteorRadius);
            if (PokecubeMod.debug)
            {
                final String message = "Meteor at " + location + " with energy of " + power;
                PokecubeCore.LOGGER.info(message);
            }
            boom.doExplosion();
        }
        PokecubeSerializer.getInstance().addMeteorLocation(new Vector4(location.x, location.y, location.z, world
                .getDimension().getType().getId()));
    }

    private static int parse(final IWorld world, final Vector3 location)
    {
        final Vector3 spawn = SpawnHandler.temp.set(world.getWorld().getSpawnPoint());
        final DimensionType type = world.getDimension().getType();
        final JEP toUse = SpawnHandler.getParser(type);
        final Function function = SpawnHandler.getFunction(type);
        final boolean r = function.radial;
        if (!r) SpawnHandler.parseExpression(toUse, location.x - spawn.x, location.z - spawn.z, r);
        else
        {
            /**
             * Set y coordinates equal to ensure only radial function in
             * horizontal plane.
             */
            spawn.y = location.y;
            final double d = location.distTo(spawn);
            SpawnHandler.parseExpression(toUse, d, location.y, r);
        }
        return (int) Math.abs(toUse.getValue());
    }

    private static JEP initJEP(final JEP parser, final String toParse, final boolean radial)
    {
        parser.initFunTab(); // clear the contents of the function table
        parser.addStandardFunctions();
        parser.initSymTab(); // clear the contents of the symbol table
        parser.addStandardConstants();
        parser.addComplex(); // among other things adds i to the symbol
                             // table
        if (!radial)
        {
            parser.addVariable("x", 0);
            parser.addVariable("y", 0);
        }
        else parser.addVariable("r", 0);
        parser.parseExpression(toParse);
        return parser;
    }

    private static void parseExpression(final JEP parser, final double xValue, final double yValue, final boolean r)
    {
        if (!r)
        {
            parser.setVarValue("x", xValue);
            parser.setVarValue("y", yValue);
        }
        else parser.setVarValue("r", xValue);
    }

    public static void refreshTerrain(final Vector3 location, final World world)
    {
        SpawnHandler.refreshTerrain(location, world, false);
    }

    public static void refreshTerrain(final Vector3 location, final World world, final boolean precise)
    {
        if (!PokecubeCore.getConfig().autoDetectSubbiomes) return;
        final TerrainSegment t = TerrainManager.getInstance().getTerrian(world, location);
        final Vector3 temp1 = Vector3.getNewVector();
        final int x0 = t.chunkX * 16, y0 = t.chunkY * 16, z0 = t.chunkZ * 16;
        final int dx = TerrainSegment.GRIDSIZE / 2;
        final int dy = TerrainSegment.GRIDSIZE / 2;
        final int dz = TerrainSegment.GRIDSIZE / 2;
        final int x1 = x0 + dx, y1 = y0 + dy, z1 = z0 + dz;

        if (precise)
        {
            final int i = location.intX();
            final int j = location.intY();
            final int k = location.intZ();
            int biome = t.getBiome(i, j, k);
            if (SpawnHandler.biomeToRefresh.apply(biome))
            {
                biome = t.adjustedCaveBiome(world, temp1);
                if (biome == -1) biome = t.adjustedNonCaveBiome(world, temp1);
                t.setBiome(i, j, k, biome);
            }
        }
        else for (int i = x1; i < x1 + 16; i += TerrainSegment.GRIDSIZE)
            for (int j = y1; j < y1 + 16; j += TerrainSegment.GRIDSIZE)
                for (int k = z1; k < z1 + 16; k += TerrainSegment.GRIDSIZE)
                {
                    temp1.set(i, j, k);
                    int biome = t.getBiome(i, j, k);
                    if (SpawnHandler.biomeToRefresh.apply(biome))
                    {
                        biome = t.adjustedCaveBiome(world, temp1);
                        if (biome == -1) biome = t.adjustedNonCaveBiome(world, temp1);
                        t.setBiome(i, j, k, biome);
                    }
                }
    }

    public static boolean removeForbiddenSpawningCoord(final BlockPos pos, final int dimensionId)
    {
        return SpawnHandler.removeForbiddenSpawningCoord(pos.getX(), pos.getY(), pos.getZ(), dimensionId);
    }

    public static boolean removeForbiddenSpawningCoord(final int x, final int y, final int z, final int dim)
    {
        final ChunkCoordinate coord = new ChunkCoordinate(x, y, z, dim);
        return SpawnHandler.forbiddenSpawningCoords.remove(coord) != null;
    }

    public JEP parser = new JEP();
    Vector3    v      = Vector3.getNewVector();

    Vector3 v1 = Vector3.getNewVector();

    Vector3 v2 = Vector3.getNewVector();

    Vector3 v3 = Vector3.getNewVector();

    public SpawnHandler()
    {
        if (PokecubeCore.getConfig().pokemonSpawn) MinecraftForge.EVENT_BUS.register(this);
    }

    public void doMeteor(final ServerWorld world)
    {
        if (!PokecubeCore.getConfig().meteors) return;
        if (!world.dimension.isSurfaceWorld()) return;
        if (world.dimension.isNether()) return;
        final List<ServerPlayerEntity> players = world.getPlayers();
        if (players.size() < 1) return;
        final Random rand = new Random(world.getSeed() + world.getGameTime());
        if (rand.nextInt(100) == 0)
        {
            final Entity player = players.get(rand.nextInt(players.size()));
            final int dx = rand.nextInt(200) - 100;
            final int dz = rand.nextInt(200) - 100;
            final Vector3 v = this.v.set(player).add(dx, 0, dz);
            final Vector4 loc = new Vector4(player);
            loc.x += dx;
            loc.z += dz;
            loc.y = world.getHeight(Type.WORLD_SURFACE, (int) loc.x, (int) loc.z);
            if (PokecubeSerializer.getInstance().canMeteorLand(loc, world))
            {
                final Vector3 direction = this.v1.set(rand.nextGaussian() / 2, -1, rand.nextGaussian() / 2);
                v.set(loc.x, loc.y, loc.z);
                final Vector3 location = Vector3.getNextSurfacePoint(world, v, direction, 255);
                if (location != null)
                {
                    if (world.getClosestPlayer(location.x, location.y, location.z, 96,
                            EntityPredicates.NOT_SPECTATING) != null) return;
                    final float energy = (float) Math.abs((rand.nextGaussian() + 1) * 50);
                    SpawnHandler.makeMeteor(world, location, energy);
                }
            }
        }
    }

    public int doSpawnForLocation(final World world, final Vector3 v)
    {
        return this.doSpawnForLocation(world, v, true);
    }

    public int doSpawnForLocation(final World world, final Vector3 v, final boolean checkPlayers)
    {
        int ret = 0;
        int num = 0;
        if (!SpawnHandler.checkNoSpawnerInArea(world, v.intX(), v.intY(), v.intZ())) return ret;
        if (v.y <= 0 || v.y >= world.getActualHeight()) return ret;
        SpawnHandler.refreshTerrain(v, world, true);
        final TerrainSegment t = TerrainManager.getInstance().getTerrian(world, v);
        if (SpawnHandler.onlySubbiomes && t.getBiome(v) < 0) return ret;
        long time = System.nanoTime();
        final PokedexEntry dbe = SpawnHandler.getSpawnForLoc(world, v);
        if (dbe == null) return ret;
        if (v.isNaN()) return ret;
        if (!SpawnHandler.isPointValidForSpawn(world, v, dbe)) return ret;
        double dt = (System.nanoTime() - time) / 10e3D;
        if (PokecubeMod.debug && dt > 500)
        {
            SpawnHandler.temp.set(v.getPos());
            final String toLog = "location: %1$s took: %2$s\u00B5s to find a valid spawn and location";
            PokecubeCore.LOGGER.info(String.format(toLog, SpawnHandler.temp, dt));
        }
        time = System.nanoTime();
        ret += num = this.doSpawnForType(world, v, dbe, this.parser, t, checkPlayers);
        dt = (System.nanoTime() - time) / 10e3D;
        if (PokecubeMod.debug && dt > 500)
        {
            SpawnHandler.temp.set(v.getPos());
            final String toLog = "location: %1$s took: %2$s\u00B5s to find a valid spawn for %3$s %4$s";
            PokecubeCore.LOGGER.info(String.format(toLog, SpawnHandler.temp, dt, num, dbe));
        }
        return ret;
    }

    /**
     * Attempts to spawn mobs near the player.
     *
     * @param player
     * @param world
     * @return number of mobs spawned.
     */
    public void doSpawnForPlayer(final PlayerEntity player, final World world)
    {
        long time = System.nanoTime();
        final int radius = PokecubeCore.getConfig().maxSpawnRadius;
        this.v.set(player);
        if (!TerrainManager.isAreaLoaded(world, this.v, radius)) return;
        if (!Tools.isAnyPlayerInRange(radius, 10, world, this.v)) return;
        final int height = world.getActualHeight();
        final AxisAlignedBB box = this.v.getAABB().grow(radius, Math.max(height, radius), radius);
        int num = PokemobTracker.countPokemobs(world, box);
        if (num > SpawnHandler.MAX_DENSITY * SpawnHandler.MAXNUM) return;
        final Vector3 v = SpawnHandler.getRandomPointNear(player, PokecubeCore.getConfig().maxSpawnRadius);
        double dt = (System.nanoTime() - time) / 1000d;
        if (PokecubeMod.debug && dt > 100) PokecubeCore.LOGGER.info("Location Find took " + dt);
        if (v == null) return;
        time = System.nanoTime();
        num = this.doSpawnForLocation(world, v);
        dt = (System.nanoTime() - time) / 10e3D;
        if (PokecubeMod.debug && dt > 100) PokecubeCore.LOGGER.info(dt + "\u00B5" + "s for player " + player
                .getDisplayName().getUnformattedComponentText() + " at " + v + ", spawned " + num);
        return;
    }

    private int doSpawnForType(final World world, final Vector3 loc, final PokedexEntry dbe, final JEP parser,
            final TerrainSegment t, final boolean checkPlayers)
    {
        final SpawnData entry = dbe.getSpawnData();

        int totalSpawnCount = 0;
        final Vector3 offset = this.v1.clear();
        final Vector3 point = this.v2.clear();
        SpawnHandler.refreshTerrain(loc, world, false);
        final SpawnBiomeMatcher matcher = entry.getMatcher(world, loc);
        if (matcher == null) return 0;
        final byte distGroupZone = 6;
        final Random rand = new Random();
        final int n = Math.max(entry.getMax(matcher) - entry.getMin(matcher), 1);
        final int spawnNumber = entry.getMin(matcher) + rand.nextInt(n);

        for (int i = 0; i < spawnNumber; i++)
        {
            offset.set(rand.nextInt(distGroupZone) - rand.nextInt(distGroupZone), rand.nextInt(1) - rand.nextInt(1),
                    rand.nextInt(distGroupZone) - rand.nextInt(distGroupZone));
            this.v.set(loc);
            point.set(this.v.addTo(offset));
            if (!SpawnHandler.isPointValidForSpawn(world, point, dbe)) continue;

            final float x = (float) point.x + 0.5F;
            final float y = (float) point.y;
            final float z = (float) point.z + 0.5F;

            final float var28 = x - world.getSpawnPoint().getX();
            final float var29 = y - world.getSpawnPoint().getY();
            final float var30 = z - world.getSpawnPoint().getZ();
            final float distFromSpawnPoint = var28 * var28 + var29 * var29 + var30 * var30;

            if (!SpawnHandler.checkNoSpawnerInArea(world, (int) x, (int) y, (int) z)) continue;
            final float dist = PokecubeCore.getConfig().minSpawnRadius;
            final boolean player = checkPlayers && Tools.isAnyPlayerInRange(dist, dist, world, point);
            if (player) continue;
            if (distFromSpawnPoint >= 256.0F)
            {
                MobEntity entity = null;
                try
                {
                    if (dbe.getPokedexNb() > 0)
                    {
                        final SpawnEvent.Pick.Final event = new SpawnEvent.Pick.Final(dbe, point, world);
                        PokecubeCore.POKEMOB_BUS.post(event);
                        if (event.getPicked() == null) continue;
                        entity = PokecubeCore.createPokemob(event.getPicked(), world);
                        entity.setHealth(entity.getMaxHealth());
                        entity.setLocationAndAngles((double) x + 0.5F, (double) y + 0.5F, (double) z + 0.5F, world.rand
                                .nextFloat() * 360.0F, 0.0F);
                        if (entity.canSpawn(world, SpawnReason.NATURAL))
                        {
                            if ((entity = SpawnHandler.creatureSpecificInit(entity, world, x, y, z, this.v3.set(entity),
                                    entry.getLevel(matcher), entry.getVariance(matcher))) != null)
                            {
                                final IPokemob pokemob = CapabilityPokemob.getPokemobFor(entity);
                                if (!event.getSpawnArgs().isEmpty())
                                {
                                    final String[] args = event.getSpawnArgs().split(" ");
                                    Pokemake.setToArgs(args, pokemob, 0, this.v, false);
                                }
                                else if (matcher.spawnRule.values.containsKey(SpawnBiomeMatcher.SPAWNCOMMAND))
                                {
                                    final String[] args = matcher.spawnRule.values.get(SpawnBiomeMatcher.SPAWNCOMMAND)
                                            .split(" ");
                                    Pokemake.setToArgs(args, pokemob, 0, this.v, false);
                                }
                                final SpawnEvent.Post evt = new SpawnEvent.Post(dbe, this.v3, world, pokemob);
                                PokecubeCore.POKEMOB_BUS.post(evt);
                                world.addEntity(entity);
                                totalSpawnCount++;
                            }
                        }
                        else entity.remove();
                    }
                }
                catch (final Throwable e)
                {
                    if (entity != null) entity.remove();

                    System.err.println("Wrong Id while spawn: " + dbe.getName());
                    e.printStackTrace();

                    return totalSpawnCount;
                }
            }

        }
        return totalSpawnCount;
    }

    public void spawn(final ServerWorld world)
    {
        final List<ServerPlayerEntity> players = world.getPlayers();
        if (players.isEmpty()) return;
        Collections.shuffle(players);
        for (final ServerPlayerEntity player : players)
        {
            if (player.dimension != world.getDimension().getType()) continue;
            this.doSpawnForPlayer(player, world);
        }
    }

    public void tick(final ServerWorld world)
    {
        if (!SpawnHandler.canSpawnInWorld(world)) return;
        try
        {
            final int rate = PokecubeCore.getConfig().spawnRate;
            if (world.getGameTime() % rate == 0)
            {
                final long time = System.nanoTime();
                this.spawn(world);
                final double dt = (System.nanoTime() - time) / 1000d;
                if (PokecubeMod.debug && dt > 100) PokecubeCore.LOGGER.info("SpawnTick took " + dt);
            }
            this.doMeteor(world);
        }
        catch (final Exception e)
        {
            e.printStackTrace();
        }
    }
}
