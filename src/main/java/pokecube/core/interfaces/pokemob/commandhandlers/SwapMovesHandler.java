package pokecube.core.interfaces.pokemob.commandhandlers;

import io.netty.buffer.ByteBuf;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.network.pokemobs.PacketCommand.DefaultHandler;

public class SwapMovesHandler extends DefaultHandler
{
    public byte indexA;
    public byte indexB;

    public SwapMovesHandler()
    {
    }

    public SwapMovesHandler(Byte indexA, Byte indexB)
    {
        this.indexA = indexA;
        this.indexB = indexB;
    }

    @Override
    public void handleCommand(IPokemob pokemob)
    {
        pokemob.exchangeMoves(this.indexA, this.indexB);
    }

    @Override
    public void readFromBuf(ByteBuf buf)
    {
        super.readFromBuf(buf);
        this.indexA = buf.readByte();
        this.indexB = buf.readByte();
    }

    @Override
    public void writeToBuf(ByteBuf buf)
    {
        super.writeToBuf(buf);
        buf.writeByte(this.indexA);
        buf.writeByte(this.indexB);
    }
}
