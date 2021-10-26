package thut.api.world;

import net.minecraft.entity.MobEntity;
import net.minecraft.entity.ai.brain.memory.WalkTarget;
import net.minecraft.pathfinding.Path;

public interface IPathHelper
{
    Path getPath(final MobEntity mob, final WalkTarget target);

    boolean shouldHelpPath(final MobEntity mob, final WalkTarget target);
}
