package thut.wearables.events;

import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.Event.HasResult;
import thut.wearables.network.PacketGui.WearableContext;

@HasResult
public class WearableUseEvent extends Event
{
    public final WearableContext context;

    public WearableUseEvent(WearableContext context)
    {
        this.context = context;
    }

}
