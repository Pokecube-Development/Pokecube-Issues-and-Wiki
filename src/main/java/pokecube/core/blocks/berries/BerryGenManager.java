package pokecube.core.blocks.berries;

import java.io.FileNotFoundException;
import java.io.Reader;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.grower.AbstractTreeGrower;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.structure.templatesystem.AlwaysTrueTest;
import net.minecraft.world.level.levelgen.structure.templatesystem.ProcessorRule;
import net.minecraft.world.level.levelgen.structure.templatesystem.TagMatchTest;
import pokecube.api.PokecubeAPI;
import pokecube.api.data.spawns.SpawnBiomeMatcher;
import pokecube.api.data.spawns.SpawnCheck;
import pokecube.api.data.spawns.SpawnRule;
import pokecube.core.PokecubeCore;
import pokecube.core.database.resources.PackFinder;
import pokecube.core.items.berries.BerryManager;
import pokecube.core.items.berries.ItemBerry;
import thut.api.maths.Vector3;
import thut.api.util.JsonUtil;
import thut.lib.RegHelper;
import thut.lib.ResourceHelper;

public class BerryGenManager
{
    public static class BerryConfig
    {
        public List<SpawnConfig> locations = Lists.newArrayList();
        public List<TreeConfig> trees = Lists.newArrayList();
    }

    public static class SpawnConfig
    {
        public List<SpawnRule> spawn;
        public String berry;
    }

    private static class TreeConfig
    {
        public String berry;
        public String tree;
        public int weight = 1;
    }

    private static class TreeProvider extends AbstractTreeGrower implements Supplier<AbstractTreeGrower>
    {
        public List<ResourceLocation> trees = Lists.newArrayList();
        private ServerLevel level;

        public void init(ServerLevel worldIn, RandomSource random)
        {
            this.level = worldIn;
        }

        @Override
        public AbstractTreeGrower get()
        {
            return this;
        }

        @Override
        protected Holder<? extends ConfiguredFeature<?, ?>> getConfiguredFeature(RandomSource rand, boolean has_flowers)
        {
            var reg = level.registryAccess().registryOrThrow(RegHelper.CONFIGURED_FEATURE_REGISTRY);
            int rng = rand.nextInt(trees.size());
            ResourceLocation loc = trees.get(rng);
            return Holder.direct(reg.get(loc));
        }
    }

    public static final String DATABASES = "database/berries/";

    public static final ResourceLocation REPLACETAG = new ResourceLocation("pokecube:berry_tree_replace");

    public static final ProcessorRule REPLACEABLEONLY = new ProcessorRule(AlwaysTrueTest.INSTANCE,
            new TagMatchTest(TagKey.create(RegHelper.BLOCK_REGISTRY, BerryGenManager.REPLACETAG)),
            Blocks.STRUCTURE_VOID.defaultBlockState());

    private static Map<Integer, TreeProvider> trees = Maps.newHashMap();

    public static Map<SpawnBiomeMatcher, List<ItemStack>> berryLocations = Maps.newHashMap();

    private static List<SpawnBiomeMatcher> matchers = Lists.newArrayList();

    public static BerryConfig list = new BerryConfig();

    private static final String prior = "priority";

    private static final Comparator<SpawnBiomeMatcher> COMPARE = (o1, o2) -> {
        Integer p1 = 50;
        Integer p2 = 50;
        if (o1.spawnRule.values.containsKey(BerryGenManager.prior))
            p1 = Integer.parseInt(o1.spawnRule.getString(BerryGenManager.prior));
        if (o2.spawnRule.values.containsKey(BerryGenManager.prior))
            p2 = Integer.parseInt(o2.spawnRule.getString(BerryGenManager.prior));
        return p1.compareTo(p2);
    };

    public String MODID = PokecubeCore.MODID;
    public ResourceLocation ROOT = new ResourceLocation(PokecubeCore.MODID, "structures/");

    public BerryGenManager()
    {}

    public BerryGenManager(final String modid)
    {
        this.MODID = modid;
        this.ROOT = new ResourceLocation(this.MODID, "structures/");
    }

