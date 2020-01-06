package pokecube.core.interfaces;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Fields representing booleans for AI states which have this annotation will
 * be set to false on save. This will prevent those states from being
 * saved/loaded.
 */
@Retention(RUNTIME)
@Target(FIELD)
public @interface OldAI
{

}
