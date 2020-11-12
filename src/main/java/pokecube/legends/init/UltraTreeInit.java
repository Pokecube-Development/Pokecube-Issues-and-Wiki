package pokecube.legends.init;

import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.WorldGenRegistries;
import net.minecraft.world.gen.blockstateprovider.SimpleBlockStateProvider;
import net.minecraft.world.gen.feature.BaseTreeFeatureConfig;
import net.minecraft.world.gen.feature.ConfiguredFeature;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.FeatureSpread;
import net.minecraft.world.gen.feature.FeatureSpreadConfig;
import net.minecraft.world.gen.feature.IFeatureConfig;
import net.minecraft.world.gen.feature.TwoLayerFeature;
import net.minecraft.world.gen.foliageplacer.BlobFoliagePlacer;
import net.minecraft.world.gen.placement.Placement;
import net.minecraft.world.gen.trunkplacer.StraightTrunkPlacer;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import pokecube.legends.Reference;
import pokecube.legends.handlers.DimensionTreeFeature;
import pokecube.legends.init.BlockInit;

public class UltraTreeInit {

	public static final DeferredRegister<Feature<?>> FEATURES = DeferredRegister.create(ForgeRegistries.FEATURES, Reference.ID);
	
	public static final RegistryObject<Feature<BaseTreeFeatureConfig>> ULTRA_TREE_CONFIG = FEATURES.register(
            "ultra_tree_config", () -> new DimensionTreeFeature(BaseTreeFeatureConfig.CODEC));
	
	 public static void registerConfiguredFeatures() {
		 register("ultra_tree1", ULTRA_TREE_CONFIG.get().withConfiguration(
            (new BaseTreeFeatureConfig.Builder(
                    new SimpleBlockStateProvider(BlockInit.ULTRA_LOGUB01.get().getDefaultState()),
                    new SimpleBlockStateProvider(BlockInit.ULTRA_LEAVEUB01.get().getDefaultState()),
                    new BlobFoliagePlacer(FeatureSpread.func_242252_a(2), FeatureSpread.func_242252_a(0), 2),
                    new StraightTrunkPlacer(10, 2, 2),
                    new TwoLayerFeature(1, 0, 1)))
                    .setIgnoreVines().build()).withPlacement(Placement.COUNT_MULTILAYER.configure(new FeatureSpreadConfig(8))));
	 }
	 
	 private static <FC extends IFeatureConfig> ConfiguredFeature<FC, ?> register(String name, ConfiguredFeature<FC, ?> feature) {
	        return Registry.register(WorldGenRegistries.CONFIGURED_FEATURE, new ResourceLocation(Reference.ID, name), feature);
	    }
	 
}
