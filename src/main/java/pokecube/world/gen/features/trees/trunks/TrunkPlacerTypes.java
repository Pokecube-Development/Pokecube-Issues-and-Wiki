package pokecube.world.gen.features.trees.trunks;

import net.minecraft.world.level.levelgen.feature.trunkplacers.TrunkPlacerType;
import net.minecraftforge.registries.RegistryObject;
import pokecube.world.PokecubeWorld;

public class TrunkPlacerTypes
{
    public static final RegistryObject<TrunkPlacerType<PalmTrunkPlacer>> PALM_TRUNK_PLACER = PokecubeWorld.TRUNK_PLACERS
            .register("palm_trunk_placer", () -> new TrunkPlacerType<>(PalmTrunkPlacer.CODEC));

    public static void init()
    {};
}
