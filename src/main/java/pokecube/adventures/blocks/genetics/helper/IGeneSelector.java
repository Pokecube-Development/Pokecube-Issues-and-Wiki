package pokecube.adventures.blocks.genetics.helper;

import java.lang.reflect.Array;
import java.util.Random;

import net.minecraft.nbt.CompoundNBT;
import pokecube.core.PokecubeCore;
import thut.api.entity.genetics.Alleles;
import thut.api.entity.genetics.Gene;
import thut.api.entity.genetics.GeneRegistry;
import thut.api.entity.genetics.IMobGenetics;
import thut.core.common.ThutCore;

public interface IGeneSelector
{
    public static Gene<?> copy(final Gene<?> geneIn) throws Exception
    {
        final CompoundNBT tag = GeneRegistry.save(geneIn);
        return GeneRegistry.load(tag);
    }

    default int arrIndex()
    {
        return -1;
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    default <T, GENE extends Gene<T>> Alleles<T, GENE> fromGenes(GENE geneSource, final GENE geneDest)
    {
        if (this.arrIndex() >= 0) try
        {
            final Object source = geneSource.getValue();
            final Object dest = geneDest.getValue();
            if (source.getClass().isArray())
            {
                final int index = this.arrIndex();
                geneSource = (GENE) IGeneSelector.copy(geneDest);
                Array.set(dest, index, Array.get(source, index));
            }
        }
        catch (final Exception e)
        {
            PokecubeCore.LOGGER.warn("Error merging genes " + geneSource.getKey() + " " + this.arrIndex(), e);
        }
        return new Alleles(geneSource, geneDest);
    }

    @SuppressWarnings("unchecked")
    default <T, GENE extends Gene<T>> Alleles<T, GENE> merge(final IMobGenetics p1, final IMobGenetics p2,
            final Alleles<T, GENE> source, final Alleles<T, GENE> destination)
    {
        final Random rand = ThutCore.newRandom();
        GENE geneSource = source.getExpressed();
        GENE geneDest = destination.getExpressed();

        // Applie epigenetics if needed
        if (geneSource.getEpigeneticRate() < rand.nextFloat()) geneSource = source.getAllele(rand.nextInt(2));
        if (geneDest.getEpigeneticRate() < rand.nextFloat()) geneDest = destination.getAllele(rand.nextInt(2));

        // Apply mutations if needed.
        if (geneSource.getMutationRate() > rand.nextFloat()) geneSource = (GENE) geneSource.mutate(p1, p2);
        if (geneDest.getMutationRate() > rand.nextFloat()) geneDest = (GENE) geneDest.mutate(p1, p2);

        return this.fromGenes(geneSource, geneDest);
    }
}
