package thut.api.data;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.Reader;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;

import javax.annotation.Nullable;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import thut.api.data.DataHelpers.IResourceData;
import thut.api.util.UnderscoreIgnore;
import thut.core.common.ThutCore;
import thut.lib.ResourceHelper;

public class StringTag<T> implements IResourceData
{
    public static Function<String, Map<ResourceLocation, List<Resource>>> RESOURCE_PROVIDER = name -> Collections
            .emptyMap();
    public static Function<Resource, BufferedReader> READER_PROVIDER = ResourceHelper::getReader;

    public static class StringValue<T>
    {
        public final String name;
        public T value = null;

        public Object _cached = null;

        public StringValue(String name)
        {
            this.name = name;
        }

        public StringValue<T> setValue(T value)
        {
            this.value = value;
            return this;
        }

        public T getValue()
        {
            return value;
        }
    }

    public static class TagHolder<T>
    {
        boolean replace = false;

        List<StringValue<T>> values = Lists.newArrayList();

        List<TagHolder<T>> _includes = Lists.newArrayList();
        List<StringValue<T>> _all_values = Lists.newArrayList();

        private Map<String, StringValue<T>> _values = Maps.newHashMap();

        void postProcess()
        {
            this.values.replaceAll(s -> {
                boolean tag = false;
                String name = s.name;
                if (tag = name.startsWith("#")) name = name.replace("#", "");
                if (!name.contains(":")) name = "pokecube:" + ThutCore.trim(name);
                if (tag) name = "#" + name;
                if (name.equals(s.name)) return s;
                else return new StringValue<T>(name).setValue(s.getValue());
            });
            this.values.forEach(s -> {
                _values.put(s.name, s);
            });
        }

        public StringValue<T> isIn(final String value)
        {
            return this._values.getOrDefault(value, getFromIncludes(value));
        }

        public List<StringValue<T>> addOrGetValues(@Nullable List<StringValue<T>> values)
        {
            if (!_all_values.isEmpty()) return _all_values;
            if (values == null) values = this._all_values;
            values.addAll(this.values);
            for (var a : _includes) a.addOrGetValues(values);
            return values;
        }

        private StringValue<T> getFromIncludes(String value)
        {
            for (final TagHolder<T> incl : this._includes)
            {
                var val = incl.isIn(value);
                if (val != null) return val;
            }
            return null;
        }

        protected void checkIncludes(final StringTag<T> parent, final Set<String> checked)
        {
            // DOLATER possible speedup by adding the included tags to our list,
            // instead of referencing the included tags.

            for (final String s : this._values.keySet()) if (s.startsWith("#"))
            {
                final String tag = s.replace("#", "");
                if (checked.contains(tag))
                {
                    ThutCore.LOGGER.warn("Warning, Recursive tags list! {}", checked);
                    continue;
                }
                final TagHolder<T> incl = parent.tagsMap.get(tag);
                if (incl == null)
                {
                    ThutCore.LOGGER.warn("Warning, Tag not found for {}", s);
                    continue;
                }
                this._includes.add(incl);
            }
        }
    }

    private final Map<String, TagHolder<T>> tagsMap = Maps.newHashMap();

    private final Map<String, Set<String>> reversedTagsMap = Maps.newHashMap();

    private final String tagPath;

    private final Gson gson;

    public boolean validLoad = false;

    private final Runnable onLoaded;

    public StringTag(String path)
    {
        this(path, null);
    }

    public StringTag(String path, Class<?> type)
    {
        this(path, type, () -> {});
    }

    public StringTag(String path, Class<?> type, Runnable onLoaded)
    {
        this.tagPath = path;
        this.onLoaded = onLoaded;
        DataHelpers.addDataType(this);
        gson = new GsonBuilder().registerTypeAdapter(StringValue.class, new StringValueAdaptor(type))
                .setPrettyPrinting().disableHtmlEscaping().setExclusionStrategies(UnderscoreIgnore.INSTANCE).create();
    }

    @Override
    public void reload(final AtomicBoolean valid)
    {
        this.tagsMap.clear();
        this.validLoad = false;
        try
        {
            final String path = new ResourceLocation(this.tagPath).getPath();
            var resources = RESOURCE_PROVIDER.apply(path);
            this.validLoad = !resources.isEmpty();
            resources.forEach((l, r) -> {
                if (l.toString().contains("//")) l = new ResourceLocation(l.toString().replace("//", "/"));
                final String tag = l.toString().replace(path, "").replace(".json", "");
                this.loadTag(l, r, tag, "");
            });
            this.checkIncludes();
        }
        catch (final Exception e)
        {
            ThutCore.LOGGER.error("Error reloading tags for {}", this.tagPath);
            e.printStackTrace();
        }
        if (this.validLoad)
        {
            valid.set(true);
            onLoaded.run();
        }
    }

