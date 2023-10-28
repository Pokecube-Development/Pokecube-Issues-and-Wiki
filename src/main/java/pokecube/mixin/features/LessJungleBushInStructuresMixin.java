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
import net.minecraft.world.level.levelgen.feature.TreeFeature;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;
import net.minecraft.world.level.levelgen.feature.foliageplacers.BushFoliagePlacer;
import pokecube.mixin.accessors.WorldGenRegionAccessor;
import pokecube.world.WorldgenTags;

@Mixin(TreeFeature.class)
public class LessJungleBushInStructuresMixin
{
    @Inject(method = "place(Lnet/minecraft/world/level/levelgen/feature/FeaturePlaceContext;)Z", at = @At(value = "HEAD"), cancellable = true)
    private void pokecube$lessJungleBushInStructures(FeaturePlaceContext<TreeConfiguration> context,
            CallbackInfoReturnable<Boolean> cir)
    {
        // Detect jungle bush like tree
        if (context.level() instanceof WorldGenRegionAccessor accessor
                && context.config().foliagePlacer instanceof BushFoliagePlacer
                && context.config().minimumSize.minClippedHeight().orElse(0) < 2)
        {
            // Rate for removal of bush
            if (context.random().nextFloat() < 0.85f)
            {
                Registry<ConfiguredStructureFeature<?, ?>> configuredStructureFeatureRegistry = context.level()
                        .registryAccess().registryOrThrow(Registry.CONFIGURED_STRUCTURE_FEATURE_REGISTRY);
                StructureFeatureManager structureFeatureManager = accessor.getStructureFeatureManager();

                for (Holder<ConfiguredStructureFeature<?, ?>> configuredStructureFeature : configuredStructureFeatureRegistry
                        .getOrCreateTag(WorldgenTags.LESS_JUNGLE_BUSHES))
                {
                    if (structureFeatureManager.getStructureAt(context.origin(), configuredStructureFeature.value())
                            .isValid())
                    {
                        cir.setReturnValue(false);
                        return;
                    }
                }
            }
        }
    }
}
