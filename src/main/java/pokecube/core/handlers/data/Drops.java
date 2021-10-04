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

import net.minecraft.advancements.critereon.EnchantmentPredicate;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.advancements.critereon.MinMaxBounds;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DataProvider;
import net.minecraft.data.HashCache;
import net.minecraft.data.loot.BlockLoot;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.LootTables;
import net.minecraft.world.level.storage.loot.RandomValueBounds;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSet;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.predicates.BonusLevelTableCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.MatchTool;
import pokecube.core.PokecubeItems;
import pokecube.core.handlers.ItemGenerator;
import pokecube.core.items.ItemFossil;
import pokecube.core.items.berries.BerryManager;
import sun.jvm.hotspot.code.ConstantIntValue;

public class Drops extends LootTableProvider
{

    public static class BlockLoot extends BlockLoot
    {
        private static final LootItemCondition.Builder SILK_TOUCH                 = MatchTool.toolMatches(
                ItemPredicate.Builder.item().hasEnchantment(new EnchantmentPredicate(Enchantments.SILK_TOUCH,
                        MinMaxBounds.Ints.atLeast(1))));
        private static final LootItemCondition.Builder NO_SILK_TOUCH              = BlockLoot.SILK_TOUCH.invert();
        private static final LootItemCondition.Builder SHEARS                     = MatchTool.toolMatches(
                ItemPredicate.Builder.item().of(Items.SHEARS));
        private static final LootItemCondition.Builder SILK_TOUCH_OR_SHEARS       = BlockLoot.SHEARS.or(
                BlockLoot.SILK_TOUCH);
        private static final LootItemCondition.Builder NOT_SILK_TOUCH_OR_SHEARS   = BlockLoot.SILK_TOUCH_OR_SHEARS
                .invert();
        private static final float[]                 DEFAULT_SAPLING_DROP_RATES = new float[] { 0.05F, 0.0625F,
                0.083333336F, 0.1F };

        /**
         * Used for all leaves, drops self with silk touch, otherwise drops the
         * second Block param with the passed chances
         * for fortune levels, adding in sticks.
         */
        protected static LootTable.Builder droppingWithChancesAndDrop(final Block blockA, final ItemLike dropA,
                final ItemLike dropB, final float... chances)
        {
            return BlockLoot.createSilkTouchOrShearsDispatchTable(blockA, BlockLoot.applyExplosionCondition(blockA,
                    LootItem.lootTableItem(dropA)).when(BonusLevelTableCondition.bonusLevelFlatChance(Enchantments.BLOCK_FORTUNE, chances)))
                    .withPool(LootPool.lootPool().setRolls(ConstantIntValue.exactly(1)).when(
                            BlockLoot.NOT_SILK_TOUCH_OR_SHEARS).add(BlockLoot.applyExplosionDecay(blockA,
                                    LootItem.lootTableItem(dropB).apply(SetItemCountFunction.setCount(RandomValueBounds.between(
                                            1.0F, 2.0F)))).when(BonusLevelTableCondition.bonusLevelFlatChance(Enchantments.BLOCK_FORTUNE,
                                                    0.02F, 0.022222223F, 0.025F, 0.033333335F, 0.1F))));
        }

        protected static LootTable.Builder shearsOrSilk(final ItemLike item)
        {
            return LootTable.lootTable().withPool(LootPool.lootPool().setRolls(ConstantIntValue.exactly(1)).when(
                    BlockLoot.SILK_TOUCH_OR_SHEARS).add(LootItem.lootTableItem(item)));
        }

        protected static LootTable.Builder silk(final ItemLike item)
        {
            return LootTable.lootTable().withPool(LootPool.lootPool().setRolls(ConstantIntValue.exactly(1)).when(
                    BlockLoot.SILK_TOUCH).add(LootItem.lootTableItem(item)));
        }

        private final List<Block> known = Lists.newArrayList();

        @Override
        protected void add(final Block blockIn, final LootTable.Builder table)
        {
            this.known.add(blockIn);
            super.add(blockIn, table);
        }

        @Override
        protected void addTables()
        {
            this.dropSelf(PokecubeItems.REPELBLOCK.get());

            final List<ItemFossil> drops = Lists.newArrayList(ItemGenerator.fossils.values());
            LootPool.Builder fossilPool = LootPool.lootPool().setRolls(ConstantIntValue.exactly(1)).add(LootItem.lootTableItem(
                    PokecubeItems.FOSSILSTONE.get()).when(BlockLoot.SILK_TOUCH));
            for (final ItemFossil fossil : drops)
                fossilPool = fossilPool.add(LootItem.lootTableItem(fossil).when(
                        BlockLoot.NO_SILK_TOUCH));
            this.add(PokecubeItems.FOSSILSTONE.get(), LootTable.lootTable().withPool(fossilPool));
            for (final String s : ItemGenerator.logs.keySet())
            {
                final Block from = ItemGenerator.logs.get(s);
                final Block to = ItemGenerator.planks.get(s);
                this.dropSelf(from);
                this.dropSelf(to);
            }
            for (final String s : ItemGenerator.leaves.keySet())
            {
                final Block to = ItemGenerator.leaves.get(s);
                final Item berry = BerryManager.byName.get(s);
                this.add(to, BlockLoot::shearsOrSilk);
                this.add(to, (block) ->
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

                this.dropOther(crop, item);
                this.add(fruit, BlockLoot.createSingleItemTable(item, RandomValueBounds.between(1, 3)));
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
    public void run(final HashCache cache)
    {

        final Path path = this.dataGenerator.getOutputFolder();
        final Map<ResourceLocation, LootTable> map = Maps.newHashMap();
        this.getTables().forEach((pair) ->
        {
            pair.getFirst().get().accept((location, builder) ->
            {
                if (map.put(location, builder.setParamSet(pair.getSecond()).build()) != null)
                    throw new IllegalStateException("Duplicate loot table " + location);
            });
        });
        map.forEach((location, table) ->
        {
            final Path path1 = Drops.getPath(path, location);

            try
            {
                DataProvider.save(Drops.GSON, cache, LootTables.serialize(table), path1);
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
    protected List<Pair<Supplier<Consumer<BiConsumer<ResourceLocation, LootTable.Builder>>>, LootContextParamSet>> getTables()
    {
        return ImmutableList.of(Pair.of(BlockLoot::new, LootContextParamSets.BLOCK));
    }
}
