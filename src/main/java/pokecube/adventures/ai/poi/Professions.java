package pokecube.adventures.ai.poi;

import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import org.apache.commons.compress.utils.Lists;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.entity.npc.VillagerTrades.ItemListing;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent.Register;
import net.minecraftforge.event.village.VillagerTradesEvent;
import pokecube.core.entity.npc.NpcType;

public class Professions
{
    public static VillagerProfession HEALER;
    public static VillagerProfession PROFESSOR;
    public static VillagerProfession MERCHANT;

    public static void register(final Register<VillagerProfession> event)
    {
        Professions.HEALER = new VillagerProfession("pokecube_adventures:healer",
                pokecube.core.ai.poi.PointsOfInterest.HEALER.get(), ImmutableSet.of(), ImmutableSet.of(), null)
                        .setRegistryName("pokecube_adventures:healer");
        Professions.PROFESSOR = new VillagerProfession("pokecube_adventures:professor", PointsOfInterest.GENELAB.get(),
                ImmutableSet.of(), ImmutableSet.of(), null).setRegistryName("pokecube_adventures:professor");
        Professions.MERCHANT = new VillagerProfession("pokecube_adventures:trader", PointsOfInterest.TRADER.get(),
                ImmutableSet.of(), ImmutableSet.of(), null).setRegistryName("pokecube_adventures:trader");

        event.getRegistry().register(HEALER);
        event.getRegistry().register(PROFESSOR);
        event.getRegistry().register(MERCHANT);

        NpcType.byType("healer").setProfession(Professions.HEALER);
        NpcType.byType("professor").setProfession(Professions.PROFESSOR);
        NpcType.byType("trader").setProfession(Professions.MERCHANT);

        MinecraftForge.EVENT_BUS.addListener(Professions::onTradeUpdate);
    }

    public static void clear()
    {
        CACHE.clear();
    }

    private static void onTradeUpdate(VillagerTradesEvent event)
    {
        Int2ObjectMap<ItemListing[]> override = CACHE.get(event.getType());
        if (override != null)
        {
            override.forEach((i, arr) -> {
                List<ItemListing> trades = event.getTrades().get(i.intValue());
                for (ItemListing l : arr) trades.add(l);
            });
        }
    }

    private static Map<VillagerProfession, Int2ObjectMap<ItemListing[]>> CACHE = Maps.newHashMap();

    public static void updateProfession(VillagerProfession profession, int level, ItemListing[] trades, boolean replace)
    {
        ItemListing[] old = getOldTrades(profession, level);
        if (old != null && !replace) trades = join(old, trades);
        Int2ObjectMap<ItemListing[]> trade_map = CACHE.get(profession);
        if (trade_map == null) CACHE.put(profession, trade_map = new Int2ObjectOpenHashMap<>());
        trade_map.put(level, trades);
    }

    @Nullable
    private static ItemListing[] getOldTrades(VillagerProfession profession, int level)
    {
        if (!CACHE.containsKey(profession)) return null;
        return CACHE.get(profession).getOrDefault(level, null);
    }

    private static ItemListing[] join(ItemListing[]... listings)
    {
        List<ItemListing> list = Lists.newArrayList();
        for (ItemListing[] listing : listings) for (ItemListing l : listing) list.add(l);
        return list.toArray(new ItemListing[list.size()]);
    }
}
