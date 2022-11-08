package thut.tech.common.entity;

import net.minecraftforge.eventbus.api.Event;

public class EventLiftConsumePower extends Event
{
    public final EntityLift lift;
    public final long       toConsume;

    public EventLiftConsumePower(EntityLift lift, long toConsume)
    {
        this.lift = lift;
        this.toConsume = toConsume;
    }
}
