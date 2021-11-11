package pokecube.legends.init;

import java.util.function.ToIntFunction;

import net.minecraft.core.Direction;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ComposterBlock;
import net.minecraft.world.level.block.FenceBlock;
import net.minecraft.world.level.block.FenceGateBlock;
import net.minecraft.world.level.block.FireBlock;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.MagmaBlock;
import net.minecraft.world.level.block.OreBlock;
import net.minecraft.world.level.block.PressurePlateBlock;
import net.minecraft.world.level.block.RedStoneOreBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.SandBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.WallBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.material.MaterialColor;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraftforge.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.minecraftforge.fmllegacy.RegistryObject;
import pokecube.core.PokecubeItems;
import pokecube.core.handlers.ItemGenerator;
import pokecube.core.handlers.ItemGenerator.GenericStairs;
import pokecube.legends.PokecubeLegends;
import pokecube.legends.blocks.BlockBase;
import pokecube.legends.blocks.EffectBlockBase;
import pokecube.legends.blocks.EffectBlockBaseSand;
import pokecube.legends.blocks.FaceBlock_Base;
import pokecube.legends.blocks.SaplingBase;
import pokecube.legends.blocks.containers.GenericBarrel;
import pokecube.legends.blocks.containers.GenericBookshelfEmpty;
import pokecube.legends.blocks.customblocks.CramomaticBlock;
import pokecube.legends.blocks.customblocks.HeatranBlock;
import pokecube.legends.blocks.customblocks.KeldeoBlock;
import pokecube.legends.blocks.customblocks.MagearnaBlock;
import pokecube.legends.blocks.customblocks.NatureCoreBlock;
import pokecube.legends.blocks.customblocks.PortalWarp;
import pokecube.legends.blocks.customblocks.RaidSpawnBlock;
import pokecube.legends.blocks.customblocks.TaoTrioBlock;
import pokecube.legends.blocks.customblocks.TapuBuluCore;
import pokecube.legends.blocks.customblocks.TapuFiniCore;
import pokecube.legends.blocks.customblocks.TapuKokoCore;
import pokecube.legends.blocks.customblocks.TapuLeleCore;
import pokecube.legends.blocks.customblocks.TimeSpaceCoreBlock;
import pokecube.legends.blocks.customblocks.TroughBlock;
import pokecube.legends.blocks.customblocks.VictiniBlock;
import pokecube.legends.blocks.customblocks.XerneasCore;
import pokecube.legends.blocks.customblocks.YveltalEgg;
import pokecube.legends.blocks.customblocks.taputotem.BuluTotem;
import pokecube.legends.blocks.customblocks.taputotem.FiniTotem;
import pokecube.legends.blocks.customblocks.taputotem.KokoTotem;
import pokecube.legends.blocks.customblocks.taputotem.LeleTotem;
import pokecube.legends.blocks.normalblocks.CorruptedLeavesBlock;
import pokecube.legends.blocks.normalblocks.DirtCorruptedBlock;
import pokecube.legends.blocks.normalblocks.DistorticCrackedStone;
import pokecube.legends.blocks.normalblocks.DistorticStoneBlock;
import pokecube.legends.blocks.normalblocks.DynaLeavesBlock;
import pokecube.legends.blocks.normalblocks.GenericBookshelf;
import pokecube.legends.blocks.normalblocks.GrassAgedBlock;
import pokecube.legends.blocks.normalblocks.GrassCorruptedBlock;
import pokecube.legends.blocks.normalblocks.GrassDistorticBlock;
import pokecube.legends.blocks.normalblocks.GrassJungleBlock;
import pokecube.legends.blocks.normalblocks.GrassMushroomBlock;
import pokecube.legends.blocks.normalblocks.InfectedTorch;
import pokecube.legends.blocks.normalblocks.InfectedTorchWall;
import pokecube.legends.blocks.normalblocks.MagneticBlock;
import pokecube.legends.blocks.normalblocks.MeteorBlock;
import pokecube.legends.blocks.normalblocks.MeteorCosmicOreBlock;
import pokecube.legends.blocks.normalblocks.MirageGlassBlock;
import pokecube.legends.blocks.normalblocks.MirageLeavesBlock;
import pokecube.legends.blocks.normalblocks.OneWayGlass;
import pokecube.legends.blocks.normalblocks.OneWayLaboratoryGlass;
import pokecube.legends.blocks.normalblocks.OneWayMirageGlass;
import pokecube.legends.blocks.normalblocks.OneWaySpectrumGlass;
import pokecube.legends.blocks.normalblocks.OneWayStainedGlass;
import pokecube.legends.blocks.normalblocks.SpectrumGlassBlock;
import pokecube.legends.blocks.normalblocks.WallGateBlock;
import pokecube.legends.blocks.plants.AgedTree;
import pokecube.legends.blocks.plants.CorruptedTree;
import pokecube.legends.blocks.plants.CrystallizedBush;
import pokecube.legends.blocks.plants.CrystallizedCactus;
import pokecube.legends.blocks.plants.DistortedVinesBlock;
import pokecube.legends.blocks.plants.DistortedVinesTopBlock;
import pokecube.legends.blocks.plants.DistorticTree;
import pokecube.legends.blocks.plants.InvertedTree;
import pokecube.legends.blocks.plants.MirageSapling;
import pokecube.legends.blocks.plants.MirageTree;
import pokecube.legends.blocks.plants.PottedCrystallizedBush;
import pokecube.legends.blocks.plants.PottedCrystallizedCactus;
import pokecube.legends.blocks.plants.TallCrystallizedBush;
import pokecube.legends.blocks.plants.TemporalBambooBlock;
import pokecube.legends.blocks.plants.TemporalBambooShootBlock;
import pokecube.legends.blocks.plants.TemporalTree;

public class BlockInit
{
    // Blocks
    public static final RegistryObject<Block> RAID_SPAWNER;

    public static final RegistryObject<Block> CRAMOMATIC_BLOCK;

    public static final RegistryObject<Block> METEOR_BLOCK;
    public static final RegistryObject<Block> METEOR_SLAB;
    public static final RegistryObject<Block> METEOR_STAIRS;

    // Decorative_Blocks
    public static final RegistryObject<Block> OCEAN_BRICKS;
    public static final RegistryObject<Block> OCEAN_BRICK_SLAB;
    public static final RegistryObject<Block> OCEAN_BRICK_STAIRS;

    public static final RegistryObject<Block> SKY_BRICKS;
    public static final RegistryObject<Block> SKY_BRICK_SLAB;
    public static final RegistryObject<Block> SKY_BRICK_STAIRS;

    public static final RegistryObject<Block> PURPUR_BRICKS;
    public static final RegistryObject<Block> PURPUR_BRICK_SLAB;
    public static final RegistryObject<Block> PURPUR_BRICK_STAIRS;

    public static final RegistryObject<Block> MAGMA_BRICKS;
    public static final RegistryObject<Block> MAGMA_BRICK_SLAB;
    public static final RegistryObject<Block> MAGMA_BRICK_STAIRS;

    public static final RegistryObject<Block> STORMY_SKY_BRICKS;
    public static final RegistryObject<Block> STORMY_SKY_BRICK_SLAB;
    public static final RegistryObject<Block> STORMY_SKY_BRICK_STAIRS;

    // Unowns
    public static final RegistryObject<Block> UNOWN_STONE_A;
    public static final RegistryObject<Block> UNOWN_STONE_B;
    public static final RegistryObject<Block> UNOWN_STONE_C;
    public static final RegistryObject<Block> UNOWN_STONE_D;
    public static final RegistryObject<Block> UNOWN_STONE_E;
    public static final RegistryObject<Block> UNOWN_STONE_F;
    public static final RegistryObject<Block> UNOWN_STONE_G;
    public static final RegistryObject<Block> UNOWN_STONE_H;
    public static final RegistryObject<Block> UNOWN_STONE_I;
    public static final RegistryObject<Block> UNOWN_STONE_J;
    public static final RegistryObject<Block> UNOWN_STONE_K;
    public static final RegistryObject<Block> UNOWN_STONE_L;
    public static final RegistryObject<Block> UNOWN_STONE_M;
    public static final RegistryObject<Block> UNOWN_STONE_N;
    public static final RegistryObject<Block> UNOWN_STONE_O;
    public static final RegistryObject<Block> UNOWN_STONE_P;
    public static final RegistryObject<Block> UNOWN_STONE_Q;
    public static final RegistryObject<Block> UNOWN_STONE_R;
    public static final RegistryObject<Block> UNOWN_STONE_S;
    public static final RegistryObject<Block> UNOWN_STONE_T;
    public static final RegistryObject<Block> UNOWN_STONE_U;
    public static final RegistryObject<Block> UNOWN_STONE_V;
    public static final RegistryObject<Block> UNOWN_STONE_W;
    public static final RegistryObject<Block> UNOWN_STONE_X;
    public static final RegistryObject<Block> UNOWN_STONE_Y;
    public static final RegistryObject<Block> UNOWN_STONE_Z;
    public static final RegistryObject<Block> UNOWN_STONE_EX;
    public static final RegistryObject<Block> UNOWN_STONE_IN;

    public static final RegistryObject<Block> DYNA_LEAVES_PINK;
    public static final RegistryObject<Block> DYNA_LEAVES_RED;
    public static final RegistryObject<Block> DYNA_LEAVES_PASTEL_PINK;
    public static final RegistryObject<Block> TOTEM_BLOCK;

    // Tapus Totems
    // Koko Totem
    public static final RegistryObject<Block> KOKO_WHITE;
    public static final RegistryObject<Block> KOKO_RED;
    public static final RegistryObject<Block> KOKO_BLUE;
    public static final RegistryObject<Block> KOKO_GREEN;
    public static final RegistryObject<Block> KOKO_YELLOW;
    public static final RegistryObject<Block> KOKO_PURPLE;
    public static final RegistryObject<Block> KOKO_PINK;
    public static final RegistryObject<Block> KOKO_BLACK;
    public static final RegistryObject<Block> KOKO_BROWN;
    public static final RegistryObject<Block> KOKO_LIME;
    public static final RegistryObject<Block> KOKO_CYAN;
    public static final RegistryObject<Block> KOKO_LIGHT_GRAY;
    public static final RegistryObject<Block> KOKO_GRAY;
    public static final RegistryObject<Block> KOKO_MAGENTA;
    public static final RegistryObject<Block> KOKO_LIGHT_BLUE;
    public static final RegistryObject<Block> KOKO_ORANGE;

    // Bulu Totem
    public static final RegistryObject<Block> BULU_WHITE;
    public static final RegistryObject<Block> BULU_RED;
    public static final RegistryObject<Block> BULU_BLUE;
    public static final RegistryObject<Block> BULU_GREEN;
    public static final RegistryObject<Block> BULU_YELLOW;
    public static final RegistryObject<Block> BULU_PURPLE;
    public static final RegistryObject<Block> BULU_PINK;
    public static final RegistryObject<Block> BULU_BLACK;
    public static final RegistryObject<Block> BULU_BROWN;
    public static final RegistryObject<Block> BULU_LIME;
    public static final RegistryObject<Block> BULU_CYAN;
    public static final RegistryObject<Block> BULU_LIGHT_GRAY;
    public static final RegistryObject<Block> BULU_GRAY;
    public static final RegistryObject<Block> BULU_MAGENTA;
    public static final RegistryObject<Block> BULU_LIGHT_BLUE;
    public static final RegistryObject<Block> BULU_ORANGE;

    // Lele Totem
    public static final RegistryObject<Block> LELE_WHITE;
    public static final RegistryObject<Block> LELE_RED;
    public static final RegistryObject<Block> LELE_BLUE;
    public static final RegistryObject<Block> LELE_GREEN;
    public static final RegistryObject<Block> LELE_YELLOW;
    public static final RegistryObject<Block> LELE_PURPLE;
    public static final RegistryObject<Block> LELE_PINK;
    public static final RegistryObject<Block> LELE_BLACK;
    public static final RegistryObject<Block> LELE_BROWN;
    public static final RegistryObject<Block> LELE_LIME;
    public static final RegistryObject<Block> LELE_CYAN;
    public static final RegistryObject<Block> LELE_LIGHT_GRAY;
    public static final RegistryObject<Block> LELE_GRAY;
    public static final RegistryObject<Block> LELE_MAGENTA;
    public static final RegistryObject<Block> LELE_LIGHT_BLUE;
    public static final RegistryObject<Block> LELE_ORANGE;

    // Fini Totem
    public static final RegistryObject<Block> FINI_WHITE;
    public static final RegistryObject<Block> FINI_RED;
    public static final RegistryObject<Block> FINI_BLUE;
    public static final RegistryObject<Block> FINI_GREEN;
    public static final RegistryObject<Block> FINI_YELLOW;
    public static final RegistryObject<Block> FINI_PURPLE;
    public static final RegistryObject<Block> FINI_PINK;
    public static final RegistryObject<Block> FINI_BLACK;
    public static final RegistryObject<Block> FINI_BROWN;
    public static final RegistryObject<Block> FINI_LIME;
    public static final RegistryObject<Block> FINI_CYAN;
    public static final RegistryObject<Block> FINI_LIGHT_GRAY;
    public static final RegistryObject<Block> FINI_GRAY;
    public static final RegistryObject<Block> FINI_MAGENTA;
    public static final RegistryObject<Block> FINI_LIGHT_BLUE;
    public static final RegistryObject<Block> FINI_ORANGE;

    // Dimensions
    // Distortic World
    public static final RegistryObject<Block> DISTORTIC_GRASS;
    public static final RegistryObject<Block> DISTORTIC_STONE;
    public static final RegistryObject<Block> DISTORTIC_STONE_SLAB;
    public static final RegistryObject<Block> DISTORTIC_STONE_STAIRS;
    public static final RegistryObject<Block> DISTORTIC_MIRROR;
    public static final RegistryObject<Block> CRACKED_DISTORTIC_STONE;
    public static final RegistryObject<Block> DISTORTIC_GLOWSTONE;
    public static final RegistryObject<Block> CHISELED_DISTORTIC_MIRROR;
    public static final RegistryObject<Block> FRAMED_DISTORTIC_MIRROR;
    public static final RegistryObject<Block> ONE_WAY_GLASS;
    public static final RegistryObject<Block> ONE_WAY_GLASS_WHITE;
    public static final RegistryObject<Block> ONE_WAY_GLASS_ORANGE;
    public static final RegistryObject<Block> ONE_WAY_GLASS_MAGENTA;
    public static final RegistryObject<Block> ONE_WAY_GLASS_LIGHT_BLUE;
    public static final RegistryObject<Block> ONE_WAY_GLASS_YELLOW;
    public static final RegistryObject<Block> ONE_WAY_GLASS_LIME;
    public static final RegistryObject<Block> ONE_WAY_GLASS_PINK;
    public static final RegistryObject<Block> ONE_WAY_GLASS_GRAY;
    public static final RegistryObject<Block> ONE_WAY_GLASS_LIGHT_GRAY;
    public static final RegistryObject<Block> ONE_WAY_GLASS_CYAN;
    public static final RegistryObject<Block> ONE_WAY_GLASS_PURPLE;
    public static final RegistryObject<Block> ONE_WAY_GLASS_BLUE;
    public static final RegistryObject<Block> ONE_WAY_GLASS_BROWN;
    public static final RegistryObject<Block> ONE_WAY_GLASS_GREEN;
    public static final RegistryObject<Block> ONE_WAY_GLASS_RED;
    public static final RegistryObject<Block> ONE_WAY_GLASS_BLACK;
    public static final RegistryObject<Block> ONE_WAY_GLASS_LAB;
    public static final RegistryObject<Block> ONE_WAY_GLASS_MIRAGE;
    public static final RegistryObject<Block> ONE_WAY_GLASS_SPECTRUM;
    public static final RegistryObject<Block> ONE_WAY_FRAMED_MIRROR;

    public static final RegistryObject<Block> DISTORTIC_STONE_BRICKS;
    public static final RegistryObject<Block> DISTORTIC_STONE_BRICK_SLAB;
    public static final RegistryObject<Block> DISTORTIC_STONE_BRICK_STAIRS;
    public static final RegistryObject<Block> DISTORTIC_STONE_BARREL;

    public static final RegistryObject<Block> CHISELED_DISTORTIC_STONE;
    public static final RegistryObject<Block> CHISELED_DISTORTIC_STONE_SLAB;
    public static final RegistryObject<Block> CHISELED_DISTORTIC_STONE_STAIRS;

    public static final RegistryObject<Block> DISTORTIC_TERRACOTTA;
    public static final RegistryObject<Block> DISTORTIC_TERRACOTTA_SLAB;
    public static final RegistryObject<Block> DISTORTIC_TERRACOTTA_STAIRS;

    public static final RegistryObject<Block> DISTORTIC_OAK_PLANKS;
    public static final RegistryObject<Block> DISTORTIC_OAK_SLAB;
    public static final RegistryObject<Block> DISTORTIC_OAK_STAIRS;

    public static final RegistryObject<Block> DISTORTIC_DARK_OAK_PLANKS;
    public static final RegistryObject<Block> DISTORTIC_DARK_OAK_SLAB;
    public static final RegistryObject<Block> DISTORTIC_DARK_OAK_STAIRS;

    public static final RegistryObject<Block> DISTORTIC_SPRUCE_PLANKS;
    public static final RegistryObject<Block> DISTORTIC_SPRUCE_SLAB;
    public static final RegistryObject<Block> DISTORTIC_SPRUCE_STAIRS;

    public static final RegistryObject<Block> DISTORTIC_BIRCH_PLANKS;
    public static final RegistryObject<Block> DISTORTIC_BIRCH_SLAB;
    public static final RegistryObject<Block> DISTORTIC_BIRCH_STAIRS;

    public static final RegistryObject<Block> DISTORTIC_ACACIA_PLANKS;
    public static final RegistryObject<Block> DISTORTIC_ACACIA_SLAB;
    public static final RegistryObject<Block> DISTORTIC_ACACIA_STAIRS;

    public static final RegistryObject<Block> DISTORTIC_JUNGLE_PLANKS;
    public static final RegistryObject<Block> DISTORTIC_JUNGLE_SLAB;
    public static final RegistryObject<Block> DISTORTIC_JUNGLE_STAIRS;

    public static final RegistryObject<Block> BOOKSHELF_EMPTY;

    public static final RegistryObject<Block> INFECTED_TORCH;
    public static final RegistryObject<Block> INFECTED_TORCH_WALL;

    public static final RegistryObject<Block> ULTRA_MAGNETIC;
    public static final RegistryObject<Block> MUSHROOM_GRASS;
    public static final RegistryObject<Block> MUSHROOM_DIRT;
    public static final RegistryObject<Block> JUNGLE_GRASS;
    public static final RegistryObject<Block> JUNGLE_DIRT;
    public static final RegistryObject<Block> SPECTRUM_GLASS;
    public static final RegistryObject<Block> CORRUPTED_GRASS;
    public static final RegistryObject<Block> CORRUPTED_DIRT;
    public static final RegistryObject<Block> AGED_GRASS;
    public static final RegistryObject<Block> AGED_DIRT;

    // Crystal Blocks
    public static final RegistryObject<Block> CRYSTAL;
    public static final RegistryObject<Block> CRYSTAL_BUTTON;
    public static final RegistryObject<Block> CRYSTAL_BRICKS;
    public static final RegistryObject<Block> CRYSTAL_STAIRS;
    public static final RegistryObject<Block> CRYSTAL_SLAB;
    public static final RegistryObject<Block> CRYSTAL_BRICK_SLAB;
    public static final RegistryObject<Block> CRYSTAL_BRICK_STAIRS;

    // Ultra Stone
    public static final RegistryObject<Block> ULTRA_STONE;
    public static final RegistryObject<Block> ULTRA_STONE_SLAB;
    public static final RegistryObject<Block> ULTRA_STONE_STAIRS;
    public static final RegistryObject<Block> ULTRA_COBBLESTONE;
    public static final RegistryObject<Block> ULTRA_COBBLESTONE_SLAB;
    public static final RegistryObject<Block> ULTRA_COBBLESTONE_STAIRS;
    public static final RegistryObject<Block> ULTRA_STONE_BRICK_SLAB;
    public static final RegistryObject<Block> ULTRA_STONE_BRICK_STAIRS;
    public static final RegistryObject<Block> ULTRA_STONE_BRICKS;
    public static final RegistryObject<Block> ULTRA_STONE_BUTTON;
    public static final RegistryObject<Block> ULTRA_STONE_PR_PLATE;

    public static final RegistryObject<Block> ULTRA_METAL;
    public static final RegistryObject<Block> ULTRA_METAL_SLAB;
    public static final RegistryObject<Block> ULTRA_METAL_STAIRS;
    public static final RegistryObject<Block> ULTRA_METAL_BUTTON;
    public static final RegistryObject<Block> ULTRA_METAL_PR_PLATE;

    // Darkstone Blocks
    public static final RegistryObject<Block> ULTRA_DARKSTONE;
    public static final RegistryObject<Block> ULTRA_DARKSTONE_BRICKS;
    public static final RegistryObject<Block> ULTRA_DARKSTONE_BRICKS_SLAB;
    public static final RegistryObject<Block> ULTRA_DARKSTONE_BRICKS_STAIRS;
    public static final RegistryObject<Block> ULTRA_DARK_COBBLESTONE;
    public static final RegistryObject<Block> ULTRA_DARK_COBBLESTONE_SLAB;
    public static final RegistryObject<Block> ULTRA_DARK_COBBLESTONE_STAIRS;
    public static final RegistryObject<Block> ULTRA_DARKSTONE_SLAB;
    public static final RegistryObject<Block> ULTRA_DARKSTONE_STAIRS;
    public static final RegistryObject<Block> ULTRA_DARKSTONE_BUTTON;
    public static final RegistryObject<Block> ULTRA_DARKSTONE_PR_PLATE;

    // Mirage Desert
    public static final RegistryObject<Block> CRYSTALLIZED_SAND;
    public static final RegistryObject<Block> CRYSTALLIZED_SANDSTONE;
    public static final RegistryObject<Block> CRYS_SANDSTONE_SLAB;
    public static final RegistryObject<Block> CRYS_SANDSTONE_STAIRS;
    public static final RegistryObject<Block> CRYS_SANDSTONE_BRICKS;
    public static final RegistryObject<Block> CRYS_SANDSTONE_BRICK_SLAB;
    public static final RegistryObject<Block> CRYS_SANDSTONE_BRICK_STAIRS;
    public static final RegistryObject<Block> CRYS_SANDSTONE_SMOOTH;
    public static final RegistryObject<Block> CRYS_SANDSTONE_SMOOTH_SLAB;
    public static final RegistryObject<Block> CRYS_SANDSTONE_SMOOTH_STAIRS;
    public static final RegistryObject<Block> CRYS_SANDSTONE_BUTTON;
    public static final RegistryObject<Block> CRYS_SANDSTONE_PR_PLATE;

    // Woods
    public static final RegistryObject<Block> AGED_LOG;
    public static final RegistryObject<Block> AGED_PLANKS;
    public static final RegistryObject<Block> AGED_LEAVES;
    public static final RegistryObject<Block> AGED_WOOD;
    public static final RegistryObject<Block> STRIP_AGED_LOG;
    public static final RegistryObject<Block> STRIP_AGED_WOOD;
    public static final RegistryObject<Block> AGED_STAIRS;
    public static final RegistryObject<Block> AGED_SLAB;
    public static final RegistryObject<Block> AGED_FENCE;
    public static final RegistryObject<Block> AGED_FENCE_GATE;
    public static final RegistryObject<Block> AGED_TRAPDOOR;
    public static final RegistryObject<Block> AGED_DOOR;
    public static final RegistryObject<Block> AGED_BUTTON;
    public static final RegistryObject<Block> AGED_PR_PLATE;
    public static final RegistryObject<Block> AGED_BARREL;
    public static final RegistryObject<Block> AGED_BOOKSHELF;
    public static final RegistryObject<Block> AGED_BOOKSHELF_EMPTY;

    public static final RegistryObject<Block> CONCRETE_LOG;
    public static final RegistryObject<Block> CONCRETE_PLANKS;
    public static final RegistryObject<Block> CONCRETE_WOOD;
    public static final RegistryObject<Block> STRIP_CONCRETE_LOG;
    public static final RegistryObject<Block> STRIP_CONCRETE_WOOD;
    public static final RegistryObject<Block> CONCRETE_STAIRS;
    public static final RegistryObject<Block> CONCRETE_SLAB;
    public static final RegistryObject<Block> CONCRETE_FENCE;
    public static final RegistryObject<Block> CONCRETE_FENCE_GATE;
    public static final RegistryObject<Block> CONCRETE_TRAPDOOR;
    public static final RegistryObject<Block> CONCRETE_DOOR;
    public static final RegistryObject<Block> CONCRETE_BUTTON;
    public static final RegistryObject<Block> CONCRETE_PR_PLATE;
    public static final RegistryObject<Block> CONCRETE_BARREL;
    public static final RegistryObject<Block> CONCRETE_BOOKSHELF;
    public static final RegistryObject<Block> CONCRETE_BOOKSHELF_EMPTY;

    public static final RegistryObject<Block> CONCRETE_DENSE_PLANKS;
    public static final RegistryObject<Block> CONCRETE_DENSE_STAIRS;
    public static final RegistryObject<Block> CONCRETE_DENSE_SLAB;
    public static final RegistryObject<Block> CONCRETE_DENSE_WALL;
    public static final RegistryObject<Block> CONCRETE_DENSE_WALL_GATE;
    public static final RegistryObject<Block> CONCRETE_DENSE_BUTTON;
    public static final RegistryObject<Block> CONCRETE_DENSE_PR_PLATE;
    public static final RegistryObject<Block> CONCRETE_DENSE_BARREL;
    public static final RegistryObject<Block> CONCRETE_DENSE_BOOKSHELF;
    public static final RegistryObject<Block> CONCRETE_DENSE_BOOKSHELF_EMPTY;

