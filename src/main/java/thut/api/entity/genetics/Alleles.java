package thut.api.entity.genetics;

import java.util.ArrayList;
import java.util.Random;

import net.minecraft.nbt.CompoundTag;
import thut.core.common.ThutCore;

public class Alleles<T, GENE extends Gene<T>>
{
    final ArrayList<GENE> alleles = new ArrayList<>(2);
    final Random          rand    = ThutCore.newRandom();
    GENE                  expressed;

    public Alleles()
    {
        while (this.alleles.size() < 2)
            this.alleles.add(null);
    }

    public Alleles(final GENE gene1, final GENE gene2)
    {
        this();
        this.alleles.set(0, gene1);
        this.alleles.set(1, gene2);
        if (gene1 == null || gene2 == null) throw new IllegalStateException("Genes cannot be null");
    }

    /**
     * This returns two Allele, one represeting each parent.
     *
     * @return
     */
    public GENE getAllele(final int index)
    {
        return this.alleles.get(index);
    }

    /**
     * This returns two Allele, one represeting each parent.
     *
     * @return
     */
    public void setAllele(final int index, final GENE gene)
    {
        this.alleles.set(index, gene);
    }

    public GENE getExpressed()
    {
        if (this.expressed == null) this.refreshExpressed();
        return this.expressed;
    }

    public void setExpressed(final GENE expressed)
    {
        this.expressed = expressed;
    }

    @SuppressWarnings("unchecked")
    public void load(final CompoundTag tag) throws Exception
    {
        this.expressed = (GENE) GeneRegistry.load(tag.getCompound("expressed"));
        this.alleles.set(0, (GENE) GeneRegistry.load(tag.getCompound("gene1")));
        this.alleles.set(1, (GENE) GeneRegistry.load(tag.getCompound("gene2")));
    }

    @SuppressWarnings("unchecked")
    public void refreshExpressed()
    {
        GENE a = this.alleles.get(0);
        GENE b = this.alleles.get(1);
        if (a == null || b == null) throw new IllegalStateException("Genes cannot be null");
        a = a.getMutationRate() > this.rand.nextFloat() ? (GENE) a.mutate() : a;
        b = b.getMutationRate() > this.rand.nextFloat() ? (GENE) b.mutate() : b;
        this.expressed = (GENE) a.interpolate(b);
    }

    public CompoundTag save()
    {
        final CompoundTag tag = new CompoundTag();
        try
        {
            tag.put("expressed", GeneRegistry.save(this.getExpressed()));
        }
        catch (final Exception e)
        {
            ThutCore.LOGGER.error(this.getExpressed() + " " + this.getExpressed().getKey(), e);
        }
        tag.put("gene1", GeneRegistry.save(this.getAllele(0)));
        tag.put("gene2", GeneRegistry.save(this.getAllele(1)));
        return tag;
    }
}
