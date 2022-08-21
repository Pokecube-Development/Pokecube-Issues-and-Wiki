package pokecube.mixin.features;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.StructureFeatureManager;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.levelgen.feature.BasaltColumnsFeature;
import net.minecraft.world.level.levelgen.feature.ConfiguredStructureFeature;
import pokecube.api.PokecubeAPI;
import pokecube.world.WorldgenTags;

@Mixin(BasaltColumnsFeature.class)
public class NoBasaltColumnsInStructuresMixin
{

    @Inject(method = "canPlaceAt(Lnet/minecraft/world/level/LevelAccessor;ILnet/minecraft/core/BlockPos$MutableBlockPos;)Z", at = @At(value = "HEAD"), cancellable = true)
    private static void repurposedstructures_noBasaltColumnsInStructures(LevelAccessor levelAccessor, int seaLevel,
            BlockPos.MutableBlockPos mutableBlockPos, CallbackInfoReturnable<Boolean> cir)
    {
        if (!(levelAccessor instanceof WorldGenRegionAccessor accessor))
        {
            return;
        }

        SectionPos sectionPos = SectionPos.of(mutableBlockPos);
        if (!levelAccessor.getChunk(sectionPos.x(), sectionPos.z()).getStatus()
                .isOrAfter(ChunkStatus.STRUCTURE_REFERENCES))
        {
            PokecubeAPI.LOGGER.warn(
                    "Repurposed Structures: Detected a mod with a broken basalt columns configuredfeature that is trying to place blocks outside the 3x3 safe chunk area for features. Find the broken mod and report to them to fix the placement of their basalt columns feature.");
            return;
        }

        Registry<ConfiguredStructureFeature<?, ?>> configuredStructureFeatureRegistry = levelAccessor.registryAccess()
                .registryOrThrow(Registry.CONFIGURED_STRUCTURE_FEATURE_REGISTRY);
        StructureFeatureManager structureFeatureManager = accessor.getStructureFeatureManager();
        for (Holder<ConfiguredStructureFeature<?, ?>> configuredStructureFeature : configuredStructureFeatureRegistry
                .getOrCreateTag(WorldgenTags.NO_BASALT))
        {
            if (structureFeatureManager.getStructureAt(mutableBlockPos, configuredStructureFeature.value()).isValid())
            {
                cir.setReturnValue(false);
                return;
            }
        }
    }
}
