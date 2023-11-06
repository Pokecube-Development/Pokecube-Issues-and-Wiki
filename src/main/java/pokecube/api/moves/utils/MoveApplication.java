package pokecube.api.moves.utils;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.function.Supplier;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.google.common.collect.Sets;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.npc.Npc;
import net.minecraft.world.entity.player.Player;
import pokecube.api.PokecubeAPI;
import pokecube.api.entity.IOngoingAffected;
import pokecube.api.entity.IOngoingAffected.IOngoingEffect;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.entity.pokemob.IPokemob.Stats;
import pokecube.api.entity.pokemob.PokemobCaps;
import pokecube.api.entity.pokemob.ai.GeneralStates;
import pokecube.api.events.pokemobs.combat.MoveUse.DuringUse;
import pokecube.api.moves.MoveEntry;
import pokecube.api.moves.MoveEntry.MoveSounds;
import pokecube.api.moves.utils.IMoveConstants.AttackCategory;
import pokecube.api.utils.PokeType;
import pokecube.api.utils.Tools;
import pokecube.core.PokecubeCore;
import pokecube.core.moves.MovesUtils;
import pokecube.core.moves.MovesUtils.StatDiff;
import pokecube.core.moves.animations.AnimationMultiAnimations;
import pokecube.core.moves.damage.PokemobDamageSource;
import pokecube.core.utils.EntityTools;
import thut.api.level.terrain.TerrainManager;
import thut.api.level.terrain.TerrainSegment;

public class MoveApplication implements Comparable<MoveApplication>
{
    public static record Accuracy(MoveApplication move, float efficiency)
    {
    }

    public static record Damage(MoveApplication move, float efficiency, int dealt, int healthBefore, int healthAfter)
    {
    }

    public static interface PreApplyTests
    {
        public static PreApplyTests DEFAULT = new PreApplyTests()
        {
        };

        default boolean checkPreApply(MoveApplication t)
        {
            final IPokemob attackedMob = PokemobCaps.getPokemobFor(t.getTarget());
            if ((t.getUser().getStatus() & IMoveConstants.STATUS_SLP) > 0)
            {
                if (attackedMob != null) MovesUtils.displayStatusMessages(attackedMob, t.getUser().getEntity(),
                        IMoveConstants.STATUS_SLP, false);
                else MovesUtils.displayStatusMessages(null, t.getUser().getEntity(), IMoveConstants.STATUS_SLP, false);
                return false;
            }
            if ((t.getUser().getStatus() & IMoveConstants.STATUS_FRZ) > 0)
            {
                if (attackedMob != null) MovesUtils.displayStatusMessages(attackedMob, t.getUser().getEntity(),
                        IMoveConstants.STATUS_FRZ, false);
                else MovesUtils.displayStatusMessages(null, t.getUser().getEntity(), IMoveConstants.STATUS_FRZ, false);
                return false;
            }
            if ((t.getUser().getStatus() & IMoveConstants.STATUS_PAR) > 0 && Math.random() > 0.75)
            {
                if (attackedMob != null) MovesUtils.displayStatusMessages(attackedMob, t.getUser().getEntity(),
                        IMoveConstants.STATUS_PAR, false);
                else MovesUtils.displayStatusMessages(null, t.getUser().getEntity(), IMoveConstants.STATUS_PAR, false);
                return false;
            }
            return true;
        }
    }

    public static interface StatusApplier
    {
        public static StatusApplier DEFAULT = new StatusApplier()
        {
        };
        public static StatusApplier NOOP = new StatusApplier()
        {
            @Override
            public void applyStatus(Damage t)
            {}
        };

        default void applyStatus(Damage t)
        {
            LivingEntity target = t.move().getTarget();
            if (t.move().status_chance > target.getRandom().nextDouble())
            {
                if (MovesUtils.setStatus(t.move().getUser(), target, t.move().status_effects))
                {
                    MovesUtils.displayStatusMessages(t.move().user, target, t.move().status_effects, true);
                    if (PokecubeCore.getConfig().debug_moves)
                        PokecubeAPI.logInfo("Applied Status Effect {} to {}", t.move().status_effects, target);
                }
            }
        }
    }

