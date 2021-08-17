package pokecube.legends.init;

import net.minecraft.block.*;
import net.minecraft.block.material.Material;
import net.minecraft.block.material.MaterialColor;
import net.minecraft.item.BlockItem;
import net.minecraft.item.DyeColor;
import net.minecraft.item.Item;
import net.minecraft.potion.Effects;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraftforge.common.ToolType;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.fml.event.lifecycle.FMLLoadCompleteEvent;
import pokecube.core.PokecubeItems;
import pokecube.core.handlers.ItemGenerator;
import pokecube.core.handlers.ItemGenerator.GenericStairs;
import pokecube.legends.PokecubeLegends;
import pokecube.legends.blocks.BlockBase;
import pokecube.legends.blocks.EffectBlockBase;
import pokecube.legends.blocks.FaceBlock_Base;
import pokecube.legends.blocks.SaplingBase;
import pokecube.legends.blocks.customblocks.*;
import pokecube.legends.blocks.customblocks.taputotem.BuluTotem;
import pokecube.legends.blocks.customblocks.taputotem.FiniTotem;
import pokecube.legends.blocks.customblocks.taputotem.KokoTotem;
import pokecube.legends.blocks.customblocks.taputotem.LeleTotem;
import pokecube.legends.blocks.normalblocks.OreBlock;
import pokecube.legends.blocks.normalblocks.*;
import pokecube.legends.blocks.plants.*;

import java.util.function.ToIntFunction;

public class BlockInit
{
    // Blocks
    public static final RegistryObject<Block> RAID_SPAWN;
    
    public static final RegistryObject<Block> CRAMOMATIC_BLOCK;
    
    public static final RegistryObject<Block> METEOR_BLOCK;
    public static final RegistryObject<Block> METEOR_SLAB;
    public static final RegistryObject<Block> METEOR_STAIRS;

    // Decorative_Blocks
    public static final RegistryObject<Block> OCEAN_BRICK;
    public static final RegistryObject<Block> OCEAN_BRICK_SLAB;
    public static final RegistryObject<Block> OCEAN_BRICK_STAIRS;
    
    public static final RegistryObject<Block> SKY_BRICK;
    public static final RegistryObject<Block> SKY_BRICK_SLAB;
    public static final RegistryObject<Block> SKY_BRICK_STAIRS;
    
    public static final RegistryObject<Block> SPATIAN_BRICK;
    public static final RegistryObject<Block> SPATIAN_BRICK_SLAB;
    public static final RegistryObject<Block> SPATIAN_BRICK_STAIRS;
    
    public static final RegistryObject<Block> MAGMA_BRICK;
    public static final RegistryObject<Block> MAGMA_BRICK_SLAB;
    public static final RegistryObject<Block> MAGMA_BRICK_STAIRS;
    
    public static final RegistryObject<Block> DARKSKY_BRICK;
    public static final RegistryObject<Block> DARKSKY_BRICK_SLAB;
    public static final RegistryObject<Block> DARKSKY_BRICK_STAIRS;
    
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
    
    public static final RegistryObject<Block> DYNA_LEAVES1;
    public static final RegistryObject<Block> DYNA_LEAVES2;
    public static final RegistryObject<Block> DYNA_LEAVES3;
    public static final RegistryObject<Block> TOTEM_BLOCK;
    
    //Tapus Totens
    //Koko Totem
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
    
    //Bulu Totem
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
    
    //Lele Totem
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
    
    //Fini Totem
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
    public static final RegistryObject<Block> DISTORTIC_CRACKED_STONE;
    public static final RegistryObject<Block> DISTORTIC_GLOWSTONE;
    public static final RegistryObject<Block> DISTORTIC_CHISELED_MIRROR;
    public static final RegistryObject<Block> DISTORTIC_FRAMED_MIRROR;
    public static final RegistryObject<Block> DISTORTIC_OW_GLASS;
    public static final RegistryObject<Block> DISTORTIC_OW_GLASS_WHITE;
    public static final RegistryObject<Block> DISTORTIC_OW_GLASS_ORANGE;
    public static final RegistryObject<Block> DISTORTIC_OW_GLASS_MAGENTA;
    public static final RegistryObject<Block> DISTORTIC_OW_GLASS_LIGHT_BLUE;
    public static final RegistryObject<Block> DISTORTIC_OW_GLASS_YELLOW;
    public static final RegistryObject<Block> DISTORTIC_OW_GLASS_LIME;
    public static final RegistryObject<Block> DISTORTIC_OW_GLASS_PINK;
    public static final RegistryObject<Block> DISTORTIC_OW_GLASS_GRAY;
    public static final RegistryObject<Block> DISTORTIC_OW_GLASS_LIGHT_GRAY;
    public static final RegistryObject<Block> DISTORTIC_OW_GLASS_CYAN;
    public static final RegistryObject<Block> DISTORTIC_OW_GLASS_PURPLE;
    public static final RegistryObject<Block> DISTORTIC_OW_GLASS_BLUE;
    public static final RegistryObject<Block> DISTORTIC_OW_GLASS_BROWN;
    public static final RegistryObject<Block> DISTORTIC_OW_GLASS_GREEN;
    public static final RegistryObject<Block> DISTORTIC_OW_GLASS_RED;
    public static final RegistryObject<Block> DISTORTIC_OW_GLASS_BLACK;
    public static final RegistryObject<Block> DISTORTIC_OW_GLASS_LAB;
    public static final RegistryObject<Block> DISTORTIC_OW_GLASS_MIRAGE;
    public static final RegistryObject<Block> DISTORTIC_OW_GLASS_SPECTRUM;
    public static final RegistryObject<Block> DISTORTIC_OW_FRAMED_MIRROR;
    
    public static final RegistryObject<Block> DISTORTIC_STONEBRICK;
    public static final RegistryObject<Block> DISTORTIC_STONEBRICK_SLAB;
    public static final RegistryObject<Block> DISTORTIC_STONEBRICK_STAIRS;
    
    public static final RegistryObject<Block> DISTORTIC_CHISELED_STONE;
    public static final RegistryObject<Block> DISTORTIC_CHISELED_SLAB;
    public static final RegistryObject<Block> DISTORTIC_CHISELED_STAIRS;
    
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
    
    //
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

    // Crystal
    public static final RegistryObject<Block> CRYSTAL;
    public static final RegistryObject<Block> CRYSTAL_BUTTON;
    public static final RegistryObject<Block> CRYSTAL_BRICK;
    public static final RegistryObject<Block> CRYSTAL_STAIRS;
    public static final RegistryObject<Block> CRYSTAL_SLAB;
    public static final RegistryObject<Block> CRYSTAL_BRICKS_SLAB;
    public static final RegistryObject<Block> CRYSTAL_BRICKS_STAIRS;
    
    // Ultra Stone
    public static final RegistryObject<Block> ULTRA_STONE;
    public static final RegistryObject<Block> ULTRA_STONE_SLAB;
    public static final RegistryObject<Block> ULTRA_STONE_STAIRS;
    public static final RegistryObject<Block> ULTRA_COBBLESTONE;
    public static final RegistryObject<Block> ULTRA_COBBLESTONE_SLAB;
    public static final RegistryObject<Block> ULTRA_COBBLESTONE_STAIRS;
    public static final RegistryObject<Block> ULTRA_STONEBRICK_SLAB;
    public static final RegistryObject<Block> ULTRA_STONEBRICK_STAIRS;
    public static final RegistryObject<Block> ULTRA_STONEBRICK;
    public static final RegistryObject<Block> ULTRA_STONE_BUTTON;
    public static final RegistryObject<Block> ULTRA_STONE_PR_PLATE;
    
    public static final RegistryObject<Block> ULTRA_METAL;
    public static final RegistryObject<Block> ULTRA_METAL_SLAB;
    public static final RegistryObject<Block> ULTRA_METAL_STAIRS;
    public static final RegistryObject<Block> ULTRA_METAL_BUTTON;
    public static final RegistryObject<Block> ULTRA_METAL_PR_PLATE;
    
    // DarkStone
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
    
    // Ultra Desert
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

    // Plants(LOG/Planks/Leaves)
    public static final RegistryObject<Block> INVERTED_SAPLING;
    public static final RegistryObject<Block> TEMPORAL_SAPLING;
    public static final RegistryObject<Block> AGED_SAPLING;
    public static final RegistryObject<Block> CORRUPTED_SAPLING;
    public static final RegistryObject<Block> MIRAGE_SAPLING;
    public static final RegistryObject<Block> DISTORTIC_SAPLING;

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

    public static final RegistryObject<Block> CONCRETE_LOG;
    public static final RegistryObject<Block> CONCRETE_PLANKS;
    public static final RegistryObject<Block> CONCRETE_DENSE_PLANKS;
    public static final RegistryObject<Block> CONCRETE_WOOD;
    public static final RegistryObject<Block> STRIP_CONCRETE_LOG;
    public static final RegistryObject<Block> STRIP_CONCRETE_WOOD;
    public static final RegistryObject<Block> CONCRETE_STAIRS;
    public static final RegistryObject<Block> CONCRETE_SLAB;
    public static final RegistryObject<Block> CONCRETE_DENSE_STAIRS;
    public static final RegistryObject<Block> CONCRETE_DENSE_SLAB;
    public static final RegistryObject<Block> CONCRETE_FENCE;
    public static final RegistryObject<Block> CONCRETE_FENCE_GATE;
    public static final RegistryObject<Block> CONCRETE_DENSE_WALL;
    public static final RegistryObject<Block> CONCRETE_DENSE_WALL_GATE;
    public static final RegistryObject<Block> CONCRETE_TRAPDOOR;
    public static final RegistryObject<Block> CONCRETE_DOOR;
    public static final RegistryObject<Block> CONCRETE_BUTTON;
    public static final RegistryObject<Block> CONCRETE_PR_PLATE;
    public static final RegistryObject<Block> CONCRETE_DENSE_BUTTON;
    public static final RegistryObject<Block> CONCRETE_DENSE_PR_PLATE;
    
    // Plants
    public static final RegistryObject<Block> CRYSTALLIZED_BUSH;
    public static final RegistryObject<Block> TALL_CRYSTALLIZED_BUSH;
    public static final RegistryObject<Block> CRYSTALLIZED_CACTUS;
    public static final RegistryObject<Block> DISTORTIC_VINES;
    public static final RegistryObject<Block> DISTORTIC_VINES_PLANT;

    // Portal
    public static final RegistryObject<Block> BLOCK_PORTALWARP;

    // Legendary Spawns
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
    public static final RegistryObject<Block> POTTED_GOLDEN_POPPY;
    public static final RegistryObject<Block> POTTED_INVERTED_ORCHID;
    public static final RegistryObject<Block> POTTED_TALL_CRYSTALLIZED_BUSH;

