package pokecube.core.blocks.berries;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.function.BiFunction;
import java.util.function.Predicate;

import javax.xml.namespace.QName;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mojang.serialization.Codec;

import net.minecraft.core.BlockPos;
import net.minecraft.core.RegistryAccess;
import net.minecraft.data.worldgen.Pools;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.GenerationStep.Decoration;
import net.minecraft.world.level.levelgen.LegacyRandomSource;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.feature.StructureFeature;
import net.minecraft.world.level.levelgen.feature.structures.StructurePoolElement;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.PoolElementStructurePiece;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.levelgen.structure.pieces.PieceGenerator;
import net.minecraft.world.level.levelgen.structure.pieces.PieceGeneratorSupplier.Context;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePiecesBuilder;
import net.minecraft.world.level.levelgen.structure.templatesystem.AlwaysTrueTest;
import net.minecraft.world.level.levelgen.structure.templatesystem.ProcessorRule;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureManager;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate.StructureBlockInfo;
import net.minecraft.world.level.levelgen.structure.templatesystem.TagMatchTest;
import net.minecraft.world.level.material.Material;
import net.minecraftforge.common.Tags;
import net.minecraftforge.event.RegistryEvent;
import pokecube.core.PokecubeCore;
import pokecube.core.database.pokedex.PokedexEntryLoader;
import pokecube.core.database.pokedex.PokedexEntryLoader.SpawnRule;
import pokecube.core.database.resources.PackFinder;
import pokecube.core.database.spawns.SpawnBiomeMatcher;
import pokecube.core.database.spawns.SpawnCheck;
import pokecube.core.database.worldgen.WorldgenHandler;
import pokecube.core.database.worldgen.WorldgenHandler.JigSawConfig;
import pokecube.core.database.worldgen.WorldgenHandler.JigSawPool;
import pokecube.core.handlers.ItemGenerator;
import pokecube.core.interfaces.PokecubeMod;
import pokecube.core.items.berries.BerryManager;
import pokecube.core.world.gen.WorldgenFeatures;
import pokecube.core.world.gen.jigsaw.CustomJigsawPiece;
import pokecube.core.world.gen.jigsaw.JigsawAssmbler;
import pokecube.core.world.gen.jigsaw.JigsawConfig;
import pokecube.core.world.gen.template.NotRuleProcessor;
import pokecube.core.world.terrain.PokecubeTerrainChecker;
import thut.api.maths.Vector3;

public class BerryGenManager
{
    static class Berries
    {
        public List<BerryPool> pools = Lists.newArrayList();
        public List<BerryJigsaw> jigsaws = Lists.newArrayList();
    }

    static class BerryJigsaw extends JigSawConfig
    {
        List<String> trees = Lists.newArrayList();
        boolean onGrow = false;
    }

    static class BerryPool extends JigSawPool
    {}

    public static final String DATABASES = "database/berries/";

    public static final ResourceLocation REPLACETAG = new ResourceLocation("pokecube:berry_tree_replace");

    public static final ProcessorRule REPLACEABLEONLY = new ProcessorRule(AlwaysTrueTest.INSTANCE,
            new TagMatchTest(BlockTags.getAllTags().getTagOrEmpty(BerryGenManager.REPLACETAG)),
            Blocks.STRUCTURE_VOID.defaultBlockState());

    public static final NotRuleProcessor NOREPLACE = new NotRuleProcessor(
            ImmutableList.of(BerryGenManager.REPLACEABLEONLY));

    public String MODID = PokecubeCore.MODID;
    public ResourceLocation ROOT = new ResourceLocation(PokecubeCore.MODID, "structures/");
    public Berries defaults;

    public BerryGenManager()
    {}

    public BerryGenManager(final String modid)
    {
        this.MODID = modid;
        this.ROOT = new ResourceLocation(this.MODID, "structures/");
    }

    public void loadStructures() throws Exception
    {
        final ResourceLocation json = new ResourceLocation(this.ROOT.toString() + "berry_trees.json");
        final InputStream res = PackFinder.getStream(json);
        final Reader reader = new InputStreamReader(res);
        this.defaults = PokedexEntryLoader.gson.fromJson(reader, Berries.class);
    }