    public static final RegistryObject<Block> CORRUPTED_LOG;
    public static final RegistryObject<Block> CORRUPTED_PLANKS;
    public static final RegistryObject<Block> CORRUPTED_LEAVES;
    public static final RegistryObject<Block> CORRUPTED_WOOD;
    public static final RegistryObject<Block> STRIP_CORRUPTED_LOG;
    public static final RegistryObject<Block> STRIP_CORRUPTED_WOOD;
    public static final RegistryObject<Block> CORRUPTED_STAIRS;
    public static final RegistryObject<Block> CORRUPTED_SLAB;
    public static final RegistryObject<Block> CORRUPTED_FENCE;
    public static final RegistryObject<Block> CORRUPTED_FENCE_GATE;
    public static final RegistryObject<Block> CORRUPTED_TRAPDOOR;
    public static final RegistryObject<Block> CORRUPTED_DOOR;
    public static final RegistryObject<Block> CORRUPTED_BUTTON;
    public static final RegistryObject<Block> CORRUPTED_PR_PLATE;
    public static final RegistryObject<Block> CORRUPTED_BARREL;
    public static final RegistryObject<Block> CORRUPTED_BOOKSHELF;
    public static final RegistryObject<Block> CORRUPTED_BOOKSHELF_EMPTY;

    public static final RegistryObject<Block> DISTORTIC_LOG;
    public static final RegistryObject<Block> DISTORTIC_PLANKS;
    public static final RegistryObject<Block> DISTORTIC_LEAVES;
    public static final RegistryObject<Block> DISTORTIC_WOOD;
    public static final RegistryObject<Block> STRIP_DISTORTIC_LOG;
    public static final RegistryObject<Block> STRIP_DISTORTIC_WOOD;
    public static final RegistryObject<Block> DISTORTIC_STAIRS;
    public static final RegistryObject<Block> DISTORTIC_SLAB;
    public static final RegistryObject<Block> DISTORTIC_FENCE;
    public static final RegistryObject<Block> DISTORTIC_FENCE_GATE;
    public static final RegistryObject<Block> DISTORTIC_TRAPDOOR;
    public static final RegistryObject<Block> DISTORTIC_DOOR;
    public static final RegistryObject<Block> DISTORTIC_BUTTON;
    public static final RegistryObject<Block> DISTORTIC_PR_PLATE;
    public static final RegistryObject<Block> DISTORTIC_BARREL;
    public static final RegistryObject<Block> DISTORTIC_BOOKSHELF;
    public static final RegistryObject<Block> DISTORTIC_BOOKSHELF_EMPTY;

    public static final RegistryObject<Block> INVERTED_LOG;
    public static final RegistryObject<Block> INVERTED_PLANKS;
    public static final RegistryObject<Block> INVERTED_LEAVES;
    public static final RegistryObject<Block> INVERTED_WOOD;
    public static final RegistryObject<Block> STRIP_INVERTED_LOG;
    public static final RegistryObject<Block> STRIP_INVERTED_WOOD;
    public static final RegistryObject<Block> INVERTED_STAIRS;
    public static final RegistryObject<Block> INVERTED_SLAB;
    public static final RegistryObject<Block> INVERTED_FENCE;
    public static final RegistryObject<Block> INVERTED_FENCE_GATE;
    public static final RegistryObject<Block> INVERTED_TRAPDOOR;
    public static final RegistryObject<Block> INVERTED_DOOR;
    public static final RegistryObject<Block> INVERTED_BUTTON;
    public static final RegistryObject<Block> INVERTED_PR_PLATE;
    public static final RegistryObject<Block> INVERTED_BARREL;
    public static final RegistryObject<Block> INVERTED_BOOKSHELF;
    public static final RegistryObject<Block> INVERTED_BOOKSHELF_EMPTY;

    public static final RegistryObject<Block> MIRAGE_GLASS;
    public static final RegistryObject<Block> MIRAGE_LOG;
    public static final RegistryObject<Block> MIRAGE_PLANKS;
    public static final RegistryObject<Block> MIRAGE_LEAVES;
    public static final RegistryObject<Block> MIRAGE_WOOD;
    public static final RegistryObject<Block> STRIP_MIRAGE_LOG;
    public static final RegistryObject<Block> STRIP_MIRAGE_WOOD;
    public static final RegistryObject<Block> MIRAGE_STAIRS;
    public static final RegistryObject<Block> MIRAGE_SLAB;
    public static final RegistryObject<Block> MIRAGE_FENCE;
    public static final RegistryObject<Block> MIRAGE_FENCE_GATE;
    public static final RegistryObject<Block> MIRAGE_TRAPDOOR;
    public static final RegistryObject<Block> MIRAGE_DOOR;
    public static final RegistryObject<Block> MIRAGE_BUTTON;
    public static final RegistryObject<Block> MIRAGE_PR_PLATE;
    public static final RegistryObject<Block> MIRAGE_BARREL;
    public static final RegistryObject<Block> MIRAGE_BOOKSHELF;
    public static final RegistryObject<Block> MIRAGE_BOOKSHELF_EMPTY;

    public static final RegistryObject<Block> TEMPORAL_LOG;
    public static final RegistryObject<Block> TEMPORAL_PLANKS;
    public static final RegistryObject<Block> TEMPORAL_LEAVES;
    public static final RegistryObject<Block> TEMPORAL_WOOD;
    public static final RegistryObject<Block> STRIP_TEMPORAL_LOG;
    public static final RegistryObject<Block> STRIP_TEMPORAL_WOOD;
    public static final RegistryObject<Block> TEMPORAL_STAIRS;
    public static final RegistryObject<Block> TEMPORAL_SLAB;
    public static final RegistryObject<Block> TEMPORAL_FENCE;
    public static final RegistryObject<Block> TEMPORAL_FENCE_GATE;
    public static final RegistryObject<Block> TEMPORAL_TRAPDOOR;
    public static final RegistryObject<Block> TEMPORAL_DOOR;
    public static final RegistryObject<Block> TEMPORAL_BUTTON;
    public static final RegistryObject<Block> TEMPORAL_PR_PLATE;
    public static final RegistryObject<Block> TEMPORAL_BARREL;
    public static final RegistryObject<Block> TEMPORAL_BOOKSHELF;
    public static final RegistryObject<Block> TEMPORAL_BOOKSHELF_EMPTY;

    // Plants
    public static final RegistryObject<Block> INVERTED_SAPLING;
    public static final RegistryObject<Block> TEMPORAL_SAPLING;
    public static final RegistryObject<Block> AGED_SAPLING;
    public static final RegistryObject<Block> CORRUPTED_SAPLING;
    public static final RegistryObject<Block> MIRAGE_SAPLING;
    public static final RegistryObject<Block> DISTORTIC_SAPLING;
    public static final RegistryObject<Block> CRYSTALLIZED_BUSH;
    public static final RegistryObject<Block> TALL_CRYSTALLIZED_BUSH;
    public static final RegistryObject<Block> CRYSTALLIZED_CACTUS;

    // Portal
    public static final RegistryObject<Block> PORTAL;

    // Legendary Spawners
    public static final RegistryObject<Block> LEGENDARY_SPAWN;
    public static final RegistryObject<Block> TROUGH_BLOCK;
    public static final RegistryObject<Block> HEATRAN_BLOCK;
    public static final RegistryObject<Block> TAO_BLOCK;
    public static final RegistryObject<Block> MAGEARNA_BLOCK;

    public static final RegistryObject<Block> GOLEM_STONE;

    public static final RegistryObject<Block> REGISTEEL_CORE;
    public static final RegistryObject<Block> REGICE_CORE;
    public static final RegistryObject<Block> REGIROCK_CORE;
    public static final RegistryObject<Block> REGIELEKI_CORE;
    public static final RegistryObject<Block> REGIDRAGO_CORE;
    public static final RegistryObject<Block> REGIGIGA_CORE;

    public static final RegistryObject<Block> TIMESPACE_CORE;
    public static final RegistryObject<Block> NATURE_CORE;
    public static final RegistryObject<Block> KELDEO_CORE;
    public static final RegistryObject<Block> VICTINI_CORE;
    public static final RegistryObject<Block> YVELTAL_CORE;
    public static final RegistryObject<Block> XERNEAS_CORE;

    public static final RegistryObject<Block> TAPU_KOKO_CORE;
    public static final RegistryObject<Block> TAPU_FINI_CORE;
    public static final RegistryObject<Block> TAPU_BULU_CORE;
    public static final RegistryObject<Block> TAPU_LELE_CORE;

    // Ores
    public static final RegistryObject<Block> RUBY_ORE;
    public static final RegistryObject<Block> SAPPHIRE_ORE;
    public static final RegistryObject<Block> SPECTRUM_ORE;
    public static final RegistryObject<Block> METEOR_COSMIC_DUST_ORE;
    public static final RegistryObject<Block> ULTRA_COSMIC_DUST_ORE;

    public static final RegistryObject<Block> ULTRA_COAL_ORE;
    public static final RegistryObject<Block> ULTRA_IRON_ORE;
    public static final RegistryObject<Block> ULTRA_GOLD_ORE;
    public static final RegistryObject<Block> ULTRA_DIAMOND_ORE;
    public static final RegistryObject<Block> ULTRA_REDSTONE_ORE;
    public static final RegistryObject<Block> ULTRA_LAPIS_ORE;
    public static final RegistryObject<Block> ULTRA_EMERALD_ORE;
    public static final RegistryObject<Block> ULTRA_COPPER_ORE;

    public static final RegistryObject<Block> FRACTAL_ORE;
    public static final RegistryObject<Block> FRACTAL_BLOCK;

    public static final RegistryObject<Block> RUBY_BLOCK;
    public static final RegistryObject<Block> RUBY_SLAB;
    public static final RegistryObject<Block> RUBY_STAIRS;

    public static final RegistryObject<Block> SAPPHIRE_BLOCK;
    public static final RegistryObject<Block> SAPPHIRE_SLAB;
    public static final RegistryObject<Block> SAPPHIRE_STAIRS;

    public static final RegistryObject<Block> SPECTRUM_BLOCK;
    public static final RegistryObject<Block> SPECTRUM_SLAB;
    public static final RegistryObject<Block> SPECTRUM_STAIRS;

    public static final RegistryObject<Block> COSMIC_DUST_BLOCK;

    public static final RegistryObject<Block> POTTED_AGED_SAPLING;
    public static final RegistryObject<Block> POTTED_CORRUPTED_SAPLING;
    public static final RegistryObject<Block> POTTED_DISTORTIC_SAPLING;
    public static final RegistryObject<Block> POTTED_INVERTED_SAPLING;
    public static final RegistryObject<Block> POTTED_MIRAGE_SAPLING;
    public static final RegistryObject<Block> POTTED_TEMPORAL_SAPLING;

    public static final RegistryObject<Block> POTTED_COMPRECED_MUSHROOM;
    public static final RegistryObject<Block> POTTED_CRYSTALLIZED_BUSH;
    public static final RegistryObject<Block> POTTED_CRYSTALLIZED_CACTUS;
    public static final RegistryObject<Block> POTTED_DISTORCED_MUSHROOM;
    public static final RegistryObject<Block> POTTED_DISTORTIC_VINES;
    public static final RegistryObject<Block> POTTED_GOLDEN_FERN;
    public static final RegistryObject<Block> POTTED_GOLDEN_GRASS;
    public static final RegistryObject<Block> POTTED_GOLDEN_POPPY;
    public static final RegistryObject<Block> POTTED_GOLDEN_SWEET_BERRY_BUSH;
    public static final RegistryObject<Block> POTTED_INVERTED_ORCHID;
    public static final RegistryObject<Block> POTTED_LARGE_GOLDEN_FERN;
    public static final RegistryObject<Block> POTTED_PINK_LILY;
    public static final RegistryObject<Block> POTTED_TALL_CRYSTALLIZED_BUSH;
    public static final RegistryObject<Block> POTTED_TALL_GOLDEN_GRASS;
    public static final RegistryObject<Block> POTTED_TEMPORAL_BAMBOO;

