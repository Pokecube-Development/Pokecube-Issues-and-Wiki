package pokecube.core.ai.tasks;

import net.minecraft.world.level.Level;

public interface IRunnable
{
    /**
     * @param world
     * @return task ran sucessfully
     */
    boolean run(Level world);
}
