package pokecube.adventures.capabilities.utils;

import net.minecraft.world.entity.LivingEntity;
import pokecube.api.entity.trainers.IHasPokemobs;
import pokecube.api.entity.trainers.TrainerCaps;
import pokecube.api.entity.trainers.actions.Action;
import pokecube.api.entity.trainers.actions.ActionContext;

public class BattleAction extends Action
{

    public BattleAction()
    {
        super("");
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
