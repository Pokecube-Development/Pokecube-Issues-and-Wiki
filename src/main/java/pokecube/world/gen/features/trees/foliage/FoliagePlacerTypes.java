package pokecube.world.gen.features.trees.foliage;

import net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacerType;
import net.minecraftforge.registries.RegistryObject;
import pokecube.world.PokecubeWorld;

public class FoliagePlacerTypes
{
    public static final RegistryObject<FoliagePlacerType<PalmFoliagePlacer>> PALM_FOLIAGE_PLACER;
    public static final RegistryObject<FoliagePlacerType<PyramidFoliagePlacer>> PYRAMID_FOLIAGE_PLACER;
    public static final RegistryObject<FoliagePlacerType<CustomShapeFoliagePlacer>> CUSTOM_SHAPE_FOLIAGE_PLACER;

    static
    {
        PALM_FOLIAGE_PLACER = PokecubeWorld.FOLIAGE_PLACERS.register("palm_foliage_placer",
                () -> new FoliagePlacerType<>(PalmFoliagePlacer.CODEC));
        PYRAMID_FOLIAGE_PLACER = PokecubeWorld.FOLIAGE_PLACERS.register("pyramid_foliage_placer",
                () -> new FoliagePlacerType<>(PyramidFoliagePlacer.CODEC));
        CUSTOM_SHAPE_FOLIAGE_PLACER = PokecubeWorld.FOLIAGE_PLACERS.register("custom_shape_foliage_placer",
                () -> new FoliagePlacerType<>(CustomShapeFoliagePlacer.CODEC));
    }

    public static void init()
    {};
}
