package pokecube.core.handlers.playerdata.advancements.triggers;

import net.minecraft.advancements.Criterion;
import net.minecraft.resources.ResourceLocation;
import pokecube.api.data.PokedexEntry;
import pokecube.core.impl.PokecubeMod;

import java.util.Optional;

public class KillPokemobTrigger extends SimplePokemobTrigger
{
    public static ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(PokecubeMod.ID, "kill");

    public static Criterion<PokedexEntryTriggerInstance> withEntry(PokedexEntry entry)
    {
        return Triggers.KILLPOKEMOB.get().createCriterion(new PokedexEntryTriggerInstance(Optional.empty(), entry));
    }
}
