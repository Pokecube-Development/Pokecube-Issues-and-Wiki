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
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap.Types;
import thut.api.boom.ExplosionCustom.BlastResult;
import thut.api.boom.ExplosionCustom.HitEntity;
import thut.api.item.ItemList;
import thut.api.maths.Vector3;
import thut.api.maths.vecmath.Vec3f;
import thut.core.common.ThutCore;

public abstract class ShadowMaskChecker extends AbstractChecker
{
    public static interface ResistProvider
    {
        default float getResistance(final BlockPos pos, final ExplosionCustom boom)
        {
            final BlockState state = boom.level.getBlockState(pos);
            if (ItemList.is(ExplosionCustom.EXPLOSION_TRANSPARENT, state)) return 0;
            float resist = state.getExplosionResistance(boom.level, pos, boom);
            if (ItemList.is(ExplosionCustom.EXPLOSION_2X_WEAK, state)) resist /= 2;
            if (ItemList.is(ExplosionCustom.EXPLOSION_10X_WEAK, state)) resist /= 10;
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

        default float getTotalValue(final Vector3 rHat, final float r, final int minCube, final ShadowMaskChecker boom)
        {

            float resist = 0;
            float res;

            // Check each block to see if we have enough power to break.
            for (float j = 0; j <= r; j += 1.0f)
            {
                boom.rTest.set(boom.rHat).scalarMultBy(j);
                if (!boom.rTest.sameBlock(boom.rTestPrev))
                {
                    boom.rTestAbs.set(boom.rTest).addTo(boom.boom.centre);
                    final BlockPos testPos = boom.rTest.getPos();
                    if (this.has(testPos)) res = this.get(testPos);
                    else
                    {
                        // Ensure the chunk exists.
                        final ChunkPos cpos = new ChunkPos(boom.rTestAbs.getPos());
                        boom.boom.level.getChunk(cpos.x, cpos.z);
                        res = boom.boom.resistProvider.getResistance(boom.rTestAbs.getPos(), boom.boom);
                        this.set(testPos, res);
                    }
                    resist += res;
                    final float str = (float) (boom.boom.strength / boom.rTest.magSq());
                    // too hard, so set as blocked and flag for next site.
                    if (resist > str)
                    {
                        boom.shadow.block(boom.r.getPos(), boom.rHat);
                        boom.ind3++;
                        return boom.boom.strength;
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
            return this.resists.getOrDefault(pos.asLong(), 0);
        }

        @Override
        public void set(final BlockPos pos, final float var)
        {
            this.resists.put(pos.asLong(), var);
        }

        @Override
        public boolean has(final BlockPos pos)
        {
            return this.resists.containsKey(pos.asLong());
        }

    }

    public static class ShadowSet implements ShadowMap
    {
        LongSet blockedSet = new LongOpenHashSet();

        final Cubes hitTracker;

        final float num;

        Vector3 tmp = new Vector3();

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
            final long key = this.tmp.getPos().asLong();
            return this.blockedSet.contains(key);
        }

        @Override
        public final void block(final BlockPos pos, final Vector3 dir)
        {
            this.tmp.set(dir).scalarMultBy(this.num);
            final long key = this.tmp.getPos().asLong();
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

        int minCube = Integer.MAX_VALUE;
        int minFound = -1;

        Vector3 tmp = new Vector3();

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
            return this.tmp.getPos().immutable();
        }

        Cube getPrev(final Vector3 r, final Vector3 rHat)
        {
            if (r.magSq() <= 1) return null;
            return this.getCube(this.getPrevPos(r, rHat));
        }

        @Override
        public float getTotalValue(final Vector3 rHat, final float r, final int minCube, final ShadowMaskChecker boom)
        {
            return ResistCache.super.getTotalValue(rHat, r, minCube, boom);
        }

        void removeLess()
        {
            // TODO this should also compound these cubes outwards, so that
            // their blast resistances all get added to the now-smallest-cube.
            while (this.minFound > this.minCube)
            {
                this.cubes.remove(this.minCube);
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
                ThutCore.LOGGER.error("wrong cube? " + r + " " + this.radius);
                return 0;
            }
            this.had = false;
            float res = this.resistMap.getOrDefault(r.asLong(), -1);
            this.had = res != -1;
            if (!this.had) res = 0;
            return res;
        }

        @Override
        public void set(final BlockPos r, final float v)
        {
            if (!this.isOn(r))
            {
                ThutCore.LOGGER.error("wrong cube? " + r + " " + this.radius);
                return;
            }
            this.resistMap.put(r.asLong(), v);
        }

        @Override
        public boolean has(final BlockPos pos)
        {
            return this.resistMap.containsKey(pos.asLong());
        }
    }

    final Vec3f unit = new Vec3f();

    Vec3f min = new Vec3f(-1, -1, -1);
    Vec3f max = new Vec3f(1, 1, 1);

    Vec3f min_next = new Vec3f(1, 1, 1);
    Vec3f max_next = new Vec3f(-1, -1, -1);

    int currentIndex = 0;
    int nextIndex = 0;

    int currentRadius = 0;

    double last_phi = 0;
    double last_rad = 0.25;

    int ind1;
    int ind2;
    int ind3;
    int ind4;

    float lastBoundCheck = 10;

    // DOLATER figure out a good way to clear these between each set of shells.
    Long2FloatOpenHashMap resistMap = new Long2FloatOpenHashMap();

    LongSet blockedSet = new LongOpenHashSet();

    ShadowMap shadow;

    ResistCache resists;

    // used to speed up the checking of if a resist exists in the map
    LongSet checked = new LongOpenHashSet();
    LongSet seen = new LongOpenHashSet();

    Cubes cubes;

    Vector3 r = new Vector3(), rAbs = new Vector3(), rHat = new Vector3(), rTest = new Vector3(),
            rTestPrev = new Vector3(), rTestAbs = new Vector3();

    public ShadowMaskChecker(final ExplosionCustom boom)
    {
        super(boom);

        this.cubes = new Cubes(boom);
        this.shadow = new ShadowSet(boom);
        this.resists = new ResistMap();
        this.resists = this.cubes;

        this.lastBoundCheck = boom.centre.intY()
                - boom.level.getHeight(Types.MOTION_BLOCKING, boom.centre.intX(), boom.centre.intZ()) + 10;
        this.lastBoundCheck = Math.max(this.lastBoundCheck, 10);
    }

    private boolean outOfBounds(final Vec3f unit)
    {
        if (unit.x < this.min.x) return true;
        if (unit.y < this.min.y) return true;
        if (unit.z < this.min.z) return true;

        if (unit.x > this.max.x) return true;
        if (unit.y > this.max.y) return true;
        if (unit.z > this.max.z) return true;

        return false;
    }

    private void validateMinMax(final float r)
    {
        if (r - this.lastBoundCheck > 5)
        {
            this.min.set(this.min_next);
            this.max.set(this.max_next);
            // Gives some area around the blocked sections for actually being
            // checked.
            final float s = 1.0f;
            this.min.scale(s);
            this.max.scale(s);
            this.min_next.set(1, 1, 1);
            this.max_next.set(-1, -1, -1);
            this.lastBoundCheck = r;
            ThutCore.LOGGER.debug("Strength: {}, Max radius: {}, Last Radius: {}", this.boom.strength, this.boom.radius,
                    (int) r);
            this.shadow.clean(this.boom);
        }
        else
        {
            this.min_next.x = Math.min(this.min_next.x, this.unit.x);
            this.min_next.y = Math.min(this.min_next.y, this.unit.y);
            this.min_next.z = Math.min(this.min_next.z, this.unit.z);

            this.max_next.x = Math.max(this.max_next.x, this.unit.x);
            this.max_next.y = Math.max(this.max_next.y, this.unit.y);
            this.max_next.z = Math.max(this.max_next.z, this.unit.z);
        }
    }

    protected boolean run(final double radSq, final int num, final Set<ChunkPos> seen,
            final Object2FloatOpenHashMap<BlockPos> ret, final List<HitEntity> entityAffected)
    {
        double rMag;
        double str;
        ChunkPos cpos;

        if (this.r.y + this.boom.centre.y > this.boom.level.getMaxBuildHeight()) return false;
        final double rSq = this.r.magSq();
        if (rSq > radSq) return false;
        rMag = Math.sqrt(rSq);
        this.rAbs.set(this.r).addTo(this.boom.centre);
        this.rHat.set(this.r).norm();
        final BlockPos relPos = this.r.getPos();
        this.unit.set(this.rHat);
        if (this.outOfBounds(this.unit)) return false;

        str = this.boom.strength / rSq;
        // Return due to out of blast power.
        if (str <= this.boom.minBlastDamage) return true;

        // Already checked here, so we exit.
        if (this.shadow.hasHit(relPos)) return false;
        this.shadow.hit(relPos);
        this.ind4++;

        // Already blocked here, so we exit.
        if (this.shadow.blocked(relPos, this.rHat))
        {
            this.ind1++;
            return false;
        }

        // Ensure the chunk exists.
        cpos = new ChunkPos(this.rAbs.getPos());
        if (seen.add(cpos)) this.boom.level.getChunk(cpos.x, cpos.z);

        final boolean doAirCheck = this.rHat.y < this.max.y * 0.9 && this.rHat.y > this.min.y * 0.9;

        // // Check for mobs to hit at this point.
        if (doAirCheck && this.rAbs.isAir(this.boom.level) && !this.r.isEmpty())
        {
            if (ExplosionCustom.AFFECTINAIR)
            {
                final List<Entity> hits = this.boom.level.getEntities(this.boom.exploder,
                        this.rAbs.getAABB().inflate(0.5, 0.5, 0.5));
                // If this is the case, we do actually need to trace to there.
                if (hits != null && !hits.isEmpty())
                {
                    rMag = this.r.mag();
                    final float res = this.resists.getTotalValue(this.rHat, (float) rMag, 0, this);
                    if (res <= str) for (final Entity e : hits) entityAffected.add(new HitEntity(e, (float) str));
                }
            }
            this.validateMinMax((float) rMag);
            return false;
        }
        // Continue to next site, we can't break this block.
        if (!this.boom.canBreak(this.rAbs, this.rAbs.getBlockState(this.boom.level)))
        {
            this.shadow.block(relPos, this.rHat);
            this.ind2++;
            return false;
        }
        float res = this.boom.resistProvider.getResistance(this.rAbs.getPos(), this.boom);
        this.resists.set(relPos, res);

        rMag = this.r.mag();
        res = this.resists.getTotalValue(this.rHat, (float) rMag, 0, this);

        // This block is too strong, so continue to next block.
        if (res > str)
        {
            this.shadow.block(relPos, this.rHat);
            return false;
        }

        this.validateMinMax((float) rMag);
        this.rAbs.set(this.r).addTo(this.boom.centre);
        final BlockPos pos = this.rAbs.getPos().immutable();
        // Add as affected location.
        this.boom.getToBlow().add(pos);
        // Check for additional mobs to hit.
        final List<Entity> hits = this.boom.level.getEntities(this.boom.exploder,
                this.rAbs.getAABB().inflate(0.5, 0.5, 0.5));
        if (hits != null) for (final Entity e : hits) entityAffected.add(new HitEntity(e, (float) str));
        // Add to blocks to remove list.
        ret.addTo(pos, (float) str);

        return false;
    }

    protected abstract boolean apply(Object2FloatOpenHashMap<BlockPos> ret, List<HitEntity> entityAffected,
            HashSet<ChunkPos> seen);

    @Override
    protected BlastResult getBlocksToRemove()
    {
        beginLoop();
        final Object2FloatOpenHashMap<BlockPos> ret = new Object2FloatOpenHashMap<>();
        final List<HitEntity> entityAffected = Lists.newArrayList();
        final HashSet<ChunkPos> seen = new HashSet<>();
        boolean done = this.apply(ret, entityAffected, seen);
        endLoop();
        return new BlastResult(ret, entityAffected, seen, done);
    }

    @Override
    protected void printDebugInfo()
    {
        this.realTotalTime = System.nanoTime() - this.realTotalTime;
        ThutCore.LOGGER.info("Strength: {}, Max radius: {}, Last Radius: {}", this.boom.strength / this.boom.factor,
                this.boom.radius, this.r.mag());
        ThutCore.LOGGER.info("time (tick/real): {}/{}ms, {} shadowed, {} denied, {} blocked, {} checked",
                this.totalTime / 1e6, this.realTotalTime / 1e6, this.ind1, this.ind2, this.ind3, this.ind4);
        ThutCore.LOGGER.info("bounds: {} {}", this.min, this.max);
    }
}
