package pokecube.legends.init.function;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Util;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.eventbus.api.Event.Result;
import pokecube.core.PokecubeCore;
import pokecube.core.PokecubeItems;
import pokecube.core.events.pokemob.CaptureEvent;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.items.pokecubes.EntityPokecube;
import pokecube.core.items.pokecubes.PokecubeManager;

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
            PokecubeCore.LOGGER.debug("Life: " + event.mob.getHealth() + "Max Life: " + event.mob.getMaxHealth());
            if (event.mob.getHealth() > event.mob.getMaxHealth() / 2)
            {
                if (catcher instanceof PlayerEntity) ((PlayerEntity) catcher).sendMessage(new TranslationTextComponent(
                        "pokecube.denied"), Util.NIL_UUID);
                event.setCanceled(true);
                event.setResult(Result.DENY);
            }
        }
        else
        {
            if (catcher instanceof PlayerEntity) ((PlayerEntity) catcher).sendMessage(new TranslationTextComponent(
                    "pokecube.denied"), Util.NIL_UUID);
            event.setCanceled(true);
            event.setResult(Result.DENY);
        }

        // No Catch normal Pokemobs
        if (dynamaxCube && !raidMob)
        {
            if (catcher instanceof PlayerEntity) ((PlayerEntity) catcher).sendMessage(new TranslationTextComponent(
                    "pokecube.denied"), Util.NIL_UUID);
            event.setCanceled(true);
            event.setResult(Result.DENY);
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
