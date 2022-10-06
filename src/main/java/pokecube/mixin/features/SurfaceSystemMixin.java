package pokecube.mixin.features;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.chunk.BlockColumn;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.NoiseChunk;
import net.minecraft.world.level.levelgen.SurfaceRules;
import net.minecraft.world.level.levelgen.SurfaceSystem;
import net.minecraft.world.level.levelgen.WorldGenerationContext;
import pokecube.core.utils.InvokeHelper;
import pokecube.mixin.invokers.SurfaceRulesContextInvoker;
import pokecube.mixin.invokers.SurfaceSystemInvoker;
import pokecube.world.WorldgenTags;

@Mixin(SurfaceSystem.class)
public abstract class SurfaceSystemMixin
{
    @Inject(method = "buildSurface", locals = LocalCapture.CAPTURE_FAILSOFT, at = @At(value = "INVOKE", target = "Lnet/minecraft/core/Holder;is(Lnet/minecraft/resources/ResourceKey;)Z", ordinal = 0))
    private void onGetBiome(BiomeManager manager, Registry<Biome> registry, boolean unk, WorldGenerationContext context,
            final ChunkAccess chunk, NoiseChunk noiseChunk, SurfaceRules.RuleSource rules, CallbackInfo ci,
            final BlockPos.MutableBlockPos blockpos$mutableblockpos, final ChunkPos chunkpos, int i, int j,
            BlockColumn blockcolumn, SurfaceRules.Context surfacerules$context,
            SurfaceRules.SurfaceRule surfacerules$surfacerule, BlockPos.MutableBlockPos blockpos$mutableblockpos1,
            int k, int l, int i1, int j1, int k1, Holder<Biome> holder)
    {
        if (holder.is(WorldgenTags.IS_ERODED))
        {
            SurfaceSystemInvoker _this = InvokeHelper.cast(this);
            _this.invokeErodedBadlandsExtension(blockcolumn, i1, j1, k1, chunk);
        }
        if (holder.is(WorldgenTags.IS_ICEBERG))
        {
            SurfaceSystemInvoker _this = InvokeHelper.cast(this);
            SurfaceRulesContextInvoker _context = InvokeHelper.cast(surfacerules$context);
            _this.invokeFrozenOceanExtension(_context.invokeGetMinSurfaceLevel(), holder.value(), blockcolumn,
                    blockpos$mutableblockpos1, i1, j1, k1);
        }
    }
}
