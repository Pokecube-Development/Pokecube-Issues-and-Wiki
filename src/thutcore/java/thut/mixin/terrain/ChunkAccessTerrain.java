package thut.mixin.terrain;

import net.minecraft.core.Registry;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ImposterProtoChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.chunk.UpgradeData;
import net.minecraft.world.level.levelgen.blending.BlendingData;
import net.neoforged.neoforge.attachment.AttachmentType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import thut.api.level.terrain.CapabilityTerrain;

import javax.annotation.Nullable;

@Mixin(ChunkAccess.class)
public abstract class ChunkAccessTerrain
{
    @Shadow
    public abstract <T> T getData(AttachmentType<T> type);

    @Inject(method = "<init>", at = @At("RETURN"))
    private void onConstructor(ChunkPos chunkPos, UpgradeData upgradeData, LevelHeightAccessor levelHeightAccessor,
            Registry<Biome> biomeRegistry, long inhabitedTime, @Nullable LevelChunkSection[] sections,
            @Nullable BlendingData blendingData, CallbackInfo ci)
    {
        var ths = (Object) this;
        // these wrap a regular level chunk, so we don't need to apply here.
        if (ths instanceof ImposterProtoChunk) return;
        this.getData(CapabilityTerrain.TYPE_SAVE.get());
    }
}
