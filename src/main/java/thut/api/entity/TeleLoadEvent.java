package thut.api.entity;

import net.minecraftforge.eventbus.api.Cancelable;
import net.minecraftforge.eventbus.api.Event;
import thut.api.entity.ThutTeleporter.TeleDest;

@Cancelable
public class TeleLoadEvent extends Event
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
