package pokecube.api.events.pokemobs.ai;

import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.event.entity.living.LivingEvent;

public class BrainInitEvent extends LivingEvent
{
    public BrainInitEvent(final LivingEntity entity)
    {
        super(entity);
    }
}
