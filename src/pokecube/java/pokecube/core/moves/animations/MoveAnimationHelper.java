package pokecube.core.moves.animations;

import com.google.common.collect.Maps;
import com.google.gson.JsonObject;
import net.neoforged.neoforgespi.language.ModFileScanData.AnnotationData;
import net.neoforged.neoforgespi.locating.IModFile;
import org.objectweb.asm.Type;
import pokecube.api.PokecubeAPI;
import pokecube.api.effects.IMoveAnimation;
import thut.lib.CompatParser.ClassFinder;

import java.util.Collection;
import java.util.Map;
import java.util.function.BiFunction;

public class MoveAnimationHelper
{
    private static final Type PRESETANNOTATION = Type.getType("Lpokecube/core/moves/animations/AnimPreset;");

    static Map<String, Class<? extends MoveAnimationBase>> presets = Maps.newHashMap();

    private static final BiFunction<IModFile, String, Boolean> validClass = (file, name) -> {
        for (final AnnotationData a : file.getScanResult().getAnnotations())
            if (name.equals(a.clazz().getClassName()) && a.annotationType()
                    .equals(MoveAnimationHelper.PRESETANNOTATION)) return true;
        return false;
    };

    static
    {
        Collection<Class<?>> foundClasses;
        try
        {
            foundClasses = ClassFinder.find(MoveAnimationHelper.class.getPackage().getName(),
                    MoveAnimationHelper.validClass);
            for (final Class<?> candidateClass : foundClasses)
            {
                if (!MoveAnimationBase.class.isAssignableFrom(candidateClass)) continue;
                if (candidateClass.getAnnotations().length == 0) continue;
                final AnimPreset preset = candidateClass.getAnnotation(AnimPreset.class);
                if (preset != null)
                {
                    @SuppressWarnings("unchecked")
                    final Class<? extends MoveAnimationBase> presetClass = (Class<? extends MoveAnimationBase>) candidateClass;
                    MoveAnimationHelper.presets.put(preset.getPreset(), presetClass);
                }
            }
        }
        catch (final Exception e)
        {
            PokecubeAPI.LOGGER.error(e);
        }
    }

    public static IMoveAnimation getAnimationPreset(final String preset, JsonObject values)
    {
        MoveAnimationBase animation = null;
        if (preset == null || preset.isEmpty()) return animation;
        final Class<? extends MoveAnimationBase> presetClass = MoveAnimationHelper.presets.get(preset);
        if (presetClass != null) try
        {
            animation = presetClass.getConstructor().newInstance();
            animation.init(values);
        }
        catch (final Exception e)
        {
            PokecubeAPI.LOGGER.error(e);
        }
        return animation;
    }
}
