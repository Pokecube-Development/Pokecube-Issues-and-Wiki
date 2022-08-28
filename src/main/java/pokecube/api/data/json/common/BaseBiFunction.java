package pokecube.api.data.json.common;

import java.util.function.BiFunction;

public abstract class BaseBiFunction<T, U, R> implements BiFunction<T, U, R>
{
    public void init()
    {}
}