    static
    {
        // Dimensions Creative Tab - Sorting depends on the order the blocks are listed in
        RUBY_ORE = PokecubeLegends.DIMENSIONS_TAB.register("ruby_ore", () -> new OreBlock(BlockBehaviour.Properties.of(
                Material.STONE, MaterialColor.STONE).sound(SoundType.STONE).strength(3.0F, 3.0f)
                .requiresCorrectToolForDrops(), UniformInt.of(3, 7)));
        SAPPHIRE_ORE = PokecubeLegends.DIMENSIONS_TAB.register("sapphire_ore", () -> new OreBlock(
                BlockBehaviour.Properties.of(Material.STONE, MaterialColor.STONE).sound(SoundType.STONE).strength(3.0F,
                        3.0f).requiresCorrectToolForDrops(), UniformInt.of(3, 7)));

        // Soils
        MUSHROOM_GRASS = PokecubeLegends.DIMENSIONS_TAB.register("mushroom_grass_block", () -> new GrassMushroomBlock(
                BlockBehaviour.Properties.of(Material.GRASS, MaterialColor.COLOR_RED).sound(SoundType.GRASS).strength(
                        1f, 2f).randomTicks()));
        MUSHROOM_DIRT = PokecubeLegends.DIMENSIONS_TAB.register("mushroom_dirt", () -> new BlockBase(Material.CLAY,
                MaterialColor.COLOR_PURPLE, 1f, 2f, SoundType.GRAVEL, false));
        JUNGLE_GRASS = PokecubeLegends.DIMENSIONS_TAB.register("jungle_grass_block", () -> new GrassJungleBlock(
                BlockBehaviour.Properties.of(Material.GRASS, MaterialColor.WARPED_NYLIUM).sound(SoundType.GRASS)
                        .strength(1f, 2f).randomTicks()));
        JUNGLE_DIRT = PokecubeLegends.DIMENSIONS_TAB.register("jungle_dirt", () -> new BlockBase(Material.VEGETABLE,
                MaterialColor.COLOR_BROWN, 1f, 2f, SoundType.GRAVEL, false));
        CORRUPTED_GRASS = PokecubeLegends.DIMENSIONS_TAB.register("corrupted_grass_block", () -> new GrassCorruptedBlock(
                BlockBehaviour.Properties.of(Material.GRASS, MaterialColor.TERRACOTTA_BLUE).sound(SoundType.SCAFFOLDING)
                        .strength(4f, 5f).randomTicks().requiresCorrectToolForDrops()));
        CORRUPTED_DIRT = PokecubeLegends.DIMENSIONS_TAB.register("corrupted_dirt", () -> new DirtCorruptedBlock(
                BlockBehaviour.Properties.of(Material.STONE, MaterialColor.TERRACOTTA_PURPLE).sound(SoundType.METAL)
                        .strength(0.9f).requiresCorrectToolForDrops()));
        AGED_GRASS = PokecubeLegends.DIMENSIONS_TAB.register("aged_grass_block", () -> new GrassAgedBlock(
                BlockBehaviour.Properties.of(Material.GRASS, MaterialColor.COLOR_ORANGE).sound(SoundType.GRASS)
                        .strength(1f, 2f).randomTicks()));
        AGED_DIRT = PokecubeLegends.DIMENSIONS_TAB.register("aged_dirt", () -> new BlockBase(Material.DIRT,
                MaterialColor.TERRACOTTA_YELLOW, 1f, 2f, SoundType.WET_GRASS, false));

        // Meteor Blocks
        METEOR_COSMIC_DUST_ORE = PokecubeLegends.DIMENSIONS_TAB.register("cosmic_dust_ore",
                () -> new MeteorCosmicOreBlock(6842513, BlockBehaviour.Properties.of(Material.STONE,
                        MaterialColor.TERRACOTTA_BLUE).sound(SoundType.STONE).strength(3.0F, 3.0f)
                        .requiresCorrectToolForDrops(), UniformInt.of(2, 5)));
        METEOR_BLOCK = PokecubeLegends.DIMENSIONS_TAB.register("meteor_block", () -> new MeteorBlock(6842513,
                BlockBehaviour.Properties.of(Material.VEGETABLE, MaterialColor.TERRACOTTA_BLUE).strength(2.5f).sound(
                        SoundType.METAL).requiresCorrectToolForDrops()));
        METEOR_STAIRS = PokecubeLegends.DIMENSIONS_TAB.register("meteor_stairs", () -> new ItemGenerator.GenericStairs(
                Blocks.STONE_STAIRS.defaultBlockState(), BlockBehaviour.Properties.of(Material.STONE,
                        MaterialColor.TERRACOTTA_BLUE).strength(2.0F, 3.0f).sound(SoundType.STONE)
                        .requiresCorrectToolForDrops()));
        METEOR_SLAB = PokecubeLegends.DIMENSIONS_TAB.register("meteor_slab", () -> new SlabBlock(BlockBehaviour.Properties
                .of(Material.STONE, MaterialColor.TERRACOTTA_BLUE).strength(2.0F, 3.0f).sound(SoundType.STONE)
                .requiresCorrectToolForDrops()));

        // Crystal Blocks
        CRYSTAL = PokecubeLegends.DIMENSIONS_TAB.register("crystal_block", () -> new BlockBase(Material.GLASS,
                MaterialColor.COLOR_LIGHT_BLUE, 1.5f, 3, SoundType.AMETHYST, false));
        CRYSTAL_STAIRS = PokecubeLegends.DIMENSIONS_TAB.register("crystal_stairs", () -> new ItemGenerator.GenericStairs(
                Blocks.STONE_STAIRS.defaultBlockState(), BlockBehaviour.Properties.of(Material.GLASS,
                        MaterialColor.COLOR_LIGHT_BLUE).strength(2.0F, 3.0f).sound(SoundType.AMETHYST)
                        .requiresCorrectToolForDrops()));
        CRYSTAL_SLAB = PokecubeLegends.DIMENSIONS_TAB.register("crystal_slab", () -> new SlabBlock(BlockBehaviour.Properties
                .of(Material.GLASS, MaterialColor.COLOR_LIGHT_BLUE).strength(2.0F, 3.0f).sound(SoundType.AMETHYST)
                .requiresCorrectToolForDrops()));
        CRYSTAL_BUTTON = PokecubeLegends.DIMENSIONS_TAB.register("crystal_button",
                () -> new ItemGenerator.GenericWoodButton(BlockBehaviour.Properties.of(Material.GLASS,
                        MaterialColor.COLOR_LIGHT_BLUE).sound(SoundType.AMETHYST).noCollission().strength(0.5F)
                        .requiresCorrectToolForDrops()));

        // Ultra Stone Blocks
        ULTRA_COAL_ORE = PokecubeLegends.DIMENSIONS_TAB.register("ultra_coal_ore", () -> new OreBlock(
                BlockBehaviour.Properties.of(Material.STONE, MaterialColor.TERRACOTTA_CYAN).sound(SoundType.STONE)
                        .strength(3.0F, 3.0f).requiresCorrectToolForDrops(), UniformInt.of(0, 2)));
        ULTRA_IRON_ORE = PokecubeLegends.DIMENSIONS_TAB.register("ultra_iron_ore", () -> new OreBlock(
                BlockBehaviour.Properties.of(Material.STONE, MaterialColor.TERRACOTTA_CYAN).sound(SoundType.STONE)
                        .strength(3.0F, 3.0f).requiresCorrectToolForDrops()));
        ULTRA_COPPER_ORE = PokecubeLegends.DIMENSIONS_TAB.register("ultra_copper_ore", () -> new OreBlock(
                BlockBehaviour.Properties.of(Material.STONE, MaterialColor.TERRACOTTA_CYAN).sound(SoundType.STONE)
                        .strength(3.0F, 3.0f).requiresCorrectToolForDrops()));
        ULTRA_GOLD_ORE = PokecubeLegends.DIMENSIONS_TAB.register("ultra_gold_ore", () -> new OreBlock(
                BlockBehaviour.Properties.of(Material.STONE, MaterialColor.TERRACOTTA_CYAN).sound(SoundType.STONE)
                        .strength(3.0F, 3.0f).requiresCorrectToolForDrops()));
        ULTRA_REDSTONE_ORE = PokecubeLegends.DIMENSIONS_TAB.register("ultra_redstone_ore", () -> new RedStoneOreBlock(
                BlockBehaviour.Properties.of(Material.STONE, MaterialColor.TERRACOTTA_CYAN).sound(SoundType.STONE)
                        .strength(3.0F, 3.0f).requiresCorrectToolForDrops().randomTicks().lightLevel(BlockInit
                                .litBlockEmission(9))));
        ULTRA_LAPIS_ORE = PokecubeLegends.DIMENSIONS_TAB.register("ultra_lazuli_ore", () -> new OreBlock(
                BlockBehaviour.Properties.of(Material.STONE, MaterialColor.TERRACOTTA_CYAN).sound(SoundType.STONE)
                        .strength(3.0F, 3.0f).requiresCorrectToolForDrops(), UniformInt.of(2, 5)));
        ULTRA_EMERALD_ORE = PokecubeLegends.DIMENSIONS_TAB.register("ultra_emerald_ore", () -> new OreBlock(
                BlockBehaviour.Properties.of(Material.STONE, MaterialColor.TERRACOTTA_CYAN).sound(SoundType.STONE)
                        .strength(3.0F, 3.0f).requiresCorrectToolForDrops(), UniformInt.of(3, 7)));
        ULTRA_DIAMOND_ORE = PokecubeLegends.DIMENSIONS_TAB.register("ultra_diamond_ore", () -> new OreBlock(
                BlockBehaviour.Properties.of(Material.STONE, MaterialColor.TERRACOTTA_CYAN).sound(SoundType.STONE)
                        .strength(3.0F, 3.0f).requiresCorrectToolForDrops(), UniformInt.of(3, 7)));
        ULTRA_COSMIC_DUST_ORE = PokecubeLegends.DIMENSIONS_TAB.register("ultra_cosmic_dust_ore", () -> new OreBlock(
                BlockBehaviour.Properties.of(Material.STONE, MaterialColor.TERRACOTTA_CYAN).sound(SoundType.STONE)
                        .strength(3.0F, 3.0f).requiresCorrectToolForDrops(), UniformInt.of(2, 5)));
        SPECTRUM_ORE = PokecubeLegends.DIMENSIONS_TAB.register("spectrum_ore", () -> new OreBlock(BlockBehaviour.Properties
                .of(Material.STONE, MaterialColor.TERRACOTTA_CYAN).sound(SoundType.STONE).strength(3.0F, 3.0f)
                .requiresCorrectToolForDrops(), UniformInt.of(3, 7)));

        ULTRA_STONE = PokecubeLegends.DIMENSIONS_TAB.register("ultra_stone", () -> new BlockBase(Material.STONE,
                MaterialColor.TERRACOTTA_CYAN, 1.5f, 6.0f, SoundType.STONE, true));
        ULTRA_STONE_STAIRS = PokecubeLegends.DIMENSIONS_TAB.register("ultra_stone_stairs",
                () -> new ItemGenerator.GenericStairs(Blocks.STONE_STAIRS.defaultBlockState(), BlockBehaviour.Properties
                        .of(Material.STONE, MaterialColor.COLOR_LIGHT_GRAY).strength(2.0F, 3.0f).sound(SoundType.STONE)
                        .requiresCorrectToolForDrops()));
        ULTRA_STONE_SLAB = PokecubeLegends.DIMENSIONS_TAB.register("ultra_stone_slab", () -> new SlabBlock(
                BlockBehaviour.Properties.of(Material.STONE, MaterialColor.COLOR_LIGHT_GRAY).strength(2.0F, 3.0f).sound(
                        SoundType.STONE).requiresCorrectToolForDrops()));
        ULTRA_STONE_BUTTON = PokecubeLegends.DIMENSIONS_TAB.register("ultra_stone_button",
                () -> new ItemGenerator.GenericWoodButton(BlockBehaviour.Properties.of(Material.STONE,
                        MaterialColor.COLOR_BLUE).sound(SoundType.BAMBOO).noCollission().strength(0.5F)
                        .requiresCorrectToolForDrops()));
        ULTRA_STONE_PR_PLATE = PokecubeLegends.DIMENSIONS_TAB.register("ultra_stone_pressure_plate",
                () -> new ItemGenerator.GenericPressurePlate(PressurePlateBlock.Sensitivity.EVERYTHING,
                        BlockBehaviour.Properties.of(Material.STONE, MaterialColor.COLOR_BLUE).sound(SoundType.BAMBOO)
                                .noCollission().strength(0.7F).requiresCorrectToolForDrops()));

        ULTRA_COBBLESTONE = PokecubeLegends.DIMENSIONS_TAB.register("ultra_cobblestone", () -> new BlockBase(Material.STONE,
                MaterialColor.TERRACOTTA_CYAN, 1.5f, 10f, SoundType.STONE, true));
        ULTRA_COBBLESTONE_STAIRS = PokecubeLegends.DIMENSIONS_TAB.register("ultra_cobblestone_stairs",
                () -> new ItemGenerator.GenericStairs(Blocks.STONE_STAIRS.defaultBlockState(), BlockBehaviour.Properties
                        .of(Material.STONE, MaterialColor.COLOR_LIGHT_GRAY).strength(2.0F, 3.0f).sound(SoundType.STONE)
                        .requiresCorrectToolForDrops()));
        ULTRA_COBBLESTONE_SLAB = PokecubeLegends.DIMENSIONS_TAB.register("ultra_cobblestone_slab", () -> new SlabBlock(
                BlockBehaviour.Properties.of(Material.STONE, MaterialColor.COLOR_LIGHT_GRAY).strength(2.0F, 3.0f).sound(
                        SoundType.STONE).requiresCorrectToolForDrops()));

        // Darkstone Blocks
        ULTRA_DARKSTONE = PokecubeLegends.DIMENSIONS_TAB.register("ultra_darkstone", () -> new EffectBlockBase(Material.STONE,
                MaterialColor.COLOR_BLACK, 5f, 8f, SoundType.GILDED_BLACKSTONE, true, MobEffects.BLINDNESS));
        ULTRA_DARKSTONE_STAIRS = PokecubeLegends.DIMENSIONS_TAB.register("ultra_darkstone_stairs",
                () -> new ItemGenerator.GenericStairs(Blocks.STONE_STAIRS.defaultBlockState(), BlockBehaviour.Properties
                        .of(Material.STONE, MaterialColor.COLOR_BLACK).strength(2.0F, 3.0f).sound(
                                SoundType.GILDED_BLACKSTONE).requiresCorrectToolForDrops()));
        ULTRA_DARKSTONE_SLAB = PokecubeLegends.DIMENSIONS_TAB.register("ultra_darkstone_slab", () -> new SlabBlock(
                BlockBehaviour.Properties.of(Material.STONE, MaterialColor.COLOR_BLACK).strength(2.0F, 3.0f).sound(
                        SoundType.GILDED_BLACKSTONE).requiresCorrectToolForDrops()));
        ULTRA_DARKSTONE_BUTTON = PokecubeLegends.DIMENSIONS_TAB.register("ultra_darkstone_button",
                () -> new ItemGenerator.GenericWoodButton(BlockBehaviour.Properties.of(Material.STONE,
                        MaterialColor.COLOR_BLACK).sound(SoundType.NETHER_BRICKS).noCollission().strength(0.5F)
                        .requiresCorrectToolForDrops()));
        ULTRA_DARKSTONE_PR_PLATE = PokecubeLegends.DIMENSIONS_TAB.register("ultra_darkstone_pressure_plate",
                () -> new ItemGenerator.GenericPressurePlate(PressurePlateBlock.Sensitivity.MOBS,
                        BlockBehaviour.Properties.of(Material.STONE, MaterialColor.COLOR_BLACK).sound(
                                SoundType.NETHER_BRICKS).noCollission().strength(0.7F).requiresCorrectToolForDrops()));

        ULTRA_DARK_COBBLESTONE = PokecubeLegends.DIMENSIONS_TAB.register("ultra_dark_cobblestone", () -> new BlockBase(Material.STONE,
                MaterialColor.COLOR_BLACK, 0.8f, 10f, SoundType.STONE, true));
        ULTRA_DARK_COBBLESTONE_STAIRS = PokecubeLegends.DIMENSIONS_TAB.register("ultra_dark_cobblestone_stairs",
                () -> new ItemGenerator.GenericStairs(Blocks.STONE_STAIRS.defaultBlockState(), BlockBehaviour.Properties
                        .of(Material.STONE, MaterialColor.COLOR_BLACK).strength(2.0F, 3.0f).sound(
                                SoundType.GILDED_BLACKSTONE).requiresCorrectToolForDrops()));
        ULTRA_DARK_COBBLESTONE_SLAB = PokecubeLegends.DIMENSIONS_TAB.register("ultra_dark_cobblestone_slab",
                () -> new SlabBlock(BlockBehaviour.Properties.of(Material.STONE, MaterialColor.COLOR_BLACK).strength(
                        2.0F, 3.0f).sound(SoundType.GILDED_BLACKSTONE).requiresCorrectToolForDrops()));

        // Ultra Desert
        CRYSTALLIZED_SAND = PokecubeLegends.DIMENSIONS_TAB.register("crystallized_sand", () -> new EffectBlockBaseSand(14737366,
                BlockBehaviour.Properties.of(Material.SAND, MaterialColor.WOOL).sound(SoundType.SAND)
                .strength(0.6f), MobEffects.LEVITATION));
        CRYSTALLIZED_SANDSTONE = PokecubeLegends.DIMENSIONS_TAB.register("crystallized_sandstone", () -> new BlockBase(
                Material.STONE, MaterialColor.SNOW, 1f, 10f, SoundType.STONE, true));
        CRYS_SANDSTONE_STAIRS = PokecubeLegends.DIMENSIONS_TAB.register("crystallized_sandstone_stairs",
                () -> new ItemGenerator.GenericStairs(Blocks.STONE_STAIRS.defaultBlockState(), BlockBehaviour.Properties
                        .of(Material.STONE, MaterialColor.SNOW).strength(2.0F, 3.0f).sound(SoundType.STONE)
                        .requiresCorrectToolForDrops()));
        CRYS_SANDSTONE_SLAB = PokecubeLegends.DIMENSIONS_TAB.register("crystallized_sandstone_slab", () -> new SlabBlock(
                BlockBehaviour.Properties.of(Material.STONE, MaterialColor.SNOW).strength(2.0F, 3.0f).sound(
                        SoundType.STONE).requiresCorrectToolForDrops()));
        CRYS_SANDSTONE_BUTTON = PokecubeLegends.DIMENSIONS_TAB.register("crystallized_sandstone_button",
                () -> new ItemGenerator.GenericWoodButton(BlockBehaviour.Properties.of(Material.STONE,
                        MaterialColor.SAND).sound(SoundType.STONE).noCollission().strength(0.5F)
                        .requiresCorrectToolForDrops()));
        CRYS_SANDSTONE_PR_PLATE = PokecubeLegends.DIMENSIONS_TAB.register("crystallized_sandstone_pressure_plate",
                () -> new ItemGenerator.GenericPressurePlate(PressurePlateBlock.Sensitivity.EVERYTHING,
                        BlockBehaviour.Properties.of(Material.STONE, MaterialColor.SNOW).sound(SoundType.STONE)
                                .noCollission().strength(0.7F).requiresCorrectToolForDrops()));

        CRYS_SANDSTONE_SMOOTH = PokecubeLegends.DIMENSIONS_TAB.register("crystallized_sandstone_smooth", () -> new BlockBase(
                Material.STONE, MaterialColor.SNOW, 1.5f, 10f, SoundType.STONE, true));
        CRYS_SANDSTONE_SMOOTH_STAIRS = PokecubeLegends.DIMENSIONS_TAB.register("crystallized_sandstone_smooth_stairs",
                () -> new ItemGenerator.GenericStairs(Blocks.STONE_STAIRS.defaultBlockState(), BlockBehaviour.Properties
                        .of(Material.STONE, MaterialColor.SNOW).strength(2.0F, 3.0f).sound(SoundType.STONE)
                        .requiresCorrectToolForDrops()));
        CRYS_SANDSTONE_SMOOTH_SLAB = PokecubeLegends.DIMENSIONS_TAB.register("crystallized_sandstone_smooth_slab",
                () -> new SlabBlock(BlockBehaviour.Properties.of(Material.STONE, MaterialColor.SNOW).strength(2.0F,
                        3.0f).sound(SoundType.STONE).requiresCorrectToolForDrops()));

        // Distortic World
        DISTORTIC_GRASS = PokecubeLegends.DIMENSIONS_TAB.register("distortic_grass_block", () -> new GrassDistorticBlock(
                BlockBehaviour.Properties.of(Material.GRASS, MaterialColor.TERRACOTTA_PINK).sound(SoundType.NYLIUM)
                        .strength(1, 2).requiresCorrectToolForDrops().randomTicks()));
        CRACKED_DISTORTIC_STONE = PokecubeLegends.DIMENSIONS_TAB.register("cracked_distortic_stone",
                () -> new DistorticCrackedStone(BlockBehaviour.Properties.of(Material.STONE,
                        MaterialColor.TERRACOTTA_BLACK).sound(SoundType.STONE).strength(1, 2)
                        .requiresCorrectToolForDrops()));

        FRACTAL_ORE = PokecubeLegends.DIMENSIONS_TAB.register("fractal_ore", () -> new OreBlock(BlockBehaviour.Properties
                .of(Material.STONE, MaterialColor.TERRACOTTA_BLACK).sound(SoundType.STONE).strength(3.0F, 3.0f)
                .requiresCorrectToolForDrops(), UniformInt.of(2, 7)));
        DISTORTIC_STONE = PokecubeLegends.DIMENSIONS_TAB.register("distortic_stone", () -> new DistorticStoneBlock(
                BlockBehaviour.Properties.of(Material.STONE, MaterialColor.TERRACOTTA_BLACK).sound(SoundType.STONE)
                        .strength(1.5f).requiresCorrectToolForDrops()));
        DISTORTIC_STONE_STAIRS = PokecubeLegends.DIMENSIONS_TAB.register("distortic_stone_stairs",
                () -> new ItemGenerator.GenericStairs(Blocks.STONE_STAIRS.defaultBlockState(), BlockBehaviour.Properties
                        .of(Material.STONE, MaterialColor.TERRACOTTA_BLACK).strength(2.0F, 3.0f).sound(SoundType.STONE)
                        .requiresCorrectToolForDrops()));
        DISTORTIC_STONE_SLAB = PokecubeLegends.DIMENSIONS_TAB.register("distortic_stone_slab", () -> new SlabBlock(
                BlockBehaviour.Properties.of(Material.STONE, MaterialColor.TERRACOTTA_BLACK).strength(2.0F, 3.0f).sound(
                        SoundType.STONE).requiresCorrectToolForDrops()));

        DISTORTIC_MIRROR = PokecubeLegends.DIMENSIONS_TAB.register("distortic_mirror", () -> new BlockBase(Material.GLASS,
                MaterialColor.SNOW, 0.3f, 0.3f, SoundType.GLASS, true));

        DISTORTIC_GLOWSTONE = PokecubeLegends.DIMENSIONS_TAB.register("distortic_glowstone", () -> new BlockBase(
                Material.STONE, MaterialColor.COLOR_ORANGE, 1.5f, 1.5f, SoundType.GLASS, true));

        // Woods
        // Aged Blocks
        AGED_SAPLING = PokecubeLegends.DIMENSIONS_TAB.register("aged_sapling", () -> new SaplingBase(
                () -> new AgedTree(), BlockBehaviour.Properties.of(Material.PLANT, MaterialColor.COLOR_YELLOW).strength(
                        0f, 1f).sound(SoundType.GRASS).noCollission().noOcclusion()));

        AGED_LEAVES = PokecubeLegends.DIMENSIONS_TAB.register("aged_leaves", () -> new LeavesBlock(
                BlockBehaviour.Properties.of(Material.LEAVES, MaterialColor.COLOR_ORANGE).strength(0.2f).sound(
                        SoundType.GRASS).noOcclusion().isSuffocating((s, r, p) -> false).isValidSpawn(
                                ItemGenerator::ocelotOrParrot).isViewBlocking((s, r, p) -> false)));

        AGED_LOG = PokecubeLegends.DIMENSIONS_TAB.register("aged_log", () -> Blocks.log(MaterialColor.COLOR_BROWN,
                MaterialColor.COLOR_BROWN));
        AGED_WOOD = PokecubeLegends.DIMENSIONS_TAB.register("aged_wood", () -> Blocks.log(MaterialColor.COLOR_BROWN,
                MaterialColor.COLOR_BROWN));
        STRIP_AGED_LOG = PokecubeLegends.DIMENSIONS_TAB.register("stripped_aged_log", () -> Blocks.log(
                MaterialColor.COLOR_BROWN, MaterialColor.COLOR_BROWN));
        STRIP_AGED_WOOD = PokecubeLegends.DIMENSIONS_TAB.register("stripped_aged_wood", () -> Blocks.log(
                MaterialColor.COLOR_BROWN, MaterialColor.COLOR_BROWN));

        AGED_BARREL = PokecubeLegends.DIMENSIONS_TAB.register("aged_barrel", () -> new GenericBarrel(
                BlockBehaviour.Properties.of(Material.WOOD, MaterialColor.COLOR_BROWN).strength(2.5F).sound(
                        SoundType.WOOD)));
        AGED_BOOKSHELF = PokecubeLegends.DIMENSIONS_TAB.register("aged_bookshelf", () -> new GenericBookshelf(
                BlockBehaviour.Properties.of(Material.WOOD, MaterialColor.COLOR_BROWN).strength(2f, 4f).sound(
                        SoundType.WOOD)));
        AGED_BOOKSHELF_EMPTY = PokecubeLegends.DIMENSIONS_TAB.register("aged_bookshelf_empty",
                () -> new GenericBookshelfEmpty(BlockBehaviour.Properties.of(Material.WOOD, MaterialColor.COLOR_BROWN)
                        .strength(2f, 4f).sound(SoundType.WOOD).dynamicShape()));

        AGED_PLANKS = PokecubeLegends.DIMENSIONS_TAB.register("aged_planks", () -> new Block(BlockBehaviour.Properties.of(
                Material.WOOD, MaterialColor.COLOR_BROWN).strength(2.0f).sound(SoundType.WOOD)));
        AGED_STAIRS = PokecubeLegends.DIMENSIONS_TAB.register("aged_stairs", () -> new ItemGenerator.GenericStairs(
                Blocks.OAK_STAIRS.defaultBlockState(), BlockBehaviour.Properties.of(Material.WOOD,
                        MaterialColor.COLOR_BROWN).strength(2.0f).sound(SoundType.WOOD)));
        AGED_SLAB = PokecubeLegends.DIMENSIONS_TAB.register("aged_slab", () -> new SlabBlock(BlockBehaviour.Properties.of(
                Material.WOOD, MaterialColor.COLOR_BROWN).strength(2.0f).sound(SoundType.WOOD)));
        AGED_FENCE = PokecubeLegends.DIMENSIONS_TAB.register("aged_fence", () -> new FenceBlock(BlockBehaviour.Properties
                .of(Material.WOOD, MaterialColor.COLOR_BROWN).strength(2.0f).sound(SoundType.WOOD)));
        AGED_FENCE_GATE = PokecubeLegends.DIMENSIONS_TAB.register("aged_fence_gate", () -> new FenceGateBlock(
                BlockBehaviour.Properties.of(Material.WOOD, MaterialColor.COLOR_BROWN).strength(2.0f).sound(
                        SoundType.WOOD)));
        AGED_BUTTON = PokecubeLegends.DIMENSIONS_TAB.register("aged_button", () -> new ItemGenerator.GenericWoodButton(
                BlockBehaviour.Properties.of(Material.WOOD, MaterialColor.COLOR_BROWN).sound(SoundType.WOOD)
                        .noCollission().strength(0.5f)));
        AGED_PR_PLATE = PokecubeLegends.DIMENSIONS_TAB.register("aged_pressure_plate",
                () -> new ItemGenerator.GenericPressurePlate(PressurePlateBlock.Sensitivity.EVERYTHING,
                        BlockBehaviour.Properties.of(Material.WOOD, MaterialColor.COLOR_BROWN).sound(SoundType.WOOD)
                                .noCollission().strength(0.5f)));

        AGED_TRAPDOOR = PokecubeLegends.DIMENSIONS_TAB.register("aged_trapdoor", () -> new ItemGenerator.GenericTrapDoor(
                BlockBehaviour.Properties.of(Material.WOOD, MaterialColor.COLOR_BROWN).sound(SoundType.WOOD).strength(
                        2.0f, 3.0f).noOcclusion()));
        AGED_DOOR = PokecubeLegends.DIMENSIONS_TAB.register("aged_door", () -> new ItemGenerator.GenericDoor(
                BlockBehaviour.Properties.of(Material.WOOD, MaterialColor.COLOR_BROWN).sound(SoundType.WOOD).strength(
                        2.0f, 3.0f).noOcclusion()));

        // Corrupted Blocks
        CORRUPTED_SAPLING = PokecubeLegends.DIMENSIONS_TAB.register("corrupted_sapling", () -> new SaplingBase(
                () -> new CorruptedTree(), BlockBehaviour.Properties.of(Material.PLANT, MaterialColor.COLOR_BLACK)
                        .strength(0f, 1f).sound(SoundType.GRASS).noCollission().noOcclusion()));

        CORRUPTED_LEAVES = PokecubeLegends.DIMENSIONS_TAB.register("corrupted_leaves", () -> new CorruptedLeavesBlock());

        CORRUPTED_LOG = PokecubeLegends.DIMENSIONS_TAB.register("corrupted_log", () -> Blocks.log(MaterialColor.WOOD,
                MaterialColor.COLOR_GRAY));
        CORRUPTED_WOOD = PokecubeLegends.DIMENSIONS_TAB.register("corrupted_wood", () -> Blocks.log(
                MaterialColor.COLOR_GRAY, MaterialColor.COLOR_GRAY));
        STRIP_CORRUPTED_LOG = PokecubeLegends.DIMENSIONS_TAB.register("stripped_corrupted_log", () -> Blocks.log(
                MaterialColor.WOOD, MaterialColor.WOOD));
        STRIP_CORRUPTED_WOOD = PokecubeLegends.DIMENSIONS_TAB.register("stripped_corrupted_wood", () -> Blocks.log(
                MaterialColor.WOOD, MaterialColor.WOOD));

        CORRUPTED_BARREL = PokecubeLegends.DIMENSIONS_TAB.register("corrupted_barrel", () -> new GenericBarrel(
                BlockBehaviour.Properties.of(Material.WOOD, MaterialColor.WOOD).strength(2.5F).sound(SoundType.WOOD)));
        CORRUPTED_BOOKSHELF = PokecubeLegends.DIMENSIONS_TAB.register("corrupted_bookshelf", () -> new GenericBookshelf(
                BlockBehaviour.Properties.of(Material.WOOD, MaterialColor.WOOD).strength(2f, 4f).sound(
                        SoundType.WOOD)));
        CORRUPTED_BOOKSHELF_EMPTY = PokecubeLegends.DIMENSIONS_TAB.register("corrupted_bookshelf_empty",
                () -> new GenericBookshelfEmpty(BlockBehaviour.Properties.of(Material.WOOD, MaterialColor.WOOD)
                        .strength(2f, 4f).sound(SoundType.WOOD).dynamicShape()));

        CORRUPTED_PLANKS = PokecubeLegends.DIMENSIONS_TAB.register("corrupted_planks", () -> new Block(
                BlockBehaviour.Properties.of(Material.WOOD, MaterialColor.WOOD).strength(2.0f).sound(SoundType.WOOD)));
        CORRUPTED_STAIRS = PokecubeLegends.DIMENSIONS_TAB.register("corrupted_stairs",
                () -> new ItemGenerator.GenericStairs(Blocks.OAK_STAIRS.defaultBlockState(), BlockBehaviour.Properties
                        .of(Material.WOOD, MaterialColor.WOOD).strength(2.0f).sound(SoundType.WOOD)));
        CORRUPTED_SLAB = PokecubeLegends.DIMENSIONS_TAB.register("corrupted_slab", () -> new SlabBlock(
                BlockBehaviour.Properties.of(Material.WOOD, MaterialColor.WOOD).strength(2.0F).sound(SoundType.WOOD)));
        CORRUPTED_FENCE = PokecubeLegends.DIMENSIONS_TAB.register("corrupted_fence", () -> new FenceBlock(
                BlockBehaviour.Properties.of(Material.WOOD, MaterialColor.WOOD).strength(2.0f).sound(SoundType.WOOD)));
        CORRUPTED_FENCE_GATE = PokecubeLegends.DIMENSIONS_TAB.register("corrupted_fence_gate", () -> new FenceGateBlock(
                BlockBehaviour.Properties.of(Material.WOOD, MaterialColor.WOOD).strength(2.0f).sound(SoundType.WOOD)));
        CORRUPTED_BUTTON = PokecubeLegends.DIMENSIONS_TAB.register("corrupted_button",
                () -> new ItemGenerator.GenericWoodButton(BlockBehaviour.Properties.of(Material.WOOD,
                        MaterialColor.WOOD).sound(SoundType.WOOD).noCollission().strength(0.5f)));
        CORRUPTED_PR_PLATE = PokecubeLegends.DIMENSIONS_TAB.register("corrupted_pressure_plate",
                () -> new ItemGenerator.GenericPressurePlate(PressurePlateBlock.Sensitivity.MOBS,
                        BlockBehaviour.Properties.of(Material.WOOD, MaterialColor.WOOD).sound(SoundType.WOOD)
                                .noCollission().strength(0.5f)));

        CORRUPTED_TRAPDOOR = PokecubeLegends.DIMENSIONS_TAB.register("corrupted_trapdoor",
                () -> new ItemGenerator.GenericTrapDoor(BlockBehaviour.Properties.of(Material.WOOD, MaterialColor.WOOD)
                        .sound(SoundType.WOOD).strength(2.0f, 3.0f).noOcclusion()));
        CORRUPTED_DOOR = PokecubeLegends.DIMENSIONS_TAB.register("corrupted_door", () -> new ItemGenerator.GenericDoor(
                BlockBehaviour.Properties.of(Material.WOOD, MaterialColor.WOOD).sound(SoundType.WOOD).strength(2.0f,
                        3.0f).noOcclusion()));

        // Distorted Blocks
        DISTORTIC_SAPLING = PokecubeLegends.DIMENSIONS_TAB.register("distortic_sapling", () -> new SaplingBase(
                () -> new DistorticTree(), BlockBehaviour.Properties.of(Material.PLANT, MaterialColor.COLOR_PURPLE)
                        .strength(0f, 1f).sound(SoundType.GRASS).noCollission().noOcclusion()));

        DISTORTIC_LEAVES = PokecubeLegends.DIMENSIONS_TAB.register("distortic_leaves", () -> new LeavesBlock(
                BlockBehaviour.Properties.of(Material.LEAVES, MaterialColor.COLOR_PURPLE).strength(0.2f).sound(
                        SoundType.GRASS).noOcclusion().isSuffocating((s, r, p) -> false).isValidSpawn(
                                ItemGenerator::ocelotOrParrot).isViewBlocking((s, r, p) -> false)));

        DISTORTIC_LOG = PokecubeLegends.DIMENSIONS_TAB.register("distortic_log", () -> Blocks.log(MaterialColor.COLOR_BLUE,
                MaterialColor.COLOR_BLUE));
        DISTORTIC_WOOD = PokecubeLegends.DIMENSIONS_TAB.register("distortic_wood", () -> Blocks.log(
                MaterialColor.COLOR_BLUE, MaterialColor.COLOR_BLUE));
        STRIP_DISTORTIC_LOG = PokecubeLegends.DIMENSIONS_TAB.register("stripped_distortic_log", () -> Blocks.log(
                MaterialColor.COLOR_BLUE, MaterialColor.COLOR_BLUE));
        STRIP_DISTORTIC_WOOD = PokecubeLegends.DIMENSIONS_TAB.register("stripped_distortic_wood", () -> Blocks.log(
                MaterialColor.COLOR_BLUE, MaterialColor.COLOR_BLUE));

        DISTORTIC_BARREL = PokecubeLegends.DIMENSIONS_TAB.register("distortic_barrel", () -> new GenericBarrel(
                BlockBehaviour.Properties.of(Material.WOOD, MaterialColor.COLOR_BLUE).strength(2.5F).sound(
                        SoundType.WOOD)));
        DISTORTIC_BOOKSHELF = PokecubeLegends.DIMENSIONS_TAB.register("distortic_bookshelf", () -> new GenericBookshelf(
                BlockBehaviour.Properties.of(Material.WOOD, MaterialColor.COLOR_BLUE).strength(2f, 4f).sound(
                        SoundType.WOOD)));
        DISTORTIC_BOOKSHELF_EMPTY = PokecubeLegends.DIMENSIONS_TAB.register("distortic_bookshelf_empty",
                () -> new GenericBookshelfEmpty(BlockBehaviour.Properties.of(Material.WOOD, MaterialColor.COLOR_BLUE)
                        .strength(2f, 4f).sound(SoundType.WOOD).dynamicShape()));

        DISTORTIC_PLANKS = PokecubeLegends.DIMENSIONS_TAB.register("distortic_planks", () -> new Block(
                BlockBehaviour.Properties.of(Material.WOOD, MaterialColor.COLOR_BLUE).strength(2.0f).sound(
                        SoundType.WOOD)));
        DISTORTIC_STAIRS = PokecubeLegends.DIMENSIONS_TAB.register("distortic_stairs",
                () -> new ItemGenerator.GenericStairs(Blocks.OAK_STAIRS.defaultBlockState(), BlockBehaviour.Properties
                        .of(Material.WOOD, MaterialColor.COLOR_BLUE).strength(2.0f).sound(SoundType.WOOD)));
        DISTORTIC_SLAB = PokecubeLegends.DIMENSIONS_TAB.register("distortic_slab", () -> new SlabBlock(
                BlockBehaviour.Properties.of(Material.WOOD, MaterialColor.COLOR_BLUE).strength(2.0f).sound(
                        SoundType.WOOD)));
        DISTORTIC_FENCE = PokecubeLegends.DIMENSIONS_TAB.register("distortic_fence", () -> new FenceBlock(
                BlockBehaviour.Properties.of(Material.WOOD, MaterialColor.COLOR_BLUE).strength(2.0f).sound(
                        SoundType.WOOD)));
        DISTORTIC_FENCE_GATE = PokecubeLegends.DIMENSIONS_TAB.register("distortic_fence_gate", () -> new FenceGateBlock(
                BlockBehaviour.Properties.of(Material.WOOD, MaterialColor.COLOR_BLUE).strength(2.0f).sound(
                        SoundType.WOOD)));
        DISTORTIC_BUTTON = PokecubeLegends.DIMENSIONS_TAB.register("distortic_button",
                () -> new ItemGenerator.GenericWoodButton(BlockBehaviour.Properties.of(Material.WOOD,
                        MaterialColor.COLOR_BLUE).sound(SoundType.WOOD).noCollission().strength(0.5f)));
        DISTORTIC_PR_PLATE = PokecubeLegends.DIMENSIONS_TAB.register("distortic_pressure_plate",
                () -> new ItemGenerator.GenericPressurePlate(PressurePlateBlock.Sensitivity.EVERYTHING,
                        BlockBehaviour.Properties.of(Material.WOOD, MaterialColor.COLOR_BLUE).sound(SoundType.WOOD)
                                .noCollission().strength(0.5f)));

        DISTORTIC_TRAPDOOR = PokecubeLegends.DIMENSIONS_TAB.register("distortic_trapdoor",
                () -> new ItemGenerator.GenericTrapDoor(BlockBehaviour.Properties.of(Material.WOOD,
                        MaterialColor.COLOR_BLUE).sound(SoundType.WOOD).strength(2.0f, 3.0f).noOcclusion()));
        DISTORTIC_DOOR = PokecubeLegends.DIMENSIONS_TAB.register("distortic_door", () -> new ItemGenerator.GenericDoor(
                BlockBehaviour.Properties.of(Material.WOOD, MaterialColor.COLOR_BLUE).sound(SoundType.WOOD).strength(
                        2.0f, 3.0f).noOcclusion()));

        // Inverted Blocks
        INVERTED_SAPLING = PokecubeLegends.DIMENSIONS_TAB.register("inverted_sapling", () -> new SaplingBase(
                () -> new InvertedTree(), BlockBehaviour.Properties.of(Material.PLANT, MaterialColor.COLOR_BLUE)
                        .strength(0f, 1f).sound(SoundType.GRASS).noCollission().noOcclusion()));

        INVERTED_LEAVES = PokecubeLegends.DIMENSIONS_TAB.register("inverted_leaves", () -> new LeavesBlock(
                BlockBehaviour.Properties.of(Material.LEAVES, MaterialColor.COLOR_LIGHT_BLUE).strength(0.2f).sound(
                        SoundType.GRASS).noOcclusion().isSuffocating((s, r, p) -> false).isValidSpawn(
                                ItemGenerator::ocelotOrParrot).isViewBlocking((s, r, p) -> false)));

        INVERTED_LOG = PokecubeLegends.DIMENSIONS_TAB.register("inverted_log", () -> Blocks.log(
                MaterialColor.TERRACOTTA_LIGHT_BLUE, MaterialColor.TERRACOTTA_LIGHT_BLUE));
        INVERTED_WOOD = PokecubeLegends.DIMENSIONS_TAB.register("inverted_wood", () -> Blocks.log(
                MaterialColor.TERRACOTTA_LIGHT_BLUE, MaterialColor.TERRACOTTA_LIGHT_BLUE));
        STRIP_INVERTED_LOG = PokecubeLegends.DIMENSIONS_TAB.register("stripped_inverted_log", () -> Blocks.log(
                MaterialColor.TERRACOTTA_LIGHT_BLUE, MaterialColor.TERRACOTTA_LIGHT_BLUE));
        STRIP_INVERTED_WOOD = PokecubeLegends.DIMENSIONS_TAB.register("stripped_inverted_wood", () -> Blocks.log(
                MaterialColor.TERRACOTTA_LIGHT_BLUE, MaterialColor.TERRACOTTA_LIGHT_BLUE));

        INVERTED_BARREL = PokecubeLegends.DIMENSIONS_TAB.register("inverted_barrel", () -> new GenericBarrel(
                BlockBehaviour.Properties.of(Material.WOOD, MaterialColor.TERRACOTTA_LIGHT_BLUE).strength(2.5F).sound(
                        SoundType.WOOD)));
        INVERTED_BOOKSHELF = PokecubeLegends.DIMENSIONS_TAB.register("inverted_bookshelf", () -> new GenericBookshelf(
                BlockBehaviour.Properties.of(Material.WOOD, MaterialColor.TERRACOTTA_LIGHT_BLUE).strength(2f, 4f).sound(
                        SoundType.WOOD)));
        INVERTED_BOOKSHELF_EMPTY = PokecubeLegends.DIMENSIONS_TAB.register("inverted_bookshelf_empty",
                () -> new GenericBookshelfEmpty(BlockBehaviour.Properties.of(Material.WOOD,
                        MaterialColor.TERRACOTTA_LIGHT_BLUE).strength(2f, 4f).sound(SoundType.WOOD).dynamicShape()));

        INVERTED_PLANKS = PokecubeLegends.DIMENSIONS_TAB.register("inverted_planks", () -> new Block(BlockBehaviour.Properties
                .of(Material.WOOD, MaterialColor.TERRACOTTA_LIGHT_BLUE).strength(2.0f, 3.0f).sound(SoundType.WOOD)));
        INVERTED_STAIRS = PokecubeLegends.DIMENSIONS_TAB.register("inverted_stairs", () -> new ItemGenerator.GenericStairs(
                Blocks.OAK_STAIRS.defaultBlockState(), BlockBehaviour.Properties.of(Material.WOOD,
                        MaterialColor.TERRACOTTA_LIGHT_BLUE).strength(2.0f, 3.0f).sound(SoundType.WOOD)));
        INVERTED_SLAB = PokecubeLegends.DIMENSIONS_TAB.register("inverted_slab", () -> new SlabBlock(
                BlockBehaviour.Properties.of(Material.WOOD, MaterialColor.TERRACOTTA_LIGHT_BLUE).strength(2.0f, 3.0f)
                        .sound(SoundType.WOOD)));
        INVERTED_FENCE = PokecubeLegends.DIMENSIONS_TAB.register("inverted_fence", () -> new FenceBlock(
                BlockBehaviour.Properties.of(Material.WOOD, MaterialColor.TERRACOTTA_LIGHT_BLUE).strength(2.0f, 3.0f)
                        .sound(SoundType.WOOD)));
        INVERTED_FENCE_GATE = PokecubeLegends.DIMENSIONS_TAB.register("inverted_fence_gate", () -> new FenceGateBlock(
                BlockBehaviour.Properties.of(Material.WOOD, MaterialColor.TERRACOTTA_LIGHT_BLUE).strength(2.0f, 3.0f)
                        .sound(SoundType.WOOD)));
        INVERTED_BUTTON = PokecubeLegends.DIMENSIONS_TAB.register("inverted_button",
                () -> new ItemGenerator.GenericWoodButton(BlockBehaviour.Properties.of(Material.WOOD,
                        MaterialColor.TERRACOTTA_LIGHT_BLUE).sound(SoundType.WOOD).noCollission().strength(0.5f)));
        INVERTED_PR_PLATE = PokecubeLegends.DIMENSIONS_TAB.register("inverted_pressure_plate",
                () -> new ItemGenerator.GenericPressurePlate(PressurePlateBlock.Sensitivity.EVERYTHING,
                        BlockBehaviour.Properties.of(Material.WOOD, MaterialColor.TERRACOTTA_LIGHT_BLUE).sound(
                                SoundType.WOOD).noCollission().strength(0.5f)));

        INVERTED_TRAPDOOR = PokecubeLegends.DIMENSIONS_TAB.register("inverted_trapdoor",
                () -> new ItemGenerator.GenericTrapDoor(BlockBehaviour.Properties.of(Material.WOOD,
                        MaterialColor.TERRACOTTA_LIGHT_BLUE).sound(SoundType.WOOD).strength(2.0f, 3.0f).noOcclusion()));
        INVERTED_DOOR = PokecubeLegends.DIMENSIONS_TAB.register("inverted_door", () -> new ItemGenerator.GenericDoor(
                BlockBehaviour.Properties.of(Material.WOOD, MaterialColor.TERRACOTTA_LIGHT_BLUE).sound(SoundType.WOOD)
                        .strength(2.0f, 3.0f).noOcclusion()));

        // MIRAGE Blocks
        MIRAGE_SAPLING = PokecubeLegends.DIMENSIONS_TAB.register("mirage_sapling", () -> new MirageSapling(
                new MirageTree(), BlockBehaviour.Properties.of(Material.PLANT, MaterialColor.COLOR_LIGHT_BLUE).strength(0f,
                        1f).sound(SoundType.GRASS).noCollission().noOcclusion()));

        MIRAGE_LEAVES = PokecubeLegends.DIMENSIONS_TAB.register("mirage_leaves", () -> new MirageLeavesBlock(9032191,
                BlockBehaviour.Properties.of(Material.LEAVES, MaterialColor.COLOR_LIGHT_BLUE).sound(SoundType.NYLIUM)
                        .strength(0.2f).noOcclusion().isSuffocating((s, r, p) -> false).isValidSpawn(
                                ItemGenerator::ocelotOrParrot).isViewBlocking((s, r, p) -> false)));

        MIRAGE_LOG = PokecubeLegends.DIMENSIONS_TAB.register("mirage_log", () -> Blocks.log(MaterialColor.SAND,
                MaterialColor.COLOR_LIGHT_BLUE));
        MIRAGE_WOOD = PokecubeLegends.DIMENSIONS_TAB.register("mirage_wood", () -> Blocks.log(
                MaterialColor.COLOR_LIGHT_BLUE, MaterialColor.COLOR_LIGHT_BLUE));
        STRIP_MIRAGE_LOG = PokecubeLegends.DIMENSIONS_TAB.register("stripped_mirage_log", () -> Blocks.log(
                MaterialColor.SAND, MaterialColor.SNOW));
        STRIP_MIRAGE_WOOD = PokecubeLegends.DIMENSIONS_TAB.register("stripped_mirage_wood", () -> Blocks.log(
                MaterialColor.SNOW, MaterialColor.SNOW));

        MIRAGE_BARREL = PokecubeLegends.DIMENSIONS_TAB.register("mirage_barrel", () -> new GenericBarrel(
                BlockBehaviour.Properties.of(Material.WOOD, MaterialColor.SAND).strength(2.5F).sound(SoundType.WOOD)));
        MIRAGE_BOOKSHELF = PokecubeLegends.DIMENSIONS_TAB.register("mirage_bookshelf", () -> new GenericBookshelf(
                BlockBehaviour.Properties.of(Material.WOOD, MaterialColor.SAND).strength(2f, 4f).sound(
                        SoundType.WOOD)));
        MIRAGE_BOOKSHELF_EMPTY = PokecubeLegends.DIMENSIONS_TAB.register("mirage_bookshelf_empty",
                () -> new GenericBookshelfEmpty(BlockBehaviour.Properties.of(Material.WOOD, MaterialColor.SAND)
                        .strength(2f, 4f).sound(SoundType.WOOD).dynamicShape()));

        MIRAGE_PLANKS = PokecubeLegends.DIMENSIONS_TAB.register("mirage_planks", () -> new Block(BlockBehaviour.Properties
                .of(Material.WOOD, MaterialColor.SAND).strength(2.0f).sound(SoundType.WOOD)));
        MIRAGE_STAIRS = PokecubeLegends.DIMENSIONS_TAB.register("mirage_stairs", () -> new ItemGenerator.GenericStairs(
                Blocks.OAK_STAIRS.defaultBlockState(), BlockBehaviour.Properties.of(Material.WOOD, MaterialColor.SAND)
                        .strength(2.0f).sound(SoundType.WOOD)));
        MIRAGE_SLAB = PokecubeLegends.DIMENSIONS_TAB.register("mirage_slab", () -> new SlabBlock(BlockBehaviour.Properties
                .of(Material.WOOD, MaterialColor.SAND).strength(2.0f).sound(SoundType.WOOD)));
        MIRAGE_FENCE = PokecubeLegends.DIMENSIONS_TAB.register("mirage_fence", () -> new FenceBlock(
                BlockBehaviour.Properties.of(Material.WOOD, MaterialColor.SAND).strength(2.0f).sound(SoundType.WOOD)));
        MIRAGE_FENCE_GATE = PokecubeLegends.DIMENSIONS_TAB.register("mirage_fence_gate", () -> new FenceGateBlock(
                BlockBehaviour.Properties.of(Material.WOOD, MaterialColor.SAND).strength(2.0f).sound(SoundType.WOOD)));
        MIRAGE_BUTTON = PokecubeLegends.DIMENSIONS_TAB.register("mirage_button", () -> new ItemGenerator.GenericWoodButton(
                BlockBehaviour.Properties.of(Material.WOOD, MaterialColor.SAND).sound(SoundType.WOOD).noCollission()
                        .strength(0.5f)));
        MIRAGE_PR_PLATE = PokecubeLegends.DIMENSIONS_TAB.register("mirage_pressure_plate",
                () -> new ItemGenerator.GenericPressurePlate(PressurePlateBlock.Sensitivity.MOBS,
                        BlockBehaviour.Properties.of(Material.WOOD, MaterialColor.SAND).sound(SoundType.WOOD)
                                .noCollission().strength(0.5f)));

        MIRAGE_TRAPDOOR = PokecubeLegends.DIMENSIONS_TAB.register("mirage_trapdoor",
                () -> new ItemGenerator.GenericTrapDoor(BlockBehaviour.Properties.of(Material.WOOD, MaterialColor.SAND)
                        .sound(SoundType.WOOD).strength(2.0f, 3.0f).noOcclusion()));
        MIRAGE_DOOR = PokecubeLegends.DIMENSIONS_TAB.register("mirage_door", () -> new ItemGenerator.GenericDoor(
                BlockBehaviour.Properties.of(Material.WOOD, MaterialColor.SAND).sound(SoundType.WOOD).strength(2.0f,
                        3.0f).noOcclusion()));

        // Temporal Blocks
        TEMPORAL_SAPLING = PokecubeLegends.DIMENSIONS_TAB.register("temporal_sapling", () -> new SaplingBase(
                () -> new TemporalTree(), BlockBehaviour.Properties.of(Material.PLANT, MaterialColor.PLANT).strength(0f,
                        1f).sound(SoundType.GRASS).noCollission().noOcclusion()));

        TEMPORAL_LEAVES = PokecubeLegends.DIMENSIONS_TAB.register("temporal_leaves", () -> new LeavesBlock(
                BlockBehaviour.Properties.of(Material.LEAVES, MaterialColor.WARPED_NYLIUM).strength(0.2f).sound(
                        SoundType.GRASS).noOcclusion().isSuffocating((s, r, p) -> false).isValidSpawn(
                                ItemGenerator::ocelotOrParrot).isViewBlocking((s, r, p) -> false)));

        TEMPORAL_LOG = PokecubeLegends.DIMENSIONS_TAB.register("temporal_log", () -> Blocks.log(MaterialColor.WARPED_NYLIUM,
                MaterialColor.COLOR_BROWN));
        TEMPORAL_WOOD = PokecubeLegends.DIMENSIONS_TAB.register("temporal_wood", () -> Blocks.log(
                MaterialColor.WARPED_NYLIUM, MaterialColor.COLOR_BROWN));
        STRIP_TEMPORAL_LOG = PokecubeLegends.DIMENSIONS_TAB.register("stripped_temporal_log", () -> Blocks.log(
                MaterialColor.WARPED_NYLIUM, MaterialColor.WARPED_NYLIUM));
        STRIP_TEMPORAL_WOOD = PokecubeLegends.DIMENSIONS_TAB.register("stripped_temporal_wood", () -> Blocks.log(
                MaterialColor.WARPED_NYLIUM, MaterialColor.WARPED_NYLIUM));

        TEMPORAL_BARREL = PokecubeLegends.DIMENSIONS_TAB.register("temporal_barrel", () -> new GenericBarrel(
                BlockBehaviour.Properties.of(Material.WOOD, MaterialColor.WARPED_NYLIUM).strength(2.5F).sound(
                        SoundType.WOOD)));
        TEMPORAL_BOOKSHELF = PokecubeLegends.DIMENSIONS_TAB.register("temporal_bookshelf", () -> new GenericBookshelf(
                BlockBehaviour.Properties.of(Material.WOOD, MaterialColor.WARPED_NYLIUM).strength(2f, 4f).sound(
                        SoundType.WOOD)));
        TEMPORAL_BOOKSHELF_EMPTY = PokecubeLegends.DIMENSIONS_TAB.register("temporal_bookshelf_empty",
                () -> new GenericBookshelfEmpty(BlockBehaviour.Properties.of(Material.WOOD, MaterialColor.WARPED_NYLIUM)
                        .strength(2f, 4f).sound(SoundType.WOOD).dynamicShape()));

        TEMPORAL_PLANKS = PokecubeLegends.DIMENSIONS_TAB.register("temporal_planks", () -> new Block(BlockBehaviour.Properties
                .of(Material.WOOD, MaterialColor.WARPED_NYLIUM).strength(2.0f).sound(SoundType.WOOD)));
        TEMPORAL_STAIRS = PokecubeLegends.DIMENSIONS_TAB.register("temporal_stairs", () -> new ItemGenerator.GenericStairs(
                Blocks.OAK_STAIRS.defaultBlockState(), BlockBehaviour.Properties.of(Material.WOOD,
                        MaterialColor.WARPED_NYLIUM).strength(2.0f).sound(SoundType.WOOD)));
        TEMPORAL_SLAB = PokecubeLegends.DIMENSIONS_TAB.register("temporal_slab", () -> new SlabBlock(
                BlockBehaviour.Properties.of(Material.WOOD, MaterialColor.WARPED_NYLIUM).strength(2.0f).sound(
                        SoundType.WOOD)));
        TEMPORAL_FENCE = PokecubeLegends.DIMENSIONS_TAB.register("temporal_fence", () -> new FenceBlock(
                BlockBehaviour.Properties.of(Material.WOOD, MaterialColor.WARPED_NYLIUM).strength(2.0f).sound(
                        SoundType.WOOD)));
        TEMPORAL_FENCE_GATE = PokecubeLegends.DIMENSIONS_TAB.register("temporal_fence_gate", () -> new FenceGateBlock(
                BlockBehaviour.Properties.of(Material.WOOD, MaterialColor.WARPED_NYLIUM).strength(2.0f).sound(
                        SoundType.WOOD)));
        TEMPORAL_BUTTON = PokecubeLegends.DIMENSIONS_TAB.register("temporal_button",
                () -> new ItemGenerator.GenericWoodButton(BlockBehaviour.Properties.of(Material.WOOD,
                        MaterialColor.WARPED_NYLIUM).sound(SoundType.WOOD).noCollission().strength(0.5f)));
        TEMPORAL_PR_PLATE = PokecubeLegends.DIMENSIONS_TAB.register("temporal_pressure_plate",
                () -> new ItemGenerator.GenericPressurePlate(PressurePlateBlock.Sensitivity.EVERYTHING,
                        BlockBehaviour.Properties.of(Material.WOOD, MaterialColor.WARPED_NYLIUM).sound(SoundType.WOOD)
                                .noCollission().strength(0.5f)));

        TEMPORAL_TRAPDOOR = PokecubeLegends.DIMENSIONS_TAB.register("temporal_trapdoor",
                () -> new ItemGenerator.GenericTrapDoor(BlockBehaviour.Properties.of(Material.WOOD,
                        MaterialColor.WARPED_NYLIUM).sound(SoundType.WOOD).strength(2.0f, 3.0f).noOcclusion()));
        TEMPORAL_DOOR = PokecubeLegends.DIMENSIONS_TAB.register("temporal_door", () -> new ItemGenerator.GenericDoor(
                BlockBehaviour.Properties.of(Material.WOOD, MaterialColor.WARPED_NYLIUM).sound(SoundType.WOOD).strength(
                        2.0f, 3.0f).noOcclusion()));

        // Dyna Leaves
        DYNA_LEAVES_RED = PokecubeLegends.DIMENSIONS_TAB.register("dyna_leaves_red", () -> new DynaLeavesBlock(
                BlockBehaviour.Properties.of(Material.LEAVES, MaterialColor.COLOR_PINK).strength(0.2f).sound(
                        SoundType.WET_GRASS).noDrops().noOcclusion().isSuffocating((s, r, p) -> false).isValidSpawn(
                                ItemGenerator::ocelotOrParrot).isViewBlocking((s, r, p) -> false)));
        DYNA_LEAVES_PINK = PokecubeLegends.DIMENSIONS_TAB.register("dyna_leaves_pink", () -> new DynaLeavesBlock(
                BlockBehaviour.Properties.of(Material.LEAVES, MaterialColor.COLOR_PINK).strength(0.2f).sound(
                        SoundType.WET_GRASS).noDrops().noOcclusion().isSuffocating((s, r, p) -> false).isValidSpawn(
                                ItemGenerator::ocelotOrParrot).isViewBlocking((s, r, p) -> false)));
        DYNA_LEAVES_PASTEL_PINK = PokecubeLegends.DIMENSIONS_TAB.register("dyna_leaves_pastel_pink", () -> new DynaLeavesBlock(
                BlockBehaviour.Properties.of(Material.LEAVES, MaterialColor.COLOR_PINK).strength(0.2f).sound(
                        SoundType.WET_GRASS).noDrops().noOcclusion().isSuffocating((s, r, p) -> false).isValidSpawn(
                                ItemGenerator::ocelotOrParrot).isViewBlocking((s, r, p) -> false)));

        CRYSTALLIZED_CACTUS = PokecubeLegends.DIMENSIONS_TAB.register("crystallized_cactus", () -> new CrystallizedCactus(
                BlockBehaviour.Properties.of(Material.CACTUS, MaterialColor.SNOW).sound(SoundType.AMETHYST).strength(
                        0.4f)));
        TALL_CRYSTALLIZED_BUSH = PokecubeLegends.DIMENSIONS_TAB.register("tall_crystallized_bush", () -> new TallCrystallizedBush(
                BlockBehaviour.Properties.of(Material.PLANT, MaterialColor.SNOW)
                .sound(SoundType.AMETHYST_CLUSTER).noCollission().instabreak()));
        CRYSTALLIZED_BUSH = PokecubeLegends.DIMENSIONS_TAB.register("crystallized_bush", () -> new CrystallizedBush(
                BlockBehaviour.Properties.of(Material.PLANT, MaterialColor.SNOW).sound(SoundType.AMETHYST_CLUSTER)
                .noCollission().instabreak()));

        // Decorations Creative Tab - Sorting depends on the order the blocks are listed in

        INFECTED_TORCH = PokecubeLegends.DECORATION_TAB.register("infected_torch", () -> new InfectedTorch());
        INFECTED_TORCH_WALL = PokecubeLegends.DECORATION_TAB.register("infected_torch_wall",
                () -> new InfectedTorchWall());

        COSMIC_DUST_BLOCK = PokecubeLegends.DECORATION_TAB.register("cosmic_dust_block", () -> new SandBlock(2730984,
                BlockBehaviour.Properties.of(Material.SAND, MaterialColor.COLOR_LIGHT_BLUE).sound(SoundType.SAND)
                        .strength(0.5f)));

        FRACTAL_BLOCK = PokecubeLegends.DECORATION_TAB.register("fractal_block", () -> new BlockBase(Material.METAL,
                MaterialColor.COLOR_LIGHT_BLUE, 3f, 12, SoundType.METAL, true));

        RUBY_BLOCK = PokecubeLegends.DECORATION_TAB.register("ruby_block", () -> new BlockBase(Material.METAL,
                MaterialColor.COLOR_RED, 1.5f, 10, SoundType.METAL, true));
        RUBY_STAIRS = PokecubeLegends.DECORATION_TAB.register("ruby_stairs", () -> new ItemGenerator.GenericStairs(
                Blocks.STONE_STAIRS.defaultBlockState(), BlockBehaviour.Properties.of(Material.METAL,
                        MaterialColor.COLOR_RED).strength(2.0F, 3.0f).sound(SoundType.METAL)
                        .requiresCorrectToolForDrops()));
        RUBY_SLAB = PokecubeLegends.DECORATION_TAB.register("ruby_slab", () -> new SlabBlock(BlockBehaviour.Properties
                .of(Material.METAL, MaterialColor.COLOR_RED).strength(2.0F, 3.0f).sound(SoundType.METAL)
                .requiresCorrectToolForDrops()));

        SAPPHIRE_BLOCK = PokecubeLegends.DECORATION_TAB.register("sapphire_block", () -> new BlockBase(Material.METAL,
                MaterialColor.COLOR_BLUE, 1.5f, 10, SoundType.METAL, true));
        SAPPHIRE_STAIRS = PokecubeLegends.DECORATION_TAB.register("sapphire_stairs",
                () -> new ItemGenerator.GenericStairs(Blocks.STONE_STAIRS.defaultBlockState(), BlockBehaviour.Properties
                        .of(Material.METAL, MaterialColor.COLOR_BLUE).strength(2.0F, 3.0f).sound(SoundType.METAL)
                        .requiresCorrectToolForDrops()));
        SAPPHIRE_SLAB = PokecubeLegends.DECORATION_TAB.register("sapphire_slab", () -> new SlabBlock(
                BlockBehaviour.Properties.of(Material.METAL, MaterialColor.COLOR_BLUE).strength(2.0F, 3.0f).sound(
                        SoundType.METAL).requiresCorrectToolForDrops()));

        SPECTRUM_BLOCK = PokecubeLegends.DECORATION_TAB.register("spectrum_block", () -> new BlockBase(Material.METAL,
                MaterialColor.COLOR_ORANGE, 5.0f, 7, SoundType.METAL, true));
        SPECTRUM_SLAB = PokecubeLegends.DECORATION_TAB.register("spectrum_slab", () -> new SlabBlock(
                BlockBehaviour.Properties.of(Material.METAL, MaterialColor.COLOR_ORANGE).strength(2.0F, 3.0f).sound(
                        SoundType.METAL).requiresCorrectToolForDrops()));
        SPECTRUM_STAIRS = PokecubeLegends.DECORATION_TAB.register("spectrum_stairs",
                () -> new ItemGenerator.GenericStairs(Blocks.STONE_STAIRS.defaultBlockState(), BlockBehaviour.Properties
                        .of(Material.METAL, MaterialColor.COLOR_ORANGE).strength(2.0F, 3.0f).sound(SoundType.METAL)
                        .requiresCorrectToolForDrops()));

        DISTORTIC_TERRACOTTA = PokecubeLegends.DECORATION_TAB.register("distortic_terracotta", () -> new BlockBase(
                Material.STONE, MaterialColor.COLOR_ORANGE, 2.0f, 3.0f, SoundType.STONE, true));
        DISTORTIC_TERRACOTTA_STAIRS = PokecubeLegends.DECORATION_TAB.register("distortic_terracotta_stairs",
                () -> new ItemGenerator.GenericStairs(Blocks.STONE_STAIRS.defaultBlockState(), BlockBehaviour.Properties
                        .of(Material.STONE, MaterialColor.TERRACOTTA_BLACK).strength(2.0F, 3.0f).sound(SoundType.STONE)
                        .requiresCorrectToolForDrops()));
        DISTORTIC_TERRACOTTA_SLAB = PokecubeLegends.DECORATION_TAB.register("distortic_terracotta_slab",
                () -> new SlabBlock(BlockBehaviour.Properties.of(Material.STONE, MaterialColor.TERRACOTTA_BLACK)
                        .strength(2.0F, 3.0f).sound(SoundType.STONE).requiresCorrectToolForDrops()));

        BOOKSHELF_EMPTY = PokecubeLegends.DECORATION_TAB.register("bookshelf_empty", () -> new GenericBookshelfEmpty(
                BlockBehaviour.Properties.of(Material.WOOD, MaterialColor.WOOD).strength(2f, 4f).sound(SoundType.WOOD)
                        .dynamicShape()));

        DISTORTIC_OAK_PLANKS = PokecubeLegends.DECORATION_TAB.register("distortic_oak_planks", () -> new Block(
                BlockBehaviour.Properties.of(Material.WOOD, MaterialColor.WOOD).strength(2.0f).sound(SoundType.WOOD)));
        DISTORTIC_OAK_STAIRS = PokecubeLegends.DECORATION_TAB.register("distortic_oak_stairs",
                () -> new ItemGenerator.GenericStairs(Blocks.OAK_STAIRS.defaultBlockState(), BlockBehaviour.Properties
                        .of(Material.WOOD, MaterialColor.WOOD).strength(2.0f).sound(SoundType.WOOD)));
        DISTORTIC_OAK_SLAB = PokecubeLegends.DECORATION_TAB.register("distortic_oak_slab", () -> new SlabBlock(
                BlockBehaviour.Properties.of(Material.WOOD, MaterialColor.WOOD).strength(2.0f).sound(SoundType.WOOD)));

        DISTORTIC_DARK_OAK_PLANKS = PokecubeLegends.DECORATION_TAB.register("distortic_dark_oak_planks",
                () -> new Block(BlockBehaviour.Properties.of(Material.WOOD, MaterialColor.WOOD).strength(2.0f).sound(
                        SoundType.WOOD)));
        DISTORTIC_DARK_OAK_STAIRS = PokecubeLegends.DECORATION_TAB.register("distortic_dark_oak_stairs",
                () -> new ItemGenerator.GenericStairs(Blocks.OAK_STAIRS.defaultBlockState(), BlockBehaviour.Properties
                        .of(Material.WOOD, MaterialColor.WOOD).strength(2.0f).sound(SoundType.WOOD)));
        DISTORTIC_DARK_OAK_SLAB = PokecubeLegends.DECORATION_TAB.register("distortic_dark_oak_slab",
                () -> new SlabBlock(BlockBehaviour.Properties.of(Material.WOOD, MaterialColor.WOOD).strength(2.0f)
                        .sound(SoundType.WOOD)));

        DISTORTIC_SPRUCE_PLANKS = PokecubeLegends.DECORATION_TAB.register("distortic_spruce_planks", () -> new Block(
                BlockBehaviour.Properties.of(Material.WOOD, MaterialColor.WOOD).strength(2.0f).sound(SoundType.WOOD)));
        DISTORTIC_SPRUCE_STAIRS = PokecubeLegends.DECORATION_TAB.register("distortic_spruce_stairs",
                () -> new ItemGenerator.GenericStairs(Blocks.OAK_STAIRS.defaultBlockState(), BlockBehaviour.Properties
                        .of(Material.WOOD, MaterialColor.WOOD).strength(2.0f).sound(SoundType.WOOD)));
        DISTORTIC_SPRUCE_SLAB = PokecubeLegends.DECORATION_TAB.register("distortic_spruce_slab", () -> new SlabBlock(
                BlockBehaviour.Properties.of(Material.WOOD, MaterialColor.WOOD).strength(2.0f).sound(SoundType.WOOD)));

        DISTORTIC_BIRCH_PLANKS = PokecubeLegends.DECORATION_TAB.register("distortic_birch_planks", () -> new Block(
                BlockBehaviour.Properties.of(Material.WOOD, MaterialColor.WOOD).strength(2.0f).sound(SoundType.WOOD)));
        DISTORTIC_BIRCH_STAIRS = PokecubeLegends.DECORATION_TAB.register("distortic_birch_stairs",
                () -> new ItemGenerator.GenericStairs(Blocks.OAK_STAIRS.defaultBlockState(), BlockBehaviour.Properties
                        .of(Material.WOOD, MaterialColor.WOOD).strength(2.0f).sound(SoundType.WOOD)));
        DISTORTIC_BIRCH_SLAB = PokecubeLegends.DECORATION_TAB.register("distortic_birch_slab", () -> new SlabBlock(
                BlockBehaviour.Properties.of(Material.WOOD, MaterialColor.WOOD).strength(2.0f).sound(SoundType.WOOD)));

        DISTORTIC_ACACIA_PLANKS = PokecubeLegends.DECORATION_TAB.register("distortic_acacia_planks", () -> new Block(
                BlockBehaviour.Properties.of(Material.WOOD, MaterialColor.WOOD).strength(2.0f).sound(SoundType.WOOD)));
        DISTORTIC_ACACIA_STAIRS = PokecubeLegends.DECORATION_TAB.register("distortic_acacia_stairs",
                () -> new ItemGenerator.GenericStairs(Blocks.OAK_STAIRS.defaultBlockState(), BlockBehaviour.Properties
                        .of(Material.WOOD, MaterialColor.WOOD).strength(2.0f).sound(SoundType.WOOD)));
        DISTORTIC_ACACIA_SLAB = PokecubeLegends.DECORATION_TAB.register("distortic_acacia_slab", () -> new SlabBlock(
                BlockBehaviour.Properties.of(Material.WOOD, MaterialColor.WOOD).strength(2.0f).sound(SoundType.WOOD)));

        DISTORTIC_JUNGLE_PLANKS = PokecubeLegends.DECORATION_TAB.register("distortic_jungle_planks", () -> new Block(
                BlockBehaviour.Properties.of(Material.WOOD, MaterialColor.WOOD).strength(2.0f).sound(SoundType.WOOD)));
        DISTORTIC_JUNGLE_STAIRS = PokecubeLegends.DECORATION_TAB.register("distortic_jungle_stairs",
                () -> new ItemGenerator.GenericStairs(Blocks.OAK_STAIRS.defaultBlockState(), BlockBehaviour.Properties
                        .of(Material.WOOD, MaterialColor.WOOD).strength(2.0f).sound(SoundType.WOOD)));
        DISTORTIC_JUNGLE_SLAB = PokecubeLegends.DECORATION_TAB.register("distortic_jungle_slab", () -> new SlabBlock(
                BlockBehaviour.Properties.of(Material.WOOD, MaterialColor.WOOD).strength(2.0f).sound(SoundType.WOOD)));

        CHISELED_DISTORTIC_MIRROR = PokecubeLegends.DECORATION_TAB.register("chiseled_distortic_mirror",
                () -> new BlockBase(Material.GLASS, MaterialColor.SNOW, 1.5f, 1.5f, SoundType.GLASS, true));

        // Concrete Blocks
        CONCRETE_LOG = PokecubeLegends.DECORATION_TAB.register("concrete_log", () -> BlockInit.concreteLog(
                MaterialColor.SNOW, MaterialColor.COLOR_GRAY));
        CONCRETE_WOOD = PokecubeLegends.DECORATION_TAB.register("concrete_wood", () -> BlockInit.concreteLog(
                MaterialColor.COLOR_GRAY, MaterialColor.COLOR_GRAY));
        STRIP_CONCRETE_LOG = PokecubeLegends.DECORATION_TAB.register("stripped_concrete_log", () -> BlockInit
                .concreteLog(MaterialColor.SNOW, MaterialColor.SNOW));
        STRIP_CONCRETE_WOOD = PokecubeLegends.DECORATION_TAB.register("stripped_concrete_wood", () -> BlockInit
                .concreteLog(MaterialColor.SNOW, MaterialColor.SNOW));

        CONCRETE_BARREL = PokecubeLegends.DECORATION_TAB.register("concrete_barrel", () -> new GenericBarrel(
                BlockBehaviour.Properties.of(Material.STONE, MaterialColor.SNOW).strength(4.5F).sound(SoundType.STONE)
                        .requiresCorrectToolForDrops()));
        CONCRETE_BOOKSHELF = PokecubeLegends.DECORATION_TAB.register("concrete_bookshelf", () -> new GenericBookshelf(
                BlockBehaviour.Properties.of(Material.STONE, MaterialColor.SNOW).strength(10.0f, 500.0f).sound(
                        SoundType.WOOD).requiresCorrectToolForDrops()));
        CONCRETE_BOOKSHELF_EMPTY = PokecubeLegends.DECORATION_TAB.register("concrete_bookshelf_empty",
                () -> new GenericBookshelfEmpty(BlockBehaviour.Properties.of(Material.STONE, MaterialColor.SNOW)
                        .strength(10.0f, 500.0f).sound(SoundType.WOOD).requiresCorrectToolForDrops().dynamicShape()));

        CONCRETE_PLANKS = PokecubeLegends.DECORATION_TAB.register("concrete_planks", () -> new Block(
                BlockBehaviour.Properties.of(Material.STONE, MaterialColor.SNOW).strength(10.0f, 500.0f).sound(
                        SoundType.STONE).requiresCorrectToolForDrops()));
        CONCRETE_STAIRS = PokecubeLegends.DECORATION_TAB.register("concrete_stairs",
                () -> new ItemGenerator.GenericStairs(Blocks.OAK_STAIRS.defaultBlockState(), BlockBehaviour.Properties
                        .of(Material.STONE, MaterialColor.SNOW).strength(10.0f, 500.0f).sound(SoundType.STONE)
                        .requiresCorrectToolForDrops()));
        CONCRETE_SLAB = PokecubeLegends.DECORATION_TAB.register("concrete_slab", () -> new SlabBlock(
                BlockBehaviour.Properties.of(Material.STONE, MaterialColor.SNOW).strength(10.0f, 500.0f).sound(
                        SoundType.STONE).requiresCorrectToolForDrops()));
        CONCRETE_FENCE = PokecubeLegends.DECORATION_TAB.register("concrete_fence", () -> new FenceBlock(
                BlockBehaviour.Properties.of(Material.STONE, MaterialColor.SNOW).strength(10.0f, 500.0f).sound(
                        SoundType.STONE).requiresCorrectToolForDrops()));
        CONCRETE_FENCE_GATE = PokecubeLegends.DECORATION_TAB.register("concrete_fence_gate", () -> new FenceGateBlock(
                BlockBehaviour.Properties.of(Material.STONE, MaterialColor.SNOW).strength(10.0f, 500.0f).sound(
                        SoundType.STONE).requiresCorrectToolForDrops()));
        CONCRETE_BUTTON = PokecubeLegends.DECORATION_TAB.register("concrete_button",
                () -> new ItemGenerator.GenericWoodButton(BlockBehaviour.Properties.of(Material.STONE,
                        MaterialColor.SNOW).sound(SoundType.STONE).noCollission().strength(1.5f, 500.0f)
                        .requiresCorrectToolForDrops()));
        CONCRETE_PR_PLATE = PokecubeLegends.DECORATION_TAB.register("concrete_pressure_plate",
                () -> new ItemGenerator.GenericPressurePlate(PressurePlateBlock.Sensitivity.EVERYTHING,
                        BlockBehaviour.Properties.of(Material.STONE, MaterialColor.SNOW).sound(SoundType.STONE)
                                .noCollission().strength(1.5f, 500.0f).requiresCorrectToolForDrops()));

        CONCRETE_TRAPDOOR = PokecubeLegends.DECORATION_TAB.register("concrete_trapdoor",
                () -> new ItemGenerator.GenericTrapDoor(BlockBehaviour.Properties.of(Material.STONE, MaterialColor.SNOW)
                        .sound(SoundType.STONE).strength(10.0f, 500.0f).noOcclusion().requiresCorrectToolForDrops()));
        CONCRETE_DOOR = PokecubeLegends.DECORATION_TAB.register("concrete_door", () -> new ItemGenerator.GenericDoor(
                BlockBehaviour.Properties.of(Material.STONE, MaterialColor.SNOW).sound(SoundType.STONE).strength(10.0f,
                        500.0f).noOcclusion().requiresCorrectToolForDrops()));

        CONCRETE_DENSE_BARREL = PokecubeLegends.DECORATION_TAB.register("concrete_dense_barrel",
                () -> new GenericBarrel(BlockBehaviour.Properties.of(Material.STONE, MaterialColor.SNOW).strength(20.0F,
                        1200.0f).sound(SoundType.STONE).requiresCorrectToolForDrops()));
        CONCRETE_DENSE_BOOKSHELF = PokecubeLegends.DECORATION_TAB.register("concrete_dense_bookshelf",
                () -> new GenericBookshelf(BlockBehaviour.Properties.of(Material.STONE, MaterialColor.SNOW).strength(
                        20.0f, 1200.0f).sound(SoundType.STONE).requiresCorrectToolForDrops()));
        CONCRETE_DENSE_BOOKSHELF_EMPTY = PokecubeLegends.DECORATION_TAB.register("concrete_dense_bookshelf_empty",
                () -> new GenericBookshelfEmpty(BlockBehaviour.Properties.of(Material.STONE, MaterialColor.SNOW)
                        .strength(20.0f, 1200.0f).sound(SoundType.STONE).requiresCorrectToolForDrops().dynamicShape()));

        CONCRETE_DENSE_PLANKS = PokecubeLegends.DECORATION_TAB.register("concrete_dense_planks", () -> new Block(
                BlockBehaviour.Properties.of(Material.STONE, MaterialColor.SNOW).strength(20.0f, 1200.0f).sound(
                        SoundType.STONE).requiresCorrectToolForDrops()));
        CONCRETE_DENSE_STAIRS = PokecubeLegends.DECORATION_TAB.register("concrete_dense_stairs",
                () -> new ItemGenerator.GenericStairs(Blocks.OAK_STAIRS.defaultBlockState(), BlockBehaviour.Properties
                        .of(Material.STONE, MaterialColor.SNOW).strength(25.0f, 1200.0f).sound(SoundType.STONE)
                        .requiresCorrectToolForDrops()));
        CONCRETE_DENSE_SLAB = PokecubeLegends.DECORATION_TAB.register("concrete_dense_slab", () -> new SlabBlock(
                BlockBehaviour.Properties.of(Material.STONE, MaterialColor.SNOW).strength(20.0f, 1200.0f).sound(
                        SoundType.STONE).requiresCorrectToolForDrops()));
        CONCRETE_DENSE_WALL = PokecubeLegends.DECORATION_TAB.register("concrete_dense_wall", () -> new WallBlock(
                BlockBehaviour.Properties.of(Material.STONE, MaterialColor.SNOW).strength(20.0f, 1200.0f).sound(
                        SoundType.STONE).requiresCorrectToolForDrops()));
        CONCRETE_DENSE_WALL_GATE = PokecubeLegends.DECORATION_TAB.register("concrete_dense_wall_gate",
                () -> new WallGateBlock(BlockBehaviour.Properties.of(Material.STONE, MaterialColor.SNOW).strength(20.0f,
                        1200.0f).sound(SoundType.STONE).requiresCorrectToolForDrops()));
        CONCRETE_DENSE_BUTTON = PokecubeLegends.DECORATION_TAB.register("concrete_dense_button",
                () -> new ItemGenerator.GenericStoneButton(BlockBehaviour.Properties.of(Material.STONE,
                        MaterialColor.SNOW).sound(SoundType.STONE).noCollission().strength(2.8f, 1200.0f)
                        .requiresCorrectToolForDrops()));
        CONCRETE_DENSE_PR_PLATE = PokecubeLegends.DECORATION_TAB.register("concrete_dense_pressure_plate",
                () -> new ItemGenerator.GenericPressurePlate(PressurePlateBlock.Sensitivity.MOBS,
                        BlockBehaviour.Properties.of(Material.STONE, MaterialColor.SNOW).sound(SoundType.STONE)
                                .noCollission().strength(2.8f, 1200.0f).requiresCorrectToolForDrops()));

        // Ultra Metal
        ULTRA_METAL = PokecubeLegends.DECORATION_TAB.register("ultra_metal", () -> new BlockBase(Material.METAL,
                MaterialColor.COLOR_LIGHT_GREEN, 5.0f, 10f, SoundType.STONE, true));
        ULTRA_METAL_STAIRS = PokecubeLegends.DECORATION_TAB.register("ultra_metal_stairs",
                () -> new ItemGenerator.GenericStairs(Blocks.STONE_STAIRS.defaultBlockState(), BlockBehaviour.Properties
                        .of(Material.STONE, MaterialColor.COLOR_LIGHT_GREEN).strength(2.0F, 3.0f).sound(SoundType.STONE)
                        .requiresCorrectToolForDrops()));
        ULTRA_METAL_SLAB = PokecubeLegends.DECORATION_TAB.register("ultra_metal_slab", () -> new SlabBlock(
                BlockBehaviour.Properties.of(Material.STONE, MaterialColor.COLOR_LIGHT_GRAY).strength(2.0F, 3.0f).sound(
                        SoundType.STONE).requiresCorrectToolForDrops()));
        ULTRA_METAL_BUTTON = PokecubeLegends.DECORATION_TAB.register("ultra_metal_button",
                () -> new ItemGenerator.GenericWoodButton(BlockBehaviour.Properties.of(Material.METAL,
                        MaterialColor.COLOR_LIGHT_GREEN).sound(SoundType.METAL).noCollission().strength(0.5F)
                        .requiresCorrectToolForDrops()));
        ULTRA_METAL_PR_PLATE = PokecubeLegends.DECORATION_TAB.register("ultra_metal_pressure_plate",
                () -> new ItemGenerator.GenericPressurePlate(PressurePlateBlock.Sensitivity.MOBS,
                        BlockBehaviour.Properties.of(Material.METAL, MaterialColor.COLOR_LIGHT_GREEN).sound(
                                SoundType.METAL).noCollission().strength(0.7F).requiresCorrectToolForDrops()));

        ULTRA_MAGNETIC = PokecubeLegends.DECORATION_TAB.register("magnetic_stone", () -> new MagneticBlock(Material.STONE,
                MaterialColor.COLOR_BLUE, 4f, 3f, SoundType.METAL, true));

        ULTRA_STONE_BRICKS = PokecubeLegends.DECORATION_TAB.register("ultra_stone_bricks", () -> new BlockBase(Material.STONE,
                MaterialColor.TERRACOTTA_CYAN, 1.5f, 10f, SoundType.STONE, true));
        ULTRA_STONE_BRICK_STAIRS = PokecubeLegends.DECORATION_TAB.register("ultra_stone_brick_stairs",
                () -> new ItemGenerator.GenericStairs(Blocks.STONE_STAIRS.defaultBlockState(), BlockBehaviour.Properties
                        .of(Material.STONE, MaterialColor.COLOR_LIGHT_GRAY).strength(2.0F, 3.0f).sound(SoundType.STONE)
                        .requiresCorrectToolForDrops()));
        ULTRA_STONE_BRICK_SLAB = PokecubeLegends.DECORATION_TAB.register("ultra_stone_brick_slab", () -> new SlabBlock(
                BlockBehaviour.Properties.of(Material.STONE, MaterialColor.COLOR_LIGHT_GRAY).strength(2.0F, 3.0f).sound(
                        SoundType.STONE).requiresCorrectToolForDrops()));

        ULTRA_DARKSTONE_BRICKS = PokecubeLegends.DECORATION_TAB.register("ultra_darkstone_bricks",
                () -> new EffectBlockBase(Material.STONE, MaterialColor.COLOR_BLACK, 5f, 8f,
                        SoundType.GILDED_BLACKSTONE, true, MobEffects.BLINDNESS));
        ULTRA_DARKSTONE_BRICKS_STAIRS = PokecubeLegends.DECORATION_TAB.register("ultra_darkstone_brick_stairs",
                () -> new ItemGenerator.GenericStairs(Blocks.STONE_STAIRS.defaultBlockState(), BlockBehaviour.Properties
                        .of(Material.STONE, MaterialColor.COLOR_BLACK).strength(2.0F, 3.0f).sound(
                                SoundType.GILDED_BLACKSTONE).requiresCorrectToolForDrops()));
        ULTRA_DARKSTONE_BRICKS_SLAB = PokecubeLegends.DECORATION_TAB.register("ultra_darkstone_brick_slab",
                () -> new SlabBlock(BlockBehaviour.Properties.of(Material.STONE, MaterialColor.COLOR_BLACK).strength(
                        2.0F, 3.0f).sound(SoundType.GILDED_BLACKSTONE).requiresCorrectToolForDrops()));

        CRYS_SANDSTONE_BRICKS = PokecubeLegends.DECORATION_TAB.register("crystallized_sandstone_bricks", () -> new BlockBase(
                Material.STONE, MaterialColor.SNOW, 1.4f, 10f, SoundType.STONE, true));
        CRYS_SANDSTONE_BRICK_STAIRS = PokecubeLegends.DECORATION_TAB.register("crystallized_sandstone_brick_stairs",
                () -> new ItemGenerator.GenericStairs(Blocks.STONE_STAIRS.defaultBlockState(), BlockBehaviour.Properties
                        .of(Material.STONE, MaterialColor.SNOW).strength(2.0F, 3.0f).sound(SoundType.STONE)
                        .requiresCorrectToolForDrops()));
        CRYS_SANDSTONE_BRICK_SLAB = PokecubeLegends.DECORATION_TAB.register("crystallized_sandstone_brick_slab", () -> new SlabBlock(
                BlockBehaviour.Properties.of(Material.STONE, MaterialColor.SNOW).strength(2.0F, 3.0f).sound(
                        SoundType.STONE).requiresCorrectToolForDrops()));

        CRYSTAL_BRICKS = PokecubeLegends.DECORATION_TAB.register("crystal_bricks", () -> new BlockBase(Material.ICE_SOLID,
                MaterialColor.COLOR_LIGHT_BLUE, 0.5F, 10, SoundType.AMETHYST, true));
        CRYSTAL_BRICK_STAIRS = PokecubeLegends.DECORATION_TAB.register("crystal_brick_stairs",
                () -> new ItemGenerator.GenericStairs(Blocks.STONE_STAIRS.defaultBlockState(), BlockBehaviour.Properties
                        .of(Material.GLASS, MaterialColor.COLOR_LIGHT_BLUE).strength(2.0F, 3.0f).sound(
                                SoundType.AMETHYST).requiresCorrectToolForDrops()));
        CRYSTAL_BRICK_SLAB = PokecubeLegends.DECORATION_TAB.register("crystal_brick_slab", () -> new SlabBlock(
                BlockBehaviour.Properties.of(Material.GLASS, MaterialColor.COLOR_LIGHT_BLUE).strength(2.0F, 3.0f)
                        .sound(SoundType.AMETHYST).requiresCorrectToolForDrops()));

        // Ocean Bricks
        OCEAN_BRICKS = PokecubeLegends.DECORATION_TAB.register("ocean_bricks", () -> new BlockBase(Material.STONE,
                MaterialColor.COLOR_CYAN, 1.5f, 10f, SoundType.STONE, true));
        OCEAN_BRICK_STAIRS = PokecubeLegends.DECORATION_TAB.register("ocean_brick_stairs", () -> new GenericStairs(
                Blocks.STONE_STAIRS.defaultBlockState(), BlockBehaviour.Properties.of(Material.STONE,
                        MaterialColor.COLOR_CYAN).strength(2.0F, 10f).sound(SoundType.STONE)
                        .requiresCorrectToolForDrops()));
        OCEAN_BRICK_SLAB = PokecubeLegends.DECORATION_TAB.register("ocean_brick_slab", () -> new SlabBlock(
                BlockBehaviour.Properties.of(Material.STONE, MaterialColor.COLOR_CYAN).strength(2.0F, 10f).sound(
                        SoundType.STONE).requiresCorrectToolForDrops()));

        // Sky Bricks
        SKY_BRICKS = PokecubeLegends.DECORATION_TAB.register("sky_bricks", () -> new BlockBase(Material.STONE,
                MaterialColor.COLOR_BLUE, 1.5f, 10f, SoundType.STONE, true));
        SKY_BRICK_STAIRS = PokecubeLegends.DECORATION_TAB.register("sky_brick_stairs",
                () -> new ItemGenerator.GenericStairs(Blocks.STONE_STAIRS.defaultBlockState(), BlockBehaviour.Properties
                        .of(Material.STONE, MaterialColor.COLOR_BLUE).strength(2.0F, 10f).sound(SoundType.STONE)
                        .requiresCorrectToolForDrops()));
        SKY_BRICK_SLAB = PokecubeLegends.DECORATION_TAB.register("sky_brick_slab", () -> new SlabBlock(
                BlockBehaviour.Properties.of(Material.STONE, MaterialColor.COLOR_BLUE).strength(2.0F, 10f).sound(
                        SoundType.STONE).requiresCorrectToolForDrops()));

        // Purpur Bricks
        PURPUR_BRICKS = PokecubeLegends.DECORATION_TAB.register("purpur_bricks", () -> new BlockBase(Material.STONE,
                MaterialColor.COLOR_MAGENTA, 1.5f, 10f, SoundType.STONE, true));
        PURPUR_BRICK_STAIRS = PokecubeLegends.DECORATION_TAB.register("purpur_brick_stairs",
                () -> new ItemGenerator.GenericStairs(Blocks.STONE_STAIRS.defaultBlockState(), BlockBehaviour.Properties
                        .of(Material.STONE, MaterialColor.COLOR_MAGENTA).strength(2.0F, 3.0f).sound(SoundType.STONE)
                        .requiresCorrectToolForDrops()));
        PURPUR_BRICK_SLAB = PokecubeLegends.DECORATION_TAB.register("purpur_brick_slab", () -> new SlabBlock(
                BlockBehaviour.Properties.of(Material.STONE, MaterialColor.COLOR_MAGENTA).strength(2.0F, 3.0f).sound(
                        SoundType.STONE).requiresCorrectToolForDrops()));

        // Magma Bricks
        MAGMA_BRICKS = PokecubeLegends.DECORATION_TAB.register("magma_bricks", () -> new MagmaBlock(
                BlockBehaviour.Properties.of(Material.STONE, MaterialColor.NETHER).strength(1.5f, 10).sound(
                        SoundType.NETHERRACK).lightLevel(b -> 3).emissiveRendering((s, r, p) -> true)
                        .requiresCorrectToolForDrops()));
        MAGMA_BRICK_STAIRS = PokecubeLegends.DECORATION_TAB.register("magma_brick_stairs",
                () -> new ItemGenerator.GenericStairs(Blocks.STONE_STAIRS.defaultBlockState(), BlockBehaviour.Properties
                        .of(Material.STONE, MaterialColor.NETHER).strength(2.0F, 3.0f).sound(
                                SoundType.NETHERRACK).lightLevel(b -> 3).emissiveRendering((s, r, p) -> true)
                        .requiresCorrectToolForDrops()));
        MAGMA_BRICK_SLAB = PokecubeLegends.DECORATION_TAB.register("magma_brick_slab", () -> new SlabBlock(
                BlockBehaviour.Properties.of(Material.STONE, MaterialColor.NETHER).strength(2.0F, 3.0f).sound(
                        SoundType.NETHERRACK).lightLevel(b -> 3).emissiveRendering((s, r, p) -> true)
                        .requiresCorrectToolForDrops()));

        // Stormy Sky Bricks
        STORMY_SKY_BRICKS = PokecubeLegends.DECORATION_TAB.register("stormy_sky_bricks", () -> new BlockBase(Material.STONE,
                MaterialColor.COLOR_LIGHT_GRAY, 1.5f, 10f, SoundType.STONE, true));
        STORMY_SKY_BRICK_STAIRS = PokecubeLegends.DECORATION_TAB.register("stormy_sky_brick_stairs",
                () -> new ItemGenerator.GenericStairs(Blocks.STONE_STAIRS.defaultBlockState(), BlockBehaviour.Properties
                        .of(Material.STONE, MaterialColor.COLOR_LIGHT_GRAY).strength(2.0F, 3.0f).sound(SoundType.STONE)
                        .requiresCorrectToolForDrops()));
        STORMY_SKY_BRICK_SLAB = PokecubeLegends.DECORATION_TAB.register("stormy_sky_brick_slab", () -> new SlabBlock(
                BlockBehaviour.Properties.of(Material.STONE, MaterialColor.COLOR_LIGHT_GRAY).strength(2.0F, 3.0f).sound(
                        SoundType.STONE).requiresCorrectToolForDrops()));

        // Distortic Stone Bricks
        DISTORTIC_STONE_BRICKS = PokecubeLegends.DECORATION_TAB.register("distortic_stone_bricks", () -> new BlockBase(
                Material.STONE, MaterialColor.TERRACOTTA_BLACK, 2.5f, 10f, SoundType.STONE, true));
        DISTORTIC_STONE_BRICK_STAIRS = PokecubeLegends.DECORATION_TAB.register("distortic_stone_brick_stairs",
                () -> new ItemGenerator.GenericStairs(Blocks.STONE_STAIRS.defaultBlockState(), BlockBehaviour.Properties
                        .of(Material.STONE, MaterialColor.TERRACOTTA_BLACK).strength(2.0F, 3.0f).sound(SoundType.STONE)
                        .requiresCorrectToolForDrops()));
        DISTORTIC_STONE_BRICK_SLAB = PokecubeLegends.DECORATION_TAB.register("distortic_stone_brick_slab",
                () -> new SlabBlock(BlockBehaviour.Properties.of(Material.STONE, MaterialColor.TERRACOTTA_BLACK)
                        .strength(2.0F, 3.0f).sound(SoundType.STONE).requiresCorrectToolForDrops()));

        CHISELED_DISTORTIC_STONE = PokecubeLegends.DECORATION_TAB.register("chiseled_distortic_stone_bricks",
                () -> new BlockBase(Material.STONE, MaterialColor.TERRACOTTA_BLACK, 2.5f, 10f, SoundType.STONE, true));
        CHISELED_DISTORTIC_STONE_SLAB = PokecubeLegends.DECORATION_TAB.register("chiseled_distortic_stone_brick_slab",
                () -> new SlabBlock(BlockBehaviour.Properties.of(Material.STONE, MaterialColor.TERRACOTTA_BLACK)
                        .strength(2.0F, 3.0f).sound(SoundType.STONE).requiresCorrectToolForDrops()));
        CHISELED_DISTORTIC_STONE_STAIRS = PokecubeLegends.DECORATION_TAB.register("chiseled_distortic_stone_brick_stairs",
                () -> new ItemGenerator.GenericStairs(Blocks.STONE_STAIRS.defaultBlockState(), BlockBehaviour.Properties
                        .of(Material.STONE, MaterialColor.TERRACOTTA_BLACK).strength(2.0F, 3.0f).sound(SoundType.STONE)
                        .requiresCorrectToolForDrops()));

        DISTORTIC_STONE_BARREL = PokecubeLegends.DECORATION_TAB.register("distortic_stone_barrel",
                () -> new GenericBarrel(BlockBehaviour.Properties.of(Material.STONE, MaterialColor.TERRACOTTA_BLACK)
                        .strength(4.5F).sound(SoundType.STONE).requiresCorrectToolForDrops()));

        // Unown Stones
        UNOWN_STONE_A = PokecubeLegends.DECORATION_TAB.register("unown_stone_a", () -> new BlockBase(Material.STONE,
                MaterialColor.TERRACOTTA_BLACK, 2f, 3f, SoundType.STONE, true));
        UNOWN_STONE_B = PokecubeLegends.DECORATION_TAB.register("unown_stone_b", () -> new BlockBase(Material.STONE,
                MaterialColor.TERRACOTTA_BLACK, 2f, 3f, SoundType.STONE, true));
        UNOWN_STONE_C = PokecubeLegends.DECORATION_TAB.register("unown_stone_c", () -> new BlockBase(Material.STONE,
                MaterialColor.TERRACOTTA_BLACK, 2f, 3f, SoundType.STONE, true));
        UNOWN_STONE_D = PokecubeLegends.DECORATION_TAB.register("unown_stone_d", () -> new BlockBase(Material.STONE,
                MaterialColor.TERRACOTTA_BLACK, 2f, 3f, SoundType.STONE, true));
        UNOWN_STONE_E = PokecubeLegends.DECORATION_TAB.register("unown_stone_e", () -> new BlockBase(Material.STONE,
                MaterialColor.TERRACOTTA_BLACK, 2f, 3f, SoundType.STONE, true));
        UNOWN_STONE_F = PokecubeLegends.DECORATION_TAB.register("unown_stone_f", () -> new BlockBase(Material.STONE,
                MaterialColor.TERRACOTTA_BLACK, 2f, 3f, SoundType.STONE, true));
        UNOWN_STONE_G = PokecubeLegends.DECORATION_TAB.register("unown_stone_g", () -> new BlockBase(Material.STONE,
                MaterialColor.TERRACOTTA_BLACK, 2f, 3f, SoundType.STONE, true));
        UNOWN_STONE_H = PokecubeLegends.DECORATION_TAB.register("unown_stone_h", () -> new BlockBase(Material.STONE,
                MaterialColor.TERRACOTTA_BLACK, 2f, 3f, SoundType.STONE, true));
        UNOWN_STONE_I = PokecubeLegends.DECORATION_TAB.register("unown_stone_i", () -> new BlockBase(Material.STONE,
                MaterialColor.TERRACOTTA_BLACK, 2f, 3f, SoundType.STONE, true));
        UNOWN_STONE_J = PokecubeLegends.DECORATION_TAB.register("unown_stone_j", () -> new BlockBase(Material.STONE,
                MaterialColor.TERRACOTTA_BLACK, 2f, 3f, SoundType.STONE, true));
        UNOWN_STONE_K = PokecubeLegends.DECORATION_TAB.register("unown_stone_k", () -> new BlockBase(Material.STONE,
                MaterialColor.TERRACOTTA_BLACK, 2f, 3f, SoundType.STONE, true));
        UNOWN_STONE_L = PokecubeLegends.DECORATION_TAB.register("unown_stone_l", () -> new BlockBase(Material.STONE,
                MaterialColor.TERRACOTTA_BLACK, 2f, 3f, SoundType.STONE, true));
        UNOWN_STONE_M = PokecubeLegends.DECORATION_TAB.register("unown_stone_m", () -> new BlockBase(Material.STONE,
                MaterialColor.TERRACOTTA_BLACK, 2f, 3f, SoundType.STONE, true));
        UNOWN_STONE_N = PokecubeLegends.DECORATION_TAB.register("unown_stone_n", () -> new BlockBase(Material.STONE,
                MaterialColor.TERRACOTTA_BLACK, 2f, 3f, SoundType.STONE, true));
        UNOWN_STONE_O = PokecubeLegends.DECORATION_TAB.register("unown_stone_o", () -> new BlockBase(Material.STONE,
                MaterialColor.TERRACOTTA_BLACK, 2f, 3f, SoundType.STONE, true));
        UNOWN_STONE_P = PokecubeLegends.DECORATION_TAB.register("unown_stone_p", () -> new BlockBase(Material.STONE,
                MaterialColor.TERRACOTTA_BLACK, 2f, 3f, SoundType.STONE, true));
        UNOWN_STONE_Q = PokecubeLegends.DECORATION_TAB.register("unown_stone_q", () -> new BlockBase(Material.STONE,
                MaterialColor.TERRACOTTA_BLACK, 2f, 3f, SoundType.STONE, true));
        UNOWN_STONE_R = PokecubeLegends.DECORATION_TAB.register("unown_stone_r", () -> new BlockBase(Material.STONE,
                MaterialColor.TERRACOTTA_BLACK, 2f, 3f, SoundType.STONE, true));
        UNOWN_STONE_S = PokecubeLegends.DECORATION_TAB.register("unown_stone_s", () -> new BlockBase(Material.STONE,
                MaterialColor.TERRACOTTA_BLACK, 2f, 3f, SoundType.STONE, true));
        UNOWN_STONE_T = PokecubeLegends.DECORATION_TAB.register("unown_stone_t", () -> new BlockBase(Material.STONE,
                MaterialColor.TERRACOTTA_BLACK, 2f, 3f, SoundType.STONE, true));
        UNOWN_STONE_U = PokecubeLegends.DECORATION_TAB.register("unown_stone_u", () -> new BlockBase(Material.STONE,
                MaterialColor.TERRACOTTA_BLACK, 2f, 3f, SoundType.STONE, true));
        UNOWN_STONE_V = PokecubeLegends.DECORATION_TAB.register("unown_stone_v", () -> new BlockBase(Material.STONE,
                MaterialColor.TERRACOTTA_BLACK, 2f, 3f, SoundType.STONE, true));
        UNOWN_STONE_W = PokecubeLegends.DECORATION_TAB.register("unown_stone_w", () -> new BlockBase(Material.STONE,
                MaterialColor.TERRACOTTA_BLACK, 2f, 3f, SoundType.STONE, true));
        UNOWN_STONE_X = PokecubeLegends.DECORATION_TAB.register("unown_stone_x", () -> new BlockBase(Material.STONE,
                MaterialColor.TERRACOTTA_BLACK, 2f, 3f, SoundType.STONE, true));
        UNOWN_STONE_Y = PokecubeLegends.DECORATION_TAB.register("unown_stone_y", () -> new BlockBase(Material.STONE,
                MaterialColor.TERRACOTTA_BLACK, 2f, 3f, SoundType.STONE, true));
        UNOWN_STONE_Z = PokecubeLegends.DECORATION_TAB.register("unown_stone_z", () -> new BlockBase(Material.STONE,
                MaterialColor.TERRACOTTA_BLACK, 2f, 3f, SoundType.STONE, true));
        UNOWN_STONE_EX = PokecubeLegends.DECORATION_TAB.register("unown_stone_ex", () -> new BlockBase(Material.STONE,
                MaterialColor.TERRACOTTA_BLACK, 2f, 3f, SoundType.STONE, true));
        UNOWN_STONE_IN = PokecubeLegends.DECORATION_TAB.register("unown_stone_in", () -> new BlockBase(Material.STONE,
                MaterialColor.TERRACOTTA_BLACK, 2f, 3f, SoundType.STONE, true));

        // Glass
        MIRAGE_GLASS = PokecubeLegends.DECORATION_TAB.register("mirage_glass", () -> new MirageGlassBlock(
                DyeColor.LIGHT_BLUE, BlockBehaviour.Properties.of(Material.GLASS, MaterialColor.COLOR_LIGHT_BLUE)
                        .strength(0.3F).sound(SoundType.GLASS).noOcclusion().isValidSpawn((s, r, p, o) -> false)
                        .isRedstoneConductor((s, r, p) -> false).isSuffocating((s, r, p) -> false).isViewBlocking((s, r,
                                p) -> false)));
        SPECTRUM_GLASS = PokecubeLegends.DECORATION_TAB.register("spectrum_glass", () -> new SpectrumGlassBlock(
                DyeColor.ORANGE, BlockBehaviour.Properties.of(Material.GLASS, MaterialColor.COLOR_ORANGE).strength(0.3F)
                        .sound(SoundType.GLASS).noOcclusion().isValidSpawn((s, r, p, o) -> false).isRedstoneConductor((
                                s, r, p) -> false).isSuffocating((s, r, p) -> false).isViewBlocking((s, r,
                                        p) -> false)));
        FRAMED_DISTORTIC_MIRROR = PokecubeLegends.DECORATION_TAB.register("framed_distortic_mirror",
                () -> new OneWayStainedGlass(DyeColor.WHITE, BlockBehaviour.Properties.of(Material.GLASS,
                        MaterialColor.SNOW).noOcclusion().sound(SoundType.GLASS).strength(0.3f)
                        .requiresCorrectToolForDrops()));

        ONE_WAY_GLASS = PokecubeLegends.DECORATION_TAB.register("distortic_one_way_glass", () -> new OneWayGlass(
                BlockBehaviour.Properties.of(Material.GLASS, MaterialColor.SNOW).noOcclusion().sound(SoundType.GLASS)
                        .strength(0.3f).requiresCorrectToolForDrops()));
        ONE_WAY_GLASS_WHITE = PokecubeLegends.DECORATION_TAB.register("distortic_one_way_white_stained_glass",
                () -> new OneWayStainedGlass(DyeColor.WHITE, BlockBehaviour.Properties.of(Material.GLASS,
                        MaterialColor.SNOW).noOcclusion().sound(SoundType.GLASS).strength(0.3f)
                        .requiresCorrectToolForDrops()));
        ONE_WAY_GLASS_ORANGE = PokecubeLegends.DECORATION_TAB.register("distortic_one_way_orange_stained_glass",
                () -> new OneWayStainedGlass(DyeColor.ORANGE, BlockBehaviour.Properties.of(Material.GLASS,
                        MaterialColor.SNOW).noOcclusion().sound(SoundType.GLASS).strength(0.3f)
                        .requiresCorrectToolForDrops()));
        ONE_WAY_GLASS_MAGENTA = PokecubeLegends.DECORATION_TAB.register("distortic_one_way_magenta_stained_glass",
                () -> new OneWayStainedGlass(DyeColor.MAGENTA, BlockBehaviour.Properties.of(Material.GLASS,
                        MaterialColor.SNOW).noOcclusion().sound(SoundType.GLASS).strength(0.3f)
                        .requiresCorrectToolForDrops()));
        ONE_WAY_GLASS_LIGHT_BLUE = PokecubeLegends.DECORATION_TAB.register("distortic_one_way_light_blue_stained_glass",
                () -> new OneWayStainedGlass(DyeColor.LIGHT_BLUE, BlockBehaviour.Properties.of(Material.GLASS,
                        MaterialColor.SNOW).noOcclusion().sound(SoundType.GLASS).strength(0.3f)
                        .requiresCorrectToolForDrops()));
        ONE_WAY_GLASS_YELLOW = PokecubeLegends.DECORATION_TAB.register("distortic_one_way_yellow_stained_glass",
                () -> new OneWayStainedGlass(DyeColor.YELLOW, BlockBehaviour.Properties.of(Material.GLASS,
                        MaterialColor.SNOW).noOcclusion().sound(SoundType.GLASS).strength(0.3f)
                        .requiresCorrectToolForDrops()));
        ONE_WAY_GLASS_LIME = PokecubeLegends.DECORATION_TAB.register("distortic_one_way_lime_stained_glass",
                () -> new OneWayStainedGlass(DyeColor.LIME, BlockBehaviour.Properties.of(Material.GLASS,
                        MaterialColor.SNOW).noOcclusion().sound(SoundType.GLASS).strength(0.3f)
                        .requiresCorrectToolForDrops()));
        ONE_WAY_GLASS_PINK = PokecubeLegends.DECORATION_TAB.register("distortic_one_way_pink_stained_glass",
                () -> new OneWayStainedGlass(DyeColor.PINK, BlockBehaviour.Properties.of(Material.GLASS,
                        MaterialColor.SNOW).noOcclusion().sound(SoundType.GLASS).strength(0.3f)
                        .requiresCorrectToolForDrops()));
        ONE_WAY_GLASS_GRAY = PokecubeLegends.DECORATION_TAB.register("distortic_one_way_gray_stained_glass",
                () -> new OneWayStainedGlass(DyeColor.GRAY, BlockBehaviour.Properties.of(Material.GLASS,
                        MaterialColor.SNOW).noOcclusion().sound(SoundType.GLASS).strength(0.3f)
                        .requiresCorrectToolForDrops()));
        ONE_WAY_GLASS_LIGHT_GRAY = PokecubeLegends.DECORATION_TAB.register("distortic_one_way_light_gray_stained_glass",
                () -> new OneWayStainedGlass(DyeColor.LIGHT_GRAY, BlockBehaviour.Properties.of(Material.GLASS,
                        MaterialColor.SNOW).noOcclusion().sound(SoundType.GLASS).strength(0.3f)
                        .requiresCorrectToolForDrops()));
        ONE_WAY_GLASS_CYAN = PokecubeLegends.DECORATION_TAB.register("distortic_one_way_cyan_stained_glass",
                () -> new OneWayStainedGlass(DyeColor.CYAN, BlockBehaviour.Properties.of(Material.GLASS,
                        MaterialColor.SNOW).noOcclusion().sound(SoundType.GLASS).strength(0.3f)
                        .requiresCorrectToolForDrops()));
        ONE_WAY_GLASS_PURPLE = PokecubeLegends.DECORATION_TAB.register("distortic_one_way_purple_stained_glass",
                () -> new OneWayStainedGlass(DyeColor.PURPLE, BlockBehaviour.Properties.of(Material.GLASS,
                        MaterialColor.SNOW).noOcclusion().sound(SoundType.GLASS).strength(0.3f)
                        .requiresCorrectToolForDrops()));
        ONE_WAY_GLASS_BLUE = PokecubeLegends.DECORATION_TAB.register("distortic_one_way_blue_stained_glass",
                () -> new OneWayStainedGlass(DyeColor.BLUE, BlockBehaviour.Properties.of(Material.GLASS,
                        MaterialColor.SNOW).noOcclusion().sound(SoundType.GLASS).strength(0.3f)
                        .requiresCorrectToolForDrops()));
        ONE_WAY_GLASS_BROWN = PokecubeLegends.DECORATION_TAB.register("distortic_one_way_brown_stained_glass",
                () -> new OneWayStainedGlass(DyeColor.BROWN, BlockBehaviour.Properties.of(Material.GLASS,
                        MaterialColor.SNOW).noOcclusion().sound(SoundType.GLASS).strength(0.3f)
                        .requiresCorrectToolForDrops()));
        ONE_WAY_GLASS_GREEN = PokecubeLegends.DECORATION_TAB.register("distortic_one_way_green_stained_glass",
                () -> new OneWayStainedGlass(DyeColor.GREEN, BlockBehaviour.Properties.of(Material.GLASS,
                        MaterialColor.SNOW).noOcclusion().sound(SoundType.GLASS).strength(0.3f)
                        .requiresCorrectToolForDrops()));
        ONE_WAY_GLASS_RED = PokecubeLegends.DECORATION_TAB.register("distortic_one_way_red_stained_glass",
                () -> new OneWayStainedGlass(DyeColor.RED, BlockBehaviour.Properties.of(Material.GLASS,
                        MaterialColor.SNOW).noOcclusion().sound(SoundType.GLASS).strength(0.3f)
                        .requiresCorrectToolForDrops()));
        ONE_WAY_GLASS_BLACK = PokecubeLegends.DECORATION_TAB.register("distortic_one_way_black_stained_glass",
                () -> new OneWayStainedGlass(DyeColor.BLACK, BlockBehaviour.Properties.of(Material.GLASS,
                        MaterialColor.SNOW).noOcclusion().sound(SoundType.GLASS).strength(0.3f)
                        .requiresCorrectToolForDrops()));
        ONE_WAY_GLASS_LAB = PokecubeLegends.DECORATION_TAB.register("distortic_one_way_laboratory_glass",
                () -> new OneWayLaboratoryGlass(DyeColor.LIGHT_BLUE, BlockBehaviour.Properties.of(Material.GLASS,
                        MaterialColor.SNOW).noOcclusion().sound(SoundType.GLASS).strength(0.3f)
                        .requiresCorrectToolForDrops()));
        ONE_WAY_GLASS_MIRAGE = PokecubeLegends.DECORATION_TAB.register("distortic_one_way_mirage_glass",
                () -> new OneWayMirageGlass(DyeColor.LIGHT_BLUE, BlockBehaviour.Properties.of(Material.GLASS,
                        MaterialColor.SNOW).noOcclusion().sound(SoundType.GLASS).strength(0.3f)
                        .requiresCorrectToolForDrops()));
        ONE_WAY_GLASS_SPECTRUM = PokecubeLegends.DECORATION_TAB.register("distortic_one_way_spectrum_glass",
                () -> new OneWaySpectrumGlass(DyeColor.ORANGE, BlockBehaviour.Properties.of(Material.GLASS,
                        MaterialColor.SNOW).noOcclusion().sound(SoundType.GLASS).strength(0.3f)
                        .requiresCorrectToolForDrops()));
        ONE_WAY_FRAMED_MIRROR = PokecubeLegends.DECORATION_TAB.register("distortic_one_way_framed_mirror",
                () -> new OneWayStainedGlass(DyeColor.WHITE, BlockBehaviour.Properties.of(Material.GLASS,
                        MaterialColor.SNOW).noOcclusion().sound(SoundType.GLASS).strength(0.3f)
                        .requiresCorrectToolForDrops()));

        // Tapus Totems
        TOTEM_BLOCK = PokecubeLegends.DECORATION_TAB.register("totem_block", () -> new BlockBase(Material.STONE,
                MaterialColor.COLOR_LIGHT_GRAY, 1.5f, 10f, SoundType.STONE, true));

        // Koko Totem
        KOKO_WHITE = PokecubeLegends.DECORATION_TAB.register("koko_white_totem", () -> new KokoTotem(
                BlockBehaviour.Properties.of(Material.WOOD, MaterialColor.TERRACOTTA_WHITE).strength(5, 15).sound(
                        SoundType.WOOD).dynamicShape()));
        KOKO_ORANGE = PokecubeLegends.DECORATION_TAB.register("koko_orange_totem", () -> new KokoTotem(
                BlockBehaviour.Properties.of(Material.WOOD, MaterialColor.TERRACOTTA_ORANGE).strength(5, 15).sound(
                        SoundType.WOOD).dynamicShape()));
        KOKO_MAGENTA = PokecubeLegends.DECORATION_TAB.register("koko_magenta_totem", () -> new KokoTotem(
                BlockBehaviour.Properties.of(Material.WOOD, MaterialColor.TERRACOTTA_MAGENTA).strength(5, 15).sound(
                        SoundType.WOOD).dynamicShape()));
        KOKO_LIGHT_BLUE = PokecubeLegends.DECORATION_TAB.register("koko_lightblue_totem", () -> new KokoTotem(
                BlockBehaviour.Properties.of(Material.WOOD, MaterialColor.TERRACOTTA_LIGHT_BLUE).strength(5, 15).sound(
                        SoundType.WOOD).dynamicShape()));
        KOKO_YELLOW = PokecubeLegends.DECORATION_TAB.register("koko_yellow_totem", () -> new KokoTotem(
                BlockBehaviour.Properties.of(Material.WOOD, MaterialColor.TERRACOTTA_YELLOW).strength(5, 15).sound(
                        SoundType.WOOD).dynamicShape()));
        KOKO_LIME = PokecubeLegends.DECORATION_TAB.register("koko_lime_totem", () -> new KokoTotem(
                BlockBehaviour.Properties.of(Material.WOOD, MaterialColor.TERRACOTTA_LIGHT_GREEN).strength(5, 15).sound(
                        SoundType.WOOD).dynamicShape()));
        KOKO_PINK = PokecubeLegends.DECORATION_TAB.register("koko_pink_totem", () -> new KokoTotem(
                BlockBehaviour.Properties.of(Material.WOOD, MaterialColor.TERRACOTTA_PINK).strength(5, 15).sound(
                        SoundType.WOOD).dynamicShape()));
        KOKO_GRAY = PokecubeLegends.DECORATION_TAB.register("koko_gray_totem", () -> new KokoTotem(
                BlockBehaviour.Properties.of(Material.WOOD, MaterialColor.TERRACOTTA_GRAY).strength(5, 15).sound(
                        SoundType.WOOD).dynamicShape()));
        KOKO_LIGHT_GRAY = PokecubeLegends.DECORATION_TAB.register("koko_lightgray_totem", () -> new KokoTotem(
                BlockBehaviour.Properties.of(Material.WOOD, MaterialColor.TERRACOTTA_LIGHT_GRAY).strength(5, 15).sound(
                        SoundType.WOOD).dynamicShape()));
        KOKO_CYAN = PokecubeLegends.DECORATION_TAB.register("koko_cyan_totem", () -> new KokoTotem(
                BlockBehaviour.Properties.of(Material.WOOD, MaterialColor.TERRACOTTA_CYAN).strength(5, 15).sound(
                        SoundType.WOOD).dynamicShape()));
        KOKO_PURPLE = PokecubeLegends.DECORATION_TAB.register("koko_purple_totem", () -> new KokoTotem(
                BlockBehaviour.Properties.of(Material.WOOD, MaterialColor.TERRACOTTA_PURPLE).strength(5, 15).sound(
                        SoundType.WOOD).dynamicShape()));
        KOKO_BLUE = PokecubeLegends.DECORATION_TAB.register("koko_blue_totem", () -> new KokoTotem(
                BlockBehaviour.Properties.of(Material.WOOD, MaterialColor.TERRACOTTA_BLUE).strength(5, 15).sound(
                        SoundType.WOOD).dynamicShape()));
        KOKO_BROWN = PokecubeLegends.DECORATION_TAB.register("koko_brown_totem", () -> new KokoTotem(
                BlockBehaviour.Properties.of(Material.WOOD, MaterialColor.TERRACOTTA_BROWN).strength(5, 15).sound(
                        SoundType.WOOD).dynamicShape()));
        KOKO_GREEN = PokecubeLegends.DECORATION_TAB.register("koko_green_totem", () -> new KokoTotem(
                BlockBehaviour.Properties.of(Material.WOOD, MaterialColor.TERRACOTTA_GREEN).strength(5, 15).sound(
                        SoundType.WOOD).dynamicShape()));
        KOKO_RED = PokecubeLegends.DECORATION_TAB.register("koko_red_totem", () -> new KokoTotem(
                BlockBehaviour.Properties.of(Material.WOOD, MaterialColor.TERRACOTTA_RED).strength(5, 15).sound(
                        SoundType.WOOD).dynamicShape()));
        KOKO_BLACK = PokecubeLegends.DECORATION_TAB.register("koko_black_totem", () -> new KokoTotem(
                BlockBehaviour.Properties.of(Material.WOOD, MaterialColor.TERRACOTTA_BLACK).strength(5, 15).sound(
                        SoundType.WOOD).dynamicShape()));

        // Bulu Totem
        BULU_WHITE = PokecubeLegends.DECORATION_TAB.register("bulu_white_totem", () -> new BuluTotem(
                BlockBehaviour.Properties.of(Material.WOOD, MaterialColor.TERRACOTTA_WHITE).strength(5, 15).sound(
                        SoundType.WOOD).dynamicShape()));
        BULU_ORANGE = PokecubeLegends.DECORATION_TAB.register("bulu_orange_totem", () -> new BuluTotem(
                BlockBehaviour.Properties.of(Material.WOOD, MaterialColor.TERRACOTTA_ORANGE).strength(5, 15).sound(
                        SoundType.WOOD).dynamicShape()));
        BULU_MAGENTA = PokecubeLegends.DECORATION_TAB.register("bulu_magenta_totem", () -> new BuluTotem(
                BlockBehaviour.Properties.of(Material.WOOD, MaterialColor.TERRACOTTA_MAGENTA).strength(5, 15).sound(
                        SoundType.WOOD).dynamicShape()));
        BULU_LIGHT_BLUE = PokecubeLegends.DECORATION_TAB.register("bulu_lightblue_totem", () -> new BuluTotem(
                BlockBehaviour.Properties.of(Material.WOOD, MaterialColor.TERRACOTTA_LIGHT_BLUE).strength(5, 15).sound(
                        SoundType.WOOD).dynamicShape()));
        BULU_YELLOW = PokecubeLegends.DECORATION_TAB.register("bulu_yellow_totem", () -> new BuluTotem(
                BlockBehaviour.Properties.of(Material.WOOD, MaterialColor.TERRACOTTA_YELLOW).strength(5, 15).sound(
                        SoundType.WOOD).dynamicShape()));
        BULU_LIME = PokecubeLegends.DECORATION_TAB.register("bulu_lime_totem", () -> new BuluTotem(
                BlockBehaviour.Properties.of(Material.WOOD, MaterialColor.TERRACOTTA_LIGHT_GREEN).strength(5, 15).sound(
                        SoundType.WOOD).dynamicShape()));
        BULU_PINK = PokecubeLegends.DECORATION_TAB.register("bulu_pink_totem", () -> new BuluTotem(
                BlockBehaviour.Properties.of(Material.WOOD, MaterialColor.TERRACOTTA_PINK).strength(5, 15).sound(
                        SoundType.WOOD).dynamicShape()));
        BULU_GRAY = PokecubeLegends.DECORATION_TAB.register("bulu_gray_totem", () -> new BuluTotem(
                BlockBehaviour.Properties.of(Material.WOOD, MaterialColor.TERRACOTTA_GRAY).strength(5, 15).sound(
                        SoundType.WOOD).dynamicShape()));
        BULU_LIGHT_GRAY = PokecubeLegends.DECORATION_TAB.register("bulu_lightgray_totem", () -> new BuluTotem(
                BlockBehaviour.Properties.of(Material.WOOD, MaterialColor.TERRACOTTA_LIGHT_GRAY).strength(5, 15).sound(
                        SoundType.WOOD).dynamicShape()));
        BULU_CYAN = PokecubeLegends.DECORATION_TAB.register("bulu_cyan_totem", () -> new BuluTotem(
                BlockBehaviour.Properties.of(Material.WOOD, MaterialColor.TERRACOTTA_CYAN).strength(5, 15).sound(
                        SoundType.WOOD).dynamicShape()));
        BULU_PURPLE = PokecubeLegends.DECORATION_TAB.register("bulu_purple_totem", () -> new BuluTotem(
                BlockBehaviour.Properties.of(Material.WOOD, MaterialColor.TERRACOTTA_PURPLE).strength(5, 15).sound(
                        SoundType.WOOD).dynamicShape()));
        BULU_BLUE = PokecubeLegends.DECORATION_TAB.register("bulu_blue_totem", () -> new BuluTotem(
                BlockBehaviour.Properties.of(Material.WOOD, MaterialColor.TERRACOTTA_BLUE).strength(5, 15).sound(
                        SoundType.WOOD).dynamicShape()));
        BULU_BROWN = PokecubeLegends.DECORATION_TAB.register("bulu_brown_totem", () -> new BuluTotem(
                BlockBehaviour.Properties.of(Material.WOOD, MaterialColor.TERRACOTTA_BROWN).strength(5, 15).sound(
                        SoundType.WOOD).dynamicShape()));
        BULU_GREEN = PokecubeLegends.DECORATION_TAB.register("bulu_green_totem", () -> new BuluTotem(
                BlockBehaviour.Properties.of(Material.WOOD, MaterialColor.TERRACOTTA_GREEN).strength(5, 15).sound(
                        SoundType.WOOD).dynamicShape()));
        BULU_RED = PokecubeLegends.DECORATION_TAB.register("bulu_red_totem", () -> new BuluTotem(
                BlockBehaviour.Properties.of(Material.WOOD, MaterialColor.TERRACOTTA_RED).strength(5, 15).sound(
                        SoundType.WOOD).dynamicShape()));
        BULU_BLACK = PokecubeLegends.DECORATION_TAB.register("bulu_black_totem", () -> new BuluTotem(
                BlockBehaviour.Properties.of(Material.WOOD, MaterialColor.TERRACOTTA_BLACK).strength(5, 15).sound(
                        SoundType.WOOD).dynamicShape()));
        //

        // Lele Totem
        LELE_WHITE = PokecubeLegends.DECORATION_TAB.register("lele_white_totem", () -> new LeleTotem(
                BlockBehaviour.Properties.of(Material.WOOD, MaterialColor.TERRACOTTA_WHITE).strength(5, 15).sound(
                        SoundType.WOOD).dynamicShape()));
        LELE_ORANGE = PokecubeLegends.DECORATION_TAB.register("lele_orange_totem", () -> new LeleTotem(
                BlockBehaviour.Properties.of(Material.WOOD, MaterialColor.TERRACOTTA_ORANGE).strength(5, 15).sound(
                        SoundType.WOOD).dynamicShape()));
        LELE_MAGENTA = PokecubeLegends.DECORATION_TAB.register("lele_magenta_totem", () -> new LeleTotem(
                BlockBehaviour.Properties.of(Material.WOOD, MaterialColor.TERRACOTTA_MAGENTA).strength(5, 15).sound(
                        SoundType.WOOD).dynamicShape()));
        LELE_LIGHT_BLUE = PokecubeLegends.DECORATION_TAB.register("lele_lightblue_totem", () -> new LeleTotem(
                BlockBehaviour.Properties.of(Material.WOOD, MaterialColor.TERRACOTTA_LIGHT_BLUE).strength(5, 15).sound(
                        SoundType.WOOD).dynamicShape()));
        LELE_YELLOW = PokecubeLegends.DECORATION_TAB.register("lele_yellow_totem", () -> new LeleTotem(
                BlockBehaviour.Properties.of(Material.WOOD, MaterialColor.TERRACOTTA_YELLOW).strength(5, 15).sound(
                        SoundType.WOOD).dynamicShape()));
        LELE_LIME = PokecubeLegends.DECORATION_TAB.register("lele_lime_totem", () -> new LeleTotem(
                BlockBehaviour.Properties.of(Material.WOOD, MaterialColor.TERRACOTTA_LIGHT_GREEN).strength(5, 15).sound(
                        SoundType.WOOD).dynamicShape()));
        LELE_PINK = PokecubeLegends.DECORATION_TAB.register("lele_pink_totem", () -> new LeleTotem(
                BlockBehaviour.Properties.of(Material.WOOD, MaterialColor.TERRACOTTA_PINK).strength(5, 15).sound(
                        SoundType.WOOD).dynamicShape()));
        LELE_GRAY = PokecubeLegends.DECORATION_TAB.register("lele_gray_totem", () -> new LeleTotem(
                BlockBehaviour.Properties.of(Material.WOOD, MaterialColor.TERRACOTTA_GRAY).strength(5, 15).sound(
                        SoundType.WOOD).dynamicShape()));
        LELE_LIGHT_GRAY = PokecubeLegends.DECORATION_TAB.register("lele_lightgray_totem", () -> new LeleTotem(
                BlockBehaviour.Properties.of(Material.WOOD, MaterialColor.TERRACOTTA_LIGHT_GRAY).strength(5, 15).sound(
                        SoundType.WOOD).dynamicShape()));
        LELE_CYAN = PokecubeLegends.DECORATION_TAB.register("lele_cyan_totem", () -> new LeleTotem(
                BlockBehaviour.Properties.of(Material.WOOD, MaterialColor.TERRACOTTA_CYAN).strength(5, 15).sound(
                        SoundType.WOOD).dynamicShape()));
        LELE_PURPLE = PokecubeLegends.DECORATION_TAB.register("lele_purple_totem", () -> new LeleTotem(
                BlockBehaviour.Properties.of(Material.WOOD, MaterialColor.TERRACOTTA_PURPLE).strength(5, 15).sound(
                        SoundType.WOOD).dynamicShape()));
        LELE_BLUE = PokecubeLegends.DECORATION_TAB.register("lele_blue_totem", () -> new LeleTotem(
                BlockBehaviour.Properties.of(Material.WOOD, MaterialColor.TERRACOTTA_BLUE).strength(5, 15).sound(
                        SoundType.WOOD).dynamicShape()));
        LELE_BROWN = PokecubeLegends.DECORATION_TAB.register("lele_brown_totem", () -> new LeleTotem(
                BlockBehaviour.Properties.of(Material.WOOD, MaterialColor.TERRACOTTA_BROWN).strength(5, 15).sound(
                        SoundType.WOOD).dynamicShape()));
        LELE_GREEN = PokecubeLegends.DECORATION_TAB.register("lele_green_totem", () -> new LeleTotem(
                BlockBehaviour.Properties.of(Material.WOOD, MaterialColor.TERRACOTTA_GREEN).strength(5, 15).sound(
                        SoundType.WOOD).dynamicShape()));
        LELE_RED = PokecubeLegends.DECORATION_TAB.register("lele_red_totem", () -> new LeleTotem(
                BlockBehaviour.Properties.of(Material.WOOD, MaterialColor.TERRACOTTA_RED).strength(5, 15).sound(
                        SoundType.WOOD).dynamicShape()));
        LELE_BLACK = PokecubeLegends.DECORATION_TAB.register("lele_black_totem", () -> new LeleTotem(
                BlockBehaviour.Properties.of(Material.WOOD, MaterialColor.TERRACOTTA_BLACK).strength(5, 15).sound(
                        SoundType.WOOD).dynamicShape()));

        // Fini Totem
        FINI_WHITE = PokecubeLegends.DECORATION_TAB.register("fini_white_totem", () -> new FiniTotem(
                BlockBehaviour.Properties.of(Material.WOOD, MaterialColor.TERRACOTTA_WHITE).strength(5, 15).sound(
                        SoundType.WOOD).dynamicShape()));
        FINI_ORANGE = PokecubeLegends.DECORATION_TAB.register("fini_orange_totem", () -> new FiniTotem(
                BlockBehaviour.Properties.of(Material.WOOD, MaterialColor.TERRACOTTA_ORANGE).strength(5, 15).sound(
                        SoundType.WOOD).dynamicShape()));
        FINI_MAGENTA = PokecubeLegends.DECORATION_TAB.register("fini_magenta_totem", () -> new FiniTotem(
                BlockBehaviour.Properties.of(Material.WOOD, MaterialColor.TERRACOTTA_MAGENTA).strength(5, 15).sound(
                        SoundType.WOOD).dynamicShape()));
        FINI_LIGHT_BLUE = PokecubeLegends.DECORATION_TAB.register("fini_lightblue_totem", () -> new FiniTotem(
                BlockBehaviour.Properties.of(Material.WOOD, MaterialColor.TERRACOTTA_LIGHT_BLUE).strength(5, 15).sound(
                        SoundType.WOOD).dynamicShape()));
        FINI_YELLOW = PokecubeLegends.DECORATION_TAB.register("fini_yellow_totem", () -> new FiniTotem(
                BlockBehaviour.Properties.of(Material.WOOD, MaterialColor.TERRACOTTA_YELLOW).strength(5, 15).sound(
                        SoundType.WOOD).dynamicShape()));
        FINI_LIME = PokecubeLegends.DECORATION_TAB.register("fini_lime_totem", () -> new FiniTotem(
                BlockBehaviour.Properties.of(Material.WOOD, MaterialColor.TERRACOTTA_LIGHT_GREEN).strength(5, 15).sound(
                        SoundType.WOOD).dynamicShape()));
        FINI_PINK = PokecubeLegends.DECORATION_TAB.register("fini_pink_totem", () -> new FiniTotem(
                BlockBehaviour.Properties.of(Material.WOOD, MaterialColor.TERRACOTTA_PINK).strength(5, 15).sound(
                        SoundType.WOOD).dynamicShape()));
        FINI_GRAY = PokecubeLegends.DECORATION_TAB.register("fini_gray_totem", () -> new FiniTotem(
                BlockBehaviour.Properties.of(Material.WOOD, MaterialColor.TERRACOTTA_GRAY).strength(5, 15).sound(
                        SoundType.WOOD).dynamicShape()));
        FINI_LIGHT_GRAY = PokecubeLegends.DECORATION_TAB.register("fini_lightgray_totem", () -> new FiniTotem(
                BlockBehaviour.Properties.of(Material.WOOD, MaterialColor.TERRACOTTA_LIGHT_GRAY).strength(5, 15).sound(
                        SoundType.WOOD).dynamicShape()));
        FINI_CYAN = PokecubeLegends.DECORATION_TAB.register("fini_cyan_totem", () -> new FiniTotem(
                BlockBehaviour.Properties.of(Material.WOOD, MaterialColor.TERRACOTTA_CYAN).strength(5, 15).sound(
                        SoundType.WOOD).dynamicShape()));
        FINI_PURPLE = PokecubeLegends.DECORATION_TAB.register("fini_purple_totem", () -> new FiniTotem(
                BlockBehaviour.Properties.of(Material.WOOD, MaterialColor.TERRACOTTA_PURPLE).strength(5, 15).sound(
                        SoundType.WOOD).dynamicShape()));
        FINI_BLUE = PokecubeLegends.DECORATION_TAB.register("fini_blue_totem", () -> new FiniTotem(
                BlockBehaviour.Properties.of(Material.WOOD, MaterialColor.TERRACOTTA_BLUE).strength(5, 15).sound(
                        SoundType.WOOD).dynamicShape()));
        FINI_BROWN = PokecubeLegends.DECORATION_TAB.register("fini_brown_totem", () -> new FiniTotem(
                BlockBehaviour.Properties.of(Material.WOOD, MaterialColor.TERRACOTTA_BROWN).strength(5, 15).sound(
                        SoundType.WOOD).dynamicShape()));
        FINI_GREEN = PokecubeLegends.DECORATION_TAB.register("fini_green_totem", () -> new FiniTotem(
                BlockBehaviour.Properties.of(Material.WOOD, MaterialColor.TERRACOTTA_GREEN).strength(5, 15).sound(
                        SoundType.WOOD).dynamicShape()));
        FINI_RED = PokecubeLegends.DECORATION_TAB.register("fini_red_totem", () -> new FiniTotem(
                BlockBehaviour.Properties.of(Material.WOOD, MaterialColor.TERRACOTTA_RED).strength(5, 15).sound(
                        SoundType.WOOD).dynamicShape()));
        FINI_BLACK = PokecubeLegends.DECORATION_TAB.register("fini_black_totem", () -> new FiniTotem(
                BlockBehaviour.Properties.of(Material.WOOD, MaterialColor.TERRACOTTA_BLACK).strength(5, 15).sound(
                        SoundType.WOOD).dynamicShape()));

        // Pokecube Blocks Creative Tab - Sorting depends on the order the blocks are listed in
        // Block Raid
        RAID_SPAWNER = PokecubeLegends.POKECUBE_BLOCKS_TAB.register("raid_spot_spawner", () -> new RaidSpawnBlock(
                BlockBehaviour.Properties.of(Material.STONE, MaterialColor.COLOR_RED).randomTicks().strength(2000,
                        2000).sound(SoundType.METAL)).setInfoBlockName("raid_spawner"));
        CRAMOMATIC_BLOCK = PokecubeLegends.POKECUBE_BLOCKS_TAB.register("cramomatic_block", () -> new CramomaticBlock(
                BlockBehaviour.Properties.of(Material.METAL, MaterialColor.TERRACOTTA_RED).strength(6, 15).sound(SoundType.ANVIL)
                .dynamicShape().requiresCorrectToolForDrops()).setToolTip("cramobot"));

        // Mirage Spot (Hoopa Ring)
        PORTAL = PokecubeLegends.POKECUBE_BLOCKS_TAB.register("mirage_spot_block", () -> new PortalWarp("mirage_spot_block", BlockBehaviour.Properties.of(
                Material.STONE, MaterialColor.GOLD).sound(SoundType.METAL).strength(2000, 2000)).setShape(Shapes.box(
                        0.05, 0, 0.05, 1, 3, 1)).setToolTip("portalwarp"));

        // Legendary Spawners
        // Regi Cores
        GOLEM_STONE = PokecubeLegends.POKECUBE_BLOCKS_TAB.register("golem_stone", () -> new BlockBase(Material.STONE,
                MaterialColor.TERRACOTTA_WHITE, 5f, 10f, SoundType.STONE, true));
        REGISTEEL_CORE = PokecubeLegends.POKECUBE_BLOCKS_TAB.register("registeel_spawn", () -> new FaceBlock_Base(Material.METAL,
                MaterialColor.TERRACOTTA_WHITE, 15, 10f, SoundType.METAL, true));
        REGICE_CORE = PokecubeLegends.POKECUBE_BLOCKS_TAB.register("regice_spawn", () -> new FaceBlock_Base(Material.ICE_SOLID,
                MaterialColor.TERRACOTTA_WHITE, 15, 10f, SoundType.GLASS, true));
        REGIROCK_CORE = PokecubeLegends.POKECUBE_BLOCKS_TAB.register("regirock_spawn", () -> new FaceBlock_Base(Material.STONE,
                MaterialColor.TERRACOTTA_WHITE, 15, 10f, SoundType.STONE, true));
        REGIELEKI_CORE = PokecubeLegends.POKECUBE_BLOCKS_TAB.register("regieleki_spawn", () -> new FaceBlock_Base(Material.STONE,
                MaterialColor.TERRACOTTA_WHITE, 15, 10f, SoundType.STONE, true));
        REGIDRAGO_CORE = PokecubeLegends.POKECUBE_BLOCKS_TAB.register("regidrago_spawn", () -> new FaceBlock_Base(Material.CLAY,
                MaterialColor.TERRACOTTA_WHITE, 15, 10f, SoundType.STONE, true));
        REGIGIGA_CORE = PokecubeLegends.POKECUBE_BLOCKS_TAB.register("regigiga_spawn", () -> new FaceBlock_Base(Material.HEAVY_METAL,
                MaterialColor.TERRACOTTA_WHITE, 15, 10f, SoundType.METAL, true));

        LEGENDARY_SPAWN = PokecubeLegends.POKECUBE_BLOCKS_TAB.register("legendary_spawn", () -> new BlockBase(Material.METAL,
                MaterialColor.GOLD, 50f, 30f, SoundType.METAL, true));

        HEATRAN_BLOCK = PokecubeLegends.POKECUBE_BLOCKS_TAB.register("heatran_spawn", () -> new HeatranBlock(
                BlockBehaviour.Properties.of(Material.STONE, MaterialColor.NETHER).strength(5, 15).sound(
                        SoundType.NETHER_BRICKS).lightLevel(b -> 4).dynamicShape().emissiveRendering((s, r, p) -> true)
                        .requiresCorrectToolForDrops()));

        MAGEARNA_BLOCK = PokecubeLegends.POKECUBE_BLOCKS_TAB.register("magearna_spawn", () -> new MagearnaBlock(
                BlockBehaviour.Properties.of(Material.STONE, MaterialColor.SAND).strength(300, 300).sound(
                        SoundType.STONE).requiresCorrectToolForDrops().dynamicShape()));

        // Tapus
        TAPU_KOKO_CORE = PokecubeLegends.POKECUBE_BLOCKS_TAB.register("koko_core", () -> new TapuKokoCore(BlockBehaviour.Properties
                .of(Material.STONE, MaterialColor.TERRACOTTA_YELLOW).strength(5, 15).sound(SoundType.BASALT)
                .dynamicShape().requiresCorrectToolForDrops()));
        TAPU_BULU_CORE = PokecubeLegends.POKECUBE_BLOCKS_TAB.register("bulu_core", () -> new TapuBuluCore(BlockBehaviour.Properties
                .of(Material.STONE, MaterialColor.TERRACOTTA_RED).strength(5, 15).sound(SoundType.BASALT).dynamicShape()
                .requiresCorrectToolForDrops()));
        TAPU_LELE_CORE = PokecubeLegends.POKECUBE_BLOCKS_TAB.register("lele_core", () -> new TapuLeleCore(BlockBehaviour.Properties
                .of(Material.STONE, MaterialColor.TERRACOTTA_PURPLE).strength(5, 15).sound(SoundType.BASALT)
                .dynamicShape().requiresCorrectToolForDrops()));
        TAPU_FINI_CORE = PokecubeLegends.POKECUBE_BLOCKS_TAB.register("fini_core", () -> new TapuFiniCore(BlockBehaviour.Properties
                .of(Material.STONE, MaterialColor.TERRACOTTA_PINK).strength(5, 15).sound(SoundType.BASALT)
                .dynamicShape().requiresCorrectToolForDrops()));

        YVELTAL_CORE = PokecubeLegends.POKECUBE_BLOCKS_TAB.register("yveltal_spawn", () -> new YveltalEgg(BlockBehaviour.Properties.of(
                Material.METAL, MaterialColor.COLOR_BLACK).strength(2000, 2000).sound(SoundType.WOOD).dynamicShape())
                        .setShape(Shapes.box(0.05, 0, 0.05, 1, 2, 1)));

        KELDEO_CORE = PokecubeLegends.POKECUBE_BLOCKS_TAB.register("keldeo_spawn", () -> new KeldeoBlock(BlockBehaviour.Properties.of(
                Material.STONE, MaterialColor.COLOR_BLUE).strength(2000, 2000).sound(SoundType.STONE).dynamicShape())
                        .setShape(Shapes.box(0.05, 0, 0.05, 1, 1, 1)));

        TIMESPACE_CORE = PokecubeLegends.POKECUBE_BLOCKS_TAB.register("timerspace_spawn", () -> new TimeSpaceCoreBlock(
                BlockBehaviour.Properties.of(Material.GRASS, MaterialColor.STONE).strength(2000, 2000).sound(
                        SoundType.STONE).dynamicShape()).setShape(Shapes.box(0.05, 0, 0.05, 1, 2, 1)));

        NATURE_CORE = PokecubeLegends.POKECUBE_BLOCKS_TAB.register("nature_spawn", () -> new NatureCoreBlock(BlockBehaviour.Properties
                .of(Material.STONE, MaterialColor.TERRACOTTA_WHITE).strength(2000, 2000).sound(SoundType.STONE)
                .dynamicShape()).setShape(Shapes.box(0.05, 0, 0.05, 1, 2, 1)));

        XERNEAS_CORE = PokecubeLegends.POKECUBE_BLOCKS_TAB.register("xerneas_spawn", () -> new XerneasCore(BlockBehaviour.Properties
                .of(Material.METAL, MaterialColor.SNOW).strength(2000, 2000).sound(SoundType.WOOD).dynamicShape())
                        .setShape(Shapes.box(0.05, 0, 0.05, 1, 2, 1)));

        TAO_BLOCK = PokecubeLegends.POKECUBE_BLOCKS_TAB.register("blackwhite_spawn", () -> new TaoTrioBlock(BlockBehaviour.Properties
                .of(Material.HEAVY_METAL, MaterialColor.SNOW).strength(5, 15).sound(SoundType.FUNGUS).dynamicShape())
                        .setShape(Shapes.box(0.05, 0, 0.05, 1, 1, 1)));

        TROUGH_BLOCK = PokecubeLegends.POKECUBE_BLOCKS_TAB.register("trough_spawn", () -> new TroughBlock(BlockBehaviour.Properties
                .of(Material.METAL, MaterialColor.COLOR_BROWN).strength(5, 15).sound(SoundType.ANVIL).lightLevel(b -> 4)
                .dynamicShape().requiresCorrectToolForDrops()));

        VICTINI_CORE = PokecubeLegends.POKECUBE_BLOCKS_TAB.register("victini_spawn", () -> new VictiniBlock(BlockBehaviour.Properties
                .of(Material.METAL, MaterialColor.GOLD).strength(5, 15).sound(SoundType.ANVIL).dynamicShape()
                .requiresCorrectToolForDrops()).setShape(Shapes.box(0.05, 0, 0.05, 1, 1, 1)));

        // No Tab
        POTTED_AGED_SAPLING = PokecubeLegends.NO_TAB.register("potted_aged_sapling",
                () -> new ItemGenerator.GenericPottedPlant(BlockInit.AGED_SAPLING.get(), BlockBehaviour.Properties.of(
                        Material.DECORATION).instabreak().noOcclusion()));
        POTTED_CORRUPTED_SAPLING = PokecubeLegends.NO_TAB.register("potted_corrupted_sapling",
                () -> new ItemGenerator.GenericPottedPlant(BlockInit.CORRUPTED_SAPLING.get(), BlockBehaviour.Properties
                        .of(Material.DECORATION).instabreak().noOcclusion()));
        POTTED_DISTORTIC_SAPLING = PokecubeLegends.NO_TAB.register("potted_distortic_sapling",
                () -> new ItemGenerator.GenericPottedPlant(BlockInit.DISTORTIC_SAPLING.get(), BlockBehaviour.Properties
                        .of(Material.DECORATION).instabreak().noOcclusion()));
        POTTED_INVERTED_SAPLING = PokecubeLegends.NO_TAB.register("potted_inverted_sapling",
                () -> new ItemGenerator.GenericPottedPlant(BlockInit.INVERTED_SAPLING.get(), BlockBehaviour.Properties
                        .of(Material.DECORATION).instabreak().noOcclusion()));
        POTTED_MIRAGE_SAPLING = PokecubeLegends.NO_TAB.register("potted_mirage_sapling",
                () -> new ItemGenerator.GenericPottedPlant(BlockInit.MIRAGE_SAPLING.get(), BlockBehaviour.Properties.of(
                        Material.DECORATION).instabreak().noOcclusion()));
        POTTED_TEMPORAL_SAPLING = PokecubeLegends.NO_TAB.register("potted_temporal_sapling",
                () -> new ItemGenerator.GenericPottedPlant(BlockInit.TEMPORAL_SAPLING.get(), BlockBehaviour.Properties
                        .of(Material.DECORATION).instabreak().noOcclusion()));

        POTTED_COMPRECED_MUSHROOM = PokecubeLegends.NO_TAB.register("potted_compreced_mushroom",
                () -> new ItemGenerator.GenericPottedPlant(PlantsInit.COMPRECED_MUSHROOM.get(),
                        BlockBehaviour.Properties.of(Material.DECORATION).instabreak().noOcclusion()));
        POTTED_CRYSTALLIZED_BUSH = PokecubeLegends.NO_TAB.register("potted_crystallized_bush",
                () -> new PottedCrystallizedBush(BlockInit.CRYSTALLIZED_BUSH.get(), BlockBehaviour.Properties.of(
                        Material.DECORATION).instabreak().noOcclusion()));
        POTTED_CRYSTALLIZED_CACTUS = PokecubeLegends.NO_TAB.register("potted_crystallized_cactus",
                () -> new PottedCrystallizedCactus(BlockInit.CRYSTALLIZED_CACTUS.get(), BlockBehaviour.Properties.of(
                        Material.DECORATION).instabreak().noOcclusion()));
        POTTED_DISTORCED_MUSHROOM = PokecubeLegends.NO_TAB.register("potted_distorced_mushroom",
                () -> new ItemGenerator.GenericPottedPlant(PlantsInit.DISTORCED_MUSHROOM.get(),
                        BlockBehaviour.Properties.of(Material.DECORATION).instabreak().noOcclusion()));
        POTTED_DISTORTIC_VINES = PokecubeLegends.NO_TAB.register("potted_distortic_vines",
                () -> new ItemGenerator.GenericPottedPlant(PlantsInit.DISTORTIC_VINES.get(), BlockBehaviour.Properties
                        .of(Material.DECORATION).instabreak().noOcclusion()));
        POTTED_GOLDEN_FERN= PokecubeLegends.NO_TAB.register("potted_golden_fern",
                () -> new ItemGenerator.GenericPottedPlant(PlantsInit.GOLDEN_FERN.get(), BlockBehaviour.Properties.of(
                        Material.DECORATION).instabreak().noOcclusion()));
        POTTED_GOLDEN_GRASS= PokecubeLegends.NO_TAB.register("potted_golden_grass",
                () -> new ItemGenerator.GenericPottedPlant(PlantsInit.GOLDEN_GRASS.get(), BlockBehaviour.Properties.of(
                        Material.DECORATION).instabreak().noOcclusion()));
        POTTED_GOLDEN_POPPY = PokecubeLegends.NO_TAB.register("potted_golden_poppy",
                () -> new ItemGenerator.GenericPottedPlant(PlantsInit.GOLDEN_POPPY.get(), BlockBehaviour.Properties.of(
                        Material.DECORATION).instabreak().noOcclusion()));
        POTTED_GOLDEN_SWEET_BERRY_BUSH = PokecubeLegends.NO_TAB.register("potted_golden_sweet_berry_bush",
                () -> new ItemGenerator.GenericPottedPlant(PlantsInit.GOLDEN_SWEET_BERRY_BUSH.get(), BlockBehaviour.Properties.of(
                        Material.DECORATION).instabreak().noOcclusion()));
        POTTED_INVERTED_ORCHID = PokecubeLegends.NO_TAB.register("potted_inverted_orchid",
                () -> new ItemGenerator.GenericPottedPlant(PlantsInit.INVERTED_ORCHID.get(), BlockBehaviour.Properties
                        .of(Material.DECORATION).instabreak().noOcclusion()));
        POTTED_LARGE_GOLDEN_FERN= PokecubeLegends.NO_TAB.register("potted_large_golden_fern",
                () -> new ItemGenerator.GenericPottedPlant(PlantsInit.LARGE_GOLDEN_FERN.get(), BlockBehaviour.Properties.of(
                        Material.DECORATION).instabreak().noOcclusion()));
        POTTED_PINK_LILY= PokecubeLegends.NO_TAB.register("potted_pink_blossom_lily",
                () -> new ItemGenerator.GenericPottedPlant(PlantsInit.PINK_TAINTED_LILY_PAD.get(), BlockBehaviour.Properties
                        .of(Material.DECORATION).instabreak().noOcclusion()));
        POTTED_TALL_CRYSTALLIZED_BUSH = PokecubeLegends.NO_TAB.register("potted_tall_crystallized_bush",
                () -> new PottedCrystallizedBush(BlockInit.TALL_CRYSTALLIZED_BUSH.get(), BlockBehaviour.Properties.of(
                        Material.DECORATION).instabreak().noOcclusion()));
        POTTED_TALL_GOLDEN_GRASS= PokecubeLegends.NO_TAB.register("potted_tall_golden_grass",
                () -> new ItemGenerator.GenericPottedPlant(PlantsInit.TALL_GOLDEN_GRASS.get(), BlockBehaviour.Properties.of(
                        Material.DECORATION).instabreak().noOcclusion()));
        POTTED_TEMPORAL_BAMBOO = PokecubeLegends.NO_TAB.register("potted_temporal_bamboo",
                () -> new ItemGenerator.GenericPottedPlant(PlantsInit.TEMPORAL_BAMBOO.get(), BlockBehaviour.Properties.of(
                        Material.DECORATION).instabreak().noOcclusion()));
    }

