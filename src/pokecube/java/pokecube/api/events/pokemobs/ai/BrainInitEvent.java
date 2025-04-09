package pokecube.api.events.pokemobs.ai;

import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.event.entity.living.LivingEvent;

public class BrainInitEvent extends LivingEvent
{
    public final boolean constructing;

    public BrainInitEvent(final LivingEntity entity, boolean onConstruction)
    {
        super(entity);
        this.constructing = onConstruction;
    }
}