    public static interface StatApplier
    {
        public static StatApplier DEFAULT = new StatApplier()
        {
        };
        public static StatApplier NOOP = new StatApplier()
        {
            @Override
            public void applyStats(Damage t)
            {}
        };

        default void applyStats(Damage t)
        {
            LivingEntity target = t.move().getTarget();
            if (t.move().stat_chance > 0)
            {
                t.move().applied_stat_effects = MovesUtils.handleStats(t.move().user, target, t.move().stat_effects,
                        t.move().stat_chance);
                if (PokecubeCore.getConfig().debug_moves)
                {
                    PokecubeAPI.logInfo("Base Stat Effect: {}", Arrays.toString(t.move().stat_effects));
                    if (t.move().applied_stat_effects.applied()) PokecubeAPI.logInfo("Applied Stats Effect {} to {}",
                            Arrays.toString(t.move().applied_stat_effects.diffs()), target);
                    else PokecubeAPI.logInfo("Failed to Apply Stats Effects to {}", target);
                }
                MovesUtils.sendStatDiffsMessages(t.move().user, target, t.move().applied_stat_effects);
            }
        }
    }

    public static interface DamageApplier
    {
        public static DamageApplier DEFAULT = new DamageApplier()
        {
        };

        default Damage applyDamage(MoveApplication t)
        {
            IPokemob user = t.user;
            MoveEntry move = t.move;
            PokeType type = t.type;
            LivingEntity target = t.getTarget();

            // This is the pokemob instance of the target, may be null.
            IPokemob targetPokemob = PokemobCaps.getPokemobFor(t.getTarget());
            // This ia a vanilla-equivalent of the attack strength.
            float attackStrength = user.getAttackStrength() * t.pwr / 150;
            // We use the RNG for the target
            var rand = t.getTarget().getRandom();

            // This will scale the damage of the move, for things like
            // not-very-effective, and is also used as a flag for if the move
            // failed/missed/etc
            float efficiency = 1;

            if (targetPokemob != null)
            {
                // Efficiency scales based on typing for pokemobs
                efficiency = Tools.getAttackEfficiency(type, targetPokemob.getType1(), targetPokemob.getType2());
                // If the target is a pokemob, then attack strength uses the
                // "proper" calculations
                attackStrength = MovesUtils.getAttackStrength(user, targetPokemob, move.getCategory(user), t.pwr, move,
                        t.stat_multipliers);
            }

            // Accuracy can then also factor in the target's stats.
            var moveAcc = t.accuracy.applyAccuracy(new Accuracy(t, efficiency));
            efficiency = moveAcc.efficiency();

            // This scales how much the critical attack will deal
            float criticalRatio = 1;

            // This is scaling factor for same type attack bonus
            float stabRatio = t.stabFactor;
            // Additional scaler for super effective moves
            float superEffectScale = t.superEffectMult;

            int critcalRate = 16;

            // Convert from crit number to crit chance
            if (t.crit == 1) critcalRate = 16;
            else if (t.crit == 2) critcalRate = 8;
            else if (t.crit == 3) critcalRate = 4;
            else if (t.crit == 4) critcalRate = 3;
            else if (t.crit == 5) critcalRate = 2;
            else critcalRate = 1;

            // Now set the criticalRatio to critFactor if we should have crit.
            if (t.crit > 0 && rand.nextInt(critcalRate) == 0) criticalRatio = t.critFactor;

            final LivingEntity attackerMob = user.getEntity();
            // See if terrain effects will scale damage
            final TerrainSegment terrain = TerrainManager.getInstance().getTerrainForEntity(attackerMob);
            // This should be the scaling factor for things like rain/sun/etc.
            float terrainDamageModifier = MovesUtils.getTerrainDamageModifier(type, attackerMob, terrain);

            // fixed damage moves this is always 1, regardless of stats.
            if (efficiency > 0 && move.fixed)
            {
                efficiency = 1;
                // also no critical for this.
                criticalRatio = 1;
                // No stab either
                stabRatio = 1;
                // And no terrain bonus
                terrainDamageModifier = 1;
                // and also no scaling from here
                superEffectScale = 1;
            }

            // Apply all of the scalings
            attackStrength *= efficiency * criticalRatio * terrainDamageModifier * stabRatio * superEffectScale;
            // Here is the damage will will actually do.
            int finalAttackStrength = Math.max(0, Math.round(attackStrength));

            int beforeHealth = 0;

            // We sometimes attack multi-part movs, so we need to use the core
            // mob
            // for this.
            final LivingEntity attackedHp = EntityTools.getCoreLiving(target);
            if (attackedHp != null) beforeHealth = (int) attackedHp.getHealth();

            if (t.noFaint)
            {
                // If we are marked as should not faint, apply things here.
                finalAttackStrength = Math.min(finalAttackStrength, beforeHealth - 1);
                finalAttackStrength = Math.max(0, finalAttackStrength);
            }

            final boolean wild = !user.getGeneralState(GeneralStates.TAMED);

            if (PokecubeCore.getConfig().maxWildPlayerDamage >= 0 && wild && target instanceof Player)
                finalAttackStrength = Math.min(PokecubeCore.getConfig().maxWildPlayerDamage, finalAttackStrength);
            else if (PokecubeCore.getConfig().maxOwnedPlayerDamage >= 0 && !wild && target instanceof Player)
                finalAttackStrength = Math.min(PokecubeCore.getConfig().maxOwnedPlayerDamage, finalAttackStrength);
            double scaleFactor = 1;
            if (target instanceof Player)
            {
                final boolean owner = target == user.getOwner();
                if (!owner || PokecubeCore.getConfig().pokemobsDamageOwner)
                    scaleFactor = PokecubeCore.getConfig().pokemobsDamagePlayers
                            ? wild ? PokecubeCore.getConfig().wildPlayerDamageRatio
                                    : PokecubeCore.getConfig().ownedPlayerDamageRatio
                            : 0;
                else scaleFactor = 0;
            }
            else if (targetPokemob == null)
                scaleFactor = target instanceof Npc ? PokecubeCore.getConfig().pokemobToNPCDamageRatio
                        : PokecubeCore.getConfig().pokemobToOtherMobDamageRatio;
            finalAttackStrength *= scaleFactor;

            if (targetPokemob != null) if (targetPokemob.getAbility() != null)
                finalAttackStrength = targetPokemob.getAbility().beforeDamage(targetPokemob, t, finalAttackStrength);

            boolean self = user == targetPokemob;

            if (self && move.defrosts && targetPokemob != null
                    && (targetPokemob.getStatus() & IMoveConstants.STATUS_FRZ) > 0)
                targetPokemob.healStatus();

            if (finalAttackStrength > 0 && !target.isInvulnerable())
            {
                // Apply attack damage to players.
                if (target instanceof Player)
                {
                    final DamageSource source1 = new PokemobDamageSource(attackerMob, move).setType(type);
                    final DamageSource source2 = new PokemobDamageSource(attackerMob, move).setType(type);
                    source2.setMagic();
                    float d1, d2;
                    if (wild)
                    {
                        d2 = (float) (finalAttackStrength
                                * Math.min(1, PokecubeCore.getConfig().wildPlayerDamageMagic));
                        d1 = finalAttackStrength - d2;
                    }
                    else
                    {
                        d2 = (float) (finalAttackStrength
                                * Math.min(1, PokecubeCore.getConfig().ownedPlayerDamageMagic));
                        d1 = finalAttackStrength - d2;
                    }
                    target.hurt(source1, d1);
                    target.hurt(source2, d2);
                    if (PokecubeCore.getConfig().debug_moves)
                    {
                        PokecubeAPI.logInfo("Attack Used: " + move.name);
                        PokecubeAPI.logInfo("Normal Component: " + d1);
                        PokecubeAPI.logInfo("Magic Component: " + d2);
                    }
                }
                // Apply attack damage to a pokemob
                else if (targetPokemob != null)
                {
                    final DamageSource source = new PokemobDamageSource(attackerMob, move).setType(type);
                    source.bypassMagic();
                    source.bypassArmor();
                    if (PokecubeCore.getConfig().debug_moves)
                    {
                        PokecubeAPI.logInfo("Attack Used: " + move.name);
                        PokecubeAPI.logInfo("Attack Damage: " + finalAttackStrength);
                    }
                    target.hurt(source, finalAttackStrength);
                }
                // Apply attack damage to another mob type.
                else
                {
                    final DamageSource source = new PokemobDamageSource(attackerMob, move).setType(type);
                    final boolean damaged = target.hurt(source, finalAttackStrength);
                    if (PokecubeCore.getConfig().debug_moves)
                    {
                        PokecubeAPI.logInfo("Attack Used: {}, expected damage: {}, Did apply? {} ", move.name,
                                finalAttackStrength, damaged);
                        PokecubeAPI.logInfo("Attack Target: " + target);
                    }
                }

                if (targetPokemob != null)
                {
                    if (t.move.category == AttackCategory.SPECIAL)
                        targetPokemob.getMoveStats().SPECIALDAMAGETAKENCOUNTER += finalAttackStrength;
                    if (t.move.category == AttackCategory.PHYSICAL)
                        targetPokemob.getMoveStats().PHYSICALDAMAGETAKENCOUNTER += finalAttackStrength;
                }
            }

            if (finalAttackStrength > 0)
            {
                MovesUtils.displayEfficiencyMessages(user, target, efficiency, criticalRatio);
                t.hit = true;
                t.didCrit = criticalRatio != 1;
            }

            int afterHealth = 0;
            if (attackedHp != null) afterHealth = (int) attackedHp.getHealth();

            // Now some custom vanilla-interacting effects

            // thunder moves apply lightning bolt effects.
            if (AnimationMultiAnimations.isThunderAnimation(t.move.getAnimation(t.getUser())))
            {
                final LightningBolt lightning = new LightningBolt(EntityType.LIGHTNING_BOLT, t.getTarget().getLevel());
                t.getTarget().thunderHit((ServerLevel) t.getTarget().getLevel(), lightning);
            }

            // Creepers fear psyhic moves.
            if (t.getTarget() instanceof Creeper creeper)
            {
                if (t.type == PokeType.getType("psychic") && creeper.getHealth() > 0) creeper.explodeCreeper();
            }

            return new Damage(t, efficiency, finalAttackStrength, beforeHealth, afterHealth);
        }
    }

