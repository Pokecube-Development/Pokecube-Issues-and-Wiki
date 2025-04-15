package pokecube.core.handlers.playerdata.advancements.triggers;

import net.minecraft.advancements.Criterion;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import pokecube.api.data.PokedexEntry;
import pokecube.core.impl.PokecubeMod;

import java.util.Optional;

public class FirstPokemobTrigger extends SimplePokemobTrigger
{
    public static ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(PokecubeMod.ID, "get_first_pokemob");

    public void trigger(ServerPlayer player)
    {
        this.trigger(player, instance -> true);
    }

    public static Criterion<PokedexEntryTriggerInstance> withEntry(PokedexEntry entry)
    {
        return Triggers.FIRSTPOKEMOB.get().createCriterion(new PokedexEntryTriggerInstance(Optional.empty(), entry));
    }
}
