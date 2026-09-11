package pokecube.mobs.abilities.simple;

import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import pokecube.api.data.abilities.Ability;
import pokecube.api.data.abilities.AbilityProvider;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.moves.utils.MoveApplication;
import pokecube.core.moves.damage.attributes.PokecubeAttributes;

@AbilityProvider(name = "defeatist")
public class Defeatist extends Ability
{
    @Override
    public void preMoveUse(final IPokemob mob, final MoveApplication move)
    {
        if (!areWeUser(mob, move)) return;
        if (mob.getHealth() <= mob.getMaxHealth() / 2.0)
        {
            var attr = mob.getEntity().getAttribute(PokecubeAttributes.VIT);
            attr.addOrReplacePermanentModifier(new AttributeModifier(PokecubeAttributes.ABILITY_STAT_MOD, -0.5, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
        }
    }

    @Override
    public void endCombat(IPokemob mob)
    {
        var attr = mob.getEntity().getAttribute(PokecubeAttributes.EVASION);
        attr.removeModifier(PokecubeAttributes.ABILITY_STAT_MOD);
    }

    @Override
    public void onRecall(IPokemob mob)
    {
        var attr = mob.getEntity().getAttribute(PokecubeAttributes.EVASION);
        attr.removeModifier(PokecubeAttributes.ABILITY_STAT_MOD);
    }
}
