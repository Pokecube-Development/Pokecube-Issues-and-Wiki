package pokecube.mobs.init;

import java.util.List;

import com.google.common.collect.Lists;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import pokecube.api.PokecubeAPI;
import pokecube.api.data.Pokedex;
import pokecube.api.data.PokedexEntry;
import pokecube.core.database.Database;
import pokecube.mobs.PokecubeMobs;

public class PokemobSounds
{
    private static void registerIfNotPresent(ResourceLocation sound, SoundEvent event)
    {
        try
        {
            PokecubeMobs.SOUNDS.register(sound.getPath(), () -> event);
        }
        catch (IllegalArgumentException e)
        {
            // pass here, it means it was already present!
        }
    }

    public static void initMobSounds()
    {
        // Register sounds for the pokemobs
        final List<PokedexEntry> toProcess = Lists.newArrayList(Pokedex.getInstance().getRegisteredEntries());
        toProcess.sort(Database.COMPARATOR);
        for (final PokedexEntry e : toProcess)
        {
            if (e.getModId() == null || e.soundEvent != null) continue;
            if (e.sound == null)
            {
                if (e.customSound != null) e.setSound("mobs." + Database.trim(e.customSound));
                else
                {
                    if (e.base) e.setSound("mobs." + e.getTrimmedName());
                    else e.setSound("mobs." + e.getBaseForme().getTrimmedName());
                    e.sound = new ResourceLocation(PokecubeMobs.MODID, e.sound.getPath());
                }
            }
            PokecubeAPI.LOGGER.debug(e + " has Sound: " + e.sound);
            e.soundEvent = new SoundEvent(e.sound);
            registerIfNotPresent(e.sound, e.soundEvent);
        }
    }
}
