package pokecube.adventures.ai.poi;

import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.entity.npc.VillagerTrades.ItemListing;
import net.minecraftforge.event.village.VillagerTradesEvent;
import net.minecraftforge.registries.RegistryObject;
import pokecube.adventures.PokecubeAdv;
import pokecube.core.entity.npc.NpcType;
import thut.core.common.ThutCore;

public class Professions
{
    public static final RegistryObject<VillagerProfession> HEALER;
    public static final RegistryObject<VillagerProfession> PROFESSOR;
    public static final RegistryObject<VillagerProfession> MERCHANT;

    static
    {
        HEALER = PokecubeAdv.PROFESSIONS.register("healer", () -> new VillagerProfession("pokecube_adventures:healer",
                pokecube.core.ai.poi.PointsOfInterest.HEALER.get(), ImmutableSet.of(), ImmutableSet.of(), null));
        PROFESSOR = PokecubeAdv.PROFESSIONS.register("professor",
                () -> new VillagerProfession("pokecube_adventures:professor", PointsOfInterest.GENELAB.get(),
                        ImmutableSet.of(), ImmutableSet.of(), null));
        MERCHANT = PokecubeAdv.PROFESSIONS.register("trader", () -> new VillagerProfession("pokecube_adventures:trader",
                PointsOfInterest.TRADER.get(), ImmutableSet.of(), ImmutableSet.of(), null));
    }

    public static void init()
    {
        ThutCore.FORGE_BUS.addListener(Professions::onTradeUpdate);
    }

    public static void postInit()
    {
        NpcType.byType("healer").setProfession(Professions.HEALER.get());
        NpcType.byType("professor").setProfession(Professions.PROFESSOR.get());
        NpcType.byType("trader").setProfession(Professions.MERCHANT.get());
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
        if (old != null && !replace) trades = NpcType.join(old, trades);
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
}
