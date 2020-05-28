package thut.api.boom;

import java.util.List;

import com.google.common.collect.Lists;

import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.chunk.IChunk;
import thut.api.boom.ExplosionCustom.BlastResult;
import thut.api.boom.ExplosionCustom.HitEntity;
import thut.api.maths.Cruncher;

public class Checker
{
    final ExplosionCustom boom;

    public Checker(final ExplosionCustom boom)
    {
        this.boom = boom;
    }

    protected BlastResult getBlocksToRemove()
    {
        final int ind = this.boom.currentIndex;
        int index;
        int index2;
        final double scaleFactor = 1500;
        double rMag;
        float resist;
        double str;
        int num = (int) Math.sqrt(this.boom.strength * scaleFactor / 0.5);
        final int max = this.boom.radius * 2 + 1;
        num = Math.min(num, max);
        final int numCubed = num * num * num;
        final double radSq = num * num / 4;
        final int maxIndex = numCubed;
        int increment = 0;
        final List<BlockPos> ret = Lists.newArrayList();
        final List<HitEntity> entityAffected = Lists.newArrayList();
        boolean done = true;
        final long start = System.currentTimeMillis();
        final long nanoS = System.nanoTime();
        for (this.boom.currentIndex = ind; this.boom.currentIndex < maxIndex; this.boom.currentIndex++)
        {
            increment++;
            final long time = System.currentTimeMillis();
            // Break out early if we have taken too long.
            if (time - start > 1)
            {
                done = false;
                break;
            }
            Cruncher.indexToVals(this.boom.currentIndex, this.boom.r);
            // TODO make this check world bounds somehow.
            if (this.boom.r.y + this.boom.centre.y < 0 || this.boom.r.y + this.boom.centre.y > 255) continue;
            final double rSq = this.boom.r.magSq();
            if (rSq > radSq) continue;
            rMag = Math.sqrt(rSq);
            this.boom.rAbs.set(this.boom.r).addTo(this.boom.centre);
            this.boom.rHat.set(this.boom.r).norm();
            index = Cruncher.getVectorInt(this.boom.rHat.scalarMultBy(num / 2d));
            this.boom.rHat.scalarMultBy(2d / num);
            // Already checked here, so we exit.
            if (this.boom.blockedSet.contains(index)) continue;
            str = this.boom.strength * scaleFactor / rSq;
            // Ensure the chunk exists.
            this.boom.world.getChunk(this.boom.rAbs.getPos());
            // Check for mobs to hit at this point.
            if (this.boom.rAbs.isAir(this.boom.world) && !this.boom.r.isEmpty())
            {
                if (ExplosionCustom.AFFECTINAIR)
                {
                    final List<Entity> hits = this.boom.world.getEntitiesWithinAABBExcludingEntity(this.boom.exploder,
                            this.boom.rAbs.getAABB().grow(0.5, 0.5, 0.5));
                    if (hits != null) for (final Entity e : hits)
                        entityAffected.add(new HitEntity(e, (float) str));
                }
                continue;
            }

            // Return due to out of blast power.
            if (str <= this.boom.minBlastDamage)
            {
                System.out.println("Terminating at distance " + rMag);
                done = true;
                break;
            }
            // Continue to next site, we can't break this block.
            if (!this.boom.canBreak(this.boom.rAbs))
            {
                this.boom.blockedSet.add(index);
                continue;
            }
            index2 = Cruncher.getVectorInt(this.boom.r);
            float res = this.boom.rAbs.getExplosionResistance(this.boom, this.boom.world);
            if (res > 1) res = res * res;
            this.boom.checked.set(index2);
            this.boom.resists.put(index2, res);
            // }
            // This block is too strong, so continue to next block.
            if (res > str)
            {
                this.boom.blockedSet.add(index);
                continue;
            }
            // Whether we should continue onto the next site.
            boolean stop = false;

            rMag = this.boom.r.mag();
            final float dj = 1;
            resist = 0;

            // Check each block to see if we have enough power to break.
            for (float j = 0; j <= (float) rMag; j += dj)
            {
                this.boom.rTest.set(this.boom.rHat).scalarMultBy(j);

                if (!this.boom.rTest.sameBlock(this.boom.rTestPrev))
                {
                    this.boom.rTestAbs.set(this.boom.rTest).addTo(this.boom.centre);

                    index2 = Cruncher.getVectorInt(this.boom.rTest);

                    if (this.boom.blockedSet.contains(index2))
                    {
                        stop = true;
                        break;
                    }

                    // Ensure the chunk exists.
                    this.boom.world.getChunk(this.boom.rTestAbs.getPos());
                    if (this.boom.checked.get(index2)) res = this.boom.resists.get(index2);
                    else
                    {
                        res = this.boom.rTestAbs.getExplosionResistance(this.boom, this.boom.world);
                        if (res > 1) res = res * res;
                        this.boom.checked.set(index2);
                        this.boom.resists.put(index2, res);
                    }
                    resist += res;
                    // Can't break this, so set as blocked and flag for next
                    // site.
                    if (!this.boom.canBreak(this.boom.rTestAbs))
                    {
                        stop = true;
                        this.boom.blockedSet.add(index);
                        break;
                    }
                    final double d1 = this.boom.rTest.magSq();
                    final double d = d1;
                    str = this.boom.strength * scaleFactor / d;
                    // too hard, so set as blocked and flag for next site.
                    if (resist > str)
                    {
                        stop = true;
                        this.boom.blockedSet.add(index);
                        break;
                    }
                }
                this.boom.rTestPrev.set(this.boom.rTest);
            }
            // Continue onto next site.
            if (stop) continue;
            this.boom.rAbs.set(this.boom.r).addTo(this.boom.centre);
            final IChunk chunk = this.boom.world.getChunk(this.boom.rAbs.getPos());
            // Add to affected chunks list.
            if (!this.boom.affected.contains(chunk)) this.boom.affected.add(chunk);
            // Add as affected location.
            this.boom.addChunkPosition(this.boom.rAbs);
            // Check for additional mobs to hit.
            final List<Entity> hits = this.boom.world.getEntitiesWithinAABBExcludingEntity(this.boom.exploder,
                    this.boom.rAbs.getAABB().grow(0.5, 0.5, 0.5));
            if (hits != null) for (final Entity e : hits)
                entityAffected.add(new HitEntity(e, (float) str));
            // Add to blocks to remove list.
            ret.add(new BlockPos(this.boom.rAbs.getPos()));
        }
        this.boom.totalTime += System.nanoTime() - nanoS;
        // if (done) System.out.println("dt: " + this.boom.totalTime / 1e6d);
        // Increment the boom index for next pass.
        this.boom.nextIndex = this.boom.currentIndex + increment;
        return new BlastResult(ret, entityAffected, done);
    }
}
