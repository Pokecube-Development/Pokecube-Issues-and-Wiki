package pokecube.core.entity.pokemobs.genetics.genes;

import java.util.Random;

import net.minecraft.util.ResourceLocation;
import pokecube.core.entity.pokemobs.genetics.GeneticsManager;
import thut.api.entity.genetics.Gene;
import thut.core.common.ThutCore;
import thut.core.common.genetics.genes.GeneFloat;

public class SizeGene extends GeneFloat
{
    public static float scaleFactor = 0.075f;
    Random              rand        = ThutCore.newRandom();

    public SizeGene()
    {
        this.value = 1f;
    }

    @Override
    public ResourceLocation getKey()
    {
        return GeneticsManager.SIZEGENE;
    }

    @Override
    public float getMutationRate()
    {
        return GeneticsManager.mutationRates.get(this.getKey());
    }

    @Override
    public Gene<Float> interpolate(final Gene<Float> other)
    {
        final SizeGene newGene = new SizeGene();
        final SizeGene otherG = (SizeGene) other;
        newGene.value = this.rand.nextBoolean() ? otherG.value : this.value;
        return newGene;
    }

    @Override
    public Gene<Float> mutate()
    {
        final SizeGene newGene = new SizeGene();
        final float factor = SizeGene.scaleFactor * (this.value > 1 ? 1 / this.value : this.value);
        newGene.value = this.value + factor * (float) ThutCore.newRandom().nextGaussian();
        newGene.value = Math.abs(newGene.value);
        return newGene;
    }

}
