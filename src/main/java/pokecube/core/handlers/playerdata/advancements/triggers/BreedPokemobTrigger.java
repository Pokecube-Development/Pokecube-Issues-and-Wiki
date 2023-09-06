package pokecube.core.handlers.playerdata.advancements.triggers;

import com.google.gson.JsonObject;

import net.minecraft.advancements.critereon.AbstractCriterionTriggerInstance;
import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.advancements.critereon.DeserializationContext;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import pokecube.api.data.PokedexEntry;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.core.database.Database;
import pokecube.core.impl.PokecubeMod;

public class BreedPokemobTrigger extends SimpleCriterionTrigger<BreedPokemobTrigger.Instance>
{
    public static class Instance extends AbstractCriterionTriggerInstance
    {
        final PokedexEntry mate1;
        final PokedexEntry mate2;

        public Instance(ContextAwarePredicate predicate, final PokedexEntry mate1, final PokedexEntry mate2)
        {
            super(BreedPokemobTrigger.ID, predicate);
            this.mate1 = mate1 != null ? mate1 : Database.missingno;
            this.mate2 = mate2 != null ? mate2 : Database.missingno;
        }

        public boolean test(final ServerPlayer player, final IPokemob first, final IPokemob second)
        {
            if (!(first.getOwner() == player || second.getOwner() == player)) return false;

            IPokemob firstmate = null;
            IPokemob secondmate = null;

            if (first.getPokedexEntry() == this.mate1)
            {
                firstmate = first;
                secondmate = second;
            }
            else if (first.getPokedexEntry() == this.mate2)
            {
                firstmate = second;
                secondmate = first;
            }

            final boolean firstMatch = firstmate.getPokedexEntry() == this.mate1 || this.mate1 == Database.missingno;
            final boolean secondMatch = secondmate.getPokedexEntry() == this.mate2 || this.mate2 == Database.missingno;

            return firstMatch && secondMatch;
        }
    }

    public static ResourceLocation ID = new ResourceLocation(PokecubeMod.ID, "breed");

    public BreedPokemobTrigger()
    {}

    @Override
    public ResourceLocation getId()
    {
        return BreedPokemobTrigger.ID;
    }

    public void trigger(final ServerPlayer player, final IPokemob first, final IPokemob second)
    {
        this.trigger(player, (instance) -> {
            return instance.test(player, first, second);
        });
    }

    @Override
    protected Instance createInstance(JsonObject json, ContextAwarePredicate predicate, DeserializationContext conditions)
    {
        final ContextAwarePredicate pred = EntityPredicate.fromJson(json, "player", conditions);
        final String mate1 = json.has("mate1") ? json.get("mate1").getAsString() : "";
        final String mate2 = json.has("mate2") ? json.get("mate2").getAsString() : "";
        return new BreedPokemobTrigger.Instance(pred, Database.getEntry(mate1), Database.getEntry(mate2));
    }
}
