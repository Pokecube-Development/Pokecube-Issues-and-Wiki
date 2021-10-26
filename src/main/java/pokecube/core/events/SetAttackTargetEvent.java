package pokecube.core.events;

import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.eventbus.api.Event;

public class SetAttackTargetEvent extends Event
{
    public final LivingEntity mob;
    public final LivingEntity originalTarget;

    public LivingEntity newTarget;

    public SetAttackTargetEvent(final LivingEntity mob, final LivingEntity target)
    {
        this.mob = mob;
        this.originalTarget = this.newTarget = target;
    }

}
