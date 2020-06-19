package thut.api.boom;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.google.common.collect.Lists;

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2FloatOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import it.unimi.dsi.fastutil.objects.Object2FloatOpenHashMap;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.LeavesBlock;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.MathHelper;
import thut.api.boom.ExplosionCustom.BlastResult;
import thut.api.boom.ExplosionCustom.HitEntity;
import thut.api.maths.Cruncher;
import thut.api.maths.Vector3;
import thut.api.maths.vecmath.Vector3f;

public class Checker
{
    public static interface ResistProvider
    {
        default float getResistance(final BlockPos pos, final ExplosionCustom boom)
        {
            final BlockState state = boom.world.getBlockState(pos);
            float resist = state.getExplosionResistance(boom.world, pos, boom.getExplosivePlacedBy(), boom);
            if (state.getBlock() == Blocks.GRASS_BLOCK) resist /= 2;
            if (state.getBlock() instanceof LeavesBlock) resist /= 10;
            if (resist > 1) resist *= resist;
            return resist;
        }
    }

    public static interface ResistCache
    {
        float get(BlockPos pos);

        void set(BlockPos pos, float var);

        boolean has(BlockPos pos);

        default void clean(final ExplosionCustom boom)
        {

        }

        default float getTotalValue(final Vector3 rHat, final float r, final int minCube, final ExplosionCustom boom)
        {

            float resist;
            float res_prev;
            final float dj = 1;
            resist = 0;
            res_prev = 0;
            float res;

            // Check each block to see if we have enough power to break.
            for (float j = 0; j <= r; j += dj)
            {
                boom.rTest.set(boom.rHat).scalarMultBy(j);

                if (!boom.rTest.sameBlock(boom.rTestPrev))
                {
                    boom.rTestAbs.set(boom.rTest).addTo(boom.centre);

                    final BlockPos testPos = boom.rTest.getPos();

                    if (boom.resists.has(testPos))
                    {
                        res_prev = boom.resists.get(testPos);
                        res = res_prev;
                    }
                    else
                    {
                        // Ensure the chunk exists.
                        final ChunkPos cpos = new ChunkPos(boom.rTestAbs.getPos());
                        boom.world.getChunk(cpos.x, cpos.z);
                        res = boom.resistProvider.getResistance(boom.rTestAbs.getPos(), boom);
                        boom.resists.set(testPos, res);
                    }
                    resist += res;
                    // Can't break this, so set as blocked and flag for next
                    // site.
                    if (!boom.canBreak(boom.rTestAbs, boom.rTestAbs.getBlockState(boom.world)))
                    {
                        boom.shadow.block(boom.r.getPos(), boom.rHat);
                        boom.ind2++;
                        return boom.strength;
                    }
                    final double d1 = boom.rTest.magSq();
                    final double d = d1;
                    final float str = (float) (boom.strength / d);
                    // too hard, so set as blocked and flag for next site.
                    if (resist > str)
                    {
                        boom.shadow.block(boom.r.getPos(), boom.rHat);
                        boom.ind3++;
                        return boom.strength;
                    }
                }
                boom.rTestPrev.set(boom.rTest);
            }

            return 0;
        }
    }

    public static interface ShadowMap
    {
        boolean blocked(BlockPos pos, Vector3 dir);

        void block(BlockPos pos, Vector3 dir);

        boolean hasHit(BlockPos pos);

        void hit(BlockPos pos);

        default void clean(final ExplosionCustom boom)
        {

        }
    }

    public static class ResistMap implements ResistCache
    {
        Long2FloatOpenHashMap resists = new Long2FloatOpenHashMap();

        @Override
        public float get(final BlockPos pos)
        {
            return this.resists.getOrDefault(pos.toLong(), 0);
        }

        @Override
        public void set(final BlockPos pos, final float var)
        {
            this.resists.put(pos.toLong(), var);
        }

        @Override
        public boolean has(final BlockPos pos)
        {
            return this.resists.containsKey(pos.toLong());
        }

    }

    public static class ShadowSet implements ShadowMap
    {
        LongSet blockedSet = new LongOpenHashSet();

