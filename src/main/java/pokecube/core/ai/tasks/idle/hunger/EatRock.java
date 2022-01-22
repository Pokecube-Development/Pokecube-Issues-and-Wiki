package pokecube.core.ai.tasks.idle.hunger;

import java.util.List;
import java.util.function.Predicate;

import net.minecraft.resources.ResourceLocation;
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
import thut.api.item.ItemList;
import thut.api.maths.Vector3;

public class EatRock extends EatBlockBase
{
    private static final ResourceLocation ORE = new ResourceLocation("forge", "ores");

    private static final ResourceLocation COBBLE = new ResourceLocation("forge", "cobblestone");

    private static final Predicate<BlockState> checker = (b2) -> PokecubeTerrainChecker.isRock(b2);

    @Override
    public EatResult eat(final IPokemob pokemob, final NearBlock block)
    {
        if (!pokemob.isLithotroph()) return EatResult.NOEAT;

        final Mob entity = pokemob.getEntity();
        double diff = 1.5;
        diff = Math.max(diff, entity.getBbWidth());
        final double dist = block.getPos().distManhattan(entity.blockPosition());
        this.setWalkTo(entity, block.getPos(), 1, 0);
        if (dist > diff) return EatResult.PATHING;

        final ServerLevel world = (ServerLevel) entity.getLevel();
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

        if (PokecubeCore.getConfig().pokemobsEatRocks)
        {
            BlockState drop = Blocks.COBBLESTONE.defaultBlockState();
            if (ItemList.is(EatRock.COBBLE, current)) drop = Blocks.GRAVEL.defaultBlockState();
            if (PokecubeCore.getConfig().pokemobsEatGravel && drop.getBlock() == Blocks.GRAVEL) drop = Blocks.AIR
                    .defaultBlockState();
            // If we are allowed to, we remove the eaten block
            final boolean canEat = MoveEventsHandler.canAffectBlock(pokemob, new Vector3().set(block.getPos()),
                    "nom_nom_nom", false, false);
            if (canEat) world.setBlockAndUpdate(block.getPos(), drop);
        }
        return EatResult.EATEN;
    }

    @Override
    public boolean isValid(final NearBlock block)
    {
        return EatRock.checker.test(block.getState());
    }

}