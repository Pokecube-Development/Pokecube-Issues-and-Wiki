package pokecube.core.network.pokemobs;

import net.minecraft.SharedConstants;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import pokecube.api.PokecubeAPI;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.entity.pokemob.PokemobCaps;
import pokecube.core.PokecubeCore;
import thut.core.common.network.Packet;
import thut.lib.TComponent;

public class PacketNickname extends Packet
{
    public static void sendPacket(final Entity mob, final String name)
    {
        final PacketNickname packet = new PacketNickname();
        packet.entityId = mob.getId();
        packet.name = name;
        PokecubeCore.packets.sendToServer(packet);
    }

    int entityId;
    String name;

    public PacketNickname()
    {}

    public PacketNickname(final FriendlyByteBuf buf)
    {
        final FriendlyByteBuf buffer = new FriendlyByteBuf(buf);
        this.entityId = buffer.readInt();
        this.name = buffer.readUtf(20);
    }

    @Override
    public void handleServer(final ServerPlayer player)
    {

        final Entity mob = PokecubeAPI.getEntityProvider().getEntity(player.getLevel(), this.entityId, true);
        final IPokemob pokemob = PokemobCaps.getPokemobFor(mob);
        if (pokemob == null) return;
        final String name = SharedConstants.filterText(new String(this.name));
        if (pokemob.getDisplayName().getString().equals(name)) return;
        boolean OT = pokemob.getOwnerId() == null || pokemob.getOriginalOwnerUUID() == null
                || pokemob.getOwnerId().equals(pokemob.getOriginalOwnerUUID());
        if (!OT && pokemob.getOwner() != null) OT = pokemob.getOwner().getUUID().equals(pokemob.getOriginalOwnerUUID());
        if (!OT)
        {
            thut.lib.ChatHelper.sendSystemMessage(player, TComponent.translatable("pokemob.rename.deny"));
        }
        else
        {
            thut.lib.ChatHelper.sendSystemMessage(player,
                    TComponent.translatable("pokemob.rename.success", pokemob.getDisplayName().getString(), name));
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