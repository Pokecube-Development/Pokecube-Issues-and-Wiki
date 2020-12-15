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
import net.minecraft.advancements.criterion.EntityPredicate.AndPredicate;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.loot.ConditionArrayParser;
import net.minecraft.util.ResourceLocation;
import pokecube.core.interfaces.PokecubeMod;

public class FirstPokemobTrigger implements ICriterionTrigger<FirstPokemobTrigger.Instance>
{
    public static class Instance extends CriterionInstance
    {
        public Instance(final AndPredicate pred)
        {
            super(FirstPokemobTrigger.ID, pred);
        }

        public boolean test()
        {
            return true;
        }

    }

    static class Listeners
    {
        private final PlayerAdvancements                                            playerAdvancements;
        private final Set<ICriterionTrigger.Listener<FirstPokemobTrigger.Instance>> listeners = Sets.<ICriterionTrigger.Listener<FirstPokemobTrigger.Instance>> newHashSet();

        public Listeners(final PlayerAdvancements playerAdvancementsIn)
        {
            this.playerAdvancements = playerAdvancementsIn;
        }

        public void add(final ICriterionTrigger.Listener<FirstPokemobTrigger.Instance> listener)
        {
            this.listeners.add(listener);
        }

        public boolean isEmpty()
        {
            return this.listeners.isEmpty();
        }

        public void remove(final ICriterionTrigger.Listener<FirstPokemobTrigger.Instance> listener)
        {
            this.listeners.remove(listener);
        }

        public void trigger(final ServerPlayerEntity player)
        {
            List<ICriterionTrigger.Listener<FirstPokemobTrigger.Instance>> list = null;

            for (final ICriterionTrigger.Listener<FirstPokemobTrigger.Instance> listener : this.listeners)
                if (listener.getCriterionInstance().test())
                {
                    if (list == null)
                        list = Lists.<ICriterionTrigger.Listener<FirstPokemobTrigger.Instance>> newArrayList();

                    list.add(listener);
                }
            if (list != null) for (final ICriterionTrigger.Listener<FirstPokemobTrigger.Instance> listener1 : list)
                listener1.grantCriterion(this.playerAdvancements);
        }
    }

    public static ResourceLocation ID = new ResourceLocation(PokecubeMod.ID, "get_first_pokemob");

    private final Map<PlayerAdvancements, FirstPokemobTrigger.Listeners> listeners = Maps.<PlayerAdvancements, FirstPokemobTrigger.Listeners> newHashMap();

    public FirstPokemobTrigger()
    {
    }

    @Override
    public void addListener(final PlayerAdvancements playerAdvancementsIn,
            final ICriterionTrigger.Listener<FirstPokemobTrigger.Instance> listener)
    {
        FirstPokemobTrigger.Listeners bredanimalstrigger$listeners = this.listeners.get(playerAdvancementsIn);

        if (bredanimalstrigger$listeners == null)
        {
            bredanimalstrigger$listeners = new FirstPokemobTrigger.Listeners(playerAdvancementsIn);
            this.listeners.put(playerAdvancementsIn, bredanimalstrigger$listeners);
        }

        bredanimalstrigger$listeners.add(listener);
    }


    @Override
    public Instance deserialize(final JsonObject json, final ConditionArrayParser conditions)
    {
        final EntityPredicate.AndPredicate pred = EntityPredicate.AndPredicate.deserializeJSONObject(json, "player", conditions);
        return new FirstPokemobTrigger.Instance(pred);
    }

    @Override
    public ResourceLocation getId()
    {
        return FirstPokemobTrigger.ID;
    }

    @Override
    public void removeAllListeners(final PlayerAdvancements playerAdvancementsIn)
    {
        this.listeners.remove(playerAdvancementsIn);
    }

    @Override
    public void removeListener(final PlayerAdvancements playerAdvancementsIn,
            final ICriterionTrigger.Listener<FirstPokemobTrigger.Instance> listener)
    {
        final FirstPokemobTrigger.Listeners bredanimalstrigger$listeners = this.listeners.get(playerAdvancementsIn);

        if (bredanimalstrigger$listeners != null)
        {
            bredanimalstrigger$listeners.remove(listener);

            if (bredanimalstrigger$listeners.isEmpty()) this.listeners.remove(playerAdvancementsIn);
        }
    }

    public void trigger(final ServerPlayerEntity player)
    {
        final FirstPokemobTrigger.Listeners bredanimalstrigger$listeners = this.listeners.get(player.getAdvancements());
        if (bredanimalstrigger$listeners != null) bredanimalstrigger$listeners.trigger(player);
    }
}
