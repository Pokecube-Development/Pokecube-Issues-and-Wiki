package pokecube.api.data.spawns.matchers;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface MatcherFunction
{
    String name();
}
