package pokecube.legends.init.function;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.eventbus.api.Event.Result;
import pokecube.api.PokecubeAPI;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.events.pokemobs.CaptureEvent;
import pokecube.core.PokecubeItems;
import pokecube.core.items.pokecubes.EntityPokecube;
import pokecube.core.items.pokecubes.EntityPokecubeBase;
import pokecube.core.items.pokecubes.PokecubeManager;
import pokecube.core.items.pokecubes.helper.CaptureManager;
import thut.lib.TComponent;

public class RaidCapture
{
    public static void CatchPokemobRaid(final CaptureEvent.Pre event)
    {
        final ResourceLocation id = PokecubeItems.getCubeId(event.getFilledCube());
        final Entity catcher = ((EntityPokecube) event.pokecube).shootingEntity;

        final boolean dynamaxCube = id.toString().equals("pokecube_legends:dyna");
        final boolean raidMob = event.mob.getPersistentData().getBoolean("pokecube_legends:raid_mob");

        // Catch Raids
        if (raidMob) if (dynamaxCube)
        {
            PokecubeAPI.LOGGER.debug("Life: " + event.mob.getHealth() + "Max Life: " + event.mob.getMaxHealth());
            if (event.mob.getHealth() > event.mob.getMaxHealth() / 2)
            {
                if (catcher instanceof Player player)
                    thut.lib.ChatHelper.sendSystemMessage(player, TComponent.translatable("pokecube.denied"));
                event.setCanceled(true);
                event.setResult(Result.DENY);
                CaptureManager.onCaptureDenied((EntityPokecubeBase) event.pokecube);
            }
        }
        else
        {
            if (catcher instanceof Player player)
                thut.lib.ChatHelper.sendSystemMessage(player, TComponent.translatable("pokecube.denied"));
            event.setCanceled(true);
            event.setResult(Result.DENY);
            CaptureManager.onCaptureDenied((EntityPokecubeBase) event.pokecube);
        }

        // No Catch normal Pokemobs
        if (dynamaxCube && !raidMob)
        {
            if (catcher instanceof Player player)
                thut.lib.ChatHelper.sendSystemMessage(player, TComponent.translatable("pokecube.denied"));
            event.setCanceled(true);
            event.setResult(Result.DENY);
            CaptureManager.onCaptureDenied((EntityPokecubeBase) event.pokecube);
        }
    }

    public static void PostCatchPokemobRaid(final CaptureEvent.Post event)
    {
        final ResourceLocation id = PokecubeItems.getCubeId(event.getFilledCube());

        // Catch Raids
        if (id.toString().equals("pokecube_legends:dyna"))
        {
            final IPokemob pokemob = event.getCaught();
            pokemob.setPokecube(PokecubeItems.getStack("pokecube"));

            // Pokemob Level Spawm
            int level = pokemob.getLevel();

            if (level <= 10 || level >= 40)
            {
                level = 20;
                pokemob.setForSpawn(level, false);
            }

            event.setFilledCube(PokecubeManager.pokemobToItem(pokemob), true);
        }
    }
}