    public void processStructures(final RegistryEvent.Register<StructureFeature<?>> event)
    {
        try
        {
            this.loadStructures();
        }
        catch (final Exception e)
        {
            if (e instanceof FileNotFoundException)
                PokecubeMod.LOGGER.warn("No Berry tree database found for " + this.MODID);
            else PokecubeMod.LOGGER.catching(e);
            return;
        }

        PokecubeMod.LOGGER.debug("=========Adding Berry Trees=========");

        final WorldgenHandler handler = WorldgenHandler.INSTANCE;

        // // Initialize the pools, applying our extra values
        for (final BerryPool pool : this.defaults.pools)
            // TODO find out why this doesn't work with BERRYLIST...
            handler.patterns.put(pool.name, WorldgenFeatures.register(pool, WorldgenFeatures.GENERICLIST));

        // Register the jigsaws
        for (final BerryJigsaw struct : this.defaults.jigsaws)
        {
            // This handles registering the structure itself
            handler.register(struct, event);
            // Now we handle registering it as a tree if needed.
            if (struct.onGrow)
            {
                final JigsawGrower default_grower = new JigsawGrower(JigsawConfig.CODEC, struct);
                for (final String berry : struct.trees)
                {
                    if (!BerryManager.byName.containsKey(berry))
                    {
                        PokecubeMod.LOGGER.warn("No Berry found for " + berry);
                        continue;
                    }
                    final int index = BerryManager.byName.get(berry).type.index;
                    BerryGenManager.trees.put(index, default_grower);
                    PokecubeMod.LOGGER.debug("Adding berry tree for {}", berry);
                }
            }
        }
        PokecubeMod.LOGGER.debug("==========Done Berry Trees==========");
    }

    private static class BerryGenList
    {
        List<BerrySpawn> locations = Lists.newArrayList();
    }

    private static class BerrySpawn
    {
        public List<SpawnRule> spawn;
        public String berry;
    }

    public static class JigsawGrower extends StructureFeature<JigsawConfig> implements TreeGrower
    {
        public static BiFunction<PieceGenerator.Context<JigsawConfig>, List<StructurePiece>, Boolean> validTreePlacement(
                BoundingBox bounds, Random rand)
        {
            return (context, parts_in) ->
            {
                final List<CustomJigsawPiece> parts = Lists.newArrayList();
                boolean valid = true;
                outer:
                for (final StructurePiece part : parts_in) if (part instanceof PoolElementStructurePiece)
                {
                    final PoolElementStructurePiece p = (PoolElementStructurePiece) part;
                    if (p.getElement() instanceof CustomJigsawPiece)
                    {
                        final BlockPos pos = p.getPosition();
                        final CustomJigsawPiece piece = (CustomJigsawPiece) p.getElement();
                        parts.add(piece);
                        final StructureTemplate t = piece.getTemplate(context.structureManager());
                        valid = true;

                        // TODO find out why this sometimes fails
                        final StructurePlaceSettings settings = piece.getSettings(p.getRotation(), bounds, false);

                        // DOLATER find out what the second block pos is
                        final List<StructureBlockInfo> list = StructureTemplate.processBlockInfos(
                                (LevelAccessor) context.heightAccessor(), pos, pos, settings,
                                settings.getRandomPalette(t.palettes, pos).blocks(), t);

                        for (final StructureBlockInfo i : list)
                            if (i != null && i.state != null && !(i.state.getBlock() == Blocks.JIGSAW
                                    || i.state.getBlock() == Blocks.STRUCTURE_VOID))
                        {
                            final BlockState state = ((LevelAccessor) context.heightAccessor()).getBlockState(i.pos);

                            // TODO better way to find the crop
                            if (state.getBlock() instanceof BerryCrop) continue;

                            if (state.getBlock() != Blocks.AIR)
                                valid = BerryGenManager.REPLACEABLEONLY.test(state, state, pos, pos, pos, rand);
                            if (!valid) break outer;
                        }
                    }
                }
                return valid;
            };
        }

        private Predicate<StructurePoolElement> isValid(final String type)
        {
            return (j) ->
            {
                if (!(j instanceof CustomJigsawPiece)) return true;
                final CustomJigsawPiece p = (CustomJigsawPiece) j;
                if (p.opts.flag.isEmpty()) return true;
                return p.opts.flag.equals(type);
            };
        }

        final BerryJigsaw jigsaw;

        public JigsawGrower(final Codec<JigsawConfig> codec, final BerryJigsaw jigsaw)
        {
            super(codec, (context) ->
            {
                JigsawConfig config = context.config();

                boolean validContext = true;

                if (!validContext)
                {
                    return Optional.empty();
                }
                else
                {
                    Pools.bootstrap();
                    final JigsawAssmbler assembler = new JigsawAssmbler(config.struct_config);
                    return assembler.build(context, (a, b) ->
                    {});
                }
            });
            this.jigsaw = jigsaw;
        }

        @Override
        public Decoration step()
        {
            return Decoration.SURFACE_STRUCTURES;
        }

