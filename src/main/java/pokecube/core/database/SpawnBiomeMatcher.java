package pokecube.core.database;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import javax.xml.namespace.QName;

import com.google.common.base.Predicate;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import net.minecraft.block.material.Material;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.Biome.Category;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.chunk.IChunk;
import net.minecraft.world.gen.Heightmap;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.registries.ForgeRegistries;
import pokecube.core.database.PokedexEntryLoader.SpawnRule;
import pokecube.core.events.pokemob.SpawnCheckEvent;
import thut.api.maths.Vector3;
import thut.api.terrain.BiomeDatabase;
import thut.api.terrain.BiomeType;
import thut.api.terrain.ITerrainProvider;
import thut.api.terrain.StructureManager;
import thut.api.terrain.StructureManager.StructureInfo;
import thut.api.terrain.TerrainManager;
import thut.api.terrain.TerrainSegment;

public class SpawnBiomeMatcher
{
    public static enum Weather
    {
        SUN, CLOUD, RAIN, SNOW, NONE;

        public static Weather getForWorld(final World world, final Vector3 location)
        {
            final boolean globalRain = world.isRaining();
            final BlockPos position = location.getPos();
            boolean outside = world.canSeeSky(position);
            outside = outside && world.getHeight(Heightmap.Type.MOTION_BLOCKING, position).getY() > position.getY();
            if (!outside) return NONE;
            if (globalRain)
            {
                final Biome.RainType type = world.getBiome(position).getPrecipitation();
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

    public static class SpawnCheck
    {
        public final boolean          day;
        public final boolean          dusk;
        public final boolean          dawn;
        public final boolean          night;
        public final Material         material;
        public final float            light;
        public final ResourceLocation biome;
        public final BiomeType        type;
        public final Weather          weather;
        public final boolean          thundering;
        public final IWorld           world;
        public final IChunk           chunk;
        public final Vector3          location;

        public SpawnCheck(final Vector3 location, final IWorld world, final Biome biome)
        {
            this.world = world;
            this.location = location;
            this.biome = biome.getRegistryName();
            this.day = this.dusk = this.dawn = this.night = true;
            this.weather = Weather.NONE;
            this.thundering = false;
            this.light = 1f;
            this.type = BiomeType.NONE;
            this.material = Material.AIR;
            this.chunk = world.getChunk(location.intX() >> 4, location.intZ() >> 4, ChunkStatus.EMPTY, false);
        }

        public SpawnCheck(final Vector3 location, final IWorld world)
        {
            this.world = world;
            this.location = location;
            this.biome = location.getBiome(world).getRegistryName();
            this.material = location.getBlockMaterial(world);
            this.chunk = ITerrainProvider.getChunk(world.getDimensionKey(), new ChunkPos(location.getPos()));
            final TerrainSegment t = TerrainManager.getInstance().getTerrian(world, location);
            final int subBiomeId = t.getBiome(location);
            if (subBiomeId >= 0) this.type = BiomeType.getType(subBiomeId);
            else this.type = BiomeType.NONE;
            // TODO better way to choose current time.
            final double time = world.getWorld().getDayTime() / 24000;
            final int lightBlock = world.getLight(location.getPos());
            this.light = lightBlock / 15f;
            final World w = world.getWorld();
            this.weather = Weather.getForWorld(w, location);
            this.thundering = this.weather == Weather.RAIN && w.isThundering();
            this.day = PokedexEntry.day.contains(time);
            this.dusk = PokedexEntry.dusk.contains(time);
            this.dawn = PokedexEntry.dawn.contains(time);
            this.night = PokedexEntry.night.contains(time);
        }
    }

    public static enum MatchResult
    {
        PASS, SUCCEED, FAIL;
    }

    public static interface StructureMatcher
    {

        static StructureMatcher or(final StructureMatcher A, final StructureMatcher B)
        {
            return new StructureMatcher()
            {
                @Override
                public MatchResult structuresMatch(final SpawnBiomeMatcher matcher, final SpawnCheck checker)
                {
                    final MatchResult resA = A.structuresMatch(matcher, checker);
                    if (resA == MatchResult.SUCCEED) return resA;
                    final MatchResult resB = B.structuresMatch(matcher, checker);
                    if (resB == MatchResult.SUCCEED) return resB;
                    return resA == MatchResult.FAIL || resB == MatchResult.FAIL ? MatchResult.FAIL : MatchResult.PASS;
                }
            };
        }

        default MatchResult structuresMatch(final SpawnBiomeMatcher matcher, final SpawnCheck checker)
        {
            if (!matcher._validStructures.isEmpty())
            {
                final Set<StructureInfo> set = StructureManager.getFor(checker.world.getDimensionKey(),
                        checker.location.getPos());
                for (final StructureInfo i : set)
                    if (matcher._validStructures.contains(i.name)) return MatchResult.SUCCEED;
                return MatchResult.FAIL;
            }
            return MatchResult.PASS;
        }
    }

    public static final QName ATYPES         = new QName("anyType");
    public static final QName TYPES          = new QName("types");
    public static final QName TYPESBLACKLIST = new QName("typesBlacklist");

    public static final QName STRUCTURES      = new QName("structures");
    public static final QName STRUCTURESBLACK = new QName("noStructures");

    public static final QName NIGHT = new QName("night");
    public static final QName DAY   = new QName("day");
    public static final QName DUSK  = new QName("dusk");
    public static final QName DAWN  = new QName("dawn");

    public static final QName AIR      = new QName("air");
    public static final QName WATER    = new QName("water");
    public static final QName MINLIGHT = new QName("minLight");

    public static final QName WEATHER    = new QName("weather");
    public static final QName WEATHERNOT = new QName("noWeather");

    public static final QName BIOMECAT   = new QName("category");
    public static final QName NOBIOMECAT = new QName("categoryBlacklist");

    public static final QName BIOMES          = new QName("biomes");
    public static final QName BIOMESBLACKLIST = new QName("biomesBlacklist");

    public static final QName MAXLIGHT = new QName("maxLight");

    public static final QName             SPAWNCOMMAND = new QName("command");
    public static final SpawnBiomeMatcher ALLMATCHER;

    static
    {
        final SpawnRule rule = new SpawnRule();
        rule.values.put(SpawnBiomeMatcher.TYPES, "all");
        ALLMATCHER = new SpawnBiomeMatcher(rule);
    }

    private static int                               lastBiomesSize = -1;
    private static List<Biome>                       allBiomes      = Lists.newArrayList();

    public static Collection<Biome> getAllBiomes()
    {
        final Collection<Biome> biomes = ForgeRegistries.BIOMES.getValues();
        if (SpawnBiomeMatcher.lastBiomesSize != biomes.size())
        {
            SpawnBiomeMatcher.allBiomes.clear();
            SpawnBiomeMatcher.allBiomes.addAll(biomes);
        }
        return SpawnBiomeMatcher.allBiomes;
    }

    public Set<ResourceLocation> _validBiomes        = Sets.newHashSet();
    public Set<String>           _validStructures    = Sets.newHashSet();
    public Set<BiomeType>        _validSubBiomes     = Sets.newHashSet();
    public Set<ResourceLocation> _blackListBiomes    = Sets.newHashSet();
    public Set<BiomeType>        _blackListSubBiomes = Sets.newHashSet();

    public Set<Weather> _neededWeather = Sets.newHashSet();
    public Set<Weather> _bannedWeather = Sets.newHashSet();

    /**
     * If the spawnRule has an anyType key, make a child for each type in it,
     * then check if any of the children are valid.
     */
    public Set<SpawnBiomeMatcher> children = Sets.newHashSet();

    public Set<Predicate<SpawnCheck>> _additionalConditions = Sets.newHashSet();

    public StructureMatcher _structs = new StructureMatcher()
    {
    };

    public float minLight = 0;
    public float maxLight = 1;

    public boolean day   = true;
    public boolean dusk  = true;
    public boolean night = true;
    public boolean dawn  = true;
    public boolean air   = true;
    public boolean water = false;

    public boolean needThunder = false;
    public boolean noThunder   = false;

    public final SpawnRule spawnRule;

    boolean parsed = false;
    boolean valid  = true;

    public SpawnBiomeMatcher(final SpawnRule rules)
    {
        this.spawnRule = rules;
    }

    /**
     * This is a check for just a single biome, it doesn't factor in the other
     * values such as subbiome (unless flagged all), lighting, time, etc.
     *
     * @param biome
     * @return
     */
    public boolean checkBiome(final Biome biome)
    {
        this.parse();
        if (!this.valid) return false;
        if (this._validSubBiomes.contains(BiomeType.ALL)) return true;
        if (this._validSubBiomes.contains(BiomeType.NONE) || this._validBiomes.isEmpty() && this._blackListBiomes
                .isEmpty()) return false;
        if (this._blackListBiomes.contains(biome.getRegistryName())) return false;
        return this._validBiomes.contains(biome.getRegistryName());
    }

    private boolean weatherMatches(final SpawnCheck checker)
    {
        if (this.needThunder && !checker.thundering) return false;
        if (this.noThunder && checker.thundering) return false;
        if (this._bannedWeather.contains(checker.weather)) return false;
        if (this._neededWeather.isEmpty()) return true;
        return this._neededWeather.contains(checker.weather);
    }

    private boolean biomeMatches(final SpawnCheck checker)
    {
        this.parse();
        if (!this.valid) return false;
        // This takes priority, regardless of the other options.
        BiomeType type = checker.type;
        if (checker.type == null) type = BiomeType.ALL;
        final boolean blackListed = this._blackListBiomes.contains(checker.biome) || this._blackListSubBiomes.contains(
                type);
        if (blackListed) return false;
        final IChunk chunk = checker.chunk;
        // No chunk here, no spawn here!
        if (chunk == null) return false;

        if (!this._validStructures.isEmpty())
        {
            // Check structures first, then check biomes, the spawn rule would
            // use
            // blacklisted types to prevent this being in the wrong spot anyway
            final MatchResult result = this._structs.structuresMatch(this, checker);
            if (result != MatchResult.PASS) return result == MatchResult.SUCCEED;
        }

        if (this._validSubBiomes.contains(BiomeType.NONE) || this._validBiomes.isEmpty() && this._validSubBiomes
                .isEmpty() && this._blackListSubBiomes.isEmpty() && this._blackListBiomes.isEmpty()) return false;
        final boolean noSubbiome = checker.type == null || checker.type == BiomeType.NONE;
        final boolean rightBiome = this._validSubBiomes.contains(BiomeType.ALL) || this._validBiomes.contains(
                checker.biome) || this._validBiomes.isEmpty();
        boolean rightSubBiome = this._validSubBiomes.isEmpty() && noSubbiome || this._validSubBiomes.contains(
                BiomeType.ALL) || this._validSubBiomes.contains(checker.type);
        if (this._validBiomes.isEmpty() && this._validSubBiomes.isEmpty()) rightSubBiome = true;
        return rightBiome && rightSubBiome;
    }

    private boolean conditionsMatch(final SpawnCheck checker)
    {
        if (checker.day && !this.day) return false;
        if (checker.night && !this.night) return false;
        if (checker.dusk && !this.dusk) return false;
        if (checker.dawn && !this.dawn) return false;
        final Material m = checker.material;
        final boolean isWater = m == Material.WATER;
        if (isWater && !this.water) return false;
        if (m.isLiquid() && !isWater) return false;
        if (!this.air && !isWater) return false;
        final float light = checker.light;
        return light <= this.maxLight && light >= this.minLight;
    }

    public boolean matches(final SpawnCheck checker)
    {
        if (!this.children.isEmpty())
        {
            for (final SpawnBiomeMatcher child : this.children)
                if (child.matches(checker)) return true;
            return false;
        }
        if (!this.weatherMatches(checker)) return false;
        final boolean biome = this.biomeMatches(checker);
        if (!biome) return false;
        final boolean loc = this.conditionsMatch(checker);
        if (!loc) return false;
        boolean subCondition = true;
        for (final Predicate<SpawnCheck> c : this._additionalConditions)
            subCondition &= c.apply(checker);
        return subCondition && !MinecraftForge.EVENT_BUS.post(new SpawnCheckEvent.Check(this, checker));
    }

    private Category getCat(final String name)
    {
        for (final Category c : Category.values())
            if (c.getName().equalsIgnoreCase(name)) return c;
        return null;
    }

    private Weather getWeather(final String name)
    {
        for (final Weather c : Weather.values())
            if (c.name().equalsIgnoreCase(name)) return c;
        return null;
    }

    public void parse()
    {
        if (this.parsed) return;
        this.parsed = true;

        if (this._validBiomes == null) this._validBiomes = Sets.newHashSet();
        if (this._validSubBiomes == null) this._validSubBiomes = Sets.newHashSet();
        if (this._blackListBiomes == null) this._blackListBiomes = Sets.newHashSet();
        if (this._blackListSubBiomes == null) this._blackListSubBiomes = Sets.newHashSet();
        if (this._validStructures == null) this._validStructures = Sets.newHashSet();
        if (this._bannedWeather == null) this._bannedWeather = Sets.newHashSet();
        if (this._neededWeather == null) this._neededWeather = Sets.newHashSet();
        if (this.children == null) this.children = Sets.newHashSet();

        this._validBiomes.clear();
        this._validSubBiomes.clear();
        this._blackListBiomes.clear();
        this._blackListSubBiomes.clear();
        this._validStructures.clear();
        this._bannedWeather.clear();
        this._neededWeather.clear();
        this.children.clear();

        if (this.spawnRule.values.containsKey(SpawnBiomeMatcher.ATYPES))
        {
            final String typeString = this.spawnRule.values.get(SpawnBiomeMatcher.ATYPES);
            final String[] args = typeString.split(",");
            for (String s : args)
            {
                s = s.trim();
                final SpawnRule newRule = new SpawnRule();
                newRule.values.putAll(this.spawnRule.values);
                newRule.values.remove(SpawnBiomeMatcher.ATYPES);
                newRule.values.put(SpawnBiomeMatcher.TYPES, s);
                this.children.add(new SpawnBiomeMatcher(newRule));
            }
        }
        this._structs = new StructureMatcher()
        {
        };
        MinecraftForge.EVENT_BUS.post(new SpawnCheckEvent.Init(this));

        if (!this.children.isEmpty())
        {
            for (final SpawnBiomeMatcher child : this.children)
                child.parse();
            return;
        }

        // Lets deal with the weather checks
        String weather = this.spawnRule.values.get(SpawnBiomeMatcher.WEATHER);
        if (weather != null)
        {
            final String[] args = weather.split(",");
            for (final String s : args)
            {
                if (s.equalsIgnoreCase("thunder"))
                {
                    this.needThunder = true;
                    continue;
                }
                final Weather w = this.getWeather(s);
                if (w != null) this._neededWeather.add(w);
            }
        }
        weather = this.spawnRule.values.get(SpawnBiomeMatcher.WEATHERNOT);
        if (weather != null)
        {
            final String[] args = weather.split(",");
            for (final String s : args)
            {
                if (s.equalsIgnoreCase("thunder"))
                {
                    this.noThunder = true;
                    continue;
                }
                final Weather w = this.getWeather(s);
                if (w != null) this._bannedWeather.add(w);
            }
        }

        this.preParseSubBiomes();
        final String biomeString = this.spawnRule.values.get(SpawnBiomeMatcher.BIOMES);
        final String typeString = this.spawnRule.values.get(SpawnBiomeMatcher.TYPES);
        final String biomeBlacklistString = this.spawnRule.values.get(SpawnBiomeMatcher.BIOMESBLACKLIST);
        final String typeBlacklistString = this.spawnRule.values.get(SpawnBiomeMatcher.TYPESBLACKLIST);
        final String biomeCat = this.spawnRule.values.get(SpawnBiomeMatcher.BIOMECAT);
        final String noBiomeCat = this.spawnRule.values.get(SpawnBiomeMatcher.NOBIOMECAT);
        final String validStructures = this.spawnRule.values.get(SpawnBiomeMatcher.STRUCTURES);
        if (this.spawnRule.values.containsKey(SpawnBiomeMatcher.DAY)) this.day = Boolean.parseBoolean(
                this.spawnRule.values.get(SpawnBiomeMatcher.DAY));
        if (this.spawnRule.values.containsKey(SpawnBiomeMatcher.NIGHT)) this.night = Boolean.parseBoolean(
                this.spawnRule.values.get(SpawnBiomeMatcher.NIGHT));
        if (this.spawnRule.values.containsKey(SpawnBiomeMatcher.DUSK)) this.dusk = Boolean.parseBoolean(
                this.spawnRule.values.get(SpawnBiomeMatcher.DUSK));
        if (this.spawnRule.values.containsKey(SpawnBiomeMatcher.DAWN)) this.dawn = Boolean.parseBoolean(
                this.spawnRule.values.get(SpawnBiomeMatcher.DAWN));
        if (this.spawnRule.values.containsKey(SpawnBiomeMatcher.WATER)) this.water = Boolean.parseBoolean(
                this.spawnRule.values.get(SpawnBiomeMatcher.WATER));
        if (this.spawnRule.values.containsKey(SpawnBiomeMatcher.AIR))
        {
            this.air = Boolean.parseBoolean(this.spawnRule.values.get(SpawnBiomeMatcher.AIR));
            if (!this.air && !this.water) this.water = true;
        }
        if (this.spawnRule.values.containsKey(SpawnBiomeMatcher.MINLIGHT)) this.minLight = Float.parseFloat(
                this.spawnRule.values.get(SpawnBiomeMatcher.MINLIGHT));
        if (this.spawnRule.values.containsKey(SpawnBiomeMatcher.MAXLIGHT)) this.maxLight = Float.parseFloat(
                this.spawnRule.values.get(SpawnBiomeMatcher.MAXLIGHT));

        final List<Category> biomeCats = Lists.newArrayList();
        final List<Category> noBiomeCats = Lists.newArrayList();
        final Set<String> blackListTypes = Sets.newHashSet();
        final Set<String> validTypes = Sets.newHashSet();

        if (biomeCat != null)
        {
            final String[] args = biomeCat.split(",");
            for (final String s : args)
            {
                final Category c = this.getCat(s);
                if (c != null) biomeCats.add(c);
            }

        }
        if (noBiomeCat != null)
        {
            final String[] args = noBiomeCat.split(",");
            for (final String s : args)
            {
                final Category c = this.getCat(s);
                if (c != null) noBiomeCats.add(c);
            }
        }

        if (validStructures != null)
        {
            final String[] args = validStructures.split(",");
            for (final String s : args)
                this._validStructures.add(s);
        }

        if (biomeString != null)
        {
            final String[] args = biomeString.split(",");
            for (String s : args)
            {
                s = s.trim();
                Biome biome = null;
                for (final Biome b : SpawnBiomeMatcher.getAllBiomes())
                {
                    if (b.getRegistryName().toString().equals(s))
                    {
                        biome = b;
                        break;
                    }
                    if (b != null) if (Database.trim(BiomeDatabase.getBiomeName(b)).equals(Database.trim(s)))
                    {
                        biome = b;
                        break;
                    }
                }
                if (biome != null) this._validBiomes.add(biome.getRegistryName());
            }
        }
        boolean hasForgeTypes = false;
        if (typeString != null)
        {
            final String[] args = typeString.split(",");
            for (String s : args)
            {
                s = s.trim();
                if (BiomeDatabase.isAType(s))
                {
                    hasForgeTypes = true;
                    if (s.equalsIgnoreCase("water"))
                    {
                        validTypes.add("river");
                        validTypes.add("ocean");
                    }
                    else validTypes.add(s);
                    continue;
                }
                final BiomeType subBiome = BiomeType.getBiome(s.trim(), false);
                this._validSubBiomes.add(subBiome);
            }
        }
        if (biomeBlacklistString != null)
        {
            final String[] args = biomeBlacklistString.split(",");
            for (String s : args)
            {
                s = s.trim();
                Biome biome = null;
                for (final Biome b : SpawnBiomeMatcher.getAllBiomes())
                {
                    if (b.getRegistryName().toString().equals(s))
                    {
                        biome = b;
                        break;
                    }
                    if (b != null) if (Database.trim(BiomeDatabase.getBiomeName(b)).equals(Database.trim(s)))
                    {
                        biome = b;
                        break;
                    }
                }
                if (biome != null) this._blackListBiomes.add(biome.getRegistryName());
            }
        }
        if (typeBlacklistString != null)
        {
            final String[] args = typeBlacklistString.split(",");
            for (String s : args)
            {
                s = s.trim();
                if (BiomeDatabase.isAType(s))
                {
                    hasForgeTypes = true;
                    if (s.equalsIgnoreCase("water"))
                    {
                        blackListTypes.add("river");
                        blackListTypes.add("ocean");
                    }
                    else blackListTypes.add(s);
                    continue;
                }
                BiomeType subBiome = null;
                for (final BiomeType b : BiomeType.values())
                    if (Database.trim(b.name).equals(Database.trim(s)))
                    {
                        subBiome = b;
                        break;
                    }
                if (subBiome != BiomeType.NONE) this._blackListSubBiomes.add(subBiome);
            }
        }
        for (final Biome b : SpawnBiomeMatcher.getAllBiomes())
            if (b != null && !this._blackListBiomes.contains(b.getRegistryName()))
            {
                boolean matches = biomeCats.contains(b.getCategory());
                if (!matches) for (final String type : validTypes)
                {
                    matches = BiomeDatabase.contains(b, type);
                    if (matches) break;
                }
                if (matches) this._validBiomes.add(b.getRegistryName());
            }

        final Set<ResourceLocation> toRemove = Sets.newHashSet();
        for (final Biome b : SpawnBiomeMatcher.getAllBiomes())
            if (b != null && !this._blackListBiomes.contains(b.getRegistryName()))
            {
                boolean matches = noBiomeCats.contains(b.getCategory());
                if (!matches) for (final String type : blackListTypes)
                {
                    matches = matches || BiomeDatabase.contains(b, type);
                    if (matches) break;
                }
                if (matches)
                {
                    toRemove.add(b.getRegistryName());
                    this._blackListBiomes.add(b.getRegistryName());
                }
            }
        this._validBiomes.removeAll(toRemove);

        if (hasForgeTypes && this._validBiomes.isEmpty()) this.valid = false;
        if (this._validSubBiomes.isEmpty() && this._validBiomes.isEmpty() && this._blackListBiomes.isEmpty()
                && this._blackListSubBiomes.isEmpty() && this._validStructures.isEmpty()) this.valid = false;
    }

    private void preParseSubBiomes()
    {
        final String typeString = this.spawnRule.values.get(SpawnBiomeMatcher.TYPES);
        if (typeString != null)
        {
            final String[] args = typeString.split(",");
            for (String s : args)
            {
                s = s.trim();
                BiomeType subBiome = null;
                for (final BiomeType b : BiomeType.values())
                    if (Database.trim(b.name).equals(Database.trim(s)))
                    {
                        subBiome = b;
                        break;
                    }
                if (subBiome == null && !BiomeDatabase.isAType(s)) BiomeType.getBiome(s.trim(), true);
            }
        }
    }

    public void reset()
    {
        this.parsed = false;
        this.valid = true;
        for (final SpawnBiomeMatcher child : this.children)
            child.reset();
    }
}
