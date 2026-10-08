package pokecube.mobs.abilities.simple;

import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import pokecube.api.data.abilities.Ability;
import pokecube.api.data.abilities.AbilityProvider;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.core.moves.damage.attributes.PokecubeAttributes;

@AbilityProvider(name = "fur-coat")
public class FurCoat extends Ability
{
    @Override
    public void startCombat(IPokemob mob)
    {
        var attr = mob.getEntity().getAttribute(PokecubeAttributes.DEFENSE);
        attr.addOrReplacePermanentModifier(new AttributeModifier(PokecubeAttributes.ABILITY_STAT_MOD, 1, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
    }
}
