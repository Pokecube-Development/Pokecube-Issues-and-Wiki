package thut.api.entity.event;

import net.minecraft.entity.LivingEntity;
import net.minecraftforge.event.entity.living.LivingEvent;

public class CopyUpdateEvent extends LivingEvent
{

    public CopyUpdateEvent(final LivingEntity e)
    {
        super(e);
    }

}
