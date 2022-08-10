package pokecube.core.ai.tasks;

import java.util.List;

import pokecube.api.entity.pokemob.IPokemob;
import thut.api.entity.ai.IAIRunnable;

public interface ITaskAdder
{
    void addTasks(final IPokemob pokemob, final List<IAIRunnable> list);
}
