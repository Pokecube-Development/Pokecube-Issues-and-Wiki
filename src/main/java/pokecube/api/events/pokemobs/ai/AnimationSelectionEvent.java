package pokecube.api.events.pokemobs.ai;

import net.minecraftforge.event.entity.EntityEvent;
import pokecube.api.entity.pokemob.IPokemob;
import thut.api.entity.IAnimated;

/**
 * This event is fired on the PokecubeAPI.POKEMOB_BUS after selecting animations
 * for a pokemob.<br>
 * <br>
 * The {@link IAnimated} provided via {@link #getAnimated()} will have had the
 * lists of selected animations populated before this event is fired.
 *
 */
public class AnimationSelectionEvent extends EntityEvent
{
    private final IPokemob pokemob;
    private final IAnimated animated;

    public AnimationSelectionEvent(final IPokemob entity, IAnimated animated)
    {
        super(entity.getEntity());
        this.pokemob = entity;
        this.animated = animated;
    }

    public IPokemob getPokemob()
    {
        return this.pokemob;
    }

    public IAnimated getAnimated()
    {
        return animated;
    }

}
