package thut.api.entity;

import java.util.List;

public interface IAnimated
{
    /**
     * This should return a prioritised list of possible animations for the mob.
     * The lower the index on the list, the higher the priority to use. The
     * renderer will walk up the list and pick the first value that it actually
     * has an animation for.
     *
     * @return
     */
    List<String> getChoices();
}
