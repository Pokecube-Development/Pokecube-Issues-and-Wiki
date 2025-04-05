package thut.api.entity.event;

import javax.annotation.Nullable;

import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.ICancellableEvent;
import net.neoforged.neoforge.event.entity.living.LivingEvent;

public class CopySetEvent extends LivingEvent implements ICancellableEvent
{
    public final @Nullable LivingEntity oldCopy;
    public final @Nullable LivingEntity newCopy;

    public CopySetEvent(final LivingEntity entity, @Nullable final LivingEntity oldCopy,
            @Nullable final LivingEntity newCopy)
    {
        super(entity);
        this.oldCopy = oldCopy;
        this.newCopy = newCopy;
    }

}
