package pokecube.core.ai.tasks.combat.management;

import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.apache.logging.log4j.Level;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Entity.RemovalReason;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.player.Player;
import pokecube.api.PokecubeAPI;
import pokecube.api.entity.TeamManager;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.entity.pokemob.PokemobCaps;
import pokecube.api.entity.pokemob.ai.CombatStates;
import pokecube.api.entity.pokemob.ai.GeneralStates;
import pokecube.api.moves.Battle;
import pokecube.core.PokecubeCore;
import pokecube.core.ai.brain.BrainUtils;
import pokecube.core.ai.brain.MemoryModules;
import pokecube.core.ai.tasks.combat.CombatTask;
import thut.api.entity.ai.RootTask;
import thut.lib.TComponent;

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
        if (this.entity instanceof AgeableMob ageable)
        {
            mate = BrainUtils.getMateTarget(ageable);
            if (mate != null && !mate.isAlive())
            {
                BrainUtils.setMateTarget(ageable, null);
                mate = null;
            }
        }

        if (!this.forgotten.isEmpty())
        {
            final Set<UUID> ids = Sets.newHashSet(this.forgotten.keySet());
            for (final UUID id : ids)
                if (!this.forgotten.get(id).isValid(this.world.getGameTime())) this.forgotten.remove(id);
        }

        boolean deAgro = mate == this.target;
        boolean exitBattle = false;

        final ForgetEntry entry = new ForgetEntry(this.world.getGameTime(), this.target);
        if (this.forgotten.containsKey(entry.mob.getUUID()))
        {
            deAgro = true;
            if (PokecubeCore.getConfig().debug_ai) PokecubeAPI.logInfo("Was Marked as Forgotten!");
        }
        int giveUpTimer = Battle.BATTLE_END_TIMER;
        if (RootTask.doLoadThrottling) giveUpTimer *= RootTask.runRate;

        if (mobB == null && this.target.getRemovalReason() == RemovalReason.DISCARDED)
        {
            deAgro = true;
        }

        agroCheck:
        if (mobB != null && !deAgro)
        {
            if (mobB.getCombatState(CombatStates.FAINTED))
            {
                giveUpTimer /= 2;
                if (PokecubeCore.getConfig().debug_ai) PokecubeAPI.logInfo("Target Fainted.");

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
                exitBattle = true;
                if (PokecubeCore.getConfig().debug_ai) PokecubeAPI.logInfo("Wild Battle too long.");
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

                    if (weHealth < 0.5) if (mobA.getEntity().getBrain().checkMemory(MemoryModules.HUNTED_BY.get(),
                            MemoryStatus.REGISTERED))
                        mobA.getEntity().getBrain().setMemory(MemoryModules.HUNTED_BY.get(), mobB.getEntity());
                    if (theyHealth < 0.5) if (mobB.getEntity().getBrain().checkMemory(MemoryModules.HUNTED_BY.get(),
                            MemoryStatus.REGISTERED))
                        mobB.getEntity().getBrain().setMemory(MemoryModules.HUNTED_BY.get(), mobA.getEntity());

                    if (PokecubeCore.getConfig().debug_ai) PokecubeAPI.logInfo("No want to fight, too weak!");
                    deAgro = true;
                }
            }
        }
        if (mobA.getCombatState(CombatStates.FAINTED))
        {
            giveUpTimer /= 2;
            exitBattle = true;
            if (PokecubeCore.getConfig().debug_ai) PokecubeAPI.logInfo("we Fainted.");
        }

        agroCheck:
        if (!deAgro)
        {
            // If health is below 0, it fainted, we give some time for other to
            // send out a new mob before we completely deagro.
            if (this.target.getHealth() <= 0)
            {
                if (PokecubeCore.getConfig().debug_ai) PokecubeAPI.logInfo("They are Dead!");
                giveUpTimer /= 2;

                // Check if we are in a battle, and if so, divert to another
                // member
                Battle b = pokemob.getBattle();
                if (b != null)
                {
                    var targets = b.getEnemies(entity);
                    for (var e : targets)
                    {
                        if (e.isAlive())
                        {
                            // Divery agro to it.
                            Battle.createOrAddToBattle(entity, e);
                            this.target = e;
                            return;
                        }
                    }
                }
                break agroCheck;
            }
            if (!this.entity.isAlive() || this.entity.getHealth() <= 0)
            {
                if (PokecubeCore.getConfig().debug_ai) PokecubeAPI.logInfo("We are Dead!");
                deAgro = true;
                exitBattle = true;
                break agroCheck;
            }

            // If our target is us, we should forget it.
            if (this.target == this.entity)
            {
                PokecubeAPI.logInfo("Cannot target self.");
                deAgro = true;
                break agroCheck;
            }

            // If we are not angry, we should forget target.
            if (!this.pokemob.getCombatState(CombatStates.BATTLING))
            {
                if (PokecubeCore.getConfig().debug_ai) PokecubeAPI.logInfo("Not Angry. losing target now.");
                deAgro = true;
                exitBattle = true;
                break agroCheck;
            }

            // If our target is owner, we should forget it.
            if (this.target.getUUID().equals(this.pokemob.getOwnerId()))
            {
                PokecubeAPI.logInfo("Cannot target owner.");
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
                    if (PokecubeCore.getConfig().debug_ai)
                        PokecubeAPI.logInfo("Cannot target mob that far while guarding.");
                    deAgro = true;
                    break agroCheck;
                }

                // If the target is a pokemob, on same team, we shouldn't target
                // it either, unless it is fighting over a mate
                if (!PokecubeCore.getConfig().teamsBattleEachOther && TeamManager.sameTeam(this.target, this.entity)
                        && !this.pokemob.getCombatState(CombatStates.MATEFIGHT))
                {
                    if (PokecubeCore.getConfig().debug_ai) PokecubeAPI.logInfo("Cannot target team mates.");
                    deAgro = true;
                    break agroCheck;
                }
            }

            if (BrainUtils.canSee(this.entity, this.target)) this.ticksSinceSeen = 0;

            // If it has been too long since last seen the target, give up.
            if (this.ticksSinceSeen++ > giveUpTimer)
            {
                // Send deagress message and put mob on cooldown.
                final Component message = TComponent.translatable("pokemob.deagress.timeout",
                        this.pokemob.getDisplayName().getString());
                try
                {
                    if (this.target instanceof Player player) thut.lib.ChatHelper.sendSystemMessage(player, message);
                }
                catch (final Exception e)
                {
                    PokecubeAPI.LOGGER.log(Level.WARN, "Error with message for " + this.target, e);
                }
                deAgro = true;
                if (PokecubeCore.getConfig().debug_ai) PokecubeAPI.logInfo("Not seen for too long.");
                break agroCheck;
            }

            // Target is too far away, lets forget it.
            distance:
            if (this.entity.distanceTo(this.target) > PokecubeCore.getConfig().chaseDistance)
            {
                // Check if we are owned
                boolean owned = this.pokemob.getOwnerId() != null && !pokemob.isPlayerOwned();
                // Do it this way for now incase we want to adjust how we decide
                // this. For now, this makes npc owned pokemobs not deagro from
                // distance.
                if (owned) break distance;

                // Send deagress message and put mob on cooldown.
                final Component message = TComponent.translatable("pokemob.deagress.timeout",
                        this.pokemob.getDisplayName().getString());
                try
                {
                    if (this.target instanceof Player player) thut.lib.ChatHelper.sendSystemMessage(player, message);
                }
                catch (final Exception e)
                {
                    PokecubeAPI.LOGGER.log(Level.WARN, "Error with message for " + this.target, e);
                }
                deAgro = true;
                if (PokecubeCore.getConfig().debug_ai) PokecubeAPI.logInfo("Too far from target.");
                break agroCheck;
            }
        }
        // All we do is deagro if needed.
        if (deAgro) this.doDeAgro(exitBattle);
    }

    @Override
    public boolean shouldRun()
    {
        final LivingEntity target = this.getAttackTarget();

        if (target != this.target)
        {
            this.battleTime = 0;
            this.ticksSinceSeen = 0;
            this.target = target;
            this.pokemobTarget = PokemobCaps.getPokemobFor(this.target);
        }
        // Only run if we have a combat target
        return this.target != null;
    }

    private void doDeAgro(boolean exitBattle)
    {
        // first check if we have another valid target in the battle, if so,
        // switch over to that one.
        if (exitBattle)
        {
            this.pokemob.setAttackCooldown(PokecubeCore.getConfig().pokemobagressticks);
            this.pokemob.getTargetFinder().clear();
            this.pokemob.onSetTarget(null, true);
            if (this.pokemobTarget != null && this.mutualDeagro)
            {
                this.pokemobTarget.getTargetFinder().clear();
                this.pokemobTarget.onSetTarget(null, true);
            }
            BrainUtils.deagro(this.entity, this.mutualDeagro);
            this.target = null;
            this.pokemobTarget = null;
            this.battleTime = 0;
            this.ticksSinceSeen = 0;
        }
        else
        {
            // Clear target finder anyway, to let it reset
            this.pokemob.getTargetFinder().clear();

            // Clear these as well.
            BrainUtils.deagro(this.entity, this.mutualDeagro);
            this.target = null;
            this.pokemobTarget = null;
            this.battleTime = 0;
            this.ticksSinceSeen = 0;

        }
    }
}
