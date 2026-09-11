package pokecube.mobs.abilities.simple;

import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import pokecube.api.data.abilities.Ability;
import pokecube.api.data.abilities.AbilityProvider;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.moves.utils.MoveApplication;
import pokecube.core.moves.damage.attributes.PokecubeAttributes;

@AbilityProvider(name = "slow-start")
public class SlowStart extends Ability
{
    @Override
    public void startCombat(IPokemob mob)
    {
        mob.getEntity().getPersistentData().putInt("pokecube:slowStartRemaining", 5);
    }

    @Override
    public void preMoveUse(final IPokemob mob, final MoveApplication move)
    {
        if (!areWeUser(mob, move)) return;
        var entity = mob.getEntity();
        var attackAttr = mob.getEntity().getAttribute(PokecubeAttributes.ATTACK);
        var vitAttr = mob.getEntity().getAttribute(PokecubeAttributes.VIT);
        if (entity.getPersistentData().contains("pokecube:slowStartRemaining"))
        {
            if (entity.getPersistentData().getInt("pokecube:slowStartRemaining") <= 0)
            {
                entity.getPersistentData().remove("pokecube:slowStartRemaining");
                attackAttr.removeModifier(PokecubeAttributes.ABILITY_STAT_MOD);
                vitAttr.removeModifier(PokecubeAttributes.ABILITY_STAT_MOD);
            }

            attackAttr.addOrReplacePermanentModifier(new AttributeModifier(PokecubeAttributes.ABILITY_STAT_MOD, -0.5, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
            vitAttr.addOrReplacePermanentModifier(new AttributeModifier(PokecubeAttributes.ABILITY_STAT_MOD, -0.5, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
        }

    }

    @Override
    public void postMoveUse(IPokemob mob, MoveApplication move)
    {
        if (!areWeUser(mob, move)) return;
        var entity = mob.getEntity();
        if (mob.getEntity().getPersistentData().contains("pokecube:slowStartRemaining"))
            entity.getPersistentData().putInt("pokecube:slowStartRemaining", entity.getPersistentData().getInt("pokecube:slowStartRemaining") - 1);
    }
}
