package pokecube.adventures.utils;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.core.component.DataComponentPredicate;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.entity.npc.VillagerTrades.ItemListing;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.trading.ItemCost;
import net.neoforged.fml.ModList;
import net.neoforged.neoforgespi.language.ModFileScanData.AnnotationData;
import net.neoforged.neoforgespi.locating.IModFile;
import org.objectweb.asm.Type;
import pokecube.adventures.ai.poi.Professions;
import pokecube.adventures.capabilities.utils.TypeTrainer;
import pokecube.adventures.capabilities.utils.TypeTrainer.TrainerTrade;
import pokecube.adventures.capabilities.utils.TypeTrainer.TrainerTrades;
import pokecube.adventures.utils.trade_presets.TradeHelper;
import pokecube.adventures.utils.trade_presets.TradePresetAn;
import pokecube.api.PokecubeAPI;
import pokecube.api.utils.Tools;
import pokecube.core.PokecubeItems;
import pokecube.core.database.resources.PackFinder;
import pokecube.core.entity.npc.NpcType;
import thut.api.item.ItemList;
import thut.api.util.JsonUtil;
import thut.lib.CompatParser.ClassFinder;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;

public class TradeEntryLoader
{
    public static class Trade
    {
        public String custom;
        public String type = "preset";
        public JsonElement sell;
        public int maxUses = Integer.MAX_VALUE;
        public int exp = 1;
        public int demand = 0;
        public float multiplier = 0.05f;
        public int count = -1;

        public final List<JsonElement> buys = Lists.newArrayList();

        public Map<String, JsonElement> values = Maps.newHashMap();
    }

    public static class TradeEntry
    {
        String template = "default";

        private final List<Trade> trades = Lists.newArrayList();
    }

    public static class ProfiessionStage
    {
        int level;
        boolean clear_old = false;

        public final List<Trade> trades = Lists.newArrayList();
    }

    public static class ProfessionEntry
    {
        String profession;
        String type = "";

        public final List<ProfiessionStage> stages = Lists.newArrayList();
    }

    public static class TradeDatabase
    {
        private final List<TradeEntry> trades = Lists.newArrayList();
        private final List<ProfessionEntry> professions = Lists.newArrayList();
    }

    public static interface TradePreset
    {
        void apply(Trade trade, TrainerTrades trades);
    }

    public static final String MIN = "min";

    public static final String MAX = "max";

