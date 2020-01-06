package pokecube.core.network.pokemobs;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import pokecube.core.PokecubeCore;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.capabilities.CapabilityPokemob;
import thut.api.entity.genetics.Alleles;
import thut.api.entity.genetics.GeneRegistry;
import thut.api.entity.genetics.IMobGenetics;
import thut.core.common.network.Packet;

public class PacketSyncGene extends Packet
{
    public static void syncGene(Entity mob, Alleles gene, ServerPlayerEntity entityPlayer)
    {
        if (mob.getEntityWorld() == null || mob.getEntityWorld().isRemote || gene == null) return;
        final PacketSyncGene packet = new PacketSyncGene();
        packet.genes = gene;
        packet.entityId = mob.getEntityId();
        PokecubeCore.packets.sendTo(packet, entityPlayer);
    }

    public static void syncGeneToTracking(Entity mob, Alleles gene)
    {
        if (mob.getEntityWorld() == null || mob.getEntityWorld().isRemote || gene == null) return;
        final PacketSyncGene packet = new PacketSyncGene();
        packet.genes = gene;
        packet.entityId = mob.getEntityId();
        PokecubeCore.packets.sendToTracking(packet, mob);
    }

    Alleles genes = new Alleles();

    int entityId;

    public PacketSyncGene()
    {
        super(null);
    }

    public PacketSyncGene(PacketBuffer buffer)
    {
        super(buffer);
        this.entityId = buffer.readInt();
        try
        {
            this.genes.load(buffer.readCompoundTag());
        }
        catch (final Exception e)
        {
            e.printStackTrace();
        }
    }

    @Override
    public void handleClient()
    {
        final PlayerEntity player = PokecubeCore.proxy.getPlayer();
        final int id = this.entityId;
        final Alleles alleles = this.genes;
        final Entity mob = PokecubeCore.getEntityProvider().getEntity(player.getEntityWorld(), id, true);
        if (mob == null) return;
        final IMobGenetics genes = mob.getCapability(GeneRegistry.GENETICS_CAP, null).orElse(null);
        final IPokemob pokemob = CapabilityPokemob.getPokemobFor(mob);
        if (genes != null && alleles != null && alleles.getExpressed() != null) genes.getAlleles().put(alleles
                .getExpressed().getKey(), alleles);
        if (pokemob != null) pokemob.onGenesChanged();
    }

    @Override
    public void write(PacketBuffer buffer)
    {
        buffer.writeInt(this.entityId);
        buffer.writeCompoundTag(this.genes.save());
    }
}
