/**
 *
 */
package pokecube.core.network;

import java.util.Arrays;
import java.util.HashMap;

import com.google.common.collect.Maps;
import com.mojang.authlib.GameProfile;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import pokecube.core.PokecubeCore;
import pokecube.core.PokecubeItems;
import pokecube.core.commands.Pokemake;
import pokecube.core.contributors.Contributor;
import pokecube.core.contributors.ContributorManager;
import pokecube.core.database.Database;
import pokecube.core.database.PokedexEntry;
import pokecube.core.interfaces.IHealer;
import pokecube.core.interfaces.IPokecube.PokecubeBehavior;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.capabilities.CapabilityPokemob;
import pokecube.core.items.pokecubes.PokecubeManager;
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
import pokecube.core.utils.PokecubeSerializer;
import pokecube.core.utils.Tools;

/** @author Manchou */
public class PokecubePacketHandler
{

    public static class StarterInfo
    {
        public static String[] infos = {};

        public static void processStarterInfo()
        {
            PokecubePacketHandler.specialStarters.clear();
            for (final String s : StarterInfo.infos)
            {
                final String[] data = s.split(";");
                if (data.length < 2) continue;
                final Contributor contrib = ContributorManager.instance().getContributor(new GameProfile(null, data[0]
                        .trim()));
                if (contrib == null)
                {
                    PokecubeCore.LOGGER.error("Error with contributor for " + data[0]);
                    continue;
                }
                if (PokecubePacketHandler.specialStarters.containsKey(contrib)) continue;
                final String[] pokemonData = new String[data.length - 1];
                for (int i = 1; i < data.length; i++)
                    pokemonData[i - 1] = data[i];
                final StarterInfo[] info = new StarterInfo[pokemonData.length];
                for (int i = 0; i < info.length; i++)
                {
                    final String s1 = pokemonData[i];
                    final String[] dat = s1.split(" ");
                    final String name = dat[0];
                    if (Database.getEntry(name) != null) info[i] = new StarterInfo(dat);
                    else info[i] = new StarterInfo(null);
                }
                final StarterInfoContainer cont = new StarterInfoContainer(info);
                PokecubePacketHandler.specialStarters.put(contrib, cont);
            }
        }

        public final String   name;
        public final String[] args;

        public StarterInfo(final String[] args)
        {
            if (args == null)
            {
                this.name = null;
                this.args = new String[0];
            }
            else
            {
                this.name = args[0];
                this.args = args;
            }
        }

        public ItemStack makeStack(final PlayerEntity owner)
        {
            final ItemStack ret = ItemStack.EMPTY;
            if (this.name == null) return ret;
            final PokedexEntry entry = Database.getEntry(this.name);
            if (entry != null)
            {
                final World worldObj = owner.getEntityWorld();
                final IPokemob pokemob = CapabilityPokemob.getPokemobFor(PokecubeCore.createPokemob(entry, worldObj));
                if (pokemob != null)
                {
                    pokemob.setOwner(owner.getUniqueID());
                    final Contributor contrib = ContributorManager.instance().getContributor(owner.getGameProfile());
                    if (contrib != null) pokemob.setPokecube(contrib.getStarterCube());
                    else pokemob.setPokecube(new ItemStack(PokecubeItems.getFilledCube(PokecubeBehavior.DEFAULTCUBE)));
                    pokemob.setExp(Tools.levelToXp(pokemob.getExperienceMode(), 5), true);
                    pokemob.getEntity().setHealth(pokemob.getEntity().getMaxHealth());
                    if (this.args.length > 1) Pokemake.setToArgs(this.args, pokemob, 1, null, false);
                    final ItemStack item = PokecubeManager.pokemobToItem(pokemob);
                    PokecubeManager.heal(item, owner.getEntityWorld());
                    pokemob.getEntity().remove();
                    return item;
                }
                return PokecubeSerializer.getInstance().starter(entry, owner);
            }
            return ret;
        }

        @Override
        public String toString()
        {
            return this.name + " " + Arrays.toString(this.args);
        }
    }

    public static class StarterInfoContainer
    {
        public final StarterInfo[] info;

        public StarterInfoContainer(final StarterInfo[] info)
        {
            this.info = info;
        }
    }

    public final static byte CHANNEL_ID_ChooseFirstPokemob = 0;
    public final static byte CHANNEL_ID_PokemobMove        = 1;

    public final static byte CHANNEL_ID_EntityPokemob = 2;
    public final static byte CHANNEL_ID_HealTable     = 3;

    public final static byte CHANNEL_ID_PokemobSpawner = 4;

    public final static byte CHANNEL_ID_STATS = 6;

    public static boolean giveHealer = true;

    public static HashMap<Contributor, StarterInfoContainer> specialStarters = Maps.newHashMap();

    public static void handlePokecenterPacket(final ServerPlayerEntity sender)
    {
        if (sender.openContainer instanceof IHealer)
        {
            final IHealer healer = (IHealer) sender.openContainer;
            healer.heal();
        }
    }

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

        PacketCommand.init();
    }
}
