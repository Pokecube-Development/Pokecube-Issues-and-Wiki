package pokecube.adventures.ai.poi;

import java.util.Set;

import com.google.common.collect.Sets;

import net.minecraft.block.BlockState;
import net.minecraft.village.PointOfInterestType;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import pokecube.adventures.PokecubeAdv;

public class PointsOfInterest
{
    public static final DeferredRegister<PointOfInterestType> REG = DeferredRegister.create(ForgeRegistries.POI_TYPES,
            PokecubeAdv.MODID);

    public static Set<BlockState> LABMACHINES = Sets.newHashSet();

    private static Set<BlockState> getLabMachines()
    {
        PointsOfInterest.LABMACHINES.clear();
        PointsOfInterest.LABMACHINES.addAll(PokecubeAdv.EXTRACTOR.get().getStateDefinition().getPossibleStates());
        PointsOfInterest.LABMACHINES.addAll(PokecubeAdv.SPLICER.get().getStateDefinition().getPossibleStates());
        PointsOfInterest.LABMACHINES.addAll(PokecubeAdv.CLONER.get().getStateDefinition().getPossibleStates());
        return PointsOfInterest.LABMACHINES;
    }

    public static final RegistryObject<PointOfInterestType> GENELAB = PointsOfInterest.REG.register("gene_lab",
            () -> new PointOfInterestType("gene_lab", PointsOfInterest.getLabMachines(), 1, 2));

    public static void postInit()
    {
        PointsOfInterest.REG.getEntries().forEach(r -> PointOfInterestType.registerBlockStates(r.get()));
    }
}
