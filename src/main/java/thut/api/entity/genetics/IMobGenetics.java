package thut.api.entity.genetics;

import java.util.Map;
import java.util.Set;

import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;

public interface IMobGenetics
{
    /**
     * This is a map of Name -> Alleles. this is to be used to sort the
     * Alleles. The keys for this should be the same as they key registed in
     * GeneRegistry
     *
     * @return
     */
    Map<ResourceLocation, Alleles> getAlleles();

    /**
     * This should return a set of genes which are epigenetic, this allows the
     * holder to edit them before saving them if needed.
     *
     * @return
     */
    Set<Alleles> getEpigenes();

    /**
     * This is called whenever the mob associated with this gene ticks.
     *
     * @param entity
     */
    default void onUpdateTick(Entity entity)
    {
        for (final Alleles allele : this.getAlleles().values())
            allele.getExpressed().onUpdateTick(entity);
    }

    void setFromParents(IMobGenetics parent1, IMobGenetics parent2);
}
