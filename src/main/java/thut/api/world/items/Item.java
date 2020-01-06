package thut.api.world.items;

import javax.annotation.Nullable;

import thut.api.world.Keyed;
import thut.api.world.utils.Info;

public interface Item extends Keyed
{
    /**
     * Stored info for this block (null if none)
     *
     * @return
     */
    @Nullable
    Info info();

    /**
     * This is a string key for this item, all item of the same "type" will
     * return the same value for this.
     *
     * @return
     */
    @Override
    String key();

}
