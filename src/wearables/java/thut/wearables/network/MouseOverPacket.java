package thut.wearables.network;

import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import thut.lib.TComponent;
import thut.wearables.ThutWearables;

public class MouseOverPacket extends Packet
{

    public MouseOverPacket()
    {}

    public void read(final FriendlyByteBuf buf)
    {}

    @OnlyIn(value = Dist.CLIENT)
    @Override
    public void handleClient(Player player)
    {
        final HitResult pos = Minecraft.getInstance().hitResult;
        if (pos != null && pos.getType() == HitResult.Type.ENTITY)
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

    private final static Type<Packet> TYPE = new Type<Packet>(ResourceLocation.parse("thut_wearables:mouse_over_mob"));

    @Override
    public Type<? extends CustomPacketPayload> type()
    {
        return TYPE;
    }
}
