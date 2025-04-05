package pokecube.core.handlers.playerdata.advancements.triggers;

import java.util.Optional;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.advancements.critereon.CriterionValidator;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.storage.loot.LootContext;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.core.impl.PokecubeMod;

public class BreedPokemobTrigger extends SimpleCriterionTrigger<BreedPokemobTrigger.TriggerInstance>
{
    public static ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(PokecubeMod.ID, "breed");

    @Override
    public Codec<TriggerInstance> codec()
    {
        return TriggerInstance.CODEC;
    }

    public void trigger(final ServerPlayer player, final IPokemob first, final IPokemob second)
    {
        LootContext lootcontext = SimplePokemobTrigger.createContext(player, first);
        LootContext lootcontext1 = SimplePokemobTrigger.createContext(player, second);
        this.trigger(player, instance -> instance.matches(lootcontext, lootcontext1));
    }

    public static record TriggerInstance(Optional<ContextAwarePredicate> player,
            Optional<ContextAwarePredicate> parent_a, Optional<ContextAwarePredicate> parent_b)
            implements SimpleCriterionTrigger.SimpleInstance
    {

        public static final Codec<TriggerInstance> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("player").forGetter(TriggerInstance::player),
                EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("parent_a").forGetter(TriggerInstance::parent_a),
                EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("parent_b").forGetter(TriggerInstance::parent_b))
                .apply(instance, TriggerInstance::new));

        public boolean matches(LootContext contextA, LootContext contextB)
        {
            boolean A_matches = !parent_a.isPresent() || (parent_a.get().matches(contextA));
            boolean B_matches = !parent_b.isPresent() || (parent_b.get().matches(contextB));
            return A_matches && B_matches;
        }

        @Override
        public void validate(CriterionValidator validator)
        {
            SimpleCriterionTrigger.SimpleInstance.super.validate(validator);
            validator.validateEntity(this.parent_a, ".parent_a");
            validator.validateEntity(this.parent_b, ".parent_b");
        }

    }
}
