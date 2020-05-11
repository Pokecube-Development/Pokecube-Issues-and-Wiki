package pokecube.mobs.moves.attacks.ongoing;

import pokecube.core.interfaces.entity.IOngoingAffected;
import pokecube.core.interfaces.entity.IOngoingAffected.IOngoingEffect;
import pokecube.core.moves.templates.Move_Ongoing;

public class Perishsong extends Move_Ongoing
{

    public Perishsong()
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
