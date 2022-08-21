package pokecube.core.entity.pokemobs.genetics.genes;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import pokecube.api.data.abilities.Ability;
import pokecube.core.entity.pokemobs.genetics.GeneticsManager;
import pokecube.core.entity.pokemobs.genetics.genes.AbilityGene.AbilityObject;
import thut.api.entity.genetics.Gene;
import thut.core.common.ThutCore;

public class AbilityGene implements Gene<AbilityObject>
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

    @Override
    public AbilityObject getValue()
    {
        return this.ability;
    }

    @Override
    public Gene<AbilityObject> interpolate(final Gene<AbilityObject> other)
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
    public void load(final CompoundTag tag)
    {
        this.ability.abilityIndex = tag.getByte("I");
        this.ability.ability = tag.getString("A");
    }

    @Override
    public Gene<AbilityObject> mutate()
    {
        final AbilityGene newGene = new AbilityGene();
        final byte index = (byte) (this.ability.abilityIndex == 2 ? ThutCore.newRandom().nextInt(2) : 2);
        newGene.ability.abilityIndex = index;
        return newGene;
    }

    @Override
    public CompoundTag save()
    {
        final CompoundTag tag = new CompoundTag();
        tag.putByte("I", this.ability.abilityIndex);
        tag.putString("A", this.ability.ability);
        return tag;
    }

    @Override
    public void setValue(final AbilityObject value)
    {
        this.ability = value;
    }

    @Override
    public String toString()
    {
        return this.ability.abilityIndex + " " + this.ability.ability;
    }

}