    private static ToIntFunction<BlockState> litBlockEmission(final int i)
    {
        return (state) ->
        {
            return state.getValue(BlockStateProperties.LIT) ? i : 0;
        };
    }

    public static void init()
    {
        PlantsInit.registry();

        for (final RegistryObject<Block> reg : PokecubeLegends.POKECUBE_BLOCKS_TAB.getEntries())
            PokecubeLegends.ITEMS.register(reg.getId().getPath(), () -> new BlockItem(reg.get(), new Item.Properties()
                    .tab(PokecubeItems.TAB_BLOCKS)));

        for (final RegistryObject<Block> reg : PokecubeLegends.DIMENSIONS_TAB.getEntries())
        {
            // These are registered separately, so skip them.
            if (reg == PlantsInit.DISTORTIC_VINES_PLANT || reg == PlantsInit.DISTORTIC_VINES ||
                    reg == PlantsInit.TEMPORAL_BAMBOO || reg == PlantsInit.TEMPORAL_BAMBOO_SHOOT ||
                    reg == PlantsInit.PINK_TAINTED_LILY_PAD || reg == PlantsInit.TAINTED_LILY_PAD) continue;
            PokecubeLegends.ITEMS.register(reg.getId().getPath(), () -> new BlockItem(reg.get(), new Item.Properties()
                    .tab(PokecubeLegends.TAB_DIMENSIONS)));
        }

        for (final RegistryObject<Block> reg : PokecubeLegends.DECORATION_TAB.getEntries())
        {
            if (reg == BlockInit.INFECTED_TORCH || reg == BlockInit.INFECTED_TORCH_WALL) continue;
            PokecubeLegends.ITEMS.register(reg.getId().getPath(), () -> new BlockItem(reg.get(), new Item.Properties()
                    .tab(PokecubeLegends.TAB_DECORATIONS)));
        }
    }

