package pokecube.api.ai;

import java.util.List;
import java.util.Map;

import pokecube.api.entity.pokemob.IPokemob;
import thut.api.entity.ai.IAIRunnable;

public interface ITaskAdder
{
    void addTasks(final IPokemob pokemob, final List<IAIRunnable> list, Map<String, IAIRunnable> namedTasks);
}
