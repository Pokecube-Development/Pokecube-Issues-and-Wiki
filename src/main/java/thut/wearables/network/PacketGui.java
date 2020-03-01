package thut.wearables.network;

import io.netty.buffer.Unpooled;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.inventory.InventoryScreen;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.SimpleNamedContainerProvider;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceContext;
import net.minecraft.util.math.RayTraceContext.BlockMode;
import net.minecraft.util.math.RayTraceContext.FluidMode;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.network.NetworkHooks;
import thut.wearables.EnumWearable;
import thut.wearables.ThutWearables;
import thut.wearables.inventory.ContainerWearables;

public class PacketGui extends Packet
{
    public static class WearableContext extends ItemUseContext
    {
        private static BlockRayTraceResult fromNBT(final PlayerEntity player, final CompoundNBT nbt)
        {
            final Vec3d origin = player.getEyePosition(1);
            final Vec3d dir = player.getLookVec();
            final Vec3d end = origin.add(dir.scale(4));
            final RayTraceContext context = new RayTraceContext(origin, end, BlockMode.OUTLINE, FluidMode.NONE, player);
            return player.world.rayTraceBlocks(context);
        }

        protected WearableContext(final PlayerEntity player, final ItemStack heldItem, final CompoundNBT nbt)
        {
            super(player.getEntityWorld(), player, Hand.MAIN_HAND, heldItem, WearableContext.fromNBT(player, nbt));
        }

    }

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
    @OnlyIn(value = Dist.CLIENT)
    public void handleClient()
    {
        Minecraft.getInstance().displayGuiScreen(new InventoryScreen(Minecraft.getInstance().player));
    }

    @Override
    public void handleServer(final ServerPlayerEntity player)
    {
        if (this.data.contains("S"))
        {
            final byte slot = this.data.getByte("S");
            final ItemStack stack = ThutWearables.getWearables(player).getStackInSlot(slot);
            if (!stack.isEmpty())
            {
                final ItemUseContext context = new WearableContext(player, stack, this.data);
                EnumWearable.interact(player, stack, slot, context);
            }
        }
        else if (this.data.contains("close"))
        {
            final boolean close = this.data.getBoolean("close");
            if (close) ThutWearables.packets.sendTo(new PacketGui(), player);
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