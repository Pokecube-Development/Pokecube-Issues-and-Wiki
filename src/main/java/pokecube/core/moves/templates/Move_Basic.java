/**
 *
 */
package pokecube.core.moves.templates;

import java.util.Random;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.npc.Npc;
import net.minecraft.world.entity.player.Player;
import pokecube.api.PokecubeAPI;
import pokecube.api.entity.CapabilityAffected;
import pokecube.api.entity.IOngoingAffected;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.entity.pokemob.IPokemob.Stats;
import pokecube.api.entity.pokemob.PokemobCaps;
import pokecube.api.entity.pokemob.ai.GeneralStates;
import pokecube.api.entity.pokemob.moves.MovePacket;
import pokecube.api.events.pokemobs.combat.MoveUse;
import pokecube.api.moves.MoveEntry;
import pokecube.api.moves.Move_Base;
import pokecube.api.moves.utils.IMoveConstants;
import pokecube.api.utils.PokeType;
import pokecube.core.PokecubeCore;
import pokecube.core.impl.PokecubeMod;
import pokecube.core.moves.MovesUtils;
import pokecube.core.moves.animations.AnimationMultiAnimations;
import pokecube.core.moves.damage.PokemobDamageSource;
import pokecube.core.utils.EntityTools;
import thut.api.terrain.TerrainManager;
import thut.api.terrain.TerrainSegment;
import thut.core.common.ThutCore;

/** @author Manchou */
public class Move_Basic extends Move_Base implements IMoveConstants
{

    /**
     * Constructor for a Pokemob move. <br/>
     * The attack category defines the way the mob will move in order to make
     * its attack.
     *
     * @param name           the English name of the attack, used as identifier
     *                       and translation key
     * @param attackCategory can be either {@link MovesUtils#CATEGORY_CONTACT}
     *                       or {@link MovesUtils#CATEGORY_DISTANCE}
     */
    public Move_Basic(final String name)
    {
        super(name);
    }

    @Override
    public void attack(final IPokemob attacker, final LivingEntity attacked)
    {
        final IPokemob attackedMob = PokemobCaps.getPokemobFor(attacked);
        if ((attacker.getStatus() & IMoveConstants.STATUS_SLP) > 0)
        {
            if (attackedMob != null)
                MovesUtils.displayStatusMessages(attackedMob, attacker.getEntity(), IMoveConstants.STATUS_SLP, false);
            else MovesUtils.displayStatusMessages(null, attacker.getEntity(), IMoveConstants.STATUS_SLP, false);
            return;
        }
        if ((attacker.getStatus() & IMoveConstants.STATUS_FRZ) > 0)
        {
            if (attackedMob != null)
                MovesUtils.displayStatusMessages(attackedMob, attacker.getEntity(), IMoveConstants.STATUS_FRZ, false);
            else MovesUtils.displayStatusMessages(null, attacker.getEntity(), IMoveConstants.STATUS_FRZ, false);
            return;
        }
        if ((attacker.getStatus() & IMoveConstants.STATUS_PAR) > 0 && Math.random() > 0.75)
        {
            if (attackedMob != null)
                MovesUtils.displayStatusMessages(attackedMob, attacker.getEntity(), IMoveConstants.STATUS_PAR, false);
            else MovesUtils.displayStatusMessages(null, attacker.getEntity(), IMoveConstants.STATUS_PAR, false);
            return;
        }
        if (AnimationMultiAnimations.isThunderAnimation(this.move.getAnimation(attacker)))
        {
            final LightningBolt lightning = new LightningBolt(EntityType.LIGHTNING_BOLT, attacked.getLevel());
            attacked.thunderHit((ServerLevel) attacked.getLevel(), lightning);
        }
        if (attacked instanceof Creeper creeper)
        {
            if (this.move.type == PokeType.getType("psychic") && creeper.getHealth() > 0) creeper.explodeCreeper();
        }
        this.playSounds(attacker.getEntity(), attacked, null);
        byte statusChange = IMoveConstants.STATUS_NON;
        byte changeAddition = IMoveConstants.CHANGE_NONE;
        if (this.move.statusChange != IMoveConstants.STATUS_NON
                && MovesUtils.rand.nextFloat() <= this.move.statusChance)
            statusChange = this.move.statusChange;
        if (this.move.change != IMoveConstants.CHANGE_NONE && MovesUtils.rand.nextFloat() <= this.move.chanceChance)
            changeAddition = this.move.change;
        final MovePacket packet = new MovePacket(attacker, attacked, this.name, this.move.getType(attacker),
                this.move.getPWR(attacker, attacked), this.move.crit, statusChange, changeAddition);

        final boolean self = this.isSelfMove();
        boolean doAttack = true;
        if (!self) doAttack = attacked != attacker;
        if (doAttack) this.onAttack(packet);
    }

