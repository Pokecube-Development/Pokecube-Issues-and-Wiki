package pokecube.core.interfaces.entity.impl;

import java.util.Map;

import com.google.common.collect.Maps;

import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import net.minecraft.entity.INPC;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.util.DamageSource;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import pokecube.core.PokecubeCore;
import pokecube.core.events.EffectEvent;
import pokecube.core.interfaces.IMoveConstants;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.PokecubeMod;
import pokecube.core.interfaces.capabilities.CapabilityPokemob;
import pokecube.core.interfaces.entity.IOngoingAffected;
import pokecube.core.interfaces.entity.IOngoingAffected.IOngoingEffect;
import thut.core.common.commands.CommandTools;

public class NonPersistantStatusEffect extends BaseEffect
{
    public static class DefaultEffects implements IEffect
    {
        public final Effect status;
        int                 tick;

        public DefaultEffects(Effect status)
        {
            this.status = status;
        }

        @Override
        public void affectTarget(IOngoingAffected target, IOngoingEffect effect)
        {
            final LivingEntity entity = target.getEntity();
            final IPokemob pokemob = CapabilityPokemob.getPokemobFor(entity);
            switch (this.status)
            {
            case CONFUSED:
                entity.addPotionEffect(new EffectInstance(Effects.NAUSEA, 10));
                break;
            case CURSED:
                if (pokemob != null)
                {
                    final ITextComponent mess = CommandTools.makeTranslatedMessage("pokemob.status.curse", "red",
                            pokemob.getDisplayName().getFormattedText());
                    pokemob.displayMessageToOwner(mess);
                }
                LivingEntity targetM = entity.getAttackingEntity();
                if (targetM == null) targetM = entity.getRevengeTarget();
                if (targetM == null) targetM = entity.getLastAttackedEntity();
                if (targetM == null) targetM = entity;
                float scale = 1;
                final IPokemob user = CapabilityPokemob.getPokemobFor(targetM);
                final DamageSource source = user != null && user.getOwner() != null ? DamageSource
                        .causeIndirectDamage(targetM, user.getOwner())
                        : targetM != null ? DamageSource.causeMobDamage(targetM) : new DamageSource("generic");

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
                if (scale <= 0) effect.setDuration(0);
                entity.attackEntityFrom(source, scale * entity.getMaxHealth() * 0.25f);
                break;
            case FLINCH:
                break;
            default:
                break;

            }
        }

        @Override
        public void setTick(int tick)
        {
            this.tick = tick;
        }
    }

    public static enum Effect implements IMoveConstants
    {
        CONFUSED(IMoveConstants.CHANGE_CONFUSED), CURSED(IMoveConstants.CHANGE_CURSE), FLINCH(
                IMoveConstants.CHANGE_FLINCH);
        public static Effect getStatus(byte mask)
        {
            return NonPersistantStatusEffect.MASKMAP.get(mask);
        }

        public static void initDefaults()
        {
            for (final Effect stat : Effect.values())
                NonPersistantStatusEffect.EFFECTMAP.put(stat, new DefaultEffects(stat));
        }

        final byte mask;

        private Effect(byte mask)
        {
            this.mask = mask;
        }

        public byte getMask()
        {
            return this.mask;
        }
    }

    public static interface IEffect
    {
        void affectTarget(IOngoingAffected target, IOngoingEffect effect);

        void setTick(int tick);
    }

    public static final ResourceLocation ID = new ResourceLocation(PokecubeMod.ID, "non_persistant_status");

    public static final Map<Effect, IEffect> EFFECTMAP = Maps.newHashMap();

    private static final Int2ObjectArrayMap<Effect> MASKMAP = new Int2ObjectArrayMap<>();

    public Effect effect;

    public NonPersistantStatusEffect()
    {
        super(NonPersistantStatusEffect.ID);
        // Default duration is -1, the mob should handle removing flinch
        // condition, or removing it when it "runs out"
        this.setDuration(-1);
    }

    public NonPersistantStatusEffect(Effect effect)
    {
        this();
        this.effect = effect;
    }

    @Override
    public void affectTarget(IOngoingAffected target)
    {
        final IEffect effect = NonPersistantStatusEffect.EFFECTMAP.get(this.effect);
        if (effect != null)
        {
            final EffectEvent event = new EffectEvent(target.getEntity(), this.effect);
            if (!PokecubeCore.MOVE_BUS.post(event))
            {
                effect.setTick(this.getDuration());
                effect.affectTarget(target, this);
            }
        }
    }

    @Override
    public boolean allowMultiple()
    {
        return true;
    }

    @Override
    public AddType canAdd(IOngoingAffected affected, IOngoingEffect toAdd)
    {
        if (toAdd instanceof NonPersistantStatusEffect && ((NonPersistantStatusEffect) toAdd).effect == this.effect)
            return AddType.DENY;
        return AddType.ACCEPT;
    }

    @Override
    public void deserializeNBT(CompoundNBT nbt)
    {
        this.effect = Effect.values()[nbt.getByte("S")];
        super.deserializeNBT(nbt);
    }

    @Override
    public boolean onSavePersistant()
    {
        return false;
    }

    @Override
    public CompoundNBT serializeNBT()
    {
        final CompoundNBT tag = super.serializeNBT();
        tag.putByte("S", (byte) this.effect.ordinal());
        return tag;
    }

}
