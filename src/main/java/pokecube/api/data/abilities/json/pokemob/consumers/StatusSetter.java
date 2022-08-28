package pokecube.api.data.abilities.json.pokemob.consumers;

import pokecube.api.data.abilities.json.common.BaseConsumer;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.moves.IMoveConstants;

public class StatusSetter extends BaseConsumer<IPokemob>
{
    String status;
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
    }

    @Override
    public void accept(IPokemob t)
    {
        t.setStatus(_status);
    }
}
