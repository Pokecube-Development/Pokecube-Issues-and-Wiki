package pokecube.core.entity.pokemobs.genetics.genes;

import java.util.Random;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;
import pokecube.core.database.abilities.Ability;
import pokecube.core.entity.pokemobs.genetics.GeneticsManager;
import thut.api.entity.genetics.Gene;

public class AbilityGene implements Gene
{
    public static class AbilityObject
    {
        // This value is only set when a pokemob makes the ability, so should
        // only exist in an expressed gene.
        public Ability abilityObject = null;
        // Have we searched for an ability yet, if not, will look for one first
        // time ability is got.
        public boolean searched     = false;
        public String  ability      = "";
        public byte    abilityIndex = 0;
    }

    protected AbilityObject ability = new AbilityObject();

    @Override
    public ResourceLocation getKey()
    {
        return GeneticsManager.ABILITYGENE;
    }

    @Override
    public float getMutationRate()
    {
        return GeneticsManager.mutationRates.get(this.getKey());
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T getValue()
    {
        return (T) this.ability;
    }

    @Override
    public Gene interpolate(final Gene other)
    {
        final AbilityGene otherA = (AbilityGene) other;
        final byte otherIndex = otherA.ability.abilityIndex;
        final byte index = otherIndex == this.ability.abilityIndex ? otherIndex
                : Math.random() < 0.5 ? otherIndex : this.ability.abilityIndex;
        final AbilityGene newGene = new AbilityGene();
        if (!otherA.ability.ability.isEmpty() && otherA.ability.ability.equals(this.ability.ability))
            newGene.ability.ability = this.ability.ability;
        newGene.ability.abilityIndex = index;
        return newGene;
    }

    @Override
    public void load(final CompoundNBT tag)
    {
        this.ability.abilityIndex = tag.getByte("I");
        this.ability.ability = tag.getString("A");
    }

    @Override
    public Gene mutate()
    {
        final AbilityGene newGene = new AbilityGene();
        final byte index = (byte) (this.ability.abilityIndex == 2 ? new Random().nextInt(2) : 2);
        newGene.ability.abilityIndex = index;
        return newGene;
    }

    @Override
    public CompoundNBT save()
    {
        final CompoundNBT tag = new CompoundNBT();
        tag.putByte("I", this.ability.abilityIndex);
        tag.putString("A", this.ability.ability);
        return tag;
    }

    @Override
    public <T> void setValue(final T value)
    {
        this.ability = (AbilityObject) value;
    }

    @Override
    public String toString()
    {
        return this.ability.abilityIndex + " " + this.ability.ability;
    }

}
