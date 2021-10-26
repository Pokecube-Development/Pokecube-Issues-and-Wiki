package pokecube.mixin;

import java.util.List;

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
    public void onGenerate(final RegistryAccess p_160465_, final ChunkGenerator p_160466_, final BiomeSource p_160467_,
            final StructureManager p_160468_, final long p_160469_, final ChunkPos p_160470_, final Biome p_160471_, final int p_160472_,
            final WorldgenRandom p_160473_, final StructureFeatureConfiguration p_160474_, final FeatureConfiguration p_160475_,
            final LevelHeightAccessor p_160476_, final CallbackInfoReturnable<StructureStart<?>> cir)
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
