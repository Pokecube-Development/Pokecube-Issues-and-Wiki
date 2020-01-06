package thut.api.entity.genetics;

import java.util.Collection;
import java.util.Map;

import com.google.common.collect.Maps;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import thut.core.common.ThutCore;

public class GeneRegistry
{
    @CapabilityInject(IMobGenetics.class)
    public static final Capability<IMobGenetics> GENETICS_CAP = null;

    static Map<ResourceLocation, Class<? extends Gene>> geneMap = Maps.newHashMap();

    public static Class<? extends Gene> getClass(ResourceLocation location)
    {
        return GeneRegistry.geneMap.get(location);
    }

    public static Collection<Class<? extends Gene>> getGenes()
    {
        return GeneRegistry.geneMap.values();
    }

    public static Gene load(CompoundNBT tag) throws Exception
    {
        Gene ret = null;
        final ResourceLocation resource = new ResourceLocation(tag.getString("K"));
        ret = GeneRegistry.geneMap.get(resource).newInstance();
        ret.load(tag);
        return ret;
    }

    public static void register(Class<? extends Gene> gene)
    {
        Gene temp;
        try
        {
            // Ensure the gene has a blank constructor for registration
            temp = gene.newInstance();
            GeneRegistry.geneMap.put(temp.getKey(), gene);
        }
        catch (InstantiationException | IllegalAccessException e)
        {
            ThutCore.LOGGER.error("Error with registry of " + gene, e);
        }
    }

    public static CompoundNBT save(Gene gene)
    {
        final CompoundNBT tag = gene.save();
        tag.putString("K", gene.getKey().toString());
        return tag;
    }

}
