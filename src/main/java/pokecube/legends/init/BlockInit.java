package pokecube.legends.init;

import net.minecraft.block.*;
import net.minecraft.block.material.Material;
import net.minecraft.block.material.MaterialColor;
import net.minecraft.item.BlockItem;
import net.minecraft.item.DyeColor;
import net.minecraft.item.Item;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraftforge.common.ToolType;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.fml.event.lifecycle.FMLLoadCompleteEvent;
import pokecube.core.PokecubeItems;
import pokecube.core.handlers.ItemGenerator;
import pokecube.legends.PokecubeLegends;
import pokecube.legends.blocks.BlockBase;
import pokecube.legends.blocks.SaplingBase;
import pokecube.legends.blocks.customblocks.*;
import pokecube.legends.blocks.customblocks.taputotem.BuluTotem;
import pokecube.legends.blocks.customblocks.taputotem.FiniTotem;
import pokecube.legends.blocks.customblocks.taputotem.KokoTotem;
import pokecube.legends.blocks.customblocks.taputotem.LeleTotem;
import pokecube.legends.blocks.normalblocks.SpectrumGlass;
import pokecube.legends.blocks.normalblocks.DistorticOneWayGlass;
import pokecube.legends.blocks.normalblocks.*;
import pokecube.legends.blocks.plants.*;

public class BlockInit
{
    // Blocks
    public static final RegistryObject<Block> RAID_SPAWN;
    
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
    
    public static final RegistryObject<Block> DYNA_LEAVES1;
    public static final RegistryObject<Block> DYNA_LEAVES2;
    public static final RegistryObject<Block> TOTEM_BLOCK;
    
    //Tapus Totens
    //Koko Totem
    public static final RegistryObject<Block> KOKO_WHITE;
    public static final RegistryObject<Block> KOKO_ORANGE;
    public static final RegistryObject<Block> KOKO_MAGENTA;
    public static final RegistryObject<Block> KOKO_LIGHT_BLUE;
    public static final RegistryObject<Block> KOKO_YELLOW;
    public static final RegistryObject<Block> KOKO_LIME;
    public static final RegistryObject<Block> KOKO_PINK;
    public static final RegistryObject<Block> KOKO_GRAY;
    public static final RegistryObject<Block> KOKO_LIGHT_GRAY;
    public static final RegistryObject<Block> KOKO_CYAN;
    public static final RegistryObject<Block> KOKO_PURPLE;
    public static final RegistryObject<Block> KOKO_BLUE;
    public static final RegistryObject<Block> KOKO_BROWN;
    public static final RegistryObject<Block> KOKO_GREEN;
    public static final RegistryObject<Block> KOKO_RED;
    public static final RegistryObject<Block> KOKO_BLACK;
    
    //Bulu Totem
    public static final RegistryObject<Block> BULU_WHITE;
    public static final RegistryObject<Block> BULU_ORANGE;
    public static final RegistryObject<Block> BULU_MAGENTA;
    public static final RegistryObject<Block> BULU_LIGHT_BLUE;
    public static final RegistryObject<Block> BULU_YELLOW;
    public static final RegistryObject<Block> BULU_LIME;
    public static final RegistryObject<Block> BULU_PINK;
    public static final RegistryObject<Block> BULU_GRAY;
    public static final RegistryObject<Block> BULU_LIGHT_GRAY;
    public static final RegistryObject<Block> BULU_CYAN;
    public static final RegistryObject<Block> BULU_PURPLE;
    public static final RegistryObject<Block> BULU_BLUE;
    public static final RegistryObject<Block> BULU_BROWN;
    public static final RegistryObject<Block> BULU_GREEN;
    public static final RegistryObject<Block> BULU_RED;
    public static final RegistryObject<Block> BULU_BLACK;
    
    //Lele Totem
    public static final RegistryObject<Block> LELE_WHITE;
    public static final RegistryObject<Block> LELE_ORANGE;
    public static final RegistryObject<Block> LELE_MAGENTA;
    public static final RegistryObject<Block> LELE_LIGHT_BLUE;
    public static final RegistryObject<Block> LELE_YELLOW;
    public static final RegistryObject<Block> LELE_LIME;
    public static final RegistryObject<Block> LELE_PINK;
    public static final RegistryObject<Block> LELE_GRAY;
    public static final RegistryObject<Block> LELE_LIGHT_GRAY;
    public static final RegistryObject<Block> LELE_CYAN;
    public static final RegistryObject<Block> LELE_PURPLE;
    public static final RegistryObject<Block> LELE_BLUE;
    public static final RegistryObject<Block> LELE_BROWN;
    public static final RegistryObject<Block> LELE_GREEN;
    public static final RegistryObject<Block> LELE_RED;
    public static final RegistryObject<Block> LELE_BLACK;
    
    //Fini Totem
    public static final RegistryObject<Block> FINI_WHITE;
    public static final RegistryObject<Block> FINI_ORANGE;
    public static final RegistryObject<Block> FINI_MAGENTA;
    public static final RegistryObject<Block> FINI_LIGHT_BLUE;
    public static final RegistryObject<Block> FINI_YELLOW;
    public static final RegistryObject<Block> FINI_LIME;
    public static final RegistryObject<Block> FINI_PINK;
    public static final RegistryObject<Block> FINI_GRAY;
    public static final RegistryObject<Block> FINI_LIGHT_GRAY;
    public static final RegistryObject<Block> FINI_CYAN;
    public static final RegistryObject<Block> FINI_PURPLE;
    public static final RegistryObject<Block> FINI_BLUE;
    public static final RegistryObject<Block> FINI_BROWN;
    public static final RegistryObject<Block> FINI_GREEN;
    public static final RegistryObject<Block> FINI_RED;
    public static final RegistryObject<Block> FINI_BLACK;

    // Dimensions
    public static final RegistryObject<Block> DISTORTIC_GRASS;
    public static final RegistryObject<Block> DISTORTIC_STONE;
    public static final RegistryObject<Block> DISTORTIC_STONE_SLAB;
    public static final RegistryObject<Block> DISTORTIC_STONE_STAIRS;
    public static final RegistryObject<Block> DISTORTIC_MIRROR;
    public static final RegistryObject<Block> DISTORTIC_OW_GLASS;

    public static final RegistryObject<Block> ULTRA_TORCH1;
    public static final RegistryObject<Block> ULTRA_TORCH1_WALL;
    
    public static final RegistryObject<Block> ULTRA_MAGNETIC;
    public static final RegistryObject<Block> ULTRA_MUSHROOM_GRASS;
    public static final RegistryObject<Block> ULTRA_MUSHROOM_DIRT;
    public static final RegistryObject<Block> ULTRA_JUNGLE_GRASS;
    public static final RegistryObject<Block> ULTRA_JUNGLE_DIRT;
    public static final RegistryObject<Block> SPECTRUM_GLASS;
    public static final RegistryObject<Block> ULTRA_CORRUPTED_GRASS;
    public static final RegistryObject<Block> ULTRA_CORRUPTED_DIRT;
    public static final RegistryObject<Block> ULTRA_AGED_GRASS;
    public static final RegistryObject<Block> ULTRA_AGED_DIRT;

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
    public static final RegistryObject<Block> ULTRA_SAND;
    public static final RegistryObject<Block> ULTRA_SANDSTONE;
    public static final RegistryObject<Block> ULTRA_SANDSTONE_SLAB;
    public static final RegistryObject<Block> ULTRA_SANDSTONE_STAIRS;
    public static final RegistryObject<Block> ULTRA_SANDBRICK;
    public static final RegistryObject<Block> ULTRA_SANDBRICK_SLAB;
    public static final RegistryObject<Block> ULTRA_SANDBRICK_STAIRS;
    public static final RegistryObject<Block> ULTRA_SANDSTONE_SMOOTH;
    public static final RegistryObject<Block> ULTRA_SANDSTONE_SMOOTH_SLAB;
    public static final RegistryObject<Block> ULTRA_SANDSTONE_SMOOTH_STAIRS;
    public static final RegistryObject<Block> ULTRA_SANDSTONE_BUTTON;
    public static final RegistryObject<Block> ULTRA_SANDSTONE_PR_PLATE;

    // Plants(LOG/Planks/Leaves)
    public static final RegistryObject<Block> INVERTED_SAPLING;
    public static final RegistryObject<Block> ULTRA_JUNGLE_SAPLING;
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

    public static final RegistryObject<Block> CRYSTALLIZED_CACTUS;
    public static final RegistryObject<Block> CRYSTALLIZED_BUSH;
    public static final RegistryObject<Block> TALL_CRYSTALLIZED_BUSH;

    // Portal
    public static final RegistryObject<Block> BLOCK_PORTALWARP;

    // Legendary Spawns
    public static final RegistryObject<Block> LEGENDARY_SPAWN;
    public static final RegistryObject<Block> TROUGH_BLOCK;
    public static final RegistryObject<Block> HEATRAN_BLOCK;
    public static final RegistryObject<Block> TAO_BLOCK;
    
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
    public static final RegistryObject<Block> OVERWORLD_COSMIC_DUST_ORE;
    
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

