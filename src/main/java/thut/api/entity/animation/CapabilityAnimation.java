package thut.api.entity.animation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import it.unimi.dsi.fastutil.objects.Object2FloatOpenHashMap;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;
import thut.api.ThutCaps;
import thut.api.entity.IAnimated;
import thut.api.entity.IAnimated.HeadInfo;
import thut.api.entity.IAnimated.IAnimationHolder;
import thut.api.entity.IAnimated.MolangVars;

public class CapabilityAnimation
{
    public static class DefaultImpl implements IAnimationHolder, ICapabilitySerializable<CompoundTag>
    {
        private static final List<Animation> EMPTY = Collections.emptyList();

        private final LazyOptional<IAnimationHolder> holder = LazyOptional.of(() -> this);

        Map<String, List<Animation>> anims = Maps.newHashMap();

        List<Animation> playingList = DefaultImpl.EMPTY;
        List<String> tmpTransients = new ArrayList<>();
        Set<Animation> transients = new HashSet<>();

        Object2FloatOpenHashMap<UUID> non_static = new Object2FloatOpenHashMap<>();
        Object2FloatOpenHashMap<UUID> start_times = new Object2FloatOpenHashMap<>();

        public String _default = "idle";

        String pending = _default;
        String playing = _default;

        boolean fixed = false;

        HeadInfo head = new HeadInfo();
        MolangVars molangs = new MolangVars();

        boolean init = false;

        float _ageInTicks;

        IAnimated context;
        IAnimationChanger changer;

        @Override
        public void clean()
        {
            this.pending = _default;
            this.playing = _default;
            this.non_static.clear();
            this.start_times.clear();
            if (this.playingList != DefaultImpl.EMPTY && !transients.isEmpty()) this.playingList.removeAll(transients);
            this.transients.clear();
            this.playingList = this.anims.getOrDefault(this.pending, DefaultImpl.EMPTY);
        }

        @Override
        public <T> LazyOptional<T> getCapability(final Capability<T> cap, final Direction side)
        {
            return ThutCaps.ANIMCAP.orEmpty(cap, this.holder);
        }

        @Override
        public String getPendingAnimations()
        {
            return this.pending;
        }

        @Override
        public void initAnimations(Map<String, List<Animation>> map, String _default)
        {
            if (map.size() == this.anims.size()) return;
            map.forEach((s, l) -> anims.computeIfAbsent(s, s2 -> Lists.newArrayList(l)));
            this._default = _default;
            init = true;
        }

        private void initPlayingList()
        {
            this.non_static.clear();
            this.start_times.clear();
            for (final Animation a : this.playingList) if (a.getLength() > 0)
            {
                this.non_static.put(a._uuid, 0);
                this.start_times.removeFloat(a._uuid);
            }
        }

        @Override
        public List<Animation> getPlaying()
        {
            if (pending.equals("none"))
            {
                non_static.clear();
                return EMPTY;
            }
            if (!this.anims.containsKey(this.playing)) this.playing = _default;

            List<Animation> playing = this.anims.getOrDefault(this.playing, EMPTY);
            if (this.playingList != playing)
            {
                this.playingList = playing;
                initPlayingList();
            }
            if (non_static.isEmpty() && !this.pending.isEmpty())
            {
                this.playingList = this.anims.getOrDefault(this.pending, DefaultImpl.EMPTY);
                this.playing = this.pending;
                initPlayingList();
            }
            return this.playingList;
        }

        @Override
        public void setPendingAnimations(final List<Animation> list, final String name)
        {
            if (name.equals(this.playing) || name.equals(this.pending)) return;
            this.anims.put(name, Lists.newArrayList(list));
            if (this.fixed)
            {
                this.pending = this.playing;
            }
            else
            {
                String transitionKey = "%s->%s".formatted(this.playing, name);
                if (!this.anims.containsKey(transitionKey))
                {
                    this.clean();
                }
                this.pending = name;
            }
            this.getPlaying();
        }

        @Override
        public void setStep(final Animation animation, final float step)
        {
            this.non_static.put(animation._uuid, step);
        }

        @Override
        public String getAnimation(final Entity entityIn)
        {
            return this.playing;
        }

