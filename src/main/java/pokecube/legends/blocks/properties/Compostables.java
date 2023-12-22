package pokecube.legends.blocks.properties;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.ComposterBlock;
import net.minecraftforge.registries.RegistryObject;
import pokecube.legends.init.BlockInit;
import pokecube.legends.init.PlantsInit;

public class Compostables
{

    public static void compostableBlocks(final float chance, final RegistryObject<Block> item)
    {
        ComposterBlock.COMPOSTABLES.put(item.get().asItem(), chance);
    }

    public static void registerDefaults()
    {
        compostableBlocks(0.3f, BlockInit.AGED_LEAVES);
        compostableBlocks(0.3f, BlockInit.AGED_SAPLING);
        compostableBlocks(0.3f, BlockInit.CORRUPTED_LEAVES);
        compostableBlocks(0.3f, BlockInit.CORRUPTED_SAPLING);
        compostableBlocks(0.3f, BlockInit.DISTORTIC_LEAVES);
        compostableBlocks(0.3f, BlockInit.DISTORTIC_SAPLING);
        compostableBlocks(0.3f, BlockInit.DYNA_LEAVES_PASTEL_PINK);
        compostableBlocks(0.3f, BlockInit.DYNA_LEAVES_PINK);
        compostableBlocks(0.3f, BlockInit.DYNA_LEAVES_RED);
        compostableBlocks(0.3f, BlockInit.INVERTED_LEAVES);
        compostableBlocks(0.3f, BlockInit.INVERTED_SAPLING);
        compostableBlocks(0.3f, BlockInit.MIRAGE_LEAVES);
        compostableBlocks(0.3f, BlockInit.MIRAGE_SAPLING);
        compostableBlocks(0.3f, BlockInit.SMALL_CONTAMINATED_DRIPLEAF);
        compostableBlocks(0.3f, BlockInit.TEMPORAL_LEAVES);
        compostableBlocks(0.3f, BlockInit.TEMPORAL_SAPLING);
        compostableBlocks(0.3f, PlantsInit.CORRUPTED_GRASS);
        compostableBlocks(0.3f, PlantsInit.DISTORTIC_GRASS);
        compostableBlocks(0.3f, PlantsInit.GOLDEN_GRASS);
        compostableBlocks(0.3f, PlantsInit.GOLDEN_SWEET_BERRY_BUSH);
        compostableBlocks(0.3f, PlantsInit.HANGING_TENDRILS);
        compostableBlocks(0.3f, PlantsInit.PURPLE_WISTERIA_VINES);
        compostableBlocks(0.3f, PlantsInit.TAINTED_KELP);
        compostableBlocks(0.3f, PlantsInit.TAINTED_SEAGRASS);

        compostableBlocks(0.5f, BlockInit.STRING_OF_PEARLS);
        compostableBlocks(0.5f, PlantsInit.TALL_CORRUPTED_GRASS);
        compostableBlocks(0.5f, PlantsInit.TALL_GOLDEN_GRASS);

        compostableBlocks(0.65f, BlockInit.BIG_CONTAMINATED_DRIPLEAF);
        compostableBlocks(0.65f, BlockInit.DYNA_SHRUB);
        compostableBlocks(0.65f, BlockInit.POLLUTING_BLOSSOM);
        compostableBlocks(0.65f, PlantsInit.AZURE_COLEUS);
        compostableBlocks(0.65f, PlantsInit.COMPRECED_MUSHROOM);
        compostableBlocks(0.65f, PlantsInit.DISTORCED_MUSHROOM);
        compostableBlocks(0.65f, PlantsInit.GOLDEN_ALLIUM);
        compostableBlocks(0.65f, PlantsInit.GOLDEN_AZURE_BLUET);
        compostableBlocks(0.65f, PlantsInit.GOLDEN_CORNFLOWER);
        compostableBlocks(0.65f, PlantsInit.GOLDEN_DANDELION);
        compostableBlocks(0.65f, PlantsInit.GOLDEN_FERN);
        compostableBlocks(0.65f, PlantsInit.GOLDEN_LILY_VALLEY);
        compostableBlocks(0.65f, PlantsInit.GOLDEN_ORCHID);
        compostableBlocks(0.65f, PlantsInit.GOLDEN_OXEYE_DAISY);
        compostableBlocks(0.65f, PlantsInit.GOLDEN_POPPY);
        compostableBlocks(0.65f, PlantsInit.GOLDEN_SHROOM_PLANT);
        compostableBlocks(0.65f, PlantsInit.GOLDEN_TULIP);
        compostableBlocks(0.65f, PlantsInit.GRACIDEA);
        compostableBlocks(0.65f, PlantsInit.INVERTED_ORCHID);
        compostableBlocks(0.65f, PlantsInit.LARGE_GOLDEN_FERN);
        compostableBlocks(0.65f, PlantsInit.PINK_TAINTED_LILY_PAD);
        compostableBlocks(0.65f, PlantsInit.TAINTED_LILY_PAD);
        compostableBlocks(0.65f, PlantsInit.TAINTED_ROOTS);
        compostableBlocks(0.65f, PlantsInit.TALL_TAINTED_SEAGRASS);
        compostableBlocks(0.65f, PlantsInit.TEMPORAL_BAMBOO);

        compostableBlocks(0.75f, BlockInit.CRYSTALLIZED_CACTUS);
    }
}
