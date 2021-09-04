package pokecube.core.database.util;

import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import com.google.common.collect.Sets;

import pokecube.core.PokecubeCore;

public class DataHelpers
{
    public static interface IResourceData
    {
        void reload(AtomicBoolean valid);

        default void postReload()
        {
        };
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
