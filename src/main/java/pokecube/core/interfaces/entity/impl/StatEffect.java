package pokecube.core.interfaces.entity.impl;

import net.minecraft.entity.LivingEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.potion.Effect;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.util.ResourceLocation;
import pokecube.core.PokecubeCore;
import pokecube.core.interfaces.IPokemob.Stats;
import pokecube.core.interfaces.PokecubeMod;
import pokecube.core.interfaces.entity.IOngoingAffected;
import pokecube.core.interfaces.entity.IOngoingAffected.IOngoingEffect;

public class StatEffect extends BaseEffect
{
    public final static ResourceLocation ID = new ResourceLocation(PokecubeMod.ID, "stat_effect");

    Stats stat;
    byte  amount;

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
        final boolean up = this.amount < 0;
        final LivingEntity entity = target.getEntity();
        final int duration = PokecubeCore.getConfig().attackCooldown + 10;
        switch (this.stat)
        {
        case ACCURACY:
            break;
        case ATTACK:
            final Effect atkD = Effects.WEAKNESS;
            final Effect atkU = Effects.STRENGTH;
            if (up)
            {
                if (entity.isPotionActive(atkD)) entity.removePotionEffect(atkD);
                entity.addPotionEffect(new EffectInstance(atkU, duration, this.amount));
            }
            else
            {
                if (entity.isPotionActive(atkU)) entity.removePotionEffect(atkU);
                entity.addPotionEffect(new EffectInstance(atkD, duration, this.amount));
            }
            break;
        case DEFENSE:
            final Effect defU = Effects.RESISTANCE;
            if (up) entity.addPotionEffect(new EffectInstance(defU, duration, this.amount));
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
            final Effect vitD = Effects.SLOWNESS;
            final Effect vitU = Effects.SPEED;
            if (up)
            {
                if (entity.isPotionActive(vitD)) entity.removePotionEffect(vitD);
                entity.addPotionEffect(new EffectInstance(vitU, duration, this.amount));
            }
            else
            {
                if (entity.isPotionActive(vitU)) entity.removePotionEffect(vitU);
                entity.addPotionEffect(new EffectInstance(vitD, duration, this.amount));
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
        if (toAdd instanceof StatEffect)
        {
            final StatEffect effect = (StatEffect) toAdd;
            if (effect.stat == this.stat)
            {
                this.setDuration(Math.max(this.getDuration(), effect.getDuration()));
                this.amount += effect.amount;
                this.amount = (byte) Math.max(Math.min(this.amount, 6), -6);
                return AddType.UPDATED;
            }
        }
        return AddType.ACCEPT;
    }

    @Override
    public void deserializeNBT(final CompoundNBT nbt)
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
    public CompoundNBT serializeNBT()
    {
        final CompoundNBT tag = super.serializeNBT();
        tag.putByte("S", (byte) this.stat.ordinal());
        tag.putByte("A", this.amount);
        return tag;
    }

}
