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
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSet;
import pokecube.api.moves.MoveEntry;
import pokecube.core.impl.PokecubeMod;

public class UseMoveTrigger extends SimpleCriterionTrigger<UseMoveTrigger.TriggerInstance>
{
    public static ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(PokecubeMod.ID, "use_move");

    public static final LootContextParamSet SET = Triggers.registerSet(ID, b -> b.required(Triggers.MOVE_NAME));

    public static LootContext createContext(ServerPlayer player, MoveEntry entry)
    {
        LootParams lootparams = new LootParams.Builder(player.serverLevel())
                .withParameter(Triggers.MOVE_NAME, entry.name).withParameter(Triggers.MOVE_TYPE, entry.type)
                .create(SET);
        return new LootContext.Builder(lootparams).create(Optional.empty());
    }

    @Override
    public Codec<UseMoveTrigger.TriggerInstance> codec()
    {
        return UseMoveTrigger.TriggerInstance.CODEC;
    }

    public void trigger(ServerPlayer player, MoveEntry packet)
    {
        LootContext lootcontext = createContext(player, packet);
        this.trigger(player, instance -> instance.matches(lootcontext));
    }

    public static record TriggerInstance(Optional<ContextAwarePredicate> player, Optional<ContextAwarePredicate> attack)
            implements SimpleCriterionTrigger.SimpleInstance
    {

        public static final Codec<UseMoveTrigger.TriggerInstance> CODEC = RecordCodecBuilder.create(instance -> instance
                .group(EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("player")
                        .forGetter(UseMoveTrigger.TriggerInstance::player),
                        EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("attack")
                                .forGetter(UseMoveTrigger.TriggerInstance::attack))
                .apply(instance, UseMoveTrigger.TriggerInstance::new));

        private boolean matches(LootContext context)
        {
            return attack().get().matches(context);
        }

        @Override
        public void validate(CriterionValidator validator)
        {
            SimpleCriterionTrigger.SimpleInstance.super.validate(validator);
            validator.validateEntity(this.attack, ".attack");
        }
    }
}