    static
    {
        // Dimensions Creative Tab - Sorting depends on the order the blocks are listed in
        // Block Raid
        RAID_SPAWN = PokecubeLegends.BLOCKS.register("raidspawn_block", () -> new RaidSpawnBlock(AbstractBlock.Properties.of(
                Material.STONE, MaterialColor.COLOR_RED).randomTicks().strength(2000, 2000)).setInfoBlockName("raidspawn"));
        CRAMOMATIC_BLOCK = PokecubeLegends.BLOCKS.register("cramomatic_block", () -> new CramomaticBlock(AbstractBlock.Properties.of(
            Material.METAL, MaterialColor.TERRACOTTA_RED).strength(6, 15).sound(SoundType.ANVIL).harvestTool(ToolType.PICKAXE)
            .harvestLevel(2).dynamicShape().requiresCorrectToolForDrops()).setToolTip("cramobot"));
        
        // Meteor Blocks
        METEOR_BLOCK = PokecubeLegends.BLOCKS_TAB.register("meteor_block", () -> new MeteorBlock(6842513,
                AbstractBlock.Properties.of(Material.VEGETABLE, MaterialColor.TERRACOTTA_BLUE).strength(2.5f)
                .sound(SoundType.METAL).harvestTool(ToolType.PICKAXE).harvestLevel(2).requiresCorrectToolForDrops()));
        METEOR_SLAB = PokecubeLegends.BLOCKS_TAB.register("meteor_slab", () -> new SlabBlock(AbstractBlock.Properties.of(
        		Material.STONE, MaterialColor.TERRACOTTA_BLUE).strength(2.0F, 3.0f).sound(SoundType.STONE)
        			.harvestTool(ToolType.PICKAXE).requiresCorrectToolForDrops()));
        METEOR_STAIRS = PokecubeLegends.BLOCKS_TAB.register("meteor_stairs",
                () -> new ItemGenerator.GenericStairs(Blocks.STONE_STAIRS.defaultBlockState(), AbstractBlock.Properties.of(
        		Material.STONE, MaterialColor.TERRACOTTA_BLUE).strength(2.0F, 3.0f).sound(SoundType.STONE)
            		.harvestTool(ToolType.PICKAXE).requiresCorrectToolForDrops()));
        
        //Grass
        MUSHROOM_GRASS = PokecubeLegends.BLOCKS_TAB.register("ultragrass1", () -> new GrassMushroomBlock(AbstractBlock.Properties.of(
            Material.GRASS, MaterialColor.COLOR_RED).sound(SoundType.GRASS)
            .strength(1f, 2f).harvestTool(ToolType.SHOVEL).harvestLevel(1).randomTicks()));
        MUSHROOM_DIRT = PokecubeLegends.BLOCKS_TAB.register("ultradirt1", () -> new BlockBase(Material.CLAY, MaterialColor.COLOR_PURPLE,
            1f, 2f, SoundType.GRAVEL, ToolType.SHOVEL, 1, false));

        JUNGLE_GRASS = PokecubeLegends.BLOCKS_TAB.register("ultragrass2", () -> new GrassJungleBlock(AbstractBlock.Properties.of(
            Material.GRASS, MaterialColor.WARPED_NYLIUM).sound(SoundType.GRASS)
            .strength(1f, 2f).harvestTool(ToolType.SHOVEL).harvestLevel(1).randomTicks()));
        JUNGLE_DIRT = PokecubeLegends.BLOCKS_TAB.register("ultradirt2", () -> new BlockBase(Material.VEGETABLE, MaterialColor.TERRACOTTA_YELLOW,
            1f, 2f, SoundType.GRAVEL, ToolType.SHOVEL, 1, false));

        CORRUPTED_GRASS = PokecubeLegends.BLOCKS_TAB.register("ultrasand1", () -> new GrassCorruptedBlock(AbstractBlock.Properties.of(
            Material.GRASS, MaterialColor.TERRACOTTA_BLUE).sound(SoundType.SCAFFOLDING)
            .strength(4f, 5f).harvestTool(ToolType.PICKAXE).harvestLevel(1).randomTicks().requiresCorrectToolForDrops()));
        CORRUPTED_DIRT = PokecubeLegends.BLOCKS_TAB.register("ultradirt4", () -> new DirtCorruptedBlock(AbstractBlock.Properties.of(
            Material.STONE, MaterialColor.TERRACOTTA_PURPLE).sound(SoundType.METAL)
            .strength(0.9f).harvestTool(ToolType.PICKAXE).harvestLevel(1).requiresCorrectToolForDrops()));

        AGED_GRASS = PokecubeLegends.BLOCKS_TAB.register("ultragrass3", () -> new GrassAgedBlock(AbstractBlock.Properties.of
            (Material.GRASS, MaterialColor.COLOR_ORANGE).sound(SoundType.GRASS)
            .strength(1f, 2f).harvestTool(ToolType.SHOVEL).harvestLevel(1).randomTicks()));
        AGED_DIRT = PokecubeLegends.BLOCKS_TAB.register("ultradirt3", () -> new BlockBase(Material.DIRT, MaterialColor.TERRACOTTA_YELLOW,
            1f, 2f, SoundType.WET_GRASS, ToolType.SHOVEL, 1, false));

        // Crystal Blocks
        CRYSTAL = PokecubeLegends.BLOCKS_TAB.register("temporal_crystal", () -> new BlockBase(Material.GLASS, MaterialColor.COLOR_LIGHT_BLUE, 
                1.5f, 3, SoundType.GLASS, ToolType.PICKAXE, 1, false));
        CRYSTAL_BRICK = PokecubeLegends.BLOCKS_TAB.register("crystalbrick", () -> new BlockBase(Material.ICE_SOLID, MaterialColor.COLOR_LIGHT_BLUE,
                0.5F, 10, SoundType.GLASS, ToolType.PICKAXE, 1, true));
        CRYSTAL_STAIRS = PokecubeLegends.BLOCKS_TAB.register("crystal_stairs", () -> new ItemGenerator.GenericStairs(Blocks.STONE_STAIRS.defaultBlockState(), AbstractBlock.Properties.of(
        		Material.GLASS, MaterialColor.TERRACOTTA_LIGHT_BLUE).strength(2.0F, 3.0f).sound(SoundType.GLASS)
        			.harvestTool(ToolType.PICKAXE).requiresCorrectToolForDrops()));
        CRYSTAL_SLAB = PokecubeLegends.BLOCKS_TAB.register("crystal_slab", () -> new SlabBlock(AbstractBlock.Properties.of(
        		Material.GLASS, MaterialColor.TERRACOTTA_LIGHT_BLUE).strength(2.0F, 3.0f).sound(SoundType.GLASS)
        		.harvestTool(ToolType.PICKAXE).requiresCorrectToolForDrops()));
        CRYSTAL_BRICKS_STAIRS = PokecubeLegends.BLOCKS_TAB.register("crystal_bricks_stairs",
                () -> new ItemGenerator.GenericStairs(Blocks.STONE_STAIRS.defaultBlockState(), AbstractBlock.Properties.of(
        		Material.GLASS, MaterialColor.TERRACOTTA_LIGHT_BLUE).strength(2.0F, 3.0f).sound(SoundType.GLASS)
        		.harvestTool(ToolType.PICKAXE).requiresCorrectToolForDrops()));
        CRYSTAL_BRICKS_SLAB = PokecubeLegends.BLOCKS_TAB.register("crystal_bricks_slab", () -> new SlabBlock(AbstractBlock.Properties.of(
        		Material.GLASS, MaterialColor.TERRACOTTA_LIGHT_BLUE).strength(2.0F, 3.0f).sound(SoundType.GLASS)
        		.harvestTool(ToolType.PICKAXE).requiresCorrectToolForDrops()));
        CRYSTAL_BUTTON = PokecubeLegends.BLOCKS_TAB.register("crystal_button",
                () -> new ItemGenerator.GenericWoodButton(AbstractBlock.Properties.of(Material.GLASS, MaterialColor.SNOW).sound(SoundType.GLASS)
                        .noCollission().strength(0.5F).harvestTool(ToolType.PICKAXE).requiresCorrectToolForDrops()));

        // Ultra Stones
        ULTRA_STONE = PokecubeLegends.BLOCKS_TAB.register("ultrastone", () -> new BlockBase(Material.STONE, MaterialColor.TERRACOTTA_CYAN,
            1.5f, 6.0f, SoundType.STONE, ToolType.PICKAXE, 1, true));

        ULTRA_COAL_ORE = PokecubeLegends.BLOCKS_TAB.register("ultra_coal_ore", () -> new OreBlock(AbstractBlock.Properties.of(
            Material.STONE, MaterialColor.TERRACOTTA_BLACK).sound(SoundType.STONE).strength(3.0F, 3.0f).requiresCorrectToolForDrops()));
        ULTRA_IRON_ORE = PokecubeLegends.BLOCKS_TAB.register("ultra_iron_ore", () -> new OreBlock(AbstractBlock.Properties.of(
            Material.STONE, MaterialColor.TERRACOTTA_BLACK).sound(SoundType.STONE).strength(3.0F, 3.0f).requiresCorrectToolForDrops()));
        ULTRA_GOLD_ORE = PokecubeLegends.BLOCKS_TAB.register("ultra_gold_ore", () -> new OreBlock(AbstractBlock.Properties.of(
            Material.STONE, MaterialColor.TERRACOTTA_BLACK).sound(SoundType.STONE).strength(3.0F, 3.0f).requiresCorrectToolForDrops()));
        ULTRA_REDSTONE_ORE = PokecubeLegends.BLOCKS_TAB.register("ultra_redstone_ore", () -> new RedstoneOreBlock(AbstractBlock.Properties.of(
            Material.STONE, MaterialColor.TERRACOTTA_CYAN).sound(SoundType.STONE).strength(3.0F, 3.0f).requiresCorrectToolForDrops().randomTicks().lightLevel(litBlockEmission(9))));
        ULTRA_LAPIS_ORE = PokecubeLegends.BLOCKS_TAB.register("ultra_lazuli_ore", () -> new OreBlock(AbstractBlock.Properties.of(
            Material.STONE, MaterialColor.TERRACOTTA_BLACK).sound(SoundType.STONE).strength(3.0F, 3.0f).requiresCorrectToolForDrops()));
        ULTRA_EMERALD_ORE = PokecubeLegends.BLOCKS_TAB.register("ultra_emerald_ore", () -> new OreBlock(AbstractBlock.Properties.of(
            Material.STONE, MaterialColor.TERRACOTTA_BLACK).sound(SoundType.STONE).strength(3.0F, 3.0f).requiresCorrectToolForDrops()));
        ULTRA_DIAMOND_ORE = PokecubeLegends.BLOCKS_TAB.register("ultra_diamond_ore", () -> new OreBlock(AbstractBlock.Properties.of(
            Material.STONE, MaterialColor.TERRACOTTA_BLACK).sound(SoundType.STONE).strength(3.0F, 3.0f).requiresCorrectToolForDrops()));
        ULTRA_COSMIC_DUST_ORE = PokecubeLegends.BLOCKS_TAB.register("ultra_cosmic_dust_ore", () -> new OreBlock(AbstractBlock.Properties.of(
            Material.STONE, MaterialColor.TERRACOTTA_BLACK).sound(SoundType.STONE).strength(3.0F, 3.0f).requiresCorrectToolForDrops()));
        SPECTRUM_ORE = PokecubeLegends.BLOCKS_TAB.register("spectrum_ore", () -> new OreBlock(AbstractBlock.Properties.of(
            Material.STONE, MaterialColor.TERRACOTTA_BLACK).sound(SoundType.STONE).strength(3.0F, 3.0f).requiresCorrectToolForDrops()));

        ULTRA_STONE_SLAB = PokecubeLegends.BLOCKS_TAB.register("ultra_stone_slab", () -> new SlabBlock(AbstractBlock.Properties.of(
            Material.STONE, MaterialColor.COLOR_LIGHT_GRAY).strength(2.0F, 3.0f).sound(SoundType.STONE)
            .harvestTool(ToolType.PICKAXE).requiresCorrectToolForDrops()));
        ULTRA_STONE_STAIRS = PokecubeLegends.BLOCKS_TAB.register("ultra_stone_stairs",
            () -> new ItemGenerator.GenericStairs(Blocks.STONE_STAIRS.defaultBlockState(), AbstractBlock.Properties.of(
                Material.STONE, MaterialColor.COLOR_LIGHT_GRAY).strength(2.0F, 3.0f).sound(SoundType.STONE)
                .harvestTool(ToolType.PICKAXE).requiresCorrectToolForDrops()));
        ULTRA_COBBLESTONE = PokecubeLegends.BLOCKS_TAB.register("ultra_cobblestone", () -> new BlockBase(Material.STONE, MaterialColor.TERRACOTTA_CYAN,
            1.5f, 10f, SoundType.STONE, ToolType.PICKAXE, 2, true));
        ULTRA_COBBLESTONE_SLAB = PokecubeLegends.BLOCKS_TAB.register("ultra_cobblestone_slab", () -> new SlabBlock(AbstractBlock.Properties.of(
            Material.STONE, MaterialColor.COLOR_LIGHT_GRAY).strength(2.0F, 3.0f).sound(SoundType.STONE)
            .harvestTool(ToolType.PICKAXE).requiresCorrectToolForDrops()));
        ULTRA_COBBLESTONE_STAIRS = PokecubeLegends.BLOCKS_TAB.register("ultra_cobblestone_stairs",
            () -> new ItemGenerator.GenericStairs(Blocks.STONE_STAIRS.defaultBlockState(), AbstractBlock.Properties.of(
                Material.STONE, MaterialColor.COLOR_LIGHT_GRAY).strength(2.0F, 3.0f).sound(SoundType.STONE)
                .harvestTool(ToolType.PICKAXE).requiresCorrectToolForDrops()));
        ULTRA_STONEBRICK = PokecubeLegends.BLOCKS_TAB.register("ultra_stonebricks", () -> new BlockBase(Material.STONE,MaterialColor.TERRACOTTA_CYAN,
            1.5f, 10f, SoundType.STONE, ToolType.PICKAXE, 2, true));
        ULTRA_STONEBRICK_SLAB = PokecubeLegends.BLOCKS_TAB.register("ultra_stonebricks_slab", () -> new SlabBlock(AbstractBlock.Properties.of(
            Material.STONE, MaterialColor.COLOR_LIGHT_GRAY).strength(2.0F, 3.0f).sound(SoundType.STONE)
            .harvestTool(ToolType.PICKAXE).requiresCorrectToolForDrops()));
        ULTRA_STONEBRICK_STAIRS = PokecubeLegends.BLOCKS_TAB.register("ultra_stonebricks_stairs",
            () -> new ItemGenerator.GenericStairs(Blocks.STONE_STAIRS.defaultBlockState(), AbstractBlock.Properties.of(
                Material.STONE, MaterialColor.COLOR_LIGHT_GRAY).strength(2.0F, 3.0f).sound(SoundType.STONE)
                .harvestTool(ToolType.PICKAXE).requiresCorrectToolForDrops()));
        ULTRA_STONE_BUTTON = PokecubeLegends.BLOCKS_TAB.register("ultra_stone_button",
            () -> new ItemGenerator.GenericWoodButton(AbstractBlock.Properties.of(Material.STONE, MaterialColor.COLOR_BLUE).sound(SoundType.BAMBOO)
                .noCollission().strength(0.5F).harvestTool(ToolType.PICKAXE).requiresCorrectToolForDrops()));
        ULTRA_STONE_PR_PLATE = PokecubeLegends.BLOCKS_TAB.register("ultra_stone_pressure_plate",
            () -> new ItemGenerator.GenericPressurePlate(PressurePlateBlock.Sensitivity.EVERYTHING, AbstractBlock.Properties.of(
                Material.STONE, MaterialColor.COLOR_BLUE).sound(SoundType.BAMBOO).noCollission().strength(
                0.7F).harvestTool(ToolType.PICKAXE).requiresCorrectToolForDrops()));
        
        // Dark Stone
        ULTRA_DARKSTONE = PokecubeLegends.BLOCKS_TAB.register("ultracobbles", () -> new EffectBlockBase(Material.STONE, MaterialColor.COLOR_BLACK,
                5f, 8f, SoundType.GILDED_BLACKSTONE, ToolType.PICKAXE, 1, true, Effects.BLINDNESS));
        ULTRA_DARKSTONE_SLAB = PokecubeLegends.BLOCKS_TAB.register("ultra_darkstone_slab", () -> new SlabBlock(AbstractBlock.Properties.of(
        		Material.STONE, MaterialColor.COLOR_BLACK).strength(2.0F, 3.0f).sound(SoundType.GILDED_BLACKSTONE)
        		.harvestTool(ToolType.PICKAXE).requiresCorrectToolForDrops()));
        ULTRA_DARKSTONE_STAIRS = PokecubeLegends.BLOCKS_TAB.register("ultra_darkstone_stairs",
                () -> new ItemGenerator.GenericStairs(Blocks.STONE_STAIRS.defaultBlockState(), AbstractBlock.Properties.of(
        		Material.STONE, MaterialColor.COLOR_BLACK).strength(2.0F, 3.0f).sound(SoundType.GILDED_BLACKSTONE)
            		.harvestTool(ToolType.PICKAXE).requiresCorrectToolForDrops()));
        ULTRA_DARK_COBBLESTONE = PokecubeLegends.BLOCKS_TAB.register("ultrarock", () -> new BlockBase(Material.STONE, MaterialColor.COLOR_BLACK,
                0.8f, 10f, SoundType.STONE, ToolType.PICKAXE, 1, true));
        ULTRA_DARK_COBBLESTONE_SLAB = PokecubeLegends.BLOCKS_TAB.register("ultra_dark_cobblestone_slab", () -> new SlabBlock(AbstractBlock.Properties.of(
        		Material.STONE, MaterialColor.COLOR_BLACK).strength(2.0F, 3.0f).sound(SoundType.GILDED_BLACKSTONE)
        			.harvestTool(ToolType.PICKAXE).requiresCorrectToolForDrops()));
        ULTRA_DARK_COBBLESTONE_STAIRS = PokecubeLegends.BLOCKS_TAB.register("ultra_dark_cobblestone_stairs",
                () -> new ItemGenerator.GenericStairs(Blocks.STONE_STAIRS.defaultBlockState(), AbstractBlock.Properties.of(
        		Material.STONE, MaterialColor.COLOR_BLACK).strength(2.0F, 3.0f).sound(SoundType.GILDED_BLACKSTONE)
            		.harvestTool(ToolType.PICKAXE).requiresCorrectToolForDrops()));
        ULTRA_DARKSTONE_BRICKS = PokecubeLegends.BLOCKS_TAB.register("ultra_darkstone_bricks", () -> new EffectBlockBase(Material.STONE, MaterialColor.COLOR_BLACK,
                5f, 8f, SoundType.GILDED_BLACKSTONE, ToolType.PICKAXE, 2, true, Effects.BLINDNESS));
        ULTRA_DARKSTONE_BRICKS_SLAB = PokecubeLegends.BLOCKS_TAB.register("ultra_darkstone_bricks_slab", () -> new SlabBlock(AbstractBlock.Properties.of(
        		Material.STONE, MaterialColor.COLOR_BLACK).strength(2.0F, 3.0f).sound(SoundType.GILDED_BLACKSTONE)
        			.harvestTool(ToolType.PICKAXE).requiresCorrectToolForDrops()));
        ULTRA_DARKSTONE_BRICKS_STAIRS = PokecubeLegends.BLOCKS_TAB.register("ultra_darkstone_bricks_stairs",
                () -> new ItemGenerator.GenericStairs(Blocks.STONE_STAIRS.defaultBlockState(), AbstractBlock.Properties.of(
        		Material.STONE, MaterialColor.COLOR_BLACK).strength(2.0F, 3.0f).sound(SoundType.GILDED_BLACKSTONE)
            		.harvestTool(ToolType.PICKAXE).requiresCorrectToolForDrops()));
        ULTRA_DARKSTONE_BUTTON = PokecubeLegends.BLOCKS_TAB.register("ultra_darkstone_button",
                () -> new ItemGenerator.GenericWoodButton(AbstractBlock.Properties.of(Material.STONE, MaterialColor.COLOR_BLACK).sound(SoundType.NETHER_BRICKS)
                        .noCollission().strength(0.5F).harvestTool(ToolType.PICKAXE).requiresCorrectToolForDrops()));
        ULTRA_DARKSTONE_PR_PLATE = PokecubeLegends.BLOCKS_TAB.register("ultra_darkstone_pressure_plate",
                () -> new ItemGenerator.GenericPressurePlate(PressurePlateBlock.Sensitivity.MOBS, AbstractBlock.Properties
                        .of(Material.STONE, MaterialColor.COLOR_BLACK).sound(SoundType.NETHER_BRICKS).noCollission().strength(
                                0.7F).harvestTool(ToolType.PICKAXE).requiresCorrectToolForDrops()));
        
        // Ultra Desert
        CRYSTALLIZED_SAND = PokecubeLegends.BLOCKS_TAB.register("ultrasand", () -> new EffectBlockBase(Material.SAND, MaterialColor.SNOW,
                2f, 4f, SoundType.SAND, ToolType.SHOVEL, 1, false, Effects.LEVITATION));
        CRYSTALLIZED_SANDSTONE = PokecubeLegends.BLOCKS_TAB.register("ultrasandstone", () -> new BlockBase(Material.STONE, MaterialColor.SNOW,
                1f, 10f, SoundType.STONE, ToolType.PICKAXE, 2, true));
        CRYS_SANDSTONE_SLAB = PokecubeLegends.BLOCKS_TAB.register("ultra_sandstone_slab", () -> new SlabBlock(AbstractBlock.Properties.of(
        		Material.STONE, MaterialColor.SNOW).strength(2.0F, 3.0f).sound(SoundType.SAND)
        			.harvestTool(ToolType.PICKAXE).requiresCorrectToolForDrops()));
        CRYS_SANDSTONE_STAIRS = PokecubeLegends.BLOCKS_TAB.register("ultra_sandstone_stairs",
                () -> new ItemGenerator.GenericStairs(Blocks.STONE_STAIRS.defaultBlockState(), AbstractBlock.Properties.of(
        		Material.STONE, MaterialColor.SNOW).strength(2.0F, 3.0f).sound(SoundType.SAND)
            		.harvestTool(ToolType.PICKAXE).requiresCorrectToolForDrops()));
        CRYS_SANDSTONE_BRICKS = PokecubeLegends.BLOCKS_TAB.register("ultra_sandbrick", () -> new BlockBase(Material.STONE, MaterialColor.SNOW, 
                1.4f, 10f, SoundType.STONE, ToolType.PICKAXE, 1, true));
        CRYS_SANDSTONE_BRICK_SLAB = PokecubeLegends.BLOCKS_TAB.register("ultra_sandbrick_slab", () -> new SlabBlock(AbstractBlock.Properties.of(
        		Material.STONE, MaterialColor.SNOW).strength(2.0F, 3.0f).sound(SoundType.SAND).harvestTool(ToolType.PICKAXE).requiresCorrectToolForDrops()));
        CRYS_SANDSTONE_BRICK_STAIRS = PokecubeLegends.BLOCKS_TAB.register("ultra_sandbrick_stairs",
                () -> new ItemGenerator.GenericStairs(Blocks.STONE_STAIRS.defaultBlockState(), AbstractBlock.Properties.of(
        		Material.STONE, MaterialColor.SNOW).strength(2.0F, 3.0f).sound(SoundType.SAND)
            		.harvestTool(ToolType.PICKAXE).requiresCorrectToolForDrops()));
        CRYS_SANDSTONE_SMOOTH = PokecubeLegends.BLOCKS_TAB.register("ultra_sandstone_smooth", () -> new BlockBase(Material.STONE, MaterialColor.SNOW,
        		1.5f, 10f, SoundType.STONE, ToolType.PICKAXE, 1, true));
        CRYS_SANDSTONE_SMOOTH_SLAB = PokecubeLegends.BLOCKS_TAB.register("ultra_sandstone_smooth_slab", () -> new SlabBlock(AbstractBlock.Properties.of(
        		Material.STONE, MaterialColor.SNOW).strength(2.0F, 3.0f).sound(SoundType.SAND)
        			.harvestTool(ToolType.PICKAXE).requiresCorrectToolForDrops()));
        CRYS_SANDSTONE_SMOOTH_STAIRS = PokecubeLegends.BLOCKS_TAB.register("ultra_sandstone_smooth_stairs",
                () -> new ItemGenerator.GenericStairs(Blocks.STONE_STAIRS.defaultBlockState(), AbstractBlock.Properties.of(
        		Material.STONE, MaterialColor.SNOW).strength(2.0F, 3.0f).sound(SoundType.SAND)
            		.harvestTool(ToolType.PICKAXE).requiresCorrectToolForDrops()));
        CRYS_SANDSTONE_BUTTON = PokecubeLegends.BLOCKS_TAB.register("ultra_sandstone_button",
                () -> new ItemGenerator.GenericWoodButton(AbstractBlock.Properties.of(Material.STONE, MaterialColor.SAND).sound(SoundType.SAND)
                        .noCollission().strength(0.5F).harvestTool(ToolType.PICKAXE).requiresCorrectToolForDrops()));
        CRYS_SANDSTONE_PR_PLATE = PokecubeLegends.BLOCKS_TAB.register("ultra_sandstone_pressure_plate",
                () -> new ItemGenerator.GenericPressurePlate(PressurePlateBlock.Sensitivity.EVERYTHING, AbstractBlock.Properties
                        .of(Material.STONE, MaterialColor.SNOW).sound(SoundType.SOUL_SAND).noCollission().strength(
                                0.7F).harvestTool(ToolType.PICKAXE).requiresCorrectToolForDrops()));
        
        // Distortic World
        DISTORTIC_GRASS = PokecubeLegends.BLOCKS_TAB.register("distortic_grass", () -> new GrassDistorticBlock(
                AbstractBlock.Properties.of(Material.GRASS, MaterialColor.TERRACOTTA_PINK).sound(SoundType.NETHER_WART).strength(1, 2)
                        .harvestTool(ToolType.SHOVEL).harvestLevel(1).requiresCorrectToolForDrops().randomTicks()));
        DISTORTIC_CRACKED_STONE = PokecubeLegends.BLOCKS_TAB.register("distortic_cracked_stone", () -> new DistorticCrackedStone(
            AbstractBlock.Properties.of(Material.STONE, MaterialColor.TERRACOTTA_BLACK).sound(SoundType.STONE).strength(1, 2)
                .harvestTool(ToolType.PICKAXE).harvestLevel(2).requiresCorrectToolForDrops()));
        DISTORTIC_STONE = PokecubeLegends.BLOCKS_TAB.register("distortic_stone", () -> new DistorticStoneBlock(AbstractBlock.Properties.of(
        		Material.STONE, MaterialColor.TERRACOTTA_BLACK).sound(SoundType.STONE)
                .strength(1.5f).harvestTool(ToolType.PICKAXE).harvestLevel(1).requiresCorrectToolForDrops()));
        FRACTAL_ORE = PokecubeLegends.BLOCKS_TAB.register("fractal_ore", () -> new OreBlock(AbstractBlock.Properties.of(
            Material.STONE, MaterialColor.TERRACOTTA_BLACK).sound(SoundType.STONE).strength(3.0F, 3.0f).requiresCorrectToolForDrops()));
        DISTORTIC_STONE_SLAB = PokecubeLegends.BLOCKS_TAB.register("distortic_stone_slab", () -> new SlabBlock(AbstractBlock.Properties.of(
        		Material.STONE, MaterialColor.TERRACOTTA_BLACK).strength(2.0F, 3.0f).sound(SoundType.STONE)
        		.harvestTool(ToolType.PICKAXE).requiresCorrectToolForDrops()));
        DISTORTIC_STONE_STAIRS = PokecubeLegends.BLOCKS_TAB.register("distortic_stone_stairs",
                () -> new ItemGenerator.GenericStairs(Blocks.STONE_STAIRS.defaultBlockState(), AbstractBlock.Properties.of(
                		Material.STONE, MaterialColor.TERRACOTTA_BLACK).strength(2.0F, 3.0f).sound(SoundType.STONE)
                		.harvestTool(ToolType.PICKAXE).requiresCorrectToolForDrops()));
        DISTORTIC_MIRROR = PokecubeLegends.BLOCKS_TAB.register("distortic_mirror", () -> new BlockBase(Material.GLASS, MaterialColor.SNOW,
        		0.3f, 0.3f, SoundType.GLASS, ToolType.PICKAXE, 3, true));

        // Woods (LOGS/LEAVES/PLANKS)
        // Inverted Blocks
        INVERTED_LEAVES = PokecubeLegends.BLOCKS_TAB.register("ultra_leave01", () -> new LeavesBlock(AbstractBlock.Properties.of(
                Material.LEAVES, MaterialColor.COLOR_LIGHT_BLUE).strength(0.2f).sound(SoundType.GRASS).noOcclusion()
                .isSuffocating((s, r, p)-> false).isValidSpawn(ItemGenerator::ocelotOrParrot).isViewBlocking((s, r, p) -> false)));
        INVERTED_LOG = PokecubeLegends.BLOCKS_TAB.register("ultra_log01", () -> Blocks.log(
                MaterialColor.TERRACOTTA_LIGHT_BLUE, MaterialColor.TERRACOTTA_LIGHT_BLUE));
        INVERTED_WOOD = PokecubeLegends.BLOCKS_TAB.register("inverted_wood", () -> Blocks.log(
                MaterialColor.TERRACOTTA_LIGHT_BLUE, MaterialColor.TERRACOTTA_LIGHT_BLUE));
        STRIP_INVERTED_LOG = PokecubeLegends.BLOCKS_TAB.register("stripped_inverted_log", () -> Blocks.log(
                MaterialColor.TERRACOTTA_LIGHT_BLUE, MaterialColor.TERRACOTTA_LIGHT_BLUE));
        STRIP_INVERTED_WOOD = PokecubeLegends.BLOCKS_TAB.register("stripped_inverted_wood", () -> Blocks.log(
                MaterialColor.TERRACOTTA_LIGHT_BLUE, MaterialColor.TERRACOTTA_LIGHT_BLUE));
        INVERTED_PLANKS = PokecubeLegends.BLOCKS_TAB.register("ultra_plank01", () -> new Block(AbstractBlock.Properties.of(
        		Material.WOOD, MaterialColor.TERRACOTTA_LIGHT_BLUE).strength(2.0f, 3.0f).sound(SoundType.WOOD)));
        INVERTED_STAIRS = PokecubeLegends.BLOCKS_TAB.register("inverted_stairs",
                () -> new ItemGenerator.GenericStairs(Blocks.OAK_STAIRS.defaultBlockState(), AbstractBlock.Properties.of(
                		Material.WOOD, MaterialColor.TERRACOTTA_LIGHT_BLUE).strength(2.0f, 3.0f).sound(SoundType.WOOD)));
        INVERTED_SLAB = PokecubeLegends.BLOCKS_TAB.register("inverted_slab", () -> new SlabBlock(AbstractBlock.Properties.of(
        		Material.WOOD, MaterialColor.TERRACOTTA_LIGHT_BLUE).strength(2.0f, 3.0f).sound(SoundType.WOOD)));
        INVERTED_FENCE = PokecubeLegends.BLOCKS_TAB.register("inverted_fence", () -> new FenceBlock(AbstractBlock.Properties.of(
        		Material.WOOD, MaterialColor.TERRACOTTA_LIGHT_BLUE).strength(2.0f, 3.0f).sound(SoundType.WOOD)));
        INVERTED_FENCE_GATE = PokecubeLegends.BLOCKS_TAB.register("inverted_fence_gate", () -> new FenceGateBlock(AbstractBlock.Properties.of(
        		Material.WOOD, MaterialColor.TERRACOTTA_LIGHT_BLUE).strength(2.0f, 3.0f).sound(SoundType.WOOD)));
        INVERTED_PR_PLATE = PokecubeLegends.BLOCKS_TAB.register("inverted_pressure_plate",
                () -> new ItemGenerator.GenericPressurePlate(PressurePlateBlock.Sensitivity.EVERYTHING, AbstractBlock.Properties
                        .of(Material.WOOD, MaterialColor.TERRACOTTA_LIGHT_BLUE).sound(SoundType.WOOD).noCollission().strength(
                                0.5f)));
        INVERTED_BUTTON = PokecubeLegends.BLOCKS_TAB.register("inverted_button",
                () -> new ItemGenerator.GenericWoodButton(AbstractBlock.Properties.of(Material.WOOD, MaterialColor.TERRACOTTA_LIGHT_BLUE).sound(SoundType.WOOD)
                        .noCollission().strength(0.5f)));
        INVERTED_TRAPDOOR = PokecubeLegends.BLOCKS_TAB.register("inverted_trapdoor",
                () -> new ItemGenerator.GenericTrapDoor(AbstractBlock.Properties.of(Material.WOOD, MaterialColor.TERRACOTTA_LIGHT_BLUE)
                        .sound(SoundType.WOOD).strength(2.0f, 3.0f).noOcclusion()));
        INVERTED_DOOR = PokecubeLegends.BLOCKS_TAB.register("inverted_door", () -> new ItemGenerator.GenericDoor(
                AbstractBlock.Properties.of(Material.WOOD, MaterialColor.TERRACOTTA_LIGHT_BLUE).sound(SoundType.WOOD).strength(
                        2.0f, 3.0f).noOcclusion()));

        // Temporal Blocks
        TEMPORAL_LEAVES = PokecubeLegends.BLOCKS_TAB.register("ultra_leave02", () -> new LeavesBlock(AbstractBlock.Properties.of(
                Material.LEAVES, MaterialColor.WARPED_NYLIUM).strength(0.2f).sound(SoundType.GRASS).noOcclusion()
                .isSuffocating((s, r, p)-> false).isValidSpawn(ItemGenerator::ocelotOrParrot).isViewBlocking((s, r, p) -> false)));
        TEMPORAL_LOG = PokecubeLegends.BLOCKS_TAB.register("ultra_log02", () -> Blocks.log(
                MaterialColor.WARPED_NYLIUM, MaterialColor.COLOR_BROWN));
        TEMPORAL_WOOD = PokecubeLegends.BLOCKS_TAB.register("temporal_wood", () -> Blocks.log(
                MaterialColor.WARPED_NYLIUM, MaterialColor.COLOR_BROWN));
        STRIP_TEMPORAL_LOG = PokecubeLegends.BLOCKS_TAB.register("stripped_temporal_log", () -> Blocks.log(
                MaterialColor.WARPED_NYLIUM, MaterialColor.WARPED_NYLIUM));
        STRIP_TEMPORAL_WOOD = PokecubeLegends.BLOCKS_TAB.register("stripped_temporal_wood", () -> Blocks.log(
                MaterialColor.WARPED_NYLIUM, MaterialColor.WARPED_NYLIUM));
        TEMPORAL_PLANKS = PokecubeLegends.BLOCKS_TAB.register("ultra_plank02", () -> new Block(AbstractBlock.Properties.of(
        		Material.WOOD, MaterialColor.WARPED_NYLIUM).strength(2.0f).sound(SoundType.WOOD)));
        TEMPORAL_STAIRS = PokecubeLegends.BLOCKS_TAB.register("temporal_stairs",
                () -> new ItemGenerator.GenericStairs(Blocks.OAK_STAIRS.defaultBlockState(), AbstractBlock.Properties.of(
                		Material.WOOD, MaterialColor.WARPED_NYLIUM).strength(2.0f).sound(SoundType.WOOD)));
        TEMPORAL_SLAB = PokecubeLegends.BLOCKS_TAB.register("temporal_slab", () -> new SlabBlock(AbstractBlock.Properties.of(
        		Material.WOOD, MaterialColor.WARPED_NYLIUM).strength(2.0f).sound(SoundType.WOOD)));
        TEMPORAL_FENCE = PokecubeLegends.BLOCKS_TAB.register("temporal_fence", () -> new FenceBlock(AbstractBlock.Properties.of(
        		Material.WOOD, MaterialColor.WARPED_NYLIUM).strength(2.0f).sound(SoundType.WOOD)));
        TEMPORAL_FENCE_GATE = PokecubeLegends.BLOCKS_TAB.register("temporal_fence_gate", () -> new FenceGateBlock(AbstractBlock.Properties.of(
        		Material.WOOD, MaterialColor.WARPED_NYLIUM).strength(2.0f).sound(SoundType.WOOD)));
        TEMPORAL_PR_PLATE = PokecubeLegends.BLOCKS_TAB.register("temporal_pressure_plate",
                () -> new ItemGenerator.GenericPressurePlate(PressurePlateBlock.Sensitivity.EVERYTHING, AbstractBlock.Properties
                        .of(Material.WOOD, MaterialColor.WARPED_NYLIUM).sound(SoundType.WOOD).noCollission().strength(
                                0.5f)));
        TEMPORAL_BUTTON = PokecubeLegends.BLOCKS_TAB.register("temporal_button",
                () -> new ItemGenerator.GenericWoodButton(AbstractBlock.Properties.of(Material.WOOD, MaterialColor.WARPED_NYLIUM).sound(SoundType.WOOD)
                        .noCollission().strength(0.5f)));
        TEMPORAL_TRAPDOOR = PokecubeLegends.BLOCKS_TAB.register("temporal_trapdoor",
                () -> new ItemGenerator.GenericTrapDoor(AbstractBlock.Properties.of(Material.WOOD, MaterialColor.WARPED_NYLIUM)
                        .sound(SoundType.WOOD).strength(2.0f, 3.0f).noOcclusion()));
        TEMPORAL_DOOR = PokecubeLegends.BLOCKS_TAB.register("temporal_door", () -> new ItemGenerator.GenericDoor(
                AbstractBlock.Properties.of(Material.WOOD, MaterialColor.WARPED_NYLIUM).sound(SoundType.WOOD).strength(
                        2.0f, 3.0f).noOcclusion()));

        // Aged Blocks
        AGED_LEAVES = PokecubeLegends.BLOCKS_TAB.register("ultra_leave03", () -> new LeavesBlock(AbstractBlock.Properties.of(
                Material.LEAVES, MaterialColor.COLOR_ORANGE).strength(0.2f).sound(SoundType.GRASS).noOcclusion()
                .isSuffocating((s, r, p)-> false).isValidSpawn(ItemGenerator::ocelotOrParrot).isViewBlocking((s, r, p) -> false)));
        AGED_LOG = PokecubeLegends.BLOCKS_TAB.register("ultra_log03", () -> Blocks.log(
                MaterialColor.COLOR_BROWN, MaterialColor.COLOR_BROWN));
        AGED_WOOD = PokecubeLegends.BLOCKS_TAB.register("aged_wood", () -> Blocks.log(MaterialColor.COLOR_BROWN,
                MaterialColor.COLOR_BROWN));
        STRIP_AGED_LOG = PokecubeLegends.BLOCKS_TAB.register("stripped_aged_log", () -> Blocks.log(
                MaterialColor.COLOR_BROWN, MaterialColor.COLOR_BROWN));
        STRIP_AGED_WOOD = PokecubeLegends.BLOCKS_TAB.register("stripped_aged_wood", () -> Blocks.log(
                MaterialColor.COLOR_BROWN, MaterialColor.COLOR_BROWN));
        AGED_PLANKS = PokecubeLegends.BLOCKS_TAB.register("ultra_plank03", () -> new Block(AbstractBlock.Properties.of(
        		Material.WOOD, MaterialColor.COLOR_BROWN).strength(2.0f).sound(SoundType.WOOD)));
        AGED_STAIRS = PokecubeLegends.BLOCKS_TAB.register("aged_stairs", () -> new ItemGenerator.GenericStairs(
                Blocks.OAK_STAIRS.defaultBlockState(), AbstractBlock.Properties.of(
                		Material.WOOD, MaterialColor.COLOR_BROWN).strength(2.0f).sound(SoundType.WOOD)));
        AGED_SLAB = PokecubeLegends.BLOCKS_TAB.register("aged_slab", () -> new SlabBlock(AbstractBlock.Properties.of(
        		Material.WOOD, MaterialColor.COLOR_BROWN).strength(2.0f).sound(SoundType.WOOD)));
        AGED_FENCE = PokecubeLegends.BLOCKS_TAB.register("aged_fence", () -> new FenceBlock(AbstractBlock.Properties.of(
        		Material.WOOD, MaterialColor.COLOR_BROWN).strength(2.0f).sound(SoundType.WOOD)));
        AGED_FENCE_GATE = PokecubeLegends.BLOCKS_TAB.register("aged_fence_gate", () -> new FenceGateBlock(AbstractBlock.Properties.of(
        		Material.WOOD, MaterialColor.COLOR_BROWN).strength(2.0f).sound(SoundType.WOOD)));
        AGED_PR_PLATE = PokecubeLegends.BLOCKS_TAB.register("aged_pressure_plate",
                () -> new ItemGenerator.GenericPressurePlate(PressurePlateBlock.Sensitivity.EVERYTHING, AbstractBlock.Properties
                        .of(Material.WOOD, MaterialColor.COLOR_BROWN).sound(SoundType.WOOD).noCollission().strength(
                                0.5f)));
        AGED_BUTTON = PokecubeLegends.BLOCKS_TAB.register("aged_button", () -> new ItemGenerator.GenericWoodButton(
                AbstractBlock.Properties.of(Material.WOOD, MaterialColor.COLOR_BROWN).sound(SoundType.WOOD).noCollission()
                        .strength(0.5f)));
        AGED_TRAPDOOR = PokecubeLegends.BLOCKS_TAB.register("aged_trapdoor", () -> new ItemGenerator.GenericTrapDoor(
                AbstractBlock.Properties.of(Material.WOOD, MaterialColor.COLOR_BROWN).sound(SoundType.WOOD).strength(
                        2.0f, 3.0f).noOcclusion()));
        AGED_DOOR = PokecubeLegends.BLOCKS_TAB.register("aged_door", () -> new ItemGenerator.GenericDoor(
                AbstractBlock.Properties.of(Material.WOOD, MaterialColor.COLOR_BROWN).sound(SoundType.WOOD).strength(
                        2.0f, 3.0f).noOcclusion()));

        // Distortic Blocks
        DISTORTIC_LEAVES = PokecubeLegends.BLOCKS_TAB.register("distortic_leave", () -> new LeavesBlock(AbstractBlock.Properties.of(
                Material.LEAVES, MaterialColor.COLOR_PURPLE).strength(0.2f).sound(SoundType.GRASS).noOcclusion()
                .isSuffocating((s, r, p)-> false).isValidSpawn(ItemGenerator::ocelotOrParrot).isViewBlocking((s, r, p) -> false)));
        DISTORTIC_LOG = PokecubeLegends.BLOCKS_TAB.register("distortic_log", () -> Blocks.log(
                MaterialColor.COLOR_BLUE, MaterialColor.COLOR_BLUE));
        DISTORTIC_WOOD = PokecubeLegends.BLOCKS_TAB.register("distortic_wood", () -> Blocks.log(
                MaterialColor.COLOR_BLUE, MaterialColor.COLOR_BLUE));
        STRIP_DISTORTIC_LOG = PokecubeLegends.BLOCKS_TAB.register("stripped_distortic_log", () -> Blocks.log(
                MaterialColor.COLOR_BLUE, MaterialColor.COLOR_BLUE));
        STRIP_DISTORTIC_WOOD = PokecubeLegends.BLOCKS_TAB.register("stripped_distortic_wood", () -> Blocks
                .log(MaterialColor.COLOR_BLUE, MaterialColor.COLOR_BLUE));
        DISTORTIC_PLANKS = PokecubeLegends.BLOCKS_TAB.register("distortic_plank", () -> new Block(AbstractBlock.Properties.of(
        		Material.WOOD, MaterialColor.COLOR_BLUE).strength(2.0f).sound(SoundType.WOOD)));
        DISTORTIC_STAIRS = PokecubeLegends.BLOCKS_TAB.register("distortic_stairs",
                () -> new ItemGenerator.GenericStairs(Blocks.OAK_STAIRS.defaultBlockState(), AbstractBlock.Properties.of(
                		Material.WOOD, MaterialColor.COLOR_BLUE).strength(2.0f).sound(SoundType.WOOD)));
        DISTORTIC_SLAB = PokecubeLegends.BLOCKS_TAB.register("distortic_slab", () -> new SlabBlock(AbstractBlock.Properties.of(
        		Material.WOOD, MaterialColor.COLOR_BLUE).strength(2.0f).sound(SoundType.WOOD)));
        DISTORTIC_FENCE = PokecubeLegends.BLOCKS_TAB.register("distortic_fence", () -> new FenceBlock(AbstractBlock.Properties.of(
        		Material.WOOD, MaterialColor.COLOR_BLUE).strength(2.0f).sound(SoundType.WOOD)));
        DISTORTIC_FENCE_GATE = PokecubeLegends.BLOCKS_TAB.register("distortic_fence_gate", () -> new FenceGateBlock(AbstractBlock.Properties.of(
        		Material.WOOD, MaterialColor.COLOR_BLUE).strength(2.0f).sound(SoundType.WOOD)));
        DISTORTIC_PR_PLATE = PokecubeLegends.BLOCKS_TAB.register("distortic_pressure_plate",
                () -> new ItemGenerator.GenericPressurePlate(PressurePlateBlock.Sensitivity.EVERYTHING, AbstractBlock.Properties
                        .of(Material.WOOD, MaterialColor.COLOR_BLUE).sound(SoundType.WOOD).noCollission().strength(
                                0.5f)));
        DISTORTIC_BUTTON = PokecubeLegends.BLOCKS_TAB.register("distortic_button",
                () -> new ItemGenerator.GenericWoodButton(AbstractBlock.Properties.of(Material.WOOD, MaterialColor.COLOR_BLUE).sound(SoundType.WOOD)
                        .noCollission().strength(0.5f)));
        DISTORTIC_TRAPDOOR = PokecubeLegends.BLOCKS_TAB.register("distortic_trapdoor",
                () -> new ItemGenerator.GenericTrapDoor(AbstractBlock.Properties.of(Material.WOOD, MaterialColor.COLOR_BLUE)
                        .sound(SoundType.WOOD).strength(2.0f, 3.0f).noOcclusion()));
        DISTORTIC_DOOR = PokecubeLegends.BLOCKS_TAB.register("distortic_door", () -> new ItemGenerator.GenericDoor(
                AbstractBlock.Properties.of(Material.WOOD, MaterialColor.COLOR_BLUE).sound(SoundType.WOOD).strength(
                        2.0f, 3.0f).noOcclusion()));
        
        // Corrupted Blocks
        CORRUPTED_LEAVES = PokecubeLegends.BLOCKS_TAB.register("corrupted_leave", () -> new CorruptedLeavesBlock());
        CORRUPTED_LOG = PokecubeLegends.BLOCKS_TAB.register("corrupted_log", () -> Blocks.log(
                MaterialColor.WOOD, MaterialColor.COLOR_GRAY));
        CORRUPTED_WOOD = PokecubeLegends.BLOCKS_TAB.register("corrupted_wood", () -> Blocks.log(
                MaterialColor.COLOR_GRAY, MaterialColor.COLOR_GRAY));
        STRIP_CORRUPTED_LOG = PokecubeLegends.BLOCKS_TAB.register("stripped_corrupted_log", () -> Blocks.log(
                MaterialColor.WOOD, MaterialColor.WOOD));
        STRIP_CORRUPTED_WOOD = PokecubeLegends.BLOCKS_TAB.register("stripped_corrupted_wood", () -> Blocks
                .log(MaterialColor.WOOD, MaterialColor.WOOD));
        CORRUPTED_PLANKS = PokecubeLegends.BLOCKS_TAB.register("corrupted_plank", () -> new Block(AbstractBlock.Properties.of(
        		Material.WOOD, MaterialColor.WOOD).strength(2.0f).sound(SoundType.WOOD)));
        CORRUPTED_STAIRS = PokecubeLegends.BLOCKS_TAB.register("corrupted_stairs",
                () -> new ItemGenerator.GenericStairs(Blocks.OAK_STAIRS.defaultBlockState(), AbstractBlock.Properties.of(
                		Material.WOOD, MaterialColor.WOOD).strength(2.0f).sound(SoundType.WOOD)));
        CORRUPTED_SLAB = PokecubeLegends.BLOCKS_TAB.register("corrupted_slab", () -> new SlabBlock(AbstractBlock.Properties.of(
        		Material.WOOD, MaterialColor.WOOD).strength(2.0F).sound(SoundType.WOOD)));
        CORRUPTED_FENCE = PokecubeLegends.BLOCKS_TAB.register("corrupted_fence", () -> new FenceBlock(AbstractBlock.Properties.of(
        		Material.WOOD, MaterialColor.WOOD).strength(2.0f).sound(SoundType.WOOD)));
        CORRUPTED_FENCE_GATE = PokecubeLegends.BLOCKS_TAB.register("corrupted_fence_gate", () -> new FenceGateBlock(AbstractBlock.Properties.of(
        		Material.WOOD, MaterialColor.WOOD).strength(2.0f).sound(SoundType.WOOD)));
        CORRUPTED_PR_PLATE = PokecubeLegends.BLOCKS_TAB.register("corrupted_pressure_plate",
                () -> new ItemGenerator.GenericPressurePlate(PressurePlateBlock.Sensitivity.MOBS, AbstractBlock.Properties
                        .of(Material.WOOD, MaterialColor.WOOD).sound(SoundType.WOOD).noCollission().strength(
                                0.5f)));
        CORRUPTED_BUTTON = PokecubeLegends.BLOCKS_TAB.register("corrupted_button",
                () -> new ItemGenerator.GenericWoodButton(AbstractBlock.Properties.of(Material.WOOD, MaterialColor.WOOD).sound(SoundType.WOOD)
                        .noCollission().strength(0.5f)));
        CORRUPTED_TRAPDOOR = PokecubeLegends.BLOCKS_TAB.register("corrupted_trapdoor",
                () -> new ItemGenerator.GenericTrapDoor(AbstractBlock.Properties.of(Material.WOOD, MaterialColor.WOOD)
                        .sound(SoundType.WOOD).strength(2.0f, 3.0f).noOcclusion()));
        CORRUPTED_DOOR = PokecubeLegends.BLOCKS_TAB.register("corrupted_door", () -> new ItemGenerator.GenericDoor(
                AbstractBlock.Properties.of(Material.WOOD, MaterialColor.WOOD).sound(SoundType.WOOD).strength(
                        2.0f, 3.0f).noOcclusion()));
        
        // MIRAGE Blocks
        MIRAGE_GLASS = PokecubeLegends.BLOCKS_TAB.register("mirage_glass", () -> new GlassBlockBase(AbstractBlock.Properties.copy(Blocks.GLASS).noOcclusion()));
        MIRAGE_LEAVES = PokecubeLegends.BLOCKS_TAB.register("mirage_leave", () -> new MirageLeavesBlock(9032191,
            AbstractBlock.Properties.of(Material.LEAVES, MaterialColor.COLOR_LIGHT_BLUE).sound(SoundType.NYLIUM).strength(0.2f).noOcclusion()
                .isSuffocating((s, r, p)-> false).isValidSpawn(ItemGenerator::ocelotOrParrot).isViewBlocking((s, r, p) -> false)));
        MIRAGE_LOG = PokecubeLegends.BLOCKS_TAB.register("mirage_log", () -> Blocks.log(
                MaterialColor.SAND, MaterialColor.COLOR_LIGHT_BLUE));
        MIRAGE_WOOD = PokecubeLegends.BLOCKS_TAB.register("mirage_wood", () -> Blocks.log(
                MaterialColor.COLOR_LIGHT_BLUE, MaterialColor.COLOR_LIGHT_BLUE));
        STRIP_MIRAGE_LOG = PokecubeLegends.BLOCKS_TAB.register("stripped_mirage_log", () -> Blocks.log(
                MaterialColor.SAND, MaterialColor.SNOW));
        STRIP_MIRAGE_WOOD = PokecubeLegends.BLOCKS_TAB.register("stripped_mirage_wood", () -> Blocks
                .log(MaterialColor.SNOW, MaterialColor.SNOW));
        MIRAGE_PLANKS = PokecubeLegends.BLOCKS_TAB.register("mirage_plank", () -> new Block(AbstractBlock.Properties.of(
        		Material.WOOD, MaterialColor.SAND).strength(2.0f).sound(SoundType.WOOD)));
        MIRAGE_STAIRS = PokecubeLegends.BLOCKS_TAB.register("mirage_stairs",
                () -> new ItemGenerator.GenericStairs(Blocks.OAK_STAIRS.defaultBlockState(), AbstractBlock.Properties.of(
                		Material.WOOD, MaterialColor.SAND).strength(2.0f).sound(SoundType.WOOD)));
        MIRAGE_SLAB = PokecubeLegends.BLOCKS_TAB.register("mirage_slab", () -> new SlabBlock(AbstractBlock.Properties.of(
        		Material.WOOD, MaterialColor.SAND).strength(2.0f).sound(SoundType.WOOD)));
        MIRAGE_FENCE = PokecubeLegends.BLOCKS_TAB.register("mirage_fence", () -> new FenceBlock(AbstractBlock.Properties.of(
        		Material.WOOD, MaterialColor.SAND).strength(2.0f).sound(SoundType.WOOD)));
        MIRAGE_FENCE_GATE = PokecubeLegends.BLOCKS_TAB.register("mirage_fence_gate", () -> new FenceGateBlock(AbstractBlock.Properties.of(
        		Material.WOOD, MaterialColor.SAND).strength(2.0f).sound(SoundType.WOOD)));
        MIRAGE_PR_PLATE = PokecubeLegends.BLOCKS_TAB.register("mirage_pressure_plate",
                () -> new ItemGenerator.GenericPressurePlate(PressurePlateBlock.Sensitivity.MOBS, AbstractBlock.Properties
                        .of(Material.WOOD, MaterialColor.SAND).sound(SoundType.WOOD).noCollission().strength(
                                0.5f)));
        MIRAGE_BUTTON = PokecubeLegends.BLOCKS_TAB.register("mirage_button",
                () -> new ItemGenerator.GenericWoodButton(AbstractBlock.Properties.of(Material.WOOD, MaterialColor.SAND).sound(SoundType.WOOD)
                        .noCollission().strength(0.5f)));
        MIRAGE_TRAPDOOR = PokecubeLegends.BLOCKS_TAB.register("mirage_trapdoor",
                () -> new ItemGenerator.GenericTrapDoor(AbstractBlock.Properties.of(Material.WOOD, MaterialColor.SAND)
                        .sound(SoundType.WOOD).strength(2.0f, 3.0f).noOcclusion()));
        MIRAGE_DOOR = PokecubeLegends.BLOCKS_TAB.register("mirage_door", () -> new ItemGenerator.GenericDoor(
                AbstractBlock.Properties.of(Material.WOOD, MaterialColor.SAND).sound(SoundType.WOOD).strength(
                        2.0f, 3.0f).noOcclusion()));
        // Ultra Metal
        ULTRA_METAL = PokecubeLegends.BLOCKS_TAB.register("ultrablock", () -> new BlockBase("ultrablock", Material.METAL,MaterialColor.COLOR_LIGHT_GREEN,
            5.0f, 10f, SoundType.STONE, ToolType.PICKAXE, 2, true));
        ULTRA_METAL_SLAB = PokecubeLegends.BLOCKS_TAB.register("ultra_metal_slab", () -> new SlabBlock(AbstractBlock.Properties.of(
            Material.STONE, MaterialColor.COLOR_LIGHT_GRAY).strength(2.0F, 3.0f).sound(SoundType.STONE)
            .harvestTool(ToolType.PICKAXE).requiresCorrectToolForDrops()));
        ULTRA_METAL_STAIRS = PokecubeLegends.BLOCKS_TAB.register("ultra_metal_stairs",
            () -> new ItemGenerator.GenericStairs(Blocks.STONE_STAIRS.defaultBlockState(), AbstractBlock.Properties.of(
                Material.STONE, MaterialColor.COLOR_LIGHT_GREEN).strength(2.0F, 3.0f).sound(SoundType.STONE)
                .harvestTool(ToolType.PICKAXE).requiresCorrectToolForDrops()));
        ULTRA_METAL_BUTTON = PokecubeLegends.BLOCKS_TAB.register("ultra_metal_button",
            () -> new ItemGenerator.GenericWoodButton(AbstractBlock.Properties.of(Material.METAL, MaterialColor.COLOR_LIGHT_GREEN).sound(SoundType.METAL)
                .noCollission().strength(0.5F).harvestTool(ToolType.PICKAXE).requiresCorrectToolForDrops()));
        ULTRA_METAL_PR_PLATE = PokecubeLegends.BLOCKS_TAB.register("ultra_metal_pressure_plate",
            () -> new ItemGenerator.GenericPressurePlate(PressurePlateBlock.Sensitivity.MOBS, AbstractBlock.Properties.of(
                Material.METAL, MaterialColor.COLOR_LIGHT_GREEN).sound(SoundType.METAL)
                .noCollission().strength(0.7F).harvestTool(ToolType.PICKAXE).requiresCorrectToolForDrops()));

        // Plants
        INVERTED_SAPLING = PokecubeLegends.BLOCKS_TAB.register("ultra_sapling01", () -> new SaplingBase(
            () -> new InvertedTree(), AbstractBlock.Properties.of(Material.PLANT, MaterialColor.COLOR_BLUE)
            .strength(0f, 1f).sound(SoundType.GRASS).noCollission().noOcclusion()));
        TEMPORAL_SAPLING = PokecubeLegends.BLOCKS_TAB.register("ultra_sapling02", () -> new SaplingBase(
            () -> new TemporalTree(), AbstractBlock.Properties.of(Material.PLANT, MaterialColor.PLANT)
            .strength(0f, 1f).sound(SoundType.GRASS).noCollission().noOcclusion()));
        AGED_SAPLING = PokecubeLegends.BLOCKS_TAB.register("ultra_sapling03", () -> new SaplingBase(
            () -> new AgedTree(), AbstractBlock.Properties.of(Material.PLANT, MaterialColor.COLOR_YELLOW)
            .strength(0f, 1f).sound(SoundType.GRASS).noCollission().noOcclusion()));
        DISTORTIC_SAPLING = PokecubeLegends.BLOCKS_TAB.register("distortic_sapling", () -> new SaplingBase(
            () -> new DistorticTree(), AbstractBlock.Properties.of(Material.PLANT, MaterialColor.COLOR_PURPLE)
            .strength(0f, 1f).sound(SoundType.GRASS).noCollission().noOcclusion()));
        CORRUPTED_SAPLING = PokecubeLegends.BLOCKS_TAB.register("corrupted_sapling", () -> new SaplingBase(
            () -> new CorruptedTree(), AbstractBlock.Properties.of(Material.PLANT, MaterialColor.COLOR_BLACK)
            .strength(0f, 1f).sound(SoundType.GRASS).noCollission().noOcclusion()));
        MIRAGE_SAPLING = PokecubeLegends.BLOCKS_TAB.register("mirage_sapling", () -> new SaplingBase(
            () -> new MirageTree(), AbstractBlock.Properties.of(Material.PLANT, MaterialColor.SAND)
            .strength(0f, 1f).sound(SoundType.GRASS).noCollission().noOcclusion()));

        CRYSTALLIZED_CACTUS = PokecubeLegends.BLOCKS_TAB.register("crystallized_cactus", () -> new CrystallizedCactus(AbstractBlock.Properties.of(
            Material.CACTUS, MaterialColor.SNOW).sound(SoundType.GLASS).strength(0.4f)));
        CRYSTALLIZED_BUSH = PokecubeLegends.BLOCKS_TAB.register("crystallized_bush", () -> new CrystallizedBush(AbstractBlock.Properties.of(
            Material.PLANT, MaterialColor.SNOW).sound(SoundType.GLASS).noCollission().instabreak()));
        TALL_CRYSTALLIZED_BUSH = PokecubeLegends.BLOCKS_TAB.register("tall_crystallized_bush", () -> new TallCrystallizedBush(AbstractBlock.Properties.of(
            Material.PLANT, MaterialColor.SNOW).sound(SoundType.GLASS).noCollission().instabreak()));

        // Dimensions
        SPECTRUM_GLASS = PokecubeLegends.BLOCKS_TAB.register("spectrum_glass", () -> new GlassBlockBase(AbstractBlock.Properties.copy(Blocks.GLASS).noOcclusion()));
        ULTRA_MAGNETIC = PokecubeLegends.BLOCKS_TAB.register("ultramagnetic", () -> new MagneticBlock(Material.STONE, MaterialColor.COLOR_BLUE,
            4f, 3f, SoundType.METAL, ToolType.PICKAXE, 2, true));
        
        // Mirage Spot (Hoopa Ring)
        BLOCK_PORTALWARP = PokecubeLegends.BLOCKS.register("portal", () -> new PortalWarp("portal", AbstractBlock.Properties
                .of(Material.STONE, MaterialColor.GOLD).sound(SoundType.METAL).strength(2000, 2000)).setShape(VoxelShapes
                        .box(0.05, 0, 0.05, 1, 3, 1)).setToolTip("portalwarp"));

        // Legendary Spawns
        GOLEM_STONE = PokecubeLegends.BLOCKS.register("golem_stone", () -> new BlockBase(Material.STONE,MaterialColor.TERRACOTTA_WHITE,
        		5f, 10f, SoundType.STONE, ToolType.PICKAXE, 2, true));

        LEGENDARY_SPAWN = PokecubeLegends.BLOCKS.register("legendaryspawn", () -> new BlockBase(Material.METAL, MaterialColor.GOLD,
                50f, 30f, SoundType.METAL, ToolType.PICKAXE, 3, true));
        TROUGH_BLOCK = PokecubeLegends.BLOCKS.register("trough_block", () -> new TroughBlock(AbstractBlock.Properties.of(
        		Material.METAL, MaterialColor.COLOR_BROWN).strength(5, 15).harvestTool(ToolType.PICKAXE)
                .harvestLevel(2).sound(SoundType.ANVIL).lightLevel(b -> 4).dynamicShape().requiresCorrectToolForDrops()));
        HEATRAN_BLOCK 	= PokecubeLegends.BLOCKS.register("heatran_block", () -> new HeatranBlock(AbstractBlock.Properties.of(
        		Material.STONE, MaterialColor.NETHER).strength(5, 15).harvestTool(ToolType.PICKAXE)
                .harvestLevel(2).sound(SoundType.NETHER_BRICKS).lightLevel(b -> 4).dynamicShape().emissiveRendering((s, r, p) -> true).requiresCorrectToolForDrops()));
        TAO_BLOCK 	= PokecubeLegends.BLOCKS.register("blackwhite_block", () -> new TaoTrioBlock(AbstractBlock.Properties.of(
        		Material.HEAVY_METAL, MaterialColor.SNOW).strength(5, 15).sound(SoundType.FUNGUS)
        		.dynamicShape()).setShape(VoxelShapes.box(0.05, 0, 0.05, 1, 1, 1)));
        
        // Regi Cores
        REGISTEEL_CORE = PokecubeLegends.BLOCKS.register("registeel_spawn", () -> new FaceBlock_Base(Material.METAL, MaterialColor.TERRACOTTA_WHITE, 
                15, 10f, SoundType.METAL, ToolType.PICKAXE, 2, true));
        REGICE_CORE = PokecubeLegends.BLOCKS.register("regice_spawn", () -> new FaceBlock_Base(Material.ICE_SOLID, MaterialColor.TERRACOTTA_WHITE, 
                15, 10f, SoundType.GLASS, ToolType.PICKAXE, 2, true));
        REGIROCK_CORE = PokecubeLegends.BLOCKS.register("regirock_spawn", () -> new FaceBlock_Base(Material.STONE, MaterialColor.TERRACOTTA_WHITE, 
                15, 10f, SoundType.STONE, ToolType.PICKAXE, 2, true));
        REGIELEKI_CORE = PokecubeLegends.BLOCKS.register("regieleki_spawn", () -> new FaceBlock_Base(Material.STONE, MaterialColor.TERRACOTTA_WHITE, 
                15, 10f, SoundType.STONE, ToolType.PICKAXE, 2, true));
        REGIDRAGO_CORE = PokecubeLegends.BLOCKS.register("regidrago_spawn", () -> new FaceBlock_Base(Material.CLAY, MaterialColor.TERRACOTTA_WHITE, 
                15, 10f, SoundType.STONE, ToolType.PICKAXE, 2, true));
        REGIGIGA_CORE = PokecubeLegends.BLOCKS.register("regigiga_spawn", () -> new FaceBlock_Base(Material.HEAVY_METAL, MaterialColor.TERRACOTTA_WHITE, 
                15, 10f, SoundType.METAL, ToolType.PICKAXE, 2, true));
        
        // Tapus
        TAPU_KOKO_CORE 	= PokecubeLegends.BLOCKS.register("koko_core", () -> new TapuKokoCore(AbstractBlock.Properties.of(
        		Material.STONE, MaterialColor.TERRACOTTA_YELLOW).strength(5, 15)
        		.sound(SoundType.BASALT).dynamicShape().harvestTool(ToolType.AXE).requiresCorrectToolForDrops()));
        TAPU_BULU_CORE 	= PokecubeLegends.BLOCKS.register("bulu_core", () -> new TapuBuluCore(AbstractBlock.Properties.of(
        		Material.STONE, MaterialColor.TERRACOTTA_RED).strength(5, 15)
        		.sound(SoundType.BASALT).dynamicShape().harvestTool(ToolType.AXE).requiresCorrectToolForDrops()));
        TAPU_LELE_CORE 	= PokecubeLegends.BLOCKS.register("lele_core", () -> new TapuLeleCore(AbstractBlock.Properties.of(
        		Material.STONE, MaterialColor.TERRACOTTA_PURPLE).strength(5, 15)
        		.sound(SoundType.BASALT).dynamicShape().harvestTool(ToolType.AXE).requiresCorrectToolForDrops()));
        TAPU_FINI_CORE 	= PokecubeLegends.BLOCKS.register("fini_core", () -> new TapuFiniCore(AbstractBlock.Properties.of(
        		Material.STONE, MaterialColor.TERRACOTTA_PINK).strength(5, 15)
        		.sound(SoundType.BASALT).dynamicShape().harvestTool(ToolType.AXE).requiresCorrectToolForDrops()));
        
        TIMESPACE_CORE = PokecubeLegends.BLOCKS.register("timerspawn", () -> new TimeSpaceCoreBlock(AbstractBlock.Properties.of(
        		Material.GRASS, MaterialColor.STONE).strength(2000, 2000).sound(SoundType.STONE)
                        .dynamicShape()).setShape(VoxelShapes.box(0.05, 0, 0.05, 1, 2, 1)));
        NATURE_CORE = PokecubeLegends.BLOCKS.register("naturespawn", () -> new NatureCoreBlock(AbstractBlock.Properties.of(
        		Material.STONE, MaterialColor.TERRACOTTA_WHITE).strength(2000, 2000).sound(SoundType.STONE)
                        .dynamicShape()).setShape(VoxelShapes.box(0.05, 0, 0.05, 1, 2, 1)));
        KELDEO_CORE = PokecubeLegends.BLOCKS.register("keldeoblock", () -> new KeldeoBlock(AbstractBlock.Properties.of(
        		Material.STONE, MaterialColor.COLOR_BLUE).strength(2000, 2000).sound(SoundType.STONE)
                        .dynamicShape()).setShape(VoxelShapes.box(0.05, 0, 0.05, 1, 1, 1)));
        VICTINI_CORE = PokecubeLegends.BLOCKS.register("victiniblock", () -> new VictiniBlock(AbstractBlock.Properties.of(
        		Material.METAL, MaterialColor.GOLD).strength(5, 15).harvestTool(ToolType.PICKAXE)
                        .harvestLevel(2).sound(SoundType.ANVIL).dynamicShape().requiresCorrectToolForDrops()).setShape(VoxelShapes.box(0.05, 0,
                                0.05, 1, 1, 1)));
        YVELTAL_CORE = PokecubeLegends.BLOCKS.register("yveltal_egg", () -> new YveltalEgg(AbstractBlock.Properties.of(
        		Material.METAL, MaterialColor.COLOR_BLACK).strength(2000, 2000).sound(SoundType.WOOD)
                        .dynamicShape()).setShape(VoxelShapes.box(0.05, 0, 0.05, 1, 2, 1)));
        XERNEAS_CORE = PokecubeLegends.BLOCKS.register("xerneas_tree", () -> new XerneasCore(AbstractBlock.Properties.of(
        		Material.METAL, MaterialColor.SNOW).strength(2000, 2000).sound(SoundType.WOOD)
                        .dynamicShape()).setShape(VoxelShapes.box(0.05, 0, 0.05, 1, 2, 1)));
        MAGEARNA_BLOCK = PokecubeLegends.BLOCKS.register("magearna_block", () -> new MagearnaBlock(AbstractBlock.Properties.of(
        		Material.STONE, MaterialColor.SAND).strength(300, 300).sound(SoundType.STONE).harvestTool(ToolType.PICKAXE)
        		.requiresCorrectToolForDrops().dynamicShape()));
        
        // Plants
        DISTORTIC_VINES = PokecubeLegends.BLOCKS_TAB.register("distortic_vines", () -> new DistortedVinesTopBlock(AbstractBlock.Properties.of(
        		Material.PLANT, MaterialColor.COLOR_MAGENTA).randomTicks().noCollission().instabreak().sound(SoundType.WEEPING_VINES)));
        DISTORTIC_VINES_PLANT = PokecubeLegends.BLOCKS_TAB.register("distortic_vines_plant", () -> new DistortedVinesBlock(AbstractBlock.Properties.of(
        		Material.PLANT, MaterialColor.COLOR_MAGENTA).noCollission().instabreak().sound(SoundType.WEEPING_VINES)));

        // Decorations Creative Tab - Sorting depends on the order the blocks are listed in
        // Torches
        INFECTED_TORCH = PokecubeLegends.DECORATION_TAB.register("ultra_torch1", () -> new InfectedTorch());
        INFECTED_TORCH_WALL = PokecubeLegends.DECORATION_TAB.register("ultra_torch1_wall", () -> new InfectedTorchWall());

        RUBY_ORE = PokecubeLegends.DECORATION_TAB.register("ruby_ore", () -> new OreBlock(AbstractBlock.Properties.of(
            Material.STONE, MaterialColor.STONE).sound(SoundType.STONE).strength(3.0F, 3.0f).requiresCorrectToolForDrops()));
        RUBY_BLOCK = PokecubeLegends.DECORATION_TAB.register("ruby_block", () -> new BlockBase(Material.METAL, MaterialColor.COLOR_RED,
            1.5f, 10, SoundType.METAL, ToolType.PICKAXE, 1, true));
        RUBY_SLAB = PokecubeLegends.DECORATION_TAB.register("ruby_slab", () -> new SlabBlock(AbstractBlock.Properties.of(
            Material.METAL, MaterialColor.COLOR_RED).strength(2.0F, 3.0f).sound(SoundType.METAL).harvestTool(
            ToolType.PICKAXE).harvestLevel(2).requiresCorrectToolForDrops()));
        RUBY_STAIRS = PokecubeLegends.DECORATION_TAB.register("ruby_stairs",
            () -> new ItemGenerator.GenericStairs(Blocks.STONE_STAIRS.defaultBlockState(), AbstractBlock.Properties.of(
                Material.METAL, MaterialColor.COLOR_RED).strength(2.0F, 3.0f).sound(SoundType.METAL).harvestTool(
                ToolType.PICKAXE).harvestLevel(2).requiresCorrectToolForDrops()));

        SAPPHIRE_ORE = PokecubeLegends.DECORATION_TAB.register("sapphire_ore", () -> new OreBlock(AbstractBlock.Properties.of(
            Material.STONE, MaterialColor.STONE).sound(SoundType.STONE).strength(3.0F, 3.0f).requiresCorrectToolForDrops()));
        SAPPHIRE_BLOCK = PokecubeLegends.DECORATION_TAB.register("sapphire_block", () -> new BlockBase(Material.METAL, MaterialColor.COLOR_BLUE,
            1.5f, 10, SoundType.METAL, ToolType.PICKAXE, 2, true));
        SAPPHIRE_SLAB = PokecubeLegends.DECORATION_TAB.register("sapphire_slab", () -> new SlabBlock(AbstractBlock.Properties.of(
            Material.METAL, MaterialColor.COLOR_BLUE).strength(2.0F, 3.0f).sound(SoundType.METAL).harvestTool(
            ToolType.PICKAXE).harvestLevel(2).requiresCorrectToolForDrops()));
        SAPPHIRE_STAIRS = PokecubeLegends.DECORATION_TAB.register("sapphire_stairs",
            () -> new ItemGenerator.GenericStairs(Blocks.STONE_STAIRS.defaultBlockState(), AbstractBlock.Properties.of(
                Material.METAL, MaterialColor.COLOR_BLUE).strength(2.0F, 3.0f).sound(SoundType.METAL).harvestTool(
                ToolType.PICKAXE).harvestLevel(2).requiresCorrectToolForDrops()));

        SPECTRUM_BLOCK = PokecubeLegends.DECORATION_TAB.register("spectrum_block", () -> new BlockBase(Material.METAL, MaterialColor.COLOR_ORANGE,
            5.0f, 7, SoundType.METAL, ToolType.PICKAXE, 2, true));
        SPECTRUM_SLAB = PokecubeLegends.DECORATION_TAB.register("spectrum_slab", () -> new SlabBlock(AbstractBlock.Properties.of(
            Material.METAL, MaterialColor.COLOR_ORANGE).strength(2.0F, 3.0f).sound(SoundType.METAL).harvestTool(
            ToolType.PICKAXE).harvestLevel(2).requiresCorrectToolForDrops()));
        SPECTRUM_STAIRS = PokecubeLegends.DECORATION_TAB.register("spectrum_stairs",
            () -> new ItemGenerator.GenericStairs(Blocks.STONE_STAIRS.defaultBlockState(), AbstractBlock.Properties.of(
                Material.METAL, MaterialColor.COLOR_ORANGE).strength(2.0F, 3.0f).sound(SoundType.METAL).harvestTool(
                ToolType.PICKAXE).harvestLevel(2).requiresCorrectToolForDrops()));

        // Meteor Ore
        METEOR_COSMIC_DUST_ORE = PokecubeLegends.DECORATION_TAB.register("cosmic_dust_ore", () -> new MeteorCosmicOreBlock(6842513, AbstractBlock.Properties.of(
            Material.STONE, MaterialColor.TERRACOTTA_BLUE).sound(SoundType.STONE).strength(3.0F, 3.0f).requiresCorrectToolForDrops()));
        COSMIC_DUST_BLOCK = PokecubeLegends.DECORATION_TAB.register("cosmic_dust_block", () -> new SandBlock(2730984,
            AbstractBlock.Properties.of(Material.SAND, MaterialColor.COLOR_LIGHT_BLUE).sound(SoundType.SAND)
                .strength(0.5f).harvestTool(ToolType.SHOVEL).harvestLevel(1)));

        FRACTAL_BLOCK = PokecubeLegends.DECORATION_TAB.register("fractal_block", () -> new BlockBase(Material.METAL, MaterialColor.COLOR_LIGHT_BLUE,
            3f, 12, SoundType.GLASS, ToolType.PICKAXE, 2, true));

        DISTORTIC_CHISELED_MIRROR = PokecubeLegends.DECORATION_TAB.register("distortic_chiseled_mirror", () -> new BlockBase(Material.GLASS, MaterialColor.SNOW,
            1.5f, 1.5f, SoundType.GLASS, ToolType.PICKAXE, 1, true));

        DISTORTIC_GLOWSTONE = PokecubeLegends.DECORATION_TAB.register("distortic_glowstone", () -> new BlockBase(Material.STONE, MaterialColor.COLOR_ORANGE,
            1.5f, 1.5f, SoundType.STONE, ToolType.PICKAXE, 2, true));

        DISTORTIC_TERRACOTTA = PokecubeLegends.DECORATION_TAB.register("distortic_terracotta", () -> new BlockBase(Material.STONE, MaterialColor.COLOR_ORANGE,
            2.0f, 3.0f, SoundType.STONE, ToolType.PICKAXE, 2, true));
        DISTORTIC_TERRACOTTA_SLAB = PokecubeLegends.DECORATION_TAB.register("distortic_terracotta_slab", () -> new SlabBlock(AbstractBlock.Properties.of(
            Material.STONE, MaterialColor.TERRACOTTA_BLACK).strength(2.0F, 3.0f).sound(SoundType.STONE)
            .harvestTool(ToolType.PICKAXE).requiresCorrectToolForDrops()));
        DISTORTIC_TERRACOTTA_STAIRS = PokecubeLegends.DECORATION_TAB.register("distortic_terracotta_stairs",
            () -> new ItemGenerator.GenericStairs(Blocks.STONE_STAIRS.defaultBlockState(), AbstractBlock.Properties.of(
                Material.STONE, MaterialColor.TERRACOTTA_BLACK).strength(2.0F, 3.0f).sound(SoundType.STONE)
                .harvestTool(ToolType.PICKAXE).requiresCorrectToolForDrops()));

        DISTORTIC_OAK_PLANKS = PokecubeLegends.DECORATION_TAB.register("distortic_oak_planks", () -> new Block(AbstractBlock.Properties.of(
            Material.WOOD, MaterialColor.WOOD).strength(2.0f).sound(SoundType.WOOD).harvestTool(ToolType.AXE)));
        DISTORTIC_OAK_STAIRS = PokecubeLegends.DECORATION_TAB.register("distortic_oak_stairs",
            () -> new ItemGenerator.GenericStairs(Blocks.OAK_STAIRS.defaultBlockState(), AbstractBlock.Properties.of(
                Material.WOOD, MaterialColor.WOOD).strength(2.0f).sound(SoundType.WOOD).harvestTool(ToolType.AXE)));
        DISTORTIC_OAK_SLAB = PokecubeLegends.DECORATION_TAB.register("distortic_oak_slab", () -> new SlabBlock(AbstractBlock.Properties.of(
            Material.WOOD, MaterialColor.WOOD).strength(2.0f).sound(SoundType.WOOD).harvestTool(ToolType.AXE)));

        DISTORTIC_DARK_OAK_PLANKS = PokecubeLegends.DECORATION_TAB.register("distortic_dark_oak_planks", () -> new Block(AbstractBlock.Properties.of(
            Material.WOOD, MaterialColor.WOOD).strength(2.0f).sound(SoundType.WOOD).harvestTool(ToolType.AXE)));
        DISTORTIC_DARK_OAK_STAIRS = PokecubeLegends.DECORATION_TAB.register("distortic_dark_oak_stairs",
            () -> new ItemGenerator.GenericStairs(Blocks.OAK_STAIRS.defaultBlockState(), AbstractBlock.Properties.of(
                Material.WOOD, MaterialColor.WOOD).strength(2.0f).sound(SoundType.WOOD).harvestTool(ToolType.AXE)));
        DISTORTIC_DARK_OAK_SLAB = PokecubeLegends.DECORATION_TAB.register("distortic_dark_oak_slab", () -> new SlabBlock(AbstractBlock.Properties.of(
            Material.WOOD, MaterialColor.WOOD).strength(2.0f).sound(SoundType.WOOD).harvestTool(ToolType.AXE)));

        DISTORTIC_SPRUCE_PLANKS = PokecubeLegends.DECORATION_TAB.register("distortic_spruce_planks", () -> new Block(AbstractBlock.Properties.of(
            Material.WOOD, MaterialColor.WOOD).strength(2.0f).sound(SoundType.WOOD).harvestTool(ToolType.AXE)));
        DISTORTIC_SPRUCE_STAIRS = PokecubeLegends.DECORATION_TAB.register("distortic_spruce_stairs",
            () -> new ItemGenerator.GenericStairs(Blocks.OAK_STAIRS.defaultBlockState(), AbstractBlock.Properties.of(
                Material.WOOD, MaterialColor.WOOD).strength(2.0f).sound(SoundType.WOOD).harvestTool(ToolType.AXE)));
        DISTORTIC_SPRUCE_SLAB = PokecubeLegends.DECORATION_TAB.register("distortic_spruce_slab", () -> new SlabBlock(AbstractBlock.Properties.of(
            Material.WOOD, MaterialColor.WOOD).strength(2.0f).sound(SoundType.WOOD).harvestTool(ToolType.AXE)));

        DISTORTIC_BIRCH_PLANKS = PokecubeLegends.DECORATION_TAB.register("distortic_birch_planks", () -> new Block(AbstractBlock.Properties.of(
            Material.WOOD, MaterialColor.WOOD).strength(2.0f).sound(SoundType.WOOD).harvestTool(ToolType.AXE)));
        DISTORTIC_BIRCH_STAIRS = PokecubeLegends.DECORATION_TAB.register("distortic_birch_stairs",
            () -> new ItemGenerator.GenericStairs(Blocks.OAK_STAIRS.defaultBlockState(), AbstractBlock.Properties.of(
                Material.WOOD, MaterialColor.WOOD).strength(2.0f).sound(SoundType.WOOD).harvestTool(ToolType.AXE)));
        DISTORTIC_BIRCH_SLAB = PokecubeLegends.DECORATION_TAB.register("distortic_birch_slab", () -> new SlabBlock(AbstractBlock.Properties.of(
            Material.WOOD, MaterialColor.WOOD).strength(2.0f).sound(SoundType.WOOD).harvestTool(ToolType.AXE)));

        DISTORTIC_ACACIA_PLANKS = PokecubeLegends.DECORATION_TAB.register("distortic_acacia_planks", () -> new Block(AbstractBlock.Properties.of(
            Material.WOOD, MaterialColor.WOOD).strength(2.0f).sound(SoundType.WOOD).harvestTool(ToolType.AXE)));
        DISTORTIC_ACACIA_STAIRS = PokecubeLegends.DECORATION_TAB.register("distortic_acacia_stairs",
            () -> new ItemGenerator.GenericStairs(Blocks.OAK_STAIRS.defaultBlockState(), AbstractBlock.Properties.of(
                Material.WOOD, MaterialColor.WOOD).strength(2.0f).sound(SoundType.WOOD).harvestTool(ToolType.AXE)));
        DISTORTIC_ACACIA_SLAB = PokecubeLegends.DECORATION_TAB.register("distortic_acacia_slab", () -> new SlabBlock(AbstractBlock.Properties.of(
            Material.WOOD, MaterialColor.WOOD).strength(2.0f).sound(SoundType.WOOD).harvestTool(ToolType.AXE)));

        DISTORTIC_JUNGLE_PLANKS = PokecubeLegends.DECORATION_TAB.register("distortic_jungle_planks", () -> new Block(AbstractBlock.Properties.of(
            Material.WOOD, MaterialColor.WOOD).strength(2.0f).sound(SoundType.WOOD).harvestTool(ToolType.AXE)));
        DISTORTIC_JUNGLE_STAIRS = PokecubeLegends.DECORATION_TAB.register("distortic_jungle_stairs",
            () -> new ItemGenerator.GenericStairs(Blocks.OAK_STAIRS.defaultBlockState(), AbstractBlock.Properties.of(
                Material.WOOD, MaterialColor.WOOD).strength(2.0f).sound(SoundType.WOOD).harvestTool(ToolType.AXE)));
        DISTORTIC_JUNGLE_SLAB = PokecubeLegends.DECORATION_TAB.register("distortic_jungle_slab", () -> new SlabBlock(AbstractBlock.Properties.of(
            Material.WOOD, MaterialColor.WOOD).strength(2.0f).sound(SoundType.WOOD).harvestTool(ToolType.AXE)));

        //Concrete Blocks
        CONCRETE_LOG = PokecubeLegends.DECORATION_TAB.register("concrete_log", () -> ItemGenerator.stoneLog(
            MaterialColor.SNOW, MaterialColor.COLOR_GRAY));
        CONCRETE_WOOD = PokecubeLegends.DECORATION_TAB.register("concrete_wood", () -> ItemGenerator.stoneLog(
            MaterialColor.COLOR_GRAY, MaterialColor.COLOR_GRAY));
        STRIP_CONCRETE_LOG = PokecubeLegends.DECORATION_TAB.register("stripped_concrete_log", () -> ItemGenerator.stoneLog(
            MaterialColor.SNOW, MaterialColor.SNOW));
        STRIP_CONCRETE_WOOD = PokecubeLegends.DECORATION_TAB.register("stripped_concrete_wood", () -> ItemGenerator.stoneLog(
            MaterialColor.SNOW, MaterialColor.SNOW));
        CONCRETE_PLANKS = PokecubeLegends.DECORATION_TAB.register("concrete_plank", () -> new Block(AbstractBlock.Properties.of(
            Material.STONE, MaterialColor.SNOW).strength(2.4f).sound(SoundType.STONE).requiresCorrectToolForDrops()));
        CONCRETE_STAIRS = PokecubeLegends.DECORATION_TAB.register("concrete_stairs",() -> new ItemGenerator.GenericStairs(Blocks.OAK_STAIRS.defaultBlockState(),
            AbstractBlock.Properties.of(Material.STONE, MaterialColor.SNOW).strength(2.4f).sound(SoundType.STONE).requiresCorrectToolForDrops()));
        CONCRETE_SLAB = PokecubeLegends.DECORATION_TAB.register("concrete_slab", () -> new SlabBlock(AbstractBlock.Properties.of(
            Material.STONE, MaterialColor.SNOW).strength(2.4f).sound(SoundType.STONE).requiresCorrectToolForDrops()));
        CONCRETE_DENSE_PLANKS = PokecubeLegends.DECORATION_TAB.register("concrete_dense_plank", () -> new Block(AbstractBlock.Properties.of(
            Material.STONE, MaterialColor.SNOW).strength(3.0f).sound(SoundType.STONE).requiresCorrectToolForDrops()));

        CONCRETE_DENSE_STAIRS = PokecubeLegends.DECORATION_TAB.register("concrete_dense_stairs",() -> new ItemGenerator.GenericStairs(Blocks.OAK_STAIRS.defaultBlockState(),
            AbstractBlock.Properties.of(Material.STONE, MaterialColor.SNOW).strength(3.0f).sound(SoundType.STONE).requiresCorrectToolForDrops()));
        CONCRETE_DENSE_SLAB = PokecubeLegends.DECORATION_TAB.register("concrete_dense_slab", () -> new SlabBlock(AbstractBlock.Properties.of(
            Material.STONE, MaterialColor.SNOW).strength(3.0f).sound(SoundType.STONE).requiresCorrectToolForDrops()));
        CONCRETE_FENCE = PokecubeLegends.DECORATION_TAB.register("concrete_fence", () -> new FenceBlock(AbstractBlock.Properties.of(
            Material.STONE, MaterialColor.SNOW).strength(2.4f).sound(SoundType.STONE).requiresCorrectToolForDrops()));
        CONCRETE_FENCE_GATE = PokecubeLegends.DECORATION_TAB.register("concrete_fence_gate", () -> new FenceGateBlock(AbstractBlock.Properties.of(
            Material.STONE, MaterialColor.SNOW).strength(2.4f).sound(SoundType.STONE).requiresCorrectToolForDrops()));
        CONCRETE_DENSE_WALL = PokecubeLegends.DECORATION_TAB.register("concrete_dense_wall", () -> new WallBlock(AbstractBlock.Properties.of(
            Material.STONE, MaterialColor.SNOW).strength(3.0f).sound(SoundType.STONE).requiresCorrectToolForDrops()));
        CONCRETE_DENSE_WALL_GATE = PokecubeLegends.DECORATION_TAB.register("concrete_dense_wall_gate", () -> new WallGateBlock(AbstractBlock.Properties.of(
            Material.STONE, MaterialColor.SNOW).strength(3.0f).sound(SoundType.STONE).requiresCorrectToolForDrops()));
        CONCRETE_PR_PLATE = PokecubeLegends.DECORATION_TAB.register("concrete_pressure_plate",
            () -> new ItemGenerator.GenericPressurePlate(PressurePlateBlock.Sensitivity.EVERYTHING, AbstractBlock.Properties
                .of(Material.STONE, MaterialColor.SNOW).sound(SoundType.STONE).noCollission().strength(
                    0.5f).requiresCorrectToolForDrops()));
        CONCRETE_BUTTON = PokecubeLegends.DECORATION_TAB.register("concrete_button",
            () -> new ItemGenerator.GenericWoodButton(AbstractBlock.Properties.of(Material.STONE, MaterialColor.SNOW)
                .sound(SoundType.STONE).noCollission().strength(0.5f).requiresCorrectToolForDrops()));
        CONCRETE_DENSE_PR_PLATE = PokecubeLegends.DECORATION_TAB.register("concrete_dense_pressure_plate",
            () -> new ItemGenerator.GenericPressurePlate(PressurePlateBlock.Sensitivity.MOBS, AbstractBlock.Properties
                .of(Material.STONE, MaterialColor.SNOW).sound(SoundType.STONE).noCollission().strength(
                    0.8f).requiresCorrectToolForDrops()));
        CONCRETE_DENSE_BUTTON = PokecubeLegends.DECORATION_TAB.register("concrete_dense_button",
            () -> new ItemGenerator.GenericStoneButton(AbstractBlock.Properties.of(Material.STONE, MaterialColor.SNOW)
                .sound(SoundType.STONE).noCollission().strength(0.8f).requiresCorrectToolForDrops()));
        CONCRETE_TRAPDOOR = PokecubeLegends.DECORATION_TAB.register("concrete_trapdoor",
            () -> new ItemGenerator.GenericTrapDoor(AbstractBlock.Properties.of(Material.STONE, MaterialColor.SNOW)
                .sound(SoundType.STONE).strength(2.0f, 3.0f).noOcclusion().requiresCorrectToolForDrops()));
        CONCRETE_DOOR = PokecubeLegends.DECORATION_TAB.register("concrete_door", () -> new ItemGenerator.GenericDoor(
            AbstractBlock.Properties.of(Material.STONE, MaterialColor.SNOW).sound(SoundType.STONE).strength(
                2.0f, 3.0f).noOcclusion().requiresCorrectToolForDrops()));

        OCEAN_BRICK = PokecubeLegends.DECORATION_TAB.register("oceanbrick", () -> new BlockBase(Material.STONE, MaterialColor.COLOR_CYAN,
            1.5f, 10f, SoundType.STONE, ToolType.PICKAXE, 1, true));
        OCEAN_BRICK_SLAB = PokecubeLegends.DECORATION_TAB.register("ocean_brick_slab", () -> new SlabBlock(AbstractBlock.Properties.of(
            Material.STONE, MaterialColor.COLOR_CYAN).strength(2.0F, 10f).sound(SoundType.STONE)
            .harvestTool(ToolType.PICKAXE).requiresCorrectToolForDrops()));
        OCEAN_BRICK_STAIRS = PokecubeLegends.DECORATION_TAB.register("ocean_brick_stairs",
            () -> new GenericStairs(Blocks.STONE_STAIRS.defaultBlockState(), AbstractBlock.Properties.of(
                Material.STONE, MaterialColor.COLOR_CYAN).strength(2.0F, 10f).sound(SoundType.STONE)
                .harvestTool(ToolType.PICKAXE).requiresCorrectToolForDrops()));

        SKY_BRICK   = PokecubeLegends.DECORATION_TAB.register("skybrick", () -> new BlockBase(Material.STONE, MaterialColor.COLOR_BLUE,
            1.5f, 10f, SoundType.STONE, ToolType.PICKAXE, 1, true));
        SKY_BRICK_SLAB = PokecubeLegends.DECORATION_TAB.register("sky_brick_slab", () -> new SlabBlock(AbstractBlock.Properties.of(
            Material.STONE, MaterialColor.COLOR_BLUE).strength(2.0F, 10f).sound(SoundType.STONE)
            .harvestTool(ToolType.PICKAXE).requiresCorrectToolForDrops()));
        SKY_BRICK_STAIRS = PokecubeLegends.DECORATION_TAB.register("sky_brick_stairs",
            () -> new ItemGenerator.GenericStairs(Blocks.STONE_STAIRS.defaultBlockState(), AbstractBlock.Properties.of(
                Material.STONE, MaterialColor.COLOR_BLUE).strength(2.0F, 10f).sound(SoundType.STONE)
                .harvestTool(ToolType.PICKAXE).requiresCorrectToolForDrops()));

        SPATIAN_BRICK = PokecubeLegends.DECORATION_TAB.register("spatianbrick", () -> new BlockBase(Material.STONE, MaterialColor.COLOR_MAGENTA,
            1.5f, 10f, SoundType.STONE, ToolType.PICKAXE, 1, true));
        SPATIAN_BRICK_SLAB = PokecubeLegends.DECORATION_TAB.register("spatian_brick_slab", () -> new SlabBlock(AbstractBlock.Properties.of(
            Material.STONE, MaterialColor.TERRACOTTA_BLUE).strength(2.0F, 3.0f).sound(SoundType.STONE)
            .harvestTool(ToolType.PICKAXE).requiresCorrectToolForDrops()));
        SPATIAN_BRICK_STAIRS = PokecubeLegends.DECORATION_TAB.register("spatian_brick_stairs",
            () -> new ItemGenerator.GenericStairs(Blocks.STONE_STAIRS.defaultBlockState(), AbstractBlock.Properties.of(
                Material.STONE, MaterialColor.TERRACOTTA_BLUE).strength(2.0F, 3.0f).sound(SoundType.STONE)
                .harvestTool(ToolType.PICKAXE).requiresCorrectToolForDrops()));

        MAGMA_BRICK   = PokecubeLegends.DECORATION_TAB.register("magmabrick", () -> new MagmaBlock(AbstractBlock.Properties.of(
            Material.STONE, MaterialColor.NETHER).strength(1.5f, 10).sound(SoundType.NETHERRACK).lightLevel(b -> 3)
            .emissiveRendering((s, r, p) -> true).harvestTool(ToolType.PICKAXE).requiresCorrectToolForDrops()));
        MAGMA_BRICK_SLAB = PokecubeLegends.DECORATION_TAB.register("magma_brick_slab", () -> new SlabBlock(AbstractBlock.Properties.of(
            Material.STONE, MaterialColor.TERRACOTTA_BLUE).strength(2.0F, 3.0f).sound(SoundType.NETHERRACK).lightLevel(b -> 3)
            .emissiveRendering((s, r, p) -> true).harvestTool(ToolType.PICKAXE).requiresCorrectToolForDrops()));
        MAGMA_BRICK_STAIRS = PokecubeLegends.DECORATION_TAB.register("magma_brick_stairs",
            () -> new ItemGenerator.GenericStairs(Blocks.STONE_STAIRS.defaultBlockState(), AbstractBlock.Properties.of(
                Material.STONE, MaterialColor.TERRACOTTA_BLUE).strength(2.0F, 3.0f).sound(SoundType.NETHERRACK).lightLevel(b -> 3)
                .emissiveRendering((s, r, p) -> true).harvestTool(ToolType.PICKAXE).requiresCorrectToolForDrops()));

        DARKSKY_BRICK = PokecubeLegends.DECORATION_TAB.register("darkskybrick", () -> new BlockBase(Material.STONE, MaterialColor.COLOR_LIGHT_GRAY,
            1.5f, 10f, SoundType.STONE, ToolType.PICKAXE, 1, true));
        DARKSKY_BRICK_SLAB = PokecubeLegends.DECORATION_TAB.register("darksky_brick_slab", () -> new SlabBlock(AbstractBlock.Properties.of(
            Material.STONE, MaterialColor.TERRACOTTA_BLUE).strength(2.0F, 3.0f).sound(SoundType.STONE)
            .harvestTool(ToolType.PICKAXE).requiresCorrectToolForDrops()));
        DARKSKY_BRICK_STAIRS = PokecubeLegends.DECORATION_TAB.register("darksky_brick_stairs",
            () -> new ItemGenerator.GenericStairs(Blocks.STONE_STAIRS.defaultBlockState(), AbstractBlock.Properties.of(
                Material.STONE, MaterialColor.TERRACOTTA_BLUE).strength(2.0F, 3.0f).sound(SoundType.STONE)
                .harvestTool(ToolType.PICKAXE).requiresCorrectToolForDrops()));

        DISTORTIC_STONEBRICK = PokecubeLegends.DECORATION_TAB.register("distortic_stonebrick", () -> new BlockBase(Material.STONE, MaterialColor.TERRACOTTA_BLACK,
            2.5f, 10f, SoundType.STONE, ToolType.PICKAXE, 2, true));
        DISTORTIC_STONEBRICK_SLAB = PokecubeLegends.DECORATION_TAB.register("distortic_stonebrick_slab", () -> new SlabBlock(AbstractBlock.Properties.of(
            Material.STONE, MaterialColor.TERRACOTTA_BLACK).strength(2.0F, 3.0f).sound(SoundType.STONE)
            .harvestTool(ToolType.PICKAXE).requiresCorrectToolForDrops()));
        DISTORTIC_STONEBRICK_STAIRS = PokecubeLegends.DECORATION_TAB.register("distortic_stonebrick_stairs",
            () -> new ItemGenerator.GenericStairs(Blocks.STONE_STAIRS.defaultBlockState(), AbstractBlock.Properties.of(
                Material.STONE, MaterialColor.TERRACOTTA_BLACK).strength(2.0F, 3.0f).sound(SoundType.STONE)
                .harvestTool(ToolType.PICKAXE).requiresCorrectToolForDrops()));

        DISTORTIC_CHISELED_STONE = PokecubeLegends.DECORATION_TAB.register("distortic_chiseled_stone", () -> new BlockBase(Material.STONE, MaterialColor.TERRACOTTA_BLACK,
            2.5f, 10f, SoundType.STONE, ToolType.PICKAXE, 2, true));
        DISTORTIC_CHISELED_SLAB = PokecubeLegends.DECORATION_TAB.register("distortic_chiseled_slab", () -> new SlabBlock(AbstractBlock.Properties.of(
            Material.STONE, MaterialColor.TERRACOTTA_BLACK).strength(2.0F, 3.0f).sound(SoundType.STONE)
            .harvestTool(ToolType.PICKAXE).requiresCorrectToolForDrops()));
        DISTORTIC_CHISELED_STAIRS = PokecubeLegends.DECORATION_TAB.register("distortic_chiseled_stairs",
            () -> new ItemGenerator.GenericStairs(Blocks.STONE_STAIRS.defaultBlockState(), AbstractBlock.Properties.of(
                Material.STONE, MaterialColor.TERRACOTTA_BLACK).strength(2.0F, 3.0f).sound(SoundType.STONE)
                .harvestTool(ToolType.PICKAXE).requiresCorrectToolForDrops()));

        // Unown Stones
        UNOWN_STONE_A = PokecubeLegends.DECORATION_TAB.register("unown_stone_a", () -> new BlockBase(
            Material.STONE, MaterialColor.TERRACOTTA_BLACK, 2f,3f, SoundType.STONE,ToolType.PICKAXE, 2, true));
        UNOWN_STONE_B = PokecubeLegends.DECORATION_TAB.register("unown_stone_b", () -> new BlockBase(
            Material.STONE, MaterialColor.TERRACOTTA_BLACK, 2f,3f, SoundType.STONE,ToolType.PICKAXE, 2, true));
        UNOWN_STONE_C = PokecubeLegends.DECORATION_TAB.register("unown_stone_c", () -> new BlockBase(
            Material.STONE, MaterialColor.TERRACOTTA_BLACK, 2f,3f, SoundType.STONE,ToolType.PICKAXE, 2, true));
        UNOWN_STONE_D = PokecubeLegends.DECORATION_TAB.register("unown_stone_d", () -> new BlockBase(
            Material.STONE, MaterialColor.TERRACOTTA_BLACK, 2f,3f, SoundType.STONE,ToolType.PICKAXE, 2, true));
        UNOWN_STONE_E = PokecubeLegends.DECORATION_TAB.register("unown_stone_e", () -> new BlockBase(
            Material.STONE, MaterialColor.TERRACOTTA_BLACK, 2f,3f, SoundType.STONE,ToolType.PICKAXE, 2, true));
        UNOWN_STONE_F = PokecubeLegends.DECORATION_TAB.register("unown_stone_f", () -> new BlockBase(
            Material.STONE, MaterialColor.TERRACOTTA_BLACK, 2f,3f, SoundType.STONE,ToolType.PICKAXE, 2, true));
        UNOWN_STONE_G = PokecubeLegends.DECORATION_TAB.register("unown_stone_g", () -> new BlockBase(
            Material.STONE, MaterialColor.TERRACOTTA_BLACK, 2f,3f, SoundType.STONE,ToolType.PICKAXE, 2, true));
        UNOWN_STONE_H = PokecubeLegends.DECORATION_TAB.register("unown_stone_h", () -> new BlockBase(
            Material.STONE, MaterialColor.TERRACOTTA_BLACK, 2f,3f, SoundType.STONE,ToolType.PICKAXE, 2, true));
        UNOWN_STONE_I = PokecubeLegends.DECORATION_TAB.register("unown_stone_i", () -> new BlockBase(
            Material.STONE, MaterialColor.TERRACOTTA_BLACK, 2f,3f, SoundType.STONE,ToolType.PICKAXE, 2, true));
        UNOWN_STONE_J = PokecubeLegends.DECORATION_TAB.register("unown_stone_j", () -> new BlockBase(
            Material.STONE, MaterialColor.TERRACOTTA_BLACK, 2f,3f, SoundType.STONE,ToolType.PICKAXE, 2, true));
        UNOWN_STONE_K = PokecubeLegends.DECORATION_TAB.register("unown_stone_k", () -> new BlockBase(
            Material.STONE, MaterialColor.TERRACOTTA_BLACK, 2f,3f, SoundType.STONE,ToolType.PICKAXE, 2, true));
        UNOWN_STONE_L = PokecubeLegends.DECORATION_TAB.register("unown_stone_l", () -> new BlockBase(
            Material.STONE, MaterialColor.TERRACOTTA_BLACK, 2f,3f, SoundType.STONE,ToolType.PICKAXE, 2, true));
        UNOWN_STONE_M = PokecubeLegends.DECORATION_TAB.register("unown_stone_m", () -> new BlockBase(
            Material.STONE, MaterialColor.TERRACOTTA_BLACK, 2f,3f, SoundType.STONE,ToolType.PICKAXE, 2, true));
        UNOWN_STONE_N = PokecubeLegends.DECORATION_TAB.register("unown_stone_n", () -> new BlockBase(
            Material.STONE, MaterialColor.TERRACOTTA_BLACK, 2f,3f, SoundType.STONE,ToolType.PICKAXE, 2, true));
        UNOWN_STONE_O = PokecubeLegends.DECORATION_TAB.register("unown_stone_o", () -> new BlockBase(
            Material.STONE, MaterialColor.TERRACOTTA_BLACK, 2f,3f, SoundType.STONE,ToolType.PICKAXE, 2, true));
        UNOWN_STONE_P = PokecubeLegends.DECORATION_TAB.register("unown_stone_p", () -> new BlockBase(
            Material.STONE, MaterialColor.TERRACOTTA_BLACK, 2f,3f, SoundType.STONE,ToolType.PICKAXE, 2, true));
        UNOWN_STONE_Q = PokecubeLegends.DECORATION_TAB.register("unown_stone_q", () -> new BlockBase(
            Material.STONE, MaterialColor.TERRACOTTA_BLACK, 2f,3f, SoundType.STONE,ToolType.PICKAXE, 2, true));
        UNOWN_STONE_R = PokecubeLegends.DECORATION_TAB.register("unown_stone_r", () -> new BlockBase(
            Material.STONE, MaterialColor.TERRACOTTA_BLACK, 2f,3f, SoundType.STONE,ToolType.PICKAXE, 2, true));
        UNOWN_STONE_S = PokecubeLegends.DECORATION_TAB.register("unown_stone_s", () -> new BlockBase(
            Material.STONE, MaterialColor.TERRACOTTA_BLACK, 2f,3f, SoundType.STONE,ToolType.PICKAXE, 2, true));
        UNOWN_STONE_T = PokecubeLegends.DECORATION_TAB.register("unown_stone_t", () -> new BlockBase(
            Material.STONE, MaterialColor.TERRACOTTA_BLACK, 2f,3f, SoundType.STONE,ToolType.PICKAXE, 2, true));
        UNOWN_STONE_U = PokecubeLegends.DECORATION_TAB.register("unown_stone_u", () -> new BlockBase(
            Material.STONE, MaterialColor.TERRACOTTA_BLACK, 2f,3f, SoundType.STONE,ToolType.PICKAXE, 2, true));
        UNOWN_STONE_V = PokecubeLegends.DECORATION_TAB.register("unown_stone_v", () -> new BlockBase(
            Material.STONE, MaterialColor.TERRACOTTA_BLACK, 2f,3f, SoundType.STONE,ToolType.PICKAXE, 2, true));
        UNOWN_STONE_W = PokecubeLegends.DECORATION_TAB.register("unown_stone_w", () -> new BlockBase(
            Material.STONE, MaterialColor.TERRACOTTA_BLACK, 2f,3f, SoundType.STONE,ToolType.PICKAXE, 2, true));
        UNOWN_STONE_X = PokecubeLegends.DECORATION_TAB.register("unown_stone_x", () -> new BlockBase(
            Material.STONE, MaterialColor.TERRACOTTA_BLACK, 2f,3f, SoundType.STONE,ToolType.PICKAXE, 2, true));
        UNOWN_STONE_Y = PokecubeLegends.DECORATION_TAB.register("unown_stone_y", () -> new BlockBase(
            Material.STONE, MaterialColor.TERRACOTTA_BLACK, 2f,3f, SoundType.STONE,ToolType.PICKAXE, 2, true));
        UNOWN_STONE_Z = PokecubeLegends.DECORATION_TAB.register("unown_stone_z", () -> new BlockBase(
            Material.STONE, MaterialColor.TERRACOTTA_BLACK, 2f,3f, SoundType.STONE,ToolType.PICKAXE, 2, true));
        UNOWN_STONE_EX = PokecubeLegends.DECORATION_TAB.register("unown_stone_ex", () -> new BlockBase(
            Material.STONE, MaterialColor.TERRACOTTA_BLACK, 2f,3f, SoundType.STONE,ToolType.PICKAXE, 2, true));
        UNOWN_STONE_IN = PokecubeLegends.DECORATION_TAB.register("unown_stone_in", () -> new BlockBase(
            Material.STONE, MaterialColor.TERRACOTTA_BLACK, 2f,3f, SoundType.STONE,ToolType.PICKAXE, 2, true));

        //Glass
        DISTORTIC_FRAMED_MIRROR = PokecubeLegends.DECORATION_TAB.register("distortic_framed_mirror", () ->
            new DistorticOneWayStainedGlass(DyeColor.WHITE, AbstractBlock.Properties.of(Material.GLASS, MaterialColor.SNOW)
                .noOcclusion().sound(SoundType.GLASS).strength(0.3f).harvestTool(ToolType.PICKAXE).requiresCorrectToolForDrops()));

        DISTORTIC_OW_GLASS = PokecubeLegends.DECORATION_TAB.register("distortic_one_way_glass", () ->
            new DistorticOneWayGlass(AbstractBlock.Properties.of(Material.GLASS, MaterialColor.SNOW)
                .noOcclusion().sound(SoundType.GLASS).strength(0.3f).harvestTool(ToolType.PICKAXE)
                .requiresCorrectToolForDrops()));
        DISTORTIC_OW_GLASS_WHITE = PokecubeLegends.DECORATION_TAB.register("distortic_one_way_white_stained_glass", () ->
            new DistorticOneWayStainedGlass(DyeColor.WHITE, AbstractBlock.Properties.of(Material.GLASS, MaterialColor.SNOW)
                .noOcclusion().sound(SoundType.GLASS).strength(0.3f).harvestTool(ToolType.PICKAXE).requiresCorrectToolForDrops()));
        DISTORTIC_OW_GLASS_ORANGE = PokecubeLegends.DECORATION_TAB.register("distortic_one_way_orange_stained_glass", () ->
            new DistorticOneWayStainedGlass(DyeColor.ORANGE, AbstractBlock.Properties.of(Material.GLASS, MaterialColor.SNOW)
                .noOcclusion().sound(SoundType.GLASS).strength(0.3f).harvestTool(ToolType.PICKAXE).requiresCorrectToolForDrops()));
        DISTORTIC_OW_GLASS_MAGENTA = PokecubeLegends.DECORATION_TAB.register("distortic_one_way_magenta_stained_glass", () ->
            new DistorticOneWayStainedGlass(DyeColor.MAGENTA, AbstractBlock.Properties.of(Material.GLASS, MaterialColor.SNOW)
                .noOcclusion().sound(SoundType.GLASS).strength(0.3f).harvestTool(ToolType.PICKAXE).requiresCorrectToolForDrops()));
        DISTORTIC_OW_GLASS_LIGHT_BLUE = PokecubeLegends.DECORATION_TAB.register("distortic_one_way_light_blue_stained_glass", () ->
            new DistorticOneWayStainedGlass(DyeColor.LIGHT_BLUE, AbstractBlock.Properties.of(Material.GLASS, MaterialColor.SNOW)
                .noOcclusion().sound(SoundType.GLASS).strength(0.3f).harvestTool(ToolType.PICKAXE).requiresCorrectToolForDrops()));
        DISTORTIC_OW_GLASS_YELLOW = PokecubeLegends.DECORATION_TAB.register("distortic_one_way_yellow_stained_glass", () ->
            new DistorticOneWayStainedGlass(DyeColor.YELLOW, AbstractBlock.Properties.of(Material.GLASS, MaterialColor.SNOW)
                .noOcclusion().sound(SoundType.GLASS).strength(0.3f).harvestTool(ToolType.PICKAXE).requiresCorrectToolForDrops()));
        DISTORTIC_OW_GLASS_LIME = PokecubeLegends.DECORATION_TAB.register("distortic_one_way_lime_stained_glass", () ->
            new DistorticOneWayStainedGlass(DyeColor.LIME, AbstractBlock.Properties.of(Material.GLASS, MaterialColor.SNOW)
                .noOcclusion().sound(SoundType.GLASS).strength(0.3f).harvestTool(ToolType.PICKAXE).requiresCorrectToolForDrops()));
        DISTORTIC_OW_GLASS_PINK = PokecubeLegends.DECORATION_TAB.register("distortic_one_way_pink_stained_glass", () ->
            new DistorticOneWayStainedGlass(DyeColor.PINK, AbstractBlock.Properties.of(Material.GLASS, MaterialColor.SNOW)
                .noOcclusion().sound(SoundType.GLASS).strength(0.3f).harvestTool(ToolType.PICKAXE).requiresCorrectToolForDrops()));
        DISTORTIC_OW_GLASS_GRAY = PokecubeLegends.DECORATION_TAB.register("distortic_one_way_gray_stained_glass", () ->
            new DistorticOneWayStainedGlass(DyeColor.GRAY, AbstractBlock.Properties.of(Material.GLASS, MaterialColor.SNOW)
                .noOcclusion().sound(SoundType.GLASS).strength(0.3f).harvestTool(ToolType.PICKAXE).requiresCorrectToolForDrops()));
        DISTORTIC_OW_GLASS_LIGHT_GRAY = PokecubeLegends.DECORATION_TAB.register("distortic_one_way_light_gray_stained_glass", () ->
            new DistorticOneWayStainedGlass(DyeColor.LIGHT_GRAY, AbstractBlock.Properties.of(Material.GLASS, MaterialColor.SNOW)
                .noOcclusion().sound(SoundType.GLASS).strength(0.3f).harvestTool(ToolType.PICKAXE).requiresCorrectToolForDrops()));
        DISTORTIC_OW_GLASS_CYAN = PokecubeLegends.DECORATION_TAB.register("distortic_one_way_cyan_stained_glass", () ->
            new DistorticOneWayStainedGlass(DyeColor.CYAN, AbstractBlock.Properties.of(Material.GLASS, MaterialColor.SNOW)
                .noOcclusion().sound(SoundType.GLASS).strength(0.3f).harvestTool(ToolType.PICKAXE).requiresCorrectToolForDrops()));
        DISTORTIC_OW_GLASS_PURPLE = PokecubeLegends.DECORATION_TAB.register("distortic_one_way_purple_stained_glass", () ->
            new DistorticOneWayStainedGlass(DyeColor.PURPLE, AbstractBlock.Properties.of(Material.GLASS, MaterialColor.SNOW)
                .noOcclusion().sound(SoundType.GLASS).strength(0.3f).harvestTool(ToolType.PICKAXE).requiresCorrectToolForDrops()));
        DISTORTIC_OW_GLASS_BLUE = PokecubeLegends.DECORATION_TAB.register("distortic_one_way_blue_stained_glass", () ->
            new DistorticOneWayStainedGlass(DyeColor.BLUE, AbstractBlock.Properties.of(Material.GLASS, MaterialColor.SNOW)
                .noOcclusion().sound(SoundType.GLASS).strength(0.3f).harvestTool(ToolType.PICKAXE).requiresCorrectToolForDrops()));
        DISTORTIC_OW_GLASS_BROWN = PokecubeLegends.DECORATION_TAB.register("distortic_one_way_brown_stained_glass", () ->
            new DistorticOneWayStainedGlass(DyeColor.BROWN, AbstractBlock.Properties.of(Material.GLASS, MaterialColor.SNOW)
                .noOcclusion().sound(SoundType.GLASS).strength(0.3f).harvestTool(ToolType.PICKAXE).requiresCorrectToolForDrops()));
        DISTORTIC_OW_GLASS_GREEN = PokecubeLegends.DECORATION_TAB.register("distortic_one_way_green_stained_glass", () ->
            new DistorticOneWayStainedGlass(DyeColor.GREEN, AbstractBlock.Properties.of(Material.GLASS, MaterialColor.SNOW)
                .noOcclusion().sound(SoundType.GLASS).strength(0.3f).harvestTool(ToolType.PICKAXE).requiresCorrectToolForDrops()));
        DISTORTIC_OW_GLASS_RED = PokecubeLegends.DECORATION_TAB.register("distortic_one_way_red_stained_glass", () ->
            new DistorticOneWayStainedGlass(DyeColor.RED, AbstractBlock.Properties.of(Material.GLASS, MaterialColor.SNOW)
                .noOcclusion().sound(SoundType.GLASS).strength(0.3f).harvestTool(ToolType.PICKAXE).requiresCorrectToolForDrops()));
        DISTORTIC_OW_GLASS_BLACK = PokecubeLegends.DECORATION_TAB.register("distortic_one_way_black_stained_glass", () ->
            new DistorticOneWayStainedGlass(DyeColor.BLACK, AbstractBlock.Properties.of(Material.GLASS, MaterialColor.SNOW)
                .noOcclusion().sound(SoundType.GLASS).strength(0.3f).harvestTool(ToolType.PICKAXE).requiresCorrectToolForDrops()));
        DISTORTIC_OW_GLASS_LAB = PokecubeLegends.DECORATION_TAB.register("distortic_one_way_laboratory_glass", () ->
            new DistorticOneWayLaboratoryGlass(DyeColor.WHITE, AbstractBlock.Properties.of(Material.GLASS, MaterialColor.SNOW)
                .noOcclusion().sound(SoundType.GLASS).strength(0.3f).harvestTool(ToolType.PICKAXE).requiresCorrectToolForDrops()));
        DISTORTIC_OW_GLASS_MIRAGE = PokecubeLegends.DECORATION_TAB.register("distortic_one_way_mirage_glass", () ->
            new DistorticOneWayMirageGlass(DyeColor.WHITE, AbstractBlock.Properties.of(Material.GLASS, MaterialColor.SNOW)
                .noOcclusion().sound(SoundType.GLASS).strength(0.3f).harvestTool(ToolType.PICKAXE).requiresCorrectToolForDrops()));
        DISTORTIC_OW_GLASS_SPECTRUM = PokecubeLegends.DECORATION_TAB.register("distortic_one_way_spectrum_glass", () ->
            new DistorticOneWaySpectrumGlass(DyeColor.WHITE, AbstractBlock.Properties.of(Material.GLASS, MaterialColor.SNOW)
                .noOcclusion().sound(SoundType.GLASS).strength(0.3f).harvestTool(ToolType.PICKAXE).requiresCorrectToolForDrops()));
        DISTORTIC_OW_FRAMED_MIRROR = PokecubeLegends.DECORATION_TAB.register("distortic_one_way_framed_mirror", () ->
            new DistorticOneWayStainedGlass(DyeColor.WHITE, AbstractBlock.Properties.of(Material.GLASS, MaterialColor.SNOW)
                .noOcclusion().sound(SoundType.GLASS).strength(0.3f).harvestTool(ToolType.PICKAXE).requiresCorrectToolForDrops()));

        // Decorative_Blocks
        DYNA_LEAVES1 = PokecubeLegends.DECORATION_TAB.register("dyna_leave_1", () -> new DynaLeavesBlock(AbstractBlock.Properties.of(
            Material.LEAVES, MaterialColor.COLOR_PINK).strength(0.2f).sound(SoundType.WET_GRASS).noDrops().noOcclusion()
            .isSuffocating((s, r, p)-> false).isValidSpawn(ItemGenerator::ocelotOrParrot).isViewBlocking((s, r, p) -> false)));
        DYNA_LEAVES2 = PokecubeLegends.DECORATION_TAB.register("dyna_leave_2", () ->  new DynaLeavesBlock(AbstractBlock.Properties.of(
            Material.LEAVES, MaterialColor.COLOR_PINK).strength(0.2f).sound(SoundType.WET_GRASS).noDrops().noOcclusion()
            .isSuffocating((s, r, p)-> false).isValidSpawn(ItemGenerator::ocelotOrParrot).isViewBlocking((s, r, p) -> false)));
        DYNA_LEAVES3 = PokecubeLegends.DECORATION_TAB.register("dyna_leave_3", () ->  new DynaLeavesBlock(AbstractBlock.Properties.of(
            Material.LEAVES, MaterialColor.COLOR_PINK).strength(0.2f).sound(SoundType.WET_GRASS).noDrops().noOcclusion()
            .isSuffocating((s, r, p)-> false).isValidSpawn(ItemGenerator::ocelotOrParrot).isViewBlocking((s, r, p) -> false)));

        //Tapus Totems
        TOTEM_BLOCK = PokecubeLegends.DECORATION_TAB.register("totem_block", () -> new BlockBase(Material.STONE, MaterialColor.COLOR_LIGHT_GRAY,
            1.5f, 10f, SoundType.STONE, ToolType.PICKAXE, 1, true));

        // Koko Totem
        KOKO_WHITE   = PokecubeLegends.DECORATION_TAB.register("koko_white_totem", () -> new KokoTotem(AbstractBlock.Properties.of(Material.WOOD, MaterialColor.TERRACOTTA_WHITE).strength(5, 15)
            .sound(SoundType.WOOD).harvestTool(ToolType.AXE).harvestLevel(2).dynamicShape()));
        KOKO_ORANGE   = PokecubeLegends.DECORATION_TAB.register("koko_orange_totem", () -> new KokoTotem(AbstractBlock.Properties.of(Material.WOOD, MaterialColor.TERRACOTTA_ORANGE).strength(5, 15)
            .sound(SoundType.WOOD).harvestTool(ToolType.AXE).harvestLevel(2).dynamicShape()));
        KOKO_MAGENTA   = PokecubeLegends.DECORATION_TAB.register("koko_magenta_totem", () -> new KokoTotem(AbstractBlock.Properties.of(Material.WOOD, MaterialColor.TERRACOTTA_MAGENTA).strength(5, 15)
            .sound(SoundType.WOOD).harvestTool(ToolType.AXE).harvestLevel(2).dynamicShape()));
        KOKO_LIGHT_BLUE = PokecubeLegends.DECORATION_TAB.register("koko_lightblue_totem", () -> new KokoTotem(AbstractBlock.Properties.of(Material.WOOD, MaterialColor.TERRACOTTA_LIGHT_BLUE).strength(5, 15)
            .sound(SoundType.WOOD).harvestTool(ToolType.AXE).harvestLevel(2).dynamicShape()));
        KOKO_YELLOW   = PokecubeLegends.DECORATION_TAB.register("koko_yellow_totem", () -> new KokoTotem(AbstractBlock.Properties.of(Material.WOOD, MaterialColor.TERRACOTTA_YELLOW).strength(5, 15)
            .sound(SoundType.WOOD).harvestTool(ToolType.AXE).harvestLevel(2).dynamicShape()));
        KOKO_LIME   = PokecubeLegends.DECORATION_TAB.register("koko_lime_totem", () -> new KokoTotem(AbstractBlock.Properties.of(Material.WOOD, MaterialColor.TERRACOTTA_LIGHT_GREEN).strength(5, 15)
            .sound(SoundType.WOOD).harvestTool(ToolType.AXE).harvestLevel(2).dynamicShape()));
        KOKO_PINK   = PokecubeLegends.DECORATION_TAB.register("koko_pink_totem", () -> new KokoTotem(AbstractBlock.Properties.of(Material.WOOD, MaterialColor.TERRACOTTA_PINK).strength(5, 15)
            .sound(SoundType.WOOD).harvestTool(ToolType.AXE).harvestLevel(2).dynamicShape()));
        KOKO_GRAY   = PokecubeLegends.DECORATION_TAB.register("koko_gray_totem", () -> new KokoTotem(AbstractBlock.Properties.of(Material.WOOD, MaterialColor.TERRACOTTA_GRAY).strength(5, 15)
            .sound(SoundType.WOOD).harvestTool(ToolType.AXE).harvestLevel(2).dynamicShape()));
        KOKO_LIGHT_GRAY = PokecubeLegends.DECORATION_TAB.register("koko_lightgray_totem", () -> new KokoTotem(AbstractBlock.Properties.of(Material.WOOD, MaterialColor.TERRACOTTA_LIGHT_GRAY).strength(5, 15)
            .sound(SoundType.WOOD).harvestTool(ToolType.AXE).harvestLevel(2).dynamicShape()));
        KOKO_CYAN   = PokecubeLegends.DECORATION_TAB.register("koko_cyan_totem", () -> new KokoTotem(AbstractBlock.Properties.of(Material.WOOD, MaterialColor.TERRACOTTA_CYAN).strength(5, 15)
            .sound(SoundType.WOOD).harvestTool(ToolType.AXE).harvestLevel(2).dynamicShape()));
        KOKO_PURPLE   = PokecubeLegends.DECORATION_TAB.register("koko_purple_totem", () -> new KokoTotem(AbstractBlock.Properties.of(Material.WOOD, MaterialColor.TERRACOTTA_PURPLE).strength(5, 15)
            .sound(SoundType.WOOD).harvestTool(ToolType.AXE).harvestLevel(2).dynamicShape()));
        KOKO_BLUE   = PokecubeLegends.DECORATION_TAB.register("koko_blue_totem", () -> new KokoTotem(AbstractBlock.Properties.of(Material.WOOD, MaterialColor.TERRACOTTA_BLUE).strength(5, 15)
            .sound(SoundType.WOOD).harvestTool(ToolType.AXE).harvestLevel(2).dynamicShape()));
        KOKO_BROWN   = PokecubeLegends.DECORATION_TAB.register("koko_brown_totem", () -> new KokoTotem(AbstractBlock.Properties.of(Material.WOOD, MaterialColor.TERRACOTTA_BROWN).strength(5, 15)
            .sound(SoundType.WOOD).harvestTool(ToolType.AXE).harvestLevel(2).dynamicShape()));
        KOKO_GREEN   = PokecubeLegends.DECORATION_TAB.register("koko_green_totem", () -> new KokoTotem(AbstractBlock.Properties.of(Material.WOOD, MaterialColor.TERRACOTTA_GREEN).strength(5, 15)
            .sound(SoundType.WOOD).harvestTool(ToolType.AXE).harvestLevel(2).dynamicShape()));
        KOKO_RED   = PokecubeLegends.DECORATION_TAB.register("koko_red_totem", () -> new KokoTotem(AbstractBlock.Properties.of(Material.WOOD, MaterialColor.TERRACOTTA_RED).strength(5, 15)
            .sound(SoundType.WOOD).harvestTool(ToolType.AXE).harvestLevel(2).dynamicShape()));
        KOKO_BLACK   = PokecubeLegends.DECORATION_TAB.register("koko_black_totem", () -> new KokoTotem(AbstractBlock.Properties.of(Material.WOOD, MaterialColor.TERRACOTTA_BLACK).strength(5, 15)
            .sound(SoundType.WOOD).harvestTool(ToolType.AXE).harvestLevel(2).dynamicShape()));

        // Bulu Totem
        BULU_WHITE   = PokecubeLegends.DECORATION_TAB.register("bulu_white_totem", () -> new BuluTotem(AbstractBlock.Properties.of(Material.WOOD, MaterialColor.TERRACOTTA_WHITE).strength(5, 15)
            .sound(SoundType.WOOD).harvestTool(ToolType.AXE).harvestLevel(2).dynamicShape()));
        BULU_ORANGE   = PokecubeLegends.DECORATION_TAB.register("bulu_orange_totem", () -> new BuluTotem(AbstractBlock.Properties.of(Material.WOOD, MaterialColor.TERRACOTTA_ORANGE).strength(5, 15)
            .sound(SoundType.WOOD).harvestTool(ToolType.AXE).harvestLevel(2).dynamicShape()));
        BULU_MAGENTA   = PokecubeLegends.DECORATION_TAB.register("bulu_magenta_totem", () -> new BuluTotem(AbstractBlock.Properties.of(Material.WOOD, MaterialColor.TERRACOTTA_MAGENTA).strength(5, 15)
            .sound(SoundType.WOOD).harvestTool(ToolType.AXE).harvestLevel(2).dynamicShape()));
        BULU_LIGHT_BLUE   = PokecubeLegends.DECORATION_TAB.register("bulu_lightblue_totem", () -> new BuluTotem(AbstractBlock.Properties.of(Material.WOOD, MaterialColor.TERRACOTTA_LIGHT_BLUE).strength(5, 15)
            .sound(SoundType.WOOD).harvestTool(ToolType.AXE).harvestLevel(2).dynamicShape()));
        BULU_YELLOW   = PokecubeLegends.DECORATION_TAB.register("bulu_yellow_totem", () -> new BuluTotem(AbstractBlock.Properties.of(Material.WOOD, MaterialColor.TERRACOTTA_YELLOW).strength(5, 15)
            .sound(SoundType.WOOD).harvestTool(ToolType.AXE).harvestLevel(2).dynamicShape()));
        BULU_LIME   = PokecubeLegends.DECORATION_TAB.register("bulu_lime_totem", () -> new BuluTotem(AbstractBlock.Properties.of(Material.WOOD, MaterialColor.TERRACOTTA_LIGHT_GREEN).strength(5, 15)
            .sound(SoundType.WOOD).harvestTool(ToolType.AXE).harvestLevel(2).dynamicShape()));
        BULU_PINK   = PokecubeLegends.DECORATION_TAB.register("bulu_pink_totem", () -> new BuluTotem(AbstractBlock.Properties.of(Material.WOOD, MaterialColor.TERRACOTTA_PINK).strength(5, 15)
            .sound(SoundType.WOOD).harvestTool(ToolType.AXE).harvestLevel(2).dynamicShape()));
        BULU_GRAY   = PokecubeLegends.DECORATION_TAB.register("bulu_gray_totem", () -> new BuluTotem(AbstractBlock.Properties.of(Material.WOOD, MaterialColor.TERRACOTTA_GRAY).strength(5, 15)
            .sound(SoundType.WOOD).harvestTool(ToolType.AXE).harvestLevel(2).dynamicShape()));
        BULU_LIGHT_GRAY   = PokecubeLegends.DECORATION_TAB.register("bulu_lightgray_totem", () -> new BuluTotem(AbstractBlock.Properties.of(Material.WOOD, MaterialColor.TERRACOTTA_LIGHT_GRAY).strength(5, 15)
            .sound(SoundType.WOOD).harvestTool(ToolType.AXE).harvestLevel(2).dynamicShape()));
        BULU_CYAN   = PokecubeLegends.DECORATION_TAB.register("bulu_cyan_totem", () -> new BuluTotem(AbstractBlock.Properties.of(Material.WOOD, MaterialColor.TERRACOTTA_CYAN).strength(5, 15)
            .sound(SoundType.WOOD).harvestTool(ToolType.AXE).harvestLevel(2).dynamicShape()));
        BULU_PURPLE   = PokecubeLegends.DECORATION_TAB.register("bulu_purple_totem", () -> new BuluTotem(AbstractBlock.Properties.of(Material.WOOD, MaterialColor.TERRACOTTA_PURPLE).strength(5, 15)
            .sound(SoundType.WOOD).harvestTool(ToolType.AXE).harvestLevel(2).dynamicShape()));
        BULU_BLUE   = PokecubeLegends.DECORATION_TAB.register("bulu_blue_totem", () -> new BuluTotem(AbstractBlock.Properties.of(Material.WOOD, MaterialColor.TERRACOTTA_BLUE).strength(5, 15)
            .sound(SoundType.WOOD).harvestTool(ToolType.AXE).harvestLevel(2).dynamicShape()));
        BULU_BROWN   = PokecubeLegends.DECORATION_TAB.register("bulu_brown_totem", () -> new BuluTotem(AbstractBlock.Properties.of(Material.WOOD, MaterialColor.TERRACOTTA_BROWN).strength(5, 15)
            .sound(SoundType.WOOD).harvestTool(ToolType.AXE).harvestLevel(2).dynamicShape()));
        BULU_GREEN   = PokecubeLegends.DECORATION_TAB.register("bulu_green_totem", () -> new BuluTotem(AbstractBlock.Properties.of(Material.WOOD, MaterialColor.TERRACOTTA_GREEN).strength(5, 15)
            .sound(SoundType.WOOD).harvestTool(ToolType.AXE).harvestLevel(2).dynamicShape()));
        BULU_RED   = PokecubeLegends.DECORATION_TAB.register("bulu_red_totem", () -> new BuluTotem(AbstractBlock.Properties.of(Material.WOOD, MaterialColor.TERRACOTTA_RED).strength(5, 15)
            .sound(SoundType.WOOD).harvestTool(ToolType.AXE).harvestLevel(2).dynamicShape()));
        BULU_BLACK   = PokecubeLegends.DECORATION_TAB.register("bulu_black_totem", () -> new BuluTotem(AbstractBlock.Properties.of(Material.WOOD, MaterialColor.TERRACOTTA_BLACK).strength(5, 15)
            .sound(SoundType.WOOD).harvestTool(ToolType.AXE).harvestLevel(2).dynamicShape()));
        //

        // Lele Totem
        LELE_WHITE   = PokecubeLegends.DECORATION_TAB.register("lele_white_totem", () -> new LeleTotem(AbstractBlock.Properties.of(Material.WOOD, MaterialColor.TERRACOTTA_WHITE).strength(5, 15)
            .sound(SoundType.WOOD).harvestTool(ToolType.AXE).harvestLevel(2).dynamicShape()));
        LELE_ORANGE   = PokecubeLegends.DECORATION_TAB.register("lele_orange_totem", () -> new LeleTotem(AbstractBlock.Properties.of(Material.WOOD, MaterialColor.TERRACOTTA_ORANGE).strength(5, 15)
            .sound(SoundType.WOOD).harvestTool(ToolType.AXE).harvestLevel(2).dynamicShape()));
        LELE_MAGENTA   = PokecubeLegends.DECORATION_TAB.register("lele_magenta_totem", () -> new LeleTotem(AbstractBlock.Properties.of(Material.WOOD, MaterialColor.TERRACOTTA_MAGENTA).strength(5, 15)
            .sound(SoundType.WOOD).harvestTool(ToolType.AXE).harvestLevel(2).dynamicShape()));
        LELE_LIGHT_BLUE   = PokecubeLegends.DECORATION_TAB.register("lele_lightblue_totem", () -> new LeleTotem(AbstractBlock.Properties.of(Material.WOOD, MaterialColor.TERRACOTTA_LIGHT_BLUE).strength(5, 15)
            .sound(SoundType.WOOD).harvestTool(ToolType.AXE).harvestLevel(2).dynamicShape()));
        LELE_YELLOW   = PokecubeLegends.DECORATION_TAB.register("lele_yellow_totem", () -> new LeleTotem(AbstractBlock.Properties.of(Material.WOOD, MaterialColor.TERRACOTTA_YELLOW).strength(5, 15)
            .sound(SoundType.WOOD).harvestTool(ToolType.AXE).harvestLevel(2).dynamicShape()));
        LELE_LIME   = PokecubeLegends.DECORATION_TAB.register("lele_lime_totem", () -> new LeleTotem(AbstractBlock.Properties.of(Material.WOOD, MaterialColor.TERRACOTTA_LIGHT_GREEN).strength(5, 15)
            .sound(SoundType.WOOD).harvestTool(ToolType.AXE).harvestLevel(2).dynamicShape()));
        LELE_PINK   = PokecubeLegends.DECORATION_TAB.register("lele_pink_totem", () -> new LeleTotem(AbstractBlock.Properties.of(Material.WOOD, MaterialColor.TERRACOTTA_PINK).strength(5, 15)
            .sound(SoundType.WOOD).harvestTool(ToolType.AXE).harvestLevel(2).dynamicShape()));
        LELE_GRAY   = PokecubeLegends.DECORATION_TAB.register("lele_gray_totem", () -> new LeleTotem(AbstractBlock.Properties.of(Material.WOOD, MaterialColor.TERRACOTTA_GRAY).strength(5, 15)
            .sound(SoundType.WOOD).harvestTool(ToolType.AXE).harvestLevel(2).dynamicShape()));
        LELE_LIGHT_GRAY   = PokecubeLegends.DECORATION_TAB.register("lele_lightgray_totem", () -> new LeleTotem(AbstractBlock.Properties.of(Material.WOOD, MaterialColor.TERRACOTTA_LIGHT_GRAY).strength(5, 15)
            .sound(SoundType.WOOD).harvestTool(ToolType.AXE).harvestLevel(2).dynamicShape()));
        LELE_CYAN   = PokecubeLegends.DECORATION_TAB.register("lele_cyan_totem", () -> new LeleTotem(AbstractBlock.Properties.of(Material.WOOD, MaterialColor.TERRACOTTA_CYAN).strength(5, 15)
            .sound(SoundType.WOOD).harvestTool(ToolType.AXE).harvestLevel(2).dynamicShape()));
        LELE_PURPLE   = PokecubeLegends.DECORATION_TAB.register("lele_purple_totem", () -> new LeleTotem(AbstractBlock.Properties.of(Material.WOOD, MaterialColor.TERRACOTTA_PURPLE).strength(5, 15)
            .sound(SoundType.WOOD).harvestTool(ToolType.AXE).harvestLevel(2).dynamicShape()));
        LELE_BLUE   = PokecubeLegends.DECORATION_TAB.register("lele_blue_totem", () -> new LeleTotem(AbstractBlock.Properties.of(Material.WOOD, MaterialColor.TERRACOTTA_BLUE).strength(5, 15)
            .sound(SoundType.WOOD).harvestTool(ToolType.AXE).harvestLevel(2).dynamicShape()));
        LELE_BROWN   = PokecubeLegends.DECORATION_TAB.register("lele_brown_totem", () -> new LeleTotem(AbstractBlock.Properties.of(Material.WOOD, MaterialColor.TERRACOTTA_BROWN).strength(5, 15)
            .sound(SoundType.WOOD).harvestTool(ToolType.AXE).harvestLevel(2).dynamicShape()));
        LELE_GREEN   = PokecubeLegends.DECORATION_TAB.register("lele_green_totem", () -> new LeleTotem(AbstractBlock.Properties.of(Material.WOOD, MaterialColor.TERRACOTTA_GREEN).strength(5, 15)
            .sound(SoundType.WOOD).harvestTool(ToolType.AXE).harvestLevel(2).dynamicShape()));
        LELE_RED   = PokecubeLegends.DECORATION_TAB.register("lele_red_totem", () -> new LeleTotem(AbstractBlock.Properties.of(Material.WOOD, MaterialColor.TERRACOTTA_RED).strength(5, 15)
            .sound(SoundType.WOOD).harvestTool(ToolType.AXE).harvestLevel(2).dynamicShape()));
        LELE_BLACK   = PokecubeLegends.DECORATION_TAB.register("lele_black_totem", () -> new LeleTotem(AbstractBlock.Properties.of(Material.WOOD, MaterialColor.TERRACOTTA_BLACK).strength(5, 15)
            .sound(SoundType.WOOD).harvestTool(ToolType.AXE).harvestLevel(2).dynamicShape()));
        //

        // Fini Totem
        FINI_WHITE   = PokecubeLegends.DECORATION_TAB.register("fini_white_totem", () -> new FiniTotem(AbstractBlock.Properties.of(Material.WOOD, MaterialColor.TERRACOTTA_WHITE).strength(5, 15)
            .sound(SoundType.WOOD).harvestTool(ToolType.AXE).harvestLevel(2).dynamicShape()));
        FINI_ORANGE   = PokecubeLegends.DECORATION_TAB.register("fini_orange_totem", () -> new FiniTotem(AbstractBlock.Properties.of(Material.WOOD, MaterialColor.TERRACOTTA_ORANGE).strength(5, 15)
            .sound(SoundType.WOOD).harvestTool(ToolType.AXE).harvestLevel(2).dynamicShape()));
        FINI_MAGENTA   = PokecubeLegends.DECORATION_TAB.register("fini_magenta_totem", () -> new FiniTotem(AbstractBlock.Properties.of(Material.WOOD, MaterialColor.TERRACOTTA_MAGENTA).strength(5, 15)
            .sound(SoundType.WOOD).harvestTool(ToolType.AXE).harvestLevel(2).dynamicShape()));
        FINI_LIGHT_BLUE   = PokecubeLegends.DECORATION_TAB.register("fini_lightblue_totem", () -> new FiniTotem(AbstractBlock.Properties.of(Material.WOOD, MaterialColor.TERRACOTTA_LIGHT_BLUE).strength(5, 15)
            .sound(SoundType.WOOD).harvestTool(ToolType.AXE).harvestLevel(2).dynamicShape()));
        FINI_YELLOW   = PokecubeLegends.DECORATION_TAB.register("fini_yellow_totem", () -> new FiniTotem(AbstractBlock.Properties.of(Material.WOOD, MaterialColor.TERRACOTTA_YELLOW).strength(5, 15)
            .sound(SoundType.WOOD).harvestTool(ToolType.AXE).harvestLevel(2).dynamicShape()));
        FINI_LIME   = PokecubeLegends.DECORATION_TAB.register("fini_lime_totem", () -> new FiniTotem(AbstractBlock.Properties.of(Material.WOOD, MaterialColor.TERRACOTTA_LIGHT_GREEN).strength(5, 15)
            .sound(SoundType.WOOD).harvestTool(ToolType.AXE).harvestLevel(2).dynamicShape()));
        FINI_PINK   = PokecubeLegends.DECORATION_TAB.register("fini_pink_totem", () -> new FiniTotem(AbstractBlock.Properties.of(Material.WOOD, MaterialColor.TERRACOTTA_PINK).strength(5, 15)
            .sound(SoundType.WOOD).harvestTool(ToolType.AXE).harvestLevel(2).dynamicShape()));
        FINI_GRAY   = PokecubeLegends.DECORATION_TAB.register("fini_gray_totem", () -> new FiniTotem(AbstractBlock.Properties.of(Material.WOOD, MaterialColor.TERRACOTTA_GRAY).strength(5, 15)
            .sound(SoundType.WOOD).harvestTool(ToolType.AXE).harvestLevel(2).dynamicShape()));
        FINI_LIGHT_GRAY   = PokecubeLegends.DECORATION_TAB.register("fini_lightgray_totem", () -> new FiniTotem(AbstractBlock.Properties.of(Material.WOOD, MaterialColor.TERRACOTTA_LIGHT_GRAY).strength(5, 15)
            .sound(SoundType.WOOD).harvestTool(ToolType.AXE).harvestLevel(2).dynamicShape()));
        FINI_CYAN   = PokecubeLegends.DECORATION_TAB.register("fini_cyan_totem", () -> new FiniTotem(AbstractBlock.Properties.of(Material.WOOD, MaterialColor.TERRACOTTA_CYAN).strength(5, 15)
            .sound(SoundType.WOOD).harvestTool(ToolType.AXE).harvestLevel(2).dynamicShape()));
        FINI_PURPLE   = PokecubeLegends.DECORATION_TAB.register("fini_purple_totem", () -> new FiniTotem(AbstractBlock.Properties.of(Material.WOOD, MaterialColor.TERRACOTTA_PURPLE).strength(5, 15)
            .sound(SoundType.WOOD).harvestTool(ToolType.AXE).harvestLevel(2).dynamicShape()));
        FINI_BLUE   = PokecubeLegends.DECORATION_TAB.register("fini_blue_totem", () -> new FiniTotem(AbstractBlock.Properties.of(Material.WOOD, MaterialColor.TERRACOTTA_BLUE).strength(5, 15)
            .sound(SoundType.WOOD).harvestTool(ToolType.AXE).harvestLevel(2).dynamicShape()));
        FINI_BROWN   = PokecubeLegends.DECORATION_TAB.register("fini_brown_totem", () -> new FiniTotem(AbstractBlock.Properties.of(Material.WOOD, MaterialColor.TERRACOTTA_BROWN).strength(5, 15)
            .sound(SoundType.WOOD).harvestTool(ToolType.AXE).harvestLevel(2).dynamicShape()));
        FINI_GREEN   = PokecubeLegends.DECORATION_TAB.register("fini_green_totem", () -> new FiniTotem(AbstractBlock.Properties.of(Material.WOOD, MaterialColor.TERRACOTTA_GREEN).strength(5, 15)
            .sound(SoundType.WOOD).harvestTool(ToolType.AXE).harvestLevel(2).dynamicShape()));
        FINI_RED   = PokecubeLegends.DECORATION_TAB.register("fini_red_totem", () -> new FiniTotem(AbstractBlock.Properties.of(Material.WOOD, MaterialColor.TERRACOTTA_RED).strength(5, 15)
            .sound(SoundType.WOOD).harvestTool(ToolType.AXE).harvestLevel(2).dynamicShape()));
        FINI_BLACK   = PokecubeLegends.DECORATION_TAB.register("fini_black_totem", () -> new FiniTotem(AbstractBlock.Properties.of(Material.WOOD, MaterialColor.TERRACOTTA_BLACK).strength(5, 15)
            .sound(SoundType.WOOD).harvestTool(ToolType.AXE).harvestLevel(2).dynamicShape()));

        POTTED_AGED_SAPLING = PokecubeLegends.NO_TAB.register("potted_aged_sapling",
            () -> new ItemGenerator.GenericPottedPlant(AGED_SAPLING.get(),
                AbstractBlock.Properties.of(Material.DECORATION).instabreak().noOcclusion()));
        POTTED_CORRUPTED_SAPLING = PokecubeLegends.NO_TAB.register("potted_corrupted_sapling",
            () -> new ItemGenerator.GenericPottedPlant(CORRUPTED_SAPLING.get(),
                AbstractBlock.Properties.of(Material.DECORATION).instabreak().noOcclusion()));
        POTTED_DISTORTIC_SAPLING = PokecubeLegends.NO_TAB.register("potted_distortic_sapling",
            () -> new ItemGenerator.GenericPottedPlant(DISTORTIC_SAPLING.get(),
                AbstractBlock.Properties.of(Material.DECORATION).instabreak().noOcclusion()));
        POTTED_INVERTED_SAPLING = PokecubeLegends.NO_TAB.register("potted_inverted_sapling",
            () -> new ItemGenerator.GenericPottedPlant(INVERTED_SAPLING.get(),
                AbstractBlock.Properties.of(Material.DECORATION).instabreak().noOcclusion()));
        POTTED_MIRAGE_SAPLING = PokecubeLegends.NO_TAB.register("potted_mirage_sapling",
            () -> new ItemGenerator.GenericPottedPlant(MIRAGE_SAPLING.get(),
                AbstractBlock.Properties.of(Material.DECORATION).instabreak().noOcclusion()));
        POTTED_TEMPORAL_SAPLING = PokecubeLegends.NO_TAB.register("potted_temporal_sapling",
            () -> new ItemGenerator.GenericPottedPlant(TEMPORAL_SAPLING.get(),
                AbstractBlock.Properties.of(Material.DECORATION).instabreak().noOcclusion()));

        POTTED_COMPRECED_MUSHROOM = PokecubeLegends.NO_TAB.register("potted_compreced_mushroom",
            () -> new ItemGenerator.GenericPottedPlant(PlantsInit.COMPRECED_MUSHROOM.get(),
                AbstractBlock.Properties.of(Material.DECORATION).instabreak().noOcclusion()));
        POTTED_CRYSTALLIZED_BUSH = PokecubeLegends.NO_TAB.register("potted_crystallized_bush",
            () -> new PottedCrystallizedBush(CRYSTALLIZED_BUSH.get(),
                AbstractBlock.Properties.of(Material.DECORATION).instabreak().noOcclusion()));
        POTTED_CRYSTALLIZED_CACTUS = PokecubeLegends.NO_TAB.register("potted_crystallized_cactus",
            () -> new PottedCrystallizedCactus(CRYSTALLIZED_CACTUS.get(),
                AbstractBlock.Properties.of(Material.DECORATION).instabreak().noOcclusion()));
        POTTED_DISTORCED_MUSHROOM = PokecubeLegends.NO_TAB.register("potted_distorced_mushroom",
            () -> new ItemGenerator.GenericPottedPlant(PlantsInit.DISTORCED_MUSHROOM.get(),
                AbstractBlock.Properties.of(Material.DECORATION).instabreak().noOcclusion()));
        POTTED_DISTORTIC_VINES = PokecubeLegends.NO_TAB.register("potted_distortic_vines",
            () -> new ItemGenerator.GenericPottedPlant(DISTORTIC_VINES.get(),
                AbstractBlock.Properties.of(Material.DECORATION).instabreak().noOcclusion()));
        POTTED_GOLDEN_POPPY = PokecubeLegends.NO_TAB.register("potted_golden_poppy",
            () -> new ItemGenerator.GenericPottedPlant(PlantsInit.GOLDEN_POPPY.get(),
                AbstractBlock.Properties.of(Material.DECORATION).instabreak().noOcclusion()));
        POTTED_INVERTED_ORCHID = PokecubeLegends.NO_TAB.register("potted_inverted_orchid",
            () -> new ItemGenerator.GenericPottedPlant(PlantsInit.INVERTED_ORCHID.get(),
                AbstractBlock.Properties.of(Material.DECORATION).instabreak().noOcclusion()));
        POTTED_TALL_CRYSTALLIZED_BUSH = PokecubeLegends.NO_TAB.register("potted_tall_crystallized_bush",
            () -> new PottedCrystallizedBush(TALL_CRYSTALLIZED_BUSH.get(),
                AbstractBlock.Properties.of(Material.DECORATION).instabreak().noOcclusion()));
    }

