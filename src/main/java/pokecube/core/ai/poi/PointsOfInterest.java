package pokecube.core.ai.poi;

import java.util.function.Predicate;

import com.google.common.collect.Sets;

import net.minecraft.core.Holder;
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

    public static final RegistryObject<PoiType> _HEALER = PointsOfInterest.REG.register("healer",
            () -> new PoiType(Sets.newHashSet(PokecubeItems.HEALER.get().getStateDefinition().getPossibleStates()), 1,
                    2));
    public static final RegistryObject<PoiType> _NEST = PointsOfInterest.REG.register("pokemob_nest",
            () -> new PoiType(Sets.newHashSet(PokecubeItems.NEST.get().getStateDefinition().getPossibleStates()), 1,
                    2));

    public static final Predicate<Holder<PoiType>> HEALER = holder -> holder.is(_HEALER.getKey());;
    public static final Predicate<Holder<PoiType>> NEST = holder -> holder.is(_NEST.getKey());;

    // 1.19 notes: These need to turn to RegistryKey<PoiType>

    // This needs to turn to "init", and then be blank
    public static void postInit()
    {
//        PointsOfInterest.REG.getEntries().forEach(r -> PoiType.registerBlockStates(r.get()));
    }
}
