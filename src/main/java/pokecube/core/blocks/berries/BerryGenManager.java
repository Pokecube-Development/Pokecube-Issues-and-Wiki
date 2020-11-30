package pokecube.core.blocks.berries;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.function.BiPredicate;
import java.util.function.Consumer;

import javax.xml.namespace.QName;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.material.Material;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MutableBoundingBox;
import net.minecraft.util.registry.DynamicRegistries;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.ChunkGenerator;
import net.minecraft.world.gen.feature.jigsaw.JigsawPiece;
import net.minecraft.world.gen.feature.structure.MarginedStructureStart;
import net.minecraft.world.gen.feature.structure.Structure;
import net.minecraft.world.gen.feature.template.AlwaysTrueRuleTest;
import net.minecraft.world.gen.feature.template.RuleEntry;
import net.minecraft.world.gen.feature.template.TagMatchRuleTest;
import net.minecraft.world.gen.feature.template.TemplateManager;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.event.RegistryEvent;
import pokecube.core.PokecubeCore;
import pokecube.core.database.Database;
import pokecube.core.database.Database.EnumDatabase;
import pokecube.core.database.PokedexEntryLoader;
import pokecube.core.database.PokedexEntryLoader.SpawnRule;
import pokecube.core.database.SpawnBiomeMatcher;
import pokecube.core.database.SpawnBiomeMatcher.SpawnCheck;
import pokecube.core.database.worldgen.WorldgenHandler.JigSawConfig;
import pokecube.core.database.worldgen.WorldgenHandler.JigSawPool;
import pokecube.core.handlers.ItemGenerator;
import pokecube.core.interfaces.PokecubeMod;
import pokecube.core.items.berries.BerryManager;
import pokecube.core.world.gen.jigsaw.JigsawConfig;
import pokecube.core.world.gen.jigsaw.JigsawPatternCustom;
import pokecube.core.world.gen.template.NotRuleProcessor;
import pokecube.core.world.terrain.PokecubeTerrainChecker;
import thut.api.maths.Vector3;

public class BerryGenManager
{
    static class Berries
    {
        public List<BerryPool>   pools   = Lists.newArrayList();
        public List<BerryJigsaw> jigsaws = Lists.newArrayList();
    }

    static class BerryJigsaw extends JigSawConfig
    {
        List<String> trees  = Lists.newArrayList();
        boolean      onGrow = false;
    }

    static class BerryPool extends JigSawPool
    {
    }

    public static final ResourceLocation REPLACETAG      = new ResourceLocation("pokecube:berry_tree_replace");
    public static final RuleEntry        REPLACEABLEONLY = new RuleEntry(AlwaysTrueRuleTest.INSTANCE,
            new TagMatchRuleTest(BlockTags.getCollection().getTagByID(BerryGenManager.REPLACETAG)),
            Blocks.STRUCTURE_VOID.getDefaultState());

    public static final NotRuleProcessor NOREPLACE = new NotRuleProcessor(ImmutableList.of(
            BerryGenManager.REPLACEABLEONLY));

    public String           MODID = PokecubeCore.MODID;
    public ResourceLocation ROOT  = new ResourceLocation(PokecubeCore.MODID, "structures/");
    public Berries          defaults;

    public BerryGenManager()
    {
    }

    public BerryGenManager(final String modid)
    {
        this.MODID = modid;
        this.ROOT = new ResourceLocation(this.MODID, "structures/");
    }

    public void loadStructures() throws Exception
    {
        final ResourceLocation json = new ResourceLocation(this.ROOT.toString() + "berry_trees.json");
        final InputStream res = Database.resourceManager.getResource(json).getInputStream();
        final Reader reader = new InputStreamReader(res);
        this.defaults = PokedexEntryLoader.gson.fromJson(reader, Berries.class);
    }

