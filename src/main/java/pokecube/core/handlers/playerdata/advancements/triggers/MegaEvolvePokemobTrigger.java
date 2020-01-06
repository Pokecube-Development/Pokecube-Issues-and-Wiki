package pokecube.core.handlers.playerdata.advancements.triggers;

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
import pokecube.core.database.Database;
import pokecube.core.database.PokedexEntry;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.PokecubeMod;

public class MegaEvolvePokemobTrigger implements ICriterionTrigger<MegaEvolvePokemobTrigger.Instance>
{
    public static class Instance extends CriterionInstance
    {
        final PokedexEntry entry;

        public Instance(PokedexEntry entry)
        {
            super(MegaEvolvePokemobTrigger.ID);
            this.entry = entry != null ? entry : Database.missingno;
        }

        public boolean test(ServerPlayerEntity player, IPokemob pokemob)
        {
            return (this.entry == Database.missingno || pokemob.getPokedexEntry() == this.entry) && pokemob
                    .getOwner() != player;
        }

    }

    static class Listeners
    {
        private final PlayerAdvancements                                                 playerAdvancements;
        private final Set<ICriterionTrigger.Listener<MegaEvolvePokemobTrigger.Instance>> listeners = Sets.<ICriterionTrigger.Listener<MegaEvolvePokemobTrigger.Instance>> newHashSet();

        public Listeners(PlayerAdvancements playerAdvancementsIn)
        {
            this.playerAdvancements = playerAdvancementsIn;
        }

        public void add(ICriterionTrigger.Listener<MegaEvolvePokemobTrigger.Instance> listener)
        {
            this.listeners.add(listener);
        }

        public boolean isEmpty()
        {
            return this.listeners.isEmpty();
        }

        public void remove(ICriterionTrigger.Listener<MegaEvolvePokemobTrigger.Instance> listener)
        {
            this.listeners.remove(listener);
        }

        public void trigger(ServerPlayerEntity player, IPokemob pokemob)
        {
            List<ICriterionTrigger.Listener<MegaEvolvePokemobTrigger.Instance>> list = null;

            for (final ICriterionTrigger.Listener<MegaEvolvePokemobTrigger.Instance> listener : this.listeners)
                if (listener.getCriterionInstance().test(player, pokemob))
                {
                    if (list == null)
                        list = Lists.<ICriterionTrigger.Listener<MegaEvolvePokemobTrigger.Instance>> newArrayList();

                    list.add(listener);
                }
            if (list != null) for (final ICriterionTrigger.Listener<MegaEvolvePokemobTrigger.Instance> listener1 : list)
                listener1.grantCriterion(this.playerAdvancements);
        }
    }

    public static ResourceLocation ID = new ResourceLocation(PokecubeMod.ID, "mega_evolve");

    private final Map<PlayerAdvancements, MegaEvolvePokemobTrigger.Listeners> listeners = Maps.<PlayerAdvancements, MegaEvolvePokemobTrigger.Listeners> newHashMap();

    public MegaEvolvePokemobTrigger()
    {
    }

    @Override
    public void addListener(PlayerAdvancements playerAdvancementsIn,
            ICriterionTrigger.Listener<MegaEvolvePokemobTrigger.Instance> listener)
    {
        MegaEvolvePokemobTrigger.Listeners bredanimalstrigger$listeners = this.listeners.get(playerAdvancementsIn);

        if (bredanimalstrigger$listeners == null)
        {
            bredanimalstrigger$listeners = new MegaEvolvePokemobTrigger.Listeners(playerAdvancementsIn);
            this.listeners.put(playerAdvancementsIn, bredanimalstrigger$listeners);
        }

        bredanimalstrigger$listeners.add(listener);
    }

    /**
     * Deserialize a ICriterionInstance of this trigger from the data in the
     * JSON.
     */
    @Override
    public MegaEvolvePokemobTrigger.Instance deserializeInstance(JsonObject json, JsonDeserializationContext context)
    {
        final String name = json.has("entry") ? json.get("entry").getAsString() : "";
        return new MegaEvolvePokemobTrigger.Instance(Database.getEntry(name));
    }

    @Override
    public ResourceLocation getId()
    {
        return MegaEvolvePokemobTrigger.ID;
    }

    @Override
    public void removeAllListeners(PlayerAdvancements playerAdvancementsIn)
    {
        this.listeners.remove(playerAdvancementsIn);
    }

    @Override
    public void removeListener(PlayerAdvancements playerAdvancementsIn,
            ICriterionTrigger.Listener<MegaEvolvePokemobTrigger.Instance> listener)
    {
        final MegaEvolvePokemobTrigger.Listeners bredanimalstrigger$listeners = this.listeners.get(
                playerAdvancementsIn);

        if (bredanimalstrigger$listeners != null)
        {
            bredanimalstrigger$listeners.remove(listener);

            if (bredanimalstrigger$listeners.isEmpty()) this.listeners.remove(playerAdvancementsIn);
        }
    }

    public void trigger(ServerPlayerEntity player, IPokemob pokemob)
    {
        final MegaEvolvePokemobTrigger.Listeners bredanimalstrigger$listeners = this.listeners.get(player
                .getAdvancements());
        if (bredanimalstrigger$listeners != null) bredanimalstrigger$listeners.trigger(player, pokemob);
    }
}
