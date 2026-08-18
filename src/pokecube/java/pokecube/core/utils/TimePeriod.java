package pokecube.core.utils;

import net.minecraft.world.level.Level;

/**
 * Represents a time period within the (Minecraftian) day.
 * <p>
 * 0 is sunrise, 6000 noon, 12000 dusk, 18000 midnight, 23999 last "tick" of the
 * night.
 * <p>
 * 24000 is a valid end time point and means "until the end of the night".
 * <p>
 * It's guaranteed that both values are in the range [0, 24000].
 */
public final class TimePeriod
{
    public final static TimePeriod fullDay = new TimePeriod(0, 1.0);

    public final static TimePeriod never = new TimePeriod(0, 0);

    public final double startTime;

    public final double endTime;

    private final boolean wrapped;

    /**
     * 0.0/1.0 means sunrise. Noon is at 0.25, dusk at 0.5, midnight at 0.75.
     * The precision is limited to Minecraft's tick precision.
     */
    public TimePeriod(final double start, final double end)
    {
        this.startTime = start;
        this.endTime = end;
        this.wrapped = this.startTime > this.endTime;
    }

    public TimePeriod(final TimePeriod other)
    {
        if (null != other)
        {
            this.startTime = other.startTime;
            this.endTime = other.endTime;
            this.wrapped = other.wrapped;
        }
        else
        {
            this.startTime = 0.0;
            this.endTime = 1.0;
            this.wrapped = false;
        }
    }

    public boolean contains(final double time)
    {
        if (this.startTime == this.endTime) return false;
        return this.wrapped ? time >= this.startTime || time <= this.endTime
                : time >= this.startTime && time <= this.endTime;
    }

    public boolean contains(long time, final long dayLength)
    {
        time = time % dayLength;
        return this.contains(time / (double) dayLength);
    }

    public static double getTime(Level level)
    {
        // Neoforge handles throttling getDayTime
        return getTime(level.getDayTime());
    }

    public static double getTime(long testTime)
    {
        double denom = Level.TICKS_PER_DAY;
        return testTime % Level.TICKS_PER_DAY / denom;
    }

}
