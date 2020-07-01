package pokecube.core.interfaces.entity.impl;

import java.util.Map;
import java.util.Random;

import com.google.common.collect.Maps;

import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import net.minecraft.entity.Entity;
import net.minecraft.entity.INPC;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.util.DamageSource;
import net.minecraft.util.ResourceLocation;
import pokecube.core.PokecubeCore;
import pokecube.core.events.pokemob.combat.StatusEvent;
import pokecube.core.interfaces.IMoveConstants;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.PokecubeMod;
import pokecube.core.interfaces.capabilities.CapabilityPokemob;
import pokecube.core.interfaces.entity.IOngoingAffected;
import pokecube.core.interfaces.entity.IOngoingAffected.IOngoingEffect;
import pokecube.core.moves.damage.StatusEffectDamageSource;
import thut.api.maths.Vector3;

public class PersistantStatusEffect extends BaseEffect
{
    public static class DefaultEffects implements IStatusEffect
    {
        final Status status;
        int          tick;

        public DefaultEffects(final Status status)
        {
            this.status = status;
        }

        @Override
        public void affectTarget(final IOngoingAffected target, final IOngoingEffect effect)
        {
            final LivingEntity entity = target.getEntity();
            final IPokemob pokemob = CapabilityPokemob.getPokemobFor(entity);
            if (pokemob != null && this.status != Status.BADPOISON) pokemob.getMoveStats().TOXIC_COUNTER = 0;

            boolean toRemove = pokemob != null ? false : Math.random() > 0.8;
            if (effect.getDuration() == 0) toRemove = true;
            final int duration = PokecubeCore.getConfig().attackCooldown * 5;

            LivingEntity targetM = entity.getAttackingEntity();
            if (targetM == null) targetM = entity.getRevengeTarget();
            if (targetM == null) targetM = entity.getLastAttackedEntity();
            if (targetM == null) targetM = entity;
            float scale = 1;
            final IPokemob user = CapabilityPokemob.getPokemobFor(targetM);
            final DamageSource source = new StatusEffectDamageSource(targetM);
            if (pokemob != null)
            {
                source.setDamageIsAbsolute();
                source.setDamageBypassesArmor();
            }
            else if (entity instanceof PlayerEntity) scale = (float) (user != null && user.isPlayerOwned()
                    ? PokecubeCore.getConfig().ownedPlayerDamageRatio
                    : PokecubeCore.getConfig().wildPlayerDamageRatio);
            else scale = (float) (entity instanceof INPC ? PokecubeCore.getConfig().pokemobToNPCDamageRatio
                    : PokecubeCore.getConfig().pokemobToOtherMobDamageRatio);
            if (scale <= 0) toRemove = true;

            switch (this.status)
            {
            case BADPOISON:
                if (pokemob != null)
                {
                    entity.attackEntityFrom(source, scale * (pokemob.getMoveStats().TOXIC_COUNTER + 1) * entity
                            .getMaxHealth() / 16f);
                    this.spawnPoisonParticle(entity);
                    this.spawnPoisonParticle(entity);
                    pokemob.getMoveStats().TOXIC_COUNTER++;
                }
                else
                {
                    entity.attackEntityFrom(source, scale * entity.getMaxHealth() / 8f);
                    this.spawnPoisonParticle(entity);
                }
                break;
            case BURN:
                if (scale > 0) entity.setFire(duration);
                entity.attackEntityFrom(source, scale * entity.getMaxHealth() / 16f);
                break;
            case FREEZE:
                if (Math.random() > 0.9) toRemove = true;
                entity.addPotionEffect(new EffectInstance(Effects.SLOWNESS, duration, 100));
                entity.addPotionEffect(new EffectInstance(Effects.WEAKNESS, duration, 100));
                break;
            case PARALYSIS:
                break;
            case POISON:
                entity.attackEntityFrom(source, scale * entity.getMaxHealth() / 8f);
                this.spawnPoisonParticle(entity);
                break;
            case SLEEP:
                if (Math.random() > 0.9) toRemove = true;
                else
                {
                    entity.addPotionEffect(new EffectInstance(Effects.BLINDNESS, duration, 100));
                    entity.addPotionEffect(new EffectInstance(Effects.SLOWNESS, duration, 100));
                    entity.addPotionEffect(new EffectInstance(Effects.WEAKNESS, duration, 100));
                    this.spawnSleepParticle(entity);
                }
                break;
            default:
                toRemove = true;
                break;
            }
            if (toRemove)
            {
                if (pokemob != null) pokemob.healStatus();
                effect.setDuration(0);
            }
        }

        @Override
        public void setTick(final int tick)
        {
            this.tick = tick;
        }

