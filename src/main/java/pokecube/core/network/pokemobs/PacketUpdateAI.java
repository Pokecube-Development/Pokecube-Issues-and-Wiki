package pokecube.core.network.pokemobs;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.common.util.INBTSerializable;
import pokecube.api.PokecubeAPI;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.entity.pokemob.PokemobCaps;
import pokecube.core.PokecubeCore;
import thut.api.entity.ai.IAIRunnable;
import thut.core.common.network.Packet;

public class PacketUpdateAI extends Packet
{
    public static void sendUpdatePacket(final IPokemob pokemob, final IAIRunnable ai)
    {
        final CompoundTag tag = new CompoundTag();
        final Tag base = INBTSerializable.class.cast(ai).serializeNBT();
        tag.put(ai.getIdentifier(), base);
        final PacketUpdateAI packet = new PacketUpdateAI();
        packet.data = tag;
        packet.entityId = pokemob.getEntity().getId();
        PokecubeCore.packets.sendToServer(packet);
    }

    public int entityId;

    public CompoundTag data = new CompoundTag();

    public PacketUpdateAI()
    {
    }

    public PacketUpdateAI(final FriendlyByteBuf buffer)
    {
        this.entityId = buffer.readInt();
        this.data = buffer.readNbt();
    }

    @SuppressWarnings("unchecked")
    @Override
    public void handleServer(final ServerPlayer player)
    {
        final int id = this.entityId;
        final CompoundTag data = this.data;
        final Entity e = PokecubeAPI.getEntityProvider().getEntity(player.level(), id, true);
        final IPokemob pokemob = PokemobCaps.getPokemobFor(e);
        if (pokemob != null) for (final IAIRunnable runnable : pokemob.getTasks())
            if (runnable instanceof INBTSerializable && data.contains(runnable.getIdentifier()))
            {
                INBTSerializable.class.cast(runnable).deserializeNBT(data.get(runnable.getIdentifier()));
                break;
            }
    }

    @Override
    public void write(final FriendlyByteBuf buffer)
    {
        buffer.writeInt(this.entityId);
        buffer.writeNbt(this.data);
    }
}
