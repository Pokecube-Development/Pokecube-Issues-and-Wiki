package pokecube.api.data.json.movepacket.consumers;

import pokecube.api.data.json.common.BaseConsumer;
import pokecube.api.entity.pokemob.moves.MovePacket;

public class PowerScaler extends BaseConsumer<MovePacket>
{
    float scale;

    @Override
    public void accept(MovePacket t)
    {
        t.PWR *= scale;
    }
}
