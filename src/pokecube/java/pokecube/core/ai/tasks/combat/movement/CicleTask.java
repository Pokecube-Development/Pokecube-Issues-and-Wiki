package pokecube.core.ai.tasks.combat.movement;

import com.google.common.collect.Maps;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.level.pathfinder.Node;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.entity.pokemob.PokemobCaps;
import pokecube.api.entity.pokemob.ai.CombatStates;
import pokecube.api.moves.Battle;
import pokecube.api.moves.MoveEntry;
import pokecube.core.PokecubeCore;
import pokecube.core.ai.brain.BrainUtils;
import pokecube.core.ai.brain.MemoryModules;
import pokecube.core.ai.tasks.TaskBase;
import pokecube.core.ai.tasks.combat.CombatTask;
import thut.api.maths.Vector3;

import java.util.Map;
import java.util.Random;

/**
 * This IAIRunnable manages the movement of the mob while it is in combat, but on cooldown between attacks. It also
 * manages the leaping at targets, and the dodging of attacks.
 */
public class CicleTask extends CombatTask
{
    private static final Map<MemoryModuleType<?>, MemoryStatus> MEMS = Maps.newHashMap();

    static
    {
        CicleTask.MEMS.put(MemoryModules.PATH, MemoryStatus.VALUE_ABSENT);
        CicleTask.MEMS.put(MemoryModules.COMBAT_CENTRE.get(), MemoryStatus.REGISTERED);
    }

    public CicleTask()
    {
        super(CicleTask.MEMS);
    }

    protected Vector3 calculateCentre(LivingEntity target, LivingEntity user, IPokemob pokemob)
    {
        var userBrain = user.getBrain();
        var targetBrain = target.getBrain();
        Vector3 centre;
        Battle b = pokemob.getBattle();
        if (b != null) centre = b.getCentre();
        else if (userBrain.hasMemoryValue(MemoryModules.COMBAT_CENTRE.get()))
        {
            centre = userBrain.getMemory(MemoryModules.COMBAT_CENTRE.get()).get();
        }
        else if (targetBrain.hasMemoryValue(MemoryModules.COMBAT_CENTRE.get()))
        {
            centre = targetBrain.getMemory(MemoryModules.COMBAT_CENTRE.get()).get();
        }
        else
        {
            final Vector3 targetLoc = new Vector3().set(target);
            final Vector3 attackerLoc = new Vector3().set(user);
            centre = targetLoc.addTo(attackerLoc).scalarMultBy(0.5);
            centre.y = Math.min(attackerLoc.y, targetLoc.y);
        }
        return centre;
    }

    @Override
    public void reset(Mob entityIn)
    {
        entityIn.getBrain().eraseMemory(MemoryModules.COMBAT_CENTRE.get());
    }

    @Override
    public void run(ServerLevel level, Mob entity)
    {
        var pokemob = PokemobCaps.getPokemobFor(entity);
        var target = this.getAttackTarget(entity);
        // Figure out where centre of combat is
        var centre = calculateCentre(target, entity, pokemob);
        float movementSpeed = 1.5f;
        Node point;
        // If the mob has a path already, check if it is near the end, if not,
        // return early, getFinalPathPoint() is nullable!
        if (!entity.getNavigation().isDone() && (point = entity.getNavigation().getPath().getEndNode()) != null)
        {
            final Vector3 end = new Vector3().set(point);
            final Vector3 here = new Vector3().set(entity);
            float f = entity.getBbWidth();
            f = Math.max(f, 0.5f);
            if (here.distTo(end) > f) return;
        }

        // Check if we can see the target, if not, try pathing directly to it.
        if (!BrainUtils.canSee(entity, target))
        {
            this.setWalkTo(entity, centre, movementSpeed, 0);
            return;
        }

        MoveEntry attack = pokemob.getSelectedMove();

        final Vector3 here = new Vector3().set(entity);
        boolean meleeCombat = !attack.isRanged(pokemob) && !pokemob.getMoveStats().targettingSelf;
        // melee mobs will instead try to be closer to the target, instead of
        // centre of battlefield
        if (meleeCombat) here.set(target);

        final Vector3 diff = here.subtract(centre);
        if (diff.magSq() < 1) diff.norm();
        int combatDistance = PokecubeCore.getConfig().combatDistance;

        // If we are using a melee move, try to stay closer to the target!
        if (meleeCombat)
        {
            combatDistance /= 2;
            meleeCombat = true;
        }
        combatDistance = Math.max(combatDistance, 1);

        final int combatDistanceSq = combatDistance * combatDistance;
        // If the mob has left the combat radius, try to return to the centre of
        if (diff.magSq() > combatDistanceSq + 1)
        {
            if (meleeCombat) this.setWalkTo(entity, target, movementSpeed, 0);
            else this.setWalkTo(entity, centre, movementSpeed, 0);
        }
        else
        {
            Vector3 perp = new Vector3(target);
            // Otherwise. find direction of target from centre, and get a
            // location on the opposite side of it.
            perp.subtractFrom(centre).reverse().norm().scalarMultBy(combatDistance);
            perp.y = 0;
            diff.set(perp);
            // Apply a random phase offset from that location
            double phase = (new Random(pokemob.getRNGValue()).nextDouble() - 1) * (Math.PI / 6);
            diff.rotateAboutLine(Vector3.secondAxis, phase, here);

            perp.set(centre).addTo(here);
            // Then path to it.
            this.setWalkTo(entity, perp, movementSpeed * 0.75, 0);
        }
    }

    @Override
    public boolean shouldRun(Mob entityIn)
    {
        var pokemob = PokemobCaps.getPokemobFor(entityIn);
        // Marked as unable to move, so skip
        if (!TaskBase.canMove(pokemob)) return false;
        // Update if we have a target
        var target = this.getAttackTarget(entityIn);
        // No target, so skip
        if (target == null) return false;
        // Using an attack, so skip
        if (pokemob.getCombatState(CombatStates.EXECUTINGMOVE)) return false;
        // Using an attack, so skip
        if (pokemob.getCombatState(CombatStates.LEAPING)) return false;
        // Is in battle.
        return pokemob.getCombatState(CombatStates.BATTLING);
    }
}