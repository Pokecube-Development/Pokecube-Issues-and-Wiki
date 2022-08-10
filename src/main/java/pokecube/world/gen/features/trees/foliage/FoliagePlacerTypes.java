package pokecube.world.gen.features.trees.foliage;

import net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacerType;
import net.minecraftforge.registries.RegistryObject;
import pokecube.world.PokecubeWorld;

public class FoliagePlacerTypes
{
    public static final RegistryObject<FoliagePlacerType<CustomShapeFoliagePlacer>> CUSTOM_SHAPE_FOLIAGE_PLACER;
    public static final RegistryObject<FoliagePlacerType<DistortedFoliagePlacer>> DISTORTED_FOLIAGE_PLACER;
    public static final RegistryObject<FoliagePlacerType<PalmFoliagePlacer>> PALM_FOLIAGE_PLACER;
    public static final RegistryObject<FoliagePlacerType<PeanutFoliagePlacer>> PEANUT_FOLIAGE_PLACER;
    public static final RegistryObject<FoliagePlacerType<RoundFoliagePlacer>> ROUND_FOLIAGE_PLACER;

    static
    {
        CUSTOM_SHAPE_FOLIAGE_PLACER = PokecubeWorld.FOLIAGE_PLACERS.register("custom_shape_foliage_placer",
                () -> new FoliagePlacerType<>(CustomShapeFoliagePlacer.CODEC));
        DISTORTED_FOLIAGE_PLACER = PokecubeWorld.FOLIAGE_PLACERS.register("distorted_foliage_placer",
                () -> new FoliagePlacerType<>(DistortedFoliagePlacer.CODEC));
        PALM_FOLIAGE_PLACER = PokecubeWorld.FOLIAGE_PLACERS.register("palm_foliage_placer",
                () -> new FoliagePlacerType<>(PalmFoliagePlacer.CODEC));
        PEANUT_FOLIAGE_PLACER = PokecubeWorld.FOLIAGE_PLACERS.register("peanut_foliage_placer",
                () -> new FoliagePlacerType<>(PeanutFoliagePlacer.CODEC));
        ROUND_FOLIAGE_PLACER = PokecubeWorld.FOLIAGE_PLACERS.register("round_foliage_placer",
                () -> new FoliagePlacerType<>(RoundFoliagePlacer.CODEC));
    }

    public static void init()
    {};
}
