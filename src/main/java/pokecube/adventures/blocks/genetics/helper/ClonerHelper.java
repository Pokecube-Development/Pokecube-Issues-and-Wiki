package pokecube.adventures.blocks.genetics.helper;

import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import pokecube.adventures.blocks.genetics.helper.recipe.RecipeSelector;
import pokecube.adventures.blocks.genetics.helper.recipe.RecipeSelector.SelectorValue;
import pokecube.api.PokecubeAPI;
import pokecube.api.data.PokedexEntry;
import pokecube.api.events.GeneEditEvent;
import pokecube.api.events.GeneEditEvent.EditType;
import pokecube.api.utils.BookInstructionsParser;
import pokecube.api.utils.TagNames;
import pokecube.core.PokecubeCore;
import pokecube.core.entity.genetics.GeneticsManager;
import pokecube.core.entity.genetics.genes.SpeciesGene;
import pokecube.core.entity.genetics.genes.SpeciesGene.SpeciesInfo;
import pokecube.core.items.pokecubes.PokecubeManager;
import thut.api.entity.genetics.Alleles;
import thut.api.entity.genetics.Gene;
import thut.api.entity.genetics.GeneRegistry;
import thut.api.entity.genetics.IMobGenetics;
import thut.core.common.ThutCore;
import thut.core.common.genetics.DefaultGenetics;

public class ClonerHelper
{
    public static class DNAPack
    {
        public final String id;
        public final Alleles<?, ?> alleles;
        public final float chance;

        public DNAPack(final String id, final Alleles<?, ?> alleles, final float chance)
        {
            this.alleles = alleles;
            this.chance = chance;
            this.id = id;
        }

        @Override
        public String toString()
        {
            return this.id;
        }

        @Override
        public int hashCode()
        {
            return this.id.hashCode();
        }

        @Override
        public boolean equals(final Object obj)
        {
            if (!(obj instanceof DNAPack)) return false;
            return this.toString().equals(obj.toString());
        }
    }

    public static final String SELECTORTAG = "DNASelector";

    public static Map<Ingredient, DNAPack> DNAITEMS = Maps.newHashMap();

    public static PokedexEntry getFromGenes(final ItemStack stack)
    {
        final IMobGenetics genes = ClonerHelper.getGenes(stack);
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
        final ResourceLocation location = new ResourceLocation(domain, path);
        final Class<? extends Gene<?>> geneClass = GeneRegistry.getClass(location);
        return geneClass;
    }

    public static IMobGenetics getGenes(final ItemStack stack)
    {
        return GeneticsManager.getGenes(stack);
    }

    public static Set<Class<? extends Gene<?>>> getGeneSelectors(final ItemStack stack)
    {
        final Set<Class<? extends Gene<?>>> ret = Sets.newHashSet();
        if (stack.isEmpty() || !stack.hasTag()) return ret;
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
                    PokecubeAPI.LOGGER.warn("Error locating selectors for " + stack + " " + stack.getTag(), e);
            }
        }
        return ret;
    }

    public static int getIndex(final ItemStack stack)
    {
        if (stack.isEmpty() || !stack.hasTag()) return -1;
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
        final SelectorValue def = RecipeSelector.getSelectorValue(selector);
        if (selector.isEmpty() || !selector.hasTag()) return def;
        final CompoundTag selectorTag = selector.getTag().getCompound(ClonerHelper.SELECTORTAG);
        return SelectorValue.load(selectorTag);
    }

    private static <T, GENE extends Gene<T>> void merge(final IMobGenetics source, final IMobGenetics destination,
            final IGeneSelector selector, final ResourceLocation loc)
    {
        final Alleles<T, GENE> alleles = source.getAlleles(loc);
        Alleles<T, GENE> eggsAllele = destination.getAlleles(loc);
        eggsAllele = selector.merge(source, destination, alleles, eggsAllele);
        if (eggsAllele != null) destination.getAlleles().put(loc, eggsAllele);
    }

    public static void mergeGenes(final IMobGenetics genesIn, final ItemStack destination, final IGeneSelector selector,
            final boolean force)
    {
        IMobGenetics eggs = ClonerHelper.getGenes(destination);
        if (eggs == null) eggs = new DefaultGenetics();
        for (final ResourceLocation loc : genesIn.getKeys()) ClonerHelper.merge(genesIn, eggs, selector, loc);
        ClonerHelper.setGenes(destination, genesIn, eggs, force ? EditType.OTHER : EditType.EXTRACT);
    }

    public static void registerDNA(final DNAPack entry, final Ingredient stack)
    {
        ClonerHelper.DNAITEMS.put(stack, entry);
    }

    public static void setGenes(final ItemStack stack, final IMobGenetics sourceGenes, final IMobGenetics genes,
            final EditType reason)
    {
        if (stack.isEmpty()) return;
        IMobGenetics destGenes = ClonerHelper.getGenes(stack);
        if (destGenes != null)
        {
            genes.getAlleles().forEach((key, value) -> destGenes.getAlleles().put(key, value));
            return;
        }
        if (!stack.hasTag() && destGenes == null) return;
        ThutCore.FORGE_BUS.post(new GeneEditEvent(sourceGenes, genes, reason));
        final CompoundTag nbt = stack.getTag();
        final Tag geneTag = genes.serializeNBT();
        if (PokecubeManager.isFilled(stack))
        {
            final CompoundTag poketag = nbt.getCompound(TagNames.POKEMOB);
            poketag.getCompound("ForgeCaps").getCompound(GeneticsManager.POKECUBEGENETICS.toString()).put("V", geneTag);
        }
        else nbt.put(GeneticsManager.GENES, geneTag);
    }

    private static <T, GENE extends Gene<T>> void splice(final IMobGenetics source, final IMobGenetics destination,
            final IGeneSelector selector, final ResourceLocation loc)
    {
        Alleles<T, GENE> alleles = source.getAlleles(loc);
        final Alleles<T, GENE> eggsAllele = destination.getAlleles(loc);
        alleles = selector.merge(source, destination, alleles, eggsAllele);
        if (alleles != null)
        {
            final Random rand = ThutCore.newRandom();
            if (alleles.getExpressed().getEpigeneticRate() > rand.nextFloat())
            {
                final GENE gene = alleles.getAllele(rand.nextBoolean() ? 0 : 1);
                alleles.setExpressed(gene);
            }
            destination.getAlleles().put(loc, alleles);
        }
    }

    public static void spliceGenes(final IMobGenetics genesIn, final ItemStack destination,
            final IGeneSelector selector)
    {
        IMobGenetics eggs = ClonerHelper.getGenes(destination);
        if (eggs == null) eggs = new DefaultGenetics();
        ClonerHelper.setGenes(destination, genesIn, genesIn, EditType.EXTRACT);
        for (final ResourceLocation loc : genesIn.getKeys()) ClonerHelper.splice(genesIn, eggs, selector, loc);
        ClonerHelper.setGenes(destination, genesIn, eggs, EditType.SPLICE);
    }
}
