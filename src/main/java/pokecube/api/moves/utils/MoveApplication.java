package pokecube.api.moves.utils;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.npc.Npc;
import net.minecraft.world.entity.player.Player;
import pokecube.api.PokecubeAPI;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.entity.pokemob.IPokemob.Stats;
import pokecube.api.entity.pokemob.PokemobCaps;
import pokecube.api.entity.pokemob.ai.GeneralStates;
import pokecube.api.events.pokemobs.combat.MoveUse.DuringUse;
import pokecube.api.moves.MoveEntry;
import pokecube.api.moves.MoveEntry.MoveSounds;
import pokecube.api.utils.PokeType;
import pokecube.core.PokecubeCore;
import pokecube.core.impl.PokecubeMod;
import pokecube.core.moves.MovesUtils;
import pokecube.core.moves.MovesUtils.StatDiff;
import pokecube.core.moves.animations.AnimationMultiAnimations;
import pokecube.core.moves.damage.PokemobDamageSource;
import pokecube.core.utils.EntityTools;
import thut.api.terrain.TerrainManager;
import thut.api.terrain.TerrainSegment;

public class MoveApplication
{
    public static interface PreApplyTests extends Predicate<MoveApplication>
    {
        public static PreApplyTests DEFAULT = new PreApplyTests()
        {
        };

