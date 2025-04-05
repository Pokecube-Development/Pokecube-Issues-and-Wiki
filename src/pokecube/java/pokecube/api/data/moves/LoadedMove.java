package pokecube.api.data.moves;

import javax.annotation.Nullable;

import pokecube.api.moves.utils.MoveApplication;
import pokecube.api.moves.utils.MoveApplication.AccuracyProvider;
import pokecube.api.moves.utils.MoveApplication.DamageApplier;
import pokecube.api.moves.utils.MoveApplication.HealProvider;
import pokecube.api.moves.utils.MoveApplication.OnMoveFail;
import pokecube.api.moves.utils.MoveApplication.OngoingApplier;
import pokecube.api.moves.utils.MoveApplication.PostMoveUse;
import pokecube.api.moves.utils.MoveApplication.PreApplyTests;
import pokecube.api.moves.utils.MoveApplication.RecoilApplier;
import pokecube.api.moves.utils.MoveApplication.StatApplier;
import pokecube.api.moves.utils.MoveApplication.StatusApplier;

public class LoadedMove implements IMove
{
    public static interface PreProcessor
    {
        PreProcessor DEFAULT = t -> {};
        
        void preProcess(MoveApplication t);
    }

    public PreProcessor preProcess = PreProcessor.DEFAULT;
    public StatusApplier status = null;
    public StatApplier stats = null;
    public AccuracyProvider accuracy = null;
    public DamageApplier damage = null;
    public RecoilApplier recoil = null;
    public HealProvider healer = null;
    public PreApplyTests doRun = null;
    public OngoingApplier applyOngoing = null;
    public PostMoveUse afterUse = null;
    public OnMoveFail onFail = null;

    /**
     * This is useful for adjusting values such as status, type, crit,
     * canceling, etc.
     * 
     * @param t - what to process
     */
    @Override
    public void preProcess(MoveApplication t)
    {
        preProcess.preProcess(t);
    }

    @Nullable
    /**
     * This is the applier for setting status effects, If null, this will use
     * StatusApplier.DEFAULT
     * 
     * @param t
     * @return new applier or null
     */
    @Override
    public StatusApplier getStatus(MoveApplication t)
    {
        return status;
    }

    @Nullable
    /**
     * This is the applier for setting stats such as lowering/raising them, If
     * null, this will use StatApplier.DEFAULT
     * 
     * @param t
     * @return new applier or null
     */
    @Override
    public StatApplier getStats(MoveApplication t)
    {
        return stats;
    }

    @Nullable
    /**
     * This is the applier for dealing damage. Custom implementations of this
     * are advised to also check the AccuracyProvider, as that is generally
     * checked during calls to the DamageApplier. If null, this will use
     * DamageApplier.DEFAULT
     * 
     * @param t
     * @return new applier or null
     */
    @Override
    public DamageApplier getDamage(MoveApplication t)
    {
        return damage;
    }

    @Nullable
    /**
     * This is the provider for move accuracy. What this should do is adjust the
     * "efficiency" of the provided Accuracy record. If the move is to miss,
     * this should be set to -1, otherwise it can be set to smaller or larger
     * numbers than 1 to adjust damage. If null, this will use
     * AccuracyProvider.DEFAULT.
     * 
     * @param t
     * @return new applier or null
     */
    @Override
    public AccuracyProvider getAccuracy(MoveApplication t)
    {
        return accuracy;
    }

    @Nullable
    /**
     * This applies "recoil" effects. This is health effects that scale based on
     * damage dealt, so includes recoil damage from moves like take-down, as
     * well as healing from moves like absorb. If null, this will use
     * RecoilApplier.DEFAULT
     * 
     * @param t
     * @return new applier or null
     */
    @Override
    public RecoilApplier getRecoil(MoveApplication t)
    {
        return recoil;
    }

    @Nullable
    /**
     * This applies healing effects to the target of the move. These effects
     * tend to scale on the maximum health of the target. If null, this will use
     * HealProvider.DEFAULT
     * 
     * @param t
     * @return new applier or null
     */
    @Override
    public HealProvider getHealer(MoveApplication t)
    {
        return healer;
    }

    @Nullable
    /**
     * This does initial checks for whether the attack can proceed. Such as
     * checking status effects, etc. If null, this will use
     * PreApplyTests.DEFAULT
     * 
     * @param t
     * @return
     */
    @Override
    public PreApplyTests getRunChecks(MoveApplication t)
    {
        return doRun;
    }

    @Nullable
    /**
     * This is called after applying healing and recoil effects if the move
     * hits, it is generally used to apply ongoing effects such as fire spin,
     * etc. If null, this will use OngoingApplier.NOOP or whatever is registered
     * via MoveApplicationRegistry
     * 
     * @param t
     * @return
     */
    @Override
    public OngoingApplier getOngoingEffect(MoveApplication t)
    {
        return applyOngoing;
    }

    @Nullable
    /**
     * This is called after the move has executed. Note that the Damage passed
     * in will indicate if the move missed via the efficiency in it, and this is
     * also called if it misses. If null, this will use PostMoveUse.DEFAULT
     * 
     * @param t
     * @return
     */
    @Override
    public PostMoveUse getPostUse(MoveApplication t)
    {
        return afterUse;
    }

    @Nullable
    /**
     * This is called if the move fails. If you need to reset counters, etc in
     * this case, here is a good place to do so. If null, this will use
     * OnMoveFail.DEFAULT
     * 
     * @param t
     * @return
     */
    @Override
    public OnMoveFail getOnFail(MoveApplication t)
    {
        return onFail;
    }
}