    public static RotatedPillarBlock concreteLog(final MaterialColor color1, final MaterialColor color2)
    {
        return new RotatedPillarBlock(BlockBehaviour.Properties.of(Material.STONE, (state) ->
        {
            return state.getValue(RotatedPillarBlock.AXIS) == Direction.Axis.Y ? color1 : color2;
        }).strength(10.0f, 500.0f).sound(SoundType.STONE).requiresCorrectToolForDrops());
    }

    public static void strippableBlocks(final FMLLoadCompleteEvent event)
    {
        // Enqueue this so that it runs on main thread, to prevent concurrency
        // issues.
        event.enqueueWork(() ->
        {
            ItemGenerator.addStrippable(BlockInit.AGED_LOG.get(), BlockInit.STRIP_AGED_LOG.get());
            ItemGenerator.addStrippable(BlockInit.AGED_WOOD.get(), BlockInit.STRIP_AGED_WOOD.get());
            ItemGenerator.addStrippable(BlockInit.CONCRETE_LOG.get(), BlockInit.STRIP_CONCRETE_LOG.get());
            ItemGenerator.addStrippable(BlockInit.CONCRETE_WOOD.get(), BlockInit.STRIP_CONCRETE_WOOD.get());
            ItemGenerator.addStrippable(BlockInit.CORRUPTED_LOG.get(), BlockInit.STRIP_CORRUPTED_LOG.get());
            ItemGenerator.addStrippable(BlockInit.CORRUPTED_WOOD.get(), BlockInit.STRIP_CORRUPTED_WOOD.get());
            ItemGenerator.addStrippable(BlockInit.DISTORTIC_LOG.get(), BlockInit.STRIP_DISTORTIC_LOG.get());
            ItemGenerator.addStrippable(BlockInit.DISTORTIC_WOOD.get(), BlockInit.STRIP_DISTORTIC_WOOD.get());
            ItemGenerator.addStrippable(BlockInit.INVERTED_LOG.get(), BlockInit.STRIP_INVERTED_LOG.get());
            ItemGenerator.addStrippable(BlockInit.INVERTED_WOOD.get(), BlockInit.STRIP_INVERTED_WOOD.get());
            ItemGenerator.addStrippable(BlockInit.MIRAGE_LOG.get(), BlockInit.STRIP_MIRAGE_LOG.get());
            ItemGenerator.addStrippable(BlockInit.MIRAGE_WOOD.get(), BlockInit.STRIP_MIRAGE_WOOD.get());
            ItemGenerator.addStrippable(BlockInit.TEMPORAL_LOG.get(), BlockInit.STRIP_TEMPORAL_LOG.get());
            ItemGenerator.addStrippable(BlockInit.TEMPORAL_WOOD.get(), BlockInit.STRIP_TEMPORAL_WOOD.get());
        });
    }

