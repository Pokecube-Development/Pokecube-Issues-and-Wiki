package thut.api.world;

import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.level.pathfinder.Path;

public interface IPathHelper
{
    Path getPath(final Mob mob, final WalkTarget target);

    boolean shouldHelpPath(final Mob mob, final WalkTarget target);
}