    public static interface OngoingApplier
    {
        OngoingApplier NOOP = new OngoingApplier()
        {
        };

        public static OngoingApplier fromFunction(Function<Damage, IOngoingEffect> provider)
        {
            return new OngoingApplier()
            {
                @Override
                public void applyOngoingEffects(Damage t)
                {
                    final IOngoingAffected targetAffected = PokemobCaps.getAffected(t.move().target);
                    if (PokecubeCore.getConfig().debug_moves)
                        PokecubeAPI.logInfo("Applying Ongoing Effect for move {} used on {}", t.move().getName(),
                                t.move().getTarget());
                    if (targetAffected != null) targetAffected.getEffects().add(provider.apply(t));
                }
            };
        };

        default void applyOngoingEffects(Damage t)
        {
            if (PokecubeCore.getConfig().debug_moves) PokecubeAPI.LOGGER
                    .info("No Ongoing Effects for move {} used on {}", t.move().getName(), t.move().getTarget());
        }
    }

    public static interface RecoilApplier
    {
        RecoilApplier DEFAULT = new RecoilApplier()
        {
        };

        default void applyRecoil(Damage t)
        {
            var moveAppl = t.move();
            MoveEntry move = moveAppl.getMove();
            int dealt = t.dealt;
            float recoil = dealt * move.root_entry._drain / 100.0f;

            if (recoil != 0)
            {
                IPokemob other = PokemobCaps.getPokemobFor(moveAppl.getTarget());
                // This means the move heals as recoil.
                if (recoil > 0)
                {
                    if (PokecubeCore.getConfig().debug_moves) PokecubeAPI.LOGGER
                            .info("Applying recoil healing for move {} of amount {}", t.move().getName(), recoil);
                    recoil = Math.min(recoil, moveAppl.getUser().getMaxHealth() - moveAppl.getUser().getHealth());
                    if (recoil > 0) moveAppl.getUser().getEntity().heal(recoil);
                    MovesUtils.sendPairedMessages(moveAppl.getUser().getEntity(), other, "pokemob.move.recoil.heal");
                }
                // Otherwise it damages as recoil.
                else
                {
                    if (PokecubeCore.getConfig().debug_moves) PokecubeAPI.LOGGER
                            .info("Applying recoil damage for move {} of amount {}", t.move().getName(), recoil);
                    moveAppl.getUser().getEntity().hurt(DamageSource.FALL, -recoil);
                    MovesUtils.sendPairedMessages(moveAppl.getUser().getEntity(), other, "pokemob.move.recoil.damage");
                }
            }
        }
    }

