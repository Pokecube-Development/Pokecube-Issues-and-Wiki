package pokecube.core.interfaces.pokemob.commandhandlers;

import io.netty.buffer.ByteBuf;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.network.pokemobs.PacketCommand.DefaultHandler;

public class MoveIndexHandler extends DefaultHandler
{
    public byte index;

    public MoveIndexHandler()
    {
    }

    public MoveIndexHandler(final Byte index)
    {
        this.index = index;
    }

    @Override
    public void handleCommand(final IPokemob pokemob)
    {
        pokemob.setMoveIndex(this.index);
    }

    @Override
    public void readFromBuf(final ByteBuf buf)
    {
        super.readFromBuf(buf);
        this.index = buf.readByte();
    }

    @Override
    public void writeToBuf(final ByteBuf buf)
    {
        super.writeToBuf(buf);
        buf.writeByte(this.index);
    }
}