    public void processStructures(final RegistryEvent.Register<Structure<?>> event)
    {
        try
        {
            this.loadStructures();
        }
        catch (final Exception e)
        {
            if (e instanceof FileNotFoundException) PokecubeMod.LOGGER.warn("No Berry tree database found for "
                    + this.MODID);
            else PokecubeMod.LOGGER.catching(e);
            return;
        }

        PokecubeMod.LOGGER.debug("=========Adding Berry Trees=========");

        // // Initialize the pools, applying our extra values
//        for (final BerryPool pool : this.defaults.pools)
//            WorldgenFeatures.register(pool, WorldgenFeatures.BERRYLIST);

        // Register the jigsaws
//        for (final BerryJigsaw struct : this.defaults.jigsaws)
//        {
//            WorldgenHandler.WORLDGEN.getOrDefault(this.MODID, WorldgenHandler.WORLDGEN.get(PokecubeCore.MODID))
//                    .register(struct, event);
//            final JigsawGrower default_grower = new JigsawGrower(struct);
//            if (struct.onGrow) for (final String berry : struct.trees)
//            {
//                if (!BerryManager.byName.containsKey(berry))
//                {
//                    PokecubeMod.LOGGER.warn("No Berry found for " + berry);
//                    continue;
//                }
//                final int index = BerryManager.byName.get(berry).type.index;
//                BerryGenManager.trees.put(index, default_grower);
//                PokecubeMod.LOGGER.debug("Adding berry tree for {}", berry);
//            }
//        }
        PokecubeMod.LOGGER.debug("==========Done Berry Trees==========");
    }

    private static class BerryGenList
    {
        List<BerrySpawn> locations = Lists.newArrayList();
    }

    private static class BerrySpawn
    {
        public List<SpawnRule> spawn;
        public String          berry;
    }

    public static class JigsawGrower implements TreeGrower
    {
        final BerryJigsaw jigsaw;

        public JigsawGrower(final BerryJigsaw jigsaw)
        {
            this.jigsaw = jigsaw;
        }

        @Override
        public void growTree(final ServerWorld world, final BlockPos cropPos, final int berryId)
        {
            Structure.init();
            final int chunkX = cropPos.getX() >> 4;
            final int chunkZ = cropPos.getZ() >> 4;
            final Biome biome = world.getBiome(cropPos);
            int xMin, yMin, zMin, xMax, yMax, zMax;

            xMin = cropPos.getX() - 32;
            yMin = 0;
            zMin = cropPos.getZ() - 32;
            xMax = cropPos.getX() + 32;
            yMax = world.getHeight();
            zMax = cropPos.getZ() + 32;
            for (int i = -2; i <= 2; i++)
                for (int j = -2; j <= 2; j++)
                    world.getChunk(i + chunkX, j + chunkZ);
            final MutableBoundingBox bounds = new MutableBoundingBox(xMin, yMin, zMin, xMax, yMax, zMax);
            final Start make = new Start(null, chunkX, chunkZ, bounds, 0, cropPos.toLong() + world.getSeed() + world
                    .getGameTime());
            final Random rand = make.build(world, biome, this, cropPos, berryId);
            if (rand != null)
            {
                // boolean valid = true;
                // parts:
                // for (final StructurePiece part : make.getComponents())
                // if (part instanceof CustomVillagePiece)
                // {
                // final CustomVillagePiece p = (CustomVillagePiece) part;
                // if (p.getJigsawPiece() instanceof SingleOffsetPiece)
                // {
                // final BlockPos pos = p.getPos();
                // final SingleOffsetPiece piece = (SingleOffsetPiece)
                // p.getJigsawPiece();
                // final Template t =
                // piece.getTemplate(world.getStructureTemplateManager());
                // valid = true;
                // final PlacementSettings settings =
                // piece.createPlacementSettings(p.getRotation(), bounds);
                // settings.clearProcessors();
                // final List<BlockInfo> list = Template.processBlockInfos(t,
                // world, pos, settings, settings
                // .func_227459_a_(t.blocks, pos));
                // for (final BlockInfo i : list)
                // if (i != null && i.state != null && !(i.state.getBlock() ==
                // Blocks.JIGSAW || i.state
                // .getBlock() == Blocks.STRUCTURE_VOID))
                // {
                // final BlockState state = world.getBlockState(i.pos);
                // if (state != null && state.getBlock() != Blocks.AIR)
                // valid = BerryGenManager.REPLACEABLEONLY.test(null, state,
                // rand);
                // if (!valid) break parts;
                // }
                // }
                // }
                // if (valid)
                // {
                // world.setBlockState(cropPos, Blocks.AIR.getDefaultState());
                // make.generateStructure(world,
                // world.getChunkProvider().getChunkGenerator(), rand, make
                // .getBoundingBox(), new ChunkPos(chunkX, chunkZ));
                // }
            }
        }

