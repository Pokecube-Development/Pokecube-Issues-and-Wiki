package pokecube.mobs.abilities.b;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.boss.dragon.EnderDragonPartEntity;
import pokecube.core.database.abilities.Ability;
import pokecube.core.interfaces.IMoveConstants;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.IPokemob.Stats;
import pokecube.core.interfaces.capabilities.CapabilityPokemob;
import pokecube.core.interfaces.pokemob.moves.MovePacket;
import pokecube.core.moves.MovesUtils;

public class BeastBoost extends Ability
{
    @Override
    public void onMoveUse(final IPokemob mob, final MovePacket move)
    {
        LivingEntity target = null;
        if (move.attacked instanceof LivingEntity) target = (LivingEntity) move.attacked;
        // TODO replace with forge multipart entity in 1.16.5
        else if (move.attacked instanceof EnderDragonPartEntity)
            target = ((EnderDragonPartEntity) move.attacked).dragon;
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
