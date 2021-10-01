/**
 *
 */
package pokecube.core.moves.templates;

import java.util.Random;

import net.minecraft.block.BlockState;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.INPC;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.LightningBoltEntity;
import net.minecraft.entity.monster.CreeperEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import pokecube.core.PokecubeCore;
import pokecube.core.database.abilities.Ability;
import pokecube.core.database.moves.MoveEntry;
import pokecube.core.events.pokemob.combat.MoveUse;
import pokecube.core.events.pokemob.combat.MoveUse.MoveWorldAction;
import pokecube.core.interfaces.IMoveConstants;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.IPokemob.Stats;
import pokecube.core.interfaces.Move_Base;
import pokecube.core.interfaces.PokecubeMod;
import pokecube.core.interfaces.capabilities.CapabilityAffected;
import pokecube.core.interfaces.capabilities.CapabilityPokemob;
import pokecube.core.interfaces.entity.IOngoingAffected;
import pokecube.core.interfaces.pokemob.ai.GeneralStates;
import pokecube.core.interfaces.pokemob.moves.MovePacket;
import pokecube.core.moves.MovesUtils;
import pokecube.core.moves.animations.AnimationMultiAnimations;
import pokecube.core.moves.damage.PokemobDamageSource;
import pokecube.core.utils.EntityTools;
import pokecube.core.utils.PokeType;
import thut.api.maths.Vector3;
import thut.api.terrain.TerrainManager;
import thut.api.terrain.TerrainSegment;
import thut.core.common.ThutCore;

/** @author Manchou */
public class Move_Basic extends Move_Base implements IMoveConstants
{
    public static boolean shouldSilk(final IPokemob pokemob)
    {
        if (pokemob.getAbility() == null) return false;
        final Ability ability = pokemob.getAbility();
        return pokemob.getLevel() >= 90 && ability.toString().equalsIgnoreCase("hypercutter");
    }

    public static void silkHarvest(final BlockState state, final BlockPos pos, final World worldIn,
            final PlayerEntity player)
    {
        final ItemStack pickaxe = new ItemStack(Items.DIAMOND_PICKAXE);
        pickaxe.enchant(Enchantments.SILK_TOUCH, 1);
        state.getBlock().playerDestroy(worldIn, player, pos, state, null, pickaxe);
        worldIn.destroyBlock(pos, false);
    }

    Vector3 v = Vector3.getNewVector();

    Vector3 v1 = Vector3.getNewVector();

    /**
     * Constructor for a Pokemob move. <br/>
     * The attack category defines the way the mob will move in order to make
     * its attack.
     *
     * @param name
     *            the English name of the attack, used as identifier and
     *            translation key
     * @param attackCategory
     *            can be either {@link MovesUtils#CATEGORY_CONTACT} or
     *            {@link MovesUtils#CATEGORY_DISTANCE}
     */
    public Move_Basic(final String name)
    {
        super(name);
    }

    @Override
    public void applyHungerCost(final IPokemob attacker)
    {
        final int pp = this.getPP();
        float relative = (50 - pp) / 30;
        relative = relative * relative;
        attacker.applyHunger((int) (relative * 100));
    }

