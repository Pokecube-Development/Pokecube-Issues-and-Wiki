package pokecube.gimmicks.zmoves;

import pokecube.api.entity.pokemob.IPokemob;

public interface ZPower
{
    default boolean canZMove(final IPokemob pokemob, final String moveIn)
    {
        return false;
    }
}
