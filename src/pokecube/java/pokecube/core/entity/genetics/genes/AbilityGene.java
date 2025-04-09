package pokecube.core.entity.genetics.genes;

import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import pokecube.api.data.abilities.Ability;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.core.entity.genetics.GeneticsManager;
import pokecube.core.entity.genetics.genes.AbilityGene.AbilityObject;
import thut.api.entity.genetics.Gene;
import thut.core.common.ThutCore;

import java.util.Random;

public class AbilityGene implements Gene<AbilityObject>
{
    public static class AbilityObject
    {
        // This value is only set when a pokemob makes the ability, so should
        // only exist in an expressed gene.
        public Ability abilityObject = null;
        // Have we searched for an ability yet, if not, will look for one first
        // time ability is got.
        public boolean searched = false;
        public String ability = "";
        public byte abilityIndex = 0;
        boolean checked = false;

        private void validate(IPokemob pokemob){
            if(checked) return;
            var entry = pokemob.getPokedexEntry();
            int abilityIndex = new Random(pokemob.getRNGValue()).nextInt(100) % 2;
            if (entry.getAbility(abilityIndex, pokemob) == null) if (abilityIndex != 0) abilityIndex = 0;
            else abilityIndex = 1;
            Ability ability = entry.getAbility(abilityIndex, pokemob);
            this.ability = "";
            this.abilityObject = ability;
            this.abilityIndex = (byte) abilityIndex;
        }

        public byte getAbilityIndex(IPokemob pokemob)
        {
            this.validate(pokemob);
            return this.abilityIndex;
        }

        public String getAbility(IPokemob pokemob)
        {
            this.validate(pokemob);
            return ability;
        }
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
        final byte index = otherIndex == this.ability.abilityIndex
                ? otherIndex
                : Math.random() < 0.5 ? otherIndex : this.ability.abilityIndex;
        final AbilityGene newGene = new AbilityGene();
        if (!otherA.ability.ability.isEmpty() && otherA.ability.ability.equals(this.ability.ability))
            newGene.ability.ability = this.ability.ability;
        newGene.ability.abilityIndex = index;
        return newGene;
    }

    @Override
    public void load(Provider provider, final CompoundTag tag)
    {
        this.ability.abilityIndex = tag.getByte("I");
        this.ability.ability = tag.getString("A");
        this.ability.checked = true;
    }

    @Override
    public Gene<AbilityObject> mutate()
    {
        final AbilityGene newGene = new AbilityGene();
        newGene.ability.abilityIndex = (byte) (this.ability.abilityIndex == 2 ? ThutCore.newRandom().nextInt(2) : 2);
        return newGene;
    }

    @Override
    public CompoundTag save(Provider provider)
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
