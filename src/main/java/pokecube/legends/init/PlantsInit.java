package pokecube.legends.init;

import net.minecraft.core.BlockPos;
import net.minecraft.data.worldgen.features.TreeFeatures;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DoublePlantBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.minecraftforge.registries.RegistryObject;
import pokecube.core.PokecubeItems;
import pokecube.legends.PokecubeLegends;
import pokecube.legends.blocks.FlowerBase;
import pokecube.legends.blocks.MushroomBase;
import pokecube.legends.blocks.SaplingBase;
import pokecube.legends.blocks.plants.AzureColeusBlock;
import pokecube.legends.blocks.plants.BigContaminatedDripleafBlock;
import pokecube.legends.blocks.plants.BigContaminatedDripleafStemBlock;
import pokecube.legends.blocks.plants.BlossomLilyPadBlock;
import pokecube.legends.blocks.plants.CrystallizedBush;
import pokecube.legends.blocks.plants.CrystallizedCactus;
import pokecube.legends.blocks.plants.DistortedVinesBlock;
import pokecube.legends.blocks.plants.DistortedVinesTopBlock;
import pokecube.legends.blocks.plants.DistorticSapling;
import pokecube.legends.blocks.plants.DynaShrubBlock;
import pokecube.legends.blocks.plants.GoldenSweetBerryBushBlock;
import pokecube.legends.blocks.plants.HangingTendrilsBlock;
import pokecube.legends.blocks.plants.HangingTendrilsPlantBlock;
import pokecube.legends.blocks.plants.InvertedOrchidBlock;
import pokecube.legends.blocks.plants.LilyPadBlock;
import pokecube.legends.blocks.plants.MirageSapling;
import pokecube.legends.blocks.plants.PollutingBlossomBlock;
import pokecube.legends.blocks.plants.PurpleWisteriaVinesBlock;
import pokecube.legends.blocks.plants.PurpleWisteriaVinesPlantBlock;
import pokecube.legends.blocks.plants.SmallContaminatedDripleafBlock;
import pokecube.legends.blocks.plants.StringOfPearlsBlock;
import pokecube.legends.blocks.plants.TaintedKelpBlock;
import pokecube.legends.blocks.plants.TaintedKelpPlantBlock;
import pokecube.legends.blocks.plants.TaintedRootsBlock;
import pokecube.legends.blocks.plants.TaintedSeagrassBlock;
import pokecube.legends.blocks.plants.TallCorruptedGrassBlock;
import pokecube.legends.blocks.plants.TallCrystallizedBush;
import pokecube.legends.blocks.plants.TallDistorticGrassBlock;
import pokecube.legends.blocks.plants.TallGoldenGrassBlock;
import pokecube.legends.blocks.plants.TallTaintedSeagrassBlock;
import pokecube.legends.blocks.plants.TemporalBambooShootBlock;
import pokecube.legends.blocks.plants.TemporalBambooStalkBlock;
import pokecube.legends.items.ItemBase;
import pokecube.legends.worldgen.trees.AgedTreeGrower;
import pokecube.legends.worldgen.trees.CorruptedTreeGrower;
import pokecube.legends.worldgen.trees.DistorticTreeGrower;
import pokecube.legends.worldgen.trees.InvertedTreeGrower;
import pokecube.legends.worldgen.trees.MirageTreeGrower;
import pokecube.legends.worldgen.trees.TemporalTreeGrower;

