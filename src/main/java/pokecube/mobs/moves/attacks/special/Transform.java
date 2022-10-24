/**
 *
 */
package pokecube.mobs.moves.attacks.special;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.entity.pokemob.PokemobCaps;
import pokecube.api.entity.pokemob.moves.MovePacket;
import pokecube.api.moves.utils.IMoveAnimation;
import pokecube.api.moves.utils.IMoveConstants;
import pokecube.core.ai.brain.BrainUtils;
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
        {}

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
        this.setNotInterceptable();
    }

    @Override
    public void attack(final IPokemob attacker, final LivingEntity attacked)
    {
        final IPokemob attackedMob = PokemobCaps.getPokemobFor(attacked);
        if (attacked instanceof LivingEntity)
        {
            BrainUtils.initiateCombat(attacker.getEntity(), attacked);
            attacker.setTransformedTo(attacked);
        }
        if (attackedMob == null)
        {
            final MovePacket packet = new MovePacket(attacker, attacked, this.name, this.move.type, 25, 1,
                    IMoveConstants.STATUS_NON, IMoveConstants.CHANGE_NONE);
            this.onAttack(packet);
            if (attacker.getTransformedTo() != null) attacker.getTransformedTo().swing(InteractionHand.MAIN_HAND);
        }
    }

    @Override
    public byte getAttackCategory(IPokemob user)
    {
        if (user.getTransformedTo() != null) return IMoveConstants.CATEGORY_CONTACT;
        return super.getAttackCategory();
    }
}
