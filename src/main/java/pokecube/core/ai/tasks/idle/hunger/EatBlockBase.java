package pokecube.core.ai.tasks.idle.hunger;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.phys.Vec3;
import pokecube.core.ai.brain.MemoryModules;
import thut.api.maths.Vector3;

public abstract class EatBlockBase implements IBlockEatTask
{
    protected void setWalkTo(final Mob entity, final Vector3 pos, final double speed, final int dist)
    {
        this.setWalkTo(entity, pos.toVec3d(), speed, dist);
    }

    protected void setWalkTo(final Mob entity, final Vec3 pos, final double speed, final int dist)
    {
        entity.getBrain().setMemory(MemoryModules.WALK_TARGET, new WalkTarget(pos, (float) speed, dist));
    }

    protected void setWalkTo(final Mob entity, final BlockPos pos, final double speed, final int dist)
    {
        entity.getBrain().setMemory(MemoryModules.WALK_TARGET, new WalkTarget(pos, (float) speed, dist));
    }

}