    public static interface HealProvider
    {

        HealProvider DEFAULT = new HealProvider()
        {
        };

        default void applyHealing(Damage t)
        {
            var moveAppl = t.move();
            MoveEntry move = moveAppl.getMove();

            float max_hp = moveAppl.getTarget().getMaxHealth();
            float current_hp = moveAppl.getTarget().getHealth();

            float heal = move.root_entry._healing * max_hp / 100.0f;
            if (heal > 0)
            {
                heal = Math.min(max_hp - current_hp, heal);
                if (PokecubeCore.getConfig().debug_moves)
                    PokecubeAPI.logInfo("Applying healing for move {} of amount {} ({}<-{})", t.move().getName(), heal,
                            move.root_entry._healing, move.root_entry.getMove().healing);
                if (heal > 0) moveAppl.getTarget().heal(heal);
            }
        }
    }

    public static interface AccuracyProvider
    {
        public static AccuracyProvider DEFAULT = new AccuracyProvider()
        {
        };

        default Accuracy applyAccuracy(Accuracy t)
        {
            int moveAcc = t.move().getMove().accuracy;
            IPokemob user = t.move().getUser();
            IPokemob targetPokemob = PokemobCaps.getPokemobFor(t.move().getTarget());
            float efficiency = t.efficiency();
            if (targetPokemob != null)
            {
                var rand = t.move().getTarget().getRandom();
                if (moveAcc > 0)
                {
                    final double accuracy = user.getFloatStat(Stats.ACCURACY, true);
                    final double evasion = targetPokemob.getFloatStat(Stats.EVASION, true);
                    final double moveAccuracy = moveAcc / 100d;
                    final double hitModifier = moveAccuracy * accuracy / evasion;
                    if (hitModifier < rand.nextDouble()) efficiency = -1;
                }
                // ohko moves use a different accuracy application.
                if (t.move().getMove().ohko)
                {
                    final double moveAccuracy = (user.getLevel() - targetPokemob.getLevel() + 30) / 100d;
                    final double hitModifier = user.getLevel() < targetPokemob.getLevel() ? -1 : moveAccuracy;
                    if (hitModifier < rand.nextDouble()) efficiency = -1;
                }
            }
            if (efficiency != t.efficiency()) t = new Accuracy(t.move(), efficiency);
            return t;
        }
    }

