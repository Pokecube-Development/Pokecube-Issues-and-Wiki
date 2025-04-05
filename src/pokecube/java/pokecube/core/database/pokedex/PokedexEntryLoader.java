package pokecube.core.database.pokedex;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.function.Predicate;

import javax.annotation.Nullable;

import org.apache.commons.lang3.ClassUtils;

import com.google.common.collect.Maps;
import com.google.gson.Gson;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import pokecube.api.PokecubeAPI;
import pokecube.api.data.PokedexEntry;
import pokecube.api.data.PokedexEntry.MovementType;
import pokecube.api.data.PokedexEntry.SpawnData;
import pokecube.api.data.PokedexEntry.SpawnData.SpawnEntry;
import pokecube.api.data.pokedex.DefaultFormeHolder;
import pokecube.api.data.spawns.SpawnBiomeMatcher;
import pokecube.api.data.spawns.SpawnRule;
import pokecube.api.entity.pokemob.IPokemob.FormeHolder;
import pokecube.api.events.pokemobs.SpawnEvent.FunctionVariance;
import pokecube.api.utils.Tools;
import pokecube.core.PokecubeCore;
import pokecube.core.commands.arguments.PokemobArgument;
import pokecube.core.database.Database;
import pokecube.core.database.spawns.PokemobSpawns;
import pokecube.core.database.spawns.PokemobSpawns.SpawnSet;
import thut.api.util.JsonUtil;
import thut.core.xml.bind.annotation.XmlAnyAttribute;
import thut.core.xml.bind.annotation.XmlElement;
import thut.core.xml.bind.annotation.XmlRootElement;

public class PokedexEntryLoader
{
    @Retention(RUNTIME)
    @Target(FIELD)
    public @interface ManualCopy
    {

    }

    public static interface IMergeable<T>
    {
        T mergeFrom(@Nullable T other);

        default void mergeBasic(T other)
        {
            Field fields[] = new Field[] {};
            try
            {
                // returns the array of Field objects representing the public
                // fields
                fields = this.getClass().getDeclaredFields();
            }
            catch (final Exception e)
            {
                e.printStackTrace();
            }

            var def = new JsonPokedexEntry();

            for (final Field field : fields) try
            {
                if (Modifier.isFinal(field.getModifiers())) continue;
                if (Modifier.isStatic(field.getModifiers())) continue;
                if (Modifier.isTransient(field.getModifiers())) continue;
                var theirs = field.get(other);

                if (theirs != null && theirs != field.get(this))
                {
                    if (ClassUtils.isPrimitiveOrWrapper(theirs.getClass()) && theirs == field.get(def)) continue;
                    if (!ClassUtils.isPrimitiveOrWrapper(theirs.getClass()) && theirs.equals(field.get(def))) continue;
                    field.set(this, field.get(other));
                }
            }
            catch (final Exception e)
            {
                try
                {
                    field.set(this, field.get(other));
                }
                catch (Exception e2)
                {
                    PokecubeAPI.LOGGER.error("Cannot merge field {}, {}", field.getName(), e);
                }
            }
        }
    }

    @XmlRootElement(name = "Drop")
    public static class Drop
    {
        @XmlAnyAttribute
        public Map<String, String> values = Maps.newHashMap();
        @XmlElement(name = "tag")
        public String tag;
        @XmlElement(name = "id")
        public String id;

        public Map<String, String> getValues()
        {
            if (this.values == null) this.values = Maps.newHashMap();
            final String tagName = new String("tag");
            final String idName = new String("id");
            if (this.tag != null && !this.values.containsKey(tagName)) this.values.put(tagName, this.tag);
            if (this.id != null && !this.values.containsKey(idName)) this.values.put(idName, this.id);
            return this.values;
        }
    }

    public static final Gson gson = JsonUtil.gson;

