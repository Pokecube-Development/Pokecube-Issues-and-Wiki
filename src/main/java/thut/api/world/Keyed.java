package thut.api.world;

public interface Keyed
{
    /**
     * This is a string key for this object, all object of the same "type" will
     * return the same value for this.
     *
     * @return
     */
    String key();
}
