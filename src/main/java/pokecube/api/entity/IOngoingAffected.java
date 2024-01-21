package pokecube.api.entity;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.annotation.Nullable;

import com.google.common.collect.Maps;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.common.util.INBTSerializable;
import pokecube.api.PokecubeAPI;

public interface IOngoingAffected extends INBTSerializable<ListTag>
{
    public static interface IOngoingEffect extends INBTSerializable<CompoundTag>
    {
        public static enum AddType
        {
            DENY, ACCEPT, UPDATED;
        }

        /**
         * Apply whatever the effect needed is, this method is responsible for
         * lowering the duration if needed.
         *
         * @param target
         */
        void affectTarget(IOngoingAffected target);

        /**
         * This is called every tick, rather than just damaging ones. This can
         * be used to apply any custom logic which needs to occur outside of the
         * regular move cooldown limits.
         * 
         * @param target
         */
        default void onTick(IOngoingAffected target)
        {

        }

        /**
         * Should multiples of this effect be allowed at once. If false, a new
         * effect of the same ID will not be allowed to be added while this one
         * is active.
         *
         * @return
         */
        default boolean allowMultiple()
        {
            return false;
        }

        /**
         * if you have an effect that allows multiple in some cases, but not all
         * cases, you can use this to filter whether the effect should be added.
         * This method will only be called if allowMultiple returns true, and
         * will always be called directly before applying affected. This means
         * you can use this to edit this effect and then cancel application of
         * affected. <br>
         * <br>
         * ACCEPT -> add the new effect and return true.<br>
         * DENY -> Do not add the new effect, return false.<br>
         * UPDATED -> Do not add the new effect, return true.
         *
         * @param affected
         * @return can this effect be added to the mob.
         */
        default AddType canAdd(final IOngoingAffected affected, final IOngoingEffect toAdd)
        {
            return AddType.ACCEPT;
        }

        @Override
        default void deserializeNBT(final CompoundTag nbt)
        {
            this.setDuration(nbt.getInt("D"));
            if (nbt.contains("U")) setSource(nbt.getUUID("U"));
        }

        @Nullable
        /**
         * @return the UUID of the mob responsible for the effect
         */
        UUID getSource();

        /**
         * @param source - UUID if the mob responsible for this effect.
         */
        void setSource(@Nullable UUID source);

        /**
         * @return how many times should affectTarget be called. by default,
         *         this happens once every
         *         {@link pokecube.core.init.Config#attackCooldown} ticks, if
         *         this value is less than 0, it will never run out.
         */
        int getDuration();

        ResourceLocation getID();

        /** @return Does this effect persist on saving and loading states. */
        default boolean onSavePersistant()
        {
            return true;
        }

        @Override
        default CompoundTag serializeNBT()
        {
            final CompoundTag tag = new CompoundTag();
            tag.putInt("D", this.getDuration());
            if (this.getSource() != null) tag.putUUID("U", getSource());
            return tag;
        }

        void setDuration(int duration);

    }

    public static Map<ResourceLocation, Class<? extends IOngoingEffect>> EFFECTS = Maps.newHashMap();

    boolean addEffect(IOngoingEffect effect);

    void clearEffects();

    @Override
    default void deserializeNBT(final ListTag nbt)
    {
        this.clearEffects();
        for (int i = 0; i < nbt.size(); i++)
        {
            final CompoundTag tag = nbt.getCompound(i);
            final String key = tag.getString("K");
            final CompoundTag value = tag.getCompound("V");
            final ResourceLocation loc = new ResourceLocation(key);
            final Class<? extends IOngoingEffect> effectClass = IOngoingAffected.EFFECTS.get(loc);
            if (effectClass != null) try
            {
                final IOngoingEffect effect = effectClass.getConstructor().newInstance();
                effect.deserializeNBT(value);
                this.addEffect(effect);
            }
            catch (final Exception e)
            {
                PokecubeAPI.LOGGER.error("Error loading effect: " + key + " " + value, e);
            }
        }
    }

    /** @return a list of effects currently applying to this. */
    List<IOngoingEffect> getEffects();

    Collection<IOngoingEffect> getEffects(ResourceLocation id);

    /** @return The Entity to be affected. */
    LivingEntity getEntity();

    void removeEffect(IOngoingEffect effect);

    void removeEffects(ResourceLocation id);

    @Override
    default ListTag serializeNBT()
    {
        final ListTag list = new ListTag();
        for (final IOngoingEffect effect : this.getEffects()) if (effect.onSavePersistant())
        {
            final CompoundTag tag = effect.serializeNBT();
            if (tag != null)
            {
                final CompoundTag nbt = new CompoundTag();
                nbt.putString("K", effect.getID() + "");
                nbt.put("V", tag);
                list.add(nbt);
            }
        }
        return list;
    }

    /**
     * This is called to apply damage effects, etc. Normally it is only called
     * once every second or so, based on the config setting for attack
     * cooldowns. This is called after {@link #tick()}, but not for every call
     * of {@link #tick()}
     */
    void tickDamage();

    /**
     * This is called every tick to apply any custom tick logic the effect
     * needs. Damage should not be done during this call! This is called before
     * {@link #tickDamage()}, but not always followed by it.
     */
    void tick();
}
