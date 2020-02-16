package pokecube.core.moves.zmoves;

import pokecube.core.interfaces.IPokemob;

public interface ZPower
{
    default boolean canZMove(final IPokemob pokemob, final String moveIn)
    {
        return false;
    }
}
