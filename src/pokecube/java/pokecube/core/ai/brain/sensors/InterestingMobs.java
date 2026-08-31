package pokecube.core.ai.brain.sensors;

import com.google.common.collect.ImmutableSet;
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
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.phys.AABB;
import pokecube.api.entity.TeamManager;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.entity.pokemob.PokemobCaps;
import pokecube.api.entity.pokemob.ai.AIRoutine;
import pokecube.api.entity.pokemob.ai.CombatStates;
import pokecube.api.events.combat.ValidBattleTarget;
import pokecube.core.ai.brain.BrainUtils;
import pokecube.core.ai.brain.MemoryModules;
import pokecube.core.items.pokemobeggs.EntityPokemobEgg;
import thut.api.ThutCaps;
import thut.api.entity.IBreedingMob;
import thut.api.entity.ai.RootTask;
import thut.api.level.terrain.TerrainManager;
import thut.core.common.ThutCore;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Random;
import java.util.Set;

public class InterestingMobs extends Sensor<LivingEntity>
{
    private static final TargetingConditions VISIBLE = TargetingConditions.forNonCombat().range(16.0D);

    public static boolean canPokemobMate(final IPokemob pokemob)
    {
        if (!pokemob.canBreed()) return false;
        if (!pokemob.getPokedexEntry().breeds) return false;
        if (pokemob.getPokedexEntry().isMega()) return false;
        if (!pokemob.isRoutineEnabled(AIRoutine.MATE)) return false;
        if (pokemob.getCombatState(CombatStates.MATEFIGHT)) return true;
        return !pokemob.getCombatState(CombatStates.BATTLING) && !BrainUtils.hasAttackTarget(pokemob.getEntity());
    }

    public static boolean validCombatTarget(LivingEntity user, LivingEntity target)
    {
        ValidBattleTarget event = new ValidBattleTarget(user, target);
        ThutCore.FORGE_BUS.post(event);
        return !event.isCanceled();
    }

    private boolean isValid(final AgeableMob entityIn, final AgeableMob otherAnimal, IPokemob other)
    {
        final IBreedingMob us = ThutCaps.getBreedable(entityIn);
        if (entityIn == otherAnimal) return false;
        if (other != null && !InterestingMobs.canPokemobMate(other)) return false;
        final IBreedingMob them = ThutCaps.getBreedable(otherAnimal);
        // Make the breeding check take either direction. This allows checking
        // things like ditto, etc.
        if (us != null) return us.canMate(otherAnimal) || them != null && them.canMate(entityIn);
        return false;
    }

