package pokecube.core.ai.brain.sensors;

import java.util.Comparator;
import java.util.List;
import java.util.Random;
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
import pokecube.core.ai.brain.BrainUtils;
import pokecube.core.ai.brain.MemoryModules;
import pokecube.core.ai.brain.RootTask;
import pokecube.core.interfaces.IMoveConstants.AIRoutine;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.capabilities.CapabilityPokemob;
import pokecube.core.interfaces.pokemob.ai.CombatStates;
import thut.api.entity.BreedableCaps;
import thut.api.entity.IBreedingMob;
import thut.api.terrain.TerrainManager;

public class InterestingMobs extends Sensor<LivingEntity>
{
    private static final EntityPredicate VISIBLE = new EntityPredicate().range(16.0D).allowSameTeam()
            .allowNonAttackable().allowInvulnerable();

    public static boolean canPokemobMate(final IPokemob pokemob)
    {
        if (!pokemob.getPokedexEntry().breeds) return false;
        if (pokemob.getPokedexEntry().isMega()) return false;
        if (pokemob.getPokedexEntry().isGMax()) return false;
        if (pokemob.getPokedexEntry().isLegendary() && !PokecubeCore.getConfig().legendsBreed) return false;
        if (!pokemob.isRoutineEnabled(AIRoutine.MATE)) return false;
        if (!pokemob.canBreed()) return false;
        if (pokemob.getCombatState(CombatStates.ANGRY) || BrainUtils.hasAttackTarget(pokemob.getEntity())) return false;
        return true;
    }

    long lastUpdate = 0;

    private boolean isValid(final AgeableEntity entityIn, final AgeableEntity otherAnimal)
    {
        final IBreedingMob us = BreedableCaps.getBreedable(entityIn);
        if (entityIn == otherAnimal) return false;
        final IPokemob other = CapabilityPokemob.getPokemobFor(otherAnimal);
        if (other != null && !InterestingMobs.canPokemobMate(other)) return false;
        if (us != null) return us.canMate(otherAnimal);
        return false;
    }

    @Override
    protected void doTick(final ServerWorld worldIn, final LivingEntity entityIn)
    {
        final double s = 16;
        if (!TerrainManager.isAreaLoaded(entityIn.getCommandSenderWorld(), entityIn.blockPosition(), 8 + s)) return;

        if (RootTask.doLoadThrottling)
        {
            final Random rng = new Random(entityIn.getUUID().hashCode());
            final int tick = rng.nextInt(RootTask.runRate);
            if (entityIn.tickCount % RootTask.runRate != tick) return;
        }

        final List<AgeableEntity> mates = Lists.newArrayList();
        final List<ItemEntity> items = Lists.newArrayList();
        final List<LivingEntity> mobs = Lists.newArrayList();
        final List<LivingEntity> visible = Lists.newArrayList();
        final double dh = 8;
        final double dv = 4;
        final AxisAlignedBB mateBox = entityIn.getBoundingBox().inflate(dh, dv, dh);
        final AxisAlignedBB checkBox = entityIn.getBoundingBox().inflate(s, s, s);
        final List<Entity> list = worldIn.getEntitiesOfClass(Entity.class, checkBox, (hit) ->
        {
            return hit != entityIn && hit.isAlive() && (hit instanceof LivingEntity || hit instanceof ItemEntity);
        });
        list.sort(Comparator.comparingDouble(entityIn::distanceToSqr));
        final Brain<?> brain = entityIn.getBrain();
        final IPokemob us = CapabilityPokemob.getPokemobFor(entityIn);
        final boolean canMate = entityIn instanceof AgeableEntity && (us == null || InterestingMobs.canPokemobMate(us));
        for (final Entity e : list)
            if (e instanceof LivingEntity)
            {
                final LivingEntity living = (LivingEntity) e;
                final boolean canSee = InterestingMobs.VISIBLE.test(entityIn, living);
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
        else brain.eraseMemory(MemoryModules.POSSIBLE_MATES);
        if (!visible.isEmpty()) brain.setMemory(MemoryModuleType.VISIBLE_LIVING_ENTITIES, visible);
        else brain.eraseMemory(MemoryModuleType.VISIBLE_LIVING_ENTITIES);
        if (!mobs.isEmpty()) brain.setMemory(MemoryModuleType.LIVING_ENTITIES, mobs);
        else brain.eraseMemory(MemoryModuleType.LIVING_ENTITIES);
        if (!items.isEmpty()) brain.setMemory(MemoryModules.VISIBLE_ITEMS, items);
        else brain.eraseMemory(MemoryModules.VISIBLE_ITEMS);
    }

    @Override
    public Set<MemoryModuleType<?>> requires()
    {
        return ImmutableSet.of(MemoryModuleType.LIVING_ENTITIES, MemoryModules.POSSIBLE_MATES,
                MemoryModules.HERD_MEMBERS, MemoryModuleType.VISIBLE_LIVING_ENTITIES, MemoryModules.VISIBLE_ITEMS);
    }

}
