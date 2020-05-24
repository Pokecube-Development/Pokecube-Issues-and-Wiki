package pokecube.core.ai.tasks.idle.hunger;

import java.util.List;
import java.util.function.Predicate;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.MobEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.world.server.ServerWorld;
import pokecube.core.ai.brain.sensors.NearBlocks.NearBlock;
import pokecube.core.ai.tasks.TaskBase.InventoryChange;
import pokecube.core.ai.tasks.utility.GatherTask.ReplantTask;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.world.terrain.PokecubeTerrainChecker;

public class EatPlant extends EatBlockBase
{
    private static boolean isHerb(final BlockState state)
    {
        return PokecubeTerrainChecker.isFruit(state) || PokecubeTerrainChecker.isEdiblePlant(state);
    }

    private static final Predicate<BlockState> checker = (b2) -> EatPlant.isHerb(b2);

    @Override
    public EatResult eat(final IPokemob pokemob, final NearBlock block)
    {
        if (!pokemob.isHerbivore()) return EatResult.NOEAT;

        final MobEntity entity = pokemob.getEntity();

        double diff = 1.5;
        diff = Math.max(diff, entity.getWidth());
        final double dist = block.getPos().manhattanDistance(entity.getPosition());
        this.setWalkTo(entity, block.getPos(), pokemob.getMovementSpeed(), 0);
        if (dist > diff) return EatResult.PATHING;

        final ServerWorld world = (ServerWorld) entity.getEntityWorld();
        final BlockState current = world.getBlockState(block.getPos());
        if (!EatPlant.checker.test(current)) return EatResult.NOEAT;

        final List<ItemStack> list = Block.getDrops(current, world, block.getPos(), null);
        if (list.isEmpty()) return EatResult.NOEAT;
        final ItemStack first = list.get(0);
        pokemob.eat(first);
        first.grow(-1);
        if (first.isEmpty()) list.remove(0);
        boolean replanted = false;
        // See if anything dropped was a seed for the thing we
        // picked.
        for (final ItemStack stack : list)
        {
            // If so, Replant it.
            if (!replanted) replanted = new ReplantTask(stack, current, block.getPos(), true).run(world);
            new InventoryChange(entity, 2, stack, true).run(world);
        }
        return EatResult.EATEN;
    }

    @Override
    public boolean isValid(final NearBlock block)
    {
        return EatPlant.checker.test(block.getState());
    }

}
