package pokecube.core.entity.pokemobs.genetics.genes;

import java.util.Random;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.util.INBTSerializable;
import pokecube.core.entity.pokemobs.genetics.GeneticsManager;
import pokecube.core.entity.pokemobs.genetics.genes.DynamaxGene.DynaObject;
import thut.api.entity.genetics.Gene;

public class DynamaxGene implements Gene<DynaObject>
{
    public static class DynaObject implements INBTSerializable<CompoundNBT>
    {
        public boolean gigantamax = false;

        @Override
        public CompoundNBT serializeNBT()
        {
            final CompoundNBT tag = new CompoundNBT();
            tag.putBoolean("gmax", this.gigantamax);
            return tag;
        }

        @Override
        public void deserializeNBT(final CompoundNBT nbt)
        {
            this.gigantamax = nbt.getBoolean("gmax");
        }
    }

    private DynaObject value = new DynaObject();

    @Override
    public ResourceLocation getKey()
    {
        return GeneticsManager.GMAXGENE;
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
        result.value = new Random().nextBoolean() ? other.getValue() : this.getValue();
        return result;
    }

    @Override
    public void load(final CompoundNBT tag)
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
    public CompoundNBT save()
    {
        return this.value.serializeNBT();
    }

    @Override
    public void setValue(final DynaObject value)
    {
        this.value = value;
    }

}
