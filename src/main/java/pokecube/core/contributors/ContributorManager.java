package pokecube.core.contributors;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.net.UnknownHostException;
import java.util.List;

import com.google.common.collect.Lists;
import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mojang.authlib.GameProfile;

import pokecube.core.PokecubeCore;
import pokecube.core.interfaces.PokecubeMod;

public class ContributorManager
{
    private static final ExclusionStrategy EXCLUDEMAPS;
    static
    {
        EXCLUDEMAPS = new ExclusionStrategy()
        {

            @Override
            public boolean shouldSkipClass(Class<?> clazz)
            {
                return false;
            }

            @Override
            public boolean shouldSkipField(FieldAttributes f)
            {
                return f.getName().equals("byUUID") || f.getName().equals("byName") || f.getName().equals("cubeStack");
            }
        };
    }

    private static final Gson GSON = new GsonBuilder().addDeserializationExclusionStrategy(
            ContributorManager.EXCLUDEMAPS).create();

    private static final String DEFAULTS = PokecubeMod.GIST + "contribs.json";

    private static final ContributorManager INSTANCE = new ContributorManager();

    public static ContributorManager instance()
    {
        return ContributorManager.INSTANCE;
    }

    public Contributors contributors = new Contributors();

    public List<ContributorType> getContributionTypes(GameProfile profile)
    {
        Contributor contrib;
        if ((contrib = this.contributors.getContributor(profile)) != null) return contrib.types;
        return Lists.newArrayList();
    }

    public Contributor getContributor(GameProfile profile)
    {
        return this.contributors.getContributor(profile);
    }

    public void loadContributors()
    {
        this.contributors.contributors.clear();
        if (PokecubeCore.getConfig().default_contributors) this.loadContributors(ContributorManager.DEFAULTS);
        if (!PokecubeCore.getConfig().extra_contributors.isEmpty()) this.loadContributors(PokecubeCore
                .getConfig().extra_contributors);
        this.contributors.init();
    }

    private void loadContributors(String location)
    {
        URL url;
        URLConnection con;
        try
        {
            url = new URL(location);
            con = url.openConnection();
            con.setConnectTimeout(1000);
            con.setReadTimeout(1000);
            final InputStream in = con.getInputStream();
            final InputStreamReader reader = new InputStreamReader(in);
            final Contributors newContribs = ContributorManager.GSON.fromJson(reader, Contributors.class);
            PokecubeCore.LOGGER.debug(newContribs + "");
            if (newContribs != null) this.contributors.contributors.addAll(newContribs.contributors);
        }
        catch (final Exception e)
        {
            if (e instanceof UnknownHostException) PokecubeCore.LOGGER.error("Error loading contributors, unknown host "
                    + location);
            else PokecubeCore.LOGGER.error("Error loading Contributors from " + location, e);
        }
    }
}