        public static class Start extends MarginedStructureStart<JigsawConfig>
        {

            private static final Consumer<JigsawPatternCustom> pre  = p ->
                                                                    {
                                                                    };
            private static final Consumer<JigsawPatternCustom> post = p ->
                                                                    {
                                                                    };

            public Start(final Structure<JigsawConfig> structure, final int x, final int z,
                    final MutableBoundingBox box, final int refs, final long seed)
            {
                super(structure, x, z, box, refs, seed);
            }

            @Override
            public void func_230364_a_(final DynamicRegistries p_230364_1_, final ChunkGenerator p_230364_2_,
                    final TemplateManager p_230364_3_, final int p_230364_4_, final int p_230364_5_,
                    final Biome p_230364_6_, final JigsawConfig p_230364_7_)
            {
                // We do nothing, we do our own thing seperately.
            }

            public Random build(final ServerWorld world, final Biome biome, final JigsawGrower grower,
                    final BlockPos pos, final int berryId)
            {
                // final ResourceLocation key = new
                // ResourceLocation(grower.jigsaw.root);
                // final JigsawPattern jigsawpattern =
                // JigsawManager.REGISTRY.get(key);
                // JigsawPatternCustom root;
                // if (jigsawpattern instanceof JigsawPatternCustom) root =
                // (JigsawPatternCustom) jigsawpattern;
                // else
                // {
                // PokecubeCore.LOGGER.error("Berry crop got wrong type of
                // pattern for {}", key);
                // return null;
                // }
                // final ChunkGenerator chunkGenerator =
                // world.getChunkProvider().generator;
                // final TemplateManager manager =
                // world.getStructureTemplateManager();
                //
                // final JigsawAssmbler assembler = new JigsawAssmbler();
                // final BiPredicate<JigsawPiece, SpawnCheck> old =
                // root.validator;
                // final boolean respects_pools = root.respects_pool_spawns;
                // root.validator = this.isValid(berryId);
                // final Rotation rot = Rotation.randomRotation(this.rand);
                // assembler.build(root, rot, grower.jigsaw.size,
                // CustomVillagePiece::new, chunkGenerator, manager,
                // pos, this.getComponents(), this.rand, biome, Start.pre,
                // Start.post), pos.getY());
                // root.validator = old;
                // root.respects_pool_spawns = respects_pools;
                return this.rand;
            }

            private BiPredicate<JigsawPiece, SpawnCheck> isValid(final int berryId)
            {
                return (j, s) ->
                {
                    final boolean valid = berryId == -1;
                    // if (!valid && j instanceof SingleOffsetPiece)
                    // {
                    // final String value = ((SingleOffsetPiece) j).flag;
                    // final String key = BerryManager.berryNames.get(berryId);
                    // if (key != null) return key.equals(value);
                    // }
                    return valid;
                };
            }

        }

