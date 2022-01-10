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
import pokecube.core.network.packets.PacketSyncRoutes;
import pokecube.core.network.packets.PacketSyncTerrain;
import pokecube.core.network.packets.PacketTMs;
import pokecube.core.network.packets.PacketTrade;
import pokecube.core.network.pokemobs.PacketAIRoutine;
import pokecube.core.network.pokemobs.PacketChangeForme;
import pokecube.core.network.pokemobs.PacketCommand;
import pokecube.core.network.pokemobs.PacketMountedControl;
import pokecube.core.network.pokemobs.PacketNickname;
import pokecube.core.network.pokemobs.PacketPingBoss;
import pokecube.core.network.pokemobs.PacketPokemobGui;
import pokecube.core.network.pokemobs.PacketPokemobMessage;
import pokecube.core.network.pokemobs.PacketSyncExp;
import pokecube.core.network.pokemobs.PacketSyncGene;
import pokecube.core.network.pokemobs.PacketSyncModifier;
import pokecube.core.network.pokemobs.PacketSyncMoveUse;
import pokecube.core.network.pokemobs.PacketSyncNewMoves;
import pokecube.core.network.pokemobs.PacketTeleport;
import pokecube.core.network.pokemobs.PacketUpdateAI;
import pokecube.core.network.pokemobs.PokemobPacketHandler;

/** @author Manchou */
public class PokecubePacketHandler
{

    public static void init()
    {
        // General Pokecube Packets
        PokecubeCore.packets.registerMessage(PacketSyncTerrain.class, PacketSyncTerrain::new);
        PokecubeCore.packets.registerMessage(PacketSyncRoutes.class, PacketSyncRoutes::new);
        PokecubeCore.packets.registerMessage(PacketPokecube.class, PacketPokecube::new);
        PokecubeCore.packets.registerMessage(PacketPokedex.class, PacketPokedex::new);
        PokecubeCore.packets.registerMessage(PacketDataSync.class, PacketDataSync::new);
        PokecubeCore.packets.registerMessage(PacketChoose.class, PacketChoose::new);

        // Packets for blocks
        PokecubeCore.packets.registerMessage(PacketPC.class, PacketPC::new);
        PokecubeCore.packets.registerMessage(PacketHeal.class, PacketHeal::new);
        PokecubeCore.packets.registerMessage(PacketTrade.class, PacketTrade::new);
        PokecubeCore.packets.registerMessage(PacketTMs.class, PacketTMs::new);

        // Packets for Pokemobs
        PokecubeCore.packets.registerMessage(PacketAIRoutine.class, PacketAIRoutine::new);
        PokecubeCore.packets.registerMessage(PacketChangeForme.class, PacketChangeForme::new);
        PokecubeCore.packets.registerMessage(PacketCommand.class, PacketCommand::new);
        PokecubeCore.packets.registerMessage(PacketMountedControl.class, PacketMountedControl::new);
        PokecubeCore.packets.registerMessage(PacketNickname.class, PacketNickname::new);
        PokecubeCore.packets.registerMessage(PacketPokemobGui.class, PacketPokemobGui::new);
        PokecubeCore.packets.registerMessage(PacketPokemobMessage.class, PacketPokemobMessage::new);
        PokecubeCore.packets.registerMessage(PacketSyncExp.class, PacketSyncExp::new);
        PokecubeCore.packets.registerMessage(PacketSyncGene.class, PacketSyncGene::new);
        PokecubeCore.packets.registerMessage(PacketSyncModifier.class, PacketSyncModifier::new);
        PokecubeCore.packets.registerMessage(PacketSyncMoveUse.class, PacketSyncMoveUse::new);
        PokecubeCore.packets.registerMessage(PacketSyncNewMoves.class, PacketSyncNewMoves::new);
        PokecubeCore.packets.registerMessage(PacketTeleport.class, PacketTeleport::new);
        PokecubeCore.packets.registerMessage(PokemobPacketHandler.MessageServer.class,
                PokemobPacketHandler.MessageServer::new);
        PokecubeCore.packets.registerMessage(PacketUpdateAI.class, PacketUpdateAI::new);
        PokecubeCore.packets.registerMessage(PacketPingBoss.class, PacketPingBoss::new);

        PacketCommand.init();
    }
}