        final Cubes hitTracker;

        final float num;

        Vector3 tmp = Vector3.getNewVector();

        public ShadowSet(final ExplosionCustom boom)
        {
            final float scaleFactor = 10;
            final int num = (int) Math.sqrt(boom.strength * scaleFactor / 0.5);
            final int max = boom.radius * 2 + 1;
            this.num = Math.min(num, max) / 2f;
            this.hitTracker = new Cubes(boom);
        }

        @Override
        public final boolean blocked(final BlockPos pos, final Vector3 dir)
        {
            this.tmp.set(dir).scalarMultBy(this.num);
            final long key = this.tmp.getPos().toLong();
            return this.blockedSet.contains(key);
        }

        @Override
        public final void block(final BlockPos pos, final Vector3 dir)
        {
            this.tmp.set(dir).scalarMultBy(this.num);
            final long key = this.tmp.getPos().toLong();
            this.blockedSet.add(key);
        }

        @Override
        public final boolean hasHit(final BlockPos pos)
        {
            return this.hitTracker.has(pos);
        }

        @Override
        public final void hit(final BlockPos pos)
        {
            this.hitTracker.set(pos, 1);
        }

        @Override
        public void clean(final ExplosionCustom boom)
        {
            this.hitTracker.clean(boom);
        }

    }

    public static class Cubes implements ResistCache
    {
        final Int2ObjectOpenHashMap<Cube> cubes;

        int minCube  = Integer.MAX_VALUE;
        int minFound = -1;

        Vector3 tmp = Vector3.getNewVector();

        public Cubes(final ExplosionCustom boom)
        {
            this.cubes = new Int2ObjectOpenHashMap<>(boom.radius, 0.25f);
        }

        Cube getCube(final BlockPos r)
        {
            final int x = Math.abs(r.getX());
            final int y = Math.abs(r.getY());
            final int z = Math.abs(r.getZ());
            final int max = Math.max(x, Math.max(y, z));
            if (!this.cubes.containsKey(max))
            {
                final Cube c = new Cube();
                c.radius = max;
                // System.out.println("New Cube: " + max);
                this.cubes.put(max, c);
                return c;
            }
            if (this.minFound == -1) this.minFound = max;
            else this.minFound = Math.min(max, this.minFound);
            this.minCube = Math.min(max, this.minCube);
            return this.cubes.get(max);
        }

        BlockPos getPrevPos(final Vector3 r, final Vector3 rHat)
        {
            this.tmp.set(r).addTo(0.5, 0.5, 0.5).subtractFrom(rHat);
            return this.tmp.getPos().toImmutable();
        }

        Cube getPrev(final Vector3 r, final Vector3 rHat)
        {
            if (r.magSq() <= 1) return null;
            return this.getCube(this.getPrevPos(r, rHat));
        }

        @Override
        public float getTotalValue(final Vector3 rHat, final float r, final int minCube, final ExplosionCustom boom)
        {
            return ResistCache.super.getTotalValue(rHat, r, minCube, boom);
        }

        void removeLess()
        {
            // TODO this should also compound these cubes outwards, so that
            // their blast resistances all get added to the now-smallest-cube.
            // System.out.println("Clean cubes less than: " + this.minFound);
            while (this.minFound > this.minCube)
            {
                this.cubes.remove(this.minCube);
                // System.out.println("Dead Cube: " + this.minCube);
                this.minCube++;
            }
            this.minFound = -1;
            this.minCube = Integer.MAX_VALUE;
        }

        @Override
        public final float get(final BlockPos pos)
        {
            final Cube c = this.getCube(pos);
            return c.get(pos);
        }

        @Override
        public final void set(final BlockPos pos, final float var)
        {
            this.getCube(pos).set(pos, var);
        }

        @Override
        public final boolean has(final BlockPos pos)
        {
            return this.getCube(pos).has(pos);
        }

        @Override
        public void clean(final ExplosionCustom boom)
        {
            this.removeLess();
        }
    }

