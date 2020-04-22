package pokecube.adventures.utils;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.bind.annotation.XmlAnyAttribute;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.namespace.QName;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.Tag;
import net.minecraft.util.ResourceLocation;
import pokecube.adventures.capabilities.utils.TypeTrainer;
import pokecube.adventures.capabilities.utils.TypeTrainer.TrainerTrade;
import pokecube.adventures.capabilities.utils.TypeTrainer.TrainerTrades;
import pokecube.adventures.utils.trade_presets.AllGenericHeld;
import pokecube.adventures.utils.trade_presets.AllMegas;
import pokecube.adventures.utils.trade_presets.AllTMs;
import pokecube.adventures.utils.trade_presets.AllVitamins;
import pokecube.adventures.utils.trade_presets.BuyRandomBadge;
import pokecube.adventures.utils.trade_presets.SellRandomBadge;
import pokecube.core.PokecubeItems;
import pokecube.core.database.Database;
import pokecube.core.database.PokedexEntryLoader;
import pokecube.core.database.PokedexEntryLoader.Drop;
import pokecube.core.utils.Tools;

public class TradeEntryLoader
{
    @XmlRootElement(name = "Buy")
    public static class Buy extends Drop
    {
        @Override
        public String toString()
        {
            return this.values + " " + this.tag;
        }
    }

    @XmlRootElement(name = "Sell")
    public static class Sell extends Drop
    {
        @Override
        public String toString()
        {
            return this.values + " " + this.tag;
        }
    }

    @XmlRootElement(name = "Trade")
    public static class Trade
    {
        @XmlAttribute
        public String             custom;
        @XmlAttribute
        public String             type = "preset";
        @XmlElement(name = "Sell")
        public Sell               sell;
        @XmlElement(name = "Buy")
        public final List<Buy>    buys = Lists.newArrayList();
        @XmlAnyAttribute
        public Map<QName, String> values;
    }

    @XmlRootElement(name = "Trades")
    public static class TradeEntry
    {
        @XmlAttribute
        String                    template = "default";
        @XmlElement(name = "Trade")
        private final List<Trade> trades   = Lists.newArrayList();
    }

    @XmlRootElement(name = "AllTrades")
    public static class XMLDatabase
    {
        @XmlElement(name = "Trades")
        private final List<TradeEntry> trades = Lists.newArrayList();
    }

    public static interface TradePreset
    {
        void apply(Trade trade, TrainerTrades trades);
    }

    public static final QName MIN = new QName("min");

    public static final QName MAX = new QName("max");

    public static final QName CHANCE = new QName("chance");

    static XMLDatabase database;

    /**
     * These items will not be auto-added to "allGenericHeld"
     */
    public static Set<String> genericTradeBlacklist = Sets.newHashSet();

    public static Map<String, TradePreset> registeredPresets = Maps.newHashMap();

    static
    {
        TradeEntryLoader.registeredPresets.put("allMegas", new AllMegas());
        TradeEntryLoader.registeredPresets.put("allVitamins", new AllVitamins());
        TradeEntryLoader.registeredPresets.put("allTMs", new AllTMs());
        TradeEntryLoader.registeredPresets.put("buyRandomBadge", new BuyRandomBadge());
        TradeEntryLoader.registeredPresets.put("sellRandomBadge", new SellRandomBadge());
        TradeEntryLoader.registeredPresets.put("allGenericHeld", new AllGenericHeld());
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
            final Tag<Item> itemtag = ItemTags.getCollection().getOrCreate(tag);
            for (final Item i : itemtag.getAllElements())
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
            final Tag<Item> itemtag = ItemTags.getCollection().get(tag);
            if (itemtag != null) for (final Item i : itemtag.getAllElements())
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

    public static XMLDatabase loadDatabase(final ResourceLocation file) throws Exception
    {
        final InputStream res = Database.resourceManager.getResource(file).getInputStream();
        final Reader reader = new InputStreamReader(res);
        final XMLDatabase database = PokedexEntryLoader.gson.fromJson(reader, XMLDatabase.class);
        reader.close();
        return database;
    }

    public static void makeEntries(final ResourceLocation file) throws Exception
    {
        if (TradeEntryLoader.database == null) TradeEntryLoader.database = TradeEntryLoader.loadDatabase(file);
        for (final TradeEntry entry : TradeEntryLoader.database.trades)
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
