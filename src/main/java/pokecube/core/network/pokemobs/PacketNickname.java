package pokecube.core.network.pokemobs;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.SharedConstants;
import net.minecraft.util.Util;
import net.minecraft.util.text.TranslationTextComponent;
import pokecube.core.PokecubeCore;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.capabilities.CapabilityPokemob;
import thut.core.common.network.Packet;

public class PacketNickname extends Packet
{
    public static void sendPacket(final Entity mob, final String name)
    {
        final PacketNickname packet = new PacketNickname();
        packet.entityId = mob.getEntityId();
        packet.name = name;
        PokecubeCore.packets.sendToServer(packet);
    }

    int    entityId;
    String name;

    public PacketNickname()
    {
    }

    public PacketNickname(final PacketBuffer buf)
    {
        final PacketBuffer buffer = new PacketBuffer(buf);
        this.entityId = buffer.readInt();
        this.name = buffer.readString(20);
    }

    @Override
    public void handleServer(final ServerPlayerEntity player)
    {

        final Entity mob = PokecubeCore.getEntityProvider().getEntity(player.getEntityWorld(), this.entityId, true);
        final IPokemob pokemob = CapabilityPokemob.getPokemobFor(mob);
        if (pokemob == null) return;
        final String name = SharedConstants.filterAllowedCharacters(new String(this.name));
        if (pokemob.getDisplayName().getString().equals(name)) return;
        boolean OT = pokemob.getOwnerId() == null || pokemob.getOriginalOwnerUUID() == null || pokemob
                .getOwnerId().equals(pokemob.getOriginalOwnerUUID());
        if (!OT && pokemob.getOwner() != null) OT = pokemob.getOwner().getUniqueID().equals(pokemob
                .getOriginalOwnerUUID());
        if (!OT)
        {
            if (pokemob.getOwner() != null) pokemob.getOwner().sendMessage(new TranslationTextComponent(
                    "pokemob.rename.deny"), Util.DUMMY_UUID);
        }
        else
        {
            pokemob.getOwner().sendMessage(new TranslationTextComponent("pokemob.rename.success", pokemob
                    .getDisplayName().getString(), name), Util.DUMMY_UUID);
            pokemob.setPokemonNickname(name);
        }
    }

    @Override
    public void write(final PacketBuffer buf)
    {
        final PacketBuffer buffer = new PacketBuffer(buf);
        buffer.writeInt(this.entityId);
        buffer.writeString(this.name);
    }
}