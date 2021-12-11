package pokecube.core.database.spawns;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.xml.namespace.QName;

import com.google.common.base.Predicate;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biome.BiomeCategory;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.material.Material;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.BiomeLoadingEvent;
import pokecube.core.PokecubeCore;
import pokecube.core.database.Database;
import pokecube.core.database.pokedex.PokedexEntryLoader.SpawnRule;
import pokecube.core.database.spawns.SpawnCheck.MatchResult;
import pokecube.core.database.spawns.SpawnCheck.Weather;
import pokecube.core.events.pokemob.SpawnCheckEvent;
import pokecube.core.network.packets.PacketPokedex;
import thut.api.terrain.BiomeDatabase;
import thut.api.terrain.BiomeType;
import thut.api.terrain.StructureManager;
import thut.api.terrain.StructureManager.StructureInfo;
import thut.core.common.ThutCore;

public class SpawnBiomeMatcher // implements Predicate<SpawnCheck>
{
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
                final Set<StructureInfo> set = StructureManager.getFor(((Level) checker.world).dimension(),
                        checker.location.getPos());
                for (final StructureInfo i : set)
                    if (matcher._validStructures.contains(i.name)) return MatchResult.SUCCEED;
                return MatchResult.FAIL;
            }
            return MatchResult.PASS;
        }
    }

    public static final QName TYPES = new QName("types");
    public static final QName TYPESBLACKLIST = new QName("typesBlacklist");

    public static final QName STRUCTURES = new QName("structures");
    public static final QName STRUCTURESBLACK = new QName("noStructures");

    public static final QName NIGHT = new QName("night");
    public static final QName DAY = new QName("day");
    public static final QName DUSK = new QName("dusk");
    public static final QName DAWN = new QName("dawn");

    public static final QName AIR = new QName("air");
    public static final QName WATER = new QName("water");
    public static final QName MINLIGHT = new QName("minLight");

    public static final QName WEATHER = new QName("weather");
    public static final QName WEATHERNOT = new QName("noWeather");

    public static final QName BIOMECAT = new QName("category");
    public static final QName NOBIOMECAT = new QName("categoryBlacklist");

    public static final QName STRICTTYPECAT = new QName("strict_type_cat");

    public static final QName BIOMES = new QName("biomes");
    public static final QName BIOMESBLACKLIST = new QName("biomesBlacklist");

    public static final QName MAXLIGHT = new QName("maxLight");

    public static final QName SPAWNCOMMAND = new QName("command");

    public static final QName PRESET = new QName("preset");

    public static final QName ANDPRESET = new QName("and_presets");
    public static final QName ORPRESET = new QName("or_presets");

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

        // FIXME remove this once forge does it itself.

        BiomeDictionary.addTypes(Biomes.MEADOW, BiomeDictionary.Type.PLAINS, BiomeDictionary.Type.PLATEAU,
                BiomeDictionary.Type.OVERWORLD);
        BiomeDictionary.addTypes(Biomes.GROVE, BiomeDictionary.Type.COLD, BiomeDictionary.Type.CONIFEROUS,
                BiomeDictionary.Type.FOREST, BiomeDictionary.Type.SNOWY, BiomeDictionary.Type.MOUNTAIN,
                BiomeDictionary.Type.OVERWORLD);
        BiomeDictionary.addTypes(Biomes.SNOWY_SLOPES, BiomeDictionary.Type.COLD, BiomeDictionary.Type.SPARSE,
                BiomeDictionary.Type.SNOWY, BiomeDictionary.Type.MOUNTAIN, BiomeDictionary.Type.OVERWORLD);
        BiomeDictionary.addTypes(Biomes.JAGGED_PEAKS, BiomeDictionary.Type.COLD, BiomeDictionary.Type.SPARSE,
                BiomeDictionary.Type.SNOWY, BiomeDictionary.Type.MOUNTAIN, BiomeDictionary.Type.OVERWORLD);
        BiomeDictionary.addTypes(Biomes.FROZEN_PEAKS, BiomeDictionary.Type.COLD, BiomeDictionary.Type.SPARSE,
                BiomeDictionary.Type.SNOWY, BiomeDictionary.Type.MOUNTAIN, BiomeDictionary.Type.OVERWORLD);
        BiomeDictionary.addTypes(Biomes.STONY_PEAKS, BiomeDictionary.Type.HOT, BiomeDictionary.Type.MOUNTAIN,
                BiomeDictionary.Type.OVERWORLD);

        BiomeDictionary.Type UNDERGROUND = BiomeDictionary.Type.getType("UNDERGROUND");

        BiomeDictionary.addTypes(Biomes.LUSH_CAVES, UNDERGROUND, BiomeDictionary.Type.LUSH, BiomeDictionary.Type.WET,
                BiomeDictionary.Type.OVERWORLD);
        BiomeDictionary.addTypes(Biomes.DRIPSTONE_CAVES, UNDERGROUND, BiomeDictionary.Type.SPARSE,
                BiomeDictionary.Type.OVERWORLD);

    }

    private static int lastBiomesSize = -1;
    private static List<ResourceKey<Biome>> allBiomeKeys = Lists.newArrayList();
    private static List<Biome> allBiomes = Lists.newArrayList();

    public static Set<ResourceKey<Biome>> SOFTBLACKLIST = Sets.newHashSet();

    private static boolean loadedIn = false;

    public static final Map<String, SpawnRule> PRESETS = Maps.newHashMap();

    public static Collection<ResourceKey<Biome>> getAllBiomeKeys()
    {
        final RegistryAccess REG = ThutCore.proxy.getRegistries();
        SpawnBiomeMatcher.loadedIn = false;
        // Before loading in.
        if (REG == null) return SpawnBiomeMatcher.allBiomeKeys;
        SpawnBiomeMatcher.loadedIn = true;
        final Collection<Entry<ResourceKey<Biome>, Biome>> biomes = REG.registryOrThrow(Registry.BIOME_REGISTRY)
                .entrySet();
        if (SpawnBiomeMatcher.lastBiomesSize != biomes.size())
        {
            SpawnBiomeMatcher.allBiomeKeys = Lists.newArrayList();
            SpawnBiomeMatcher.allBiomes = Lists.newArrayList();
            for (final Entry<ResourceKey<Biome>, Biome> b : biomes)
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
    public Set<ResourceKey<Biome>> _validBiomes = Sets.newHashSet();
    public Set<ResourceKey<Biome>> _blackListBiomes = Sets.newHashSet();

    public Set<BiomeCategory> _validCats = Sets.newHashSet();
    public Set<BiomeCategory> _blackListCats = Sets.newHashSet();

    public Set<BiomeDictionary.Type> _validTypes = Sets.newHashSet();
    public Set<BiomeDictionary.Type> _invalidTypes = Sets.newHashSet();

    public Set<String> _validStructures = Sets.newHashSet();
    public Set<BiomeType> _validSubBiomes = Sets.newHashSet();
    public Set<BiomeType> _blackListSubBiomes = Sets.newHashSet();

    public Set<Weather> _neededWeather = Sets.newHashSet();
    public Set<Weather> _bannedWeather = Sets.newHashSet();

    // These two sets are used for syncing _validBiomes and _validTypes over to
    // clients on multiplayer.
    public Set<ResourceKey<Biome>> clientBiomes = Sets.newHashSet();

    public Set<String> clientTypes = Sets.newHashSet();

    /**
     * If the spawnRule has an anyType key, make a child for each type in it,
     * then check if any of the children are valid.
     */
    public Set<SpawnBiomeMatcher> _and_children = Sets.newHashSet();
    public Set<SpawnBiomeMatcher> _or_children = Sets.newHashSet();

    public Set<Predicate<SpawnCheck>> _additionalConditions = Sets.newHashSet();

    public StructureMatcher _structs = new StructureMatcher()
    {
    };

    public float minLight = 0;
    public float maxLight = 1;

    public boolean day = true;
    public boolean dusk = true;
    public boolean night = true;
    public boolean dawn = true;
    public boolean air = true;
    public boolean water = false;

    public boolean needThunder = false;
    public boolean noThunder = false;

    public boolean strict_type_cat = false;

    public final SpawnRule spawnRule;

    boolean parsed = false;
    boolean valid = true;

    private boolean _checked_cats = false;

    public SpawnBiomeMatcher(final SpawnRule rules)
    {
        this.spawnRule = rules;
    }

    public boolean validCategory(final BiomeCategory cat)
    {
        this.parse();
        if (this._blackListCats.contains(cat)) return false;
        if (this._validCats.isEmpty()) return true;
        return this._validCats.contains(cat);
    }

    public Set<ResourceKey<Biome>> getInvalidBiomes()
    {
        // Ensures we are actually loaded in, this is required to set loadedIn
        // true for the below check.
        SpawnBiomeMatcher.getAllBiomeKeys();
        if (!this._checked_cats && SpawnBiomeMatcher.loadedIn)
        {
            this._checked_cats = true;
            for (final Biome b : SpawnBiomeMatcher.getAllBiomes())
            {
                final ResourceKey<Biome> key = BiomeDatabase.getKey(b);
                if (this._blackListCats.contains(b.getBiomeCategory())) this._blackListBiomes.add(key);
                for (final BiomeDictionary.Type type : this._invalidTypes) if (BiomeDictionary.hasType(key, type))
                {
                    this.getInvalidBiomes().add(key);
                    break;
                }
            }
            for (final Biome b : SpawnBiomeMatcher.getAllBiomes())
            {
                final ResourceKey<Biome> key = BiomeDatabase.getKey(b);
                if (key.location() == null) continue;
                if (this._blackListBiomes.contains(key))
                {
                    this._validBiomes.remove(key);
                    continue;
                }
                if (this._validCats.isEmpty() && this._validTypes.isEmpty()) continue;

                boolean validCat = this._validCats.isEmpty() || this._validCats.contains(b.getBiomeCategory());

                if (!validCat && _validTypes.isEmpty()) continue;
                boolean validType = true;
                if (!this._validTypes.isEmpty())
                {
                    for (final BiomeDictionary.Type type : this._validTypes)
                        validType = validType && BiomeDictionary.hasType(key, type);
                }
                if (!validType) continue;
                this._validBiomes.add(key);
            }
        }
        return this._blackListBiomes;
    }

    public Set<ResourceKey<Biome>> getValidBiomes()
    {
        this.getInvalidBiomes();
        return this._validBiomes;
    }

    private ResourceKey<Biome> from(final BiomeLoadingEvent event)
    {
        return ResourceKey.create(Registry.BIOME_REGISTRY, event.getName());
    }

    public boolean checkLoadEvent(final BiomeLoadingEvent event)
    {
        // Parse this to initialize the lists at least.
        this.parse();

        // First check children
        if (!this._and_children.isEmpty())
        {
            return _and_children.stream().allMatch(m -> m.checkLoadEvent(event));
        }
        if (!this._or_children.isEmpty())
        {
            return _or_children.stream().anyMatch(m -> m.checkLoadEvent(event));
        }

        // This checks if there is acategory at all.
        if (!this.validCategory(event.getCategory())) return false;
        final ResourceKey<Biome> key = this.from(event);
        // Check types, etc manually here, as this can be run before biomes are
        // actually valid.
        for (final BiomeDictionary.Type type : this._invalidTypes) if (BiomeDictionary.hasType(key, type)) return false;
        if (this._blackListBiomes.contains(key)) return false;
        if (this._validSubBiomes.contains(BiomeType.ALL)) return true;
        if (!this._validTypes.isEmpty())
        {
            boolean all = true;

            for (final BiomeDictionary.Type type : this._validTypes) all = all && BiomeDictionary.hasType(key, type);
            if (!all) return false;
        }
        if (!this._validBiomes.isEmpty()) return this._validBiomes.contains(key);
        return true;
    }

    /**
     * This is a check for just a single biome, it doesn't factor in the other
     * values such as subbiome (unless flagged all), lighting, time, etc.
     *
     * @param biome
     * @return
     */
    public boolean checkBiome(final ResourceKey<Biome> biome)
    {
        this.parse();
        if (!this.valid) return false;
        
        // First check children
        if (!this._and_children.isEmpty())
        {
            return _and_children.stream().allMatch(m -> m.checkBiome(biome));
        }
        if (!this._or_children.isEmpty())
        {
            return _or_children.stream().anyMatch(m -> m.checkBiome(biome));
        }
        
        if (this.getInvalidBiomes().contains(biome)) return false;
        if (this.getValidBiomes().contains(biome)) return true;
        if (SpawnBiomeMatcher.SOFTBLACKLIST.contains(biome)) return false;
        // The check for all subbiomes overrides the check for biomes.
        if (this._validSubBiomes.contains(BiomeType.ALL)) return true;
        // Otherwise, only return true if we have no valid biomes otherwise!
        return this.getValidBiomes().isEmpty();
    }

    public boolean matches(final SpawnCheck checker)
    {
        this.parse();
        if (!this.valid) return false;
        
        // First check children
        if (!this._and_children.isEmpty())
        {
            return _and_children.stream().allMatch(m -> m.matches(checker));
        }
        if (!this._or_children.isEmpty())
        {
            return _or_children.stream().anyMatch(m -> m.matches(checker));
        }
        
        if (!this.weatherMatches(checker)) return false;
        final boolean biome = this.biomeMatches(checker);
        if (!biome) return false;
        final boolean loc = this.conditionsMatch(checker);
        if (!loc) return false;
        boolean subCondition = true;
        for (final Predicate<SpawnCheck> c : this._additionalConditions) subCondition &= c.apply(checker);
        return subCondition && !MinecraftForge.EVENT_BUS.post(new SpawnCheckEvent.Check(this, checker));
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
        final BiomeType type = checker.type;

        // Check the blacklist first, if this does match, we leave early.
        final boolean blackListed = type.anyMatch(this._blackListSubBiomes);

        if (blackListed) return false;

        final boolean rightBiome = this.checkBiome(checker.biome);

        // If we are not allowed in this biome, return false.
        // This checks if we are blackisted for the biome, or if we need
        // specific biomes and this is not one of them.
        if (!rightBiome) return false;

        final ChunkAccess chunk = checker.chunk;
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
        final boolean noSubbiome = type == BiomeType.NONE;

        final boolean needsSubbiome = !this._validSubBiomes.isEmpty();

        // We need a subbiome, but there is none here! so no spawn.
        if (needsSubbiome && noSubbiome) return false;

        // We are the correct subbiome if we either don't need one, or the valid
        // subbiomes has out current one.
        final boolean rightSubBiome = noSubbiome || type.anyMatch(this._validSubBiomes);

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

    private BiomeCategory getCat(final String name)
    {
        for (final BiomeCategory c : BiomeCategory.values()) if (c.getName().equalsIgnoreCase(name)) return c;
        return null;
    }

    private Weather getWeather(final String name)
    {
        for (final Weather c : Weather.values()) if (c.name().equalsIgnoreCase(name)) return c;
        return null;
    }

    private void parseBasic()
    {
        if (this.spawnRule.values.containsKey(SpawnBiomeMatcher.DAY))
            this.day = Boolean.parseBoolean(this.spawnRule.values.get(SpawnBiomeMatcher.DAY));
        if (this.spawnRule.values.containsKey(SpawnBiomeMatcher.NIGHT))
            this.night = Boolean.parseBoolean(this.spawnRule.values.get(SpawnBiomeMatcher.NIGHT));
        if (this.spawnRule.values.containsKey(SpawnBiomeMatcher.DUSK))
            this.dusk = Boolean.parseBoolean(this.spawnRule.values.get(SpawnBiomeMatcher.DUSK));
        if (this.spawnRule.values.containsKey(SpawnBiomeMatcher.DAWN))
            this.dawn = Boolean.parseBoolean(this.spawnRule.values.get(SpawnBiomeMatcher.DAWN));
        if (this.spawnRule.values.containsKey(SpawnBiomeMatcher.WATER))
            this.water = Boolean.parseBoolean(this.spawnRule.values.get(SpawnBiomeMatcher.WATER));
        if (this.spawnRule.values.containsKey(SpawnBiomeMatcher.AIR))
        {
            this.air = Boolean.parseBoolean(this.spawnRule.values.get(SpawnBiomeMatcher.AIR));
            if (!this.air && !this.water) this.water = true;
        }
        if (this.spawnRule.values.containsKey(SpawnBiomeMatcher.MINLIGHT))
            this.minLight = Float.parseFloat(this.spawnRule.values.get(SpawnBiomeMatcher.MINLIGHT));
        if (this.spawnRule.values.containsKey(SpawnBiomeMatcher.MAXLIGHT))
            this.maxLight = Float.parseFloat(this.spawnRule.values.get(SpawnBiomeMatcher.MAXLIGHT));
    }

    public void parse()
    {
        if (this.parsed) return;

        SpawnRule spawnRule = this.spawnRule;
        if (spawnRule.values.containsKey(PRESET))
            spawnRule = PRESETS.getOrDefault(spawnRule.values.get(PRESET), spawnRule);

        String or_presets = spawnRule.values.get(SpawnBiomeMatcher.ORPRESET);
        String and_presets = spawnRule.values.get(SpawnBiomeMatcher.ANDPRESET);

        this.reset();

        this.parsed = true;
        this.valid = true;

        if (or_presets != null)
        {
            String[] args = or_presets.split(",");
            for (String s : args)
            {
                SpawnRule rule = PRESETS.get(s);
                if (rule != null)
                {
                    SpawnBiomeMatcher child = new SpawnBiomeMatcher(rule);
                    if (child.valid) this._or_children.add(child);
                }
            }
        }

        if (and_presets != null)
        {
            String[] args = and_presets.split(",");
            for (String s : args)
            {
                SpawnRule rule = PRESETS.get(s);
                if (rule != null)
                {
                    SpawnBiomeMatcher child = new SpawnBiomeMatcher(rule);
                    if (child.valid) this._and_children.add(child);
                }
            }
        }

        this._structs = new StructureMatcher()
        {
        };
        MinecraftForge.EVENT_BUS.post(new SpawnCheckEvent.Init(this));

        for (final SpawnBiomeMatcher child : this._and_children) child.parse();
        for (final SpawnBiomeMatcher child : this._or_children) child.parse();

        if (this._or_children.size() > 0 || this._and_children.size() > 0)
        {
            boolean or_valid = this._or_children.size() > 0;
            boolean and_valid = this._and_children.size() > 0;

            for (final SpawnBiomeMatcher child : this._and_children) and_valid = or_valid && child.valid;
            for (final SpawnBiomeMatcher child : this._or_children) or_valid = or_valid && child.valid;

            this.valid = or_valid || and_valid;
            return;
        }

        // Lets deal with the weather checks
        String weather = spawnRule.values.get(SpawnBiomeMatcher.WEATHER);
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
        weather = spawnRule.values.get(SpawnBiomeMatcher.WEATHERNOT);
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

        final String biomeString = spawnRule.values.get(SpawnBiomeMatcher.BIOMES);
        final String typeString = spawnRule.values.get(SpawnBiomeMatcher.TYPES);
        final String biomeBlacklistString = spawnRule.values.get(SpawnBiomeMatcher.BIOMESBLACKLIST);
        final String typeBlacklistString = spawnRule.values.get(SpawnBiomeMatcher.TYPESBLACKLIST);
        final String biomeCat = spawnRule.values.get(SpawnBiomeMatcher.BIOMECAT);
        final String noBiomeCat = spawnRule.values.get(SpawnBiomeMatcher.NOBIOMECAT);
        final String validStructures = spawnRule.values.get(SpawnBiomeMatcher.STRUCTURES);

        this.strict_type_cat = false;
        if (spawnRule.values.containsKey(STRICTTYPECAT))
        {
            this.strict_type_cat = true;
        }

        final Set<BiomeCategory> biomeCats = this._validCats;
        final Set<BiomeCategory> noBiomeCats = this._blackListCats;

        if (biomeCat != null)
        {
            String[] args = biomeCat.split(",");
            for (final String s : args)
            {
                final BiomeCategory c = this.getCat(s);
                if (c != null) biomeCats.add(c);
            }
        }
        if (!this.strict_type_cat && typeString != null)
        {
            String[] args = typeString.split(",");
            for (final String s : args)
            {
                final BiomeCategory c = this.getCat(s);
                if (c != null) biomeCats.add(c);
            }
        }
        if (noBiomeCat != null)
        {
            String[] args = noBiomeCat.split(",");
            for (final String s : args)
            {
                final BiomeCategory c = this.getCat(s);
                if (c != null) noBiomeCats.add(c);
            }
        }
        if (!this.strict_type_cat && typeBlacklistString != null)
        {
            String[] args = typeBlacklistString.split(",");
            for (final String s : args)
            {
                final BiomeCategory c = this.getCat(s);
                if (c != null) noBiomeCats.add(c);
            }
        }

        if (validStructures != null)
        {
            final String[] args = validStructures.split(",");
            for (final String s : args) this._validStructures.add(s);
        }

        if (biomeString != null)
        {
            final String[] args = biomeString.split(",");
            for (String s : args)
            {
                s = s.trim();
                // Ensure we are a resourcelocation!
                if (!s.contains(":")) s = "minecraft:" + s;
                final ResourceKey<Biome> biome = ResourceKey.create(Registry.BIOME_REGISTRY, new ResourceLocation(s));
                this._validBiomes.add(biome);
            }
        }
        boolean hasForgeTypes = false;
        if (typeString != null)
        {
            String[] args = typeString.split(",");
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
        if (!this.strict_type_cat && biomeCat != null)
        {
            String[] args = biomeCat.split(",");
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
                final ResourceKey<Biome> biome = ResourceKey.create(Registry.BIOME_REGISTRY, new ResourceLocation(s));
                this._blackListBiomes.add(biome);
            }
        }
        if (typeBlacklistString != null)
        {
            String[] args = typeBlacklistString.split(",");
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
                for (final BiomeType b : BiomeType.values()) if (Database.trim(b.name).equals(s))
                {
                    subBiome = b;
                    break;
                }
                if (subBiome == null) subBiome = BiomeType.getBiome(s);
                if (subBiome != BiomeType.NONE) this._blackListSubBiomes.add(subBiome);
            }
        }
        if (!this.strict_type_cat && noBiomeCat != null)
        {
            String[] args = noBiomeCat.split(",");
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

        if (!this.valid && SpawnBiomeMatcher.loadedIn)
            PokecubeCore.LOGGER.error("Invalid Matcher: {}", PacketPokedex.gson.toJson(this));
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
                for (final BiomeType b : BiomeType.values()) if (Database.trim(b.name).equals(Database.trim(s)))
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
        if (this._and_children == null) this._and_children = Sets.newHashSet();
        if (this._or_children == null) this._or_children = Sets.newHashSet();

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
        this._and_children.clear();
        this._or_children.clear();

        minLight = 0;
        maxLight = 1;

        day = true;
        dusk = true;
        night = true;
        dawn = true;
        air = true;
        water = false;
    }
}
