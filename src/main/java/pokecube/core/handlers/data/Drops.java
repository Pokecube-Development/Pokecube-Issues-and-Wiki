package pokecube.core.handlers.data;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mojang.datafixers.util.Pair;

import net.minecraft.advancements.criterion.EnchantmentPredicate;
import net.minecraft.advancements.criterion.ItemPredicate;
import net.minecraft.advancements.criterion.MinMaxBounds;
import net.minecraft.block.Block;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DirectoryCache;
import net.minecraft.data.IDataProvider;
import net.minecraft.data.LootTableProvider;
import net.minecraft.data.loot.BlockLootTables;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.loot.ConstantRange;
import net.minecraft.loot.ItemLootEntry;
import net.minecraft.loot.LootParameterSet;
import net.minecraft.loot.LootParameterSets;
import net.minecraft.loot.LootPool;
import net.minecraft.loot.LootTable;
import net.minecraft.loot.LootTableManager;
import net.minecraft.loot.RandomValueRange;
import net.minecraft.loot.conditions.ILootCondition;
import net.minecraft.loot.conditions.MatchTool;
import net.minecraft.loot.conditions.TableBonus;
import net.minecraft.loot.functions.SetCount;
import net.minecraft.util.IItemProvider;
import net.minecraft.util.ResourceLocation;
import pokecube.core.PokecubeItems;
import pokecube.core.handlers.ItemGenerator;
import pokecube.core.items.ItemFossil;
import pokecube.core.items.berries.BerryManager;

public class Drops extends LootTableProvider
{

    public static class BlockLoot extends BlockLootTables
    {
        private static final ILootCondition.IBuilder SILK_TOUCH                 = MatchTool.builder(
                ItemPredicate.Builder.create().enchantment(new EnchantmentPredicate(Enchantments.SILK_TOUCH,
                        MinMaxBounds.IntBound.atLeast(1))));
        private static final ILootCondition.IBuilder NO_SILK_TOUCH              = BlockLoot.SILK_TOUCH.inverted();
        private static final ILootCondition.IBuilder SHEARS                     = MatchTool.builder(
                ItemPredicate.Builder.create().item(Items.SHEARS));
        private static final ILootCondition.IBuilder SILK_TOUCH_OR_SHEARS       = BlockLoot.SHEARS.alternative(
                BlockLoot.SILK_TOUCH);
        private static final ILootCondition.IBuilder NOT_SILK_TOUCH_OR_SHEARS   = BlockLoot.SILK_TOUCH_OR_SHEARS
                .inverted();
        private static final float[]                 DEFAULT_SAPLING_DROP_RATES = new float[] { 0.05F, 0.0625F,
                0.083333336F, 0.1F };

        /**
         * Used for all leaves, drops self with silk touch, otherwise drops the
         * second Block param with the passed chances
         * for fortune levels, adding in sticks.
         */
        protected static LootTable.Builder droppingWithChancesAndDrop(final Block blockA, final IItemProvider dropA,
                final IItemProvider dropB, final float... chances)
        {
            return BlockLootTables.droppingWithSilkTouchOrShears(blockA, BlockLootTables.withSurvivesExplosion(blockA,
                    ItemLootEntry.builder(dropA)).acceptCondition(TableBonus.builder(Enchantments.FORTUNE, chances)))
                    .addLootPool(LootPool.builder().rolls(ConstantRange.of(1)).acceptCondition(
                            BlockLoot.NOT_SILK_TOUCH_OR_SHEARS).addEntry(BlockLootTables.withExplosionDecay(blockA,
                                    ItemLootEntry.builder(dropB).acceptFunction(SetCount.builder(RandomValueRange.of(
                                            1.0F, 2.0F)))).acceptCondition(TableBonus.builder(Enchantments.FORTUNE,
                                                    0.02F, 0.022222223F, 0.025F, 0.033333335F, 0.1F))));
        }

        protected static LootTable.Builder shearsOrSilk(final IItemProvider item)
        {
            return LootTable.builder().addLootPool(LootPool.builder().rolls(ConstantRange.of(1)).acceptCondition(
                    BlockLoot.SILK_TOUCH_OR_SHEARS).addEntry(ItemLootEntry.builder(item)));
        }

