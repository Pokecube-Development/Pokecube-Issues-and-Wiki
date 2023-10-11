package pokecube.api.entity.pokemob.commandhandlers;

import java.util.List;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import pokecube.api.data.PokedexEntry;
import pokecube.api.entity.pokemob.ICanEvolve;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.entity.pokemob.ai.CombatStates;
import pokecube.api.entity.pokemob.ai.GeneralStates;
import pokecube.core.PokecubeCore;
import pokecube.core.blocks.maxspot.MaxTile;
import pokecube.core.database.Database;
import pokecube.core.eventhandlers.SpawnHandler;
import pokecube.core.eventhandlers.SpawnHandler.ForbiddenEntry;
import pokecube.core.handlers.PokecubePlayerDataHandler;
import pokecube.core.items.megastuff.MegaCapability;
import pokecube.core.network.pokemobs.PacketCommand.DefaultHandler;
import thut.api.Tracker;
import thut.lib.TComponent;

public class ChangeFormHandler extends DefaultHandler
{
    public ChangeFormHandler()
    {}

    @Override
    public void handleCommand(final IPokemob pokemob) throws Exception
    {
        final LivingEntity owner = pokemob.getOwner();

        final Entity mob = pokemob.getEntity();
        Player player = owner instanceof Player p ? p : null;
        final Level world = mob.getLevel();
        final BlockPos pos = mob.blockPosition();
        final MinecraftServer server = mob.getServer();

        if (pokemob.getGeneralState(GeneralStates.EVOLVING) || server == null || owner == null) return;

        final boolean hasRing = player == null || MegaCapability.canMegaEvolve(owner, pokemob);
        if (!hasRing)
        {
            thut.lib.ChatHelper.sendSystemMessage(player,
                    TComponent.translatable("pokecube.mega.noring", pokemob.getDisplayName()));
            return;
        }
        final PokedexEntry entry = pokemob.getPokedexEntry();
        final Component oldName = pokemob.getDisplayName();

        // Check dynamax/gigantamax first.
        List<ForbiddenEntry> reasons = SpawnHandler.getForbiddenEntries(world, pos);
        boolean isMaxSpot = false;
        for (ForbiddenEntry e : reasons)
        {
            if (e.reason == MaxTile.MAXSPOT)
            {
                isMaxSpot = true;
                break;
            }
        }

        boolean gigant = pokemob.getCombatState(CombatStates.GIGANTAMAX);
        boolean isDyna = pokemob.getCombatState(CombatStates.DYNAMAX);
        if (isMaxSpot)
        {
            PokedexEntry newEntry = entry;

            if (gigant && !isDyna)
            {
                newEntry = Database.getEntry(newEntry.getTrimmedName() + "-gmax");
                if (newEntry == null) gigant = false;
            }
            if (isDyna)
            {
                Component mess = TComponent.translatable("pokemob.dynamax.command.revert", oldName);
                pokemob.displayMessageToOwner(mess);
                pokemob.setCombatState(CombatStates.MEGAFORME, false);
                mess = TComponent.translatable("pokemob.dynamax.revert", oldName);
                ICanEvolve.setDelayedMegaEvolve(pokemob, newEntry, mess, true);
                return;
            }
            else
            {
                final long dynatime = PokecubePlayerDataHandler.getCustomDataTag(owner.getUUID())
                        .getLong("pokecube:dynatime");
                final long time = Tracker.instance().getTick();
                final long dynaagain = dynatime + PokecubeCore.getConfig().dynamax_cooldown;
                if (dynatime != 0 && time < dynaagain)
                {
                    thut.lib.ChatHelper.sendSystemMessage(player,
                            TComponent.translatable("pokemob.dynamax.too_soon", pokemob.getDisplayName()));
                    return;
                }
                Component mess = TComponent.translatable("pokemob.dynamax.command.evolve", oldName);
                pokemob.displayMessageToOwner(mess);
                mess = TComponent.translatable("pokemob.dynamax.success", oldName);
                if (gigant) pokemob.setCombatState(CombatStates.MEGAFORME, true);
                ICanEvolve.setDelayedMegaEvolve(pokemob, newEntry, mess, true);
                return;
            }
        }

        PokedexEntry newEntry = entry;
        if (gigant && !isDyna)
        {
            newEntry = Database.getEntry(newEntry.getTrimmedName() + "-gmax");
            if (newEntry == null) gigant = false;
        }
        if (isDyna || gigant)
        {
            Component mess = TComponent.translatable("pokemob.dynamax.command.revert", oldName);
            pokemob.displayMessageToOwner(mess);
            pokemob.setCombatState(CombatStates.MEGAFORME, false);
            mess = TComponent.translatable("pokemob.dynamax.revert", oldName);
            ICanEvolve.setDelayedMegaEvolve(pokemob, newEntry, mess, true);
            return;
        }

        newEntry = pokemob.getPokedexEntry().getMegaEvo(pokemob);
        if (newEntry != null && newEntry.getPokedexNb() == pokemob.getPokedexEntry().getPokedexNb())
        {
            if (pokemob.getPokedexEntry() == newEntry)
            {
                Component mess = TComponent.translatable("pokemob.megaevolve.command.revert", oldName);
                pokemob.displayMessageToOwner(mess);
                pokemob.setCombatState(CombatStates.MEGAFORME, false);
                mess = TComponent.translatable("pokemob.megaevolve.revert", oldName,
                        TComponent.translatable(newEntry.getUnlocalizedName()));
                ICanEvolve.setDelayedMegaEvolve(pokemob, newEntry, mess);
            }
            else
            {
                Component mess = TComponent.translatable("pokemob.megaevolve.command.evolve", oldName);
                pokemob.displayMessageToOwner(mess);
                mess = TComponent.translatable("pokemob.megaevolve.success", oldName,
                        TComponent.translatable(newEntry.getUnlocalizedName()));
                pokemob.setCombatState(CombatStates.MEGAFORME, true);
                ICanEvolve.setDelayedMegaEvolve(pokemob, newEntry, mess);
            }
        }
        else if (pokemob.getCombatState(CombatStates.MEGAFORME))
        {
            Component mess = TComponent.translatable("pokemob.megaevolve.command.revert", oldName);
            pokemob.displayMessageToOwner(mess);
            newEntry = pokemob.getBasePokedexEntry();
            pokemob.setCombatState(CombatStates.MEGAFORME, false);
            mess = TComponent.translatable("pokemob.megaevolve.revert", oldName,
                    TComponent.translatable(newEntry.getUnlocalizedName()));
            ICanEvolve.setDelayedMegaEvolve(pokemob, newEntry, mess);
        }
        else thut.lib.ChatHelper.sendSystemMessage(player,
                TComponent.translatable("pokemob.megaevolve.failed", pokemob.getDisplayName()));
    }
}
