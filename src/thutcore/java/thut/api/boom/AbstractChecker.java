package thut.api.boom;

import thut.api.boom.ExplosionCustom.BlastResult;

public abstract class AbstractChecker
{
    // The explosion we work for
    protected final ExplosionCustom boom;

    // Time taken actually computing
    public long totalTime = 0;
    // Time taken including not our computation
    public long realTotalTime = 0;

    public AbstractChecker(final ExplosionCustom boom)
    {
        this.boom = boom;
    }

    protected void start()
    {
        this.realTotalTime = System.nanoTime();
    }

    private long start;
    private long nanoS;

    protected void beginLoop()
    {
        start = System.currentTimeMillis();
        nanoS = System.nanoTime();
    }
    
    protected void endLoop()
    {
        this.totalTime += System.nanoTime() - nanoS;
    }

    protected boolean canContinue()
    {
        return System.currentTimeMillis() - start < boom.maxPerTick;
    }

    protected abstract BlastResult getBlocksToRemove();

    protected abstract void printDebugInfo();
}
