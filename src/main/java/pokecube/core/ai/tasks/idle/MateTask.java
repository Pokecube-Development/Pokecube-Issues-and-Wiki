package pokecube.core.ai.tasks.idle;

import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import net.minecraft.entity.AgeableEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.memory.MemoryModuleStatus;
import net.minecraft.entity.ai.brain.memory.MemoryModuleType;
import net.minecraft.entity.ai.brain.memory.WalkTarget;
import net.minecraft.util.math.EntityPosWrapper;
import pokecube.core.PokecubeCore;
import pokecube.core.ai.brain.BrainUtils;
import pokecube.core.ai.brain.MemoryModules;
import pokecube.core.interfaces.IMoveConstants.AIRoutine;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.capabilities.CapabilityPokemob;
import pokecube.core.interfaces.pokemob.ai.CombatStates;
import pokecube.core.interfaces.pokemob.ai.GeneralStates;

/**
 * This IAIRunnable is responsible for most of the breeding AI for the
 * pokemobs. It finds the mates, initiates the fighting over a mate (if
 * applicable), then tells the mobs to breed if they should.
 */
public class MateTask extends BaseIdleTask
{
    private static final Map<MemoryModuleType<?>, MemoryModuleStatus> mems = Maps.newHashMap();
    static
    {
        // only run this if we have mate targets.
        MateTask.mems.put(MemoryModules.POSSIBLE_MATES, MemoryModuleStatus.VALUE_PRESENT);
    }

    int spawnBabyDelay = 0;

    List<AgeableEntity> mates = Lists.newArrayList();

    AgeableEntity mate;

    AgeableEntity mobA = null;
    AgeableEntity mobB = null;

    WalkTarget startSpot = null;

    public MateTask(final IPokemob mob)
    {
        super(mob, MateTask.mems);
    }

    @Override
    public void reset()
    {
        this.spawnBabyDelay = 0;
        this.mate = null;
        this.mobA = null;
        this.mobB = null;
        this.startSpot = null;
        BrainUtils.setMateTarget(this.entity, null);
    }

    @Override
    public void run()
    {
        // already have a mate, lets return early from this
        if (this.mate != null) return;
        // No options, return.
        if (this.mates == null || this.mates.isEmpty()) return;

        // Only one mate, we can choose it
        if (this.mates.size() == 1)
        {
            this.mate = this.mates.get(0);
            return;
        }
        if (this.startSpot != null) this.setWalkTo(this.startSpot);

        if (this.mobA != null && this.mobB != null && this.mates.contains(this.mobA) && this.mates.contains(this.mobB))
            return;

        // Flag them all as valid mates
        for (final AgeableEntity mob : this.mates)
            BrainUtils.setMateTarget(mob, this.entity);

        // Battle between the first two on the list.
        this.mobA = this.mates.get(0);
        this.mobB = this.mates.get(1);

        final IPokemob pokeA = CapabilityPokemob.getPokemobFor(this.mobA);
        final IPokemob pokeB = CapabilityPokemob.getPokemobFor(this.mobB);

        if (pokeA != null) pokeA.setCombatState(CombatStates.MATEFIGHT, true);
        if (pokeB != null) pokeB.setCombatState(CombatStates.MATEFIGHT, true);

        // This fight should end when one gets below half health, which would
        // then be invalid for the next selection round of mating targets.
        BrainUtils.initiateCombat(this.mobA, this.mobB);

        this.startSpot = new WalkTarget(this.entity.getPositionVec(), 1, 0);
    }

    @Override
    public boolean shouldRun()
    {
        if (!this.pokemob.getPokedexEntry().breeds) return false;
        if (this.pokemob.getPokedexEntry().isLegendary() && !PokecubeCore.getConfig().legendsBreed) return false;
        if (!this.pokemob.isRoutineEnabled(AIRoutine.MATE)) return false;
        if (this.pokemob.getSexe() == IPokemob.MALE || !this.pokemob.canBreed()) return false;
        if (this.pokemob.getCombatState(CombatStates.ANGRY) || BrainUtils.hasAttackTarget(this.entity)) return false;
        this.mate = BrainUtils.getMateTarget(this.entity);
        if (this.mate != null && !this.mate.isAlive())
        {
            BrainUtils.setMateTarget(this.entity, null);
            this.mate = null;
        }
        if (this.mate != null) return true;
        this.mates = BrainUtils.getMates(this.entity);
        if (this.mates != null)
        {
            int mateNum = PokecubeCore.getConfig().mobSpawnNumber;
            mateNum *= this.pokemob.isPlayerOwned() ? PokecubeCore.getConfig().mateDensityPlayer
                    : PokecubeCore.getConfig().mateDensityWild;
            this.mates.removeIf(e -> !e.isAlive());
            if (this.mates.size() > mateNum) return false;
        }
        return this.mates != null;
    }

    @Override
    public void tick()
    {
        // No chosen mate, return early
        if (this.mate == null) return;

        // Make them walk to each other
        this.approachEachOther(this.entity, this.mate, 1);

        BrainUtils.setMateTarget(this.entity, this.mate);
        BrainUtils.setMateTarget(this.mate, this.entity);

        this.pokemob.setGeneralState(GeneralStates.MATING, true);
        final IPokemob other = CapabilityPokemob.getPokemobFor(this.mate);
        if (other != null) other.setGeneralState(GeneralStates.MATING, true);
        if (this.spawnBabyDelay++ < 100) return;
        if (other != null) this.pokemob.mateWith(other);
        this.reset();
        other.resetLoveStatus();
        this.pokemob.resetLoveStatus();
    }

    void approachEachOther(final LivingEntity firstEntity, final LivingEntity secondEntity, final float speed)
    {
        this.approach(firstEntity, secondEntity, speed);
        this.approach(secondEntity, firstEntity, speed);
    }

    void approach(final LivingEntity living, final LivingEntity target, final float speed)
    {
        final EntityPosWrapper entityposwrapper = new EntityPosWrapper(target);
        final WalkTarget walktarget = new WalkTarget(entityposwrapper, speed, 0);
        living.getBrain().setMemory(MemoryModuleType.LOOK_TARGET, entityposwrapper);
        living.getBrain().setMemory(MemoryModuleType.WALK_TARGET, walktarget);
    }
}