    public static void parseConfig()
    {
        BerryGenManager.berryLocations.clear();
        BerryGenManager.matchers.clear();
        BerryGenManager.loadBerrySpawns();
        if (BerryGenManager.list != null)
            for (final SpawnConfig rule : BerryGenManager.list.locations) for (final SpawnRule spawn : rule.spawn)
        {
            final SpawnBiomeMatcher matcher = SpawnBiomeMatcher.get(spawn);
            final List<ItemStack> berries = Lists.newArrayList();
            if (rule.berry != null) for (final String s : rule.berry.split(","))
            {
                final Item berry = BerryManager.getBerryItem(s.trim());
                if (berry != null) berries.add(new ItemStack(berry));
            }
            if (!berries.isEmpty())
            {
                BerryGenManager.matchers.add(matcher);
                BerryGenManager.berryLocations.put(matcher, berries);
            }
        }
        if (BerryGenManager.berryLocations.isEmpty() && PokecubeCore.getConfig().autoAddNullBerries)
        {
            final SpawnBiomeMatcher matcher = SpawnBiomeMatcher.ALLMATCHER;
            matcher.reset();
            final List<ItemStack> berries = Lists.newArrayList();
            berries.add(new ItemStack(BerryManager.berryItems.get(0).get()));
            BerryGenManager.matchers.add(matcher);
            BerryGenManager.berryLocations.put(matcher, berries);
        }
        if (!BerryGenManager.matchers.isEmpty()) BerryGenManager.matchers.sort(BerryGenManager.COMPARE);
    }

    public static boolean isTree(int index)
    {
        if (trees.isEmpty() && !list.trees.isEmpty())
        {
            list.trees.forEach(conf -> {
                ItemBerry item = BerryManager.byName.get(conf.berry).get();
                if (item != null)
                {
                    int id = item.type.index;
                    TreeProvider prov = trees.computeIfAbsent(id, (i) -> {
                        TreeProvider t = new TreeProvider();
                        return t;
                    });
                    ResourceLocation loc = new ResourceLocation(conf.tree);
                    if (!prov.trees.contains(loc)) for (int i = 0; i < conf.weight; i++) prov.trees.add(loc);
                }
            });
        }
        return trees.containsKey(index);
    }

    public static Supplier<AbstractTreeGrower> getTree(ServerLevel worldIn, RandomSource random, int index)
    {
        if (!isTree(index)) return null;
        TreeProvider prov = trees.get(index);
        prov.init(worldIn, random);
        return prov;
    }

    private static void loadBerrySpawns()
    {
        BerryGenManager.list = new BerryConfig();
        final Map<ResourceLocation, Resource> resources = PackFinder.getJsonResources(BerryGenManager.DATABASES);
        resources.forEach((s, resource) -> {
            try
            {
                BerryConfig loaded;
                final Reader reader = ResourceHelper.getReader(resource);
                if (reader == null) throw new FileNotFoundException(s.toString());
                loaded = JsonUtil.gson.fromJson(reader, BerryConfig.class);
                reader.close();
                BerryGenManager.list.locations.addAll(loaded.locations);
                BerryGenManager.list.trees.addAll(loaded.trees);
            }
            catch (final FileNotFoundException e1)
            {
                PokecubeAPI.logDebug("No berry spawns list {} found.", s);
            }
            catch (final Exception e)
            {
                PokecubeAPI.LOGGER.error("Error loading Berries Spawn Database " + s, e);
            }
        });
    }

    public static ItemStack getRandomBerryForBiome(final Level world, final BlockPos location)
    {
        if (!(world instanceof ServerLevel level))
        {
            PokecubeAPI.LOGGER.error("Warning, calling getRandomBerryForBiome on wrong side!");
            PokecubeAPI.LOGGER.error(new IllegalAccessError());
            return ItemStack.EMPTY;
        }
        SpawnBiomeMatcher toMatch = null;
        final SpawnCheck checker = new SpawnCheck(new Vector3().set(location), level);
        /**
         * Shuffle list, then re-sort it. This allows the values of the same
         * priority to be randomized, but then still respect priority order for
         * specific ones.
         */
        Collections.shuffle(BerryGenManager.matchers);
        BerryGenManager.matchers.sort(BerryGenManager.COMPARE);
        for (final SpawnBiomeMatcher matcher : BerryGenManager.matchers) if (matcher.matches(checker))
        {
            toMatch = matcher;
            break;
        }
        if (toMatch == null) return ItemStack.EMPTY;
        final List<ItemStack> options = BerryGenManager.berryLocations.get(toMatch);
        if (options == null || options.isEmpty()) return ItemStack.EMPTY;
        final ItemStack ret = options.get(world.random.nextInt(options.size())).copy();
        final int size = 1 + world.random.nextInt(PokecubeCore.getConfig().berryStackScale);
        ret.setCount(size);
        return ret;
    }
}
