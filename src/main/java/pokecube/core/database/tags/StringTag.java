package pokecube.core.database.tags;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraftforge.fml.loading.FMLEnvironment;
import pokecube.api.PokecubeAPI;
import pokecube.core.database.resources.PackFinder;
import pokecube.core.database.util.DataHelpers;
import pokecube.core.database.util.DataHelpers.IResourceData;
import thut.api.util.JsonUtil;
import thut.core.common.ThutCore;

public class StringTag implements IResourceData
{
    public static class TagHolder
    {
        boolean replace = false;

        List<String> values = Lists.newArrayList();

        List<TagHolder> _includes = Lists.newArrayList();

        void postProcess()
        {
            this.values.replaceAll(s -> {
                boolean tag = false;
                if (tag = s.startsWith("#")) s = s.replace("#", "");
                if (!s.contains(":")) s = "pokecube:" + ThutCore.trim(s);
                if (tag) s = "#" + s;
                return s;
            });
        }

        public boolean isIn(final String value)
        {
            if (this.values.contains(value)) return true;
            for (final TagHolder incl : this._includes) if (incl.isIn(value)) return true;
            return false;
        }

        public void checkIncludes(final StringTag parent, final Set<String> checked)
        {
            // TODO possible speedup by adding the included tags to our list,
            // instead of referencing the included tags.

            for (final String s : this.values) if (s.startsWith("#"))
            {
                final String tag = s.replace("#", "");
                if (checked.contains(tag))
                {
                    PokecubeAPI.LOGGER.warn("Warning, Recursive tags list! {}", checked);
                    continue;
                }
                final TagHolder incl = parent.tagsMap.get(tag);
                if (incl == null)
                {
                    PokecubeAPI.LOGGER.warn("Warning, Tag not found for {}", s);
                    continue;
                }
                this._includes.add(incl);
            }
        }
    }

    private final Map<String, TagHolder> tagsMap = Maps.newHashMap();

    private final Map<String, Set<String>> reversedTagsMap = Maps.newHashMap();

    private final String tagPath;

    public boolean validLoad = false;

    public StringTag(final String path)
    {
        this.tagPath = path;
        DataHelpers.addDataType(this);
    }

    @Override
    public void reload(final AtomicBoolean valid)
    {
        if (!FMLEnvironment.production) this.printTags();
        this.tagsMap.clear();
        this.validLoad = false;
        try
        {
            final String path = new ResourceLocation(this.tagPath).getPath();
            var resources = PackFinder.getAllJsonResources(path);
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
            PokecubeAPI.LOGGER.error("Error reloading tags for {}", this.tagPath);
            e.printStackTrace();
        }
        if (this.validLoad) valid.set(true);
    }

    public Set<String> lookupTags(String name)
    {
        if (!name.contains(":")) name = "pokecube:" + ThutCore.trim(name);
        return this.reversedTagsMap.getOrDefault(name, Collections.emptySet());
    }

    public void checkIncludes()
    {
        for (final Entry<String, TagHolder> entry : this.tagsMap.entrySet())
            entry.getValue().checkIncludes(this, Sets.newHashSet(entry.getKey()));
    }

    public void addToTag(String tag, String value)
    {
        if (!tag.contains(":")) tag = "pokecube:" + ThutCore.trim(tag);
        if (!value.contains(":")) value = "pokecube:" + ThutCore.trim(value);
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
        if (!toCheck.contains(":")) toCheck = "pokecube:" + ThutCore.trim(toCheck);
        if (!tag.contains(":")) tag = "pokecube:" + ThutCore.trim(tag);
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
        this.tagsMap.forEach((s, h) -> {
            final ResourceLocation name = new ResourceLocation(s);
            final File dir = new File("./generated/data/" + name.getNamespace() + "/" + this.tagPath);
            if (!dir.exists()) dir.mkdirs();
            File file = null;
            file = new File(dir, name.getPath() + ".json");
            String json = "";
            try
            {
                json = JsonUtil.gson.toJson(h);
                final OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(file),
                        Charset.forName("UTF-8").newEncoder());
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

    private boolean loadTag(final ResourceLocation tagLoc, List<Resource> resources, final String tag,
            final String toCheck)
    {
        try
        {
            final TagHolder tagged = new TagHolder();
            for (final Resource resource : resources)
            {
                final Reader reader = PackFinder.getReader(resource);
                final TagHolder temp = JsonUtil.gson.fromJson(reader, TagHolder.class);
                temp.postProcess();
                if (temp.replace) tagged.values.clear();
                temp.values.forEach(s -> {
                    if (!tagged.values.contains(s)) tagged.values.add(s);
                });
                reader.close();
                // If we were replacing, we want to exit here.
                if (temp.replace) break;
            }
            this.tagsMap.put(tag, tagged);
            // Now we update the reversedTagsMap accordingly
            // Iterate over the values in the tag, and put toCheck in their set.
            for (String s : tagged.values)
            {
                if (!s.contains(":")) s = "pokecube:" + ThutCore.trim(s);
                final Set<String> tags = this.reversedTagsMap.getOrDefault(s, Sets.newHashSet());
                tags.add(tag);
                this.reversedTagsMap.put(s, tags);
            }
            // now just return if it was present.
            return tagged.values.contains(toCheck);
        }
        catch (final FileNotFoundException e)
        {
            PokecubeAPI.LOGGER.debug("No Tag: {}", tagLoc);
        }
        catch (final Exception e)
        {
            PokecubeAPI.LOGGER.error("Error reading tag " + tagLoc, e);
        }
        return false;
    }

    @Override
    public String getKey()
    {
        return tagPath;
    }

}
