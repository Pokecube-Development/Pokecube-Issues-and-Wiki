package pokecube.core.ai.tasks.combat.movement;

import java.util.Map;

import com.google.common.collect.Maps;

import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.level.pathfinder.Node;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.entity.pokemob.ai.CombatStates;
import pokecube.api.moves.Battle;
import pokecube.api.moves.MoveEntry;
import pokecube.core.PokecubeCore;
import pokecube.core.ai.brain.BrainUtils;
import pokecube.core.ai.brain.MemoryModules;
import pokecube.core.ai.tasks.TaskBase;
import pokecube.core.ai.tasks.combat.CombatTask;
import thut.api.entity.ai.IAICombat;
import thut.api.maths.Vector3;

/**
 * This IAIRunnable manages the movement of the mob while it is in combat, but
 * on cooldown between attacks. It also manages the leaping at targets, and the
 * dodging of attacks.
 */
public class CicleTask extends CombatTask implements IAICombat
{
    private static final Map<MemoryModuleType<?>, MemoryStatus> MEMS = Maps.newHashMap();

    static
    {
        CicleTask.MEMS.put(MemoryModules.PATH, MemoryStatus.VALUE_ABSENT);
    }

    Vector3 centre;
    double movementSpeed;

    public CicleTask(final IPokemob mob)
    {
        super(mob, CicleTask.MEMS);
        this.centre = null;
        this.movementSpeed = 1.5f;
    }

    protected void calculateCentre()
    {
        Battle b = pokemob.getBattle();
        if (b != null) this.centre = b.getCentre();
        if (this.centre == null)
        {
            final Vector3 targetLoc = new Vector3().set(this.target);
            final Vector3 attackerLoc = new Vector3().set(this.entity);
            final Vector3 diff = targetLoc.addTo(attackerLoc).scalarMultBy(0.5);
            this.centre = diff;
            this.centre.y = Math.min(attackerLoc.y, targetLoc.y);
        }
    }

    @Override
    public void checkAttackTarget()
    {
        var target = this.target;
        super.checkAttackTarget();
        if (target != this.target) this.reset();
    }

    @Override
    public void reset()
    {
        if (this.target == null) this.centre = null;
    }

    @Override
    public void run()
    {
        // Figure out where centre of combat is
        this.calculateCentre();
        Node point = null;
        // If the mob has a path already, check if it is near the end, if not,
        // return early, getFinalPathPoint() is nullable!
        if (!this.entity.getNavigation().isDone()
                && (point = this.entity.getNavigation().getPath().getEndNode()) != null)
        {
            final Vector3 end = new Vector3().set(point);
            final Vector3 here = new Vector3().set(this.entity);
            float f = this.entity.getBbWidth();
            f = Math.max(f, 0.5f);
            if (here.distTo(end) > f) return;
        }

        // Check if we can see the target, if not, try pathing directly to it.
        if (!BrainUtils.canSee(entity, target))
        {
            this.setWalkTo(this.centre, this.movementSpeed, 0);
            return;
        }

        MoveEntry attack = this.pokemob.getSelectedMove();

        final Vector3 here = new Vector3().set(this.entity);
        boolean meleeCombat = !attack.isRanged(this.pokemob) && !this.pokemob.getMoveStats().targettingSelf;
        // melee mobs will instead try to be closer to the target, instead of
        // centre of battlefield
        if (meleeCombat) here.set(target);

        final Vector3 diff = here.subtract(this.centre);
        if (diff.magSq() < 1) diff.norm();
        int combatDistance = PokecubeCore.getConfig().combatDistance;

        // If we are using a melee move, try to stay closer to the target!
        if (meleeCombat)
        {
            combatDistance = Math.min(combatDistance, 1);
            meleeCombat = true;
        }
        combatDistance = Math.max(combatDistance, 1);

        final int combatDistanceSq = combatDistance * combatDistance;
        // If the mob has left the combat radius, try to return to the centre of
        // combat. Otherwise, find a random spot in a consistant direction
        // related to the center to run in, this results in the mobs somewhat
        // circling the middle, and reversing direction every 10 seconds or so.
        if (diff.magSq() > combatDistanceSq)
        {
            if (meleeCombat) this.setWalkTo(this.target, this.movementSpeed, 0);
            else this.setWalkTo(this.centre, this.movementSpeed, 0);
        }
        else
        {
            final Vector3 perp = diff.horizonalPerp().scalarMultBy(combatDistance);
            final int revTime = 200;
            if (this.entity.tickCount % revTime > revTime / 2) perp.reverse();
            perp.addTo(here);
            if (Math.abs(perp.y - this.centre.y) > combatDistance / 2) perp.y = this.centre.y;
            this.setWalkTo(perp, this.movementSpeed, 0);
        }
    }

    @Override
    public boolean shouldRun()
    {
        // Marked as unable to move, so skip
        if (!TaskBase.canMove(this.pokemob)) return false;
        // Update if we have a target
        this.checkAttackTarget();
        // No target, so skip
        if (target == null) return false;
        // Using an attack, so skip
        if (this.pokemob.getCombatState(CombatStates.EXECUTINGMOVE)) return false;
        // Is in battle.
        return this.pokemob.getCombatState(CombatStates.BATTLING);
    }
}