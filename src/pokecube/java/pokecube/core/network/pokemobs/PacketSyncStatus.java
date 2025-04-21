package pokecube.core.network.pokemobs;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import pokecube.api.PokecubeAPI;
import pokecube.core.PokecubeCore;
import pokecube.core.moves.damage.effects.StatusEffect;
import thut.core.common.network.Packet;

public class PacketSyncStatus extends Packet
{
    public static void sendUpdate(final LivingEntity mob)
    {
        if (mob == null) return;
        if (mob.level().isClientSide()) return;
        var eff = mob.getActiveEffectsMap();
        if (eff.isEmpty()) return;
        final PacketSyncStatus packet = new PacketSyncStatus();
        packet.entityId = mob.getId();
        ListTag listtag = new ListTag();
        for (MobEffectInstance mobeffectinstance : eff.values())
            if (mobeffectinstance.getEffect().value() instanceof StatusEffect) listtag.add(mobeffectinstance.save());
        if (listtag.isEmpty()) return;
        packet.data.put("e", listtag);
        PokecubeCore.packets.sendToTrackingAndSelf(packet, mob);
    }

    int entityId;
    CompoundTag data = new CompoundTag();

    public PacketSyncStatus()
    {
    }

    public void read(final FriendlyByteBuf buf)
    {
        this.entityId = buf.readInt();
        this.data = buf.readNbt();
    }

    @Override
    public void handleClient(Player player)
    {
        final int id = this.entityId;
        final Entity e = PokecubeAPI.getEntityProvider().getEntity(player.level(), id, true);
        if (e instanceof LivingEntity living)
        {
            var eff = living.getActiveEffectsMap();
            if (data.contains("e", 9))
            {
                ListTag listtag = data.getList("e", 10);
                for (int i = 0; i < listtag.size(); i++)
                {
                    CompoundTag compoundtag = listtag.getCompound(i);
                    MobEffectInstance mobeffectinstance = MobEffectInstance.load(compoundtag);
                    if (mobeffectinstance != null)
                    {
                        eff.put(mobeffectinstance.getEffect(), mobeffectinstance);
                    }
                }
            }
        }
    }

    @Override
    public void write(final FriendlyByteBuf buf)
    {
        buf.writeInt(this.entityId);
        buf.writeNbt(this.data);
    }

    private final static Type<Packet> TYPE = new Type<Packet>(ResourceLocation.parse("pokecube:pokemob_status_sync"));

    @Override
    public Type<? extends CustomPacketPayload> type()
    {
        return TYPE;
    }

}