        @Override
        public void growTree(final ServerLevel world, final BlockPos cropPos, final int berryId)
        {
            final int chunkX = cropPos.getX() >> 4;
            final int chunkZ = cropPos.getZ() >> 4;

            int xMin, yMin, zMin, xMax, yMax, zMax;

            xMin = cropPos.getX() - 32;
            yMin = cropPos.getY() - 32;
            zMin = cropPos.getZ() - 32;
            xMax = cropPos.getX() + 32;
            yMax = cropPos.getY() + 32;
            zMax = cropPos.getZ() + 32;
            for (int i = -2; i <= 2; i++) for (int j = -2; j <= 2; j++) world.getChunk(i + chunkX, j + chunkZ);
            final BoundingBox bounds = new BoundingBox(xMin, yMin, zMin, xMax, yMax, zMax);

            final JigsawAssmbler assembler = new JigsawAssmbler(jigsaw);

            ChunkGenerator gen = world.getChunkSource().getGenerator();
            BiomeSource biomes = gen.getBiomeSource();
            long seed = world.getSeed();
            ChunkPos pos = new ChunkPos(chunkX, chunkZ);
            JigsawConfig config = new JigsawConfig(jigsaw);
            LevelHeightAccessor heightAccessor = world;
            Predicate<Biome> validBiome = b -> true;
            StructureManager structureManager = world.getStructureManager();
            RegistryAccess registryAccess = world.registryAccess();

            Context<JigsawConfig> context = new Context<JigsawConfig>(gen, biomes, seed, pos, config, heightAccessor,
                    validBiome, structureManager, registryAccess);

            WorldgenRandom rand = new WorldgenRandom(new LegacyRandomSource(0L));
            rand.setLargeFeatureSeed(seed, pos.x, pos.z);

            Optional<PieceGenerator<JigsawConfig>> made = assembler.build(context, (a, b) ->
            {}, cropPos, cropPos.getY(), this.isValid(BerryManager.berryNames.get(berryId)));

            if (made.isPresent())
            {
                PieceGenerator<JigsawConfig> gener = made.get();
                StructurePiecesBuilder builder = new StructurePiecesBuilder();
                PieceGenerator.Context<JigsawConfig> newcontext = new PieceGenerator.Context<JigsawConfig>(config, gen,
                        structureManager, pos, heightAccessor, rand, seed);
                gener.generatePieces(builder, newcontext);
                StructureStart<?> start = new StructureStart<>(this, pos, cropPos.getY(), builder.build());

                if (validTreePlacement(bounds, rand).apply(newcontext, start.getPieces()))
                {
                    start.placeInChunk(world, world.structureFeatureManager(), gen, rand, bounds, pos);
                }
            }
        }
    }

    public static class GenericGrower implements TreeGrower
    {
        final BlockState wood;

        public GenericGrower(final BlockState trunk)
        {
            if (trunk == null) this.wood = Blocks.OAK_LOG.defaultBlockState();
            else this.wood = trunk;
        }

