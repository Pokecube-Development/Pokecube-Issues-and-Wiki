package pokecube.core.ai.tasks.idle;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.behavior.EntityTracker;
import net.minecraft.world.entity.ai.behavior.PositionTracker;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.entity.pokemob.PokemobCaps;
import pokecube.api.entity.pokemob.ai.CombatStates;
import pokecube.api.entity.pokemob.ai.GeneralStates;
import pokecube.api.moves.Battle;
import pokecube.core.PokecubeCore;
import pokecube.core.ai.brain.BrainUtils;
import pokecube.core.ai.brain.MemoryModules;
import pokecube.core.ai.brain.sensors.InterestingMobs;
import thut.api.entity.IBreedingMob;
import thut.api.entity.ai.PosWrapWrap;

import java.util.List;
import java.util.Map;

/**
 * This IAIRunnable is responsible for most of the breeding AI for the pokemobs. It finds the mates, initiates the
 * fighting over a mate (if applicable), then tells the mobs to breed if they should.
 */
public class MateTask extends BaseIdleTask
{
    private static final Map<MemoryModuleType<?>, MemoryStatus> mems = Maps.newHashMap();

    static
    {
        // only run this if we have mate targets.
        MateTask.mems.put(MemoryModules.POSSIBLE_MATES.get(), MemoryStatus.VALUE_PRESENT);
    }

    int spawnBabyDelay = 0;

    List<AgeableMob> mates = Lists.newArrayList();

    AgeableMob mate;

    AgeableMob mobA = null;
    AgeableMob mobB = null;

    WalkTarget startSpot = null;

    public MateTask()
    {
        super(MateTask.mems);
    }

    @Override
    public void reset(Mob entity)
    {
        this.spawnBabyDelay = -1;
        this.mate = null;
        this.mobA = null;
        this.mobB = null;
        this.startSpot = null;
        BrainUtils.setMateTarget((AgeableMob) entity, null);
    }

    @Override
    protected void tick(final ServerLevel level, final Mob entity, final long gameTime)
    {
        IPokemob pokemob = PokemobCaps.getPokemobFor(entity);
        pre:
        {
            // already have a mate, lets return early from this
            if (this.mate != null) break pre;
            // No options, return.
            if (this.mates == null || this.mates.isEmpty()) break pre;

            // Only one mate, we can choose it
            if (this.mates.size() == 1)
            {
                this.mate = this.mates.getFirst();
                break pre;
            }
            if (this.startSpot != null) this.setWalkTo(entity, this.startSpot);

            if (this.mobA != null && this.mobB != null && this.mates.contains(this.mobA) && this.mates.contains(
                    this.mobB)) break pre;

            // Flag them all as valid mates
            for (final AgeableMob mob : this.mates) BrainUtils.setMateTarget(mob, (AgeableMob) entity);

            // Battle between the first two on the list.
            this.mobA = this.mates.get(0);
            this.mobB = this.mates.get(1);

            final IPokemob pokeA = PokemobCaps.getPokemobFor(this.mobA);
            final IPokemob pokeB = PokemobCaps.getPokemobFor(this.mobB);

            if (pokeA != null) pokeA.setCombatState(CombatStates.MATEFIGHT, true);
            if (pokeB != null) pokeB.setCombatState(CombatStates.MATEFIGHT, true);

            // This fight should end when one gets below half health, which would
            // then be invalid for the next selection round of mating targets.
            Battle.createOrAddToBattle(this.mobA, this.mobB);

            this.startSpot = new WalkTarget(entity.position(), 1, 0);
        }

        // No chosen mate, return here
        if (this.mate == null) return;

        // Make them walk to each other
        this.approachEachOther(entity, this.mate, 1);

        BrainUtils.setMateTarget((AgeableMob) entity, this.mate);
        BrainUtils.setMateTarget(this.mate, (AgeableMob) entity);

        pokemob.setGeneralState(GeneralStates.MATING, true);
        final IPokemob other = PokemobCaps.getPokemobFor(this.mate);
        if (other != null) other.setGeneralState(GeneralStates.MATING, true);
        if (this.spawnBabyDelay <= 0) this.spawnBabyDelay = entity.tickCount + 100;
        if (this.spawnBabyDelay > entity.tickCount) return;
        if (other instanceof IBreedingMob mate) pokemob.mateWith(mate);
        this.reset(entity);
        other.resetLoveStatus();
        pokemob.resetLoveStatus();
    }

    @Override
    public boolean shouldRun(Mob entity)
    {
        var pokemob = PokemobCaps.getPokemobFor(entity);
        if (!InterestingMobs.canPokemobMate(pokemob)) return false;
        // This AI is only run on the female side.
        if (pokemob.getSexe() == IPokemob.MALE) return false;
        this.mate = BrainUtils.getMateTarget((AgeableMob) entity);
        if (this.mate != null && !this.mate.isAlive())
        {
            BrainUtils.setMateTarget((AgeableMob) entity, null);
            this.mate = null;
        }
        if (this.mate != null) return true;
        this.mates = BrainUtils.getMates((AgeableMob) entity);
        if (this.mates != null)
        {
            double mateNum = PokecubeCore.getConfig().mobSpawnNumber;
            mateNum *= pokemob.isPlayerOwned()
                    ? PokecubeCore.getConfig().mateDensityPlayer
                    : PokecubeCore.getConfig().mateDensityWild;
            this.mates.removeIf(e -> !e.isAlive());
            if (this.mates.size() > mateNum) return false;
        }
        return this.mates != null;
    }

    void approachEachOther(final LivingEntity firstEntity, final LivingEntity secondEntity, final float speed)
    {
        this.approach(firstEntity, secondEntity, speed);
        this.approach(secondEntity, firstEntity, speed);
    }

    void approach(final LivingEntity living, final LivingEntity target, final float speed)
    {
        final PositionTracker entityposwrapper = new PosWrapWrap(new EntityTracker(target, false), this.loadThrottle());
        final WalkTarget walktarget = new WalkTarget(entityposwrapper, speed, 0);
        living.getBrain().setMemory(MemoryModuleType.LOOK_TARGET, entityposwrapper);
        living.getBrain().setMemory(MemoryModuleType.WALK_TARGET, walktarget);
    }
}
