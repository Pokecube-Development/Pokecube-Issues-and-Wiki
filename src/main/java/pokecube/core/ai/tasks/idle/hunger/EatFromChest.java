package pokecube.core.ai.tasks.idle.hunger;

import java.util.function.Predicate;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import pokecube.core.ai.brain.sensors.NearBlocks.NearBlock;
import pokecube.core.ai.tasks.idle.HungerTask;
import pokecube.core.interfaces.IPokemob;
import thut.api.item.ItemList;

public class EatFromChest extends EatBlockBase
{
    private static boolean isTrappedChest(final BlockState state)
    {
        return state.getBlock() == Blocks.TRAPPED_CHEST;
    }

    private static final Predicate<BlockState> checker = (b2) -> EatFromChest.isTrappedChest(b2);

    @Override
    public EatResult eat(final IPokemob pokemob, final NearBlock block)
    {
        if (!pokemob.isHerbivore()) return EatResult.NOEAT;

        final Mob entity = pokemob.getEntity();

        double diff = 1.5;
        diff = Math.max(diff, entity.getBbWidth());
        final double dist = block.getPos().distManhattan(entity.blockPosition());
        this.setWalkTo(entity, block.getPos(), 1, 0);
        if (dist > diff) return EatResult.PATHING;
        final ServerLevel world = (ServerLevel) entity.getCommandSenderWorld();
        final BlockState current = world.getBlockState(block.getPos());
        if (!EatFromChest.checker.test(current)) return EatResult.NOEAT;
        final Container container = (Container) world.getBlockEntity(block.getPos());
        for (int i1 = 0; i1 < container.getContainerSize(); i1++)
        {
            final ItemStack stack = container.getItem(i1);
            if (ItemList.is(HungerTask.FOODTAG, stack))
            {
                pokemob.eat(stack);
                stack.shrink(1);
                if (stack.isEmpty()) container.setItem(i1, ItemStack.EMPTY);
                return EatResult.EATEN;
            }
        }
        return EatResult.NOEAT;
    }

    @Override
    public boolean isValid(final NearBlock block)
    {
        return EatFromChest.checker.test(block.getState());
    }

}