        @Override
        public void growTree(final ServerLevel world, final BlockPos pos, final int berryId)
        {
            final int l = world.random.nextInt(1) + 6;
            boolean flag = true;
            BlockPos temp;
            final int y = pos.getY();
            final int z = pos.getZ();
            final int x = pos.getX();
            if (y >= 1 && y + l + 1 <= world.getMaxBuildHeight())
            {
                int i1;
                byte b0;
                int j1;
                int k1;

                for (i1 = y; i1 <= y + 1 + l; ++i1)
                {
                    b0 = 1;

                    if (i1 == y) b0 = 0;

                    if (i1 >= y + 1 + l - 2) b0 = 2;

                    for (int l1 = x - b0; l1 <= x + b0 && flag; ++l1)
                        for (j1 = z - b0; j1 <= z + b0 && flag; ++j1) if (i1 >= 0 && i1 < world.getMaxBuildHeight())
                    {
                        temp = new BlockPos(l1, i1, j1);
                        final BlockState state = world.getBlockState(temp);
                        final Block block = world.getBlockState(temp).getBlock();
                        if (!world.isEmptyBlock(temp) && !PokecubeTerrainChecker.isLeaves(world.getBlockState(temp))
                                && block != Blocks.GRASS && block != Blocks.DIRT
                                && !PokecubeTerrainChecker.isWood(state))
                            flag = false;
                    }
                        else flag = false;
                }
                flag = true;
                if (!flag) return;
                temp = pos.below();
                BlockState state = world.getBlockState(temp);
                // final Block soil = state.getBlock();
                final boolean isSoil = true;
                // TODO This used to check canSustainPlant, maybe we should?

                if (isSoil && y < world.getMaxBuildHeight() - l - 1)
                {
                    // This is what onPlantGrow did.
                    if (state.is(Tags.Blocks.DIRT)) world.setBlock(pos, Blocks.DIRT.defaultBlockState(), 2);

                    b0 = 3;
                    final byte b1 = 0;
                    int i2;
                    int j2;
                    int k2;

                    for (j1 = y - b0 + l; j1 <= y + l; ++j1)
                    {
                        k1 = j1 - (y + l);
                        i2 = b1 + 1 - k1 / 2;

                        for (j2 = x - i2; j2 <= x + i2; ++j2)
                        {
                            k2 = j2 - x;

                            for (int l2 = z - i2; l2 <= z + i2; ++l2)
                            {
                                final int i3 = l2 - z;

                                if (Math.abs(k2) != i2 || Math.abs(i3) != i2 || world.random.nextInt(2) != 0 && k1 != 0)
                                {
                                    temp = new BlockPos(j2, j1, l2);
                                    final BlockState block = world.getBlockState(temp);
                                    // TODO check if berry leaves don't properly
                                    // replace valid things!
                                    if (block == null || block.isSolidRender(world, temp)) if (world.isEmptyBlock(temp))
                                        BerryGenManager.placeBerryLeaf(world, temp, berryId);
                                }
                            }
                        }
                    }

                    world.setBlockAndUpdate(pos, this.wood);
                    for (j1 = 0; j1 < l; ++j1)
                    {
                        temp = new BlockPos(x, y + j1, z);
                        state = world.getBlockState(temp);
                        final Block block = state.getBlock();

                        if (block == null || world.getBlockState(temp).isAir()
                                || PokecubeTerrainChecker.isLeaves(world.getBlockState(temp))
                                        && state.getMaterial() == Material.LEAVES)
                            world.setBlockAndUpdate(temp, this.wood);
                    }
                }
            }
        }
    }

    public static class PalmGrower implements TreeGrower
    {
        final BlockState wood;

        public PalmGrower(final BlockState trunk)
        {
            if (trunk == null) this.wood = Blocks.JUNGLE_LOG.defaultBlockState();
            else this.wood = trunk;
        }

        @Override
        public void growTree(final ServerLevel world, final BlockPos pos, final int berryId)
        {
            final int l = world.random.nextInt(1) + 5;
            BlockPos temp;
            if (pos.getY() >= 1 && pos.getY() + l + 1 <= world.getMaxBuildHeight())
            {
                boolean stopped = false;
                // Trunk
                world.setBlockAndUpdate(pos, this.wood);
                for (int i = 1; i < l; i++)
                    if (world.isEmptyBlock(temp = pos.above(i))) world.setBlockAndUpdate(temp, this.wood);
                    else
                {
                    stopped = true;
                    break;
                }

                if (!stopped)
                {
                    int d = 0;
                    int d1 = 0;
                    for (int i = 0; i <= l / 1.5; i++)
                    {
                        d = i / 3;
                        temp = new BlockPos(pos.getX(), pos.getY() + l - d, pos.getZ() + i);

                        if (world.isEmptyBlock(temp)) BerryGenManager.placeBerryLeaf(world, temp, berryId);
                        else if (i != 0)
                        {
                            stopped = true;
                            break;
                        }
                        if (d1 != d)
                        {
                            temp = new BlockPos(pos.getX(), pos.getY() + l - d, pos.getZ() + i - 1);
                            if (world.isEmptyBlock(temp)) BerryGenManager.placeBerryLeaf(world, temp, berryId);
                        }

                        d1 = d;
                    }
                    d1 = 0;
                    for (int i = 0; i <= l / 1.5; i++)
                    {
                        d = i / 3;
                        temp = new BlockPos(pos.getX(), pos.getY() + l - d, pos.getZ() - i);
                        if (world.isEmptyBlock(temp)) BerryGenManager.placeBerryLeaf(world, temp, berryId);
                        else if (i != 0)
                        {
                            stopped = true;
                            break;
                        }
                        if (d1 != d)
                        {
                            temp = new BlockPos(pos.getX(), pos.getY() + l - d, pos.getZ() - i + 1);
                            if (world.isEmptyBlock(temp)) BerryGenManager.placeBerryLeaf(world, temp, berryId);
                        }
                        d1 = d;
                    }
                    d1 = 0;
                    for (int i = 0; i <= l / 1.5; i++)
                    {
                        d = i / 3;
                        temp = new BlockPos(pos.getX() + i, pos.getY() + l - d, pos.getZ());
                        if (world.isEmptyBlock(temp)) BerryGenManager.placeBerryLeaf(world, temp, berryId);
                        else if (i != 0)
                        {
                            stopped = true;
                            break;
                        }
                        if (d1 != d)
                        {
                            temp = new BlockPos(pos.getX() + i - 1, pos.getY() + l - d, pos.getZ());
                            if (world.isEmptyBlock(temp)) BerryGenManager.placeBerryLeaf(world, temp, berryId);
                        }
                        d1 = d;
                    }
                    d1 = 0;
                    for (int i = 0; i <= l / 1.5; i++)
                    {
                        d = i / 3;
                        temp = new BlockPos(pos.getX() - i, pos.getY() + l - d, pos.getZ());

                        if (world.isEmptyBlock(temp)) BerryGenManager.placeBerryLeaf(world, temp, berryId);
                        else if (i != 0)
                        {
                            stopped = true;
                            break;
                        }
                        if (d1 != d)
                        {
                            temp = new BlockPos(pos.getX() - i + 1, pos.getY() + l - d, pos.getZ());
                            if (world.isEmptyBlock(temp)) BerryGenManager.placeBerryLeaf(world, temp, berryId);
                        }
                        d1 = d;
                    }
                }
            }
        }
    }

