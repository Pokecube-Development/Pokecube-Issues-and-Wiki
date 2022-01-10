package pokecube.core.entity.npc;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import javax.annotation.Nullable;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.entity.npc.VillagerTrades;
import net.minecraft.world.entity.npc.VillagerTrades.ItemListing;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraftforge.registries.ForgeRegistries;
import pokecube.core.PokecubeCore;
import pokecube.core.database.Database;
import pokecube.core.interfaces.PokecubeMod;
import pokecube.core.inventory.healer.HealerContainer;
import pokecube.core.network.packets.PacketChoose;
import pokecube.core.utils.PokecubeSerializer;
import thut.core.common.ThutCore;

public class NpcType
{
    public static interface IInteract
    {
        boolean processInteract(final Player player, final InteractionHand hand, NpcMob mob);

        default IInteract and(final IInteract other)
        {
            Objects.requireNonNull(other);
            return (p, h, m) -> this.processInteract(p, h, m) && other.processInteract(p, h, m);
        }

        default IInteract or(final IInteract other)
        {
            Objects.requireNonNull(other);
            return (p, h, m) -> this.processInteract(p, h, m) || other.processInteract(p, h, m);
        }
    }

    public static final Map<String, Int2ObjectMap<ItemListing[]>> TRADE_MAP = Maps.newHashMap();

    private static final ItemListing[] EMPTY_LISTING = new ItemListing[0];

    public static final String DATALOC = "database/trainer";

    public static final Map<String, NpcType> typeMap = Maps.newHashMap();

    private static final NpcType PROFESSOR = new NpcType("professor");
    private static final NpcType HEALER = new NpcType("healer");
    private static final NpcType TRADER = new NpcType("trader");
    private static final NpcType NONE = new NpcType("none");

    static
    {
        // Initialize a "none" type, this will be the default return unless
        // something else overrides by constructing another type by name "none"
        final IInteract trade = (player, hand, mob) -> {
            if (player.isShiftKeyDown()) return false;
            final boolean validCustomer = mob.getTradingPlayer() == null;
            if (validCustomer && !mob.getOffers().isEmpty())
            {
                if (mob.getTradingPlayer() == player) return true;
                if (!player.level.isClientSide())
                {
                    mob.setTradingPlayer(player);
                    mob.openTradingScreen(player, mob.getDisplayName(), mob.getVillagerData().getLevel());
                }
                return true;
            }
            return false;
        };
        final IInteract starter = (player, hand, mob) -> {
            if (player instanceof ServerPlayer && !PokecubeSerializer.getInstance().hasStarter(player))
            {
                if (player.isShiftKeyDown()) return false;
                PacketChoose packet;
                final boolean special = false;
                final boolean pick = false;
                packet = PacketChoose.createOpenPacket(special, pick, Database.getStarters());
                PokecubeCore.packets.sendTo(packet, (ServerPlayer) player);
                return true;
            }
            return false;
        };
        final IInteract heal = (player, hand, mob) -> {
            if (player.isShiftKeyDown()) return false;
            if (player instanceof ServerPlayer) player
                    .openMenu(new SimpleMenuProvider(
                            (id, playerInventory, playerIn) -> new HealerContainer(id, playerInventory,
                                    ContainerLevelAccess.create(mob.level, mob.blockPosition())),
                            player.getDisplayName()));
            return true;
        };
        // Initialize the interactions for these defaults.
        NpcType.HEALER.setInteraction(heal);
        NpcType.TRADER.setInteraction(trade);
        NpcType.PROFESSOR.setInteraction(starter.or(trade));
        NpcType.NONE.setProfession(VillagerProfession.NONE);
    }

    public static NpcType byType(String string)
    {
        if (NpcType.typeMap.containsKey(string = ThutCore.trim(string))) return NpcType.typeMap.get(string);
        return NpcType.typeMap.get("none");
    }

