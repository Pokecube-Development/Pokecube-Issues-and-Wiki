package pokecube.api.data.moves;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import pokecube.api.data.moves.MoveApplicationRegistry.MergeOrder;

@Retention(RetentionPolicy.RUNTIME)
public @interface MoveProvider
{
    String[] name();
    
    MergeOrder order() default MergeOrder.AFTER;
}
