package pokecube.core.entity.pokemobs.genetics.genes;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.util.INBTSerializable;
import pokecube.core.entity.pokemobs.genetics.GeneticsManager;
import pokecube.core.entity.pokemobs.genetics.genes.DynamaxGene.DynaObject;
import thut.api.entity.genetics.Gene;
import thut.core.common.ThutCore;

public class DynamaxGene implements Gene<DynaObject>
{
    public static class DynaObject implements INBTSerializable<CompoundTag>
    {
        public boolean gigantamax = false;

        @Override
        public CompoundTag serializeNBT()
        {
            final CompoundTag tag = new CompoundTag();
            tag.putBoolean("gmax", this.gigantamax);
            return tag;
        }

        @Override
        public void deserializeNBT(final CompoundTag nbt)
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

}
