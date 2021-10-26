package pokecube.core.moves.animations;

import java.util.List;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.ForgeRegistries;
import pokecube.core.PokecubeCore;
import pokecube.core.database.moves.MoveEntry;
import pokecube.core.database.moves.json.JsonMoves.AnimationJson;
import pokecube.core.interfaces.IMoveAnimation;
import pokecube.core.interfaces.Move_Base;
import pokecube.core.moves.animations.presets.Thunder;
import thut.api.maths.Vector3;

public class AnimationMultiAnimations extends MoveAnimationBase
{
    public static class WrappedAnimation
    {
        IMoveAnimation   wrapped;
        ResourceLocation sound;
        SoundEvent       soundEvent;
        boolean          soundSource = false;
        boolean          soundTarget = false;
        float            volume      = 1;
        float            pitch       = 1;
        int              start;
    }

    public static boolean isThunderAnimation(final IMoveAnimation input)
    {
        if (input == null) return false;
        if (!(input instanceof AnimationMultiAnimations)) return input instanceof Thunder;
        final AnimationMultiAnimations anim = (AnimationMultiAnimations) input;
        for (final WrappedAnimation a : anim.components)
            if (a.wrapped instanceof Thunder) return true;
        return false;
    }

    List<WrappedAnimation> components = Lists.newArrayList();

    private int applicationTick = 0;

    public AnimationMultiAnimations(final MoveEntry move)
    {
        final List<AnimationJson> animations = move.baseEntry.animations;
        this.duration = 0;
        if (animations == null || animations.isEmpty()) return;
        for (final AnimationJson anim : animations)
        {
            if (!anim.preset.endsWith(":~" + move.name)) anim.preset = anim.preset + ":~" + move.name;
            final IMoveAnimation animation = MoveAnimationHelper.getAnimationPreset(anim.preset);
            if (animation == null)
            {
                PokecubeCore.LOGGER.warn("Warning, unknown animation for preset: {}", anim.preset);
                continue;
            }
            final int start = Integer.parseInt(anim.starttick);
            final int dur = Integer.parseInt(anim.duration);
            if (anim.applyAfter) this.applicationTick = Math.max(start + dur, this.applicationTick);
            this.duration = Math.max(this.duration, start + dur);
            final WrappedAnimation wrapped = new WrappedAnimation();
            if (anim.sound != null)
            {
                wrapped.sound = new ResourceLocation(anim.sound);
                wrapped.soundSource = anim.soundSource != null ? anim.soundSource : false;
                wrapped.soundTarget = anim.soundTarget != null ? anim.soundTarget : true;
                wrapped.pitch = anim.pitch != null ? anim.pitch : 1;
                wrapped.volume = anim.volume != null ? anim.volume : 1;
            }
            wrapped.wrapped = animation;
            wrapped.start = start;
            this.components.add(wrapped);
        }
        this.components.sort((arg0, arg1) -> arg0.start - arg1.start);
    }

    @Override
    public void clientAnimation(final PoseStack mat, final MultiBufferSource buffer, final MovePacketInfo info,
            final float partialTick)
    {
        final int tick = info.currentTick;
        for (final WrappedAnimation toRun : this.components)
        {
            info.currentTick = tick;
            if (tick > toRun.start + toRun.wrapped.getDuration()) continue;
            if (toRun.start > tick) continue;
            info.currentTick = tick - toRun.start;
            toRun.wrapped.clientAnimation(mat, buffer, info, partialTick);
        }
    }

    @Override
    public int getApplicationTick()
    {
        return this.applicationTick;
    }

    @Override
    public void initColour(final long time, final float partialTicks, final Move_Base move)
    {
        // We don't do this.
    }

    @Override
    public void spawnClientEntities(final MovePacketInfo info)
    {
        final int tick = info.currentTick;
        final float scale = (float) PokecubeCore.getConfig().moveVolumeEffect;
        final Level world = PokecubeCore.proxy.getWorld();
        for (int i = 0; i < this.components.size(); i++)
        {
            info.currentTick = tick;
            final WrappedAnimation toRun = this.components.get(i);
            if (tick > toRun.start + toRun.wrapped.getDuration()) continue;
            if (toRun.start > tick) continue;
            info.currentTick = tick - toRun.start;
            toRun.wrapped.spawnClientEntities(info);
            final float volume = toRun.volume * scale;
            final float pitch = toRun.pitch;
            sound:
            if (info.currentTick == 0 && toRun.sound != null)
            {
                if (toRun.soundEvent == null)
                {
                    toRun.soundEvent = ForgeRegistries.SOUND_EVENTS.getValue(toRun.sound);
                    if (toRun.soundEvent == null)
                    {
                        PokecubeCore.LOGGER.error("No Registered Sound for " + toRun.sound);
                        toRun.sound = null;
                        break sound;
                    }
                }
                boolean valid = toRun.soundSource;
                final Vector3 pos = Vector3.getNewVector();
                // Check source sounds.
                if (valid = info.source != null || info.attacker != null) pos.set(info.source != null ? info.source
                        : info.attacker);
                if (valid) world.playLocalSound(pos.x, pos.y, pos.z, toRun.soundEvent, SoundSource.HOSTILE, volume,
                        pitch, true);
                // Check target sounds.
                valid = toRun.soundTarget;
                if (valid = info.target != null || info.attacked != null) pos.set(info.target != null ? info.target
                        : info.attacked);
                if (valid) world.playLocalSound(pos.x, pos.y, pos.z, toRun.soundEvent, SoundSource.HOSTILE, volume,
                        pitch, true);
            }
        }
    }

}
