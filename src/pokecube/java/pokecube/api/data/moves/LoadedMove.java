package pokecube.api.data.moves;

import pokecube.api.moves.utils.MoveApplication;
import pokecube.api.moves.utils.MoveApplication.AccuracyProvider;
import pokecube.api.moves.utils.MoveApplication.DamageApplier;
import pokecube.api.moves.utils.MoveApplication.HealProvider;
import pokecube.api.moves.utils.MoveApplication.LastMoveEffect;
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
    public LastMoveEffect lastMoveEffects = null;
    public OnMoveFail onFail = null;

    @Override
    public void preProcess(MoveApplication t)
    {
        preProcess.preProcess(t);
    }

    @Override
    public StatusApplier getStatus(MoveApplication t)
    {
        return status;
    }

    @Override
    public StatApplier getStats(MoveApplication t)
    {
        return stats;
    }

    @Override
    public DamageApplier getDamage(MoveApplication t)
    {
        return damage;
    }

    @Override
    public AccuracyProvider getAccuracy(MoveApplication t)
    {
        return accuracy;
    }

    @Override
    public RecoilApplier getRecoil(MoveApplication t)
    {
        return recoil;
    }

    @Override
    public HealProvider getHealer(MoveApplication t)
    {
        return healer;
    }

    @Override
    public PreApplyTests getRunChecks(MoveApplication t)
    {
        return doRun;
    }

    @Override
    public OngoingApplier getOngoingEffect(MoveApplication t)
    {
        return applyOngoing;
    }

    @Override
    public PostMoveUse getPostUse(MoveApplication t)
    {
        return afterUse;
    }

    @Override
    public LastMoveEffect getLastMoveEffect(MoveApplication t) { return lastMoveEffects; }

    @Override
    public OnMoveFail getOnFail(MoveApplication t)
    {
        return onFail;
    }
}
