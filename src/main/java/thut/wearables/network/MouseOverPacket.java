package thut.wearables.network;

import net.minecraft.client.Minecraft;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.EntityRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.RayTraceResult.Type;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import thut.wearables.ThutWearables;

public class MouseOverPacket extends Packet
{

    public MouseOverPacket()
    {
    }

    public MouseOverPacket(final PacketBuffer buf)
    {
    }

    @OnlyIn(value = Dist.CLIENT)
    @Override
    public void handleClient()
    {
        final RayTraceResult pos = Minecraft.getInstance().objectMouseOver;
        if (pos != null && pos.getType() == Type.ENTITY)
        {
            final EntityRayTraceResult result = (EntityRayTraceResult) pos;
            if (result.getEntity() != null)
            {
                final int id = result.getEntity().getEntityId();
                final PacketGui packet = new PacketGui();
                packet.data.putInt("w_open_target_", id);
                ThutWearables.packets.sendToServer(packet);
            }
        }
        else Minecraft.getInstance().player.sendMessage(new TranslationTextComponent("wearables.other.fail"));
    }

    @Override
    public void write(final PacketBuffer buf)
    {
    }
}