    public static void compostableBlocks(final float chance, final RegistryObject<Block> item)
    {
        ComposterBlock.COMPOSTABLES.put(item.get().asItem(), chance);
    }

    public static void compostables()
    {
        BlockInit.compostableBlocks(0.3f, BlockInit.AGED_LEAVES);
        BlockInit.compostableBlocks(0.3f, BlockInit.AGED_SAPLING);
        BlockInit.compostableBlocks(0.3f, BlockInit.CORRUPTED_LEAVES);
        BlockInit.compostableBlocks(0.3f, BlockInit.CORRUPTED_SAPLING);
        BlockInit.compostableBlocks(0.3f, BlockInit.DISTORTIC_LEAVES);
        BlockInit.compostableBlocks(0.3f, BlockInit.DISTORTIC_SAPLING);
        BlockInit.compostableBlocks(0.3f, BlockInit.DYNA_LEAVES_PINK);
        BlockInit.compostableBlocks(0.3f, BlockInit.DYNA_LEAVES_RED);
        BlockInit.compostableBlocks(0.3f, BlockInit.INVERTED_LEAVES);
        BlockInit.compostableBlocks(0.3f, BlockInit.INVERTED_SAPLING);
        BlockInit.compostableBlocks(0.3f, BlockInit.MIRAGE_LEAVES);
        BlockInit.compostableBlocks(0.3f, BlockInit.MIRAGE_SAPLING);
        BlockInit.compostableBlocks(0.3f, BlockInit.TEMPORAL_SAPLING);
        BlockInit.compostableBlocks(0.3f, PlantsInit.GOLDEN_GRASS);
        BlockInit.compostableBlocks(0.3f, PlantsInit.GOLDEN_SWEET_BERRY_BUSH);
        BlockInit.compostableBlocks(0.3f, PlantsInit.TAINTED_KELP);
        BlockInit.compostableBlocks(0.3f, PlantsInit.TAINTED_SEAGRASS);

        BlockInit.compostableBlocks(0.5f, PlantsInit.TALL_GOLDEN_GRASS);

        BlockInit.compostableBlocks(0.65f, PlantsInit.COMPRECED_MUSHROOM);
        BlockInit.compostableBlocks(0.65f, PlantsInit.DISTORCED_MUSHROOM);
        BlockInit.compostableBlocks(0.65f, PlantsInit.GOLDEN_FERN);
        BlockInit.compostableBlocks(0.65f, PlantsInit.GOLDEN_POPPY);
        BlockInit.compostableBlocks(0.65f, PlantsInit.INVERTED_ORCHID);
        BlockInit.compostableBlocks(0.65f, PlantsInit.LARGE_GOLDEN_FERN);
        BlockInit.compostableBlocks(0.65f, PlantsInit.PINK_TAINTED_LILY_PAD);
        BlockInit.compostableBlocks(0.65f, PlantsInit.TAINTED_LILY_PAD);
        BlockInit.compostableBlocks(0.65f, PlantsInit.TALL_TAINTED_SEAGRASS);
        BlockInit.compostableBlocks(0.65f, PlantsInit.TEMPORAL_BAMBOO);

        BlockInit.compostableBlocks(0.75f, BlockInit.CRYSTALLIZED_CACTUS);
    }

