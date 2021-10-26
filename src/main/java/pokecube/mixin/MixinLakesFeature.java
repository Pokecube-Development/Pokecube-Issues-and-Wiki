package pokecube.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.mojang.serialization.Codec;

import net.minecraft.core.SectionPos;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.LakeFeature;
import net.minecraft.world.level.levelgen.feature.StructureFeature;
import net.minecraft.world.level.levelgen.feature.configurations.BlockStateConfiguration;
import pokecube.core.PokecubeCore;

@Mixin(LakeFeature.class)
public abstract class MixinLakesFeature extends Feature<BlockStateConfiguration>
{

    public MixinLakesFeature(final Codec<BlockStateConfiguration> codec)
    {
        super(codec);
    }

    @Inject(//@formatter:off

            // We hook into the "generate" method
            method = "place",

            // We want to look for where it assigns the pos = pos.down(4), hence looking for BlockPos;down(I)
            at = @At(value = "INVOKE_ASSIGN", target = "Lnet/minecraft/core/BlockPos;below(I)Lnet/minecraft/core/BlockPos;"),

            // We will cancel and return false early if our check works,
            // the stock behaviour returns true only for standard villages
            cancellable = true
            )//@formatter:on
    private void checkForRSVillages(final FeaturePlaceContext<?> context, final CallbackInfoReturnable<Boolean> cir)
    {
        if (!PokecubeCore.getConfig().lakeFeatureMixin) return;
        for (final StructureFeature<?> village : StructureFeature.NOISE_AFFECTING_FEATURES)
            if (context.level().startsForFeature(SectionPos.of(context.origin()), village).findAny().isPresent())
            {
                cir.setReturnValue(false);
                break;
            }
    }

}
