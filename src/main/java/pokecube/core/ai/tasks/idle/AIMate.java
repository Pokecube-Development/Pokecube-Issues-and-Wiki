package pokecube.core.ai.tasks.idle;

import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import net.minecraft.entity.AgeableEntity;
import net.minecraft.entity.ai.brain.memory.MemoryModuleStatus;
import net.minecraft.entity.ai.brain.memory.MemoryModuleType;
import net.minecraft.entity.ai.brain.memory.WalkTarget;
import net.minecraft.util.math.EntityPosWrapper;
import pokecube.core.PokecubeCore;
import pokecube.core.ai.brain.BrainUtils;
import pokecube.core.ai.brain.MemoryModules;
import pokecube.core.ai.tasks.combat.AIFindTarget;
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
public class AIMate extends IdleTask
{
    private static final Map<MemoryModuleType<?>, MemoryModuleStatus> mems = Maps.newHashMap();
    static
    {
        // only run this if we have mate targets.
        AIMate.mems.put(MemoryModules.POSSIBLE_MATES, MemoryModuleStatus.VALUE_PRESENT);
    }

    int spawnBabyDelay = 0;

    List<AgeableEntity> mates = Lists.newArrayList();

    AgeableEntity mate;

    public AIMate(final IPokemob mob)
    {
        super(mob, AIMate.mems);
    }

    @Override
    public void reset()
    {
        this.spawnBabyDelay = 0;
        this.mate = null;
        this.pokemob.setGeneralState(GeneralStates.MATING, false);
        BrainUtils.setMateTarget((AgeableEntity) this.entity, null);
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

        // Battle between the first two on the list.
        final AgeableEntity mobA = this.mates.get(0);
        final AgeableEntity mobB = this.mates.get(1);

        final IPokemob pokeA = CapabilityPokemob.getPokemobFor(mobA);
        final IPokemob pokeB = CapabilityPokemob.getPokemobFor(mobB);

        if (pokeA != null) pokeA.setCombatState(CombatStates.MATEFIGHT, true);
        if (pokeB != null) pokeB.setCombatState(CombatStates.MATEFIGHT, true);

        // This fight should end when one gets below half health, which would
        // then be invalid for the next selection round of mating targets.
        AIFindTarget.initiateCombat(mobA, mobB);
    }

    @Override
    public boolean shouldRun()
    {
        if (!this.pokemob.getPokedexEntry().breeds) return false;
        if (this.pokemob.getPokedexEntry().isLegendary() && !PokecubeCore.getConfig().legendsBreed) return false;
        if (!this.pokemob.isRoutineEnabled(AIRoutine.MATE)) return false;
        if (this.pokemob.getSexe() == IPokemob.MALE || !this.pokemob.canBreed()) return false;
        if (this.pokemob.getCombatState(CombatStates.ANGRY) || BrainUtils.hasAttackTarget(this.entity)) return false;
        this.mate = BrainUtils.getMateTarget((AgeableEntity) this.entity);
        if (this.mate != null) return true;
        this.mates = BrainUtils.getMates((AgeableEntity) this.entity);
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
        this.setWalkTo(this.mate, this.pokemob.getMovementSpeed(), 0);
        this.mate.getBrain().setMemory(MemoryModules.WALK_TARGET, new WalkTarget(new EntityPosWrapper(this.entity),
                (float) this.pokemob.getMovementSpeed(), 0));

        this.pokemob.setGeneralState(GeneralStates.MATING, true);
        if (this.spawnBabyDelay++ < 50) return;
        final IPokemob other = CapabilityPokemob.getPokemobFor(this.mate);
        if (other != null) this.pokemob.mateWith(other);
        this.reset();
        other.resetLoveStatus();
        this.pokemob.resetLoveStatus();
    }
}
