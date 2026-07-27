package pokecube.mixin.features;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.BlockStateConfiguration;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import pokecube.core.PokecubeCore;
import pokecube.world.WorldgenTags;
import pokecube.world.utils.GeneralUtils;

import java.util.List;

@SuppressWarnings("deprecation")
@Mixin(net.minecraft.world.level.levelgen.feature.LakeFeature.class)
public class NoLakesInStructuresMixin
{

    @Inject(method = "place(Lnet/minecraft/world/level/levelgen/feature/FeaturePlaceContext;)Z", at = @At(value = "HEAD"), cancellable = true)
    private void pokecube$noLakesInStructures(FeaturePlaceContext<BlockStateConfiguration> context,
            CallbackInfoReturnable<Boolean> cir)
    {
        if (!(context.level() instanceof WorldGenRegion worldGenRegion)||!PokecubeCore.getConfig().lakeFeatureMixin)
        {
            return;
        }

        Registry<Structure> structureRegistry = worldGenRegion.registryAccess().registry(Registries.STRUCTURE).get();

        List<StructureStart> structureStarts = GeneralUtils.inboundsValidStartsForAllStructure(worldGenRegion,
                context.origin(),
                struct -> structureRegistry.getHolderOrThrow(structureRegistry.getResourceKey(struct).get())
                        .is(WorldgenTags.NO_LAKES));

        if (!structureStarts.isEmpty())
        {
            cir.setReturnValue(false);
            return;
        }
    }
}