        // public static class Start extends MarginedStructureStart
        // {
        // final Biome biome;
        // public Rotation rot;
        //
        // public Start(final Structure<?> structureIn, final int chunkX, final
        // int chunkZ, final Biome biomeIn,
        // final MutableBoundingBox boundsIn, final int referenceIn, final long
        // seed)
        // {
        // super(structureIn, chunkX, chunkZ, boundsIn, referenceIn, seed);
        // this.biome = biomeIn;
        // }
        //
        // public Random build(final ServerWorld world, final JigsawGrower
        // grower, final BlockPos pos,
        // final int berryId)
        // {
        // final ResourceLocation key = new
        // ResourceLocation(grower.jigsaw.root);
        // final JigsawPattern jigsawpattern = JigsawManager.REGISTRY.get(key);
        // JigsawPatternCustom root;
        // if (jigsawpattern instanceof JigsawPatternCustom) root =
        // (JigsawPatternCustom) jigsawpattern;
        // else
        // {
        // PokecubeCore.LOGGER.error("Berry crop got wrong type of pattern for
        // {}", key);
        // return null;
        // }
        // final ChunkGenerator<?> chunkGenerator =
        // world.getChunkProvider().generator;
        // final TemplateManager manager = world.getStructureTemplateManager();
        //
        // final JigsawAssmbler assembler = new JigsawAssmbler();
        // final BiPredicate<JigsawPiece, SpawnCheck> old = root.validator;
        // final boolean respects_pools = root.respects_pool_spawns;
        // root.validator = this.isValid(berryId);
        // this.rot = Rotation.randomRotation(this.rand);
        // assembler.build(root, this.rot, grower.jigsaw.size,
        // CustomJigsawPiece::new, chunkGenerator, manager,
        // pos, this.getComponents(), this.rand, this.biome, this.pre(),
        // this.post(), pos.getY());
        // root.validator = old;
        // root.respects_pool_spawns = respects_pools;
        // return this.rand;
        // }
        //
        // private Consumer<JigsawPatternCustom> pre()
        // {
        // return p ->
        // {
        // };
        // }
        //
        // private Consumer<JigsawPatternCustom> post()
        // {
        // return p ->
        // {
        // };
        // }
        //
        // private BiPredicate<JigsawPiece, SpawnCheck> isValid(final int
        // berryId)
        // {
        // return (j, s) ->
        // {
        // final boolean valid = berryId == -1;
        // if (!valid && j instanceof SingleOffsetPiece)
        // {
        // final String value = ((SingleOffsetPiece) j).flag;
        // final String key = BerryManager.berryNames.get(berryId);
        // if (key != null) return key.equals(value);
        // }
        // return valid;
        // };
        // }
        //
        // @Override
        // public void init(final ChunkGenerator<?> generator, final
        // TemplateManager templateManagerIn,
        // final int chunkX, final int chunkZ, final Biome biomeIn)
        // {
        // // We do nothing here, as we work differently from normal
        // }
        //
        // }

    }

    public static class GenericGrower implements TreeGrower
    {
        final BlockState wood;

        public GenericGrower(final BlockState trunk)
        {
            if (trunk == null) this.wood = Blocks.OAK_LOG.getDefaultState();
            else this.wood = trunk;
        }

        @Override
        public void growTree(final ServerWorld world, final BlockPos pos, final int berryId)
        {
            final int l = world.rand.nextInt(1) + 6;
            boolean flag = true;
            BlockPos temp;
            final int y = pos.getY();
            final int z = pos.getZ();
            final int x = pos.getX();
            if (y >= 1 && y + l + 1 <= world.getHeight())
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
                        for (j1 = z - b0; j1 <= z + b0 && flag; ++j1)
                            if (i1 >= 0 && i1 < world.getHeight())
                            {
                                temp = new BlockPos(l1, i1, j1);
                                final BlockState state = world.getBlockState(temp);
                                final Block block = world.getBlockState(temp).getBlock();
                                if (!world.isAirBlock(temp) && !PokecubeTerrainChecker.isLeaves(world.getBlockState(
                                        temp)) && block != Blocks.GRASS && block != Blocks.DIRT
                                        && !PokecubeTerrainChecker.isWood(state)) flag = false;
                            }
                            else flag = false;
                }
                flag = true;
                if (!flag) return;
                temp = pos.down();
                final Block soil = world.getBlockState(temp).getBlock();
                final boolean isSoil = true;// (soil != null &&
                // soil.canSustainPlant(par1World,
                // par3,
                // par4 - 1, par5, Direction.UP,
                // (BlockSapling)Block.sapling));