    @Override
    public void attack(final IPokemob attacker, final Entity attacked)
    {
        final IPokemob attackedMob = CapabilityPokemob.getPokemobFor(attacked);
        if ((attacker.getStatus() & IMoveConstants.STATUS_SLP) > 0)
        {
            if (attackedMob != null) MovesUtils.displayStatusMessages(attackedMob, attacker.getEntity(),
                    IMoveConstants.STATUS_SLP, false);
            else MovesUtils.displayStatusMessages(null, attacker.getEntity(), IMoveConstants.STATUS_SLP, false);
            return;
        }
        if ((attacker.getStatus() & IMoveConstants.STATUS_FRZ) > 0)
        {
            if (attackedMob != null) MovesUtils.displayStatusMessages(attackedMob, attacker.getEntity(),
                    IMoveConstants.STATUS_FRZ, false);
            else MovesUtils.displayStatusMessages(null, attacker.getEntity(), IMoveConstants.STATUS_FRZ, false);
            return;
        }
        if ((attacker.getStatus() & IMoveConstants.STATUS_PAR) > 0 && Math.random() > 0.75)
        {
            if (attackedMob != null) MovesUtils.displayStatusMessages(attackedMob, attacker.getEntity(),
                    IMoveConstants.STATUS_PAR, false);
            else MovesUtils.displayStatusMessages(null, attacker.getEntity(), IMoveConstants.STATUS_PAR, false);
            return;
        }
        if (AnimationMultiAnimations.isThunderAnimation(this.getAnimation(attacker)))
        {
            final LightningBoltEntity lightning = new LightningBoltEntity(EntityType.LIGHTNING_BOLT, attacked
                    .getCommandSenderWorld());
            attacked.thunderHit((ServerWorld) attacked.getCommandSenderWorld(), lightning);
        }
        if (attacked instanceof CreeperEntity)
        {
            final CreeperEntity creeper = (CreeperEntity) attacked;
            if (this.move.type == PokeType.getType("psychic") && creeper.getHealth() > 0) creeper.explodeCreeper();
        }
        this.playSounds(attacker.getEntity(), attacked, null);
        byte statusChange = IMoveConstants.STATUS_NON;
        byte changeAddition = IMoveConstants.CHANGE_NONE;
        if (this.move.statusChange != IMoveConstants.STATUS_NON && MovesUtils.rand
                .nextFloat() <= this.move.statusChance) statusChange = this.move.statusChange;
        if (this.move.change != IMoveConstants.CHANGE_NONE && MovesUtils.rand.nextFloat() <= this.move.chanceChance)
            changeAddition = this.move.change;
        final MovePacket packet = new MovePacket(attacker, attacked, this.name, this.getType(attacker), this.getPWR(
                attacker, attacked), this.move.crit, statusChange, changeAddition);

        final boolean self = this.isSelfMove();
        boolean doAttack = true;
        if (!self) doAttack = attacked != attacker;
        if (doAttack) this.onAttack(packet);
    }

    @Override
    public void doWorldAction(final IPokemob attacker, Vector3 location)
    {
        final Vector3 origin = Vector3.getNewVector().set(attacker.getEntity().getEyePosition(0));
        final Vector3 direction = location.subtract(origin).norm().scalarMultBy(0.5);
        location = location.add(direction);
        final MoveWorldAction.PreAction preEvent = new MoveWorldAction.PreAction(this, attacker, location);
        if (!PokecubeCore.MOVE_BUS.post(preEvent))
        {
            final MoveWorldAction.OnAction onEvent = new MoveWorldAction.OnAction(this, attacker, location);
            PokecubeCore.MOVE_BUS.post(onEvent);
            final MoveWorldAction.PostAction postEvent = new MoveWorldAction.PostAction(this, attacker, location);
            PokecubeCore.MOVE_BUS.post(postEvent);
        }
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
        if (packet.getMove().hasStatModTarget && packet.hit) effect = MovesUtils.handleStats(packet.attacker,
                packet.attacked, packet, true);
        if (packet.getMove().hasStatModSelf) effect = MovesUtils.handleStats(packet.attacker, packet.attacker
                .getEntity(), packet, false);
        if (!effect) MovesUtils.displayStatsMessage(packet.attacker, packet.attacked, -2, (byte) 0, (byte) 0);
    }

    @Override
    public void onAttack(MovePacket packet)
    {
        this.preAttack(packet);
        if (packet.denied) return;

        final IPokemob attacker = packet.attacker;
        final LivingEntity attackerMob = attacker.getEntity();
        final Entity attacked = packet.attacked;

        final LivingEntity attackedHp = EntityTools.getCoreLiving(attacked);

        final IPokemob targetPokemob = CapabilityPokemob.getPokemobFor(attacked);
        final Random rand = ThutCore.newRandom();
        final String attack = packet.attack;
        final PokeType type = packet.attackType;
        final int PWR = packet.PWR;
        int criticalLevel = packet.criticalLevel;
        final float criticalFactor = packet.critFactor;
        final byte statusChange = packet.statusChange;
        final byte changeAddition = packet.changeAddition;
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

        if (packet.infatuateTarget && targetPokemob != null) targetPokemob.getMoveStats().infatuateTarget = attacker
                .getEntity();

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
            if (efficiency > 0 && packet.getMove().fixedDamage) efficiency = 1;
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
            attackStrength = MovesUtils.getAttackStrength(attacker, targetPokemob, packet.getMove().getCategory(
                    attacker), PWR, packet);

            final int moveAcc = packet.getMove().move.accuracy;
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
            Move_Ongoing ongoing;
            if (MovesUtils.getMoveFromName(attack) instanceof Move_Ongoing)
            {
                ongoing = (Move_Ongoing) MovesUtils.getMoveFromName(attack);
                final IOngoingAffected targetAffected = CapabilityAffected.getAffected(attacked);
                final IOngoingAffected sourceAffected = CapabilityAffected.getAffected(attackerMob);
                if (ongoing.onTarget() && targetAffected != null) targetAffected.getEffects().add(ongoing.makeEffect(
                        attackerMob));
                if (ongoing.onSource() && sourceAffected != null) sourceAffected.getEffects().add(ongoing.makeEffect(
                        attackerMob));
            }
        }
        final TerrainSegment terrain = TerrainManager.getInstance().getTerrainForEntity(attackerMob);
        float terrainDamageModifier = MovesUtils.getTerrainDamageModifier(type, attackerMob, terrain);

