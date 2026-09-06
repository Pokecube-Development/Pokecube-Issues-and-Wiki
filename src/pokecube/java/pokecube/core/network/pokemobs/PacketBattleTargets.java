package pokecube.core.network.pokemobs;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import pokecube.api.PokecubeAPI;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.entity.pokemob.PokemobCaps;
import pokecube.api.events.combat.ExitBattleEvent;
import pokecube.core.PokecubeCore;
import pokecube.core.ai.brain.BrainUtils;
import pokecube.core.network.packets.PacketSyncBattle;
import thut.core.common.ThutCore;
import thut.core.common.network.Packet;

public class PacketBattleTargets extends Packet
{
    public static int manualTargetIndex;
    public static int manualAllyIndex;

    public static void cycleAlly(IPokemob pokemob, boolean up)
    {
        manualAllyIndex += (up ? 1 : -1);
        var list = PacketSyncBattle.getAllies();
        if(list.isEmpty()) return;
        int n = list.size();
        manualAllyIndex %= n;
        if (manualAllyIndex < 0) manualAllyIndex += n;
        if (pokemob == null)
        {
            return;
        }
        var entity = list.get(manualAllyIndex);
        pokemob.getMoveStats().targetAlly = entity;
        int id = entity.getId();
        PokecubeCore.packets.sendToServer(new PacketBattleTargets(pokemob.getEntity().getId(), TYPE_ALLY, id));
    }

    public static void cycleEnemy(IPokemob pokemob, boolean up)
    {
        manualTargetIndex += (up ? 1 : -1);
        var list = PacketSyncBattle.getEnemies();
        if(list.isEmpty()) return;
        int n = list.size();
        manualTargetIndex %= n;
        if (manualTargetIndex < 0) manualTargetIndex += n;
        if (pokemob == null)
        {
            return;
        }
        var entity = list.get(manualTargetIndex);
        pokemob.getMoveStats().targetEnemy = entity;
        int id = entity.getId();
        PokecubeCore.packets.sendToServer(new PacketBattleTargets(pokemob.getEntity().getId(), TYPE_ENEMY, id));
    }

    public static void yieldBattle(IPokemob pokemob)
    {
        if (pokemob == null)
        {
            // TODO decide if we want to handle this?
            return;
        }
        int targetId = pokemob.getTargetID();
        PokecubeCore.packets.sendToServer(new PacketBattleTargets(pokemob.getEntity().getId(), TYPE_YIELD, targetId));
    }

    private static final byte TYPE_ALLY = 1;
    private static final byte TYPE_ENEMY = 2;
    private static final byte TYPE_YIELD = 3;

    public int entityId;
    public byte type;
    public int order;

    public PacketBattleTargets()
    {}

    public PacketBattleTargets(int id, byte type, int order)
    {
        this.entityId = id;
        this.type = type;
        this.order = order;
    }

    public void read(final FriendlyByteBuf buffer)
    {
        this.entityId = buffer.readInt();
        this.type = buffer.readByte();
        this.order = buffer.readInt();
    }

    @Override
    public void handleServer(final ServerPlayer player)
    {
        int id = this.entityId;
        Entity e = PokecubeAPI.getEntityProvider().getEntity(player.level(), id, true);
        final IPokemob pokemob = PokemobCaps.getPokemobFor(e);
        if (pokemob == null || player != pokemob.getOwner()) return;
        switch (type)
        {
        // The actual setting of the IDs from this gets done in LogicMiscUpdate,
        // so we just handle changing the index here.
        case TYPE_ALLY:
            pokemob.setAllyID(order);
            break;
        case TYPE_ENEMY:
            pokemob.setTargetID(order);
            break;
        case TYPE_YIELD:
            // Attempt to remove the target from the battle
            var battle = pokemob.getBattle();
            if (battle != null)
            {
                id = pokemob.getTargetID();
                e = PokecubeAPI.getEntity(e.level(), id);
                if (e instanceof LivingEntity living)
                {
                    ExitBattleEvent event = new ExitBattleEvent(pokemob.getEntity(), living, battle);
                    ThutCore.FORGE_BUS.post(event);
                    if (!event.isCanceled())
                    {
                        BrainUtils.clearAttackTarget(pokemob.getEntity());
                        BrainUtils.clearAttackTarget(living);
                        battle.removeFromBattle(living);
                    }
                }
            }
            break;
        default:
        }
    }

    @Override
    public void write(final FriendlyByteBuf buffer)
    {
        buffer.writeInt(this.entityId);
        buffer.writeByte(type);
        buffer.writeInt(order);
    }

    private final static Type<Packet> TYPE = new Type<Packet>(ResourceLocation.parse("pokecube:battle_targets"));

    @Override
    public Type<? extends CustomPacketPayload> type()
    {
        return TYPE;
    }
}
