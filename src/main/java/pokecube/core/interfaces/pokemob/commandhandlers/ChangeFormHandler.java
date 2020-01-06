package pokecube.core.interfaces.pokemob.commandhandlers;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.PacketBuffer;
import pokecube.core.database.Database;
import pokecube.core.database.PokedexEntry;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.network.pokemobs.PacketCommand.DefaultHandler;

public class ChangeFormHandler extends DefaultHandler
{
    String forme;

    public ChangeFormHandler()
    {
    }

    public ChangeFormHandler(String forme)
    {
        this.forme = forme;
    }

    @Override
    public void handleCommand(IPokemob pokemob) throws Exception
    {
        final PokedexEntry entry = Database.getEntry(this.forme);
        if (entry == null) throw new NullPointerException("No Entry found for " + this.forme);
        if (entry.getPokedexNb() != pokemob.getPokedexNb()) throw new IllegalArgumentException(
                "Cannot change form to a different pokedex number.");
        pokemob.megaEvolve(entry);
    }

    @Override
    public void readFromBuf(ByteBuf buf)
    {
        super.readFromBuf(buf);
        this.forme = new PacketBuffer(buf).readString(20);
    }

    @Override
    public void writeToBuf(ByteBuf buf)
    {
        super.writeToBuf(buf);
        new PacketBuffer(buf).writeString(this.forme);
    }

}
