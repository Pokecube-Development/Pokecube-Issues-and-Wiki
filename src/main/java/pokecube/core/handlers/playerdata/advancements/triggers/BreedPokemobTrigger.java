package pokecube.core.handlers.playerdata.advancements.triggers;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.gson.JsonObject;

import net.minecraft.advancements.CriterionTrigger;
import net.minecraft.advancements.critereon.AbstractCriterionTriggerInstance;
import net.minecraft.advancements.critereon.DeserializationContext;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.PlayerAdvancements;
import net.minecraft.server.level.ServerPlayer;
import pokecube.api.data.PokedexEntry;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.core.database.Database;
import pokecube.core.impl.PokecubeMod;

public class BreedPokemobTrigger implements CriterionTrigger<BreedPokemobTrigger.Instance>
{
    public static class Instance extends AbstractCriterionTriggerInstance
    {
        final PokedexEntry mate1;
        final PokedexEntry mate2;

        public Instance(final EntityPredicate.Composite player, final PokedexEntry mate1, final PokedexEntry mate2)
        {
            super(BreedPokemobTrigger.ID, player);
            this.mate1 = mate1 != null ? mate1 : Database.missingno;
            this.mate2 = mate2 != null ? mate2 : Database.missingno;
        }

        public boolean test(final ServerPlayer player, final IPokemob first, final IPokemob second)
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
        private final PlayerAdvancements playerAdvancements;
        private final Set<CriterionTrigger.Listener<Instance>> listeners = Sets.<CriterionTrigger.Listener<Instance>>newHashSet();

        public Listeners(final PlayerAdvancements playerAdvancementsIn)
        {
            this.playerAdvancements = playerAdvancementsIn;
        }

        public void add(final CriterionTrigger.Listener<Instance> listener)
        {
            this.listeners.add(listener);
        }

        public boolean isEmpty()
        {
            return this.listeners.isEmpty();
        }

        public void remove(final CriterionTrigger.Listener<Instance> listener)
        {
            this.listeners.remove(listener);
        }

        public void trigger(final ServerPlayer player, final IPokemob first, final IPokemob second)
        {
            List<Listener<Instance>> toTrigger = new ArrayList<>();
            for (var listener : this.listeners)
                if (listener.getTriggerInstance().test(player, first, second)) toTrigger.add(listener);
            toTrigger.forEach(l -> l.run(playerAdvancements));
        }
    }

    public static ResourceLocation ID = new ResourceLocation(PokecubeMod.ID, "breed");

    private final Map<PlayerAdvancements, Listeners> listeners = Maps.newHashMap();

    public BreedPokemobTrigger()
    {}

    @Override
    public void addPlayerListener(final PlayerAdvancements playerAdvancementsIn,
            final CriterionTrigger.Listener<Instance> listener)
    {
        Listeners listeners = this.listeners.get(playerAdvancementsIn);
        if (listeners == null)
        {
            listeners = new Listeners(playerAdvancementsIn);
            this.listeners.put(playerAdvancementsIn, listeners);
        }
        listeners.add(listener);
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
            final CriterionTrigger.Listener<Instance> listener)
    {
        final Listeners listeners = this.listeners.get(playerAdvancementsIn);
        if (listeners != null)
        {
            listeners.remove(listener);
            if (listeners.isEmpty()) this.listeners.remove(playerAdvancementsIn);
        }
    }

    public void trigger(final ServerPlayer player, final IPokemob first, final IPokemob second)
    {
        final Listeners listeners = this.listeners.get(player.getAdvancements());
        if (listeners != null) listeners.trigger(player, first, second);
    }

    @Override
    public Instance createInstance(final JsonObject json, final DeserializationContext conditions)
    {
        final EntityPredicate.Composite pred = EntityPredicate.Composite.fromJson(json, "player", conditions);
        final String mate1 = json.has("mate1") ? json.get("mate1").getAsString() : "";
        final String mate2 = json.has("mate2") ? json.get("mate2").getAsString() : "";
        return new BreedPokemobTrigger.Instance(pred, Database.getEntry(mate1), Database.getEntry(mate2));
    }
}
