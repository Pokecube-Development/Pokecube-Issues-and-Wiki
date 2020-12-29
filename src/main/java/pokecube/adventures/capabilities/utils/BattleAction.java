package pokecube.adventures.capabilities.utils;

import net.minecraft.entity.LivingEntity;
import pokecube.adventures.capabilities.CapabilityHasPokemobs.IHasPokemobs;
import pokecube.adventures.capabilities.TrainerCaps;

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
