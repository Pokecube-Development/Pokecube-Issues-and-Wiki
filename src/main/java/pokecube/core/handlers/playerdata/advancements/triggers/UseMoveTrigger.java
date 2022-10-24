package pokecube.core.handlers.playerdata.advancements.triggers;

import com.google.gson.JsonObject;

import net.minecraft.advancements.critereon.AbstractCriterionTriggerInstance;
import net.minecraft.advancements.critereon.DeserializationContext;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.advancements.critereon.EntityPredicate.Composite;
import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import pokecube.api.entity.pokemob.moves.MovePacket;
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

    public static ResourceLocation ID = new ResourceLocation(PokecubeMod.ID, "use_move");

    public UseMoveTrigger()
    {}

    @Override
    public ResourceLocation getId()
    {
        return UseMoveTrigger.ID;
    }

    public void trigger(final ServerPlayer player, final MovePacket packet)
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
        // TODO get this done better.
        final MovePacket packet = new MovePacket(null, null, MovesUtils.getMoveFromName(attack).move);
        return new Instance(pred, packet);
    }
}
