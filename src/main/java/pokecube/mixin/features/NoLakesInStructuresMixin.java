package pokecube.mixin.features;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.BlockStateConfiguration;
import net.minecraft.world.level.levelgen.structure.Structure;
import pokecube.mixin.accessors.WorldGenRegionAccessor;
import pokecube.world.WorldgenTags;
import thut.lib.RegHelper;

@SuppressWarnings("deprecation")
@Mixin(net.minecraft.world.level.levelgen.feature.LakeFeature.class)
public class NoLakesInStructuresMixin
{

    @Inject(method = "place(Lnet/minecraft/world/level/levelgen/feature/FeaturePlaceContext;)Z", at = @At(value = "HEAD"), cancellable = true)
    private void repurposedstructures_noLakesInStructures(FeaturePlaceContext<BlockStateConfiguration> context,
            CallbackInfoReturnable<Boolean> cir)
    {
        if (!(context.level() instanceof WorldGenRegionAccessor accessor))
        {
            return;
        }

        Registry<Structure> configuredStructureFeatureRegistry = context.level().registryAccess()
                .registryOrThrow(RegHelper.STRUCTURE_REGISTRY);
        StructureManager structureFeatureManager = accessor.getStructureManager();

        for (Holder<Structure> configuredStructureFeature : configuredStructureFeatureRegistry
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
