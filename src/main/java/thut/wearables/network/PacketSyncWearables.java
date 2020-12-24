package thut.wearables.network;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.MinecraftForge;
import thut.wearables.ThutWearables;
import thut.wearables.events.WearablesLoadedEvent;
import thut.wearables.inventory.PlayerWearables;

public class PacketSyncWearables extends Packet
{
    CompoundNBT data;

    public PacketSyncWearables()
    {
        this.data = new CompoundNBT();
    }

    public PacketSyncWearables(final LivingEntity player)
    {
        this();
        final PlayerWearables cap = ThutWearables.getWearables(player);
        if (cap != null)
        {
            MinecraftForge.EVENT_BUS.post(new WearablesLoadedEvent(player, cap));
            this.data.putInt("I", player.getEntityId());
            cap.writeToNBT(this.data);
        }
        else this.data.putInt("I", -1);
    }

    public PacketSyncWearables(final PacketBuffer buffer)
    {
        this.data = buffer.readCompoundTag();

    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void handleClient()
    {
        final World world = Minecraft.getInstance().world;
        System.out.println(this.data);
        if (world == null) return;
        final Entity p = world.getEntityByID(this.data.getInt("I"));
        if (p != null && p instanceof LivingEntity)
        {
            final PlayerWearables cap = ThutWearables.getWearables((LivingEntity) p);
            cap.readFromNBT(this.data);
        }
        return;
    }

    @Override
    public void write(final PacketBuffer buffer)
    {
        buffer.writeCompoundTag(this.data);
    }

}
