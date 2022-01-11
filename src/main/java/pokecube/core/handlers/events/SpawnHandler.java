package pokecube.core.handlers.events;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;

import javax.annotation.Nullable;

import org.nfunk.jep.JEP;

import com.google.common.base.Predicate;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnPlacements.Type;
import net.minecraft.world.level.BaseSpawner;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.NaturalSpawner;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.Heightmap.Types;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.ForgeEventFactory;
import pokecube.core.PokecubeCore;
import pokecube.core.commands.Pokemake;
import pokecube.core.database.Database;
import pokecube.core.database.PokedexEntry;
import pokecube.core.database.PokedexEntry.SpawnData;
import pokecube.core.database.spawns.SpawnBiomeMatcher;
import pokecube.core.database.spawns.SpawnCheck;
import pokecube.core.events.MeteorEvent;
import pokecube.core.events.pokemob.SpawnEvent;
import pokecube.core.events.pokemob.SpawnEvent.Function;
import pokecube.core.events.pokemob.SpawnEvent.SpawnContext;
import pokecube.core.events.pokemob.SpawnEvent.Variance;
import pokecube.core.handlers.Config;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.PokecubeMod;
import pokecube.core.interfaces.capabilities.CapabilityPokemob;
import pokecube.core.utils.ChunkCoordinate;
import pokecube.core.utils.PokecubeSerializer;
import pokecube.core.utils.PokemobTracker;
import pokecube.core.utils.Tools;
import thut.api.boom.ExplosionCustom;
import thut.api.boom.ExplosionCustom.BlockBreaker;
import thut.api.maths.Vector3;
import thut.api.maths.Vector4;
import thut.api.terrain.BiomeType;
import thut.api.terrain.TerrainManager;
import thut.api.terrain.TerrainSegment;
import thut.api.util.JsonUtil;
import thut.core.common.ThutCore;

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
        public final int range;
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
        private final AABB box;

        private final BlockPos mid;

        public AABBRegion(final AABB box)
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

    private static Object2ObjectOpenHashMap<ResourceKey<Level>, Function> functions = new Object2ObjectOpenHashMap<>();
    public static final Object2ObjectOpenHashMap<ResourceKey<Level>, JEP> parsers = new Object2ObjectOpenHashMap<>();

    public static Variance DEFAULT_VARIANCE = new Variance();

    private static final Map<ResourceKey<Level>, Map<BlockPos, ForbiddenEntry>> forbidReasons = new HashMap<>();

    public static HashMap<BiomeType, Variance> subBiomeLevels = new HashMap<>();

    public static boolean onlySubbiomes = false;
    public static boolean refreshSubbiomes = false;

    public static HashSet<ResourceKey<Level>> dimensionBlacklist = Sets.newHashSet();
    public static HashSet<ResourceKey<Level>> dimensionWhitelist = Sets.newHashSet();

    public static Predicate<BiomeType> biomeToRefresh = input -> {
        if (SpawnHandler.refreshSubbiomes) return true;
        return !input.shouldSave();
    };

    public static double MAX_DENSITY = 1;
    public static int MAXNUM = 10;
    public static boolean lvlCap = false;
    public static int capLevel = 50;

    public static boolean addForbiddenSpawningCoord(final BlockPos pos, final Level dim, final int range,
            final ForbidReason reason)
    {
        Map<BlockPos, ForbiddenEntry> entries = SpawnHandler.forbidReasons.get(dim.dimension());
        if (entries == null) SpawnHandler.forbidReasons.put(dim.dimension(), entries = Maps.newHashMap());
        if (entries.containsKey(pos)) return false;
        entries.put(pos, new ForbiddenEntry(range, reason, pos));
        return true;
    }

    public static boolean addForbiddenSpawningCoord(final Level dim, final ForbidRegion region,
            final ForbidReason reason)
    {
        Map<BlockPos, ForbiddenEntry> entries = SpawnHandler.forbidReasons.get(dim.dimension());
        if (entries == null) SpawnHandler.forbidReasons.put(dim.dimension(), entries = Maps.newHashMap());
        if (entries.containsKey(region.getPos())) return false;
        entries.put(region.getPos(), new ForbiddenEntry(reason, region));
        return true;
    }

    public static boolean canPokemonSpawnHere(SpawnContext context)
    {
        if (!SpawnHandler.canSpawn(context.entry().getSpawnData(), context, true)) return false;
        final EntityType<?> entityTypeIn = context.entry().getEntityType();
        if (entityTypeIn == null) return false;
        if (NaturalSpawner.canSpawnAtBody(Type.ON_GROUND, context.level(), context.location().getPos(), entityTypeIn))
            return true;
        if (context.entry().swims() && NaturalSpawner.canSpawnAtBody(Type.IN_WATER, context.level(),
                context.location().getPos(), entityTypeIn))
            return true;
        return false;
    }

    public static boolean canSpawn(final SpawnData data, final SpawnContext context, final boolean respectDensity)
    {
        if (data == null) return false;
        if (respectDensity)
        {
            final int count = PokemobTracker.countPokemobs(context.level(), context.location(),
                    PokecubeCore.getConfig().maxSpawnRadius);
            if (count > PokecubeCore.getConfig().mobSpawnNumber * PokecubeCore.getConfig().mobDensityMultiplier)
                return false;
        }
        SpawnCheck check = new SpawnCheck(context.location(), context.level());
        return data.isValid(context, check);
    }

    public static boolean canSpawnInWorld(final Level world, final boolean respectDifficulty)
    {
        if (world == null || !(world instanceof ServerLevel level)) return true;
        if (respectDifficulty && world.getDifficulty() == Difficulty.PEACEFUL) return false;
        if (!Config.Rules.doSpawn(level)) return false;
        if (SpawnHandler.dimensionBlacklist.contains(level.dimension())) return false;
        if (PokecubeCore.getConfig().spawnWhitelisted && !SpawnHandler.dimensionWhitelist.contains(level.dimension()))
            return false;
        return true;
    }

    public static boolean canSpawnInWorld(final Level world)
    {
        return SpawnHandler.canSpawnInWorld(world, true);
    }

    public static boolean checkNoSpawnerInArea(final Level world, final int x, final int y, final int z)
    {
        final ForbidReason reason = SpawnHandler.getNoSpawnReason(world, x, y, z);
        return reason == ForbidReason.NONE;
    }

    public static void clear()
    {
        SpawnHandler.forbidReasons.clear();
    }

    public static Mob creatureSpecificInit(final Mob MobEntity, final Level world, final double posX, final double posY,
            final double posZ, final Vector3 spawnPoint, final SpawnData entry, final SpawnBiomeMatcher matcher)
    {
        final BaseSpawner spawner = new BaseSpawner()
        {
            @Override
            public void broadcastEvent(final Level world, final BlockPos pos, final int i)
            {
                // TODO Auto-generated method stub

            }
        };
        if (ForgeEventFactory.doSpecialSpawn(MobEntity, world, (float) posX, (float) posY, (float) posZ, spawner,
                MobSpawnType.NATURAL))
            return null;
        IPokemob pokemob = CapabilityPokemob.getPokemobFor(MobEntity);
        if (pokemob != null)
        {
            pokemob = pokemob.spawnInit(matcher.spawnRule);
            return pokemob.getEntity();
        }
        return null;
    }

    public static ForbiddenEntry getForbiddenEntry(final Level world, final int x, final int y, final int z)
    {
        final Map<BlockPos, ForbiddenEntry> entries = SpawnHandler.forbidReasons.get(world.dimension());
        if (entries == null) return null;
        final BlockPos here = new BlockPos(x, y, z);
        for (final ForbiddenEntry entry : entries.values()) if (entry.region.isInside(here)) return entry;
        return null;
    }

    public static List<ForbiddenEntry> getForbiddenEntries(final Level world, final BlockPos pos)
    {
        final List<ForbiddenEntry> ret = Lists.newArrayList();
        final Map<BlockPos, ForbiddenEntry> entries = SpawnHandler.forbidReasons.get(world.dimension());
        if (entries == null) return ret;
        for (final ForbiddenEntry entry : entries.values()) if (entry.region.isInside(pos)) ret.add(entry);
        return ret;
    }

    public static ForbidReason getNoSpawnReason(final Level world, final BlockPos pos)
    {
        return SpawnHandler.getNoSpawnReason(world, pos.getX(), pos.getY(), pos.getZ());
    }

    public static ForbidReason getNoSpawnReason(final Level world, final int x, final int y, final int z)
    {
        final ForbiddenEntry entry = SpawnHandler.getForbiddenEntry(world, x, y, z);
        return entry == null ? ForbidReason.NONE : entry.reason;
    }

    private static BlockPos getRandomHeight(final Level worldIn, final LevelChunk chunk, final int yCenter,
            final int dy)
    {
        final ChunkPos chunkpos = chunk.getPos();
        final int x = chunkpos.getMinBlockX() + worldIn.random.nextInt(16);
        final int z = chunkpos.getMinBlockZ() + worldIn.random.nextInt(16);
        int y = yCenter - dy + worldIn.random.nextInt(2 * dy + 1);
        final int top = chunk.getHeight(Heightmap.Types.WORLD_SURFACE, x, z) + 1;
        if (y > top) y = top;
        return new BlockPos(x, y, z);
    }

    public static Vector3 getRandomPointNear(final ServerLevel world, final Vector3 pos, final int range)
    {
        // Lets try a few times
        int n = 100;
        while (n-- > 0)
        {
            int dx = world.getRandom().nextInt(range);
            int dz = world.getRandom().nextInt(range);
            final int dy = world.getRandom().nextInt(10);
            dx *= world.getRandom().nextBoolean() ? 1 : -1;
            dz *= world.getRandom().nextBoolean() ? 1 : -1;
            final Vector3 vec = pos.add(dx, 0, dz);
            final ChunkAccess chunk = world.getChunk(vec.getPos());
            if (!(chunk instanceof LevelChunk)) continue;
            final BlockPos blockpos = SpawnHandler.getRandomHeight(world, (LevelChunk) chunk, vec.intY(), dy);
            final int j = blockpos.getX();
            final int k = blockpos.getY();
            final int l = blockpos.getZ();
            vec.set(j + 0.5, k, l + 0.5);
            if (vec.distanceTo(pos) > range) continue;
            final BlockState blockstate = world.getBlockState(blockpos);
            if (blockstate.isRedstoneConductor(world, blockpos)) continue;
            final VoxelShape shape = blockstate.getCollisionShape(world, blockpos);
            if (!shape.isEmpty()) vec.y += shape.max(Axis.Y);
            return vec;
        }
        return null;

    }

    public static Vector3 getRandomPointNear(final Entity player, final int range)
    {
        if (player == null || !(player.getLevel() instanceof ServerLevel)) return null;
        return SpawnHandler.getRandomPointNear((ServerLevel) player.getLevel(),
                Vector3.getNewVector().set(player), range);
    }

    public static SpawnContext getSpawnForLoc(SpawnContext context)
    {
        SpawnEvent.Pick event = new SpawnEvent.Pick.Pre(context);
        PokecubeCore.POKEMOB_BUS.post(event);
        PokedexEntry dbe = event.getPicked();
        if (dbe == null || dbe == Database.missingno) return null;
        context = new SpawnContext(context, dbe);
        event = new SpawnEvent.Pick.Post(context);
        PokecubeCore.POKEMOB_BUS.post(event);
        dbe = event.getPicked();
        if (event.getLocation() == null) context.location().set(0, Double.NaN);
        else context.location().set(event.getLocation());
        return context;
    }

    public static int getSpawnLevel(SpawnContext context)
    {
        return SpawnHandler.getSpawnLevel(context, SpawnHandler.DEFAULT_VARIANCE, -1);
    }

    public static int getSpawnLevel(SpawnContext context, Variance variance, final int baseLevel)
    {
        int spawnLevel = baseLevel;

        final TerrainSegment t = TerrainManager.getInstance().getTerrian(context.level(), context.location());
        final BiomeType type = t.getBiome(context.location());
        if (variance == null)
            if (SpawnHandler.subBiomeLevels.containsKey(type)) variance = SpawnHandler.subBiomeLevels.get(type);
            else variance = SpawnHandler.DEFAULT_VARIANCE;
        if (spawnLevel == -1) if (SpawnHandler.subBiomeLevels.containsKey(type))
        {
            variance = SpawnHandler.subBiomeLevels.get(type);
            spawnLevel = variance.apply(baseLevel);
        }

        if (spawnLevel == baseLevel)
        {
            spawnLevel = SpawnHandler.parse(context.level(), context.location());
            variance = variance == null ? SpawnHandler.DEFAULT_VARIANCE : variance;
            spawnLevel = variance.apply(spawnLevel);
        }
        final SpawnEvent.PickLevel event = new SpawnEvent.PickLevel(context, spawnLevel, variance);
        PokecubeCore.POKEMOB_BUS.post(event);
        return event.getLevel();
    }

    public static Vector3 getSpawnSurface(final Level world, final Vector3 loc, final int range)
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

    public static int getSpawnXp(SpawnContext context)
    {
        return Tools.levelToXp(context.entry().getEvolutionMode(), SpawnHandler.getSpawnLevel(context));
    }

    public static int getSpawnXp(SpawnContext context, final Variance variance, final int baseLevel)
    {
        return Tools.levelToXp(context.entry().getEvolutionMode(),
                SpawnHandler.getSpawnLevel(context, variance, baseLevel));
    }

    public static boolean isPointValidForSpawn(SpawnContext context)
    {
        final int i = context.location().intX();
        final int j = context.location().intY();
        final int k = context.location().intZ();
        if (!SpawnHandler.checkNoSpawnerInArea(context.level(), i, j, k)) return false;
        final boolean validLocation = SpawnHandler.canPokemonSpawnHere(context);
        return validLocation;
    }

    public static void loadFunctionFromString(final String args)
    {
        final Function func = JsonUtil.gson.fromJson(args, Function.class);
        final ResourceKey<Level> dim = ResourceKey.create(Registry.DIMENSION_REGISTRY, new ResourceLocation(func.dim));
        SpawnHandler.functions.put(dim, func);
        SpawnHandler.parsers.put(dim, SpawnHandler.initJEP(new JEP(), func.func, func.radial));
    }

    public static JEP getParser(final ResourceKey<Level> type)
    {
        if (SpawnHandler.functions.isEmpty()) SpawnHandler.initSpawnFunctions();
        return SpawnHandler.parsers.getOrDefault(type, SpawnHandler.parsers.get(Level.OVERWORLD));
    }

    public static Function getFunction(final ResourceKey<Level> dim)
    {
        if (SpawnHandler.functions.isEmpty()) SpawnHandler.initSpawnFunctions();
        return SpawnHandler.functions.getOrDefault(dim, SpawnHandler.functions.get(Level.OVERWORLD));
    }

    public static void initSpawnFunctions()
    {
        for (final String s : PokecubeCore.getConfig().dimensionSpawnLevels) SpawnHandler.loadFunctionFromString(s);
    }

    public static void makeMeteor(final ServerLevel world, final Vector3 location, final float power)
    {
        if (power > 0)
        {
            final ExplosionCustom boom = new ExplosionCustom(world, null, location,
                    (float) (power * PokecubeCore.getConfig().meteorScale))
                            .setMaxRadius(PokecubeCore.getConfig().meteorRadius);

            boom.breaker = new BlockBreaker()
            {
                @Override
                public BlockState applyBreak(ExplosionCustom boom, BlockPos pos, BlockState state, float power,
                        boolean applyBreak, ServerLevel level)
                {
                    BlockState to = BlockBreaker.super.applyBreak(boom, pos, state, power, applyBreak, level);
                    final MeteorEvent event = new MeteorEvent(state, to, pos, power, boom);
                    MinecraftForge.EVENT_BUS.post(event);
                    final TerrainSegment seg = TerrainManager.getInstance().getTerrain(boom.level, pos);
                    seg.setBiome(pos, BiomeType.METEOR);
                    return to;
                }
            };

            final String message = "Meteor at " + location + " with energy of " + power;
            PokecubeCore.LOGGER.debug(message);

            boom.doKineticImpactor(world, Vector3.getNewVector().set(0, -1, 0), location, null, 0.1f, power);

//            boom.doExplosion();
        }
        PokecubeSerializer.getInstance().addMeteorLocation(GlobalPos.of(world.dimension(), location.getPos()));
    }

    private static int parse(final Level world, final Vector3 location)
    {
        if (!(world instanceof ServerLevel level)) return 0;
        // BlockPos p = world.
        final Vector3 spawn = Vector3.getNewVector().set(level.getSharedSpawnPos());
        final ResourceKey<Level> type = world.dimension();
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
            final double t = Mth.atan2(location.x, location.z);
            SpawnHandler.parseExpression(toUse, d, t, r);
        }
        return (int) Math.abs(toUse.getValue());
    }

    public static JEP initJEP(final JEP parser, final String toParse, final boolean radial)
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

    public static void refreshTerrain(final Vector3 location, final Level world)
    {
        SpawnHandler.refreshTerrain(location, world, false);
    }

    public static void refreshTerrain(final Vector3 location, final Level world, final boolean precise)
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
            BiomeType biome = t.getBiome(i, j, k);
            if (SpawnHandler.biomeToRefresh.apply(biome))
            {
                temp1.set(i, j, k);
                biome = t.adjustedCaveBiome(world, temp1);
                if (biome.isNone()) biome = t.adjustedNonCaveBiome(world, temp1);
                t.setBiome(i, j, k, biome);
            }
        }
        else for (int i = x1; i < x1 + 16; i += TerrainSegment.GRIDSIZE)
            for (int j = y1; j < y1 + 16; j += TerrainSegment.GRIDSIZE)
                for (int k = z1; k < z1 + 16; k += TerrainSegment.GRIDSIZE)
        {
            temp1.set(i, j, k);
            BiomeType biome = t.getBiome(i, j, k);
            if (SpawnHandler.biomeToRefresh.apply(biome))
            {
                biome = t.adjustedCaveBiome(world, temp1);
                if (biome.isNone()) biome = t.adjustedNonCaveBiome(world, temp1);
                t.setBiome(i, j, k, biome);
            }
        }
    }

    public static boolean removeForbiddenSpawningCoord(final BlockPos pos, final Level world)
    {
        if (world == null) return false;
        final Map<BlockPos, ForbiddenEntry> entries = SpawnHandler.forbidReasons.get(world.dimension());
        if (entries == null) return false;
        return entries.remove(pos) != null;
    }

    public JEP parser = new JEP();

    public SpawnHandler()
    {
        MinecraftForge.EVENT_BUS.register(this);
    }

    public void doMeteor(final ServerLevel world)
    {
        if (!PokecubeCore.getConfig().meteors) return;
        if (!world.dimensionType().hasCeiling()) return;
        if (!world.dimensionType().hasSkyLight()) return;
        final List<ServerPlayer> players = world.players();
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
            loc.y = world.getHeight(Types.WORLD_SURFACE, (int) loc.x, (int) loc.z);
            final GlobalPos pos = GlobalPos.of(world.dimension(), new BlockPos(loc.x, loc.y, loc.z));
            if (PokecubeSerializer.getInstance().canMeteorLand(pos, world))
            {
                final Vector3 direction = v1.set(rand.nextGaussian() / 2, -1, rand.nextGaussian() / 2);
                v.set(loc.x, loc.y, loc.z);
                final Vector3 location = Vector3.getNextSurfacePoint(world, v, direction, 255);
                if (location != null)
                {
                    if (world.getNearestPlayer(location.x, location.y, location.z, 64,
                            EntitySelector.NO_SPECTATORS) != null)
                        return;
                    final float energy = (float) Math.abs((rand.nextGaussian() + 1) * 50);
                    SpawnHandler.makeMeteor(world, location, energy);
                }
            }
        }
    }

    public int doSpawnForContext(@Nullable SpawnContext context)
    {
        if (context == null) return 0;
        int ret = 0;
        int num = 0;
        Vector3 v = context.location();
        ServerLevel world = context.level();
        if (!SpawnHandler.checkNoSpawnerInArea(world, v.intX(), v.intY(), v.intZ())) return ret;
        if (v.y <= world.getMinBuildHeight() || v.y >= world.dimensionType().logicalHeight()) return ret;
        if (!world.isPositionEntityTicking(v.getPos())) return ret;
        SpawnHandler.refreshTerrain(v, world, true);
        final TerrainSegment t = TerrainManager.getInstance().getTerrian(world, v);
        if (SpawnHandler.onlySubbiomes && t.getBiome(v).isNone()) return ret;
        long time = System.nanoTime();
        context = SpawnHandler.getSpawnForLoc(context);
        if (context == null || context.entry() == Database.missingno) return ret;
        if (v.isNaN()) return ret;

        if (!SpawnHandler.isPointValidForSpawn(context)) return ret;
        double dt = (System.nanoTime() - time) / 10e3D;
        if (PokecubeMod.debug && dt > 500)
        {
            final Vector3 debug = Vector3.getNewVector().set(v.getPos());
            final String toLog = "location: %1$s took: %2$s\u00B5s to find a valid spawn and location";
            PokecubeCore.LOGGER.info(String.format(toLog, debug.getPos(), dt));
        }
        time = System.nanoTime();
        ret += num = this.doSpawnForType(context, this.parser, t);
        dt = (System.nanoTime() - time) / 10e3D;
        if (PokecubeMod.debug && dt > 500)
        {
            final Vector3 debug = Vector3.getNewVector().set(v.getPos());
            final String toLog = "location: %1$s took: %2$s\u00B5s to find a valid spawn for %3$s %4$s";
            PokecubeCore.LOGGER.info(String.format(toLog, debug.getPos(), dt, num, context.entry()));
        }
        return ret;
    }

    @Nullable
    public SpawnContext randomSpawnContext(SpawnContext base, final int minRadius, final int maxRadius)
    {
        Vector3 v = base.location();
        ServerLevel level = base.level();
        long time = System.nanoTime();
        if (!TerrainManager.isAreaLoaded(level, v, maxRadius)) return null;
        final int height = level.getMaxBuildHeight();
        // This lookup box uses configs rather than the passed in radius.
        final int boxR = PokecubeCore.getConfig().maxSpawnRadius;
        final AABB box = v.getAABB().inflate(boxR, Math.max(height, boxR), boxR);
        int num = PokemobTracker.countPokemobs(level, box);
        if (num >= SpawnHandler.MAX_DENSITY * SpawnHandler.MAXNUM) return null;
        final Vector3 v1 = SpawnHandler.getRandomPointNear(level, v, maxRadius);
        double dt = (System.nanoTime() - time) / 1e3D;
        if (PokecubeMod.debug && dt > 100) PokecubeCore.LOGGER.debug("Location Find took " + dt);
        if (v1 == null) return null;
        if (v.distanceTo(v1) < minRadius) return null;
        return new SpawnContext(base, v1);
    }

    /**
     * Attempts to spawn mobs near the player.
     *
     * @param player
     * @param world
     * @param maxSpawnRadius
     * @return number of mobs spawned.
     */
    public void doSpawn(ServerPlayer player, ServerLevel level, final int minRadius, final int maxRadius)
    {
        if (minRadius > maxRadius) return;
        Vector3 v = Vector3.getNewVector().set(player);
        SpawnContext base = new SpawnContext(player, level, Database.missingno, v);
        SpawnContext context = randomSpawnContext(base, minRadius, maxRadius);
        this.doSpawnForContext(context);
        return;
    }

    private int doSpawnForType(SpawnContext context, final JEP parser, final TerrainSegment t)
    {
        final SpawnData entry = context.entry().getSpawnData();

        ServerLevel level = context.level();
        Vector3 loc = context.location();
        PokedexEntry dbe = context.entry();

        final Vector3 v = Vector3.getNewVector();
        final Vector3 v2 = Vector3.getNewVector();
        final Vector3 v3 = Vector3.getNewVector();
        int totalSpawnCount = 0;
        final Vector3 point = v2.clear();
        SpawnHandler.refreshTerrain(loc, level, false);
        final SpawnBiomeMatcher matcher = entry.getMatcher(context);
        if (matcher == null) return 0;
        final byte distGroupZone = 4;
        final Random rand = ThutCore.newRandom();
        final int n = Math.max(entry.getMax(matcher) - entry.getMin(matcher), 1);
        final int spawnNumber = entry.getMin(matcher) + rand.nextInt(n);

        for (int i = 0; i < spawnNumber; i++)
        {
            final Vector3 dr = SpawnHandler.getRandomPointNear(level, loc, distGroupZone);
            if (dr != null) point.set(dr);
            else point.set(loc);

            if (!SpawnHandler.checkNoSpawnerInArea(level, point.intX(), point.intY(), point.intZ())) continue;

            final float x = (float) point.x;
            final float y = (float) point.y;
            final float z = (float) point.z;

            Mob entity = null;
            try
            {
                context = new SpawnContext(context, point);
                final SpawnEvent.Pick.Final event = new SpawnEvent.Pick.Final(context);
                PokecubeCore.POKEMOB_BUS.post(event);
                if (event.getPicked() == null) continue;
                entity = PokecubeCore.createPokemob(event.getPicked(), level);
                entity.setHealth(entity.getMaxHealth());
                entity.moveTo(x, y, z, level.random.nextFloat() * 360.0F, 0.0F);
                if (entity.checkSpawnRules(level, MobSpawnType.NATURAL))
                {
                    if ((entity = SpawnHandler.creatureSpecificInit(entity, level, x, y, z, v3.set(entity), entry,
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
                        final SpawnEvent.Post evt = new SpawnEvent.Post(pokemob);
                        PokecubeCore.POKEMOB_BUS.post(evt);
                        entity.finalizeSpawn(level, level.getCurrentDifficultyAt(v.getPos()), MobSpawnType.NATURAL,
                                null, null);
                        entity = pokemob.onAddedInit().getEntity();
                        level.addFreshEntity(entity);
                        totalSpawnCount++;
                    }
                }
                else entity.discard();
            }
            catch (final Throwable e)
            {
                if (entity != null) entity.discard();
                PokecubeCore.LOGGER.error("Wrong Id while spawn: " + dbe.getName(), e);
                return totalSpawnCount;
            }
        }
        return totalSpawnCount;
    }

    private void spawn(final ServerLevel world)
    {
        final List<ServerPlayer> players = world.players();
        if (players.isEmpty()) return;
        Collections.shuffle(players);
        for (final ServerPlayer player : players)
        {
            if (player.level.dimension() != world.dimension()) continue;
            this.doSpawn(player, world, PokecubeCore.getConfig().minSpawnRadius,
                    PokecubeCore.getConfig().maxSpawnRadius);
        }
    }

    public void tick(final ServerLevel world)
    {
        if (!SpawnHandler.canSpawnInWorld(world)) return;
        if (!Config.Rules.doSpawn(world)) return;
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
