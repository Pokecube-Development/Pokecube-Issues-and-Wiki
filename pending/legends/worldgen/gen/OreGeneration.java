package pokecube.legends.worldgen.gen;

import java.util.ArrayList;

import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.WorldGenRegistries;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.GenerationStage;
import net.minecraft.world.gen.feature.ConfiguredFeature;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.IFeatureConfig;
import net.minecraft.world.gen.feature.OreFeatureConfig;
import net.minecraftforge.common.world.BiomeGenerationSettingsBuilder;
import net.minecraftforge.event.world.BiomeLoadingEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import pokecube.legends.init.BlockInit;
import pokecube.legends.Reference;

@Mod.EventBusSubscriber
public class OreGeneration {
	
	private static final ArrayList<ConfiguredFeature<?, ?>> overworldOres = new ArrayList<ConfiguredFeature<?,?>>();
	
	public static void registerOres() {
		
		//Overworld Ores
		overworldOres.add(register("legends_rubyore", Feature.ORE.withConfiguration(new OreFeatureConfig(
				OreFeatureConfig.FillerBlockType.BASE_STONE_OVERWORLD, BlockInit.RUBY_ORE.get().getDefaultState(), 3))
				.range(25).square()
				.func_242731_b(12)));
	}
	
	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public static void gen(BiomeLoadingEvent event) {
		BiomeGenerationSettingsBuilder generator = event.getGeneration();
		if(event.getCategory().equals(Biome.Category.FOREST)) {
			for(ConfiguredFeature<?, ?> ore : overworldOres) {
				if(ore != null) generator.withFeature(GenerationStage.Decoration.UNDERGROUND_ORES, ore);
			}
		}
	}
	
	private static <FC extends IFeatureConfig> ConfiguredFeature<FC, ?> register(String name, ConfiguredFeature<FC, ?> configureFeature) {
		return Registry.register(WorldGenRegistries.CONFIGURED_FEATURE, Reference.ID + ":" + name, configureFeature);
	}

}
