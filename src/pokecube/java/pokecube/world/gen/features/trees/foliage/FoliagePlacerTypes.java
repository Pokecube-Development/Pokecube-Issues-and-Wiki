package pokecube.world.gen.features.trees.foliage;

import java.util.function.Supplier;

import net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacerType;
import pokecube.world.PokecubeWorld;

public class FoliagePlacerTypes
{
    public static final Supplier<FoliagePlacerType<CustomShapeFoliagePlacer>> CUSTOM_SHAPE_FOLIAGE_PLACER;
    public static final Supplier<FoliagePlacerType<DistortedFoliagePlacer>> DISTORTED_FOLIAGE_PLACER;
    public static final Supplier<FoliagePlacerType<PalmFoliagePlacer>> PALM_FOLIAGE_PLACER;
    public static final Supplier<FoliagePlacerType<RainDropFoliagePlacer>> RAIN_DROP_FOLIAGE_PLACER;
    public static final Supplier<FoliagePlacerType<RoundFoliagePlacer>> ROUND_FOLIAGE_PLACER;

    static
    {
        CUSTOM_SHAPE_FOLIAGE_PLACER = PokecubeWorld.FOLIAGE_PLACERS.register("custom_shape_foliage_placer",
                () -> new FoliagePlacerType<>(CustomShapeFoliagePlacer.CODEC));
        DISTORTED_FOLIAGE_PLACER = PokecubeWorld.FOLIAGE_PLACERS.register("distorted_foliage_placer",
                () -> new FoliagePlacerType<>(DistortedFoliagePlacer.CODEC));
        PALM_FOLIAGE_PLACER = PokecubeWorld.FOLIAGE_PLACERS.register("palm_foliage_placer",
                () -> new FoliagePlacerType<>(PalmFoliagePlacer.CODEC));
        RAIN_DROP_FOLIAGE_PLACER = PokecubeWorld.FOLIAGE_PLACERS.register("rain_drop_foliage_placer",
                () -> new FoliagePlacerType<>(RainDropFoliagePlacer.CODEC));
        ROUND_FOLIAGE_PLACER = PokecubeWorld.FOLIAGE_PLACERS.register("round_foliage_placer",
                () -> new FoliagePlacerType<>(RoundFoliagePlacer.CODEC));
    }

    public static void init()
    {};
}