    static
    {
        // Block Raid
        RAID_SPAWN = PokecubeLegends.BLOCKS.register("raidspawn_block", () -> new RaidSpawnBlock(AbstractBlock.Properties.of(
                Material.STONE, MaterialColor.COLOR_RED).randomTicks().strength(2000, 2000)).setInfoBlockName("raidspawn"));

        // Decorative_Blocks
        DYNA_LEAVES1 = PokecubeLegends.DECORATION_TAB.register("dyna_leave_1", () -> new LeavesBlock(AbstractBlock.Properties.of(
                Material.LEAVES, MaterialColor.COLOR_PINK).strength(1f, 5).sound(SoundType.WET_GRASS).noDrops().noOcclusion()));
        DYNA_LEAVES2 = PokecubeLegends.DECORATION_TAB.register("dyna_leave_2", () -> new LeavesBlock(AbstractBlock.Properties.of(
                Material.LEAVES, MaterialColor.COLOR_PINK).strength(1f, 5).sound(SoundType.WET_GRASS).noDrops().noOcclusion()));

        OCEAN_BRICK = PokecubeLegends.DECORATION_TAB.register("oceanbrick", () -> new Block(AbstractBlock.Properties.of(
                Material.STONE, MaterialColor.COLOR_CYAN).strength(1.5f, 10).sound(SoundType.STONE).requiresCorrectToolForDrops()));
        OCEAN_BRICK_SLAB = PokecubeLegends.DECORATION_TAB.register("ocean_brick_slab", () -> new SlabBlock(AbstractBlock.Properties.of(
        		Material.STONE, MaterialColor.COLOR_CYAN).strength(2.0F, 10f).sound(SoundType.STONE).requiresCorrectToolForDrops()));
        OCEAN_BRICK_STAIRS = PokecubeLegends.DECORATION_TAB.register("ocean_brick_stairs",
                () -> new ItemGenerator.GenericStairs(Blocks.STONE_STAIRS.defaultBlockState(), AbstractBlock.Properties.of(
                		Material.STONE, MaterialColor.COLOR_CYAN).strength(2.0F, 10f).sound(SoundType.STONE).requiresCorrectToolForDrops()));
        
        SKY_BRICK   = PokecubeLegends.DECORATION_TAB.register("skybrick", () -> new Block(AbstractBlock.Properties.of(Material.STONE,
                MaterialColor.COLOR_BLUE).strength(1.5f, 10).sound(SoundType.STONE).requiresCorrectToolForDrops()));
        SKY_BRICK_SLAB = PokecubeLegends.DECORATION_TAB.register("sky_brick_slab", () -> new SlabBlock(AbstractBlock.Properties.of(
        		Material.STONE, MaterialColor.COLOR_BLUE).strength(2.0F, 10f).sound(SoundType.STONE).requiresCorrectToolForDrops()));
        SKY_BRICK_STAIRS = PokecubeLegends.DECORATION_TAB.register("sky_brick_stairs",
                () -> new ItemGenerator.GenericStairs(Blocks.STONE_STAIRS.defaultBlockState(), AbstractBlock.Properties.of(
                		Material.STONE, MaterialColor.COLOR_BLUE).strength(2.0F, 10f).sound(SoundType.STONE).requiresCorrectToolForDrops()));
        
        SPATIAN_BRICK = PokecubeLegends.DECORATION_TAB.register("spatianbrick", () -> new Block(AbstractBlock.Properties.of(
                Material.STONE, MaterialColor.COLOR_MAGENTA).strength(1.5f, 10).sound(SoundType.STONE).requiresCorrectToolForDrops()));
        SPATIAN_BRICK_SLAB = PokecubeLegends.DECORATION_TAB.register("spatian_brick_slab", () -> new SlabBlock(AbstractBlock.Properties.of(
        		Material.STONE, MaterialColor.COLOR_MAGENTA).strength(2.0F, 3.0f).sound(SoundType.STONE).requiresCorrectToolForDrops()));
        SPATIAN_BRICK_STAIRS = PokecubeLegends.DECORATION_TAB.register("spatian_brick_stairs",
                () -> new ItemGenerator.GenericStairs(Blocks.STONE_STAIRS.defaultBlockState(), AbstractBlock.Properties.of(
                		Material.STONE, MaterialColor.COLOR_MAGENTA).strength(2.0F, 3.0f).sound(SoundType.STONE).requiresCorrectToolForDrops()));
        
        MAGMA_BRICK   = PokecubeLegends.DECORATION_TAB.register("magmabrick", () -> new MagmaBlock(AbstractBlock.Properties.of(
                Material.STONE, MaterialColor.NETHER).strength(1.5f, 10).sound(SoundType.NETHER_BRICKS).lightLevel(b -> 3)
        			.emissiveRendering((s, r, p) -> true).requiresCorrectToolForDrops()));
        MAGMA_BRICK_SLAB = PokecubeLegends.DECORATION_TAB.register("magma_brick_slab", () -> new SlabBlock(AbstractBlock.Properties.of(
        		Material.STONE, MaterialColor.NETHER).strength(2.0F, 3.0f).sound(SoundType.NETHER_BRICKS).lightLevel(b -> 3)
    			.emissiveRendering((s, r, p) -> true).requiresCorrectToolForDrops()));
        MAGMA_BRICK_STAIRS = PokecubeLegends.DECORATION_TAB.register("magma_brick_stairs",
                () -> new ItemGenerator.GenericStairs(Blocks.STONE_STAIRS.defaultBlockState(), AbstractBlock.Properties.of(
                		Material.STONE, MaterialColor.NETHER).strength(2.0F, 3.0f).sound(SoundType.NETHER_BRICKS).lightLevel(b -> 3)
            			.emissiveRendering((s, r, p) -> true).requiresCorrectToolForDrops()));
        
        DARKSKY_BRICK = PokecubeLegends.DECORATION_TAB.register("darkskybrick", () -> new Block(AbstractBlock.Properties.of(
                Material.STONE, MaterialColor.COLOR_LIGHT_GRAY).strength(1.5f, 10).sound(SoundType.STONE).requiresCorrectToolForDrops()));
        DARKSKY_BRICK_SLAB = PokecubeLegends.DECORATION_TAB.register("darksky_brick_slab", () -> new SlabBlock(AbstractBlock.Properties.of(
        		Material.STONE, MaterialColor.COLOR_LIGHT_GRAY).strength(2.0F, 3.0f).sound(SoundType.STONE).requiresCorrectToolForDrops()));
        DARKSKY_BRICK_STAIRS = PokecubeLegends.DECORATION_TAB.register("darksky_brick_stairs",
                () -> new ItemGenerator.GenericStairs(Blocks.STONE_STAIRS.defaultBlockState(), AbstractBlock.Properties.of(
                		Material.STONE, MaterialColor.COLOR_LIGHT_GRAY).strength(2.0F, 3.0f).sound(SoundType.STONE).requiresCorrectToolForDrops()));
        
        // Meteor Blocks
        METEOR_BLOCK = PokecubeLegends.BLOCKS_TAB.register("meteor_block", () -> new MeteorBlock(6842513,
                AbstractBlock.Properties.of(Material.VEGETABLE, MaterialColor.TERRACOTTA_BLUE).strength(2.5f)
                .sound(SoundType.METAL).harvestTool(ToolType.PICKAXE).harvestLevel(2).requiresCorrectToolForDrops()));
        METEOR_SLAB = PokecubeLegends.BLOCKS_TAB.register("meteor_slab", () -> new SlabBlock(AbstractBlock.Properties.of(
        		Material.STONE, MaterialColor.TERRACOTTA_BLUE).strength(2.0F, 3.0f).sound(SoundType.STONE).requiresCorrectToolForDrops()));
        METEOR_STAIRS = PokecubeLegends.BLOCKS_TAB.register("meteor_stairs",
                () -> new ItemGenerator.GenericStairs(Blocks.STONE_STAIRS.defaultBlockState(), AbstractBlock.Properties.of(
                		Material.STONE, MaterialColor.TERRACOTTA_BLUE).strength(2.0F, 3.0f).sound(SoundType.STONE).requiresCorrectToolForDrops()));

        TOTEM_BLOCK = PokecubeLegends.DECORATION_TAB.register("totem_block", () -> new Block(AbstractBlock.Properties.of(
                Material.STONE, MaterialColor.COLOR_LIGHT_GRAY).strength(1.5f, 10).sound(SoundType.STONE).requiresCorrectToolForDrops()));

        
        //Tapus Totems
        
        // Koko Totem
        KOKO_WHITE   = PokecubeLegends.DECORATION_TAB.register("koko_white_totem", () -> new KokoTotem("koko_white_totem",
            AbstractBlock.Properties.of(Material.WOOD, MaterialColor.TERRACOTTA_WHITE).strength(5, 15)
                .sound(SoundType.WOOD).harvestTool(ToolType.AXE).harvestLevel(2).dynamicShape()).noInfoBlock());
        KOKO_ORANGE   = PokecubeLegends.DECORATION_TAB.register("koko_orange_totem", () -> new KokoTotem("koko_orange_totem",
            AbstractBlock.Properties.of(Material.WOOD, MaterialColor.TERRACOTTA_ORANGE).strength(5, 15)
                .sound(SoundType.WOOD).harvestTool(ToolType.AXE).harvestLevel(2).dynamicShape()).noInfoBlock());
        KOKO_MAGENTA   = PokecubeLegends.DECORATION_TAB.register("koko_magenta_totem", () -> new KokoTotem("koko_magenta_totem",
            AbstractBlock.Properties.of(Material.WOOD, MaterialColor.TERRACOTTA_MAGENTA).strength(5, 15)
                .sound(SoundType.WOOD).harvestTool(ToolType.AXE).harvestLevel(2).dynamicShape()).noInfoBlock());
        KOKO_LIGHT_BLUE   = PokecubeLegends.DECORATION_TAB.register("koko_lightblue_totem", () -> new KokoTotem("koko_lightblue_totem",
            AbstractBlock.Properties.of(Material.WOOD, MaterialColor.TERRACOTTA_LIGHT_BLUE).strength(5, 15)
                .sound(SoundType.WOOD).harvestTool(ToolType.AXE).harvestLevel(2).dynamicShape()).noInfoBlock());
        KOKO_YELLOW   = PokecubeLegends.DECORATION_TAB.register("koko_yellow_totem", () -> new KokoTotem("koko_yellow_totem",
            AbstractBlock.Properties.of(Material.WOOD, MaterialColor.TERRACOTTA_YELLOW).strength(5, 15)
                .sound(SoundType.WOOD).harvestTool(ToolType.AXE).harvestLevel(2).dynamicShape()).noInfoBlock());
        KOKO_LIME   = PokecubeLegends.DECORATION_TAB.register("koko_lime_totem", () -> new KokoTotem("koko_lime_totem",
            AbstractBlock.Properties.of(Material.WOOD, MaterialColor.TERRACOTTA_LIGHT_GREEN).strength(5, 15)
                .sound(SoundType.WOOD).harvestTool(ToolType.AXE).harvestLevel(2).dynamicShape()).noInfoBlock());
        KOKO_PINK   = PokecubeLegends.DECORATION_TAB.register("koko_pink_totem", () -> new KokoTotem("koko_pink_totem",
            AbstractBlock.Properties.of(Material.WOOD, MaterialColor.TERRACOTTA_PINK).strength(5, 15)
                .sound(SoundType.WOOD).harvestTool(ToolType.AXE).harvestLevel(2).dynamicShape()).noInfoBlock());
        KOKO_GRAY   = PokecubeLegends.DECORATION_TAB.register("koko_gray_totem", () -> new KokoTotem("koko_gray_totem",
            AbstractBlock.Properties.of(Material.WOOD, MaterialColor.TERRACOTTA_GRAY).strength(5, 15)
                .sound(SoundType.WOOD).harvestTool(ToolType.AXE).harvestLevel(2).dynamicShape()).noInfoBlock());
        KOKO_LIGHT_GRAY   = PokecubeLegends.DECORATION_TAB.register("koko_lightgray_totem", () -> new KokoTotem("koko_lightgray_totem",
            AbstractBlock.Properties.of(Material.WOOD, MaterialColor.TERRACOTTA_LIGHT_GRAY).strength(5, 15)
                .sound(SoundType.WOOD).harvestTool(ToolType.AXE).harvestLevel(2).dynamicShape()).noInfoBlock());
        KOKO_CYAN   = PokecubeLegends.DECORATION_TAB.register("koko_cyan_totem", () -> new KokoTotem("koko_cyan_totem",
            AbstractBlock.Properties.of(Material.WOOD, MaterialColor.TERRACOTTA_CYAN).strength(5, 15)
                .sound(SoundType.WOOD).harvestTool(ToolType.AXE).harvestLevel(2).dynamicShape()).noInfoBlock());
        KOKO_PURPLE   = PokecubeLegends.DECORATION_TAB.register("koko_purple_totem", () -> new KokoTotem("koko_purple_totem",
            AbstractBlock.Properties.of(Material.WOOD, MaterialColor.TERRACOTTA_PURPLE).strength(5, 15)
                .sound(SoundType.WOOD).harvestTool(ToolType.AXE).harvestLevel(2).dynamicShape()).noInfoBlock());
        KOKO_BLUE   = PokecubeLegends.DECORATION_TAB.register("koko_blue_totem", () -> new KokoTotem("koko_blue_totem",
            AbstractBlock.Properties.of(Material.WOOD, MaterialColor.TERRACOTTA_BLUE).strength(5, 15)
                .sound(SoundType.WOOD).harvestTool(ToolType.AXE).harvestLevel(2).dynamicShape()).noInfoBlock());
        KOKO_BROWN   = PokecubeLegends.DECORATION_TAB.register("koko_brown_totem", () -> new KokoTotem("koko_brown_totem",
            AbstractBlock.Properties.of(Material.WOOD, MaterialColor.TERRACOTTA_BROWN).strength(5, 15)
                .sound(SoundType.WOOD).harvestTool(ToolType.AXE).harvestLevel(2).dynamicShape()).noInfoBlock());
        KOKO_GREEN   = PokecubeLegends.DECORATION_TAB.register("koko_green_totem", () -> new KokoTotem("koko_green_totem",
            AbstractBlock.Properties.of(Material.WOOD, MaterialColor.TERRACOTTA_GREEN).strength(5, 15)
                .sound(SoundType.WOOD).harvestTool(ToolType.AXE).harvestLevel(2).dynamicShape()).noInfoBlock());
        KOKO_RED   = PokecubeLegends.DECORATION_TAB.register("koko_red_totem", () -> new KokoTotem("koko_red_totem",
            AbstractBlock.Properties.of(Material.WOOD, MaterialColor.TERRACOTTA_RED).strength(5, 15)
                .sound(SoundType.WOOD).harvestTool(ToolType.AXE).harvestLevel(2).dynamicShape()).noInfoBlock());
        KOKO_BLACK   = PokecubeLegends.DECORATION_TAB.register("koko_black_totem", () -> new KokoTotem("koko_black_totem",
            AbstractBlock.Properties.of(Material.WOOD, MaterialColor.TERRACOTTA_BLACK).strength(5, 15)
                .sound(SoundType.WOOD).harvestTool(ToolType.AXE).harvestLevel(2).dynamicShape()).noInfoBlock());
        
        // Bulu Totem
        BULU_WHITE   = PokecubeLegends.DECORATION_TAB.register("bulu_white_totem", () -> new BuluTotem("bulu_white_totem",
            AbstractBlock.Properties.of(Material.WOOD, MaterialColor.TERRACOTTA_WHITE).strength(5, 15)
                .sound(SoundType.WOOD).harvestTool(ToolType.AXE).harvestLevel(2).dynamicShape()).noInfoBlock());
        BULU_ORANGE   = PokecubeLegends.DECORATION_TAB.register("bulu_orange_totem", () -> new BuluTotem("bulu_orange_totem",
            AbstractBlock.Properties.of(Material.WOOD, MaterialColor.TERRACOTTA_ORANGE).strength(5, 15)
                .sound(SoundType.WOOD).harvestTool(ToolType.AXE).harvestLevel(2).dynamicShape()).noInfoBlock());
        BULU_MAGENTA   = PokecubeLegends.DECORATION_TAB.register("bulu_magenta_totem", () -> new BuluTotem("bulu_magenta_totem",
            AbstractBlock.Properties.of(Material.WOOD, MaterialColor.TERRACOTTA_MAGENTA).strength(5, 15)
                .sound(SoundType.WOOD).harvestTool(ToolType.AXE).harvestLevel(2).dynamicShape()).noInfoBlock());
        BULU_LIGHT_BLUE   = PokecubeLegends.DECORATION_TAB.register("bulu_lightblue_totem", () -> new BuluTotem("bulu_lightblue_totem",
            AbstractBlock.Properties.of(Material.WOOD, MaterialColor.TERRACOTTA_LIGHT_BLUE).strength(5, 15)
                .sound(SoundType.WOOD).harvestTool(ToolType.AXE).harvestLevel(2).dynamicShape()).noInfoBlock());
        BULU_YELLOW   = PokecubeLegends.DECORATION_TAB.register("bulu_yellow_totem", () -> new BuluTotem("bulu_yellow_totem",
            AbstractBlock.Properties.of(Material.WOOD, MaterialColor.TERRACOTTA_YELLOW).strength(5, 15)
                .sound(SoundType.WOOD).harvestTool(ToolType.AXE).harvestLevel(2).dynamicShape()).noInfoBlock());
        BULU_LIME   = PokecubeLegends.DECORATION_TAB.register("bulu_lime_totem", () -> new BuluTotem("bulu_lime_totem",
            AbstractBlock.Properties.of(Material.WOOD, MaterialColor.TERRACOTTA_LIGHT_GREEN).strength(5, 15)
                .sound(SoundType.WOOD).harvestTool(ToolType.AXE).harvestLevel(2).dynamicShape()).noInfoBlock());
        BULU_PINK   = PokecubeLegends.DECORATION_TAB.register("bulu_pink_totem", () -> new BuluTotem("bulu_pink_totem",
            AbstractBlock.Properties.of(Material.WOOD, MaterialColor.TERRACOTTA_PINK).strength(5, 15)
                .sound(SoundType.WOOD).harvestTool(ToolType.AXE).harvestLevel(2).dynamicShape()).noInfoBlock());
        BULU_GRAY   = PokecubeLegends.DECORATION_TAB.register("bulu_gray_totem", () -> new BuluTotem("bulu_gray_totem",
            AbstractBlock.Properties.of(Material.WOOD, MaterialColor.TERRACOTTA_GRAY).strength(5, 15)
                .sound(SoundType.WOOD).harvestTool(ToolType.AXE).harvestLevel(2).dynamicShape()).noInfoBlock());
        BULU_LIGHT_GRAY   = PokecubeLegends.DECORATION_TAB.register("bulu_lightgray_totem", () -> new BuluTotem("bulu_lightgray_totem",
            AbstractBlock.Properties.of(Material.WOOD, MaterialColor.TERRACOTTA_LIGHT_GRAY).strength(5, 15)
                .sound(SoundType.WOOD).harvestTool(ToolType.AXE).harvestLevel(2).dynamicShape()).noInfoBlock());
        BULU_CYAN   = PokecubeLegends.DECORATION_TAB.register("bulu_cyan_totem", () -> new BuluTotem("bulu_cyan_totem",
            AbstractBlock.Properties.of(Material.WOOD, MaterialColor.TERRACOTTA_CYAN).strength(5, 15)
                .sound(SoundType.WOOD).harvestTool(ToolType.AXE).harvestLevel(2).dynamicShape()).noInfoBlock());
        BULU_PURPLE   = PokecubeLegends.DECORATION_TAB.register("bulu_purple_totem", () -> new BuluTotem("bulu_purple_totem",
            AbstractBlock.Properties.of(Material.WOOD, MaterialColor.TERRACOTTA_PURPLE).strength(5, 15)
                .sound(SoundType.WOOD).harvestTool(ToolType.AXE).harvestLevel(2).dynamicShape()).noInfoBlock());
        BULU_BLUE   = PokecubeLegends.DECORATION_TAB.register("bulu_blue_totem", () -> new BuluTotem("bulu_blue_totem",
            AbstractBlock.Properties.of(Material.WOOD, MaterialColor.TERRACOTTA_BLUE).strength(5, 15)
                .sound(SoundType.WOOD).harvestTool(ToolType.AXE).harvestLevel(2).dynamicShape()).noInfoBlock());
        BULU_BROWN   = PokecubeLegends.DECORATION_TAB.register("bulu_brown_totem", () -> new BuluTotem("bulu_brown_totem",
            AbstractBlock.Properties.of(Material.WOOD, MaterialColor.TERRACOTTA_BROWN).strength(5, 15)
                .sound(SoundType.WOOD).harvestTool(ToolType.AXE).harvestLevel(2).dynamicShape()).noInfoBlock());
        BULU_GREEN   = PokecubeLegends.DECORATION_TAB.register("bulu_green_totem", () -> new BuluTotem("bulu_green_totem",
            AbstractBlock.Properties.of(Material.WOOD, MaterialColor.TERRACOTTA_GREEN).strength(5, 15)
                .sound(SoundType.WOOD).harvestTool(ToolType.AXE).harvestLevel(2).dynamicShape()).noInfoBlock());
        BULU_RED   = PokecubeLegends.DECORATION_TAB.register("bulu_red_totem", () -> new BuluTotem("bulu_red_totem",
            AbstractBlock.Properties.of(Material.WOOD, MaterialColor.TERRACOTTA_RED).strength(5, 15)
                .sound(SoundType.WOOD).harvestTool(ToolType.AXE).harvestLevel(2).dynamicShape()).noInfoBlock());
        BULU_BLACK   = PokecubeLegends.DECORATION_TAB.register("bulu_black_totem", () -> new BuluTotem("bulu_black_totem",
            AbstractBlock.Properties.of(Material.WOOD, MaterialColor.TERRACOTTA_BLACK).strength(5, 15)
                .sound(SoundType.WOOD).harvestTool(ToolType.AXE).harvestLevel(2).dynamicShape()).noInfoBlock());
        
        // Lele Totem
        LELE_WHITE   = PokecubeLegends.DECORATION_TAB.register("lele_white_totem", () -> new LeleTotem("lele_white_totem",
            AbstractBlock.Properties.of(Material.WOOD, MaterialColor.TERRACOTTA_WHITE).strength(5, 15)
                .sound(SoundType.WOOD).harvestTool(ToolType.AXE).harvestLevel(2).dynamicShape()).noInfoBlock());
        LELE_ORANGE   = PokecubeLegends.DECORATION_TAB.register("lele_orange_totem", () -> new LeleTotem("lele_orange_totem",
            AbstractBlock.Properties.of(Material.WOOD, MaterialColor.TERRACOTTA_ORANGE).strength(5, 15)
                .sound(SoundType.WOOD).harvestTool(ToolType.AXE).harvestLevel(2).dynamicShape()).noInfoBlock());
        LELE_MAGENTA   = PokecubeLegends.DECORATION_TAB.register("lele_magenta_totem", () -> new LeleTotem("lele_magenta_totem",
            AbstractBlock.Properties.of(Material.WOOD, MaterialColor.TERRACOTTA_MAGENTA).strength(5, 15)
                .sound(SoundType.WOOD).harvestTool(ToolType.AXE).harvestLevel(2).dynamicShape()).noInfoBlock());
        LELE_LIGHT_BLUE   = PokecubeLegends.DECORATION_TAB.register("lele_lightblue_totem", () -> new LeleTotem("lele_lightblue_totem",
            AbstractBlock.Properties.of(Material.WOOD, MaterialColor.TERRACOTTA_LIGHT_BLUE).strength(5, 15)
                .sound(SoundType.WOOD).harvestTool(ToolType.AXE).harvestLevel(2).dynamicShape()).noInfoBlock());
        LELE_YELLOW   = PokecubeLegends.DECORATION_TAB.register("lele_yellow_totem", () -> new LeleTotem("lele_yellow_totem",
            AbstractBlock.Properties.of(Material.WOOD, MaterialColor.TERRACOTTA_YELLOW).strength(5, 15)
                .sound(SoundType.WOOD).harvestTool(ToolType.AXE).harvestLevel(2).dynamicShape()).noInfoBlock());
        LELE_LIME   = PokecubeLegends.DECORATION_TAB.register("lele_lime_totem", () -> new LeleTotem("lele_lime_totem",
            AbstractBlock.Properties.of(Material.WOOD, MaterialColor.TERRACOTTA_LIGHT_GREEN).strength(5, 15)
                .sound(SoundType.WOOD).harvestTool(ToolType.AXE).harvestLevel(2).dynamicShape()).noInfoBlock());
        LELE_PINK   = PokecubeLegends.DECORATION_TAB.register("lele_pink_totem", () -> new LeleTotem("lele_pink_totem",
            AbstractBlock.Properties.of(Material.WOOD, MaterialColor.TERRACOTTA_PINK).strength(5, 15)
                .sound(SoundType.WOOD).harvestTool(ToolType.AXE).harvestLevel(2).dynamicShape()).noInfoBlock());
        LELE_GRAY   = PokecubeLegends.DECORATION_TAB.register("lele_gray_totem", () -> new LeleTotem("lele_gray_totem",
            AbstractBlock.Properties.of(Material.WOOD, MaterialColor.TERRACOTTA_GRAY).strength(5, 15)
                .sound(SoundType.WOOD).harvestTool(ToolType.AXE).harvestLevel(2).dynamicShape()).noInfoBlock());
        LELE_LIGHT_GRAY   = PokecubeLegends.DECORATION_TAB.register("lele_lightgray_totem", () -> new LeleTotem("lele_lightgray_totem",
            AbstractBlock.Properties.of(Material.WOOD, MaterialColor.TERRACOTTA_LIGHT_GRAY).strength(5, 15)
                .sound(SoundType.WOOD).harvestTool(ToolType.AXE).harvestLevel(2).dynamicShape()).noInfoBlock());
        LELE_CYAN   = PokecubeLegends.DECORATION_TAB.register("lele_cyan_totem", () -> new LeleTotem("lele_cyan_totem",
            AbstractBlock.Properties.of(Material.WOOD, MaterialColor.TERRACOTTA_CYAN).strength(5, 15)
                .sound(SoundType.WOOD).harvestTool(ToolType.AXE).harvestLevel(2).dynamicShape()).noInfoBlock());
        LELE_PURPLE   = PokecubeLegends.DECORATION_TAB.register("lele_purple_totem", () -> new LeleTotem("lele_purple_totem",
            AbstractBlock.Properties.of(Material.WOOD, MaterialColor.TERRACOTTA_PURPLE).strength(5, 15)
                .sound(SoundType.WOOD).harvestTool(ToolType.AXE).harvestLevel(2).dynamicShape()).noInfoBlock());
        LELE_BLUE   = PokecubeLegends.DECORATION_TAB.register("lele_blue_totem", () -> new LeleTotem("lele_blue_totem",
            AbstractBlock.Properties.of(Material.WOOD, MaterialColor.TERRACOTTA_BLUE).strength(5, 15)
                .sound(SoundType.WOOD).harvestTool(ToolType.AXE).harvestLevel(2).dynamicShape()).noInfoBlock());
        LELE_BROWN   = PokecubeLegends.DECORATION_TAB.register("lele_brown_totem", () -> new LeleTotem("lele_brown_totem",
            AbstractBlock.Properties.of(Material.WOOD, MaterialColor.TERRACOTTA_BROWN).strength(5, 15)
                .sound(SoundType.WOOD).harvestTool(ToolType.AXE).harvestLevel(2).dynamicShape()).noInfoBlock());
        LELE_GREEN   = PokecubeLegends.DECORATION_TAB.register("lele_green_totem", () -> new LeleTotem("lele_green_totem",
            AbstractBlock.Properties.of(Material.WOOD, MaterialColor.TERRACOTTA_GREEN).strength(5, 15)
                .sound(SoundType.WOOD).harvestTool(ToolType.AXE).harvestLevel(2).dynamicShape()).noInfoBlock());
        LELE_RED   = PokecubeLegends.DECORATION_TAB.register("lele_red_totem", () -> new LeleTotem("lele_red_totem",
            AbstractBlock.Properties.of(Material.WOOD, MaterialColor.TERRACOTTA_RED).strength(5, 15)
                .sound(SoundType.WOOD).harvestTool(ToolType.AXE).harvestLevel(2).dynamicShape()).noInfoBlock());
        LELE_BLACK   = PokecubeLegends.DECORATION_TAB.register("lele_black_totem", () -> new LeleTotem("lele_black_totem",
            AbstractBlock.Properties.of(Material.WOOD, MaterialColor.TERRACOTTA_BLACK).strength(5, 15)
                .sound(SoundType.WOOD).harvestTool(ToolType.AXE).harvestLevel(2).dynamicShape()).noInfoBlock());
        
        // Fini Totem
        FINI_WHITE   = PokecubeLegends.DECORATION_TAB.register("fini_white_totem", () -> new FiniTotem("fini_white_totem",
            AbstractBlock.Properties.of(Material.WOOD, MaterialColor.TERRACOTTA_WHITE).strength(5, 15)
                .sound(SoundType.WOOD).harvestTool(ToolType.AXE).harvestLevel(2).dynamicShape()).noInfoBlock());
        FINI_ORANGE   = PokecubeLegends.DECORATION_TAB.register("fini_orange_totem", () -> new FiniTotem("fini_orange_totem",
            AbstractBlock.Properties.of(Material.WOOD, MaterialColor.TERRACOTTA_ORANGE).strength(5, 15)
                .sound(SoundType.WOOD).harvestTool(ToolType.AXE).harvestLevel(2).dynamicShape()).noInfoBlock());
        FINI_MAGENTA   = PokecubeLegends.DECORATION_TAB.register("fini_magenta_totem", () -> new FiniTotem("fini_magenta_totem",
            AbstractBlock.Properties.of(Material.WOOD, MaterialColor.TERRACOTTA_MAGENTA).strength(5, 15)
                .sound(SoundType.WOOD).harvestTool(ToolType.AXE).harvestLevel(2).dynamicShape()).noInfoBlock());
        FINI_LIGHT_BLUE   = PokecubeLegends.DECORATION_TAB.register("fini_lightblue_totem", () -> new FiniTotem("fini_lightblue_totem",
            AbstractBlock.Properties.of(Material.WOOD, MaterialColor.TERRACOTTA_LIGHT_BLUE).strength(5, 15)
                .sound(SoundType.WOOD).harvestTool(ToolType.AXE).harvestLevel(2).dynamicShape()).noInfoBlock());
        FINI_YELLOW   = PokecubeLegends.DECORATION_TAB.register("fini_yellow_totem", () -> new FiniTotem("fini_yellow_totem",
            AbstractBlock.Properties.of(Material.WOOD, MaterialColor.TERRACOTTA_YELLOW).strength(5, 15)
                .sound(SoundType.WOOD).harvestTool(ToolType.AXE).harvestLevel(2).dynamicShape()).noInfoBlock());
        FINI_LIME   = PokecubeLegends.DECORATION_TAB.register("fini_lime_totem", () -> new FiniTotem("fini_lime_totem",
            AbstractBlock.Properties.of(Material.WOOD, MaterialColor.TERRACOTTA_LIGHT_GREEN).strength(5, 15)
                .sound(SoundType.WOOD).harvestTool(ToolType.AXE).harvestLevel(2).dynamicShape()).noInfoBlock());
        FINI_PINK   = PokecubeLegends.DECORATION_TAB.register("fini_pink_totem", () -> new FiniTotem("fini_pink_totem",
            AbstractBlock.Properties.of(Material.WOOD, MaterialColor.TERRACOTTA_PINK).strength(5, 15)
                .sound(SoundType.WOOD).harvestTool(ToolType.AXE).harvestLevel(2).dynamicShape()).noInfoBlock());
        FINI_GRAY   = PokecubeLegends.DECORATION_TAB.register("fini_gray_totem", () -> new FiniTotem("fini_gray_totem",
            AbstractBlock.Properties.of(Material.WOOD, MaterialColor.TERRACOTTA_GRAY).strength(5, 15)
                .sound(SoundType.WOOD).harvestTool(ToolType.AXE).harvestLevel(2).dynamicShape()).noInfoBlock());
        FINI_LIGHT_GRAY   = PokecubeLegends.DECORATION_TAB.register("fini_lightgray_totem", () -> new FiniTotem("fini_lightgray_totem",
            AbstractBlock.Properties.of(Material.WOOD, MaterialColor.TERRACOTTA_LIGHT_GRAY).strength(5, 15)
                .sound(SoundType.WOOD).harvestTool(ToolType.AXE).harvestLevel(2).dynamicShape()).noInfoBlock());
        FINI_CYAN   = PokecubeLegends.DECORATION_TAB.register("fini_cyan_totem", () -> new FiniTotem("fini_cyan_totem",
            AbstractBlock.Properties.of(Material.WOOD, MaterialColor.TERRACOTTA_CYAN).strength(5, 15)
                .sound(SoundType.WOOD).harvestTool(ToolType.AXE).harvestLevel(2).dynamicShape()).noInfoBlock());
        FINI_PURPLE   = PokecubeLegends.DECORATION_TAB.register("fini_purple_totem", () -> new FiniTotem("fini_purple_totem",
            AbstractBlock.Properties.of(Material.WOOD, MaterialColor.TERRACOTTA_PURPLE).strength(5, 15)
                .sound(SoundType.WOOD).harvestTool(ToolType.AXE).harvestLevel(2).dynamicShape()).noInfoBlock());
        FINI_BLUE   = PokecubeLegends.DECORATION_TAB.register("fini_blue_totem", () -> new FiniTotem("fini_blue_totem",
            AbstractBlock.Properties.of(Material.WOOD, MaterialColor.TERRACOTTA_BLUE).strength(5, 15)
                .sound(SoundType.WOOD).harvestTool(ToolType.AXE).harvestLevel(2).dynamicShape()).noInfoBlock());
        FINI_BROWN   = PokecubeLegends.DECORATION_TAB.register("fini_brown_totem", () -> new FiniTotem("fini_brown_totem",
            AbstractBlock.Properties.of(Material.WOOD, MaterialColor.TERRACOTTA_BROWN).strength(5, 15)
                .sound(SoundType.WOOD).harvestTool(ToolType.AXE).harvestLevel(2).dynamicShape()).noInfoBlock());
        FINI_GREEN   = PokecubeLegends.DECORATION_TAB.register("fini_green_totem", () -> new FiniTotem("fini_green_totem",
            AbstractBlock.Properties.of(Material.WOOD, MaterialColor.TERRACOTTA_GREEN).strength(5, 15)
                .sound(SoundType.WOOD).harvestTool(ToolType.AXE).harvestLevel(2).dynamicShape()).noInfoBlock());
        FINI_RED   = PokecubeLegends.DECORATION_TAB.register("fini_red_totem", () -> new FiniTotem("fini_red_totem",
            AbstractBlock.Properties.of(Material.WOOD, MaterialColor.TERRACOTTA_RED).strength(5, 15)
                .sound(SoundType.WOOD).harvestTool(ToolType.AXE).harvestLevel(2).dynamicShape()).noInfoBlock());
        FINI_BLACK   = PokecubeLegends.DECORATION_TAB.register("fini_black_totem", () -> new FiniTotem("fini_black_totem",
            AbstractBlock.Properties.of(Material.WOOD, MaterialColor.TERRACOTTA_BLACK).strength(5, 15)
                .sound(SoundType.WOOD).harvestTool(ToolType.AXE).harvestLevel(2).dynamicShape()).noInfoBlock());

        // Dimensions
        SPECTRUM_GLASS = PokecubeLegends.BLOCKS_TAB.register("spectrum_glass", () -> new SpectrumGlass("spectrum_glass",
                DyeColor.ORANGE, AbstractBlock.Properties.of(Material.GLASS, MaterialColor.COLOR_ORANGE).noOcclusion().sound(SoundType.GLASS)
                .strength(0.3f)));
        ULTRA_AGED_DIRT = PokecubeLegends.BLOCKS_TAB.register("ultradirt3", () -> new BlockBase("ultradirt3",
                Material.GRASS, MaterialColor.TERRACOTTA_YELLOW, 0.5f, SoundType.WET_GRASS, ToolType.SHOVEL, 1).noInfoBlock());
        ULTRA_CORRUPTED_DIRT = PokecubeLegends.BLOCKS_TAB.register("ultradirt4", () -> new DirtCorruptedBlock("ultradirt4",
            AbstractBlock.Properties.of(Material.STONE, MaterialColor.TERRACOTTA_PURPLE).sound(SoundType.METAL)
                .strength(0.9f).harvestTool(ToolType.PICKAXE).harvestLevel(1).requiresCorrectToolForDrops()));
        ULTRA_MAGNETIC = PokecubeLegends.BLOCKS_TAB.register("ultramagnetic", () -> new MagneticBlock("ultramagnetic",
                AbstractBlock.Properties.of(Material.STONE, MaterialColor.COLOR_BLUE).sound(SoundType.STONE).strength(3, 8).harvestTool(
                ToolType.PICKAXE).harvestLevel(1)).noInfoBlock());
        ULTRA_MUSHROOM_GRASS = PokecubeLegends.BLOCKS_TAB.register("ultragrass1", () -> new GrassMushroomBlock("ultragrass1",
            AbstractBlock.Properties.of(Material.GRASS, MaterialColor.COLOR_RED).sound(SoundType.GRASS)
                .strength(1f, 2f).harvestTool(ToolType.SHOVEL).harvestLevel(1).randomTicks()));
        ULTRA_MUSHROOM_DIRT = PokecubeLegends.BLOCKS_TAB.register("ultradirt1", () -> new BlockBase("ultradirt1",
                Material.CLAY, MaterialColor.COLOR_PURPLE, 0.5f, SoundType.GRAVEL, ToolType.SHOVEL, 1).noInfoBlock());
        ULTRA_JUNGLE_GRASS = PokecubeLegends.BLOCKS_TAB.register("ultragrass2", () -> new GrassJungleBlock("ultragrass2",
            AbstractBlock.Properties.of(Material.GRASS, MaterialColor.WARPED_NYLIUM).sound(SoundType.GRASS)
                .strength(1f, 2f).harvestTool(ToolType.SHOVEL).harvestLevel(1).randomTicks()));
        ULTRA_JUNGLE_DIRT = PokecubeLegends.BLOCKS_TAB.register("ultradirt2", () -> new BlockBase("ultradirt2",
                Material.VEGETABLE, MaterialColor.TERRACOTTA_YELLOW, 0.5f, SoundType.GRAVEL, ToolType.SHOVEL, 1).noInfoBlock());
        ULTRA_CORRUPTED_GRASS = PokecubeLegends.BLOCKS_TAB.register("ultrasand1", () -> new GrassCorruptedBlock("ultrasand1",
            AbstractBlock.Properties.of(Material.GRASS, MaterialColor.TERRACOTTA_BLUE).sound(SoundType.SCAFFOLDING)
                .strength(4f, 5f).harvestTool(ToolType.PICKAXE).harvestLevel(1).randomTicks().requiresCorrectToolForDrops()));
        ULTRA_AGED_GRASS = PokecubeLegends.BLOCKS_TAB.register("ultragrass3", () -> new GrassAgedBlock("ultragrass3",
            AbstractBlock.Properties.of(Material.GRASS, MaterialColor.COLOR_ORANGE).sound(SoundType.GRASS)
                .strength(1f, 2f).harvestTool(ToolType.SHOVEL).harvestLevel(1).randomTicks()));

        // Crystal Blocks
        CRYSTAL = PokecubeLegends.BLOCKS_TAB.register("temporal_crystal", () -> new BlockBase(
                "temporal_crystal", Material.GLASS, MaterialColor.COLOR_LIGHT_BLUE, 1.5f, SoundType.GLASS, ToolType.PICKAXE, 1).noInfoBlock());
        CRYSTAL_STAIRS = PokecubeLegends.BLOCKS_TAB.register("crystal_stairs",
                () -> new ItemGenerator.GenericStairs(Blocks.STONE_STAIRS.defaultBlockState(), AbstractBlock.Properties.of(
                Material.GLASS, MaterialColor.TERRACOTTA_LIGHT_BLUE).strength(0.4F, 0.3f).sound(SoundType.GLASS)));
        CRYSTAL_SLAB = PokecubeLegends.BLOCKS_TAB.register("crystal_slab", () -> new SlabBlock(AbstractBlock.Properties.of(
        		Material.GLASS, MaterialColor.TERRACOTTA_LIGHT_BLUE).strength(0.4F, 0.3f).sound(SoundType.GLASS)));
        CRYSTAL_BUTTON = PokecubeLegends.BLOCKS_TAB.register("crystal_button",
                () -> new ItemGenerator.GenericButton(AbstractBlock.Properties.of(Material.GLASS, MaterialColor.SNOW).sound(SoundType.GLASS)
                .noCollission().strength(0.5F)));
        CRYSTAL_BRICK = PokecubeLegends.DECORATION_TAB.register("crystalbrick", () -> new BlockBase("crystalbrick",
                Material.ICE_SOLID, MaterialColor.COLOR_LIGHT_BLUE, 0.4F, SoundType.GLASS, ToolType.PICKAXE, 1).noInfoBlock());
        CRYSTAL_BRICKS_STAIRS = PokecubeLegends.DECORATION_TAB.register("crystal_bricks_stairs",
                () -> new ItemGenerator.GenericStairs(Blocks.STONE_STAIRS.defaultBlockState(), AbstractBlock.Properties.of(
                Material.GLASS, MaterialColor.TERRACOTTA_LIGHT_BLUE).strength(0.4F, 0.3f).sound(SoundType.GLASS)));
        CRYSTAL_BRICKS_SLAB = PokecubeLegends.DECORATION_TAB.register("crystal_bricks_slab", () -> new SlabBlock(AbstractBlock.Properties.of(
        		Material.GLASS, MaterialColor.TERRACOTTA_LIGHT_BLUE).strength(0.4F, 0.3f).sound(SoundType.GLASS)));
        CRYSTALLIZED_CACTUS = PokecubeLegends.BLOCKS_TAB.register("crystallized_cactus", () -> new CrystallizedCactus("crystallized_cactus",
                AbstractBlock.Properties.of(Material.CACTUS, MaterialColor.SNOW).sound(SoundType.GLASS).strength(0.4F)));
        CRYSTALLIZED_BUSH = PokecubeLegends.BLOCKS_TAB.register("crystallized_bush", () -> new CrystallizedBush("crystallized_bush",
                AbstractBlock.Properties.of(Material.PLANT, MaterialColor.SNOW).sound(SoundType.GLASS).noCollission().instabreak()));
        TALL_CRYSTALLIZED_BUSH = PokecubeLegends.BLOCKS_TAB.register("tall_crystallized_bush", () -> new TallCrystallizedBush("tall_crystallized_bush",
                AbstractBlock.Properties.of(Material.PLANT, MaterialColor.SNOW).sound(SoundType.GLASS).noCollission().instabreak()));
        
        // Dark Stone
        ULTRA_DARKSTONE = PokecubeLegends.BLOCKS_TAB.register("ultracobbles", () -> new DarkStoneBlock("ultracobbles",
                Material.STONE, MaterialColor.COLOR_BLACK).noInfoBlock());
        ULTRA_DARKSTONE_SLAB = PokecubeLegends.BLOCKS_TAB.register("ultra_darkstone_slab", () -> new SlabBlock(AbstractBlock.Properties.of(
        		Material.STONE, MaterialColor.COLOR_BLACK).strength(2.0F, 3.0f).sound(SoundType.GILDED_BLACKSTONE).requiresCorrectToolForDrops()));
        ULTRA_DARKSTONE_STAIRS = PokecubeLegends.BLOCKS_TAB.register("ultra_darkstone_stairs",
                () -> new ItemGenerator.GenericStairs(Blocks.STONE_STAIRS.defaultBlockState(), AbstractBlock.Properties.of(
                		Material.STONE, MaterialColor.COLOR_BLACK).strength(2.0F, 3.0f).sound(SoundType.GILDED_BLACKSTONE).requiresCorrectToolForDrops()));
        ULTRA_DARK_COBBLESTONE = PokecubeLegends.BLOCKS_TAB.register("ultrarock", () -> new BlockBase("ultrarock",
                Material.STONE, MaterialColor.COLOR_BLACK, 0.8f, SoundType.STONE, ToolType.PICKAXE, 2).noInfoBlock());
        ULTRA_DARK_COBBLESTONE_SLAB = PokecubeLegends.BLOCKS_TAB.register("ultra_dark_cobblestone_slab", () -> new SlabBlock(AbstractBlock.Properties.of(
        		Material.STONE, MaterialColor.COLOR_BLACK).strength(2.0F, 3.0f).sound(SoundType.GILDED_BLACKSTONE).requiresCorrectToolForDrops()));
        ULTRA_DARK_COBBLESTONE_STAIRS = PokecubeLegends.BLOCKS_TAB.register("ultra_dark_cobblestone_stairs",
                () -> new ItemGenerator.GenericStairs(Blocks.STONE_STAIRS.defaultBlockState(), AbstractBlock.Properties.of(
                		Material.STONE, MaterialColor.COLOR_BLACK).strength(2.0F, 3.0f).sound(SoundType.GILDED_BLACKSTONE).requiresCorrectToolForDrops()));
        ULTRA_DARKSTONE_BRICKS = PokecubeLegends.BLOCKS_TAB.register("ultra_darkstone_bricks", () -> new DarkStoneBlock("ultra_darkstone_bricks",
                Material.STONE, MaterialColor.COLOR_BLACK).noInfoBlock());
        ULTRA_DARKSTONE_BRICKS_SLAB = PokecubeLegends.BLOCKS_TAB.register("ultra_darkstone_bricks_slab", () -> new SlabBlock(AbstractBlock.Properties.of(
        		Material.STONE, MaterialColor.COLOR_BLACK).strength(2.0F, 3.0f).sound(SoundType.GILDED_BLACKSTONE).requiresCorrectToolForDrops()));
        ULTRA_DARKSTONE_BRICKS_STAIRS = PokecubeLegends.BLOCKS_TAB.register("ultra_darkstone_bricks_stairs",
                () -> new ItemGenerator.GenericStairs(Blocks.STONE_STAIRS.defaultBlockState(), AbstractBlock.Properties.of(
                		Material.STONE, MaterialColor.COLOR_BLACK).strength(2.0F, 3.0f).sound(SoundType.GILDED_BLACKSTONE).requiresCorrectToolForDrops()));
        ULTRA_DARKSTONE_BUTTON = PokecubeLegends.BLOCKS_TAB.register("ultra_darkstone_button",
                () -> new ItemGenerator.GenericButton(AbstractBlock.Properties.of(Material.STONE, MaterialColor.COLOR_BLACK).sound(SoundType.NETHER_BRICKS)
                        .noCollission().strength(0.5F).requiresCorrectToolForDrops()));
        ULTRA_DARKSTONE_PR_PLATE = PokecubeLegends.BLOCKS_TAB.register("ultra_darkstone_pressure_plate",
                () -> new ItemGenerator.GenericPressurePlate(PressurePlateBlock.Sensitivity.MOBS, AbstractBlock.Properties
                        .of(Material.STONE, MaterialColor.COLOR_BLACK).sound(SoundType.NETHER_BRICKS).noCollission().strength(
                                0.7F).requiresCorrectToolForDrops()));
        
        // Ultra Desert
        ULTRA_SAND = PokecubeLegends.BLOCKS_TAB.register("ultrasand", () -> new SandUltraBlock("ultrasand",
                Material.SAND, MaterialColor.SNOW).noInfoBlock());
        ULTRA_SANDSTONE = PokecubeLegends.BLOCKS_TAB.register("ultrasandstone", () -> new BlockBase("ultrasandstone",
                Material.STONE, MaterialColor.SNOW, 1f, SoundType.STONE, ToolType.PICKAXE, 2).noInfoBlock());
        ULTRA_SANDSTONE_SLAB = PokecubeLegends.BLOCKS_TAB.register("ultra_sandstone_slab", () -> new SlabBlock(AbstractBlock.Properties.of(
        		Material.STONE, MaterialColor.SNOW).strength(2.0F, 3.0f).sound(SoundType.STONE).requiresCorrectToolForDrops()));
        ULTRA_SANDSTONE_STAIRS = PokecubeLegends.BLOCKS_TAB.register("ultra_sandstone_stairs",
                () -> new ItemGenerator.GenericStairs(Blocks.STONE_STAIRS.defaultBlockState(), AbstractBlock.Properties.of(
                		Material.STONE, MaterialColor.SNOW).strength(2.0F, 3.0f).sound(SoundType.STONE).requiresCorrectToolForDrops()));
        ULTRA_SANDBRICK = PokecubeLegends.BLOCKS_TAB.register("ultra_sandbrick", () -> new BlockBase("ultra_sandbrick",
                Material.STONE, MaterialColor.SNOW, 1.4f, SoundType.STONE, ToolType.PICKAXE, 1).noInfoBlock());
        ULTRA_SANDBRICK_SLAB = PokecubeLegends.BLOCKS_TAB.register("ultra_sandbrick_slab", () -> new SlabBlock(AbstractBlock.Properties.of(
        		Material.STONE, MaterialColor.SNOW).strength(2.0F, 3.0f).sound(SoundType.STONE).requiresCorrectToolForDrops()));
        ULTRA_SANDBRICK_STAIRS = PokecubeLegends.BLOCKS_TAB.register("ultra_sandbrick_stairs",
                () -> new ItemGenerator.GenericStairs(Blocks.STONE_STAIRS.defaultBlockState(), AbstractBlock.Properties.of(
                		Material.STONE, MaterialColor.SNOW).strength(2.0F, 3.0f).sound(SoundType.STONE).requiresCorrectToolForDrops()));
        ULTRA_SANDSTONE_SMOOTH = PokecubeLegends.BLOCKS_TAB.register("ultra_sandstone_smooth", () -> new BlockBase("ultra_sandstone_smooth",
                Material.STONE, MaterialColor.SNOW, 1.5f, SoundType.STONE, ToolType.PICKAXE, 1).noInfoBlock());
        ULTRA_SANDSTONE_SMOOTH_SLAB = PokecubeLegends.BLOCKS_TAB.register("ultra_sandstone_smooth_slab", () -> new SlabBlock(AbstractBlock.Properties.of(
        		Material.STONE, MaterialColor.SNOW).strength(2.0F, 3.0f).sound(SoundType.STONE).requiresCorrectToolForDrops()));
        ULTRA_SANDSTONE_SMOOTH_STAIRS = PokecubeLegends.BLOCKS_TAB.register("ultra_sandstone_smooth_stairs",
                () -> new ItemGenerator.GenericStairs(Blocks.STONE_STAIRS.defaultBlockState(), AbstractBlock.Properties.of(
                		Material.STONE, MaterialColor.SNOW).strength(2.0F, 3.0f).sound(SoundType.STONE).requiresCorrectToolForDrops()));
        ULTRA_SANDSTONE_BUTTON = PokecubeLegends.BLOCKS_TAB.register("ultra_sandstone_button",
                () -> new ItemGenerator.GenericButton(AbstractBlock.Properties.of(Material.STONE, MaterialColor.SAND).sound(SoundType.STONE)
                        .noCollission().strength(0.5F).requiresCorrectToolForDrops()));
        ULTRA_SANDSTONE_PR_PLATE = PokecubeLegends.BLOCKS_TAB.register("ultra_sandstone_pressure_plate",
                () -> new ItemGenerator.GenericPressurePlate(PressurePlateBlock.Sensitivity.MOBS, AbstractBlock.Properties
                .of(Material.STONE, MaterialColor.SNOW).sound(SoundType.STONE).noCollission().strength(0.7F).requiresCorrectToolForDrops()));
        
        // Distortic World
        DISTORTIC_GRASS = PokecubeLegends.BLOCKS_TAB.register("distortic_grass", () -> new GrassDistorticBlock(
                AbstractBlock.Properties.of(Material.GRASS, MaterialColor.TERRACOTTA_PINK).sound(SoundType.NETHER_WART).strength(1, 2)
                .harvestTool(ToolType.PICKAXE).harvestLevel(1).requiresCorrectToolForDrops().randomTicks()));
        DISTORTIC_STONE = PokecubeLegends.BLOCKS_TAB.register("distortic_stone", () -> new DistorticStoneBlock("distortic_stone",
            AbstractBlock.Properties.of(Material.STONE, MaterialColor.TERRACOTTA_BLACK).sound(SoundType.STONE)
                .strength(1.5f).harvestTool(ToolType.PICKAXE).harvestLevel(1).requiresCorrectToolForDrops()));
        DISTORTIC_STONE_SLAB = PokecubeLegends.BLOCKS_TAB.register("distortic_stone_slab", () -> new SlabBlock(AbstractBlock.Properties.of(
        		Material.STONE, MaterialColor.TERRACOTTA_BLACK).strength(2.0F, 3.0f).sound(SoundType.STONE)
                .requiresCorrectToolForDrops()));
        DISTORTIC_STONE_STAIRS = PokecubeLegends.BLOCKS_TAB.register("distortic_stone_stairs",
                () -> new ItemGenerator.GenericStairs(Blocks.STONE_STAIRS.defaultBlockState(), AbstractBlock.Properties.of(
                Material.STONE, MaterialColor.TERRACOTTA_BLACK).strength(2.0F, 3.0f).sound(SoundType.STONE).requiresCorrectToolForDrops()));
        DISTORTIC_MIRROR = PokecubeLegends.BLOCKS_TAB.register("distortic_mirror", () -> new BlockBase("distortic_mirror",
                Material.GLASS, MaterialColor.CLAY, 2.5f, SoundType.GLASS, ToolType.PICKAXE, 1).noInfoBlock());
        DISTORTIC_OW_GLASS = PokecubeLegends.DECORATION_TAB.register("distortic_one_way_glass", () ->
            new DistorticOneWayGlass("distortic_one_way_glass",
                DyeColor.WHITE, AbstractBlock.Properties.of(Material.GLASS, MaterialColor.CLAY).noOcclusion().sound(SoundType.GLASS)
                .strength(2.5f).harvestTool(ToolType.PICKAXE).requiresCorrectToolForDrops()));
        DISTORTIC_OW_GLASS_WHITE = PokecubeLegends.DECORATION_TAB.register("distortic_one_way_white_stained_glass", () ->
            new DistorticOneWayGlass("distortic_one_way_white_stained_glass",
                DyeColor.WHITE, AbstractBlock.Properties.of(Material.GLASS, MaterialColor.CLAY).noOcclusion().sound(SoundType.GLASS)
                .strength(2.5f).harvestTool(ToolType.PICKAXE).requiresCorrectToolForDrops()));

        // Ultra Stones
        ULTRA_STONE = PokecubeLegends.BLOCKS_TAB.register("ultrastone", () -> new BlockBase("ultrastone", Material.STONE, 
        		MaterialColor.COLOR_CYAN, 1.5f, SoundType.STONE, ToolType.PICKAXE, 2).noInfoBlock());
        ULTRA_STONE_SLAB = PokecubeLegends.BLOCKS_TAB.register("ultra_stone_slab", () -> new SlabBlock(AbstractBlock.Properties.of(
        		Material.STONE, MaterialColor.COLOR_LIGHT_GRAY).strength(2.0F, 3.0f).sound(SoundType.STONE).requiresCorrectToolForDrops()));
        ULTRA_STONE_STAIRS = PokecubeLegends.BLOCKS_TAB.register("ultra_stone_stairs",
                () -> new ItemGenerator.GenericStairs(Blocks.STONE_STAIRS.defaultBlockState(), AbstractBlock.Properties.of(
                Material.STONE, MaterialColor.COLOR_LIGHT_GRAY).strength(2.0F, 3.0f).sound(SoundType.STONE).requiresCorrectToolForDrops()));
        ULTRA_COBBLESTONE = PokecubeLegends.BLOCKS_TAB.register("ultra_cobblestone", () -> new BlockBase("ultra_cobblestone", Material.STONE, 
        		MaterialColor.TERRACOTTA_CYAN, 1.5f, SoundType.STONE, ToolType.PICKAXE, 2).noInfoBlock());
        ULTRA_COBBLESTONE_SLAB = PokecubeLegends.BLOCKS_TAB.register("ultra_cobblestone_slab", () -> new SlabBlock(AbstractBlock.Properties.of(
        		Material.STONE, MaterialColor.COLOR_LIGHT_GRAY).strength(2.0F, 3.0f).sound(SoundType.STONE).requiresCorrectToolForDrops()));
        ULTRA_COBBLESTONE_STAIRS = PokecubeLegends.BLOCKS_TAB.register("ultra_cobblestone_stairs",
                () -> new ItemGenerator.GenericStairs(Blocks.STONE_STAIRS.defaultBlockState(), AbstractBlock.Properties.of(
                Material.STONE, MaterialColor.COLOR_LIGHT_GRAY).strength(2.0F, 3.0f).sound(SoundType.STONE).requiresCorrectToolForDrops()));
        ULTRA_STONEBRICK = PokecubeLegends.BLOCKS_TAB.register("ultra_stonebricks", () -> new BlockBase("ultra_stonebricks", Material.STONE, 
        		MaterialColor.TERRACOTTA_CYAN, 1.5f, SoundType.STONE, ToolType.PICKAXE, 2).noInfoBlock());
        ULTRA_STONEBRICK_SLAB = PokecubeLegends.BLOCKS_TAB.register("ultra_stonebricks_slab", () -> new SlabBlock(
                AbstractBlock.Properties.of(Material.STONE, MaterialColor.COLOR_LIGHT_GRAY).strength(2.0F, 3.0f)
                .sound(SoundType.STONE).requiresCorrectToolForDrops()));
        ULTRA_STONEBRICK_STAIRS = PokecubeLegends.BLOCKS_TAB.register("ultra_stonebricks_stairs",
                () -> new ItemGenerator.GenericStairs(Blocks.STONE_STAIRS.defaultBlockState(), AbstractBlock.Properties.of(
                Material.STONE, MaterialColor.COLOR_LIGHT_GRAY).strength(2.0F, 3.0f).sound(SoundType.STONE)
                .requiresCorrectToolForDrops()));
        ULTRA_STONE_BUTTON = PokecubeLegends.BLOCKS_TAB.register("ultra_stone_button",
                () -> new ItemGenerator.GenericButton(AbstractBlock.Properties.of(Material.STONE, MaterialColor.COLOR_BLUE)
                .sound(SoundType.BAMBOO).noCollission().strength(0.5F).requiresCorrectToolForDrops()));
        ULTRA_STONE_PR_PLATE = PokecubeLegends.BLOCKS_TAB.register("ultra_stone_pressure_plate",
                () -> new ItemGenerator.GenericPressurePlate(PressurePlateBlock.Sensitivity.MOBS, AbstractBlock.Properties
                .of(Material.STONE, MaterialColor.COLOR_BLUE).sound(SoundType.BAMBOO).noCollission().strength(0.7F).requiresCorrectToolForDrops()));
        
        //
        ULTRA_METAL = PokecubeLegends.BLOCKS_TAB.register("ultrablock", () -> new BlockBase("ultrablock", Material.METAL, 
        		MaterialColor.COLOR_LIGHT_GREEN, 5.0f, 10f, SoundType.STONE, ToolType.PICKAXE, 2).noInfoBlock());
        ULTRA_METAL_SLAB = PokecubeLegends.BLOCKS_TAB.register("ultra_metal_slab", () -> new SlabBlock(AbstractBlock.Properties.of(
        		Material.STONE, MaterialColor.COLOR_LIGHT_GRAY).strength(2.0F, 3.0f).sound(SoundType.STONE)
                .requiresCorrectToolForDrops()));
        ULTRA_METAL_STAIRS = PokecubeLegends.BLOCKS_TAB.register("ultra_metal_stairs",
                () -> new ItemGenerator.GenericStairs(Blocks.STONE_STAIRS.defaultBlockState(), AbstractBlock.Properties.of(
                Material.STONE, MaterialColor.COLOR_LIGHT_GREEN).strength(2.0F, 3.0f).sound(SoundType.STONE)
                .requiresCorrectToolForDrops()));
        ULTRA_METAL_BUTTON = PokecubeLegends.BLOCKS_TAB.register("ultra_metal_button",
                () -> new ItemGenerator.GenericButton(AbstractBlock.Properties.of(Material.METAL, MaterialColor.COLOR_LIGHT_GREEN)
                .sound(SoundType.METAL).noCollission().strength(0.5F).requiresCorrectToolForDrops()));
        ULTRA_METAL_PR_PLATE = PokecubeLegends.BLOCKS_TAB.register("ultra_metal_pressure_plate",
                () -> new ItemGenerator.GenericPressurePlate(PressurePlateBlock.Sensitivity.MOBS, AbstractBlock.Properties
                .of(Material.METAL, MaterialColor.COLOR_LIGHT_GREEN).sound(SoundType.METAL).noCollission().strength(0.7F).requiresCorrectToolForDrops()));
        
        // Torches
        ULTRA_TORCH1 = PokecubeLegends.BLOCKS_TAB.register("ultra_torch1", () -> new UltraTorch1());
        ULTRA_TORCH1_WALL = PokecubeLegends.BLOCKS_TAB.register("ultra_torch1_wall", () -> new UltraTorch1Wall());

        // Plants
        INVERTED_SAPLING = PokecubeLegends.BLOCKS_TAB.register("ultra_sapling01", () -> new SaplingBase(
                () -> new Ultra_Tree01(), AbstractBlock.Properties.of(Material.PLANT, MaterialColor.COLOR_BLUE)
                .strength(0f, 1f).sound(SoundType.GRASS).noCollission().noOcclusion()));
        ULTRA_JUNGLE_SAPLING = PokecubeLegends.BLOCKS_TAB.register("ultra_sapling02", () -> new SaplingBase(
                () -> new Ultra_Tree02(), AbstractBlock.Properties.of(Material.PLANT, MaterialColor.PLANT)
                .strength(0f, 1f).sound(SoundType.GRASS).noCollission().noOcclusion()));
        AGED_SAPLING = PokecubeLegends.BLOCKS_TAB.register("ultra_sapling03", () -> new SaplingBase(
                () -> new Ultra_Tree03(), AbstractBlock.Properties.of(Material.PLANT, MaterialColor.COLOR_YELLOW)
                .strength(0f, 1f).sound(SoundType.GRASS).noCollission().noOcclusion()));
        DISTORTIC_SAPLING = PokecubeLegends.BLOCKS_TAB.register("distortic_sapling", () -> new SaplingBase(
                () -> new Distortic_Tree(), AbstractBlock.Properties.of(Material.PLANT, MaterialColor.COLOR_PURPLE)
                .strength(0f, 1f).sound(SoundType.GRASS).noCollission().noOcclusion()));
        CORRUPTED_SAPLING = PokecubeLegends.BLOCKS_TAB.register("corrupted_sapling", () -> new SaplingBase(
                () -> new Ultra_Tree04(), AbstractBlock.Properties.of(Material.PLANT, MaterialColor.COLOR_BLACK)
                .strength(0f, 1f).sound(SoundType.GRASS).noCollission().noOcclusion()));
        MIRAGE_SAPLING = PokecubeLegends.BLOCKS_TAB.register("mirage_sapling", () -> new SaplingBase(
                () -> new Ultra_Tree05(), AbstractBlock.Properties.of(Material.PLANT, MaterialColor.SAND)
                .strength(0f, 1f).sound(SoundType.GRASS).noCollission().noOcclusion()));

        // Woods (LOGS/LEAVES/PLANKS)
        // Inverted Blocks
        INVERTED_LEAVES = PokecubeLegends.BLOCKS_TAB.register("ultra_leave01", () -> new LeavesBlock(AbstractBlock.Properties.of(
                Material.LEAVES, MaterialColor.COLOR_LIGHT_BLUE).strength(1f, 5).sound(SoundType.GRASS).noOcclusion()));
        INVERTED_LOG = PokecubeLegends.BLOCKS_TAB.register("ultra_log01", () -> Blocks.log(
                MaterialColor.TERRACOTTA_LIGHT_BLUE, MaterialColor.TERRACOTTA_LIGHT_BLUE));
        INVERTED_WOOD = PokecubeLegends.BLOCKS_TAB.register("inverted_wood", () -> Blocks.log(
                MaterialColor.TERRACOTTA_LIGHT_BLUE, MaterialColor.TERRACOTTA_LIGHT_BLUE));
        STRIP_INVERTED_LOG = PokecubeLegends.BLOCKS_TAB.register("stripped_inverted_log", () -> Blocks.log(
                MaterialColor.TERRACOTTA_LIGHT_BLUE, MaterialColor.TERRACOTTA_LIGHT_BLUE));
        STRIP_INVERTED_WOOD = PokecubeLegends.BLOCKS_TAB.register("stripped_inverted_wood", () -> Blocks.log(
                MaterialColor.TERRACOTTA_LIGHT_BLUE, MaterialColor.TERRACOTTA_LIGHT_BLUE));
        INVERTED_PLANKS = PokecubeLegends.BLOCKS_TAB.register("ultra_plank01", () -> new Block(AbstractBlock.Properties.of(
        		Material.WOOD, MaterialColor.TERRACOTTA_LIGHT_BLUE).strength(2.0F, 3.0f).sound(SoundType.WOOD)));
        INVERTED_STAIRS = PokecubeLegends.BLOCKS_TAB.register("inverted_stairs",
                () -> new ItemGenerator.GenericStairs(Blocks.OAK_STAIRS.defaultBlockState(), AbstractBlock.Properties.of(
                		Material.WOOD, MaterialColor.TERRACOTTA_LIGHT_BLUE).strength(2.0F, 3.0f).sound(SoundType.WOOD)));
        INVERTED_SLAB = PokecubeLegends.BLOCKS_TAB.register("inverted_slab", () -> new SlabBlock(AbstractBlock.Properties.of(
        		Material.WOOD, MaterialColor.TERRACOTTA_LIGHT_BLUE).strength(2.0F, 3.0f).sound(SoundType.WOOD)));
        INVERTED_FENCE = PokecubeLegends.BLOCKS_TAB.register("inverted_fence", () -> new FenceBlock(AbstractBlock.Properties.of(
        		Material.WOOD, MaterialColor.TERRACOTTA_LIGHT_BLUE).strength(2.0F, 3.0f).sound(SoundType.WOOD)));
        INVERTED_FENCE_GATE = PokecubeLegends.BLOCKS_TAB.register("inverted_fence_gate", () -> new FenceGateBlock(AbstractBlock.Properties.of(
        		Material.WOOD, MaterialColor.TERRACOTTA_LIGHT_BLUE).strength(2.0F, 3.0f).sound(SoundType.WOOD)));
        INVERTED_PR_PLATE = PokecubeLegends.BLOCKS_TAB.register("inverted_pressure_plate",
                () -> new ItemGenerator.GenericPressurePlate(PressurePlateBlock.Sensitivity.EVERYTHING, AbstractBlock.Properties
                        .of(Material.WOOD, MaterialColor.TERRACOTTA_LIGHT_BLUE).sound(SoundType.WOOD).noCollission().strength(
                                0.5F)));
        INVERTED_BUTTON = PokecubeLegends.BLOCKS_TAB.register("inverted_button",
                () -> new ItemGenerator.GenericButton(AbstractBlock.Properties.of(Material.WOOD, MaterialColor.TERRACOTTA_LIGHT_BLUE).sound(SoundType.WOOD)
                        .noCollission().strength(0.5F)));
        INVERTED_TRAPDOOR = PokecubeLegends.BLOCKS_TAB.register("inverted_trapdoor",
                () -> new ItemGenerator.GenericTrapDoor(AbstractBlock.Properties.of(Material.WOOD, MaterialColor.TERRACOTTA_LIGHT_BLUE)
                        .sound(SoundType.WOOD).strength(2.0f, 3.0f).noOcclusion()));
        INVERTED_DOOR = PokecubeLegends.BLOCKS_TAB.register("inverted_door", () -> new ItemGenerator.GenericDoor(
                AbstractBlock.Properties.of(Material.WOOD, MaterialColor.TERRACOTTA_LIGHT_BLUE).sound(SoundType.WOOD).strength(
                        2.0f, 3.0f).noOcclusion()));

        // Temporal Blocks
        TEMPORAL_LEAVES = PokecubeLegends.BLOCKS_TAB.register("ultra_leave02", () -> new LeavesBlock(AbstractBlock.Properties.of(
                Material.LEAVES, MaterialColor.PLANT).strength(1f, 5).sound(SoundType.GRASS).noOcclusion()));
        TEMPORAL_LOG = PokecubeLegends.BLOCKS_TAB.register("ultra_log02", () -> Blocks.log(
                MaterialColor.COLOR_GREEN, MaterialColor.COLOR_YELLOW));
        TEMPORAL_WOOD = PokecubeLegends.BLOCKS_TAB.register("temporal_wood", () -> Blocks.log(
                MaterialColor.COLOR_GREEN, MaterialColor.COLOR_YELLOW));
        STRIP_TEMPORAL_LOG = PokecubeLegends.BLOCKS_TAB.register("stripped_temporal_log", () -> Blocks.log(
                MaterialColor.COLOR_LIGHT_GREEN, MaterialColor.COLOR_LIGHT_GREEN));
        STRIP_TEMPORAL_WOOD = PokecubeLegends.BLOCKS_TAB.register("stripped_temporal_wood", () -> Blocks.log(
                MaterialColor.COLOR_LIGHT_GREEN, MaterialColor.COLOR_LIGHT_GREEN));
        TEMPORAL_PLANKS = PokecubeLegends.BLOCKS_TAB.register("ultra_plank02", () -> new Block(AbstractBlock.Properties.of(
        		Material.WOOD, MaterialColor.COLOR_LIGHT_GREEN).strength(2.0F).sound(SoundType.WOOD)));
        TEMPORAL_STAIRS = PokecubeLegends.BLOCKS_TAB.register("temporal_stairs",
                () -> new ItemGenerator.GenericStairs(Blocks.OAK_STAIRS.defaultBlockState(), AbstractBlock.Properties.of(
                		Material.WOOD, MaterialColor.COLOR_LIGHT_GREEN).strength(2.0F).sound(SoundType.WOOD)));
        TEMPORAL_SLAB = PokecubeLegends.BLOCKS_TAB.register("temporal_slab", () -> new SlabBlock(AbstractBlock.Properties.of(
        		Material.WOOD, MaterialColor.COLOR_LIGHT_GREEN).strength(2.0F).sound(SoundType.WOOD)));
        TEMPORAL_FENCE = PokecubeLegends.BLOCKS_TAB.register("temporal_fence", () -> new FenceBlock(AbstractBlock.Properties.of(
        		Material.WOOD, MaterialColor.COLOR_LIGHT_GREEN).strength(2.0F).sound(SoundType.WOOD)));
        TEMPORAL_FENCE_GATE = PokecubeLegends.BLOCKS_TAB.register("temporal_fence_gate", () -> new FenceGateBlock(AbstractBlock.Properties.of(
        		Material.WOOD, MaterialColor.COLOR_LIGHT_GREEN).strength(2.0F).sound(SoundType.WOOD)));
        TEMPORAL_PR_PLATE = PokecubeLegends.BLOCKS_TAB.register("temporal_pressure_plate",
                () -> new ItemGenerator.GenericPressurePlate(PressurePlateBlock.Sensitivity.EVERYTHING, AbstractBlock.Properties
                        .of(Material.WOOD, MaterialColor.COLOR_LIGHT_GREEN).sound(SoundType.WOOD).noCollission().strength(
                                0.5F)));
        TEMPORAL_BUTTON = PokecubeLegends.BLOCKS_TAB.register("temporal_button",
                () -> new ItemGenerator.GenericButton(AbstractBlock.Properties.of(Material.WOOD, MaterialColor.COLOR_LIGHT_GREEN).sound(SoundType.WOOD)
                        .noCollission().strength(0.5F)));
        TEMPORAL_TRAPDOOR = PokecubeLegends.BLOCKS_TAB.register("temporal_trapdoor",
                () -> new ItemGenerator.GenericTrapDoor(AbstractBlock.Properties.of(Material.WOOD, MaterialColor.COLOR_LIGHT_GREEN)
                        .sound(SoundType.WOOD).strength(2.0f, 3.0f).noOcclusion()));
        TEMPORAL_DOOR = PokecubeLegends.BLOCKS_TAB.register("temporal_door", () -> new ItemGenerator.GenericDoor(
                AbstractBlock.Properties.of(Material.WOOD, MaterialColor.COLOR_LIGHT_GREEN).sound(SoundType.WOOD).strength(
                        2.0f, 3.0f).noOcclusion()));

        // Aged Blocks
        AGED_LEAVES = PokecubeLegends.BLOCKS_TAB.register("ultra_leave03", () -> new LeavesBlock(AbstractBlock.Properties.of(
                Material.LEAVES, MaterialColor.COLOR_YELLOW).strength(1f, 5).sound(SoundType.GRASS).noOcclusion()));
        AGED_LOG = PokecubeLegends.BLOCKS_TAB.register("ultra_log03", () -> Blocks.log(
                MaterialColor.COLOR_BROWN, MaterialColor.COLOR_BROWN));
        AGED_WOOD = PokecubeLegends.BLOCKS_TAB.register("aged_wood", () -> Blocks.log(MaterialColor.COLOR_BROWN,
                MaterialColor.COLOR_BROWN));
        STRIP_AGED_LOG = PokecubeLegends.BLOCKS_TAB.register("stripped_aged_log", () -> Blocks.log(
                MaterialColor.COLOR_BROWN, MaterialColor.COLOR_BROWN));
        STRIP_AGED_WOOD = PokecubeLegends.BLOCKS_TAB.register("stripped_aged_wood", () -> Blocks.log(
                MaterialColor.COLOR_BROWN, MaterialColor.COLOR_BROWN));
        AGED_PLANKS = PokecubeLegends.BLOCKS_TAB.register("ultra_plank03", () -> new Block(AbstractBlock.Properties.of(
        		Material.WOOD, MaterialColor.COLOR_BROWN).strength(2.0F).sound(SoundType.WOOD)));
        AGED_STAIRS = PokecubeLegends.BLOCKS_TAB.register("aged_stairs", () -> new ItemGenerator.GenericStairs(
                Blocks.OAK_STAIRS.defaultBlockState(), AbstractBlock.Properties.of(
                		Material.WOOD, MaterialColor.COLOR_BROWN).strength(2.0F).sound(SoundType.WOOD)));
        AGED_SLAB = PokecubeLegends.BLOCKS_TAB.register("aged_slab", () -> new SlabBlock(AbstractBlock.Properties.of(
        		Material.WOOD, MaterialColor.COLOR_BROWN).strength(2.0F).sound(SoundType.WOOD)));
        AGED_FENCE = PokecubeLegends.BLOCKS_TAB.register("aged_fence", () -> new FenceBlock(AbstractBlock.Properties.of(
        		Material.WOOD, MaterialColor.COLOR_BROWN).strength(2.0F).sound(SoundType.WOOD)));
        AGED_FENCE_GATE = PokecubeLegends.BLOCKS_TAB.register("aged_fence_gate", () -> new FenceGateBlock(AbstractBlock.Properties.of(
        		Material.WOOD, MaterialColor.COLOR_BROWN).strength(2.0F).sound(SoundType.WOOD)));
        AGED_PR_PLATE = PokecubeLegends.BLOCKS_TAB.register("aged_pressure_plate",
                () -> new ItemGenerator.GenericPressurePlate(PressurePlateBlock.Sensitivity.EVERYTHING, AbstractBlock.Properties
                        .of(Material.WOOD, MaterialColor.COLOR_BROWN).sound(SoundType.WOOD).noCollission().strength(
                                0.5F)));
        AGED_BUTTON = PokecubeLegends.BLOCKS_TAB.register("aged_button", () -> new ItemGenerator.GenericButton(
                AbstractBlock.Properties.of(Material.WOOD, MaterialColor.COLOR_BROWN).sound(SoundType.WOOD).noCollission()
                        .strength(0.5F)));
        AGED_TRAPDOOR = PokecubeLegends.BLOCKS_TAB.register("aged_trapdoor", () -> new ItemGenerator.GenericTrapDoor(
                AbstractBlock.Properties.of(Material.WOOD, MaterialColor.COLOR_BROWN).sound(SoundType.WOOD).strength(
                        2.0f, 3.0f).noOcclusion()));
        AGED_DOOR = PokecubeLegends.BLOCKS_TAB.register("aged_door", () -> new ItemGenerator.GenericDoor(
                AbstractBlock.Properties.of(Material.WOOD, MaterialColor.COLOR_BROWN).sound(SoundType.WOOD).strength(
                        2.0f, 3.0f).noOcclusion()));

        // Distortic Blocks
        DISTORTIC_LEAVES = PokecubeLegends.BLOCKS_TAB.register("distortic_leave", () -> new LeavesBlock(AbstractBlock.Properties.of(
                Material.LEAVES, MaterialColor.COLOR_PURPLE).strength(1f, 5).sound(SoundType.GRASS).noOcclusion()));
        DISTORTIC_LOG = PokecubeLegends.BLOCKS_TAB.register("distortic_log", () -> Blocks.log(
                MaterialColor.COLOR_BLUE, MaterialColor.COLOR_BLUE));
        DISTORTIC_WOOD = PokecubeLegends.BLOCKS_TAB.register("distortic_wood", () -> Blocks.log(
                MaterialColor.COLOR_BLUE, MaterialColor.COLOR_BLUE));
        STRIP_DISTORTIC_LOG = PokecubeLegends.BLOCKS_TAB.register("stripped_distortic_log", () -> Blocks.log(
                MaterialColor.COLOR_BLUE, MaterialColor.COLOR_BLUE));
        STRIP_DISTORTIC_WOOD = PokecubeLegends.BLOCKS_TAB.register("stripped_distortic_wood", () -> Blocks
                .log(MaterialColor.COLOR_BLUE, MaterialColor.COLOR_BLUE));
        DISTORTIC_PLANKS = PokecubeLegends.BLOCKS_TAB.register("distortic_plank", () -> new Block(AbstractBlock.Properties.of(
        		Material.WOOD, MaterialColor.COLOR_BLUE).strength(2.0F).sound(SoundType.WOOD)));
        DISTORTIC_STAIRS = PokecubeLegends.BLOCKS_TAB.register("distortic_stairs",
                () -> new ItemGenerator.GenericStairs(Blocks.OAK_STAIRS.defaultBlockState(), AbstractBlock.Properties.of(
                		Material.WOOD, MaterialColor.COLOR_BLUE).strength(2.0F).sound(SoundType.WOOD)));
        DISTORTIC_SLAB = PokecubeLegends.BLOCKS_TAB.register("distortic_slab", () -> new SlabBlock(AbstractBlock.Properties.of(
        		Material.WOOD, MaterialColor.COLOR_BLUE).strength(2.0F).sound(SoundType.WOOD)));
        DISTORTIC_FENCE = PokecubeLegends.BLOCKS_TAB.register("distortic_fence", () -> new FenceBlock(AbstractBlock.Properties.of(
        		Material.WOOD, MaterialColor.COLOR_BLUE).strength(2.0F).sound(SoundType.WOOD)));
        DISTORTIC_FENCE_GATE = PokecubeLegends.BLOCKS_TAB.register("distortic_fence_gate", () -> new FenceGateBlock(AbstractBlock.Properties.of(
        		Material.WOOD, MaterialColor.COLOR_BLUE).strength(2.0F).sound(SoundType.WOOD)));
        DISTORTIC_PR_PLATE = PokecubeLegends.BLOCKS_TAB.register("distortic_pressure_plate",
                () -> new ItemGenerator.GenericPressurePlate(PressurePlateBlock.Sensitivity.EVERYTHING, AbstractBlock.Properties
                        .of(Material.WOOD, MaterialColor.COLOR_BLUE).sound(SoundType.WOOD).noCollission().strength(
                                0.5F)));
        DISTORTIC_BUTTON = PokecubeLegends.BLOCKS_TAB.register("distortic_button",
                () -> new ItemGenerator.GenericButton(AbstractBlock.Properties.of(Material.WOOD, MaterialColor.COLOR_BLUE).sound(SoundType.WOOD)
                        .noCollission().strength(0.5F)));
        DISTORTIC_TRAPDOOR = PokecubeLegends.BLOCKS_TAB.register("distortic_trapdoor",
                () -> new ItemGenerator.GenericTrapDoor(AbstractBlock.Properties.of(Material.WOOD, MaterialColor.COLOR_BLUE)
                        .sound(SoundType.WOOD).strength(2.0f, 3.0f).noOcclusion()));
        DISTORTIC_DOOR = PokecubeLegends.BLOCKS_TAB.register("distortic_door", () -> new ItemGenerator.GenericDoor(
                AbstractBlock.Properties.of(Material.WOOD, MaterialColor.COLOR_BLUE).sound(SoundType.WOOD).strength(
                        2.0f, 3.0f).noOcclusion()));
        
        // Corrupted Blocks
        CORRUPTED_LEAVES = PokecubeLegends.BLOCKS_TAB.register("corrupted_leave", () -> new CorruptedLeaveBlock());
        CORRUPTED_LOG = PokecubeLegends.BLOCKS_TAB.register("corrupted_log", () -> Blocks.log(
                MaterialColor.COLOR_BROWN, MaterialColor.COLOR_BLACK));
        CORRUPTED_WOOD = PokecubeLegends.BLOCKS_TAB.register("corrupted_wood", () -> Blocks.log(
                MaterialColor.COLOR_BLACK, MaterialColor.COLOR_BLACK));
        STRIP_CORRUPTED_LOG = PokecubeLegends.BLOCKS_TAB.register("stripped_corrupted_log", () -> Blocks.log(
                MaterialColor.COLOR_BROWN, MaterialColor.COLOR_BROWN));
        STRIP_CORRUPTED_WOOD = PokecubeLegends.BLOCKS_TAB.register("stripped_corrupted_wood", () -> Blocks
                .log(MaterialColor.COLOR_BROWN, MaterialColor.COLOR_BROWN));
        CORRUPTED_PLANKS = PokecubeLegends.BLOCKS_TAB.register("corrupted_plank", () -> new Block(AbstractBlock.Properties.of(
        		Material.WOOD, MaterialColor.COLOR_BROWN).strength(2.0F).sound(SoundType.WOOD)));
        CORRUPTED_STAIRS = PokecubeLegends.BLOCKS_TAB.register("corrupted_stairs",
                () -> new ItemGenerator.GenericStairs(Blocks.OAK_STAIRS.defaultBlockState(), AbstractBlock.Properties.of(
                		Material.WOOD, MaterialColor.COLOR_BROWN).strength(2.0F).sound(SoundType.WOOD)));
        CORRUPTED_SLAB = PokecubeLegends.BLOCKS_TAB.register("corrupted_slab", () -> new SlabBlock(AbstractBlock.Properties.of(
        		Material.WOOD, MaterialColor.COLOR_BROWN).strength(2.0F).sound(SoundType.WOOD)));
        CORRUPTED_FENCE = PokecubeLegends.BLOCKS_TAB.register("corrupted_fence", () -> new FenceBlock(AbstractBlock.Properties.of(
        		Material.WOOD, MaterialColor.COLOR_BROWN).strength(2.0F).sound(SoundType.WOOD)));
        CORRUPTED_FENCE_GATE = PokecubeLegends.BLOCKS_TAB.register("corrupted_fence_gate", () -> new FenceGateBlock(AbstractBlock.Properties.of(
        		Material.WOOD, MaterialColor.COLOR_BROWN).strength(2.0F).sound(SoundType.WOOD)));
        CORRUPTED_PR_PLATE = PokecubeLegends.BLOCKS_TAB.register("corrupted_pressure_plate",
                () -> new ItemGenerator.GenericPressurePlate(PressurePlateBlock.Sensitivity.MOBS, AbstractBlock.Properties
                        .of(Material.WOOD, MaterialColor.COLOR_BROWN).sound(SoundType.WOOD).noCollission().strength(
                                0.5F)));
        CORRUPTED_BUTTON = PokecubeLegends.BLOCKS_TAB.register("corrupted_button",
                () -> new ItemGenerator.GenericButton(AbstractBlock.Properties.of(Material.WOOD, MaterialColor.COLOR_BROWN).sound(SoundType.WOOD)
                        .noCollission().strength(0.5F)));
        CORRUPTED_TRAPDOOR = PokecubeLegends.BLOCKS_TAB.register("corrupted_trapdoor",
                () -> new ItemGenerator.GenericTrapDoor(AbstractBlock.Properties.of(Material.WOOD, MaterialColor.COLOR_BROWN)
                        .sound(SoundType.WOOD).strength(2.0f, 3.0f).noOcclusion()));
        CORRUPTED_DOOR = PokecubeLegends.BLOCKS_TAB.register("corrupted_door", () -> new ItemGenerator.GenericDoor(
                AbstractBlock.Properties.of(Material.WOOD, MaterialColor.COLOR_BROWN).sound(SoundType.WOOD).strength(
                        2.0f, 3.0f).noOcclusion()));
        
        // MIRAGE Blocks
        MIRAGE_GLASS = PokecubeLegends.BLOCKS_TAB.register("mirage_glass", () -> new UltraGlass("mirage_glass",
                DyeColor.LIGHT_BLUE, AbstractBlock.Properties.of(Material.GLASS, MaterialColor.COLOR_LIGHT_BLUE).sound(SoundType.GLASS)
                .strength(0.3F).noOcclusion()));
        MIRAGE_LEAVES = PokecubeLegends.BLOCKS_TAB.register("mirage_leave", () -> new MirageLeaveBlock());
        MIRAGE_LOG = PokecubeLegends.BLOCKS_TAB.register("mirage_log", () -> Blocks.log(
                MaterialColor.TERRACOTTA_YELLOW, MaterialColor.SNOW));
        MIRAGE_WOOD = PokecubeLegends.BLOCKS_TAB.register("mirage_wood", () -> Blocks.log(
                MaterialColor.SNOW, MaterialColor.SNOW));
        STRIP_MIRAGE_LOG = PokecubeLegends.BLOCKS_TAB.register("stripped_mirage_log", () -> Blocks.log(
                MaterialColor.TERRACOTTA_YELLOW, MaterialColor.COLOR_PURPLE));
        STRIP_MIRAGE_WOOD = PokecubeLegends.BLOCKS_TAB.register("stripped_mirage_wood", () -> Blocks
                .log(MaterialColor.COLOR_PURPLE, MaterialColor.COLOR_PURPLE));
        MIRAGE_PLANKS = PokecubeLegends.BLOCKS_TAB.register("mirage_plank", () -> new Block(AbstractBlock.Properties.of(
        		Material.WOOD, MaterialColor.TERRACOTTA_WHITE).strength(2.0F).sound(SoundType.WOOD)));
        MIRAGE_STAIRS = PokecubeLegends.BLOCKS_TAB.register("mirage_stairs",
                () -> new ItemGenerator.GenericStairs(Blocks.OAK_STAIRS.defaultBlockState(), AbstractBlock.Properties.of(
                		Material.WOOD, MaterialColor.TERRACOTTA_WHITE).strength(2.0F).sound(SoundType.WOOD)));
        MIRAGE_SLAB = PokecubeLegends.BLOCKS_TAB.register("mirage_slab", () -> new SlabBlock(AbstractBlock.Properties.of(
        		Material.WOOD, MaterialColor.TERRACOTTA_WHITE).strength(2.0F).sound(SoundType.WOOD)));
        MIRAGE_FENCE = PokecubeLegends.BLOCKS_TAB.register("mirage_fence", () -> new FenceBlock(AbstractBlock.Properties.of(
        		Material.WOOD, MaterialColor.TERRACOTTA_WHITE).strength(2.0F).sound(SoundType.WOOD)));
        MIRAGE_FENCE_GATE = PokecubeLegends.BLOCKS_TAB.register("mirage_fence_gate", () -> new FenceGateBlock(AbstractBlock.Properties.of(
        		Material.WOOD, MaterialColor.TERRACOTTA_WHITE).strength(2.0F).sound(SoundType.WOOD)));
        MIRAGE_PR_PLATE = PokecubeLegends.BLOCKS_TAB.register("mirage_pressure_plate",
                () -> new ItemGenerator.GenericPressurePlate(PressurePlateBlock.Sensitivity.MOBS, AbstractBlock.Properties
                        .of(Material.WOOD, MaterialColor.TERRACOTTA_WHITE).sound(SoundType.WOOD).noCollission().strength(
                                0.5F)));
        MIRAGE_BUTTON = PokecubeLegends.BLOCKS_TAB.register("mirage_button",
                () -> new ItemGenerator.GenericButton(AbstractBlock.Properties.of(Material.WOOD, MaterialColor.TERRACOTTA_WHITE)
                        .sound(SoundType.WOOD).noCollission().strength(0.5F)));
        MIRAGE_TRAPDOOR = PokecubeLegends.BLOCKS_TAB.register("mirage_trapdoor",
                () -> new ItemGenerator.GenericTrapDoor(AbstractBlock.Properties.of(Material.WOOD, MaterialColor.TERRACOTTA_WHITE)
                        .sound(SoundType.WOOD).strength(2.0f, 3.0f).noOcclusion()));
        MIRAGE_DOOR = PokecubeLegends.BLOCKS_TAB.register("mirage_door", () -> new ItemGenerator.GenericDoor(
                AbstractBlock.Properties.of(Material.WOOD, MaterialColor.TERRACOTTA_WHITE).sound(SoundType.WOOD).strength(
                        2.0f, 3.0f).noOcclusion()));
        
        // Mirage Spot (Hoopa Ring)
        BLOCK_PORTALWARP = PokecubeLegends.BLOCKS.register("portal", () -> new PortalWarp("portal", AbstractBlock.Properties
                .of(Material.STONE, MaterialColor.GOLD).sound(SoundType.METAL).strength(2000, 2000)).setShape(VoxelShapes
                .box(0.05, 0, 0.05, 1, 3, 1)).setInfoBlockName("portalwarp"));

        // Legendary Spawns
        GOLEM_STONE = PokecubeLegends.BLOCKS.register("golem_stone", () -> new BlockBase("golem_stone", Material.STONE, 
        		MaterialColor.TERRACOTTA_WHITE, 3f, SoundType.STONE, ToolType.PICKAXE, 2).noInfoBlock());

        LEGENDARY_SPAWN = PokecubeLegends.BLOCKS.register("legendaryspawn", () -> new LegendaryBlock("legendaryspawn",
                Material.METAL, MaterialColor.GOLD).noInfoBlock());
        TROUGH_BLOCK = PokecubeLegends.BLOCKS.register("trough_block", () -> new TroughBlock("trough_block",
                AbstractBlock.Properties.of(Material.METAL, MaterialColor.COLOR_BROWN).strength(5, 15).harvestTool(ToolType.PICKAXE)
                .harvestLevel(2).sound(SoundType.ANVIL).lightLevel(b -> 4).dynamicShape()).noInfoBlock());
        HEATRAN_BLOCK 	= PokecubeLegends.BLOCKS.register("heatran_block", () -> new HeatranBlock("heatran_block",
        		AbstractBlock.Properties.of(Material.STONE, MaterialColor.NETHER).strength(5, 15).harvestTool(ToolType.PICKAXE)
                .harvestLevel(2).sound(SoundType.NETHER_BRICKS).lightLevel(b -> 4).dynamicShape().emissiveRendering((s, r, p) -> true)).noInfoBlock());
        TAO_BLOCK 	= PokecubeLegends.BLOCKS.register("blackwhite_block", () -> new TaoTrioBlock("blackwhite_block",
        		AbstractBlock.Properties.of(Material.HEAVY_METAL, MaterialColor.SNOW).strength(5, 15).sound(SoundType.FUNGUS)
        		.dynamicShape()).setShape(VoxelShapes.box(0.05, 0, 0.05, 1, 1, 1)).noInfoBlock());
        
        // Regi Cores
        REGISTEEL_CORE = PokecubeLegends.BLOCKS.register("registeel_spawn", () -> new Registeel_Core("registeel_spawn",
                Material.METAL, MaterialColor.TERRACOTTA_WHITE, 15, SoundType.METAL, ToolType.PICKAXE, 2).noInfoBlock());
        REGICE_CORE = PokecubeLegends.BLOCKS.register("regice_spawn", () -> new Regice_Core("regice_spawn",
                Material.ICE_SOLID, MaterialColor.TERRACOTTA_WHITE, 15, SoundType.STONE, ToolType.PICKAXE, 2).noInfoBlock());
        REGIROCK_CORE = PokecubeLegends.BLOCKS.register("regirock_spawn", () -> new Regirock_Core("regirock_spawn",
                Material.STONE, MaterialColor.TERRACOTTA_WHITE, 15, SoundType.STONE, ToolType.PICKAXE, 2).noInfoBlock());
        REGIELEKI_CORE = PokecubeLegends.BLOCKS.register("regieleki_spawn", () -> new Regieleki_Core("regieleki_spawn",
                Material.STONE, MaterialColor.TERRACOTTA_WHITE, 15, SoundType.STONE, ToolType.PICKAXE, 2).noInfoBlock());
        REGIDRAGO_CORE = PokecubeLegends.BLOCKS.register("regidrago_spawn", () -> new Regidrago_Core("regidrago_spawn",
                Material.STONE, MaterialColor.TERRACOTTA_WHITE, 15, SoundType.STONE, ToolType.PICKAXE, 2).noInfoBlock());
        REGIGIGA_CORE = PokecubeLegends.BLOCKS.register("regigiga_spawn", () -> new Regigigas_Core("regigiga_spawn",
                Material.METAL, MaterialColor.TERRACOTTA_WHITE, 15, SoundType.METAL, ToolType.PICKAXE, 2).noInfoBlock());
        
        // Tapus
        TAPU_KOKO_CORE 	= PokecubeLegends.BLOCKS.register("koko_core", () -> new TapuKokoCore("koko_core",
        		AbstractBlock.Properties.of(Material.STONE, MaterialColor.TERRACOTTA_YELLOW).strength(5, 15)
        		.sound(SoundType.BASALT).dynamicShape()).noInfoBlock());
        TAPU_BULU_CORE 	= PokecubeLegends.BLOCKS.register("bulu_core", () -> new TapuBuluCore("bulu_core",
        		AbstractBlock.Properties.of(Material.STONE, MaterialColor.TERRACOTTA_RED).strength(5, 15)
        		.sound(SoundType.BASALT).dynamicShape()).noInfoBlock());
        TAPU_LELE_CORE 	= PokecubeLegends.BLOCKS.register("lele_core", () -> new TapuLeleCore("lele_core",
        		AbstractBlock.Properties.of(Material.STONE, MaterialColor.TERRACOTTA_PURPLE).strength(5, 15)
        		.sound(SoundType.BASALT).dynamicShape()).noInfoBlock());
        TAPU_FINI_CORE 	= PokecubeLegends.BLOCKS.register("fini_core", () -> new TapuFiniCore("fini_core",
        		AbstractBlock.Properties.of(Material.STONE, MaterialColor.TERRACOTTA_PINK).strength(5, 15)
        		.sound(SoundType.BASALT).dynamicShape()).noInfoBlock());
        
        TIMESPACE_CORE = PokecubeLegends.BLOCKS.register("timerspawn", () -> new TimeSpaceCoreBlock("timerspawn",
                AbstractBlock.Properties.of(Material.GRASS, MaterialColor.STONE).strength(2000, 2000).sound(SoundType.STONE)
                .dynamicShape()).setShape(VoxelShapes.box(0.05, 0, 0.05, 1, 2, 1)).noInfoBlock());
        NATURE_CORE = PokecubeLegends.BLOCKS.register("naturespawn", () -> new NatureCoreBlock("naturespawn",
                AbstractBlock.Properties.of(Material.STONE, MaterialColor.TERRACOTTA_WHITE).strength(2000, 2000).sound(SoundType.STONE)
                .dynamicShape()).setShape(VoxelShapes.box(0.05, 0, 0.05, 1, 2, 1)).noInfoBlock());
        KELDEO_CORE = PokecubeLegends.BLOCKS.register("keldeoblock", () -> new KeldeoBlock("keldeoblock",
                AbstractBlock.Properties.of(Material.STONE, MaterialColor.COLOR_BLUE).strength(2000, 2000).sound(SoundType.STONE)
                .dynamicShape()).setShape(VoxelShapes.box(0.05, 0, 0.05, 1, 1, 1)).noInfoBlock());
        VICTINI_CORE = PokecubeLegends.BLOCKS.register("victiniblock", () -> new VictiniBlock("victiniblock",
                AbstractBlock.Properties.of(Material.METAL, MaterialColor.GOLD).strength(5, 15).harvestTool(ToolType.PICKAXE)
                .harvestLevel(2).sound(SoundType.ANVIL).dynamicShape())
                .setShape(VoxelShapes.box(0.05, 0, 0.05, 1, 1, 1))
                .noInfoBlock());
        YVELTAL_CORE = PokecubeLegends.BLOCKS.register("yveltal_egg", () -> new YveltalEgg("yveltal_egg",
                AbstractBlock.Properties.of(Material.METAL, MaterialColor.COLOR_BLACK).strength(2000, 2000).sound(SoundType.WOOD)
                .dynamicShape()).setShape(VoxelShapes.box(0.05, 0, 0.05, 1, 2, 1)).noInfoBlock());
        XERNEAS_CORE = PokecubeLegends.BLOCKS.register("xerneas_tree", () -> new XerneasCore("xerneas_tree",
                AbstractBlock.Properties.of(Material.METAL, MaterialColor.SNOW).strength(2000, 2000).sound(SoundType.WOOD)
                .dynamicShape()).setShape(VoxelShapes.box(0.05, 0, 0.05, 1, 2, 1)).noInfoBlock());

        // Ores
        RUBY_ORE = PokecubeLegends.DECORATION_TAB.register("ruby_ore", () -> new BlockBase("ruby_ore", AbstractBlock.Properties.of(
                Material.STONE, MaterialColor.STONE).sound(SoundType.STONE).strength(5, 15)
                .harvestTool(ToolType.PICKAXE).harvestLevel(2).requiresCorrectToolForDrops()).noInfoBlock());
        SAPPHIRE_ORE = PokecubeLegends.DECORATION_TAB.register("sapphire_ore", () -> new BlockBase("sapphire_ore",
                AbstractBlock.Properties.of(Material.STONE, MaterialColor.STONE).sound(SoundType.STONE)
                .strength(5, 15).harvestTool(ToolType.PICKAXE).harvestLevel(2).requiresCorrectToolForDrops())
                .noInfoBlock());
        SPECTRUM_ORE = PokecubeLegends.BLOCKS_TAB.register("spectrum_ore", () -> new BlockBase("spectrum_ore",
                AbstractBlock.Properties.of(Material.STONE, MaterialColor.TERRACOTTA_CYAN).sound(SoundType.STONE)
                .strength(5, 15).harvestTool(ToolType.PICKAXE).harvestLevel(2).requiresCorrectToolForDrops())
                .noInfoBlock());
        
        RUBY_BLOCK = PokecubeLegends.DECORATION_TAB.register("ruby_block", () -> new Block(AbstractBlock.Properties.of(
                Material.METAL, MaterialColor.COLOR_RED).strength(5.0F, 6.0F).sound(SoundType.METAL)
                .harvestTool(ToolType.PICKAXE).requiresCorrectToolForDrops()));
        RUBY_SLAB = PokecubeLegends.DECORATION_TAB.register("ruby_slab", () -> new SlabBlock(AbstractBlock.Properties.of(
        		Material.METAL, MaterialColor.COLOR_RED).strength(5.0F, 6.0F).sound(SoundType.METAL).lightLevel(b -> 4).harvestTool(
                ToolType.PICKAXE).requiresCorrectToolForDrops()));
        RUBY_STAIRS = PokecubeLegends.DECORATION_TAB.register("ruby_stairs",
                () -> new ItemGenerator.GenericStairs(Blocks.STONE_STAIRS.defaultBlockState(), AbstractBlock.Properties.of(
                Material.METAL, MaterialColor.COLOR_RED).strength(5.0F, 6.0F).sound(SoundType.METAL)
                .lightLevel(b -> 4).harvestTool(ToolType.PICKAXE).requiresCorrectToolForDrops()));
        
        SAPPHIRE_BLOCK = PokecubeLegends.DECORATION_TAB.register("sapphire_block", () -> new Block(AbstractBlock.Properties.of(
                Material.METAL, MaterialColor.COLOR_BLUE).strength(5.0F, 6.0F).sound(SoundType.METAL)
                .harvestTool(ToolType.PICKAXE).requiresCorrectToolForDrops()));
        SAPPHIRE_SLAB = PokecubeLegends.DECORATION_TAB.register("sapphire_slab", () -> new SlabBlock(AbstractBlock.Properties.of(
        		Material.METAL, MaterialColor.COLOR_BLUE).strength(5.0F, 6.0F).sound(SoundType.METAL)
                .lightLevel(b -> 4).harvestTool(ToolType.PICKAXE).requiresCorrectToolForDrops()));
        SAPPHIRE_STAIRS = PokecubeLegends.DECORATION_TAB.register("sapphire_stairs",
                () -> new ItemGenerator.GenericStairs(Blocks.STONE_STAIRS.defaultBlockState(), AbstractBlock.Properties.of(
                Material.METAL, MaterialColor.COLOR_BLUE).strength(5.0F, 6.0F).sound(SoundType.METAL)
                .lightLevel(b -> 4).harvestTool(ToolType.PICKAXE).requiresCorrectToolForDrops()));
        
        SPECTRUM_BLOCK = PokecubeLegends.DECORATION_TAB.register("spectrum_block", () -> new Block(AbstractBlock.Properties.of(
                Material.METAL, MaterialColor.COLOR_ORANGE).strength(5.0F, 6.0F).sound(SoundType.METAL).lightLevel(b -> 4).harvestTool(
                ToolType.PICKAXE).requiresCorrectToolForDrops()));
        SPECTRUM_SLAB = PokecubeLegends.DECORATION_TAB.register("spectrum_slab", () -> new SlabBlock(AbstractBlock.Properties.of(
        		Material.METAL, MaterialColor.COLOR_ORANGE).strength(5.0F, 6.0F).sound(SoundType.METAL).lightLevel(b -> 4).harvestTool(
                ToolType.PICKAXE).requiresCorrectToolForDrops()));
        SPECTRUM_STAIRS = PokecubeLegends.DECORATION_TAB.register("spectrum_stairs",
                () -> new ItemGenerator.GenericStairs(Blocks.STONE_STAIRS.defaultBlockState(), AbstractBlock.Properties.of(
                Material.METAL, MaterialColor.COLOR_ORANGE).strength(5.0F, 6.0F).sound(SoundType.METAL).lightLevel(b -> 4).harvestTool(
                ToolType.PICKAXE).requiresCorrectToolForDrops()));

        // Meteor Ore
        OVERWORLD_COSMIC_DUST_ORE = PokecubeLegends.BLOCKS_TAB.register("cosmic_dust_ore", () -> new MeteorBlock(6842513,
                AbstractBlock.Properties.of(Material.STONE, MaterialColor.TERRACOTTA_BLUE).sound(SoundType.STONE)
                .strength(5, 15).harvestTool(ToolType.PICKAXE).requiresCorrectToolForDrops().harvestLevel(2)));
        COSMIC_DUST_BLOCK = PokecubeLegends.DECORATION_TAB.register("cosmic_dust_block", () -> new SandBlock(2730984,
                AbstractBlock.Properties.of(Material.SAND, MaterialColor.COLOR_LIGHT_BLUE).sound(SoundType.SAND)
                .strength(0.5F).harvestTool(ToolType.SHOVEL).harvestLevel(1)));
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
            if (reg == BlockInit.ULTRA_TORCH1 || reg == BlockInit.ULTRA_TORCH1_WALL) continue;
            PokecubeLegends.ITEMS.register(reg.getId().getPath(), () -> new BlockItem(reg.get(), new Item.Properties()
                    .tab(PokecubeLegends.TAB)));
        }
        
