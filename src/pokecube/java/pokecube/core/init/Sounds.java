package pokecube.core.init;

import java.util.function.Supplier;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import pokecube.api.data.moves.Animations.AnimationJson;
import pokecube.api.data.moves.Moves;
import pokecube.core.PokecubeCore;

public class Sounds
{
    public static final Supplier<SoundEvent> CAPTURE_SOUND;
    public static final Supplier<SoundEvent> HEAL_SOUND;
    public static final Supplier<SoundEvent> HEAL_MUSIC;
    public static final Supplier<SoundEvent> REPEL_SPRAYS;

    static
    {
        CAPTURE_SOUND = PokecubeCore.SOUNDS.register("pokecube_caught", () -> SoundEvent
                .createVariableRangeEvent(ResourceLocation.parse(PokecubeCore.MODID + ":pokecube_caught")));
        HEAL_SOUND = PokecubeCore.SOUNDS.register("pokecenter",
                () -> SoundEvent.createVariableRangeEvent(ResourceLocation.parse(PokecubeCore.MODID + ":pokecenter")));
        HEAL_MUSIC = PokecubeCore.SOUNDS.register("pokecenterloop", () -> SoundEvent
                .createVariableRangeEvent(ResourceLocation.parse(PokecubeCore.MODID + ":pokecenterloop")));
        REPEL_SPRAYS = PokecubeCore.SOUNDS.register("block.repel_sprays", () -> SoundEvent
                .createVariableRangeEvent(ResourceLocation.parse(PokecubeCore.MODID + ":block.repel_sprays")));
    }

    public static void init()
    {}

    private static void registerIfNotPresent(ResourceLocation sound, SoundEvent event)
    {
        try
        {
            PokecubeCore.SOUNDS.register(sound.getPath(), () -> event);
        }
        catch (IllegalArgumentException e)
        {
            // pass here, it means it was already present!
        }
    }

    public static void initConfigSounds()
    {
        for (final String var : PokecubeCore.getConfig().customSounds)
        {
            final ResourceLocation sound = ResourceLocation.parse(var);
            final SoundEvent event = SoundEvent.createVariableRangeEvent(sound);
            if (!sound.getNamespace().equals("minecraft")) registerIfNotPresent(sound, event);
        }
    }

    public static void initMoveSounds()
    {
        // null as it should have been populated already
        for (final var entry : Moves.ALL_MOVES)
        {
            // Register sound on source
            if (entry._sound_effect_source != null)
            {
                final ResourceLocation sound = ResourceLocation.parse(entry.getMove().sound_effect_source);
                // TODO: Check if correct
                final SoundEvent event = SoundEvent.createVariableRangeEvent(sound);
                if (!sound.getNamespace().equals("minecraft")) registerIfNotPresent(sound, event);
            }
            // Register sound on target
            if (entry._sound_effect_target != null)
            {
                final ResourceLocation sound = ResourceLocation.parse(entry.getMove().sound_effect_target);
                // TODO: Check if correct
                final SoundEvent event = SoundEvent.createVariableRangeEvent(sound);
                if (!sound.getNamespace().equals("minecraft")) registerIfNotPresent(sound, event);
            }
            // Register sounds for the animations
            if (entry.animation.animations != null)
                for (final AnimationJson anim : entry.animation.animations) if (anim.sound != null)
            {
                final ResourceLocation sound = ResourceLocation.parse(anim.sound);
                // TODO: Check if correct
                final SoundEvent event = SoundEvent.createVariableRangeEvent(sound);
                if (!sound.getNamespace().equals("minecraft")) registerIfNotPresent(sound, event);
            }
        }
    }
}
