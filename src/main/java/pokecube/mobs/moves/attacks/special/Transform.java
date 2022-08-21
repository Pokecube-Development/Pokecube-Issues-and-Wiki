/**
 *
 */
package pokecube.mobs.moves.attacks.special;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.entity.pokemob.PokemobCaps;
import pokecube.api.entity.pokemob.moves.MovePacket;
import pokecube.api.moves.IMoveAnimation;
import pokecube.api.moves.IMoveConstants;
import pokecube.api.moves.IMoveNames;
import pokecube.core.ai.brain.BrainUtils;
import pokecube.core.moves.MovesUtils;
import pokecube.core.moves.templates.Move_Basic;

/** @author Manchou */
public class Transform extends Move_Basic
{

    public static class Animation implements IMoveAnimation
    {

        @Override
        public int getApplicationTick()
        {
            return 0;
        }

        @Override
        public int getDuration()
        {
            return 0;
        }

        @Override
        public void setDuration(final int arg0)
        {
        }

    }

    /**
     * @param name
     * @param type
     * @param PWR
     * @param PRE
     * @param PP
     * @param attackCategory
     */
    public Transform()
    {
        super("transform");
        this.setAnimation(new Animation());
        this.setSelf();
        this.setNotInterceptable();

    }

    @Override
    public void attack(final IPokemob attacker, final Entity attacked)
    {
        final IPokemob attackedMob = PokemobCaps.getPokemobFor(attacked);
        if (attacked instanceof LivingEntity)
        {
            BrainUtils.initiateCombat(attacker.getEntity(), (LivingEntity) attacked);
            attacker.setTransformedTo((LivingEntity) attacked);
        }
        else if (attackedMob != null)
        {
            final String move = attackedMob.getMove(0);
            if (move != null && !IMoveNames.MOVE_TRANSFORM.equals(move)) MovesUtils.doAttack(move, attacker, attacked);
            else if (MovesUtils.canUseMove(attacker)) MovesUtils.displayEfficiencyMessages(attacker, attacked, 0F, 1F);
        }
        if (attackedMob == null)
        {
            final MovePacket packet = new MovePacket(attacker, attacked, this.name, this.move.type, 25, 1,
                    IMoveConstants.STATUS_NON, IMoveConstants.CHANGE_NONE);
            this.onAttack(packet);
        }
    }
}
