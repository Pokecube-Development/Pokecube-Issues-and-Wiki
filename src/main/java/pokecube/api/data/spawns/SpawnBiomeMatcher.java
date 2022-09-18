package pokecube.api.data.spawns;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.google.common.base.Predicate;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.material.Material;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.server.ServerLifecycleHooks;
import pokecube.api.PokecubeAPI;
import pokecube.api.data.spawns.SpawnCheck.MatchResult;
import pokecube.api.data.spawns.SpawnCheck.TerrainType;
import pokecube.api.data.spawns.SpawnCheck.Weather;
import pokecube.api.events.pokemobs.SpawnCheckEvent;
import pokecube.core.database.Database;
import pokecube.core.database.pokedex.PokedexEntryLoader.SpawnRule;
import pokecube.core.network.packets.PacketPokedex;
import thut.api.terrain.BiomeDatabase;
import thut.api.terrain.BiomeType;
import thut.api.terrain.StructureManager;
import thut.api.terrain.StructureManager.StructureInfo;

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
                    if (matcher._validStructures.contains(i.getName())) return MatchResult.SUCCEED;
                return MatchResult.FAIL;
            }
            return MatchResult.PASS;
        }
    }

    public static final String TYPES = "types";
    public static final String TYPESBLACKLIST = "typesBlacklist";

    public static final String STRUCTURES = "structures";
    public static final String STRUCTURESBLACK = "noStructures";

    public static final String TERRAIN = "terrain";

    public static final String NIGHT = "night";
    public static final String DAY = "day";
    public static final String DUSK = "dusk";
    public static final String DAWN = "dawn";

    public static final String AIR = "air";
    public static final String WATER = "water";
    public static final String MINLIGHT = "minLight";

    public static final String WEATHER = "weather";
    public static final String WEATHERNOT = "noWeather";

    public static final String BIOMES = "biomes";
    public static final String BIOMESBLACKLIST = "biomesBlacklist";

    public static final String MAXLIGHT = "maxLight";

    public static final String SPAWNCOMMAND = "command";

    public static final String PRESET = "preset";

    public static final String ANDPRESET = "and_preset";
    public static final String ORPRESET = "or_preset";
    public static final String NOTPRESET = "not_preset";

    public static final SpawnBiomeMatcher ALLMATCHER;
    public static final SpawnBiomeMatcher NONEMATCHER;

    private static final Map<String, SpawnRule> RULES = Maps.newHashMap();
    private static final Map<SpawnRule, SpawnBiomeMatcher> MATCHERS = Maps.newHashMap();

    @Nonnull
    public static SpawnBiomeMatcher get(SpawnRule rule)
    {
        final SpawnRule orig = rule;
        rule = RULES.computeIfAbsent(rule.toString(), s -> orig.copy());
        return MATCHERS.computeIfAbsent(rule, r -> new SpawnBiomeMatcher(r));
    }

    @Nullable
    public static SpawnBiomeMatcher get(String preset)
    {
        SpawnRule rule = SpawnBiomeMatcher.PRESETS.get(preset);
        if (rule == null) return null;
        final SpawnRule orig = rule;
        rule = RULES.computeIfAbsent(rule.toString(), s -> orig.copy());
        return MATCHERS.computeIfAbsent(rule, r -> new SpawnBiomeMatcher(r));
    }

    static
    {
        SpawnRule rule = new SpawnRule();
        rule.values.put(SpawnBiomeMatcher.TYPES, "all");
        ALLMATCHER = SpawnBiomeMatcher.get(rule);
        rule = new SpawnRule();
        rule.values.put(SpawnBiomeMatcher.TYPES, "none");
        NONEMATCHER = SpawnBiomeMatcher.get(rule);
    }

    public static void populateClientValues(SpawnBiomeMatcher matcher)
    {
        clearClientValues(matcher);
        try
        {
            var reg = ServerLifecycleHooks.getCurrentServer().registryAccess().registryOrThrow(Registry.BIOME_REGISTRY);
            for (final ResourceLocation test : reg.keySet())
            {
                var holder = reg.getHolderOrThrow(ResourceKey.create(Registry.BIOME_REGISTRY, test));
                final boolean valid = matcher.checkBiome(holder);
                if (valid)
                {
                    matcher.clientBiomes.add(test);
                }
            }
            if (matcher.clientBiomes.size() == reg.keySet().size()) matcher.clientBiomes.clear();
        }
        catch (Exception e)
        {
            PokecubeAPI.LOGGER.error("Error attempting to read biomes for a spawnBiomeMatcher to send to client!");
            PokecubeAPI.LOGGER.error(e);
        }
        for (var type : BiomeType.values())
        {
            if (matcher.checkSubBiome(type)) matcher.clientTypes.add(type.name);
        }
        if (matcher.clientTypes.size() == BiomeType.values().size()) matcher.clientTypes.clear();
        matcher.clientStructures.addAll(matcher._validStructures);
    }

    public static void clearClientValues(SpawnBiomeMatcher matcher)
    {
        matcher.clientBiomes.clear();
        matcher.clientTypes.clear();
        matcher.clientStructures.clear();
    }

    public static Set<TagKey<Biome>> SOFTBLACKLIST = Sets.newHashSet();

    private static boolean loadedIn = false;

    public static final Map<String, SpawnRule> PRESETS = Maps.newHashMap();

    private static final Set<TerrainType> ALL_TERRAIN = Sets.newHashSet(TerrainType.values());

    // These are private so that they can force an update based on categories
    public Set<TagKey<Biome>> _validBiomes = Sets.newHashSet();
    public Set<TagKey<Biome>> _blackListBiomes = Sets.newHashSet();

    public HolderSet<Biome> _biomeHolderset = null;

    public Set<String> _validStructures = Sets.newHashSet();
    public Set<BiomeType> _validSubBiomes = Sets.newHashSet();
    public Set<BiomeType> _blackListSubBiomes = Sets.newHashSet();

    public Set<Weather> _neededWeather = Sets.newHashSet();
    public Set<Weather> _bannedWeather = Sets.newHashSet();

    /**
     * If the spawnRule has an anyType key, make a child for each type in it,
     * then check if any of the children are valid.
     */
    public List<SpawnBiomeMatcher> _and_children = new ArrayList<>();
    public List<SpawnBiomeMatcher> _or_children = new ArrayList<>();
    public List<SpawnBiomeMatcher> _not_children = new ArrayList<>();

    public Set<Predicate<SpawnCheck>> _additionalConditions = Sets.newHashSet();

    public StructureMatcher _structs = new StructureMatcher()
    {
    };

    private boolean __client__ = false;

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

    public boolean _parsed = false;
    public boolean _valid = true;

    public MutableComponent _description = null;

    public Set<TerrainType> _validTerrain = ALL_TERRAIN;

    public List<ResourceLocation> clientBiomes = new ArrayList<>();
    public List<String> clientTypes = new ArrayList<>();
    public List<String> clientStructures = new ArrayList<>();

    /**
     * Do not call this, use the static method instead!
     * 
     * @param rules
     */
    private SpawnBiomeMatcher(final SpawnRule rules)
    {
        this.spawnRule = rules;
        if (!this.spawnRule.isValid()) PokecubeAPI.LOGGER.error("No rules found!", new IllegalArgumentException());
    }

    public SpawnBiomeMatcher setClient()
    {
        return setClient(true);
    }

    public SpawnBiomeMatcher setClient(boolean client)
    {
        __client__ = client;
        return this;
    }

    public Set<TagKey<Biome>> getInvalidBiomes()
    {
        return this._blackListBiomes;
    }

    public Set<TagKey<Biome>> getValidBiomes()
    {
        return this._validBiomes;
    }

    /**
     * This is a check for just a single biome, it doesn't factor in the other
     * values such as subbiome (unless flagged all), lighting, time, etc.
     * 
     * This is synchronised as it is run during worldgen, on multiple threads.
     *
     * @param biome
     * @return true if the biome matches us
     */
    public synchronized boolean checkBiome(final Holder<Biome> biome)
    {
        this.parse();
        if (!this._valid)
        {
            return false;
        }
        // First check children
        if (!this._not_children.isEmpty())
        {
            boolean any = _not_children.stream().anyMatch(m -> m.checkBiome(biome));
            if (any) return false;
        }
        boolean or_valid = true;
        if (!this._or_children.isEmpty())
        {
            or_valid = _or_children.stream().anyMatch(m -> m.checkBiome(biome));
        }
        if (!or_valid) return false;
        if (!this._and_children.isEmpty())
        {
            boolean and_valid = _and_children.stream().allMatch(m -> m.checkBiome(biome));
            return and_valid;
        }
        if (!this._or_children.isEmpty()) return or_valid;
        if (this.getInvalidBiomes().stream().anyMatch(biome::is)) return false;
        if (this.getValidBiomes().stream().anyMatch(biome::is)) return true;
        if (SpawnBiomeMatcher.SOFTBLACKLIST.stream().anyMatch(biome::is)) return false;

        // Next check our holderset if we have one
        if (_biomeHolderset != null) return _biomeHolderset.contains(biome);

        // Otherwise, only return true if we have no valid biomes otherwise!
        return this.getValidBiomes().isEmpty();
    }

    /**
     * This is a check for just a single biome type, it doesn't factor in the
     * other values such as biome, lighting, time, etc.
     * 
     *
     * @param subbiome to check
     * @return true if the biome matches us
     */
    public boolean checkSubBiome(final BiomeType biome)
    {
        this.parse();
        if (!this._valid)
        {
            return false;
        }
        // First check children
        if (!this._not_children.isEmpty())
        {
            boolean any = _not_children.stream().anyMatch(m -> m.checkSubBiome(biome));
            if (any) return false;
        }
        boolean or_valid = true;
        if (!this._or_children.isEmpty())
        {
            or_valid = _or_children.stream().anyMatch(m -> m.checkSubBiome(biome));
        }
        if (!or_valid) return false;
        if (!this._and_children.isEmpty())
        {
            boolean and_valid = _and_children.stream().allMatch(m -> m.checkSubBiome(biome));
            return and_valid;
        }
        if (!this._or_children.isEmpty()) return or_valid;
        if (this._blackListSubBiomes.stream().anyMatch(biome::equals)) return false;
        if (this._validSubBiomes.contains(BiomeType.ALL)) return true;
        if (this._validSubBiomes.stream().anyMatch(biome::equals)) return true;
        return false;
    }

    public synchronized boolean matches(final SpawnCheck checker)
    {
        this.parse();
        if (!this._valid) return false;
        // First check children
        if (!this._not_children.isEmpty())
        {
            boolean any = _not_children.stream().anyMatch(m -> m.matches(checker));
            if (any) return false;
        }
        boolean or_valid = true;
        if (!this._or_children.isEmpty())
        {
            or_valid = _or_children.stream().anyMatch(m -> m.matches(checker));
        }
        if (!or_valid) return false;
        if (!this._and_children.isEmpty())
        {
            return _and_children.stream().allMatch(m -> m.matches(checker));
        }
        if (!this._or_children.isEmpty()) return or_valid;
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
        if (!this._valid) return false;
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

        // If the type is "none", or we don't have any valid biomes or subbiomes
        // then it means this entry doesn't spawn (ie is supposed to be in a
        // structure)
        // structures were checked earlier, so we return false here.
        final boolean noSpawn = this._validSubBiomes.contains(BiomeType.NONE);
        if (noSpawn) return false;

        //@formatter:off
        // allValid means we spawn everywhere not blacklisted, so return true;
        final boolean allValid = this._validSubBiomes.contains(BiomeType.ALL)
        // Alternately, if no locations are specified at all, this means we are most likely
        // a preset for say time of say or terrain, in which ase, we should return true.
                || (this.getValidBiomes().isEmpty() 
                    && this._validSubBiomes.isEmpty());
        //@formatter:on
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
        if (!_validTerrain.contains(checker.terrain)) return false;
        final Material m = checker.material;
        final boolean isWater = m == Material.WATER;
        if (isWater && !this.water) return false;
        if (m.isLiquid() && !isWater) return false;
        if (!this.air && !isWater) return false;
        final float light = checker.light;
        return light <= this.maxLight && light >= this.minLight;
    }

    private TerrainType getTerrain(final String name)
    {
        for (final TerrainType c : TerrainType.values()) if (c.name().equalsIgnoreCase(name)) return c;
        return null;
    }

    private Weather getWeather(final String name)
    {
        for (final Weather c : Weather.values()) if (c.name().equalsIgnoreCase(name)) return c;
        return null;
    }

    private boolean parseBasic(SpawnRule rule)
    {
        boolean changed = false;

        if (rule.values.containsKey(SpawnBiomeMatcher.DAY))
        {
            changed = true;
            this.day = Boolean.parseBoolean(rule.values.get(SpawnBiomeMatcher.DAY));
        }
        if (rule.values.containsKey(SpawnBiomeMatcher.NIGHT))
        {
            changed = true;
            this.night = Boolean.parseBoolean(rule.values.get(SpawnBiomeMatcher.NIGHT));
        }
        if (rule.values.containsKey(SpawnBiomeMatcher.DUSK))
        {
            changed = true;
            this.dusk = Boolean.parseBoolean(rule.values.get(SpawnBiomeMatcher.DUSK));
        }
        if (rule.values.containsKey(SpawnBiomeMatcher.DAWN))
        {
            changed = true;
            this.dawn = Boolean.parseBoolean(rule.values.get(SpawnBiomeMatcher.DAWN));
        }
        if (rule.values.containsKey(SpawnBiomeMatcher.WATER))
        {
            changed = true;
            this.water = Boolean.parseBoolean(rule.values.get(SpawnBiomeMatcher.WATER));
        }
        if (rule.values.containsKey(SpawnBiomeMatcher.AIR))
        {
            changed = true;
            this.air = Boolean.parseBoolean(rule.values.get(SpawnBiomeMatcher.AIR));
            if (!this.air && !this.water) this.water = true;
        }
        if (rule.values.containsKey(SpawnBiomeMatcher.MINLIGHT))
        {
            changed = true;
            this.minLight = Float.parseFloat(rule.values.get(SpawnBiomeMatcher.MINLIGHT));
        }
        if (rule.values.containsKey(SpawnBiomeMatcher.MAXLIGHT))
        {
            changed = true;
            this.maxLight = Float.parseFloat(rule.values.get(SpawnBiomeMatcher.MAXLIGHT));
        }
        return changed;
    }

    // These two will be used to account for any custom stuff in the spawnRule.
    // In the case where the spawn rule only defines presets, then these will be
    // discarded later.
    SpawnBiomeMatcher _or_base = null;
    SpawnBiomeMatcher _and_base = null;

    private void createChildren()
    {
        SpawnRule spawnRule = this.spawnRule.copy();
        if (spawnRule.values.containsKey(PRESET))
        {
            String key = spawnRule.values.remove(PRESET);
            SpawnRule preset = PRESETS.get(key);
            if (preset != null)
            {
                preset = preset.copy();
                preset.values.putAll(spawnRule.values);
                spawnRule = preset;
            }
            else
            {
                PokecubeAPI.LOGGER.error("No preset found for {}", key);
            }
        }

        String or_presets = spawnRule.values.remove(SpawnBiomeMatcher.ORPRESET);
        String and_presets = spawnRule.values.remove(SpawnBiomeMatcher.ANDPRESET);
        String not_presets = spawnRule.values.remove(SpawnBiomeMatcher.NOTPRESET);

        if (or_presets != null)
        {
            String[] args = or_presets.split(",");

            SpawnRule base = spawnRule.copy();
            base.values.remove(SpawnBiomeMatcher.ORPRESET);
            base.values.remove(PRESET);
            if (!base.values.isEmpty())
            {
                _or_base = SpawnBiomeMatcher.get(base).setClient(__client__);
                _or_base.reset();
                this._or_children.add(_or_base);
            }

            for (String s : args)
            {
                SpawnRule rule = PRESETS.get(s);
                if (rule != null)
                {
                    rule = rule.copy();
                    SpawnBiomeMatcher child = SpawnBiomeMatcher.get(rule).setClient(__client__);
                    child.reset();
                    this._or_children.add(child);
                }
                else if (!__client__) PokecubeAPI.LOGGER.error("No preset found for or_preset {} in {}", s, or_presets);
            }
        }
        if (and_presets != null)
        {
            String[] args = and_presets.split(",");

            SpawnRule base = spawnRule.copy();
            base.values.remove(SpawnBiomeMatcher.ANDPRESET);
            base.values.remove(PRESET);
            if (!base.values.isEmpty())
            {
                _and_base = SpawnBiomeMatcher.get(base).setClient(__client__);
                _and_base.reset();
                this._and_children.add(_and_base);
            }

            for (String s : args)
            {
                SpawnRule rule = PRESETS.get(s);
                if (rule != null)
                {
                    rule = rule.copy();
                    SpawnBiomeMatcher child = SpawnBiomeMatcher.get(rule).setClient(__client__);
                    child.reset();
                    this._and_children.add(child);
                }
                else if (!__client__)
                    PokecubeAPI.LOGGER.error("No preset found for and_preset {} in {}", s, and_presets);
            }
        }
        if (not_presets != null)
        {
            String[] args = not_presets.split(",");
            for (String s : args)
            {
                SpawnRule rule = PRESETS.get(s);
                if (rule != null)
                {
                    rule = rule.copy();
                    SpawnBiomeMatcher child = SpawnBiomeMatcher.get(rule).setClient(__client__);
                    child.reset();
                    this._not_children.add(child);
                }
                else if (!__client__)
                    PokecubeAPI.LOGGER.error("No preset found for and_preset {} in {}", s, and_presets);
            }
        }
    }

    private boolean initRawLists()
    {
        final String typeString = spawnRule.values.get(SpawnBiomeMatcher.TYPES);
        final String typeBlacklistString = spawnRule.values.get(SpawnBiomeMatcher.TYPESBLACKLIST);
        final String validStructures = spawnRule.values.get(SpawnBiomeMatcher.STRUCTURES);
        final String terrain = spawnRule.values.get(SpawnBiomeMatcher.TERRAIN);

        if (validStructures != null)
        {
            final String[] args = validStructures.split(",");
            for (final String s : args) this._validStructures.add(s);
        }
        if (typeString != null)
        {
            String[] args = typeString.split(",");
            for (String s : args)
            {
                if (BiomeDatabase.isBiomeTag(s))
                {
                    TagKey<Biome> tag = TagKey.create(Registry.BIOME_REGISTRY,
                            new ResourceLocation(s.replace("#", "")));
                    this._validBiomes.add(tag);
                    continue;
                }
                s = Database.trim(s);
                final BiomeType subBiome = BiomeType.getBiome(s);
                this._validSubBiomes.add(subBiome);
            }
        }
        if (typeBlacklistString != null)
        {
            String[] args = typeBlacklistString.split(",");
            for (String s : args)
            {
                if (BiomeDatabase.isBiomeTag(s))
                {
                    TagKey<Biome> tag = TagKey.create(Registry.BIOME_REGISTRY,
                            new ResourceLocation(s.replace("#", "")));
                    this._blackListBiomes.add(tag);
                    continue;
                }
                s = Database.trim(s);
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
        if (terrain != null)
        {
            String[] args = terrain.split(",");
            Set<TerrainType> valid = Sets.newHashSet();
            for (String s : args)
            {
                TerrainType type = getTerrain(s);
                if (type != null)
                {
                    valid.add(type);
                }
            }
            _validTerrain = valid;
        }
        this._validBiomes.removeAll(this._blackListBiomes);
        return !this._validBiomes.isEmpty() || !this._blackListBiomes.isEmpty();
    }

    /**
     * This sets up the lists of valid biomes and types.
     * 
     * This is synchronised as it is run during worldgen, on multiple threads.
     */
    public synchronized void parse()
    {
        if (this._parsed || __client__) return;

        if (this.spawnRule.values.isEmpty())
            PokecubeAPI.LOGGER.error("No rules found!", new IllegalArgumentException());

        SpawnRule spawnRule = this.spawnRule.copy();
        if (spawnRule.values.containsKey(PRESET))
        {
            SpawnRule preset = PRESETS.get(spawnRule.values.remove(PRESET));
            if (preset != null)
            {
                preset = preset.copy();
                preset.values.putAll(spawnRule.values);
                spawnRule = preset;
            }
        }

        this.reset();

        this._parsed = true;
        this._valid = true;

        createChildren();

        this._structs = new StructureMatcher()
        {
        };
        MinecraftForge.EVENT_BUS.post(new SpawnCheckEvent.Init(this));

        if (spawnRule.biomes != null)
        {
            PokecubeAPI.LOGGER.warn("Warning, holdersets are not yet implemented here!");
        }

        for (final SpawnBiomeMatcher child : this._and_children) child.parse();
        for (final SpawnBiomeMatcher child : this._or_children) child.parse();
        for (final SpawnBiomeMatcher child : this._not_children) child.parse();

        // These were base cases, they can be invalid and be fine.
        if (_or_base != null && !_or_base._valid)
        {
            this._or_children.remove(_or_base);
        }
        // These were base cases, they can be invalid and be fine.
        if (_and_base != null && !_and_base._valid)
        {
            this._and_children.remove(_and_base);
        }

        if (this._or_children.size() > 0 || this._and_children.size() > 0)
        {
            boolean or_valid = this._or_children.size() > 0;
            boolean and_valid = this._and_children.size() > 0;

            for (final SpawnBiomeMatcher child : this._and_children) and_valid = and_valid && child._valid;
            for (final SpawnBiomeMatcher child : this._or_children) or_valid = or_valid || child._valid;

            this._valid = or_valid || and_valid;

            if (!this._valid && SpawnBiomeMatcher.loadedIn && !__client__)
            {
                PokecubeAPI.LOGGER.debug("Invalid Matcher: {}", PacketPokedex.gson.toJson(spawnRule));
            }
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

        this.preParseSubBiomes(spawnRule);
        boolean hasBasicSettings = this.parseBasic(spawnRule) || this._biomeHolderset != null;
        initRawLists();

        //@formatter:off
        final boolean hasSomething = !(
                                   this._validSubBiomes.isEmpty()
                                && this._validBiomes.isEmpty()
                                && this._validStructures.isEmpty()
                                && this._blackListBiomes.isEmpty()
                                && this._blackListSubBiomes.isEmpty()
                                );
        //@formatter:on
        if (!hasSomething && !hasBasicSettings) this._valid = false;

        if (!this._valid && SpawnBiomeMatcher.loadedIn) PokecubeAPI.LOGGER.debug("Invalid Matcher: {} ({})",
                PacketPokedex.gson.toJson(spawnRule), PacketPokedex.gson.toJson(this.spawnRule));
    }

    private void preParseSubBiomes(SpawnRule rule)
    {
        final String typeString = rule.values.get(SpawnBiomeMatcher.TYPES);
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
                if (subBiome == null && !BiomeDatabase.isBiomeTag(s)) BiomeType.getBiome(s.trim(), true);
            }
        }
    }

    /**
     * This resets the lists for if they need to be recomputed for resource
     * reloading/etc.
     * 
     * This is synchronised as it may be run during worldgen, on multiple
     * threads.
     */
    public synchronized void reset()
    {
        this._parsed = false;
        this._valid = false;
        this._description = null;

        // Somehow these can end up null after the gson parsing, so we need to
        // ensure they are not null here.
        if (this._validBiomes == null) this._validBiomes = Sets.newHashSet();
        if (this._validSubBiomes == null) this._validSubBiomes = Sets.newHashSet();
        if (this._blackListBiomes == null) this._blackListBiomes = Sets.newHashSet();
        if (this._blackListSubBiomes == null) this._blackListSubBiomes = Sets.newHashSet();
        if (this._validStructures == null) this._validStructures = Sets.newHashSet();
        if (this._bannedWeather == null) this._bannedWeather = Sets.newHashSet();
        if (this._neededWeather == null) this._neededWeather = Sets.newHashSet();
        if (this._and_children == null) this._and_children = new ArrayList<>();
        if (this._or_children == null) this._or_children = new ArrayList<>();
        if (this._not_children == null) this._not_children = new ArrayList<>();

        // Now lets ensure they are empty.
        this._validBiomes.clear();
        this._validSubBiomes.clear();
        this._blackListBiomes.clear();
        this._blackListSubBiomes.clear();
        this._validStructures.clear();
        this._bannedWeather.clear();
        this._neededWeather.clear();
        this._and_children.clear();
        this._or_children.clear();
        this._not_children.clear();

        minLight = 0;
        maxLight = 1;

        day = true;
        dusk = true;
        night = true;
        dawn = true;
        air = true;
        water = false;

        this._biomeHolderset = null;

        _validTerrain = ALL_TERRAIN;
    }

    public String debugPrint(int depth)
    {
        String header = "\n";
        for (int i = 0; i < depth; i++) header += " ";
        String ret = header;
        if (!_not_children.isEmpty())
        {
            ret += header + "Not: ";
            for (SpawnBiomeMatcher m : _not_children) ret += header + m.debugPrint(depth + 1);
        }
        if (!_or_children.isEmpty())
        {
            ret += header + "Or: ";
            for (SpawnBiomeMatcher m : _or_children) ret += header + m.debugPrint(depth + 1);
        }
        if (!_and_children.isEmpty())
        {
            ret += header + "And: ";
            for (SpawnBiomeMatcher m : _and_children) ret += header + m.debugPrint(depth + 1);
            return ret;
        }
        if (!_or_children.isEmpty()) return ret;
        if (!_validBiomes.isEmpty()) ret += header + "biomes: " + this._validBiomes;
        if (!_validSubBiomes.isEmpty()) ret += header + "subbiomes: " + this._validSubBiomes;
        if (!_validStructures.isEmpty()) ret += header + "structures: " + this._validStructures;
        if (!_blackListBiomes.isEmpty()) ret += header + "not-biomes: " + this._blackListBiomes;
        if (!_blackListSubBiomes.isEmpty()) ret += header + "not-subbiomes: " + this._blackListSubBiomes;
        return ret;
    }
}
