package pokecube.core.network.packets;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import pokecube.api.moves.Battle;
import pokecube.core.PokecubeCore;
import thut.api.entity.EntityProvider;
import thut.core.common.network.Packet;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class PacketSyncBattle extends Packet
{
    private static final List<LivingEntity> OUR_SIDE = new ArrayList<>();
    private static final List<LivingEntity> OTHER_SIDE = new ArrayList<>();

    public static List<LivingEntity> getEnemies()
    {
        List<LivingEntity> ret;
        synchronized (OTHER_SIDE)
        {
            ret = new ArrayList<>(OTHER_SIDE);
        }
        return ret;
    }

    public static List<LivingEntity> getAllies()
    {
        List<LivingEntity> ret;
        synchronized (OUR_SIDE)
        {
            ret = new ArrayList<>(OUR_SIDE);
        }
        return ret;
    }

    public static void reset()
    {
        synchronized (OUR_SIDE)
        {
            OTHER_SIDE.clear();
            OUR_SIDE.clear();
        }
    }

    public static void trySendBattle(Battle battle)
    {
        if (battle.involved_players.isEmpty()) return;
        Set<ServerPlayer> sent = new HashSet<>();
        battle.involved_players.forEach(p -> {
            PacketSyncBattle sync = new PacketSyncBattle();
            p.pside().forEach(e -> sync.our_side.add(e.getId()));
            p.oside().forEach(e -> sync.other_side.add(e.getId()));
            if (sent.add(p.player()))
            {
                PokecubeCore.packets.sendTo(sync, p.player());
            }
        });
    }

    Set<Integer> our_side = new HashSet<>();
    Set<Integer> other_side = new HashSet<>();

    public PacketSyncBattle(){}

    @Override
    public void write(FriendlyByteBuf buffer)
    {
        buffer.writeInt(our_side.size());
        our_side.forEach(buffer::writeInt);
        buffer.writeInt(other_side.size());
        other_side.forEach(buffer::writeInt);
    }

    @Override
    public void read(FriendlyByteBuf buffer)
    {
        int number = buffer.readInt();
        for (int i = 0; i < number; i++) our_side.add(buffer.readInt());
        number = buffer.readInt();
        for (int i = 0; i < number; i++) other_side.add(buffer.readInt());
    }

    @Override
    public void handleClient(Player player)
    {
        synchronized (OUR_SIDE)
        {
            OUR_SIDE.clear();
            OTHER_SIDE.clear();
            var list = new ArrayList<>(this.our_side);
            list.sort(null);
            for (var id : list)
            {
                var e = EntityProvider.provider.getEntity(player.level(), id, false);
                if (e instanceof LivingEntity l) OUR_SIDE.add(l);
            }
            list = new ArrayList<>(this.other_side);
            list.sort(null);
            for (var id : list)
            {
                var e = EntityProvider.provider.getEntity(player.level(), id, false);
                if (e instanceof LivingEntity l) OTHER_SIDE.add(l);
            }
        }
    }

    private final static Type<Packet> TYPE = new Type<>(ResourceLocation.parse("pokecube:sync_battle_contents"));

    @Override
    public Type<? extends CustomPacketPayload> type()
    {
        return TYPE;
    }
}
