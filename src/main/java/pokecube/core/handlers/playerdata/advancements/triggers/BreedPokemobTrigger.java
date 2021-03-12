package pokecube.core.handlers.playerdata.advancements.triggers;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.gson.JsonObject;

import net.minecraft.advancements.ICriterionTrigger;
import net.minecraft.advancements.PlayerAdvancements;
import net.minecraft.advancements.criterion.CriterionInstance;
import net.minecraft.advancements.criterion.EntityPredicate;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.loot.ConditionArrayParser;
import net.minecraft.util.ResourceLocation;
import pokecube.core.database.Database;
import pokecube.core.database.PokedexEntry;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.PokecubeMod;

public class BreedPokemobTrigger implements ICriterionTrigger<BreedPokemobTrigger.Instance>
{
    public static class Instance extends CriterionInstance
    {
        final PokedexEntry mate1;
        final PokedexEntry mate2;

        public Instance(final EntityPredicate.AndPredicate player, final PokedexEntry mate1, final PokedexEntry mate2)
        {
            super(BreedPokemobTrigger.ID, player);
            this.mate1 = mate1 != null ? mate1 : Database.missingno;
            this.mate2 = mate2 != null ? mate2 : Database.missingno;
        }

        public boolean test(final ServerPlayerEntity player, final IPokemob first, final IPokemob second)
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

    static class Listeners
    {
        private final PlayerAdvancements                                            playerAdvancements;
        private final Set<ICriterionTrigger.Listener<BreedPokemobTrigger.Instance>> listeners = Sets.<ICriterionTrigger.Listener<BreedPokemobTrigger.Instance>> newHashSet();

        public Listeners(final PlayerAdvancements playerAdvancementsIn)
        {
            this.playerAdvancements = playerAdvancementsIn;
        }

        public void add(final ICriterionTrigger.Listener<BreedPokemobTrigger.Instance> listener)
        {
            this.listeners.add(listener);
        }

        public boolean isEmpty()
        {
            return this.listeners.isEmpty();
        }

        public void remove(final ICriterionTrigger.Listener<BreedPokemobTrigger.Instance> listener)
        {
            this.listeners.remove(listener);
        }

        public void trigger(final ServerPlayerEntity player, final IPokemob first, final IPokemob second)
        {
            List<ICriterionTrigger.Listener<BreedPokemobTrigger.Instance>> list = null;

            for (final ICriterionTrigger.Listener<BreedPokemobTrigger.Instance> listener : this.listeners)
                if (listener.getTriggerInstance().test(player, first, second))
                {
                    if (list == null)
                        list = Lists.<ICriterionTrigger.Listener<BreedPokemobTrigger.Instance>> newArrayList();

                    list.add(listener);
                }
            if (list != null) for (final ICriterionTrigger.Listener<BreedPokemobTrigger.Instance> listener1 : list)
                listener1.run(this.playerAdvancements);
        }
    }

    public static ResourceLocation ID = new ResourceLocation(PokecubeMod.ID, "breed");

    private final Map<PlayerAdvancements, BreedPokemobTrigger.Listeners> listeners = Maps.<PlayerAdvancements, BreedPokemobTrigger.Listeners> newHashMap();

    public BreedPokemobTrigger()
    {
    }

    @Override
    public void addPlayerListener(final PlayerAdvancements playerAdvancementsIn,
            final ICriterionTrigger.Listener<BreedPokemobTrigger.Instance> listener)
    {
        BreedPokemobTrigger.Listeners bredanimalstrigger$listeners = this.listeners.get(playerAdvancementsIn);

        if (bredanimalstrigger$listeners == null)
        {
            bredanimalstrigger$listeners = new BreedPokemobTrigger.Listeners(playerAdvancementsIn);
            this.listeners.put(playerAdvancementsIn, bredanimalstrigger$listeners);
        }

        bredanimalstrigger$listeners.add(listener);
    }

    @Override
    public ResourceLocation getId()
    {
        return BreedPokemobTrigger.ID;
    }

    @Override
    public void removePlayerListeners(final PlayerAdvancements playerAdvancementsIn)
    {
        this.listeners.remove(playerAdvancementsIn);
    }

    @Override
    public void removePlayerListener(final PlayerAdvancements playerAdvancementsIn,
            final ICriterionTrigger.Listener<BreedPokemobTrigger.Instance> listener)
    {
        final BreedPokemobTrigger.Listeners bredanimalstrigger$listeners = this.listeners.get(playerAdvancementsIn);

        if (bredanimalstrigger$listeners != null)
        {
            bredanimalstrigger$listeners.remove(listener);

            if (bredanimalstrigger$listeners.isEmpty()) this.listeners.remove(playerAdvancementsIn);
        }
    }

    public void trigger(final ServerPlayerEntity player, final IPokemob first, final IPokemob second)
    {
        final BreedPokemobTrigger.Listeners bredanimalstrigger$listeners = this.listeners.get(player.getAdvancements());
        if (bredanimalstrigger$listeners != null) bredanimalstrigger$listeners.trigger(player, first, second);
    }

    @Override
    public Instance createInstance(final JsonObject json, final ConditionArrayParser conditions)
    {
        final EntityPredicate.AndPredicate pred = EntityPredicate.AndPredicate.fromJson(json, "player", conditions);
        final String mate1 = json.has("mate1") ? json.get("mate1").getAsString() : "";
        final String mate2 = json.has("mate2") ? json.get("mate2").getAsString() : "";
        return new BreedPokemobTrigger.Instance(pred, Database.getEntry(mate1), Database.getEntry(mate2));
    }
}
