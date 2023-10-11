package pokecube.api.utils;

import net.minecraft.network.chat.Component;
import pokecube.api.PokecubeAPI;
import pokecube.api.data.PokedexEntry;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.events.pokemobs.ChangeForm;
import pokecube.core.eventhandlers.PokemobEventsHandler.MegaEvoTicker;
import thut.api.Tracker;

public class MegaEvolveHelper
{
    public static void init()
    {
        PokecubeAPI.POKEMOB_BUS.addListener(MegaEvolveHelper::onFormRevert);
    }

    public static boolean isMega(IPokemob pokemob)
    {
        var entity = pokemob.getEntity();
        return entity.getPersistentData().contains("pokecube:megatime");
    }

    public static void megaEvolve(IPokemob pokemob, PokedexEntry newEntry, Component mess)
    {
        var entity = pokemob.getEntity();
        entity.getPersistentData().putLong("pokecube:megatime", Tracker.instance().getTick());
        MegaEvoTicker.scheduleEvolve(newEntry, pokemob, mess);
    }

    private static void onMegaRevert(IPokemob pokemob)
    {
        var entity = pokemob.getEntity();
        entity.getPersistentData().remove("pokecube:megatime");
    }

    private static void onFormRevert(ChangeForm.Revert event)
    {
        onMegaRevert(event.getPokemob());
    }
}
