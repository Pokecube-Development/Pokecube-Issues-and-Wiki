package pokecube.adventures.utils;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.JsonObject;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.Tag;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import pokecube.adventures.capabilities.utils.TypeTrainer;
import pokecube.adventures.capabilities.utils.TypeTrainer.TrainerTrade;
import pokecube.adventures.capabilities.utils.TypeTrainer.TrainerTrades;
import pokecube.adventures.utils.trade_presets.AllMegas;
import pokecube.adventures.utils.trade_presets.AllTMs;
import pokecube.adventures.utils.trade_presets.AllVitamins;
import pokecube.adventures.utils.trade_presets.BuyRandomBadge;
import pokecube.adventures.utils.trade_presets.SellRandomBadge;
import pokecube.core.PokecubeCore;
import pokecube.core.PokecubeItems;
import pokecube.core.database.pokedex.PokedexEntryLoader;
import pokecube.core.database.pokedex.PokedexEntryLoader.Drop;
import pokecube.core.database.resources.PackFinder;
import pokecube.core.entity.npc.NpcType;
import pokecube.core.utils.Tools;

public class TradeEntryLoader
{
    public static class Buy extends Drop
    {
        @Override
        public String toString()
        {
            return this.values + " " + this.tag;
        }
    }

    public static class Sell extends Drop
    {
        @Override
        public String toString()
        {
            return this.values + " " + this.tag;
        }
    }

    public static class Trade
    {
        public String custom;
        public String type = "preset";
        public Sell   sell;

        public final List<Buy> buys = Lists.newArrayList();

        public Map<QName, String> values;
    }

    public static class TradeEntry
    {
        String template = "default";

        private final List<Trade> trades = Lists.newArrayList();
    }

    public static class XMLDatabase
    {
        private final List<TradeEntry> trades = Lists.newArrayList();
    }

    public static interface TradePreset
    {
        void apply(Trade trade, TrainerTrades trades);
    }

    public static final QName MIN = new QName("min");

    public static final QName MAX = new QName("max");

    public static final QName CHANCE = new QName("chance");

    public static Map<String, TradePreset> registeredPresets = Maps.newHashMap();

    static
    {
        TradeEntryLoader.registeredPresets.put("allMegas", new AllMegas());
        TradeEntryLoader.registeredPresets.put("allVitamins", new AllVitamins());
        TradeEntryLoader.registeredPresets.put("allTMs", new AllTMs());
        TradeEntryLoader.registeredPresets.put("buyRandomBadge", new BuyRandomBadge());
        TradeEntryLoader.registeredPresets.put("sellRandomBadge", new SellRandomBadge());
    }

    private static boolean addTemplatedTrades(final Trade trade, final TrainerTrades trades)
    {
        final String flag = trade.type;
        final String custom = trade.custom;
        if (custom == null) return false;
        if (flag == null || flag.equals("preset"))
        {
            final TradePreset preset = TradeEntryLoader.registeredPresets.get(custom);
            if (preset != null)
            {
                preset.apply(trade, trades);
                return true;
            }
        }
        else if (flag.equals("tag_sell"))
        {
            final ResourceLocation tag = PokecubeItems.toPokecubeResource(custom);
            final Tag<Item> itemtag = ItemTags.getAllTags().getTagOrEmpty(tag);
            for (final Item i : itemtag.getValues())
            {
                final ItemStack stack = new ItemStack(i);
                if (!stack.isEmpty())
                {
                    Map<QName, String> values;
                    TrainerTrade recipe;
                    ItemStack buy1 = ItemStack.EMPTY;
                    ItemStack buy2 = ItemStack.EMPTY;
                    values = trade.buys.get(0).getValues();
                    buy1 = Tools.getStack(values);
                    if (trade.buys.size() > 1)
                    {
                        values = trade.buys.get(1).getValues();
                        buy2 = Tools.getStack(values);
                    }
                    recipe = new TrainerTrade(buy1, buy2, stack);
                    values = trade.values;
                    if (values.containsKey(TradeEntryLoader.CHANCE)) recipe.chance = Float.parseFloat(values.get(
                            TradeEntryLoader.CHANCE));
                    if (values.containsKey(TradeEntryLoader.MIN)) recipe.min = Integer.parseInt(values.get(
                            TradeEntryLoader.MIN));
                    if (values.containsKey(TradeEntryLoader.MAX)) recipe.max = Integer.parseInt(values.get(
                            TradeEntryLoader.MAX));
                    trades.tradesList.add(recipe);
                }
            }
            return true;
        }
        else if (flag.equals("tag_buy"))
        {
            final ResourceLocation tag = PokecubeItems.toPokecubeResource(custom);
            final Tag<Item> itemtag = ItemTags.getAllTags().getTagOrEmpty(tag);
            if (itemtag != null) for (final Item i : itemtag.getValues())
            {
                final ItemStack stack = new ItemStack(i);
                if (!stack.isEmpty())
                {
                    Map<QName, String> values = trade.sell.getValues();
                    TrainerTrade recipe;
                    final ItemStack sell = Tools.getStack(values);
                    recipe = new TrainerTrade(stack, ItemStack.EMPTY, sell);
                    values = trade.values;
                    if (values.containsKey(TradeEntryLoader.CHANCE)) recipe.chance = Float.parseFloat(values.get(
                            TradeEntryLoader.CHANCE));
                    if (values.containsKey(TradeEntryLoader.MIN)) recipe.min = Integer.parseInt(values.get(
                            TradeEntryLoader.MIN));
                    if (values.containsKey(TradeEntryLoader.MAX)) recipe.max = Integer.parseInt(values.get(
                            TradeEntryLoader.MAX));
                    trades.tradesList.add(recipe);
                }
            }
            return true;
        }
        return false;
    }