    public static void flammableBlocks(final Block block, final int speed, final int flammability)
    {
        final FireBlock fire = (FireBlock) Blocks.FIRE;
        fire.setFlammable(block, speed, flammability);
    }

    public static void flammables()
    {
        // Logs
        BlockInit.flammableBlocks(BlockInit.AGED_LOG.get(), 5, 5);
        BlockInit.flammableBlocks(BlockInit.AGED_WOOD.get(), 5, 5);
        BlockInit.flammableBlocks(BlockInit.CORRUPTED_LOG.get(), 5, 5);
        BlockInit.flammableBlocks(BlockInit.CORRUPTED_WOOD.get(), 5, 5);
        BlockInit.flammableBlocks(BlockInit.DISTORTIC_LOG.get(), 5, 5);
        BlockInit.flammableBlocks(BlockInit.DISTORTIC_WOOD.get(), 5, 5);
        BlockInit.flammableBlocks(BlockInit.INVERTED_LOG.get(), 5, 5);
        BlockInit.flammableBlocks(BlockInit.INVERTED_WOOD.get(), 5, 5);
        BlockInit.flammableBlocks(BlockInit.MIRAGE_LOG.get(), 5, 5);
        BlockInit.flammableBlocks(BlockInit.MIRAGE_WOOD.get(), 5, 5);
        BlockInit.flammableBlocks(BlockInit.TEMPORAL_LOG.get(), 5, 5);
        BlockInit.flammableBlocks(BlockInit.TEMPORAL_WOOD.get(), 5, 5);

        // Stripped Logs
        BlockInit.flammableBlocks(BlockInit.STRIP_AGED_LOG.get(), 5, 5);
        BlockInit.flammableBlocks(BlockInit.STRIP_AGED_WOOD.get(), 5, 5);
        BlockInit.flammableBlocks(BlockInit.STRIP_CORRUPTED_LOG.get(), 5, 5);
        BlockInit.flammableBlocks(BlockInit.STRIP_CORRUPTED_WOOD.get(), 5, 5);
        BlockInit.flammableBlocks(BlockInit.STRIP_DISTORTIC_LOG.get(), 5, 5);
        BlockInit.flammableBlocks(BlockInit.STRIP_DISTORTIC_WOOD.get(), 5, 5);
        BlockInit.flammableBlocks(BlockInit.STRIP_INVERTED_LOG.get(), 5, 5);
        BlockInit.flammableBlocks(BlockInit.STRIP_INVERTED_WOOD.get(), 5, 5);
        BlockInit.flammableBlocks(BlockInit.STRIP_MIRAGE_LOG.get(), 5, 5);
        BlockInit.flammableBlocks(BlockInit.STRIP_MIRAGE_WOOD.get(), 5, 5);
        BlockInit.flammableBlocks(BlockInit.STRIP_TEMPORAL_LOG.get(), 5, 5);
        BlockInit.flammableBlocks(BlockInit.STRIP_TEMPORAL_WOOD.get(), 5, 5);

        // Leaves
        BlockInit.flammableBlocks(BlockInit.AGED_LEAVES.get(), 30, 60);
        BlockInit.flammableBlocks(BlockInit.CORRUPTED_LEAVES.get(), 30, 60);
        BlockInit.flammableBlocks(BlockInit.DISTORTIC_LEAVES.get(), 30, 60);
        BlockInit.flammableBlocks(BlockInit.DYNA_LEAVES_PINK.get(), 30, 60);
        BlockInit.flammableBlocks(BlockInit.DYNA_LEAVES_RED.get(), 30, 60);
        BlockInit.flammableBlocks(BlockInit.INVERTED_LEAVES.get(), 30, 60);
        BlockInit.flammableBlocks(BlockInit.MIRAGE_LEAVES.get(), 30, 60);
        BlockInit.flammableBlocks(BlockInit.TEMPORAL_LEAVES.get(), 30, 60);

        // Planks
        BlockInit.flammableBlocks(BlockInit.AGED_PLANKS.get(), 5, 20);
        BlockInit.flammableBlocks(BlockInit.CORRUPTED_PLANKS.get(), 5, 20);
        BlockInit.flammableBlocks(BlockInit.DISTORTIC_PLANKS.get(), 5, 20);
        BlockInit.flammableBlocks(BlockInit.INVERTED_PLANKS.get(), 5, 20);
        BlockInit.flammableBlocks(BlockInit.MIRAGE_PLANKS.get(), 5, 20);
        BlockInit.flammableBlocks(BlockInit.TEMPORAL_PLANKS.get(), 5, 20);

        // Slabs
        BlockInit.flammableBlocks(BlockInit.AGED_SLAB.get(), 5, 20);
        BlockInit.flammableBlocks(BlockInit.CORRUPTED_SLAB.get(), 5, 20);
        BlockInit.flammableBlocks(BlockInit.DISTORTIC_SLAB.get(), 5, 20);
        BlockInit.flammableBlocks(BlockInit.INVERTED_SLAB.get(), 5, 20);
        BlockInit.flammableBlocks(BlockInit.MIRAGE_SLAB.get(), 5, 20);
        BlockInit.flammableBlocks(BlockInit.TEMPORAL_SLAB.get(), 5, 20);

        // Stairs
        BlockInit.flammableBlocks(BlockInit.AGED_STAIRS.get(), 5, 20);
        BlockInit.flammableBlocks(BlockInit.CORRUPTED_STAIRS.get(), 5, 20);
        BlockInit.flammableBlocks(BlockInit.DISTORTIC_STAIRS.get(), 5, 20);
        BlockInit.flammableBlocks(BlockInit.INVERTED_STAIRS.get(), 5, 20);
        BlockInit.flammableBlocks(BlockInit.MIRAGE_STAIRS.get(), 5, 20);
        BlockInit.flammableBlocks(BlockInit.TEMPORAL_STAIRS.get(), 5, 20);

        // Fences
        BlockInit.flammableBlocks(BlockInit.AGED_FENCE.get(), 5, 20);
        BlockInit.flammableBlocks(BlockInit.CORRUPTED_FENCE.get(), 5, 20);
        BlockInit.flammableBlocks(BlockInit.DISTORTIC_FENCE.get(), 5, 20);
        BlockInit.flammableBlocks(BlockInit.INVERTED_FENCE.get(), 5, 20);
        BlockInit.flammableBlocks(BlockInit.MIRAGE_FENCE.get(), 5, 20);
        BlockInit.flammableBlocks(BlockInit.TEMPORAL_FENCE.get(), 5, 20);

        // Fence Gates
        BlockInit.flammableBlocks(BlockInit.AGED_FENCE_GATE.get(), 5, 20);
        BlockInit.flammableBlocks(BlockInit.CORRUPTED_FENCE_GATE.get(), 5, 20);
        BlockInit.flammableBlocks(BlockInit.DISTORTIC_FENCE_GATE.get(), 5, 20);
        BlockInit.flammableBlocks(BlockInit.INVERTED_FENCE_GATE.get(), 5, 20);
        BlockInit.flammableBlocks(BlockInit.MIRAGE_FENCE_GATE.get(), 5, 20);
        BlockInit.flammableBlocks(BlockInit.TEMPORAL_FENCE_GATE.get(), 5, 20);

        // Plants
        BlockInit.flammableBlocks(PlantsInit.COMPRECED_MUSHROOM.get(), 60, 100);
        BlockInit.flammableBlocks(PlantsInit.DISTORCED_MUSHROOM.get(), 60, 100);
        BlockInit.flammableBlocks(PlantsInit.GOLDEN_FERN.get(), 60, 100);
        BlockInit.flammableBlocks(PlantsInit.GOLDEN_GRASS.get(), 60, 100);
        BlockInit.flammableBlocks(PlantsInit.GOLDEN_POPPY.get(), 60, 100);
        BlockInit.flammableBlocks(PlantsInit.GOLDEN_SWEET_BERRY_BUSH.get(), 60, 100);
        BlockInit.flammableBlocks(PlantsInit.INVERTED_ORCHID.get(), 60, 100);
        BlockInit.flammableBlocks(PlantsInit.TEMPORAL_BAMBOO.get(), 60, 60);

        // Bookshelves
        BlockInit.flammableBlocks(BlockInit.AGED_BOOKSHELF.get(), 5, 20);
        BlockInit.flammableBlocks(BlockInit.CORRUPTED_BOOKSHELF.get(), 5, 20);
        BlockInit.flammableBlocks(BlockInit.DISTORTIC_BOOKSHELF.get(), 5, 20);
        BlockInit.flammableBlocks(BlockInit.INVERTED_BOOKSHELF.get(), 5, 20);
        BlockInit.flammableBlocks(BlockInit.MIRAGE_BOOKSHELF.get(), 5, 20);
        BlockInit.flammableBlocks(BlockInit.TEMPORAL_BOOKSHELF.get(), 5, 20);
    }
}
