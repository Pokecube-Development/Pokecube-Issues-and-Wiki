package pokecube.core.ai.tasks.idle;

import java.util.List;

import net.minecraft.entity.Entity;
import net.minecraft.pathfinding.Path;
import net.minecraft.util.math.AxisAlignedBB;
import pokecube.core.ai.tasks.AIBase;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.pokemob.ai.CombatStates;
import pokecube.core.items.pokemobeggs.EntityPokemobEgg;

/**
 * This IAIRunnable results in the mother of an egg always staying within 4
 * blocks of it. It also prevents the mother from breeding, as well as prevents
 * the mother's breeding cooldown from dropping while an egg is being
 * guarded.
 */
public class AIGuardEgg extends AIBase
{
    public static int PATHCOOLDOWN   = 50;
    public static int SEARCHCOOLDOWN = 50;

    EntityPokemobEgg egg               = null;
    int              eggSearchCooldown = 0;
    int              eggPathCooldown   = 0;

    public AIGuardEgg(final IPokemob mob)
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
        if (this.entity.getDistanceSq(this.egg) < 4) return;
        // On cooldown
        if (this.eggPathCooldown-- > 0) return;
        this.eggPathCooldown = AIGuardEgg.PATHCOOLDOWN;
        // Path to the egg.
        final Path path = this.entity.getNavigator().getPathToEntityLiving(this.egg, 0);
        this.addEntityPath(this.entity, path, this.pokemob.getMovementSpeed());
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
        if (this.eggSearchCooldown-- > 0) return false;
        // Only the female (or neutral) will guard the eggs.
        if (this.pokemob.getSexe() == IPokemob.MALE) return false;
        this.eggSearchCooldown = AIGuardEgg.SEARCHCOOLDOWN;

        if (!this.world.isAreaLoaded(this.entity.getPosition(), 18)) return false;

        final AxisAlignedBB bb = this.entity.getBoundingBox().grow(16, 8, 16);
        // Search for valid eggs.
        final List<Entity> list2 = this.entity.getEntityWorld().getEntitiesInAABBexcluding(this.entity, bb,
                input -> input instanceof EntityPokemobEgg && AIGuardEgg.this.entity.getUniqueID().equals(
                        ((EntityPokemobEgg) input).getMotherId()) && input.isAlive());
        // Select first egg found to guard, remove target, set not angry
        if (!list2.isEmpty())
        {
            this.egg = (EntityPokemobEgg) list2.get(0);
            this.egg.mother = this.pokemob;
            this.pokemob.getEntity().setAttackTarget(null);
            this.pokemob.setCombatState(CombatStates.ANGRY, false);
        }
        // Only run if we have a live egg to watch.
        if (this.egg != null) return this.egg.isAlive() ? true : false;
        return false;
    }

}
