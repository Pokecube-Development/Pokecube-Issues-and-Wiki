package pokecube.api.data.abilities;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import pokecube.api.utils.Tools.MergeOrder;

@Retention(RetentionPolicy.RUNTIME)
public @interface AbilityProvider
{
    String[] name();
    
    MergeOrder order() default MergeOrder.AFTER;
}
