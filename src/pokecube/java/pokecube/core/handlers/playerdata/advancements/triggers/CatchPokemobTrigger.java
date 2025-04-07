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
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import pokecube.api.data.PokedexEntry;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.stats.CaptureStats;
import pokecube.api.utils.TagNames;
import pokecube.core.database.Database;
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
        LootParams lootparams = new LootParams.Builder(player.serverLevel())
                .withParameter(Triggers.POKEDEX_ENTRY, pokemob.getPokedexEntry())
                .withParameter(LootContextParams.THIS_ENTITY, pokemob.getEntity())
                .create(SimplePokemobTrigger.SIMPLE_POKEMOB_SET);
        return new LootContext.Builder(lootparams).create(Optional.empty());
    }

    public void trigger(ServerPlayer player, IPokemob pokemob)
    {
        LootContext lootcontext = createContext(player, pokemob);
        this.trigger(player, instance -> instance.matches(lootcontext));
    }

    public static record TriggerInstance(Optional<ContextAwarePredicate> player, Optional<ContextAwarePredicate> entry,
            Optional<ContextAwarePredicate> number) implements SimpleCriterionTrigger.SimpleInstance
    {

        public static final Codec<TriggerInstance> CODEC = RecordCodecBuilder.create(instance -> instance
                .group(EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("player").forGetter(TriggerInstance::player),
                        EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("entry").forGetter(TriggerInstance::entry),
                        EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("number").forGetter(TriggerInstance::number))
                .apply(instance, TriggerInstance::new));

        public boolean matches(LootContext context)
        {
            if (entry.isEmpty()) return false;
            if(!entry().stream().allMatch(e->e.matches(context))) return false;
            return number().stream().allMatch(e -> e.matches(context));
        }

        @Override
        public void validate(CriterionValidator validator)
        {
            SimpleCriterionTrigger.SimpleInstance.super.validate(validator);
            validator.validateEntity(this.entry, ".entry");
        }

    }

    public static class Instance
    {
        final PokedexEntry entry;
        boolean lenient = false;
        int number = -1;
        int sign = 0;

        public Instance(final ContextAwarePredicate predicate, final PokedexEntry entry, final boolean lenient,
                final int number, final int sign)
        {
            this.entry = entry != null ? entry : Database.missingno;
            this.lenient = lenient;
            this.number = number;
            this.sign = sign;
        }

        public boolean test(final ServerPlayer player, final IPokemob pokemob)
        {
            PokedexEntry entry = this.entry;
            PokedexEntry testEntry = pokemob.getPokedexEntry();
            boolean numCheck = true;
            if (this.lenient)
            {
                entry = entry.base ? entry : entry.getBaseForme();
                testEntry = testEntry.base ? testEntry : testEntry.getBaseForme();
            }
            if (this.number != -1)
            {
                int num = -1;
                if (entry == Database.missingno) num = CaptureStats.getNumberUniqueCaughtBy(player.getUUID());
                else num = CaptureStats.getTotalNumberOfPokemobCaughtBy(player.getUUID(), entry);
                if (num == -1) return false;
                numCheck = num * this.sign > this.number;
            }
            if (pokemob.getEntity().getPersistentData().getBoolean(TagNames.HATCHED)) return false;
            return numCheck && (entry == Database.missingno || testEntry == entry) && pokemob.getOwner() == player;
        }

    }
}
