package pokecube.legends.handlers;

import net.minecraftforge.eventbus.api.SubscribeEvent;
import pokecube.api.data.PokedexEntry;
import pokecube.api.events.GeneEditEvent;
import pokecube.api.events.GeneEditEvent.EditType;
import pokecube.api.stats.SpecialCaseRegister;
import pokecube.core.entity.genetics.GeneticsManager;
import pokecube.core.entity.genetics.genes.SpeciesGene;
import pokecube.core.entity.genetics.genes.SpeciesGene.SpeciesInfo;
import thut.api.entity.genetics.Alleles;

public class GeneProtector
{
    public boolean invalidEntry(final PokedexEntry entry, final boolean breeding)
    {
        // No cloning legends, breeding them is fine, as should be flagged as
        // unable to do so manually.
        if (!breeding && entry.isLegendary()) return true;
        // No cloning things with requirements
        if (SpecialCaseRegister.getCaptureCondition(entry) != null
                || SpecialCaseRegister.getSpawnCondition(entry) != null)
            return true;
        // No cloning things that can't breed
        if (!entry.breeds) return true;
        return false;
    }

    public boolean invalidGene(final SpeciesGene gene)
    {
        final SpeciesInfo info = gene.getValue();
        final PokedexEntry entry = info.getEntry();
        return this.invalidEntry(entry, false);
    }

    @SubscribeEvent
    public void GeneEditEvent(final GeneEditEvent evt)
    {
        if (evt.resultGenes.getAlleles().containsKey(GeneticsManager.SPECIESGENE))
        {
            final Alleles<SpeciesInfo, SpeciesGene> alleles = evt.resultGenes.getAlleles(GeneticsManager.SPECIESGENE);
            final SpeciesGene gene = alleles.getExpressed();
            final SpeciesGene parentA = alleles.getAllele(0);
            final SpeciesGene parentB = alleles.getAllele(1);
            if (evt.reason == EditType.EXTRACT || evt.reason == EditType.SPLICE)
            {
                if (this.invalidGene(gene)) evt.resultGenes.getAlleles().remove(GeneticsManager.SPECIESGENE);
                if (this.invalidGene(parentA)) evt.resultGenes.getAlleles().remove(GeneticsManager.SPECIESGENE);
                if (this.invalidGene(parentB)) evt.resultGenes.getAlleles().remove(GeneticsManager.SPECIESGENE);
            }
        }
    }
}