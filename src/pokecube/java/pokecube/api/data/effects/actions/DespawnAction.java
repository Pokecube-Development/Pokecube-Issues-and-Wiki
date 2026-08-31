package pokecube.api.data.effects.actions;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.neoforged.neoforge.event.EventHooks;
import thut.api.ThutCaps;
import thut.api.attachments.IOwnable;

public class DespawnAction implements IEffectAction
{
    public DespawnAction()
    {}

    @Override
    public void applyEffect(LivingEntity mob)
    {
        IOwnable ownable = ThutCaps.getOwnable(mob);
        // Don't despawn owned things.
        if (ownable != null && ownable.getOwnerId() != null) return;
        // Check the despawn event
        if (mob instanceof Mob mon && EventHooks.checkMobDespawn(mon)) return;
        mob.discard();
    }
}
