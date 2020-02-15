package pokecube.core.handlers.events;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.eventbus.api.Event.Result;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.server.permission.IPermissionHandler;
import net.minecraftforge.server.permission.PermissionAPI;
import net.minecraftforge.server.permission.context.PlayerContext;
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
import pokecube.core.utils.Permissions;

public class StatsHandler
{
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void canCapture(final CaptureEvent.Pre evt)
    {
        final ResourceLocation id = PokecubeItems.getCubeId(evt.filledCube);
        if (IPokecube.BEHAVIORS.containsKey(id))
        {
            final PokecubeBehavior cube = IPokecube.BEHAVIORS.getValue(id);
            cube.onPreCapture(evt);
        }
        if (evt.caught == null) return;
        final PokedexEntry entry = evt.caught.getPokedexEntry();
        if (evt.caught.getGeneralState(GeneralStates.TAMED)) evt.setResult(Result.DENY);
        if (evt.caught.getGeneralState(GeneralStates.DENYCAPTURE)) evt.setResult(Result.DENY);
        final Entity catcher = ((EntityPokecube) evt.pokecube).shootingEntity;
        if (!EntityPokecubeBase.canCaptureBasedOnConfigs(evt.caught))
        {
            evt.setCanceled(true);
            if (catcher instanceof PlayerEntity) ((PlayerEntity) catcher).sendMessage(new TranslationTextComponent(
                    "pokecube.denied"));
            evt.pokecube.entityDropItem(((EntityPokecube) evt.pokecube).getItem(), (float) 0.5);
            evt.pokecube.remove();
            return;
        }
        final Config config = PokecubeCore.getConfig();
        // Check permissions
        if (catcher instanceof PlayerEntity && (config.permsCapture || config.permsCaptureSpecific))
        {
            final PlayerEntity player = (PlayerEntity) catcher;
            final IPermissionHandler handler = PermissionAPI.getPermissionHandler();
            final PlayerContext context = new PlayerContext(player);
            boolean denied = false;
            if (config.permsCapture && !handler.hasPermission(player.getGameProfile(), Permissions.CATCHPOKEMOB,
                    context)) denied = true;
            if (config.permsCaptureSpecific && !denied && !handler.hasPermission(player.getGameProfile(),
                    Permissions.CATCHSPECIFIC.get(entry), context)) denied = true;
            if (denied)
            {
                evt.setCanceled(true);
                if (catcher instanceof PlayerEntity) ((PlayerEntity) catcher).sendMessage(new TranslationTextComponent(
                        "pokecube.denied"));
                evt.pokecube.entityDropItem(((EntityPokecube) evt.pokecube).getItem(), (float) 0.5);
                evt.pokecube.remove();
                return;
            }
        }

        if (ISpecialCaptureCondition.captureMap.containsKey(entry))
        {
            boolean deny = true;
            try
            {
                deny = !ISpecialCaptureCondition.captureMap.get(entry).canCapture(catcher, evt.caught);
            }
            catch (final Exception e)
            {
                PokecubeCore.LOGGER.error("Error checking capture for " + entry, e);
            }

            if (deny)
            {
                evt.setCanceled(true);
                if (catcher instanceof PlayerEntity) ((PlayerEntity) catcher).sendMessage(new TranslationTextComponent(
                        "pokecube.denied"));
                evt.pokecube.entityDropItem(((EntityPokecube) evt.pokecube).getItem(), (float) 0.5);
                evt.pokecube.remove();
                return;
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST, receiveCanceled = false)
    public static void recordCapture(final CaptureEvent.Post evt)
    {
        final ResourceLocation id = PokecubeItems.getCubeId(evt.filledCube);
        if (IPokecube.BEHAVIORS.containsKey(id))
        {
            final PokecubeBehavior cube = IPokecube.BEHAVIORS.getValue(id);
            cube.onPostCapture(evt);
        }
        if (evt.caught == null || evt.isCanceled() || evt.caught.isShadow()) return;
        StatsCollector.addCapture(evt.caught);
    }

    @SubscribeEvent(priority = EventPriority.LOWEST, receiveCanceled = false)
    public static void recordEvolve(final EvolveEvent.Post evt)
    {
        if (evt.mob.isShadow()) return;
        StatsCollector.addCapture(evt.mob);
    }

    @SubscribeEvent(priority = EventPriority.LOWEST, receiveCanceled = false)
    public static void recordHatch(final EggEvent.Hatch evt)
    {
        StatsCollector.addHatched(evt.egg);
    }

    @SubscribeEvent(priority = EventPriority.LOWEST, receiveCanceled = false)
    public static void recordKill(final KillEvent evt)
    {
        if (!evt.killed.isShadow()) StatsCollector.addKill(evt.killed, evt.killer);
    }

    @SubscribeEvent(priority = EventPriority.LOWEST, receiveCanceled = false)
    public static void recordTrade(final TradeEvent evt)
    {
        if (evt.mob == null || evt.mob.isShadow()) return;
        StatsCollector.addCapture(evt.mob);
    }
}
