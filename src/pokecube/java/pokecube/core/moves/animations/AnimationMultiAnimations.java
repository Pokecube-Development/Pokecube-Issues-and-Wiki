package pokecube.core.moves.animations;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import pokecube.api.PokecubeAPI;
import pokecube.api.data.moves.Animations.AnimationJson;
import pokecube.api.moves.MoveEntry;
import pokecube.api.moves.utils.IMoveAnimation;
import pokecube.core.PokecubeCore;
import pokecube.core.moves.animations.presets.Thunder;
import thut.api.maths.Vector3;

import java.util.Comparator;
import java.util.List;

public class AnimationMultiAnimations extends MoveAnimationBase
{
    public static class WrappedAnimation
    {
        IMoveAnimation wrapped;
        ResourceLocation sound;
        SoundEvent soundEvent;
        boolean soundSource = false;
        boolean soundTarget = false;
        float volume = 1;
        float pitch = 1;
        int start;
    }

    public static boolean isThunderAnimation(final IMoveAnimation input)
    {
        if (input == null) return false;
        if (!(input instanceof AnimationMultiAnimations anim)) return input instanceof Thunder;
        for (final WrappedAnimation a : anim.components) if (a.wrapped instanceof Thunder) return true;
        return false;
    }

    List<WrappedAnimation> components = Lists.newArrayList();

    private int applicationTick = 0;

    public AnimationMultiAnimations(final MoveEntry move)
    {
        final List<AnimationJson> animations = move.root_entry.animation.animations;
        this.values.duration = 0;
        if (animations == null || animations.isEmpty()) return;
        for (final AnimationJson anim : animations)
        {
            final IMoveAnimation animation = MoveAnimationHelper.getAnimationPreset(anim.preset, anim.preset_values);
            if (animation == null)
            {
                PokecubeAPI.LOGGER.warn("Warning, unknown animation for preset: {}", anim.preset);
                continue;
            }
            final int start = anim.starttick;
            final int dur = anim.duration;
            if (anim.applyAfter) this.applicationTick = Math.max(start + dur, this.applicationTick);
            this.values.duration = Math.max(this.values.duration, start + dur);
            final WrappedAnimation wrapped = new WrappedAnimation();
            if (anim.sound != null)
            {
                wrapped.sound = ResourceLocation.parse(anim.sound);
                wrapped.soundSource = anim.soundSource != null ? anim.soundSource : false;
                wrapped.soundTarget = anim.soundTarget != null ? anim.soundTarget : true;
                wrapped.pitch = anim.pitch != null ? anim.pitch : 1;
                wrapped.volume = anim.volume != null ? anim.volume : 1;
            }
            wrapped.wrapped = animation;
            wrapped.start = start;
            this.components.add(wrapped);
        }
        this.components.sort(Comparator.comparingInt(arg0 -> arg0.start));
    }

    @Override
    public void clientAnimation(final PoseStack mat, final MultiBufferSource buffer, final MovePacketInfo info,
            final float partialTick, int packedLightIn)
    {
        float tick = info.currentTick;
        for (WrappedAnimation toRun : this.components)
        {
            if (tick > toRun.start + toRun.wrapped.getDuration()) continue;
            if (toRun.start > tick) continue;
            info.currentTick = tick - toRun.start;
            toRun.wrapped.clientAnimation(mat, buffer, info, partialTick, packedLightIn);
        }
        info.currentTick = tick;
    }

    @Override
    public int getApplicationTick()
    {
        return this.applicationTick;
    }

    @Override
    public void initColour(float time, final MoveEntry move)
    {
        // We don't do this.
    }

    @Override
    public void spawnClientEntities(final MovePacketInfo info, float partialTicks)
    {
        final float tick = info.currentTick;
        final float scale = (float) PokecubeCore.getConfig().moveVolumeEffect;
        final Level world = PokecubeCore.proxy.getWorld();
        final Vector3 pos = new Vector3();
        for (WrappedAnimation component : this.components)
        {
            if (component.start > tick) continue;
            if (tick > component.start + component.wrapped.getDuration()) continue;
            info.currentTick = tick - component.start;
            component.wrapped.spawnClientEntities(info, partialTicks);
            final float volume = component.volume * scale;
            final float pitch = component.pitch;
            sound:
            if (info.currentTick < 1 && component.sound != null)
            {
                if (component.soundEvent == null)
                {
                    component.soundEvent = BuiltInRegistries.SOUND_EVENT.get(component.sound);
                    if (component.soundEvent == null)
                    {
                        PokecubeAPI.LOGGER.error("No Registered Sound for {}", component.sound);
                        component.sound = null;
                        break sound;
                    }
                }
                boolean valid = component.soundSource;
                // Check source sounds.
                if (valid &= (info.source != null || info.attacker != null))
                    pos.set(info.source != null ? info.source : info.attacker);
                if (valid)
                    world.playLocalSound(pos.x, pos.y, pos.z, component.soundEvent, SoundSource.HOSTILE, volume, pitch,
                            true);
                // Check target sounds.
                valid = component.soundTarget;
                if (valid &= (info.target != null || info.attacked != null))
                    pos.set(info.target != null ? info.target : info.attacked);
                if (valid)
                    world.playLocalSound(pos.x, pos.y, pos.z, component.soundEvent, SoundSource.HOSTILE, volume, pitch,
                            true);
            }
        }
        info.currentTick = tick;
    }

}
