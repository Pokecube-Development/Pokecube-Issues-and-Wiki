package thut.api.entity.event;

import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.ICancellableEvent;
import net.neoforged.neoforge.event.entity.living.LivingEvent;

public class CopyUpdateEvent extends LivingEvent implements ICancellableEvent
{
    public final LivingEntity realEntity;

    public CopyUpdateEvent(final LivingEntity e, final LivingEntity base)
    {
        super(e);
        this.realEntity = base;
    }
}
