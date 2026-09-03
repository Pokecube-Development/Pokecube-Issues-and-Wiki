package pokecube.gimmicks.pokeplayer.network;

import pokecube.core.PokecubeCore;
import pokecube.gimmicks.pokeplayer.network.packets.PacketHandshake;

public class PokeplayerPacketHandler
{
    /// All that we need to do here is to register the packet.
    public static void init()
    {
        // Here we are registering a client -> server packet.
        // You can also register server -> client packets and client <-> server packets.
        PokecubeCore.packets.registerToServerMessage(PacketHandshake.class);
    }
}
