package pokecube.core.impl.entity.impl;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import pokecube.api.PokecubeAPI;
import pokecube.api.entity.IOngoingAffected;
import pokecube.api.entity.IOngoingAffected.IOngoingEffect;
import pokecube.api.entity.pokemob.IPokemob.Stats;
import pokecube.core.PokecubeCore;
import pokecube.core.impl.PokecubeMod;

public class StatEffect extends BaseEffect
{
    public final static ResourceLocation ID = new ResourceLocation(PokecubeMod.ID, "stat_effect");

    Stats stat;
    byte amount;

    public StatEffect()
    {
        super(StatEffect.ID);
        this.setDuration(5);
    }

    public StatEffect(final Stats stat, final byte amount)
    {
        this();
        this.stat = stat;
        this.amount = amount;
    }

    @Override
    public void affectTarget(final IOngoingAffected target)
    {
        if (this.amount == 0)
        {
            this.setDuration(0);
            return;
        }
        final boolean up = this.amount > 0;
        final LivingEntity entity = target.getEntity();
        final int duration = PokecubeCore.getConfig().attackCooldown + 10;

        if (PokecubeCore.getConfig().debug_moves)
        {
            PokecubeAPI.logInfo("Stat effect {} of amount {} on {}", stat, this.amount, entity);
        }

        int amt = Math.abs(this.amount);

        switch (this.stat)
        {
        case ACCURACY:
            break;
        case ATTACK:
            final MobEffect atkD = MobEffects.WEAKNESS;
            final MobEffect atkU = MobEffects.DAMAGE_BOOST;
            // TODO make this configurable, currently the weakness affect is a
            // bit too much
            if (up)
            {
                if (entity.hasEffect(atkD)) entity.removeEffect(atkD);
                entity.addEffect(new MobEffectInstance(atkU, duration, amt));
            }
            else
            {
                if (entity.hasEffect(atkU)) entity.removeEffect(atkU);
                entity.addEffect(new MobEffectInstance(atkD, duration, amt));
            }
            break;
        case DEFENSE:
            final MobEffect defU = MobEffects.DAMAGE_RESISTANCE;
            if (up) entity.addEffect(new MobEffectInstance(defU, duration, amt));
            break;
        case EVASION:
            break;
        case HP:
            break;
        case SPATTACK:
            break;
        case SPDEFENSE:
            break;
        case VIT:
            final MobEffect vitD = MobEffects.MOVEMENT_SLOWDOWN;
            final MobEffect vitU = MobEffects.MOVEMENT_SPEED;
            if (up)
            {
                if (entity.hasEffect(vitD)) entity.removeEffect(vitD);
                entity.addEffect(new MobEffectInstance(vitU, duration, amt));
            }
            else
            {
                if (entity.hasEffect(vitU)) entity.removeEffect(vitU);
                entity.addEffect(new MobEffectInstance(vitD, duration, amt));
            }
            break;
        default:
            break;

        }
    }

    @Override
    public boolean allowMultiple()
    {
        return true;
    }

    @Override
    public AddType canAdd(final IOngoingAffected affected, final IOngoingEffect toAdd)
    {
        // Check if this is same stat, and if the stat in that direction is
        // capped.
        if (toAdd instanceof StatEffect effect && effect.stat == this.stat)
        {
            this.setDuration(Math.max(this.getDuration(), effect.getDuration()));
            this.amount += effect.amount;
            this.amount = (byte) Math.max(Math.min(this.amount, 6), -6);
            return AddType.UPDATED;
        }
        return AddType.ACCEPT;
    }

    @Override
    public void deserializeNBT(final CompoundTag nbt)
    {
        this.stat = Stats.values()[nbt.getByte("S")];
        this.amount = nbt.getByte("A");
        super.deserializeNBT(nbt);
    }

    @Override
    public boolean onSavePersistant()
    {
        return true;
    }

    @Override
    public CompoundTag serializeNBT()
    {
        final CompoundTag tag = super.serializeNBT();
        tag.putByte("S", (byte) this.stat.ordinal());
        tag.putByte("A", this.amount);
        return tag;
    }

}
