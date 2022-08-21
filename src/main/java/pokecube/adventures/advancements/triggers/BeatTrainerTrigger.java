package pokecube.adventures.advancements.triggers;

import com.google.gson.JsonObject;

import net.minecraft.advancements.critereon.AbstractCriterionTriggerInstance;
import net.minecraft.advancements.critereon.DeserializationContext;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.advancements.critereon.EntityPredicate.Composite;
import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import pokecube.adventures.PokecubeAdv;
import pokecube.adventures.entity.trainer.LeaderNpc;
import pokecube.adventures.entity.trainer.TrainerBase;

public class BeatTrainerTrigger extends SimpleCriterionTrigger<BeatTrainerTrigger.Instance>
{
    public static ResourceLocation ID = new ResourceLocation(PokecubeAdv.MODID, "beat_trainer");

    public static class Instance extends AbstractCriterionTriggerInstance
    {
        public Instance(final Composite pred)
        {
            super(BeatTrainerTrigger.ID, pred);
        }

        public boolean test(final ServerPlayer player, final TrainerBase defeated)
        {
            return !(defeated instanceof LeaderNpc);
        }

    }

    public BeatTrainerTrigger()
    {}

    @Override
    public ResourceLocation getId()
    {
        return BeatTrainerTrigger.ID;
    }

    public void trigger(final ServerPlayer player, final TrainerBase defeated)
    {
        this.trigger(player, (instance) -> {
            return instance.test(player, defeated);
        });
    }

    @Override
    protected Instance createInstance(JsonObject json, Composite composite, DeserializationContext conditions)
    {
        final EntityPredicate.Composite pred = EntityPredicate.Composite.fromJson(json, "player", conditions);
        return new Instance(pred);
    }
}