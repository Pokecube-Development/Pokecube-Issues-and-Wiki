/**
 *
 */
package pokecube.core.network;

import pokecube.core.PokecubeCore;
import pokecube.core.network.packets.PacketChoose;
import pokecube.core.network.packets.PacketDataSync;
import pokecube.core.network.packets.PacketHeal;
import pokecube.core.network.packets.PacketPC;
import pokecube.core.network.packets.PacketPokecube;
import pokecube.core.network.packets.PacketPokedex;
import pokecube.core.network.packets.PacketSyncPokedex;
import pokecube.core.network.packets.PacketSyncRoutes;
import pokecube.core.network.packets.PacketSyncTerrain;
import pokecube.core.network.packets.PacketTMs;
import pokecube.core.network.packets.PacketTrade;
import pokecube.core.network.pokemobs.PacketAIRoutine;
import pokecube.core.network.pokemobs.PacketBattleTargets;
import pokecube.core.network.pokemobs.PacketChangeForme;
import pokecube.core.network.pokemobs.PacketCommand;
import pokecube.core.network.pokemobs.PacketMountedControl;
import pokecube.core.network.pokemobs.PacketNickname;
import pokecube.core.network.pokemobs.PacketPingBoss;
import pokecube.core.network.pokemobs.PacketPokemobGui;
import pokecube.core.network.pokemobs.PacketPokemobMessage;
import pokecube.core.network.pokemobs.PacketSyncModifier;
import pokecube.core.network.pokemobs.PacketSyncMoveUse;
import pokecube.core.network.pokemobs.PacketSyncNewMoves;
import pokecube.core.network.pokemobs.PacketSyncStatus;
import pokecube.core.network.pokemobs.PacketTeleport;
import pokecube.core.network.pokemobs.PacketUpdateAI;
import pokecube.core.network.pokemobs.PokemobPacketHandler;

/** @author Manchou */
public class PokecubePacketHandler
{

    public static void init()
    {
        // General Pokecube Packets
        PokecubeCore.packets.registerToClientMessage(PacketSyncTerrain.class);
        PokecubeCore.packets.registerBiDirectionalMessage(PacketSyncRoutes.class);
        PokecubeCore.packets.registerToClientMessage(PacketPokecube.class);
        PokecubeCore.packets.registerBiDirectionalMessage(PacketPokedex.class);
        PokecubeCore.packets.registerToClientMessage(PacketDataSync.class);
        PokecubeCore.packets.registerBiDirectionalMessage(PacketChoose.class);
        PokecubeCore.packets.registerToClientMessage(PacketSyncPokedex.class);

        // Packets for blocks
        PokecubeCore.packets.registerBiDirectionalMessage(PacketPC.class);
        PokecubeCore.packets.registerToServerMessage(PacketHeal.class);
        PokecubeCore.packets.registerBiDirectionalMessage(PacketTrade.class);
        PokecubeCore.packets.registerToServerMessage(PacketTMs.class);

        // Packets for Pokemobs
        PokecubeCore.packets.registerToServerMessage(PacketAIRoutine.class);
        PokecubeCore.packets.registerToClientMessage(PacketChangeForme.class);
        PokecubeCore.packets.registerToServerMessage(PacketCommand.class);
        PokecubeCore.packets.registerBiDirectionalMessage(PacketMountedControl.class);
        PokecubeCore.packets.registerToServerMessage(PacketNickname.class);
        PokecubeCore.packets.registerToServerMessage(PacketPokemobGui.class);
        PokecubeCore.packets.registerToClientMessage(PacketPokemobMessage.class);
        PokecubeCore.packets.registerToClientMessage(PacketSyncModifier.class);
        PokecubeCore.packets.registerToClientMessage(PacketSyncStatus.class);
        PokecubeCore.packets.registerToClientMessage(PacketSyncMoveUse.class);
        PokecubeCore.packets.registerToClientMessage(PacketSyncNewMoves.class);
        PokecubeCore.packets.registerToServerMessage(PacketTeleport.class);
        PokecubeCore.packets.registerToServerMessage(PokemobPacketHandler.MessageServer.class);
        PokecubeCore.packets.registerToServerMessage(PacketUpdateAI.class);
        PokecubeCore.packets.registerBiDirectionalMessage(PacketPingBoss.class);
        PokecubeCore.packets.registerToServerMessage(PacketBattleTargets.class);

        PacketCommand.init();
        PacketUpdateAI.init();
    }
}
