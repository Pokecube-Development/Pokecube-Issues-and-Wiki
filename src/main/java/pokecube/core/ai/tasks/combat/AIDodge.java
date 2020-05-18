package pokecube.core.ai.tasks.combat;

import java.util.Random;

import org.apache.logging.log4j.Level;

import net.minecraft.entity.Entity;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import pokecube.core.PokecubeCore;
import pokecube.core.interfaces.IMoveConstants;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.IPokemob.Stats;
import pokecube.core.interfaces.Move_Base;
import pokecube.core.interfaces.PokecubeMod;
import pokecube.core.interfaces.capabilities.CapabilityPokemob;
import pokecube.core.interfaces.pokemob.ai.CombatStates;
import pokecube.core.moves.MovesUtils;
import pokecube.core.utils.AITools;
import thut.api.entity.ai.IAICombat;
import thut.api.maths.Vector3;

public class AIDodge extends FightTask implements IAICombat
{
    Entity target;
    double movementSpeed;
    double dodgeSpeedFactor = 0.25f;
    int    dodgeCooldown    = 10;

    public AIDodge(final IPokemob mob)
    {
        super(mob);
        this.movementSpeed = this.entity.getAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).getValue() * 1.8;
        this.setMutex(0);
    }

    /**
     * Gets a random sound to play on dodging, selects from the options in
     * config.
     */
    private SoundEvent getDodgeSound()
    {
        if (PokecubeCore.getConfig().dodges.length == 1) return PokecubeCore.getConfig().dodges[0];
        return PokecubeCore.getConfig().dodges[new Random().nextInt(PokecubeCore.getConfig().dodges.length)];
    }

    @Override
    public void reset()
    {
        this.target = null;
    }

    /**
     * If the mob should dodge, then make it jump in a random perpendicular
     * direction to where the current combat target is in. This should result in
     * whatever attack is incomming from missing, assuming the incomming attack
     * is dodgeable, and has a thin enough radius of effect. It also make a
     * sound when it occurs.
     */
    @Override
    public void run()
    {
        if (PokecubeMod.debug) PokecubeCore.LOGGER.log(Level.INFO, "Dodge: " + this.entity);
        /*
         * Set dodging state to notify attack AI that target is dodging.
         */
        if (!this.pokemob.getCombatState(CombatStates.DODGING))
        {
            this.pokemob.setCombatState(CombatStates.DODGING, true);
            this.dodgeCooldown = PokecubeCore.getConfig().attackCooldown;
        }
        /*
         * Compute a random perpendicular direction.
         */
        final Vector3 loc = Vector3.getNewVector().set(this.entity);
        final Vector3 target = Vector3.getNewVector().set(this.entity.getAttackTarget());
        final Vector3 temp = Vector3.getNewVector();
        Vector3 perp = target.subtractFrom(loc).rotateAboutLine(Vector3.secondAxis, Math.PI / 2, temp);
        if (Math.random() > 0.5) perp = perp.scalarMultBy(-1);
        perp = perp.normalize();
        if (perp.isNaN())
        {
            new Exception().printStackTrace();
            perp.clear();
        }

        final double evasionMod = this.pokemob.getFloatStat(Stats.EVASION, true);
        /*
         * Scale by evasion modifier
         */
        perp.scalarMultBy(evasionMod * this.dodgeSpeedFactor * PokecubeCore.getConfig().dodgeSpeedFactor);
        if (perp.magSq() > 1) perp.norm();

        /*
         * Only flying or floating things can dodge properly in the air.
         */
        if (!AITools.canNavigate.test(this.pokemob)) perp.scalarMultBy(0.2);
        /*
         * Apply the dodge
         */
        perp.addVelocities(this.entity);
        // Only play sound once.
        if (this.dodgeCooldown == -1) this.toRun.add(new PlaySound(this.entity.dimension, Vector3.getNewVector().set(
                this.entity), this.getDodgeSound(), SoundCategory.HOSTILE, 1, 1));
    }

    /**
     * Check if the mob should dodge. It checks that the mob can dodge (ie is
     * on ground if it can't float or fly), and then factors in evasion for
     * whether or not the mob should be dodging now.
     *
     * @return
     */
    @Override
    public boolean shouldRun()
    {
        if (!this.canMove()) return false;

        // We are already set to dodge, cooldown ensures dodge state lasts long
        // enough to make some ranged attacks miss
        if (this.pokemob.getCombatState(CombatStates.DODGING)) if (this.dodgeCooldown-- < 0) return true;
        // Off cooldown, reset dodge state.
        else this.pokemob.setCombatState(CombatStates.DODGING, false);
        // Only dodge if there is an attack target.
        if ((this.target = this.entity.getAttackTarget()) == null) return false;
        // Only flying or floating can dodge while in the air
        if (!AITools.canNavigate.test(this.pokemob)) return false;

        final IPokemob target = CapabilityPokemob.getPokemobFor(this.entity.getAttackTarget());
        if (target != null)
        {
            boolean shouldDodgeMove = target.getCombatState(CombatStates.EXECUTINGMOVE);
            if (shouldDodgeMove)
            {
                /*
                 * Check if the enemy is using a self move, if so, no point in
                 * trying to dodge this.
                 */
                final Move_Base move = MovesUtils.getMoveFromName(target.getMove(target.getMoveIndex()));
                if (move == null || (move.getAttackCategory() & IMoveConstants.CATEGORY_SELF) > 0)
                    shouldDodgeMove = false;
            }
            return shouldDodgeMove;
        }
        /*
         * Scale amount jumped by evasion stat.
         */
        final double evasionMod = this.pokemob.getFloatStat(Stats.EVASION, true) / 30d;
        return Math.random() > 1 - evasionMod;
    }
}
