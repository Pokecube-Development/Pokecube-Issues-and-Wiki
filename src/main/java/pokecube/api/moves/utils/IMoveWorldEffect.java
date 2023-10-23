package pokecube.api.moves.utils;

import pokecube.api.entity.pokemob.IPokemob;
import thut.api.maths.Vector3;

public interface IMoveWorldEffect
{
    /** Apply the effect for the pokemob, move is used at the given location */
    default boolean applyOutOfCombat(IPokemob user, Vector3 location)
    {
        return false;
    }

    /** Apply the effect for the pokemob, move is used at the given location */
    default boolean applyInCombat(IPokemob user, Vector3 location)
    {
        return false;
    }

    /** The name of the move associated with this action */
    String getMoveName();

    /**
     * Called when the action is created, if the action exists by postinit, this
     * is called then, otherwise it is called when it is created.
     */
    default void init()
    {

    }
    
    default boolean isValid() {
        return true;
    }
}
