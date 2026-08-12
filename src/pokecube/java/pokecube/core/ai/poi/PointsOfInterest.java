package pokecube.core.ai.poi;

import java.util.function.Predicate;

import com.google.common.collect.Sets;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import pokecube.core.PokecubeCore;
import pokecube.core.PokecubeItems;

public class PointsOfInterest
{
    public static final DeferredRegister<PoiType> REG = DeferredRegister
            .create(BuiltInRegistries.POINT_OF_INTEREST_TYPE, PokecubeCore.MODID);

    public static final DeferredHolder<PoiType, PoiType> _HEALER = PointsOfInterest.REG.register("healer",
            () -> new PoiType(Sets.newHashSet(PokecubeItems.HEALER.get().getStateDefinition().getPossibleStates()), 1,
                    2));

    public static final Predicate<Holder<PoiType>> HEALER = holder -> holder.is(_HEALER.getKey());

    // 1.19 notes: These need to turn to RegistryKey<PoiType>

    // This needs to turn to "init", and then be blank
    public static void postInit()
    {
//        PointsOfInterest.REG.getEntries().forEach(r -> PoiType.registerBlockStates(r.get()));
    }
}
