package pokecube.core.events.pokemob.combat;

import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.eventbus.api.Event;
import pokecube.core.interfaces.IPokemob;

/** Fired on the PokecubeCore.MOVE_BUS */
public class MoveMessageEvent extends Event
{
    public ITextComponent message;
    public final IPokemob sender;

    public MoveMessageEvent(IPokemob sender, ITextComponent message)
    {
        this.message = message;
        this.sender = sender;
    }
}
