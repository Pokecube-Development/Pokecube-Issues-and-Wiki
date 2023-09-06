package pokecube.core.network.pokemobs;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import pokecube.api.PokecubeAPI;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.entity.pokemob.PokemobCaps;
import pokecube.core.PokecubeCore;
import thut.api.ThutCaps;
import thut.api.entity.genetics.Alleles;
import thut.api.entity.genetics.IMobGenetics;
import thut.core.common.network.Packet;

public class PacketSyncGene extends Packet
{
    public static void syncGene(final Entity mob, final Alleles<?, ?> gene, final ServerPlayer entityPlayer)
    {
        if (!(mob.level() instanceof ServerLevel) || gene == null) return;
        final PacketSyncGene packet = new PacketSyncGene();
        packet.genes = gene;
        packet.entityId = mob.getId();
        PokecubeCore.packets.sendTo(packet, entityPlayer);
    }

    public static void syncGeneToTracking(final Entity mob, final Alleles<?, ?> gene)
    {
        if (!(mob.level() instanceof ServerLevel) || gene == null) return;
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

    public PacketSyncGene(final FriendlyByteBuf buffer)
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
        final Player player = PokecubeCore.proxy.getPlayer();
        final int id = this.entityId;
        final Alleles<?, ?> alleles = this.genes;
        final Entity mob = PokecubeAPI.getEntityProvider().getEntity(player.level(), id, true);
        if (mob == null) return;
        final IMobGenetics genes = mob.getCapability(ThutCaps.GENETICS_CAP, null).orElse(null);
        final IPokemob pokemob = PokemobCaps.getPokemobFor(mob);
        if (genes != null && alleles != null && alleles.getExpressed() != null) genes.getAlleles().put(alleles
                .getExpressed().getKey(), alleles);
        if (pokemob != null) pokemob.onGenesChanged();
    }

    @Override
    public void write(final FriendlyByteBuf buffer)
    {
        buffer.writeInt(this.entityId);
        CompoundTag tag = new CompoundTag();
        try
        {
            tag = this.genes.save();
        }
        catch (final Exception e)
        {
            PokecubeAPI.LOGGER.error("Error syncing a gene! {}", this.genes, e);
        }
        buffer.writeNbt(tag);
    }
}