        @Override
        public void preRunAll()
        {
            if (context != null && context.getContext() instanceof Entity e && e.tickCount % 10 == 0)
            {
                var transients = context.transientAnimations();
                synchronized (transients)
                {
                    if (!transients.isEmpty() && this.playingList != EMPTY)
                    {

                        for (var anim : transients)
                        {
                            this.tmpTransients.clear();
                            if (this.changer != null)
                            {
                                this.changer.getAlternates(tmpTransients, anims.keySet(), e, anim);
                                for (String s : tmpTransients)
                                {
                                    var animList = anims.get(s);
                                    int index = animList.size() > 1 ? e.random.nextInt(animList.size()) : 0;
                                    this.transients.add(animList.get(index));
                                }
                            }
                            else if (this.anims.containsKey(anim))
                            {
                                this.transients.addAll(anims.get(anim));
                            }
                        }
                        for (Animation a : this.transients)
                        {
                            existing:
                            if (this.playingList.contains(a))
                            {
                                // Check if we need to clean it up
                                float started = this.start_times.getFloat(a._uuid);
                                float shouldEnd = started + a.getLength();
                                if (this._ageInTicks > shouldEnd)
                                {
                                    this.playingList.remove(a);
                                    break existing;
                                }
                                continue;
                            }
                            this.playingList.add(0, a);
                            this.non_static.put(a._uuid, 0);
                            this.start_times.removeFloat(a._uuid);
                        }
                    }
                    transients.clear();
                }
            }
        }

        @Override
        public void postRunAll()
        {}

        @Override
        public void initHeadInfoAndMolangs(Entity entityIn, float limbSwing, float limbSwingAmount, float ageInTicks,
                float netHeadYaw, float headPitch)
        {
            IAnimationHolder.super.initHeadInfoAndMolangs(entityIn, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw,
                    headPitch);
            this._ageInTicks = ageInTicks;
        }

        @Override
        public void preRunAnim(Animation animation)
        {
            this.non_static.put(animation._uuid, 0);
            float t_0 = this.start_times.getOrDefault(animation._uuid, this._ageInTicks);
            this.start_times.put(animation._uuid, t_0);
            this.getMolangVars().startTimer(t_0);
        }

        @Override
        public void postRunAnim(Animation animation)
        {
            float i = this.non_static.getFloat(animation._uuid);
            if (this.pending != this.playing)
            {
                if (i >= animation.length)
                {
                    this.non_static.removeFloat(animation._uuid);
                    if (this.transients.contains(animation)) this.playingList.remove(animation);
                    this.transients.remove(animation);
                }
            }
            else
            {
                boolean dontCleanup = (animation.loops || animation.hasLimbBased || animation.holdWhenDone);
                if (i >= animation.length && !dontCleanup)
                {
                    this.non_static.removeFloat(animation._uuid);
                    if (this.transients.contains(animation)) this.playingList.remove(animation);
                    this.transients.remove(animation);
                }
            }
        }

        @Override
        public CompoundTag serializeNBT()
        {
            final CompoundTag tag = new CompoundTag();
            tag.putString("pl", this.playing);
            tag.putString("pn", this.pending);
            tag.putBoolean("f", this.fixed);

            // In this case, also load/save head info
            if (this.fixed)
            {
                tag.putFloat("Hy", this.head.headYaw);
                tag.putFloat("Hp", this.head.headPitch);
            }

            return tag;
        }

        @Override
        public void deserializeNBT(final CompoundTag nbt)
        {
            this.playing = nbt.getString("pl");
            this.pending = nbt.getString("pn");
            this.fixed = nbt.getBoolean("f");
            if (this.fixed)
            {
                this.head.fixed = true;
                this.head.headYaw = nbt.getFloat("Hy");
                this.head.headPitch = nbt.getFloat("Hp");
            }
        }

        @Override
        public boolean isFixed()
        {
            return this.fixed;
        }

        @Override
        public HeadInfo getHeadInfo()
        {
            return this.head;
        }

        @Override
        public void setFixed(final boolean fixed)
        {
            this.fixed = fixed;
        }

        @Override
        public void overridePlaying(final String anim)
        {
            this.playing = anim;
        }

        @Override
        public MolangVars getMolangVars()
        {
            return molangs;
        }

        @Override
        public void setContext(IAnimated context)
        {
            this.context = context;
        }

        @Override
        public void setAnimationChanger(IAnimationChanger changer)
        {
            this.changer = changer;
        }
    }
}
