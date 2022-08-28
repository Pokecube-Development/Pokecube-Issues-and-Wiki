package pokecube.api.data.json.common;

import java.util.function.Consumer;

public abstract class BaseConsumer<T> implements Consumer<T>
{
    public void init()
    {}
}
