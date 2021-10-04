package pokecube.adventures.advancements.triggers;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Lists;
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
import pokecube.adventures.PokecubeAdv;
import pokecube.adventures.entity.trainer.LeaderNpc;
import pokecube.adventures.entity.trainer.TrainerBase;

public class BeatTrainerTrigger implements CriterionTrigger<BeatTrainerTrigger.Instance>
{
    public static ResourceLocation ID = new ResourceLocation(PokecubeAdv.MODID, "beat_trainer");

    public static class Instance extends AbstractCriterionTriggerInstance
    {
        public Instance(final Composite pred)
        {
            super(BeatTrainerTrigger.ID, pred);
        }

        public boolean test(final ServerPlayer player, final TrainerBase defeated)
        {
            return !(defeated instanceof LeaderNpc);
        }

    }

    static class Listeners
    {
        private final PlayerAdvancements                                           playerAdvancements;
        private final Set<CriterionTrigger.Listener<BeatTrainerTrigger.Instance>> listeners = Sets.<CriterionTrigger.Listener<BeatTrainerTrigger.Instance>> newHashSet();

        public Listeners(final PlayerAdvancements playerAdvancementsIn)
        {
            this.playerAdvancements = playerAdvancementsIn;
        }

        public boolean isEmpty()
        {
            return this.listeners.isEmpty();
        }

        public void add(final CriterionTrigger.Listener<BeatTrainerTrigger.Instance> listener)
        {
            this.listeners.add(listener);
        }

        public void remove(final CriterionTrigger.Listener<BeatTrainerTrigger.Instance> listener)
        {
            this.listeners.remove(listener);
        }

        public void trigger(final ServerPlayer player, final TrainerBase defeated)
        {
            List<CriterionTrigger.Listener<BeatTrainerTrigger.Instance>> list = null;

            for (final CriterionTrigger.Listener<BeatTrainerTrigger.Instance> listener : this.listeners)
                if (listener.getTriggerInstance().test(player, defeated))
                {
                    if (list == null)
                        list = Lists.<CriterionTrigger.Listener<BeatTrainerTrigger.Instance>> newArrayList();

                    list.add(listener);
                }
            if (list != null) for (final CriterionTrigger.Listener<BeatTrainerTrigger.Instance> listener1 : list)
                listener1.run(this.playerAdvancements);
        }
    }

    private final Map<PlayerAdvancements, BeatTrainerTrigger.Listeners> listeners = Maps.<PlayerAdvancements, BeatTrainerTrigger.Listeners> newHashMap();

    public BeatTrainerTrigger()
    {
    }

    @Override
    public ResourceLocation getId()
    {
        return BeatTrainerTrigger.ID;
    }

    @Override
    public void addPlayerListener(final PlayerAdvancements playerAdvancementsIn,
            final CriterionTrigger.Listener<BeatTrainerTrigger.Instance> listener)
    {
        BeatTrainerTrigger.Listeners bredanimalstrigger$listeners = this.listeners.get(playerAdvancementsIn);

        if (bredanimalstrigger$listeners == null)
        {
            bredanimalstrigger$listeners = new BeatTrainerTrigger.Listeners(playerAdvancementsIn);
            this.listeners.put(playerAdvancementsIn, bredanimalstrigger$listeners);
        }

        bredanimalstrigger$listeners.add(listener);
    }

    @Override
    public void removePlayerListener(final PlayerAdvancements playerAdvancementsIn,
            final CriterionTrigger.Listener<BeatTrainerTrigger.Instance> listener)
    {
        final BeatTrainerTrigger.Listeners bredanimalstrigger$listeners = this.listeners.get(playerAdvancementsIn);

        if (bredanimalstrigger$listeners != null)
        {
            bredanimalstrigger$listeners.remove(listener);

            if (bredanimalstrigger$listeners.isEmpty()) this.listeners.remove(playerAdvancementsIn);
        }
    }

    @Override
    public void removePlayerListeners(final PlayerAdvancements playerAdvancementsIn)
    {
        this.listeners.remove(playerAdvancementsIn);
    }

    @Override
    public Instance createInstance(final JsonObject json, final DeserializationContext conditions)
    {
        final EntityPredicate.Composite pred = EntityPredicate.Composite.fromJson(json, "player", conditions);
        return new BeatTrainerTrigger.Instance(pred);
    }

    public void trigger(final ServerPlayer player, final TrainerBase defeated)
    {
        final BeatTrainerTrigger.Listeners bredanimalstrigger$listeners = this.listeners.get(player.getAdvancements());
        if (bredanimalstrigger$listeners != null) bredanimalstrigger$listeners.trigger(player, defeated);
    }
}