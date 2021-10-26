package pokecube.core.ai.poi;

import com.google.common.collect.Sets;

import net.minecraft.village.PointOfInterestType;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import pokecube.core.PokecubeCore;
import pokecube.core.PokecubeItems;

public class PointsOfInterest
{
    public static final DeferredRegister<PointOfInterestType> REG = DeferredRegister.create(ForgeRegistries.POI_TYPES,
            PokecubeCore.MODID);

    public static final RegistryObject<PointOfInterestType> HEALER = PointsOfInterest.REG.register("healer",
            () -> new PointOfInterestType("healer", Sets.newHashSet(PokecubeItems.HEALER.get().getStateDefinition()
                    .getPossibleStates()), 1, 2));
    public static final RegistryObject<PointOfInterestType> NEST   = PointsOfInterest.REG.register("pokemob_nest",
            () -> new PointOfInterestType("pokemob_nest", Sets.newHashSet(PokecubeItems.NESTBLOCK.get()
                    .getStateDefinition().getPossibleStates()), 1, 2));

    public static void postInit()
    {
        PointsOfInterest.REG.getEntries().forEach(r -> PointOfInterestType.registerBlockStates(r.get()));
    }
}
