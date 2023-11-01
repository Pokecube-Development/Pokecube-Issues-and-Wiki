package thut.core.common.genetics;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.function.Consumer;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceLocation;
import thut.api.entity.genetics.Alleles;
import thut.api.entity.genetics.Gene;
import thut.api.entity.genetics.IMobGenetics;
import thut.core.common.ThutCore;

public class DefaultGenetics implements IMobGenetics
{
    Random rand = ThutCore.newRandom();
    Map<ResourceLocation, Alleles<?, ?>> genetics = Maps.newHashMap();
    Set<Alleles<?, ?>> epigenes;

    public DefaultGenetics()
    {}

    @Override
    public Map<ResourceLocation, Alleles<?, ?>> getAlleles()
    {
        return this.genetics;
    }

    @Override
    public Collection<ResourceLocation> getKeys()
    {
        return this.genetics.keySet();
    }

    private List<Consumer<Gene<?>>> _listeners = new ArrayList<>();

    public void addChangeListener(Consumer<Gene<?>> listener)
    {
        _listeners.add(listener);
    }

    @Override
    public List<Consumer<Gene<?>>> getChangeListeners()
    {
        return _listeners;
    }

    @Override
    public <GENE extends Gene<?>> void setGenes(GENE g1, GENE g2)
    {
        @SuppressWarnings(
        { "rawtypes", "unchecked" })
        var a = new Alleles(g1, g2, this);
        this.genetics.put(g1.getKey(), a);
        // Update the expressed gene after adding it to our map. This notifies
        // gene listeners, and ensures they can look it up from our map.
        a.getExpressed();
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T, GENE extends Gene<T>> Alleles<T, GENE> getAlleles(final ResourceLocation key)
    {
        return (Alleles<T, GENE>) this.genetics.get(key);
    }

    @Override
    public Set<Alleles<?, ?>> getEpigenes()
    {
        if (this.epigenes == null)
        {
            this.epigenes = Sets.newHashSet();
            for (final Alleles<?, ?> a : this.genetics.values())
                if (a.getExpressed().getEpigeneticRate() > 0) this.epigenes.add(a);
        }
        return this.epigenes;
    }

    @Override
    public void setFromParents(final IMobGenetics parent1, final IMobGenetics parent2)
    {
        final Map<ResourceLocation, Alleles<?, ?>> genetics1 = parent1.getAlleles();
        final Map<ResourceLocation, Alleles<?, ?>> genetics2 = parent2.getAlleles();
        for (final Alleles<?, ?> a1 : genetics1.values())
        {
            // Get the key from here.
            @SuppressWarnings("rawtypes")
            Gene gene1 = a1.getExpressed();
            final Alleles<?, ?> a2 = genetics2.get(gene1.getKey());
            if (a2 != null)
            {
                // Get expressed gene for checking epigenetic rate first.
                @SuppressWarnings("rawtypes")
                Gene gene2 = a2.getExpressed();

                // Get the genes based on if epigenes or not.
                gene1 = gene1.getEpigeneticRate() > this.rand.nextFloat() ? gene1 : a1.getAllele(this.rand.nextInt(2));
                gene2 = gene2.getEpigeneticRate() > this.rand.nextFloat() ? gene2 : a2.getAllele(this.rand.nextInt(2));

                // Apply mutations if needed.
                if (gene1.getMutationRate() > this.rand.nextFloat()) gene1 = gene1.mutate(parent1, parent2);
                if (gene2.getMutationRate() > this.rand.nextFloat()) gene2 = gene2.mutate(parent1, parent2);

                // Make the new allele.
                final Alleles<?, ?> allele = new Alleles<>(gene1, gene2, this);
                this.getAlleles().put(gene1.getKey(), allele);
            }
        }
    }

    @Override
    public ListTag serializeNBT()
    {
        final ListTag genes = new ListTag();

        final List<ResourceLocation> keys = Lists.newArrayList(this.getKeys());
        Collections.sort(keys);

        for (final ResourceLocation key : keys)
        {
            final CompoundTag tag = new CompoundTag();
            final Alleles<?, ?> gene = this.getAlleles(key);
            tag.putString("K", key.toString());
            tag.put("V", gene.save());
            genes.add(tag);
        }
        return genes;
    }

    @Override
    public void deserializeNBT(final ListTag list)
    {
        for (int i = 0; i < list.size(); i++)
        {
            final CompoundTag tag = list.getCompound(i);
            final Alleles<?, ?> alleles = new Alleles<>(this);
            final ResourceLocation key = new ResourceLocation(tag.getString("K"));
            try
            {
                // Set in map first, so that it can be checked during load's
                // change listeners.
                this.getAlleles().put(key, alleles);
                alleles.load(tag.getCompound("V"), key);
            }
            catch (final Exception e)
            {
                this.getAlleles().remove(key);
                ThutCore.LOGGER.error("Error loading gene for key: " + key, e);
            }
        }
    }
}
