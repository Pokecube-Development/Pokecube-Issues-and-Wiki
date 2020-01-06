package thut.api.entity.genetics;

import java.util.Random;

import net.minecraft.nbt.CompoundNBT;

public class Alleles
{
    final Gene[] alleles = new Gene[2];
    final Random rand    = new Random();
    Gene         expressed;

    public Alleles()
    {
    }

    public Alleles(Gene gene1, Gene gene2)
    {
        this.alleles[0] = gene1;
        this.alleles[1] = gene2;
        if (gene1 == null || gene2 == null) throw new IllegalStateException("Genes cannot be null");
    }

    /**
     * This returns two Allele, one represeting each parent.
     *
     * @return
     */
    public Gene[] getAlleles()
    {
        return this.alleles;
    }

    @SuppressWarnings("unchecked")
    public <T extends Gene> T getExpressed()
    {
        if (this.expressed == null) this.refreshExpressed();
        return (T) this.expressed;
    }

    public void load(CompoundNBT tag) throws Exception
    {
        this.expressed = GeneRegistry.load(tag.getCompound("expressed"));
        this.getAlleles()[0] = GeneRegistry.load(tag.getCompound("gene1"));
        this.getAlleles()[1] = GeneRegistry.load(tag.getCompound("gene2"));
    }

    public void refreshExpressed()
    {
        if (this.alleles[0] == null || this.alleles[1] == null) throw new IllegalStateException("Genes cannot be null");
        final Gene a = this.alleles[0].getMutationRate() > this.rand.nextFloat() ? this.alleles[0].mutate()
                : this.alleles[0];
        final Gene b = this.alleles[1].getMutationRate() > this.rand.nextFloat() ? this.alleles[1].mutate()
                : this.alleles[1];
        this.expressed = a.interpolate(b);
    }

    public CompoundNBT save()
    {
        final CompoundNBT tag = new CompoundNBT();
        tag.put("expressed", GeneRegistry.save(this.getExpressed()));
        tag.put("gene1", GeneRegistry.save(this.getAlleles()[0]));
        tag.put("gene2", GeneRegistry.save(this.getAlleles()[1]));
        return tag;
    }
}
