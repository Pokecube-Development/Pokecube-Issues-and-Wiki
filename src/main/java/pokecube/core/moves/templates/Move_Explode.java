/**
 *
 */
package pokecube.core.moves.templates;

import java.util.BitSet;
import java.util.List;

import com.google.common.collect.Lists;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Explosion;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.ExplosionEvent;
import pokecube.core.PokecubeCore;
import pokecube.core.database.moves.MoveEntry;
import pokecube.core.handlers.Config;
import pokecube.core.handlers.events.MoveEventsHandler;
import pokecube.core.interfaces.IMoveAnimation;
import pokecube.core.interfaces.IMoveConstants;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.IPokemob.Stats;
import pokecube.core.interfaces.capabilities.CapabilityPokemob;
import pokecube.core.interfaces.pokemob.ai.GeneralStates;
import pokecube.core.interfaces.pokemob.moves.MovePacket;
import pokecube.core.moves.MovesUtils;
import pokecube.core.utils.Tools;
import thut.api.boom.ExplosionCustom;
import thut.api.boom.ExplosionCustom.IEntityHitter;
import thut.api.maths.Vector3;

/** @author Manchou */
public class Move_Explode extends Move_Basic
{
    private static class Hitter implements IEntityHitter
    {
        private final IPokemob user;
        private final Move_Explode move;
        private final BitSet hit = new BitSet();

        public Hitter(final IPokemob user, final Move_Explode move)
        {
            this.user = user;
            this.move = move;
        }

        @Override
        public void hitEntity(final Entity e, final float power, final Explosion boom)
        {
            // Dont hit twice, and only hit living entities.
            if (this.hit.get(e.getId()) || !(e instanceof LivingEntity)) return;
            // Dont hit self, that is taken care of elsewhere.
            if (e == this.user.getEntity()) return;
            // Flag as already hit.
            this.hit.set(e.getId());

            byte statusChange = IMoveConstants.STATUS_NON;
            byte changeAddition = IMoveConstants.CHANGE_NONE;
            if (this.move.move.statusChange != IMoveConstants.STATUS_NON
                    && MovesUtils.rand.nextFloat() <= this.move.move.statusChance)
                statusChange = this.move.move.statusChange;
            if (this.move.move.change != IMoveConstants.CHANGE_NONE
                    && MovesUtils.rand.nextFloat() <= this.move.move.chanceChance)
                changeAddition = this.move.move.change;
            final MovePacket packet = new MovePacket(this.user, e, this.move.name, this.move.getType(this.user),
                    this.move.getPWR(this.user, e), this.move.move.crit, statusChange, changeAddition);
            this.move.onAttack(packet);
        }

    }

    public static final DamageSource SELFBOOM = new DamageSource("pokemob.exploded").setExplosion().bypassMagic();

    /**
     * @param name
     * @param attackCategory
     */
    public Move_Explode(final String name)
    {
        super(name);
        this.move.selfDamage = 100;
        this.move.selfDamageType = MoveEntry.TOTALHP;
    }

    /**
     * This does the somewhat normal attack code.
     *
     * @param attacker
     * @param location
     */
    public void actualAttack(final IPokemob attacker, final Vector3 location)
    {
        final List<Entity> targets = attacker.getEntity().getLevel().getEntities(attacker.getEntity(),
                location.getAABB().inflate(8));
        final List<Entity> toRemove = Lists.newArrayList();
        for (final Entity e : targets) if (!(e instanceof LivingEntity)) toRemove.add(e);
        targets.removeAll(toRemove);
        final int n = targets.size();
        if (n > 0)
        {
            for (final Entity e : targets) if (e != null)
            {
                final Entity attacked = e;
                final MovePacket packet = new MovePacket(attacker, attacked, this.name, this.getType(attacker),
                        this.getPWR(attacker, attacked), this.move.crit, IMoveConstants.STATUS_NON,
                        IMoveConstants.CHANGE_NONE);
                packet.applyOngoing = false;
                this.onAttack(packet);
            }
        }
        else MovesUtils.displayEfficiencyMessages(attacker, null, -1, 0);
        this.doWorldAction(attacker, location);
    }

