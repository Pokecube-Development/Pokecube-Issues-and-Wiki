package pokecube.adventures.ai.tasks.battle;

import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.world.server.ServerWorld;
import pokecube.core.PokecubeItems;
import pokecube.core.interfaces.IPokecube;
import pokecube.core.interfaces.IPokecube.PokecubeBehavior;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.capabilities.CapabilityPokemob;

public class CaptureMob extends BaseBattleTask
{
    public static int COOLDOWN = 100;

    long lastTry = 0;

    public CaptureMob(final LivingEntity trainer, final float chance)
    {
        super(trainer);
    }

    @Override
    protected void startExecuting(final ServerWorld worldIn, final LivingEntity entityIn, final long gameTimeIn)
    {
        final IPokemob targ = CapabilityPokemob.getPokemobFor(this.trainer.getTarget());
        if (targ != null && targ.getOwnerId() == null && gameTimeIn - this.lastTry > CaptureMob.COOLDOWN)
        {
            this.lastTry = gameTimeIn;
            final ItemStack itemStack = new ItemStack(PokecubeItems.getFilledCube(PokecubeBehavior.DEFAULTCUBE), 1);
            ((IPokecube) itemStack.getItem()).throwPokecubeAt(this.world, this.entity, itemStack, null, this.trainer
                    .getTarget());
        }
    }

    @Override
    protected boolean shouldExecute(final ServerWorld worldIn, final LivingEntity owner)
    {
        if (!super.shouldExecute(worldIn, owner)) return false;
        return this.trainer.countPokemon() < this.trainer.getMaxPokemobCount() / 2;
    }
}
