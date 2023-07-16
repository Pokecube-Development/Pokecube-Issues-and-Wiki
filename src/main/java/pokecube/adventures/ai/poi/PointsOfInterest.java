package pokecube.adventures.ai.poi;

import java.util.Set;
import java.util.function.Predicate;

import com.google.common.collect.Sets;

import net.minecraft.core.Holder;
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

    public static final RegistryObject<PoiType> _GENELAB = PointsOfInterest.REG.register("gene_lab",
            () -> new PoiType(PointsOfInterest.getLabMachines(), 1, 2));

    public static final RegistryObject<PoiType> _TRADER = PointsOfInterest.REG.register("trader",
            () -> new PoiType(PointsOfInterest.getTradebMachines(), 1, 2));

    public static final Predicate<Holder<PoiType>> GENELAB = holder -> holder.is(_GENELAB.getKey());;
    public static final Predicate<Holder<PoiType>> TRADER = holder -> holder.is(_TRADER.getKey());;

    public static void postInit()
    {}
}
