package pokecube.mixin.features;

import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.core.Registry;
import net.minecraft.world.level.levelgen.feature.DeltaFeature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.DeltaFeatureConfiguration;
import net.minecraft.world.level.levelgen.structure.Structure;
import pokecube.world.utils.GeneralUtils;
import pokecube.world.WorldgenTags;

import java.util.List;

@Mixin(DeltaFeature.class)
public class NoDeltasInStructuresMixin
{

    @Inject(method = "place(Lnet/minecraft/world/level/levelgen/feature/FeaturePlaceContext;)Z", at = @At(value = "HEAD"), cancellable = true)
    private void pokecube$noDeltasInStructures(FeaturePlaceContext<DeltaFeatureConfiguration> context,
            CallbackInfoReturnable<Boolean> cir)
    {
        if (!(context.level() instanceof WorldGenRegion worldGenRegion)) {
            return;
        }

        Registry<Structure> structureRegistry = worldGenRegion.registryAccess().registry(Registries.STRUCTURE).get();

        List<StructureStart> structureStarts = GeneralUtils.inboundsValidStartsForAllStructure(
                worldGenRegion,
                context.origin(),
                struct -> structureRegistry.getHolderOrThrow(structureRegistry.getResourceKey(struct).get()).is(WorldgenTags.NO_BASALT));

        if (!structureStarts.isEmpty()) {
            cir.setReturnValue(false);
        }
    }
}
