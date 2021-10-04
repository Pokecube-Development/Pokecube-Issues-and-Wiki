package thut.api.entity.genetics;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;

public interface Gene<T>
{
    /**
     * This is how frequently the expressed gene is used instead of the
     * parent's genes.
     *
     * @return value from 0-1 of how often it uses expressed..
     */
    default float getEpigeneticRate()
    {
        return 0;
    }

    /**
     * @return key to correspond to this class of Gene. This should return the
     *         same value for every instance of this class.
     */
    ResourceLocation getKey();

    default float getMutationRate()
    {
        return 0;
    }

    /** @return the value of this gene. */
    T getValue();

    /**
     * This method should return the new gene which results from mixing other
     * with this gene.
     */
    Gene<T> interpolate(Gene<T> other);

    /**
     * Loads the data from tag.
     *
     * @param tag
     */
    void load(CompoundTag tag);

    /** This method should return a mutated gene. */
    Gene<T> mutate();

    /**
     * This method should return a mutated gene, this one is called during
     * breeding, to allow any changes needed caused by the entirety of the
     * parents genes.
     */
    default Gene<T> mutate(final IMobGenetics parent1, final IMobGenetics parent2)
    {
        return this.mutate();
    }

    /**
     * This is called whenever the mob associated with this gene ticks. This is
     * only called if this gene is expressed.
     *
     * @param genes
     */
    default void onUpdateTick(final Entity entity)
    {

    }

    /** @return nbttag compount for saving. */
    CompoundTag save();

    /**
     * @param value
     *            Sets the value of the gene.
     */
    void setValue(T value);
}
