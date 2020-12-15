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
import pokecube.core.interfaces.pokemob.moves.MovePacket;
import pokecube.core.moves.MovesUtils;
import pokecube.core.utils.PokeType;

public class UseMoveTrigger implements ICriterionTrigger<UseMoveTrigger.Instance>
{
    public static class Instance extends CriterionInstance
    {
        String   attack;
        PokeType type;
        int      power;
        float    damage;

        public Instance(final AndPredicate pred, final MovePacket move)
        {
            super(UseMoveTrigger.ID, pred);
            this.attack = move.attack;
            this.type = move.attackType;
            this.power = move.PWR;
        }

        public boolean test(final ServerPlayerEntity player, final MovePacket packet)
        {
            return packet.attack.equals(this.attack);
        }

    }

    static class Listeners
    {
        private final PlayerAdvancements                                       playerAdvancements;
        private final Set<ICriterionTrigger.Listener<UseMoveTrigger.Instance>> listeners = Sets.<ICriterionTrigger.Listener<UseMoveTrigger.Instance>> newHashSet();

        public Listeners(final PlayerAdvancements playerAdvancementsIn)
        {
            this.playerAdvancements = playerAdvancementsIn;
        }

        public void add(final ICriterionTrigger.Listener<UseMoveTrigger.Instance> listener)
        {
            this.listeners.add(listener);
        }

        public boolean isEmpty()
        {
            return this.listeners.isEmpty();
        }

        public void remove(final ICriterionTrigger.Listener<UseMoveTrigger.Instance> listener)
        {
            this.listeners.remove(listener);
        }

        public void trigger(final ServerPlayerEntity player, final MovePacket packet)
        {
            List<ICriterionTrigger.Listener<UseMoveTrigger.Instance>> list = null;

            for (final ICriterionTrigger.Listener<UseMoveTrigger.Instance> listener : this.listeners)
                if (listener.getCriterionInstance().test(player, packet))
                {
                    if (list == null) list = Lists.<ICriterionTrigger.Listener<UseMoveTrigger.Instance>> newArrayList();

                    list.add(listener);
                }
            if (list != null) for (final ICriterionTrigger.Listener<UseMoveTrigger.Instance> listener1 : list)
                listener1.grantCriterion(this.playerAdvancements);
        }
    }

    public static ResourceLocation ID = new ResourceLocation(PokecubeMod.ID, "use_move");

    private final Map<PlayerAdvancements, UseMoveTrigger.Listeners> listeners = Maps.<PlayerAdvancements, UseMoveTrigger.Listeners> newHashMap();

    public UseMoveTrigger()
    {
    }

    @Override
    public void addListener(final PlayerAdvancements playerAdvancementsIn,
            final ICriterionTrigger.Listener<UseMoveTrigger.Instance> listener)
    {
        UseMoveTrigger.Listeners bredanimalstrigger$listeners = this.listeners.get(playerAdvancementsIn);

        if (bredanimalstrigger$listeners == null)
        {
            bredanimalstrigger$listeners = new UseMoveTrigger.Listeners(playerAdvancementsIn);
            this.listeners.put(playerAdvancementsIn, bredanimalstrigger$listeners);
        }

        bredanimalstrigger$listeners.add(listener);
    }


    @Override
    public Instance deserialize(final JsonObject json, final ConditionArrayParser conditions)
    {
        final EntityPredicate.AndPredicate pred = EntityPredicate.AndPredicate.deserializeJSONObject(json, "player", conditions);
        final String attack = json.get("move").getAsString();
        // TODO get this done better.
        final MovePacket packet = new MovePacket(null, null, MovesUtils.getMoveFromName(attack));
        return new UseMoveTrigger.Instance(pred, packet);
    }

    @Override
    public ResourceLocation getId()
    {
        return UseMoveTrigger.ID;
    }

    @Override
    public void removeAllListeners(final PlayerAdvancements playerAdvancementsIn)
    {
        this.listeners.remove(playerAdvancementsIn);
    }

    @Override
    public void removeListener(final PlayerAdvancements playerAdvancementsIn,
            final ICriterionTrigger.Listener<UseMoveTrigger.Instance> listener)
    {
        final UseMoveTrigger.Listeners bredanimalstrigger$listeners = this.listeners.get(playerAdvancementsIn);

        if (bredanimalstrigger$listeners != null)
        {
            bredanimalstrigger$listeners.remove(listener);

            if (bredanimalstrigger$listeners.isEmpty()) this.listeners.remove(playerAdvancementsIn);
        }
    }

    public void trigger(final ServerPlayerEntity player, final MovePacket packet)
    {
        final UseMoveTrigger.Listeners bredanimalstrigger$listeners = this.listeners.get(player.getAdvancements());
        if (bredanimalstrigger$listeners != null) bredanimalstrigger$listeners.trigger(player, packet);
    }
}
