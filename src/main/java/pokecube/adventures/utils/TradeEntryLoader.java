package pokecube.adventures.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;

import javax.xml.namespace.QName;

import org.objectweb.asm.Type;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.JsonObject;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.Tag;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.loading.moddiscovery.ModFile;
import net.minecraftforge.forgespi.language.ModFileScanData.AnnotationData;
import pokecube.adventures.capabilities.utils.TypeTrainer;
import pokecube.adventures.capabilities.utils.TypeTrainer.TrainerTrade;
import pokecube.adventures.capabilities.utils.TypeTrainer.TrainerTrades;
import pokecube.adventures.utils.trade_presets.TradePresetAn;
import pokecube.core.PokecubeCore;
import pokecube.core.PokecubeItems;
import pokecube.core.database.pokedex.PokedexEntryLoader;
import pokecube.core.database.pokedex.PokedexEntryLoader.Drop;
import pokecube.core.database.resources.PackFinder;
import pokecube.core.entity.npc.NpcType;
import pokecube.core.utils.Tools;
import thut.lib.CompatParser.ClassFinder;

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
        public Sell sell;

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

    static List<String> MODULEPACKAGES = Lists.newArrayList();

    static
    {
        MODULEPACKAGES.add(TradePresetAn.class.getPackageName());
    }

    public static void init()
    {
        if (!registeredPresets.isEmpty()) return;

        Type ANNOTE = Type.getType(TradePresetAn.class);
        BiFunction<ModFile, String, Boolean> validClass = (file, name) -> {
            for (final AnnotationData a : file.getScanResult().getAnnotations())
                if (name.equals(a.clazz().getClassName()) && a.annotationType().equals(ANNOTE))
            {
                if (a.annotationData().containsKey("mod"))
                {
                    String modid = (String) a.annotationData().get("mod");
                    return ModList.get().isLoaded(modid);
                }
                return true;
            }
            return false;
        };

        Collection<Class<?>> foundClasses;
        for (String name : MODULEPACKAGES)
        {
            try
            {
                foundClasses = ClassFinder.find(name, validClass);
                for (final Class<?> candidateClass : foundClasses)
                {
                    if (candidateClass.getAnnotations().length == 0) continue;
                    final TradePresetAn preset = candidateClass.getAnnotation(TradePresetAn.class);
                    if (preset != null)
                    {
                        try
                        {
                            TradeEntryLoader.registeredPresets.put(preset.key(),
                                    (TradePreset) candidateClass.getConstructor().newInstance());
                        }
                        catch (InstantiationException | IllegalAccessException | IllegalArgumentException
                                | InvocationTargetException | NoSuchMethodException | SecurityException e)
                        {
                            e.printStackTrace();
                        }
                    }
                }

            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
    }

    private static boolean addTemplatedTrades(final Trade trade, final TrainerTrades trades)
    {
        init();

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
                    if (values.containsKey(TradeEntryLoader.CHANCE))
                        recipe.chance = Float.parseFloat(values.get(TradeEntryLoader.CHANCE));
                    if (values.containsKey(TradeEntryLoader.MIN))
                        recipe.min = Integer.parseInt(values.get(TradeEntryLoader.MIN));
                    if (values.containsKey(TradeEntryLoader.MAX))
                        recipe.max = Integer.parseInt(values.get(TradeEntryLoader.MAX));
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
                    if (values.containsKey(TradeEntryLoader.CHANCE))
                        recipe.chance = Float.parseFloat(values.get(TradeEntryLoader.CHANCE));
                    if (values.containsKey(TradeEntryLoader.MIN))
                        recipe.min = Integer.parseInt(values.get(TradeEntryLoader.MIN));
                    if (values.containsKey(TradeEntryLoader.MAX))
                        recipe.max = Integer.parseInt(values.get(TradeEntryLoader.MAX));
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
                    for (final TradeEntry entry : database.trades) full.trades.add(entry);
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
                if (values.containsKey(TradeEntryLoader.CHANCE))
                    recipe.chance = Float.parseFloat(values.get(TradeEntryLoader.CHANCE));
                if (values.containsKey(TradeEntryLoader.MIN))
                    recipe.min = Integer.parseInt(values.get(TradeEntryLoader.MIN));
                if (values.containsKey(TradeEntryLoader.MAX))
                    recipe.max = Integer.parseInt(values.get(TradeEntryLoader.MAX));
                trades.tradesList.add(recipe);
            }
            TypeTrainer.tradesMap.put(entry.template, trades);
        }
    }

}
