package pokecube.core.ai.tasks.misc;

import com.google.common.collect.ImmutableMap;

import net.minecraft.entity.CreatureEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ai.RandomPositionGenerator;
import net.minecraft.entity.ai.brain.memory.MemoryModuleStatus;
import net.minecraft.entity.ai.brain.memory.MemoryModuleType;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.server.ServerWorld;
import pokecube.core.ai.brain.RootTask;

public class RunAway extends RootTask<CreatureEntity>
{
    private final MemoryModuleType<? extends Entity> badMemory;

    private final float runSpeed;

    public RunAway(final MemoryModuleType<? extends Entity> badMemory, final float speed)
    {
        super(ImmutableMap.of(MemoryModuleType.WALK_TARGET, MemoryModuleStatus.VALUE_ABSENT, badMemory,
                MemoryModuleStatus.VALUE_PRESENT));
        this.badMemory = badMemory;
        this.runSpeed = speed;
    }

    @Override
    protected boolean checkExtraStartConditions(final ServerWorld worldIn, final CreatureEntity owner)
    {
        final Entity entity = owner.getBrain().getMemory(this.badMemory).get();
        return owner.distanceToSqr(entity) < 36.0D;
    }

    @Override
    protected void start(final ServerWorld worldIn, final CreatureEntity entityIn, final long gameTimeIn)
    {
        final Entity entity = entityIn.getBrain().getMemory(this.badMemory).get();
        this.runAway(entityIn, entity, this.runSpeed);
    }

    public void runAway(final CreatureEntity mob, final Entity runFrom, final float speedIn)
    {
        for (int i = 0; i < 10; ++i)
        {
            final Vector3d vec3d = new Vector3d(runFrom.getX(), runFrom.getY(), runFrom.getZ());
            final Vector3d vec3d1 = RandomPositionGenerator.getLandPosAvoid(mob, 16, 7, vec3d);
            if (vec3d1 != null)
            {
                this.setWalkTo(vec3d1, speedIn, 0);
                return;
            }
        }

    }
}