        if (packet.getMove().fixedDamage)
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
            final double damageRatio = (packet.getMove().getAttackCategory() & IMoveConstants.CATEGORY_CONTACT) > 0
                    ? PokecubeCore.getConfig().contactAttackDamageScale
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

        if (PokecubeCore.getConfig().maxWildPlayerDamage >= 0 && wild && attacked instanceof PlayerEntity)
            finalAttackStrength = Math.min(PokecubeCore.getConfig().maxWildPlayerDamage, finalAttackStrength);
        else if (PokecubeCore.getConfig().maxOwnedPlayerDamage >= 0 && !wild && attacked instanceof PlayerEntity)
            finalAttackStrength = Math.min(PokecubeCore.getConfig().maxOwnedPlayerDamage, finalAttackStrength);
        double scaleFactor = 1;
        if (attacked instanceof PlayerEntity)
        {
            final boolean owner = attacked == attacker.getOwner();
            if (!owner || PokecubeCore.getConfig().pokemobsDamageOwner) scaleFactor = PokecubeCore
                    .getConfig().pokemobsDamagePlayers ? wild ? PokecubeCore.getConfig().wildPlayerDamageRatio
                            : PokecubeCore.getConfig().ownedPlayerDamageRatio : 0;
            else scaleFactor = 0;
        }
        else if (targetPokemob == null) scaleFactor = attacked instanceof INPC ? PokecubeCore
                .getConfig().pokemobToNPCDamageRatio : PokecubeCore.getConfig().pokemobToOtherMobDamageRatio;
        finalAttackStrength *= scaleFactor;

        if (targetPokemob != null) if (targetPokemob.getAbility() != null) finalAttackStrength = targetPokemob
                .getAbility().beforeDamage(targetPokemob, packet, finalAttackStrength);

        if ((this.getAttackCategory() & IMoveConstants.CATEGORY_SELF) == 0 && this.move.defrosts
                && targetPokemob != null && (targetPokemob.getStatus() & IMoveConstants.STATUS_FRZ) > 0) targetPokemob
                        .healStatus();

        if (!((this.getAttackCategory() & IMoveConstants.CATEGORY_SELF) > 0 && PWR == 0) && finalAttackStrength > 0
                && !attacked.isInvulnerable())
        {
            // Apply attack damage to players.
            if (attacked instanceof PlayerEntity)
            {
                final DamageSource source1 = new PokemobDamageSource(attackerMob, MovesUtils.getMoveFromName(attack))
                        .setType(type);
                final DamageSource source2 = new PokemobDamageSource(attackerMob, MovesUtils.getMoveFromName(attack))
                        .setType(type);
                source2.bypassArmor();
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
                    PokecubeCore.LOGGER.info("Attack Used: " + attack);
                    PokecubeCore.LOGGER.info("Normal Component: " + d1);
                    PokecubeCore.LOGGER.info("Magic Component: " + d2);
                }
            }
            // Apply attack damage to a pokemob
            else if (targetPokemob != null)
            {
                final DamageSource source = new PokemobDamageSource(attackerMob, MovesUtils.getMoveFromName(attack))
                        .setType(type);
                source.bypassMagic();
                source.bypassArmor();
                if (PokecubeMod.debug)
                {
                    PokecubeCore.LOGGER.info("Attack Used: " + attack);
                    PokecubeCore.LOGGER.info("Attack Damage: " + finalAttackStrength);
                }
                attacked.hurt(source, finalAttackStrength);
            }
            // Apply attack damage to another mob type.
            else
            {
                final DamageSource source = new PokemobDamageSource(attackerMob, MovesUtils.getMoveFromName(attack))
                        .setType(type);
                final boolean damaged = attacked.hurt(source, finalAttackStrength);
                if (PokecubeMod.debug)
                {
                    PokecubeCore.LOGGER.info("Attack Used: {}, expected damage: {}, Did apply? {} ", attack,
                            finalAttackStrength, damaged);
                    PokecubeCore.LOGGER.info("Attack Target: " + attacked);
                }
            }

