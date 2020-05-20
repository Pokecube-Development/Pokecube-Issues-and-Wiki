package pokecube.core.ai.tasks;

import net.minecraft.world.World;

public interface IRunnable
{
    /**
     * @param world
     * @return task ran sucessfully
     */
    boolean run(World world);
}