    public static class Cube implements ResistCache
    {
        final Long2FloatOpenHashMap resistMap;

        int radius = 1;

        boolean had = false;

        public Cube()
        {
            this.resistMap = new Long2FloatOpenHashMap(1024, 0.75f);
        }

        public boolean isOn(final BlockPos r)
        {
            final int x = Math.abs(r.getX());
            final int y = Math.abs(r.getY());
            final int z = Math.abs(r.getZ());
            final int max = Math.max(x, Math.max(y, z));
            if (max != this.radius) return false;
            final boolean xFace = x == this.radius && y <= this.radius && z <= this.radius;
            if (xFace) return true;
            final boolean yFace = y == this.radius && x <= this.radius && z <= this.radius;
            if (yFace) return true;
            final boolean zFace = z == this.radius && y <= this.radius && x <= this.radius;
            if (zFace) return true;
            return false;
        }

        @Override
        public float get(final BlockPos r)
        {
            if (!this.isOn(r))
            {
                System.out.println("wrong cube? " + r + " " + this.radius);
                return 0;
            }
            this.had = false;
            float res = this.resistMap.getOrDefault(r.toLong(), -1);
            this.had = res != -1;
            if (!this.had) res = 0;
            return res;
        }

        @Override
        public void set(final BlockPos r, final float v)
        {
            if (!this.isOn(r))
            {
                System.out.println("wrong cube? " + r + " " + this.radius);
                return;
            }
            this.resistMap.put(r.toLong(), v);
        }

        @Override
        public boolean has(final BlockPos pos)
        {
            return this.resistMap.containsKey(pos.toLong());
        }
    }

    final ExplosionCustom boom;

    final Vector3f unit = new Vector3f();

    public Checker(final ExplosionCustom boom)
    {
        this.boom = boom;
    }

    private boolean outOfBounds(final Vector3f unit)
    {
        if (unit.x < this.boom.min.x) return true;
        if (unit.y < this.boom.min.y) return true;
        if (unit.z < this.boom.min.z) return true;

        if (unit.x > this.boom.max.x) return true;
        if (unit.y > this.boom.max.y) return true;
        if (unit.z > this.boom.max.z) return true;

        return false;
    }

    private void validateMinMax(final float r)
    {
        if (r - this.boom.lastBoundCheck > 5)
        {
            this.boom.min.set(this.boom.min_next);
            this.boom.max.set(this.boom.max_next);
            // Gives some area around the blocked sections for actually being
            // checked.
            final float s = 1.0f;
            this.boom.min.scale(s);
            this.boom.max.scale(s);
            this.boom.min_next.set(1, 1, 1);
            this.boom.max_next.set(-1, -1, -1);
            this.boom.lastBoundCheck = r;

            this.boom.shadow.clean(this.boom);
        }
        else
        {
            this.boom.min_next.x = Math.min(this.boom.min_next.x, this.unit.x);
            this.boom.min_next.y = Math.min(this.boom.min_next.y, this.unit.y);
            this.boom.min_next.z = Math.min(this.boom.min_next.z, this.unit.z);

            this.boom.max_next.x = Math.max(this.boom.max_next.x, this.unit.x);
            this.boom.max_next.y = Math.max(this.boom.max_next.y, this.unit.y);
            this.boom.max_next.z = Math.max(this.boom.max_next.z, this.unit.z);
        }
    }

