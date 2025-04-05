package pokecube.nbtedit.packets;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.entity.PartEntity;
import pokecube.nbtedit.NBTEdit;
import thut.core.common.network.Packet;

public class MouseOverPacket extends Packet
{

    /** Required default constructor. */
    public MouseOverPacket()
    {}

    public void read(FriendlyByteBuf buf)
    {}

    @Override
    @OnlyIn(value = Dist.CLIENT)
    public void handleClient(Player player)
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
                Entity entity = ((EntityHitResult) pos).getEntity();
                if (entity instanceof PartEntity<?> part) entity = part.getParent();
                ret = new EntityRequestPacket(entity.getId());
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
    {}

    private final static Type<Packet> TYPE = new Type<Packet>(ResourceLocation.parse("pokecube:nbtedit_mouse_over"));

    @Override
    public Type<? extends CustomPacketPayload> type()
    {
        return TYPE;
    }

}
