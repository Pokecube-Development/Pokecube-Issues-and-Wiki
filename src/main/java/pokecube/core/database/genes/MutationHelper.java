package pokecube.core.database.genes;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import pokecube.core.PokecubeCore;
import pokecube.core.database.genes.Mutations.Mutation;
import pokecube.core.database.genes.Mutations.MutationHolder;
import pokecube.core.database.pokedex.PokedexEntryLoader;
import pokecube.core.database.resources.PackFinder;
import pokecube.core.database.util.DataHelpers;
import pokecube.core.database.util.DataHelpers.IResourceData;

public class MutationHelper implements IResourceData
{
    private final String tagPath;

    public boolean validLoad = false;

    Map<ResourceLocation, Map<String, MutationHolder>> mutations = Maps.newHashMap();

    public MutationHelper(final String string)
    {
        this.tagPath = string;
        DataHelpers.addDataType(this);
    }

    public Map<String, MutationHolder> getMutations(final ResourceLocation gene)
    {
        return this.mutations.getOrDefault(gene, Collections.emptyMap());
    }

    @Override
    public void reload(final AtomicBoolean valid)
    {
        this.mutations.clear();
        this.validLoad = false;
        final String path = new ResourceLocation(this.tagPath).getPath();
        final Collection<ResourceLocation> resources = PackFinder.getJsonResources(path);
        this.validLoad = !resources.isEmpty();
        resources.forEach(l -> this.loadFile(l));
        if (this.validLoad) valid.set(true);
    }

    private void loadFile(final ResourceLocation l)
    {
        try
        {
            final List<Mutations> loaded = Lists.newArrayList();
            for (final Resource resource : PackFinder.getResources(l))
            {
                final InputStream res = resource.getInputStream();
                final Reader reader = new InputStreamReader(res);
                try
                {
                    final Mutations temp = PokedexEntryLoader.gson.fromJson(reader, Mutations.class);
                    if (temp.replace) loaded.clear();
                    loaded.add(temp);
                }
                catch (final Exception e)
                {
                    // Might not be valid, so log and skip in that case.
                    PokecubeCore.LOGGER.error("Malformed Json for Mutations in {}", l);
                    PokecubeCore.LOGGER.error(e);
                }
                reader.close();
            }

            for (final Mutations m : loaded)
            {
                final List<MutationHolder> muts = m.mutations;
                for (final MutationHolder mut : muts)
                {
                    Map<String, MutationHolder> mutations = this.mutations.get(new ResourceLocation(mut.geneType));
                    if (mutations == null) this.mutations.put(new ResourceLocation(mut.geneType), mutations = Maps
                            .newHashMap());
                    final String key = mut.toString();
                    if (mutations.containsKey(key))
                    {
                        final boolean replace = mut.replace;
                        if (replace) mutations.put(key, mut);
                        else
                        {
                            final List<Mutation> old = mutations.get(key).options;
                            for (final Mutation opt : mut.options)
                            {
                                if (old.contains(opt)) continue;
                                old.add(opt);
                            }
                        }
                    }
                    else mutations.put(key, mut);
                    mutations.get(key).postProcess();
                }
            }
        }
        catch (final Exception e)
        {
            // Might not be valid, so log and skip in that case.
            PokecubeCore.LOGGER.error("Error with resources in {}", l);
            PokecubeCore.LOGGER.error(e);
        }

    }

}
