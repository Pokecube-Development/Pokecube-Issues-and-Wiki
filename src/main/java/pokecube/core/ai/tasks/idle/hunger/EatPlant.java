package pokecube.core.ai.tasks.idle.hunger;

import java.util.List;
import java.util.function.Predicate;

import com.google.common.collect.Lists;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import pokecube.core.PokecubeCore;
import pokecube.core.ai.brain.sensors.NearBlocks.NearBlock;
import pokecube.core.ai.tasks.TaskBase.InventoryChange;
import pokecube.core.ai.tasks.utility.GatherTask.ReplantTask;
import pokecube.core.handlers.events.MoveEventsHandler;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.world.terrain.PokecubeTerrainChecker;
import thut.api.maths.Vector3;

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

        final Mob entity = pokemob.getEntity();

        double diff = 1.5;
        diff = Math.max(diff, entity.getBbWidth());
        final double dist = block.getPos().distManhattan(entity.blockPosition());
        this.setWalkTo(entity, block.getPos(), 1, 0);
        if (dist > diff) return EatResult.PATHING;

        final ServerLevel world = (ServerLevel) entity.getLevel();
        final BlockState current = world.getBlockState(block.getPos());
        if (!EatPlant.checker.test(current)) return EatResult.NOEAT;

        List<ItemStack> list = Block.getDrops(current, world, block.getPos(), null);
        if (list.isEmpty()) return EatResult.NOEAT;

        // Copy the list incase the original was immutable.
        list = Lists.newArrayList(list);
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

        if (PokecubeCore.getConfig().pokemobsEatPlants)
        {
            // If we are allowed to, we remove the eaten block
            final boolean canEat = MoveEventsHandler.canAffectBlock(pokemob, Vector3.getNewVector().set(block.getPos()),
                    "nom_nom_nom", false, false);
            if (canEat) world.setBlockAndUpdate(block.getPos(), Blocks.AIR.defaultBlockState());
        }
        return EatResult.EATEN;
    }

    @Override
    public boolean isValid(final NearBlock block)
    {
        return EatPlant.checker.test(block.getState());
    }

}
