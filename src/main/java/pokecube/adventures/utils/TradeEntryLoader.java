package pokecube.adventures.utils;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAnyAttribute;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.namespace.QName;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import pokecube.adventures.capabilities.utils.TypeTrainer;
import pokecube.adventures.capabilities.utils.TypeTrainer.TrainerTrade;
import pokecube.adventures.capabilities.utils.TypeTrainer.TrainerTrades;
import pokecube.core.PokecubeItems;
import pokecube.core.database.Database;
import pokecube.core.handlers.ItemGenerator;
import pokecube.core.items.ItemTM;
import pokecube.core.items.vitamins.ItemVitamin;
import pokecube.core.moves.MovesUtils;
import pokecube.core.utils.PokeType;
import pokecube.core.utils.Tools;

public class TradeEntryLoader
{
    @XmlRootElement(name = "Buy")
    public static class Buy
    {
        @XmlAnyAttribute
        Map<QName, String> values;
        @XmlElement(name = "tag")
        String             tag;

        @Override
        public String toString()
        {
            return this.values + " " + this.tag;
        }
    }

    @XmlRootElement(name = "Sell")
    public static class Sell
    {
        @XmlAnyAttribute
        Map<QName, String> values;
        @XmlElement(name = "tag")
        String             tag;

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
        String                  custom;
        @XmlElement(name = "Sell")
        Sell                    sell;
        @XmlElement(name = "Buy")
        private final List<Buy> buys = Lists.newArrayList();
        @XmlAnyAttribute
        Map<QName, String>      values;
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

    static final QName MIN = new QName("min");

    static final QName MAX = new QName("max");

    static final QName CHANCE = new QName("chance");

    static XMLDatabase database;

    /**
     * These items will not be auto-added to "allGenericHeld"
     */
    public static Set<String> genericTradeBlacklist = Sets.newHashSet();

