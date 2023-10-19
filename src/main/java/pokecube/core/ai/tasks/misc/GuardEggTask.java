package pokecube.core.ai.tasks.misc;

import java.util.Map;
import java.util.Optional;
import java.util.Random;

import com.google.common.collect.Maps;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.moves.Battle;
import pokecube.core.ai.brain.BrainUtils;
import pokecube.core.ai.brain.MemoryModules;
import pokecube.core.ai.tasks.TaskBase;
import pokecube.core.items.pokemobeggs.EntityPokemobEgg;
import thut.core.common.ThutCore;

/**
 * This IAIRunnable results in the mother of an egg always staying within 4
 * blocks of it. It also prevents the mother from breeding, as well as prevents
 * the mother's breeding cooldown from dropping while an egg is being guarded.
 */
public class GuardEggTask extends TaskBase
{
    private static final Map<MemoryModuleType<?>, MemoryStatus> _MEMS = Maps.newHashMap();

    private static final Map<MemoryModuleType<?>, MemoryStatus> _getMems()
    {
        if (_MEMS.isEmpty())
        {
            // Only run when we have an egg
            _MEMS.put(MemoryModules.EGG.get(), MemoryStatus.VALUE_PRESENT);
        }
        return _MEMS;
    }

    EntityPokemobEgg egg = null;

    public GuardEggTask(final IPokemob mob)
    {
        super(mob, _getMems());
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
        double eggDist = this.entity.distanceToSqr(this.egg);
        // No breeding while guarding egg.
        this.pokemob.resetLoveStatus();
        // If too close to egg, don't bother moving.
        if (eggDist < 4) return;
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

        var enemy = BrainUtils.getAttackTarget(entity);
        if (enemy != null)
        {
            // guarding things are respected, but we will play annoyed
            // particles.
            if (enemy.getLevel() instanceof ServerLevel level)
            {
                double size = pokemob.getMobSizes().mag();
                double x = this.entity.getX();
                double y = this.entity.getY();
                double z = this.entity.getZ();

                Random r = ThutCore.newRandom();
                for (int l = 0; l < 5; l++)
                {
                    double i = r.nextGaussian() * size;
                    double j = r.nextGaussian() * size;
                    double k = r.nextGaussian() * size;
                    level.sendParticles(ParticleTypes.ANGRY_VILLAGER, x + i, y + j, z + k, 1, 0, 0, 0, 0);
                }
            }
            Battle b = Battle.getBattle(entity);
            if (b != null) b.removeFromBattle(entity);
            BrainUtils.deagro(this.pokemob.getEntity());
        }

        // Only run if we have a live egg to watch.
        if (this.egg != null) return this.egg.isAlive() ? true : false;
        return false;
    }

}
