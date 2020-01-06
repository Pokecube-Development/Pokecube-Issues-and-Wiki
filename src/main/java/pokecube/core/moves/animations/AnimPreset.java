package pokecube.core.moves.animations;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface AnimPreset
{
    String getPreset();
}
