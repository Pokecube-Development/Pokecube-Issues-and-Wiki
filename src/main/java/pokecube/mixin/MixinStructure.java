package pokecube.mixin;

import java.util.List;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.core.RegistryAccess;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.feature.StructureFeature;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.StructureFeatureConfiguration;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureManager;
import pokecube.core.PokecubeCore;

@Mixin(StructureFeature.class)
public class MixinStructure
{

    @Inject(method = "generate", at = @At(value = "HEAD"), cancellable = true)
    public void onGenerate(final RegistryAccess p_242785_1_, final ChunkGenerator p_242785_2_,
            final BiomeSource p_242785_3_, final StructureManager p_242785_4_, final long p_242785_5_,
            final ChunkPos p_242785_7_, final Biome p_242785_8_, final int p_242785_9_,
            final WorldgenRandom p_242785_10_, final StructureFeatureConfiguration p_242785_11_,
            final FeatureConfiguration p_242785_12_, final CallbackInfoReturnable<StructureStart<?>> cir)
    {
        final Object us = this;
        // We didn't extend it, so using this as a workaround cast instead.
        final StructureFeature<?> s = (StructureFeature<?>) us;
        final List<String> removedStructures = PokecubeCore.getConfig().removedStructures;
        if (removedStructures.contains(s.getFeatureName()) || removedStructures.contains(s.getRegistryName()
                .toString()))
        {
            cir.setReturnValue(StructureStart.INVALID_START);
            return;
        }
    }
}
