package pokecube.core.handlers.playerdata.advancements.triggers;

import com.google.gson.JsonObject;

import net.minecraft.advancements.critereon.AbstractCriterionTriggerInstance;
import net.minecraft.advancements.critereon.DeserializationContext;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.advancements.critereon.EntityPredicate.Composite;
import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import pokecube.api.data.PokedexEntry;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.stats.CaptureStats;
import pokecube.api.utils.TagNames;
import pokecube.core.database.Database;
import pokecube.core.impl.PokecubeMod;

public class CatchPokemobTrigger extends SimpleCriterionTrigger<CatchPokemobTrigger.Instance>
{
    public static class Instance extends AbstractCriterionTriggerInstance
    {
        final PokedexEntry entry;
        boolean lenient = false;
        int number = -1;
        int sign = 0;

        public Instance(final EntityPredicate.Composite player, final PokedexEntry entry, final boolean lenient,
                final int number, final int sign)
        {
            super(CatchPokemobTrigger.ID, player);
            this.entry = entry != null ? entry : Database.missingno;
            this.lenient = lenient;
            this.number = number;
            this.sign = sign;
        }

        public boolean test(final ServerPlayer player, final IPokemob pokemob)
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
                if (entry == Database.missingno) num = CaptureStats.getNumberUniqueCaughtBy(player.getUUID());
                else num = CaptureStats.getTotalNumberOfPokemobCaughtBy(player.getUUID(), entry);
                if (num == -1) return false;
                numCheck = num * this.sign > this.number;
            }
            if (pokemob.getEntity().getPersistentData().getBoolean(TagNames.HATCHED)) return false;
            return numCheck && (entry == Database.missingno || testEntry == entry) && pokemob.getOwner() == player;
        }

    }

    public static ResourceLocation ID = new ResourceLocation(PokecubeMod.ID, "catch");

    public CatchPokemobTrigger()
    {}

    @Override
    public ResourceLocation getId()
    {
        return CatchPokemobTrigger.ID;
    }

    public void trigger(final ServerPlayer player, final IPokemob pokemob)
    {
        this.trigger(player, (instance) -> {
            return instance.test(player, pokemob);
        });
    }

    @Override
    protected Instance createInstance(JsonObject json, Composite composite, DeserializationContext conditions)
    {
        final EntityPredicate.Composite pred = EntityPredicate.Composite.fromJson(json, "player", conditions);
        final String name = json.has("entry") ? json.get("entry").getAsString() : "";
        final int number = json.has("number") ? json.get("number").getAsInt() : -1;
        final int sign = json.has("sign") ? json.get("sign").getAsInt() : 0;
        final boolean lenient = json.has("lenient") ? json.get("lenient").getAsBoolean() : false;
        return new CatchPokemobTrigger.Instance(pred, Database.getEntry(name), lenient, number, sign);
    }
}
