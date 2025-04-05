package pokecube.core.network.pokemobs;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.network.connection.ConnectionType;
import pokecube.core.PokecubeCore;
import thut.core.common.network.Packet;

public class PacketPokemobMessage extends Packet
{
    public static void sendMessage(final Player sendTo, final Component message)
    {
        final PacketPokemobMessage toSend = new PacketPokemobMessage(message);
        PokecubeCore.packets.sendTo(toSend, (ServerPlayer) sendTo);
    }

    Component message;

    public PacketPokemobMessage()
    {}

    public PacketPokemobMessage(final Component message)
    {
        this.message = message;
    }

    public void read(final FriendlyByteBuf buf)
    {
        var buffer = new RegistryFriendlyByteBuf(buf, PokecubeCore.proxy.getRegistries(), ConnectionType.NEOFORGE);
        this.message = ComponentSerialization.STREAM_CODEC.decode(buffer);
    }

    @Override
    @OnlyIn(value = Dist.CLIENT)
    public void handleClient(Player player)
    {
        final Component component = this.message;
        pokecube.core.client.gui.GuiInfoMessages.addMessage(component);
    }

    @Override
    public void write(final FriendlyByteBuf buf)
    {
        var buffer = new RegistryFriendlyByteBuf(buf, PokecubeCore.proxy.getRegistries(), ConnectionType.NEOFORGE);
        ComponentSerialization.STREAM_CODEC.encode(buffer, this.message);
    }

    private final static Type<Packet> TYPE = new Type<Packet>(ResourceLocation.parse("pokecube:pokemob_messages"));

    @Override
    public Type<? extends CustomPacketPayload> type()
    {
        return TYPE;
    }
}
