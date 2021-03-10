package pokecube.core.database;

import java.util.Collection;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import javax.xml.namespace.QName;

import com.google.common.base.Predicate;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import net.minecraft.block.material.Material;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.registry.DynamicRegistries;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.Biome.Category;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.chunk.IChunk;
import net.minecraft.world.gen.Heightmap;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.BiomeLoadingEvent;
import pokecube.core.PokecubeCore;
import pokecube.core.database.pokedex.PokedexEntryLoader.SpawnRule;
import pokecube.core.events.pokemob.SpawnCheckEvent;
import pokecube.core.network.packets.PacketPokedex;
import thut.api.maths.Vector3;
import thut.api.terrain.BiomeDatabase;
import thut.api.terrain.BiomeType;
import thut.api.terrain.ITerrainProvider;
import thut.api.terrain.StructureManager;
import thut.api.terrain.StructureManager.StructureInfo;
import thut.api.terrain.TerrainManager;
import thut.api.terrain.TerrainSegment;
import thut.core.common.ThutCore;

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
        public final boolean            day;
        public final boolean            dusk;
        public final boolean            dawn;
        public final boolean            night;
        public final Material           material;
        public final float              light;
        public final RegistryKey<Biome> biome;
        public final BiomeType          type;
        public final Weather            weather;
        public final Category           cat;
        public final boolean            thundering;
        public final IWorld             world;
        public final IChunk             chunk;
        public final Vector3            location;

        public SpawnCheck(final Vector3 location, final IWorld world, final Biome biome)
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
            this.cat = biome.getCategory();
        }

        public SpawnCheck(final Vector3 location, final IWorld world)
        {
            this.world = world;
            this.location = location;
            final Biome biome = location.getBiome(world);
            this.biome = BiomeDatabase.getKey(biome);
            this.cat = biome.getCategory();
            this.material = location.getBlockMaterial(world);
            this.chunk = ITerrainProvider.getChunk(((World) world).getDimensionKey(), new ChunkPos(location.getPos()));
            final TerrainSegment t = TerrainManager.getInstance().getTerrian(world, location);
            final int subBiomeId = t.getBiome(location);
            if (subBiomeId >= 0) this.type = BiomeType.getType(subBiomeId);
            else this.type = BiomeType.NONE;
            // TODO better way to choose current time.
            final double time = ((ServerWorld) world).getDayTime() / 24000.0;
            final int lightBlock = world.getLight(location.getPos());
            this.light = lightBlock / 15f;
            final World w = (ServerWorld) world;
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
                final Set<StructureInfo> set = StructureManager.getFor(((World) checker.world).getDimensionKey(),
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

    public static final QName SPAWNCOMMAND = new QName("command");

    public static final SpawnBiomeMatcher ALLMATCHER;
    public static final SpawnBiomeMatcher NONEMATCHER;

    static
    {
        SpawnRule rule = new SpawnRule();
        rule.values.put(SpawnBiomeMatcher.TYPES, "all");
        ALLMATCHER = new SpawnBiomeMatcher(rule);
        rule = new SpawnRule();
        rule.values.put(SpawnBiomeMatcher.TYPES, "none");
        NONEMATCHER = new SpawnBiomeMatcher(rule);
    }

    private static int                      lastBiomesSize = -1;
    private static List<RegistryKey<Biome>> allBiomeKeys   = Lists.newArrayList();
    private static List<Biome>              allBiomes      = Lists.newArrayList();

    public static Set<RegistryKey<Biome>> SOFTBLACKLIST = Sets.newHashSet();

    private static boolean loadedIn = false;

    public static Collection<RegistryKey<Biome>> getAllBiomeKeys()
    {
        final DynamicRegistries REG = ThutCore.proxy.getRegistries();
        SpawnBiomeMatcher.loadedIn = false;
        // Before loading in.
        if (REG == null) return SpawnBiomeMatcher.allBiomeKeys;
        SpawnBiomeMatcher.loadedIn = true;
        final Collection<Entry<RegistryKey<Biome>, Biome>> biomes = REG.getRegistry(Registry.BIOME_KEY).getEntries();
        if (SpawnBiomeMatcher.lastBiomesSize != biomes.size())
        {
            SpawnBiomeMatcher.allBiomeKeys = Lists.newArrayList();
            SpawnBiomeMatcher.allBiomes = Lists.newArrayList();
            for (final Entry<RegistryKey<Biome>, Biome> b : biomes)
            {
                SpawnBiomeMatcher.allBiomeKeys.add(b.getKey());
                SpawnBiomeMatcher.allBiomes.add(b.getValue());
            }
            SpawnBiomeMatcher.lastBiomesSize = biomes.size();
        }
        return SpawnBiomeMatcher.allBiomeKeys;
    }

    public static Collection<Biome> getAllBiomes()
    {
        // This ensures that the list is populated correctly.
        SpawnBiomeMatcher.getAllBiomeKeys();
        return SpawnBiomeMatcher.allBiomes;
    }

    // These are private so that they can force an update based on categories
    private Set<RegistryKey<Biome>> _validBiomes     = Sets.newHashSet();
    private Set<RegistryKey<Biome>> _blackListBiomes = Sets.newHashSet();

    private Set<Category> _validCats     = Sets.newHashSet();
    private Set<Category> _blackListCats = Sets.newHashSet();

    public Set<BiomeDictionary.Type> _validTypes   = Sets.newHashSet();
    public Set<BiomeDictionary.Type> _invalidTypes = Sets.newHashSet();

    public Set<String>    _validStructures    = Sets.newHashSet();
    public Set<BiomeType> _validSubBiomes     = Sets.newHashSet();
    public Set<BiomeType> _blackListSubBiomes = Sets.newHashSet();

    public Set<Weather> _neededWeather = Sets.newHashSet();
    public Set<Weather> _bannedWeather = Sets.newHashSet();

    // These two sets are used for syncing _validBiomes and _validTypes over to
    // clients on multiplayer.
    public Set<RegistryKey<Biome>> clientBiomes = Sets.newHashSet();

    public Set<String> clientTypes = Sets.newHashSet();

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

    private boolean _checked_cats = false;

    public SpawnBiomeMatcher(final SpawnRule rules)
    {
        this.spawnRule = rules;
        this.parseBasic();
    }

    public boolean validCategory(final Category cat)
    {
        this.parse();
        if (this._blackListCats.contains(cat)) return false;
        if (this._validCats.isEmpty()) return true;
        return this._validCats.contains(cat);
    }

    public Set<RegistryKey<Biome>> getInvalidBiomes()
    {
        // Ensures we are actually loaded in, this is required to set loadedIn
        // true for the below check.
        SpawnBiomeMatcher.getAllBiomeKeys();
        if (!this._checked_cats && SpawnBiomeMatcher.loadedIn)
        {
            this._checked_cats = true;
            for (final Biome b : SpawnBiomeMatcher.getAllBiomes())
            {
                final RegistryKey<Biome> key = BiomeDatabase.getKey(b);
                if (this._blackListCats.contains(b.getCategory())) this._blackListBiomes.add(key);
                for (final BiomeDictionary.Type type : this._invalidTypes)
                    if (BiomeDictionary.hasType(key, type))
                    {
                        this.getInvalidBiomes().add(key);
                        break;
                    }
            }
            for (final Biome b : SpawnBiomeMatcher.getAllBiomes())
            {
                final RegistryKey<Biome> key = BiomeDatabase.getKey(b);
                if (key.getLocation() == null) continue;
                if (this._blackListBiomes.contains(key))
                {
                    this._validBiomes.remove(key);
                    continue;
                }
                if (this._validCats.isEmpty() && this._validTypes.isEmpty()) continue;
                if (!this._validCats.isEmpty() && !this._validCats.contains(b.getCategory())) continue;
                if (!this._validTypes.isEmpty())
                {
                    boolean hasAll = true;
                    for (final BiomeDictionary.Type type : this._validTypes)
                        hasAll = hasAll && BiomeDictionary.hasType(key, type);
                    if (!hasAll) continue;
                }
                this._validBiomes.add(key);
            }
        }
        return this._blackListBiomes;
    }

    public Set<RegistryKey<Biome>> getValidBiomes()
    {
        this.getInvalidBiomes();
        return this._validBiomes;
    }

    private RegistryKey<Biome> from(final BiomeLoadingEvent event)
    {
        return RegistryKey.getOrCreateKey(Registry.BIOME_KEY, event.getName());
    }

    public boolean checkLoadEvent(final BiomeLoadingEvent event)
    {
        return this.checkBiome(this.from(event)) && this.validCategory(event.getCategory());
    }

    /**
     * This is a check for just a single biome, it doesn't factor in the other
     * values such as subbiome (unless flagged all), lighting, time, etc.
     *
     * @param biome
     * @return
     */
    public boolean checkBiome(final RegistryKey<Biome> biome)
    {
        this.parse();
        if (!this.valid) return false;
        if (this.getInvalidBiomes().contains(biome)) return false;
        if (this.getValidBiomes().contains(biome)) return true;
        if (SpawnBiomeMatcher.SOFTBLACKLIST.contains(biome)) return false;
        // The check for all subbiomes overrides the check for biomes.
        if (this._validSubBiomes.contains(BiomeType.ALL)) return true;
        // Otherwise, only return true if we have no valid biomes otherwise!
        return this.getValidBiomes().isEmpty();
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

        // Check the blacklist first, if this does match, we leave early.
        final boolean blackListed = this._blackListSubBiomes.contains(type);

        if (blackListed) return false;

        final boolean rightBiome = this.checkBiome(checker.biome) && this.validCategory(checker.cat);

        // If we are not allowed in this biome, return false.
        // This checks if we are blackisted for the biome, or if we need
        // specific biomes and this is not one of them.
        if (!rightBiome) return false;

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

        //@formatter:off
        // If the type is "none", or we don't have any valid biomes or subbiomes
        // then it means this entry doesn't spawn (ie is supposed to be in a structure)
        // structures were checked earlier, so we return false here.
        final boolean noSpawn = this._validSubBiomes.contains(BiomeType.NONE)
                             || this.getValidBiomes().isEmpty() &&
                                this._validSubBiomes .isEmpty() && this._validCats.isEmpty();
        //@formatter:on
        if (noSpawn) return false;

        // allValid means we spawn everywhere not blacklisted, so return true;
        final boolean allValid = this._validSubBiomes.contains(BiomeType.ALL);
        if (allValid) return true;

        // If there is no subbiome, then the checker's type is null or none
        final boolean noSubbiome = checker.type == null || checker.type == BiomeType.NONE;

        final boolean needsSubbiome = !this._validSubBiomes.isEmpty();

        // We need a subbiome, but there is none here! so no spawn.
        if (needsSubbiome && noSubbiome) return false;

        // We are the correct subbiome if we either don't need one, or the valid
        // subbiomes has out current one.
        final boolean rightSubBiome = noSubbiome || this._validSubBiomes.contains(checker.type);

        // Return true if both correct biome and subbiome.
        return rightSubBiome;
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
        this.parse();
        if (!this.valid) return false;
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

    private void parseBasic()
    {
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
    }

    public void parse()
    {
        if (this.parsed) return;

        this.reset();
        this.parsed = true;
        this.valid = true;

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
        this.parseBasic();

        final String biomeString = this.spawnRule.values.get(SpawnBiomeMatcher.BIOMES);
        final String typeString = this.spawnRule.values.get(SpawnBiomeMatcher.TYPES);
        final String biomeBlacklistString = this.spawnRule.values.get(SpawnBiomeMatcher.BIOMESBLACKLIST);
        final String typeBlacklistString = this.spawnRule.values.get(SpawnBiomeMatcher.TYPESBLACKLIST);
        final String biomeCat = this.spawnRule.values.get(SpawnBiomeMatcher.BIOMECAT);
        final String noBiomeCat = this.spawnRule.values.get(SpawnBiomeMatcher.NOBIOMECAT);
        final String validStructures = this.spawnRule.values.get(SpawnBiomeMatcher.STRUCTURES);

        final Set<Category> biomeCats = this._validCats;
        final Set<Category> noBiomeCats = this._blackListCats;

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
                // Ensure we are a resourcelocation!
                if (!s.contains(":")) s = "minecraft:" + s;
                final RegistryKey<Biome> biome = RegistryKey.getOrCreateKey(Registry.BIOME_KEY, new ResourceLocation(
                        s));
                this._validBiomes.add(biome);
            }
        }
        boolean hasForgeTypes = false;
        if (typeString != null)
        {
            final String[] args = typeString.split(",");
            for (String s : args)
            {
                s = Database.trim(s);
                if (BiomeDatabase.isAType(s))
                {
                    hasForgeTypes = true;
                    if (s.equalsIgnoreCase("water"))
                    {
                        this._validTypes.add(BiomeDictionary.Type.getType("river"));
                        this._validTypes.add(BiomeDictionary.Type.getType("ocean"));
                    }
                    else this._validTypes.add(BiomeDictionary.Type.getType(s));
                    continue;
                }
                final BiomeType subBiome = BiomeType.getBiome(s);
                this._validSubBiomes.add(subBiome);
            }
        }
        if (biomeBlacklistString != null)
        {
            final String[] args = biomeBlacklistString.split(",");
            for (String s : args)
            {
                s = s.trim();
                // Ensure we are a resourcelocation!
                if (!s.contains(":")) s = "minecraft:" + s;
                final RegistryKey<Biome> biome = RegistryKey.getOrCreateKey(Registry.BIOME_KEY, new ResourceLocation(
                        s));
                this._blackListBiomes.add(biome);
            }
        }
        if (typeBlacklistString != null)
        {
            final String[] args = typeBlacklistString.split(",");
            for (String s : args)
            {
                s = Database.trim(s);
                if (BiomeDatabase.isAType(s))
                {
                    if (s.equalsIgnoreCase("water"))
                    {
                        this._invalidTypes.add(BiomeDictionary.Type.getType("river"));
                        this._invalidTypes.add(BiomeDictionary.Type.getType("ocean"));
                    }
                    else this._invalidTypes.add(BiomeDictionary.Type.getType(s));
                    continue;
                }
                BiomeType subBiome = null;
                for (final BiomeType b : BiomeType.values())
                    if (Database.trim(b.name).equals(s))
                    {
                        subBiome = b;
                        break;
                    }
                if (subBiome == null) subBiome = BiomeType.getBiome(s);
                if (subBiome != BiomeType.NONE) this._blackListSubBiomes.add(subBiome);
            }
        }

        this._validBiomes.removeAll(this._blackListBiomes);

        // This refeshes the _validBiomes, and validates things.
        this.getValidBiomes();

        // We are not valid if we specified some types, but found no biomes.
        if (hasForgeTypes && this._validBiomes.isEmpty()) this.valid = false;

        //@formatter:off
        final boolean hasSomething = !(
                                   this._validSubBiomes.isEmpty()
                                && this._validBiomes.isEmpty()
                                && this._validStructures.isEmpty()
                                && this._validCats.isEmpty()
                                && this._validTypes.isEmpty()
                                );
        //@formatter:on
        if (!hasSomething) this.valid = false;

        if (!this.valid && SpawnBiomeMatcher.loadedIn) PokecubeCore.LOGGER.error("Invalid Matcher: {}",
                PacketPokedex.gson.toJson(this));
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
        this._checked_cats = false;

        // Somehow these can end up null after the gson parsing, so we need to
        // ensure they are not null here.
        if (this._validTypes == null) this._validTypes = Sets.newHashSet();
        if (this._invalidTypes == null) this._invalidTypes = Sets.newHashSet();
        if (this._validBiomes == null) this._validBiomes = Sets.newHashSet();
        if (this._validSubBiomes == null) this._validSubBiomes = Sets.newHashSet();
        if (this._blackListBiomes == null) this._blackListBiomes = Sets.newHashSet();
        if (this._blackListSubBiomes == null) this._blackListSubBiomes = Sets.newHashSet();
        if (this._validStructures == null) this._validStructures = Sets.newHashSet();
        if (this._bannedWeather == null) this._bannedWeather = Sets.newHashSet();
        if (this._neededWeather == null) this._neededWeather = Sets.newHashSet();
        if (this._validCats == null) this._validCats = Sets.newHashSet();
        if (this._blackListCats == null) this._blackListCats = Sets.newHashSet();
        if (this.children == null) this.children = Sets.newHashSet();

        // Now lets ensure they are empty.
        this._validCats.clear();
        this._blackListCats.clear();
        this._validTypes.clear();
        this._invalidTypes.clear();
        this._validBiomes.clear();
        this._validSubBiomes.clear();
        this._blackListBiomes.clear();
        this._blackListSubBiomes.clear();
        this._validStructures.clear();
        this._bannedWeather.clear();
        this._neededWeather.clear();
        this.children.clear();
    }
}
