package pokecube.api.events.pokemobs.combat;

import net.minecraftforge.eventbus.api.Event;
import pokecube.api.moves.utils.MoveApplication;

/**
 * This event is fired when MoveApplication is being constructed, you may use this
 * to interfere with the packet. fored on the PokecubeAPI.MOVE_BUS
 */
public class AttackEvent extends Event
{
    public final MoveApplication moveInfo;

    public AttackEvent(MoveApplication moveInfo)
    {
        this.moveInfo = moveInfo;
    }
}