    private boolean run(final double radSq, final int num,
            final Set<ChunkPos> seen,
            final Object2FloatOpenHashMap<BlockPos> ret, final List<HitEntity> entityAffected)
    {
        double rMag;
        double str;
        ChunkPos cpos;

        if (this.boom.r.y + this.boom.centre.y > this.boom.world.getHeight()) return false;
        final double rSq = this.boom.r.magSq();
        if (rSq > radSq) return false;
        rMag = Math.sqrt(rSq);
        this.boom.rAbs.set(this.boom.r).addTo(this.boom.centre);
        this.boom.rHat.set(this.boom.r).norm();
        final BlockPos relPos = this.boom.r.getPos();
        this.unit.set(this.boom.rHat);
        if (this.outOfBounds(this.unit)) return false;

        str = this.boom.strength / rSq;

        // Return due to out of blast power.
        if (str <= this.boom.minBlastDamage) return true;

        // Already checked here, so we exit.
        if (this.boom.shadow.hasHit(relPos)) return false;
        this.boom.shadow.hit(relPos);
        this.boom.ind4++;

        // Already blocked here, so we exit.
        if (this.boom.shadow.blocked(relPos, this.boom.rHat))
        {
            this.boom.ind1++;
            return false;
        }

        // Ensure the chunk exists.
        cpos = new ChunkPos(this.boom.rAbs.getPos());
        if (!seen.contains(cpos) && seen.add(cpos)) this.boom.world.getChunk(cpos.x, cpos.z);

        final boolean doAirCheck = this.boom.rHat.y < this.boom.max.y * 0.9 && this.boom.rHat.y > this.boom.min.y * 0.9;

        // // Check for mobs to hit at this point.
        if (doAirCheck && this.boom.rAbs.isAir(this.boom.world) && !this.boom.r.isEmpty())
        {
            if (ExplosionCustom.AFFECTINAIR)
            {
                final List<Entity> hits = this.boom.world.getEntitiesWithinAABBExcludingEntity(this.boom.exploder,
                        this.boom.rAbs.getAABB().grow(0.5, 0.5, 0.5));
                // If this is the case, we do actually need to trace to there.
                if (hits != null && !hits.isEmpty())
                {
                    rMag = this.boom.r.mag();
                    final float res = this.boom.resists.getTotalValue(this.boom.rHat, (float) rMag, 0, this.boom);
                    if (res <= str) for (final Entity e : hits)
                        entityAffected.add(new HitEntity(e, (float) str));
                }
            }
            this.validateMinMax((float) rMag);
            return false;
        }
        // Continue to next site, we can't break this block.
        if (!this.boom.canBreak(this.boom.rAbs, this.boom.rAbs.getBlockState(this.boom.world)))
        {
            this.boom.shadow.block(relPos, this.boom.rHat);
            this.boom.ind2++;
            return false;
        }
        float res = this.boom.resistProvider.getResistance(this.boom.rAbs.getPos(), this.boom);
        this.boom.resists.set(relPos, res);

        rMag = this.boom.r.mag();
        res = this.boom.resists.getTotalValue(this.boom.rHat, (float) rMag, 0, this.boom);

        // This block is too strong, so continue to next block.
        if (res > str)
        {
            this.boom.shadow.block(relPos, this.boom.rHat);
            return false;
        }

        this.validateMinMax((float) rMag);
        this.boom.rAbs.set(this.boom.r).addTo(this.boom.centre);
        final BlockPos pos = this.boom.rAbs.getPos().toImmutable();
        // Add as affected location.
        this.boom.getAffectedBlockPositions().add(pos);
        // Check for additional mobs to hit.
        final List<Entity> hits = this.boom.world.getEntitiesWithinAABBExcludingEntity(this.boom.exploder,
                this.boom.rAbs.getAABB().grow(0.5, 0.5, 0.5));
        if (hits != null) for (final Entity e : hits)
            entityAffected.add(new HitEntity(e, (float) str));
        // Add to blocks to remove list.
        ret.addTo(pos, (float) str);

        return false;
    }