    public static interface PostMoveUse
    {
        PostMoveUse DEFAULT = new PostMoveUse()
        {
        };

        default void applyPostMove(Damage t)
        {
            // NO-OP
        }
    }

    public static interface OnMoveFail
    {
        OnMoveFail DEFAULT = new OnMoveFail()
        {
        };

        default void onMoveFail(MoveApplication t)
        {
            // NO-OP
        }
    }

    private IPokemob user;
    private MoveEntry move;
    private LivingEntity target;

    // Move specific things
    public int pwr;
    public int crit;
    public PokeType type;
    public boolean stab = false;

    // Counter to track application, this is for moves that can hit multiple
    // times, or over multiple turns.
    public int apply_number = 0;

    public float status_chance = 0;
    public int status_effects = 0;
    public float stat_chance = 0;
    public int[] stat_effects;

    @Nullable
    public StatDiff applied_stat_effects;

    // Generally the same things
    public float stabFactor = 1.5f;
    public float critFactor = 1.5f;
    // Some abilities may scale specifically super effective moves
    public float superEffectMult = 1;
    // If we are forced to survive, we handle that here, moves such as endure
    // before, or abilities like sturdy can set this.
    public boolean noFaint = false;

    // These are scaling factors on the stat to apply during the move use.
    // Abilities, etc can use these to adjust particular stats when being
    // checked.
    public float[] stat_multipliers =
    { 1, 1, 1, 1, 1, 1, 1, 1, 1 };

    // Things set during the move applications
    public boolean didCrit = false;
    public boolean hit = false;
    public boolean infatuate = false;

    // These two act similar, and prevent move use.

