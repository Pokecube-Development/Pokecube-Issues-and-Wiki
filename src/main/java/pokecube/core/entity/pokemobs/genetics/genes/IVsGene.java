package pokecube.core.entity.pokemobs.genetics.genes;

import java.util.Random;

import net.minecraft.util.ResourceLocation;
import pokecube.core.PokecubeCore;
import pokecube.core.entity.pokemobs.genetics.GeneticsManager;
import pokecube.core.entity.pokemobs.genetics.epigenes.EVsGene;
import pokecube.core.utils.Tools;
import thut.api.entity.genetics.Alleles;
import thut.api.entity.genetics.Gene;
import thut.api.entity.genetics.IMobGenetics;
import thut.core.common.genetics.genes.GeneByteArr;

public class IVsGene extends GeneByteArr
{
    public IVsGene()
    {
        final Random rand = new Random();
        this.value = new byte[] { Tools.getRandomIV(rand), Tools.getRandomIV(rand), Tools.getRandomIV(rand), Tools
                .getRandomIV(rand), Tools.getRandomIV(rand), Tools.getRandomIV(rand) };
    }

    @Override
    public ResourceLocation getKey()
    {
        return GeneticsManager.IVSGENE;
    }

    @Override
    public float getMutationRate()
    {
        return GeneticsManager.mutationRates.get(this.getKey());
    }

    @Override
    public Gene interpolate(Gene other)
    {
        final IVsGene newGene = new IVsGene();
        final byte[] ret = newGene.value;
        final IVsGene otherI = (IVsGene) other;
        for (int i = 0; i < 6; i++)
        {
            final byte mi = this.value[i];
            final byte fi = otherI.value[i];
            final byte iv = (byte) ((mi + fi) / 2);
            ret[i] = iv;
        }
        return newGene;
    }

    @Override
    public Gene mutate()
    {
        final IVsGene newGene = new IVsGene();
        newGene.value = this.value.clone();
        final byte[] ret = newGene.value;
        final Random rand = new Random();
        final float chance = GeneticsManager.mutationRates.get(this.getKey());
        for (int i = 0; i < 6; i++)
        {
            if (rand.nextFloat() > chance) continue;
            final byte mi = (byte) rand.nextInt(this.value[i] + 1);
            final byte fi = (byte) rand.nextInt(this.value[i] + 1);
            final byte iv = (byte) Math.min(mi + fi, 31);
            ret[i] = iv;
        }
        return newGene;
    }

    @Override
    public Gene mutate(IMobGenetics parent1, IMobGenetics parent2)
    {
        final Alleles evs1 = parent1.getAlleles().get(GeneticsManager.EVSGENE);
        final Alleles evs2 = parent2.getAlleles().get(GeneticsManager.EVSGENE);
        final Alleles ivs1 = parent1.getAlleles().get(GeneticsManager.IVSGENE);
        final Alleles ivs2 = parent2.getAlleles().get(GeneticsManager.IVSGENE);
        final IVsGene newGene = new IVsGene();
        newGene.value = this.value.clone();
        if (evs1 == null || evs2 == null || ivs1 == null || ivs2 == null)
        {
            // No Mutation, return clone of this gene.
            PokecubeCore.LOGGER.error("Someone has null genes: " + evs1 + " " + evs2 + " " + ivs1 + " " + ivs2 + " "
                    + parent1 + " " + parent2);
            return newGene;
        }
        final Random rand = new Random();
        final EVsGene gene1 = evs1.getExpressed();
        final EVsGene gene2 = evs2.getExpressed();
        final byte[] ret = newGene.value;
        final byte[] ev1 = gene1.getValue();
        final byte[] ev2 = gene2.getValue();
        final byte[] iv1 = ivs1.getExpressed().getEpigeneticRate() > rand.nextFloat() ? ivs1.getExpressed().getValue()
                : ivs1.getAlleles()[rand.nextInt(2)].getValue();
        final byte[] iv2 = ivs2.getExpressed().getEpigeneticRate() > rand.nextFloat() ? ivs2.getExpressed().getValue()
                : ivs2.getAlleles()[rand.nextInt(2)].getValue();
        for (int i = 0; i < 6; i++)
        {
            final int v = (ev1[i] + ev2[2]) / 2;
            GeneticsManager.epigeneticParser.setVarValue("v", v);
            final byte mi = (byte) rand.nextInt((int) (iv1[i] + 1 + GeneticsManager.epigeneticParser.getValue()));
            final byte fi = (byte) rand.nextInt((int) (iv2[i] + 1 + GeneticsManager.epigeneticParser.getValue()));
            final byte iv = (byte) Math.min(mi + fi, 31);
            ret[i] = iv;
        }
        return newGene;
    }

}
