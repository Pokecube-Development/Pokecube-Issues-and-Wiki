package pokecube.core.ai.brain.sensors;

import java.util.Comparator;
import java.util.List;
import java.util.Set;

import com.google.common.collect.ImmutableSet;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.Brain;
import net.minecraft.entity.ai.brain.memory.MemoryModuleType;
import net.minecraft.entity.ai.brain.sensor.Sensor;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.world.server.ServerWorld;
import pokecube.core.ai.brain.MemoryModules;

public class NearItems extends Sensor<LivingEntity>
{
    long lastUpdate = 0;

    @Override
    protected void update(final ServerWorld worldIn, final LivingEntity entityIn)
    {
        final List<ItemEntity> list = worldIn.getEntitiesWithinAABB(ItemEntity.class, entityIn.getBoundingBox().grow(
                16.0D, 16.0D, 16.0D), (item) ->
                {
                    return item.isAlive() && entityIn.canEntityBeSeen(item);
                });
        list.sort(Comparator.comparingDouble(entityIn::getDistanceSq));
        final Brain<?> brain = entityIn.getBrain();
        if (!list.isEmpty()) brain.setMemory(MemoryModules.VISIBLE_ITEMS, list);
        else brain.removeMemory(MemoryModules.VISIBLE_ITEMS);
    }

    @Override
    public Set<MemoryModuleType<?>> getUsedMemories()
    {
        return ImmutableSet.of(MemoryModules.VISIBLE_ITEMS);
    }

}