    // This is due to things like already had status, etc
    public boolean failed = false;
    // This is due to abilities, etc cancelling the move.
    public boolean canceled = false;
    // This is whether all application of the move is finished, for marking that
    // the pokemob can forget about it.
    public Supplier<Boolean> finished = () -> true;

    public Damage dealt;

    /**
     * Replace this if you want to adjust status effects, for things like
     * freeze, burn, par, etc.
     */
    public StatusApplier status = StatusApplier.DEFAULT;

    /**
     * Replace this if you want to adjust stat effects, like for evasion, attack
     * increases, etc.
     */
    public StatApplier stats = StatApplier.DEFAULT;

    /**
     * Replace this if you want to adjust accuracy of the move, otherwise it
     * uses whatever the move had in data.
     */
    public AccuracyProvider accuracy = AccuracyProvider.DEFAULT;

    /**
     * This deals the damage, and returns info regarding damage dealt. Replace
     * this if you want to adjust damage itself, and the events are not enough.
     */
    public DamageApplier damage = DamageApplier.DEFAULT;
    /**
     * This applies recoil after damage is dealt. Replace this if you want to
     * adjust recoil mechanics.
     */
    public RecoilApplier recoil = RecoilApplier.DEFAULT;
    /**
     * This applies healing effects, after damage and recoil are applied.
     */
    public HealProvider healer = HealProvider.DEFAULT;
    /**
     * This should return true if the move is attempted at all. By default this
     * handles things like checking status effects, etc.
     */
    public PreApplyTests doRun = PreApplyTests.DEFAULT;
    /**
     * Applies ongoing effects, ie things which are liable to apply to damage
     * over a longer duration, or later in time in general.
     */
    public OngoingApplier applyOngoing = OngoingApplier.NOOP;
    /**
     * Post move application, default is no operation, but this can be used for
     * incrementing timers after move use, etc.
     */
    public PostMoveUse afterUse = PostMoveUse.DEFAULT;
    /**
     * This gets applied if the move has failed or is cancelled.
     */
    public OnMoveFail onFail = OnMoveFail.DEFAULT;

    /**
     * Sounds played on the move use, if this is null, it will use whatever was
     * loaded in.
     */
    public MoveSounds sounds;

    /**
     * Flags that can be assigned to the MoveApplication, for purposes of
     * tracking changes, etc in sub-applications. See Move_Explode for an
     * example of using this.
     */
    public Map<String, Object> customFlags = new HashMap<>();

    /**
     * Collection of UUIDs of mobs this has already applied to. This is used by
     * the EntityMoveUse to decide what mobs to hit. This is also populated with
     * entity uuids for mobs which are invalid targets for the move, such as the
     * user for melee moves.
     */
    public Set<UUID> alreadyHit = Sets.newHashSet();

    public MoveApplication(MoveEntry move, IPokemob user, LivingEntity target)
    {
        this.setTarget(target);
        this.setMove(move);
        this.setUser(user);
    }

    public String getName()
    {
        return getMove().getName();
    }

    public void preApply()
    {
        apply_number = 0;
        this.canceled = this.failed = false;
        this.infatuate = false;
    }

