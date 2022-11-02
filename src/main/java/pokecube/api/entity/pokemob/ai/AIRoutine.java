package pokecube.api.entity.pokemob.ai;

import java.util.function.Predicate;

import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.moves.utils.IMoveConstants;

public enum AIRoutine
{
    //@formatter:off
    //Does the pokemob gather item drops and harvest crops.
    GATHER,
    //Does the pokemob act like a vanilla bee
    BEEAI(true, IMoveConstants.isBee),
    //Does the pokemob act like an ant
    ANTAI(true, IMoveConstants.isAnt),
    //Does the pokemob make burrows
    BURROWS(true, IMoveConstants.burrows),
    //Does the pokemob store its inventory when full.
    STORE(false),
   //Does the pokemob wander around randomly
    WANDER,
    //Does the pokemob breed.
    MATE,
    //Does the pokemob follow its owner.
    FOLLOW,
    //Does the pokemob find targets to attack.
    AGRESSIVE,
    //Does the pokemob fly around, or can it only walk.
    AIRBORNE(true, IMoveConstants.canFly),
    //Can the pokemob open and close doors
    USEDOORS(true, IMoveConstants.canOpenDoors);
    //@formatter:on

    private final boolean default_;

    private final Predicate<IPokemob> isAllowed;

    private AIRoutine()
    {
        this(true);
    }

    private AIRoutine(final boolean value)
    {
        this(value, p -> true);
    }

    private AIRoutine(final boolean value, final Predicate<IPokemob> isAllowed)
    {
        this.default_ = value;
        this.isAllowed = isAllowed;
    }

    /** @return default state for this routine. */
    public boolean getDefault()
    {
        return this.default_;
    }

    public boolean isAllowed(final IPokemob pokemob)
    {
        return this.isAllowed.test(pokemob);
    }
}