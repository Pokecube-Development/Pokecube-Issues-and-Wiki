package pokecube.core.ai.poi;

import com.google.common.collect.Sets;

import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import pokecube.core.PokecubeCore;
import pokecube.core.PokecubeItems;

public class PointsOfInterest
{
    public static final DeferredRegister<PoiType> REG = DeferredRegister.create(ForgeRegistries.POI_TYPES,
            PokecubeCore.MODID);

    public static final RegistryObject<PoiType> HEALER = PointsOfInterest.REG.register("healer",
            () -> new PoiType("healer",
                    Sets.newHashSet(PokecubeItems.HEALER.get().getStateDefinition().getPossibleStates()), 1, 2));
    public static final RegistryObject<PoiType> NEST = PointsOfInterest.REG.register("pokemob_nest",
            () -> new PoiType("pokemob_nest",
                    Sets.newHashSet(PokecubeItems.NESTBLOCK.get().getStateDefinition().getPossibleStates()), 1, 2));

    public static void postInit()
    {
        PointsOfInterest.REG.getEntries().forEach(r -> PoiType.registerBlockStates(r.get()));
    }
}
