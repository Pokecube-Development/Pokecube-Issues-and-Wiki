package pokecube.mobs.moves.attacks.ongoing;

import pokecube.core.interfaces.IMoveConstants;
import pokecube.core.interfaces.entity.IOngoingAffected;
import pokecube.core.interfaces.entity.IOngoingAffected.IOngoingEffect;
import pokecube.core.moves.MovesUtils;
import pokecube.core.moves.templates.Move_Ongoing;

public class MoveYawn extends Move_Ongoing
{
    public MoveYawn()
    {
        super("yawn");
    }

    @Override
    public void doOngoingEffect(IOngoingAffected mob, IOngoingEffect effect)
    {
        if (effect.getDuration() == 0) MovesUtils.setStatus(mob.getEntity(), IMoveConstants.STATUS_SLP);
    }

    @Override
    public int getDuration()
    {
        return 2;
    }

}
