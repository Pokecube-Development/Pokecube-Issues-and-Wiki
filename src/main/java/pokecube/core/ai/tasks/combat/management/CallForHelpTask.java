package pokecube.core.ai.tasks.combat.management;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import pokecube.api.entity.TeamManager;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.entity.pokemob.PokemobCaps;
import pokecube.api.moves.Battle;
import pokecube.core.ai.brain.BrainUtils;
import pokecube.core.ai.tasks.combat.CombatTask;

public class CallForHelpTask extends CombatTask
{
    boolean checked = false;

    final float chance;

    public CallForHelpTask(final IPokemob pokemob, final float chance)
    {
        super(pokemob);
        this.chance = chance;
    }

    @Override
    public void reset()
    {
        this.checked = false;
        this.target = null;
    }

    private boolean shouldCallForHelp(LivingEntity from)
    {
        // No need to get help against null
        if (from == null || !this.entity.getBrain().hasMemoryValue(MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES))
            return false;

        // Not social. doesn't do this.
        if (!this.pokemob.getPokedexEntry().isSocial) return false;

        // If it has not hurt us, don't call for help
        if (this.entity.getLastHurtByMob() != from) return false;

        return true;
    }

    /**
     * Check if there are any mobs nearby that will help us. <br>
     * <br>
     * This is called from {@link FindTargetsTask#shouldRun()}
     *
     * @return someone needed help.
     */
    protected boolean checkForHelp(final LivingEntity from)
    {
        final List<LivingEntity> ret = new ArrayList<>();

        // We check for whether it is the same species and, has the same owner
        // (including null) or is on the team.
        final Predicate<LivingEntity> relationCheck = input -> {
            final IPokemob other = PokemobCaps.getPokemobFor(input);
            // No pokemob, no helps.
            if (other == null) return false;
            // Not related, no helps.
            if (!other.getPokedexEntry().areRelated(this.pokemob.getPokedexEntry())) return false;
            // both wild, helps.
            if (other.getOwnerId() == null && this.pokemob.getOwnerId() == null) return true;
            // Same team, helps.
            if (TeamManager.sameTeam(input, this.entity)) return true;
            return false;
        };
        // Only allow valid guard targets.
        final Iterable<LivingEntity> pokemobs = this.entity.getBrain()
                .getMemory(MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES).get().findAll(relationCheck);

        pokemobs.forEach(o -> {
            if (relationCheck.test(o)) ret.add(o);
        });

        for (final LivingEntity living : ret)
        {
            if (!(living instanceof Mob mob)) continue;
            // Only agress mobs that can see you are really under attack.
            if (!mob.hasLineOfSight(this.entity)) continue;
            // Only agress if not currently in combat.
            if (BrainUtils.hasAttackTarget(mob)) continue;
            // Make all valid ones agress the target.
            Battle.createOrAddToBattle(mob, from);
        }
        return false;
    }

    @Override
    public void run()
    {
        if (this.checked) return;
        if (!shouldCallForHelp(target)) return;
        this.checked = true;
        if (Math.random() < this.chance) return;
        this.checkForHelp(this.target);
    }

    @Override
    public boolean shouldRun()
    {
        this.checkAttackTarget();
        return this.target != null
                && this.entity.getBrain().hasMemoryValue(MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES);
    }

}
