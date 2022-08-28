package pokecube.api.data.abilities.json.common;

import java.util.function.Consumer;

public abstract class BaseConsumer<T> implements Consumer<T>
{
    public void init()
    {}
}
