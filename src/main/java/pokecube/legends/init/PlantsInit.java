package pokecube.legends.init;

import net.minecraft.core.BlockPos;
import net.minecraft.data.worldgen.features.TreeFeatures;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DoublePlantBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.minecraftforge.registries.RegistryObject;
import pokecube.legends.PokecubeLegends;
import pokecube.legends.blocks.FlowerBase;
import pokecube.legends.blocks.MushroomBase;
import pokecube.legends.blocks.plants.AzureColeusBlock;
import pokecube.legends.blocks.plants.BlossomLilyPadBlock;
import pokecube.legends.blocks.plants.DistortedVinesBlock;
import pokecube.legends.blocks.plants.DistortedVinesTopBlock;
import pokecube.legends.blocks.plants.GoldenSweetBerryBushBlock;
import pokecube.legends.blocks.plants.HangingTendrilsBlock;
import pokecube.legends.blocks.plants.HangingTendrilsPlantBlock;
import pokecube.legends.blocks.plants.InvertedOrchidBlock;
import pokecube.legends.blocks.plants.LilyPadBlock;
import pokecube.legends.blocks.plants.PurpleWisteriaVinesBlock;
import pokecube.legends.blocks.plants.PurpleWisteriaVinesPlantBlock;
import pokecube.legends.blocks.plants.TaintedKelpBlock;
import pokecube.legends.blocks.plants.TaintedKelpPlantBlock;
import pokecube.legends.blocks.plants.TaintedRootsBlock;
import pokecube.legends.blocks.plants.TaintedSeagrassBlock;
import pokecube.legends.blocks.plants.TallCorruptedGrassBlock;
import pokecube.legends.blocks.plants.TallDistorticGrassBlock;
import pokecube.legends.blocks.plants.TallGoldenGrassBlock;
import pokecube.legends.blocks.plants.TallTaintedSeagrassBlock;
import pokecube.legends.blocks.plants.TemporalBambooShootBlock;
import pokecube.legends.blocks.plants.TemporalBambooStalkBlock;

public class PlantsInit
{
    // Plants
    public static final RegistryObject<Block> AZURE_COLEUS;
    public static final RegistryObject<Block> COMPRECED_MUSHROOM;
    public static final RegistryObject<Block> CORRUPTED_GRASS;
    public static final RegistryObject<Block> DISTORCED_MUSHROOM;
    public static final RegistryObject<Block> DISTORTIC_GRASS;
    public static final RegistryObject<Block> DISTORTIC_VINES;
    public static final RegistryObject<Block> DISTORTIC_VINES_PLANT;
    public static final RegistryObject<Block> GOLDEN_ALLIUM;
    public static final RegistryObject<Block> GOLDEN_AZURE_BLUET;
    public static final RegistryObject<Block> GOLDEN_CORNFLOWER;
    public static final RegistryObject<Block> GOLDEN_DANDELION;
    public static final RegistryObject<Block> GOLDEN_FERN;
    public static final RegistryObject<Block> GOLDEN_GRASS;
    public static final RegistryObject<Block> GOLDEN_LILY_VALLEY;
    public static final RegistryObject<Block> GOLDEN_ORCHID;
    public static final RegistryObject<Block> GOLDEN_OXEYE_DAISY;
    public static final RegistryObject<Block> GOLDEN_POPPY;
    public static final RegistryObject<Block> GOLDEN_SHROOM_PLANT;
    public static final RegistryObject<Block> GOLDEN_SWEET_BERRY_BUSH;
    public static final RegistryObject<Block> GOLDEN_TULIP;
    public static final RegistryObject<Block> INVERTED_ORCHID;
    public static final RegistryObject<Block> HANGING_TENDRILS;
    public static final RegistryObject<Block> HANGING_TENDRILS_PLANT;
    public static final RegistryObject<Block> LARGE_GOLDEN_FERN;
    public static final RegistryObject<Block> PINK_TAINTED_LILY_PAD;
    public static final RegistryObject<Block> PURPLE_WISTERIA_VINES;
    public static final RegistryObject<Block> PURPLE_WISTERIA_VINES_PLANT;
    public static final RegistryObject<Block> TAINTED_KELP;
    public static final RegistryObject<Block> TAINTED_KELP_PLANT;
    public static final RegistryObject<Block> TAINTED_LILY_PAD;
    public static final RegistryObject<Block> TAINTED_ROOTS;
    public static final RegistryObject<Block> TAINTED_SEAGRASS;
    public static final RegistryObject<Block> TALL_CORRUPTED_GRASS;
    public static final RegistryObject<Block> TALL_GOLDEN_GRASS;
    public static final RegistryObject<Block> TALL_TAINTED_SEAGRASS;
    public static final RegistryObject<Block> TEMPORAL_BAMBOO;
    public static final RegistryObject<Block> TEMPORAL_BAMBOO_SHOOT;

