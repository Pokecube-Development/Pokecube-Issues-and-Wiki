package pokecube.core.interfaces;

import thut.api.maths.Vector3;

public interface IMoveAction
{
    /** Apply the effect for the pokemob, move is used at the given location */
    boolean applyEffect(IPokemob user, Vector3 location);

    /** The name of the move associated with this action */
    String getMoveName();

    /**
     * Called when the action is created, if the action exists by postinit,
     * this is called then, otherwise it is called when it is created.
     */
    default void init()
    {

    }
}
