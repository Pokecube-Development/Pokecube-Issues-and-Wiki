package pokecube.core.impl.entity.impl;

import java.util.Map;
import java.util.Objects;
import java.util.Random;

import com.google.common.collect.Maps;

import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.npc.Npc;
import net.minecraft.world.entity.player.Player;
import pokecube.api.PokecubeAPI;
import pokecube.api.entity.IOngoingAffected;
import pokecube.api.entity.IOngoingAffected.IOngoingEffect;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.entity.pokemob.PokemobCaps;
import pokecube.api.events.pokemobs.combat.StatusEvent;
import pokecube.api.moves.utils.IMoveConstants;
import pokecube.core.PokecubeCore;
import pokecube.core.impl.PokecubeMod;
import pokecube.core.moves.damage.StatusEffectDamageSource;
import thut.api.maths.Vector3;
import thut.core.common.ThutCore;

public class PersistantStatusEffect extends BaseEffect
{
    public static class DefaultEffects implements IStatusEffect
    {
        final Status status;

        int tick;

        public DefaultEffects(final Status status)
        {
            this.status = status;
        }

        @Override
        public void affectTarget(final IOngoingAffected target, final IOngoingEffect effect)
        {
            final LivingEntity entity = target.getEntity();
            final IPokemob pokemob = PokemobCaps.getPokemobFor(entity);
            if (pokemob != null && this.status != Status.BADPOISON) pokemob.getMoveStats().TOXIC_COUNTER = 0;

            boolean toRemove = pokemob != null ? false : Math.random() > 0.8;
            if (effect.getDuration() == 0) toRemove = true;
            final int duration = PokecubeCore.getConfig().attackCooldown * 5;

            LivingEntity targetM = entity.getKillCredit();
            if (targetM == null) targetM = entity.getLastHurtByMob();
            if (targetM == null) targetM = entity.getLastHurtMob();
            if (targetM == null) targetM = entity;
            float scale = 1;
            final IPokemob user = PokemobCaps.getPokemobFor(targetM);
            if (entity.getLastDamageSource() != null)
            {
                final DamageSource source = new StatusEffectDamageSource(entity.getLastDamageSource().typeHolder(), targetM);
                if (pokemob != null)
                {
                    // TODO: Check if correct
                    source.is(DamageTypeTags.BYPASSES_ARMOR);
                    source.is(DamageTypeTags.BYPASSES_ENCHANTMENTS); // Same as .bypassMagic?
                } else if (entity instanceof Player)
                    scale = (float) (user != null && user.isPlayerOwned() ? PokecubeCore.getConfig().ownedPlayerDamageRatio
                            : PokecubeCore.getConfig().wildPlayerDamageRatio);
                else scale = (float) (entity instanceof Npc ? PokecubeCore.getConfig().pokemobToNPCDamageRatio
                            : PokecubeCore.getConfig().pokemobToOtherMobDamageRatio);
                if (scale <= 0) toRemove = true;

                switch (this.status) {
                    case BADPOISON:
                        if (pokemob != null)
                        {
                            entity.hurt(source,
                                    scale * (pokemob.getMoveStats().TOXIC_COUNTER + 1) * entity.getMaxHealth() / 16f);
                            this.spawnPoisonParticle(entity);
                            this.spawnPoisonParticle(entity);
                            pokemob.getMoveStats().TOXIC_COUNTER++;
                        } else {
                            entity.hurt(source, scale * entity.getMaxHealth() / 8f);
                            this.spawnPoisonParticle(entity);
                        }
                        break;
                    case BURN:
                        if (scale > 0) entity.setSecondsOnFire(duration / 20);
                        entity.hurt(source, scale * entity.getMaxHealth() / 16f);
                        break;
                    case FREEZE:
                        if (Math.random() > 0.9) toRemove = true;
                        entity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, duration, 100));
                        entity.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, duration, 100));
                        break;
                    case PARALYSIS:
                        break;
                    case POISON:
                        entity.hurt(source, scale * entity.getMaxHealth() / 8f);
                        this.spawnPoisonParticle(entity);
                        break;
                    case SLEEP:
                        if (Math.random() > 0.9) toRemove = true;
                        else {
                            entity.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, duration, 100));
                            entity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, duration, 100));
                            entity.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, duration, 100));
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
        }

        @Override
        public void setTick(final int tick)
        {
            this.tick = tick;
        }

        protected void spawnPoisonParticle(final Entity entity)
        {
            final Random rand = ThutCore.newRandom();
            final Vector3 loc = new Vector3();
            // int i = 0xFFFF00FF;
            // final double d0 = (i >> 16 & 255) / 255.0D;
            // final double d1 = (i >> 8 & 255) / 255.0D;
            // final double d2 = (i >> 0 & 255) / 255.0D;
            // final Vector3 vel = new Vector3().set(d0, d1, d2);
            for (int i = 0; i < 3; ++i)
            {
                loc.set(entity.getX(), entity.getY() + 0.5D + rand.nextFloat() * entity.getBbHeight(), entity.getZ());
                // PokecubeCore.spawnParticle(entity.getEntityWorld(),
                // "mobSpell", particleLoc, vel);TODO figure out colouring
                entity.level().addParticle(ParticleTypes.WITCH, loc.x, loc.y, loc.z, 0, 0, 0);
            }
        }

        protected void spawnSleepParticle(final Entity entity)
        {
            final Random rand = ThutCore.newRandom();
            final Vector3 loc = new Vector3();
            new Vector3();
            for (int i = 0; i < 3; ++i)
            {
                loc.set(entity.getX(), entity.getY() + 0.5D + rand.nextFloat() * entity.getBbHeight(), entity.getZ());
                entity.level().addParticle(ParticleTypes.WITCH, loc.x, loc.y, loc.z, 0, 0, 0);
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
        SLEEP(IMoveConstants.STATUS_SLP), FREEZE(IMoveConstants.STATUS_FRZ), PARALYSIS(IMoveConstants.STATUS_PAR),
        BURN(IMoveConstants.STATUS_BRN), POISON(IMoveConstants.STATUS_PSN), BADPOISON(IMoveConstants.STATUS_PSN2);

        public static Status getStatus(final int mask)
        {
            return PersistantStatusEffect.MASKMAP.get(mask);
        }

        public static void initDefaults()
        {
            for (final Status stat : Status.values())
                PersistantStatusEffect.EFFECTMAP.put(stat, new DefaultEffects(stat));
        }

        final int mask;

        private Status(final int mask)
        {
            this.mask = mask;
            PersistantStatusEffect.MASKMAP.put(mask, this);
        }

        public int getMask()
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

    public PersistantStatusEffect(final int status, final int timer)
    {
        super(PersistantStatusEffect.ID);
        this.status = Status.getStatus(status);
        if (this.status == null)
        {
            PokecubeAPI.LOGGER.error("Error setting of status. " + status);
            throw new IllegalArgumentException();
        }
        this.setDuration(timer);
    }

    @Override
    public void affectTarget(final IOngoingAffected target)
    {
        if (this.status == null)
        {
            final IPokemob pokemob = PokemobCaps.getPokemobFor(target.getEntity());
            if (pokemob.getStatus() == IMoveConstants.STATUS_NON) this.setDuration(0);
            else if (pokemob != null) this.status = Status.getStatus(pokemob.getStatus());
        }
        final IStatusEffect effect = PersistantStatusEffect.EFFECTMAP.get(this.status);
        if (effect != null)
        {
            final StatusEvent event = new StatusEvent(target.getEntity(), this.status);
            if (!PokecubeAPI.MOVE_BUS.post(event))
            {
                effect.setTick(this.getDuration());
                effect.affectTarget(target, this);
            }
        }
    }

    @Override
    public void deserializeNBT(final CompoundTag nbt)
    {
        super.deserializeNBT(nbt);
        if (nbt.contains("S")) this.status = Status.values()[nbt.getByte("S")];
        else this.setDuration(0);
    }

    @Override
    public CompoundTag serializeNBT()
    {
        final CompoundTag tag = super.serializeNBT();
        if (this.status != null) tag.putByte("S", (byte) this.status.ordinal());
        return tag;
    }

}
