package pokecube.core.interfaces.capabilities;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import net.minecraft.core.Direction;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;
import pokecube.core.events.OngoingTickEvent;
import pokecube.core.interfaces.entity.IOngoingAffected;
import pokecube.core.interfaces.entity.IOngoingAffected.IOngoingEffect;
import pokecube.core.interfaces.entity.IOngoingAffected.IOngoingEffect.AddType;

public class CapabilityAffected
{
    public static class DefaultAffected implements IOngoingAffected, ICapabilitySerializable<ListTag>
    {
        private final LazyOptional<IOngoingAffected>     holder  = LazyOptional.of(() -> this);
        LivingEntity                                     entity;
        final List<IOngoingEffect>                       effects = Lists.newArrayList();
        IOngoingEffect[]                                 cachedArray;
        final Map<ResourceLocation, Set<IOngoingEffect>> map     = Maps.newHashMap();

        public DefaultAffected()
        {
        }

        public DefaultAffected(final LivingEntity entity)
        {
            this.entity = entity;
            for (final ResourceLocation id : IOngoingAffected.EFFECTS.keySet())
                this.map.put(id, Sets.newConcurrentHashSet());
        }

        @Override
        public boolean addEffect(final IOngoingEffect effect)
        {
            if (effect.allowMultiple())
            {
                final Collection<IOngoingEffect> set = this.getEffects(effect.getID());
                for (final IOngoingEffect old : set)
                {
                    final AddType type = old.canAdd(this, effect);
                    if (type != AddType.ACCEPT) switch (type)
                    {
                    case UPDATED:
                        return true;
                    default:
                        return false;
                    }
                }
                this.effects.add(effect);
                this.getEffects(effect.getID()).add(effect);
                return true;
            }
            else
            {
                final Collection<IOngoingEffect> set = this.getEffects(effect.getID());
                if (!set.isEmpty()) return false;
                set.add(effect);
                this.effects.add(effect);
                return true;
            }
        }

        @Override
        public void clearEffects()
        {
            this.effects.clear();
            for (final Set<IOngoingEffect> set : this.map.values())
                set.clear();
        }

        @Override
        public <T> LazyOptional<T> getCapability(final Capability<T> capability, final Direction facing)
        {
            return PokemobCaps.AFFECTED_CAP.orEmpty(capability, this.holder);
        }

        @Override
        public List<IOngoingEffect> getEffects()
        {
            return this.effects;
        }

        @Override
        public Collection<IOngoingEffect> getEffects(final ResourceLocation id)
        {
            if (!this.map.containsKey(id)) this.map.put(id, Sets.newHashSet());
            return this.map.get(id);
        }

        @Override
        public LivingEntity getEntity()
        {
            return this.entity;
        }

        @Override
        public void removeEffect(final IOngoingEffect effect)
        {
            final Collection<IOngoingEffect> set = this.getEffects(effect.getID());
            this.effects.remove(effect);
            set.remove(effect);
        }

        @Override
        public void removeEffects(final ResourceLocation id)
        {
            final Collection<IOngoingEffect> set = this.getEffects(id);
            this.effects.removeAll(set);
            set.clear();
        }

        @Override
        public void tick()
        {
            final Set<IOngoingEffect> stale = Sets.newHashSet();
            this.cachedArray = this.effects.toArray(new IOngoingEffect[this.effects.size()]);
            for (final IOngoingEffect effect : this.cachedArray)
            {
                final OngoingTickEvent event = new OngoingTickEvent(this.getEntity(), effect);
                if (!MinecraftForge.EVENT_BUS.post(event))
                {
                    final int duration = event.getDuration();
                    effect.setDuration(duration);
                    effect.affectTarget(this);
                    if (effect.getDuration() == 0) stale.add(effect);
                }
                else if (effect.getDuration() == 0) stale.add(effect);
            }
            for (final IOngoingEffect effect : stale)
                this.removeEffect(effect);
        }
    }

    public static class Storage implements Capability.IStorage<IOngoingAffected>
    {

        @Override
        public void readNBT(final Capability<IOngoingAffected> capability, final IOngoingAffected instance,
                final Direction side, final Tag nbt)
        {
            if (nbt instanceof ListTag) instance.deserializeNBT((ListTag) nbt);
        }

        @Override
        public Tag writeNBT(final Capability<IOngoingAffected> capability, final IOngoingAffected instance,
                final Direction side)
        {
            return instance.serializeNBT();
        }

    }

    public static boolean addEffect(final Entity mob, final IOngoingEffect effect)
    {
        final IOngoingAffected affected = CapabilityAffected.getAffected(mob);
        if (affected != null) return affected.addEffect(effect);
        return false;
    }

    public static IOngoingAffected getAffected(final ICapabilityProvider entityIn)
    {
        if (entityIn == null) return null;
        final IOngoingAffected var = entityIn.getCapability(PokemobCaps.AFFECTED_CAP, null).orElse(null);
        if (var != null) return var;
        else if (IOngoingAffected.class.isInstance(entityIn)) return IOngoingAffected.class.cast(entityIn);
        return null;
    }
}
