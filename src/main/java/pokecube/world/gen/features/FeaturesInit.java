package pokecube.world.gen.features;

import com.google.common.collect.ImmutableList;
import net.minecraft.core.Registry;
import net.minecraft.data.worldgen.features.OreFeatures;
import net.minecraft.data.worldgen.placement.PlacementUtils;
import net.minecraft.data.worldgen.placement.VegetationPlacements;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.BiomeTags;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
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
import pokecube.core.blocks.berries.BerryTree;
import pokecube.world.PokecubeWorld;
import thut.api.terrain.BiomeDatabase;

import java.util.List;
import java.util.function.Predicate;

import static pokecube.legends.worldgen.trees.Trees.TREE_FEATURES;

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

    private static final RegistryObject<ConfiguredFeature<?, ?>> ORE_FOSSIL_SMALL_FEATURE;
    private static final RegistryObject<ConfiguredFeature<?, ?>> ORE_FOSSIL_LARGE_FEATURE;
    private static final RegistryObject<ConfiguredFeature<?, ?>> ORE_FOSSIL_BURIED_FEATURE;

    public static final RegistryObject<PlacedFeature> PLACED_SMALL_FOSSIL;
    public static final RegistryObject<PlacedFeature> PLACED_LARGE_FOSSIL;
    public static final RegistryObject<PlacedFeature> PLACED_BURIED_FOSSIL;

    public static RegistryObject<ConfiguredFeature<?, ?>> TREE_LEPPA_FEATURE;
    public static final RegistryObject<PlacedFeature> PLACED_TREE_LEPPA;
    public static RegistryObject<ConfiguredFeature<?, ?>> TREE_NANAB_FEATURE;
    public static final RegistryObject<PlacedFeature> PLACED_TREE_NANAB;

    public static List<PlacementModifier> treePlacement(PlacementModifier modifier) {
        PokecubeCore.LOGGER.info("Generating Berry Trees Placement");
        return treePlacementBase(modifier).add(BlockPredicateFilter.forPredicate(BerryTree.BERRY_TREE_PREDICATE)).build();
    }

    private static ImmutableList.Builder<PlacementModifier> treePlacementBase(PlacementModifier modifier) {
        return ImmutableList.<PlacementModifier>builder().add(modifier).add(InSquarePlacement.spread()).add(VegetationPlacements.TREE_THRESHOLD)
                .add(PlacementUtils.HEIGHTMAP_OCEAN_FLOOR).add(BiomeFilter.biome());
    }

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

        TREE_LEPPA_FEATURE = TREE_FEATURES.register("leppa_tree",
                () -> new ConfiguredFeature<>(Feature.TREE, BerryTree.getLeppaTree().build()));
        PLACED_TREE_LEPPA = PokecubeWorld.PLACED_FEATURES.register("leppa_tree",
                () -> new PlacedFeature(TREE_LEPPA_FEATURE.getHolder().get(),
                        treePlacement(PlacementUtils.countExtra(0, 0.1F, 1))));

        TREE_NANAB_FEATURE = TREE_FEATURES.register("nanab_tree",
                () -> new ConfiguredFeature<>(Feature.TREE, BerryTree.getNanabTree().build()));
        PLACED_TREE_NANAB = PokecubeWorld.PLACED_FEATURES.register("nanab_tree",
                () -> new PlacedFeature(TREE_NANAB_FEATURE.getHolder().get(),
                        treePlacement(PlacementUtils.countExtra(0, 0.1F, 1))));
    }

    private static final Predicate<ResourceKey<Biome>> ores_biome_check = k ->
            PokecubeCore.getConfig().generateFossils
            && (BiomeDatabase.contains(k, "mesa") || BiomeDatabase.contains(k, "ocean")
                    || BiomeDatabase.contains(k, "river") || BiomeDatabase.contains(k, "sandy"));

    private static final Predicate<ResourceKey<Biome>> leppa_trees_biome_check = k ->
            PokecubeCore.getConfig().generateBerryTrees && PokecubeCore.getConfig().generateLeppaBerryTrees
                    && Biomes.FLOWER_FOREST.equals(k);
    private static final Predicate<ResourceKey<Biome>> nanab_trees_biome_check = k ->
            PokecubeCore.getConfig().generateBerryTrees && PokecubeCore.getConfig().generateNanabBerryTrees
                    && BiomeTags.IS_BEACH.equals(k);

    private static void onBiomeLoading(BiomeLoadingEvent event)
    {
        PokecubeCore.LOGGER.info(event);
        ResourceKey<Biome> key = ResourceKey.create(Registry.BIOME_REGISTRY, event.getName());
        if (ores_biome_check.test(key))
        {
            event.getGeneration().addFeature(GenerationStep.Decoration.UNDERGROUND_ORES,
                    PLACED_SMALL_FOSSIL.getHolder().get());
            event.getGeneration().addFeature(GenerationStep.Decoration.UNDERGROUND_ORES,
                    PLACED_LARGE_FOSSIL.getHolder().get());
            event.getGeneration().addFeature(GenerationStep.Decoration.UNDERGROUND_ORES,
                    PLACED_BURIED_FOSSIL.getHolder().get());
        }
        if (leppa_trees_biome_check.test(key))
        {
            event.getGeneration().addFeature(GenerationStep.Decoration.VEGETAL_DECORATION,
                    PLACED_TREE_LEPPA.getHolder().get());
        }
        if (nanab_trees_biome_check.test(key))
        {
            event.getGeneration().addFeature(GenerationStep.Decoration.VEGETAL_DECORATION,
                    PLACED_TREE_NANAB.getHolder().get());
        }
    }

}