    protected BlastResult getBlocksToRemove()
    {
        final int threshold = this.boom.maxPerTick = 5000;
        int num = (int) Math.sqrt(this.boom.strength / 0.5);
        final int max = this.boom.radius * 2 + 1;
        num = Math.min(num, max);
        final int numCubed = num * num * num;
        final double radSq = num * num / 4;
        int increment = 0;
        final Object2FloatOpenHashMap<BlockPos> ret = new Object2FloatOpenHashMap<>();
        final List<HitEntity> entityAffected = Lists.newArrayList();
        final HashSet<ChunkPos> seen = new HashSet<>();
        boolean done = true;

        final boolean sphere = true;

        final long start = System.currentTimeMillis();
        final long nanoS = System.nanoTime();

        if (!sphere)
        {
            final int ind = this.boom.currentIndex;
            final int maxIndex = numCubed;
            for (this.boom.currentIndex = ind; this.boom.currentIndex < maxIndex; this.boom.currentIndex++)
            {
                increment++;
                final long time = System.currentTimeMillis();
                // Break out early if we have taken too long.
                if (time - start > threshold)
                {
                    done = false;
                    break;
                }
                Cruncher.indexToVals(this.boom.currentIndex, this.boom.r, sphere);
                done = this.run(radSq, num, seen, ret, entityAffected);
                if (done) break;
            }
        }
        else
        {

            double radius = this.boom.last_rad;
            final double C = 4;
            double area = 4 * Math.PI * radius * radius;
            final float grid = 0.5f;
            float N = (float) Math.ceil(area / grid);
            boom:
            while (System.currentTimeMillis() - start < threshold && radius < this.boom.radius)
            {
                int k;
                double phi_k_1 = this.boom.last_phi;
                if (this.boom.currentIndex < 1) this.boom.currentIndex = 1;

                // Easy critera to skip a good portion of the loop, as we
                // can easily determine which ending index will fit within the
                // maximum value of y on the unit sphere which was checked
                // in the last shell.
                final int k_end = this.yToKMax(this.boom.max.y, N);
                final float sqrtN = (float) Math.sqrt(N);

                // Spiral algorithm based on
                // https://doi.org/10.1007/BF03024331
                for (k = this.boom.currentIndex; k <= k_end; k++)
                {
                    this.boom.currentIndex = k;
                    // Break out early if we have taken too long.
                    if (System.currentTimeMillis() - start > threshold)
                    {
                        done = false;
                        break boom;
                    }
                    final float h_k = -this.kToY(k, N);
                    final float sin_theta = (float) Math.sqrt(1 - h_k * h_k);
                    final float phi_k = (float) (k > 1 && k < N ? (phi_k_1 + C / (sqrtN * sin_theta)) % (2 * Math.PI)
                            : 0);
                    this.boom.last_phi = phi_k_1 = phi_k;
                    final double x = sin_theta * MathHelper.cos(phi_k) * radius;
                    final double y = h_k * radius;
                    final double z = sin_theta * MathHelper.sin(phi_k) * radius;
                    this.boom.rTest.set(x, y, z);
                    this.boom.r.set(this.boom.rTest.intX(), this.boom.rTest.intY(), this.boom.rTest.intZ());
                    done = this.run(radSq, num, seen, ret, entityAffected);
                    if (done) break boom;
                }
                // This gives us an easy way to determine which is the first
                // value of k which will result in a point that will fall within
                // the bounds of the last unit-sphere checked. Y is most likely
                // to run out first, as that is the most limited coordinate
                // here, as usually at least half of it is masked by the ground.
                int k_start = this.yToKMin(this.boom.min.y, N);

                this.boom.currentIndex = 1;
                this.boom.last_phi = 0;
                radius += grid;
                area = 4 * Math.PI * radius * radius;
                N = (float) Math.ceil(area / grid);
                k_start = this.yToKMin(this.boom.min.y, N);
                this.boom.currentIndex = k_start;
                this.boom.currentRadius = MathHelper.ceil(radius);
            }
            this.boom.last_rad = radius;
        }

        this.boom.totalTime += System.nanoTime() - nanoS;
        // Increment the boom index for next pass.
        this.boom.nextIndex = this.boom.currentIndex + increment;
        return new BlastResult(ret, entityAffected, done);
    }

    float kToY(final int k, final float N)
    {
        return -1 + 2 * (k - 1) / (N - 1);
    }

    int yToKMin(final float y, final float N)
    {
        int k = (int) (1 + (y + 1) * (N - 1) / 2);
        k = Math.max(1, k);
        return k;
    }

    int yToKMax(final float y, final float N)
    {
        int k = MathHelper.ceil(1 + (y + 1) * (N - 1) / 2);
        k = (int) Math.min(N, k);
        return k;
    }
}
