package pokecube.legends.init;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.Material;
import net.minecraftforge.registries.RegistryObject;
import pokecube.core.init.ItemGenerator;
import pokecube.legends.PokecubeLegends;
import pokecube.legends.blocks.plants.PottedCrystallizedBush;
import pokecube.legends.blocks.plants.PottedCrystallizedCactus;

public class PottedPlantsInit
{
    public static final RegistryObject<Block> POTTED_AGED_SAPLING;
    public static final RegistryObject<Block> POTTED_CORRUPTED_SAPLING;
    public static final RegistryObject<Block> POTTED_DISTORTIC_SAPLING;
    public static final RegistryObject<Block> POTTED_INVERTED_SAPLING;
    public static final RegistryObject<Block> POTTED_MIRAGE_SAPLING;
    public static final RegistryObject<Block> POTTED_TEMPORAL_SAPLING;

    public static final RegistryObject<Block> POTTED_AZURE_COLEUS;
    public static final RegistryObject<Block> POTTED_COMPRECED_MUSHROOM;
    public static final RegistryObject<Block> POTTED_CORRUPTED_GRASS;
    public static final RegistryObject<Block> POTTED_CRYSTALLIZED_BUSH;
    public static final RegistryObject<Block> POTTED_CRYSTALLIZED_CACTUS;
    public static final RegistryObject<Block> POTTED_DISTORCED_MUSHROOM;
    public static final RegistryObject<Block> POTTED_DISTORTIC_GRASS;
    public static final RegistryObject<Block> POTTED_DISTORTIC_VINES;
    public static final RegistryObject<Block> POTTED_DYNA_SHRUB;
    public static final RegistryObject<Block> POTTED_GOLDEN_ALLIUM;
    public static final RegistryObject<Block> POTTED_GOLDEN_AZURE_BLUET;
    public static final RegistryObject<Block> POTTED_GOLDEN_CORNFLOWER;
    public static final RegistryObject<Block> POTTED_GOLDEN_DANDELION;
    public static final RegistryObject<Block> POTTED_GOLDEN_FERN;
    public static final RegistryObject<Block> POTTED_GOLDEN_GRASS;
    public static final RegistryObject<Block> POTTED_GOLDEN_LILY_VALLEY;
    public static final RegistryObject<Block> POTTED_GOLDEN_ORCHID;
    public static final RegistryObject<Block> POTTED_GOLDEN_OXEYE_DAISY;
    public static final RegistryObject<Block> POTTED_GOLDEN_POPPY;
    public static final RegistryObject<Block> POTTED_GOLDEN_SHROOM;
    public static final RegistryObject<Block> POTTED_GOLDEN_SWEET_BERRY_BUSH;
    public static final RegistryObject<Block> POTTED_GOLDEN_TULIP;
    public static final RegistryObject<Block> POTTED_GRACIDEA;
    public static final RegistryObject<Block> POTTED_HANGING_TENDRILS;
    public static final RegistryObject<Block> POTTED_INVERTED_ORCHID;
    public static final RegistryObject<Block> POTTED_LARGE_GOLDEN_FERN;
    public static final RegistryObject<Block> POTTED_PINK_LILY;
    public static final RegistryObject<Block> POTTED_POLLUTING_BLOSSOM;
    public static final RegistryObject<Block> POTTED_PURPLE_WISTERIA_VINES;
    public static final RegistryObject<Block> POTTED_STRING_OF_PEARLS;
    public static final RegistryObject<Block> POTTED_TAINTED_ROOTS;
    public static final RegistryObject<Block> POTTED_TALL_CRYSTALLIZED_BUSH;
    public static final RegistryObject<Block> POTTED_TALL_CORRUPTED_GRASS;
    public static final RegistryObject<Block> POTTED_TALL_GOLDEN_GRASS;
    public static final RegistryObject<Block> POTTED_TEMPORAL_BAMBOO;

