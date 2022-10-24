package pokecube.api.data.abilities;

import java.util.function.Consumer;
import java.util.function.Predicate;

import net.minecraft.world.entity.LivingEntity;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.moves.utils.MoveApplication;

public class JsonAbility extends Ability
{
    public Predicate<IPokemob> _onUpdateCheckUser = p -> false;
    public Predicate<IPokemob> _onMoveUseCheckUser = p -> false;
    public Predicate<IPokemob> _onAgressCheckUser = p -> false;

    public Predicate<MoveApplication> _onMoveUseCheckMove = p -> false;
    public Predicate<LivingEntity> _onAgressCheckTarget = p -> false;

    public Consumer<IPokemob> _onUpdateApplyUser = p -> {};
    public Consumer<IPokemob> _onMoveUseApplyUser = p -> {};
    public Consumer<IPokemob> _onAggressApplyUser = p -> {};
    
    public Consumer<MoveApplication> _onMoveUseApplyMove = p -> {};
    public Consumer<LivingEntity> _onAgressApplyTarget = p -> {};

    @Override
    public void onUpdate(IPokemob mob)
    {
        if (_onUpdateCheckUser.test(mob)) _onUpdateApplyUser.accept(mob);
    }

    @Override
    public void preMoveUse(final IPokemob mob, final MoveApplication move)
    {
        if (_onMoveUseCheckUser.test(mob)) _onMoveUseApplyUser.accept(mob);
        if (_onMoveUseCheckMove.test(move)) _onMoveUseApplyMove.accept(move);
    }

    @Override
    public void onAgress(IPokemob mob, LivingEntity target)
    {
        if (_onAgressCheckUser.test(mob)) _onAggressApplyUser.accept(mob);
        if (_onAgressCheckTarget.test(target)) _onAgressApplyTarget.accept(target);
    }
}