    @Override
    public Move_Base getMove(final String name)
    {
        return MovesUtils.getMoveFromName(name);
    }

    @Override
    public void handleStatsChanges(final MovePacket packet)
    {
        final boolean shouldEffect = packet.attackedStatModProb > 0 || packet.attackerStatModProb > 0;
        if (!shouldEffect) return;
        boolean effect = false;
        if (packet.getMove().hasStatModTarget && packet.hit)
            effect = MovesUtils.handleStats(packet.attacker, packet.attacked, packet, true);
        if (packet.getMove().hasStatModSelf)
            effect = MovesUtils.handleStats(packet.attacker, packet.attacker.getEntity(), packet, false);
        if (!effect) MovesUtils.displayStatsMessage(packet.attacker, packet.attacked, -2, (byte) 0, (byte) 0);
    }

    @Override
    public void onAttack(MovePacket packet)
    {
        this.preAttack(packet);
        if (packet.denied) return;

        final IPokemob attacker = packet.attacker;
        final LivingEntity attackerMob = attacker.getEntity();
        final LivingEntity attacked = packet.attacked;

        final LivingEntity attackedHp = EntityTools.getCoreLiving(attacked);

        final IPokemob targetPokemob = PokemobCaps.getPokemobFor(attacked);
        final Random rand = ThutCore.newRandom();
        final String attack = packet.attack;
        final PokeType type = packet.attackType;
        final int PWR = packet.PWR;
        int criticalLevel = packet.criticalLevel;
        final float criticalFactor = packet.critFactor;
        final int statusChange = packet.statusChange;
        final int changeAddition = packet.changeAddition;
        float stabFactor = packet.stabFactor;
        if (!packet.stab) packet.stab = packet.attacker.isType(type);
        if (!packet.stab) stabFactor = 1;
        if (packet.canceled)
        {
            MovesUtils.displayEfficiencyMessages(attacker, attacked, -2, 0);
            packet = new MovePacket(attacker, attacked, attack, type, PWR, criticalLevel, statusChange, changeAddition,
                    false);
            packet.hit = false;
            packet.didCrit = false;
            this.postAttack(packet);
            return;
        }
        if (packet.failed)
        {
            MovesUtils.displayEfficiencyMessages(attacker, attacked, -2, 0);
            packet = new MovePacket(attacker, attacked, attack, type, PWR, criticalLevel, statusChange, changeAddition,
                    false);
            packet.hit = false;
            packet.didCrit = false;
            this.postAttack(packet);
            return;
        }

        if (packet.infatuateTarget && targetPokemob != null)
            targetPokemob.getMoveStats().infatuateTarget = attacker.getEntity();

        if (packet.infatuateAttacker) attacker.getMoveStats().infatuateTarget = attacked;
        if (attacked == null)
        {
            packet = new MovePacket(attacker, attacked, attack, type, PWR, criticalLevel, statusChange, changeAddition,
                    false);
            packet.hit = false;
            packet.didCrit = false;
            this.postAttack(packet);
            return;
        }

        float efficiency = 1;

        if (targetPokemob != null)
        {
            efficiency = PokeType.getAttackEfficiency(type, targetPokemob.getType1(), targetPokemob.getType2());
            if (efficiency > 0 && packet.getMove().fixed) efficiency = 1;
        }

        float criticalRatio = 1;

        if (attacker.getMoveStats().SPECIALTYPE == IPokemob.TYPE_CRIT)
        {
            criticalLevel += 1;
            attacker.getMoveStats().SPECIALTYPE = 0;
        }

        int critcalRate = 16;

        if (criticalLevel == 1) critcalRate = 16;
        else if (criticalLevel == 2) critcalRate = 8;
        else if (criticalLevel == 3) critcalRate = 4;
        else if (criticalLevel == 4) critcalRate = 3;
        else if (criticalLevel == 5) critcalRate = 2;
        else critcalRate = 1;

        if (criticalLevel > 0 && rand.nextInt(critcalRate) == 0) criticalRatio = criticalFactor;

        float attackStrength = attacker.getAttackStrength() * PWR / 150;

        if (targetPokemob != null)
        {
            attackStrength = MovesUtils.getAttackStrength(attacker, targetPokemob,
                    packet.getMove().getCategory(attacker), PWR, packet.getMove(), packet.statMults);

            final int moveAcc = packet.getMove().accuracy;
            if (moveAcc > 0)
            {
                final double accuracy = attacker.getFloatStat(Stats.ACCURACY, true);
                final double evasion = targetPokemob.getFloatStat(Stats.EVASION, true);
                final double moveAccuracy = moveAcc / 100d;

                final double hitModifier = moveAccuracy * accuracy / evasion;

                if (hitModifier < Math.random()) efficiency = -1;
            }
            if (moveAcc == -3)
            {
                final double moveAccuracy = (attacker.getLevel() - targetPokemob.getLevel() + 30) / 100d;

                final double hitModifier = attacker.getLevel() < targetPokemob.getLevel() ? -1 : moveAccuracy;

                if (hitModifier < Math.random()) efficiency = -1;
            }
        }
        if (efficiency > 0 && packet.applyOngoing)
        {
            if (MovesUtils.getMoveFromName(attack) instanceof Move_Ongoing ongoing)
            {
                final IOngoingAffected targetAffected = CapabilityAffected.getAffected(attacked);
                final IOngoingAffected sourceAffected = CapabilityAffected.getAffected(attackerMob);
                if (ongoing.onTarget() && targetAffected != null)
                    targetAffected.getEffects().add(ongoing.makeEffect(attackerMob));
                if (ongoing.onSource() && sourceAffected != null)
                    sourceAffected.getEffects().add(ongoing.makeEffect(attackerMob));
            }
        }
        final TerrainSegment terrain = TerrainManager.getInstance().getTerrainForEntity(attackerMob);
        float terrainDamageModifier = MovesUtils.getTerrainDamageModifier(type, attackerMob, terrain);

        if (packet.getMove().fixed)
        {
            criticalRatio = 1;
            terrainDamageModifier = 1;
            stabFactor = 1;
            packet.superEffectMult = 1;
        }

        int finalAttackStrength = Math.max(0, Math.round(attackStrength * efficiency * criticalRatio
                * terrainDamageModifier * stabFactor * packet.superEffectMult));

        // Apply configs for scaling attack damage by category.
        if (finalAttackStrength > 0)
        {
            final double damageRatio = (packet.getMove().getAttackCategory(packet.attacker)
                    & IMoveConstants.CATEGORY_CONTACT) > 0 ? PokecubeCore.getConfig().contactAttackDamageScale
                            : PokecubeCore.getConfig().rangedAttackDamageScale;
            finalAttackStrength = (int) Math.max(1, finalAttackStrength * damageRatio);
        }

        float healRatio;
        float damageRatio;

        int beforeHealth = 0;

        if (attackedHp != null) beforeHealth = (int) attackedHp.getHealth();

        if (efficiency > 0 && MoveEntry.oneHitKos.contains(attack)) finalAttackStrength = beforeHealth;

        final boolean toSurvive = packet.noFaint;
        if (toSurvive)
        {
            finalAttackStrength = Math.min(finalAttackStrength, beforeHealth - 1);
            finalAttackStrength = Math.max(0, finalAttackStrength);
        }

        final boolean wild = !attacker.getGeneralState(GeneralStates.TAMED);

        if (PokecubeCore.getConfig().maxWildPlayerDamage >= 0 && wild && attacked instanceof Player)
            finalAttackStrength = Math.min(PokecubeCore.getConfig().maxWildPlayerDamage, finalAttackStrength);
        else if (PokecubeCore.getConfig().maxOwnedPlayerDamage >= 0 && !wild && attacked instanceof Player)
            finalAttackStrength = Math.min(PokecubeCore.getConfig().maxOwnedPlayerDamage, finalAttackStrength);
        double scaleFactor = 1;
        if (attacked instanceof Player)
        {
            final boolean owner = attacked == attacker.getOwner();
            if (!owner || PokecubeCore.getConfig().pokemobsDamageOwner)
                scaleFactor = PokecubeCore.getConfig().pokemobsDamagePlayers
                        ? wild ? PokecubeCore.getConfig().wildPlayerDamageRatio
                                : PokecubeCore.getConfig().ownedPlayerDamageRatio
                        : 0;
            else scaleFactor = 0;
        }
        else if (targetPokemob == null)
            scaleFactor = attacked instanceof Npc ? PokecubeCore.getConfig().pokemobToNPCDamageRatio
                    : PokecubeCore.getConfig().pokemobToOtherMobDamageRatio;
        finalAttackStrength *= scaleFactor;

        if (targetPokemob != null) if (targetPokemob.getAbility() != null)
            finalAttackStrength = targetPokemob.getAbility().beforeDamage(targetPokemob, packet.convert(), finalAttackStrength);

        boolean self = (this.getAttackCategory(packet.attacker) & IMoveConstants.CATEGORY_SELF) == 0;

        if (self && this.move.defrosts && targetPokemob != null
                && (targetPokemob.getStatus() & IMoveConstants.STATUS_FRZ) > 0)
            targetPokemob.healStatus();

        if (!(self && PWR == 0) && finalAttackStrength > 0 && !attacked.isInvulnerable())
        {
            // Apply attack damage to players.
            if (attacked instanceof Player)
            {
                final DamageSource source1 = new PokemobDamageSource(attackerMob, MovesUtils.getMove(attack))
                        .setType(type);
                final DamageSource source2 = new PokemobDamageSource(attackerMob, MovesUtils.getMove(attack))
                        .setType(type);
                source2.setMagic();
                float d1, d2;
                if (wild)
                {
                    d2 = (float) (finalAttackStrength * Math.min(1, PokecubeCore.getConfig().wildPlayerDamageMagic));
                    d1 = finalAttackStrength - d2;
                }
                else
                {
                    d2 = (float) (finalAttackStrength * Math.min(1, PokecubeCore.getConfig().ownedPlayerDamageMagic));
                    d1 = finalAttackStrength - d2;
                }
                attacked.hurt(source1, d1);
                attacked.hurt(source2, d2);
                if (PokecubeMod.debug)
                {
                    PokecubeAPI.LOGGER.info("Attack Used: " + attack);
                    PokecubeAPI.LOGGER.info("Normal Component: " + d1);
                    PokecubeAPI.LOGGER.info("Magic Component: " + d2);
                }
            }
            // Apply attack damage to a pokemob
            else if (targetPokemob != null)
            {
                final DamageSource source = new PokemobDamageSource(attackerMob, MovesUtils.getMove(attack))
                        .setType(type);
                source.bypassMagic();
                source.bypassArmor();
                if (PokecubeMod.debug)
                {
                    PokecubeAPI.LOGGER.info("Attack Used: " + attack);
                    PokecubeAPI.LOGGER.info("Attack Damage: " + finalAttackStrength);
                }
                attacked.hurt(source, finalAttackStrength);
            }
            // Apply attack damage to another mob type.
            else
            {
                final DamageSource source = new PokemobDamageSource(attackerMob, MovesUtils.getMove(attack))
                        .setType(type);
                final boolean damaged = attacked.hurt(source, finalAttackStrength);
                if (PokecubeMod.debug)
                {
                    PokecubeAPI.LOGGER.info("Attack Used: {}, expected damage: {}, Did apply? {} ", attack,
                            finalAttackStrength, damaged);
                    PokecubeAPI.LOGGER.info("Attack Target: " + attacked);
                }
            }

            if (targetPokemob != null)
            {
                if (this.move.category == IMoveConstants.SPECIAL)
                    targetPokemob.getMoveStats().SPECIALDAMAGETAKENCOUNTER += finalAttackStrength;
                if (this.move.category == IMoveConstants.PHYSICAL)
                    targetPokemob.getMoveStats().PHYSICALDAMAGETAKENCOUNTER += finalAttackStrength;
            }
        }

        if (finalAttackStrength > 0)
            MovesUtils.displayEfficiencyMessages(attacker, attacked, efficiency, criticalRatio);

        if ((efficiency > 0 || self) && statusChange != IMoveConstants.STATUS_NON)
            if (MovesUtils.setStatus(attacked, statusChange))
                MovesUtils.displayStatusMessages(attacker, attacked, statusChange, true);
            else MovesUtils.displayEfficiencyMessages(attacker, attacked, -2, 0);
        if (efficiency > 0 && changeAddition != IMoveConstants.CHANGE_NONE)
            MovesUtils.addChange(attacked, attacker, changeAddition);

        int afterHealth = 0;
        if (attackedHp != null) afterHealth = (int) attackedHp.getHealth();

        final int damageDealt = beforeHealth - afterHealth;

        healRatio = packet.getMove().damageHeal;
        damageRatio = packet.getMove().selfDamage;
        if (damageRatio > 0)
        {
            if (packet.getMove().selfDamageType == MoveEntry.TOTALHP)
            {
                final float max = attackerMob.getMaxHealth();
                final float diff = max * damageRatio;
                attackerMob.setHealth(max - diff);
            }
            if (packet.getMove().selfDamageType == MoveEntry.MISS && efficiency <= 0)
            {
                final float max = attackerMob.getMaxHealth();
                final float diff = max * damageRatio;
                attackerMob.hurt(DamageSource.FALL, diff);
            }
            if (packet.getMove().selfDamageType == MoveEntry.DAMAGEDEALT)
            {
                final float diff = damageDealt * damageRatio;
                attackerMob.hurt(DamageSource.FALL, diff);
            }
            if (packet.getMove().selfDamageType == MoveEntry.RELATIVEHP)
            {
                final float current = attackerMob.getHealth();
                final float diff = current * damageRatio;
                attackerMob.hurt(DamageSource.FALL, diff);
            }
        }

        if (healRatio > 0)
        {
            final float toHeal = Math.max(1, damageDealt * healRatio);
            attackerMob.setHealth(Math.min(attackerMob.getMaxHealth(), attackerMob.getHealth() + toHeal));
        }

        healRatio = this.getSelfHealRatio(attacker);
        boolean canHeal = attackerMob.getHealth() < attackerMob.getMaxHealth();
        if (healRatio > 0 && canHeal) attackerMob.setHealth(
                Math.min(attackerMob.getMaxHealth(), attackerMob.getHealth() + attackerMob.getMaxHealth() * healRatio));

        healRatio = this.getTargetHealRatio(attacker);
        canHeal = attackedHp.getHealth() < attackedHp.getMaxHealth();
        if (healRatio > 0 && canHeal) attackedHp.setHealth(
                Math.min(attackedHp.getMaxHealth(), attackedHp.getHealth() + attackedHp.getMaxHealth() * healRatio));

        packet = new MovePacket(attacker, attacked, attack, type, PWR, criticalLevel, statusChange, changeAddition,
                false);
        packet.hit = efficiency >= 0;
        packet.didCrit = criticalRatio > 1;
        packet.damageDealt = beforeHealth - afterHealth;
        this.handleStatsChanges(packet);
        this.postAttack(packet);
    }

    @Override
    public void postAttack(final MovePacket packet)
    {
        PokecubeAPI.MOVE_BUS.post(new MoveUse.ActualMoveUse.Post(packet.attacker, this.move, packet.attacked));
    }

    @Override
    public void preAttack(final MovePacket packet)
    {
        PokecubeAPI.MOVE_BUS.post(new MoveUse.ActualMoveUse.Pre(packet.attacker, this.move, packet.attacked));
    }
}
