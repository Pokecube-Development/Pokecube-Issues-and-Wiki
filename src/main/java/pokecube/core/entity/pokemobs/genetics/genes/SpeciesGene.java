package pokecube.core.entity.pokemobs.genetics.genes;

import java.util.Random;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;
import pokecube.core.database.Database;
import pokecube.core.database.PokedexEntry;
import pokecube.core.entity.pokemobs.genetics.GeneticsManager;
import pokecube.core.interfaces.IPokemob;
import thut.api.entity.genetics.Gene;

public class SpeciesGene implements Gene
{
    public static class SpeciesInfo
    {
        public byte         value;
        public PokedexEntry entry;

        @Override
        public SpeciesInfo clone()
        {
            final SpeciesInfo info = new SpeciesInfo();
            info.value = this.value;
            info.entry = this.entry;
            return info;
        }

        @Override
        public boolean equals(Object obj)
        {
            if (!(obj instanceof SpeciesInfo)) return false;
            final SpeciesInfo info = (SpeciesInfo) obj;
            return this.value == info.value && (this.entry == null ? true : this.entry.equals(info.entry));
        }

        void load(CompoundNBT tag)
        {
            this.value = tag.getByte("G");
            this.entry = Database.getEntry(tag.getString("E"));
        }

        CompoundNBT save()
        {
            final CompoundNBT tag = new CompoundNBT();
            tag.putByte("G", this.value);
            if (this.entry != null) tag.putString("E", this.entry.getName());
            return tag;
        }

        @Override
        public String toString()
        {
            return this.entry + " " + this.value;
        }
    }

    public static byte getSexe(int baseValue, Random random)
    {
        if (baseValue == 255) return IPokemob.NOSEXE;
        if (random.nextInt(255) >= baseValue) return IPokemob.MALE;
        return IPokemob.FEMALE;
    }

    SpeciesInfo info = new SpeciesInfo();
    Random      rand = new Random();

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

    @SuppressWarnings("unchecked")
    @Override
    public <T> T getValue()
    {
        return (T) this.info;
    }

    @Override
    public Gene interpolate(Gene other)
    {
        final SpeciesGene newGene = new SpeciesGene();
        final SpeciesGene otherG = (SpeciesGene) other;
        SpeciesGene mother = this.info.value == IPokemob.FEMALE ? this : this.info.value > 0 ? this : otherG;
        if (this.info.value == otherG.info.value) mother = this.rand.nextFloat() < 0.5 ? this : otherG;
        final SpeciesGene father = mother == otherG ? this : otherG;
        newGene.setValue(mother.info.clone());
        if (newGene.info.entry.isMega) newGene.info.entry = newGene.info.entry.getBaseForme();
        newGene.info.entry = newGene.info.entry.getChild(father.info.entry);
        newGene.mutate();
        return newGene;
    }

    @Override
    public void load(CompoundNBT tag)
    {
        this.info.load(tag.getCompound("V"));
    }

    @Override
    public Gene mutate()
    {
        final SpeciesGene newGene = new SpeciesGene();
        newGene.setValue(this.info.clone());
        newGene.info.value = SpeciesGene.getSexe(newGene.info.entry.getSexeRatio(), this.rand);
        // Prevents mobs from hatching with wrong forms.
        newGene.info.entry = this.info.entry.getChild();
        return newGene;
    }

    @Override
    public CompoundNBT save()
    {
        final CompoundNBT tag = new CompoundNBT();
        tag.put("V", this.info.save());
        return tag;
    }

    @Override
    public <T> void setValue(T value)
    {
        this.info = (SpeciesInfo) value;
    }

    @Override
    public String toString()
    {
        return this.info.toString();
    }
}
