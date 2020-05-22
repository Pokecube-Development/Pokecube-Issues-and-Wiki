package pokecube.core.ai.tasks.combat;

import net.minecraft.entity.Entity;
import net.minecraft.entity.SharedMonsterAttributes;
import pokecube.core.PokecubeCore;
import pokecube.core.ai.brain.BrainUtils;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.pokemob.ai.CombatStates;
import thut.api.entity.ai.IAICombat;
import thut.api.maths.Vector3;

/**
 * This IAIRunnable manages the movement of the mob while it is in combat, but
 * on cooldown between attacks. It also manages the leaping at targets, and the
 * dodging of attacks.
 */
public class CicleTask extends CombatTask implements IAICombat
{
    Entity  target;
    Vector3 centre;
    double  movementSpeed;

    public CicleTask(final IPokemob mob)
    {
        super(mob);
        this.movementSpeed = this.entity.getAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).getValue() * 1.8;
        this.centre = null;
        this.setMutex(0);
    }

    protected void calculateCentre()
    {
        if (this.centre == null)
        {
            final Vector3 targetLoc = Vector3.getNewVector().set(this.target);
            final Vector3 attackerLoc = Vector3.getNewVector().set(this.entity);
            final Vector3 diff = targetLoc.addTo(attackerLoc).scalarMultBy(0.5);
            this.centre = diff;
            this.centre.y = Math.min(attackerLoc.y, targetLoc.y);
        }
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
        // If the mob has a path already, check if it is near the end, if not,
        // return early.
        if (!this.entity.getNavigator().noPath())
        {
            final Vector3 end = Vector3.getNewVector().set(this.entity.getNavigator().getPath().getFinalPathPoint());
            final Vector3 here = Vector3.getNewVector().set(this.entity);
            float f = this.entity.getWidth();
            f = Math.max(f, 0.5f);
            if (here.distTo(end) > f) return;
        }

        final Vector3 here = Vector3.getNewVector().set(this.entity);
        final Vector3 diff = here.subtract(this.centre);
        if (diff.magSq() < 1) diff.norm();
        int combatDistance = PokecubeCore.getConfig().combatDistance;
        combatDistance = Math.max(combatDistance, 2);
        final int combatDistanceSq = combatDistance * combatDistance;
        // If the mob has left the combat radius, try to return to the centre of
        // combat. Otherwise, find a random spot in a consistant direction
        // related to the center to run in, this results in the mobs somewhat
        // circling the middle, and reversing direction every 10 seconds or so.
        if (diff.magSq() > combatDistanceSq)
        {
            this.pokemob.setCombatState(CombatStates.LEAPING, false);
            this.setWalkTo(this.centre, this.movementSpeed, 0);
        }
        else
        {
            final Vector3 perp = diff.horizonalPerp().scalarMultBy(combatDistance);
            final int revTime = 200;
            if (this.entity.ticksExisted % revTime > revTime / 2) perp.reverse();
            perp.addTo(here);
            if (Math.abs(perp.y - this.centre.y) > combatDistance / 2) perp.y = this.centre.y;
            this.setWalkTo(perp, this.movementSpeed, 0);
        }
    }

    @Override
    public boolean shouldRun()
    {
        // Has target and is angry.
        return (this.target = BrainUtils.getAttackTarget(this.entity)) != null && this.pokemob.getCombatState(
                CombatStates.ANGRY);
    }
}