package pokecube.core.handlers.events;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.nfunk.jep.JEP;

import com.google.common.base.Predicate;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import it.unimi.dsi.fastutil.objects.Object2FloatMap.Entry;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntitySpawnPlacementRegistry.PlacementType;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.EntityPredicates;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.GlobalPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.Difficulty;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.IChunk;
import net.minecraft.world.gen.Heightmap;
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
import pokecube.core.events.MeteorEvent;
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
import thut.api.boom.ExplosionCustom.BlastResult;
import thut.api.boom.ExplosionCustom.BlockBreaker;
import thut.api.maths.Vector3;
import thut.api.maths.Vector4;
import thut.api.terrain.BiomeType;
import thut.api.terrain.TerrainManager;
import thut.api.terrain.TerrainSegment;

/** @author Manchou Heavily modified by Thutmose */
public final class SpawnHandler
{
    public static interface ForbidRegion
    {
        boolean isInside(BlockPos pos);

        BlockPos getPos();
    }

    public static class CubeRegion implements ForbidRegion
    {
        public final int      range;
        public final BlockPos origin;

        public CubeRegion(final int range, final BlockPos origin)
        {
            this.range = range;
            this.origin = origin;
        }

        @Override
        public boolean isInside(final BlockPos pos)
        {
            return ChunkCoordinate.isWithin(pos, this.origin, this.range);
        }

        @Override
        public BlockPos getPos()
        {
            return this.origin;
        }
    }

    public static class AABBRegion implements ForbidRegion
    {
        private final AxisAlignedBB box;

        private final BlockPos mid;

        public AABBRegion(final AxisAlignedBB box)
        {
            this.box = box;
            this.mid = new BlockPos(box.getCenter());
        }

        @Override
        public boolean isInside(final BlockPos pos)
        {
            return this.box.contains(pos.getX(), pos.getY(), pos.getZ());
        }

        @Override
        public BlockPos getPos()
        {
            return this.mid;
        }

    }

    public static class ForbiddenEntry
    {
        public final ForbidReason reason;
        public final ForbidRegion region;

        public ForbiddenEntry(final ForbidReason reason, final ForbidRegion region)
        {
            this.reason = reason;
            this.region = region;
        }

