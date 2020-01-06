package pokecube.core.moves.implementations.attacks.ongoing;

import pokecube.core.interfaces.entity.IOngoingAffected;
import pokecube.core.interfaces.entity.IOngoingAffected.IOngoingEffect;
import pokecube.core.moves.templates.Move_Ongoing;

public class MovePerishsong extends Move_Ongoing
{

    public MovePerishsong()
    {
        super("perishsong");
    }

    @Override
    public void doOngoingEffect(IOngoingAffected mob, IOngoingEffect effect)
    {
        if (effect.getDuration() == 0) this.damageTarget(mob.getEntity(), this.getOngoingDamage(mob.getEntity())
                .setDamageIsAbsolute().setDamageBypassesArmor(), Integer.MAX_VALUE);
        else
        {
            // TODO perish counter here.
        }
    }

    @Override
    public int getDuration()
    {
        return 3;
    }

    @Override
    public boolean onSource()
    {
        return true;
    }

}
