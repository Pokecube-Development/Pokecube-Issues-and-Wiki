package thut.crafts.entity;

import net.minecraftforge.eventbus.api.Event;

public class EventCraftConsumePower extends Event
{
    public final EntityCraft craft;
    public final long        toConsume;

    public EventCraftConsumePower(EntityCraft lift, long toConsume)
    {
        this.craft = lift;
        this.toConsume = toConsume;
    }
}