public class PlantsInit
{
    public static final RegistryObject<Block> AGED_SAPLING;
    public static final RegistryObject<Block> AZURE_COLEUS;
    public static final RegistryObject<Block> COMPRECED_MUSHROOM;
    public static final RegistryObject<Block> CORRUPTED_GRASS;
    public static final RegistryObject<Block> CORRUPTED_SAPLING;
    public static final RegistryObject<Block> CRYSTALLIZED_BUSH;
    public static final RegistryObject<Block> CRYSTALLIZED_CACTUS;
    public static final RegistryObject<Block> DISTORCED_MUSHROOM;
    public static final RegistryObject<Block> DISTORTIC_GRASS;
    public static final RegistryObject<Block> DISTORTIC_SAPLING;
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
    public static final RegistryObject<Block> BIG_CONTAMINATED_DRIPLEAF;
    public static final RegistryObject<Block> BIG_CONTAMINATED_DRIPLEAF_STEM;
    public static final RegistryObject<Block> POLLUTING_BLOSSOM;
    public static final RegistryObject<Block> SMALL_CONTAMINATED_DRIPLEAF;
    public static final RegistryObject<Block> DYNA_SHRUB;
    public static final RegistryObject<Block> GOLDEN_POPPY;
    public static final RegistryObject<Block> GOLDEN_SHROOM_PLANT;
    public static final RegistryObject<Block> GOLDEN_SWEET_BERRY_BUSH;
    public static final RegistryObject<Block> GOLDEN_TULIP;
    public static final RegistryObject<Block> GRACIDEA;
    public static final RegistryObject<Block> HANGING_TENDRILS;
    public static final RegistryObject<Block> HANGING_TENDRILS_PLANT;
    public static final RegistryObject<Block> INVERTED_ORCHID;
    public static final RegistryObject<Block> INVERTED_SAPLING;
    public static final RegistryObject<Block> LARGE_GOLDEN_FERN;
    public static final RegistryObject<Block> MIRAGE_SAPLING;
    public static final RegistryObject<Block> PINK_TAINTED_LILY_PAD;
    public static final RegistryObject<Block> PURPLE_WISTERIA_VINES;
    public static final RegistryObject<Block> PURPLE_WISTERIA_VINES_PLANT;
    public static final RegistryObject<Block> STRING_OF_PEARLS;
    public static final RegistryObject<Block> TAINTED_KELP;
    public static final RegistryObject<Block> TAINTED_KELP_PLANT;
    public static final RegistryObject<Block> TAINTED_LILY_PAD;
    public static final RegistryObject<Block> TAINTED_ROOTS;
    public static final RegistryObject<Block> TAINTED_SEAGRASS;
    public static final RegistryObject<Block> TALL_CORRUPTED_GRASS;
    public static final RegistryObject<Block> TALL_CRYSTALLIZED_BUSH;
    public static final RegistryObject<Block> TALL_GOLDEN_GRASS;
    public static final RegistryObject<Block> TALL_TAINTED_SEAGRASS;
    public static final RegistryObject<Block> TEMPORAL_BAMBOO;
    public static final RegistryObject<Block> TEMPORAL_BAMBOO_SHOOT;
    public static final RegistryObject<Block> TEMPORAL_SAPLING;

    static
    {
        AGED_SAPLING = PokecubeLegends.BLOCKS.register("aged_sapling",
                () -> new SaplingBase(AgedTreeGrower::new, BlockBehaviour.Properties.of().mapColor(MapColor.GOLD)
                        .sound(SoundType.GRASS).pushReaction(PushReaction.DESTROY).instabreak().noCollission()));
        CORRUPTED_SAPLING = PokecubeLegends.BLOCKS.register("corrupted_sapling",
                () -> new SaplingBase(CorruptedTreeGrower::new, BlockBehaviour.Properties.of()
                        .mapColor(MapColor.COLOR_BLACK).sound(SoundType.GRASS)
                        .pushReaction(PushReaction.DESTROY).instabreak().noCollission()));
        DISTORTIC_SAPLING = PokecubeLegends.BLOCKS.register("distortic_sapling",
                () -> new DistorticSapling(new DistorticTreeGrower(), BlockBehaviour.Properties.of()
                        .mapColor(MapColor.COLOR_PURPLE).sound(SoundType.GRASS)
                        .pushReaction(PushReaction.DESTROY).instabreak().noCollission()));
        DYNA_SHRUB = PokecubeLegends.BLOCKS.register("dyna_shrub",
                () -> new DynaShrubBlock(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_RED)
                        .sound(SoundType.AZALEA).pushReaction(PushReaction.DESTROY)
                        .instabreak().noOcclusion().forceSolidOff()));
        INVERTED_SAPLING = PokecubeLegends.BLOCKS.register("inverted_sapling",
                () -> new SaplingBase(InvertedTreeGrower::new, BlockBehaviour.Properties.of()
                        .mapColor(MapColor.COLOR_BLUE).sound(SoundType.GRASS)
                        .pushReaction(PushReaction.DESTROY).instabreak().noCollission()));
        MIRAGE_SAPLING = PokecubeLegends.BLOCKS.register("mirage_sapling",
                () -> new MirageSapling(new MirageTreeGrower(), BlockBehaviour.Properties.of()
                        .mapColor(MapColor.COLOR_LIGHT_BLUE).sound(SoundType.GRASS)
                        .pushReaction(PushReaction.DESTROY).instabreak().noCollission()));
        TEMPORAL_SAPLING = PokecubeLegends.BLOCKS.register("temporal_sapling",
                () -> new SaplingBase(TemporalTreeGrower::new, BlockBehaviour.Properties.of().mapColor(MapColor.PLANT)
                        .sound(SoundType.GRASS).pushReaction(PushReaction.DESTROY).instabreak().noCollission()));

