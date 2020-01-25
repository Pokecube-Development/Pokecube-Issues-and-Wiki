package pokecube.core.blocks.berries;

import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.material.Material;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import pokecube.core.PokecubeCore;
import pokecube.core.database.Database;
import pokecube.core.database.Database.EnumDatabase;
import pokecube.core.database.PokedexEntryLoader;
import pokecube.core.database.PokedexEntryLoader.SpawnRule;
import pokecube.core.database.SpawnBiomeMatcher;
import pokecube.core.database.SpawnBiomeMatcher.SpawnCheck;
import pokecube.core.handlers.ItemGenerator;
import pokecube.core.items.berries.BerryManager;
import pokecube.core.world.terrain.PokecubeTerrainChecker;
import thut.api.maths.Vector3;

public class BerryGenManager
{
    private static class BerryGenList
    {
        List<BerrySpawn> locations = Lists.newArrayList();
    }

    private static class BerrySpawn
    {
        public List<SpawnRule> spawn;
        public String          berry;
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
        public void growTree(final World world, final BlockPos pos, final int berryId)
        {
            final int l = world.rand.nextInt(1) + 6;
            boolean flag = true;
            BlockPos temp;
            final int y = pos.getY();
            final int z = pos.getZ();
            final int x = pos.getX();
            if (y >= 1 && y + l + 1 <= world.dimension.getActualHeight())
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
                            if (i1 >= 0 && i1 < world.dimension.getActualHeight())
                            {
                                temp = new BlockPos(l1, i1, j1);
                                final BlockState state = world.getBlockState(temp);
                                final Block block = world.getBlockState(temp).getBlock();
                                if (!world.isAirBlock(temp) && !block.isFoliage(world.getBlockState(temp), world, temp)
                                        && block != Blocks.GRASS && block != Blocks.DIRT && !PokecubeTerrainChecker
                                                .isWood(state)) flag = false;
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

                if (isSoil && y < world.dimension.getActualHeight() - l - 1)
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

                        if (block == null || block.isAir(world.getBlockState(temp), world, temp) || block.isFoliage(
                                world.getBlockState(temp), world, temp) && state.getMaterial() == Material.LEAVES) world
                                        .setBlockState(temp, this.wood);
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
        public void growTree(final World world, final BlockPos pos, final int berryId)
        {
            final int l = world.rand.nextInt(1) + 5;
            BlockPos temp;
            if (pos.getY() >= 1 && pos.getY() + l + 1 <= world.dimension.getActualHeight())
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
        void growTree(World world, BlockPos cropPos, int berryId);
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
        final int size = 1 + world.rand.nextInt(ret.getCount() + 5);
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
