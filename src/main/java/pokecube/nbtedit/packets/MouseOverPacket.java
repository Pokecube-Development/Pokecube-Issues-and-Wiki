package pokecube.nbtedit.packets;

import net.minecraft.client.Minecraft;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.EntityRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import pokecube.nbtedit.NBTEdit;
import thut.core.common.network.Packet;

public class MouseOverPacket extends Packet
{

    /** Required default constructor. */
    public MouseOverPacket()
    {
    }

    public MouseOverPacket(PacketBuffer buf)
    {
    }

    @Override
    @OnlyIn(value = Dist.CLIENT)
    public void handleClient()
    {
        final RayTraceResult pos = Minecraft.getInstance().objectMouseOver;
        if (pos != null)
        {
            Packet ret = null;
            switch (pos.getType())
            {
            case BLOCK:
                ret = new TileRequestPacket(((BlockRayTraceResult) pos).getPos());
                break;
            case ENTITY:
                ret = new EntityRequestPacket(((EntityRayTraceResult) pos).getEntity().getEntityId());
                break;
            case MISS:
                NBTEdit.proxy.sendMessage(null, "Error - No tile or entity selected", TextFormatting.RED);
                return;
            default:
                NBTEdit.proxy.sendMessage(null, "Error - No tile or entity selected", TextFormatting.RED);
                return;
            }
            PacketHandler.INSTANCE.sendToServer(ret);
        }
    }

    @Override
    public void write(PacketBuffer buf)
    {
    }

}
