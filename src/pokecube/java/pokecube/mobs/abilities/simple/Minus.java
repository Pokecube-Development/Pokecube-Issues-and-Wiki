package pokecube.mobs.abilities.simple;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import pokecube.api.data.abilities.Ability;
import pokecube.api.data.abilities.AbilityManager;
import pokecube.api.data.abilities.AbilityProvider;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.entity.pokemob.PokemobCaps;
import pokecube.api.moves.utils.MoveApplication;
import pokecube.core.moves.damage.attributes.PokecubeAttributes;

@AbilityProvider(name = "minus")
public class Minus extends Ability
{
    @Override
    public void preMoveUse(final IPokemob mob, final MoveApplication move)
    {
        if (!areWeUser(mob, move)) return;
        boolean plusAlly = false;

        for (LivingEntity combatant : mob.getBattle().getAllies(mob.getEntity()))
        {
            IPokemob pokemob = PokemobCaps.getPokemobFor(combatant);
            if (pokemob == null) continue;

            if (pokemob.getAbility().equals(AbilityManager.getAbility("plus")))
                plusAlly = true;
        }

        if (plusAlly)
        {
            var attr = mob.getEntity().getAttribute(PokecubeAttributes.SPATTACK);
            attr.addOrReplacePermanentModifier(new AttributeModifier(PokecubeAttributes.ABILITY_STAT_MOD, 0.5, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
        }
    }

    @Override
    public void endCombat(IPokemob mob)
    {
        var attr = mob.getEntity().getAttribute(PokecubeAttributes.SPATTACK);
        attr.removeModifier(PokecubeAttributes.ABILITY_STAT_MOD);
    }

    @Override
    public void onRecall(IPokemob mob)
    {
        var attr = mob.getEntity().getAttribute(PokecubeAttributes.SPATTACK);
        attr.removeModifier(PokecubeAttributes.ABILITY_STAT_MOD);
    }
}