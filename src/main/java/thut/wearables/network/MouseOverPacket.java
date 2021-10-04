package thut.wearables.network;

import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.HitResult.Type;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import thut.wearables.ThutWearables;

public class MouseOverPacket extends Packet
{

    public MouseOverPacket()
    {
    }

    public MouseOverPacket(final FriendlyByteBuf buf)
    {
    }

    @OnlyIn(value = Dist.CLIENT)
    @Override
    public void handleClient()
    {
        final HitResult pos = Minecraft.getInstance().hitResult;
        if (pos != null && pos.getType() == Type.ENTITY)
        {
            final EntityHitResult result = (EntityHitResult) pos;
            if (result.getEntity() != null)
            {
                final int id = result.getEntity().getId();
                final PacketGui packet = new PacketGui();
                packet.data.putInt("w_open_target_", id);
                ThutWearables.packets.sendToServer(packet);
            }
        }
        else Minecraft.getInstance().player.sendMessage(new TranslatableComponent("wearables.other.fail"),
                Util.NIL_UUID);
    }

    @Override
    public void write(final FriendlyByteBuf buf)
    {
    }
}
