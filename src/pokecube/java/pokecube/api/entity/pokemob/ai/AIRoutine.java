package pokecube.api.entity.pokemob.ai;

import com.google.common.collect.Maps;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.moves.utils.IMoveConstants;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Predicate;

public class AIRoutine
{

    private static AIRoutine[] values = new AIRoutine[0];
    private static Map<String, AIRoutine> names = Maps.newHashMap();

    //@formatter:off
    public static final AIRoutine 
    //Does the pokemob gather item drops and harvest crops.
    GATHER = new AIRoutine("GATHER"),
    //Does the pokemob store its inventory when full.
    STORE = new AIRoutine("STORE", false),
    //Does the pokemob return to inventory when dead
    POOFS = new AIRoutine("POOFS"),
   //Does the pokemob wander around randomly
    WANDER = new AIRoutine("WANDER"),
    //Does the pokemob breed.
    MATE = new AIRoutine("MATE"),
    //Does the pokemob follow its owner.
    FOLLOW = new AIRoutine("FOLLOW"),
    //Does the pokemob find targets to attack.
    AGRESSIVE = new AIRoutine("AGRESSIVE"),
    //Does the pokemob fly around, or can it only walk.
    AIRBORNE = new AIRoutine("AIRBORNE", true, IMoveConstants.canFly),
    //Can the pokemob open and close doors
    USEDOORS = new AIRoutine("USEDOORS", true, IMoveConstants.canOpenDoors);
    //@formatter:on

    public static AIRoutine[] values()
    {
        return values;
    }

    public static AIRoutine valueOf(String name)
    {
        return names.get(name);
    }

    private final boolean default_;

    private final Predicate<IPokemob> isAllowed;

    private final int ordinal;

    private final String name;

    public AIRoutine(String name)
    {
        this(name, true);
    }

    public AIRoutine(String name, boolean value)
    {
        this(name, value, p -> true);
    }

    public AIRoutine(String name, boolean value, final Predicate<IPokemob> isAllowed)
    {
        this.default_ = value;
        this.isAllowed = isAllowed;
        this.name = name;
        this.ordinal = values.length;
        values = Arrays.copyOf(values, values.length + 1);
        values[values.length - 1] = this;;
        names.put(name, this);
    }

    public String name()
    {
        return this.name;
    }

    @Override
    public String toString()
    {
        return this.name;
    }

    public int ordinal()
    {
        return this.ordinal;
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