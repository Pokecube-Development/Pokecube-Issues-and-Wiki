package pokecube.core.events.onload;

import net.minecraftforge.eventbus.api.Event;

public class RegisterPokemobsEvent extends Event
{

    /** This is called after Register, incase anything needs to be done then. */
    public static class Post extends RegisterPokemobsEvent
    {

    }

    /**
     * This is called before the Register event, it should be used to get
     * anything setup that is needed before registration.
     */
    public static class Pre extends RegisterPokemobsEvent
    {

    }

    /**
     * This is called to do the registration itself. All new pokemobs should be
     * registered during this event.
     */
    public static class Register extends RegisterPokemobsEvent
    {

    }

    public RegisterPokemobsEvent()
    {
    }
}
