package pokecube.core.events;

import net.minecraft.world.entity.Entity;
import net.minecraftforge.event.entity.EntityEvent;
import net.minecraftforge.eventbus.api.Cancelable;

@Cancelable
public class PokedexInspectEvent extends EntityEvent
{
    public final boolean shouldReward;

    public PokedexInspectEvent(Entity entity, boolean reward)
    {
        super(entity);
        this.shouldReward = reward;
    }
}