        protected void spawnPoisonParticle(final Entity entity)
        {
            final Random rand = new Random();
            final Vector3 loc = Vector3.getNewVector();
            // int i = 0xFFFF00FF;
            // final double d0 = (i >> 16 & 255) / 255.0D;
            // final double d1 = (i >> 8 & 255) / 255.0D;
            // final double d2 = (i >> 0 & 255) / 255.0D;
            // final Vector3 vel = Vector3.getNewVector().set(d0, d1, d2);
            for (int i = 0; i < 3; ++i)
            {
                loc.set(entity.getPosX(), entity.getPosY() + 0.5D + rand.nextFloat() * entity.getHeight(), entity.getPosZ());
                // PokecubeCore.spawnParticle(entity.getEntityWorld(),
                // "mobSpell", particleLoc, vel);TODO figure out colouring
                entity.getEntityWorld().addParticle(ParticleTypes.WITCH, loc.x, loc.y, loc.z, 0, 0, 0);
            }
        }

        protected void spawnSleepParticle(final Entity entity)
        {
            final Random rand = new Random();
            final Vector3 loc = Vector3.getNewVector();
            Vector3.getNewVector();
            for (int i = 0; i < 3; ++i)
            {
                loc.set(entity.getPosX(), entity.getPosY() + 0.5D + rand.nextFloat() * entity.getHeight(), entity.getPosZ());
                entity.getEntityWorld().addParticle(ParticleTypes.WITCH, loc.x, loc.y, loc.z, 0, 0, 0);
            }
        }
    }

    public static interface IStatusEffect
    {
        void affectTarget(IOngoingAffected target, IOngoingEffect effect);

        void setTick(int tick);
    }

    public static enum Status implements IMoveConstants
    {
        SLEEP(IMoveConstants.STATUS_SLP), FREEZE(IMoveConstants.STATUS_FRZ), PARALYSIS(IMoveConstants.STATUS_PAR), BURN(
                IMoveConstants.STATUS_BRN), POISON(IMoveConstants.STATUS_PSN), BADPOISON(IMoveConstants.STATUS_PSN2);

        public static Status getStatus(final byte mask)
        {
            return PersistantStatusEffect.MASKMAP.get(mask);
        }

        public static void initDefaults()
        {
            for (final Status stat : Status.values())
                PersistantStatusEffect.EFFECTMAP.put(stat, new DefaultEffects(stat));
        }

        final byte mask;

        private Status(final byte mask)
        {
            this.mask = mask;
            PersistantStatusEffect.MASKMAP.put(mask, this);
        }

        public byte getMask()
        {
            return this.mask;
        }
    }

    public static final ResourceLocation ID = new ResourceLocation(PokecubeMod.ID, "persistant_status");

    public static final Map<Status, IStatusEffect> EFFECTMAP = Maps.newHashMap();

    private static final Int2ObjectArrayMap<Status> MASKMAP = new Int2ObjectArrayMap<>();

    private Status status;

    public PersistantStatusEffect()
    {
        super(PersistantStatusEffect.ID);
    }

    public PersistantStatusEffect(final byte status, final int timer)
    {
        super(PersistantStatusEffect.ID);
        this.status = Status.getStatus(status);
        if (this.status == null)
        {
            PokecubeCore.LOGGER.error("Error setting of status. " + status);
            throw new IllegalArgumentException();
        }
        this.setDuration(timer);
    }

    @Override
    public void affectTarget(final IOngoingAffected target)
    {
        if (this.status == null)
        {
            final IPokemob pokemob = CapabilityPokemob.getPokemobFor(target.getEntity());
            if (pokemob == null || pokemob.getStatus() == IMoveConstants.STATUS_NON) this.setDuration(0);
            else if (pokemob != null) this.status = Status.getStatus(pokemob.getStatus());
        }
        final IStatusEffect effect = PersistantStatusEffect.EFFECTMAP.get(this.status);
        if (effect != null)
        {
            final StatusEvent event = new StatusEvent(target.getEntity(), this.status);
            if (!PokecubeCore.MOVE_BUS.post(event))
            {
                effect.setTick(this.getDuration());
                effect.affectTarget(target, this);
            }
        }
    }

    @Override
    public void deserializeNBT(final CompoundNBT nbt)
    {
        super.deserializeNBT(nbt);
        if (nbt.contains("S")) this.status = Status.values()[nbt.getByte("S")];
        else this.setDuration(0);
    }

    @Override
    public CompoundNBT serializeNBT()
    {
        final CompoundNBT tag = super.serializeNBT();
        if (this.status != null) tag.putByte("S", (byte) this.status.ordinal());
        return tag;
    }

}
