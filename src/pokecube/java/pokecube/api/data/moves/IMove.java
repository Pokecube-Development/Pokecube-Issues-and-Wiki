package pokecube.api.data.moves;

import java.util.function.Consumer;

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

public interface IMove extends Consumer<MoveApplication>
{
    @Override
    default void accept(MoveApplication t)
    {
        StatusApplier status = this.getStatus(t);
        StatApplier stats = this.getStats(t);
        AccuracyProvider accuracy = this.getAccuracy(t);
        DamageApplier damage = this.getDamage(t);
        RecoilApplier recoil = this.getRecoil(t);
        HealProvider healer = this.getHealer(t);
        PreApplyTests doRun = this.getRunChecks(t);
        OngoingApplier applyOngoing = this.getOngoingEffect(t);
        PostMoveUse afterUse = this.getPostUse(t);
        OnMoveFail onFail = this.getOnFail(t);

        if (status != null) t.status = status;
        if (stats != null) t.stats = stats;
        if (accuracy != null) t.accuracy = accuracy;
        if (damage != null) t.damage = damage;
        if (recoil != null) t.recoil = recoil;
        if (healer != null) t.healer = healer;
        if (doRun != null) t.doRun = doRun;
        if (applyOngoing != null) t.applyOngoing = applyOngoing;
        if (afterUse != null) t.afterUse = afterUse;
        if (onFail != null) t.onFail = onFail;
        preProcess(t);
    }

    /**
     * This is useful for adjusting values such as status, type, crit,
     * canceling, etc.
     * 
     * @param t - what to process
     */
    default void preProcess(MoveApplication t)
    {

    }

    @Nullable
    /**
     * This is the applier for setting status effects, If null, this will use
     * StatusApplier.DEFAULT
     * 
     * @param t
     * @return new applier or null
     */
    default StatusApplier getStatus(MoveApplication t)
    {
        return null;
    }

    @Nullable
    /**
     * This is the applier for setting stats such as lowering/raising them, If
     * null, this will use StatApplier.DEFAULT
     * 
     * @param t
     * @return new applier or null
     */
    default StatApplier getStats(MoveApplication t)
    {
        return null;
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
    default DamageApplier getDamage(MoveApplication t)
    {
        return null;
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
    default AccuracyProvider getAccuracy(MoveApplication t)
    {
        return null;
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
    default RecoilApplier getRecoil(MoveApplication t)
    {
        return null;
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
    default HealProvider getHealer(MoveApplication t)
    {
        return null;
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
    default PreApplyTests getRunChecks(MoveApplication t)
    {
        return null;
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
    default OngoingApplier getOngoingEffect(MoveApplication t)
    {
        return null;
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
    default PostMoveUse getPostUse(MoveApplication t)
    {
        return null;
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
    default OnMoveFail getOnFail(MoveApplication t)
    {
        return null;
    }
}