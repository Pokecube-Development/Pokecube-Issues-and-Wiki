package pokecube.core.network.packets;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import pokecube.api.events.TMMachineEvent;
import pokecube.core.PokecubeCore;
import pokecube.core.inventory.tms.TMContainer;
import pokecube.core.moves.MovesUtils;
import thut.core.common.network.Packet;

public class PacketTMs extends Packet
{
    public CompoundTag data = new CompoundTag();

    public static void sendApplyMove(String move)
    {
        PacketTMs packet = new PacketTMs();
        packet.data.putString("m", move);
        PokecubeCore.packets.sendToServer(packet);
    }

    public static void sendMovesList(TMMachineEvent event)
    {
        var player = event.player;
        PacketTMs packet = new PacketTMs();
        ListTag list = new ListTag();
        event.moves.removeIf(m -> !MovesUtils.isMoveImplemented(m));
        for (String s : event.moves) list.add(StringTag.valueOf(s));
        packet.data.put("l", list);
        PokecubeCore.packets.sendTo(packet, player);
    }

    public PacketTMs()
    {}

    public void read(final FriendlyByteBuf buf)
    {
        this.data = buf.readNbt();
    }

    @Override
    public void handleServer(final ServerPlayer player)
    {
        final AbstractContainerMenu cont = player.containerMenu;
        if (!(cont instanceof TMContainer container)) return;
        var moves = container.moves;
        String selected = this.data.getString("m");
        if(moves.contains(selected)) return;
        container.getInv().setItem(0, container.tile.addMoveToTM(selected, container.getInv().getItem(0)));
    }

    @Override
    public void handleClient(Player player)
    {
        final AbstractContainerMenu cont = player.containerMenu;
        if (!(cont instanceof TMContainer container)) return;
        var list = this.data.getList("l", StringTag.TAG_STRING);
        synchronized (container.moves)
        {
            container.moves.clear();
            list.forEach(t -> container.moves.add(t.getAsString()));
        }
    }

    @Override
    public void write(final FriendlyByteBuf buf)
    {
        final FriendlyByteBuf buffer = new FriendlyByteBuf(buf);
        buffer.writeNbt(this.data);
    }

    private final static Type<Packet> TYPE = new Type<>(ResourceLocation.parse("pokecube:use_tm_machine"));

    @Override
    public Type<? extends CustomPacketPayload> type()
    {
        return TYPE;
    }
}
