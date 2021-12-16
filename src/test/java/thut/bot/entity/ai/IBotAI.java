package thut.bot.entity.ai;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;

import javax.annotation.Nullable;

import org.objectweb.asm.Type;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.loading.moddiscovery.ModFile;
import net.minecraftforge.forgespi.language.ModFileScanData.AnnotationData;
import thut.bot.entity.BotPlayer;
import thut.lib.CompatParser.ClassFinder;

public interface IBotAI
{
    static Map<String, Factory<?>> REGISTRY = Maps.newHashMap();

    static List<String> MODULEPACKAGES = Lists.newArrayList();

    void tick();

    BotPlayer getBot();

    String getKey();

    void setKey(String key);

    default CompoundTag getTag()
    {
        CompoundTag tag = getBot().getPersistentData().getCompound(getKey());
        getBot().getPersistentData().put(getKey(), tag);
        return tag;
    }

    default void start(@Nullable ServerPlayer commander)
    {}

    default void end(@Nullable ServerPlayer commander)
    {}

    default boolean init(String args)
    {
        return true;
    }

    default boolean canComplete()
    {
        return false;
    }

    default boolean isCompleted()
    {
        return false;
    }

    public static interface Factory<T extends IBotAI>
    {
        T create(BotPlayer owner);
    }

    @SuppressWarnings("unchecked")
    static void init()
    {
        REGISTRY.clear();
        Type ANNOTE = Type.getType("Lthut/bot/entity/ai/BotAI;");
        BiFunction<ModFile, String, Boolean> validClass = (file, name) -> {
            for (final AnnotationData a : file.getScanResult().getAnnotations())
                if (name.equals(a.clazz().getClassName()) && a.annotationType().equals(ANNOTE))
            {
                if (a.annotationData().containsKey("mod"))
                {
                    String modid = (String) a.annotationData().get("mod");
                    return ModList.get().isLoaded(modid);
                }
                return true;
            }
            return false;
        };

        Collection<Class<?>> foundClasses;
        for (String name : MODULEPACKAGES)
        {
            try
            {
                foundClasses = ClassFinder.find(name, validClass);
                for (final Class<?> candidateClass : foundClasses)
                {
                    if (candidateClass.getAnnotations().length == 0) continue;
                    final BotAI preset = candidateClass.getAnnotation(BotAI.class);
                    if (preset != null)
                    {
                        Constructor<? extends IBotAI> construct;
                        try
                        {
                            construct = (Constructor<? extends IBotAI>) candidateClass.getConstructor(BotPlayer.class);
                            REGISTRY.put(preset.key(), owner -> {
                                try
                                {
                                    return construct.newInstance(owner);
                                }
                                catch (InstantiationException | IllegalAccessException | IllegalArgumentException
                                        | InvocationTargetException e)
                                {
                                    e.printStackTrace();
                                }
                                return null;
                            });
                        }
                        catch (NoSuchMethodException | SecurityException e1)
                        {
                            e1.printStackTrace();
                        }
                    }
                }

            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }

    }
}
