package pokecube.legends.init;

import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DoublePlantBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.material.MaterialColor;
import net.minecraftforge.fmllegacy.RegistryObject;
import pokecube.legends.PokecubeLegends;
import pokecube.legends.blocks.FlowerBase;
import pokecube.legends.blocks.MushroomBase;
import pokecube.legends.blocks.plants.BlossomLilyPadBlock;
import pokecube.legends.blocks.plants.DistortedVinesBlock;
import pokecube.legends.blocks.plants.DistortedVinesTopBlock;
import pokecube.legends.blocks.plants.GoldenSweetBerryBushBlock;
import pokecube.legends.blocks.plants.LilyPadBlock;
import pokecube.legends.blocks.plants.TaintedKelpBlock;
import pokecube.legends.blocks.plants.TaintedKelpPlantBlock;
import pokecube.legends.blocks.plants.TaintedRootsBlock;
import pokecube.legends.blocks.plants.TaintedSeagrassBlock;
import pokecube.legends.blocks.plants.TallCorruptedGrassBlock;
import pokecube.legends.blocks.plants.TallGoldenGrassBlock;
import pokecube.legends.blocks.plants.TallTaintedSeagrassBlock;
import pokecube.legends.blocks.plants.TemporalBambooBlock;
import pokecube.legends.blocks.plants.TemporalBambooShootBlock;

public class PlantsInit
{
    // Plants
    public static final RegistryObject<Block> COMPRECED_MUSHROOM;
    public static final RegistryObject<Block> CORRUPTED_GRASS;
    public static final RegistryObject<Block> DISTORCED_MUSHROOM;
    public static final RegistryObject<Block> GOLDEN_FERN;
    public static final RegistryObject<Block> LARGE_GOLDEN_FERN;
    public static final RegistryObject<Block> GOLDEN_GRASS;
    public static final RegistryObject<Block> TALL_GOLDEN_GRASS;
    public static final RegistryObject<Block> GOLDEN_POPPY;
    public static final RegistryObject<Block> GOLDEN_SWEET_BERRY_BUSH;
    public static final RegistryObject<Block> INVERTED_ORCHID;
    public static final RegistryObject<Block> TAINTED_KELP;
    public static final RegistryObject<Block> TAINTED_KELP_PLANT;
    public static final RegistryObject<Block> PINK_TAINTED_LILY_PAD;
    public static final RegistryObject<Block> TAINTED_LILY_PAD;
    public static final RegistryObject<Block> TAINTED_ROOTS;
    public static final RegistryObject<Block> TAINTED_SEAGRASS;
    public static final RegistryObject<Block> TALL_TAINTED_SEAGRASS;
    public static final RegistryObject<Block> TEMPORAL_BAMBOO;
    public static final RegistryObject<Block> TEMPORAL_BAMBOO_SHOOT;
    public static final RegistryObject<Block> DISTORTIC_VINES;
    public static final RegistryObject<Block> DISTORTIC_VINES_PLANT;

