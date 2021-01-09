package pokecube.core.database.util;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
        boolean      replace = false;
        List<String> values  = Lists.newArrayList();

        void postProcess()
        {
            this.values.replaceAll(s -> ThutCore.trim(s));
        }
    }

    private static final Set<StringTagsHelper> tagHelpers = Sets.newHashSet();

    public static void onResourcesReloaded()
    {
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
            }
            catch (final Exception e)
            {
                PokecubeCore.LOGGER.error("Error reloading tags for {}", t.tagPath);
                e.printStackTrace();
            }
        });
        PokecubeCore.LOGGER.debug("Reloaded Custom Tags");
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

    public void addToTag(String tag, String value)
    {
        tag = ThutCore.trim(tag);
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
        if (this.tagsMap.containsKey(tag)) return this.tagsMap.get(tag).values.contains(toCheck);
        return false;
    }

    private boolean loadTag(final ResourceLocation tagLoc, final String tag, final String toCheck)
    {
        try
        {
            final TagHolder tagged = new TagHolder();
            for (final IResource resource : Database.resourceManager.getAllResources(tagLoc))
            {
                // Database.resourceManager.getResource(tagLoc)
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