        @Override
        default boolean test(MoveApplication t)
        {
            final IPokemob attackedMob = PokemobCaps.getPokemobFor(t.target);
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

    public static interface StatusApplier extends Consumer<MoveApplication>
    {
        public static StatusApplier DEFAULT = new StatusApplier()
        {
        };
        public static StatusApplier NOOP = new StatusApplier()
        {
            @Override
            public void accept(MoveApplication t)
            {}
        };

        @Override
        default void accept(MoveApplication t)
        {
            LivingEntity target = t.target;
            if (t.status_chance > target.getRandom().nextDouble())
            {
                if (MovesUtils.setStatus(target, t.status_effects))
                    MovesUtils.displayStatusMessages(t.user, target, t.status_effects, true);
            }
        }
    }

    public static interface StatApplier extends Consumer<MoveApplication>
    {
        public static StatApplier DEFAULT = new StatApplier()
        {
        };
        public static StatApplier NOOP = new StatApplier()
        {
            @Override
            public void accept(MoveApplication t)
            {}
        };

        @Override
        default void accept(MoveApplication t)
        {
            LivingEntity target = t.target;
            if (t.stat_chance > 0)
            {
                t.applied_stat_effects = MovesUtils.handleStats(t.user, target, t.stat_effects, t.stat_chance);
                MovesUtils.sendStatDiffsMessages(t.user, target, t.applied_stat_effects);
            }
        }
    }

    public static record Damage(MoveApplication move, float efficiency, int dealt, int healthBefore, int healthAfter)
    {
    };

    public static interface DamageApplier extends Function<MoveApplication, Damage>
    {
        public static DamageApplier DEFAULT = new DamageApplier()
        {
        };

        @Override
        default Damage apply(MoveApplication t)
        {
            IPokemob user = t.user;
            MoveEntry move = t.move;
            PokeType type = t.type;
            LivingEntity target = t.target;

            // This is the pokemob instance of the target, may be null.
            IPokemob targetPokemob = PokemobCaps.getPokemobFor(t.target);
            // This ia a vanilla-equivalent of the attack strength.
            float attackStrength = user.getAttackStrength() * t.pwr / 150;
            // We use the RNG for the target
            var rand = t.target.getRandom();

            // This will scale the damage of the move, for things like
            // not-very-effective, and is also used as a flag for if the move
            // failed/missed/etc
            float efficiency = 1;

            if (targetPokemob != null)
            {
                // Efficiency scales based on typing for pokemobs
                efficiency = PokeType.getAttackEfficiency(type, targetPokemob.getType1(), targetPokemob.getType2());

                // If the target is a pokemob, then attack strength uses the
                // "proper" calculations
                attackStrength = MovesUtils.getAttackStrength(user, targetPokemob, move.getCategory(user), t.pwr, move,
                        t.stat_multipliers);

                // Accuracy can then also factor in the target's stats.
                final int moveAcc = t.accuracy.apply(t);
                if (moveAcc > 0)
                {
                    final double accuracy = user.getFloatStat(Stats.ACCURACY, true);
                    final double evasion = targetPokemob.getFloatStat(Stats.EVASION, true);
                    final double moveAccuracy = moveAcc / 100d;
                    final double hitModifier = moveAccuracy * accuracy / evasion;
                    if (hitModifier < rand.nextDouble()) efficiency = -1;
                }
                // ohko moves use a different accuracy application.
                if (move.ohko)
                {
                    final double moveAccuracy = (user.getLevel() - targetPokemob.getLevel() + 30) / 100d;
                    final double hitModifier = user.getLevel() < targetPokemob.getLevel() ? -1 : moveAccuracy;
                    if (hitModifier < rand.nextDouble()) efficiency = -1;
                }
            }

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

            // TODO apply ongoing effect here.
//            if (move instanceof Move_Ongoing ongoing)
//            {
//                final IOngoingAffected targetAffected = CapabilityAffected.getAffected(attacked);
//                final IOngoingAffected sourceAffected = CapabilityAffected.getAffected(attackerMob);
//                if (ongoing.onTarget() && targetAffected != null)
//                    targetAffected.getEffects().add(ongoing.makeEffect(attackerMob));
//                if (ongoing.onSource() && sourceAffected != null)
//                    sourceAffected.getEffects().add(ongoing.makeEffect(attackerMob));
//            }

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

            boolean self = (move.getAttackCategory(user) & IMoveConstants.CATEGORY_SELF) == 0;

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
                    if (PokecubeMod.debug)
                    {
                        PokecubeAPI.LOGGER.info("Attack Used: " + move.name);
                        PokecubeAPI.LOGGER.info("Normal Component: " + d1);
                        PokecubeAPI.LOGGER.info("Magic Component: " + d2);
                    }
                }
                // Apply attack damage to a pokemob
                else if (targetPokemob != null)
                {
                    final DamageSource source = new PokemobDamageSource(attackerMob, move).setType(type);
                    source.bypassMagic();
                    source.bypassArmor();
                    if (PokecubeMod.debug)
                    {
                        PokecubeAPI.LOGGER.info("Attack Used: " + move.name);
                        PokecubeAPI.LOGGER.info("Attack Damage: " + finalAttackStrength);
                    }
                    target.hurt(source, finalAttackStrength);
                }
                // Apply attack damage to another mob type.
                else
                {
                    final DamageSource source = new PokemobDamageSource(attackerMob, move).setType(type);
                    final boolean damaged = target.hurt(source, finalAttackStrength);
                    if (PokecubeMod.debug)
                    {
                        PokecubeAPI.LOGGER.info("Attack Used: {}, expected damage: {}, Did apply? {} ", move.name,
                                finalAttackStrength, damaged);
                        PokecubeAPI.LOGGER.info("Attack Target: " + target);
                    }
                }

                if (targetPokemob != null)
                {
                    if (t.move.category == IMoveConstants.SPECIAL)
                        targetPokemob.getMoveStats().SPECIALDAMAGETAKENCOUNTER += finalAttackStrength;
                    if (t.move.category == IMoveConstants.PHYSICAL)
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
                final LightningBolt lightning = new LightningBolt(EntityType.LIGHTNING_BOLT, t.target.getLevel());
                t.target.thunderHit((ServerLevel) t.target.getLevel(), lightning);
            }

            // Creepers fear psyhic moves.
            if (t.target instanceof Creeper creeper)
            {
                if (t.type == PokeType.getType("psychic") && creeper.getHealth() > 0) creeper.explodeCreeper();
            }

            return new Damage(t, efficiency, finalAttackStrength, beforeHealth, afterHealth);
        }
    }

    public static interface RecoilApplier extends Consumer<Damage>
    {
        RecoilApplier DEFAULT = new RecoilApplier()
        {
        };

        @Override
        default void accept(Damage t)
        {
            var moveAppl = t.move();
            MoveEntry move = moveAppl.getMove();
            int dealt = t.dealt;
            float recoil = dealt * move.root_entry.move.drain / 100.0f;

            if (recoil != 0)
            {
                // This means the move heals as recoil.
                if (recoil > 0)
                {
                    recoil = Math.min(recoil, moveAppl.getUser().getMaxHealth() - moveAppl.getUser().getHealth());
                    if (recoil > 0) moveAppl.getUser().getEntity().heal(recoil);
                }
                // Otherwise it damages as recoil.
                else
                {
                    moveAppl.getUser().getEntity().hurt(DamageSource.FALL, recoil);
                }

            }
        }
    }

