package pokecube.legends.worldgen;

import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.BlockStateConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.ProbabilityFeatureConfiguration;
import net.minecraft.world.level.levelgen.surfacebuilders.SurfaceBuilder;
import net.minecraft.world.level.levelgen.surfacebuilders.SurfaceBuilderBaseConfiguration;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fmllegacy.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import pokecube.legends.Reference;
import pokecube.legends.worldgen.features.LakeFeature;
import pokecube.legends.worldgen.features.TaintedKelpFeature;
import pokecube.legends.worldgen.features.TaintedSeagrassFeature;
import pokecube.legends.worldgen.features.TemporalBambooFeature;
import pokecube.legends.worldgen.surface_builders.BlindingDeltasSurfaceBuilder;
import pokecube.legends.worldgen.surface_builders.MirageDesertSurfaceBuilder;
import pokecube.legends.worldgen.surface_builders.TaintedBarrensSurfaceBuilder;

public class WorldgenFeatures
{
    public static final DeferredRegister<SurfaceBuilder<?>> SURFACE_BUILDERS = DeferredRegister.create(
            ForgeRegistries.SURFACE_BUILDERS, Reference.ID);
    public static final DeferredRegister<Feature<?>> FEATURES = DeferredRegister.create(
            ForgeRegistries.FEATURES, Reference.ID);

    public static final RegistryObject<SurfaceBuilder<?>> MIRAGE_DESERT = WorldgenFeatures.SURFACE_BUILDERS.register("mirage_desert",
            () -> new MirageDesertSurfaceBuilder(SurfaceBuilderBaseConfiguration.CODEC));
    public static final RegistryObject<SurfaceBuilder<?>> BLINDING_DELTAS = WorldgenFeatures.SURFACE_BUILDERS.register("blinding_deltas",
            () -> new BlindingDeltasSurfaceBuilder(SurfaceBuilderBaseConfiguration.CODEC));
    public static final RegistryObject<SurfaceBuilder<?>> TAINTED_BARRENS = WorldgenFeatures.SURFACE_BUILDERS.register("tainted_barrens",
            () -> new TaintedBarrensSurfaceBuilder(SurfaceBuilderBaseConfiguration.CODEC));

    public static final RegistryObject<Feature<?>> LAKE = WorldgenFeatures.FEATURES.register("lake",
            () -> new LakeFeature(BlockStateConfiguration.CODEC));
    public static final RegistryObject<Feature<?>> TEMPORAL_BAMBOO = WorldgenFeatures.FEATURES.register("temporal_bamboo",
            () -> new TemporalBambooFeature(ProbabilityFeatureConfiguration.CODEC));
    public static final RegistryObject<Feature<?>> TAINTED_KELP= WorldgenFeatures.FEATURES.register("tainted_kelp",
            () -> new TaintedKelpFeature(NoneFeatureConfiguration.CODEC));
    public static final RegistryObject<Feature<?>> TAINTED_SEAGRASS = WorldgenFeatures.FEATURES.register("tainted_seagrass",
            () -> new TaintedSeagrassFeature(ProbabilityFeatureConfiguration.CODEC));

    public static void init(final IEventBus bus)
    {
        WorldgenFeatures.SURFACE_BUILDERS.register(bus);
        WorldgenFeatures.FEATURES.register(bus);
    }
}
