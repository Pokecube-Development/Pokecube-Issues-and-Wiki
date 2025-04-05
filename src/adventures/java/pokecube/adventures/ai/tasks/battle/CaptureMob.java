package pokecube.adventures.ai.tasks.battle;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.entity.pokemob.PokemobCaps;
import pokecube.api.items.IPokecube;
import pokecube.api.items.IPokecube.PokecubeBehaviour;
import pokecube.core.PokecubeItems;

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
        final IPokemob targ = PokemobCaps.getPokemobFor(this.trainer.getTarget());
        if (targ != null && targ.getOwnerId() == null && gameTimeIn - this.lastTry > CaptureMob.COOLDOWN)
        {
            this.lastTry = gameTimeIn;
            final ItemStack itemStack = new ItemStack(PokecubeItems.getFilledCube(PokecubeBehaviour.DEFAULTCUBE), 1);
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
