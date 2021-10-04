package pokecube.core.ai.tasks.combat.movement;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.PositionTracker;
import pokecube.core.PokecubeCore;
import pokecube.core.ai.brain.BrainUtils;
import pokecube.core.ai.tasks.TaskBase;
import pokecube.core.ai.tasks.combat.CombatTask;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.IPokemob.Stats;
import pokecube.core.interfaces.PokecubeMod;
import pokecube.core.interfaces.pokemob.ai.CombatStates;
import pokecube.core.utils.AITools;
import thut.api.entity.ai.IAICombat;
import thut.api.maths.Vector3;
import thut.core.common.ThutCore;

public class DodgeTask extends CombatTask implements IAICombat
{

    // Location of the targetted attack
    PositionTracker pos = null;

    double dodgeSpeedFactor = 0.25f;

    int dodgeCooldown = -1;

    public DodgeTask(final IPokemob mob)
    {
        super(mob);
    }

    /**
     * Gets a random sound to play on dodging, selects from the options in
     * config.
     */
    private SoundEvent getDodgeSound()
    {
        if (PokecubeCore.getConfig().dodges.length == 1) return PokecubeCore.getConfig().dodges[0];
        return PokecubeCore.getConfig().dodges[ThutCore.newRandom().nextInt(PokecubeCore.getConfig().dodges.length)];
    }

    @Override
    public void reset()
    {
        this.dodgeCooldown = -1;
        this.pokemob.setCombatState(CombatStates.DODGING, false);
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
        /*
         * We just dodged, so return false here for now
         */
        if (this.pokemob.getCombatState(CombatStates.DODGING)) return;
        // set the dodge flag so other mobs know about this for missing
        this.pokemob.setCombatState(CombatStates.DODGING, true);
        if (PokecubeMod.debug) PokecubeCore.LOGGER.debug("Dodge: " + this.entity);
        /*
         * Compute a random perpendicular direction.
         */
        final Vector3 loc = Vector3.getNewVector().set(this.entity);
        final Vector3 target = Vector3.getNewVector().set(this.pos.currentPosition());
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

        new PlaySound(this.entity.getCommandSenderWorld().dimension(), Vector3.getNewVector().set(this.entity), this.getDodgeSound(),
                SoundSource.HOSTILE, 1, 1).run(this.world);
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
        if (!TaskBase.canMove(this.pokemob)) return false;

        // We are still preparing to dodge
        if (this.dodgeCooldown-- >= 0) return true;

        final LivingEntity target = BrainUtils.getAttackTarget(this.entity);
        // Only dodge if there is an attack target.
        if (target == null) return false;

        // Only flying or floating can dodge while in the air
        if (!AITools.canNavigate.test(this.pokemob)) return false;

        this.pos = BrainUtils.getMoveUseTarget(target);
        if (this.pos != null)
        {
            final double ds2 = this.entity.distanceToSqr(this.pos.currentPosition());
            // No need to dodge if the target isn't near us
            if (ds2 > 16) return false;
        }
        // Nothing to dodge if target isn't attacking!
        else return false;

        /*
         * Scale amount jumped by evasion stat.
         */
        final double evasionMod = this.pokemob.getFloatStat(Stats.EVASION, true) / 30d;
        final boolean dodge = Math.random() > 1 - evasionMod;
        if (dodge) this.dodgeCooldown = 10;

        return dodge;
    }
}