    @SuppressWarnings(
    { "unchecked" })
    public static Object getSerializableCopy(final Class<?> type, final Object original)
            throws InstantiationException, IllegalAccessException
    {
        Field fields[] = new Field[] {};
        try
        {
            // returns the array of Field objects representing the public fields
            fields = type.getDeclaredFields();
        }
        catch (final Exception e)
        {
            e.printStackTrace();
        }
        Object copy = null;
        try
        {
            copy = type.getConstructor().newInstance();
        }
        catch (final Exception e1)
        {
            copy = original;
        }
        if (copy == original || copy != null && copy.equals(original))
        {
            return copy;
        }
        Object value;
        Object defaultvalue;
        for (final Field field : fields) try
        {
            if (Modifier.isFinal(field.getModifiers())) continue;
            if (Modifier.isStatic(field.getModifiers())) continue;
            if (Modifier.isTransient(field.getModifiers())) continue;
            if (field.getName().startsWith("_")) continue;
            try
            {
                field.setAccessible(true);
            }
            catch (Throwable e)
            {
                // Module stuff can make this fail in some cases.
                continue;
            }
            value = field.get(original);
            defaultvalue = field.get(copy);
            if (value == null) continue;
            if (ClassUtils.isPrimitiveOrWrapper(value.getClass())) field.set(copy, value);
            else if (defaultvalue != null && defaultvalue.equals(value)) field.set(copy, null);
            else if (value instanceof String)
            {
                if (((String) value).isEmpty()) field.set(copy, null);
                else field.set(copy, value);
            }
            else if (value instanceof Object[])
            {
                if (((Object[]) value).length == 0) field.set(copy, null);
                else field.set(copy, value);
            }
            else if (value instanceof Map)
            {
                if (((Map<?, ?>) value).isEmpty()) field.set(copy, null);
                else field.set(copy, value);
            }
            else if (value instanceof Collection)
            {
                if (((Collection<?>) value).isEmpty()) field.set(copy, null);
                else
                {
                    if (value instanceof List)
                    {
                        final List<Object> args = (List<Object>) value;
                        final ListIterator<Object> iter = args.listIterator();
                        while (iter.hasNext())
                        {
                            final Object var = iter.next();
                            iter.set(PokedexEntryLoader.getSerializableCopy(var.getClass(), var));
                        }
                    }
                    field.set(copy, value);
                }
            }
            else field.set(copy, PokedexEntryLoader.getSerializableCopy(value.getClass(), value));
        }
        catch (final IllegalAccessException e)
        {
            e.printStackTrace();
        }
        return copy;
    }

    /**
     * This must be called after tags are loaded server side.
     *
     * @param d
     * @return
     */
    public static ItemStack getStackFromDrop(final Drop drop)
    {
        return Tools.getStack(drop.getValues());
    }

    public static final String MIN = new String("min");
    public static final String MAX = new String("max");
    public static final String RATE = new String("rate");
    public static final String LEVEL = new String("level");
    public static final String VARIANCE = new String("variance");
    public static final String MINY = new String("minY");
    public static final String MAXY = new String("maxY");

    /**
     * This is safe to run before tags are loaded.
     * 
     * @param entry
     *
     * @param spawnData
     * @param rule
     */
    public static SpawnBiomeMatcher handleAddSpawn(PokedexEntry entry, SpawnBiomeMatcher matcher)
    {
        final SpawnEntry spawnEntry = new SpawnEntry();
        String val;
        SpawnRule rule = matcher.spawnRule.copy();
        if ((val = rule.removeString(MIN)) != null) spawnEntry.min = Integer.parseInt(val);
        if ((val = rule.removeString(MAX)) != null) spawnEntry.max = Integer.parseInt(val);
        if ((val = rule.removeString(MINY)) != null) spawnEntry.minY = Integer.parseInt(val);
        if ((val = rule.removeString(MAXY)) != null) spawnEntry.maxY = Integer.parseInt(val);
        if ((val = rule.removeString(RATE)) != null) spawnEntry.rate = Float.parseFloat(val);
        if ((val = rule.removeString(LEVEL)) != null) spawnEntry.level = Integer.parseInt(val);
        if ((val = rule.removeString(VARIANCE)) != null) spawnEntry.variance = new FunctionVariance(val);
        if (entry.getSpawnData() == null) entry.setSpawnData(new SpawnData(entry));
        matcher = SpawnBiomeMatcher.get(rule);
        entry.getSpawnData().matchers.put(matcher, spawnEntry);
        if (!Database.spawnables.contains(entry)) Database.spawnables.add(entry);
        // If it can spawn in water, then it can swim in water.
        if (matcher.water) entry.mobType |= MovementType.WATER.mask;

        String variant = rule.model == null ? "" : rule.model.key;
        if (!variant.isBlank())
        {
            var matching = PokemobArgument.getMatching(variant);
            SpawnSet set = new SpawnSet(matcher, spawnEntry);
            for (var _entry : matching)
            {
                // this is the main spawn, we ignore it.
                if (_entry == entry) continue;
                // Add the matcher to the custom list for here.
                PokemobSpawns.REGEX_SPAWNS.compute(_entry, (e, list) -> {
                    if (list == null) list = new ArrayList<>();
                    list.add(set);
                    return list;
                });
            }
        }
        return matcher;
    }

