package pokecube.core.entity.genetics.epigenes;

import java.util.Arrays;
import java.util.Comparator;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import pokecube.api.PokecubeAPI;
import pokecube.api.moves.MoveEntry;
import pokecube.core.PokecubeCore;
import pokecube.core.entity.genetics.GeneticsManager;
import thut.api.entity.genetics.Gene;

public class MovesGene implements Gene<String[]>
{
    private static final Comparator<String> SORTER = (o1, o2) -> {
        if (o1 == null && o2 != null) return 1;
        else if (o2 == null && o1 != null) return -1;
        return 0;
    };

    private static final void cleanup(final String[] moves)
    {
        outer:
        for (int i = 0; i < moves.length; i++)
        {
            String temp = moves[i];
            if (temp == null) continue;
            // Check move entries
            MoveEntry entry = MoveEntry.get(temp);
            if (entry != null)
            {
                // Update name if needed from legacy names
                moves[i] = temp = entry.name;
            }
            else if (PokecubeCore.getConfig().debug_moves)
            {
                PokecubeAPI.LOGGER.warn("Unknown move {}", temp);
            }
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

    @Override
    public String[] getValue()
    {
        return this.moves;
    }

    @Override
    public Gene<String[]> interpolate(final Gene<String[]> other)
    {
        final MovesGene newGene = new MovesGene();
        final MovesGene otherG = (MovesGene) other;
        for (int i = 0; i < this.moves.length; i++)
        {
            if (this.moves[i] == null) continue;
            for (int j = 0; j < otherG.moves.length; j++) if (this.moves[i].equals(otherG.moves[j]))
            {
                newGene.moves[i] = this.moves[i];
                break;
            }
        }
        newGene.setValue(newGene.moves);
        return newGene;
    }

    @Override
    public void load(final CompoundTag tag)
    {
        for (int i = 0; i < this.moves.length; i++) if (tag.contains("" + i)) this.moves[i] = tag.getString("" + i);
        MovesGene.cleanup(this.moves);
    }

    @Override
    public Gene<String[]> mutate()
    {
        final MovesGene newGene = new MovesGene();
        newGene.moves = this.moves.clone();
        return newGene;
    }

    @Override
    public CompoundTag save()
    {
        final CompoundTag tag = new CompoundTag();
        MovesGene.cleanup(this.moves);
        for (int i = 0; i < this.moves.length; i++) if (this.moves[i] != null) tag.putString("" + i, this.moves[i]);
        return tag;
    }

    @Override
    public void setValue(final String[] value)
    {
        this.moves = value;
        MovesGene.cleanup(this.moves);
    }

    @Override
    public String toString()
    {
        return Arrays.toString(this.moves);
    }

}
