package pokecube.core.entity.pokemobs.genetics.genes;

import java.util.Random;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import pokecube.core.entity.pokemobs.genetics.GeneticsManager;
import pokecube.core.interfaces.Nature;
import thut.api.entity.genetics.Gene;
import thut.core.common.ThutCore;

public class NatureGene implements Gene<Nature>
{
    private static Nature getNature(final Nature nature, final Nature nature2)
    {
        byte ret = 0;
        final Random rand = ThutCore.newRandom();
        final byte[] motherMods = nature.getStatsMod();
        final byte[] fatherMods = nature2.getStatsMod();
        final byte[] sum = new byte[6];
        for (int i = 0; i < 6; i++)
            sum[i] = (byte) (motherMods[i] + fatherMods[i]);
        int pos = 0;
        int start = rand.nextInt(100);
        for (int i = 0; i < 6; i++)
            if (sum[(i + start) % 6] > 0)
            {
                pos = (i + start) % 6;
                break;
            }
        int neg = 0;
        start = rand.nextInt(100);
        for (int i = 0; i < 6; i++)
            if (sum[(i + start) % 6] < 0)
            {
                neg = (i + start) % 6;
                break;
            }
        if (pos != 0 && neg != 0)
        {
            for (byte i = 0; i < 25; i++)
                if (Nature.values()[i].getStatsMod()[pos] > 0 && Nature.values()[i].getStatsMod()[neg] < 0)
                {
                    ret = i;
                    break;
                }
        }
        else if (pos != 0)
        {
            start = rand.nextInt(1000);
            for (byte i = 0; i < 25; i++)
                if (Nature.values()[(byte) ((i + start) % 25)].getStatsMod()[pos] > 0)
                {
                    ret = (byte) ((i + start) % 25);
                    break;
                }
        }
        else if (neg != 0)
        {
            start = rand.nextInt(1000);
            for (byte i = 0; i < 25; i++)
                if (Nature.values()[(byte) ((i + start) % 25)].getStatsMod()[neg] < 0)
                {
                    ret = (byte) ((i + start) % 25);
                    break;
                }
        }
        else
        {
            final int num = rand.nextInt(5);
            ret = (byte) (num * 6);
        }
        return Nature.values()[ret];
    }

    Random rand = ThutCore.newRandom();

    Nature nature = Nature.values()[this.rand.nextInt(Nature.values().length)];

    @Override
    public ResourceLocation getKey()
    {
        return GeneticsManager.NATUREGENE;
    }

    @Override
    public float getMutationRate()
    {
        return GeneticsManager.mutationRates.get(this.getKey());
    }

    @Override
    public Nature getValue()
    {
        return this.nature;
    }

    @Override
    public Gene<Nature> interpolate(final Gene<Nature> other)
    {
        final NatureGene newGene = new NatureGene();
        final NatureGene otherG = (NatureGene) other;
        newGene.nature = NatureGene.getNature(this.nature, otherG.nature);
        return newGene;
    }

    @Override
    public void load(final CompoundTag tag)
    {
        this.nature = Nature.values()[tag.getByte("V")];
    }

    @Override
    public Gene<Nature> mutate()
    {
        final NatureGene newGene = new NatureGene();
        return newGene;
    }

    @Override
    public CompoundTag save()
    {
        final CompoundTag tag = new CompoundTag();
        tag.putByte("V", (byte) this.nature.ordinal());
        return tag;
    }

    @Override
    public void setValue(final Nature value)
    {
        this.nature = value;
    }

    @Override
    public String toString()
    {
        return "" + this.nature;
    }

}
