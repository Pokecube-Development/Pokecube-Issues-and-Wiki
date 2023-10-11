package pokecube.api.entity.pokemob.commandhandlers;

import java.util.List;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import pokecube.api.data.PokedexEntry;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.entity.pokemob.ai.GeneralStates;
import pokecube.api.utils.DynamaxHelper;
import pokecube.api.utils.MegaEvolveHelper;
import pokecube.core.PokecubeCore;
import pokecube.core.blocks.maxspot.MaxTile;
import pokecube.core.eventhandlers.PokemobEventsHandler.MegaEvoTicker;
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
        if (!(world instanceof ServerLevel level)) return;

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

        boolean isDyna = DynamaxHelper.isDynamax(pokemob);
        boolean isMega = MegaEvolveHelper.isMega(pokemob);
        if (isMaxSpot)
        {
            PokedexEntry newEntry = entry;
            if (isDyna)
            {
                Component mess = TComponent.translatable("pokemob.dynamax.command.revert", oldName);
                pokemob.displayMessageToOwner(mess);
                mess = TComponent.translatable("pokemob.dynamax.revert", oldName);
                MegaEvoTicker.scheduleRevert(newEntry, pokemob, mess);
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
                DynamaxHelper.dynamax(pokemob, PokecubeCore.getConfig().dynamax_duration);
                MegaEvoTicker.scheduleEvolve(newEntry, pokemob, mess);
                return;
            }
        }
        PokedexEntry newEntry = entry;
        if (isDyna)
        {
            Component mess = TComponent.translatable("pokemob.dynamax.command.revert", oldName);
            pokemob.displayMessageToOwner(mess);
            mess = TComponent.translatable("pokemob.dynamax.revert", oldName);
            MegaEvoTicker.scheduleRevert(newEntry, pokemob, mess);
            return;
        }

        newEntry = pokemob.getPokedexEntry().getMegaEvo(pokemob);
        if (newEntry != null && !isMega)
        {
            Component mess = TComponent.translatable("pokemob.megaevolve.command.evolve", oldName);
            pokemob.displayMessageToOwner(mess);
            mess = TComponent.translatable("pokemob.megaevolve.success", oldName,
                    TComponent.translatable(newEntry.getUnlocalizedName()));
            MegaEvolveHelper.megaEvolve(pokemob, newEntry, mess);
        }
        else if (isMega)
        {
            Component mess = TComponent.translatable("pokemob.megaevolve.command.revert", oldName);
            pokemob.displayMessageToOwner(mess);
            newEntry = pokemob.getBasePokedexEntry();
            mess = TComponent.translatable("pokemob.megaevolve.revert", oldName,
                    TComponent.translatable(newEntry.getUnlocalizedName()));
            MegaEvoTicker.scheduleRevert(newEntry, pokemob, mess);
            
        }
        else thut.lib.ChatHelper.sendSystemMessage(player,
                TComponent.translatable("pokemob.megaevolve.failed", pokemob.getDisplayName()));
    }
}
