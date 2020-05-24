package pokecube.adventures.capabilities.utils;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import pokecube.adventures.capabilities.CapabilityHasPokemobs.IHasPokemobs;
import pokecube.adventures.capabilities.TrainerCaps;

public class BattleAction extends Action
{

    public BattleAction()
    {
        super("");
    }

    @Override
    public void doAction(final PlayerEntity target, final Entity holder)
    {
        final IHasPokemobs trainer = TrainerCaps.getHasPokemobs(holder);
        if (trainer != null && target instanceof ServerPlayerEntity)
        {
            trainer.resetPokemob();
            trainer.onSetTarget(target);
        }
    }

}