    private static void addTemplatedTrades(final Trade trade, final TrainerTrades trades)
    {
        final String custom = trade.custom;
        if (custom.equals("allMegas"))
        {
            for (final String s : ItemGenerator.variants)
                // Only mega stones
                if (s.contains("mega") && !s.equals("megastone"))
                {
                    final ItemStack sell = PokecubeItems.getStack(s);
                    Map<QName, String> values;
                    TrainerTrade recipe;
                    ItemStack buy1 = ItemStack.EMPTY;
                    ItemStack buy2 = ItemStack.EMPTY;
                    values = trade.buys.get(0).values;
                    if (trade.buys.get(0).tag != null) values.put(new QName("tag"), trade.buys.get(0).tag);
                    buy1 = Tools.getStack(values);
                    if (trade.buys.size() > 1)
                    {
                        values = trade.buys.get(1).values;
                        if (trade.buys.get(1).tag != null) values.put(new QName("tag"), trade.buys.get(1).tag);
                        buy2 = Tools.getStack(values);
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
        }
        else if (custom.equals("allVitamins")) for (final String s : ItemVitamin.vitamins)
        {
            final ItemStack sell = PokecubeItems.getStack(s);
            Map<QName, String> values;
            TrainerTrade recipe;
            ItemStack buy1 = ItemStack.EMPTY;
            ItemStack buy2 = ItemStack.EMPTY;
            values = trade.buys.get(0).values;
            if (trade.buys.get(0).tag != null) values.put(new QName("tag"), trade.buys.get(0).tag);
            buy1 = Tools.getStack(values);
            if (trade.buys.size() > 1)
            {
                values = trade.buys.get(1).values;
                if (trade.buys.get(1).tag != null) values.put(new QName("tag"), trade.buys.get(1).tag);
                buy2 = Tools.getStack(values);
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
        else if (custom.equals("allGenericHeld")) for (final String s : ItemGenerator.variants)
        {
            // Exclude mega stones.
            if (s.contains("mega") && !s.equals("megastone")) continue;
            // Exclude specifically blacklisted.
            if (TradeEntryLoader.genericTradeBlacklist.contains(s)) continue;

            final ItemStack sell = PokecubeItems.getStack(s);
            Map<QName, String> values;
            TrainerTrade recipe;
            ItemStack buy1 = ItemStack.EMPTY;
            ItemStack buy2 = ItemStack.EMPTY;
            values = trade.buys.get(0).values;
            if (trade.buys.get(0).tag != null) values.put(new QName("tag"), trade.buys.get(0).tag);
            buy1 = Tools.getStack(values);
            if (trade.buys.size() > 1)
            {
                values = trade.buys.get(1).values;
                if (trade.buys.get(1).tag != null) values.put(new QName("tag"), trade.buys.get(1).tag);
                buy2 = Tools.getStack(values);
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
        else if (custom.equals("allTMs"))
        {
            final ArrayList<String> moves = Lists.newArrayList(MovesUtils.moves.keySet());
            Collections.sort(moves);
            for (int i = 0; i < moves.size(); i++)
            {
                final int index = i;
                final String name = moves.get(index);
                final ItemStack sell = ItemTM.getTM(name);
                Map<QName, String> values;
                TrainerTrade recipe;
                ItemStack buy1 = ItemStack.EMPTY;
                ItemStack buy2 = ItemStack.EMPTY;
                values = trade.buys.get(0).values;
                if (trade.buys.get(0).tag != null) values.put(new QName("tag"), trade.buys.get(0).tag);
                buy1 = Tools.getStack(values);
                if (trade.buys.size() > 1)
                {
                    values = trade.buys.get(1).values;
                    if (trade.buys.get(1).tag != null) values.put(new QName("tag"), trade.buys.get(1).tag);
                    buy2 = Tools.getStack(values);
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
        }
        else if (custom.equals("sellRandomBadge"))
        {
            for (final PokeType type : PokeType.values())
                if (type != PokeType.unknown)
                {
                    final ItemStack badge = PokecubeItems.getStack("pokecube_adventures:badge_" + type);
                    if (!badge.isEmpty())
                    {
                        Map<QName, String> values;
                        TrainerTrade recipe;
                        ItemStack buy1 = ItemStack.EMPTY;
                        ItemStack buy2 = ItemStack.EMPTY;
                        values = trade.buys.get(0).values;
                        if (trade.buys.get(0).tag != null) values.put(new QName("tag"), trade.buys.get(0).tag);
                        buy1 = Tools.getStack(values);
                        if (trade.buys.size() > 1)
                        {
                            values = trade.buys.get(1).values;
                            if (trade.buys.get(1).tag != null) values.put(new QName("tag"), trade.buys.get(1).tag);
                            buy2 = Tools.getStack(values);
                        }
                        recipe = new TrainerTrade(buy1, buy2, badge);
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
        }
        else if (custom.equals("buyRandomBadge")) for (final PokeType type : PokeType.values())
            if (type != PokeType.unknown)
            {
                final ItemStack badge = PokecubeItems.getStack("pokecube_adventures:badge_" + type);
                if (!badge.isEmpty())
                {
                    Map<QName, String> values = trade.sell.values;
                    TrainerTrade recipe;
                    if (trade.sell.tag != null) values.put(new QName("tag"), trade.sell.tag);
                    final ItemStack sell = Tools.getStack(values);
                    recipe = new TrainerTrade(badge, ItemStack.EMPTY, sell);
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
    }

    public static XMLDatabase loadDatabase(final ResourceLocation file) throws Exception
    {
        final InputStream res = Database.resourceManager.getResource(file).getInputStream();
        final JAXBContext jaxbContext = JAXBContext.newInstance(XMLDatabase.class);
        final Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
        final Reader reader = new InputStreamReader(res);
        final XMLDatabase database = (XMLDatabase) unmarshaller.unmarshal(reader);
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
                if (trade.custom != null)
                {
                    TradeEntryLoader.addTemplatedTrades(trade, trades);
                    continue inner;
                }
                Map<QName, String> values = trade.sell.values;
                TrainerTrade recipe;
                ItemStack sell = ItemStack.EMPTY;
                ItemStack buy1 = ItemStack.EMPTY;
                ItemStack buy2 = ItemStack.EMPTY;
                if (trade.sell.tag != null) values.put(new QName("tag"), trade.sell.tag);
                sell = Tools.getStack(values);
                values = trade.buys.get(0).values;
                if (trade.buys.get(0).tag != null) values.put(new QName("tag"), trade.buys.get(0).tag);
                buy1 = Tools.getStack(values);
                if (trade.buys.size() > 1)
                {
                    values = trade.buys.get(1).values;
                    if (trade.buys.get(1).tag != null) values.put(new QName("tag"), trade.buys.get(1).tag);
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
