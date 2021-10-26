package thut.wearables.network;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.MinecraftForge;
import thut.wearables.ThutWearables;
import thut.wearables.events.WearablesLoadedEvent;
import thut.wearables.inventory.PlayerWearables;

public class PacketSyncWearables extends Packet
{
    CompoundTag data;

    public PacketSyncWearables()
    {
        this.data = new CompoundTag();
    }

    public PacketSyncWearables(final LivingEntity player)
    {
        this();
        final PlayerWearables cap = ThutWearables.getWearables(player);
        if (cap != null)
        {
            MinecraftForge.EVENT_BUS.post(new WearablesLoadedEvent(player, cap));
            this.data.putInt("I", player.getId());
            cap.writeToNBT(this.data);
        }
        else this.data.putInt("I", -1);
    }

    public PacketSyncWearables(final FriendlyByteBuf buffer)
    {
        this.data = buffer.readNbt();

    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void handleClient()
    {
        final Level world = net.minecraft.client.Minecraft.getInstance().level;
        if (world == null) return;
        final Entity p = world.getEntity(this.data.getInt("I"));
        if (p instanceof LivingEntity)
        {
            final PlayerWearables cap = ThutWearables.getWearables((LivingEntity) p);
            if (cap != null) cap.readFromNBT(this.data);
        }
        return;
    }

    @Override
    public void write(final FriendlyByteBuf buffer)
    {
        buffer.writeNbt(this.data);
    }

}
