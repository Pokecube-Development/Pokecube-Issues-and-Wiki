package pokecube.api.data.json.movepacket.consumers;

import pokecube.api.data.json.common.BaseConsumer;
import pokecube.api.entity.pokemob.moves.MovePacket;

public class MoveFlagSetter extends BaseConsumer<MovePacket>
{
    String flag;
    boolean value = true;

    @Override
    public void accept(MovePacket t)
    {
        MovePacket.setFlag(flag, t, value);
    }
}