        public ForbiddenEntry(final int range, final ForbidReason reason, final BlockPos origin)
        {
            this(reason, new CubeRegion(range, origin));
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

    private static Object2ObjectOpenHashMap<RegistryKey<World>, Function> functions = new Object2ObjectOpenHashMap<>();
    public static final Object2ObjectOpenHashMap<RegistryKey<World>, JEP> parsers   = new Object2ObjectOpenHashMap<>();

    public static Variance DEFAULT_VARIANCE = new Variance();

    private static final Map<RegistryKey<World>, Map<BlockPos, ForbiddenEntry>> forbidReasons = new HashMap<>();

    public static HashMap<BiomeType, Variance> subBiomeLevels = new HashMap<>();

    public static boolean doSpawns         = true;
    public static boolean onlySubbiomes    = false;
    public static boolean refreshSubbiomes = false;

    public static HashSet<RegistryKey<World>> dimensionBlacklist = Sets.newHashSet();
    public static HashSet<RegistryKey<World>> dimensionWhitelist = Sets.newHashSet();

    public static Predicate<Integer> biomeToRefresh = input ->
    {
        if (input == -1 || SpawnHandler.refreshSubbiomes) return true;
        return input == BiomeType.SKY.getType() || input == BiomeType.CAVE.getType() || input == BiomeType.CAVE_WATER
                .getType() || input == BiomeType.ALL.getType() || input == PokecubeTerrainChecker.INSIDE.getType()
                || input == BiomeType.NONE.getType();
    };

    public static double  MAX_DENSITY = 1;
    public static int     MAXNUM      = 10;
    public static boolean lvlCap      = false;
    public static int     capLevel    = 50;

    public static boolean addForbiddenSpawningCoord(final BlockPos pos, final World dim, final int range,
            final ForbidReason reason)
    {
        Map<BlockPos, ForbiddenEntry> entries = SpawnHandler.forbidReasons.get(dim.getDimensionKey());
        if (entries == null) SpawnHandler.forbidReasons.put(dim.getDimensionKey(), entries = Maps.newHashMap());
        if (entries.containsKey(pos)) return false;
        entries.put(pos, new ForbiddenEntry(range, reason, pos));
        return true;
    }

    public static boolean addForbiddenSpawningCoord(final World dim, final ForbidRegion region,
            final ForbidReason reason)
    {
        Map<BlockPos, ForbiddenEntry> entries = SpawnHandler.forbidReasons.get(dim.getDimensionKey());
        if (entries == null) SpawnHandler.forbidReasons.put(dim.getDimensionKey(), entries = Maps.newHashMap());
        if (entries.containsKey(region.getPos())) return false;
        entries.put(region.getPos(), new ForbiddenEntry(reason, region));
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

    public static boolean canSpawnInWorld(final World world, final boolean respectDifficulty)
    {
        if (world == null) return true;
        if (respectDifficulty && world.getDifficulty() == Difficulty.PEACEFUL) return false;
        if (!SpawnHandler.doSpawns) return false;
        if (SpawnHandler.dimensionBlacklist.contains(world.getDimensionKey())) return false;
        if (PokecubeCore.getConfig().spawnWhitelisted && !SpawnHandler.dimensionWhitelist.contains(world
                .getDimensionKey())) return false;
        return true;
    }

    public static boolean canSpawnInWorld(final World world)
    {
        return SpawnHandler.canSpawnInWorld(world, true);
    }

    public static boolean checkNoSpawnerInArea(final World world, final int x, final int y, final int z)
    {
        final ForbidReason reason = SpawnHandler.getNoSpawnReason(world, x, y, z);
        return reason == ForbidReason.NONE;
    }

    public static void clear()
    {
        SpawnHandler.forbidReasons.clear();
    }

    public static MobEntity creatureSpecificInit(final MobEntity MobEntity, final World world, final double posX,
            final double posY, final double posZ, final Vector3 spawnPoint, final SpawnData entry,
            final SpawnBiomeMatcher matcher)
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
        final IPokemob pokemob = CapabilityPokemob.getPokemobFor(MobEntity);
        if (pokemob != null)
        {
            pokemob.spawnInit(matcher.spawnRule);
            return pokemob.getEntity();
        }
        return null;
    }

    public static ForbiddenEntry getForbiddenEntry(final World world, final int x, final int y, final int z)
    {
        final Map<BlockPos, ForbiddenEntry> entries = SpawnHandler.forbidReasons.get(world.getDimensionKey());
        if (entries == null) return null;
        final BlockPos here = new BlockPos(x, y, z);
        for (final ForbiddenEntry entry : entries.values())
            if (entry.region.isInside(here)) return entry;
        return null;
    }

    public static ForbidReason getNoSpawnReason(final World world, final BlockPos pos)
    {
        return SpawnHandler.getNoSpawnReason(world, pos.getX(), pos.getY(), pos.getZ());
    }

    public static ForbidReason getNoSpawnReason(final World world, final int x, final int y, final int z)
    {
        final ForbiddenEntry entry = SpawnHandler.getForbiddenEntry(world, x, y, z);
        return entry == null ? ForbidReason.NONE : entry.reason;
    }

    private static BlockPos getRandomHeight(final World worldIn, final Chunk chunk, final int yCenter, final int dy)
    {
        final ChunkPos chunkpos = chunk.getPos();
        final int x = chunkpos.getXStart() + worldIn.rand.nextInt(16);
        final int z = chunkpos.getZStart() + worldIn.rand.nextInt(16);
        int y = yCenter - dy + worldIn.rand.nextInt(2 * dy + 1);
        final int top = chunk.getTopBlockY(Heightmap.Type.WORLD_SURFACE, x, z) + 1;
        if (y > top) y = top;
        return new BlockPos(x, y, z);
    }

    public static Vector3 getRandomPointNear(final ServerWorld world, final Vector3 pos, final int range)
    {
        // Lets try a few times
        int n = 10;
        while (n-- > 0)
        {
            int dx = world.getRandom().nextInt(range);
            int dz = world.getRandom().nextInt(range);
            dx *= world.getRandom().nextBoolean() ? 1 : -1;
            dz *= world.getRandom().nextBoolean() ? 1 : -1;
            final int dy = 10;
            final Vector3 vec = pos.add(dx, 0, dz);
            final IChunk chunk = world.getChunk(vec.getPos());
            if (!(chunk instanceof Chunk)) continue;
            final BlockPos blockpos = SpawnHandler.getRandomHeight(world, (Chunk) chunk, vec.intY(), dy);
            final int j = blockpos.getX();
            final int k = blockpos.getY();
            final int l = blockpos.getZ();
            vec.set(j + 0.5, k, l + 0.5);
            if (vec.distanceTo(pos) > range) continue;
            final BlockState blockstate = world.getBlockState(blockpos);
            if (blockstate.isNormalCube(world, blockpos)) continue;
            final VoxelShape shape = blockstate.getCollisionShape(world, blockpos);
            if (!shape.isEmpty()) vec.y += shape.getEnd(Axis.Y);
            return vec;
        }
        return null;

    }

    public static Vector3 getRandomPointNear(final Entity player, final int range)
    {
        if (player == null || !(player.getEntityWorld() instanceof ServerWorld)) return null;
        return SpawnHandler.getRandomPointNear((ServerWorld) player.getEntityWorld(), Vector3.getNewVector().set(
                player), range);
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

    public static int getSpawnLevel(final World world, final Vector3 location, final PokedexEntry pokemon)
    {
        return SpawnHandler.getSpawnLevel(world, location, pokemon, SpawnHandler.DEFAULT_VARIANCE, -1);
    }

    public static int getSpawnLevel(final World world, final Vector3 location, final PokedexEntry pokemon,
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
        if (loc.y > 0) while (tries++ <= range)
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
        return Tools.levelToXp(pokemon.getEvolutionMode(), SpawnHandler.getSpawnLevel(world, location, pokemon));
    }

    public static int getSpawnXp(final World world, final Vector3 location, final PokedexEntry pokemon,
            final Variance variance, final int baseLevel)
    {
        return Tools.levelToXp(pokemon.getEvolutionMode(), SpawnHandler.getSpawnLevel(world, location, pokemon,
                variance, baseLevel));
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
        final RegistryKey<World> dim = RegistryKey.getOrCreateKey(Registry.WORLD_KEY, new ResourceLocation(func.dim));
        SpawnHandler.functions.put(dim, func);
        SpawnHandler.parsers.put(dim, SpawnHandler.initJEP(new JEP(), func.func, func.radial));
    }

    public static JEP getParser(final RegistryKey<World> type)
    {
        if (SpawnHandler.functions.isEmpty()) SpawnHandler.initSpawnFunctions();
        return SpawnHandler.parsers.getOrDefault(type, SpawnHandler.parsers.get(World.OVERWORLD));
    }

    public static Function getFunction(final RegistryKey<World> dim)
    {
        if (SpawnHandler.functions.isEmpty()) SpawnHandler.initSpawnFunctions();
        return SpawnHandler.functions.getOrDefault(dim, SpawnHandler.functions.get(World.OVERWORLD));
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
                    .getConfig().meteorScale)).setMaxRadius(PokecubeCore.getConfig().meteorRadius);

            boom.breaker = new BlockBreaker()
            {
                @Override
                public void breakBlocks(final BlastResult result, final ExplosionCustom boom)
                {
                    for (final Entry<BlockPos> entry : result.results.object2FloatEntrySet())
                    {
                        final BlockPos pos = entry.getKey();
                        final float power = entry.getFloatValue();
                        boom.getAffectedBlockPositions().add(pos);
                        final BlockState destroyed = boom.world.getBlockState(pos);
                        BlockState to = Blocks.AIR.getDefaultState();
                        if (power < 36)
                        {
                            if (destroyed.getMaterial() == Material.LEAVES) to = Blocks.FIRE.getDefaultState();
                            if (destroyed.getMaterial() == Material.TALL_PLANTS) to = Blocks.FIRE.getDefaultState();
                        }
                        final MeteorEvent event = new MeteorEvent(destroyed, to, pos, power, boom);
                        MinecraftForge.EVENT_BUS.post(event);
                        final TerrainSegment seg = TerrainManager.getInstance().getTerrain(boom.world, pos);
                        seg.setBiome(pos, BiomeType.METEOR.getType());
                        boom.world.setBlockState(pos, to, 3);
                    }
                }
            };

            final String message = "Meteor at " + location + " with energy of " + power;
            PokecubeCore.LOGGER.debug(message);
            boom.doExplosion();
        }
        PokecubeSerializer.getInstance().addMeteorLocation(GlobalPos.getPosition(world.getDimensionKey(), location
                .getPos()));
    }

