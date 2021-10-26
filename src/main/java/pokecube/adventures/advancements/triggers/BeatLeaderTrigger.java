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

public class BeatLeaderTrigger implements CriterionTrigger<BeatLeaderTrigger.Instance>
{
    public static ResourceLocation ID = new ResourceLocation(PokecubeAdv.MODID, "beat_leader");

    public static class Instance extends AbstractCriterionTriggerInstance
    {
        public Instance(final Composite pred)
        {
            super(BeatLeaderTrigger.ID, pred);
        }

        public boolean test(final ServerPlayer player, final TrainerBase defeated)
        {
            return defeated instanceof LeaderNpc;
        }

    }

    static class Listeners
    {
        private final PlayerAdvancements                                          playerAdvancements;
        private final Set<CriterionTrigger.Listener<BeatLeaderTrigger.Instance>> listeners = Sets.<CriterionTrigger.Listener<BeatLeaderTrigger.Instance>> newHashSet();

        public Listeners(final PlayerAdvancements playerAdvancementsIn)
        {
            this.playerAdvancements = playerAdvancementsIn;
        }

        public boolean isEmpty()
        {
            return this.listeners.isEmpty();
        }

        public void add(final CriterionTrigger.Listener<BeatLeaderTrigger.Instance> listener)
        {
            this.listeners.add(listener);
        }

        public void remove(final CriterionTrigger.Listener<BeatLeaderTrigger.Instance> listener)
        {
            this.listeners.remove(listener);
        }

        public void trigger(final ServerPlayer player, final TrainerBase defeated)
        {
            List<CriterionTrigger.Listener<BeatLeaderTrigger.Instance>> list = null;

            for (final CriterionTrigger.Listener<BeatLeaderTrigger.Instance> listener : this.listeners)
                if (listener.getTriggerInstance().test(player, defeated))
                {
                    if (list == null)
                        list = Lists.<CriterionTrigger.Listener<BeatLeaderTrigger.Instance>> newArrayList();

                    list.add(listener);
                }
            if (list != null) for (final CriterionTrigger.Listener<BeatLeaderTrigger.Instance> listener1 : list)
                listener1.run(this.playerAdvancements);
        }
    }

    private final Map<PlayerAdvancements, BeatLeaderTrigger.Listeners> listeners = Maps.<PlayerAdvancements, BeatLeaderTrigger.Listeners> newHashMap();

    public BeatLeaderTrigger()
    {
    }

    @Override
    public ResourceLocation getId()
    {
        return BeatLeaderTrigger.ID;
    }

    @Override
    public void addPlayerListener(final PlayerAdvancements playerAdvancementsIn,
            final CriterionTrigger.Listener<BeatLeaderTrigger.Instance> listener)
    {
        BeatLeaderTrigger.Listeners bredanimalstrigger$listeners = this.listeners.get(playerAdvancementsIn);

        if (bredanimalstrigger$listeners == null)
        {
            bredanimalstrigger$listeners = new BeatLeaderTrigger.Listeners(playerAdvancementsIn);
            this.listeners.put(playerAdvancementsIn, bredanimalstrigger$listeners);
        }

        bredanimalstrigger$listeners.add(listener);
    }

    @Override
    public void removePlayerListener(final PlayerAdvancements playerAdvancementsIn,
            final CriterionTrigger.Listener<BeatLeaderTrigger.Instance> listener)
    {
        final BeatLeaderTrigger.Listeners bredanimalstrigger$listeners = this.listeners.get(playerAdvancementsIn);

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
        return new BeatLeaderTrigger.Instance(pred);
    }

    public void trigger(final ServerPlayer player, final TrainerBase defeated)
    {
        final BeatLeaderTrigger.Listeners bredanimalstrigger$listeners = this.listeners.get(player.getAdvancements());
        if (bredanimalstrigger$listeners != null) bredanimalstrigger$listeners.trigger(player, defeated);
    }
}