    @Override
    protected void doTick(final ServerLevel worldIn, final LivingEntity user)
    {
        final double s = 16;
        if (!TerrainManager.isAreaLoaded(user.level(), user.blockPosition(), 8 + s)) return;

        if (RootTask.doLoadThrottling)
        {
            final Random rng = new Random(user.getUUID().hashCode());
            final int tick = rng.nextInt(RootTask.runRate);
            if (user.tickCount % RootTask.runRate != tick) return;
        }

        final List<AgeableMob> mates = new ArrayList<>();
        final List<ItemEntity> items = new ArrayList<>();
        final List<Projectile> projectiles = new ArrayList<>();
        final List<LivingEntity> mobs = new ArrayList<>();
        final List<LivingEntity> visible = new ArrayList<>();
        final List<LivingEntity> herd = new ArrayList<>();
        List<LivingEntity> combatOptions = new ArrayList<>();
        List<Player> survivalPlayers = new ArrayList<>();
        EntityPokemobEgg egg = null;
        final double dh = 8;
        final double dv = 4;
        final AABB mateBox = user.getBoundingBox().inflate(dh, dv, dh);
        final AABB checkBox = user.getBoundingBox().inflate(s, s, s);
        final List<Entity> list = worldIn.getEntitiesOfClass(Entity.class, checkBox, (hit) -> hit != user);
        list.sort(Comparator.comparingDouble(user::distanceToSqr));
        final Brain<?> brain = user.getBrain();
        final IPokemob us = PokemobCaps.getPokemobFor(user);
        final boolean canMate = user instanceof AgeableMob && (us == null || InterestingMobs.canPokemobMate(us));
        for (final Entity e : list) if (e instanceof LivingEntity living)
        {
            mobs.add(living);
            IPokemob pokemob = PokemobCaps.getPokemobFor(e);
            if (pokemob != null && us != null)
            {
                boolean bothWild = pokemob.getOwnerId() == null && us.getOwnerId() == null;
                if (us.getPokedexEntry().areRelated(pokemob.getPokedexEntry())
                        && (bothWild || TeamManager.sameTeam(user, e)))
                {
                    herd.add(living);
                }
            }
            if (living instanceof EntityPokemobEgg newEgg && user.getUUID().equals(newEgg.getMotherId()))
            {
                if (egg == null) egg = newEgg;
                else if (egg.distanceToSqr(user) > newEgg.distanceToSqr(user)) egg = newEgg;
            }
            else if (InterestingMobs.VISIBLE.test(user, living))
            {
                visible.add(living);
                if (living instanceof Player player && isEntityTargetable(user, living))
                    survivalPlayers.add(player);
                final boolean validMate = canMate && e instanceof AgeableMob mob
                        && mateBox.intersects(living.getBoundingBox())
                        && this.isValid((AgeableMob) user, mob, pokemob);
                if (validMate) mates.add((AgeableMob) living);
                if (validCombatTarget(user, living)) combatOptions.add(living);
            }
        }
        else if (e instanceof ItemEntity item) items.add(item);
        else if (e instanceof Projectile item) projectiles.add(item);
        if (!mates.isEmpty()) brain.setMemory(MemoryModules.POSSIBLE_MATES.get(), mates);
        else brain.eraseMemory(MemoryModules.POSSIBLE_MATES.get());
        if (!visible.isEmpty()) brain.setMemory(MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES,
                new NearestVisibleLivingEntities(user, visible));
        else brain.eraseMemory(MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES);
        if (!mobs.isEmpty()) brain.setMemory(MemoryModuleType.NEAREST_LIVING_ENTITIES, mobs);
        else brain.eraseMemory(MemoryModuleType.NEAREST_LIVING_ENTITIES);
        if (!items.isEmpty()) brain.setMemory(MemoryModules.VISIBLE_ITEMS.get(), items);
        else brain.eraseMemory(MemoryModules.VISIBLE_ITEMS.get());
        if (!herd.isEmpty()) brain.setMemory(MemoryModules.HERD_MEMBERS.get(), herd);
        else brain.eraseMemory(MemoryModules.HERD_MEMBERS.get());
        if (!combatOptions.isEmpty()) brain.setMemory(MemoryModules.POSSIBLE_TARGETS.get(),
                new NearestVisibleLivingEntities(user, combatOptions));
        else brain.eraseMemory(MemoryModules.POSSIBLE_TARGETS.get());
        if (!projectiles.isEmpty()) brain.setMemory(MemoryModules.VISIBLE_PROJECTILES.get(), projectiles);
        else brain.eraseMemory(MemoryModules.VISIBLE_PROJECTILES.get());
        if (!survivalPlayers.isEmpty())
            brain.setMemory(MemoryModuleType.NEAREST_VISIBLE_ATTACKABLE_PLAYER, survivalPlayers.getFirst());
        else brain.eraseMemory(MemoryModuleType.NEAREST_VISIBLE_ATTACKABLE_PLAYER);
        if (brain.checkMemory(MemoryModules.EGG.get(), MemoryStatus.REGISTERED))
        {
            if (egg != null) brain.setMemory(MemoryModules.EGG.get(), egg);
            else brain.eraseMemory(MemoryModules.EGG.get());
        }
    }

    @Override
    public Set<MemoryModuleType<?>> requires()
    {
        return ImmutableSet.of(MemoryModuleType.NEAREST_LIVING_ENTITIES,
                MemoryModuleType.NEAREST_VISIBLE_ATTACKABLE_PLAYER, MemoryModules.POSSIBLE_MATES.get(),
                MemoryModules.POSSIBLE_MATES.get(), MemoryModules.POSSIBLE_TARGETS.get(),
                MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES,
                MemoryModules.VISIBLE_ITEMS.get(), MemoryModules.VISIBLE_PROJECTILES.get());
    }

}
