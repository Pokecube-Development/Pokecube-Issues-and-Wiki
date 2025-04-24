package pokecube.core.ai.tasks.combat.management;

import com.google.common.collect.Maps;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import pokecube.api.entity.TeamManager;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.entity.pokemob.PokemobCaps;
import pokecube.api.moves.Battle;
import pokecube.core.ai.brain.BrainUtils;
import pokecube.core.ai.brain.MemoryModules;
import pokecube.core.ai.tasks.combat.CombatTask;
import thut.api.entity.ai.IAIRunnable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

public class CallForHelpTask extends CombatTask
{
    private static final Map<MemoryModuleType<?>, MemoryStatus> MEMS = Maps.newHashMap();

    static
    {
        MEMS.put(MemoryModules.CALLED_HELP.get(), MemoryStatus.REGISTERED);
    }

    public final float chance;

    public CallForHelpTask(float chance)
    {
        super(MEMS);
        this.chance = chance;
    }

    @Override
    public void reset(Mob entityIn)
    {
        entityIn.getBrain().eraseMemory(MemoryModules.CALLED_HELP.get());
    }

    private boolean shouldCallForHelp(IPokemob pokemob, LivingEntity from)
    {
        var entity = pokemob.getEntity();
        // No need to get help against null
        if (from == null || !entity.getBrain().hasMemoryValue(MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES))
            return false;

        // Not social. doesn't do this.
        if (!pokemob.getPokedexEntry().isSocial) return false;

        // If it has not hurt us, don't call for help
        return entity.getLastHurtByMob() == from;
    }

    /**
     * Check if there are any mobs nearby that will help us. <br>
     * <br>
     * This is called from {@link IAIRunnable#shouldRun(Mob)}
     */
    protected void checkForHelp(IPokemob pokemob, final LivingEntity from)
    {
        final List<LivingEntity> ret = new ArrayList<>();

        var entity = pokemob.getEntity();
        // We check for whether it is the same species and, has the same owner
        // (including null) or is on the team.
        final Predicate<LivingEntity> relationCheck = input -> {
            final IPokemob other = PokemobCaps.getPokemobFor(input);
            // No pokemob, no helps.
            if (other == null) return false;
            // Not related, no helps.
            if (!other.getPokedexEntry().areRelated(pokemob.getPokedexEntry())) return false;
            // both wild, helps.
            if (other.getOwnerId() == null && pokemob.getOwnerId() == null) return true;
            // Same team, helps.
            return TeamManager.sameTeam(input, entity);
        };
        // Only allow valid guard targets.
        final Iterable<LivingEntity> pokemobs = entity.getBrain()
                .getMemory(MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES).get().findAll(relationCheck);

        pokemobs.forEach(o -> {
            if (relationCheck.test(o)) ret.add(o);
        });

        for (final LivingEntity living : ret)
        {
            if (!(living instanceof Mob mob)) continue;
            // Only agress mobs that can see you are really under attack.
            if (!mob.hasLineOfSight(entity)) continue;
            // Only agress if not currently in combat.
            if (BrainUtils.hasAttackTarget(mob)) continue;
            // Make all valid ones agress the target.
            Battle.createOrAddToBattle(mob, from);
        }
    }

    @Override
    public void run(ServerLevel level, Mob owner)
    {
        var brain = owner.getBrain();
        if (brain.getMemory(MemoryModules.CALLED_HELP.get()).orElse(false)) return;
        var target = this.getAttackTarget(owner);
        var pokemob = PokemobCaps.getPokemobFor(owner);
        if (!shouldCallForHelp(pokemob, target)) return;
        brain.setMemory(MemoryModules.CALLED_HELP.get(), true);
        if (Math.random() < this.chance) return;
        this.checkForHelp(pokemob, target);
    }

    @Override
    public boolean shouldRun(Mob entityIn)
    {
        var target = this.getAttackTarget(entityIn);
        return target != null && entityIn.getBrain().hasMemoryValue(MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES);
    }

}