    public static interface TreeGrower
    {
        void growTree(ServerLevel world, BlockPos cropPos, int berryId);
    }

    public static HashMap<Integer, TreeGrower> trees = Maps.newHashMap();

    public static Map<SpawnBiomeMatcher, List<ItemStack>> berryLocations = Maps.newHashMap();

    private static List<SpawnBiomeMatcher> matchers = Lists.newArrayList();

    private static BerryGenList list;

    private static final QName prior = new QName("priority");

    private static final Comparator<SpawnBiomeMatcher> COMPARE = (o1, o2) ->
    {
        Integer p1 = 50;
        Integer p2 = 50;
        if (o1.spawnRule.values.containsKey(BerryGenManager.prior))
            p1 = Integer.parseInt(o1.spawnRule.values.get(BerryGenManager.prior));
        if (o2.spawnRule.values.containsKey(BerryGenManager.prior))
            p2 = Integer.parseInt(o2.spawnRule.values.get(BerryGenManager.prior));
        return p1.compareTo(p2);
    };

    public static ItemStack getRandomBerryForBiome(final Level world, final BlockPos location)
    {
        SpawnBiomeMatcher toMatch = null;
        final SpawnCheck checker = new SpawnCheck(Vector3.getNewVector().set(location), world);
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

    private static void loadConfig()
    {
        BerryGenManager.list = new BerryGenList();
        final Collection<ResourceLocation> resources = PackFinder.getJsonResources(BerryGenManager.DATABASES);
        for (final ResourceLocation s : resources) try
        {
            BerryGenList loaded;
            final Reader reader = new InputStreamReader(PackFinder.getStream(s));
            loaded = PokedexEntryLoader.gson.fromJson(reader, BerryGenList.class);
            reader.close();
            BerryGenManager.list.locations.addAll(loaded.locations);
        }
        catch (final FileNotFoundException e1)
        {
            PokecubeCore.LOGGER.debug("No berry spawns list {} found.", s);
        }
        catch (final Exception e)
        {
            PokecubeCore.LOGGER.error("Error loading Berries Spawn Database " + s, e);
        }
    }

    public static void parseConfig()
    {
        BerryGenManager.berryLocations.clear();
        BerryGenManager.matchers.clear();
        if (BerryGenManager.list == null) BerryGenManager.loadConfig();
        if (BerryGenManager.list != null)
            for (final BerrySpawn rule : BerryGenManager.list.locations) for (final SpawnRule spawn : rule.spawn)
        {
            final SpawnBiomeMatcher matcher = new SpawnBiomeMatcher(spawn);
            final List<ItemStack> berries = Lists.newArrayList();
            for (final String s : rule.berry.split(","))
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
            berries.add(new ItemStack(BerryManager.berryItems.get(0)));
            BerryGenManager.matchers.add(matcher);
            BerryGenManager.berryLocations.put(matcher, berries);
        }
        if (!BerryGenManager.matchers.isEmpty()) BerryGenManager.matchers.sort(BerryGenManager.COMPARE);
    }

    public static void placeBerryLeaf(final Level world, final BlockPos pos, final int berryId)
    {
        final String id = BerryManager.berryNames.get(berryId);
        Block leaves = ItemGenerator.leaves.get(id);
        if (leaves == null)
        {
            PokecubeCore.LOGGER.error("Trying to make leaves for unregistered berry: " + id);
            leaves = Blocks.OAK_LEAVES;
        }
        world.setBlockAndUpdate(pos, leaves.defaultBlockState());
    }

}
