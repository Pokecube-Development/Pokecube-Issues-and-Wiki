package pokecube.core.ai.tasks.idle.hunger;

import java.util.List;
import java.util.function.Predicate;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.MobEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.server.ServerWorld;
import pokecube.core.ai.brain.sensors.NearBlocks.NearBlock;
import pokecube.core.ai.tasks.TaskBase.InventoryChange;
import pokecube.core.ai.tasks.utility.AIGatherStuff.ReplantTask;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.world.terrain.PokecubeTerrainChecker;
import thut.api.item.ItemList;

public class EatRock extends EatBlockBase
{
    private static final ResourceLocation ORE = new ResourceLocation("forge", "ores");

    private static final Predicate<BlockState> checker = (b2) -> PokecubeTerrainChecker.isRock(b2);

    @Override
    public EatResult eat(final IPokemob pokemob, final NearBlock block)
    {
        if (!pokemob.isLithotroph()) return EatResult.NOEAT;

        final MobEntity entity = pokemob.getEntity();
        double diff = 1.5;
        diff = Math.max(diff, entity.getWidth());
        final double dist = block.getPos().manhattanDistance(entity.getPosition());
        this.setWalkTo(entity, block.getPos(), pokemob.getMovementSpeed(), 0);
        if (dist > diff) return EatResult.PATHING;

        final ServerWorld world = (ServerWorld) entity.getEntityWorld();
        final BlockState current = world.getBlockState(block.getPos());
        if (!EatRock.checker.test(current)) return EatResult.NOEAT;

        final List<ItemStack> list = Block.getDrops(current, world, block.getPos(), null);
        if (list.isEmpty()) return EatResult.NOEAT;
        final ItemStack first = list.get(0);
        final boolean isOre = ItemList.is(EatRock.ORE, first);
        pokemob.eat(first);
        first.grow(-1);
        if (first.isEmpty()) list.remove(0);
        if (isOre) list.add(0, new ItemStack(Blocks.COBBLESTONE));
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
        return EatRock.checker.test(block.getState());
    }

}