package pokecube.core.ai.tasks.misc;

import com.google.common.collect.ImmutableMap;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.util.LandRandomPos;
import net.minecraft.world.phys.Vec3;
import pokecube.core.ai.brain.RootTask;

public class RunAway extends RootTask<PathfinderMob>
{
    private final MemoryModuleType<? extends Entity> badMemory;

    private final float runSpeed;

    public RunAway(final MemoryModuleType<? extends Entity> badMemory, final float speed)
    {
        super(ImmutableMap.of(MemoryModuleType.WALK_TARGET, MemoryStatus.VALUE_ABSENT, badMemory,
                MemoryStatus.VALUE_PRESENT));
        this.badMemory = badMemory;
        this.runSpeed = speed;
    }

    @Override
    protected boolean checkExtraStartConditions(final ServerLevel worldIn, final PathfinderMob owner)
    {
        final Entity entity = owner.getBrain().getMemory(this.badMemory).get();
        return owner.distanceToSqr(entity) < 36.0D;
    }

    @Override
    protected void start(final ServerLevel worldIn, final PathfinderMob entityIn, final long gameTimeIn)
    {
        final Entity entity = entityIn.getBrain().getMemory(this.badMemory).get();
        this.runAway(entityIn, entity, this.runSpeed);
    }

    public void runAway(final PathfinderMob mob, final Entity runFrom, final float speedIn)
    {
        for (int i = 0; i < 10; ++i)
        {
            final Vec3 vec3d = new Vec3(runFrom.getX(), runFrom.getY(), runFrom.getZ());
            final Vec3 vec3d1 = LandRandomPos.getPosAway(mob, 16, 7, vec3d);
            if (vec3d1 != null)
            {
                this.setWalkTo(vec3d1, speedIn, 0);
                return;
            }
        }

    }
}
