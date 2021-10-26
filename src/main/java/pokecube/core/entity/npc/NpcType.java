package pokecube.core.entity.npc;

import java.util.Map;
import java.util.Objects;
import java.util.Set;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerLevelAccess;
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

    public static final String DATALOC = "database/trainer";

    public static final Map<String, NpcType> typeMap = Maps.newHashMap();

    private static final NpcType PROFESSOR = new NpcType("professor");
    private static final NpcType HEALER    = new NpcType("healer");
    private static final NpcType TRADER    = new NpcType("trader");

    static
    {
        // Initialize a "none" type, this will be the default return unless
        // something else overrides by constructing another type by name "none"
        new NpcType("none");
        final IInteract trade = (player, hand, mob) ->
        {
            if (player.isShiftKeyDown()) return false;
            final boolean validCustomer = mob.getTradingPlayer() == null;
            if (validCustomer && !mob.getOffers().isEmpty())
            {
                if (mob.getTradingPlayer() == player) return true;
                mob.setTradingPlayer(player);
                mob.openTradingScreen(player, mob.getDisplayName(), 10);
                return true;
            }
            return false;
        };
        final IInteract starter = (player, hand, mob) ->
        {
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
        final IInteract heal = (player, hand, mob) ->
        {
            if (player.isShiftKeyDown()) return false;
            if (player instanceof ServerPlayer) player.openMenu(new SimpleMenuProvider((id,
                    playerInventory, playerIn) -> new HealerContainer(id, playerInventory, ContainerLevelAccess.create(
                            mob.level, mob.blockPosition())), player.getDisplayName()));
            return true;
        };
        // Initialize the interactions for these defaults.
        NpcType.HEALER.setInteraction(heal);
        NpcType.TRADER.setInteraction(trade);
        NpcType.PROFESSOR.setInteraction(starter.or(trade));
    }

    public static NpcType byType(String string)
    {
        if (NpcType.typeMap.containsKey(string = ThutCore.trim(string))) return NpcType.typeMap.get(string);
        return NpcType.typeMap.get("none");
    }

    private final String     name;
    private ResourceLocation maleTex;
    private ResourceLocation femaleTex;

    // This is nitwit, as if it is none, the villagerentity super class
    // completely prevents trades
    private VillagerProfession profession = VillagerProfession.NITWIT;

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
    }

    /**
     * @param maleTex
     *            the maleTex to set
     */
    public NpcType setMaleTex(final ResourceLocation maleTex)
    {
        this.maleTex = maleTex;
        return this;
    }

    /**
     * @param femaleTex
     *            the femaleTex to set
     */
    public NpcType setFemaleTex(final ResourceLocation femaleTex)
    {
        this.femaleTex = femaleTex;
        return this;
    }

    /**
     * @param interaction
     *            the interaction to set
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
}
