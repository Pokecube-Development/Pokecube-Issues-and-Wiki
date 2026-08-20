package pokecube.gimmicks.mega.abilities;

import pokecube.api.data.abilities.Ability;
import pokecube.api.data.abilities.AbilityProvider;
import pokecube.api.entity.pokemob.IHasCommands;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.entity.pokemob.PokemobCaps;
import pokecube.api.entity.pokemob.commandhandlers.ChangeFormHandler;
import pokecube.api.moves.utils.MoveApplication;
import pokecube.gimmicks.mega.MegaEvolveHelper;

@AbilityProvider(name = "battle-bond")
public class BattleBond extends Ability
{
    @Override
    public void postMoveUse(final IPokemob mob, final MoveApplication move)
    {
        if (!areWeTarget(mob, move)) return;
        final IPokemob targetMob = PokemobCaps.getPokemobFor(move.getTarget());
        if (targetMob == null) return;
        if (!targetMob.inCombat() && !MegaEvolveHelper.isMega(mob))
        {
            mob.handleCommand(IHasCommands.Command.CHANGEFORM, new ChangeFormHandler());
        }
    }

    @Override
    public int beforeDamage(final IPokemob mob, final MoveApplication move, final int damage)
    {
        return damage;
    }
}
