package pokecube.core.network.pokemobs;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import pokecube.core.PokecubeCore;
import thut.core.common.network.Packet;

public class PacketPokemobMessage extends Packet
{
    public static void sendMessage(final PlayerEntity sendTo, final ITextComponent message)
    {
        final PacketPokemobMessage toSend = new PacketPokemobMessage(message);
        PokecubeCore.packets.sendTo(toSend, (ServerPlayerEntity) sendTo);
    }

    ITextComponent message;

    public PacketPokemobMessage()
    {
    }

    public PacketPokemobMessage(final ITextComponent message)
    {
        this.message = message;
    }

    public PacketPokemobMessage(final PacketBuffer buf)
    {
        final PacketBuffer buffer = new PacketBuffer(buf);
        this.message = buffer.readComponent();
    }

    @Override
    @OnlyIn(value = Dist.CLIENT)
    public void handleClient()
    {
        final ITextComponent component = this.message;
        pokecube.core.client.gui.GuiInfoMessages.addMessage(component);
    }

    @Override
    public void write(final PacketBuffer buf)
    {
        final PacketBuffer buffer = new PacketBuffer(buf);
        buffer.writeComponent(this.message);
    }
}
