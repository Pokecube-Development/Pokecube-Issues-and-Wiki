package pokecube.core.ai.logic;

import net.minecraft.world.level.Level;

public interface Logic
{
    default boolean shouldRun()
    {
        return true;
    }

    void tick(Level world);
}
