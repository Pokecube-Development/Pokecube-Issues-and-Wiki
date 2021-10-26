package pokecube.core.entity.pokemobs.genetics.epigenes;

import net.minecraft.util.ResourceLocation;
import pokecube.core.entity.pokemobs.genetics.GeneticsManager;
import thut.api.entity.genetics.Gene;
import thut.core.common.genetics.genes.GeneByteArr;

public class EVsGene extends GeneByteArr
{
    public EVsGene()
    {
        this.value = new byte[6];
        for (int i = 0; i < 6; i++)
            this.value[i] = Byte.MIN_VALUE;
    }

    @Override
    public float getEpigeneticRate()
    {
        return 0.75f;
    }

    @Override
    public ResourceLocation getKey()
    {
        return GeneticsManager.EVSGENE;
    }

    @Override
    public Gene<byte[]> interpolate(final Gene<byte[]> other)
    {
        // Don't actually interpolate the EVs.
        final EVsGene newGene = new EVsGene();
        return newGene;
    }

    @Override
    public Gene<byte[]> mutate()
    {
        final EVsGene newGene = new EVsGene();
        newGene.value = this.value.clone();
        return newGene;
    }

}
