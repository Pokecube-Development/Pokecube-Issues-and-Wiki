package pokecube.api.ai;

import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import pokecube.api.events.pokemobs.InitAIEvent.Init;

public class TaskAdders
{
    private static Map<Init.Type, List<ITaskAdder>> taskAdders = Maps.newConcurrentMap();

    static
    {
        for (final Init.Type type : Init.Type.values()) TaskAdders.taskAdders.put(type, Lists.newArrayList());
    }

    public static void register(final Init.Type type, final ITaskAdder adder)
    {
        TaskAdders.taskAdders.get(type).add(adder);
    }

    public static List<ITaskAdder> getAdders(final Init.Type type)
    {
        return TaskAdders.taskAdders.get(type);
    }
}
