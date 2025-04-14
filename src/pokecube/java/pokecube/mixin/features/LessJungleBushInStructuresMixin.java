package pokecube.mixin.features;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.TreeFeature;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;
import net.minecraft.world.level.levelgen.feature.foliageplacers.BushFoliagePlacer;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import pokecube.world.WorldgenTags;
import pokecube.world.utils.GeneralUtils;

import java.util.List;

@Mixin(TreeFeature.class)
public class LessJungleBushInStructuresMixin
{
    @Inject(method = "place(Lnet/minecraft/world/level/levelgen/feature/FeaturePlaceContext;)Z", at = @At(value = "HEAD"), cancellable = true)
    private void pokecube$lessJungleBushInStructures(FeaturePlaceContext<TreeConfiguration> context,
            CallbackInfoReturnable<Boolean> cir)
    {        // Detect jungle bush like tree
        if (context.level() instanceof WorldGenRegion worldGenRegion
                && context.config().foliagePlacer instanceof BushFoliagePlacer
                && context.config().minimumSize.minClippedHeight().orElse(0) < 2)
        {
            // Rate for removal of bush
            if (context.random().nextFloat() < 0.85f)
            {
                Registry<Structure> structureRegistry = worldGenRegion.registryAccess().registry(Registries.STRUCTURE)
                        .get();

                List<StructureStart> structureStarts = GeneralUtils.inboundsValidStartsForAllStructure(worldGenRegion,
                        context.origin(),
                        struct -> structureRegistry.getHolderOrThrow(structureRegistry.getResourceKey(struct).get())
                                .is(WorldgenTags.LESS_JUNGLE_BUSHES));

                if (!structureStarts.isEmpty())
                {
                    cir.setReturnValue(false);
                }
            }
        }
    }
}
