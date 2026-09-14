package pokecube.core.init;

import java.util.List;
import java.util.function.Supplier;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import pokecube.api.PokecubeAPI;
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

    private static Object registerIfNotPresent(ResourceLocation sound, SoundEvent event)
    {
        try
        {
            PokecubeCore.SOUNDS.register(sound.getPath(), () -> event);
            return null;
        }
        catch (Exception e)
        {
            // pass here, it means it was already present!
            return e;
        }
    }

    private static void checkIfRegistered(ResourceLocation sound, SoundEvent event)
    {
        var except = registerIfNotPresent(sound, event);
        if (except instanceof IllegalStateException)
        {
            boolean exists = PokecubeCore.SOUNDS.getRegistry().get().containsKey(sound);
            if (!exists)
            {
                PokecubeAPI.LOGGER.error("No Sound: {}", sound);
            }
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

    public static void initMoveSounds(List<Moves.MoveHolder> moves)
    {
        // null as it should have been populated already
        for (final var entry : moves)
        {
            // Check sound on source
            if (entry._sound_effect_source != null)
            {
                final ResourceLocation sound = ResourceLocation.parse(entry.getMove().sound_effect_source);
                final SoundEvent event = SoundEvent.createVariableRangeEvent(sound);
                if (!sound.getNamespace().equals("minecraft")) checkIfRegistered(sound, event);
            }
            // Check sound on target
            if (entry._sound_effect_target != null)
            {
                final ResourceLocation sound = ResourceLocation.parse(entry.getMove().sound_effect_target);
                final SoundEvent event = SoundEvent.createVariableRangeEvent(sound);
                if (!sound.getNamespace().equals("minecraft")) checkIfRegistered(sound, event);
            }
            // Check sounds for the animations if present
            if (entry.animation != null && entry.animation.animations != null)
                for (final AnimationJson anim : entry.animation.animations)
                    if (anim.sound != null)
                    {
                        final ResourceLocation sound = ResourceLocation.parse(anim.sound);
                        final SoundEvent event = SoundEvent.createVariableRangeEvent(sound);
                        if (!sound.getNamespace().equals("minecraft")) checkIfRegistered(sound, event);
                    }
        }
    }
}
