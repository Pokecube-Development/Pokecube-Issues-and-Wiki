package pokecube.world.gen.features;

import net.minecraft.core.Registry;
import net.minecraft.data.worldgen.features.OreFeatures;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.VerticalAnchor;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.OreConfiguration;
import net.minecraft.world.level.levelgen.placement.*;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.BiomeLoadingEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.RegistryObject;
import pokecube.core.PokecubeCore;
import pokecube.core.PokecubeItems;
import pokecube.core.blocks.berries.BerryGenManager;
import pokecube.world.PokecubeWorld;
import pokecube.world.gen.features.register.PlacedFeatureHolder;
import thut.api.terrain.BiomeDatabase;

import java.util.List;
import java.util.function.Predicate;

public class FeaturesInit
{
    public static void init(IEventBus bus)
    {
        MinecraftForge.EVENT_BUS.addListener(EventPriority.HIGH, FeaturesInit::onBiomeLoading);
    }

    final static List<OreConfiguration.TargetBlockState> getOres()
    {
        return List.of(
                OreConfiguration.target(OreFeatures.STONE_ORE_REPLACEABLES,
                        PokecubeItems.FOSSIL_ORE.get().defaultBlockState()),
                OreConfiguration.target(OreFeatures.DEEPSLATE_ORE_REPLACEABLES,
                        PokecubeItems.DEEPSLATE_FOSSIL_ORE.get().defaultBlockState()));
    };

    public static final RegistryObject<ConfiguredFeature<?, ?>> ORE_FOSSIL_SMALL_FEATURE;
    public static final RegistryObject<ConfiguredFeature<?, ?>> ORE_FOSSIL_LARGE_FEATURE;
    public static final RegistryObject<ConfiguredFeature<?, ?>> ORE_FOSSIL_BURIED_FEATURE;

    public static final RegistryObject<PlacedFeature> PLACED_SMALL_FOSSIL;
    public static final RegistryObject<PlacedFeature> PLACED_LARGE_FOSSIL;
    public static final RegistryObject<PlacedFeature> PLACED_BURIED_FOSSIL;

    static
    {
        ORE_FOSSIL_SMALL_FEATURE = PokecubeWorld.CONFIGURED_FEATURES.register("fossil_ore",
                () -> new ConfiguredFeature<>(Feature.ORE, new OreConfiguration(getOres(), 4, 0.5f)));
        ORE_FOSSIL_LARGE_FEATURE = PokecubeWorld.CONFIGURED_FEATURES.register("fossil_ore_large",
                () -> new ConfiguredFeature<>(Feature.ORE, new OreConfiguration(getOres(), 12, 0.7f)));
        ORE_FOSSIL_BURIED_FEATURE = PokecubeWorld.CONFIGURED_FEATURES.register("fossil_ore_buried",
                () -> new ConfiguredFeature<>(Feature.ORE, new OreConfiguration(getOres(), 8, 1.0f)));

        PLACED_SMALL_FOSSIL = PokecubeWorld.PLACED_FEATURES.register("fossil_ore",
                () -> new PlacedFeature(ORE_FOSSIL_SMALL_FEATURE.getHolder().get(),
                        List.of(CountPlacement.of(5), InSquarePlacement.spread(), HeightRangePlacement
                                .triangle(VerticalAnchor.aboveBottom(-80), VerticalAnchor.aboveBottom(380)),
                                BiomeFilter.biome())));
        PLACED_LARGE_FOSSIL = PokecubeWorld.PLACED_FEATURES.register("fossil_ore_large",
                () -> new PlacedFeature(ORE_FOSSIL_LARGE_FEATURE.getHolder().get(),
                        List.of(RarityFilter.onAverageOnceEvery(8), InSquarePlacement.spread(), HeightRangePlacement
                                .triangle(VerticalAnchor.aboveBottom(-80), VerticalAnchor.aboveBottom(380)),
                                BiomeFilter.biome())));
        PLACED_BURIED_FOSSIL = PokecubeWorld.PLACED_FEATURES.register("fossil_ore_buried",
                () -> new PlacedFeature(ORE_FOSSIL_BURIED_FEATURE.getHolder().get(),
                        List.of(CountPlacement.of(3), InSquarePlacement.spread(), HeightRangePlacement
                                .triangle(VerticalAnchor.aboveBottom(-80), VerticalAnchor.aboveBottom(380)),
                                BiomeFilter.biome())));
    }

    private static final Predicate<ResourceKey<Biome>> ores_biome_check = k ->
            PokecubeCore.getConfig().generateFossils
            && (BiomeDatabase.contains(k, "mesa") || BiomeDatabase.contains(k, "ocean")
                    || BiomeDatabase.contains(k, "river") || BiomeDatabase.contains(k, "sandy"));

    private static void onBiomeLoading(BiomeLoadingEvent event)
    {
        PokecubeCore.LOGGER.info(event);
        ResourceKey<Biome> key = ResourceKey.create(Registry.BIOME_REGISTRY, event.getName());
        if (ores_biome_check.test(key))
        {
            PokecubeCore.LOGGER.info("Adding Fossils to " + event.getName());
            event.getGeneration().addFeature(GenerationStep.Decoration.UNDERGROUND_ORES,
                    PLACED_SMALL_FOSSIL.getHolder().get());
            event.getGeneration().addFeature(GenerationStep.Decoration.UNDERGROUND_ORES,
                    PLACED_LARGE_FOSSIL.getHolder().get());
            event.getGeneration().addFeature(GenerationStep.Decoration.UNDERGROUND_ORES,
                    PLACED_BURIED_FOSSIL.getHolder().get());
        }

        if (PokecubeCore.getConfig().generateBerries) BerryGenManager.list.locations.forEach(config -> {
            if (config.matches(event))
            {
                PokecubeCore.LOGGER.info("Adding " + config.placement + " to " + event.getName());
                event.getGeneration().addFeature(GenerationStep.Decoration.VEGETAL_DECORATION,
                        new PlacedFeatureHolder(config.placement));
            }
        });
    }

}
