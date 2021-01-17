package pokecube.core.database.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.nio.charset.Charset;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import net.minecraft.resources.IResource;
import net.minecraft.util.ResourceLocation;
import pokecube.core.PokecubeCore;
import pokecube.core.database.Database;
import pokecube.core.database.PokedexEntryLoader;
import thut.core.common.ThutCore;

public class StringTagsHelper
{
    public static class TagHolder
    {
        boolean replace = false;

        List<String> values = Lists.newArrayList();

        List<TagHolder> _includes = Lists.newArrayList();

        void postProcess()
        {
            this.values.replaceAll(s ->
            {
                boolean tag = false;
                if (tag = s.startsWith("#")) s = s.replace("#", "");
                s = ThutCore.trim(s);
                if (!s.contains(":")) s = "pokecube:" + s;
                if (tag) s = "#" + s;
                return s;
            });
        }

        public boolean isIn(final String value)
        {
            if (this.values.contains(value)) return true;
            for (final TagHolder incl : this._includes)
                if (incl.isIn(value)) return true;
            return false;
        }

        public void checkIncludes(final StringTagsHelper parent, final Set<String> checked)
        {
            // TODO possible speedup by adding the included tags to our list,
            // instead of referencing the included tags.

            for (final String s : this.values)
                if (s.startsWith("#"))
                {
                    final String tag = s.replace("#", "");
                    if (checked.contains(tag))
                    {
                        PokecubeCore.LOGGER.warn("Warning, Recursive tags list! {}", checked);
                        continue;
                    }
                    final TagHolder incl = parent.tagsMap.get(tag);
                    if (incl == null)
                    {
                        PokecubeCore.LOGGER.warn("Warning, Tag not found for {}", checked);
                        continue;
                    }
                    this._includes.add(incl);
                }
        }
    }

    private static final Set<StringTagsHelper> tagHelpers = Sets.newHashSet();

    public static void onResourcesReloaded()
    {
        final AtomicBoolean valid = new AtomicBoolean(false);
        StringTagsHelper.tagHelpers.forEach(t ->
        {
            t.tagsMap.clear();
            t.validLoad = false;
            try
            {
                final String path = new ResourceLocation(t.tagPath).getPath();
                final Collection<ResourceLocation> resources = Database.resourceManager.getAllResourceLocations(path,
                        s -> s.endsWith(".json"));
                t.validLoad = !resources.isEmpty();
                resources.forEach(l ->
                {
                    final String tag = l.toString().replace(path, "").replace(".json", "");
                    t.loadTag(l, tag, "");
                });
                t.checkIncludes();
            }
            catch (final Exception e)
            {
                PokecubeCore.LOGGER.error("Error reloading tags for {}", t.tagPath);
                e.printStackTrace();
            }
            if (t.validLoad) valid.set(true);
        });
        if (valid.get()) PokecubeCore.LOGGER.debug("Reloaded Custom Tags");
    }

    private final Map<String, TagHolder> tagsMap = Maps.newHashMap();

    private final Map<String, Set<String>> reversedTagsMap = Maps.newHashMap();

    private final String tagPath;

    public boolean validLoad = false;

    public StringTagsHelper(final String path)
    {
        this.tagPath = path;
        StringTagsHelper.tagHelpers.add(this);
    }

    public Set<String> lookupTags(final String name)
    {
        return this.reversedTagsMap.getOrDefault(name, Collections.emptySet());
    }

    public void checkIncludes()
    {
        for (final Entry<String, TagHolder> entry : this.tagsMap.entrySet())
            entry.getValue().checkIncludes(this, Sets.newHashSet(entry.getKey()));
    }

    public void addToTag(String tag, String value)
    {
        tag = ThutCore.trim(tag);
        if (!tag.contains(":")) tag = "pokecube:" + tag;
        value = ThutCore.trim(value);
        TagHolder holder = this.tagsMap.get(tag);
        if (holder == null) this.tagsMap.put(tag, holder = new TagHolder());
        if (holder.values.contains(value)) return;
        holder.values.add(value);
        final Set<String> tagged = this.reversedTagsMap.getOrDefault(value, Sets.newHashSet());
        tagged.add(tag);
        this.reversedTagsMap.put(value, tagged);
    }

    public boolean isIn(String tag, String toCheck)
    {
        tag = ThutCore.trim(tag);
        toCheck = ThutCore.trim(toCheck);
        if (!tag.contains(":")) tag = "pokecube:" + tag;
        // If we have the tag loaded, lets use the value from there.
        if (this.tagsMap.containsKey(tag))
        {
            final TagHolder holder = this.tagsMap.get(tag);
            return holder.isIn(toCheck);
        }
        return false;
    }

    // This is a helper method for generating tags as needed
    void printTags()
    {
        this.tagsMap.forEach((s, h) ->
        {
            final ResourceLocation name = new ResourceLocation(s);
            final File dir = new File("./generated/data/" + name.getNamespace() + "/" + this.tagPath);
            if (!dir.exists()) dir.mkdirs();
            File file = null;
            file = new File(dir, name.getPath() + ".json");
            String json = "";
            try
            {
                json = PokedexEntryLoader.gson.toJson(h);
                final OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(file), Charset.forName(
                        "UTF-8").newEncoder());
                writer.write(json);
                writer.close();
            }
            catch (final Exception e)
            {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        });
    }

    private boolean loadTag(final ResourceLocation tagLoc, final String tag, final String toCheck)
    {
        try
        {
            final TagHolder tagged = new TagHolder();
            for (final IResource resource : Database.resourceManager.getAllResources(tagLoc))
            {
                final InputStream res = resource.getInputStream();
                final Reader reader = new InputStreamReader(res);
                final TagHolder temp = PokedexEntryLoader.gson.fromJson(reader, TagHolder.class);
                temp.postProcess();
                if (temp.replace) tagged.values.clear();
                temp.values.forEach(s ->
                {
                    if (!tagged.values.contains(s)) tagged.values.add(s);
                });
                reader.close();
            }
            this.tagsMap.put(tag, tagged);
            // Now we update the reversedTagsMap accordingly
            // Iterate over the values in the tag, and put toCheck in their set.
            for (final String s : tagged.values)
            {
                final Set<String> tags = this.reversedTagsMap.getOrDefault(s, Sets.newHashSet());
                tags.add(tag);
                this.reversedTagsMap.put(s, tags);
            }
            // now just return if it was present.
            return tagged.values.contains(toCheck);
        }
        catch (final FileNotFoundException e)
        {
            PokecubeCore.LOGGER.debug("No Tag: {}", tagLoc);
        }
        catch (final Exception e)
        {
            PokecubeCore.LOGGER.error("Error reading tag " + tagLoc, e);
        }
        return false;
    }

}
