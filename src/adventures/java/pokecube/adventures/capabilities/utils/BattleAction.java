package pokecube.adventures.capabilities.utils;

import net.minecraft.world.entity.LivingEntity;
import pokecube.api.entity.trainers.IHasPokemobs;
import pokecube.api.entity.trainers.TrainerCaps;
import pokecube.api.entity.trainers.actions.ActionContext;
import pokecube.api.entity.trainers.actions.IAction;

public class BattleAction implements IAction
{

    public BattleAction()
    {
    }

    @Override
    public boolean doAction(final ActionContext action)
    {
        final LivingEntity target = action.target;
        final LivingEntity holder = action.holder;
        final IHasPokemobs trainer = TrainerCaps.getHasPokemobs(holder);
        if (trainer != null)
        {
            trainer.resetPokemob();
            trainer.onSetTarget(target, true);
            return true;
        }
        return false;
    }

}
