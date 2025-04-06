package pokecube.core.network.pokemobs;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import pokecube.api.PokecubeAPI;
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
        if (!mob.isAddedToLevel()) return;
        final PacketSyncGene packet = new PacketSyncGene();
        packet.genes = gene;
        packet.entityId = mob.getId();
        PokecubeCore.packets.sendTo(packet, entityPlayer);
    }

    public static void syncGeneToTracking(final Entity mob, final Alleles<?, ?> gene)
    {
        if (!(mob.level() instanceof ServerLevel) || gene == null) return;
        if (!mob.isAddedToLevel()) return;
        final PacketSyncGene packet = new PacketSyncGene();
        packet.genes = gene;
        packet.entityId = mob.getId();
        PokecubeCore.packets.sendToTracking(packet, mob);
        if (mob instanceof ServerPlayer player) syncGene(mob, gene, player);
    }

    Alleles<?, ?> genes = new Alleles<>();

    int entityId;

    public PacketSyncGene()
    {}

    public void read(final FriendlyByteBuf buffer)
    {
        this.entityId = buffer.readInt();
        try
        {
            var tag = buffer.readNbt();
            ResourceLocation key = ResourceLocation.parse(tag.getString("K"));
            PokecubeAPI.logInfo("Read " + tag);
            this.genes.load(PokecubeCore.proxy.getRegistries(), tag, key);
        }
        catch (final Exception e)
        {
            e.printStackTrace();
        }
    }

    @Override
    public void handleClient(Player player)
    {
        final int id = this.entityId;
        final Alleles<?, ?> alleles = this.genes;
        final Entity mob = PokecubeAPI.getEntityProvider().getEntity(player.level(), id, true);
        if (mob == null) return;
        final IMobGenetics genes = ThutCaps.getGenetics(mob);
        if (genes != null && alleles != null && alleles.getExpressed() != null)
        {
            genes.setGenes(alleles.getAllele(0), alleles.getAllele(1), alleles.getExpressed());
            alleles.setChangeListeners(genes.getChangeListeners());
            alleles.onChanged();
        }
    }

    @Override
    public void write(final FriendlyByteBuf buffer)
    {
        buffer.writeInt(this.entityId);
        CompoundTag tag = new CompoundTag();
        try
        {
            tag = this.genes.save(PokecubeCore.proxy.getRegistries());
            tag.putString("K", this.genes.getExpressed().getKey().toString());
        }
        catch (final Exception e)
        {
            PokecubeAPI.LOGGER.error("Error syncing a gene! {}", this.genes, e);
        }
        buffer.writeNbt(tag);
    }

    private final static Type<Packet> TYPE = new Type<Packet>(ResourceLocation.parse("pokecube:pokemob_genes"));

    @Override
    public Type<? extends CustomPacketPayload> type()
    {
        return TYPE;
    }
}
