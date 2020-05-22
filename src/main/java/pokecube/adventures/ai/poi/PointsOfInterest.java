package pokecube.adventures.ai.poi;

import java.util.Set;

import com.google.common.collect.Sets;

import net.minecraft.block.BlockState;
import net.minecraft.village.PointOfInterestType;
import net.minecraftforge.event.RegistryEvent.Register;
import pokecube.adventures.PokecubeAdv;
import pokecube.core.PokecubeItems;

public class PointsOfInterest
{
    public static Set<BlockState> LABMACHINES = Sets.newHashSet();

    public static PointOfInterestType GENELAB;
    public static PointOfInterestType HEALER;

    public static void register(final Register<PointOfInterestType> event)
    {
        PointsOfInterest.LABMACHINES.addAll(PokecubeAdv.EXTRACTOR.getStateContainer().getValidStates());
        PointsOfInterest.LABMACHINES.addAll(PokecubeAdv.SPLICER.getStateContainer().getValidStates());
        PointsOfInterest.LABMACHINES.addAll(PokecubeAdv.CLONER.getStateContainer().getValidStates());

        PointsOfInterest.GENELAB = PointOfInterestType.register("pokecube_adventures:gene_lab",
                PointsOfInterest.LABMACHINES, 1, 2);
        PointsOfInterest.HEALER = PointOfInterestType.register("pokecube_adventures:healer", Sets.newHashSet(
                PokecubeItems.HEALER.getStateContainer().getValidStates()), 1, 2);
    }

}
