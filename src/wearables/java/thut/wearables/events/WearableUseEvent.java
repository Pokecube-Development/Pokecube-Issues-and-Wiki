package thut.wearables.events;

import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.ICancellableEvent;
import thut.wearables.network.PacketGui.WearableContext;

public class WearableUseEvent extends Event implements ICancellableEvent
{
    public final WearableContext context;

    public WearableUseEvent(WearableContext context)
    {
        this.context = context;
    }

}