    public void AddValue(String key, StringValue<T> value)
    {
        TagHolder<T> holder = this.tagsMap.computeIfAbsent(key, (k) -> {
            var h = new TagHolder<T>();
            return h;
        });

        if (!holder._values.containsKey(value.name))
        {
            holder.values.add(value);
            holder._values.put(value.name, value);
        }
        // Now we update the reversedTagsMap accordingly
        // Iterate over the values in the tag, and put toCheck in their set.
        for (StringValue<T> s : holder.values)
        {
            String name = s.name;
            if (!name.contains(":")) name = "pokecube:" + ThutCore.trim(name);
            final Set<String> tags = this.reversedTagsMap.getOrDefault(name, Sets.newHashSet());
            tags.add(key);
            this.reversedTagsMap.put(name, tags);
        }
    }

    public Set<String> lookupTags(String name)
    {
        if (!name.contains(":")) name = "pokecube:" + ThutCore.trim(name);
        return this.reversedTagsMap.getOrDefault(name, Collections.emptySet());
    }

    public void checkIncludes()
    {
        for (final Entry<String, TagHolder<T>> entry : this.tagsMap.entrySet())
            entry.getValue().checkIncludes(this, Sets.newHashSet(entry.getKey()));
    }

    public boolean isIn(String tag, String toCheck)
    {
        return getValueHolder(tag, toCheck) != null;
    }

    public T get(String tag, String toCheck)
    {
        var holder = this.getValueHolder(tag, toCheck);
        return holder == null ? null : holder.getValue();
    }

    public StringValue<T> getValueHolder(String tag, String toCheck)
    {
        if (!toCheck.contains(":")) toCheck = "pokecube:" + ThutCore.trim(toCheck);
        if (!tag.contains(":")) tag = "pokecube:" + ThutCore.trim(tag);
        // If we have the tag loaded, lets use the value from there.
        if (this.tagsMap.containsKey(tag))
        {
            final TagHolder<T> holder = this.tagsMap.get(tag);
            var value = holder.isIn(toCheck);
            if (value != null) return value;
        }
        return null;
    }

    public Collection<String> getKeys()
    {
        return tagsMap.keySet();
    }

    public List<StringValue<T>> getValues(String tag)
    {
        var holder = this.tagsMap.get(tag);
        if (holder == null) return Collections.emptyList();
        return holder.addOrGetValues(null);
    }

    private boolean loadTag(final ResourceLocation tagLoc, List<Resource> resources, final String tag,
            final String toCheck)
    {
        try
        {
            final TagHolder<T> tagged = new TagHolder<T>();
            for (final Resource resource : resources)
            {
                final Reader reader = READER_PROVIDER.apply(resource);
                @SuppressWarnings("unchecked")
                final TagHolder<T> temp = gson.fromJson(reader, TagHolder.class);
                temp.postProcess();
                if (temp.replace) tagged.values.clear();
                temp._values.forEach((k, v) -> {
                    if (!tagged._values.containsKey(k))
                    {
                        tagged.values.add(v);
                        tagged._values.put(k, v);
                    }
                });
                reader.close();
                // If we were replacing, we want to exit here.
                if (temp.replace) break;
            }
            this.tagsMap.put(tag, tagged);
            // Now we update the reversedTagsMap accordingly
            // Iterate over the values in the tag, and put toCheck in their set.
            for (StringValue<T> s : tagged.values)
            {
                String name = s.name;
                if (!name.contains(":")) name = "pokecube:" + ThutCore.trim(name);
                final Set<String> tags = this.reversedTagsMap.getOrDefault(name, Sets.newHashSet());
                tags.add(tag);
                this.reversedTagsMap.put(name, tags);
            }
            // now just return if it was present.
            return tagged.isIn(toCheck) != null;
        }
        catch (final FileNotFoundException e)
        {
            ThutCore.logDebug("No Tag: {}", tagLoc);
        }
        catch (final Exception e)
        {
            ThutCore.LOGGER.error("Error reading tag " + tagLoc, e);
        }
        return false;
    }

    @Override
    public String getKey()
    {
        return tagPath;
    }

}
