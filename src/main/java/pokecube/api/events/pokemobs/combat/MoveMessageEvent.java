package pokecube.api.events.pokemobs.combat;

import net.minecraft.network.chat.Component;
import net.minecraftforge.eventbus.api.Event;
import pokecube.api.entity.pokemob.IPokemob;

/** Fired on the PokecubeAPI.MOVE_BUS */
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
