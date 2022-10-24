package pokecube.core.handlers.playerdata.advancements.triggers;

import com.google.gson.JsonObject;

import net.minecraft.advancements.critereon.AbstractCriterionTriggerInstance;
import net.minecraft.advancements.critereon.DeserializationContext;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.advancements.critereon.EntityPredicate.Composite;
import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import pokecube.api.moves.MoveEntry;
import pokecube.api.utils.PokeType;
import pokecube.core.impl.PokecubeMod;
import pokecube.core.moves.MovesUtils;

public class UseMoveTrigger extends SimpleCriterionTrigger<UseMoveTrigger.Instance>
{
    public static class Instance extends AbstractCriterionTriggerInstance
    {
        String attack;
        PokeType type;
        int power;
        float damage;

        public Instance(final Composite pred, final MoveEntry move)
        {
            super(UseMoveTrigger.ID, pred);
            this.attack = move.getName();
            this.type = move.type;
            this.power = move.power;
        }

        public boolean test(final ServerPlayer player, final MoveEntry packet)
        {
            return packet.getName().equals(this.attack);
        }
    }

    public static ResourceLocation ID = new ResourceLocation(PokecubeMod.ID, "use_move");

    public UseMoveTrigger()
    {}

    @Override
    public ResourceLocation getId()
    {
        return UseMoveTrigger.ID;
    }

    public void trigger(final ServerPlayer player, final MoveEntry packet)
    {
        this.trigger(player, (instance) -> {
            return instance.test(player, packet);
        });
    }

    @Override
    protected Instance createInstance(JsonObject json, Composite composite, DeserializationContext conditions)
    {
        final EntityPredicate.Composite pred = EntityPredicate.Composite.fromJson(json, "player", conditions);
        final String attack = json.get("move").getAsString();
        MoveEntry move = MovesUtils.getMove(attack);
        return new Instance(pred, move);
    }
}
