package pokecube.api.entity.pokemob.ai;

import java.util.function.Predicate;

import net.minecraftforge.common.IExtensibleEnum;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.moves.utils.IMoveConstants;

public enum AIRoutine implements IExtensibleEnum
{
    //@formatter:off
    //Does the pokemob gather item drops and harvest crops.
    GATHER,
    //Does the pokemob store its inventory when full.
    STORE(false),
    //Does the pokemob return to inventory when dead
    POOFS,
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

    public static AIRoutine create(String name, boolean value, Predicate<IPokemob> isAllowed)
    {
        throw new IllegalStateException("Enum not extended");
    }

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