package pokecube.core.network.pokemobs;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
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
        if (!pokemob.getEntity().isAddedToWorld()) return;
        final PacketSyncModifier packet = new PacketSyncModifier();
        packet.entityId = pokemob.getEntity().getId();
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

    public PacketSyncModifier(final FriendlyByteBuf buf)
    {
        this.entityId = buf.readInt();
        this.modifier = buf.readInt();
        for (int i = 0; i < this.values.length; i++)
            this.values[i] = buf.readFloat();
    }

    @Override
    public void handleClient()
    {
        final Player player = PokecubeCore.proxy.getPlayer();
        final int id = this.entityId;
        final int modifier = this.modifier;
        final float[] values = this.values;
        final Entity e = PokecubeCore.getEntityProvider().getEntity(player.getLevel(), id, true);
        final IPokemob mob = CapabilityPokemob.getPokemobFor(e);
        if (mob != null)
        {
            final IStatsModifiers stats = mob.getModifiers().sortedModifiers.get(modifier);
            for (final Stats stat : Stats.values())
                stats.setModifier(stat, values[stat.ordinal()]);
        }
    }

    @Override
    public void write(final FriendlyByteBuf buf)
    {
        buf.writeInt(this.entityId);
        buf.writeInt(this.modifier);
        for (final float value : this.values)
            buf.writeFloat(value);
    }

}
