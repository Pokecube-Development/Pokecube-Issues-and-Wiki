package pokecube.core.network.pokemobs;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
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
    {
    }

    public PacketPokemobMessage(final Component message)
    {
        this.message = message;
    }

    public PacketPokemobMessage(final FriendlyByteBuf buf)
    {
        final FriendlyByteBuf buffer = new FriendlyByteBuf(buf);
        this.message = buffer.readComponent();
    }

    @Override
    @OnlyIn(value = Dist.CLIENT)
    public void handleClient()
    {
        final Component component = this.message;
        pokecube.core.client.gui.GuiInfoMessages.addMessage(component);
    }

    @Override
    public void write(final FriendlyByteBuf buf)
    {
        final FriendlyByteBuf buffer = new FriendlyByteBuf(buf);
        buffer.writeComponent(this.message);
    }
}