    private static ToIntFunction<BlockState> litBlockEmission(int i) {
        return (state) -> {
            return (Boolean)state.getValue(BlockStateProperties.LIT) ? i : 0;
        };
    }

    public static void init()
    {
        PlantsInit.registry();

        for (final RegistryObject<Block> reg : PokecubeLegends.BLOCKS.getEntries())
            PokecubeLegends.ITEMS.register(reg.getId().getPath(), () -> new BlockItem(reg.get(), new Item.Properties()
                    .tab(PokecubeItems.POKECUBEBLOCKS)));

        for (final RegistryObject<Block> reg : PokecubeLegends.BLOCKS_TAB.getEntries())
        {
            // These are registered separately, so skip them.
            if (reg == BlockInit.DISTORTIC_VINES || reg == BlockInit.DISTORTIC_VINES_PLANT) continue;
            PokecubeLegends.ITEMS.register(reg.getId().getPath(), () -> new BlockItem(reg.get(), new Item.Properties()
                    .tab(PokecubeLegends.TAB)));
        }
        
        for (final RegistryObject<Block> reg : PokecubeLegends.DECORATION_TAB.getEntries())
        {
            if (reg == BlockInit.INFECTED_TORCH || reg == BlockInit.INFECTED_TORCH_WALL) continue;
            PokecubeLegends.ITEMS.register(reg.getId().getPath(), () -> new BlockItem(reg.get(), new Item.Properties()
                .tab(PokecubeLegends.DECO_TAB)));
        }
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
            ItemGenerator.addStrippable(BlockInit.DISTORTIC_LOG.get(), BlockInit.STRIP_DISTORTIC_LOG.get());
            ItemGenerator.addStrippable(BlockInit.DISTORTIC_WOOD.get(), BlockInit.STRIP_DISTORTIC_WOOD.get());
            ItemGenerator.addStrippable(BlockInit.INVERTED_LOG.get(), BlockInit.STRIP_INVERTED_LOG.get());
            ItemGenerator.addStrippable(BlockInit.INVERTED_WOOD.get(), BlockInit.STRIP_INVERTED_WOOD.get());
            ItemGenerator.addStrippable(BlockInit.TEMPORAL_LOG.get(), BlockInit.STRIP_TEMPORAL_LOG.get());
            ItemGenerator.addStrippable(BlockInit.TEMPORAL_WOOD.get(), BlockInit.STRIP_TEMPORAL_WOOD.get());
            ItemGenerator.addStrippable(BlockInit.CORRUPTED_LOG.get(), BlockInit.STRIP_CORRUPTED_LOG.get());
            ItemGenerator.addStrippable(BlockInit.CORRUPTED_WOOD.get(), BlockInit.STRIP_CORRUPTED_WOOD.get());
            ItemGenerator.addStrippable(BlockInit.MIRAGE_LOG.get(), BlockInit.STRIP_MIRAGE_LOG.get());
            ItemGenerator.addStrippable(BlockInit.MIRAGE_WOOD.get(), BlockInit.STRIP_MIRAGE_WOOD.get());         
        });
    }
    
    public static void compostableBlocks(float chance, RegistryObject<Block> item) 
    {
        ComposterBlock.COMPOSTABLES.put(item.get().asItem(), chance);
    }
    
    public static void compostables() 
    {
        compostableBlocks(0.3f, BlockInit.DYNA_LEAVES1);
        compostableBlocks(0.3f, BlockInit.DYNA_LEAVES2);
        compostableBlocks(0.3f, BlockInit.INVERTED_SAPLING);
        compostableBlocks(0.3f, BlockInit.TEMPORAL_SAPLING);
        compostableBlocks(0.3f, BlockInit.AGED_SAPLING);
        compostableBlocks(0.3f, BlockInit.CORRUPTED_SAPLING);
        compostableBlocks(0.3f, BlockInit.MIRAGE_SAPLING);
        compostableBlocks(0.3f, BlockInit.DISTORTIC_SAPLING);
        compostableBlocks(0.3f, BlockInit.INVERTED_LEAVES);
        compostableBlocks(0.3f, BlockInit.AGED_LEAVES);
        compostableBlocks(0.3f, BlockInit.CORRUPTED_LEAVES);
        compostableBlocks(0.3f, BlockInit.DISTORTIC_LEAVES);
        compostableBlocks(0.3f, BlockInit.MIRAGE_LEAVES);
        compostableBlocks(0.75f, BlockInit.CRYSTALLIZED_CACTUS);
        compostableBlocks(0.65f, PlantsInit.DISTORCED_MUSHROOM);
        compostableBlocks(0.65f, PlantsInit.COMPRECED_MUSHROOM);
        compostableBlocks(0.65f, PlantsInit.GOLDEN_POPPY);
        compostableBlocks(0.65f, PlantsInit.INVERTED_ORCHID);
    }
    
    public static void flammableBlocks(Block block, int speed, int flammability) {
        FireBlock fire = (FireBlock) Blocks.FIRE;
        fire.setFlammable(block, speed, flammability);
    }

    public static void flammables() 
    {
        //Logs
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

        //Stripped Logs
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

        //Leaves
        flammableBlocks(BlockInit.AGED_LEAVES.get(), 30, 60);
        flammableBlocks(BlockInit.CORRUPTED_LEAVES.get(), 30, 60);
        flammableBlocks(BlockInit.DISTORTIC_LEAVES.get(), 30, 60);
        flammableBlocks(BlockInit.INVERTED_LEAVES.get(), 30, 60);
        flammableBlocks(BlockInit.MIRAGE_LEAVES.get(), 30, 60);
        flammableBlocks(BlockInit.TEMPORAL_LEAVES.get(), 30, 60);
        flammableBlocks(BlockInit.DYNA_LEAVES1.get(), 30, 60);
        flammableBlocks(BlockInit.DYNA_LEAVES2.get(), 30, 60);

        //Planks
        flammableBlocks(BlockInit.AGED_PLANKS.get(), 5, 20);
        flammableBlocks(BlockInit.CORRUPTED_PLANKS.get(), 5, 20);
        flammableBlocks(BlockInit.DISTORTIC_PLANKS.get(), 5, 20);
        flammableBlocks(BlockInit.INVERTED_PLANKS.get(), 5, 20);
        flammableBlocks(BlockInit.MIRAGE_PLANKS.get(), 5, 20);
        flammableBlocks(BlockInit.TEMPORAL_PLANKS.get(), 5, 20);

        //Slabs
        flammableBlocks(BlockInit.AGED_SLAB.get(), 5, 20);
        flammableBlocks(BlockInit.CORRUPTED_SLAB.get(), 5, 20);
        flammableBlocks(BlockInit.DISTORTIC_SLAB.get(), 5, 20);
        flammableBlocks(BlockInit.INVERTED_SLAB.get(), 5, 20);
        flammableBlocks(BlockInit.MIRAGE_SLAB.get(), 5, 20);
        flammableBlocks(BlockInit.TEMPORAL_SLAB.get(), 5, 20);

        //Stairs
        flammableBlocks(BlockInit.AGED_STAIRS.get(), 5, 20);
        flammableBlocks(BlockInit.CORRUPTED_STAIRS.get(), 5, 20);
        flammableBlocks(BlockInit.DISTORTIC_STAIRS.get(), 5, 20);
        flammableBlocks(BlockInit.INVERTED_STAIRS.get(), 5, 20);
        flammableBlocks(BlockInit.MIRAGE_STAIRS.get(), 5, 20);
        flammableBlocks(BlockInit.TEMPORAL_STAIRS.get(), 5, 20);

        //Fences
        flammableBlocks(BlockInit.AGED_FENCE.get(), 5, 20);
        flammableBlocks(BlockInit.CORRUPTED_FENCE.get(), 5, 20);
        flammableBlocks(BlockInit.DISTORTIC_FENCE.get(), 5, 20);
        flammableBlocks(BlockInit.INVERTED_FENCE.get(), 5, 20);
        flammableBlocks(BlockInit.MIRAGE_FENCE.get(), 5, 20);
        flammableBlocks(BlockInit.TEMPORAL_FENCE.get(), 5, 20);

        //Fence Gates
        flammableBlocks(BlockInit.AGED_FENCE_GATE.get(), 5, 20);
        flammableBlocks(BlockInit.CORRUPTED_FENCE_GATE.get(), 5, 20);
        flammableBlocks(BlockInit.DISTORTIC_FENCE_GATE.get(), 5, 20);
        flammableBlocks(BlockInit.INVERTED_FENCE_GATE.get(), 5, 20);
        flammableBlocks(BlockInit.MIRAGE_FENCE_GATE.get(), 5, 20);
        flammableBlocks(BlockInit.TEMPORAL_FENCE_GATE.get(), 5, 20);

        //Plants
        flammableBlocks(PlantsInit.GOLDEN_POPPY.get(), 60, 100);
        flammableBlocks(PlantsInit.INVERTED_ORCHID.get(), 60, 100);
        flammableBlocks(PlantsInit.DISTORCED_MUSHROOM.get(), 60, 100);
        flammableBlocks(PlantsInit.COMPRECED_MUSHROOM.get(), 60, 100);
    }
}
