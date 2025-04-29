package pokecube.adventures.blocks.genetics.helper;

import java.lang.reflect.Array;
import java.util.Random;

import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.nbt.CompoundTag;
import pokecube.api.PokecubeAPI;
import thut.api.entity.genetics.Alleles;
import thut.api.entity.genetics.Gene;
import thut.api.entity.genetics.GeneRegistry;
import thut.api.entity.genetics.IMobGenetics;
import thut.core.common.ThutCore;

public interface IGeneSelector
{
    public static Gene<?> copy(Provider provider, final Gene<?> geneIn) throws Exception
    {
        final CompoundTag tag = GeneRegistry.save(provider, geneIn);
        return GeneRegistry.load(provider, tag, geneIn.getKey());
    }

    default int arrIndex()
    {
        return -1;
    }

    @SuppressWarnings(
    { "rawtypes", "unchecked" })
    default <T, GENE extends Gene<T>> Alleles<T, GENE> fromGenes(Provider provider, GENE geneSource,
            final GENE geneDest)
    {
        if (this.arrIndex() >= 0) try
        {
            final Object source = geneSource.getValue();
            final Object dest = geneDest.getValue();
            if (source.getClass().isArray())
            {
                final int index = this.arrIndex();
                geneSource = (GENE) IGeneSelector.copy(provider, geneDest);
                Array.set(dest, index, Array.get(source, index));
            }
        }
        catch (final Exception e)
        {
            PokecubeAPI.LOGGER.warn("Error merging genes " + geneSource.getKey() + " " + this.arrIndex(), e);
        }
        return new Alleles(geneSource, geneDest);
    }

    @SuppressWarnings("unchecked")
    default <T, GENE extends Gene<T>> Alleles<T, GENE> merge(Provider provider, final IMobGenetics p1,
            final IMobGenetics p2, final Alleles<T, GENE> source, final Alleles<T, GENE> destination)
    {
        final Random rand = ThutCore.newRandom();
        GENE geneSource = source.getExpressed();
        GENE geneDest = destination.getExpressed();

        // This should, by default, be true, and use a random parent gene from each
        if (geneSource.getEpigeneticRate() < rand.nextFloat()) geneSource = source.getAllele(rand.nextInt(2));
        if (geneDest.getEpigeneticRate() < rand.nextFloat()) geneDest = destination.getAllele(rand.nextInt(2));

        // Apply mutations if needed.
        if (geneSource.getMutationRate() > rand.nextFloat()) geneSource = (GENE) geneSource.mutate(p1, p2);
        if (geneDest.getMutationRate() > rand.nextFloat()) geneDest = (GENE) geneDest.mutate(p1, p2);

        return this.fromGenes(provider, geneSource, geneDest);
    }
}
