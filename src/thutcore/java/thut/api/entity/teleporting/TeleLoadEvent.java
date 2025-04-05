package thut.api.entity.teleporting;

import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.ICancellableEvent;

public class TeleLoadEvent extends Event implements ICancellableEvent
{
    private final TeleDest dest;

    private TeleDest override;

    public TeleLoadEvent(final TeleDest dest)
    {
        this.dest = dest;
        this.setOverride(dest);
    }

    public TeleDest getDest()
    {
        return this.dest;
    }

    public TeleDest getOverride()
    {
        return this.override;
    }

    public void setOverride(final TeleDest override)
    {
        this.override = override;
    }

}
