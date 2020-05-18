package pokecube.core.ai.tasks.combat;

import java.util.Random;

import org.apache.logging.log4j.Level;

import net.minecraft.entity.Entity;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import pokecube.core.PokecubeCore;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.PokecubeMod;
import pokecube.core.interfaces.pokemob.ai.CombatStates;
import thut.api.entity.ai.IAICombat;
import thut.api.maths.Vector3;

public class AILeap extends FightTask implements IAICombat
{
    Entity  target;
    int     leapCooldown = 10;
    double  leapSpeed    = 1;
    double  movementSpeed;
    Vector3 leapTarget   = null;
    Vector3 leapOrigin   = null;

    public AILeap(final IPokemob mob)
    {
        super(mob);
        this.movementSpeed = this.entity.getAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).getValue() * 1.8;
        this.setMutex(0);
    }

    /**
     * Gets a random sound to play on leaping, selects from the options in
     * config.
     */
    private SoundEvent getLeapSound()
    {
        if (PokecubeCore.getConfig().leaps.length == 1) return PokecubeCore.getConfig().leaps[0];
        return PokecubeCore.getConfig().leaps[new Random().nextInt(PokecubeCore.getConfig().leaps.length)];
    }

    @Override
    public void reset()
    {
        this.target = null;
        this.leapTarget = null;
        this.leapOrigin = null;
    }

    @Override
    public void run()
    {

        // Target loc could just be a position
        this.leapTarget = this.target != null ? Vector3.getNewVector().set(this.target) : this.pokemob.getTargetPos();
        final Vector3 location = Vector3.getNewVector().set(this.entity);
        final Vector3 dir = this.leapTarget.subtract(location);

        /* Don't leap up if too far. */
        if (dir.y > 5) return;

        final double dist = dir.x * dir.x + dir.z * dir.z;
        float diff = this.entity.getWidth() + (this.target == null ? 0 : this.target.getWidth());
        diff = diff * diff;

        // Wait till it is a bit closer than this...
        if (dist >= 16.0D) return;
        if (dist <= diff)
        {
            this.pokemob.setCombatState(CombatStates.LEAPING, false);
            this.leapCooldown = PokecubeCore.getConfig().attackCooldown / 2;
            return;
        }

        dir.norm();
        dir.scalarMultBy(this.leapSpeed * PokecubeCore.getConfig().leapSpeedFactor);
        if (dir.isNaN())
        {
            new Exception().printStackTrace();
            dir.clear();
        }

        if (PokecubeMod.debug) PokecubeCore.LOGGER.log(Level.INFO, "Leap: " + this.entity + " " + dir.mag());

        // Compute differences in velocities, and then account for that during
        // the leap.
        final Vector3 v_a = Vector3.getNewVector().setToVelocity(this.entity);
        final Vector3 v_t = Vector3.getNewVector();
        if (this.target != null) v_t.setToVelocity(this.target);
        // Compute velocity differential.
        final Vector3 dv = v_a.subtractFrom(v_t);
        // Adjust for existing velocity differential.
        dir.subtractFrom(dv);
        /*
         * Apply the leap
         */
        dir.addVelocities(this.entity);
        // Only play sound once.
        if (this.leapCooldown == -1) this.toRun.add(new PlaySound(this.entity.dimension, Vector3.getNewVector().set(
                this.entity), this.getLeapSound(), SoundCategory.HOSTILE, 1, 1));
    }

    @Override
    public boolean shouldRun()
    {
        if (!this.canMove()) return false;

        return this.leapCooldown-- < 0 && this.pokemob.getCombatState(CombatStates.LEAPING)
                && ((this.target = this.entity.getAttackTarget()) != null || this.pokemob.getTargetPos() != null);
    }

}
