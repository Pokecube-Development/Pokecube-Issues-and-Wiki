package pokecube.core.ai.tasks;

import java.util.function.Predicate;

import com.google.common.collect.ImmutableMap;

import net.minecraft.entity.EntityClassification;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.Brain;
import net.minecraft.entity.ai.brain.memory.MemoryModuleStatus;
import net.minecraft.entity.ai.brain.memory.MemoryModuleType;
import net.minecraft.util.math.EntityPosWrapper;
import net.minecraft.world.server.ServerWorld;
import pokecube.core.ai.brain.RootTask;

public class LookAtMob extends RootTask<LivingEntity>
{
    private final Predicate<LivingEntity> matcher;

    private final float distance_squared;

    public LookAtMob(final EntityClassification type, final float distance)
    {
        this((mob) ->
        {
            return type.equals(mob.getType().getClassification());
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
        super(ImmutableMap.of(MemoryModuleType.LOOK_TARGET, MemoryModuleStatus.VALUE_ABSENT,
                MemoryModuleType.VISIBLE_MOBS, MemoryModuleStatus.VALUE_PRESENT));
        this.matcher = matcher;
        this.distance_squared = distance * distance;
    }

    @Override
    protected boolean canTimeOut()
    {
        return true;
    }

    @Override
    protected boolean shouldExecute(final ServerWorld worldIn, final LivingEntity owner)
    {
        return owner.getBrain().getMemory(MemoryModuleType.VISIBLE_MOBS).get().stream().anyMatch(this.matcher);
    }

    @Override
    protected void startExecuting(final ServerWorld worldIn, final LivingEntity entityIn, final long gameTimeIn)
    {
        final Brain<?> brain = entityIn.getBrain();
        brain.getMemory(MemoryModuleType.VISIBLE_MOBS).ifPresent((list) ->
        {
            list.stream().filter(this.matcher).filter((mob) ->
            {
                return mob.getDistanceSq(entityIn) <= this.distance_squared;
            }).findFirst().ifPresent((mob) ->
            {
                brain.setMemory(MemoryModuleType.LOOK_TARGET, new EntityPosWrapper(mob));
            });
        });
    }
}
