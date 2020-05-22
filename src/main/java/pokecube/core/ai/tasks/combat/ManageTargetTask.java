package pokecube.core.ai.tasks.combat;

import net.minecraft.entity.AgeableEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.memory.MemoryModuleStatus;
import pokecube.core.PokecubeCore;
import pokecube.core.ai.brain.BrainUtils;
import pokecube.core.ai.brain.MemoryModules;
import pokecube.core.handlers.TeamManager;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.capabilities.CapabilityPokemob;
import pokecube.core.interfaces.pokemob.ai.CombatStates;
import pokecube.core.interfaces.pokemob.ai.GeneralStates;

public class ManageTargetTask extends CombatTask
{
    /** The target being attacked. */
    LivingEntity entityTarget;

    /** IPokemob version of entityTarget. */
    IPokemob pokemobTarget;

    int battleTime = 0;

    public ManageTargetTask(final IPokemob pokemob)
    {
        super(pokemob);
    }

    @Override
    public void reset()
    {
        this.entityTarget = null;
        this.pokemobTarget = null;
        FindTargetsTask.deagro(this.entity);
    }

    @Override
    public void run()
    {
        this.battleTime++;

        // Check if we should be cancelling due to wild mobs

        final IPokemob mobA = this.pokemob;
        final IPokemob mobB = this.pokemobTarget;

        LivingEntity mate = BrainUtils.getMateTarget((AgeableEntity) this.entity);

        if (mate != null && !mate.isAlive())
        {
            BrainUtils.setMateTarget((AgeableEntity) this.entity, null);
            mate = null;
        }

        boolean deAgro = mate == this.entityTarget;

        agroCheck:
        if (mobB != null && !deAgro)
        {
            if (mobB.getCombatState(CombatStates.FAINTED))
            {
                deAgro = true;
                break agroCheck;
            }

            final boolean weTame = mobA.getOwnerId() != null && !mobA.getCombatState(CombatStates.MATEFIGHT);
            final boolean theyTame = mobB.getOwnerId() != null && !mobB.getCombatState(CombatStates.MATEFIGHT);
            final boolean weHunt = mobA.getCombatState(CombatStates.HUNTING);
            final boolean theyHunt = mobB.getCombatState(CombatStates.HUNTING);

            final boolean bothWild = !weTame && !theyTame;
            final boolean oneHunting = weHunt || theyHunt;

            // Give up if we took too long to fight.
            if (bothWild && this.battleTime > UseAttacksTask.maxWildBattleDur)
            {
                deAgro = true;
                break agroCheck;
            }

            if (bothWild && !oneHunting)
            {
                final float weHealth = mobA.getEntity().getHealth() / mobA.getEntity().getMaxHealth();
                final float theyHealth = mobB.getEntity().getHealth() / mobB.getEntity().getMaxHealth();
                // Wild mobs shouldn't fight to the death unless hunting.
                if (weHealth < 0.5 || theyHealth < 0.5)
                {
                    mobA.setCombatState(CombatStates.MATEFIGHT, false);
                    mobB.setCombatState(CombatStates.MATEFIGHT, false);

                    if (weHealth < 0.5) if (mobA.getEntity().getBrain().hasMemory(MemoryModules.HUNTED_BY,
                            MemoryModuleStatus.REGISTERED)) mobA.getEntity().getBrain().setMemory(
                                    MemoryModules.HUNTED_BY, mobB.getEntity());
                    if (theyHealth < 0.5) if (mobB.getEntity().getBrain().hasMemory(MemoryModules.HUNTED_BY,
                            MemoryModuleStatus.REGISTERED)) mobB.getEntity().getBrain().setMemory(
                                    MemoryModules.HUNTED_BY, mobA.getEntity());

                    PokecubeCore.LOGGER.debug("No want to fight, too weak!");
                    deAgro = true;
                }
            }
        }
        if (mobA.getCombatState(CombatStates.FAINTED)) deAgro = true;

        agroCheck:
        if (!deAgro)
        {
            if (!this.entityTarget.isAlive() || this.entityTarget.getHealth() <= 0)
            {
                PokecubeCore.LOGGER.debug("They are Dead!");
                deAgro = true;
                break agroCheck;
            }
            if (!this.entity.isAlive() || this.entity.getHealth() <= 0)
            {
                PokecubeCore.LOGGER.debug("We are Dead!");
                deAgro = true;
                break agroCheck;
            }

            // If our target is us, we should forget it.
            if (this.entityTarget == this.entity)
            {
                PokecubeCore.LOGGER.debug("Cannot target self.");
                deAgro = true;
                break agroCheck;
            }

            // If we are not angry, we should forget target.
            if (!this.pokemob.getCombatState(CombatStates.ANGRY))
            {
                PokecubeCore.LOGGER.debug("Not Angry. losing target now.");
                deAgro = true;
                break agroCheck;
            }

            // If our target is owner, we should forget it.
            if (this.entityTarget.getUniqueID().equals(this.pokemob.getOwnerId()))
            {
                PokecubeCore.LOGGER.debug("Cannot target owner.");
                deAgro = true;
                break agroCheck;
            }

            final boolean tame = this.pokemob.getGeneralState(GeneralStates.TAMED);
            // If your owner is too far away, shouldn't have a target, should be
            // going back to the owner.
            if (tame)
            {
                final Entity owner = this.pokemob.getOwner();
                final boolean stayOrGuard = this.pokemob.getCombatState(CombatStates.GUARDING) || this.pokemob
                        .getGeneralState(GeneralStates.STAYING);
                if (owner != null && !stayOrGuard && owner.getDistance(this.entity) > PokecubeCore
                        .getConfig().chaseDistance)
                {
                    PokecubeCore.LOGGER.debug("Cannot target mob that far while guarding.");
                    deAgro = true;
                    break agroCheck;
                }

                // If the target is a pokemob, on same team, we shouldn't target
                // it either, unless it is fighting over a mate
                if (TeamManager.sameTeam(this.entityTarget, this.entity) && !this.pokemob.getCombatState(
                        CombatStates.MATEFIGHT))
                {
                    PokecubeCore.LOGGER.debug("Cannot target team mates.");
                    deAgro = true;
                    break agroCheck;
                }

            }
        }
        // All we do is deagro if needed.
        if (deAgro) FindTargetsTask.deagro(this.entity);
    }

    @Override
    public boolean shouldRun()
    {
        this.entityTarget = BrainUtils.getAttackTarget(this.entity);
        this.pokemobTarget = CapabilityPokemob.getPokemobFor(this.entityTarget);
        // Only run if we have a combat target
        return this.entityTarget != null;
    }

}
