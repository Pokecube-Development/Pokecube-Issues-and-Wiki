package thut.api.entity.genetics;

import com.google.common.collect.Maps;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.attachment.IAttachmentHolder;
import thut.core.common.ThutCore;

import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Predicate;

public class GeneRegistry
{
    public record GeneInit(IMobGenetics genetics, IAttachmentHolder holder, int index) {}

    static Map<ResourceLocation, Class<? extends Gene<?>>> geneMap = Maps.newHashMap();

    public static Class<? extends Gene<?>> getClass(final ResourceLocation location)
    {
        return GeneRegistry.geneMap.get(location);
    }

    public static final Map<Class<? extends Gene<?>>, Predicate<IAttachmentHolder>> DEFAULT_GENES = new HashMap<>();
    public static final Map<Class<? extends Gene<?>>, BiFunction<GeneInit, Gene<?>, Boolean>> DEFAULT_INITS = new HashMap<>();

    public static Collection<Class<? extends Gene<?>>> getGenes()
    {
        return GeneRegistry.geneMap.values();
    }

    public static Gene<?> load(Provider provider, final CompoundTag tag, ResourceLocation key) throws Exception
    {
        Gene<?> ret = GeneRegistry.geneMap.get(key).getConstructor().newInstance();
        ret.load(provider, tag);
        return ret;
    }

    public static void registerGeneInit(Class<? extends Gene<?>> clazz, BiFunction<GeneInit, Gene<?>, Boolean> applier)
    {
        DEFAULT_INITS.compute(clazz, (k, v) -> {
            if (v == null) return applier;
            return (a, b) -> applier.apply(a, b) && v.apply(a, b);
        });
    }

    public static void registerDefaultGene(Predicate<IAttachmentHolder> test, Class<? extends Gene<?>>... genes)
    {
        // Or the predicates together so you can add any list you want.
        for (var gene : genes)
            DEFAULT_GENES.compute(gene, (k, v) -> v == null ? test : v.or(test));
    }

    public static void applyDefaultGenes(IMobGenetics genetics, IAttachmentHolder holder)
    {
        List<Class<? extends Gene<?>>> deferred = new ArrayList<>();
        DEFAULT_GENES.forEach((gene, test) -> {
            Gene<?> temp1, temp2, temp3;
            try
            {
                // Ensure the gene has a blank constructor for registration
                temp1 = gene.getConstructor().newInstance();
                temp2 = gene.getConstructor().newInstance();
                temp3 = gene.getConstructor().newInstance();
                var applier = DEFAULT_INITS.get(gene);
                if (applier != null)
                {
                    var init = new GeneInit(genetics, holder, 0);
                    boolean applied = applier.apply(init, temp1);
                    if (!applied)
                    {
                        deferred.add(gene);
                        return;
                    }
                    init = new GeneInit(genetics, holder, 1);
                    applied = applier.apply(init, temp2);
                    if (!applied)
                    {
                        deferred.add(gene);
                        return;
                    }
                    init = new GeneInit(genetics, holder, 2);
                    applied = applier.apply(init, temp3);
                    if (!applied)
                    {
                        deferred.add(gene);
                        return;
                    }
                }
                genetics.setGenes(temp1, temp2, temp3);
            }
            catch (final Exception e)
            {
                ThutCore.LOGGER.error("Error with gene of {}", gene, e);
            }
        });

        deferred.forEach(gene -> {
            Gene<?> temp1, temp2, temp3;
            try
            {
                // Ensure the gene has a blank constructor for registration
                temp1 = gene.getConstructor().newInstance();
                temp2 = gene.getConstructor().newInstance();
                temp3 = gene.getConstructor().newInstance();
                var applier = DEFAULT_INITS.get(gene);
                var init = new GeneInit(genetics, holder, 0);
                boolean applied = applier.apply(init, temp1);
                if (!applied)
                {
                    throw new RuntimeException("Error processing " + gene + ", it failed twice");
                }
                init = new GeneInit(genetics, holder, 1);
                applied = applier.apply(init, temp2);
                if (!applied)
                {
                    throw new RuntimeException("Error processing " + gene + ", it failed twice");
                }
                init = new GeneInit(genetics, holder, 2);
                applied = applier.apply(init, temp3);
                if (!applied)
                {
                    throw new RuntimeException("Error processing " + gene + ", it failed twice");
                }
                genetics.setGenes(temp1, temp2, temp3);
            }
            catch (final Exception e)
            {
                ThutCore.LOGGER.error("Error with gene of {}", gene, e);
            }
        });
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
            ThutCore.LOGGER.error("Error with registry of {}", gene, e);
        }
    }

    public static CompoundTag save(Provider provider, final Gene<?> gene)
    {
        return gene.save(provider);
    }

}