    public void apply()
    {
        // Increment number of times this has been used.
        this.apply_number++;
        if (PokecubeCore.getConfig().debug_moves) PokecubeAPI.logInfo("Applying move: {} used by {}", getMove().name,
                this.getUser().getDisplayName().getString());

        // then basic events and checks.
        // Events are: Pre, Post
        var preEvent = new DuringUse.Pre(this);
        var postEvent = new DuringUse.Post(this);
        // Fire the pre event, if cancelled, assume someone else is handling the
        // moves.
        if (PokecubeAPI.MOVE_BUS.post(preEvent)) return;

        boolean no_run = this.canceled || this.failed;
        // Now check other things, such as possible move failure.
        if (no_run || !(no_run = doRun.checkPreApply(this)) || this.getTarget() == null)
        {
            // for now, buth have the same message, as "cancelled" and
            // "failed" are normally similar causes.

            // If the attacked was null, this message should tell at least
            // the user that things broke.
            MovesUtils.displayEfficiencyMessages(user, getTarget(), -2, 0);

            // We can get here if we had no target, but otherwise the move
            // worked. This can be the case for using move on terrain, etc. In
            // that case, we still want to play sounds.
            if (!no_run) this.getMove().playSounds(this);

            // Run the on failure.
            onFail.onMoveFail(this);

            // Fire the post event, we did not hit, this is apparent in the
            // from didHit == false.
            PokecubeAPI.MOVE_BUS.post(postEvent);
            if (PokecubeCore.getConfig().debug_moves) PokecubeAPI.logInfo("Move Failed or Cancelled!: {} used by {}",
                    getMove().name, this.getUser().getDisplayName().getString());
            return;
        }

        // Once that passes, lets play the sounds for the move.
        this.getMove().playSounds(this);

        // Now process infatuation if it occured
        final IPokemob targetPokemob = PokemobCaps.getPokemobFor(getTarget());
        // Now lets set infatuation if needed
        if (infatuate && targetPokemob != null) targetPokemob.getMoveStats().infatuateTarget = user.getEntity();

        // First apply damage and see if we actually hit
        if (PokecubeCore.getConfig().debug_moves) PokecubeAPI.logInfo("Applying Damage check");
        dealt = damage.applyDamage(this);
        // If this is the case, then lets do others.
        if (dealt.efficiency > 0)
        {
            // First apply stat effects
            if (PokecubeCore.getConfig().debug_moves) PokecubeAPI.logInfo("Applying Stats Checks");
            stats.applyStats(dealt);
            // Next apply status effects
            if (PokecubeCore.getConfig().debug_moves) PokecubeAPI.logInfo("Applying Status Checks");
            status.applyStatus(dealt);
            // Next apply recoil then healing
            if (PokecubeCore.getConfig().debug_moves) PokecubeAPI.logInfo("Applying Recoil Checks");
            recoil.applyRecoil(dealt);
            if (PokecubeCore.getConfig().debug_moves) PokecubeAPI.logInfo("Applying Healing Checks");
            healer.applyHealing(dealt);
            // and finally apply ongoing effects
            if (PokecubeCore.getConfig().debug_moves) PokecubeAPI.logInfo("Applying Ongoing Effect Checks");
            applyOngoing.applyOngoingEffects(dealt);
        }
        // Now apply the after move use, this gets done even if it missed.
        if (PokecubeCore.getConfig().debug_moves) PokecubeAPI.logInfo("Applying Post Move Checks");
        afterUse.applyPostMove(dealt);

        PokecubeAPI.MOVE_BUS.post(postEvent);
    }

    @Nonnull
    public MoveEntry getMove()
    {
        return move;
    }

    @Nonnull
    public IPokemob getUser()
    {
        return user;
    }

    @Nonnull
    public void setMove(MoveEntry move)
    {
        this.move = move;

        this.crit = move.crit;

        this.status_chance = move.root_entry._status_chance;
        this.status_effects = move.root_entry._status_effects;
        this.infatuate = move.root_entry._infatuates;

        this.stat_chance = move.root_entry._stat_chance;
        this.stat_effects = move.root_entry._stat_effects.clone();
    }

    /**
     * This will set the user, but will then also update pwr, type and stab
     * accordingly, so if you do not intend those to change, ensure to reset
     * them after!
     * 
     * @param user
     */
    public void setUser(@Nonnull IPokemob user)
    {
        this.user = user;
        this.pwr = move.getPWR(user, target);
        this.type = move.getType(user);
        this.stab = user.isType(type);
    }

    @Nullable
    public LivingEntity getTarget()
    {
        return target;
    }

    public void setTarget(@Nullable LivingEntity target)
    {
        this.target = target;
    }

    @Override
    public int compareTo(MoveApplication o)
    {
        double d0 = 0;
        double d1 = 0;

        if (getTarget() != null) d0 = getTarget().distanceToSqr(getUser().getEntity());
        if (o.getTarget() != null) d1 = o.getTarget().distanceToSqr(o.getUser().getEntity());

        return Double.compare(d0, d1);
    }

    public boolean isFinished()
    {
        return finished.get();
    }

    public MoveApplication copyForMoveUse()
    {
        var copy = new MoveApplication(getMove(), getUser(), getTarget());
        // Ensure they all share the same already hit set.
        copy.alreadyHit = this.alreadyHit;
        return copy;
    }
}
