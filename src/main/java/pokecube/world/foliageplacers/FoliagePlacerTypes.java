package pokecube.world.foliageplacers;

import net.minecraft.core.Registry;
import net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacerType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;
import pokecube.core.PokecubeCore;

public class FoliagePlacerTypes {
    public static final DeferredRegister<FoliagePlacerType<?>> FOLIAGE_PLACERS = DeferredRegister.create(Registry.FOLIAGE_PLACER_TYPE_REGISTRY, PokecubeCore.MODID);

    public static final RegistryObject<FoliagePlacerType<PalmFoilagePlacer>> PALM_FOLIAGE_PLACER =
            FOLIAGE_PLACERS.register("palm_foliage_placer", () -> new FoliagePlacerType<>(PalmFoilagePlacer.CODEC));
}