    static
    {
        AZURE_COLEUS = PokecubeLegends.BLOCKS.register("azure_coleus", () -> new AzureColeusBlock(MobEffects.INVISIBILITY, 15,
                BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_BLUE).randomTicks().noCollission().instabreak().ignitedByLava()
                        .sound(SoundType.AZALEA).offsetType(BlockBehaviour.OffsetType.XZ).pushReaction(PushReaction.DESTROY)));

        COMPRECED_MUSHROOM = PokecubeLegends.BLOCKS.register("compreced_mushroom",
                () -> new MushroomBase(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_PURPLE)
                        .noCollission().randomTicks().instabreak().ignitedByLava()
                        .sound(SoundType.GRASS).pushReaction(PushReaction.DESTROY).offsetType(BlockBehaviour.OffsetType.XZ),
                        TreeFeatures.HUGE_RED_MUSHROOM).bonemealTarget(false));

        DISTORCED_MUSHROOM = PokecubeLegends.BLOCKS.register("distorced_mushroom",
                () -> new MushroomBase(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_PURPLE)
                        .noCollission().randomTicks().instabreak().ignitedByLava()
                        .sound(SoundType.GRASS).pushReaction(PushReaction.DESTROY).offsetType(BlockBehaviour.OffsetType.XZ),
                        TreeFeatures.HUGE_RED_MUSHROOM).bonemealTarget(false));

        GOLDEN_FERN = PokecubeLegends.BLOCKS.register("golden_fern", () -> new TallGoldenGrassBlock(
                BlockBehaviour.Properties.of().mapColor(MapColor.GOLD).noCollission().replaceable().instabreak().ignitedByLava()
                        .sound(SoundType.GRASS).offsetType(BlockBehaviour.OffsetType.XZ).pushReaction(PushReaction.DESTROY)));

        LARGE_GOLDEN_FERN = PokecubeLegends.BLOCKS.register("large_golden_fern", () -> new DoublePlantBlock(
                BlockBehaviour.Properties.of().mapColor(MapColor.GOLD).noCollission().replaceable().instabreak().ignitedByLava()
                        .sound(SoundType.GRASS).offsetType(BlockBehaviour.OffsetType.XZ).pushReaction(PushReaction.DESTROY)));

        GOLDEN_GRASS = PokecubeLegends.BLOCKS.register("golden_grass", () -> new TallGoldenGrassBlock(
                BlockBehaviour.Properties.of().mapColor(MapColor.GOLD).noCollission().replaceable().instabreak().ignitedByLava()
                        .sound(SoundType.GRASS).offsetType(BlockBehaviour.OffsetType.XZ).pushReaction(PushReaction.DESTROY)));

        TALL_GOLDEN_GRASS = PokecubeLegends.BLOCKS.register("tall_golden_grass", () -> new DoublePlantBlock(
                BlockBehaviour.Properties.of().mapColor(MapColor.GOLD).noCollission().replaceable().instabreak().ignitedByLava()
                        .sound(SoundType.GRASS).offsetType(BlockBehaviour.OffsetType.XZ).pushReaction(PushReaction.DESTROY)));

        GOLDEN_ALLIUM = PokecubeLegends.BLOCKS.register("golden_allium", () -> new FlowerBase(MobEffects.ABSORPTION, 10,
                BlockBehaviour.Properties.of().mapColor(MapColor.GOLD).noCollission().instabreak().ignitedByLava()
                        .sound(SoundType.GRASS).offsetType(BlockBehaviour.OffsetType.XZ).pushReaction(PushReaction.DESTROY)));
        
        GOLDEN_AZURE_BLUET = PokecubeLegends.BLOCKS.register("golden_azure_bluet", () -> new FlowerBase(MobEffects.ABSORPTION, 10,
                BlockBehaviour.Properties.of().mapColor(MapColor.GOLD).noCollission().instabreak().ignitedByLava()
                        .sound(SoundType.GRASS).offsetType(BlockBehaviour.OffsetType.XZ).pushReaction(PushReaction.DESTROY)));
        
        GOLDEN_CORNFLOWER = PokecubeLegends.BLOCKS.register("golden_cornflower", () -> new FlowerBase(MobEffects.ABSORPTION, 10,
                BlockBehaviour.Properties.of().mapColor(MapColor.GOLD).noCollission().instabreak().ignitedByLava()
                        .sound(SoundType.GRASS).offsetType(BlockBehaviour.OffsetType.XZ).pushReaction(PushReaction.DESTROY)));
        
        GOLDEN_DANDELION = PokecubeLegends.BLOCKS.register("golden_dandelion", () -> new FlowerBase(MobEffects.ABSORPTION, 10,
                BlockBehaviour.Properties.of().mapColor(MapColor.GOLD).noCollission().instabreak().ignitedByLava()
                        .sound(SoundType.GRASS).offsetType(BlockBehaviour.OffsetType.XZ).pushReaction(PushReaction.DESTROY)));
        
        GOLDEN_LILY_VALLEY = PokecubeLegends.BLOCKS.register("golden_lily_of_the_valley", () -> new FlowerBase(MobEffects.ABSORPTION, 10,
                BlockBehaviour.Properties.of().mapColor(MapColor.GOLD).noCollission().instabreak().ignitedByLava()
                        .sound(SoundType.GRASS).offsetType(BlockBehaviour.OffsetType.XZ).pushReaction(PushReaction.DESTROY)));
        
        GOLDEN_POPPY = PokecubeLegends.BLOCKS.register("golden_poppy", () -> new FlowerBase(MobEffects.ABSORPTION, 10,
                BlockBehaviour.Properties.of().mapColor(MapColor.GOLD).noCollission().instabreak().ignitedByLava()
                        .sound(SoundType.GRASS).offsetType(BlockBehaviour.OffsetType.XZ).pushReaction(PushReaction.DESTROY)));
        
        GOLDEN_ORCHID = PokecubeLegends.BLOCKS.register("golden_orchid", () -> new FlowerBase(MobEffects.ABSORPTION, 10,
                BlockBehaviour.Properties.of().mapColor(MapColor.GOLD).noCollission().instabreak().ignitedByLava()
                        .sound(SoundType.GRASS).offsetType(BlockBehaviour.OffsetType.XZ).pushReaction(PushReaction.DESTROY)));
        
        GOLDEN_OXEYE_DAISY = PokecubeLegends.BLOCKS.register("golden_oxeye_daisy", () -> new FlowerBase(MobEffects.ABSORPTION, 10,
                BlockBehaviour.Properties.of().mapColor(MapColor.GOLD).noCollission().instabreak().ignitedByLava()
                        .sound(SoundType.GRASS).offsetType(BlockBehaviour.OffsetType.XZ).pushReaction(PushReaction.DESTROY)));

        GOLDEN_TULIP = PokecubeLegends.BLOCKS.register("golden_tulip", () -> new FlowerBase(MobEffects.ABSORPTION, 10,
                BlockBehaviour.Properties.of().mapColor(MapColor.GOLD).noCollission().instabreak().ignitedByLava()
                        .sound(SoundType.GRASS).offsetType(BlockBehaviour.OffsetType.XZ).pushReaction(PushReaction.DESTROY)));

        GOLDEN_SHROOM_PLANT = PokecubeLegends.BLOCKS.register("golden_shroom_plant",
                () -> new MushroomBase(BlockBehaviour.Properties.of().mapColor(MapColor.GOLD)
                        .noCollission().randomTicks().instabreak().ignitedByLava()
                        .sound(SoundType.GRASS).offsetType(BlockBehaviour.OffsetType.XZ).pushReaction(PushReaction.DESTROY), 
                        TreeFeatures.HUGE_RED_MUSHROOM).bonemealTarget(false));

        GOLDEN_SWEET_BERRY_BUSH = PokecubeLegends.BLOCKS.register("golden_sweet_berry_bush", () -> new GoldenSweetBerryBushBlock(
                BlockBehaviour.Properties.of().mapColor(MapColor.GOLD).randomTicks().noCollission().instabreak().ignitedByLava()
                        .sound(SoundType.SWEET_BERRY_BUSH).offsetType(BlockBehaviour.OffsetType.XZ).pushReaction(PushReaction.DESTROY)));

        INVERTED_ORCHID = PokecubeLegends.BLOCKS.register("inverted_orchid", () -> new InvertedOrchidBlock(MobEffects.HEAL, 10,
                BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_PINK).noCollission().instabreak().ignitedByLava()
                        .sound(SoundType.BAMBOO_SAPLING).offsetType(BlockBehaviour.OffsetType.XZ).pushReaction(PushReaction.DESTROY)));

        CORRUPTED_GRASS = PokecubeLegends.BLOCKS.register("corrupted_grass", () -> new TallCorruptedGrassBlock(
                BlockBehaviour.Properties.of().mapColor(MapColor.TERRACOTTA_BLUE).noCollission().replaceable().instabreak().ignitedByLava()
                        .sound(SoundType.GRASS).offsetType(BlockBehaviour.OffsetType.XZ).pushReaction(PushReaction.DESTROY)));

        TALL_CORRUPTED_GRASS = PokecubeLegends.BLOCKS.register("tall_corrupted_grass", () -> new DoublePlantBlock(
                BlockBehaviour.Properties.of().mapColor(MapColor.TERRACOTTA_BLUE).noCollission().replaceable().instabreak().ignitedByLava()
                        .sound(SoundType.GRASS).offsetType(BlockBehaviour.OffsetType.XZ).pushReaction(PushReaction.DESTROY)));

        TAINTED_ROOTS = PokecubeLegends.BLOCKS.register("tainted_roots", () -> new TaintedRootsBlock(
                BlockBehaviour.Properties.of().mapColor(MapColor.TERRACOTTA_PURPLE).noCollission().replaceable().instabreak().ignitedByLava()
                        .sound(SoundType.ROOTS).offsetType(BlockBehaviour.OffsetType.XZ).pushReaction(PushReaction.DESTROY)));

        TAINTED_KELP = PokecubeLegends.BLOCKS.register("tainted_kelp", () -> new TaintedKelpBlock(
                BlockBehaviour.Properties.of().mapColor(MapColor.TERRACOTTA_PURPLE).noCollission().instabreak().ignitedByLava()
                        .sound(SoundType.WET_GRASS).offsetType(BlockBehaviour.OffsetType.XZ).pushReaction(PushReaction.DESTROY)));

        TAINTED_KELP_PLANT = PokecubeLegends.BLOCKS.register("tainted_kelp_plant", () -> new TaintedKelpPlantBlock(
                BlockBehaviour.Properties.of().mapColor(MapColor.TERRACOTTA_PURPLE).noCollission().instabreak().ignitedByLava()
                        .sound(SoundType.WET_GRASS).offsetType(BlockBehaviour.OffsetType.XZ).pushReaction(PushReaction.DESTROY)));

        PINK_TAINTED_LILY_PAD = PokecubeLegends.BLOCKS.register("pink_blossom_tainted_lily_pad", () -> new BlossomLilyPadBlock(
                BlockBehaviour.Properties.of().mapColor(MapColor.TERRACOTTA_PURPLE).noOcclusion().instabreak().ignitedByLava()
                        .sound(SoundType.LILY_PAD).pushReaction(PushReaction.DESTROY)));

        TAINTED_LILY_PAD = PokecubeLegends.BLOCKS.register("tainted_lily_pad", () -> new LilyPadBlock(
                BlockBehaviour.Properties.of().mapColor(MapColor.TERRACOTTA_PURPLE).noOcclusion().instabreak().ignitedByLava()
                        .sound(SoundType.GRASS).pushReaction(PushReaction.DESTROY)));

        HANGING_TENDRILS = PokecubeLegends.BLOCKS.register("hanging_tendrils", () -> new HangingTendrilsBlock(
                BlockBehaviour.Properties.of().mapColor(MapColor.TERRACOTTA_PURPLE).randomTicks().noCollission().instabreak().ignitedByLava()
                        .sound(SoundType.CAVE_VINES).pushReaction(PushReaction.DESTROY).lightLevel(HangingTendrilsBlock.emission(5))));
        HANGING_TENDRILS_PLANT = PokecubeLegends.BLOCKS.register("hanging_tendrils_plant", () -> new HangingTendrilsPlantBlock(
                BlockBehaviour.Properties.of().mapColor(MapColor.TERRACOTTA_PURPLE).noCollission().instabreak().ignitedByLava()
                        .sound(SoundType.CAVE_VINES).pushReaction(PushReaction.DESTROY).lightLevel(HangingTendrilsBlock.emission(5))));

        PURPLE_WISTERIA_VINES = PokecubeLegends.BLOCKS.register("purple_wisteria_vines", () -> new PurpleWisteriaVinesBlock(
                BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_PURPLE).randomTicks().noCollission().instabreak().ignitedByLava()
                        .sound(SoundType.CAVE_VINES).pushReaction(PushReaction.DESTROY)));
        PURPLE_WISTERIA_VINES_PLANT = PokecubeLegends.BLOCKS.register("purple_wisteria_vines_plant", () -> new PurpleWisteriaVinesPlantBlock(
                BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_PURPLE).noCollission().instabreak().ignitedByLava()
                        .sound(SoundType.CAVE_VINES).pushReaction(PushReaction.DESTROY)));

        TAINTED_SEAGRASS = PokecubeLegends.BLOCKS.register("tainted_seagrass", () -> new TaintedSeagrassBlock(
                BlockBehaviour.Properties.of().mapColor(MapColor.TERRACOTTA_PURPLE).replaceable().instabreak().ignitedByLava()
                        .sound(SoundType.WET_GRASS).pushReaction(PushReaction.DESTROY)));

        TALL_TAINTED_SEAGRASS = PokecubeLegends.BLOCKS.register("tall_tainted_seagrass", () -> new TallTaintedSeagrassBlock(
                BlockBehaviour.Properties.of().mapColor(MapColor.TERRACOTTA_PURPLE).replaceable().instabreak().ignitedByLava()
                        .sound(SoundType.WET_GRASS).pushReaction(PushReaction.DESTROY)));

        TEMPORAL_BAMBOO = PokecubeLegends.BLOCKS.register("temporal_bamboo", () -> new TemporalBambooStalkBlock(
                BlockBehaviour.Properties.of().mapColor(MapColor.WARPED_NYLIUM)
                        .strength(1.2f).noOcclusion().randomTicks().ignitedByLava().dynamicShape().forceSolidOn()
                        .sound(SoundType.BAMBOO).pushReaction(PushReaction.DESTROY).offsetType(BlockBehaviour.OffsetType.XZ).isRedstoneConductor(PlantsInit::never)));
        TEMPORAL_BAMBOO_SHOOT = PokecubeLegends.BLOCKS.register("temporal_bamboo_shoot", () -> new TemporalBambooShootBlock(
                BlockBehaviour.Properties.of().mapColor(MapColor.WARPED_NYLIUM)
                        .strength(1.2f).noOcclusion().randomTicks().instabreak().ignitedByLava().dynamicShape().forceSolidOn()
                        .sound(SoundType.BAMBOO).pushReaction(PushReaction.DESTROY).offsetType(BlockBehaviour.OffsetType.XZ)));

        DISTORTIC_GRASS = PokecubeLegends.BLOCKS.register("distortic_grass", () -> new TallDistorticGrassBlock(
                BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_MAGENTA).noCollission().replaceable().instabreak().ignitedByLava()
                        .sound(SoundType.ROOTS).offsetType(BlockBehaviour.OffsetType.XZ).pushReaction(PushReaction.DESTROY)));
        
        DISTORTIC_VINES = PokecubeLegends.BLOCKS.register("distortic_vines", () -> new DistortedVinesTopBlock(
                BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_MAGENTA).randomTicks().noCollission().instabreak().ignitedByLava()
                        .sound(SoundType.WEEPING_VINES).offsetType(BlockBehaviour.OffsetType.XZ).pushReaction(PushReaction.DESTROY)));
        DISTORTIC_VINES_PLANT = PokecubeLegends.BLOCKS.register("distortic_vines_plant", () -> new DistortedVinesBlock(
                BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_MAGENTA).noCollission().instabreak().ignitedByLava()
                        .sound(SoundType.WEEPING_VINES).offsetType(BlockBehaviour.OffsetType.XZ).pushReaction(PushReaction.DESTROY)));
    }

    public static Boolean always(BlockState state, BlockGetter block, BlockPos pos, EntityType<?> type)
    {
        return Boolean.TRUE;
    }

    public static boolean never(BlockState state, BlockGetter blockGetter, BlockPos pos) {
        return Boolean.FALSE;
    }

    public static void registry()
    {
    }
}
