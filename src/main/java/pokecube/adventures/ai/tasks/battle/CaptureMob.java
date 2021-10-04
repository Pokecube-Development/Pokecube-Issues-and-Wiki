package pokecube.adventures.ai.tasks.battle;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
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
    protected void start(final ServerLevel worldIn, final LivingEntity entityIn, final long gameTimeIn)
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
    protected boolean checkExtraStartConditions(final ServerLevel worldIn, final LivingEntity owner)
    {
        if (!super.checkExtraStartConditions(worldIn, owner)) return false;
        return this.trainer.countPokemon() < this.trainer.getMaxPokemobCount() / 2;
    }
}
