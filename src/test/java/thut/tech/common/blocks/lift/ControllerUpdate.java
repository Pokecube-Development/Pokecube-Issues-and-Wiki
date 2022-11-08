package thut.tech.common.blocks.lift;

import net.minecraftforge.eventbus.api.Event;

public class ControllerUpdate extends Event
{
    private final ControllerTile tile;

    public ControllerUpdate(ControllerTile tile)
    {
        this.tile = tile;
    }

    public ControllerTile getTile()
    {
        return this.tile;
    }
}
