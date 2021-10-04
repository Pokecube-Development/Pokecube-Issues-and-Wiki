package pokecube.mobs.moves.attacks.ongoing;

import net.minecraft.world.entity.LivingEntity;
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
    public void doOngoingEffect(final LivingEntity user, final IOngoingAffected mob, final IOngoingEffect effect)
    {
        final float thisMaxHP = mob.getEntity().getMaxHealth();
        final int damage = Math.max(1, (int) (0.125 * thisMaxHP));
        this.damageTarget(mob.getEntity(), user, damage);
    }
}