    public static interface HealProvider extends Consumer<Damage>
    {

        HealProvider DEFAULT = new HealProvider()
        {
        };

        @Override
        default void accept(Damage t)
        {
            var moveAppl = t.move();
            MoveEntry move = moveAppl.getMove();

            float max_hp = moveAppl.getUser().getMaxHealth();
            float current_hp = moveAppl.getUser().getHealth();

            float heal = move.root_entry.move.healing * max_hp / 100.0f;
            if (heal > 0)
            {
                heal = Math.min(max_hp - current_hp, heal);
                if (heal > 0) moveAppl.getUser().getEntity().heal(heal);
            }
        }
    }

    public static interface AccuracyProvider extends Function<MoveApplication, Integer>
    {}

    private IPokemob user;
    private MoveEntry move;

    // Move specific things
    public LivingEntity target;
    public int pwr;
    public int crit;
    public PokeType type;
    public boolean stab = false;

    public float status_chance = 0;
    public int status_effects = 0;
    public float stat_chance = 0;
    public int[] stat_effects;

    public StatDiff applied_stat_effects;

    // Generally the same things
    public float stabFactor = 1.5f;
    public float critFactor = 1.5f;
    // Some abilities may scale specifically super effective moves
    public float superEffectMult = 1;
    // If we are forced to survive, we handle that here, moves such as endure
    // before, or abilities like sturdy can set this.
    public boolean noFaint = true;

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
    public boolean failed = false;
    public boolean canceled = false;

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
    public AccuracyProvider accuracy = (move) -> {
        return move.move.accuracy;
    };

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
     * Sounds played on the move use, if this is null, it will use whatever was
     * loaded in.
     */
    public MoveSounds sounds;

    public MoveApplication(MoveEntry move, IPokemob user, LivingEntity target)
    {
        this.user = user;
        this.move = move;

        this.target = target;
        this.crit = move.crit;
        this.pwr = move.getPWR(user, target);
        this.type = move.getType(user);
        this.stab = user.isType(type);

        this.status_chance = move.root_entry._status_chance;
        this.status_effects = move.root_entry._status_effects;
        this.infatuate = move.root_entry._infatuates;

        this.stat_chance = move.root_entry._stat_chance;
        this.stat_effects = move.root_entry._stat_effects.clone();
    }

    public MoveEntry getMove()
    {
        return move;
    }

    public IPokemob getUser()
    {
        return user;
    }

    public String getName()
    {
        return getMove().getName();
    }

    public void apply()
    {
        // Start with basic events and checks.
        // Events are: Pre, Post
        var preEvent = new DuringUse.Pre(this);
        // Fire the pre event, if cancelled, assume someone else is handling the
        // moves.
        if (PokecubeAPI.MOVE_BUS.post(preEvent)) return;

        // Now check other things, such as possible move failure.
        if (this.canceled || this.failed || this.target == null)
        {
            // for now, buth have the same message, as "cancelled" and
            // "failed" are normally similar causes.

            // If the attacked was null, this message should tell at least
            // the user that things broke.
            MovesUtils.displayEfficiencyMessages(user, target, -2, 0);
            return;
        }

        // Check if we should run the move.
        if (!doRun.test(this)) return;

        // Once that passes, lets play the sounds for the move.
        this.getMove().playSounds(this);

        // Now process infatuation if it occured
        final IPokemob targetPokemob = PokemobCaps.getPokemobFor(target);
        // Now lets set infatuation if needed
        if (infatuate && targetPokemob != null) targetPokemob.getMoveStats().infatuateTarget = user.getEntity();

        // First apply damage and see if we actually hit
        dealt = damage.apply(this);
        // If this is the case, then lets do others.
        if (dealt.efficiency > 0)
        {
            // First apply stat effects
            stats.accept(this);
            // Next apply status effects
            status.accept(this);
            // Finally apply recoil then healing
            recoil.accept(dealt);
            healer.accept(dealt);
        }
        var postEvent = new DuringUse.Post(this);
        PokecubeAPI.MOVE_BUS.post(postEvent);
    }

    public void setUser(IPokemob user)
    {
        this.user = user;
    }
}