    @Override
    public void attack(final IPokemob attacker, final Entity attacked)
    {
        if (!attacker.getEntity().isAlive()) return;
        final Mob mob = attacker.getEntity();
        final IPokemob pokemob = attacker;
        if (pokemob.getMoveStats().timeSinceIgnited-- <= 0)
        {
            mob.playSound(SoundEvents.CREEPER_PRIMED, 1.0F, 0.5F);
            pokemob.setExplosionState(1);
            pokemob.getMoveStats().timeSinceIgnited = 10;
        }
        if (attacker.getStatus() == IMoveConstants.STATUS_SLP)
        {
            MovesUtils.displayStatusMessages(attacker, attacked, IMoveConstants.STATUS_SLP, false);
            return;
        }
        if (attacker.getStatus() == IMoveConstants.STATUS_FRZ)
        {
            MovesUtils.displayStatusMessages(attacker, attacked, IMoveConstants.STATUS_FRZ, false);
            return;
        }
        if (attacker.getStatus() == IMoveConstants.STATUS_PAR && Math.random() > 0.75)
        {
            MovesUtils.displayStatusMessages(attacker, attacked, IMoveConstants.STATUS_PAR, false);
            return;
        }
        this.playSounds(mob, attacked, null);
        final float f1 = (float) (this.getPWR(pokemob, attacked) * PokecubeCore.getConfig().blastStrength
                * pokemob.getStat(Stats.ATTACK, true) / 500000f);

        final ExplosionCustom boom = MovesUtils.newExplosion(mob, mob.getX(), mob.getY(), mob.getZ(), f1);
        boom.hitter = new Hitter(pokemob, this);
        final ExplosionEvent.Start evt = new ExplosionEvent.Start(mob.getLevel(), boom);
        MinecraftForge.EVENT_BUS.post(evt);
        if (!evt.isCanceled())
        {
            final boolean explodeDamage = mob.getLevel() instanceof ServerLevel level && Config.Rules.doBoom(level);
            final boolean damagePerms = MoveEventsHandler.canAffectBlock(pokemob, this.v.set(mob), this.getName());
            // If these, we let the explosion handle the damage.
            if (explodeDamage && damagePerms) boom.doExplosion();
            else
            {
                // Otherwise spawn in some effects
                mob.getLevel().playSound((Player) null, mob.getX(), mob.getY(), mob.getZ(),
                        SoundEvents.GENERIC_EXPLODE, SoundSource.BLOCKS, 4.0F,
                        (1.0F + (mob.getLevel().random.nextFloat()
                                - mob.getLevel().random.nextFloat()) * 0.2F) * 0.7F);
                if (this.getPWR() > 200) mob.getLevel().addParticle(ParticleTypes.EXPLOSION, mob.getX(),
                        mob.getY(), mob.getZ(), 1.0D, 0.0D, 0.0D);
                else mob.getLevel().addParticle(ParticleTypes.EXPLOSION, mob.getX(), mob.getY(),
                        mob.getZ(), 1.0D, 0.0D, 0.0D);
                // and hit nearby targets normally.
                this.actualAttack(pokemob, new Vector3().set(pokemob.getEntity()).add(0,
                        pokemob.getSize() * pokemob.getPokedexEntry().height / 2, 0));
            }
            // First give it some health so it is alive
            mob.setHealth(1);
            // Now we kill the user via a damage source.
            mob.hurt(Move_Explode.SELFBOOM, mob.getMaxHealth() * 1e5f);
        }
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public IMoveAnimation getAnimation()
    {
        return null;
    }

    @Override
    public void postAttack(final MovePacket packet)
    {
        final Entity attacked = packet.attacked;
        final IPokemob pokemob = packet.attacker;
        final IPokemob target = CapabilityPokemob.getPokemobFor(attacked);

        final boolean explodeDamage = attacked.getLevel() instanceof ServerLevel level && Config.Rules.doBoom(level);

        if (!explodeDamage && pokemob.getHealth() <= 0 && target != null && pokemob.getHealth() >= 0
                && attacked != pokemob)
        {
            boolean giveExp = true;
            if (target.getGeneralState(GeneralStates.TAMED) && !PokecubeCore.getConfig().pvpExp
                    && target.getOwner() instanceof Player)
                giveExp = false;
            if (target.getGeneralState(GeneralStates.TAMED) && !PokecubeCore.getConfig().trainerExp) giveExp = false;
            if (giveExp)
            {
                // voltorb's enemy wins XP and EVs even if it didn't
                // attack
                target.setExp(target.getExp() + Tools.getExp((float) PokecubeCore.getConfig().expScaleFactor,
                        pokemob.getBaseXP(), pokemob.getLevel()), true);
                final byte[] evsToAdd = pokemob.getPokedexEntry().getEVs();
                target.addEVs(evsToAdd);
            }
        }
        super.postAttack(packet);
    }
}
