package thut.api.boom;

import java.util.HashSet;
import java.util.List;

import it.unimi.dsi.fastutil.objects.Object2FloatOpenHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.ChunkPos;
import thut.api.boom.ExplosionCustom.HitEntity;

public class SphereMaskChecker extends ShadowMaskChecker
{

    public SphereMaskChecker(ExplosionCustom boom)
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
        final double radSq = num * num / 4;
        boolean done = last_rad >= this.boom.radius;
        double radius = this.last_rad;
        final double C = 4;
        double area = 4 * Math.PI * radius * radius;
        final float grid = 0.5f;
        float N = (float) Math.ceil(area / grid);
        boom:
        while (canContinue() && radius < this.boom.radius)
        {
            int k;
            double phi_k_1 = this.last_phi;
            if (this.currentIndex < 1) this.currentIndex = 1;

            // Easy critera to skip a good portion of the loop, as we
            // can easily determine which ending index will fit within the
            // maximum value of y on the unit sphere which was checked
            // in the last shell.
            final int k_end = this.yToKMax(this.max.y, N);
            final float sqrtN = (float) Math.sqrt(N);

            // Spiral algorithm based on
            // https://doi.org/10.1007/BF03024331
            for (k = this.currentIndex; k <= k_end; k++)
            {
                this.currentIndex = k;
                // Break out early if we have taken too long.
                if (!canContinue())
                {
                    done = false;
                    break boom;
                }
                final float h_k = -this.kToY(k, N);
                final float sin_theta = (float) Math.sqrt(1 - h_k * h_k);
                final float phi_k = (float) (k > 1 && k < N ? (phi_k_1 + C / (sqrtN * sin_theta)) % (2 * Math.PI) : 0);
                this.last_phi = phi_k_1 = phi_k;
                final double x = sin_theta * Mth.cos(phi_k) * radius;
                final double y = h_k * radius;
                final double z = sin_theta * Mth.sin(phi_k) * radius;
                this.rTest.set(x, y, z);
                this.r.set(this.rTest.intX(), this.rTest.intY(), this.rTest.intZ());
                done = this.run(radSq, num, seen, ret, entityAffected);
                if (done) break boom;
            }
            // This gives us an easy way to determine which is the first
            // value of k which will result in a point that will fall within
            // the bounds of the last unit-sphere checked. Y is most likely
            // to run out first, as that is the most limited coordinate
            // here, as usually at least half of it is masked by the ground.
            int k_start = this.yToKMin(this.min.y, N);

            this.currentIndex = 1;
            this.last_phi = 0;
            radius += grid;
            area = 4 * Math.PI * radius * radius;
            N = (float) Math.ceil(area / grid);
            k_start = this.yToKMin(this.min.y, N);
            this.currentIndex = k_start;
            this.currentRadius = Mth.ceil(radius);
        }
        this.last_rad = radius;
        return done;
    }

    private float kToY(final int k, final float N)
    {
        return -1 + 2 * (k - 1) / (N - 1);
    }

    private int yToKMin(final float y, final float N)
    {
        int k = (int) (1 + (y + 1) * (N - 1) / 2);
        k = Math.max(1, k);
        return k;
    }

    private int yToKMax(final float y, final float N)
    {
        int k = Mth.ceil(1 + (y + 1) * (N - 1) / 2);
        k = (int) Math.min(N, k);
        return k;
    }

}
