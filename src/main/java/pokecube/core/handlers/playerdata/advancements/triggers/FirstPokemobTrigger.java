package pokecube.core.handlers.playerdata.advancements.triggers;

import com.google.gson.JsonObject;

import net.minecraft.advancements.critereon.AbstractCriterionTriggerInstance;
import net.minecraft.advancements.critereon.DeserializationContext;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.advancements.critereon.EntityPredicate.Composite;
import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import pokecube.core.impl.PokecubeMod;

public class FirstPokemobTrigger extends SimpleCriterionTrigger<FirstPokemobTrigger.Instance>
{
    public static class Instance extends AbstractCriterionTriggerInstance
    {
        public Instance(final Composite pred)
        {
            super(FirstPokemobTrigger.ID, pred);
        }

        public boolean test()
        {
            return true;
        }
    }

    public static ResourceLocation ID = new ResourceLocation(PokecubeMod.ID, "get_first_pokemob");

    public FirstPokemobTrigger()
    {}

    @Override
    public ResourceLocation getId()
    {
        return FirstPokemobTrigger.ID;
    }

    public void trigger(final ServerPlayer player)
    {
        this.trigger(player, (instance) -> {
            return instance.test();
        });
    }

    @Override
    protected Instance createInstance(JsonObject json, Composite composite, DeserializationContext conditions)
    {
        final EntityPredicate.Composite pred = EntityPredicate.Composite.fromJson(json, "player", conditions);
        return new Instance(pred);
    }
}
