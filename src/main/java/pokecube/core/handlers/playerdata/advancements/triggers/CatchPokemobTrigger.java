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
import pokecube.core.database.stats.CaptureStats;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.PokecubeMod;

public class CatchPokemobTrigger implements ICriterionTrigger<CatchPokemobTrigger.Instance>
{
    public static class Instance extends CriterionInstance
    {
        final PokedexEntry entry;
        boolean            lenient = false;
        int                number  = -1;
        int                sign    = 0;

        public Instance(PokedexEntry entry, boolean lenient, int number, int sign)
        {
            super(CatchPokemobTrigger.ID);
            this.entry = entry != null ? entry : Database.missingno;
            this.lenient = lenient;
            this.number = number;
            this.sign = sign;
        }

        public boolean test(ServerPlayerEntity player, IPokemob pokemob)
        {
            PokedexEntry entry = this.entry;
            PokedexEntry testEntry = pokemob.getPokedexEntry();
            boolean numCheck = true;
            if (this.lenient)
            {
                entry = entry.base ? entry : entry.getBaseForme();
                testEntry = testEntry.base ? testEntry : testEntry.getBaseForme();
            }
            if (this.number != -1)
            {
                int num = -1;
                if (entry == Database.missingno) num = CaptureStats.getNumberUniqueCaughtBy(player.getUniqueID());
                else num = CaptureStats.getTotalNumberOfPokemobCaughtBy(player.getUniqueID(), entry);
                if (num == -1) return false;
                numCheck = num * this.sign > this.number;
            }
            return numCheck && (entry == Database.missingno || testEntry == entry) && pokemob
                    .getOwner() == player;
        }

    }

    static class Listeners
    {
        private final PlayerAdvancements                                            playerAdvancements;
        private final Set<ICriterionTrigger.Listener<CatchPokemobTrigger.Instance>> listeners = Sets.<ICriterionTrigger.Listener<CatchPokemobTrigger.Instance>> newHashSet();

        public Listeners(PlayerAdvancements playerAdvancementsIn)
        {
            this.playerAdvancements = playerAdvancementsIn;
        }

        public void add(ICriterionTrigger.Listener<CatchPokemobTrigger.Instance> listener)
        {
            this.listeners.add(listener);
        }

        public boolean isEmpty()
        {
            return this.listeners.isEmpty();
        }

        public void remove(ICriterionTrigger.Listener<CatchPokemobTrigger.Instance> listener)
        {
            this.listeners.remove(listener);
        }

        public void trigger(ServerPlayerEntity player, IPokemob pokemob)
        {
            List<ICriterionTrigger.Listener<CatchPokemobTrigger.Instance>> list = null;

            for (final ICriterionTrigger.Listener<CatchPokemobTrigger.Instance> listener : this.listeners)
                if (listener.getCriterionInstance().test(player, pokemob))
                {
                    if (list == null)
                        list = Lists.<ICriterionTrigger.Listener<CatchPokemobTrigger.Instance>> newArrayList();

                    list.add(listener);
                }
            if (list != null) for (final ICriterionTrigger.Listener<CatchPokemobTrigger.Instance> listener1 : list)
                listener1.grantCriterion(this.playerAdvancements);
        }
    }

    public static ResourceLocation ID = new ResourceLocation(PokecubeMod.ID, "catch");

    private final Map<PlayerAdvancements, CatchPokemobTrigger.Listeners> listeners = Maps.<PlayerAdvancements, CatchPokemobTrigger.Listeners> newHashMap();

    public CatchPokemobTrigger()
    {
    }

    @Override
    public void addListener(PlayerAdvancements playerAdvancementsIn,
            ICriterionTrigger.Listener<CatchPokemobTrigger.Instance> listener)
    {
        CatchPokemobTrigger.Listeners bredanimalstrigger$listeners = this.listeners.get(playerAdvancementsIn);

        if (bredanimalstrigger$listeners == null)
        {
            bredanimalstrigger$listeners = new CatchPokemobTrigger.Listeners(playerAdvancementsIn);
            this.listeners.put(playerAdvancementsIn, bredanimalstrigger$listeners);
        }

        bredanimalstrigger$listeners.add(listener);
    }

    /**
     * Deserialize a ICriterionInstance of this trigger from the data in the
     * JSON.
     */
    @Override
    public CatchPokemobTrigger.Instance deserializeInstance(JsonObject json, JsonDeserializationContext context)
    {
        final String name = json.has("entry") ? json.get("entry").getAsString() : "";
        final int number = json.has("number") ? json.get("number").getAsInt() : -1;
        final int sign = json.has("sign") ? json.get("sign").getAsInt() : 0;
        final boolean lenient = json.has("lenient") ? json.get("lenient").getAsBoolean() : false;
        return new CatchPokemobTrigger.Instance(Database.getEntry(name), lenient, number, sign);
    }

    @Override
    public ResourceLocation getId()
    {
        return CatchPokemobTrigger.ID;
    }

    @Override
    public void removeAllListeners(PlayerAdvancements playerAdvancementsIn)
    {
        this.listeners.remove(playerAdvancementsIn);
    }

    @Override
    public void removeListener(PlayerAdvancements playerAdvancementsIn,
            ICriterionTrigger.Listener<CatchPokemobTrigger.Instance> listener)
    {
        final CatchPokemobTrigger.Listeners bredanimalstrigger$listeners = this.listeners.get(playerAdvancementsIn);

        if (bredanimalstrigger$listeners != null)
        {
            bredanimalstrigger$listeners.remove(listener);

            if (bredanimalstrigger$listeners.isEmpty()) this.listeners.remove(playerAdvancementsIn);
        }
    }

    public void trigger(ServerPlayerEntity player, IPokemob pokemob)
    {
        final CatchPokemobTrigger.Listeners bredanimalstrigger$listeners = this.listeners.get(player.getAdvancements());
        if (bredanimalstrigger$listeners != null) bredanimalstrigger$listeners.trigger(player, pokemob);
    }
}
