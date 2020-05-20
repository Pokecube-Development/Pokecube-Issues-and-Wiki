package pokecube.core.ai.brain.sensors;

import java.util.Comparator;
import java.util.List;
import java.util.Set;

import com.google.common.collect.ImmutableSet;

import net.minecraft.entity.AgeableEntity;
import net.minecraft.entity.ai.brain.Brain;
import net.minecraft.entity.ai.brain.memory.MemoryModuleType;
import net.minecraft.entity.ai.brain.sensor.Sensor;
import net.minecraft.world.server.ServerWorld;
import pokecube.core.ai.brain.MemoryModules;
import thut.api.entity.BreedableCaps;
import thut.api.entity.IBreedingMob;

public class PossibleMates extends Sensor<AgeableEntity>
{
    long lastUpdate = 0;

    private boolean isValid(final AgeableEntity entityIn, final AgeableEntity otherAnimal)
    {
        final IBreedingMob us = BreedableCaps.getBreedable(entityIn);
        if (entityIn == otherAnimal) return false;
        if (us != null) return us.canMate(otherAnimal);
        return false;
    }

    @Override
    protected void update(final ServerWorld worldIn, final AgeableEntity entityIn)
    {
        final double dh = 8;
        final double dv = 4;
        final List<AgeableEntity> list = worldIn.getEntitiesWithinAABB(AgeableEntity.class, entityIn.getBoundingBox()
                .grow(dh, dv, dh), (otherAnimal) ->
                {
                    return otherAnimal.isAlive() && entityIn.canEntityBeSeen(otherAnimal) && this.isValid(entityIn,
                            otherAnimal);
                });
        list.sort(Comparator.comparingDouble(entityIn::getDistanceSq));
        final Brain<?> brain = entityIn.getBrain();
        if (!list.isEmpty()) brain.setMemory(MemoryModules.POSSIBLE_MATES, list);
        else brain.removeMemory(MemoryModules.POSSIBLE_MATES);
    }

    @Override
    public Set<MemoryModuleType<?>> getUsedMemories()
    {
        return ImmutableSet.of(MemoryModules.POSSIBLE_MATES);
    }

}