        TEMPORAL_BAMBOO = PokecubeLegends.NO_ITEM_BLOCKS.register("temporal_bamboo", () -> new TemporalBambooStalkBlock(
                BlockBehaviour.Properties.of().mapColor(MapColor.WARPED_NYLIUM)
                        .strength(1.2f).noOcclusion().randomTicks().ignitedByLava().dynamicShape().forceSolidOn()
                        .sound(SoundType.BAMBOO).pushReaction(PushReaction.DESTROY).offsetType(BlockBehaviour.OffsetType.XZ).isRedstoneConductor(PlantsInit::never)));
        TEMPORAL_BAMBOO_SHOOT = PokecubeLegends.NO_ITEM_BLOCKS.register("temporal_bamboo_shoot", () -> new TemporalBambooShootBlock(
                BlockBehaviour.Properties.of().mapColor(MapColor.WARPED_NYLIUM)
                        .strength(1.2f).noOcclusion().randomTicks().instabreak().ignitedByLava().dynamicShape().forceSolidOn()
                        .sound(SoundType.BAMBOO).pushReaction(PushReaction.DESTROY).offsetType(BlockBehaviour.OffsetType.XZ)));

        AZURE_COLEUS = PokecubeLegends.BLOCKS.register("azure_coleus", () -> new AzureColeusBlock(MobEffects.INVISIBILITY, 15,
                BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_BLUE).randomTicks().noCollission().instabreak().ignitedByLava()
                        .sound(SoundType.AZALEA).offsetType(BlockBehaviour.OffsetType.XZ).pushReaction(PushReaction.DESTROY)));
        GRACIDEA = PokecubeLegends.BLOCKS.register("gracidea",
                () -> new FlowerBase("gracidea", 1, MobEffects.LUCK, 10,
                        BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_PINK).noCollission().instabreak().ignitedByLava()
                                .sound(SoundType.GRASS).offsetType(BlockBehaviour.OffsetType.XZ).pushReaction(PushReaction.DESTROY)));

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

        CRYSTALLIZED_CACTUS = PokecubeLegends.BLOCKS.register("crystallized_cactus",
                () -> new CrystallizedCactus(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_LIGHT_BLUE)
                        .sound(SoundType.AMETHYST).pushReaction(PushReaction.DESTROY).strength(0.4f)));
        TALL_CRYSTALLIZED_BUSH = PokecubeLegends.BLOCKS.register("tall_crystallized_bush",
                () -> new TallCrystallizedBush(BlockBehaviour.Properties.of().mapColor(MapColor.SNOW)
                        .sound(SoundType.AMETHYST_CLUSTER).pushReaction(PushReaction.DESTROY)
                        .noCollission().instabreak()));
        CRYSTALLIZED_BUSH = PokecubeLegends.BLOCKS.register("crystallized_bush",
                () -> new CrystallizedBush(BlockBehaviour.Properties.of().mapColor(MapColor.SNOW)
                        .sound(SoundType.LARGE_AMETHYST_BUD).pushReaction(PushReaction.DESTROY)
                        .noCollission().instabreak()));

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

        GOLDEN_SHROOM_PLANT = PokecubeLegends.NO_ITEM_BLOCKS.register("golden_shroom_plant",
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

        POLLUTING_BLOSSOM = PokecubeLegends.BLOCKS.register("polluting_blossom",
                () -> new PollutingBlossomBlock(BlockBehaviour.Properties.of().mapColor(MapColor.TERRACOTTA_PURPLE)
                        .sound(SoundType.SPORE_BLOSSOM).pushReaction(PushReaction.DESTROY).noCollission().instabreak()));
        SMALL_CONTAMINATED_DRIPLEAF = PokecubeLegends.BLOCKS.register("small_contaminated_dripleaf",
                () -> new SmallContaminatedDripleafBlock(BlockBehaviour.Properties.of()
                        .mapColor(MapColor.TERRACOTTA_RED).sound(SoundType.SMALL_DRIPLEAF)
                        .pushReaction(PushReaction.DESTROY).offsetType(BlockBehaviour.OffsetType.XYZ)
                        .noCollission().instabreak()));
        BIG_CONTAMINATED_DRIPLEAF = PokecubeLegends.BLOCKS.register("big_contaminated_dripleaf",
                () -> new BigContaminatedDripleafBlock(BlockBehaviour.Properties.of().mapColor(MapColor.TERRACOTTA_RED)
                        .sound(SoundType.BIG_DRIPLEAF).pushReaction(PushReaction.DESTROY)
                        .strength(0.1F).forceSolidOff()));
        BIG_CONTAMINATED_DRIPLEAF_STEM = PokecubeLegends.NO_ITEM_BLOCKS.register("big_contaminated_dripleaf_stem",
                () -> new BigContaminatedDripleafStemBlock(BlockBehaviour.Properties.of()
                        .mapColor(MapColor.TERRACOTTA_RED).sound(SoundType.BIG_DRIPLEAF)
                        .pushReaction(PushReaction.DESTROY).strength(0.1F).noCollission()));

        TAINTED_ROOTS = PokecubeLegends.BLOCKS.register("tainted_roots", () -> new TaintedRootsBlock(
                BlockBehaviour.Properties.of().mapColor(MapColor.TERRACOTTA_PURPLE).noCollission().replaceable().instabreak().ignitedByLava()
                        .sound(SoundType.ROOTS).offsetType(BlockBehaviour.OffsetType.XZ).pushReaction(PushReaction.DESTROY)));

        TAINTED_KELP = PokecubeLegends.BLOCKS.register("tainted_kelp", () -> new TaintedKelpBlock(
                BlockBehaviour.Properties.of().mapColor(MapColor.TERRACOTTA_PURPLE).noCollission().instabreak().ignitedByLava()
                        .sound(SoundType.WET_GRASS).offsetType(BlockBehaviour.OffsetType.XZ).pushReaction(PushReaction.DESTROY)));

        TAINTED_KELP_PLANT = PokecubeLegends.BLOCKS.register("tainted_kelp_plant", () -> new TaintedKelpPlantBlock(
                BlockBehaviour.Properties.of().mapColor(MapColor.TERRACOTTA_PURPLE).noCollission().instabreak().ignitedByLava()
                        .sound(SoundType.WET_GRASS).offsetType(BlockBehaviour.OffsetType.XZ).pushReaction(PushReaction.DESTROY)));

        PINK_TAINTED_LILY_PAD = PokecubeLegends.NO_ITEM_BLOCKS.register("pink_blossom_tainted_lily_pad", () -> new BlossomLilyPadBlock(
                BlockBehaviour.Properties.of().mapColor(MapColor.TERRACOTTA_PURPLE).noOcclusion().instabreak().ignitedByLava()
                        .sound(SoundType.LILY_PAD).pushReaction(PushReaction.DESTROY)));

        TAINTED_LILY_PAD = PokecubeLegends.NO_ITEM_BLOCKS.register("tainted_lily_pad", () -> new LilyPadBlock(
                BlockBehaviour.Properties.of().mapColor(MapColor.TERRACOTTA_PURPLE).noOcclusion().instabreak().ignitedByLava()
                        .sound(SoundType.GRASS).pushReaction(PushReaction.DESTROY)));

        HANGING_TENDRILS = PokecubeLegends.BLOCKS.register("hanging_tendrils", () -> new HangingTendrilsBlock(
                BlockBehaviour.Properties.of().mapColor(MapColor.TERRACOTTA_PURPLE).randomTicks().noCollission().instabreak().ignitedByLava()
                        .sound(SoundType.CAVE_VINES).pushReaction(PushReaction.DESTROY).lightLevel(HangingTendrilsBlock.emission(5))));
        HANGING_TENDRILS_PLANT = PokecubeLegends.NO_ITEM_BLOCKS.register("hanging_tendrils_plant", () -> new HangingTendrilsPlantBlock(
                BlockBehaviour.Properties.of().mapColor(MapColor.TERRACOTTA_PURPLE).noCollission().instabreak().ignitedByLava()
                        .sound(SoundType.CAVE_VINES).pushReaction(PushReaction.DESTROY).lightLevel(HangingTendrilsBlock.emission(5))));

        TAINTED_SEAGRASS = PokecubeLegends.BLOCKS.register("tainted_seagrass", () -> new TaintedSeagrassBlock(
                BlockBehaviour.Properties.of().mapColor(MapColor.TERRACOTTA_PURPLE).replaceable().instabreak().ignitedByLava()
                        .sound(SoundType.WET_GRASS).pushReaction(PushReaction.DESTROY)));

        TALL_TAINTED_SEAGRASS = PokecubeLegends.BLOCKS.register("tall_tainted_seagrass", () -> new TallTaintedSeagrassBlock(
                BlockBehaviour.Properties.of().mapColor(MapColor.TERRACOTTA_PURPLE).replaceable().instabreak().ignitedByLava()
                        .sound(SoundType.WET_GRASS).pushReaction(PushReaction.DESTROY)));

        PURPLE_WISTERIA_VINES = PokecubeLegends.BLOCKS.register("purple_wisteria_vines", () -> new PurpleWisteriaVinesBlock(
                BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_PURPLE).randomTicks().noCollission().instabreak().ignitedByLava()
                        .sound(SoundType.CAVE_VINES).pushReaction(PushReaction.DESTROY)));
        PURPLE_WISTERIA_VINES_PLANT = PokecubeLegends.NO_ITEM_BLOCKS.register("purple_wisteria_vines_plant", () -> new PurpleWisteriaVinesPlantBlock(
                BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_PURPLE).noCollission().instabreak().ignitedByLava()
                        .sound(SoundType.CAVE_VINES).pushReaction(PushReaction.DESTROY)));

        DISTORTIC_GRASS = PokecubeLegends.BLOCKS.register("distortic_grass", () -> new TallDistorticGrassBlock(
                BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_MAGENTA).noCollission().replaceable().instabreak().ignitedByLava()
                        .sound(SoundType.ROOTS).offsetType(BlockBehaviour.OffsetType.XZ).pushReaction(PushReaction.DESTROY)));
        
        DISTORTIC_VINES = PokecubeLegends.NO_ITEM_BLOCKS.register("distortic_vines", () -> new DistortedVinesTopBlock(
                BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_MAGENTA).randomTicks().noCollission().instabreak().ignitedByLava()
                        .sound(SoundType.WEEPING_VINES).offsetType(BlockBehaviour.OffsetType.XZ).pushReaction(PushReaction.DESTROY)));
        DISTORTIC_VINES_PLANT = PokecubeLegends.NO_ITEM_BLOCKS.register("distortic_vines_plant", () -> new DistortedVinesBlock(
                BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_MAGENTA).noCollission().instabreak().ignitedByLava()
                        .sound(SoundType.WEEPING_VINES).offsetType(BlockBehaviour.OffsetType.XZ).pushReaction(PushReaction.DESTROY)));
        STRING_OF_PEARLS = PokecubeLegends.BLOCKS.register("string_of_pearls",
                () -> new StringOfPearlsBlock(BlockBehaviour.Properties.of().mapColor(MapColor.WARPED_NYLIUM)
                        .sound(SoundType.VINE).pushReaction(PushReaction.DESTROY).strength(0.2F)
                        .noCollission().ignitedByLava().replaceable().randomTicks()));
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
