package pokecube.core.handlers.playerdata.advancements.triggers;

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
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import pokecube.api.data.PokedexEntry;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.core.database.Database;
import pokecube.core.handlers.playerdata.advancements.triggers.SimplePokemobTrigger.PokedexEntryTriggerInstance;

import java.util.Optional;

public abstract class SimplePokemobTrigger extends SimpleCriterionTrigger<PokedexEntryTriggerInstance>
{
    @Override
    public Codec<PokedexEntryTriggerInstance> codec()
    {
        return PokedexEntryTriggerInstance.CODEC;
    }

    public static LootContext createContext(ServerPlayer player, IPokemob pokemob)
    {
        LootParams lootparams = new LootParams.Builder(player.serverLevel()).withParameter(
                        LootContextParams.THIS_ENTITY, player)
                .withOptionalParameter(Triggers.POKEDEX_ENTRY, pokemob.getPokedexEntry()).create(SIMPLE_POKEMOB_SET);
        return new LootContext.Builder(lootparams).create(Optional.empty());
    }

    public void trigger(ServerPlayer player, IPokemob pokemob)
    {
        LootContext lootcontext = createContext(player, pokemob);
        this.trigger(player, instance -> instance.matches(lootcontext));
    }

    public static final LootContextParamSet SIMPLE_POKEMOB_SET = Triggers.registerSet(
            ResourceLocation.fromNamespaceAndPath("pokecube", "any_pokedex_entry"),
            b -> b.required(LootContextParams.THIS_ENTITY).optional(Triggers.POKEDEX_ENTRY));

    public static record PokedexEntryTriggerInstance(Optional<ContextAwarePredicate> player, PokedexEntry entry)
            implements SimpleCriterionTrigger.SimpleInstance
    {
        public PokedexEntryTriggerInstance(Optional<ContextAwarePredicate> player, String entry)
        {
            this(player, Database.getEntry(entry));
        }

        public static final Codec<PokedexEntryTriggerInstance> CODEC = RecordCodecBuilder.create(
                instance -> instance.group(EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("player")
                                .forGetter(PokedexEntryTriggerInstance::player), Codec.STRING.optionalFieldOf("entry", null)
                                .forGetter(e -> e.entry() != null ? e.entry().getName() : "missingno"))
                        .apply(instance, PokedexEntryTriggerInstance::new));

        public boolean matches(LootContext context)
        {
            return matches(context, false);
        }

        public boolean matches(LootContext context, boolean allowEmpty)
        {
            if (!allowEmpty && (entry == null || entry == Database.missingno)) return false;
            if (!context.hasParam(Triggers.POKEDEX_ENTRY)) return false;
            PokedexEntry test = context.getParam(Triggers.POKEDEX_ENTRY);
            return entry == test;
        }

        @Override
        public void validate(CriterionValidator validator)
        {
            SimpleCriterionTrigger.SimpleInstance.super.validate(validator);
        }

    }
}
