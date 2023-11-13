package pokecube.core.ai.tasks.utility;

import pokecube.api.entity.pokemob.IPokemob;

/**
 * This IAIRunnable gets the mob to build a structure as defined in a book given
 * to it in the offhand slot.
 */
public class BuildStructureTask extends UtilTask
{

    public BuildStructureTask(IPokemob pokemob)
    {
        super(pokemob);
    }

    @Override
    public void reset()
    {
    }

    @Override
    public void run()
    {
    }

    @Override
    public boolean shouldRun()
    {
        return false;
    }

}
