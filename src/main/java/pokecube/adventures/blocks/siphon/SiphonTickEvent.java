package pokecube.adventures.blocks.siphon;

import net.minecraftforge.eventbus.api.Event;

public class SiphonTickEvent extends Event
{
    private final SiphonTile tile;

    public SiphonTickEvent(SiphonTile tile)
    {
        this.tile = tile;
    }

    public SiphonTile getTile()
    {
        return tile;
    }

}
