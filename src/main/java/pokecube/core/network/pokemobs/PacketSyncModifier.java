package pokecube.core.network.pokemobs;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketBuffer;
import pokecube.core.PokecubeCore;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.IPokemob.Stats;
import pokecube.core.interfaces.capabilities.CapabilityPokemob;
import pokecube.core.interfaces.pokemob.stats.IStatsModifiers;
import thut.core.common.network.Packet;

public class PacketSyncModifier extends Packet
{
    public static void sendUpdate(final String type, final IPokemob pokemob)
    {
        if (pokemob == null) return;
        if (!pokemob.getEntity().addedToChunk) return;
        final PacketSyncModifier packet = new PacketSyncModifier();
        packet.entityId = pokemob.getEntity().getEntityId();
        packet.modifier = pokemob.getModifiers().indecies.get(type);
        for (final Stats stat : Stats.values())
            packet.values[stat.ordinal()] = pokemob.getModifiers().sortedModifiers.get(packet.modifier).getModifierRaw(
                    stat);
        PokecubeCore.packets.sendToTracking(packet, pokemob.getEntity());
    }

    int   entityId;
    int   modifier;
    float values[] = new float[Stats.values().length];

    public PacketSyncModifier()
    {
    }

    public PacketSyncModifier(final PacketBuffer buf)
    {
        this.entityId = buf.readInt();
        this.modifier = buf.readInt();
        for (int i = 0; i < this.values.length; i++)
            this.values[i] = buf.readFloat();
    }

    @Override
    public void handleClient()
    {
        final PlayerEntity player = PokecubeCore.proxy.getPlayer();
        final int id = this.entityId;
        final int modifier = this.modifier;
        final float[] values = this.values;
        final Entity e = PokecubeCore.getEntityProvider().getEntity(player.getEntityWorld(), id, true);
        final IPokemob mob = CapabilityPokemob.getPokemobFor(e);
        if (mob != null)
        {
            final IStatsModifiers stats = mob.getModifiers().sortedModifiers.get(modifier);
            for (final Stats stat : Stats.values())
                stats.setModifier(stat, values[stat.ordinal()]);
        }
    }

    @Override
    public void write(final PacketBuffer buf)
    {
        buf.writeInt(this.entityId);
        buf.writeInt(this.modifier);
        for (final float value : this.values)
            buf.writeFloat(value);
    }

}
