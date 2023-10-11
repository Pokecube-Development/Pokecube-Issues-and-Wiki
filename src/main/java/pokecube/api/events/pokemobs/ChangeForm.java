package pokecube.api.events.pokemobs;

import net.minecraftforge.eventbus.api.Event;
import pokecube.api.entity.pokemob.IPokemob;

public abstract class ChangeForm extends Event
{

    /**
     * Fired on the PokecubeAPI.POKEMOB_BUS before a pokemob reverts to its
     * original form, usually called when recalling, but can be called due to
     * command
     */
    public static class Revert extends ChangeForm
    {
        private final boolean onRecall;

        public Revert(IPokemob pokemob, boolean onRecall)
        {
            super(pokemob);
            this.onRecall = onRecall;
        }

        public boolean isOnRecall()
        {
            return onRecall;
        }
    }

    /**
     * Fired on the PokecubeAPI.POKEMOB_BUS after a pokemob changes from to its
     * original form, usually called when mega evolving or g-maxing
     */
    public static class Post extends ChangeForm
    {
        public Post(IPokemob pokemob)
        {
            super(pokemob);
        }
    }
    /**
     * Fired on the PokecubeAPI.POKEMOB_BUS before a pokemob changes from to its
     * original form, usually called when mega evolving or g-maxing
     */
    public static class Pre extends ChangeForm
    {
        public Pre(IPokemob pokemob)
        {
            super(pokemob);
        }
    }

    private final IPokemob pokemob;

    public ChangeForm(IPokemob pokemob)
    {
        this.pokemob = pokemob;
    }

    public IPokemob getPokemob()
    {
        return pokemob;
    }
}
