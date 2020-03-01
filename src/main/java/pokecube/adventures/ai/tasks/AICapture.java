package pokecube.adventures.ai.tasks;

import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import pokecube.core.PokecubeItems;
import pokecube.core.interfaces.IPokecube;
import pokecube.core.interfaces.IPokecube.PokecubeBehavior;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.capabilities.CapabilityPokemob;

public class AICapture extends AITrainerBase
{
    public static int COOLDOWN = 100;

    int cooldown = 0;

    public AICapture(final LivingEntity trainer)
    {
        super(trainer);
    }

    @Override
    public boolean shouldRun()
    {
        return this.trainer.getTarget() != null && this.trainer.countPokemon() < this.trainer.getMaxPokemobCount() / 2;
    }

    @Override
    public void run()
    {
        this.cooldown--;
        final IPokemob targ = CapabilityPokemob.getPokemobFor(this.trainer.getTarget());
        if (targ != null && targ.getOwnerId() == null && targ.getHealth() < targ.getMaxHealth() / 4
                && this.cooldown < 0)
        {
            this.cooldown = AICapture.COOLDOWN;
            final ItemStack itemStack = new ItemStack(PokecubeItems.getFilledCube(PokecubeBehavior.DEFAULTCUBE), 1);
            ((IPokecube) itemStack.getItem()).throwPokecubeAt(this.world, this.entity, itemStack, null, this.trainer
                    .getTarget());
        }
    }

}
