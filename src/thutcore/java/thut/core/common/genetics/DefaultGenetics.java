package thut.core.common.genetics;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.attachment.IAttachmentHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import thut.api.entity.genetics.Alleles;
import thut.api.entity.genetics.Gene;
import thut.api.entity.genetics.GeneHolder;
import thut.api.entity.genetics.GeneRegistry;
import thut.api.entity.genetics.IMobGenetics;
import thut.core.common.ThutCore;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Supplier;

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

    private final List<Consumer<Gene<?>>> _listeners = new ArrayList<>();

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
        @SuppressWarnings({ "rawtypes", "unchecked" })
        var a = new Alleles(g1, g2, this);
        this.genetics.put(g1.getKey(), a);
        // Update the expressed gene after adding it to our map. This notifies
        // gene listeners, and ensures they can look it up from our map.
        a.getExpressed();
        a.onChanged();
        markDirty();
    }

    @Override
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public <GENE extends Gene<?>> void setGenes(GENE g1, GENE g2, GENE gexp)
    {
        var a = new Alleles(g1, g2, this);
        a.setExpressed(gexp);
        this.genetics.put(g1.getKey(), a);
        a.onChanged();
        markDirty();
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
                this.setGenes(gene1, gene2);
            }
        }
    }

    @Override
    public ListTag serializeNBT(HolderLookup.Provider provider)
    {
        final ListTag genes = new ListTag();

        final List<ResourceLocation> keys = Lists.newArrayList(this.getKeys());
        Collections.sort(keys);

        for (final ResourceLocation key : keys)
        {
            final CompoundTag tag = new CompoundTag();
            final Alleles<?, ?> gene = this.getAlleles(key);
            tag.putString("K", key.toString());
            tag.put("V", gene.save(provider));
            genes.add(tag);
        }
        return genes;
    }

    @Override
    public void deserializeNBT(HolderLookup.Provider provider, final ListTag list)
    {
        for (int i = 0; i < list.size(); i++)
        {
            final CompoundTag tag = list.getCompound(i);
            final Alleles<?, ?> alleles = new Alleles<>();
            final ResourceLocation key = ResourceLocation.parse(tag.getString("K"));
            try
            {
                alleles.load(provider, tag.getCompound("V"), key);
                this.setGenes(alleles.getAllele(0), alleles.getAllele(1), alleles.getExpressed());
            }
            catch (final Exception e)
            {
                this.getAlleles().remove(key);
                ThutCore.LOGGER.error("Error loading gene for key: {}", key, e);
            }
        }
    }

    public static IMobGenetics makeProvider(final IAttachmentHolder in)
    {
        var genes = new DefaultGenetics();
        GeneRegistry.applyDefaultGenes(genes, in);
        return genes;
    }

    public static IMobGenetics get(final IAttachmentHolder in)
    {
        return in.getData(TYPE.get());
    }

    public static final ResourceLocation KEY = ResourceLocation.parse("thutcore:genetics");

    public static Supplier<AttachmentType<IMobGenetics>> TYPE;

    public static void registerAttachment(DeferredRegister<AttachmentType<?>> registry)
    {
        TYPE = registry.register(KEY.getPath(),
                () -> AttachmentType.serializable(DefaultGenetics::makeProvider).copyOnDeath().build());
    }

    public static Supplier<DataComponentType<GeneHolder>> GENE_STORE;

    public static void registerItemData(DeferredRegister<DataComponentType<?>> registry)
    {
        GENE_STORE = registry.register("gene_storage",
                name -> new DataComponentType.Builder<GeneHolder>().persistent(GeneHolder.CODEC)
                        .networkSynchronized(GeneHolder.STREAM_CODEC).build());
    }

    private boolean isDirty = false;

    @Override
    public void markDirty()
    {
        this.isDirty = true;
    }

    @Override
    public void markClean()
    {
        this.isDirty = false;
    }

    @Override
    public boolean isDirty()
    {
        return isDirty;
    }
}
