package pokecube.core.network.pokemobs;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import pokecube.core.PokecubeCore;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.capabilities.CapabilityPokemob;
import thut.core.common.network.Packet;

public class PacketPokemobMessage extends Packet
{
    public static void sendMessage(final PlayerEntity sendTo, final int senderId, final ITextComponent message)
    {
        final PacketPokemobMessage toSend = new PacketPokemobMessage(message, senderId);
        PokecubeCore.packets.sendTo(toSend, (ServerPlayerEntity) sendTo);
    }

    ITextComponent message;
    int            senderId;

    public PacketPokemobMessage()
    {
    }

    public PacketPokemobMessage(final ITextComponent message, final int senderId)
    {
        this.message = message;
        this.senderId = senderId;
    }

    public PacketPokemobMessage(final PacketBuffer buf)
    {
        final PacketBuffer buffer = new PacketBuffer(buf);
        this.senderId = buffer.readInt();
        this.message = buffer.readTextComponent();
    }

    @Override
    @OnlyIn(value = Dist.CLIENT)
    public void handleClient()
    {
        final PlayerEntity player = PokecubeCore.proxy.getPlayer();
        final int id = this.senderId;
        final ITextComponent component = this.message;
        final Entity e = PokecubeCore.getEntityProvider().getEntity(player.getEntityWorld(), id, false);
        final IPokemob pokemob = CapabilityPokemob.getPokemobFor(e);
        if (pokemob != null) pokemob.displayMessageToOwner(component);
        else if (e == player) pokecube.core.client.gui.GuiInfoMessages.addMessage(component);
    }

    @Override
    public void write(final PacketBuffer buf)
    {
        final PacketBuffer buffer = new PacketBuffer(buf);
        buffer.writeInt(this.senderId);
        buffer.writeTextComponent(this.message);
    }
}