        protected static LootTable.Builder silk(final IItemProvider item)
        {
            return LootTable.builder().addLootPool(LootPool.builder().rolls(ConstantRange.of(1)).acceptCondition(
                    BlockLoot.SILK_TOUCH).addEntry(ItemLootEntry.builder(item)));
        }

        private final List<Block> known = Lists.newArrayList();

        @Override
        protected void registerLootTable(final Block blockIn, final LootTable.Builder table)
        {
            this.known.add(blockIn);
            super.registerLootTable(blockIn, table);
        }

        @Override
        protected void addTables()
        {
            this.registerDropSelfLootTable(PokecubeItems.REPELBLOCK);

            final List<ItemFossil> drops = Lists.newArrayList(ItemGenerator.fossils.values());
            LootPool.Builder fossilPool = LootPool.builder().rolls(ConstantRange.of(1)).addEntry(ItemLootEntry.builder(
                    PokecubeItems.FOSSILSTONE).acceptCondition(BlockLoot.SILK_TOUCH));
            for (final ItemFossil fossil : drops)
                fossilPool = fossilPool.addEntry(ItemLootEntry.builder(fossil).acceptCondition(
                        BlockLoot.NO_SILK_TOUCH));
            this.registerLootTable(PokecubeItems.FOSSILSTONE, LootTable.builder().addLootPool(fossilPool));
            for (final String s : ItemGenerator.logs.keySet())
            {
                final Block from = ItemGenerator.logs.get(s);
                final Block to = ItemGenerator.planks.get(s);
                this.registerDropSelfLootTable(from);
                this.registerDropSelfLootTable(to);
            }
            for (final String s : ItemGenerator.leaves.keySet())
            {
                final Block to = ItemGenerator.leaves.get(s);
                final Item berry = BerryManager.byName.get(s);
                this.registerLootTable(to, BlockLoot::shearsOrSilk);
                this.registerLootTable(to, (block) ->
                {
                    return BlockLoot.droppingWithChancesAndDrop(block, berry, berry,
                            BlockLoot.DEFAULT_SAPLING_DROP_RATES);
                });
            }
            for (final int i : BerryManager.berryCrops.keySet())
            {
                final Block crop = BerryManager.berryCrops.get(i);
                final Block fruit = BerryManager.berryFruits.get(i);
                final Item item = BerryManager.berryItems.get(i);

                this.registerDropping(crop, item);
                this.registerLootTable(fruit, BlockLootTables.droppingRandomly(item, RandomValueRange.of(1, 3)));
            }
        }

        @Override
        protected Iterable<Block> getKnownBlocks()
        {
            return this.known;
        }
    }

    private static final Gson   GSON   = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
    private final DataGenerator dataGenerator;
    private static final Logger LOGGER = LogManager.getLogger();

    public Drops(final DataGenerator dataGeneratorIn)
    {
        super(dataGeneratorIn);
        this.dataGenerator = dataGeneratorIn;
    }

    @Override
    public void act(final DirectoryCache cache)
    {

        final Path path = this.dataGenerator.getOutputFolder();
        final Map<ResourceLocation, LootTable> map = Maps.newHashMap();
        this.getTables().forEach((pair) ->
        {
            pair.getFirst().get().accept((location, builder) ->
            {
                if (map.put(location, builder.setParameterSet(pair.getSecond()).build()) != null)
                    throw new IllegalStateException("Duplicate loot table " + location);
            });
        });
        map.forEach((location, table) ->
        {
            final Path path1 = Drops.getPath(path, location);

            try
            {
                IDataProvider.save(Drops.GSON, cache, LootTableManager.toJson(table), path1);
            }
            catch (final IOException ioexception)
            {
                Drops.LOGGER.error("Couldn't save loot table {}", path1, ioexception);
            }

        });
    }

    private static Path getPath(final Path pathIn, final ResourceLocation id)
    {
        return pathIn.resolve("data/" + id.getNamespace() + "/loot_tables/" + id.getPath() + ".json");
    }

    @Override
    protected List<Pair<Supplier<Consumer<BiConsumer<ResourceLocation, LootTable.Builder>>>, LootParameterSet>> getTables()
    {
        return ImmutableList.of(Pair.of(BlockLoot::new, LootParameterSets.BLOCK));
    }
}
