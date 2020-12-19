package pokecube.mixin;

import java.util.Random;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.mojang.serialization.Codec;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.SectionPos;
import net.minecraft.world.ISeedReader;
import net.minecraft.world.gen.ChunkGenerator;
import net.minecraft.world.gen.feature.BlockStateFeatureConfig;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.LakesFeature;
import net.minecraft.world.gen.feature.structure.Structure;
import pokecube.core.PokecubeCore;

@Mixin(LakesFeature.class)
public abstract class MixinLakesFeature extends Feature<BlockStateFeatureConfig>
{

    public MixinLakesFeature(final Codec<BlockStateFeatureConfig> codec)
    {
        super(codec);
    }

    @Inject(//@formatter:off

            // We hook into the "generate" method
            method = "generate",

            // We want to look for where it assigns the pos = pos.down(4), hence looking for BlockPos;down(I)
            at = @At(value = "INVOKE_ASSIGN", target = "Lnet/minecraft/util/math/BlockPos;down(I)Lnet/minecraft/util/math/BlockPos;"),

            // We will cancel and return false early if our check works,
            // the stock behaviour returns true only for standard villages
            cancellable = true
            )//@formatter:on
    private void checkForRSVillages(final ISeedReader world, final ChunkGenerator chunkGen, final Random random,
            final BlockPos blockPos, final BlockStateFeatureConfig config, final CallbackInfoReturnable<Boolean> cir)
    {
        if (!PokecubeCore.getConfig().lakeFeatureMixin) return;
        for (final Structure<?> village : Structure.field_236384_t_)
            if (world.func_241827_a(SectionPos.from(blockPos), village).findAny().isPresent())
            {
                cir.setReturnValue(false);
                break;
            }
    }

}
