package pokecube.core.network.pokemobs;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import pokecube.api.PokecubeAPI;
import pokecube.api.entity.SharedAttributes;
import pokecube.core.PokecubeCore;
import pokecube.core.moves.damage.attributes.PokecubeAttributes;
import thut.core.common.network.Packet;

public class PacketSyncModifier extends Packet
{
    public static void sendUpdate(final LivingEntity mob)
    {
        if (mob == null) return;
        if (mob.level().isClientSide()) return;
        final PacketSyncModifier packet = new PacketSyncModifier();
        packet.entityId = mob.getId();
        packet.tag.put("L", mob.getAttributes().save());
        PokecubeCore.packets.sendToTracking(packet, mob);
    }

    int entityId;
    CompoundTag tag = new CompoundTag();

    public PacketSyncModifier()
    {
    }

    public void read(final FriendlyByteBuf buf)
    {
        this.entityId = buf.readInt();
        this.tag = buf.readNbt();
    }

    @Override
    public void handleClient(Player player)
    {
        final int id = this.entityId;
        final Entity e = PokecubeAPI.getEntityProvider().getEntity(player.level(), id, true);
        if (e instanceof LivingEntity living)
        {
            var list = this.tag.getList("L", 10);
            // Ensure we clear the old ones
            for (var a : PokecubeAttributes.ATTRIBUTES) living.getAttribute(a).removeModifiers();
            living.getAttribute(SharedAttributes.MOB_SIZE_SCALE).removeModifiers();
            // Then update the new ones
            living.getAttributes().load(list);
        }
    }

    @Override
    public void write(final FriendlyByteBuf buf)
    {
        buf.writeInt(this.entityId);
        buf.writeNbt(this.tag);
    }

    private final static Type<Packet> TYPE = new Type<>(ResourceLocation.parse("pokecube:pokemob_stat_mods"));

    @Override
    public Type<? extends CustomPacketPayload> type()
    {
        return TYPE;
    }

}