    static
    {
        // No Tab
        POTTED_AGED_SAPLING = PokecubeLegends.NO_TAB.register("potted_aged_sapling",
                () -> new ItemGenerator.GenericPottedPlant(BlockInit.AGED_SAPLING.get(),
                        BlockBehaviour.Properties.of(Material.DECORATION).instabreak().noOcclusion()));
        POTTED_AZURE_COLEUS = PokecubeLegends.NO_TAB.register("potted_azure_coleus",
                () -> new ItemGenerator.GenericPottedPlant(PlantsInit.AZURE_COLEUS.get(),
                        BlockBehaviour.Properties.of(Material.DECORATION).instabreak().noOcclusion()));
        POTTED_CORRUPTED_SAPLING = PokecubeLegends.NO_TAB.register("potted_corrupted_sapling",
                () -> new ItemGenerator.GenericPottedPlant(BlockInit.CORRUPTED_SAPLING.get(),
                        BlockBehaviour.Properties.of(Material.DECORATION).instabreak().noOcclusion()));
        POTTED_CORRUPTED_GRASS = PokecubeLegends.NO_TAB.register("potted_corrupted_grass",
                () -> new ItemGenerator.GenericPottedPlant(PlantsInit.CORRUPTED_GRASS.get(),
                        BlockBehaviour.Properties.of(Material.DECORATION).instabreak().noOcclusion()));
        POTTED_DISTORTIC_SAPLING = PokecubeLegends.NO_TAB.register("potted_distortic_sapling",
                () -> new ItemGenerator.GenericPottedPlant(BlockInit.DISTORTIC_SAPLING.get(),
                        BlockBehaviour.Properties.of(Material.DECORATION).instabreak().noOcclusion()));
        POTTED_INVERTED_SAPLING = PokecubeLegends.NO_TAB.register("potted_inverted_sapling",
                () -> new ItemGenerator.GenericPottedPlant(BlockInit.INVERTED_SAPLING.get(),
                        BlockBehaviour.Properties.of(Material.DECORATION).instabreak().noOcclusion()));
        POTTED_MIRAGE_SAPLING = PokecubeLegends.NO_TAB.register("potted_mirage_sapling",
                () -> new ItemGenerator.GenericPottedPlant(BlockInit.MIRAGE_SAPLING.get(),
                        BlockBehaviour.Properties.of(Material.DECORATION).instabreak().noOcclusion()));
        POTTED_TEMPORAL_SAPLING = PokecubeLegends.NO_TAB.register("potted_temporal_sapling",
                () -> new ItemGenerator.GenericPottedPlant(BlockInit.TEMPORAL_SAPLING.get(),
                        BlockBehaviour.Properties.of(Material.DECORATION).instabreak().noOcclusion()));

        POTTED_COMPRECED_MUSHROOM = PokecubeLegends.NO_TAB.register("potted_compreced_mushroom",
                () -> new ItemGenerator.GenericPottedPlant(PlantsInit.COMPRECED_MUSHROOM.get(),
                        BlockBehaviour.Properties.of(Material.DECORATION).instabreak().noOcclusion()));
        POTTED_CRYSTALLIZED_BUSH = PokecubeLegends.NO_TAB.register("potted_crystallized_bush",
                () -> new PottedCrystallizedBush(BlockInit.CRYSTALLIZED_BUSH.get(),
                        BlockBehaviour.Properties.of(Material.DECORATION).instabreak().noOcclusion()));
        POTTED_CRYSTALLIZED_CACTUS = PokecubeLegends.NO_TAB.register("potted_crystallized_cactus",
                () -> new PottedCrystallizedCactus(BlockInit.CRYSTALLIZED_CACTUS.get(),
                        BlockBehaviour.Properties.of(Material.DECORATION).instabreak().noOcclusion()));
        POTTED_DISTORCED_MUSHROOM = PokecubeLegends.NO_TAB.register("potted_distorced_mushroom",
                () -> new ItemGenerator.GenericPottedPlant(PlantsInit.DISTORCED_MUSHROOM.get(),
                        BlockBehaviour.Properties.of(Material.DECORATION).instabreak().noOcclusion()));
        POTTED_DISTORTIC_GRASS = PokecubeLegends.NO_TAB.register("potted_distortic_grass",
                () -> new ItemGenerator.GenericPottedPlant(PlantsInit.DISTORTIC_GRASS.get(),
                        BlockBehaviour.Properties.of(Material.DECORATION).instabreak().noOcclusion()));
        POTTED_DISTORTIC_VINES = PokecubeLegends.NO_TAB.register("potted_distortic_vines",
                () -> new ItemGenerator.GenericPottedPlant(PlantsInit.DISTORTIC_VINES.get(),
                        BlockBehaviour.Properties.of(Material.DECORATION).instabreak().noOcclusion()));
        POTTED_DYNA_SHRUB = PokecubeLegends.NO_TAB.register("potted_dyna_shrub",
                () -> new ItemGenerator.GenericPottedPlant(BlockInit.DYNA_SHRUB.get(),
                        BlockBehaviour.Properties.of(Material.DECORATION).instabreak().noOcclusion()));
        POTTED_GOLDEN_ALLIUM = PokecubeLegends.NO_TAB.register("potted_golden_allium",
                () -> new ItemGenerator.GenericPottedPlant(PlantsInit.GOLDEN_ALLIUM.get(),
                        BlockBehaviour.Properties.of(Material.DECORATION).instabreak().noOcclusion()));
        POTTED_GOLDEN_AZURE_BLUET = PokecubeLegends.NO_TAB.register("potted_golden_azure_bluet",
                () -> new ItemGenerator.GenericPottedPlant(PlantsInit.GOLDEN_AZURE_BLUET.get(),
                        BlockBehaviour.Properties.of(Material.DECORATION).instabreak().noOcclusion()));
        POTTED_GOLDEN_CORNFLOWER = PokecubeLegends.NO_TAB.register("potted_golden_cornflower",
                () -> new ItemGenerator.GenericPottedPlant(PlantsInit.GOLDEN_CORNFLOWER.get(),
                        BlockBehaviour.Properties.of(Material.DECORATION).instabreak().noOcclusion()));
        POTTED_GOLDEN_DANDELION = PokecubeLegends.NO_TAB.register("potted_golden_dandelion",
                () -> new ItemGenerator.GenericPottedPlant(PlantsInit.GOLDEN_DANDELION.get(),
                        BlockBehaviour.Properties.of(Material.DECORATION).instabreak().noOcclusion()));
        POTTED_GOLDEN_LILY_VALLEY = PokecubeLegends.NO_TAB.register("potted_golden_lily_of_the_valley",
                () -> new ItemGenerator.GenericPottedPlant(PlantsInit.GOLDEN_LILY_VALLEY.get(),
                        BlockBehaviour.Properties.of(Material.DECORATION).instabreak().noOcclusion()));
        POTTED_GOLDEN_FERN = PokecubeLegends.NO_TAB.register("potted_golden_fern",
                () -> new ItemGenerator.GenericPottedPlant(PlantsInit.GOLDEN_FERN.get(),
                        BlockBehaviour.Properties.of(Material.DECORATION).instabreak().noOcclusion()));
        POTTED_GOLDEN_GRASS = PokecubeLegends.NO_TAB.register("potted_golden_grass",
                () -> new ItemGenerator.GenericPottedPlant(PlantsInit.GOLDEN_GRASS.get(),
                        BlockBehaviour.Properties.of(Material.DECORATION).instabreak().noOcclusion()));
        POTTED_GOLDEN_ORCHID = PokecubeLegends.NO_TAB.register("potted_golden_orchid",
                () -> new ItemGenerator.GenericPottedPlant(PlantsInit.GOLDEN_ORCHID.get(),
                        BlockBehaviour.Properties.of(Material.DECORATION).instabreak().noOcclusion()));
        POTTED_GOLDEN_OXEYE_DAISY = PokecubeLegends.NO_TAB.register("potted_golden_oxeye_daisy",
                () -> new ItemGenerator.GenericPottedPlant(PlantsInit.GOLDEN_OXEYE_DAISY.get(),
                        BlockBehaviour.Properties.of(Material.DECORATION).instabreak().noOcclusion()));
        POTTED_GOLDEN_POPPY = PokecubeLegends.NO_TAB.register("potted_golden_poppy",
                () -> new ItemGenerator.GenericPottedPlant(PlantsInit.GOLDEN_POPPY.get(),
                        BlockBehaviour.Properties.of(Material.DECORATION).instabreak().noOcclusion()));
        POTTED_GOLDEN_SHROOM = PokecubeLegends.NO_TAB.register("potted_golden_shroom",
                () -> new ItemGenerator.GenericPottedPlant(PlantsInit.GOLDEN_SHROOM_PLANT.get(),
                        BlockBehaviour.Properties.of(Material.DECORATION).instabreak().noOcclusion()));
        POTTED_GOLDEN_SWEET_BERRY_BUSH = PokecubeLegends.NO_TAB.register("potted_golden_sweet_berry_bush",
                () -> new ItemGenerator.GenericPottedPlant(PlantsInit.GOLDEN_SWEET_BERRY_BUSH.get(),
                        BlockBehaviour.Properties.of(Material.DECORATION).instabreak().noOcclusion()));
        POTTED_GOLDEN_TULIP = PokecubeLegends.NO_TAB.register("potted_golden_tulip",
                () -> new ItemGenerator.GenericPottedPlant(PlantsInit.GOLDEN_TULIP.get(),
                        BlockBehaviour.Properties.of(Material.DECORATION).instabreak().noOcclusion()));
        POTTED_GRACIDEA = PokecubeLegends.NO_TAB.register("potted_gracidea",
                () -> new ItemGenerator.GenericPottedPlant(PlantsInit.GRACIDEA.get(),
                        BlockBehaviour.Properties.of(Material.DECORATION).instabreak().noOcclusion()));
        POTTED_INVERTED_ORCHID = PokecubeLegends.NO_TAB.register("potted_inverted_orchid",
                () -> new ItemGenerator.GenericPottedPlant(PlantsInit.INVERTED_ORCHID.get(),
                        BlockBehaviour.Properties.of(Material.DECORATION).instabreak().noOcclusion()));
        POTTED_HANGING_TENDRILS = PokecubeLegends.NO_TAB.register("potted_hanging_tendrils",
                () -> new ItemGenerator.GenericPottedPlant(PlantsInit.HANGING_TENDRILS.get(),
                        BlockBehaviour.Properties.of(Material.DECORATION).instabreak().noOcclusion()));
        POTTED_LARGE_GOLDEN_FERN = PokecubeLegends.NO_TAB.register("potted_large_golden_fern",
                () -> new ItemGenerator.GenericPottedPlant(PlantsInit.LARGE_GOLDEN_FERN.get(),
                        BlockBehaviour.Properties.of(Material.DECORATION).instabreak().noOcclusion()));
        POTTED_PINK_LILY = PokecubeLegends.NO_TAB.register("potted_pink_blossom_lily",
                () -> new ItemGenerator.GenericPottedPlant(PlantsInit.PINK_TAINTED_LILY_PAD.get(),
                        BlockBehaviour.Properties.of(Material.DECORATION).instabreak().noOcclusion()));
        POTTED_POLLUTING_BLOSSOM = PokecubeLegends.NO_TAB.register("potted_polluting_blossom",
                () -> new ItemGenerator.GenericPottedPlant(BlockInit.POLLUTING_BLOSSOM.get(),
                        BlockBehaviour.Properties.of(Material.DECORATION).instabreak().noOcclusion()));
        POTTED_PURPLE_WISTERIA_VINES = PokecubeLegends.NO_TAB.register("potted_purple_wisteria_vines",
                () -> new ItemGenerator.GenericPottedPlant(PlantsInit.PURPLE_WISTERIA_VINES.get(),
                        BlockBehaviour.Properties.of(Material.DECORATION).instabreak().noOcclusion()));
        POTTED_STRING_OF_PEARLS = PokecubeLegends.NO_TAB.register("potted_string_of_pearls",
                () -> new ItemGenerator.GenericPottedPlant(BlockInit.STRING_OF_PEARLS.get(),
                        BlockBehaviour.Properties.of(Material.DECORATION).instabreak().noOcclusion()));
        POTTED_TAINTED_ROOTS = PokecubeLegends.NO_TAB.register("potted_tainted_roots",
                () -> new ItemGenerator.GenericPottedPlant(PlantsInit.TAINTED_ROOTS.get(),
                        BlockBehaviour.Properties.of(Material.DECORATION).instabreak().noOcclusion()));
        POTTED_TALL_CRYSTALLIZED_BUSH = PokecubeLegends.NO_TAB.register("potted_tall_crystallized_bush",
                () -> new PottedCrystallizedBush(BlockInit.TALL_CRYSTALLIZED_BUSH.get(),
                        BlockBehaviour.Properties.of(Material.DECORATION).instabreak().noOcclusion()));
        POTTED_TALL_CORRUPTED_GRASS = PokecubeLegends.NO_TAB.register("potted_tall_corrupted_grass",
                () -> new ItemGenerator.GenericPottedPlant(PlantsInit.TALL_CORRUPTED_GRASS.get(),
                        BlockBehaviour.Properties.of(Material.DECORATION).instabreak().noOcclusion()));
        POTTED_TALL_GOLDEN_GRASS = PokecubeLegends.NO_TAB.register("potted_tall_golden_grass",
                () -> new ItemGenerator.GenericPottedPlant(PlantsInit.TALL_GOLDEN_GRASS.get(),
                        BlockBehaviour.Properties.of(Material.DECORATION).instabreak().noOcclusion()));
        POTTED_TEMPORAL_BAMBOO = PokecubeLegends.NO_TAB.register("potted_temporal_bamboo",
                () -> new ItemGenerator.GenericPottedPlant(PlantsInit.TEMPORAL_BAMBOO.get(),
                        BlockBehaviour.Properties.of(Material.DECORATION).instabreak().noOcclusion()));
    }

    public static void registry()
    {

    }
}
