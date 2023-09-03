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

public class EvolvePokemobTrigger extends SimpleCriterionTrigger<EvolvePokemobTrigger.Instance>
{
    public static class Instance extends AbstractCriterionTriggerInstance
    {
        final PokedexEntry entry;

        public Instance(ContextAwarePredicate predicate, final PokedexEntry entry)
        {
            super(EvolvePokemobTrigger.ID, predicate);
            this.entry = entry != null ? entry : Database.missingno;
        }

        public boolean test(final ServerPlayer player, final IPokemob pokemob)
        {
            return (this.entry == Database.missingno || pokemob.getPokedexEntry() == this.entry)
                    && pokemob.getOwner() != player;
        }

    }

    public static ResourceLocation ID = new ResourceLocation(PokecubeMod.ID, "evolve");

    public EvolvePokemobTrigger()
    {}

    @Override
    public ResourceLocation getId()
    {
        return EvolvePokemobTrigger.ID;
    }

    public void trigger(final ServerPlayer player, final IPokemob pokemob)
    {
        this.trigger(player, (instance) -> {
            return instance.test(player, pokemob);
        });
    }

    @Override
    protected Instance createInstance(JsonObject json, ContextAwarePredicate predicate, DeserializationContext conditions)
    {
        final ContextAwarePredicate pred = EntityPredicate.fromJson(json, "player", conditions);
        final String name = json.has("entry") ? json.get("entry").getAsString() : "";
        return new Instance(pred, Database.getEntry(name));
    }

}
