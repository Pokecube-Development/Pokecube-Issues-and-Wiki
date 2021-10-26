package pokecube.core.ai.tasks.idle.hunger;

import net.minecraft.entity.MobEntity;
import net.minecraft.entity.ai.brain.memory.WalkTarget;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import pokecube.core.ai.brain.MemoryModules;
import thut.api.maths.Vector3;

public abstract class EatBlockBase implements IBlockEatTask
{
    protected void setWalkTo(final MobEntity entity, final Vector3 pos, final double speed, final int dist)
    {
        this.setWalkTo(entity, pos.toVec3d(), speed, dist);
    }

    protected void setWalkTo(final MobEntity entity, final Vector3d pos, final double speed, final int dist)
    {
        entity.getBrain().setMemory(MemoryModules.WALK_TARGET, new WalkTarget(pos, (float) speed, dist));
    }

    protected void setWalkTo(final MobEntity entity, final BlockPos pos, final double speed, final int dist)
    {
        entity.getBrain().setMemory(MemoryModules.WALK_TARGET, new WalkTarget(pos, (float) speed, dist));
    }

}
