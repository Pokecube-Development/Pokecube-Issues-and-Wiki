package pokecube.core.network.pokemobs;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.world.server.ServerWorld;
import pokecube.core.PokecubeCore;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.capabilities.CapabilityPokemob;
import thut.api.entity.genetics.Alleles;
import thut.api.entity.genetics.GeneRegistry;
import thut.api.entity.genetics.IMobGenetics;
import thut.core.common.network.Packet;

public class PacketSyncGene extends Packet
{
    public static void syncGene(final Entity mob, final Alleles<?, ?> gene, final ServerPlayerEntity entityPlayer)
    {
        if (!(mob.getCommandSenderWorld() instanceof ServerWorld) || gene == null) return;
        final PacketSyncGene packet = new PacketSyncGene();
        packet.genes = gene;
        packet.entityId = mob.getId();
        PokecubeCore.packets.sendTo(packet, entityPlayer);
    }

    public static void syncGeneToTracking(final Entity mob, final Alleles<?, ?> gene)
    {
        if (!(mob.getCommandSenderWorld() instanceof ServerWorld) || gene == null) return;
        final PacketSyncGene packet = new PacketSyncGene();
        packet.genes = gene;
        packet.entityId = mob.getId();
        PokecubeCore.packets.sendToTracking(packet, mob);
    }

    Alleles<?, ?> genes = new Alleles<>();

    int entityId;

    public PacketSyncGene()
    {
        super(null);
    }

    public PacketSyncGene(final PacketBuffer buffer)
    {
        super(buffer);
        this.entityId = buffer.readInt();
        try
        {
            this.genes.load(buffer.readNbt());
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
        final Alleles<?, ?> alleles = this.genes;
        final Entity mob = PokecubeCore.getEntityProvider().getEntity(player.getCommandSenderWorld(), id, true);
        if (mob == null) return;
        final IMobGenetics genes = mob.getCapability(GeneRegistry.GENETICS_CAP, null).orElse(null);
        final IPokemob pokemob = CapabilityPokemob.getPokemobFor(mob);
        if (genes != null && alleles != null && alleles.getExpressed() != null) genes.getAlleles().put(alleles
                .getExpressed().getKey(), alleles);
        if (pokemob != null) pokemob.onGenesChanged();
    }

    @Override
    public void write(final PacketBuffer buffer)
    {
        buffer.writeInt(this.entityId);
        CompoundNBT tag = new CompoundNBT();
        try
        {
            tag = this.genes.save();
        }
        catch (final Exception e)
        {
            PokecubeCore.LOGGER.error("Error syncing a gene! {}", this.genes, e);
        }
        buffer.writeNbt(tag);
    }
}
