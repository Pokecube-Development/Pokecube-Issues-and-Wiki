package pokecube.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;
import it.unimi.dsi.fastutil.objects.ObjectListIterator;
import net.minecraft.Util;
import net.minecraft.core.SectionPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureFeatureManager;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.Beardifier;
import net.minecraft.world.level.levelgen.feature.NoiseEffect;
import net.minecraft.world.level.levelgen.feature.StructureFeature;
import net.minecraft.world.level.levelgen.feature.structures.JigsawJunction;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.PoolElementStructurePiece;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import pokecube.core.database.worldgen.WorldgenHandler;
import pokecube.core.world.gen.jigsaw.CustomJigsawPiece;

@Mixin(Beardifier.class)
public class MixinBeardifier
{
    private static final int KERNEL_RADIUS = 12;
    private static final int KERNEL_SIZE = 24;
    private static final float[] KERNEL = Util.make(new float[13824], (p_158082_) -> {
        for (int i = 0; i < KERNEL_SIZE; ++i)
        {
            for (int j = 0; j < KERNEL_SIZE; ++j)
            {
                for (int k = 0; k < KERNEL_SIZE; ++k)
                {
                    p_158082_[i * KERNEL_SIZE * KERNEL_SIZE + j * KERNEL_SIZE + k] = (float) _computeBeardContribution(
                            j - KERNEL_RADIUS, k - KERNEL_RADIUS, i - KERNEL_RADIUS);
                }
            }
        }
    });

    protected ObjectList<StructurePiece> rigids2;
    protected ObjectList<JigsawJunction> junctions2;
    protected ObjectListIterator<StructurePiece> pieceIterator2;
    protected ObjectListIterator<JigsawJunction> junctionIterator2;

    @Inject(method = "<init>", at = @At(value = "RETURN"))
    protected void onConstructorEnd(StructureFeatureManager structureManager, ChunkAccess chunkAccess, CallbackInfo ci)
    {
        ChunkPos chunkpos = chunkAccess.getPos();

        this.junctions2 = new ObjectArrayList<>(32);
        this.rigids2 = new ObjectArrayList<>(10);

        for (StructureFeature<?> structurefeature : WorldgenHandler.HAS_BASE_OVERRIDES)
        {
            structureManager.startsForFeature(SectionPos.bottomOf(chunkAccess), structurefeature)
                    .forEach((p_158080_) ->
                    {
                        for (StructurePiece structurepiece : p_158080_.getPieces())
                        {
                            if (structurepiece.isCloseToChunk(chunkpos, KERNEL_RADIUS)
                                    && structurepiece instanceof PoolElementStructurePiece poole
                                    && poole.getElement() instanceof CustomJigsawPiece piece
                                    && piece.opts.base_override)
                            {
                                this.rigids2.add(poole);
                            }
                        }
                    });
        }
        this.pieceIterator2 = this.rigids2.iterator();
        this.junctionIterator2 = this.junctions2.iterator();
    }

    @Inject(method = "calculateNoise", at = @At(value = "RETURN"), cancellable = true)
    public void onCalculateNoise(int p_188452_, int p_188453_, int p_188454_, CallbackInfoReturnable<Double> ci)
    {
        double d0 = ci.getReturnValueD();
        while (this.pieceIterator2.hasNext())
        {
            StructurePiece structurepiece = this.pieceIterator2.next();
            BoundingBox boundingbox = structurepiece.getBoundingBox();
            int i = Math.max(0, Math.max(boundingbox.minX() - p_188452_, p_188452_ - boundingbox.maxX()));
            int j = p_188453_ - (boundingbox.minY() + (structurepiece instanceof PoolElementStructurePiece
                    ? ((PoolElementStructurePiece) structurepiece).getGroundLevelDelta()
                    : 0));
            int k = Math.max(0, Math.max(boundingbox.minZ() - p_188454_, p_188454_ - boundingbox.maxZ()));
            NoiseEffect noiseeffect = structurepiece.getNoiseEffect();
            if (noiseeffect == NoiseEffect.BURY)
            {
                d0 += _getBuryContribution(i, j, k);
            }
            else if (noiseeffect == NoiseEffect.BEARD)
            {
                d0 += _getBeardContribution(i, j, k) * 0.8D;
            }
        }
        this.pieceIterator2.back(this.rigids2.size());
        ci.setReturnValue(d0);
    }

    // The blow is copied from Beardifier
    private static double _getBuryContribution(int p_158084_, int p_158085_, int p_158086_)
    {
        double d0 = Mth.length((double) p_158084_, (double) p_158085_ / 2.0D, (double) p_158086_);
        return Mth.clampedMap(d0, 0.0D, 6.0D, 1.0D, 0.0D);
    }

    private static double _getBeardContribution(int p_158088_, int p_158089_, int p_158090_)
    {
        int i = p_158088_ + KERNEL_RADIUS;
        int j = p_158089_ + KERNEL_RADIUS;
        int k = p_158090_ + KERNEL_RADIUS;
        if (i >= 0 && i < KERNEL_SIZE)
        {
            if (j >= 0 && j < KERNEL_SIZE)
            {
                return k >= 0 && k < KERNEL_SIZE ? (double) KERNEL[k * KERNEL_SIZE * KERNEL_SIZE + i * KERNEL_SIZE + j]
                        : 0.0D;
            }
            else
            {
                return 0.0D;
            }
        }
        else
        {
            return 0.0D;
        }
    }

    private static double _computeBeardContribution(int p_158092_, int p_158093_, int p_158094_)
    {
        double d0 = (double) (p_158092_ * p_158092_ + p_158094_ * p_158094_);
        double d1 = (double) p_158093_ + 0.5D;
        double d2 = d1 * d1;
        double d3 = Math.pow(Math.E, -(d2 / 16.0D + d0 / 16.0D));
        double d4 = -d1 * Mth.fastInvSqrt(d2 / 2.0D + d0 / 2.0D) / 2.0D;
        return d4 * d3;
    }
}
