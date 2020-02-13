package pokecube.adventures.advancements.triggers;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;

import net.minecraft.advancements.ICriterionTrigger;
import net.minecraft.advancements.PlayerAdvancements;
import net.minecraft.advancements.criterion.CriterionInstance;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.ResourceLocation;
import pokecube.adventures.PokecubeAdv;
import pokecube.adventures.entity.trainer.LeaderNpc;
import pokecube.adventures.entity.trainer.TrainerBase;

public class BeatTrainerTrigger implements ICriterionTrigger<BeatTrainerTrigger.Instance>
{
    public static ResourceLocation ID = new ResourceLocation(PokecubeAdv.ID, "beat_trainer");

    public static class Instance extends CriterionInstance
    {
        public Instance()
        {
            super(BeatTrainerTrigger.ID);
        }

        public boolean test(final ServerPlayerEntity player, final TrainerBase defeated)
        {
            return !(defeated instanceof LeaderNpc);
        }

    }

    static class Listeners
    {
        private final PlayerAdvancements                                           playerAdvancements;
        private final Set<ICriterionTrigger.Listener<BeatTrainerTrigger.Instance>> listeners = Sets.<ICriterionTrigger.Listener<BeatTrainerTrigger.Instance>> newHashSet();

        public Listeners(final PlayerAdvancements playerAdvancementsIn)
        {
            this.playerAdvancements = playerAdvancementsIn;
        }

        public boolean isEmpty()
        {
            return this.listeners.isEmpty();
        }

        public void add(final ICriterionTrigger.Listener<BeatTrainerTrigger.Instance> listener)
        {
            this.listeners.add(listener);
        }

        public void remove(final ICriterionTrigger.Listener<BeatTrainerTrigger.Instance> listener)
        {
            this.listeners.remove(listener);
        }

        public void trigger(final ServerPlayerEntity player, final TrainerBase defeated)
        {
            List<ICriterionTrigger.Listener<BeatTrainerTrigger.Instance>> list = null;

            for (final ICriterionTrigger.Listener<BeatTrainerTrigger.Instance> listener : this.listeners)
                if (listener.getCriterionInstance().test(player, defeated))
                {
                    if (list == null)
                        list = Lists.<ICriterionTrigger.Listener<BeatTrainerTrigger.Instance>> newArrayList();

                    list.add(listener);
                }
            if (list != null) for (final ICriterionTrigger.Listener<BeatTrainerTrigger.Instance> listener1 : list)
                listener1.grantCriterion(this.playerAdvancements);
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
    public void addListener(final PlayerAdvancements playerAdvancementsIn,
            final ICriterionTrigger.Listener<BeatTrainerTrigger.Instance> listener)
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
    public void removeListener(final PlayerAdvancements playerAdvancementsIn,
            final ICriterionTrigger.Listener<BeatTrainerTrigger.Instance> listener)
    {
        final BeatTrainerTrigger.Listeners bredanimalstrigger$listeners = this.listeners.get(playerAdvancementsIn);

        if (bredanimalstrigger$listeners != null)
        {
            bredanimalstrigger$listeners.remove(listener);

            if (bredanimalstrigger$listeners.isEmpty()) this.listeners.remove(playerAdvancementsIn);
        }
    }

    @Override
    public void removeAllListeners(final PlayerAdvancements playerAdvancementsIn)
    {
        this.listeners.remove(playerAdvancementsIn);
    }

    /**
     * Deserialize a ICriterionInstance of this trigger from the data in the
     * JSON.
     */
    @Override
    public BeatTrainerTrigger.Instance deserializeInstance(final JsonObject json,
            final JsonDeserializationContext context)
    {
        return new BeatTrainerTrigger.Instance();
    }

    public void trigger(final ServerPlayerEntity player, final TrainerBase defeated)
    {
        final BeatTrainerTrigger.Listeners bredanimalstrigger$listeners = this.listeners.get(player.getAdvancements());
        if (bredanimalstrigger$listeners != null) bredanimalstrigger$listeners.trigger(player, defeated);
    }
}