package pokecube.api.events.core.pokemob.combat;

import net.minecraftforge.eventbus.api.Event;
import pokecube.api.entity.pokemob.moves.MovePacket;

/**
 * This event is fired when MovePacket is being constructed, you may use this
 * to interfere with the packet. fored on the PokecubeAPI.MOVE_BUS
 */
public class AttackEvent extends Event
{
    public final MovePacket moveInfo;

    public AttackEvent(MovePacket moveInfo)
    {
        this.moveInfo = moveInfo;
    }
}
