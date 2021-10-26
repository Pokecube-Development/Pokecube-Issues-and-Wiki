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
            final Effect atkU = Effects.DAMAGE_BOOST;
            // TODO make this configurable, currently the weakness affect is a
            // bit too much
            if (up)
            {
                if (entity.hasEffect(atkD)) entity.removeEffect(atkD);
                entity.addEffect(new EffectInstance(atkU, duration, this.amount));
            }
            else
            {
                if (entity.hasEffect(atkU)) entity.removeEffect(atkU);
                entity.addEffect(new EffectInstance(atkD, duration, this.amount));
            }
            break;
        case DEFENSE:
            final Effect defU = Effects.DAMAGE_RESISTANCE;
            if (up) entity.addEffect(new EffectInstance(defU, duration, this.amount));
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
            final Effect vitD = Effects.MOVEMENT_SLOWDOWN;
            final Effect vitU = Effects.MOVEMENT_SPEED;
            if (up)
            {
                if (entity.hasEffect(vitD)) entity.removeEffect(vitD);
                entity.addEffect(new EffectInstance(vitU, duration, this.amount));
            }
            else
            {
                if (entity.hasEffect(vitU)) entity.removeEffect(vitU);
                entity.addEffect(new EffectInstance(vitD, duration, this.amount));
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
