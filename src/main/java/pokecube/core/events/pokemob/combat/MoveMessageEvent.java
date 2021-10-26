package pokecube.core.events.pokemob.combat;

import net.minecraft.network.chat.Component;
import net.minecraftforge.eventbus.api.Event;
import pokecube.core.interfaces.IPokemob;

/** Fired on the PokecubeCore.MOVE_BUS */
public class MoveMessageEvent extends Event
{
    public Component message;
    public final IPokemob sender;

    public MoveMessageEvent(IPokemob sender, Component message)
    {
        this.message = message;
        this.sender = sender;
    }
}
