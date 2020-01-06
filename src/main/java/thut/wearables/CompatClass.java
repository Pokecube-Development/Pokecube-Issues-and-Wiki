package thut.wearables;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface CompatClass
{
    /**
     * It is up to the user to actually send these at the correct time. This
     * just is here for a way for the user to determine when they should be
     * calling the methods. POSTPOST means it should occur after postinit.
     */
    public static enum Phase
    {
        CONSTRUCT, SETUP, FINALIZE;
    }

    Phase phase() default Phase.SETUP;

    boolean takesEvent() default false;
}
