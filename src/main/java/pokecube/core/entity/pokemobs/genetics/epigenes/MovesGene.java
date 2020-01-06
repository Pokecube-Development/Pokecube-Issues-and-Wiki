package pokecube.core.entity.pokemobs.genetics.epigenes;

import java.util.Arrays;
import java.util.Comparator;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;
import pokecube.core.entity.pokemobs.genetics.GeneticsManager;
import thut.api.entity.genetics.Gene;

public class MovesGene implements Gene
{
    private static final Comparator<String> SORTER = (o1, o2) ->
    {
        if (o1 == null && o2 != null) return 1;
        else if (o2 == null && o1 != null) return -1;
        return 0;
    };

    private static final void cleanup(String[] moves)
    {
        outer:
        for (int i = 0; i < moves.length; i++)
        {
            final String temp = moves[i];
            if (temp == null) continue;
            for (int j = i + 1; j < moves.length; j++)
            {
                final String temp2 = moves[j];
                if (temp2 == null) continue;
                if (temp.equals(temp2))
                {
                    moves[j] = null;
                    continue outer;
                }
            }
        }
        Arrays.sort(moves, MovesGene.SORTER);
    }

    String[] moves = new String[4];

    @Override
    public float getEpigeneticRate()
    {
        return 0.75f;
    }

    @Override
    public ResourceLocation getKey()
    {
        return GeneticsManager.MOVESGENE;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T getValue()
    {
        return (T) this.moves;
    }

    @Override
    public Gene interpolate(Gene other)
    {
        final MovesGene newGene = new MovesGene();
        final MovesGene otherG = (MovesGene) other;
        for (int i = 0; i < this.moves.length; i++)
        {
            if (this.moves[i] == null) continue;
            for (int j = 0; j < otherG.moves.length; j++)
                if (this.moves[i].equals(otherG.moves[j]))
                {
                    newGene.moves[i] = this.moves[i];
                    break;
                }
        }
        newGene.setValue(newGene.moves);
        return newGene;
    }

    @Override
    public void load(CompoundNBT tag)
    {
        for (int i = 0; i < this.moves.length; i++)
            if (tag.contains("" + i)) this.moves[i] = tag.getString("" + i);
        MovesGene.cleanup(this.moves);
    }

    @Override
    public Gene mutate()
    {
        final MovesGene newGene = new MovesGene();
        newGene.moves = this.moves.clone();
        return newGene;
    }

    @Override
    public CompoundNBT save()
    {
        final CompoundNBT tag = new CompoundNBT();
        MovesGene.cleanup(this.moves);
        for (int i = 0; i < this.moves.length; i++)
            if (this.moves[i] != null) tag.putString("" + i, this.moves[i]);
        return tag;
    }

    @Override
    public <T> void setValue(T value)
    {
        this.moves = (String[]) value;
        MovesGene.cleanup(this.moves);
    }

    @Override
    public String toString()
    {
        return Arrays.toString(this.moves);
    }

}
