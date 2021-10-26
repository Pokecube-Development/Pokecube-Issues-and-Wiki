package pokecube.core.events;

import net.minecraft.entity.LivingEntity;
import net.minecraftforge.event.entity.living.LivingEvent;

public class BrainInitEvent extends LivingEvent
{

    public BrainInitEvent(final LivingEntity entity)
    {
        super(entity);
    }

}
