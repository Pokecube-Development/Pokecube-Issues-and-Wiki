package pokecube.core.ai.tasks.misc;

import java.util.Map;
import java.util.Optional;

import com.google.common.collect.Maps;

import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.core.ai.brain.BrainUtils;
import pokecube.core.ai.brain.MemoryModules;
import pokecube.core.ai.tasks.TaskBase;
import pokecube.core.items.pokemobeggs.EntityPokemobEgg;

/**
 * This IAIRunnable results in the mother of an egg always staying within 4
 * blocks of it. It also prevents the mother from breeding, as well as prevents
 * the mother's breeding cooldown from dropping while an egg is being guarded.
 */
public class GuardEggTask extends TaskBase
{
    private static final Map<MemoryModuleType<?>, MemoryStatus> MEMS = Maps.newHashMap();

    static
    {
        MEMS.put(MemoryModules.EGG.get(), MemoryStatus.VALUE_PRESENT);
    }

    public static int PATHCOOLDOWN = 50;
    public static int SEARCHCOOLDOWN = 50;

    EntityPokemobEgg egg = null;

    int eggSearchCooldown = 0;
    int eggPathCooldown = 0;

    public GuardEggTask(final IPokemob mob)
    {
        super(mob, MEMS);
    }

    @Override
    public void reset()
    {
        this.egg = null;
        entity.getBrain().eraseMemory(MemoryModules.EGG.get());
    }

    @Override
    public void run()
    {
        // No breeding while guarding egg.
        this.pokemob.resetLoveStatus();
        // If too close to egg, don't bother moving.
        if (this.entity.distanceToSqr(this.egg) < 4) return;
        // On cooldown
        if (this.eggPathCooldown-- > 0) return;
        this.eggPathCooldown = GuardEggTask.PATHCOOLDOWN;
        // Path to the egg.
        this.setWalkTo(this.egg.position(), 1, 0);
    }

    @Override
    public boolean shouldRun()
    {
        Optional<EntityPokemobEgg> eggOpt = entity.getBrain().getMemory(MemoryModules.EGG.get());
        if (!eggOpt.isPresent()) return false;
        this.egg = eggOpt.get();
        if (!this.egg.isAlive())
        {
            this.egg = null;
            return false;
        }
        if (this.egg == null) return false;
        this.egg.mother = this.pokemob;
        BrainUtils.deagro(this.pokemob.getEntity());

        // Only run if we have a live egg to watch.
        if (this.egg != null) return this.egg.isAlive() ? true : false;
        return false;
    }

}
