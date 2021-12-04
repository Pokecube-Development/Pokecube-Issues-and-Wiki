package pokecube.mixin;

import java.util.List;
import java.util.function.Predicate;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.core.RegistryAccess;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.StructureFeature;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.StructureFeatureConfiguration;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureManager;
import pokecube.core.PokecubeCore;

@Mixin(StructureFeature.class)
public class MixinStructure {
	@Inject(method = "generate", at = @At(value = "HEAD"), cancellable = true)
	public void onGenerate(RegistryAccess p_191133_, ChunkGenerator p_191134_, BiomeSource p_191135_,
			StructureManager p_191136_, long p_191137_, ChunkPos p_191138_, int p_191139_,
			StructureFeatureConfiguration p_191140_, FeatureConfiguration p_191141_, LevelHeightAccessor p_191142_,
			Predicate<Biome> p_191143_, final CallbackInfoReturnable<StructureStart<?>> cir) {
		final Object us = this;
		// We didn't extend it, so using this as a workaround cast instead.
		final StructureFeature<?> s = (StructureFeature<?>) us;
		final List<String> removedStructures = PokecubeCore.getConfig().removedStructures;
		if (removedStructures.contains(s.getFeatureName())
				|| removedStructures.contains(s.getRegistryName().toString())) {
			cir.setReturnValue(StructureStart.INVALID_START);
			return;
		}
	}
}
