package pokecube.core.handlers.playerdata.advancements.triggers;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.advancements.critereon.CriterionValidator;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import pokecube.api.data.PokedexEntry;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.core.database.Database;
import pokecube.core.eventhandlers.StatsCollector;
import pokecube.core.impl.PokecubeMod;

import java.util.Optional;

public class CatchPokemobTrigger extends SimpleCriterionTrigger<CatchPokemobTrigger.TriggerInstance>
{
    public static ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(PokecubeMod.ID, "catch");

    @Override
    public Codec<TriggerInstance> codec()
    {
        return TriggerInstance.CODEC;
    }

    public CatchPokemobTrigger()
    {}

    public static LootContext createContext(ServerPlayer player, IPokemob pokemob)
    {
        LootParams lootparams = new LootParams.Builder(player.serverLevel()).withParameter(Triggers.POKEDEX_ENTRY,
                        pokemob.getPokedexEntry()).withParameter(LootContextParams.THIS_ENTITY, player)
                .create(SimplePokemobTrigger.SIMPLE_POKEMOB_SET);
        return new LootContext.Builder(lootparams).create(Optional.empty());
    }

    public void trigger(ServerPlayer player, IPokemob pokemob)
    {
        LootContext lootcontext = createContext(player, pokemob);
        this.trigger(player, instance -> instance.matches(lootcontext));
    }

    public static Criterion<TriggerInstance> withEntry(PokedexEntry entry)
    {
        return Triggers.CATCHPOKEMOB.get().createCriterion(new TriggerInstance(Optional.empty(), entry, -1));
    }

    public static record TriggerInstance(Optional<ContextAwarePredicate> player, PokedexEntry entry, int number)
            implements SimpleCriterionTrigger.SimpleInstance
    {
        public TriggerInstance(Optional<ContextAwarePredicate> player, String entry, int number)
        {
            this(player, Database.getEntry(entry), number);
        }

        public static final Codec<TriggerInstance> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                        EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("player").forGetter(TriggerInstance::player),
                        Codec.STRING.optionalFieldOf("entry", null)
                                .forGetter(e -> e.entry() != null ? e.entry().getName() : "missingno"),
                        Codec.INT.optionalFieldOf("number", -1).forGetter(TriggerInstance::number))
                .apply(instance, TriggerInstance::new));

        public boolean matches(LootContext context)
        {
            if ((entry == null || entry == Database.missingno)) return false;
            if (!context.hasParam(Triggers.POKEDEX_ENTRY)) return false;
            if (!context.hasParam(LootContextParams.THIS_ENTITY)) return false;
            PokedexEntry test = context.getParam(Triggers.POKEDEX_ENTRY);
            if (entry != test) return false;
            if (!(context.getParam(LootContextParams.THIS_ENTITY) instanceof Player splayer)) return false;
            return StatsCollector.getCaptured(entry, splayer) > number();
        }

        @Override
        public void validate(CriterionValidator validator)
        {
            SimpleCriterionTrigger.SimpleInstance.super.validate(validator);
        }

    }
}
