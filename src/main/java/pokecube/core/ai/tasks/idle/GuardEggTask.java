package pokecube.core.ai.tasks.idle;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import pokecube.core.PokecubeCore;
import pokecube.core.ai.brain.BrainUtils;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.items.pokemobeggs.EntityPokemobEgg;

/**
 * This IAIRunnable results in the mother of an egg always staying within 4
 * blocks of it. It also prevents the mother from breeding, as well as prevents
 * the mother's breeding cooldown from dropping while an egg is being
 * guarded.
 */
public class GuardEggTask extends BaseIdleTask
{
    public static int PATHCOOLDOWN   = 50;
    public static int SEARCHCOOLDOWN = 50;

    EntityPokemobEgg egg = null;

    int eggSearchCooldown = 0;
    int eggPathCooldown   = 0;

    public GuardEggTask(final IPokemob mob)
    {
        super(mob);
    }

    @Override
    public void reset()
    {
        this.egg = null;
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
        egg:
        if (this.egg != null)
        {
            if (!this.egg.isAlive())
            {
                this.egg = null;
                break egg;
            }
            return true;
        }
        if (!this.entity.getBrain().hasMemoryValue(MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES)) return false;

        if (this.eggSearchCooldown-- > 0) return false;
        // Only the female (or neutral) will guard the eggs.
        if (this.pokemob.getSexe() == IPokemob.MALE) return false;
        this.eggSearchCooldown = GuardEggTask.SEARCHCOOLDOWN;

        final List<LivingEntity> list = new ArrayList<>();
        final List<LivingEntity> pokemobs = this.entity.getBrain().getMemory(MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES).get();
        list.addAll(pokemobs);
        final Predicate<LivingEntity> isEgg = input -> input instanceof EntityPokemobEgg && GuardEggTask.this.entity
                .getUUID().equals(((EntityPokemobEgg) input).getMotherId()) && input.isAlive();
        list.removeIf(e -> !isEgg.test(e));
        list.removeIf(e -> e.distanceTo(this.entity) > PokecubeCore.getConfig().guardSearchDistance);
        if (list.isEmpty()) return false;
        // Select first egg found to guard, remove target, set not angry

        this.egg = (EntityPokemobEgg) list.get(0);
        this.egg.mother = this.pokemob;
        BrainUtils.deagro(this.pokemob.getEntity());

        // Only run if we have a live egg to watch.
        if (this.egg != null) return this.egg.isAlive() ? true : false;
        return false;
    }

}
