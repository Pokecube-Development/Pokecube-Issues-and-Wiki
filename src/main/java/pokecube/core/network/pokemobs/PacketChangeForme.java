package pokecube.core.network.pokemobs;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import pokecube.core.PokecubeCore;
import pokecube.core.database.Database;
import pokecube.core.database.PokedexEntry;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.capabilities.CapabilityPokemob;
import pokecube.core.interfaces.pokemob.ICanEvolve;
import pokecube.core.interfaces.pokemob.ai.CombatStates;
import pokecube.core.interfaces.pokemob.ai.GeneralStates;
import pokecube.core.items.megastuff.MegaCapability;
import thut.core.common.commands.CommandTools;
import thut.core.common.network.Packet;

public class PacketChangeForme extends Packet
{
    public static void sendPacketToServer(Entity mob, PokedexEntry forme)
    {
        final PacketChangeForme packet = new PacketChangeForme();
        packet.entityId = mob.getEntityId();
        packet.forme = forme;
        PokecubeCore.packets.sendToServer(packet);
    }

    public static void sendPacketToTracking(Entity mob, PokedexEntry forme)
    {
        final PacketChangeForme packet = new PacketChangeForme();
        packet.entityId = mob.getEntityId();
        packet.forme = forme;
        PokecubeCore.packets.sendToTracking(packet, mob);
    }

    int          entityId;
    PokedexEntry forme;

    public PacketChangeForme()
    {
    }

    public PacketChangeForme(PacketBuffer buffer)
    {
        this.entityId = buffer.readInt();
        this.forme = Database.getEntry(buffer.readString(20));
    }

    @Override
    public void handleClient()
    {
        final PlayerEntity player = PokecubeCore.proxy.getPlayer();
        final Entity mob = PokecubeCore.getEntityProvider().getEntity(player.getEntityWorld(), this.entityId, true);
        final IPokemob pokemob = CapabilityPokemob.getPokemobFor(mob);
        if (pokemob == null) return;
        pokemob.setPokedexEntry(this.forme);
    }

    @Override
    public void handleServer(ServerPlayerEntity player)
    {
        final Entity mob = PokecubeCore.getEntityProvider().getEntity(player.getEntityWorld(), this.entityId, true);
        final IPokemob pokemob = CapabilityPokemob.getPokemobFor(mob);
        if (pokemob == null) return;
        if (pokemob.getGeneralState(GeneralStates.EVOLVING)) return;
        final boolean hasRing = MegaCapability.canMegaEvolve(player, pokemob);
        if (!hasRing)
        {
            player.sendMessage(new TranslationTextComponent("pokecube.mega.noring", pokemob.getDisplayName()));
            return;
        }
        PokedexEntry newEntry = pokemob.getPokedexEntry().getEvo(pokemob);
        if (newEntry != null && newEntry.getPokedexNb() == pokemob.getPokedexEntry().getPokedexNb())
        {
            final String old = pokemob.getDisplayName().getFormattedText();
            if (pokemob.getPokedexEntry() == newEntry)
            {
                ITextComponent mess = CommandTools.makeTranslatedMessage("pokemob.megaevolve.command.revert", "green",
                        old);
                pokemob.displayMessageToOwner(mess);
                pokemob.setCombatState(CombatStates.MEGAFORME, false);
                mess = CommandTools.makeTranslatedMessage("pokemob.megaevolve.revert", "green", old, newEntry
                        .getUnlocalizedName());
                ICanEvolve.setDelayedMegaEvolve(pokemob, newEntry, mess);
            }
            else
            {
                ITextComponent mess = CommandTools.makeTranslatedMessage("pokemob.megaevolve.command.evolve", "green",
                        old);
                pokemob.displayMessageToOwner(mess);
                mess = CommandTools.makeTranslatedMessage("pokemob.megaevolve.success", "green", old, newEntry
                        .getUnlocalizedName());
                pokemob.setCombatState(CombatStates.MEGAFORME, true);
                ICanEvolve.setDelayedMegaEvolve(pokemob, newEntry, mess);
            }
        }
        else if (pokemob.getCombatState(CombatStates.MEGAFORME))
        {
            final String old = pokemob.getDisplayName().getFormattedText();
            ITextComponent mess = CommandTools.makeTranslatedMessage("pokemob.megaevolve.command.revert", "green", old);
            pokemob.displayMessageToOwner(mess);
            newEntry = pokemob.getPokedexEntry().getBaseForme();
            pokemob.setCombatState(CombatStates.MEGAFORME, false);
            mess = CommandTools.makeTranslatedMessage("pokemob.megaevolve.revert", "green", old, newEntry
                    .getUnlocalizedName());
            ICanEvolve.setDelayedMegaEvolve(pokemob, newEntry, mess);
        }
        else player.sendMessage(CommandTools.makeTranslatedMessage("pokemob.megaevolve.failed", "red", pokemob
                .getDisplayName()));
    }

    @Override
    public void write(PacketBuffer buf)
    {
        final PacketBuffer buffer = new PacketBuffer(buf);
        buffer.writeInt(this.entityId);
        if (this.forme != null) buffer.writeString(this.forme.getName());
        else buffer.writeString("");
    }

}