        for (final RegistryObject<Block> reg : PokecubeLegends.DECORATION_TAB.getEntries())
            PokecubeLegends.ITEMS.register(reg.getId().getPath(), () -> new BlockItem(reg.get(), new Item.Properties()
                    .tab(PokecubeLegends.DECO_TAB)));
    }

    public static void strippableBlocks(final FMLLoadCompleteEvent event)
    {
        // Enqueue this so that it runs on main thread, to prevent concurrency
        // issues.
        event.enqueueWork(() ->
        {
            ItemGenerator.addStrippable(BlockInit.AGED_LOG.get(), BlockInit.STRIP_AGED_LOG.get());
            ItemGenerator.addStrippable(BlockInit.AGED_WOOD.get(), BlockInit.STRIP_AGED_WOOD.get());
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
    
    public static void compostableBlocks(float chance, RegistryObject<Block> item) 
    {
        ComposterBlock.COMPOSTABLES.put(item.get().asItem(), chance);
    }
    
    public static void compostables() 
    {
        compostableBlocks(0.3f, BlockInit.DYNA_LEAVES1);
        compostableBlocks(0.3f, BlockInit.DYNA_LEAVES2);
        compostableBlocks(0.3f, BlockInit.INVERTED_SAPLING);
        compostableBlocks(0.3f, BlockInit.ULTRA_JUNGLE_SAPLING);
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
        compostableBlocks(0.65f, PlantsInit.MUSH_PLANT1);
        compostableBlocks(0.65f, PlantsInit.MUSH_PLANT2);
        compostableBlocks(0.65f, PlantsInit.AGED_FLOWER);
        compostableBlocks(0.65f, PlantsInit.DIRST_FLOWER);
    }

    public static void flammableBlocks(Block block, int speed, int flammability) {
        FireBlock fire = (FireBlock) Blocks.FIRE;
        fire.setFlammable(block, speed, flammability);
    }

    public static void flammables() {
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
        flammableBlocks(PlantsInit.AGED_FLOWER.get(), 60, 100);
        flammableBlocks(PlantsInit.DIRST_FLOWER.get(), 60, 100);
        flammableBlocks(PlantsInit.MUSH_PLANT1.get(), 60, 100);
        flammableBlocks(PlantsInit.MUSH_PLANT2.get(), 60, 100);
    }
}
