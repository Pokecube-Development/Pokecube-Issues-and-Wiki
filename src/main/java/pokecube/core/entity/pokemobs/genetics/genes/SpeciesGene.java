package pokecube.core.entity.pokemobs.genetics.genes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Random;

import javax.annotation.Nullable;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import pokecube.core.database.Database;
import pokecube.core.database.PokedexEntry;
import pokecube.core.database.genes.Mutations.Mutation;
import pokecube.core.database.genes.Mutations.MutationHolder;
import pokecube.core.database.tags.Tags;
import pokecube.core.entity.pokemobs.genetics.GeneticsManager;
import pokecube.core.entity.pokemobs.genetics.genes.SpeciesGene.SpeciesInfo;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.IPokemob.FormeHolder;
import thut.api.entity.genetics.Gene;
import thut.core.common.ThutCore;

public class SpeciesGene implements Gene<SpeciesInfo>
{
    public static class SpeciesInfo
    {
        public byte value;
        public PokedexEntry entry;
        @Nullable
        public FormeHolder forme;

        @Override
        public SpeciesInfo clone()
        {
            final SpeciesInfo info = new SpeciesInfo();
            info.value = this.value;
            info.entry = this.entry;
            info.forme = this.forme;
            return info;
        }

        @Override
        public boolean equals(final Object obj)
        {
            if (!(obj instanceof SpeciesInfo)) return false;
            final SpeciesInfo info = (SpeciesInfo) obj;
            return this.value == info.value && (this.entry == null ? true : this.entry.equals(info.entry));
        }

        void load(final CompoundTag tag)
        {
            this.value = tag.getByte("G");
            this.entry = Database.getEntry(tag.getString("E"));
            if (tag.contains("F")) forme = FormeHolder.load(tag.getCompound("F"));
        }

        CompoundTag save()
        {
            final CompoundTag tag = new CompoundTag();
            tag.putByte("G", this.value);
            if (this.entry != null) tag.putString("E", this.entry.getName());
            if (forme != null) tag.put("F", forme.save());
            return tag;
        }

        @Override
        public String toString()
        {
            return this.entry + " " + this.value;
        }
    }

    public static byte getSexe(final int baseValue, final Random random)
    {
        if (baseValue == 255) return IPokemob.NOSEXE;
        if (random.nextInt(255) >= baseValue) return IPokemob.MALE;
        return IPokemob.FEMALE;
    }

    SpeciesInfo info = new SpeciesInfo();

    Random rand = ThutCore.newRandom();

    /** The value here is of format {gender, ratio}. */
    public SpeciesGene()
    {
        this.info.value = 0;
    }

    @Override
    public float getEpigeneticRate()
    {
        return GeneticsManager.mutationRates.get(this.getKey());
    }

    @Override
    public ResourceLocation getKey()
    {
        return GeneticsManager.SPECIESGENE;
    }

    @Override
    public float getMutationRate()
    {
        return 1;
    }

    @Override
    public SpeciesInfo getValue()
    {
        return this.info;
    }

    @Override
    public Gene<SpeciesInfo> interpolate(final Gene<SpeciesInfo> other)
    {
        final SpeciesGene newGene = new SpeciesGene();
        final SpeciesGene otherG = (SpeciesGene) other;
        SpeciesGene mother = this.info.value == IPokemob.FEMALE ? this : this.info.value > 0 ? this : otherG;
        if (this.info.value == otherG.info.value) mother = this.rand.nextFloat() < 0.5 ? this : otherG;
        final SpeciesGene father = mother == otherG ? this : otherG;
        newGene.setValue(mother.info.clone());
        if (newGene.info.entry.isMega()) newGene.info.entry = newGene.info.entry.getBaseForme();
        newGene.info.entry = newGene.info.entry.getChild(father.info.entry);

        // First get out whatever the default choice was here.
        newGene.mutate();

        final Map<String, MutationHolder> mutations = Tags.GENES.getMutations(this.getKey());
        if (!mutations.isEmpty())
        {
            final List<String> opts = new ArrayList<>();

            // These are the possile combinations of mutations, we will check
            // them in this order.
            opts.add(mother.info.entry.getTrimmedName() + "+" + father.info.entry.getTrimmedName());
            opts.add(father.info.entry.getTrimmedName() + "+" + mother.info.entry.getTrimmedName());
            opts.add("null+" + mother.info.entry.getTrimmedName());
            opts.add("null+" + father.info.entry.getTrimmedName());
            opts.add(mother.info.entry.getTrimmedName() + "+null");
            opts.add(father.info.entry.getTrimmedName() + "+null");
            opts.add("");

            Collections.shuffle(opts);

            boolean mutated = false;
            for (String s : opts)
            {
                if (!s.isEmpty()) s = s + "->";
                s = s + newGene.info.entry.getTrimmedName();
                mutated = mutations.containsKey(s);
                if (mutated)
                {
                    this.applyMutation(mutations.get(s), newGene);
                    break;
                }
            }
        }
        return newGene;
    }

    private void applyMutation(final MutationHolder mutationHolder, final SpeciesGene newGene)
    {
        final Mutation mutation = mutationHolder.getFor(this.rand.nextFloat());

        String[] args = mutation.result.split(":;");

        PokedexEntry value = Database.getEntry(args[0]);
        FormeHolder forme = args.length > 1 ? Database.formeHolders.get(new ResourceLocation(args[1])) : null;

        if (value != null)
        {
            newGene.info.entry = value;
            // Ensure gender ratios are correct
            newGene.info.value = SpeciesGene.getSexe(newGene.info.entry.getSexeRatio(), this.rand);
            // Also apply the formeholder if present (defaults to null)
            newGene.info.forme = forme;
        }
    }

    @Override
    public void load(final CompoundTag tag)
    {
        this.info.load(tag.getCompound("V"));
    }

    @Override
    public Gene<SpeciesInfo> mutate()
    {
        final SpeciesGene newGene = new SpeciesGene();
        newGene.setValue(this.info.clone());
        // Prevents mobs from hatching with wrong forms.
        newGene.info.entry = this.info.entry.getChild();
        // Ensure gender ratios are correct
        newGene.info.value = SpeciesGene.getSexe(newGene.info.entry.getSexeRatio(), this.rand);
        return newGene;
    }

    @Override
    public CompoundTag save()
    {
        final CompoundTag tag = new CompoundTag();
        tag.put("V", this.info.save());
        return tag;
    }

    @Override
    public void setValue(final SpeciesInfo value)
    {
        this.info = value;
    }

    @Override
    public String toString()
    {
        return this.info.toString();
    }
}
