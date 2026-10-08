package pokecube.gimmicks.pokeplayer.network.packets;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.entity.pokemob.PokemobCaps;
import pokecube.core.PokecubeCore;
import thut.api.ThutCaps;
import thut.core.common.network.Packet;

/// Very barebones packet, if you would like another example see
/// PacketPokemobGui or PacketTMs in pokecube.core.network.
/// More specifically, this packet is sent from client to server to tell
/// the server to check whether the CopyMob of the player has changed.
public class PacketHandshake extends Packet
{
    /// Empty constructor in this case.
    public PacketHandshake()
    {}

    /// Functions like these are good for readability purposes.
    public static void sendPacket()
    {
        PokecubeCore.packets.sendToServer(new PacketHandshake());
    }


    /// No need to read data from the buffer in this case.
    public void read(final FriendlyByteBuf buffer)
    {}

    @Override
    /// Handling for when the packet is received on the server.
    /// Here, we mark the pokemob and player as needing to be synced.
    public void handleServer(final ServerPlayer player)
    {
        var copy = ThutCaps.getCopyMob(player);
        if (copy == null) return;
        IPokemob pokemob = PokemobCaps.getPokemobFor(copy.getCopiedMob());
        if (pokemob == null) return;
        player.getPersistentData().putBoolean("pokeplayer:needs_sync", true);
    }

    @Override
    /// No need to write to the data buffer in this case.
    public void write(final FriendlyByteBuf buffer)
    {}

    /// Type of packet. No extra data required, just make a unique name (and make sure it starts with pokecube:, the game crashes otherwise)
    private final static Type<Packet> TYPE = new Type<>(ResourceLocation.parse("pokecube:pokeplayer_handshake"));

    @Override
    public Type<? extends CustomPacketPayload> type()
    {
        return TYPE;
    }
}