    private static int parse(final World world, final Vector3 location)
    {
        if (!(world instanceof ServerWorld)) return 0;
        // BlockPos p = world.
        final Vector3 spawn = Vector3.getNewVector().set(((ServerWorld) world).getSpawnPoint());
        final RegistryKey<World> type = world.getDimensionKey();
        final JEP toUse = SpawnHandler.getParser(type);
        final Function function = SpawnHandler.getFunction(type);
        final boolean r = function.radial;
        // Central functions are centred on 0,0, not the world spawn
        if (function.central) spawn.clear();
        if (!r) SpawnHandler.parseExpression(toUse, location.x - spawn.x, location.z - spawn.z, r);
        else
        {
            /**
             * Set y coordinates equal to ensure only radial function in
             * horizontal plane.
             */
            spawn.y = location.y;
            final double d = location.distTo(spawn);
            final double t = MathHelper.atan2(location.x, location.z);
            SpawnHandler.parseExpression(toUse, d, t, r);
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
        else
        {
            parser.addVariable("r", 0);
            parser.addVariable("t", 0);
        }
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
        else
        {
            parser.setVarValue("r", xValue);
            parser.setVarValue("t", yValue);
        }
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
                temp1.set(i, j, k);
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

    public static boolean removeForbiddenSpawningCoord(final BlockPos pos, final World world)
    {
        if (world == null) return false;
        final Map<BlockPos, ForbiddenEntry> entries = SpawnHandler.forbidReasons.get(world.getDimensionKey());
        if (entries == null) return false;
        return entries.remove(pos) != null;
    }

    public JEP parser = new JEP();

    public SpawnHandler()
    {
        if (PokecubeCore.getConfig().pokemonSpawn) MinecraftForge.EVENT_BUS.register(this);
    }

    public void doMeteor(final ServerWorld world)
    {
        if (!PokecubeCore.getConfig().meteors) return;
        if (!world.getDimensionType().getHasCeiling()) return;
        if (!world.getDimensionType().hasSkyLight()) return;
        final List<ServerPlayerEntity> players = world.getPlayers();
        if (players.size() < 1) return;
        final Random rand = new Random(world.getSeed() + world.getGameTime());
        if (rand.nextInt(100) == 0)
        {
            final Entity player = players.get(rand.nextInt(players.size()));
            final int dx = rand.nextInt(200) - 100;
            final int dz = rand.nextInt(200) - 100;
            final Vector3 v = Vector3.getNewVector();
            final Vector3 v1 = Vector3.getNewVector();
            v.set(player).add(dx, 0, dz);
            final Vector4 loc = new Vector4(player);
            loc.x += dx;
            loc.z += dz;
            if (!TerrainManager.isAreaLoaded(world, v, 0)) return;
            // This getHeight can block if the above check doesn't work out!
            loc.y = world.getHeight(Type.WORLD_SURFACE, (int) loc.x, (int) loc.z);
            final GlobalPos pos = GlobalPos.getPosition(world.getDimensionKey(), new BlockPos(loc.x, loc.y, loc.z));
            if (PokecubeSerializer.getInstance().canMeteorLand(pos, world))
            {
                final Vector3 direction = v1.set(rand.nextGaussian() / 2, -1, rand.nextGaussian() / 2);
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

    public int doSpawnForLocation(final ServerWorld world, final Vector3 v)
    {
        int ret = 0;
        int num = 0;
        if (!SpawnHandler.checkNoSpawnerInArea(world, v.intX(), v.intY(), v.intZ())) return ret;
        if (v.y <= 0 || v.y >= world.getDimensionType().getLogicalHeight()) return ret;
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
            final Vector3 debug = Vector3.getNewVector().set(v.getPos());
            final String toLog = "location: %1$s took: %2$s\u00B5s to find a valid spawn and location";
            PokecubeCore.LOGGER.info(String.format(toLog, debug.getPos(), dt));
        }
        time = System.nanoTime();
        ret += num = this.doSpawnForType(world, v, dbe, this.parser, t);
        dt = (System.nanoTime() - time) / 10e3D;
        if (PokecubeMod.debug && dt > 500)
        {
            final Vector3 debug = Vector3.getNewVector().set(v.getPos());
            final String toLog = "location: %1$s took: %2$s\u00B5s to find a valid spawn for %3$s %4$s";
            PokecubeCore.LOGGER.info(String.format(toLog, debug.getPos(), dt, num, dbe));
        }
        return ret;
    }

    /**
     * Attempts to spawn mobs near the player.
     *
     * @param player
     * @param world
     * @param maxSpawnRadius
     * @return number of mobs spawned.
     */
    public void doSpawnForPlayer(final PlayerEntity player, final ServerWorld world, final int minRadius,
            final int maxRadius)
    {
        final Vector3 v = Vector3.getNewVector();
        v.set(player);
        this.doSpawnForPoint(v, world, minRadius, maxRadius);
        return;
    }

    /**
     * Attempts to spawn mobs near the player.
     *
     * @param player
     * @param world
     * @param maxSpawnRadius
     * @return number of mobs spawned.
     */
    public void doSpawnForPoint(final Vector3 v, final ServerWorld world, final int minRadius, final int maxRadius)
    {
        if (minRadius > maxRadius) return;
        long time = System.nanoTime();
        if (!TerrainManager.isAreaLoaded(world, v, maxRadius)) return;
        final int height = world.getHeight();
        // This lookup box uses configs rather than the passed in radius.
        final int boxR = PokecubeCore.getConfig().maxSpawnRadius;
        final AxisAlignedBB box = v.getAABB().grow(boxR, Math.max(height, boxR), boxR);
        int num = PokemobTracker.countPokemobs(world, box);
        if (num > SpawnHandler.MAX_DENSITY * SpawnHandler.MAXNUM) return;
        final Vector3 v1 = SpawnHandler.getRandomPointNear(world, v, maxRadius);
        double dt = (System.nanoTime() - time) / 1e3D;
        if (PokecubeMod.debug && dt > 100) PokecubeCore.LOGGER.debug("Location Find took " + dt);
        if (v1 == null) return;
        if (v.distanceTo(v1) < minRadius) return;
        time = System.nanoTime();
        num = this.doSpawnForLocation(world, v1);
        dt = (System.nanoTime() - time) / 1e3D;
        if (PokecubeMod.debug && dt > 100) PokecubeCore.LOGGER.debug(dt + "\u00B5" + "s for  " + v + ", spawned "
                + num);
        return;
    }

    private int doSpawnForType(final ServerWorld world, final Vector3 loc, final PokedexEntry dbe, final JEP parser,
            final TerrainSegment t)
    {
        final SpawnData entry = dbe.getSpawnData();

        final Vector3 v = Vector3.getNewVector();
        final Vector3 v2 = Vector3.getNewVector();
        final Vector3 v3 = Vector3.getNewVector();
        int totalSpawnCount = 0;
        final Vector3 point = v2.clear();
        SpawnHandler.refreshTerrain(loc, world, false);
        final SpawnBiomeMatcher matcher = entry.getMatcher(world, loc);
        if (matcher == null) return 0;
        final byte distGroupZone = 4;
        final Random rand = new Random();
        final int n = Math.max(entry.getMax(matcher) - entry.getMin(matcher), 1);
        final int spawnNumber = entry.getMin(matcher) + rand.nextInt(n);

        for (int i = 0; i < spawnNumber; i++)
        {
            final Vector3 dr = SpawnHandler.getRandomPointNear(world, loc, distGroupZone);
            if (dr != null) point.set(dr);
            else point.set(loc);

            if (!SpawnHandler.checkNoSpawnerInArea(world, point.intX(), point.intY(), point.intZ())) continue;

            final float x = (float) point.x;
            final float y = (float) point.y;
            final float z = (float) point.z;

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
                    entity.setLocationAndAngles(x, y, z, world.rand.nextFloat() * 360.0F, 0.0F);
                    if (entity.canSpawn(world, SpawnReason.NATURAL))
                    {
                        if ((entity = SpawnHandler.creatureSpecificInit(entity, world, x, y, z, v3.set(entity), entry,
                                matcher)) != null)
                        {
                            final IPokemob pokemob = CapabilityPokemob.getPokemobFor(entity);
                            if (!event.getSpawnArgs().isEmpty())
                            {
                                final String[] args = event.getSpawnArgs().split(" ");
                                Pokemake.setToArgs(args, pokemob, 0, v, false);
                            }
                            else if (matcher.spawnRule.values.containsKey(SpawnBiomeMatcher.SPAWNCOMMAND))
                            {
                                final String[] args = matcher.spawnRule.values.get(SpawnBiomeMatcher.SPAWNCOMMAND)
                                        .split(" ");
                                Pokemake.setToArgs(args, pokemob, 0, v, false);
                            }
                            final SpawnEvent.Post evt = new SpawnEvent.Post(dbe, v3, world, pokemob);
                            PokecubeCore.POKEMOB_BUS.post(evt);
                            entity.onInitialSpawn(world, world.getDifficultyForLocation(v.getPos()),
                                    SpawnReason.NATURAL, null, null);
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
        return totalSpawnCount;
    }

    private void spawn(final ServerWorld world)
    {
        final List<ServerPlayerEntity> players = world.getPlayers();
        if (players.isEmpty()) return;
        Collections.shuffle(players);
        for (final ServerPlayerEntity player : players)
        {
            if (player.getEntityWorld().getDimensionKey() != world.getDimensionKey()) continue;
            this.doSpawnForPlayer(player, world, PokecubeCore.getConfig().minSpawnRadius, PokecubeCore
                    .getConfig().maxSpawnRadius);
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
