package pokecube.legends.init;

import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BushBlock;
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
import pokecube.legends.blocks.plants.GoldenSweetBerryBushBlock;
import pokecube.legends.blocks.plants.LilyPadBlock;
import pokecube.legends.blocks.plants.TaintedKelpBlock;
import pokecube.legends.blocks.plants.TaintedKelpPlantBlock;
import pokecube.legends.blocks.plants.TaintedSeagrassBlock;
import pokecube.legends.blocks.plants.TallGoldenGrassBlock;
import pokecube.legends.blocks.plants.TallTaintedSeagrassBlock;

public class PlantsInit
{
    // Plants
    public static RegistryObject<Block> COMPRECED_MUSHROOM;
    public static RegistryObject<Block> DISTORCED_MUSHROOM;
    public static RegistryObject<Block> GOLDEN_FERN;
    public static RegistryObject<Block> LARGE_GOLDEN_FERN;
    public static RegistryObject<Block> GOLDEN_GRASS;
    public static RegistryObject<Block> TALL_GOLDEN_GRASS;
    public static RegistryObject<Block> GOLDEN_POPPY;
    public static RegistryObject<Block> GOLDEN_SWEET_BERRY_BUSH;
    public static RegistryObject<Block> INVERTED_ORCHID;
    public static RegistryObject<Block> TAINTED_KELP;
    public static RegistryObject<Block> TAINTED_KELP_PLANT;
    public static RegistryObject<Block> PINK_TAINTED_LILY_PAD;
    public static RegistryObject<Block> TAINTED_LILY_PAD;
    public static RegistryObject<Block> TAINTED_SEAGRASS;
    public static RegistryObject<Block> TALL_TAINTED_SEAGRASS;

    static
    {
        PlantsInit.COMPRECED_MUSHROOM = PokecubeLegends.DIMENSIONS_TAB.register("compreced_mushroom", () -> new MushroomBase(BlockBehaviour.Properties
                .of(Material.PLANT, MaterialColor.COLOR_PURPLE).noCollission().randomTicks().instabreak().sound(SoundType.GRASS)
                .lightLevel((i) -> {return 1;})));

        PlantsInit.DISTORCED_MUSHROOM = PokecubeLegends.DIMENSIONS_TAB.register("distorced_mushroom", () -> new MushroomBase(BlockBehaviour.Properties
                .of(Material.PLANT, MaterialColor.COLOR_PURPLE).noCollission().randomTicks().instabreak().sound(SoundType.GRASS)
                .lightLevel((i) -> {return 1;})));

        PlantsInit.GOLDEN_FERN = PokecubeLegends.DIMENSIONS_TAB.register("golden_fern", () -> new TallGoldenGrassBlock(
                BlockBehaviour.Properties.of(Material.REPLACEABLE_PLANT, MaterialColor.GOLD).noCollission().instabreak().sound(SoundType.GRASS)));

        PlantsInit.LARGE_GOLDEN_FERN = PokecubeLegends.DIMENSIONS_TAB.register("large_golden_fern", () -> new DoublePlantBlock(
                BlockBehaviour.Properties.of(Material.REPLACEABLE_PLANT, MaterialColor.GOLD).noCollission().instabreak().sound(SoundType.GRASS)));

        PlantsInit.GOLDEN_GRASS = PokecubeLegends.DIMENSIONS_TAB.register("golden_grass", () -> new TallGoldenGrassBlock(
                BlockBehaviour.Properties.of(Material.REPLACEABLE_PLANT, MaterialColor.GOLD).noCollission().instabreak().sound(SoundType.GRASS)));

        PlantsInit.TALL_GOLDEN_GRASS = PokecubeLegends.DIMENSIONS_TAB.register("tall_golden_grass", () -> new DoublePlantBlock(
                BlockBehaviour.Properties.of(Material.REPLACEABLE_PLANT, MaterialColor.GOLD).noCollission().instabreak().sound(SoundType.GRASS)));

        PlantsInit.GOLDEN_POPPY = PokecubeLegends.DIMENSIONS_TAB.register("golden_poppy", () -> new FlowerBase(MobEffects.ABSORPTION, 10,
                BlockBehaviour.Properties.of(Material.PLANT, MaterialColor.GOLD).noCollission().instabreak().sound(SoundType.CORAL_BLOCK)));

        PlantsInit.GOLDEN_SWEET_BERRY_BUSH = PokecubeLegends.DIMENSIONS_TAB.register("golden_sweet_berry_bush", () -> new GoldenSweetBerryBushBlock(
                BlockBehaviour.Properties.of(Material.PLANT, MaterialColor.GOLD).randomTicks().noCollission().sound(SoundType.SWEET_BERRY_BUSH)));

        PlantsInit.INVERTED_ORCHID = PokecubeLegends.DIMENSIONS_TAB.register("inverted_orchid", () -> new FlowerBase(MobEffects.HEAL, 10,
                BlockBehaviour.Properties.of(Material.PLANT, MaterialColor.COLOR_PINK).noCollission().instabreak().sound(SoundType.BAMBOO_SAPLING)));

        PlantsInit.TAINTED_KELP = PokecubeLegends.DIMENSIONS_TAB.register("tainted_kelp", () -> new TaintedKelpBlock(
                BlockBehaviour.Properties.of(Material.WATER_PLANT, MaterialColor.COLOR_PURPLE).noCollission()
                .randomTicks().instabreak().sound(SoundType.WET_GRASS)));

        PlantsInit.TAINTED_KELP_PLANT = PokecubeLegends.NO_TAB.register("tainted_kelp_plant", () -> new TaintedKelpPlantBlock(
                BlockBehaviour.Properties.of(Material.WATER_PLANT, MaterialColor.COLOR_PURPLE).noCollission()
                .instabreak().sound(SoundType.WET_GRASS)));

        PlantsInit.PINK_TAINTED_LILY_PAD = PokecubeLegends.DIMENSIONS_TAB.register("pink_blossom_tainted_lily_pad", () -> new BlossomLilyPadBlock(
                BlockBehaviour.Properties.of(Material.PLANT, MaterialColor.COLOR_PURPLE).instabreak().sound(SoundType.LILY_PAD).noOcclusion()));

        PlantsInit.TAINTED_LILY_PAD = PokecubeLegends.DIMENSIONS_TAB.register("tainted_lily_pad", () -> new LilyPadBlock(
                BlockBehaviour.Properties.of(Material.PLANT, MaterialColor.COLOR_PURPLE).instabreak().sound(SoundType.LILY_PAD).noOcclusion()));

        PlantsInit.TAINTED_SEAGRASS = PokecubeLegends.DIMENSIONS_TAB.register("tainted_seagrass", () -> new TaintedSeagrassBlock(
                BlockBehaviour.Properties.of(Material.REPLACEABLE_WATER_PLANT, MaterialColor.COLOR_PURPLE)
                .noCollission().instabreak().sound(SoundType.WET_GRASS)));

        PlantsInit.TALL_TAINTED_SEAGRASS = PokecubeLegends.DIMENSIONS_TAB.register("tall_tainted_seagrass", () -> new TallTaintedSeagrassBlock(
                BlockBehaviour.Properties.of(Material.REPLACEABLE_WATER_PLANT, MaterialColor.COLOR_PURPLE)
                .noCollission().instabreak().sound(SoundType.WET_GRASS)));
    }

    public static void registry() {

    }
}
