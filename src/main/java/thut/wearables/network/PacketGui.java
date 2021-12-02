package thut.wearables.network;

import io.netty.buffer.Unpooled;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.ClipContext.Block;
import net.minecraft.world.level.ClipContext.Fluid;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.network.NetworkHooks;
import thut.wearables.EnumWearable;
import thut.wearables.ThutWearables;
import thut.wearables.inventory.ContainerWearables;

public class PacketGui extends Packet
{
    public static class WearableContext extends UseOnContext
    {
        private static BlockHitResult fromNBT(final Player player, final CompoundTag nbt)
        {
            final Vec3 origin = player.getEyePosition(1);
            final Vec3 dir = player.getLookAngle();
            final Vec3 end = origin.add(dir.scale(4));
            final ClipContext context = new ClipContext(origin, end, Block.OUTLINE, Fluid.NONE, player);
            return player.level.clip(context);
        }

        protected WearableContext(final Player player, final ItemStack heldItem, final CompoundTag nbt)
        {
            super(player.getCommandSenderWorld(), player, InteractionHand.MAIN_HAND, heldItem, WearableContext.fromNBT(player, nbt));
        }

    }

    public CompoundTag data;

    public PacketGui()
    {
        this.data = new CompoundTag();
    }

    public PacketGui(final FriendlyByteBuf buffer)
    {
        this.data = buffer.readNbt();
    }

    @Override
    @OnlyIn(value = Dist.CLIENT)
    public void handleClient()
    {
        Minecraft.getInstance().setScreen(new InventoryScreen(Minecraft.getInstance().player));
    }

    @Override
    public void handleServer(final ServerPlayer player)
    {
        if (this.data.contains("S"))
        {
            final byte slot = this.data.getByte("S");
            final ItemStack stack = ThutWearables.getWearables(player).getStackInSlot(slot);
            if (!stack.isEmpty())
            {
                final UseOnContext context = new WearableContext(player, stack, this.data);
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
                final FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer(0));
                buffer.writeInt(t.getId());
                final SimpleMenuProvider provider = new SimpleMenuProvider((i, p,
                        e) -> new ContainerWearables(i, p, buffer), t.getName());
                NetworkHooks.openGui(player, provider, buf -> buf.writeInt(t.getId()));
            }
        }
        else
        {
            LivingEntity target = player;
            if (this.data.contains("w_open_target_"))
            {
                final Entity mob = player.getCommandSenderWorld().getEntity(this.data.getInt("w_open_target_"));
                if (mob instanceof LivingEntity) target = (LivingEntity) mob;
            }
            final LivingEntity t = target;
            final FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer(0));
            buffer.writeInt(t.getId());
            final SimpleMenuProvider provider = new SimpleMenuProvider((i, p,
                    e) -> new ContainerWearables(i, p, buffer), t.getName());
            NetworkHooks.openGui(player, provider, buf -> buf.writeInt(t.getId()));
        }
    }

    @Override
    public void write(final FriendlyByteBuf buffer)
    {
        buffer.writeNbt(this.data);
    }
}