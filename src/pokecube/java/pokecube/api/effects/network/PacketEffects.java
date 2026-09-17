package pokecube.api.effects.network;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.connection.ConnectionType;
import pokecube.api.effects.EffectPacketInfo;
import pokecube.api.effects.ParticleEffects;
import pokecube.core.PokecubeCore;
import thut.core.common.network.Packet;

public class PacketEffects extends Packet
{
    EffectPacketInfo info;
    ByteBuf buffer = null;

    public static void sendPacket(EffectPacketInfo info)
    {
        PacketEffects packet = new PacketEffects();
        packet.info = info;
        var r = info.getSource();
        var pos = BlockPos.containing(r.x, r.y, r.z);
        var chunk = info.level.getChunkAt(pos);
        PokecubeCore.packets.sendToTracking(packet, chunk);
    }

    @Override
    public void write(FriendlyByteBuf buffer)
    {
        EffectPacketInfo.write(
                new RegistryFriendlyByteBuf(buffer, info.level.registryAccess(), ConnectionType.NEOFORGE), info);
    }

    @Override
    public void read(FriendlyByteBuf buffer)
    {
        this.buffer = new FriendlyByteBuf(Unpooled.copiedBuffer(buffer));
        buffer.readBytes(buffer.readableBytes());
    }

    @Override
    public void handleClient(Player player)
    {
        info = EffectPacketInfo.read(
                new RegistryFriendlyByteBuf(buffer, player.level().registryAccess(), ConnectionType.NEOFORGE),
                player.level());
        if (info != null) ParticleEffects.ADD_FOR_RENDER.accept(info);
    }

    private final static Type<Packet> TYPE = new Type<>(ResourceLocation.parse("pokecube:particle_effects"));

    @Override
    public Type<? extends CustomPacketPayload> type()
    {
        return TYPE;
    }
}