    public static void initFormeModels(final PokedexEntry entry, final List<DefaultFormeHolder> list)
    {
        list.forEach(holder -> initFormeModel(entry, holder));
    }

    public static void initFormeModel(final PokedexEntry entry, DefaultFormeHolder holder)
    {
        FormeHolder forme = holder.getForme(entry);
        if (PokecubeCore.getConfig().debug_data) PokecubeAPI.logInfo("Loaded form for {}: ({} {} {}) -> ({} {} {} {})",
                entry, holder.model, holder.anim, holder.tex, forme.key, forme.model, forme.animation, forme.texture);
    }

    public static void updateEntry(final PokedexEntry entry)
    {
        var json = entry._root_json;
        if (json != null)
        {
            Predicate<ResourceLocation> valid = l -> JsonPokedexEntry._compound_files.contains(l)
                    || json.__loaded_from.contains(l);
            JsonPokedexEntry.loadPokedex(valid, false);
        }
    }

    public static void onReloaded()
    {
        JsonPokedexEntry.loadPokedex();
        // now register bulk defined spawns
        PokemobSpawns.registerSpawns();
    }

    public static void mergeNonDefaults(final Object defaults, final Object outOf, final Object inTo)
    {
        if (outOf.getClass() != inTo.getClass())
            throw new IllegalArgumentException("To and From must be of the same class!");
        Field fields[] = new Field[] {};
        try
        {
            fields = outOf.getClass().getDeclaredFields();
        }
        catch (final Exception e)
        {
            e.printStackTrace();
        }
        Object valueOut;
        Object valueIn;
        Object valueDefault;
        for (final Field field : fields) try
        {
            if (Modifier.isFinal(field.getModifiers())) continue;
            if (Modifier.isStatic(field.getModifiers())) continue;
            if (Modifier.isTransient(field.getModifiers())) continue;
            final ManualCopy annot = field.getAnnotation(ManualCopy.class);
            if (annot != null) continue;
            field.setAccessible(true);
            valueOut = field.get(outOf);
            valueIn = field.get(inTo);
            valueDefault = field.get(defaults);
            if (valueOut == null) continue;
            if (valueIn == null && valueOut != null)
            {
                field.set(inTo, valueOut);
                continue;
            }

            final boolean outIsDefault = valueOut == valueDefault
                    || valueDefault != null && valueDefault.equals(valueOut);
            if (!outIsDefault) if (valueOut instanceof String) field.set(inTo, valueOut);
            else if (valueOut instanceof Object[]) field.set(inTo, ((Object[]) valueOut).clone());
            else if (valueOut instanceof Map) field.set(inTo, valueOut);
            else if (valueOut instanceof Collection) field.set(inTo, valueOut);
            else try
            {
                valueDefault = valueOut.getClass().getConstructor().newInstance();
                PokedexEntryLoader.mergeNonDefaults(valueDefault, valueOut, valueIn);
                field.set(inTo, valueIn);
            }
            catch (final Exception e)
            {
                field.set(inTo, valueOut);
            }
        }
        catch (final IllegalAccessException e)
        {
            e.printStackTrace();
        }
    }

    public static void postInit()
    {
        try
        {
            PokedexEntry.InteractionLogic.initDefaults();
        }
        catch (final Exception e)
        {
            PokecubeAPI.LOGGER.error("Error with postinit of loading pokedex entries", e);
        }
    }
}
