package pokecube.api.events;

import net.minecraft.world.entity.Entity;
import net.neoforged.bus.api.ICancellableEvent;
import net.neoforged.neoforge.event.entity.EntityEvent;

public class PokedexInspectEvent extends EntityEvent implements ICancellableEvent
{
    public final boolean shouldReward;

    public PokedexInspectEvent(Entity entity, boolean reward)
    {
        super(entity);
        this.shouldReward = reward;
    }
}
