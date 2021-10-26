package pokecube.nbtedit.packets;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
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

    public MouseOverPacket(FriendlyByteBuf buf)
    {
    }

    @Override
    @OnlyIn(value = Dist.CLIENT)
    public void handleClient()
    {
        final HitResult pos = Minecraft.getInstance().hitResult;
        if (pos != null)
        {
            Packet ret = null;
            switch (pos.getType())
            {
            case BLOCK:
                ret = new TileRequestPacket(((BlockHitResult) pos).getBlockPos());
                break;
            case ENTITY:
                ret = new EntityRequestPacket(((EntityHitResult) pos).getEntity().getId());
                break;
            case MISS:
                NBTEdit.proxy.sendMessage(null, "Error - No tile or entity selected", ChatFormatting.RED);
                return;
            default:
                NBTEdit.proxy.sendMessage(null, "Error - No tile or entity selected", ChatFormatting.RED);
                return;
            }
            PacketHandler.INSTANCE.sendToServer(ret);
        }
    }

    @Override
    public void write(FriendlyByteBuf buf)
    {
    }

}
