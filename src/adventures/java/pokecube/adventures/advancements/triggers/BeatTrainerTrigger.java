package pokecube.adventures.advancements.triggers;

import java.util.Optional;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import pokecube.adventures.PokecubeAdv;

public class BeatTrainerTrigger extends SimpleCriterionTrigger<BeatTrainerTrigger.TriggerInstance>
{
    public static ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(PokecubeAdv.MODID, "beat_trainer");

    @Override
    public Codec<TriggerInstance> codec()
    {
        return TriggerInstance.CODEC;
    }

    public BeatTrainerTrigger()
    {}

    public void trigger(ServerPlayer player, LivingEntity trainer)
    {
        this.trigger(player, instance -> true);
    }

    public static record TriggerInstance(Optional<ContextAwarePredicate> player)
            implements SimpleCriterionTrigger.SimpleInstance
    {
        public static final Codec<TriggerInstance> CODEC = RecordCodecBuilder.create(instance -> instance
                .group(EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("player").forGetter(TriggerInstance::player))
                .apply(instance, TriggerInstance::new));
    }
}