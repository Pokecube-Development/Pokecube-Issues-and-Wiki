package pokecube.mobs.moves.attacks.ongoing;

import pokecube.core.interfaces.entity.IOngoingAffected;
import pokecube.core.interfaces.entity.IOngoingAffected.IOngoingEffect;
import pokecube.core.moves.templates.Move_Ongoing;

public class Infestation extends Move_Ongoing
{

    public Infestation()
    {
        super("infestation");
    }

    @Override
    public void doOngoingEffect(IOngoingAffected mob, IOngoingEffect effect)
    {
        final float thisMaxHP = mob.getEntity().getMaxHealth();
        final int damage = Math.max(1, (int) (0.125 * thisMaxHP));
        this.damageTarget(mob.getEntity(), null, damage);
    }
}
