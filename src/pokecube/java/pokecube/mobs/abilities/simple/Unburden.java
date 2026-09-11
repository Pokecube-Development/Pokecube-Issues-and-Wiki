package pokecube.mobs.abilities.simple;


import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import pokecube.api.data.abilities.Ability;
import pokecube.api.data.abilities.AbilityProvider;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.moves.utils.MoveApplication;
import pokecube.core.moves.damage.attributes.PokecubeAttributes;

@AbilityProvider(name = "unburden")
public class Unburden extends Ability
{
    @Override
    public void preMoveUse(final IPokemob mob, final MoveApplication move)
    {
        // TODO: Add places where this tag is set
        if (mob.getEntity().getPersistentData().contains("pokecube:itemUsedOrLost"))
        {
            var attr = mob.getEntity().getAttribute(PokecubeAttributes.VIT);
            attr.addOrReplacePermanentModifier(new AttributeModifier(PokecubeAttributes.ABILITY_STAT_MOD, 1, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
        }
    }
}
