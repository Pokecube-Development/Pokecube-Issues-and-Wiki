package pokecube.core.ai.tasks.combat.management;

import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.apache.logging.log4j.Level;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import net.minecraft.Util;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Entity.RemovalReason;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import pokecube.core.PokecubeCore;
import pokecube.core.ai.brain.BrainUtils;
import pokecube.core.ai.brain.MemoryModules;
import pokecube.core.ai.tasks.combat.CombatTask;
import pokecube.core.handlers.TeamManager;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.PokecubeMod;
import pokecube.core.interfaces.capabilities.CapabilityPokemob;
import pokecube.core.interfaces.pokemob.ai.CombatStates;
import pokecube.core.interfaces.pokemob.ai.GeneralStates;
import pokecube.core.moves.Battle;
import thut.api.entity.ai.RootTask;

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
            return this.mob.getUUID().hashCode();
        }

        @Override
        public boolean equals(final Object obj)
        {
            return this.mob.getUUID().equals(obj);
        }
    }

    public static int maxWildBattleDur = 600;
    /** The target being attacked. */
    LivingEntity entityTarget;

    /** IPokemob version of entityTarget. */
    IPokemob pokemobTarget;

    int battleTime = 0;

    int ticksSinceSeen = 0;

    boolean mutualDeagro = true;

    Map<UUID, ForgetEntry> forgotten = Maps.newHashMap();

    public ForgetTargetTask(final IPokemob pokemob)
    {
        super(pokemob);
    }

    @Override
    public void reset()
    {}

    @Override
    public void run()
    {
        this.battleTime++;

        // Check if we should be cancelling due to wild mobs
        this.mutualDeagro = true;
        final IPokemob mobA = this.pokemob;
        final IPokemob mobB = this.pokemobTarget;

        LivingEntity mate = null;
        if (this.entity instanceof AgeableMob)
        {
            mate = BrainUtils.getMateTarget((AgeableMob) this.entity);
            if (mate != null && !mate.isAlive())
            {
                BrainUtils.setMateTarget((AgeableMob) this.entity, null);
                mate = null;
            }
        }

        if (!this.forgotten.isEmpty())
        {
            final Set<UUID> ids = Sets.newHashSet(this.forgotten.keySet());
            for (final UUID id : ids)
                if (!this.forgotten.get(id).isValid(this.world.getGameTime())) this.forgotten.remove(id);
        }

        boolean deAgro = mate == this.entityTarget;

        final ForgetEntry entry = new ForgetEntry(this.world.getGameTime(), this.entityTarget);
        if (this.forgotten.containsKey(entry.mob.getUUID()))
        {
            deAgro = true;
            if (PokecubeMod.debug) PokecubeCore.LOGGER.debug("Was Marked as Forgotten!");
        }
        int giveUpTimer = Battle.BATTLE_END_TIMER;
        if (RootTask.doLoadThrottling) giveUpTimer *= RootTask.runRate;

        if (mobB == null && entityTarget.getRemovalReason() == RemovalReason.DISCARDED)
        {
            deAgro = true;
        }

        agroCheck:
        if (mobB != null && !deAgro)
        {
            if (mobB.getCombatState(CombatStates.FAINTED))
            {
                giveUpTimer /= 2;
                if (PokecubeMod.debug) PokecubeCore.LOGGER.debug("Target Fainted.");

                if (mobB.getOwnerId() == null) deAgro = true;

                break agroCheck;
            }

            final boolean weTame = mobA.getOwnerId() != null && !mobA.getCombatState(CombatStates.MATEFIGHT);
            final boolean theyTame = mobB.getOwnerId() != null && !mobB.getCombatState(CombatStates.MATEFIGHT);
            final boolean weHunt = mobA.getCombatState(CombatStates.HUNTING);
            final boolean theyHunt = mobB.getCombatState(CombatStates.HUNTING);

            final boolean bothWild = !weTame && !theyTame;
            final boolean oneHunting = weHunt || theyHunt;

            this.mutualDeagro = !oneHunting;

            // Give up if we took too long to fight.
            if (bothWild && this.battleTime > ForgetTargetTask.maxWildBattleDur)
            {
                deAgro = true;
                if (PokecubeMod.debug) PokecubeCore.LOGGER.debug("Wild Battle too long.");
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

                    if (weHealth < 0.5)
                        if (mobA.getEntity().getBrain().checkMemory(MemoryModules.HUNTED_BY, MemoryStatus.REGISTERED))
                            mobA.getEntity().getBrain().setMemory(MemoryModules.HUNTED_BY, mobB.getEntity());
                    if (theyHealth < 0.5)
                        if (mobB.getEntity().getBrain().checkMemory(MemoryModules.HUNTED_BY, MemoryStatus.REGISTERED))
                            mobB.getEntity().getBrain().setMemory(MemoryModules.HUNTED_BY, mobA.getEntity());

                    if (PokecubeMod.debug) PokecubeCore.LOGGER.debug("No want to fight, too weak!");
                    deAgro = true;
                }
            }
        }
        if (mobA.getCombatState(CombatStates.FAINTED))
        {
            giveUpTimer /= 2;
            if (PokecubeMod.debug) PokecubeCore.LOGGER.debug("we Fainted.");
        }

        agroCheck:
        if (!deAgro)
        {
            // If health is below 0, it fainted, we give some time for other to
            // send out a new mob before we completely deagro.
            if (this.entityTarget.getHealth() <= 0)
            {
                if (PokecubeMod.debug) PokecubeCore.LOGGER.debug("They are Dead!");
                giveUpTimer /= 2;
                break agroCheck;
            }
            if (!this.entity.isAlive() || this.entity.getHealth() <= 0)
            {
                if (PokecubeMod.debug) PokecubeCore.LOGGER.debug("We are Dead!");
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
                if (PokecubeMod.debug) PokecubeCore.LOGGER.debug("Not Angry. losing target now.");
                deAgro = true;
                break agroCheck;
            }

            // If our target is owner, we should forget it.
            if (this.entityTarget.getUUID().equals(this.pokemob.getOwnerId()))
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
                final boolean stayOrGuard = this.pokemob.getCombatState(CombatStates.GUARDING)
                        || this.pokemob.getGeneralState(GeneralStates.STAYING);
                if (owner != null && !stayOrGuard
                        && owner.distanceTo(this.entity) > PokecubeCore.getConfig().chaseDistance)
                {
                    if (PokecubeMod.debug) PokecubeCore.LOGGER.debug("Cannot target mob that far while guarding.");
                    deAgro = true;
                    break agroCheck;
                }

                // If the target is a pokemob, on same team, we shouldn't target
                // it either, unless it is fighting over a mate
                if (!PokecubeCore.getConfig().teamsBattleEachOther
                        && TeamManager.sameTeam(this.entityTarget, this.entity)
                        && !this.pokemob.getCombatState(CombatStates.MATEFIGHT))
                {
                    if (PokecubeMod.debug) PokecubeCore.LOGGER.debug("Cannot target team mates.");
                    deAgro = true;
                    break agroCheck;
                }
            }

            if (BrainUtils.canSee(this.entity, this.entityTarget)) this.ticksSinceSeen = 0;

            if (PokecubeMod.debug) PokecubeCore.LOGGER.debug("Seen Time: {}->{}, {}", this.entity.getName().getString(),
                    this.entityTarget.getName().getString(), this.ticksSinceSeen);

            // If it has been too long since last seen the target, give up.
            if (this.ticksSinceSeen++ > giveUpTimer)
            {
                // Send deagress message and put mob on cooldown.
                final Component message = new TranslatableComponent("pokemob.deagress.timeout",
                        this.pokemob.getDisplayName().getString());
                try
                {
                    this.entityTarget.sendMessage(message, Util.NIL_UUID);
                }
                catch (final Exception e)
                {
                    PokecubeCore.LOGGER.log(Level.WARN, "Error with message for " + this.entityTarget, e);
                }
                deAgro = true;
                if (PokecubeMod.debug) PokecubeCore.LOGGER.debug("Not seen for too long.");
                break agroCheck;
            }

            // Target is too far away, lets forget it.
            if (this.entity.distanceTo(this.entityTarget) > PokecubeCore.getConfig().chaseDistance)
            {
                // Send deagress message and put mob on cooldown.
                final Component message = new TranslatableComponent("pokemob.deagress.timeout",
                        this.pokemob.getDisplayName().getString());
                try
                {
                    this.entityTarget.sendMessage(message, Util.NIL_UUID);
                }
                catch (final Exception e)
                {
                    PokecubeCore.LOGGER.log(Level.WARN, "Error with message for " + this.entityTarget, e);
                }
                deAgro = true;
                if (PokecubeMod.debug) PokecubeCore.LOGGER.debug("Too far from target.");
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
        final LivingEntity target = BrainUtils.getAttackTarget(this.entity);

        if (target != this.entityTarget)
        {
            this.battleTime = 0;
            this.ticksSinceSeen = 0;
            this.entityTarget = target;
            this.pokemobTarget = CapabilityPokemob.getPokemobFor(this.entityTarget);
        }

        if (this.entityTarget == null && this.entity.getBrain().hasMemoryValue(MemoryModuleType.HURT_BY_ENTITY))
            this.entityTarget = this.entity.getBrain().getMemory(MemoryModuleType.HURT_BY_ENTITY).get();

        // Only run if we have a combat target
        return this.entityTarget != null;
    }

    private void endBattle()
    {
        final Battle battle = Battle.getBattle(this.entity);
        if (battle != null) battle.removeFromBattle(this.entity);
        this.pokemob.getTargetFinder().clear();
        this.pokemob.onSetTarget(null, true);
        if (this.pokemobTarget != null && !this.mutualDeagro)
        {
            this.pokemobTarget.getTargetFinder().clear();
            this.pokemobTarget.onSetTarget(null, true);
        }
        BrainUtils.deagro(this.entity, this.mutualDeagro);

        this.entityTarget = null;
        this.pokemobTarget = null;
        this.battleTime = 0;
        this.ticksSinceSeen = 0;
    }
}
