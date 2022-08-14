package thut.wearables.network;

import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.HitResult.Type;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import thut.lib.TComponent;
import thut.wearables.ThutWearables;

public class MouseOverPacket extends Packet
{

    public MouseOverPacket()
    {}

    public MouseOverPacket(final FriendlyByteBuf buf)
    {}

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
        else thut.lib.ChatHelper.sendSystemMessage(Minecraft.getInstance().player,
                TComponent.translatable("wearables.other.fail"));
    }

    @Override
    public void write(final FriendlyByteBuf buf)
    {}
}
