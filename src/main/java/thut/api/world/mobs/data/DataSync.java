package thut.api.world.mobs.data;

import java.util.List;

public interface DataSync
{
    /**
     * Gets the value for the entry.
     *
     * @param key
     * @return
     */
    <T> T get(int key);

    /**
     * Gets all entries.
     *
     * @return
     */
    List<Data<?>> getAll();

    /**
     * Gets all entries which need to by synced.
     *
     * @return
     */
    List<Data<?>> getDirty();

    /**
     * This registers the given data type, the integer returned is the key for
     * this data.
     *
     * @param data
     * @return
     */
    <T> int register(Data<T> data, T value);

    /**
     * Sets the given entry to the value.
     *
     * @param key
     * @param value
     */
    <T> void set(int key, T value);

    /**
     * Updates the given values.
     *
     * @param values
     */
    void update(List<Data<?>> values);

    /**
     * This returns the last tick it was synced, this is used to prevent
     * over-sending of the update packets
     * 
     * @return
     */
    long getTick();

    /**
     * Sets the last tick that this was synced.
     * 
     * @param tick
     */
    void setTick(long tick);

    /**
     * @return How often this gets synced.
     */
    default int tickRate()
    {
        return 2;
    }

    /**
     * @return A random offset to apply with use with tickRate()
     */
    int tickOffset();
}
