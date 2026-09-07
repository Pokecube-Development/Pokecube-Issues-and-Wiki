package pokecube.core.network.pokemobs;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import pokecube.api.PokecubeAPI;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.entity.pokemob.PokemobCaps;
import pokecube.api.events.combat.ExitBattleEvent;
import pokecube.api.moves.Battle;
import pokecube.core.PokecubeCore;
import pokecube.core.ai.brain.BrainUtils;
import pokecube.core.network.packets.PacketSyncBattle;
import thut.api.Tracker;
import thut.core.common.ThutCore;
import thut.core.common.network.Packet;

public class PacketBattleTargets extends Packet
{
    public static int manualTargetIndex;
    public static int manualAllyIndex;

    public static long sentEnemyTick = -1;
    public static long sentAllyTick = -1;

    public static long recvEnemyTick = -1;
    public static long recvAllyTick = -1;

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
        setAlly(pokemob, entity);
    }

    public static void cycleEnemy(IPokemob pokemob, boolean up)
    {
        int i = manualTargetIndex;
        i += (up ? 1 : -1);
        var list = PacketSyncBattle.getEnemies();
        if(list.isEmpty()) return;
        int n = list.size();
        i %= n;
        if (i < 0) i += n;
        if (pokemob == null)
        {
            return;
        }
        var entity = list.get(i);
        setEnemy(pokemob, entity);
    }

    public static void setEnemy(IPokemob pokemob, LivingEntity entity)
    {
        long tick = Tracker.instance().getTick();
        if (tick == sentEnemyTick) return;
        if (!entity.level().isClientSide()) return;
        if (!(pokemob.getOwner() instanceof Player)) return;
        sentEnemyTick = tick;
        int id = entity.getId();
        PokecubeCore.packets.sendToServer(new PacketBattleTargets(pokemob.getEntity().getId(), TYPE_ENEMY, id));
    }

    public static void setAlly(IPokemob pokemob, LivingEntity entity)
    {
        long tick = Tracker.instance().getTick();
        if (tick == sentAllyTick) return;
        if (!entity.level().isClientSide()) return;
        if (!(pokemob.getOwner() instanceof Player)) return;
        sentAllyTick = tick;
        int id = entity.getId();
        PokecubeCore.packets.sendToServer(new PacketBattleTargets(pokemob.getEntity().getId(), TYPE_ALLY, id));
    }

    public static void yieldBattle(IPokemob pokemob)
    {
        if (pokemob == null || pokemob.getMoveStats().getTargetEnemy() == null)
        {
            // TODO decide if we want to handle this for not pokemobs?
            var list = PacketSyncBattle.getEnemies();
            int n = list.size();
            int i = manualTargetIndex;
            i %= n;
            var target = list.get(i);
            pokemob = PokemobCaps.getPokemobFor(target);
            if (pokemob != null && pokemob.getPokedexEntry().stock)
            {
                int targetId = target.getId();
                PokecubeCore.packets.sendToServer(new PacketBattleTargets(-1, TYPE_YIELD, targetId));
            }
            return;
        }
        int targetId = pokemob.getMoveStats().getTargetEnemy().getId();
        PokecubeCore.packets.sendToServer(new PacketBattleTargets(pokemob.getEntity().getId(), TYPE_YIELD, targetId));
    }

    public static void sentToClient(ServerPlayer player, IPokemob pokemob, boolean enemy)
    {
        var target = enemy ? pokemob.getMoveStats().getTargetEnemy() : pokemob.getMoveStats().getTargetAlly();
        byte key = enemy ? TYPE_ENEMY : TYPE_ALLY;
        int id = target != null ? target.getId() : -1;
        var packet = new PacketBattleTargets(pokemob.getEntity().getId(), key, id);
        PokecubeCore.packets.sendTo(packet, player);
    }

    private static final byte TYPE_ALLY = 1;
    private static final byte TYPE_ENEMY = 2;
    private static final byte TYPE_YIELD = 3;

    public int entityId;
    public byte type;
    public int order;

    public PacketBattleTargets()
    {}

    private PacketBattleTargets(int id, byte type, int order)
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
    public void handleClient(Player player)
    {
        int id = this.entityId;
        Entity e = id == -1 ? player : PokecubeAPI.getEntityProvider().getEntity(player.level(), id, true);
        final IPokemob pokemob = PokemobCaps.getPokemobFor(e);
        Entity e2;
        switch (type)
        {
        case TYPE_ALLY:
            recvAllyTick = Tracker.instance().getTick();
            e2 = PokecubeAPI.getEntityProvider().getEntity(player.level(), order, false);
            if (pokemob != null && pokemob.getOwner() == player)
            {
                if (e2 instanceof LivingEntity living)
                {
                    pokemob.getMoveStats().setTargetAlly(living);
                }
                else pokemob.getMoveStats().setTargetAlly(null);
            }
            break;
        case TYPE_ENEMY:
            recvEnemyTick = Tracker.instance().getTick();
            e2 = PokecubeAPI.getEntityProvider().getEntity(player.level(), order, false);
            if (pokemob != null && pokemob.getOwner() == player)
            {
                if (e2 instanceof LivingEntity living)
                {
                    pokemob.getMoveStats().setTargetEnemy(living);
                }
                else
                {
                    pokemob.getMoveStats().setTargetEnemy(null);
                }
            }
            if (e2 instanceof LivingEntity living)
            {
                manualTargetIndex = PacketSyncBattle.getEnemies().indexOf(living);
                if (manualTargetIndex == -1) manualTargetIndex = 0;
            }
            else manualTargetIndex = 0;
            break;
        default:
        }
    }

    @Override
    public void handleServer(final ServerPlayer player)
    {
        int id = this.entityId;
        Entity e = id == -1 ? player : PokecubeAPI.getEntityProvider().getEntity(player.level(), id, true);
        IPokemob pokemob = PokemobCaps.getPokemobFor(e);
        Entity e2;
        if (pokemob == null || player != pokemob.getOwner())
        {
            var battle = Battle.getBattle(player);
            if (e == player && type == TYPE_YIELD && battle != null)
            {
                e2 = PokecubeAPI.getEntityProvider().getEntity(player.level(), order, false);
                if (e2 instanceof LivingEntity living)
                {
                    pokemob = PokemobCaps.getPokemobFor(living);
                    // TODO decide if we want to handle this for not pokemobs?
                    if (pokemob == null || !pokemob.getPokedexEntry().stock) return;

                    ExitBattleEvent event = new ExitBattleEvent(player, living, battle);
                    ThutCore.FORGE_BUS.post(event);
                    if (!event.isCanceled())
                    {
                        BrainUtils.clearAttackTarget(player);
                        BrainUtils.clearAttackTarget(living);
                        battle.removeFromBattle(living);
                    }
                }
            }
            return;
        }
        switch (type)
        {
        case TYPE_ALLY:
            e2 = PokecubeAPI.getEntityProvider().getEntity(player.level(), order, false);
            if (e2 instanceof LivingEntity living)
            {
                pokemob.getMoveStats().setTargetAlly(living);
            }
            else pokemob.getMoveStats().setTargetAlly(null);
            break;
        case TYPE_ENEMY:
            e2 = PokecubeAPI.getEntityProvider().getEntity(player.level(), order, false);
            if (e2 instanceof LivingEntity living)
            {
                pokemob.getMoveStats().setTargetEnemy(living);
            }
            else pokemob.getMoveStats().setTargetEnemy(null);
            break;
        case TYPE_YIELD:
            // Attempt to remove the target from the battle
            var battle = pokemob.getBattle();
            if (battle != null)
            {
                e2 = PokecubeAPI.getEntityProvider().getEntity(player.level(), order, false);
                if (e2 instanceof LivingEntity living)
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
