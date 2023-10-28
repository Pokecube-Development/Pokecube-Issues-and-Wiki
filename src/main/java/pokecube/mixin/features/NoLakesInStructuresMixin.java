package pokecube.mixin.features;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.world.level.StructureFeatureManager;
import net.minecraft.world.level.levelgen.feature.ConfiguredStructureFeature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.BlockStateConfiguration;
import pokecube.mixin.accessors.WorldGenRegionAccessor;
import pokecube.world.WorldgenTags;

@SuppressWarnings("deprecation")
@Mixin(net.minecraft.world.level.levelgen.feature.LakeFeature.class)
public class NoLakesInStructuresMixin
{

    @Inject(method = "place(Lnet/minecraft/world/level/levelgen/feature/FeaturePlaceContext;)Z", at = @At(value = "HEAD"), cancellable = true)
    private void pokecube$noLakesInStructures(FeaturePlaceContext<BlockStateConfiguration> context,
            CallbackInfoReturnable<Boolean> cir)
    {
        if (!(context.level() instanceof WorldGenRegionAccessor accessor))
        {
            return;
        }

        Registry<ConfiguredStructureFeature<?, ?>> configuredStructureFeatureRegistry = context.level().registryAccess()
                .registryOrThrow(Registry.CONFIGURED_STRUCTURE_FEATURE_REGISTRY);
        StructureFeatureManager structureFeatureManager = accessor.getStructureFeatureManager();

        for (Holder<ConfiguredStructureFeature<?, ?>> configuredStructureFeature : configuredStructureFeatureRegistry
                .getOrCreateTag(WorldgenTags.NO_LAKES))
        {
            if (structureFeatureManager.getStructureAt(context.origin(), configuredStructureFeature.value()).isValid())
            {
                cir.setReturnValue(false);
                return;
            }
        }
    }
}
