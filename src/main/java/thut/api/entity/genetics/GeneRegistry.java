package thut.api.entity.genetics;

import java.util.Collection;
import java.util.Map;

import com.google.common.collect.Maps;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import thut.core.common.ThutCore;

public class GeneRegistry
{
    static Map<ResourceLocation, Class<? extends Gene<?>>> geneMap = Maps.newHashMap();

    public static Class<? extends Gene<?>> getClass(final ResourceLocation location)
    {
        return GeneRegistry.geneMap.get(location);
    }

    public static Collection<Class<? extends Gene<?>>> getGenes()
    {
        return GeneRegistry.geneMap.values();
    }

    public static Gene<?> load(final CompoundTag tag) throws Exception
    {
        Gene<?> ret = null;
        final ResourceLocation resource = new ResourceLocation(tag.getString("K"));
        ret = GeneRegistry.geneMap.get(resource).getConstructor().newInstance();
        ret.load(tag);
        return ret;
    }

    public static void register(final Class<? extends Gene<?>> gene)
    {
        Gene<?> temp;
        try
        {
            // Ensure the gene has a blank constructor for registration
            temp = gene.getConstructor().newInstance();
            GeneRegistry.geneMap.put(temp.getKey(), gene);
        }
        catch (final Exception e)
        {
            ThutCore.LOGGER.error("Error with registry of " + gene, e);
        }
    }

    public static CompoundTag save(final Gene<?> gene)
    {
        final CompoundTag tag = gene.save();
        tag.putString("K", gene.getKey().toString());
        return tag;
    }

}