    static
    {
        COMPRECED_MUSHROOM = PokecubeLegends.DIMENSIONS_TAB.register("compreced_mushroom", () -> new MushroomBase(BlockBehaviour.Properties
                .of(Material.PLANT, MaterialColor.COLOR_PURPLE).noCollission().randomTicks().instabreak().sound(SoundType.GRASS)
                .lightLevel((i) -> {return 1;})));

        DISTORCED_MUSHROOM = PokecubeLegends.DIMENSIONS_TAB.register("distorced_mushroom", () -> new MushroomBase(BlockBehaviour.Properties
                .of(Material.PLANT, MaterialColor.COLOR_PURPLE).noCollission().randomTicks().instabreak().sound(SoundType.GRASS)
                .lightLevel((i) -> {return 1;})));

        GOLDEN_FERN = PokecubeLegends.DIMENSIONS_TAB.register("golden_fern", () -> new TallGoldenGrassBlock(
                BlockBehaviour.Properties.of(Material.REPLACEABLE_PLANT, MaterialColor.GOLD).noCollission().instabreak().sound(SoundType.GRASS)));

        LARGE_GOLDEN_FERN = PokecubeLegends.DIMENSIONS_TAB.register("large_golden_fern", () -> new DoublePlantBlock(
                BlockBehaviour.Properties.of(Material.REPLACEABLE_PLANT, MaterialColor.GOLD).noCollission().instabreak().sound(SoundType.GRASS)));

        GOLDEN_GRASS = PokecubeLegends.DIMENSIONS_TAB.register("golden_grass", () -> new TallGoldenGrassBlock(
                BlockBehaviour.Properties.of(Material.REPLACEABLE_PLANT, MaterialColor.GOLD).noCollission().instabreak().sound(SoundType.GRASS)));

        TALL_GOLDEN_GRASS = PokecubeLegends.DIMENSIONS_TAB.register("tall_golden_grass", () -> new DoublePlantBlock(
                BlockBehaviour.Properties.of(Material.REPLACEABLE_PLANT, MaterialColor.GOLD).noCollission().instabreak().sound(SoundType.GRASS)));

        GOLDEN_POPPY = PokecubeLegends.DIMENSIONS_TAB.register("golden_poppy", () -> new FlowerBase(MobEffects.ABSORPTION, 10,
                BlockBehaviour.Properties.of(Material.PLANT, MaterialColor.GOLD).noCollission().instabreak().sound(SoundType.CORAL_BLOCK)));

        GOLDEN_SWEET_BERRY_BUSH = PokecubeLegends.DIMENSIONS_TAB.register("golden_sweet_berry_bush", () -> new GoldenSweetBerryBushBlock(
                BlockBehaviour.Properties.of(Material.PLANT, MaterialColor.GOLD).randomTicks().noCollission().sound(SoundType.SWEET_BERRY_BUSH)));

        INVERTED_ORCHID = PokecubeLegends.DIMENSIONS_TAB.register("inverted_orchid", () -> new FlowerBase(MobEffects.HEAL, 10,
                BlockBehaviour.Properties.of(Material.PLANT, MaterialColor.COLOR_PINK).noCollission().instabreak().sound(SoundType.BAMBOO_SAPLING)));

        CORRUPTED_GRASS = PokecubeLegends.DIMENSIONS_TAB.register("corrupted_grass", () -> new TallCorruptedGrassBlock(
                BlockBehaviour.Properties.of(Material.REPLACEABLE_PLANT, MaterialColor.TERRACOTTA_BLUE).noCollission()
                .instabreak().sound(SoundType.GRASS)));

        TAINTED_ROOTS = PokecubeLegends.DIMENSIONS_TAB.register("tainted_roots", () -> new TaintedRootsBlock(
                BlockBehaviour.Properties.of(Material.REPLACEABLE_PLANT, MaterialColor.TERRACOTTA_PURPLE).noCollission()
                .instabreak().sound(SoundType.ROOTS)));

        TAINTED_KELP = PokecubeLegends.DIMENSIONS_TAB.register("tainted_kelp", () -> new TaintedKelpBlock(
                BlockBehaviour.Properties.of(Material.WATER_PLANT, MaterialColor.TERRACOTTA_PURPLE).noCollission()
                .randomTicks().instabreak().sound(SoundType.WET_GRASS)));

        TAINTED_KELP_PLANT = PokecubeLegends.NO_TAB.register("tainted_kelp_plant", () -> new TaintedKelpPlantBlock(
                BlockBehaviour.Properties.of(Material.WATER_PLANT, MaterialColor.TERRACOTTA_PURPLE).noCollission()
                .instabreak().sound(SoundType.WET_GRASS)));

        PINK_TAINTED_LILY_PAD = PokecubeLegends.DIMENSIONS_TAB.register("pink_blossom_tainted_lily_pad", () -> new BlossomLilyPadBlock(
                BlockBehaviour.Properties.of(Material.PLANT, MaterialColor.TERRACOTTA_PURPLE).instabreak().sound(SoundType.LILY_PAD).noOcclusion()));

        TAINTED_LILY_PAD = PokecubeLegends.DIMENSIONS_TAB.register("tainted_lily_pad", () -> new LilyPadBlock(
                BlockBehaviour.Properties.of(Material.PLANT, MaterialColor.TERRACOTTA_PURPLE).instabreak().sound(SoundType.LILY_PAD).noOcclusion()));

        TAINTED_SEAGRASS = PokecubeLegends.DIMENSIONS_TAB.register("tainted_seagrass", () -> new TaintedSeagrassBlock(
                BlockBehaviour.Properties.of(Material.REPLACEABLE_WATER_PLANT, MaterialColor.TERRACOTTA_PURPLE)
                .noCollission().instabreak().sound(SoundType.WET_GRASS)));

        TALL_TAINTED_SEAGRASS = PokecubeLegends.DIMENSIONS_TAB.register("tall_tainted_seagrass", () -> new TallTaintedSeagrassBlock(
                BlockBehaviour.Properties.of(Material.REPLACEABLE_WATER_PLANT, MaterialColor.TERRACOTTA_PURPLE)
                .noCollission().instabreak().sound(SoundType.WET_GRASS)));

        TEMPORAL_BAMBOO = PokecubeLegends.DIMENSIONS_TAB.register("temporal_bamboo", () -> new TemporalBambooBlock(
                BlockBehaviour.Properties.of(Material.BAMBOO, MaterialColor.WARPED_NYLIUM).randomTicks().instabreak()
                .strength(1.2f).sound(SoundType.BAMBOO).noOcclusion().dynamicShape()));
        TEMPORAL_BAMBOO_SHOOT = PokecubeLegends.DIMENSIONS_TAB.register("temporal_bamboo_shoot", () -> new TemporalBambooShootBlock(
                BlockBehaviour.Properties.of(Material.BAMBOO_SAPLING, MaterialColor.WARPED_NYLIUM).randomTicks().instabreak().noCollission()
                .strength(1.2f).sound(SoundType.BAMBOO_SAPLING)));

        DISTORTIC_VINES = PokecubeLegends.DIMENSIONS_TAB.register("distortic_vines", () -> new DistortedVinesTopBlock(
                BlockBehaviour.Properties.of(Material.PLANT, MaterialColor.COLOR_MAGENTA).randomTicks().noCollission()
                        .instabreak().sound(SoundType.WEEPING_VINES)));
        DISTORTIC_VINES_PLANT = PokecubeLegends.DIMENSIONS_TAB.register("distortic_vines_plant",
                () -> new DistortedVinesBlock(BlockBehaviour.Properties.of(Material.PLANT, MaterialColor.COLOR_MAGENTA)
                        .noCollission().instabreak().sound(SoundType.WEEPING_VINES)));
    }

    public static void registry() {

    }
}
