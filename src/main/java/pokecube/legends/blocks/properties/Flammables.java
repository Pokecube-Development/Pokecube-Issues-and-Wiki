package pokecube.legends.blocks.properties;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FireBlock;
import pokecube.legends.init.BlockInit;
import pokecube.legends.init.PlantsInit;

public class Flammables
{
    public static void flammableBlocks(final Block block, final int speed, final int flammability)
    {
        final FireBlock fire = (FireBlock) Blocks.FIRE;
        fire.setFlammable(block, speed, flammability);
    }

    public static void registerDefaults()
    {
        // Logs
        flammableBlocks(BlockInit.AGED_LOG.get(), 5, 5);
        flammableBlocks(BlockInit.AGED_WOOD.get(), 5, 5);
        flammableBlocks(BlockInit.CORRUPTED_LOG.get(), 5, 5);
        flammableBlocks(BlockInit.CORRUPTED_WOOD.get(), 5, 5);
        flammableBlocks(BlockInit.DISTORTIC_LOG.get(), 5, 5);
        flammableBlocks(BlockInit.DISTORTIC_WOOD.get(), 5, 5);
        flammableBlocks(BlockInit.INVERTED_LOG.get(), 5, 5);
        flammableBlocks(BlockInit.INVERTED_WOOD.get(), 5, 5);
        flammableBlocks(BlockInit.MIRAGE_LOG.get(), 5, 5);
        flammableBlocks(BlockInit.MIRAGE_WOOD.get(), 5, 5);
        flammableBlocks(BlockInit.TEMPORAL_LOG.get(), 5, 5);
        flammableBlocks(BlockInit.TEMPORAL_WOOD.get(), 5, 5);

        // Stripped Logs
        flammableBlocks(BlockInit.STRIP_AGED_LOG.get(), 5, 5);
        flammableBlocks(BlockInit.STRIP_AGED_WOOD.get(), 5, 5);
        flammableBlocks(BlockInit.STRIP_CORRUPTED_LOG.get(), 5, 5);
        flammableBlocks(BlockInit.STRIP_CORRUPTED_WOOD.get(), 5, 5);
        flammableBlocks(BlockInit.STRIP_DISTORTIC_LOG.get(), 5, 5);
        flammableBlocks(BlockInit.STRIP_DISTORTIC_WOOD.get(), 5, 5);
        flammableBlocks(BlockInit.STRIP_INVERTED_LOG.get(), 5, 5);
        flammableBlocks(BlockInit.STRIP_INVERTED_WOOD.get(), 5, 5);
        flammableBlocks(BlockInit.STRIP_MIRAGE_LOG.get(), 5, 5);
        flammableBlocks(BlockInit.STRIP_MIRAGE_WOOD.get(), 5, 5);
        flammableBlocks(BlockInit.STRIP_TEMPORAL_LOG.get(), 5, 5);
        flammableBlocks(BlockInit.STRIP_TEMPORAL_WOOD.get(), 5, 5);

        // Leaves
        flammableBlocks(BlockInit.AGED_LEAVES.get(), 30, 60);
        flammableBlocks(BlockInit.CORRUPTED_LEAVES.get(), 30, 60);
        flammableBlocks(BlockInit.DISTORTIC_LEAVES.get(), 30, 60);
        flammableBlocks(BlockInit.DYNA_LEAVES_PASTEL_PINK.get(), 30, 60);
        flammableBlocks(BlockInit.DYNA_LEAVES_PINK.get(), 30, 60);
        flammableBlocks(BlockInit.DYNA_LEAVES_RED.get(), 30, 60);
        flammableBlocks(BlockInit.INVERTED_LEAVES.get(), 30, 60);
        flammableBlocks(BlockInit.MIRAGE_LEAVES.get(), 30, 60);
        flammableBlocks(BlockInit.TEMPORAL_LEAVES.get(), 30, 60);

        // Planks
        flammableBlocks(BlockInit.AGED_PLANKS.get(), 5, 20);
        flammableBlocks(BlockInit.CORRUPTED_PLANKS.get(), 5, 20);
        flammableBlocks(BlockInit.DISTORTIC_PLANKS.get(), 5, 20);
        flammableBlocks(BlockInit.INVERTED_PLANKS.get(), 5, 20);
        flammableBlocks(BlockInit.MIRAGE_PLANKS.get(), 5, 20);
        flammableBlocks(BlockInit.TEMPORAL_PLANKS.get(), 5, 20);

        // Slabs
        flammableBlocks(BlockInit.AGED_SLAB.get(), 5, 20);
        flammableBlocks(BlockInit.CORRUPTED_SLAB.get(), 5, 20);
        flammableBlocks(BlockInit.DISTORTIC_SLAB.get(), 5, 20);
        flammableBlocks(BlockInit.INVERTED_SLAB.get(), 5, 20);
        flammableBlocks(BlockInit.MIRAGE_SLAB.get(), 5, 20);
        flammableBlocks(BlockInit.TEMPORAL_SLAB.get(), 5, 20);

        // Stairs
        flammableBlocks(BlockInit.AGED_STAIRS.get(), 5, 20);
        flammableBlocks(BlockInit.CORRUPTED_STAIRS.get(), 5, 20);
        flammableBlocks(BlockInit.DISTORTIC_STAIRS.get(), 5, 20);
        flammableBlocks(BlockInit.INVERTED_STAIRS.get(), 5, 20);
        flammableBlocks(BlockInit.MIRAGE_STAIRS.get(), 5, 20);
        flammableBlocks(BlockInit.TEMPORAL_STAIRS.get(), 5, 20);

        // Fences
        flammableBlocks(BlockInit.AGED_FENCE.get(), 5, 20);
        flammableBlocks(BlockInit.CORRUPTED_FENCE.get(), 5, 20);
        flammableBlocks(BlockInit.DISTORTIC_FENCE.get(), 5, 20);
        flammableBlocks(BlockInit.INVERTED_FENCE.get(), 5, 20);
        flammableBlocks(BlockInit.MIRAGE_FENCE.get(), 5, 20);
        flammableBlocks(BlockInit.TEMPORAL_FENCE.get(), 5, 20);

        // Fence Gates
        flammableBlocks(BlockInit.AGED_FENCE_GATE.get(), 5, 20);
        flammableBlocks(BlockInit.CORRUPTED_FENCE_GATE.get(), 5, 20);
        flammableBlocks(BlockInit.DISTORTIC_FENCE_GATE.get(), 5, 20);
        flammableBlocks(BlockInit.INVERTED_FENCE_GATE.get(), 5, 20);
        flammableBlocks(BlockInit.MIRAGE_FENCE_GATE.get(), 5, 20);
        flammableBlocks(BlockInit.TEMPORAL_FENCE_GATE.get(), 5, 20);

        // Plants
        flammableBlocks(BlockInit.STRING_OF_PEARLS.get(), 15, 100);
        flammableBlocks(BlockInit.DYNA_SHRUB.get(), 30, 60);
        flammableBlocks(BlockInit.BIG_CONTAMINATED_DRIPLEAF.get(), 60, 100);
        flammableBlocks(BlockInit.BIG_CONTAMINATED_DRIPLEAF_STEM.get(), 60, 100);
        flammableBlocks(BlockInit.POLLUTING_BLOSSOM.get(), 60, 100);
        flammableBlocks(BlockInit.SMALL_CONTAMINATED_DRIPLEAF.get(), 60, 100);

        flammableBlocks(PlantsInit.AZURE_COLEUS.get(), 60, 100);
        flammableBlocks(PlantsInit.COMPRECED_MUSHROOM.get(), 60, 100);
        flammableBlocks(PlantsInit.CORRUPTED_GRASS.get(), 60, 100);
        flammableBlocks(PlantsInit.DISTORCED_MUSHROOM.get(), 60, 100);
        flammableBlocks(PlantsInit.DISTORTIC_GRASS.get(), 60, 100);
        flammableBlocks(PlantsInit.GOLDEN_ALLIUM.get(), 60, 100);
        flammableBlocks(PlantsInit.GOLDEN_AZURE_BLUET.get(), 60, 100);
        flammableBlocks(PlantsInit.GOLDEN_CORNFLOWER.get(), 60, 100);
        flammableBlocks(PlantsInit.GOLDEN_DANDELION.get(), 60, 100);
        flammableBlocks(PlantsInit.GOLDEN_FERN.get(), 60, 100);
        flammableBlocks(PlantsInit.GOLDEN_GRASS.get(), 60, 100);
        flammableBlocks(PlantsInit.GOLDEN_LILY_VALLEY.get(), 60, 100);
        flammableBlocks(PlantsInit.GOLDEN_ORCHID.get(), 60, 100);
        flammableBlocks(PlantsInit.GOLDEN_OXEYE_DAISY.get(), 60, 100);
        flammableBlocks(PlantsInit.GOLDEN_POPPY.get(), 60, 100);
        flammableBlocks(PlantsInit.GOLDEN_SHROOM_PLANT.get(), 60, 100);
        flammableBlocks(PlantsInit.GOLDEN_SWEET_BERRY_BUSH.get(), 60, 100);
        flammableBlocks(PlantsInit.GOLDEN_TULIP.get(), 60, 100);
        flammableBlocks(PlantsInit.GRACIDEA.get(), 60, 100);
        flammableBlocks(PlantsInit.INVERTED_ORCHID.get(), 60, 100);
        flammableBlocks(PlantsInit.HANGING_TENDRILS.get(), 60, 100);
        flammableBlocks(PlantsInit.HANGING_TENDRILS_PLANT.get(), 60, 100);
        flammableBlocks(PlantsInit.LARGE_GOLDEN_FERN.get(), 60, 100);
        flammableBlocks(PlantsInit.PURPLE_WISTERIA_VINES.get(), 60, 100);
        flammableBlocks(PlantsInit.PURPLE_WISTERIA_VINES_PLANT.get(), 60, 100);
        flammableBlocks(PlantsInit.TAINTED_ROOTS.get(), 60, 100);
        flammableBlocks(PlantsInit.TALL_CORRUPTED_GRASS.get(), 60, 100);
        flammableBlocks(PlantsInit.TALL_GOLDEN_GRASS.get(), 60, 100);
        flammableBlocks(PlantsInit.TEMPORAL_BAMBOO.get(), 60, 60);

        // Bookshelves
        flammableBlocks(BlockInit.AGED_BOOKSHELF.get(), 5, 20);
        flammableBlocks(BlockInit.CORRUPTED_BOOKSHELF.get(), 5, 20);
        flammableBlocks(BlockInit.DISTORTIC_BOOKSHELF.get(), 5, 20);
        flammableBlocks(BlockInit.INVERTED_BOOKSHELF.get(), 5, 20);
        flammableBlocks(BlockInit.MIRAGE_BOOKSHELF.get(), 5, 20);
        flammableBlocks(BlockInit.TEMPORAL_BOOKSHELF.get(), 5, 20);
    }
}
