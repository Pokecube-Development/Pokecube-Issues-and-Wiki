package pokecube.core.network.pokemobs;

import net.minecraft.SharedConstants;
import net.minecraft.Util;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import pokecube.core.PokecubeCore;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.capabilities.CapabilityPokemob;
import thut.core.common.network.Packet;

public class PacketNickname extends Packet
{
    public static void sendPacket(final Entity mob, final String name)
    {
        final PacketNickname packet = new PacketNickname();
        packet.entityId = mob.getId();
        packet.name = name;
        PokecubeCore.packets.sendToServer(packet);
    }

    int    entityId;
    String name;

    public PacketNickname()
    {
    }

    public PacketNickname(final FriendlyByteBuf buf)
    {
        final FriendlyByteBuf buffer = new FriendlyByteBuf(buf);
        this.entityId = buffer.readInt();
        this.name = buffer.readUtf(20);
    }

    @Override
    public void handleServer(final ServerPlayer player)
    {

        final Entity mob = PokecubeCore.getEntityProvider().getEntity(player.getCommandSenderWorld(), this.entityId, true);
        final IPokemob pokemob = CapabilityPokemob.getPokemobFor(mob);
        if (pokemob == null) return;
        final String name = SharedConstants.filterText(new String(this.name));
        if (pokemob.getDisplayName().getString().equals(name)) return;
        boolean OT = pokemob.getOwnerId() == null || pokemob.getOriginalOwnerUUID() == null || pokemob
                .getOwnerId().equals(pokemob.getOriginalOwnerUUID());
        if (!OT && pokemob.getOwner() != null) OT = pokemob.getOwner().getUUID().equals(pokemob
                .getOriginalOwnerUUID());
        if (!OT)
        {
            if (pokemob.getOwner() != null) pokemob.getOwner().sendMessage(new TranslatableComponent(
                    "pokemob.rename.deny"), Util.NIL_UUID);
        }
        else
        {
            pokemob.getOwner().sendMessage(new TranslatableComponent("pokemob.rename.success", pokemob
                    .getDisplayName().getString(), name), Util.NIL_UUID);
            pokemob.setPokemonNickname(name);
        }
    }

    @Override
    public void write(final FriendlyByteBuf buf)
    {
        final FriendlyByteBuf buffer = new FriendlyByteBuf(buf);
        buffer.writeInt(this.entityId);
        buffer.writeUtf(this.name);
    }
}