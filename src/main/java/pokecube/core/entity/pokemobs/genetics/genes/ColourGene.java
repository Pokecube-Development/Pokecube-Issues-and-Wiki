package pokecube.core.entity.pokemobs.genetics.genes;

import java.util.Random;

import net.minecraft.util.ResourceLocation;
import pokecube.core.entity.pokemobs.genetics.GeneticsManager;
import thut.api.entity.genetics.Gene;
import thut.core.common.ThutCore;
import thut.core.common.genetics.genes.GeneIntArray;

public class ColourGene extends GeneIntArray
{
    /**
     * The higher this value, the more likely for mobs to range in colour. It
     * is very sensitive to the size of this number.
     */
    private static final double colourDiffFactor = 0.25;

    public ColourGene()
    {
        this.value = new int[] { 127, 127, 127, 255 };
        this.setRandomColour();
    }

    @Override
    public ResourceLocation getKey()
    {
        return GeneticsManager.COLOURGENE;
    }

    @Override
    public float getMutationRate()
    {
        return GeneticsManager.mutationRates.get(this.getKey());
    }

    @Override
    public Gene<int[]> interpolate(final Gene<int[]> other)
    {
        final ColourGene otherC = (ColourGene) other;
        final ColourGene newGene = new ColourGene();
        final int[] ret = newGene.value;
        for (int i = 0; i < this.value.length; i++)
            ret[i] = (this.value[i] + otherC.value[i]) / 2;
        return newGene;
    }

    @Override
    public Gene<int[]> mutate()
    {
        final ColourGene mutate = new ColourGene();
        mutate.setRandomColour();
        return mutate;
    }

    void setRandomColour()
    {
        final Random r = ThutCore.newRandom();
        final int first = r.nextInt(3);
        byte red = 127, green = 127, blue = 127;

        final double shift = 4;

        final double dr = Math.min((shift - Math.abs(ColourGene.colourDiffFactor * r.nextGaussian())) * 32, 127);
        final double dg = Math.min((shift - Math.abs(ColourGene.colourDiffFactor * r.nextGaussian())) * 32, 127);
        final double db = Math.min((shift - Math.abs(ColourGene.colourDiffFactor * r.nextGaussian())) * 32, 127);

        if (first == 0)
        {
            int min = 0;
            red = (byte) Math.max(dr, min);
            min = red < 63 ? 63 : 0;

            green = (byte) Math.max(dg, min);
            min = green < 63 ? 63 : 0;

            blue = (byte) Math.max(db, min);
        }
        if (first == 1)
        {
            int min = 0;

            green = (byte) Math.max(dg, min);
            min = green < 63 ? 63 : 0;

            red = (byte) Math.max(dr, min);
            min = red < 63 ? 63 : 0;

            blue = (byte) Math.max(db, min);
        }
        if (first == 2)
        {
            int min = 0;
            blue = (byte) Math.max(db, min);
            min = blue < 63 ? 63 : 0;

            red = (byte) Math.max(dr, min);
            min = red < 63 ? 63 : 0;

            green = (byte) Math.max(dg, min);
        }
        this.value[0] = red + 128;
        this.value[1] = green + 128;
        this.value[2] = blue + 128;
    }
}
