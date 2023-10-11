package pokecube.core.entity.pokemobs.genetics.genes;

import javax.annotation.Nullable;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.common.util.INBTSerializable;
import pokecube.core.entity.pokemobs.genetics.GeneticsManager;
import pokecube.core.entity.pokemobs.genetics.genes.DynamaxGene.DynaObject;
import thut.api.ThutCaps;
import thut.api.entity.genetics.Alleles;
import thut.api.entity.genetics.Gene;
import thut.api.entity.genetics.IMobGenetics;
import thut.core.common.ThutCore;

public class DynamaxGene implements Gene<DynaObject>
{
    @Nullable
    public static DynaObject getDyna(Entity mob)
    {
        final IMobGenetics genes = mob.getCapability(ThutCaps.GENETICS_CAP, null).orElse(null);
        if (genes == null) return null;
        try
        {
            Alleles<DynaObject, Gene<DynaObject>> alleles = genes.getAlleles(GeneticsManager.GMAXGENE);
            if (alleles == null) return null;
            Gene<DynaObject> gene = alleles.getExpressed();
            return gene.getValue();
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return null;
        }
    }

    public static class DynaObject implements INBTSerializable<CompoundTag>
    {
        public boolean gigantamax = false;
        public int dynaLevel = 0;

        @Override
        public CompoundTag serializeNBT()
        {
            final CompoundTag tag = new CompoundTag();
            tag.putBoolean("gmax", this.gigantamax);
            tag.putInt("lvl", this.dynaLevel);
            return tag;
        }

        @Override
        public void deserializeNBT(final CompoundTag nbt)
        {
            this.gigantamax = nbt.getBoolean("gmax");
            this.dynaLevel = nbt.getInt("lvl");
        }
    }

    private DynaObject value = new DynaObject();

    @Override
    public ResourceLocation getKey()
    {
        return GeneticsManager.GMAXGENE;
    }

    @Override
    public float getMutationRate()
    {
        return GeneticsManager.mutationRates.get(this.getKey());
    }

    @Override
    public DynaObject getValue()
    {
        return this.value;
    }

    @Override
    public Gene<DynaObject> interpolate(final Gene<DynaObject> other)
    {
        final DynamaxGene result = new DynamaxGene();
        result.value = ThutCore.newRandom().nextBoolean() ? other.getValue() : this.getValue();
        return result;
    }

    @Override
    public void load(final CompoundTag tag)
    {
        this.value.deserializeNBT(tag);
    }

    @Override
    public Gene<DynaObject> mutate()
    {
        final DynamaxGene gene = new DynamaxGene();
        gene.value.gigantamax = true;
        return gene;
    }

    @Override
    public CompoundTag save()
    {
        return this.value.serializeNBT();
    }

    @Override
    public void setValue(final DynaObject value)
    {
        this.value = value;
    }

    @Override
    public String toString()
    {
        return "" + value.gigantamax;
    }
}
