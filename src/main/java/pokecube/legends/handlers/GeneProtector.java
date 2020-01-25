package pokecube.legends.handlers;

import net.minecraftforge.eventbus.api.SubscribeEvent;
import pokecube.adventures.events.GeneEditEvent;
import pokecube.adventures.events.GeneEditEvent.EditType;
import pokecube.core.database.PokedexEntry;
import pokecube.core.database.stats.SpecialCaseRegister;
import pokecube.core.entity.pokemobs.genetics.GeneticsManager;
import pokecube.core.entity.pokemobs.genetics.genes.SpeciesGene;
import pokecube.core.entity.pokemobs.genetics.genes.SpeciesGene.SpeciesInfo;
import thut.api.entity.genetics.Alleles;

public class GeneProtector
{
    public boolean invalidGene(final SpeciesGene gene)
    {
        final SpeciesInfo info = gene.getValue();
        final PokedexEntry entry = info.entry;
        // No cloning legends.
        if (entry.legendary) return true;
        // No cloning things with requirements
        if (SpecialCaseRegister.getCaptureCondition(entry) != null || SpecialCaseRegister.getSpawnCondition(
                entry) != null) return true;
        // No cloning things that can't breed
        if (!entry.breeds) return true;
        return false;
    }

    @SubscribeEvent
    public void GeneEditEvent(final GeneEditEvent evt)
    {
        if (evt.resultGenes.getAlleles().containsKey(GeneticsManager.SPECIESGENE) && evt.reason == EditType.EXTRACT)
        {
            final Alleles alleles = evt.resultGenes.getAlleles().get(GeneticsManager.SPECIESGENE);
            final SpeciesGene gene = alleles.getExpressed();
            if (this.invalidGene(gene)) evt.resultGenes.getAlleles().remove(GeneticsManager.SPECIESGENE);
        }
    }
}