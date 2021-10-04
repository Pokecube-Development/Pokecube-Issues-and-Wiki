package pokecube.mobs.abilities.b;

import net.minecraft.world.entity.LivingEntity;
import pokecube.core.database.abilities.Ability;
import pokecube.core.interfaces.IMoveConstants;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.IPokemob.Stats;
import pokecube.core.interfaces.capabilities.CapabilityPokemob;
import pokecube.core.interfaces.pokemob.moves.MovePacket;
import pokecube.core.moves.MovesUtils;
import pokecube.core.utils.EntityTools;

public class BeastBoost extends Ability
{
    @Override
    public void onMoveUse(final IPokemob mob, final MovePacket move)
    {
        final LivingEntity target = EntityTools.getCoreLiving(move.attacked);
        if (target == null) return;
        final IPokemob targetMob = CapabilityPokemob.getPokemobFor(target);
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
