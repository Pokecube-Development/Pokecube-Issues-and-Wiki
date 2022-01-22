package thut.api.boom;

import java.util.HashSet;
import java.util.List;

import it.unimi.dsi.fastutil.objects.Object2FloatOpenHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import thut.api.boom.ExplosionCustom.HitEntity;
import thut.api.maths.Cruncher;

public class CubeMaskChecker extends ShadowMaskChecker
{

    public CubeMaskChecker(ExplosionCustom boom)
    {
        super(boom);
    }

    @Override
    protected boolean apply(Object2FloatOpenHashMap<BlockPos> ret, List<HitEntity> entityAffected,
            HashSet<ChunkPos> seen)
    {
        int num = (int) Math.sqrt(this.boom.strength / 0.5);
        final int max = this.boom.radius * 2 + 1;
        num = Math.min(num, max);
        final int numCubed = num * num * num;
        final double radSq = num * num / 4;
        int increment = 0;
        boolean done = last_rad >= this.boom.radius;
        final int ind = this.currentIndex;
        final int maxIndex = numCubed;
        for (this.currentIndex = ind; this.currentIndex < maxIndex; this.currentIndex++)
        {
            increment++;
            // Break out early if we have taken too long.
            if (!canContinue())
            {
                done = false;
                break;
            }
            Cruncher.indexToVals(this.currentIndex, this.r, false);
            done = this.run(radSq, num, seen, ret, entityAffected);
            if (done) break;
        }
        // Increment the boom index for next pass.
        this.nextIndex = this.currentIndex + increment;
        return done;
    }

}
