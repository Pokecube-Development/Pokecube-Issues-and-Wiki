package pokecube.core.database.util;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import net.minecraft.util.ResourceLocation;
import pokecube.core.PokecubeCore;
import pokecube.core.database.Database;
import pokecube.core.database.PokedexEntryLoader;
import thut.core.common.ThutCore;

public class StringTagsHelper
{
    public static class TagHolder
    {
        public List<String> values = Lists.newArrayList();

        void postProcess()
        {
            this.values.replaceAll(s -> ThutCore.trim(s));
        }
    }

    private static final Set<StringTagsHelper> tagHelpers = Sets.newHashSet();

    public static void onResourcesReloaded()
    {
        StringTagsHelper.tagHelpers.forEach(t ->
        {        // Clear these, they will be reloaded when used.
            t.tagsMap.clear();
            t.notTags.clear();
        });
    }

    private final Map<String, TagHolder> tagsMap = Maps.newHashMap();

    private final Set<String> notTags = Sets.newHashSet();

    private final String tagPath;

    public StringTagsHelper(final String path)
    {
        this.tagPath = path;
        StringTagsHelper.tagHelpers.add(this);
    }

    public boolean isIn(String tag, String move)
    {
        tag = ThutCore.trim(tag);
        move = ThutCore.trim(move);
        // If we have the tag loaded, lets use the value from there.
        if (this.tagsMap.containsKey(tag)) return this.tagsMap.get(tag).values.contains(move);

        // This tag was not found, it was logged then, so we just exit here.
        if (this.notTags.contains(tag)) return false;

        // Otherwise, try to load the tag in.
        final ResourceLocation tagLoc = new ResourceLocation(this.tagPath + tag + ".json");
        try
        {
            final InputStream res = Database.resourceManager.getResource(tagLoc).getInputStream();
            final Reader reader = new InputStreamReader(res);
            final TagHolder tagged = PokedexEntryLoader.gson.fromJson(reader, TagHolder.class);
            tagged.postProcess();
            this.tagsMap.put(tag, tagged);
            reader.close();
            return tagged.values.contains(move);
        }
        catch (final FileNotFoundException e)
        {
            PokecubeCore.LOGGER.debug("No Tag: {}", tagLoc);
            this.notTags.add(tag);
        }
        catch (final Exception e)
        {
            PokecubeCore.LOGGER.error("Error reading tag " + tagLoc, e);
            this.notTags.add(tag);
        }
        return false;
    }

}
