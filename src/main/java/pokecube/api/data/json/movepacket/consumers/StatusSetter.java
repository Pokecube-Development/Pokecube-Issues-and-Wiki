package pokecube.api.data.json.movepacket.consumers;

import pokecube.api.data.json.common.BaseConsumer;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.entity.pokemob.PokemobCaps;
import pokecube.api.entity.pokemob.moves.MovePacket;
import pokecube.api.moves.IMoveConstants;

public class StatusSetter extends BaseConsumer<MovePacket>
{
    String status;
    String target = "attacker";

    byte _target = -1;
    byte _status = -1;

    @Override
    public void init()
    {
        switch (status)
        {
        case ("BRN"):
            _status = IMoveConstants.STATUS_BRN;
            break;
        case ("PSN"):
            _status = IMoveConstants.STATUS_PSN;
            break;
        case ("PSN2"):
            _status = IMoveConstants.STATUS_PSN2;
            break;
        case ("FRZ"):
            _status = IMoveConstants.STATUS_FRZ;
            break;
        case ("PAR"):
            _status = IMoveConstants.STATUS_PAR;
            break;
        default:
            _status = IMoveConstants.STATUS_NON;
        }
        switch (target)
        {
        case ("attacker"):
            _target = 1;
            break;
        case ("move_packet"):
            _target = 0;
            break;
        default:
            _target = 2;
        }
    }

    @Override
    public void accept(MovePacket t)
    {
        switch (_target)
        {
        case (0):
            t.statusChange = _status;
            break;
        case (1):
            t.attacker.setStatus(_status);
            break;
        default:
            IPokemob hit = PokemobCaps.getPokemobFor(t.attacked);
            if (hit != null) hit.setStatus(_status);
            break;
        }
    }
}
