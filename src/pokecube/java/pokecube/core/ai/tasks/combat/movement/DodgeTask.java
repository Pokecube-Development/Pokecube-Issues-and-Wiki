package pokecube.core.ai.tasks.combat.movement;

import com.google.common.collect.Maps;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import pokecube.api.PokecubeAPI;
import pokecube.api.entity.pokemob.IPokemob.Stats;
import pokecube.api.entity.pokemob.PokemobCaps;
import pokecube.api.entity.pokemob.ai.CombatStates;
import pokecube.core.PokecubeCore;
import pokecube.core.ai.brain.BrainUtils;
import pokecube.core.ai.brain.MemoryModules;
import pokecube.core.ai.tasks.TaskBase;
import pokecube.core.ai.tasks.combat.CombatTask;
import pokecube.core.utils.AITools;
import thut.api.maths.Vector3;
import thut.core.common.ThutCore;

import java.util.Map;

public class DodgeTask extends CombatTask
{
    private static final Map<MemoryModuleType<?>, MemoryStatus> MEMS = Maps.newHashMap();

    static
    {
        MEMS.put(MemoryModules.TIMER_DODGE.get(), MemoryStatus.REGISTERED);
    }

    public DodgeTask()
    {
        super(MEMS);
    }

    /**
     * Gets a random sound to play on dodging, selects from the options in config.
     */
    private SoundEvent getDodgeSound()
    {
        if (PokecubeCore.getConfig().dodges.length == 1) return PokecubeCore.getConfig().dodges[0];
        return PokecubeCore.getConfig().dodges[ThutCore.newRandom().nextInt(PokecubeCore.getConfig().dodges.length)];
    }

    @Override
    public void reset(Mob entity)
    {
        var pokemob = PokemobCaps.getPokemobFor(entity);
        pokemob.setCombatState(CombatStates.DODGING, false);
        entity.getBrain().eraseMemory(MemoryModules.TIMER_DODGE.get());
    }

    /**
     * If the mob should dodge, then make it jump in a random perpendicular direction to where the current combat target
     * is in. This should result in whatever attack is incomming from missing, assuming the incomming attack is
     * dodgeable, and has a thin enough radius of effect. It also make a sound when it occurs.
     */
    @Override
    protected void tick(final ServerLevel level, final Mob entity, final long gameTime)
    {
        var pokemob = PokemobCaps.getPokemobFor(entity);
        /*
         * We just dodged, so return false here for now
         */
        if (pokemob.getCombatState(CombatStates.DODGING)) return;
        // set the dodge flag so other mobs know about this for missing
        pokemob.setCombatState(CombatStates.DODGING, true);
        var pos = BrainUtils.getMoveUseTarget(this.getAttackTarget(entity));
        if (pos == null)
        {
            reset(entity);
            return;
        }
        if (PokecubeCore.getConfig().debug_ai) PokecubeAPI.logInfo("Dodge: " + entity);
        /*
         * Compute a random perpendicular direction.
         */
        Vector3 loc = new Vector3(entity);
        Vector3 target = new Vector3(pos.currentPosition());
        Vector3 diff = target.subtract(loc);
        Vector3 temp = new Vector3();
        diff.rotateAboutLine(Vector3.secondAxis, Math.PI / 2, temp);
        temp.y = 0;
        if (Math.random() > 0.5) temp.scalarMultBy(-1);
        Vector3 perp = temp.normalize();
        if (perp.isNaN())
        {
            new Exception().printStackTrace();
            perp.clear();
        }

        final double evasionMod = pokemob.getFloatStat(Stats.EVASION);
        /*
         * Scale by evasion modifier
         */
        perp.scalarMultBy(evasionMod * PokecubeCore.getConfig().dodgeSpeedFactor);
        if (perp.magSq() > 1) perp.norm();

        /*
         * Only flying or floating things can dodge properly in the air.
         */
        if (!AITools.canNavigate.test(pokemob)) perp.scalarMultBy(0.2);
        /*
         * Apply the dodge
         */
        perp.addVelocities(entity);

        new TaskBase.PlaySound(entity.level().dimension(), new Vector3().set(entity), this.getDodgeSound(),
                SoundSource.HOSTILE, 1, 1).run(level);
    }

    /**
     * Check if the mob should dodge. It checks that the mob can dodge (ie is on ground if it can't float or fly), and
     * then factors in evasion for whether or not the mob should be dodging now.
     */
    @Override
    public boolean shouldRun(Mob entity)
    {
        var pokemob = PokemobCaps.getPokemobFor(entity);
        if (!TaskBase.canMove(pokemob)) return false;

        var brain = entity.getBrain();
        int dodgeCooldown = brain.getMemory(MemoryModules.TIMER_DODGE.get()).orElse(10);
        // We are still preparing to dodge
        if (dodgeCooldown-- >= 0)
        {
            brain.setMemory(MemoryModules.TIMER_DODGE.get(), dodgeCooldown);
            return true;
        }

        var target = this.getAttackTarget(entity);
        // Only dodge if there is an attack target.
        if (target == null) return false;

        // Only flying or floating can dodge while in the air
        if (!AITools.canNavigate.test(pokemob)) return false;

        var pos = BrainUtils.getMoveUseTarget(target);
        if (pos != null)
        {
            final double ds2 = entity.distanceToSqr(pos.currentPosition());
            // No need to dodge if the target isn't near us
            if (ds2 > 16) return false;
        }
        // Nothing to dodge if target isn't attacking!
        else return false;

        /*
         * Scale amount jumped by evasion stat.
         */
        final double evasionMod = pokemob.getFloatStat(Stats.EVASION) / 30d;
        final boolean dodge = Math.random() > 1 - evasionMod;
        if (dodge) dodgeCooldown = 10;
        brain.setMemory(MemoryModules.TIMER_DODGE.get(), dodgeCooldown);

        return dodge;
    }
}
