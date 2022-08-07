package pokecube.world.gen.features.trees.foliage;

import net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacerType;
import net.minecraftforge.registries.RegistryObject;
import pokecube.world.PokecubeWorld;

public class FoliagePlacerTypes
{
    public static final RegistryObject<FoliagePlacerType<PalmFoliagePlacer>> PALM_FOLIAGE_PLACER = PokecubeWorld.FOLIAGE_PLACERS
            .register("palm_foliage_placer", () -> new FoliagePlacerType<>(PalmFoliagePlacer.CODEC));
    public static final RegistryObject<FoliagePlacerType<PyramidFoliagePlacer>> PYRAMID_FOLIAGE_PLACER = PokecubeWorld.FOLIAGE_PLACERS
            .register("pyramid_foliage_placer", () -> new FoliagePlacerType<>(PyramidFoliagePlacer.CODEC));
    public static final RegistryObject<FoliagePlacerType<RoundFoliagePlacer>> ROUND_FOLIAGE_PLACER = PokecubeWorld.FOLIAGE_PLACERS
            .register("round_foliage_placer", () -> new FoliagePlacerType<>(RoundFoliagePlacer.CODEC));

    public static void init()
    {};
}
