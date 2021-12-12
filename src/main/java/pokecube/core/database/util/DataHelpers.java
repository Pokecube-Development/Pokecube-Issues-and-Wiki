package pokecube.core.database.util;

import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import com.google.common.collect.Sets;
import com.google.common.hash.Hashing;

import net.minecraft.resources.ResourceLocation;
import pokecube.core.PokecubeCore;
import pokecube.core.database.pokedex.PokedexEntryLoader;

public class DataHelpers
{
    public static interface IResourceData
    {
        void reload(AtomicBoolean valid);

        default void postReload()
        {};
    }

    public static abstract class ResourceData implements IResourceData
    {
        final Set<String> md5s = Sets.newHashSet();

        protected void preLoad()
        {
            md5s.clear();
        }

        protected boolean confirmNew(Object obj, ResourceLocation l)
        {
            String ret = PokedexEntryLoader.gson.toJson(obj);
            if (!md5s.add(Hashing.goodFastHash(64).hashUnencodedChars(ret).padToLong() + ""))
            {
                PokecubeCore.LOGGER.warn("Warning, tried loading identical file for {}, skipping the copy.", l);
                return false;
            }
            return true;
        }

    }

    private static final Set<IResourceData> tagHelpers = Sets.newHashSet();

    public static void onResourcesReloaded()
    {
        final AtomicBoolean valid = new AtomicBoolean(false);
        DataHelpers.tagHelpers.forEach(t -> t.reload(valid));
        if (valid.get())
        {
            DataHelpers.tagHelpers.forEach(t -> t.postReload());
            PokecubeCore.LOGGER.debug("Reloaded Custom Tags");
        }
    }

    public static void addDataType(final IResourceData type)
    {
        DataHelpers.tagHelpers.add(type);
    }
}
