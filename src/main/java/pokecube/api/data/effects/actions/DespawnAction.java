package pokecube.api.data.effects.actions;

import net.minecraft.world.entity.LivingEntity;
import thut.api.IOwnable;
import thut.api.OwnableCaps;

public class DespawnAction implements IEffectAction
{
    public DespawnAction()
    {}

    @Override
    public void applyEffect(LivingEntity mob)
    {
        IOwnable ownable = OwnableCaps.getOwnable(mob);
        // Don't despawn owned things.
        if (ownable != null && ownable.getOwnerId() != null) return;
        mob.discard();
    }
}
