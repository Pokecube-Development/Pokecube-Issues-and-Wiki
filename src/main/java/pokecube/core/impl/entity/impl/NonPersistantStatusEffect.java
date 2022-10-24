package pokecube.core.impl.entity.impl;

import java.util.Map;

import com.google.common.collect.Maps;

import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.npc.Npc;
import net.minecraft.world.entity.player.Player;
import pokecube.api.PokecubeAPI;
import pokecube.api.entity.IOngoingAffected;
import pokecube.api.entity.IOngoingAffected.IOngoingEffect;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.entity.pokemob.PokemobCaps;
import pokecube.api.events.pokemobs.combat.EffectEvent;
import pokecube.api.moves.utils.IMoveConstants;
import pokecube.core.PokecubeCore;
import pokecube.core.impl.PokecubeMod;
import pokecube.core.moves.damage.StatusEffectDamageSource;
import thut.lib.TComponent;

public class NonPersistantStatusEffect extends BaseEffect
{
    public static class DefaultEffects implements IEffect
    {
        public final Effect status;
        int                 tick;

        public DefaultEffects(final Effect status)
        {
            this.status = status;
        }

        @Override
        public void affectTarget(final IOngoingAffected target, final IOngoingEffect effect)
        {
            final LivingEntity entity = target.getEntity();
            final IPokemob pokemob = PokemobCaps.getPokemobFor(entity);
            switch (this.status)
            {
            case CONFUSED:
                entity.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 10));
                break;
            case CURSED:
                if (pokemob != null)
                {
                    final Component mess = TComponent.translatable("pokemob.status.curse.ours", pokemob
                            .getDisplayName());
                    pokemob.displayMessageToOwner(mess);
                }
                LivingEntity targetM = entity.getKillCredit();
                if (targetM == null) targetM = entity.getLastHurtByMob();
                if (targetM == null) targetM = entity.getLastHurtMob();
                if (targetM == null) targetM = entity;
                float scale = 1;
                final IPokemob user = PokemobCaps.getPokemobFor(targetM);
                final DamageSource source = new StatusEffectDamageSource(targetM);
                if (pokemob != null)
                {
                    source.bypassMagic();
                    source.bypassArmor();
                }
                else if (entity instanceof Player) scale = (float) (user != null && user.isPlayerOwned()
                        ? PokecubeCore.getConfig().ownedPlayerDamageRatio
                        : PokecubeCore.getConfig().wildPlayerDamageRatio);
                else scale = (float) (entity instanceof Npc ? PokecubeCore.getConfig().pokemobToNPCDamageRatio
                        : PokecubeCore.getConfig().pokemobToOtherMobDamageRatio);
                if (scale <= 0) effect.setDuration(0);
                entity.hurt(source, scale * entity.getMaxHealth() * 0.25f);
                break;
            case FLINCH:
                break;
            default:
                break;

            }
        }

        @Override
        public void setTick(final int tick)
        {
            this.tick = tick;
        }
    }

    public static enum Effect implements IMoveConstants
    {
        CONFUSED(IMoveConstants.CHANGE_CONFUSED), CURSED(IMoveConstants.CHANGE_CURSE), FLINCH(
                IMoveConstants.CHANGE_FLINCH);

        public static Effect getStatus(final int mask)
        {
            return NonPersistantStatusEffect.MASKMAP.get(mask);
        }

        public static void initDefaults()
        {
            for (final Effect stat : Effect.values())
                NonPersistantStatusEffect.EFFECTMAP.put(stat, new DefaultEffects(stat));
        }

        final int mask;

        private Effect(final int mask)
        {
            this.mask = mask;
        }

        public int getMask()
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

    public NonPersistantStatusEffect(final Effect effect)
    {
        this();
        this.effect = effect;
    }

    @Override
    public void affectTarget(final IOngoingAffected target)
    {
        final IEffect effect = NonPersistantStatusEffect.EFFECTMAP.get(this.effect);
        if (effect != null)
        {
            final EffectEvent event = new EffectEvent(target.getEntity(), this.effect);
            if (!PokecubeAPI.MOVE_BUS.post(event))
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
    public AddType canAdd(final IOngoingAffected affected, final IOngoingEffect toAdd)
    {
        if (toAdd instanceof NonPersistantStatusEffect && ((NonPersistantStatusEffect) toAdd).effect == this.effect)
            return AddType.DENY;
        return AddType.ACCEPT;
    }

    @Override
    public void deserializeNBT(final CompoundTag nbt)
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
    public CompoundTag serializeNBT()
    {
        final CompoundTag tag = super.serializeNBT();
        tag.putByte("S", (byte) this.effect.ordinal());
        return tag;
    }

}