                if (isSoil && y < world.getHeight() - l - 1)
                {
                    soil.onPlantGrow(world.getBlockState(temp), world, temp, pos);
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

                                if (Math.abs(k2) != i2 || Math.abs(i3) != i2 || world.rand.nextInt(2) != 0 && k1 != 0)
                                {
                                    temp = new BlockPos(j2, j1, l2);
                                    final Block block = world.getBlockState(temp).getBlock();

                                    if (block == null || block.canBeReplacedByLeaves(world.getBlockState(temp), world,
                                            temp)) if (world.isAirBlock(temp)) BerryGenManager.placeBerryLeaf(world,
                                                    temp, berryId);
                                }
                            }
                        }
                    }

                    world.setBlockState(pos, this.wood);
                    for (j1 = 0; j1 < l; ++j1)
                    {
                        temp = new BlockPos(x, y + j1, z);
                        final BlockState state = world.getBlockState(temp);
                        final Block block = state.getBlock();

                        if (block == null || block.isAir(world.getBlockState(temp), world, temp)
                                || PokecubeTerrainChecker.isLeaves(world.getBlockState(temp)) && state
                                        .getMaterial() == Material.LEAVES) world.setBlockState(temp, this.wood);
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
            if (trunk == null) this.wood = Blocks.JUNGLE_LOG.getDefaultState();
            else this.wood = trunk;
        }

        @Override
        public void growTree(final ServerWorld world, final BlockPos pos, final int berryId)
        {
            final int l = world.rand.nextInt(1) + 5;
            BlockPos temp;
            if (pos.getY() >= 1 && pos.getY() + l + 1 <= world.getHeight())
            {
                boolean stopped = false;
                // Trunk
                world.setBlockState(pos, this.wood);
                for (int i = 1; i < l; i++)
                    if (world.isAirBlock(temp = pos.up(i))) world.setBlockState(temp, this.wood);
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

                        if (world.isAirBlock(temp)) BerryGenManager.placeBerryLeaf(world, temp, berryId);
                        else if (i != 0)
                        {
                            stopped = true;
                            break;
                        }
                        if (d1 != d)
                        {
                            temp = new BlockPos(pos.getX(), pos.getY() + l - d, pos.getZ() + i - 1);
                            if (world.isAirBlock(temp)) BerryGenManager.placeBerryLeaf(world, temp, berryId);
                        }

                        d1 = d;
                    }
                    d1 = 0;
                    for (int i = 0; i <= l / 1.5; i++)
                    {
                        d = i / 3;
                        temp = new BlockPos(pos.getX(), pos.getY() + l - d, pos.getZ() - i);
                        if (world.isAirBlock(temp)) BerryGenManager.placeBerryLeaf(world, temp, berryId);
                        else if (i != 0)
                        {
                            stopped = true;
                            break;
                        }
                        if (d1 != d)
                        {
                            temp = new BlockPos(pos.getX(), pos.getY() + l - d, pos.getZ() - i + 1);
                            if (world.isAirBlock(temp)) BerryGenManager.placeBerryLeaf(world, temp, berryId);
                        }
                        d1 = d;
                    }
                    d1 = 0;
                    for (int i = 0; i <= l / 1.5; i++)
                    {
                        d = i / 3;
                        temp = new BlockPos(pos.getX() + i, pos.getY() + l - d, pos.getZ());
                        if (world.isAirBlock(temp)) BerryGenManager.placeBerryLeaf(world, temp, berryId);
                        else if (i != 0)
                        {
                            stopped = true;
                            break;
                        }
                        if (d1 != d)
                        {
                            temp = new BlockPos(pos.getX() + i - 1, pos.getY() + l - d, pos.getZ());
                            if (world.isAirBlock(temp)) BerryGenManager.placeBerryLeaf(world, temp, berryId);
                        }
                        d1 = d;
                    }
                    d1 = 0;
                    for (int i = 0; i <= l / 1.5; i++)
                    {
                        d = i / 3;
                        temp = new BlockPos(pos.getX() - i, pos.getY() + l - d, pos.getZ());

                        if (world.isAirBlock(temp)) BerryGenManager.placeBerryLeaf(world, temp, berryId);
                        else if (i != 0)
                        {
                            stopped = true;
                            break;
                        }
                        if (d1 != d)
                        {
                            temp = new BlockPos(pos.getX() - i + 1, pos.getY() + l - d, pos.getZ());
                            if (world.isAirBlock(temp)) BerryGenManager.placeBerryLeaf(world, temp, berryId);
                        }
                        d1 = d;
                    }
                }
            }
        }
    }

    public static interface TreeGrower
    {
        void growTree(ServerWorld world, BlockPos cropPos, int berryId);
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
        if (o1.spawnRule.values.containsKey(BerryGenManager.prior)) p1 = Integer.parseInt(o1.spawnRule.values.get(
                BerryGenManager.prior));
        if (o2.spawnRule.values.containsKey(BerryGenManager.prior)) p2 = Integer.parseInt(o2.spawnRule.values.get(
                BerryGenManager.prior));
        return p1.compareTo(p2);
    };

    public static ItemStack getRandomBerryForBiome(final World world, final BlockPos location)
    {
        if (BerryGenManager.berryLocations.isEmpty()) BerryGenManager.parseConfig();
        SpawnBiomeMatcher toMatch = null;
        final SpawnCheck checker = new SpawnCheck(Vector3.getNewVector().set(location), world);
        /**
         * Shuffle list, then re-sort it. This allows the values of the same
         * priority to be randomized, but then still respect priority order for
         * specific ones.
         */
        Collections.shuffle(BerryGenManager.matchers);
        BerryGenManager.matchers.sort(BerryGenManager.COMPARE);
        for (final SpawnBiomeMatcher matcher : BerryGenManager.matchers)
            if (matcher.matches(checker))
            {
                toMatch = matcher;
                break;
            }
        if (toMatch == null) return ItemStack.EMPTY;
        final List<ItemStack> options = BerryGenManager.berryLocations.get(toMatch);
        if (options == null || options.isEmpty()) return ItemStack.EMPTY;
        final ItemStack ret = options.get(world.rand.nextInt(options.size())).copy();
        final int size = 1 + world.rand.nextInt(ret.getCount() + 15);
        ret.setCount(size);
        return ret;
    }

    private static void loadConfig()
    {
        BerryGenManager.list = new BerryGenList();
        for (final ResourceLocation s : Database.configDatabases.get(EnumDatabase.BERRIES.ordinal()))
            try
            {
                BerryGenList loaded;
                final Reader reader = new InputStreamReader(Database.resourceManager.getResource(s).getInputStream());
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
        if (BerryGenManager.list != null) for (final BerrySpawn rule : BerryGenManager.list.locations)
            for (final SpawnRule spawn : rule.spawn)
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

    public static void placeBerryLeaf(final World world, final BlockPos pos, final int berryId)
    {
        final String id = BerryManager.berryNames.get(berryId);
        Block leaves = ItemGenerator.leaves.get(id);
        if (leaves == null)
        {
            PokecubeCore.LOGGER.error("Trying to make leaves for unregistered berry: " + id);
            leaves = Blocks.OAK_LEAVES.getBlock();
        }
        world.setBlockState(pos, leaves.getDefaultState());
    }

}
