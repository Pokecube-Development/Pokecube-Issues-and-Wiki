package pokecube.mixin;

import java.util.List;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.util.SharedSeedRandom;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.registry.DynamicRegistries;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.provider.BiomeProvider;
import net.minecraft.world.gen.ChunkGenerator;
import net.minecraft.world.gen.feature.IFeatureConfig;
import net.minecraft.world.gen.feature.structure.Structure;
import net.minecraft.world.gen.feature.structure.StructureStart;
import net.minecraft.world.gen.feature.template.TemplateManager;
import net.minecraft.world.gen.settings.StructureSeparationSettings;
import pokecube.core.PokecubeCore;

@Mixin(Structure.class)
public class MixinStructure
{

    @Inject(method = "generate", at = @At(value = "HEAD"), cancellable = true)
    public void onGenerate(final DynamicRegistries p_242785_1_, final ChunkGenerator p_242785_2_,
            final BiomeProvider p_242785_3_, final TemplateManager p_242785_4_, final long p_242785_5_,
            final ChunkPos p_242785_7_, final Biome p_242785_8_, final int p_242785_9_,
            final SharedSeedRandom p_242785_10_, final StructureSeparationSettings p_242785_11_,
            final IFeatureConfig p_242785_12_, final CallbackInfoReturnable<StructureStart<?>> cir)
    {
        final Object us = this;
        // We didn't extend it, so using this as a workaround cast instead.
        final Structure<?> s = (Structure<?>) us;
        final List<String> removedStructures = PokecubeCore.getConfig().removedStructures;
        if (removedStructures.contains(s.getFeatureName()) || removedStructures.contains(s.getRegistryName()
                .toString()))
        {
            cir.setReturnValue(StructureStart.INVALID_START);
            return;
        }
    }
}
