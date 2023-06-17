package pokecube.core.ai.tasks.idle.hunger;

import java.util.List;
import java.util.function.Predicate;

import com.google.common.collect.Lists;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.core.PokecubeCore;
import pokecube.core.ai.brain.sensors.NearBlocks.NearBlock;
import pokecube.core.ai.tasks.TaskBase.InventoryChange;
import pokecube.core.ai.tasks.utility.GatherTask.ReplantTask;
import pokecube.core.eventhandlers.MoveEventsHandler;
import pokecube.world.terrain.PokecubeTerrainChecker;
import thut.api.item.ItemList;
import thut.api.maths.Vector3;
import java.util.function.Supplier;

public class EatRock extends EatBlockBase
{
    public static record CobbleConversion(ResourceLocation tag, Supplier<BlockState> cobble)
    {
    };

    public static List<CobbleConversion> toCobble = new ArrayList<>();

    static
    {
        toCobble.add(new CobbleConversion(new ResourceLocation("forge", "ores"), Blocks.COBBLESTONE::defaultBlockState));
        toCobble.add(new CobbleConversion(new ResourceLocation("forge", "cobblestone"), Blocks.COBBLESTONE::defaultBlockState));
    }

    private static final ResourceLocation ORE = new ResourceLocation("forge", "ores_in_ground/stone");

    private static final ResourceLocation DEEPSLATE_ORE = new ResourceLocation("forge", "ores_in_ground/deepslate");

    private static final ResourceLocation NETHER_ORE = new ResourceLocation("forge", "ores_in_ground/netherrack");

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

        List<ItemStack> list = Block.getDrops(current, world, block.getPos(), null);
        if (list.isEmpty()) return EatResult.NOEAT;
        
        // Clear immutability if some mod makes an immutable list...
        list = Lists.newArrayList(list);

        final ItemStack first = list.get(0);
        final boolean isOre = ItemList.is(EatRock.ORE, first);
        final boolean isDeepslateOre = ItemList.is(EatRock.DEEPSLATE_ORE, first);
        final boolean isNetherOre = ItemList.is(EatRock.NETHER_ORE, first);
        pokemob.eat(first);
        first.grow(-1);
        if (first.isEmpty()) list.remove(0);
        if (isOre) list.add(0, new ItemStack(Blocks.COBBLESTONE));
        else if (isDeepslateOre) list.add(0, new ItemStack(Blocks.COBBLED_DEEPSLATE));
        else if (isNetherOre) list.add(0, new ItemStack(Blocks.NETHERRACK));
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

            for (var conversion : toCobble)
            {
                if (ItemList.is(conversion.tag(), current))
                {
                    drop = conversion.cobble().get();
                    break;
                }
            }

            if (PokecubeCore.getConfig().pokemobsEatGravel && drop.getBlock() == Blocks.GRAVEL)
                drop = Blocks.AIR.defaultBlockState();
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