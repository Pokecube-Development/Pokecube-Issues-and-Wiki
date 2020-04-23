/**
 *
 */
package pokecube.core.moves.templates;

import java.util.BitSet;
import java.util.List;

import com.google.common.collect.Lists;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.world.Explosion;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.ExplosionEvent;
import pokecube.core.PokecubeCore;
import pokecube.core.database.Pokedex;
import pokecube.core.database.moves.MoveEntry;
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
        private final IPokemob     user;
        private final Move_Explode move;
        private final BitSet       hit = new BitSet();

        public Hitter(final IPokemob user, final Move_Explode move)
        {
            this.user = user;
            this.move = move;
        }

        @Override
        public void hitEntity(final Entity e, final float power, final Explosion boom)
        {
            if (this.hit.get(e.getEntityId()) || !(e instanceof LivingEntity)) return;
            this.hit.set(e.getEntityId());

            byte statusChange = IMoveConstants.STATUS_NON;
            byte changeAddition = IMoveConstants.CHANGE_NONE;
            if (this.move.move.statusChange != IMoveConstants.STATUS_NON && MovesUtils.rand
                    .nextFloat() <= this.move.move.statusChance) statusChange = this.move.move.statusChange;
            if (this.move.move.change != IMoveConstants.CHANGE_NONE && MovesUtils.rand
                    .nextFloat() <= this.move.move.chanceChance) changeAddition = this.move.move.change;
            final MovePacket packet = new MovePacket(this.user, e, this.move.name, this.move.getType(this.user),
                    this.move.getPWR(this.user, e), this.move.move.crit, statusChange, changeAddition);
            this.move.onAttack(packet);
        }

    }

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
        final List<Entity> targets = attacker.getEntity().getEntityWorld().getEntitiesWithinAABBExcludingEntity(attacker
                .getEntity(), location.getAABB().grow(8));
        final List<Entity> toRemove = Lists.newArrayList();
        for (final Entity e : targets)
            if (!(e instanceof LivingEntity)) toRemove.add(e);
        targets.removeAll(toRemove);
        final int n = targets.size();
        if (n > 0)
        {
            for (final Entity e : targets)
                if (e != null)
                {
                    final Entity attacked = e;
                    final MovePacket packet = new MovePacket(attacker, attacked, this.name, this.getType(attacker), this
                            .getPWR(attacker, attacked), this.move.crit, IMoveConstants.STATUS_NON,
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
        final MobEntity mob = attacker.getEntity();
        final IPokemob pokemob = attacker;
        if (pokemob.getMoveStats().timeSinceIgnited-- <= 0)
        {
            mob.playSound(SoundEvents.ENTITY_CREEPER_PRIMED, 1.0F, 0.5F);
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
        final float f1 = (float) (this.getPWR(pokemob, attacked) * PokecubeCore.getConfig().blastStrength * pokemob
                .getStat(Stats.ATTACK, true) / 500000f);

        final ExplosionCustom boom = MovesUtils.newExplosion(mob, mob.posX, mob.posY, mob.posZ, f1, false, true);
        boom.hitter = new Hitter(pokemob, this);
        final ExplosionEvent.Start evt = new ExplosionEvent.Start(mob.getEntityWorld(), boom);
        MinecraftForge.EVENT_BUS.post(evt);
        if (!evt.isCanceled())
        {
            pokemob.setHealth(0);// kill the mob.
            if (PokecubeCore.getConfig().explosions && MoveEventsHandler.canEffectBlock(pokemob, this.v.set(mob))) boom
                    .doExplosion();
            else
            {
                mob.getEntityWorld().playSound((PlayerEntity) null, mob.posX, mob.posY, mob.posZ,
                        SoundEvents.ENTITY_GENERIC_EXPLODE, SoundCategory.BLOCKS, 4.0F, (1.0F + (mob
                                .getEntityWorld().rand.nextFloat() - mob.getEntityWorld().rand.nextFloat()) * 0.2F)
                                * 0.7F);

                if (this.getPWR() > 200) mob.getEntityWorld().addParticle(ParticleTypes.EXPLOSION, mob.posX, mob.posY,
                        mob.posZ, 1.0D, 0.0D, 0.0D);
                else mob.getEntityWorld().addParticle(ParticleTypes.EXPLOSION, mob.posX, mob.posY, mob.posZ, 1.0D, 0.0D,
                        0.0D);
                this.actualAttack(pokemob, Vector3.getNewVector().set(pokemob.getEntity()).add(0, pokemob.getSize()
                        * pokemob.getPokedexEntry().height / 2, 0));
            }
            attacker.onRecall();
        }
    }

    @Override
    public void attack(final IPokemob attacker, final Vector3 attacked)
    {
        if (!attacker.getEntity().isAlive()) return;
        if (PokecubeCore.getConfig().explosions) this.attack(attacker, attacker.getEntity());
        else super.attack(attacker, attacked);
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
        if (!PokecubeCore.getConfig().explosions) if (pokemob.getHealth() <= 0 && target != null && pokemob
                .getHealth() >= 0 && attacked != pokemob)
        {
            boolean giveExp = true;
            if (target.getGeneralState(GeneralStates.TAMED) && !PokecubeCore.getConfig().pvpExp && target
                    .getOwner() instanceof PlayerEntity) giveExp = false;
            if (target.getGeneralState(GeneralStates.TAMED) && !PokecubeCore.getConfig().trainerExp) giveExp = false;
            if (giveExp)
            {
                // voltorb's enemy wins XP and EVs even if it didn't
                // attack
                target.setExp(target.getExp() + Tools.getExp((float) PokecubeCore.getConfig().expScaleFactor, pokemob
                        .getBaseXP(), pokemob.getLevel()), true);
                final byte[] evsToAdd = Pokedex.getInstance().getEntry(pokemob.getPokedexNb()).getEVs();
                target.addEVs(evsToAdd);
            }
        }
        super.postAttack(packet);
    }
}
