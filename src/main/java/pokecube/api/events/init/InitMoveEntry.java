package pokecube.api.events.init;

import net.minecraftforge.eventbus.api.Event;
import pokecube.api.moves.MoveEntry;

/**
 * Fired on the PokecubeAPI.MOVE_BUS when a move entry is being initialised. Use
 * this to apply custom move typers, etc.
 *
 */
public class InitMoveEntry extends Event
{
    private final MoveEntry entry;

    public InitMoveEntry(MoveEntry entry)
    {
        this.entry = entry;
    }

    public MoveEntry getEntry()
    {
        return entry;
    }
}
