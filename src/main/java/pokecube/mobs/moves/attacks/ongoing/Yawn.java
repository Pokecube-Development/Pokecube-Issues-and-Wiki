package pokecube.mobs.moves.attacks.ongoing;

import net.minecraft.world.entity.LivingEntity;
import pokecube.api.entity.IOngoingAffected;
import pokecube.api.entity.IOngoingAffected.IOngoingEffect;
import pokecube.api.moves.IMoveConstants;
import pokecube.core.moves.MovesUtils;
import pokecube.core.moves.templates.Move_Ongoing;

public class Yawn extends Move_Ongoing
{
    public Yawn()
    {
        super("yawn");
    }

    @Override
    public void doOngoingEffect(final LivingEntity user, final IOngoingAffected mob, final IOngoingEffect effect)
    {
        if (effect.getDuration() == 0) MovesUtils.setStatus(mob.getEntity(), IMoveConstants.STATUS_SLP);
    }

    @Override
    public int getDuration()
    {
        return 2;
    }

}
