package pokecube.mobs.abilities.simple;

import net.minecraft.world.entity.LivingEntity;
import pokecube.api.data.abilities.Ability;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.entity.pokemob.IPokemob.Stats;
import pokecube.api.entity.pokemob.PokemobCaps;
import pokecube.api.entity.pokemob.moves.MovePacket;
import pokecube.api.moves.IMoveConstants;
import pokecube.core.moves.MovesUtils;
import pokecube.core.utils.EntityTools;

public class BeastBoost extends Ability
{
    @Override
    public void onMoveUse(final IPokemob mob, final MovePacket move)
    {
        final LivingEntity target = EntityTools.getCoreLiving(move.attacked);
        if (target == null) return;
        final IPokemob targetMob = PokemobCaps.getPokemobFor(target);
        if (targetMob == null) return;
        if (mob == move.attacked) if (!targetMob.inCombat())
        {
            byte boost = IMoveConstants.ATTACK;
            int stat = mob.getStat(Stats.ATTACK, true);
            int tmp;
            if ((tmp = mob.getStat(Stats.SPATTACK, true)) > stat)
            {
                stat = tmp;
                boost = IMoveConstants.SPATACK;
            }
            if ((tmp = mob.getStat(Stats.DEFENSE, true)) > stat)
            {
                stat = tmp;
                boost = IMoveConstants.DEFENSE;
            }
            if ((tmp = mob.getStat(Stats.SPDEFENSE, true)) > stat)
            {
                stat = tmp;
                boost = IMoveConstants.SPDEFENSE;
            }
            if ((tmp = mob.getStat(Stats.VIT, true)) > stat)
            {
                stat = tmp;
                boost = IMoveConstants.VIT;
            }
            MovesUtils.handleStats2(mob, mob.getOwner(), boost, IMoveConstants.RAISE);
        }
    }
}
