package pokecube.core.init;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraftforge.registries.RegistryObject;
import pokecube.api.data.moves.Animations.AnimationJson;
import pokecube.api.data.moves.Moves;
import pokecube.core.PokecubeCore;

public class Sounds
{
    public static final RegistryObject<SoundEvent> CAPTURE_SOUND;
    public static final RegistryObject<SoundEvent> HEAL_SOUND;
    public static final RegistryObject<SoundEvent> HEAL_MUSIC;
    public static final RegistryObject<SoundEvent> REPEL_SPRAYS;

    static
    {
        CAPTURE_SOUND = PokecubeCore.SOUNDS.register("pokecube_caught",
                () -> SoundEvent.createVariableRangeEvent(new ResourceLocation(PokecubeCore.MODID + ":pokecube_caught")));
        HEAL_SOUND = PokecubeCore.SOUNDS.register("pokecenter",
                () -> SoundEvent.createVariableRangeEvent(new ResourceLocation(PokecubeCore.MODID + ":pokecenter")));
        HEAL_MUSIC = PokecubeCore.SOUNDS.register("pokecenterloop",
                () -> SoundEvent.createVariableRangeEvent(new ResourceLocation(PokecubeCore.MODID + ":pokecenterloop")));
        REPEL_SPRAYS = PokecubeCore.SOUNDS.register("block.repel_sprays",
                () -> SoundEvent.createVariableRangeEvent(new ResourceLocation(PokecubeCore.MODID + ":block.repel_sprays")));
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
            final ResourceLocation sound = new ResourceLocation(var);
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
                final ResourceLocation sound = new ResourceLocation(entry.getMove().sound_effect_source);
                // TODO: Check if correct
                final SoundEvent event = SoundEvent.createVariableRangeEvent(sound);
                if (!sound.getNamespace().equals("minecraft")) registerIfNotPresent(sound, event);
            }
            // Register sound on target
            if (entry._sound_effect_target != null)
            {
                final ResourceLocation sound = new ResourceLocation(entry.getMove().sound_effect_target);
                // TODO: Check if correct
                final SoundEvent event = SoundEvent.createVariableRangeEvent(sound);
                if (!sound.getNamespace().equals("minecraft")) registerIfNotPresent(sound, event);
            }
            // Register sounds for the animations
            if (entry.animation.animations != null)
                for (final AnimationJson anim : entry.animation.animations) if (anim.sound != null)
            {
                final ResourceLocation sound = new ResourceLocation(anim.sound);
                // TODO: Check if correct
                final SoundEvent event = SoundEvent.createVariableRangeEvent(sound);
                if (!sound.getNamespace().equals("minecraft")) registerIfNotPresent(sound, event);
            }
        }
    }
}
