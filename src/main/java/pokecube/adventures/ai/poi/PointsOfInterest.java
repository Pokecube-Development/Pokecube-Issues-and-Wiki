package pokecube.adventures.ai.poi;

import java.util.Set;

import com.google.common.collect.Sets;

import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import pokecube.adventures.PokecubeAdv;
import pokecube.core.PokecubeItems;

public class PointsOfInterest
{
    public static final DeferredRegister<PoiType> REG = DeferredRegister.create(ForgeRegistries.POI_TYPES,
            PokecubeAdv.MODID);

    private static Set<BlockState> LABMACHINES = Sets.newHashSet();

    private static Set<BlockState> getLabMachines()
    {
        PointsOfInterest.LABMACHINES.clear();
        PointsOfInterest.LABMACHINES.addAll(PokecubeAdv.EXTRACTOR.get().getStateDefinition().getPossibleStates());
        PointsOfInterest.LABMACHINES.addAll(PokecubeAdv.SPLICER.get().getStateDefinition().getPossibleStates());
        PointsOfInterest.LABMACHINES.addAll(PokecubeAdv.CLONER.get().getStateDefinition().getPossibleStates());
        return PointsOfInterest.LABMACHINES;
    }

    private static Set<BlockState> TRADEMACHINES = Sets.newHashSet();

    private static Set<BlockState> getTradebMachines()
    {
        PointsOfInterest.TRADEMACHINES.clear();
        PointsOfInterest.TRADEMACHINES.addAll(PokecubeItems.TRADER.get().getStateDefinition().getPossibleStates());
        return PointsOfInterest.TRADEMACHINES;
    }

    public static final RegistryObject<PoiType> GENELAB = PointsOfInterest.REG.register("gene_lab",
            () -> new PoiType("gene_lab", PointsOfInterest.getLabMachines(), 1, 2));

    public static final RegistryObject<PoiType> TRADER = PointsOfInterest.REG.register("trader",
            () -> new PoiType("trader", PointsOfInterest.getTradebMachines(), 1, 2));

    public static void postInit()
    {
        PointsOfInterest.REG.getEntries().forEach(r -> PoiType.registerBlockStates(r.get()));
    }
}
