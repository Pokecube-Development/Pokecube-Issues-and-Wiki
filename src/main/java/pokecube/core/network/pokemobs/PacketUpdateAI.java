package pokecube.core.network.pokemobs;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.common.util.INBTSerializable;
import pokecube.core.PokecubeCore;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.capabilities.CapabilityPokemob;
import thut.api.entity.ai.IAIRunnable;
import thut.core.common.network.Packet;

public class PacketUpdateAI extends Packet
{
    public static void sendUpdatePacket(final IPokemob pokemob, final IAIRunnable ai)
    {
        final CompoundNBT tag = new CompoundNBT();
        final INBT base = INBTSerializable.class.cast(ai).serializeNBT();
        tag.put(ai.getIdentifier(), base);
        final PacketUpdateAI packet = new PacketUpdateAI();
        packet.data = tag;
        packet.entityId = pokemob.getEntity().getId();
        PokecubeCore.packets.sendToServer(packet);
    }

    public int entityId;

    public CompoundNBT data = new CompoundNBT();

    public PacketUpdateAI()
    {
    }

    public PacketUpdateAI(final PacketBuffer buffer)
    {
        this.entityId = buffer.readInt();
        this.data = buffer.readNbt();
    }

    @SuppressWarnings("unchecked")
    @Override
    public void handleServer(final ServerPlayerEntity player)
    {
        final int id = this.entityId;
        final CompoundNBT data = this.data;
        final Entity e = PokecubeCore.getEntityProvider().getEntity(player.getCommandSenderWorld(), id, true);
        final IPokemob pokemob = CapabilityPokemob.getPokemobFor(e);
        if (pokemob != null) for (final IAIRunnable runnable : pokemob.getTasks())
            if (runnable instanceof INBTSerializable && data.contains(runnable.getIdentifier()))
            {
                INBTSerializable.class.cast(runnable).deserializeNBT(data.get(runnable.getIdentifier()));
                break;
            }
    }

    @Override
    public void write(final PacketBuffer buffer)
    {
        buffer.writeInt(this.entityId);
        buffer.writeNbt(this.data);
    }
}
