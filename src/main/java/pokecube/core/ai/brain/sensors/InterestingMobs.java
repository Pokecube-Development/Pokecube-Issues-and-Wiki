package pokecube.core.ai.brain.sensors;

import java.util.Comparator;
import java.util.List;
import java.util.Set;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;

import net.minecraft.entity.AgeableEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityPredicate;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.Brain;
import net.minecraft.entity.ai.brain.memory.MemoryModuleType;
import net.minecraft.entity.ai.brain.sensor.Sensor;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.world.server.ServerWorld;
import pokecube.core.PokecubeCore;
import pokecube.core.ai.brain.MemoryModules;
import thut.api.entity.BreedableCaps;
import thut.api.entity.IBreedingMob;
import thut.api.terrain.TerrainManager;

public class InterestingMobs extends Sensor<LivingEntity>
{
    private static final EntityPredicate VISIBLE = new EntityPredicate().setDistance(16.0D).allowFriendlyFire()
            .setSkipAttackChecks();

    long lastUpdate = 0;

    private boolean isValid(final AgeableEntity entityIn, final AgeableEntity otherAnimal)
    {
        final IBreedingMob us = BreedableCaps.getBreedable(entityIn);
        if (entityIn == otherAnimal) return false;
        if (us != null) return us.canMate(otherAnimal);
        return false;
    }

    @Override
    protected void update(final ServerWorld worldIn, final LivingEntity entityIn)
    {
        final double s = 16;
        if (!TerrainManager.isAreaLoaded(entityIn.dimension, entityIn.getPosition(), PokecubeCore
                .getConfig().movementPauseThreshold + s)) return;
        final List<AgeableEntity> mates = Lists.newArrayList();
        final List<ItemEntity> items = Lists.newArrayList();
        final List<LivingEntity> mobs = Lists.newArrayList();
        final List<LivingEntity> visible = Lists.newArrayList();
        final double dh = 8;
        final double dv = 4;
        final AxisAlignedBB mateBox = entityIn.getBoundingBox().grow(dh, dv, dh);
        final AxisAlignedBB checkBox = entityIn.getBoundingBox().grow(s, s, s);
        final List<Entity> list = worldIn.getEntitiesWithinAABB(Entity.class, checkBox, (hit) ->
        {
            return hit != entityIn && hit.isAlive() && (hit instanceof LivingEntity || hit instanceof ItemEntity);
        });
        list.sort(Comparator.comparingDouble(entityIn::getDistanceSq));
        final Brain<?> brain = entityIn.getBrain();
        final boolean canMate = entityIn instanceof AgeableEntity;
        for (final Entity e : list)
            if (e instanceof LivingEntity)
            {
                final LivingEntity living = (LivingEntity) e;
                final boolean canSee = InterestingMobs.VISIBLE.canTarget(entityIn, living);
                mobs.add(living);
                if (canSee)
                {
                    visible.add(living);
                    if (canMate && e instanceof AgeableEntity && mateBox.intersects(living.getBoundingBox()) && this
                            .isValid((AgeableEntity) entityIn, (AgeableEntity) living)) mates.add(
                                    (AgeableEntity) living);
                }
            }
            else if (e instanceof ItemEntity) items.add((ItemEntity) e);
        if (!mates.isEmpty()) brain.setMemory(MemoryModules.POSSIBLE_MATES, mates);
        else brain.removeMemory(MemoryModules.POSSIBLE_MATES);
        if (!visible.isEmpty()) brain.setMemory(MemoryModuleType.VISIBLE_MOBS, visible);
        else brain.removeMemory(MemoryModuleType.VISIBLE_MOBS);
        if (!mobs.isEmpty()) brain.setMemory(MemoryModuleType.MOBS, mobs);
        else brain.removeMemory(MemoryModuleType.MOBS);
        if (!items.isEmpty()) brain.setMemory(MemoryModules.VISIBLE_ITEMS, items);
        else brain.removeMemory(MemoryModules.VISIBLE_ITEMS);
    }

    @Override
    public Set<MemoryModuleType<?>> getUsedMemories()
    {
        return ImmutableSet.of(MemoryModuleType.MOBS, MemoryModules.POSSIBLE_MATES, MemoryModules.HERD_MEMBERS,
                MemoryModuleType.VISIBLE_MOBS, MemoryModules.VISIBLE_ITEMS);
    }

}
