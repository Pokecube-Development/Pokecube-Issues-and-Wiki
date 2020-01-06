package pokecube.core.interfaces.capabilities;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;
import pokecube.core.events.OngoingTickEvent;
import pokecube.core.interfaces.entity.IOngoingAffected;
import pokecube.core.interfaces.entity.IOngoingAffected.IOngoingEffect;
import pokecube.core.interfaces.entity.IOngoingAffected.IOngoingEffect.AddType;

public class CapabilityAffected
{
    public static class DefaultAffected implements IOngoingAffected, ICapabilitySerializable<ListNBT>
    {
        private final LazyOptional<IOngoingAffected>     holder  = LazyOptional.of(() -> this);
        LivingEntity                                     entity;
        final List<IOngoingEffect>                       effects = Lists.newArrayList();
        IOngoingEffect[]                                 cachedArray;
        final Map<ResourceLocation, Set<IOngoingEffect>> map     = Maps.newHashMap();

        public DefaultAffected()
        {
        }

        public DefaultAffected(LivingEntity entity)
        {
            this.entity = entity;
            for (final ResourceLocation id : IOngoingAffected.EFFECTS.keySet())
                this.map.put(id, Sets.newConcurrentHashSet());
        }

        @Override
        public boolean addEffect(IOngoingEffect effect)
        {
            if (effect.allowMultiple())
            {
                final Collection<IOngoingEffect> set = this.getEffects(effect.getID());
                for (final IOngoingEffect old : set)
                {
                    final AddType type = effect.canAdd(this, old);
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
        public <T> LazyOptional<T> getCapability(Capability<T> capability, Direction facing)
        {
            return CapabilityAffected.AFFECTED_CAP.orEmpty(capability, this.holder);
        }

        @Override
        public List<IOngoingEffect> getEffects()
        {
            return this.effects;
        }

        @Override
        public Collection<IOngoingEffect> getEffects(ResourceLocation id)
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
        public void removeEffect(IOngoingEffect effect)
        {
            final Collection<IOngoingEffect> set = this.getEffects(effect.getID());
            this.effects.remove(effect);
            set.remove(effect);
        }

        @Override
        public void removeEffects(ResourceLocation id)
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
                if (!MinecraftForge.EVENT_BUS.post(new OngoingTickEvent(this.getEntity(), effect)))
                {
                    int duration = effect.getDuration();
                    if (duration > 0) duration = duration - 1;
                    effect.setDuration(duration);
                    effect.affectTarget(this);
                    if (effect.getDuration() == 0) stale.add(effect);
                }
                else if (effect.getDuration() == 0) stale.add(effect);
            for (final IOngoingEffect effect : stale)
                this.removeEffect(effect);
        }
    }

    public static class Storage implements Capability.IStorage<IOngoingAffected>
    {

        @Override
        public void readNBT(Capability<IOngoingAffected> capability, IOngoingAffected instance, Direction side,
                INBT nbt)
        {
            if (nbt instanceof ListNBT) instance.deserializeNBT((ListNBT) nbt);
        }

        @Override
        public INBT writeNBT(Capability<IOngoingAffected> capability, IOngoingAffected instance, Direction side)
        {
            return instance.serializeNBT();
        }

    }

    @CapabilityInject(IOngoingAffected.class)
    public static final Capability<IOngoingAffected> AFFECTED_CAP = null;

    public static boolean addEffect(Entity mob, IOngoingEffect effect)
    {
        final IOngoingAffected affected = CapabilityAffected.getAffected(mob);
        if (affected != null) return affected.addEffect(effect);
        return false;
    }

    public static IOngoingAffected getAffected(ICapabilityProvider entityIn)
    {
        if (entityIn == null) return null;
        final IOngoingAffected var = entityIn.getCapability(CapabilityAffected.AFFECTED_CAP, null).orElse(null);
        if (var != null) return var;
        else if (IOngoingAffected.class.isInstance(entityIn)) return IOngoingAffected.class.cast(entityIn);
        return null;
    }
}