    public static void addTrade(String type, int level, ItemListing[] trades, boolean replace)
    {
        ItemListing[] old = getOldTrades(type, level);
        if (old != null && !replace) trades = NpcType.join(old, trades);
        Int2ObjectMap<ItemListing[]> trade_map = TRADE_MAP.get(type);
        if (trade_map == null) TRADE_MAP.put(type, trade_map = new Int2ObjectOpenHashMap<>());
        trade_map.put(level, trades);
    }

    @Nullable
    private static ItemListing[] getOldTrades(String type, int level)
    {
        if (!TRADE_MAP.containsKey(type)) return null;
        return TRADE_MAP.get(type).getOrDefault(level, null);
    }

    public static ItemListing[] join(ItemListing[]... listings)
    {
        List<ItemListing> list = new ArrayList<>();
        for (ItemListing[] listing : listings) for (ItemListing l : listing) list.add(l);
        return list.toArray(new ItemListing[list.size()]);
    }

    private final String name;
    private ResourceLocation maleTex;
    private ResourceLocation femaleTex;

    // This is nitwit, as if it is none, the villagerentity super class
    // completely prevents trades
    private VillagerProfession profession;

    public Set<ResourceLocation> tags = Sets.newHashSet();

    private IInteract interaction = (p, h, mob) -> false;

    public NpcType(String string)
    {
        this.name = string;
        NpcType.typeMap.put(string = ThutCore.trim(string), this);

        // We will set these as a default here, sub-classes can replace them
        // later, or by calling their setters.
        this.maleTex = new ResourceLocation(PokecubeMod.ID + ":textures/entity/" + string + "_male.png");
        this.femaleTex = new ResourceLocation(PokecubeMod.ID + ":textures/entity/" + string + "_female.png");

        if (ForgeRegistries.PROFESSIONS.containsKey(new ResourceLocation(string)))
        {
            profession = ForgeRegistries.PROFESSIONS.getValue(new ResourceLocation(string));
        }
        else
        {
            profession = VillagerProfession.NITWIT;
        }

        Int2ObjectMap<ItemListing[]> trades = new Int2ObjectOpenHashMap<>();
        for (int i = 1; i < 6; i++)
        {
            trades.put(i, new ItemListing[0]);
        }
        TRADE_MAP.put(name, trades);
    }

    /**
     * @param maleTex the maleTex to set
     */
    public NpcType setMaleTex(final ResourceLocation maleTex)
    {
        this.maleTex = maleTex;
        return this;
    }

    /**
     * @param femaleTex the femaleTex to set
     */
    public NpcType setFemaleTex(final ResourceLocation femaleTex)
    {
        this.femaleTex = femaleTex;
        return this;
    }

    /**
     * @param interaction the interaction to set
     */
    public NpcType setInteraction(final IInteract interaction)
    {
        this.interaction = interaction;
        return this;
    }

    /** @return the name */
    public String getName()
    {
        return this.name;
    }

    /** @return the maleTex */
    public ResourceLocation getMaleTex()
    {
        return this.maleTex;
    }

    /** @return the femaleTex */
    public ResourceLocation getFemaleTex()
    {
        return this.femaleTex;
    }

    /** @return the interaction */
    public IInteract getInteraction()
    {
        return this.interaction;
    }

    public VillagerProfession getProfession()
    {
        return this.profession;
    }

    public void setProfession(final VillagerProfession profession)
    {
        this.profession = profession;
    }

    public ItemListing[] getTrades(int level)
    {
        if (!TRADE_MAP.containsKey(name)) return EMPTY_LISTING;
        return TRADE_MAP.get(name).getOrDefault(level, EMPTY_LISTING);
    }

    public boolean hasTrades(int level)
    {
        if (TRADE_MAP.get(name) == null) return false;
        /*
         * We have trades in the following cases:
         * 
         * our TRADE_MAP tells us we have trades
         * 
         * The vanilla TRADES map tells us we have trades
         * 
         */
        return TRADE_MAP.get(name).get(level).length > 0 || (VillagerTrades.TRADES.containsKey(this.getProfession())
                && VillagerTrades.TRADES.get(this.getProfession()).get(level) != null
                && VillagerTrades.TRADES.get(this.getProfession()).get(level).length > 0);
    }
}
