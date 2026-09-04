package pokecube.gimmicks.vanilla_pokemobs.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import pokecube.core.PokecubeCore;
import pokecube.gimmicks.vanilla_pokemobs.VanillaPokemobs;
import thut.core.common.network.Packet;

/// Very barebones packet, if you would like another example see
/// PacketPokemobGui or PacketTMs in pokecube.core.network.
/// More specifically, this packet is sent from server to client if
/// vanilla pokemobs is enabled
public class PacketHandshake extends Packet
{
    /// Empty constructor in this case.
    public PacketHandshake()
    {}

    /// Functions like these are important for error checking.
    /// Neoforge raises an exception if you have no exception handling.
    public static void sendPacket(ServerPlayer target)
    {
        PokecubeCore.packets.sendTo(new PacketHandshake(), target);
    }

    boolean vanilla_pokemobs, non_vanilla_pokemobs;
    /// Read the 2 booleans
    public void read(final FriendlyByteBuf buffer)
    {
        vanilla_pokemobs = buffer.readBoolean();
        non_vanilla_pokemobs = buffer.readBoolean();
    }

    @Override
    /// Handling for when the packet is received on the client
    /// We need to disconnect with appropriate message if configs do not match.
    public void handleClient(final Player player)
    {
        if (vanilla_pokemobs != VanillaPokemobs.config.vanilla_pokemobs
                || non_vanilla_pokemobs != VanillaPokemobs.config.non_vanilla_pokemobs)
        {
            if (player instanceof net.minecraft.client.player.LocalPlayer local)
            {
                local.connection.disconnect(
                        Component.translatable("pokecube.vanilla_pokemobs.config_missmatch", vanilla_pokemobs,
                                non_vanilla_pokemobs));
            }
        }
    }

    @Override
    /// Write the two booleans
    public void write(final FriendlyByteBuf buffer)
    {
        buffer.writeBoolean(VanillaPokemobs.config.vanilla_pokemobs);
        buffer.writeBoolean(VanillaPokemobs.config.non_vanilla_pokemobs);
    }

    /// Type of packet. No extra data required, just make a unique name (and make sure it starts with pokecube:, the game crashes otherwise)
    private final static Type<Packet> TYPE = new Type<>(ResourceLocation.parse("pokecube:vanilla_mobs_handshake"));

    @Override
    public Type<? extends CustomPacketPayload> type()
    {
        return TYPE;
    }
}
