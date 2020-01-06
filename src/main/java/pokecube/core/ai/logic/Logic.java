package pokecube.core.ai.logic;

import net.minecraft.world.World;

public interface Logic
{
    default boolean shouldRun()
    {
        return true;
    }

    void tick(World world);
}
