package pokecube.mobs.abilities.simple;

import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import pokecube.api.data.abilities.Ability;
import pokecube.api.data.abilities.AbilityProvider;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.moves.utils.MoveApplication;
import pokecube.core.moves.damage.attributes.PokecubeAttributes;

@AbilityProvider(name = "hustle")
public class Hustle extends Ability
{
    @Override
    public void preMoveUse(final IPokemob mob, final MoveApplication move)
    {
        if (!areWeUser(mob, move)) return;
        var attackAttr = mob.getEntity().getAttribute(PokecubeAttributes.ATTACK);
        var accuracyAttr = mob.getEntity().getAttribute(PokecubeAttributes.ACCURACY);
        attackAttr.addOrReplacePermanentModifier(new AttributeModifier(PokecubeAttributes.ABILITY_STAT_MOD, 0.5, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
        accuracyAttr.addOrReplacePermanentModifier(new AttributeModifier(PokecubeAttributes.ABILITY_STAT_MOD, -3277.0/4096.0, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
    }
}
