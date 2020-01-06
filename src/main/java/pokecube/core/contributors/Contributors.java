package pokecube.core.contributors;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mojang.authlib.GameProfile;

public class Contributors
{
    public List<Contributor> contributors = Lists.newArrayList();

    private final Map<UUID, Contributor>   byUUID = Maps.newHashMap();
    private final Map<String, Contributor> byName = Maps.newHashMap();

    public Contributor getContributor(GameProfile profile)
    {
        if (this.byName.containsKey(profile.getName())) return this.byName.get(profile.getName());
        return this.byUUID.get(profile.getId());
    }

    public void init()
    {
        this.byUUID.clear();
        this.byName.clear();
        for (final Contributor c : this.contributors)
        {
            if (c.uuid != null) this.byUUID.put(c.uuid, c);
            this.byName.put(c.name, c);
        }
        // TODO merge duplicates here somehow?
    }
}
