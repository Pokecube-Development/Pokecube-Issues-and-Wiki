package thut.api.entity;

public interface IMobColourable
{
    /**
     * These are specific changes for when dye is used on the mob.
     *
     * @return
     */
    int getDyeColour();

    /**
     * These are global colour changes.
     *
     * @param colours
     */
    int[] getRGBA();

    void setDyeColour(int colour);

    void setRGBA(int... colours);
}
