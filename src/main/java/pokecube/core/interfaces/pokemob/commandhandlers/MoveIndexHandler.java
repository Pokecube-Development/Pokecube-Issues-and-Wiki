package pokecube.core.interfaces.pokemob.commandhandlers;

import io.netty.buffer.ByteBuf;
import pokecube.core.PokecubeCore;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.network.pokemobs.PacketCommand.DefaultHandler;

public class MoveIndexHandler extends DefaultHandler
{
    public byte index;

    public MoveIndexHandler()
    {
    }

    public MoveIndexHandler(Byte index)
    {
        this.index = index;
    }

    @Override
    public void handleCommand(IPokemob pokemob)
    {
        PokecubeCore.LOGGER.debug(this.index + " " + pokemob.getMoveIndex() + " " + pokemob.getEntity());
        pokemob.setMoveIndex(this.index);
    }

    @Override
    public void readFromBuf(ByteBuf buf)
    {
        super.readFromBuf(buf);
        this.index = buf.readByte();
    }

    @Override
    public void writeToBuf(ByteBuf buf)
    {
        super.writeToBuf(buf);
        buf.writeByte(this.index);
    }
}