    public static XMLDatabase loadDatabase()
    {
        final XMLDatabase full = new XMLDatabase();
        final Collection<ResourceLocation> resources = PackFinder.getJsonResources(NpcType.DATALOC);
        for (final ResourceLocation file : resources)
        {
            JsonObject loaded;
            try
            {
                final BufferedReader reader = new BufferedReader(new InputStreamReader(PackFinder.getStream(file)));
                loaded = PokedexEntryLoader.gson.fromJson(reader, JsonObject.class);
                reader.close();
                if (loaded.has("trades"))
                {
                    final XMLDatabase database = PokedexEntryLoader.gson.fromJson(loaded, XMLDatabase.class);
                    for (final TradeEntry entry : database.trades)
                        full.trades.add(entry);
                }
            }
            catch (final Exception e)
            {
                PokecubeCore.LOGGER.error("Error with database file {}", file, e);
            }
        }
        return full;
    }

    public static void makeEntries()
    {
        final XMLDatabase database = TradeEntryLoader.loadDatabase();
        for (final TradeEntry entry : database.trades)
        {
            final TrainerTrades trades = new TrainerTrades();
            inner:
            for (final Trade trade : entry.trades)
            {
                if (TradeEntryLoader.addTemplatedTrades(trade, trades)) continue inner;
                TrainerTrade recipe;
                ItemStack sell = ItemStack.EMPTY;
                ItemStack buy1 = ItemStack.EMPTY;
                ItemStack buy2 = ItemStack.EMPTY;
                Map<QName, String> values = trade.sell.getValues();
                sell = Tools.getStack(values);
                values = trade.buys.get(0).getValues();
                buy1 = Tools.getStack(values);
                if (trade.buys.size() > 1)
                {
                    values = trade.buys.get(1).getValues();
                    buy2 = Tools.getStack(values);
                }
                if (sell.isEmpty())
                {
                    System.err.println("No Sell:" + trade.sell + " " + trade.buys);
                    continue;
                }

                recipe = new TrainerTrade(buy1, buy2, sell);
                values = trade.values;
                if (values.containsKey(TradeEntryLoader.CHANCE)) recipe.chance = Float.parseFloat(values.get(
                        TradeEntryLoader.CHANCE));
                if (values.containsKey(TradeEntryLoader.MIN)) recipe.min = Integer.parseInt(values.get(
                        TradeEntryLoader.MIN));
                if (values.containsKey(TradeEntryLoader.MAX)) recipe.max = Integer.parseInt(values.get(
                        TradeEntryLoader.MAX));
                trades.tradesList.add(recipe);
            }
            TypeTrainer.tradesMap.put(entry.template, trades);
        }
    }

}
