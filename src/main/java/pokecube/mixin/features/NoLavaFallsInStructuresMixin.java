package pokecube.mixin.features;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.level.StructureFeatureManager;
import net.minecraft.world.level.levelgen.feature.ConfiguredStructureFeature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.SpringFeature;
import net.minecraft.world.level.levelgen.feature.configurations.SpringConfiguration;
import pokecube.world.WorldgenTags;

@Mixin(SpringFeature.class)
public class NoLavaFallsInStructuresMixin
{

    @Inject(method = "place(Lnet/minecraft/world/level/levelgen/feature/FeaturePlaceContext;)Z", at = @At(value = "HEAD"), cancellable = true)
    private void repurposedstructures_noLavaInStructures(FeaturePlaceContext<SpringConfiguration> context,
            CallbackInfoReturnable<Boolean> cir)
    {
        if (!(context.level() instanceof WorldGenRegion))
        {
            return;
        }

        if (context.config().state.is(FluidTags.LAVA))
        {
            BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();
            for (Direction face : Direction.Plane.HORIZONTAL)
            {
                mutable.set(context.origin()).move(face);
                Registry<ConfiguredStructureFeature<?, ?>> configuredStructureFeatureRegistry = context.level()
                        .registryAccess().registryOrThrow(Registry.CONFIGURED_STRUCTURE_FEATURE_REGISTRY);
                StructureFeatureManager structureFeatureManager = ((WorldGenRegionAccessor) context.level())
                        .getStructureFeatureManager();

                for (Holder<ConfiguredStructureFeature<?, ?>> configuredStructureFeature : configuredStructureFeatureRegistry
                        .getOrCreateTag(WorldgenTags.NO_LAVAFALLS))
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
