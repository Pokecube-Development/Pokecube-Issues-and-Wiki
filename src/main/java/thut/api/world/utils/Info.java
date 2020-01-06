package thut.api.world.utils;

import java.io.Serializable;

import javax.annotation.Nullable;

public interface Info extends Serializable
{
    /**
     * Loads this back from a serialized string.
     *
     * @param value
     */
    void deserialize(String value);

    /**
     * Converts this to a serialized string.
     *
     * @return
     */
    String serialize();

    /**
     * Sets value as the return parameter for the given key.
     *
     * @param key
     * @param value
     */
    <T> void set(String key, T value);

    /**
     * @param key
     * @return The value of type T that matches the given key
     */
    @Nullable
    <T> T value(String key, Class<T> type);
}
