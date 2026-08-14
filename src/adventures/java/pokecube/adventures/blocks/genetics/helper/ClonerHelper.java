package pokecube.adventures.blocks.genetics.helper;

import java.util.List;
import java.util.Random;
import java.util.Set;

import com.google.common.collect.Sets;

import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import pokecube.adventures.blocks.genetics.helper.SelectorImpl.SelectorValue;
import pokecube.api.PokecubeAPI;
import pokecube.api.data.PokedexEntry;
import pokecube.api.events.GeneEditEvent;
import pokecube.api.events.GeneEditEvent.EditType;
import pokecube.api.utils.BookInstructionsParser;
import pokecube.core.PokecubeCore;
import pokecube.core.entity.genetics.GeneticsManager;
import pokecube.core.entity.genetics.genes.SpeciesGene;
import pokecube.core.entity.genetics.genes.SpeciesGene.SpeciesInfo;
import thut.api.entity.genetics.Alleles;
import thut.api.entity.genetics.Gene;
import thut.api.entity.genetics.GeneHolder;
import thut.api.entity.genetics.GeneRegistry;
import thut.api.entity.genetics.IMobGenetics;
import thut.core.common.ThutCore;
import thut.core.common.genetics.DefaultGenetics;

public class ClonerHelper
{
    public static PokedexEntry getFromGenes(Provider provider, final ItemStack stack)
    {
        final IMobGenetics genes = ClonerHelper.getGenes(provider, stack);
        if (genes == null) return null;
        final Alleles<SpeciesInfo, SpeciesGene> gene = genes.getAlleles(GeneticsManager.SPECIESGENE);
        if (gene != null)
        {
            final SpeciesGene sgene = gene.getExpressed();
            final SpeciesInfo info = sgene.getValue();
            return info.getEntry();
        }
        return null;
    }

    public static Class<? extends Gene<?>> getGene(final String line)
    {
        final String[] args = line.split(":");
        String domain = "pokecube";
        String path = "";
        if (args.length == 2)
        {
            domain = args[0];
            path = args[1];
        }
        else path = args[0];
        path = path.split("#")[0];
        path = ThutCore.trim(path);
        final ResourceLocation location = ResourceLocation.fromNamespaceAndPath(domain, path);
        final Class<? extends Gene<?>> geneClass = GeneRegistry.getClass(location);
        return geneClass;
    }

    public static IMobGenetics getGenes(Provider provider, final ItemStack stack)
    {
        return GeneticsManager.getGenes(stack, provider);
    }

    public static Set<Class<? extends Gene<?>>> getGeneSelectors(Provider provider, final ItemStack stack)
    {
        final Set<Class<? extends Gene<?>>> ret = Sets.newHashSet();
        List<String> instructions = BookInstructionsParser.getInstructions(stack, "genes", false);
        for (String line : instructions)
        {
            if (line.equalsIgnoreCase("ALL"))
            {
                ret.addAll(GeneRegistry.getGenes());
                break;
            }
            try
            {
                final Class<? extends Gene<?>> geneClass = ClonerHelper.getGene(line);
                if (geneClass != null) ret.add(geneClass);
            }
            catch (Exception e)
            {
                if (PokecubeCore.getConfig().debug_misc)
                    PokecubeAPI.LOGGER.warn("Error locating selectors for {}", stack, e);
            }
        }
        return ret;
    }

    public static int getIndex(final ItemStack stack)
    {
        List<String> instructions = BookInstructionsParser.getInstructions(stack, "genes", false);
        for (String line : instructions)
        {
            if (line.equalsIgnoreCase("ALL")) return -1;
            final String[] args = line.split("#");
            if (args.length == 2) return Integer.parseInt(args[1]);
        }
        return -1;
    }

    public static SelectorValue getSelectorValue(final ItemStack selector)
    {
        return selector.has(SelectorImpl.VALUE_STORE)
                ? selector.get(SelectorImpl.VALUE_STORE)
                : SelectorImpl.defaultSelector;
    }

    private static <T, GENE extends Gene<T>> void merge(Provider provider, final IMobGenetics source,
            final IMobGenetics destination, final IGeneSelector selector, final ResourceLocation loc)
    {
        final Alleles<T, GENE> alleles = source.getAlleles(loc);
        Alleles<T, GENE> eggsAllele = destination.getAlleles(loc);
        eggsAllele = selector.merge(provider, source, destination, alleles, eggsAllele);
        if (eggsAllele != null)
            destination.setGenes(eggsAllele.getAllele(0), eggsAllele.getAllele(1), eggsAllele.getExpressed());
    }

    public static void mergeGenes(Provider provider, final IMobGenetics genesIn, final ItemStack destination,
            final IGeneSelector selector, final boolean force)
    {
        IMobGenetics eggs = ClonerHelper.getGenes(provider, destination);
        if (eggs == null) eggs = new DefaultGenetics();
        for (final ResourceLocation loc : genesIn.getKeys()) ClonerHelper.merge(provider, genesIn, eggs, selector, loc);
        ClonerHelper.setGenes(provider, destination, genesIn, eggs, force ? EditType.OTHER : EditType.EXTRACT);
    }

    public static void setGenes(Provider provider, final ItemStack stack, final IMobGenetics sourceGenes,
            final IMobGenetics genes, final EditType reason)
    {
        if (stack.isEmpty()) return;
        IMobGenetics destGenes = ClonerHelper.getGenes(provider, stack);
        if (destGenes != null)
        {
            genes.getAlleles().forEach((key, value) -> destGenes.getAlleles().put(key, value));
            stack.set(DefaultGenetics.GENE_STORE, new GeneHolder(genes, provider));
            return;
        }
        ThutCore.FORGE_BUS.post(new GeneEditEvent(sourceGenes, genes, reason));
        stack.set(DefaultGenetics.GENE_STORE, new GeneHolder(genes, provider));
    }

    private static <T, GENE extends Gene<T>> void splice(Provider provider, final IMobGenetics source,
            final IMobGenetics destination, final IGeneSelector selector, final ResourceLocation loc)
    {
        Alleles<T, GENE> alleles = source.getAlleles(loc);
        final Alleles<T, GENE> eggsAllele = destination.getAlleles(loc);
        alleles = selector.merge(provider, source, destination, alleles, eggsAllele);
        if (alleles != null)
        {
            final Random rand = ThutCore.newRandom();
            if (alleles.getExpressed().getEpigeneticRate() > rand.nextFloat())
            {
                final GENE gene = alleles.getAllele(rand.nextBoolean() ? 0 : 1);
                alleles.setExpressed(gene);
            }
            destination.setGenes(alleles.getAllele(0), alleles.getAllele(1), alleles.getExpressed());
        }
    }

    public static void spliceGenes(Provider provider, final IMobGenetics genesIn, final ItemStack destination,
            final IGeneSelector selector)
    {
        IMobGenetics eggs = ClonerHelper.getGenes(provider, destination);
        if (eggs == null) eggs = new DefaultGenetics();
        ClonerHelper.setGenes(provider, destination, genesIn, genesIn, EditType.EXTRACT);
        for (final ResourceLocation loc : genesIn.getKeys())
            ClonerHelper.splice(provider, genesIn, eggs, selector, loc);
        ClonerHelper.setGenes(provider, destination, genesIn, eggs, EditType.SPLICE);
    }
}
