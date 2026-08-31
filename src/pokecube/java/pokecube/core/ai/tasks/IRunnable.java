package pokecube.core.ai.tasks;

import net.minecraft.world.level.Level;

public interface IRunnable
{
    /**
     * @return task ran sucessfully
     */
    boolean run(Level world);
}
