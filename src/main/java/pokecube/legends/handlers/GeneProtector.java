package pokecube.legends.handlers;

import net.minecraftforge.eventbus.api.SubscribeEvent;
import pokecube.adventures.events.GeneEditEvent;
import pokecube.adventures.events.GeneEditEvent.EditType;
import pokecube.core.database.PokedexEntry;
import pokecube.core.database.stats.SpecialCaseRegister;
import pokecube.core.entity.pokemobs.genetics.GeneticsManager;
import pokecube.core.entity.pokemobs.genetics.genes.SpeciesGene;
import pokecube.core.entity.pokemobs.genetics.genes.SpeciesGene.SpeciesInfo;
import pokecube.core.events.EggEvent.CanBreed;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.capabilities.CapabilityPokemob;
import thut.api.entity.genetics.Alleles;

public class GeneProtector
{
    public boolean invalidEntry(final PokedexEntry entry, final boolean breeding)
    {
        // No cloning legends, breeding them is fine, as should be flagged as
        // unable to do so manually.
        if (!breeding && entry.isLegendary()) return true;
        // No cloning things with requirements
        if (SpecialCaseRegister.getCaptureCondition(entry) != null || SpecialCaseRegister.getSpawnCondition(
                entry) != null) return true;
        // No cloning things that can't breed
        if (!entry.breeds) return true;
        return false;
    }

    public boolean invalidGene(final SpeciesGene gene)
    {
        final SpeciesInfo info = gene.getValue();
        final PokedexEntry entry = info.entry;
        return this.invalidEntry(entry, false);
    }

    @SubscribeEvent
    public void GeneEditEvent(final GeneEditEvent evt)
    {
        if (evt.resultGenes.getAlleles().containsKey(GeneticsManager.SPECIESGENE))
        {
            final Alleles alleles = evt.resultGenes.getAlleles().get(GeneticsManager.SPECIESGENE);
            final SpeciesGene gene = alleles.getExpressed();
            if(evt.reason==EditType.EXTRACT) if (this.invalidGene(gene)) evt.resultGenes.getAlleles().remove(GeneticsManager.SPECIESGENE);
        }
    }

    @SubscribeEvent
    public void CanBreedEvent(final CanBreed evt)
    {
        final IPokemob mobA = CapabilityPokemob.getPokemobFor(evt.getEntity());
        final IPokemob mobB = CapabilityPokemob.getPokemobFor(evt.getOther());
        if (mobA != null && this.invalidEntry(mobA.getPokedexEntry(), true)) evt.setCanceled(true);
        if (mobB != null && this.invalidEntry(mobB.getPokedexEntry(), true)) evt.setCanceled(true);
    }
}