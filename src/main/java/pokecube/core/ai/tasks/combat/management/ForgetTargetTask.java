package pokecube.core.ai.tasks.combat.management;

import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.apache.logging.log4j.Level;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.BrainUtil;
import net.minecraft.entity.ai.brain.memory.MemoryModuleStatus;
import net.minecraft.entity.ai.brain.memory.MemoryModuleType;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import pokecube.core.PokecubeCore;
import pokecube.core.ai.brain.BrainUtils;
import pokecube.core.ai.brain.MemoryModules;
import pokecube.core.ai.tasks.combat.CombatTask;
import pokecube.core.handlers.TeamManager;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.capabilities.CapabilityPokemob;
import pokecube.core.interfaces.pokemob.ai.CombatStates;
import pokecube.core.interfaces.pokemob.ai.GeneralStates;

public class ForgetTargetTask extends CombatTask
{
    private static class ForgetEntry
    {
        long forgotTime;

        LivingEntity mob;

        public ForgetEntry(final long time, final LivingEntity mob)
        {
            this.forgotTime = time;
            this.mob = mob;
        }

        boolean isValid(final long gameTime)
        {
            if (gameTime - this.forgotTime > 100) return false;
            return true;
        }

        @Override
        public int hashCode()
        {
            return this.mob.getUniqueID().hashCode();
        }

        @Override
        public boolean equals(final Object obj)
        {
            return this.mob.getUniqueID().equals(obj);
        }
    }

    public static int maxWildBattleDur = 600;
    /** The target being attacked. */
    LivingEntity      entityTarget;

    /** IPokemob version of entityTarget. */
    IPokemob pokemobTarget;

    int battleTime = 0;

    int ticksSinceSeen = 0;

    Map<UUID, ForgetEntry> forgotten = Maps.newHashMap();

    public ForgetTargetTask(final IPokemob pokemob)
    {
        super(pokemob);
    }

    @Override
    public void reset()
    {
        this.entityTarget = null;
        this.pokemobTarget = null;
        this.battleTime = 0;
        this.ticksSinceSeen = 0;
        this.endBattle();
    }

    @Override
    public void run()
    {
        this.battleTime++;

        // Check if we should be cancelling due to wild mobs

        final IPokemob mobA = this.pokemob;
        final IPokemob mobB = this.pokemobTarget;

        LivingEntity mate = BrainUtils.getMateTarget(this.entity);

        if (mate != null && !mate.isAlive())
        {
            BrainUtils.setMateTarget(this.entity, null);
            mate = null;
        }

        if (!this.forgotten.isEmpty())
        {
            final Set<UUID> ids = Sets.newHashSet(this.forgotten.keySet());
            for (final UUID id : ids)
                if (!this.forgotten.get(id).isValid(this.world.getGameTime())) this.forgotten.remove(id);
        }

        boolean deAgro = mate == this.entityTarget;

        final ForgetEntry entry = new ForgetEntry(this.world.getGameTime(), this.entityTarget);
        if (this.forgotten.containsKey(entry.mob.getUniqueID())) deAgro = true;

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
            if (bothWild && this.battleTime > ForgetTargetTask.maxWildBattleDur)
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
                if (!PokecubeCore.getConfig().teamsBattleEachOther && TeamManager.sameTeam(this.entityTarget,
                        this.entity) && !this.pokemob.getCombatState(CombatStates.MATEFIGHT))
                {
                    PokecubeCore.LOGGER.debug("Cannot target team mates.");
                    deAgro = true;
                    break agroCheck;
                }
            }

            if (BrainUtil.canSee(this.entity.getBrain(), this.entityTarget)) this.ticksSinceSeen = 0;

            // If it has been too long since last seen the target, give up.
            if (this.ticksSinceSeen++ > 100)
            {
                // Send deagress message and put mob on cooldown.
                final ITextComponent message = new TranslationTextComponent("pokemob.deagress.timeout", this.pokemob
                        .getDisplayName().getFormattedText());
                try
                {
                    this.entityTarget.sendMessage(message);
                }
                catch (final Exception e)
                {
                    PokecubeCore.LOGGER.log(Level.WARN, "Error with message for " + this.entityTarget, e);
                }
                deAgro = true;
                break agroCheck;
            }

            // Target is too far away, lets forget it.
            if (this.entity.getDistance(this.entityTarget) > PokecubeCore.getConfig().chaseDistance)
            {
                // Send deagress message and put mob on cooldown.
                final ITextComponent message = new TranslationTextComponent("pokemob.deagress.timeout", this.pokemob
                        .getDisplayName().getFormattedText());
                try
                {
                    this.entityTarget.sendMessage(message);
                }
                catch (final Exception e)
                {
                    PokecubeCore.LOGGER.log(Level.WARN, "Error with message for " + this.entityTarget, e);
                }
                deAgro = true;
                break agroCheck;
            }
        }
        // All we do is deagro if needed.
        if (deAgro)
        {
            this.pokemob.setAttackCooldown(PokecubeCore.getConfig().pokemobagressticks);
            this.endBattle();
        }
    }

    @Override
    public boolean shouldRun()
    {
        this.entityTarget = BrainUtils.getAttackTarget(this.entity);
        this.pokemobTarget = CapabilityPokemob.getPokemobFor(this.entityTarget);

        if (this.entityTarget == null && this.entity.getBrain().hasMemory(MemoryModuleType.HURT_BY_ENTITY))
            this.entityTarget = this.entity.getBrain().getMemory(MemoryModuleType.HURT_BY_ENTITY).get();

        // Only run if we have a combat target
        return this.entityTarget != null;
    }

    private void endBattle(){
        final Battle battle = Battle.battles.get(this.entity);
        if(battle != null) battle.end();
        this.pokemob.getTargetFinder().clear();
        this.pokemob.onSetTarget(null, true);
        BrainUtils.deagro(this.entity);
    }
}
