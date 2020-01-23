package pokecube.core.database;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.namespace.QName;

import com.google.common.base.Predicate;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import net.minecraft.block.material.Material;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.LightType;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.registries.ForgeRegistries;
import pokecube.core.database.PokedexEntryLoader.SpawnRule;
import pokecube.core.events.pokemob.SpawnCheckEvent;
import thut.api.maths.Vector3;
import thut.api.terrain.BiomeDatabase;
import thut.api.terrain.BiomeType;
import thut.api.terrain.TerrainManager;
import thut.api.terrain.TerrainSegment;

public class SpawnBiomeMatcher
{
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
        public final World            world;
        public final Vector3          location;

        public SpawnCheck(final Vector3 location, final World world)
        {
            this.world = world;
            this.location = location;
            // TODO better way to choose current time.
            final double time = world.getDayTime() / 24000;
            this.day = PokedexEntry.day.contains(time);
            this.dusk = PokedexEntry.dusk.contains(time);
            this.dawn = PokedexEntry.dawn.contains(time);
            this.night = PokedexEntry.night.contains(time);
            this.material = location.getBlockMaterial(world);
            int lightBlock = world.getLightFor(LightType.BLOCK, location.getPos());
            final int lightDay = world.getLightFor(LightType.SKY, location.getPos());
            if (lightBlock == 0 && world.isDaytime()) lightBlock = lightDay;
            this.light = lightBlock / 15f;
            this.biome = location.getBiome(world).getRegistryName();
            final TerrainSegment t = TerrainManager.getInstance().getTerrian(world, location);
            final int subBiomeId = t.getBiome(location);
            if (subBiomeId >= 0) this.type = BiomeType.getType(subBiomeId);
            else this.type = null;
        }
    }

    public static final QName ATYPES          = new QName("anyType");
    public static final QName BIOMES          = new QName("biomes");
    public static final QName TYPES           = new QName("types");
    public static final QName BIOMESBLACKLIST = new QName("biomesBlacklist");
    public static final QName TYPESBLACKLIST  = new QName("typesBlacklist");
    public static final QName NIGHT           = new QName("night");
    public static final QName DAY             = new QName("day");
    public static final QName DUSK            = new QName("dusk");
    public static final QName DAWN            = new QName("dawn");
    public static final QName AIR             = new QName("air");
    public static final QName WATER           = new QName("water");
    public static final QName MINLIGHT        = new QName("minLight");

    public static final QName MAXLIGHT = new QName("maxLight");

    public static final QName             SPAWNCOMMAND = new QName("command");
    public static final SpawnBiomeMatcher ALLMATCHER;

    static
    {
        final SpawnRule rule = new SpawnRule();
        rule.values.put(SpawnBiomeMatcher.TYPES, "all");
        ALLMATCHER = new SpawnBiomeMatcher(rule);
    }

    private static int                               lastTypesSize  = -1;
    private static int                               lastBiomesSize = -1;
    private static Map<String, BiomeDictionary.Type> typeMap        = Maps.newHashMap();
    private static List<Biome>                       allBiomes      = Lists.newArrayList();

    public static boolean contains(final Biome biome, final BiomeDictionary.Type type)
    {
        return BiomeDatabase.contains(biome, type);
    }

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

    public static BiomeDictionary.Type getBiomeType(String name)
    {
        name = name.toUpperCase();
        if (SpawnBiomeMatcher.lastTypesSize != BiomeDictionary.Type.getAll().size())
        {
            SpawnBiomeMatcher.typeMap.clear();
            for (final BiomeDictionary.Type type : BiomeDictionary.Type.getAll())
                SpawnBiomeMatcher.typeMap.put(type.getName(), type);
        }
        return SpawnBiomeMatcher.typeMap.get(name);
    }

    public Set<ResourceLocation> validBiomes        = Sets.newHashSet();
    public Set<BiomeType>        validSubBiomes     = Sets.newHashSet();
    public Set<ResourceLocation> blackListBiomes    = Sets.newHashSet();
    public Set<BiomeType>        blackListSubBiomes = Sets.newHashSet();

    /**
     * If the spawnRule has an anyType key, make a child for each type in it,
     * then check if any of the children are valid.
     */
    public Set<SpawnBiomeMatcher> children = Sets.newHashSet();

    public Set<Predicate<SpawnCheck>> additionalConditions = Sets.newHashSet();

    public float   minLight = 0;
    public float   maxLight = 1;
    public boolean day      = true;
    public boolean dusk     = true;
    public boolean night    = true;
    public boolean dawn     = true;
    public boolean air      = true;
    public boolean water    = false;

    public final SpawnRule spawnRule;

    boolean parsed = false;
    boolean valid  = true;

    public SpawnBiomeMatcher(final SpawnRule rules)
    {
        this.spawnRule = rules;
        if (rules.values.containsKey(SpawnBiomeMatcher.ATYPES))
        {
            final String typeString = this.spawnRule.values.get(SpawnBiomeMatcher.ATYPES);
            final String[] args = typeString.split(",");
            for (String s : args)
            {
                s = s.trim();
                final SpawnRule newRule = new SpawnRule();
                newRule.values.putAll(rules.values);
                newRule.values.remove(SpawnBiomeMatcher.ATYPES);
                newRule.values.put(SpawnBiomeMatcher.TYPES, s);
                this.children.add(new SpawnBiomeMatcher(newRule));
            }
        }
        MinecraftForge.EVENT_BUS.post(new SpawnCheckEvent.Init(this));
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
        if (this.validSubBiomes.contains(BiomeType.ALL)) return true;
        if (this.validSubBiomes.contains(BiomeType.NONE) || this.validBiomes.isEmpty() && this.blackListBiomes
                .isEmpty()) return false;
        if (this.blackListBiomes.contains(biome.getRegistryName())) return false;
        return this.validBiomes.contains(biome.getRegistryName());
    }

    private boolean biomeMatches(final SpawnCheck checker)
    {
        this.parse();
        if (!this.valid) return false;
        if (this.validSubBiomes.contains(BiomeType.ALL)) return true;
        if (this.validSubBiomes.contains(BiomeType.NONE) || this.validBiomes.isEmpty() && this.validSubBiomes.isEmpty()
                && this.blackListSubBiomes.isEmpty() && this.blackListBiomes.isEmpty()) return false;
        final boolean rightBiome = this.validBiomes.contains(checker.biome) || this.validBiomes.isEmpty();
        boolean rightSubBiome = this.validSubBiomes.isEmpty() && checker.type == null || this.validSubBiomes.contains(
                checker.type);
        if (this.validBiomes.isEmpty() && this.validSubBiomes.isEmpty()) rightSubBiome = true;
        BiomeType type = checker.type;
        if (checker.type == null) type = BiomeType.ALL;
        final boolean blackListed = this.blackListBiomes.contains(checker.biome) || this.blackListSubBiomes.contains(
                type);
        if (blackListed) return false;
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
        final boolean biome = this.biomeMatches(checker);
        if (!biome) return false;
        final boolean loc = this.conditionsMatch(checker);
        if (!loc) return false;
        boolean subCondition = true;
        for (final Predicate<SpawnCheck> c : this.additionalConditions)
            subCondition &= c.apply(checker);
        return subCondition && !MinecraftForge.EVENT_BUS.post(new SpawnCheckEvent.Check(this, checker));
    }

    public void parse()
    {
        if (this.parsed) return;
        this.parsed = true;

        if (!this.children.isEmpty())
        {
            for (final SpawnBiomeMatcher child : this.children)
                child.parse();
            return;
        }

        this.validBiomes.clear();
        this.validSubBiomes.clear();
        this.blackListBiomes.clear();
        this.blackListSubBiomes.clear();
        this.preParseSubBiomes();
        final String biomeString = this.spawnRule.values.get(SpawnBiomeMatcher.BIOMES);
        final String typeString = this.spawnRule.values.get(SpawnBiomeMatcher.TYPES);
        final String biomeBlacklistString = this.spawnRule.values.get(SpawnBiomeMatcher.BIOMESBLACKLIST);
        final String typeBlacklistString = this.spawnRule.values.get(SpawnBiomeMatcher.TYPESBLACKLIST);
        final Set<BiomeDictionary.Type> blackListTypes = Sets.newHashSet();
        final Set<BiomeDictionary.Type> validTypes = Sets.newHashSet();
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
                if (biome != null) this.validBiomes.add(biome.getRegistryName());
            }
        }
        boolean hasForgeTypes = false;
        if (typeString != null)
        {
            final String[] args = typeString.split(",");
            for (String s : args)
            {
                s = s.trim();
                BiomeDictionary.Type type;
                type = SpawnBiomeMatcher.getBiomeType(s);
                if (type != null)
                {
                    hasForgeTypes = true;
                    if (type == BiomeDictionary.Type.WATER)
                    {
                        validTypes.add(BiomeDictionary.Type.RIVER);
                        validTypes.add(BiomeDictionary.Type.OCEAN);
                    }
                    else validTypes.add(type);
                    continue;
                }
                final BiomeType subBiome = BiomeType.getBiome(s.trim(), false);
                this.validSubBiomes.add(subBiome);
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
                if (biome != null) this.blackListBiomes.add(biome.getRegistryName());
            }
        }
        if (typeBlacklistString != null)
        {
            final String[] args = typeBlacklistString.split(",");
            for (String s : args)
            {
                s = s.trim();
                final BiomeDictionary.Type type = SpawnBiomeMatcher.getBiomeType(s);
                if (type != null)
                {
                    if (type == BiomeDictionary.Type.WATER)
                    {
                        blackListTypes.add(BiomeDictionary.Type.RIVER);
                        blackListTypes.add(BiomeDictionary.Type.OCEAN);
                    }
                    else blackListTypes.add(type);
                    continue;
                }

                BiomeType subBiome = null;
                for (final BiomeType b : BiomeType.values())
                    if (Database.trim(b.name).equals(Database.trim(s)))
                    {
                        subBiome = b;
                        break;
                    }
                if (subBiome != BiomeType.NONE) this.blackListSubBiomes.add(subBiome);
            }
        }
        if (!validTypes.isEmpty()) for (final Biome b : SpawnBiomeMatcher.getAllBiomes())
            if (b != null && !this.blackListBiomes.contains(b))
            {
                boolean matches = true;
                for (final BiomeDictionary.Type type : validTypes)
                {
                    matches = matches && SpawnBiomeMatcher.contains(b, type);
                    if (!matches) break;
                }
                if (matches) this.validBiomes.add(b.getRegistryName());
            }
        final Set<ResourceLocation> toRemove = Sets.newHashSet();
        if (!blackListTypes.isEmpty()) for (final Biome b : SpawnBiomeMatcher.getAllBiomes())
            if (b != null && !this.blackListBiomes.contains(b))
            {
                boolean matches = false;
                for (final BiomeDictionary.Type type : blackListTypes)
                {
                    matches = matches || SpawnBiomeMatcher.contains(b, type);
                    if (matches) break;
                }
                if (matches)
                {
                    toRemove.add(b.getRegistryName());
                    this.blackListBiomes.add(b.getRegistryName());
                }
            }
        this.validBiomes.removeAll(toRemove);
        if (hasForgeTypes && this.validBiomes.isEmpty()) this.valid = false;
        if (this.validSubBiomes.isEmpty() && this.validBiomes.isEmpty() && this.blackListBiomes.isEmpty()
                && this.blackListSubBiomes.isEmpty()) this.valid = false;
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
                if (subBiome == null && SpawnBiomeMatcher.getBiomeType(s) == null) BiomeType.getBiome(s.trim(), true);
            }
        }
    }

    public void reset()
    {
        this.validBiomes.clear();
        this.validSubBiomes.clear();
        this.blackListBiomes.clear();
        this.blackListSubBiomes.clear();
        this.parsed = false;
        this.valid = true;
        for (final SpawnBiomeMatcher child : this.children)
            child.reset();
    }
}
