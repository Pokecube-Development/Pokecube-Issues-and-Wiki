package pokecube.core.handlers.events;

import net.minecraft.Util;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.eventbus.api.Event.Result;
import net.minecraftforge.eventbus.api.EventPriority;
import pokecube.core.PokecubeCore;
import pokecube.core.PokecubeItems;
import pokecube.core.database.PokedexEntry;
import pokecube.core.database.stats.ISpecialCaptureCondition;
import pokecube.core.database.stats.StatsCollector;
import pokecube.core.events.EggEvent;
import pokecube.core.events.pokemob.CaptureEvent;
import pokecube.core.events.pokemob.EvolveEvent;
import pokecube.core.events.pokemob.TradeEvent;
import pokecube.core.events.pokemob.combat.KillEvent;
import pokecube.core.handlers.Config;
import pokecube.core.interfaces.IPokecube;
import pokecube.core.interfaces.IPokecube.PokecubeBehavior;
import pokecube.core.interfaces.pokemob.ai.GeneralStates;
import pokecube.core.items.pokecubes.EntityPokecube;
import pokecube.core.items.pokecubes.EntityPokecubeBase;
import pokecube.core.items.pokecubes.helper.CaptureManager;
import pokecube.core.utils.PermNodes;
import pokecube.core.utils.Permissions;

public class StatsHandler
{
    public static void register()
    {
        // This checks if the capture is allowed, and cancels the event
        // otherwise. It checks things such as: if the pokecube can capture it,
        // if the player is allowed to capture it, and if the pokemob is already
        // tamed, etc.
        PokecubeCore.POKEMOB_BUS.addListener(EventPriority.HIGHEST, StatsHandler::canCapture);

        // From here down, they are lowest, false to allow addons to override
        // the behaviour.
        // These just record the given event for use in things like pokedex
        // stats, etc
        PokecubeCore.POKEMOB_BUS.addListener(EventPriority.LOWEST, false, StatsHandler::recordCapture);
        PokecubeCore.POKEMOB_BUS.addListener(EventPriority.LOWEST, false, StatsHandler::recordEvolve);
        PokecubeCore.POKEMOB_BUS.addListener(EventPriority.LOWEST, false, StatsHandler::recordHatch);
        PokecubeCore.POKEMOB_BUS.addListener(EventPriority.LOWEST, false, StatsHandler::recordKill);
        PokecubeCore.POKEMOB_BUS.addListener(EventPriority.LOWEST, false, StatsHandler::recordTrade);

    }

    private static void canCapture(final CaptureEvent.Pre evt)
    {
        final ResourceLocation id = PokecubeItems.getCubeId(evt.getFilledCube());
        if (IPokecube.BEHAVIORS.containsKey(id))
        {
            final PokecubeBehavior cube = IPokecube.BEHAVIORS.getValue(id);
            cube.onPreCapture(evt);
        }
        if (evt.getCaught() == null) return;
        final PokedexEntry entry = evt.getCaught().getPokedexEntry();
        if (evt.getCaught().getGeneralState(GeneralStates.TAMED)) evt.setResult(Result.DENY);
        if (evt.getCaught().getGeneralState(GeneralStates.DENYCAPTURE)) evt.setResult(Result.DENY);
        final Entity catcher = ((EntityPokecube) evt.pokecube).shootingEntity;
        if (!EntityPokecubeBase.canCaptureBasedOnConfigs(evt.getCaught()))
        {
            evt.setCanceled(true);
            if (catcher instanceof Player)
                ((Player) catcher).sendMessage(new TranslatableComponent("pokecube.denied"), Util.NIL_UUID);
            CaptureManager.onCaptureDenied((EntityPokecubeBase) evt.pokecube);
            return;
        }
        final Config config = PokecubeCore.getConfig();
        // Check permissions
        if (catcher instanceof ServerPlayer player && (config.permsCapture || config.permsCaptureSpecific))
        {
            boolean denied = false;
            if (config.permsCapture && !PermNodes.getBooleanPerm(player, Permissions.CATCHPOKEMOB)) denied = true;
            if (config.permsCaptureSpecific && !denied
                    && !PermNodes.getBooleanPerm(player, Permissions.CATCHSPECIFIC.get(entry)))
                denied = true;
            if (denied)
            {
                evt.setCanceled(true);
                if (catcher instanceof Player)
                    ((Player) catcher).sendMessage(new TranslatableComponent("pokecube.denied"), Util.NIL_UUID);
                CaptureManager.onCaptureDenied((EntityPokecubeBase) evt.pokecube);
                return;
            }
        }

        if (ISpecialCaptureCondition.captureMap.containsKey(entry))
        {
            boolean deny = true;
            final ISpecialCaptureCondition condition = ISpecialCaptureCondition.captureMap.get(entry);
            try
            {
                deny = !condition.canCapture(catcher, evt.getCaught());
            }
            catch (final Exception e)
            {
                PokecubeCore.LOGGER.error("Error checking capture for " + entry, e);
            }

            if (deny)
            {
                evt.setCanceled(true);
                if (catcher instanceof Player)
                    ((Player) catcher).sendMessage(new TranslatableComponent("pokecube.denied"), Util.NIL_UUID);
                condition.onCaptureFail(catcher, evt.getCaught());
                CaptureManager.onCaptureDenied((EntityPokecubeBase) evt.pokecube);
                return;
            }
        }
    }

    private static void recordCapture(final CaptureEvent.Post evt)
    {
        final ResourceLocation id = PokecubeItems.getCubeId(evt.getFilledCube());
        if (IPokecube.BEHAVIORS.containsKey(id))
        {
            final PokecubeBehavior cube = IPokecube.BEHAVIORS.getValue(id);
            cube.onPostCapture(evt);
        }
        if (evt.getCaught() == null || evt.isCanceled() || evt.getCaught().isShadow()) return;
        StatsCollector.addCapture(evt.getCaught());
    }

    private static void recordEvolve(final EvolveEvent.Post evt)
    {
        if (evt.mob.isShadow()) return;
        StatsCollector.addCapture(evt.mob);
    }

    private static void recordHatch(final EggEvent.Hatch evt)
    {
        StatsCollector.addHatched(evt.egg);
    }

    private static void recordKill(final KillEvent evt)
    {
        if (!evt.killed.isShadow()) StatsCollector.addKill(evt.killed, evt.killer);
    }

    private static void recordTrade(final TradeEvent evt)
    {
        if (evt.mob == null || evt.mob.isShadow()) return;
        StatsCollector.addCapture(evt.mob);
    }
}
