package thut.api.world.mobs.data;

import net.minecraft.core.HolderLookup;

import java.util.List;

/**
 * Capability for synchronizing generic values between server and client.
 *
 */
public interface DataSync
{
    void setHolderLookup(HolderLookup.Provider provider);

    /**
     * Gets all entries.
     */
    List<Data<?>> getAll();

    /**
     * Gets all entries which need to be synced.
     */
    List<Data<?>> getDirty();

    /**
     * This registers the given data type, the integer returned is the key for
     * this data.
     */
    <T> Data<T> register(Data<T> data);

    void setRegisterTag(String tag);

    /**
     * Updates the given values.
     */
    void update(List<Data<?>> values);

    boolean needInit();

    void clearNeedInit();

    void init(List<Data<?>> values);

    /**
     * This returns the last tick it was synced, this is used to prevent
     * over-sending of the update packets
     *
     */
    long getTick();

    /**
     * Sets the last tick that this was synced.
     */
    void setTick(long tick);

    /**
     * @return How often this gets synced.
     */
    default int tickRate()
    {
        return 20;
    }

    boolean syncNow();

    void setSyncNow();
    /**
     * @return A random offset to apply with use with tickRate()
     */
    int tickOffset();

    void clearMatching(String tag);

    List<Data<?>> getTagged(String tag);

    void mapFrom(DataSync other, String tag);
}
