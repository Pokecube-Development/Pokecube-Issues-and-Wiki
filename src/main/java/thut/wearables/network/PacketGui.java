package thut.wearables.network;

import io.netty.buffer.Unpooled;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.SimpleNamedContainerProvider;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkHooks;
import thut.wearables.EnumWearable;
import thut.wearables.ThutWearables;
import thut.wearables.inventory.ContainerWearables;

public class PacketGui extends Packet
{

    public CompoundNBT data;

    public PacketGui()
    {
        this.data = new CompoundNBT();
    }

    public PacketGui(final PacketBuffer buffer)
    {

        this.data = buffer.readCompoundTag();

    }

    @Override
    public void handleServer(final ServerPlayerEntity player)
    {
        if (this.data.contains("S"))
        {
            final byte slot = this.data.getByte("S");
            final ItemStack stack = ThutWearables.getWearables(player).getStackInSlot(slot);
            if (stack != null) EnumWearable.interact(player, stack, slot);
        }
        else if (this.data.contains("close"))
        {
            final boolean close = this.data.getBoolean("close");
            if (close)
            {
                // player.openContainer(player.container);
            }
            else
            {
                final LivingEntity t = player;
                final PacketBuffer buffer = new PacketBuffer(Unpooled.buffer(0));
                buffer.writeInt(t.getEntityId());
                final SimpleNamedContainerProvider provider = new SimpleNamedContainerProvider((i, p,
                        e) -> new ContainerWearables(i, p, buffer), t.getName());
                NetworkHooks.openGui(player, provider, buf -> buf.writeInt(t.getEntityId()));
            }
        }
        else
        {
            LivingEntity target = player;
            if (this.data.contains("w_open_target_"))
            {
                final Entity mob = player.getEntityWorld().getEntityByID(this.data.getInt("w_open_target_"));
                if (mob instanceof LivingEntity) target = (LivingEntity) mob;
            }
            final LivingEntity t = target;
            final PacketBuffer buffer = new PacketBuffer(Unpooled.buffer(0));
            buffer.writeInt(t.getEntityId());
            final SimpleNamedContainerProvider provider = new SimpleNamedContainerProvider((i, p,
                    e) -> new ContainerWearables(i, p, buffer), t.getName());
            NetworkHooks.openGui(player, provider, buf -> buf.writeInt(t.getEntityId()));
        }
    }

    @Override
    public void write(final PacketBuffer buffer)
    {
        buffer.writeCompoundTag(this.data);
    }
}