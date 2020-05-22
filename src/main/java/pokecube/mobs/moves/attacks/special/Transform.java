/**
 *
 */
package pokecube.mobs.moves.attacks.special;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import pokecube.core.ai.tasks.combat.FindTargetsTask;
import pokecube.core.interfaces.IMoveAnimation;
import pokecube.core.interfaces.IMoveConstants;
import pokecube.core.interfaces.IMoveNames;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.capabilities.CapabilityPokemob;
import pokecube.core.interfaces.pokemob.moves.MovePacket;
import pokecube.core.moves.MovesUtils;
import pokecube.core.moves.templates.Move_Basic;

/** @author Manchou */
public class Transform extends Move_Basic
{

    public static class Animation implements IMoveAnimation
    {
        @Override
        public void clientAnimation(final MovePacketInfo info, final float partialTick)
        {
        }

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
        public void reallyInitRGBA()
        {
        }

        @Override
        public void setDuration(final int arg0)
        {
        }

        @Override
        public void spawnClientEntities(final MovePacketInfo info)
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
        final IPokemob attackedMob = CapabilityPokemob.getPokemobFor(attacked);
        if (attacked instanceof LivingEntity)
        {
            FindTargetsTask.initiateCombat(attacker.getEntity(), (LivingEntity) attacked);
            attacker.setTransformedTo(attacked);
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
