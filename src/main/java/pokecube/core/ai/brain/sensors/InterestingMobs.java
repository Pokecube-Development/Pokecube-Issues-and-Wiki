package pokecube.core.ai.brain.sensors;

import java.util.Comparator;
import java.util.List;
import java.util.Random;
import java.util.Set;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.memory.NearestVisibleLivingEntities;
import net.minecraft.world.entity.ai.sensing.Sensor;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.phys.AABB;
import pokecube.core.ai.brain.BrainUtils;
import pokecube.core.ai.brain.MemoryModules;
import pokecube.core.interfaces.IMoveConstants.AIRoutine;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.capabilities.CapabilityPokemob;
import pokecube.core.interfaces.pokemob.ai.CombatStates;
import pokecube.core.items.pokemobeggs.EntityPokemobEgg;
import thut.api.entity.BreedableCaps;
import thut.api.entity.IBreedingMob;
import thut.api.entity.ai.RootTask;
import thut.api.terrain.TerrainManager;

public class InterestingMobs extends Sensor<LivingEntity>
{
    private static final TargetingConditions VISIBLE = TargetingConditions.forNonCombat().range(16.0D);

    public static boolean canPokemobMate(final IPokemob pokemob)
    {
        if (!pokemob.canBreed()) return false;
        if (!pokemob.getPokedexEntry().breeds) return false;
        if (pokemob.getPokedexEntry().isMega()) return false;
        if (pokemob.getPokedexEntry().isGMax()) return false;
        if (!pokemob.isRoutineEnabled(AIRoutine.MATE)) return false;
        if (pokemob.getCombatState(CombatStates.ANGRY) || BrainUtils.hasAttackTarget(pokemob.getEntity())) return false;
        return true;
    }

    long lastUpdate = 0;

    private boolean isValid(final AgeableMob entityIn, final AgeableMob otherAnimal)
    {
        final IBreedingMob us = BreedableCaps.getBreedable(entityIn);
        if (entityIn == otherAnimal) return false;
        final IPokemob other = CapabilityPokemob.getPokemobFor(otherAnimal);
        if (other != null && !InterestingMobs.canPokemobMate(other)) return false;
        final IBreedingMob them = BreedableCaps.getBreedable(otherAnimal);
        // Make the breeding check take either direction. This allows checking
        // things like ditto, etc.
        if (us != null) return us.canMate(otherAnimal) || them != null && them.canMate(entityIn);
        return false;
    }

    @Override
    protected void doTick(final ServerLevel worldIn, final LivingEntity entityIn)
    {
        final double s = 16;
        if (!TerrainManager.isAreaLoaded(entityIn.getLevel(), entityIn.blockPosition(), 8 + s)) return;

        if (RootTask.doLoadThrottling)
        {
            final Random rng = new Random(entityIn.getUUID().hashCode());
            final int tick = rng.nextInt(RootTask.runRate);
            if (entityIn.tickCount % RootTask.runRate != tick) return;
        }

        final List<AgeableMob> mates = Lists.newArrayList();
        final List<ItemEntity> items = Lists.newArrayList();
        final List<LivingEntity> mobs = Lists.newArrayList();
        final List<LivingEntity> visible = Lists.newArrayList();
        EntityPokemobEgg egg = null;
        final double dh = 8;
        final double dv = 4;
        final AABB mateBox = entityIn.getBoundingBox().inflate(dh, dv, dh);
        final AABB checkBox = entityIn.getBoundingBox().inflate(s, s, s);
        final List<Entity> list = worldIn.getEntitiesOfClass(Entity.class, checkBox, (hit) -> {
            return hit != entityIn && (hit instanceof LivingEntity || hit instanceof ItemEntity) && hit.isAlive();
        });
        list.sort(Comparator.comparingDouble(entityIn::distanceToSqr));
        final Brain<?> brain = entityIn.getBrain();
        final IPokemob us = CapabilityPokemob.getPokemobFor(entityIn);
        final boolean canMate = entityIn instanceof AgeableMob && (us == null || InterestingMobs.canPokemobMate(us));
        for (final Entity e : list) if (e instanceof LivingEntity)
        {
            final LivingEntity living = (LivingEntity) e;
            mobs.add(living);
            if (living instanceof EntityPokemobEgg newEgg && entityIn.getUUID().equals(newEgg.getMotherId()))
            {
                if (egg == null) egg = newEgg;
                else if (egg.distanceToSqr(entityIn) > newEgg.distanceToSqr(entityIn)) egg = newEgg;
            }
            else if (InterestingMobs.VISIBLE.test(entityIn, living))
            {
                visible.add(living);
                final boolean validMate = canMate && e instanceof AgeableMob
                        && mateBox.intersects(living.getBoundingBox())
                        && this.isValid((AgeableMob) entityIn, (AgeableMob) living);
                if (validMate) mates.add((AgeableMob) living);
            }
        }
        else if (e instanceof ItemEntity) items.add((ItemEntity) e);
        if (!mates.isEmpty()) brain.setMemory(MemoryModules.POSSIBLE_MATES, mates);
        else brain.eraseMemory(MemoryModules.POSSIBLE_MATES);
        if (!visible.isEmpty()) brain.setMemory(MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES,
                new NearestVisibleLivingEntities(entityIn, visible));
        else brain.eraseMemory(MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES);
        if (!mobs.isEmpty()) brain.setMemory(MemoryModuleType.NEAREST_LIVING_ENTITIES, mobs);
        else brain.eraseMemory(MemoryModuleType.NEAREST_LIVING_ENTITIES);
        if (!items.isEmpty()) brain.setMemory(MemoryModules.VISIBLE_ITEMS, items);
        else brain.eraseMemory(MemoryModules.VISIBLE_ITEMS);
        if (brain.checkMemory(MemoryModules.EGG, MemoryStatus.REGISTERED))
        {
            if (egg != null) brain.setMemory(MemoryModules.EGG, egg);
            else brain.eraseMemory(MemoryModules.EGG);
        }
    }

    @Override
    public Set<MemoryModuleType<?>> requires()
    {
        return ImmutableSet.of(MemoryModuleType.NEAREST_LIVING_ENTITIES, MemoryModules.POSSIBLE_MATES,
                MemoryModules.HERD_MEMBERS, MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES,
                MemoryModules.VISIBLE_ITEMS);
    }

}
