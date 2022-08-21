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
import net.minecraft.advancements.critereon.EntityPredicate.Composite;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.PlayerAdvancements;
import net.minecraft.server.level.ServerPlayer;
import pokecube.api.data.PokedexEntry;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.core.database.Database;
import pokecube.core.impl.PokecubeMod;

public class EvolvePokemobTrigger implements CriterionTrigger<EvolvePokemobTrigger.Instance>
{
    public static class Instance extends AbstractCriterionTriggerInstance
    {
        final PokedexEntry entry;

        public Instance(final Composite pred, final PokedexEntry entry)
        {
            super(EvolvePokemobTrigger.ID, pred);
            this.entry = entry != null ? entry : Database.missingno;
        }

        public boolean test(final ServerPlayer player, final IPokemob pokemob)
        {
            return (this.entry == Database.missingno || pokemob.getPokedexEntry() == this.entry)
                    && pokemob.getOwner() != player;
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

        public void trigger(final ServerPlayer player, final IPokemob pokemob)
        {
            List<Listener<Instance>> toTrigger = new ArrayList<>();
            for (var listener : this.listeners)
                if (listener.getTriggerInstance().test(player, pokemob)) toTrigger.add(listener);
            toTrigger.forEach(l -> l.run(playerAdvancements));
        }
    }

    public static ResourceLocation ID = new ResourceLocation(PokecubeMod.ID, "evolve");

    private final Map<PlayerAdvancements, Listeners> listeners = Maps.newHashMap();

    public EvolvePokemobTrigger()
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
    public Instance createInstance(final JsonObject json, final DeserializationContext conditions)
    {
        final EntityPredicate.Composite pred = EntityPredicate.Composite.fromJson(json, "player", conditions);
        final String name = json.has("entry") ? json.get("entry").getAsString() : "";
        return new Instance(pred, Database.getEntry(name));
    }

    @Override
    public ResourceLocation getId()
    {
        return EvolvePokemobTrigger.ID;
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

    public void trigger(final ServerPlayer player, final IPokemob pokemob)
    {
        final Listeners listeners = this.listeners.get(player.getAdvancements());
        if (listeners != null) listeners.trigger(player, pokemob);
    }
}
