package pokecube.api.data.moves;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import pokecube.api.utils.Tools.MergeOrder;

@Retention(RetentionPolicy.RUNTIME)
public @interface MoveProvider
{
    String[] name();
    
    MergeOrder order() default MergeOrder.AFTER;
}