    public static final String CHANCE = "chance";

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
        BiFunction<IModFile, String, Boolean> validClass = (file, name) -> {
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
                        catch (InstantiationException | IllegalAccessException | IllegalArgumentException |
                                InvocationTargetException | NoSuchMethodException | SecurityException e)
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
            List<Item> items = BuiltInRegistries.ITEM.stream().filter(item -> ItemList.is(tag, item)).toList();
            for (final Item i : items)
            {
                final ItemStack sell = new ItemStack(i);
                if (!sell.isEmpty())
                {
                    TrainerTrade recipe;
                    ItemStack buy1;
                    ItemStack buy2 = ItemStack.EMPTY;
                    buy1 = Tools.getStack(trade.buys.get(0));
                    if (trade.buys.size() > 1)
                    {
                        buy2 = Tools.getStack(trade.buys.get(1));
                    }
                    var cost = TradeHelper.getCost(buy1);
                    var _buy2 = Optional.ofNullable(buy2.isEmpty()
                            ? null
                            : TradeHelper.getCost(buy2));
                    recipe = new TrainerTrade(cost, _buy2, sell, trade);
                    var values = trade.values;
                    if (values.containsKey(TradeEntryLoader.CHANCE))
                        recipe.chance = values.get(TradeEntryLoader.CHANCE).getAsFloat();
                    trades.tradesList.add(recipe);
                }
            }
            return true;
        }
        else if (flag.equals("tag_buy"))
        {
            final ResourceLocation tag = PokecubeItems.toPokecubeResource(custom);
            List<Item> items = BuiltInRegistries.ITEM.stream().filter(item -> ItemList.is(tag, item)).toList();
            for (final Item i : items)
            {
                final ItemStack buy = new ItemStack(i);
                if (!buy.isEmpty())
                {
                    TrainerTrade recipe;
                    final ItemStack sell = Tools.getStack(trade.sell);
                    var cost = TradeHelper.getCost(buy);
                    recipe = new TrainerTrade(cost, Optional.empty(), sell, trade);
                    var values = trade.values;
                    if (values.containsKey(TradeEntryLoader.CHANCE))
                        recipe.chance = values.get(TradeEntryLoader.CHANCE).getAsFloat();
                    trades.tradesList.add(recipe);
                }
            }
            return true;
        }
        return false;
    }

    public static TradeDatabase loadDatabase()
    {
        final TradeDatabase full = new TradeDatabase();
        final Map<ResourceLocation, Resource> resources = PackFinder.getJsonResources(NpcType.DATALOC);
        resources.forEach((file, resource) -> {
            JsonObject loaded;
            try
            {
                final BufferedReader reader = PackFinder.getReader(file);
                if (reader == null) throw new FileNotFoundException(file.toString());
                loaded = JsonUtil.gson.fromJson(reader, JsonObject.class);
                TradeDatabase database = null;
                reader.close();
                if (loaded.has("trades") && !loaded.has("template"))
                {
                    database = JsonUtil.gson.fromJson(loaded, TradeDatabase.class);
                    full.trades.addAll(database.trades);
                }
                else if (loaded.has("template"))
                {
                    var entry = JsonUtil.gson.fromJson(loaded, TradeEntry.class);
                    full.trades.add(entry);
                }
                if (loaded.has("professions"))
                {
                    if (database == null) database = JsonUtil.gson.fromJson(loaded, TradeDatabase.class);
                    full.professions.addAll(database.professions);
                }
                else if (loaded.has("stages"))
                {
                    var entry = JsonUtil.gson.fromJson(loaded, ProfessionEntry.class);
                    full.professions.add(entry);
                }
            }
            catch (final Exception e)
            {
                PokecubeAPI.LOGGER.error("Error with database file {}", file, e);
            }
        });
        return full;
    }

    public static void makeEntries()
    {
        final TradeDatabase database = TradeEntryLoader.loadDatabase();
        Professions.clear();
        NpcType.TRADE_MAP.clear();
        for (final TradeEntry entry : database.trades)
        {
            final TrainerTrades trades = new TrainerTrades();
            processTrades(trades, entry.trades);
            if (trades.tradesList.isEmpty())
            {
                PokecubeAPI.LOGGER.error("Warning, no trades found for {}", entry.template);
                continue;
            }
            TypeTrainer.tradesMap.put(entry.template, trades);
        }
        for (ProfessionEntry entry : database.professions)
        {
            for (final ProfiessionStage stage : entry.stages)
            {
                int level = stage.level;
                final TrainerTrades trades = new TrainerTrades();
                processTrades(trades, stage.trades);
                ItemListing[] arr = trades.tradesList.toArray(new ItemListing[0]);
                if (entry.profession != null)
                {
                    ResourceLocation id = ResourceLocation.parse(entry.profession);
                    if (BuiltInRegistries.VILLAGER_PROFESSION.containsKey(id))
                    {
                        VillagerProfession profession = BuiltInRegistries.VILLAGER_PROFESSION.get(id);
                        Professions.updateProfession(profession, level, arr, stage.clear_old);
                    }
                }
                if (!entry.type.isEmpty()) NpcType.addTrade(entry.type, level, arr, stage.clear_old);
            }
        }
    }

    private static void processTrades(TrainerTrades trades, List<Trade> list)
    {
        for (final Trade trade : list)
        {
            try
            {
                if (TradeEntryLoader.addTemplatedTrades(trade, trades)) continue;
                TrainerTrade recipe;
                ItemStack sell;
                ItemStack buy1;
                ItemStack buy2 = ItemStack.EMPTY;

                sell = Tools.getStack(trade.sell);
                buy1 = Tools.getStack(trade.buys.get(0));
                if (trade.buys.size() > 1)
                {
                    buy2 = Tools.getStack(trade.buys.get(1));
                }
                if (sell.isEmpty())
                {
                    PokecubeAPI.LOGGER.error("No Sell:" + trade.sell + " " + trade.buys);
                    continue;
                }
                var cost = TradeHelper.getCost(buy1);
                var _buy2 = Optional.ofNullable(buy2.isEmpty()
                        ? null
                        : TradeHelper.getCost(buy2));
                recipe = new TrainerTrade(cost, _buy2, sell, trade);
                var values = trade.values;
                if (values.containsKey(TradeEntryLoader.CHANCE))
                    recipe.chance = values.get(TradeEntryLoader.CHANCE).getAsFloat();
                trades.tradesList.add(recipe);
            }
            catch (Throwable t)
            {
                PokecubeAPI.LOGGER.error("Error with trade: {}", JsonUtil.gson.toJson(trade), t);
            }

        }
    }
}
