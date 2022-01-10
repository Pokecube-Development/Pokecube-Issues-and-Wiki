package pokecube.core.ai.tasks.misc;

import java.util.Optional;
import java.util.function.Predicate;

import com.google.common.collect.ImmutableMap;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.EntityTracker;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import thut.api.entity.ai.RootTask;

public class LookAtMob extends RootTask<LivingEntity>
{
    private final Predicate<LivingEntity> matcher;

    private final float distance_squared;

    public LookAtMob(final MobCategory type, final float distance)
    {
        this((mob) ->
        {
            return type.equals(mob.getType().getCategory());
        }, distance);
    }

    public LookAtMob(final EntityType<?> type, final float distance)
    {
        this((mob) ->
        {
            return type.equals(mob.getType());
        }, distance);
    }

    public LookAtMob(final Predicate<LivingEntity> matcher, final float distance)
    {
        super(ImmutableMap.of(MemoryModuleType.LOOK_TARGET, MemoryStatus.VALUE_ABSENT,
                MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES, MemoryStatus.VALUE_PRESENT));
        this.matcher = matcher;
        this.distance_squared = distance * distance;
    }

    @Override
    protected boolean canTimeOut()
    {
        return true;
    }

    @Override
    protected boolean checkExtraStartConditions(final ServerLevel worldIn, final LivingEntity owner)
    {
        return owner.getBrain().getMemory(MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES).get().findClosest(
                this.matcher).isPresent();
    }

    @Override
    protected void start(final ServerLevel worldIn, final LivingEntity entityIn, final long gameTimeIn)
    {
        final Brain<?> brain = entityIn.getBrain();

        final Optional<LivingEntity> found = brain.getMemory(MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES).get()
                .findClosest(this.matcher.and(mob -> mob.distanceToSqr(entityIn) <= this.distance_squared));

        found.ifPresent((mob) ->
        {
            brain.setMemory(MemoryModuleType.LOOK_TARGET, new EntityTracker(mob, true));
        });
    }
}