            if (targetPokemob != null)
            {
                if (this.move.category == IMoveConstants.SPECIAL) targetPokemob
                        .getMoveStats().SPECIALDAMAGETAKENCOUNTER += finalAttackStrength;
                if (this.move.category == IMoveConstants.PHYSICAL) targetPokemob
                        .getMoveStats().PHYSICALDAMAGETAKENCOUNTER += finalAttackStrength;
            }
        }

        if ((efficiency > 0 || packet.getMove().getAttackCategory() == IMoveConstants.CATEGORY_SELF)
                && statusChange != IMoveConstants.STATUS_NON) if (MovesUtils.setStatus(attacked, statusChange))
                    MovesUtils.displayStatusMessages(attacker, attacked, statusChange, true);
        else MovesUtils.displayEfficiencyMessages(attacker, attacked, -2, 0);
        if (efficiency > 0 && changeAddition != IMoveConstants.CHANGE_NONE) MovesUtils.addChange(attacked, attacker,
                changeAddition);

        if (finalAttackStrength > 0) MovesUtils.displayEfficiencyMessages(attacker, attacked, efficiency,
                criticalRatio);

        int afterHealth = 0;
        if (attackedHp != null) afterHealth = (int) attackedHp.getHealth();

        final int damageDealt = beforeHealth - afterHealth;

        healRatio = packet.getMove().move.damageHeal;
        damageRatio = packet.getMove().move.selfDamage;
        if (damageRatio > 0)
        {
            if (packet.getMove().move.selfDamageType == MoveEntry.TOTALHP)
            {
                final float max = attackerMob.getMaxHealth();
                final float diff = max * damageRatio;
                attackerMob.setHealth(max - diff);
            }
            if (packet.getMove().move.selfDamageType == MoveEntry.MISS && efficiency <= 0)
            {
                final float max = attackerMob.getMaxHealth();
                final float diff = max * damageRatio;
                attackerMob.hurt(DamageSource.FALL, diff);
            }
            if (packet.getMove().move.selfDamageType == MoveEntry.DAMAGEDEALT)
            {
                final float diff = damageDealt * damageRatio;
                attackerMob.hurt(DamageSource.FALL, diff);
            }
            if (packet.getMove().move.selfDamageType == MoveEntry.RELATIVEHP)
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
        if (healRatio > 0 && canHeal) attackerMob.setHealth(Math.min(attackerMob.getMaxHealth(), attackerMob.getHealth()
                + attackerMob.getMaxHealth() * healRatio));

        healRatio = this.getTargetHealRatio(attacker);
        canHeal = attackedHp.getHealth() < attackedHp.getMaxHealth();
        if (healRatio > 0 && canHeal) attackedHp.setHealth(Math.min(attackedHp.getMaxHealth(), attackedHp.getHealth()
                + attackedHp.getMaxHealth() * healRatio));

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
        final IPokemob attacker = packet.attacker;
        attacker.onMoveUse(packet);
        final IPokemob attacked = CapabilityPokemob.getPokemobFor(packet.attacked);
        if (attacked != null) attacked.onMoveUse(packet);
        PokecubeCore.MOVE_BUS.post(new MoveUse.ActualMoveUse.Post(packet.attacker, this, packet.attacked));
    }

    @Override
    public void preAttack(final MovePacket packet)
    {
        PokecubeCore.MOVE_BUS.post(new MoveUse.ActualMoveUse.Pre(packet.attacker, this, packet.attacked));
        final IPokemob attacker = packet.attacker;
        attacker.onMoveUse(packet);
        final IPokemob attacked = CapabilityPokemob.getPokemobFor(packet.attacked);
        if (attacked != null) attacked.onMoveUse(packet);
    }
}
