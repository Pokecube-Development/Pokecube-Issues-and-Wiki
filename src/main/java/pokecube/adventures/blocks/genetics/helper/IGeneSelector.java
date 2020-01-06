package pokecube.adventures.blocks.genetics.helper;

import java.lang.reflect.Array;
import java.util.Random;

import net.minecraft.nbt.CompoundNBT;
import pokecube.core.PokecubeCore;
import thut.api.entity.genetics.Alleles;
import thut.api.entity.genetics.Gene;
import thut.api.entity.genetics.GeneRegistry;

public interface IGeneSelector
{
    public static Gene copy(final Gene geneIn) throws Exception
    {
        final CompoundNBT tag = GeneRegistry.save(geneIn);
        return GeneRegistry.load(tag);
    }

    default int arrIndex()
    {
        return -1;
    }

    default Alleles fromGenes(Gene geneSource, final Gene geneDest)
    {
        if (this.arrIndex() >= 0) try
        {
            final Object source = geneSource.getValue();
            final Object dest = geneDest.getValue();
            if (source.getClass().isArray())
            {
                final int index = this.arrIndex();
                geneSource = IGeneSelector.copy(geneDest);
                Array.set(dest, index, Array.get(source, index));
            }
        }
        catch (final Exception e)
        {
            PokecubeCore.LOGGER.warn("Error merging genes " + geneSource.getKey() + " " + this.arrIndex(), e);
        }
        return new Alleles(geneSource, geneDest);
    }

    default Alleles merge(final Alleles source, final Alleles destination)
    {
        final Random rand = new Random();
        Gene geneSource = source.getExpressed();
        Gene geneDest = destination.getExpressed();
        if (geneSource.getEpigeneticRate() < rand.nextFloat())
        {
            geneSource = source.getAlleles()[rand.nextInt(2)];
            geneDest = destination.getAlleles()[rand.nextInt(2)];
        }
        return this.fromGenes(geneSource, geneDest);
    }
}
