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

        PointsOfInterest.GENELAB = new PointOfInterestType("pokecube_adventures:gene_lab", PointsOfInterest.LABMACHINES,
                1, null, 2);
        PointsOfInterest.HEALER = new PointOfInterestType("pokecube_adventures:healer", Sets.newHashSet(
                PokecubeItems.HEALER.getStateContainer().getValidStates()), 1, null, 2);
        System.out.println(Sets.newHashSet(PokecubeItems.HEALER.getStateContainer().getValidStates()));
        System.out.println(PointsOfInterest.LABMACHINES);

        event.getRegistry().register(PointsOfInterest.GENELAB.setRegistryName("pokecube_adventures:gene_lab"));
        event.getRegistry().register(PointsOfInterest.HEALER.setRegistryName("pokecube_adventures:healer"));

        PointOfInterestType.func_221052_a(PointsOfInterest.GENELAB);
        PointOfInterestType.func_221052_a(PointsOfInterest.HEALER);
    }

}
