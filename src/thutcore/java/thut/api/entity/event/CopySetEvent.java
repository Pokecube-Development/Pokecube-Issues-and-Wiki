package thut.api.entity.event;

import javax.annotation.Nullable;

import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.Cancelable;

@Cancelable
public class CopySetEvent extends LivingEvent
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
