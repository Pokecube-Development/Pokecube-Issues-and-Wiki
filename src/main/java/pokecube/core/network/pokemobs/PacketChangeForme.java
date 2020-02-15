package pokecube.core.network.pokemobs;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import pokecube.core.PokecubeCore;
import pokecube.core.blocks.maxspot.MaxTile;
import pokecube.core.database.Database;
import pokecube.core.database.PokedexEntry;
import pokecube.core.handlers.events.SpawnHandler;
import pokecube.core.handlers.events.SpawnHandler.ForbidReason;
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
    public static void sendPacketToServer(final Entity mob, final PokedexEntry forme)
    {
        final PacketChangeForme packet = new PacketChangeForme();
        packet.entityId = mob.getEntityId();
        packet.forme = forme;
        PokecubeCore.packets.sendToServer(packet);
    }

    public static void sendPacketToTracking(final Entity mob, final PokedexEntry forme)
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

    public PacketChangeForme(final PacketBuffer buffer)
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
    public void handleServer(final ServerPlayerEntity player)
    {
        final World world = player.getEntityWorld();
        final BlockPos pos = player.getPosition();
        final Entity mob = PokecubeCore.getEntityProvider().getEntity(world, this.entityId, true);
        final IPokemob pokemob = CapabilityPokemob.getPokemobFor(mob);
        if (pokemob == null) return;
        if (pokemob.getGeneralState(GeneralStates.EVOLVING)) return;
        final boolean hasRing = MegaCapability.canMegaEvolve(player, pokemob);
        if (!hasRing)
        {
            player.sendMessage(new TranslationTextComponent("pokecube.mega.noring", pokemob.getDisplayName()));
            return;
        }
        final PokedexEntry entry = pokemob.getPokedexEntry();
        final ITextComponent oldName = pokemob.getDisplayName();

        // Check dynamax/gigantamax first.
        final ForbidReason reason = SpawnHandler.getNoSpawnReason(world, pos);

        if (reason == MaxTile.MAXSPOT)
        {
            boolean gigant = pokemob.getCombatState(CombatStates.GIGANTAMAX);
            PokedexEntry newEntry = entry.getBaseForme() != null ? entry.getBaseForme() : entry;
            final boolean isDyna = pokemob.getCombatState(CombatStates.DYNAMAX) || entry.isMega;

            if (gigant && !isDyna)
            {
                newEntry = Database.getEntry(newEntry.getTrimmedName() + "_gigantamax");
                if (newEntry == null) gigant = false;
            }

            if (isDyna)
            {
                ITextComponent mess = CommandTools.makeTranslatedMessage("pokemob.dynamax.command.revert", "green",
                        oldName);
                pokemob.displayMessageToOwner(mess);
                pokemob.setCombatState(CombatStates.MEGAFORME, false);
                mess = CommandTools.makeTranslatedMessage("pokemob.dynamax.revert", "green", oldName);
                ICanEvolve.setDelayedMegaEvolve(pokemob, newEntry, mess, true);
                return;
            }
            else if (gigant)
            {
                ITextComponent mess = CommandTools.makeTranslatedMessage("pokemob.dynamax.command.evolve", "green",
                        oldName);
                pokemob.displayMessageToOwner(mess);
                mess = CommandTools.makeTranslatedMessage("pokemob.dynamax.success", "green", oldName);
                pokemob.setCombatState(CombatStates.MEGAFORME, true);
                ICanEvolve.setDelayedMegaEvolve(pokemob, newEntry, mess, true);
                return;
            }
            else
            {
                ITextComponent mess = CommandTools.makeTranslatedMessage("pokemob.dynamax.command.evolve", "green",
                        oldName);
                pokemob.displayMessageToOwner(mess);
                mess = CommandTools.makeTranslatedMessage("pokemob.dynamax.success", "green", oldName);
                ICanEvolve.setDelayedMegaEvolve(pokemob, newEntry, mess, true);
                return;
            }
        }

        PokedexEntry newEntry = pokemob.getPokedexEntry().getEvo(pokemob);
        if (newEntry != null && newEntry.getPokedexNb() == pokemob.getPokedexEntry().getPokedexNb())
        {
            if (pokemob.getPokedexEntry() == newEntry)
            {
                ITextComponent mess = CommandTools.makeTranslatedMessage("pokemob.megaevolve.command.revert", "green",
                        oldName);
                pokemob.displayMessageToOwner(mess);
                pokemob.setCombatState(CombatStates.MEGAFORME, false);
                mess = CommandTools.makeTranslatedMessage("pokemob.megaevolve.revert", "green", oldName, newEntry
                        .getUnlocalizedName());
                ICanEvolve.setDelayedMegaEvolve(pokemob, newEntry, mess);
            }
            else
            {
                ITextComponent mess = CommandTools.makeTranslatedMessage("pokemob.megaevolve.command.evolve", "green",
                        oldName);
                pokemob.displayMessageToOwner(mess);
                mess = CommandTools.makeTranslatedMessage("pokemob.megaevolve.success", "green", oldName, newEntry
                        .getUnlocalizedName());
                pokemob.setCombatState(CombatStates.MEGAFORME, true);
                ICanEvolve.setDelayedMegaEvolve(pokemob, newEntry, mess);
            }
        }
        else if (pokemob.getCombatState(CombatStates.MEGAFORME))
        {
            ITextComponent mess = CommandTools.makeTranslatedMessage("pokemob.megaevolve.command.revert", "green",
                    oldName);
            pokemob.displayMessageToOwner(mess);
            newEntry = pokemob.getPokedexEntry().getBaseForme();
            pokemob.setCombatState(CombatStates.MEGAFORME, false);
            mess = CommandTools.makeTranslatedMessage("pokemob.megaevolve.revert", "green", oldName, newEntry
                    .getUnlocalizedName());
            ICanEvolve.setDelayedMegaEvolve(pokemob, newEntry, mess);
        }
        else player.sendMessage(CommandTools.makeTranslatedMessage("pokemob.megaevolve.failed", "red", pokemob
                .getDisplayName()));
    }

    @Override
    public void write(final PacketBuffer buf)
    {
        final PacketBuffer buffer = new PacketBuffer(buf);
        buffer.writeInt(this.entityId);
        if (this.forme != null) buffer.writeString(this.forme.getName());
        else buffer.writeString("");
    }

}
