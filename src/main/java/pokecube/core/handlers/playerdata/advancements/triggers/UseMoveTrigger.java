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
import pokecube.api.entity.pokemob.moves.MovePacket;
import pokecube.api.utils.PokeType;
import pokecube.core.impl.PokecubeMod;
import pokecube.core.moves.MovesUtils;

public class UseMoveTrigger implements CriterionTrigger<UseMoveTrigger.Instance>
{
    public static class Instance extends AbstractCriterionTriggerInstance
    {
        String attack;
        PokeType type;
        int power;
        float damage;

        public Instance(final Composite pred, final MovePacket move)
        {
            super(UseMoveTrigger.ID, pred);
            this.attack = move.attack;
            this.type = move.attackType;
            this.power = move.PWR;
        }

        public boolean test(final ServerPlayer player, final MovePacket packet)
        {
            return packet.attack.equals(this.attack);
        }
    }

    static class Listeners
    {
        private final PlayerAdvancements playerAdvancements;
        private final Set<CriterionTrigger.Listener<Instance>> listeners = Sets.newHashSet();

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

        public void trigger(final ServerPlayer player, final MovePacket packet)
        {
            List<Listener<Instance>> toTrigger = new ArrayList<>();
            for (var listener : this.listeners)
                if (listener.getTriggerInstance().test(player, packet)) toTrigger.add(listener);
            toTrigger.forEach(l -> l.run(playerAdvancements));
        }
    }

    public static ResourceLocation ID = new ResourceLocation(PokecubeMod.ID, "use_move");

    private final Map<PlayerAdvancements, Listeners> listeners = Maps.newHashMap();

    public UseMoveTrigger()
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
        final String attack = json.get("move").getAsString();
        // TODO get this done better.
        final MovePacket packet = new MovePacket(null, null, MovesUtils.getMoveFromName(attack));
        return new Instance(pred, packet);
    }

    @Override
    public ResourceLocation getId()
    {
        return UseMoveTrigger.ID;
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

    public void trigger(final ServerPlayer player, final MovePacket packet)
    {
        final Listeners listeners = this.listeners.get(player.getAdvancements());
        if (listeners != null) listeners.trigger(player, packet);
    }
}
