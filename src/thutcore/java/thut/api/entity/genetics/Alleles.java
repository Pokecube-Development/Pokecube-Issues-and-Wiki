package thut.api.entity.genetics;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.function.Consumer;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import thut.core.common.ThutCore;

public class Alleles<T, GENE extends Gene<T>>
{
    final ArrayList<GENE> alleles = new ArrayList<>(2);
    final Random rand = ThutCore.newRandom();
    GENE expressed;

    public Alleles()
    {
        while (this.alleles.size() < 2) this.alleles.add(null);
    }

    public Alleles(final GENE gene1, final GENE gene2)
    {
        this();
        this.alleles.set(0, gene1);
        this.alleles.set(1, gene2);
        if (gene1 == null || gene2 == null) throw new IllegalStateException("Genes cannot be null");
    }

    public Alleles(IMobGenetics holder)
    {
        while (this.alleles.size() < 2) this.alleles.add(null);
        this._listeners = holder.getChangeListeners();
    }

    public Alleles(final GENE gene1, final GENE gene2, IMobGenetics holder)
    {
        this(holder);
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
        if (this.expressed != null) this.onChanged();
    }

    @SuppressWarnings("unchecked")
    public void load(final CompoundTag tag, ResourceLocation key) throws Exception
    {
        this.alleles.set(0, (GENE) GeneRegistry.load(tag.getCompound("gene1"), key));
        this.alleles.set(1, (GENE) GeneRegistry.load(tag.getCompound("gene2"), key));
        if (tag.contains("expressed")) this.setExpressed((GENE) GeneRegistry.load(tag.getCompound("expressed"), key));
    }

    @SuppressWarnings("unchecked")
    public void refreshExpressed()
    {
        GENE a = this.alleles.get(0);
        GENE b = this.alleles.get(1);
        if (a == null || b == null) throw new IllegalStateException("Genes cannot be null");
        a = a.getMutationRate() > this.rand.nextFloat() ? (GENE) a.mutate() : a;
        b = b.getMutationRate() > this.rand.nextFloat() ? (GENE) b.mutate() : b;
        this.setExpressed((GENE) a.interpolate(b));
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

    private List<Consumer<Gene<?>>> _listeners = new ArrayList<>();

    public void setChangeListeners(List<Consumer<Gene<?>>> listeners)
    {
        _listeners = listeners;
    }

    public List<Consumer<Gene<?>>> getChangeListeners()
    {
        return _listeners;
    }

    public void onChanged()
    {
        _listeners.forEach(c -> c.accept(this.expressed));
    }
}
