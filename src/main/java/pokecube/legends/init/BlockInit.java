package pokecube.legends.init;

import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.FallingBlock;
import net.minecraft.block.FenceBlock;
import net.minecraft.block.FenceGateBlock;
import net.minecraft.block.LeavesBlock;
import net.minecraft.block.MagmaBlock;
import net.minecraft.block.PressurePlateBlock;
import net.minecraft.block.SlabBlock;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.material.MaterialColor;
import net.minecraft.item.BlockItem;
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
import pokecube.legends.blocks.customblocks.HeatranBlock;
import pokecube.legends.blocks.customblocks.KeldeoBlock;
import pokecube.legends.blocks.customblocks.LegendaryBlock;
import pokecube.legends.blocks.customblocks.NatureCoreBlock;
import pokecube.legends.blocks.customblocks.PortalWarp;
import pokecube.legends.blocks.customblocks.RaidSpawnBlock;
import pokecube.legends.blocks.customblocks.Regice_Core;
import pokecube.legends.blocks.customblocks.Regidrago_Core;
import pokecube.legends.blocks.customblocks.Regieleki_Core;
import pokecube.legends.blocks.customblocks.Regigigas_Core;
import pokecube.legends.blocks.customblocks.Regirock_Core;
import pokecube.legends.blocks.customblocks.Registeel_Core;
import pokecube.legends.blocks.customblocks.SpaceCoreBlock;
import pokecube.legends.blocks.customblocks.SpectrumGlass;
import pokecube.legends.blocks.customblocks.TaoTrioBlock;
import pokecube.legends.blocks.customblocks.TapuBuluCore;
import pokecube.legends.blocks.customblocks.TapuFiniCore;
import pokecube.legends.blocks.customblocks.TapuKokoCore;
import pokecube.legends.blocks.customblocks.TapuLeleCore;
import pokecube.legends.blocks.customblocks.TroughBlock;
import pokecube.legends.blocks.customblocks.VictiniBlock;
import pokecube.legends.blocks.customblocks.XerneasCore;
import pokecube.legends.blocks.customblocks.YveltalEgg;
import pokecube.legends.blocks.customblocks.taputotem.BuluTotem;
import pokecube.legends.blocks.customblocks.taputotem.FiniTotem;
import pokecube.legends.blocks.customblocks.taputotem.KokoTotem;
import pokecube.legends.blocks.customblocks.taputotem.LeleTotem;
import pokecube.legends.blocks.normalblocks.DarkStoneBlock;
import pokecube.legends.blocks.normalblocks.GrassAgedBlock;
import pokecube.legends.blocks.normalblocks.GrassDistorticBlock;
import pokecube.legends.blocks.normalblocks.GrassJungleBlock;
import pokecube.legends.blocks.normalblocks.GrassMussBlock;
import pokecube.legends.blocks.normalblocks.MagneticBlock;
import pokecube.legends.blocks.normalblocks.SandDistorBlock;
import pokecube.legends.blocks.normalblocks.SandUltraBlock;
import pokecube.legends.blocks.normalblocks.UltraTorch1;
import pokecube.legends.blocks.normalblocks.UltraTorch1Wall;
import pokecube.legends.blocks.plants.Distortic_Tree;
import pokecube.legends.blocks.plants.Ultra_Tree01;
import pokecube.legends.blocks.plants.Ultra_Tree02;
import pokecube.legends.blocks.plants.Ultra_Tree03;

public class BlockInit
{
    // Blocks
    public static final RegistryObject<Block> RAID_SPAWN;
    public static final RegistryObject<Block> METEOR_BLOCK;

    // Decorative_Blocks
    public static final RegistryObject<Block> OCEAN_BRICK;
    public static final RegistryObject<Block> SKY_BRICK;
    public static final RegistryObject<Block> SPATIAN_BRICK;
    public static final RegistryObject<Block> MAGMA_BRICK;
    public static final RegistryObject<Block> CRYSTAL_BRICK;
    public static final RegistryObject<Block> DARKSKY_BRICK;
    public static final RegistryObject<Block> DYNA_LEAVE1;
    public static final RegistryObject<Block> DYNA_LEAVE2;
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
    public static final RegistryObject<Block> TEMPORAL_CRYSTAL;
    public static final RegistryObject<Block> ULTRA_MAGNETIC;
    public static final RegistryObject<Block> ULTRA_SANDSTONE;
    public static final RegistryObject<Block> ULTRA_DARKSTONE;
    public static final RegistryObject<Block> ULTRA_GRASSMUSS;
    public static final RegistryObject<Block> ULTRA_DIRTMUSS;
    public static final RegistryObject<Block> ULTRA_GRASSJUN;
    public static final RegistryObject<Block> ULTRA_DIRTJUN;
    public static final RegistryObject<Block> ULTRA_STONE;
    public static final RegistryObject<Block> ULTRA_METAL;
    public static final RegistryObject<Block> ULTRA_SAND;
    public static final RegistryObject<Block> ULTRA_DARKCOBBLES;
    public static final RegistryObject<Block> SPECTRUM_GLASS;
    public static final RegistryObject<Block> ULTRA_SANDDISTOR;
    public static final RegistryObject<Block> ULTRA_ROCKDISTOR;
    public static final RegistryObject<Block> ULTRA_GRASSAGED;
    public static final RegistryObject<Block> ULTRA_DIRTAGED;

    public static final RegistryObject<Block> DISTORTIC_GRASS;
    public static final RegistryObject<Block> DISTORTIC_STONE;
    public static final RegistryObject<Block> DISTORTIC_MIRROR;

    public static final RegistryObject<Block> ULTRA_TORCH1;
    public static final RegistryObject<Block> ULTRA_TORCH1_WALL;

    // Plants(LOG/Planks/Leaves)
    public static final RegistryObject<Block> ULTRA_SAPLING_UB01;
    public static final RegistryObject<Block> ULTRA_SAPLING_UB02;
    public static final RegistryObject<Block> ULTRA_SAPLING_UB03;
    public static final RegistryObject<Block> DISTORTIC_SAPLING;

    public static final RegistryObject<Block> ULTRA_LOGUB01;
    public static final RegistryObject<Block> ULTRA_PLANKUB01;
    public static final RegistryObject<Block> ULTRA_LEAVEUB01;
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

    public static final RegistryObject<Block> ULTRA_LOGUB02;
    public static final RegistryObject<Block> ULTRA_PLANKUB02;
    public static final RegistryObject<Block> ULTRA_LEAVEUB02;
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

    public static final RegistryObject<Block> ULTRA_LOGUB03;
    public static final RegistryObject<Block> ULTRA_PLANKUB03;
    public static final RegistryObject<Block> ULTRA_LEAVEUB03;
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
    public static final RegistryObject<Block> DISTORTIC_PLANK;
    public static final RegistryObject<Block> DISTORTIC_LEAVE;
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
    public static final RegistryObject<Block> RUBY_BLOCK;
    public static final RegistryObject<Block> SAPPHIRE_BLOCK;
    public static final RegistryObject<Block> SPECTRUM_ORE;
    public static final RegistryObject<Block> SPECTRUM_BLOCK;
    public static final RegistryObject<Block> COSMIC_DUST_ORE;

    static
    {
        // Block Raid
        RAID_SPAWN = PokecubeLegends.BLOCKS.register("raidspawn_block", () -> new RaidSpawnBlock(Material.IRON, MaterialColor.BLACK)
                .setInfoBlockName("raidspawn"));

        // Decorative_Blocks
        DYNA_LEAVE1 = PokecubeLegends.DECORATION_TAB.register("dyna_leave_1", () -> new LeavesBlock(Block.Properties.create(
                Material.LEAVES, MaterialColor.PINK).hardnessAndResistance(1f, 5).sound(SoundType.WET_GRASS).noDrops().notSolid()));
        DYNA_LEAVE2 = PokecubeLegends.DECORATION_TAB.register("dyna_leave_2", () -> new LeavesBlock(Block.Properties.create(
                Material.LEAVES, MaterialColor.PINK).hardnessAndResistance(1f, 5).sound(SoundType.WET_GRASS).noDrops().notSolid()));

        OCEAN_BRICK = PokecubeLegends.DECORATION_TAB.register("oceanbrick", () -> new Block(Block.Properties.create(
                Material.ROCK, MaterialColor.CYAN).hardnessAndResistance(1.5f, 10).sound(SoundType.STONE)));
        SKY_BRICK   = PokecubeLegends.DECORATION_TAB.register("skybrick", () -> new Block(Block.Properties.create(Material.ROCK, MaterialColor.BLUE)
                .hardnessAndResistance(1.5f, 10).sound(SoundType.STONE)));
        SPATIAN_BRICK = PokecubeLegends.DECORATION_TAB.register("spatianbrick", () -> new Block(Block.Properties.create(
                Material.ROCK, MaterialColor.MAGENTA).hardnessAndResistance(1.5f, 10).sound(SoundType.STONE)));
        MAGMA_BRICK   = PokecubeLegends.DECORATION_TAB.register("magmabrick", () -> new MagmaBlock(Block.Properties.create(
                Material.ROCK, MaterialColor.NETHERRACK).hardnessAndResistance(1.5f, 10).sound(SoundType.NETHERRACK).setLightLevel(b -> 3)
        			.setEmmisiveRendering((s, r, p) -> true)));
        DARKSKY_BRICK = PokecubeLegends.DECORATION_TAB.register("darkskybrick", () -> new Block(Block.Properties.create(
                Material.ROCK, MaterialColor.LIGHT_GRAY).hardnessAndResistance(1.5f, 10).sound(SoundType.STONE)));

        METEOR_BLOCK = PokecubeLegends.BLOCKS_TAB.register("meteor_block", () -> new FallingBlock(Block.Properties
                .create(Material.GOURD, MaterialColor.BLUE_TERRACOTTA).hardnessAndResistance(2.5f).sound(SoundType.METAL).harvestTool(ToolType.PICKAXE)
                .harvestLevel(2)));

        TOTEM_BLOCK = PokecubeLegends.DECORATION_TAB.register("totem_block", () -> new Block(Block.Properties.create(
                Material.ROCK, MaterialColor.LIGHT_GRAY).hardnessAndResistance(1.5f, 10).sound(SoundType.STONE)));

        
        //Tapus Totems
        
        // Koko Totem
        KOKO_WHITE   = PokecubeLegends.DECORATION_TAB.register("koko_white_totem", () -> new KokoTotem("koko_white_totem",
        		Block.Properties.create(Material.WOOD, MaterialColor.WHITE_TERRACOTTA).hardnessAndResistance(5, 15)
        		.sound(SoundType.WOOD).harvestTool(ToolType.AXE).harvestLevel(2).variableOpacity()).noInfoBlock());
        KOKO_RED   = PokecubeLegends.DECORATION_TAB.register("koko_red_totem", () -> new KokoTotem("koko_red_totem",
        		Block.Properties.create(Material.WOOD, MaterialColor.RED_TERRACOTTA).hardnessAndResistance(5, 15)
        		.sound(SoundType.WOOD).harvestTool(ToolType.AXE).harvestLevel(2).variableOpacity()).noInfoBlock());
        KOKO_BLUE   = PokecubeLegends.DECORATION_TAB.register("koko_blue_totem", () -> new KokoTotem("koko_blue_totem",
        		Block.Properties.create(Material.WOOD, MaterialColor.BLUE_TERRACOTTA).hardnessAndResistance(5, 15)
        		.sound(SoundType.WOOD).harvestTool(ToolType.AXE).harvestLevel(2).variableOpacity()).noInfoBlock());
        KOKO_YELLOW   = PokecubeLegends.DECORATION_TAB.register("koko_yellow_totem", () -> new KokoTotem("koko_yellow_totem",
        		Block.Properties.create(Material.WOOD, MaterialColor.YELLOW_TERRACOTTA).hardnessAndResistance(5, 15)
        		.sound(SoundType.WOOD).harvestTool(ToolType.AXE).harvestLevel(2).variableOpacity()).noInfoBlock());
        KOKO_GREEN   = PokecubeLegends.DECORATION_TAB.register("koko_green_totem", () -> new KokoTotem("koko_green_totem",
        		Block.Properties.create(Material.WOOD, MaterialColor.GREEN_TERRACOTTA).hardnessAndResistance(5, 15)
        		.sound(SoundType.WOOD).harvestTool(ToolType.AXE).harvestLevel(2).variableOpacity()).noInfoBlock());
        KOKO_ORANGE   = PokecubeLegends.DECORATION_TAB.register("koko_orange_totem", () -> new KokoTotem("koko_orange_totem",
        		Block.Properties.create(Material.WOOD, MaterialColor.ORANGE_TERRACOTTA).hardnessAndResistance(5, 15)
        		.sound(SoundType.WOOD).harvestTool(ToolType.AXE).harvestLevel(2).variableOpacity()).noInfoBlock());
        KOKO_CYAN   = PokecubeLegends.DECORATION_TAB.register("koko_cyan_totem", () -> new KokoTotem("koko_cyan_totem",
        		Block.Properties.create(Material.WOOD, MaterialColor.CYAN_TERRACOTTA).hardnessAndResistance(5, 15)
        		.sound(SoundType.WOOD).harvestTool(ToolType.AXE).harvestLevel(2).variableOpacity()).noInfoBlock());
        KOKO_LIGHT_BLUE   = PokecubeLegends.DECORATION_TAB.register("koko_lightblue_totem", () -> new KokoTotem("koko_lightblue_totem",
        		Block.Properties.create(Material.WOOD, MaterialColor.LIGHT_BLUE_TERRACOTTA).hardnessAndResistance(5, 15)
        		.sound(SoundType.WOOD).harvestTool(ToolType.AXE).harvestLevel(2).variableOpacity()).noInfoBlock());
        KOKO_GRAY   = PokecubeLegends.DECORATION_TAB.register("koko_gray_totem", () -> new KokoTotem("koko_gray_totem",
        		Block.Properties.create(Material.WOOD, MaterialColor.GRAY_TERRACOTTA).hardnessAndResistance(5, 15)
        		.sound(SoundType.WOOD).harvestTool(ToolType.AXE).harvestLevel(2).variableOpacity()).noInfoBlock());
        KOKO_LIGHT_GRAY   = PokecubeLegends.DECORATION_TAB.register("koko_lightgray_totem", () -> new KokoTotem("koko_lightgray_totem",
        		Block.Properties.create(Material.WOOD, MaterialColor.LIGHT_GRAY_TERRACOTTA).hardnessAndResistance(5, 15)
        		.sound(SoundType.WOOD).harvestTool(ToolType.AXE).harvestLevel(2).variableOpacity()).noInfoBlock());
        KOKO_BROWN   = PokecubeLegends.DECORATION_TAB.register("koko_brown_totem", () -> new KokoTotem("koko_brown_totem",
        		Block.Properties.create(Material.WOOD, MaterialColor.BROWN_TERRACOTTA).hardnessAndResistance(5, 15)
        		.sound(SoundType.WOOD).harvestTool(ToolType.AXE).harvestLevel(2).variableOpacity()).noInfoBlock());
        KOKO_BLACK   = PokecubeLegends.DECORATION_TAB.register("koko_black_totem", () -> new KokoTotem("koko_black_totem",
        		Block.Properties.create(Material.WOOD, MaterialColor.BLACK_TERRACOTTA).hardnessAndResistance(5, 15)
        		.sound(SoundType.WOOD).harvestTool(ToolType.AXE).harvestLevel(2).variableOpacity()).noInfoBlock());
        KOKO_MAGENTA   = PokecubeLegends.DECORATION_TAB.register("koko_magenta_totem", () -> new KokoTotem("koko_magenta_totem",
        		Block.Properties.create(Material.WOOD, MaterialColor.MAGENTA_TERRACOTTA).hardnessAndResistance(5, 15)
        		.sound(SoundType.WOOD).harvestTool(ToolType.AXE).harvestLevel(2).variableOpacity()).noInfoBlock());
        KOKO_LIME   = PokecubeLegends.DECORATION_TAB.register("koko_lime_totem", () -> new KokoTotem("koko_lime_totem",
        		Block.Properties.create(Material.WOOD, MaterialColor.LIME_TERRACOTTA).hardnessAndResistance(5, 15)
        		.sound(SoundType.WOOD).harvestTool(ToolType.AXE).harvestLevel(2).variableOpacity()).noInfoBlock());
        KOKO_PINK   = PokecubeLegends.DECORATION_TAB.register("koko_pink_totem", () -> new KokoTotem("koko_pink_totem",
        		Block.Properties.create(Material.WOOD, MaterialColor.PINK_TERRACOTTA).hardnessAndResistance(5, 15)
        		.sound(SoundType.WOOD).harvestTool(ToolType.AXE).harvestLevel(2).variableOpacity()).noInfoBlock());
        KOKO_PURPLE   = PokecubeLegends.DECORATION_TAB.register("koko_purple_totem", () -> new KokoTotem("koko_purple_totem",
        		Block.Properties.create(Material.WOOD, MaterialColor.PURPLE_TERRACOTTA).hardnessAndResistance(5, 15)
        		.sound(SoundType.WOOD).harvestTool(ToolType.AXE).harvestLevel(2).variableOpacity()).noInfoBlock());
        //
        
        // Bulu Totem
        BULU_WHITE   = PokecubeLegends.DECORATION_TAB.register("bulu_white_totem", () -> new BuluTotem("bulu_white_totem",
        		Block.Properties.create(Material.WOOD, MaterialColor.WHITE_TERRACOTTA).hardnessAndResistance(5, 15)
        		.sound(SoundType.WOOD).harvestTool(ToolType.AXE).harvestLevel(2).variableOpacity()).noInfoBlock());
        BULU_RED   = PokecubeLegends.DECORATION_TAB.register("bulu_red_totem", () -> new BuluTotem("bulu_red_totem",
        		Block.Properties.create(Material.WOOD, MaterialColor.RED_TERRACOTTA).hardnessAndResistance(5, 15)
        		.sound(SoundType.WOOD).harvestTool(ToolType.AXE).harvestLevel(2).variableOpacity()).noInfoBlock());
        BULU_BLUE   = PokecubeLegends.DECORATION_TAB.register("bulu_blue_totem", () -> new BuluTotem("bulu_blue_totem",
        		Block.Properties.create(Material.WOOD, MaterialColor.BLUE_TERRACOTTA).hardnessAndResistance(5, 15)
        		.sound(SoundType.WOOD).harvestTool(ToolType.AXE).harvestLevel(2).variableOpacity()).noInfoBlock());
        BULU_YELLOW   = PokecubeLegends.DECORATION_TAB.register("bulu_yellow_totem", () -> new BuluTotem("bulu_yellow_totem",
        		Block.Properties.create(Material.WOOD, MaterialColor.YELLOW_TERRACOTTA).hardnessAndResistance(5, 15)
        		.sound(SoundType.WOOD).harvestTool(ToolType.AXE).harvestLevel(2).variableOpacity()).noInfoBlock());
        BULU_GREEN   = PokecubeLegends.DECORATION_TAB.register("bulu_green_totem", () -> new BuluTotem("bulu_green_totem",
        		Block.Properties.create(Material.WOOD, MaterialColor.GREEN_TERRACOTTA).hardnessAndResistance(5, 15)
        		.sound(SoundType.WOOD).harvestTool(ToolType.AXE).harvestLevel(2).variableOpacity()).noInfoBlock());
        BULU_ORANGE   = PokecubeLegends.DECORATION_TAB.register("bulu_orange_totem", () -> new BuluTotem("bulu_orange_totem",
        		Block.Properties.create(Material.WOOD, MaterialColor.ORANGE_TERRACOTTA).hardnessAndResistance(5, 15)
        		.sound(SoundType.WOOD).harvestTool(ToolType.AXE).harvestLevel(2).variableOpacity()).noInfoBlock());
        BULU_CYAN   = PokecubeLegends.DECORATION_TAB.register("bulu_cyan_totem", () -> new BuluTotem("bulu_cyan_totem",
        		Block.Properties.create(Material.WOOD, MaterialColor.CYAN_TERRACOTTA).hardnessAndResistance(5, 15)
        		.sound(SoundType.WOOD).harvestTool(ToolType.AXE).harvestLevel(2).variableOpacity()).noInfoBlock());
        BULU_LIGHT_BLUE   = PokecubeLegends.DECORATION_TAB.register("bulu_lightblue_totem", () -> new BuluTotem("bulu_lightblue_totem",
        		Block.Properties.create(Material.WOOD, MaterialColor.LIGHT_BLUE_TERRACOTTA).hardnessAndResistance(5, 15)
        		.sound(SoundType.WOOD).harvestTool(ToolType.AXE).harvestLevel(2).variableOpacity()).noInfoBlock());
        BULU_GRAY   = PokecubeLegends.DECORATION_TAB.register("bulu_gray_totem", () -> new BuluTotem("bulu_gray_totem",
        		Block.Properties.create(Material.WOOD, MaterialColor.GRAY_TERRACOTTA).hardnessAndResistance(5, 15)
        		.sound(SoundType.WOOD).harvestTool(ToolType.AXE).harvestLevel(2).variableOpacity()).noInfoBlock());
        BULU_LIGHT_GRAY   = PokecubeLegends.DECORATION_TAB.register("bulu_lightgray_totem", () -> new BuluTotem("bulu_lightgray_totem",
        		Block.Properties.create(Material.WOOD, MaterialColor.LIGHT_GRAY_TERRACOTTA).hardnessAndResistance(5, 15)
        		.sound(SoundType.WOOD).harvestTool(ToolType.AXE).harvestLevel(2).variableOpacity()).noInfoBlock());
        BULU_BROWN   = PokecubeLegends.DECORATION_TAB.register("bulu_brown_totem", () -> new BuluTotem("bulu_brown_totem",
        		Block.Properties.create(Material.WOOD, MaterialColor.BROWN_TERRACOTTA).hardnessAndResistance(5, 15)
        		.sound(SoundType.WOOD).harvestTool(ToolType.AXE).harvestLevel(2).variableOpacity()).noInfoBlock());
        BULU_BLACK   = PokecubeLegends.DECORATION_TAB.register("bulu_black_totem", () -> new BuluTotem("bulu_black_totem",
        		Block.Properties.create(Material.WOOD, MaterialColor.BLACK_TERRACOTTA).hardnessAndResistance(5, 15)
        		.sound(SoundType.WOOD).harvestTool(ToolType.AXE).harvestLevel(2).variableOpacity()).noInfoBlock());
        BULU_MAGENTA   = PokecubeLegends.DECORATION_TAB.register("bulu_magenta_totem", () -> new BuluTotem("bulu_magenta_totem",
        		Block.Properties.create(Material.WOOD, MaterialColor.MAGENTA_TERRACOTTA).hardnessAndResistance(5, 15)
        		.sound(SoundType.WOOD).harvestTool(ToolType.AXE).harvestLevel(2).variableOpacity()).noInfoBlock());
        BULU_LIME   = PokecubeLegends.DECORATION_TAB.register("bulu_lime_totem", () -> new BuluTotem("bulu_lime_totem",
        		Block.Properties.create(Material.WOOD, MaterialColor.LIME_TERRACOTTA).hardnessAndResistance(5, 15)
        		.sound(SoundType.WOOD).harvestTool(ToolType.AXE).harvestLevel(2).variableOpacity()).noInfoBlock());
        BULU_PINK   = PokecubeLegends.DECORATION_TAB.register("bulu_pink_totem", () -> new BuluTotem("bulu_pink_totem",
        		Block.Properties.create(Material.WOOD, MaterialColor.PINK_TERRACOTTA).hardnessAndResistance(5, 15)
        		.sound(SoundType.WOOD).harvestTool(ToolType.AXE).harvestLevel(2).variableOpacity()).noInfoBlock());
        BULU_PURPLE   = PokecubeLegends.DECORATION_TAB.register("bulu_purple_totem", () -> new BuluTotem("bulu_purple_totem",
        		Block.Properties.create(Material.WOOD, MaterialColor.PURPLE_TERRACOTTA).hardnessAndResistance(5, 15)
        		.sound(SoundType.WOOD).harvestTool(ToolType.AXE).harvestLevel(2).variableOpacity()).noInfoBlock());
        //
        
        // Lele Totem
        LELE_WHITE   = PokecubeLegends.DECORATION_TAB.register("lele_white_totem", () -> new LeleTotem("lele_white_totem",
        		Block.Properties.create(Material.WOOD, MaterialColor.WHITE_TERRACOTTA).hardnessAndResistance(5, 15)
        		.sound(SoundType.WOOD).harvestTool(ToolType.AXE).harvestLevel(2).variableOpacity()).noInfoBlock());
        LELE_RED   = PokecubeLegends.DECORATION_TAB.register("lele_red_totem", () -> new LeleTotem("lele_red_totem",
        		Block.Properties.create(Material.WOOD, MaterialColor.RED_TERRACOTTA).hardnessAndResistance(5, 15)
        		.sound(SoundType.WOOD).harvestTool(ToolType.AXE).harvestLevel(2).variableOpacity()).noInfoBlock());
        LELE_BLUE   = PokecubeLegends.DECORATION_TAB.register("lele_blue_totem", () -> new LeleTotem("lele_blue_totem",
        		Block.Properties.create(Material.WOOD, MaterialColor.BLUE_TERRACOTTA).hardnessAndResistance(5, 15)
        		.sound(SoundType.WOOD).harvestTool(ToolType.AXE).harvestLevel(2).variableOpacity()).noInfoBlock());
        LELE_YELLOW   = PokecubeLegends.DECORATION_TAB.register("lele_yellow_totem", () -> new LeleTotem("lele_yellow_totem",
        		Block.Properties.create(Material.WOOD, MaterialColor.YELLOW_TERRACOTTA).hardnessAndResistance(5, 15)
        		.sound(SoundType.WOOD).harvestTool(ToolType.AXE).harvestLevel(2).variableOpacity()).noInfoBlock());
        LELE_GREEN   = PokecubeLegends.DECORATION_TAB.register("lele_green_totem", () -> new LeleTotem("lele_green_totem",
        		Block.Properties.create(Material.WOOD, MaterialColor.GREEN_TERRACOTTA).hardnessAndResistance(5, 15)
        		.sound(SoundType.WOOD).harvestTool(ToolType.AXE).harvestLevel(2).variableOpacity()).noInfoBlock());
        LELE_ORANGE   = PokecubeLegends.DECORATION_TAB.register("lele_orange_totem", () -> new LeleTotem("lele_orange_totem",
        		Block.Properties.create(Material.WOOD, MaterialColor.ORANGE_TERRACOTTA).hardnessAndResistance(5, 15)
        		.sound(SoundType.WOOD).harvestTool(ToolType.AXE).harvestLevel(2).variableOpacity()).noInfoBlock());
        LELE_CYAN   = PokecubeLegends.DECORATION_TAB.register("lele_cyan_totem", () -> new LeleTotem("lele_cyan_totem",
        		Block.Properties.create(Material.WOOD, MaterialColor.CYAN_TERRACOTTA).hardnessAndResistance(5, 15)
        		.sound(SoundType.WOOD).harvestTool(ToolType.AXE).harvestLevel(2).variableOpacity()).noInfoBlock());
        LELE_LIGHT_BLUE   = PokecubeLegends.DECORATION_TAB.register("lele_lightblue_totem", () -> new LeleTotem("lele_lightblue_totem",
        		Block.Properties.create(Material.WOOD, MaterialColor.LIGHT_BLUE_TERRACOTTA).hardnessAndResistance(5, 15)
        		.sound(SoundType.WOOD).harvestTool(ToolType.AXE).harvestLevel(2).variableOpacity()).noInfoBlock());
        LELE_GRAY   = PokecubeLegends.DECORATION_TAB.register("lele_gray_totem", () -> new LeleTotem("lele_gray_totem",
        		Block.Properties.create(Material.WOOD, MaterialColor.GRAY_TERRACOTTA).hardnessAndResistance(5, 15)
        		.sound(SoundType.WOOD).harvestTool(ToolType.AXE).harvestLevel(2).variableOpacity()).noInfoBlock());
        LELE_LIGHT_GRAY   = PokecubeLegends.DECORATION_TAB.register("lele_lightgray_totem", () -> new LeleTotem("lele_lightgray_totem",
        		Block.Properties.create(Material.WOOD, MaterialColor.LIGHT_GRAY_TERRACOTTA).hardnessAndResistance(5, 15)
        		.sound(SoundType.WOOD).harvestTool(ToolType.AXE).harvestLevel(2).variableOpacity()).noInfoBlock());
        LELE_BROWN   = PokecubeLegends.DECORATION_TAB.register("lele_brown_totem", () -> new LeleTotem("lele_brown_totem",
        		Block.Properties.create(Material.WOOD, MaterialColor.BROWN_TERRACOTTA).hardnessAndResistance(5, 15)
        		.sound(SoundType.WOOD).harvestTool(ToolType.AXE).harvestLevel(2).variableOpacity()).noInfoBlock());
        LELE_BLACK   = PokecubeLegends.DECORATION_TAB.register("lele_black_totem", () -> new LeleTotem("lele_black_totem",
        		Block.Properties.create(Material.WOOD, MaterialColor.BLACK_TERRACOTTA).hardnessAndResistance(5, 15)
        		.sound(SoundType.WOOD).harvestTool(ToolType.AXE).harvestLevel(2).variableOpacity()).noInfoBlock());
        LELE_MAGENTA   = PokecubeLegends.DECORATION_TAB.register("lele_magenta_totem", () -> new LeleTotem("lele_magenta_totem",
        		Block.Properties.create(Material.WOOD, MaterialColor.MAGENTA_TERRACOTTA).hardnessAndResistance(5, 15)
        		.sound(SoundType.WOOD).harvestTool(ToolType.AXE).harvestLevel(2).variableOpacity()).noInfoBlock());
        LELE_LIME   = PokecubeLegends.DECORATION_TAB.register("lele_lime_totem", () -> new LeleTotem("lele_lime_totem",
        		Block.Properties.create(Material.WOOD, MaterialColor.LIME_TERRACOTTA).hardnessAndResistance(5, 15)
        		.sound(SoundType.WOOD).harvestTool(ToolType.AXE).harvestLevel(2).variableOpacity()).noInfoBlock());
        LELE_PINK   = PokecubeLegends.DECORATION_TAB.register("lele_pink_totem", () -> new LeleTotem("lele_pink_totem",
        		Block.Properties.create(Material.WOOD, MaterialColor.PINK_TERRACOTTA).hardnessAndResistance(5, 15)
        		.sound(SoundType.WOOD).harvestTool(ToolType.AXE).harvestLevel(2).variableOpacity()).noInfoBlock());
        LELE_PURPLE   = PokecubeLegends.DECORATION_TAB.register("lele_purple_totem", () -> new LeleTotem("lele_purple_totem",
        		Block.Properties.create(Material.WOOD, MaterialColor.PURPLE_TERRACOTTA).hardnessAndResistance(5, 15)
        		.sound(SoundType.WOOD).harvestTool(ToolType.AXE).harvestLevel(2).variableOpacity()).noInfoBlock());
        //
        
        // Fini Totem
        FINI_WHITE   = PokecubeLegends.DECORATION_TAB.register("fini_white_totem", () -> new FiniTotem("fini_white_totem",
        		Block.Properties.create(Material.WOOD, MaterialColor.WHITE_TERRACOTTA).hardnessAndResistance(5, 15)
        		.sound(SoundType.WOOD).harvestTool(ToolType.AXE).harvestLevel(2).variableOpacity()).noInfoBlock());
        FINI_RED   = PokecubeLegends.DECORATION_TAB.register("fini_red_totem", () -> new FiniTotem("fini_red_totem",
        		Block.Properties.create(Material.WOOD, MaterialColor.RED_TERRACOTTA).hardnessAndResistance(5, 15)
        		.sound(SoundType.WOOD).harvestTool(ToolType.AXE).harvestLevel(2).variableOpacity()).noInfoBlock());
        FINI_BLUE   = PokecubeLegends.DECORATION_TAB.register("fini_blue_totem", () -> new FiniTotem("fini_blue_totem",
        		Block.Properties.create(Material.WOOD, MaterialColor.BLUE_TERRACOTTA).hardnessAndResistance(5, 15)
        		.sound(SoundType.WOOD).harvestTool(ToolType.AXE).harvestLevel(2).variableOpacity()).noInfoBlock());
        FINI_YELLOW   = PokecubeLegends.DECORATION_TAB.register("fini_yellow_totem", () -> new FiniTotem("fini_yellow_totem",
        		Block.Properties.create(Material.WOOD, MaterialColor.YELLOW_TERRACOTTA).hardnessAndResistance(5, 15)
        		.sound(SoundType.WOOD).harvestTool(ToolType.AXE).harvestLevel(2).variableOpacity()).noInfoBlock());
        FINI_GREEN   = PokecubeLegends.DECORATION_TAB.register("fini_green_totem", () -> new FiniTotem("fini_green_totem",
        		Block.Properties.create(Material.WOOD, MaterialColor.GREEN_TERRACOTTA).hardnessAndResistance(5, 15)
        		.sound(SoundType.WOOD).harvestTool(ToolType.AXE).harvestLevel(2).variableOpacity()).noInfoBlock());
        FINI_ORANGE   = PokecubeLegends.DECORATION_TAB.register("fini_orange_totem", () -> new FiniTotem("fini_orange_totem",
        		Block.Properties.create(Material.WOOD, MaterialColor.ORANGE_TERRACOTTA).hardnessAndResistance(5, 15)
        		.sound(SoundType.WOOD).harvestTool(ToolType.AXE).harvestLevel(2).variableOpacity()).noInfoBlock());
        FINI_CYAN   = PokecubeLegends.DECORATION_TAB.register("fini_cyan_totem", () -> new FiniTotem("fini_cyan_totem",
        		Block.Properties.create(Material.WOOD, MaterialColor.CYAN_TERRACOTTA).hardnessAndResistance(5, 15)
        		.sound(SoundType.WOOD).harvestTool(ToolType.AXE).harvestLevel(2).variableOpacity()).noInfoBlock());
        FINI_LIGHT_BLUE   = PokecubeLegends.DECORATION_TAB.register("fini_lightblue_totem", () -> new FiniTotem("fini_lightblue_totem",
        		Block.Properties.create(Material.WOOD, MaterialColor.LIGHT_BLUE_TERRACOTTA).hardnessAndResistance(5, 15)
        		.sound(SoundType.WOOD).harvestTool(ToolType.AXE).harvestLevel(2).variableOpacity()).noInfoBlock());
        FINI_GRAY   = PokecubeLegends.DECORATION_TAB.register("fini_gray_totem", () -> new FiniTotem("fini_gray_totem",
        		Block.Properties.create(Material.WOOD, MaterialColor.GRAY_TERRACOTTA).hardnessAndResistance(5, 15)
        		.sound(SoundType.WOOD).harvestTool(ToolType.AXE).harvestLevel(2).variableOpacity()).noInfoBlock());
        FINI_LIGHT_GRAY   = PokecubeLegends.DECORATION_TAB.register("fini_lightgray_totem", () -> new FiniTotem("fini_lightgray_totem",
        		Block.Properties.create(Material.WOOD, MaterialColor.LIGHT_GRAY_TERRACOTTA).hardnessAndResistance(5, 15)
        		.sound(SoundType.WOOD).harvestTool(ToolType.AXE).harvestLevel(2).variableOpacity()).noInfoBlock());
        FINI_BROWN   = PokecubeLegends.DECORATION_TAB.register("fini_brown_totem", () -> new FiniTotem("fini_brown_totem",
        		Block.Properties.create(Material.WOOD, MaterialColor.BROWN_TERRACOTTA).hardnessAndResistance(5, 15)
        		.sound(SoundType.WOOD).harvestTool(ToolType.AXE).harvestLevel(2).variableOpacity()).noInfoBlock());
        FINI_BLACK   = PokecubeLegends.DECORATION_TAB.register("fini_black_totem", () -> new FiniTotem("fini_black_totem",
        		Block.Properties.create(Material.WOOD, MaterialColor.BLACK_TERRACOTTA).hardnessAndResistance(5, 15)
        		.sound(SoundType.WOOD).harvestTool(ToolType.AXE).harvestLevel(2).variableOpacity()).noInfoBlock());
        FINI_MAGENTA   = PokecubeLegends.DECORATION_TAB.register("fini_magenta_totem", () -> new FiniTotem("fini_magenta_totem",
        		Block.Properties.create(Material.WOOD, MaterialColor.MAGENTA_TERRACOTTA).hardnessAndResistance(5, 15)
        		.sound(SoundType.WOOD).harvestTool(ToolType.AXE).harvestLevel(2).variableOpacity()).noInfoBlock());
        FINI_LIME   = PokecubeLegends.DECORATION_TAB.register("fini_lime_totem", () -> new FiniTotem("fini_lime_totem",
        		Block.Properties.create(Material.WOOD, MaterialColor.LIME_TERRACOTTA).hardnessAndResistance(5, 15)
        		.sound(SoundType.WOOD).harvestTool(ToolType.AXE).harvestLevel(2).variableOpacity()).noInfoBlock());
        FINI_PINK   = PokecubeLegends.DECORATION_TAB.register("fini_pink_totem", () -> new FiniTotem("fini_pink_totem",
        		Block.Properties.create(Material.WOOD, MaterialColor.PINK_TERRACOTTA).hardnessAndResistance(5, 15)
        		.sound(SoundType.WOOD).harvestTool(ToolType.AXE).harvestLevel(2).variableOpacity()).noInfoBlock());
        FINI_PURPLE   = PokecubeLegends.DECORATION_TAB.register("fini_purple_totem", () -> new FiniTotem("fini_purple_totem",
        		Block.Properties.create(Material.WOOD, MaterialColor.PURPLE_TERRACOTTA).hardnessAndResistance(5, 15)
        		.sound(SoundType.WOOD).harvestTool(ToolType.AXE).harvestLevel(2).variableOpacity()).noInfoBlock());
        //
        
        CRYSTAL_BRICK = PokecubeLegends.BLOCKS_TAB.register("crystalbrick", () -> new BlockBase("crystalbrick",
                Material.PACKED_ICE, MaterialColor.LIGHT_BLUE, 0.5F, SoundType.GLASS, ToolType.PICKAXE, 1).noInfoBlock());

        // Dimensions
        SPECTRUM_GLASS = PokecubeLegends.BLOCKS_TAB.register("spectrum_glass", () -> new SpectrumGlass("spectrum_glass",
                Block.Properties.from(Blocks.GLASS).notSolid()));
        ULTRA_DIRTAGED = PokecubeLegends.BLOCKS_TAB.register("ultradirt3", () -> new BlockBase("ultradirt3",
                Material.ORGANIC, MaterialColor.YELLOW_TERRACOTTA, 0.5f, SoundType.WET_GRASS, ToolType.SHOVEL, 1).noInfoBlock());
        ULTRA_ROCKDISTOR = PokecubeLegends.BLOCKS_TAB.register("ultradirt4", () -> new BlockBase("ultradirt4",
                Material.ROCK, MaterialColor.PURPLE_TERRACOTTA, 0.9f, SoundType.METAL, ToolType.PICKAXE, 1).noInfoBlock());
        ULTRA_MAGNETIC = PokecubeLegends.BLOCKS_TAB.register("ultramagnetic", () -> new MagneticBlock("ultramagnetic",
                Material.ROCK, MaterialColor.BLUE).noInfoBlock());
        ULTRA_SANDSTONE = PokecubeLegends.BLOCKS_TAB.register("ultrasandstone", () -> new BlockBase("ultrasandstone",
                Material.ROCK, MaterialColor.SNOW, 0.5f, SoundType.STONE, ToolType.PICKAXE, 1).noInfoBlock());
        ULTRA_DARKSTONE = PokecubeLegends.BLOCKS_TAB.register("ultracobbles", () -> new DarkStoneBlock("ultracobbles",
                Material.ROCK, MaterialColor.BLACK).noInfoBlock());
        ULTRA_DARKCOBBLES = PokecubeLegends.BLOCKS_TAB.register("ultrarock", () -> new BlockBase("ultrarock",
                Material.ROCK, MaterialColor.BLACK, 0.8f, SoundType.STONE, ToolType.PICKAXE, 2).noInfoBlock());
        ULTRA_GRASSMUSS = PokecubeLegends.BLOCKS_TAB.register("ultragrass1", () -> new GrassMussBlock("ultragrass1",
                Material.ORGANIC, MaterialColor.RED).noInfoBlock());
        ULTRA_DIRTMUSS = PokecubeLegends.BLOCKS_TAB.register("ultradirt1", () -> new BlockBase("ultradirt1",
                Material.CLAY, MaterialColor.PURPLE, 0.5f, SoundType.GROUND, ToolType.SHOVEL, 1).noInfoBlock());
        ULTRA_GRASSJUN = PokecubeLegends.BLOCKS_TAB.register("ultragrass2", () -> new GrassJungleBlock("ultragrass2",
                Material.ORGANIC, MaterialColor.CYAN).noInfoBlock());
        ULTRA_DIRTJUN = PokecubeLegends.BLOCKS_TAB.register("ultradirt2", () -> new BlockBase("ultradirt2",
                Material.GOURD, MaterialColor.YELLOW_TERRACOTTA, 0.5f, SoundType.GROUND, ToolType.SHOVEL, 1).noInfoBlock());
        ULTRA_STONE = PokecubeLegends.BLOCKS_TAB.register("ultrastone", () -> new BlockBase("ultrastone", Material.ROCK, 
        		MaterialColor.CYAN_TERRACOTTA, 1.5f, SoundType.STONE, ToolType.PICKAXE, 2).noInfoBlock());
        ULTRA_METAL = PokecubeLegends.BLOCKS_TAB.register("ultrablock", () -> new BlockBase("ultrablock", Material.IRON, 
        		MaterialColor.LIME, 5.0f, 10f, SoundType.STONE, ToolType.PICKAXE, 2).noInfoBlock());
        ULTRA_SAND = PokecubeLegends.BLOCKS_TAB.register("ultrasand", () -> new SandUltraBlock("ultrasand",
                Material.SAND, MaterialColor.SNOW).noInfoBlock());
        ULTRA_SANDDISTOR = PokecubeLegends.BLOCKS_TAB.register("ultrasand1", () -> new SandDistorBlock("ultrasand1",
                Material.CLAY, MaterialColor.PURPLE).noInfoBlock());
        ULTRA_GRASSAGED = PokecubeLegends.BLOCKS_TAB.register("ultragrass3", () -> new GrassAgedBlock("ultragrass3",
                Material.GOURD, MaterialColor.YELLOW).noInfoBlock());
        TEMPORAL_CRYSTAL = PokecubeLegends.BLOCKS_TAB.register("temporal_crystal", () -> new BlockBase(
                "temporal_crystal", Material.GLASS, MaterialColor.LIGHT_BLUE, 1.5f, SoundType.GLASS, ToolType.PICKAXE, 1).noInfoBlock());

        // Distortic World
        DISTORTIC_GRASS = PokecubeLegends.BLOCKS_TAB.register("distortic_grass", () -> new GrassDistorticBlock(
                BlockBase.Properties.create(Material.ORGANIC, MaterialColor.PINK_TERRACOTTA).sound(SoundType.NETHER_WART).hardnessAndResistance(1, 2)
                        .harvestTool(ToolType.SHOVEL).harvestLevel(1)));
        DISTORTIC_STONE = PokecubeLegends.BLOCKS_TAB.register("distortic_stone", () -> new BlockBase("distortic_stone",
                Material.ROCK, MaterialColor.BLACK_TERRACOTTA, 1.5f, SoundType.STONE, ToolType.PICKAXE, 2).noInfoBlock());
        DISTORTIC_MIRROR = PokecubeLegends.BLOCKS_TAB.register("distortic_mirror", () -> new BlockBase(
                "distortic_mirror", Material.GLASS, MaterialColor.CLAY, 2.5f, SoundType.GLASS, ToolType.PICKAXE, 2).noInfoBlock());

        // Torches
        ULTRA_TORCH1 = PokecubeLegends.BLOCKS_TAB.register("ultra_torch1", () -> new UltraTorch1());
        ULTRA_TORCH1_WALL = PokecubeLegends.BLOCKS_TAB.register("ultra_torch1_wall", () -> new UltraTorch1Wall());

        // Plants
        ULTRA_SAPLING_UB01 = PokecubeLegends.BLOCKS_TAB.register("ultra_sapling01", () -> new SaplingBase(
                () -> new Ultra_Tree01(), Block.Properties.create(Material.PLANTS, MaterialColor.BLUE)
                .hardnessAndResistance(0f, 1f).sound(SoundType.PLANT).doesNotBlockMovement().notSolid()));
        ULTRA_SAPLING_UB02 = PokecubeLegends.BLOCKS_TAB.register("ultra_sapling02", () -> new SaplingBase(
                () -> new Ultra_Tree02(), Block.Properties.create(Material.PLANTS, MaterialColor.FOLIAGE)
                .hardnessAndResistance(0f, 1f).sound(SoundType.PLANT).doesNotBlockMovement().notSolid()));
        ULTRA_SAPLING_UB03 = PokecubeLegends.BLOCKS_TAB.register("ultra_sapling03", () -> new SaplingBase(
                () -> new Ultra_Tree03(), Block.Properties.create(Material.PLANTS, MaterialColor.YELLOW)
                .hardnessAndResistance(0f, 1f).sound(SoundType.PLANT).doesNotBlockMovement().notSolid()));
        DISTORTIC_SAPLING = PokecubeLegends.BLOCKS_TAB.register("distortic_sapling", () -> new SaplingBase(
                () -> new Distortic_Tree(), Block.Properties.create(Material.PLANTS, MaterialColor.PURPLE)
                .hardnessAndResistance(0f, 1f).sound(SoundType.PLANT).doesNotBlockMovement().notSolid()));

        // Woods (LOGS/LEAVES/PLANKS)
        ULTRA_LEAVEUB01 = PokecubeLegends.BLOCKS_TAB.register("ultra_leave01", () -> new LeavesBlock(Block.Properties.create(
                Material.LEAVES, MaterialColor.LIGHT_BLUE).hardnessAndResistance(1f, 5).sound(SoundType.PLANT).notSolid()));
        ULTRA_LOGUB01 = PokecubeLegends.BLOCKS_TAB.register("ultra_log01", () -> Blocks.createLogBlock(
                MaterialColor.LIGHT_BLUE_TERRACOTTA, MaterialColor.LIGHT_BLUE_TERRACOTTA));
        INVERTED_WOOD = PokecubeLegends.BLOCKS_TAB.register("inverted_wood", () -> Blocks.createLogBlock(
                MaterialColor.LIGHT_BLUE_TERRACOTTA, MaterialColor.LIGHT_BLUE_TERRACOTTA));
        STRIP_INVERTED_LOG = PokecubeLegends.BLOCKS_TAB.register("stripped_inverted_log", () -> Blocks.createLogBlock(
                MaterialColor.LIGHT_BLUE_TERRACOTTA, MaterialColor.LIGHT_BLUE_TERRACOTTA));
        STRIP_INVERTED_WOOD = PokecubeLegends.BLOCKS_TAB.register("stripped_inverted_wood", () -> Blocks.createLogBlock(
                MaterialColor.LIGHT_BLUE_TERRACOTTA, MaterialColor.LIGHT_BLUE_TERRACOTTA));
        ULTRA_PLANKUB01 = PokecubeLegends.BLOCKS_TAB.register("ultra_plank01", () -> new Block(Block.Properties.create(
        		Material.WOOD, MaterialColor.LIGHT_BLUE_TERRACOTTA).hardnessAndResistance(2.0F, 3.0f).sound(SoundType.WOOD)));
        INVERTED_STAIRS = PokecubeLegends.BLOCKS_TAB.register("inverted_stairs",
                () -> new ItemGenerator.GenericWoodStairs(Blocks.OAK_STAIRS.getDefaultState(), Block.Properties.create(
                		Material.WOOD, MaterialColor.LIGHT_BLUE_TERRACOTTA).hardnessAndResistance(2.0F, 3.0f).sound(SoundType.WOOD)));
        INVERTED_SLAB = PokecubeLegends.BLOCKS_TAB.register("inverted_slab", () -> new SlabBlock(Block.Properties.create(
        		Material.WOOD, MaterialColor.LIGHT_BLUE_TERRACOTTA).hardnessAndResistance(2.0F, 3.0f).sound(SoundType.WOOD)));
        INVERTED_FENCE = PokecubeLegends.BLOCKS_TAB.register("inverted_fence", () -> new FenceBlock(Block.Properties.create(
        		Material.WOOD, MaterialColor.LIGHT_BLUE_TERRACOTTA).hardnessAndResistance(2.0F, 3.0f).sound(SoundType.WOOD)));
        INVERTED_FENCE_GATE = PokecubeLegends.BLOCKS_TAB.register("inverted_fence_gate", () -> new FenceGateBlock(Block.Properties.create(
        		Material.WOOD, MaterialColor.LIGHT_BLUE_TERRACOTTA).hardnessAndResistance(2.0F, 3.0f).sound(SoundType.WOOD)));
        INVERTED_PR_PLATE = PokecubeLegends.BLOCKS_TAB.register("inverted_pressure_plate",
                () -> new ItemGenerator.GenericPressurePlate(PressurePlateBlock.Sensitivity.EVERYTHING, Block.Properties
                        .create(Material.WOOD, MaterialColor.LIGHT_BLUE_TERRACOTTA).sound(SoundType.WOOD).doesNotBlockMovement().hardnessAndResistance(
                                0.5F)));
        INVERTED_BUTTON = PokecubeLegends.BLOCKS_TAB.register("inverted_button",
                () -> new ItemGenerator.GenericWoodButton(Block.Properties.create(Material.WOOD, MaterialColor.LIGHT_BLUE_TERRACOTTA).sound(SoundType.WOOD)
                        .doesNotBlockMovement().hardnessAndResistance(0.5F)));
        INVERTED_TRAPDOOR = PokecubeLegends.BLOCKS_TAB.register("inverted_trapdoor",
                () -> new ItemGenerator.GenericTrapDoor(Block.Properties.create(Material.WOOD, MaterialColor.LIGHT_BLUE_TERRACOTTA)
                        .sound(SoundType.WOOD).hardnessAndResistance(2.0f, 3.0f).notSolid()));
        INVERTED_DOOR = PokecubeLegends.BLOCKS_TAB.register("inverted_door", () -> new ItemGenerator.GenericDoor(
                Block.Properties.create(Material.WOOD, MaterialColor.LIGHT_BLUE_TERRACOTTA).sound(SoundType.WOOD).hardnessAndResistance(
                        2.0f, 3.0f).notSolid()));

        ULTRA_LEAVEUB02 = PokecubeLegends.BLOCKS_TAB.register("ultra_leave02", () -> new LeavesBlock(Block.Properties.create(
                Material.LEAVES, MaterialColor.FOLIAGE).hardnessAndResistance(1f, 5).sound(SoundType.PLANT).notSolid()));
        ULTRA_LOGUB02 = PokecubeLegends.BLOCKS_TAB.register("ultra_log02", () -> Blocks.createLogBlock(
                MaterialColor.GREEN, MaterialColor.YELLOW));
        TEMPORAL_WOOD = PokecubeLegends.BLOCKS_TAB.register("temporal_wood", () -> Blocks.createLogBlock(
                MaterialColor.GREEN, MaterialColor.YELLOW));
        STRIP_TEMPORAL_LOG = PokecubeLegends.BLOCKS_TAB.register("stripped_temporal_log", () -> Blocks.createLogBlock(
                MaterialColor.LIME, MaterialColor.LIME));
        STRIP_TEMPORAL_WOOD = PokecubeLegends.BLOCKS_TAB.register("stripped_temporal_wood", () -> Blocks.createLogBlock(
                MaterialColor.LIME, MaterialColor.LIME));
        ULTRA_PLANKUB02 = PokecubeLegends.BLOCKS_TAB.register("ultra_plank02", () -> new Block(Block.Properties.create(
        		Material.WOOD, MaterialColor.LIME).hardnessAndResistance(2.0F).sound(SoundType.WOOD)));
        TEMPORAL_STAIRS = PokecubeLegends.BLOCKS_TAB.register("temporal_stairs",
                () -> new ItemGenerator.GenericWoodStairs(Blocks.OAK_STAIRS.getDefaultState(), Block.Properties.create(
                		Material.WOOD, MaterialColor.LIME).hardnessAndResistance(2.0F).sound(SoundType.WOOD)));
        TEMPORAL_SLAB = PokecubeLegends.BLOCKS_TAB.register("temporal_slab", () -> new SlabBlock(Block.Properties.create(
        		Material.WOOD, MaterialColor.LIME).hardnessAndResistance(2.0F).sound(SoundType.WOOD)));
        TEMPORAL_FENCE = PokecubeLegends.BLOCKS_TAB.register("temporal_fence", () -> new FenceBlock(Block.Properties.create(
        		Material.WOOD, MaterialColor.LIME).hardnessAndResistance(2.0F).sound(SoundType.WOOD)));
        TEMPORAL_FENCE_GATE = PokecubeLegends.BLOCKS_TAB.register("temporal_fence_gate", () -> new FenceGateBlock(Block.Properties.create(
        		Material.WOOD, MaterialColor.LIME).hardnessAndResistance(2.0F).sound(SoundType.WOOD)));
        TEMPORAL_PR_PLATE = PokecubeLegends.BLOCKS_TAB.register("temporal_pressure_plate",
                () -> new ItemGenerator.GenericPressurePlate(PressurePlateBlock.Sensitivity.EVERYTHING, Block.Properties
                        .create(Material.WOOD, MaterialColor.LIME).sound(SoundType.WOOD).doesNotBlockMovement().hardnessAndResistance(
                                0.5F)));
        TEMPORAL_BUTTON = PokecubeLegends.BLOCKS_TAB.register("temporal_button",
                () -> new ItemGenerator.GenericWoodButton(Block.Properties.create(Material.WOOD, MaterialColor.LIME).sound(SoundType.WOOD)
                        .doesNotBlockMovement().hardnessAndResistance(0.5F)));
        TEMPORAL_TRAPDOOR = PokecubeLegends.BLOCKS_TAB.register("temporal_trapdoor",
                () -> new ItemGenerator.GenericTrapDoor(Block.Properties.create(Material.WOOD, MaterialColor.LIME)
                        .sound(SoundType.WOOD).hardnessAndResistance(2.0f, 3.0f).notSolid()));
        TEMPORAL_DOOR = PokecubeLegends.BLOCKS_TAB.register("temporal_door", () -> new ItemGenerator.GenericDoor(
                Block.Properties.create(Material.WOOD, MaterialColor.LIME).sound(SoundType.WOOD).hardnessAndResistance(
                        2.0f, 3.0f).notSolid()));

        ULTRA_LEAVEUB03 = PokecubeLegends.BLOCKS_TAB.register("ultra_leave03", () -> new LeavesBlock(Block.Properties.create(
                Material.LEAVES, MaterialColor.YELLOW).hardnessAndResistance(1f, 5).sound(SoundType.PLANT).notSolid()));
        ULTRA_LOGUB03 = PokecubeLegends.BLOCKS_TAB.register("ultra_log03", () -> Blocks.createLogBlock(
                MaterialColor.BROWN, MaterialColor.BROWN));
        AGED_WOOD = PokecubeLegends.BLOCKS_TAB.register("aged_wood", () -> Blocks.createLogBlock(MaterialColor.BROWN,
                MaterialColor.BROWN));
        STRIP_AGED_LOG = PokecubeLegends.BLOCKS_TAB.register("stripped_aged_log", () -> Blocks.createLogBlock(
                MaterialColor.BROWN, MaterialColor.BROWN));
        STRIP_AGED_WOOD = PokecubeLegends.BLOCKS_TAB.register("stripped_aged_wood", () -> Blocks.createLogBlock(
                MaterialColor.BROWN, MaterialColor.BROWN));
        ULTRA_PLANKUB03 = PokecubeLegends.BLOCKS_TAB.register("ultra_plank03", () -> new Block(Block.Properties.create(
        		Material.WOOD, MaterialColor.BROWN).hardnessAndResistance(2.0F).sound(SoundType.WOOD)));
        AGED_STAIRS = PokecubeLegends.BLOCKS_TAB.register("aged_stairs", () -> new ItemGenerator.GenericWoodStairs(
                Blocks.OAK_STAIRS.getDefaultState(), Block.Properties.create(
                		Material.WOOD, MaterialColor.BROWN).hardnessAndResistance(2.0F).sound(SoundType.WOOD)));
        AGED_SLAB = PokecubeLegends.BLOCKS_TAB.register("aged_slab", () -> new SlabBlock(Block.Properties.create(
        		Material.WOOD, MaterialColor.BROWN).hardnessAndResistance(2.0F).sound(SoundType.WOOD)));
        AGED_FENCE = PokecubeLegends.BLOCKS_TAB.register("aged_fence", () -> new FenceBlock(Block.Properties.create(
        		Material.WOOD, MaterialColor.BROWN).hardnessAndResistance(2.0F).sound(SoundType.WOOD)));
        AGED_FENCE_GATE = PokecubeLegends.BLOCKS_TAB.register("aged_fence_gate", () -> new FenceGateBlock(Block.Properties.create(
        		Material.WOOD, MaterialColor.BROWN).hardnessAndResistance(2.0F).sound(SoundType.WOOD)));
        AGED_PR_PLATE = PokecubeLegends.BLOCKS_TAB.register("aged_pressure_plate",
                () -> new ItemGenerator.GenericPressurePlate(PressurePlateBlock.Sensitivity.EVERYTHING, Block.Properties
                        .create(Material.WOOD, MaterialColor.BROWN).sound(SoundType.WOOD).doesNotBlockMovement().hardnessAndResistance(
                                0.5F)));
        AGED_BUTTON = PokecubeLegends.BLOCKS_TAB.register("aged_button", () -> new ItemGenerator.GenericWoodButton(
                Block.Properties.create(Material.WOOD, MaterialColor.BROWN).sound(SoundType.WOOD).doesNotBlockMovement()
                        .hardnessAndResistance(0.5F)));
        AGED_TRAPDOOR = PokecubeLegends.BLOCKS_TAB.register("aged_trapdoor", () -> new ItemGenerator.GenericTrapDoor(
                Block.Properties.create(Material.WOOD, MaterialColor.BROWN).sound(SoundType.WOOD).hardnessAndResistance(
                        2.0f, 3.0f).notSolid()));
        AGED_DOOR = PokecubeLegends.BLOCKS_TAB.register("aged_door", () -> new ItemGenerator.GenericDoor(
                Block.Properties.create(Material.WOOD, MaterialColor.BROWN).sound(SoundType.WOOD).hardnessAndResistance(
                        2.0f, 3.0f).notSolid()));

        DISTORTIC_LEAVE = PokecubeLegends.BLOCKS_TAB.register("distortic_leave", () -> new LeavesBlock(Block.Properties.create(
                Material.LEAVES, MaterialColor.PURPLE).hardnessAndResistance(1f, 5).sound(SoundType.PLANT).notSolid()));
        DISTORTIC_LOG = PokecubeLegends.BLOCKS_TAB.register("distortic_log", () -> Blocks.createLogBlock(
                MaterialColor.BLUE, MaterialColor.BLUE));
        DISTORTIC_WOOD = PokecubeLegends.BLOCKS_TAB.register("distortic_wood", () -> Blocks.createLogBlock(
                MaterialColor.BLUE, MaterialColor.BLUE));
        STRIP_DISTORTIC_LOG = PokecubeLegends.BLOCKS_TAB.register("stripped_distortic_log", () -> Blocks.createLogBlock(
                MaterialColor.BLUE, MaterialColor.BLUE));
        STRIP_DISTORTIC_WOOD = PokecubeLegends.BLOCKS_TAB.register("stripped_distortic_wood", () -> Blocks
                .createLogBlock(MaterialColor.BLUE, MaterialColor.BLUE));
        DISTORTIC_PLANK = PokecubeLegends.BLOCKS_TAB.register("distortic_plank", () -> new Block(Block.Properties.create(
        		Material.WOOD, MaterialColor.BLUE).hardnessAndResistance(2.0F).sound(SoundType.WOOD)));
        DISTORTIC_STAIRS = PokecubeLegends.BLOCKS_TAB.register("distortic_stairs",
                () -> new ItemGenerator.GenericWoodStairs(Blocks.OAK_STAIRS.getDefaultState(), Block.Properties.create(
                		Material.WOOD, MaterialColor.BLUE).hardnessAndResistance(2.0F).sound(SoundType.WOOD)));
        DISTORTIC_SLAB = PokecubeLegends.BLOCKS_TAB.register("distortic_slab", () -> new SlabBlock(Block.Properties.create(
        		Material.WOOD, MaterialColor.BLUE).hardnessAndResistance(2.0F).sound(SoundType.WOOD)));
        DISTORTIC_FENCE = PokecubeLegends.BLOCKS_TAB.register("distortic_fence", () -> new FenceBlock(Block.Properties.create(
        		Material.WOOD, MaterialColor.BLUE).hardnessAndResistance(2.0F).sound(SoundType.WOOD)));
        DISTORTIC_FENCE_GATE = PokecubeLegends.BLOCKS_TAB.register("distortic_fence_gate", () -> new FenceGateBlock(Block.Properties.create(
        		Material.WOOD, MaterialColor.BLUE).hardnessAndResistance(2.0F).sound(SoundType.WOOD)));
        DISTORTIC_PR_PLATE = PokecubeLegends.BLOCKS_TAB.register("distortic_pressure_plate",
                () -> new ItemGenerator.GenericPressurePlate(PressurePlateBlock.Sensitivity.EVERYTHING, Block.Properties
                        .create(Material.WOOD, MaterialColor.BLUE).sound(SoundType.WOOD).doesNotBlockMovement().hardnessAndResistance(
                                0.5F)));
        DISTORTIC_BUTTON = PokecubeLegends.BLOCKS_TAB.register("distortic_button",
                () -> new ItemGenerator.GenericWoodButton(Block.Properties.create(Material.WOOD, MaterialColor.BLUE).sound(SoundType.WOOD)
                        .doesNotBlockMovement().hardnessAndResistance(0.5F)));
        DISTORTIC_TRAPDOOR = PokecubeLegends.BLOCKS_TAB.register("distortic_trapdoor",
                () -> new ItemGenerator.GenericTrapDoor(Block.Properties.create(Material.WOOD, MaterialColor.BLUE)
                        .sound(SoundType.WOOD).hardnessAndResistance(2.0f, 3.0f).notSolid()));
        DISTORTIC_DOOR = PokecubeLegends.BLOCKS_TAB.register("distortic_door", () -> new ItemGenerator.GenericDoor(
                Block.Properties.create(Material.WOOD, MaterialColor.BLUE).sound(SoundType.WOOD).hardnessAndResistance(
                        2.0f, 3.0f).notSolid()));
        
        // Mirage Spot (Hoopa Ring)
        BLOCK_PORTALWARP = PokecubeLegends.BLOCKS.register("portal", () -> new PortalWarp("portal", Block.Properties
                .create(Material.ROCK, MaterialColor.GOLD).sound(SoundType.METAL).hardnessAndResistance(2000, 2000)).setShape(VoxelShapes
                        .create(0.05, 0, 0.05, 1, 3, 1)).setInfoBlockName("portalwarp"));

        // Legendary Spawns
        GOLEM_STONE = PokecubeLegends.BLOCKS.register("golem_stone", () -> new BlockBase("golem_stone", Material.ROCK, 
        		MaterialColor.WHITE_TERRACOTTA, 5f, SoundType.STONE, ToolType.PICKAXE, 2).noInfoBlock());

        LEGENDARY_SPAWN = PokecubeLegends.BLOCKS.register("legendaryspawn", () -> new LegendaryBlock("legendaryspawn",
                Material.IRON, MaterialColor.GOLD).noInfoBlock());
        TROUGH_BLOCK = PokecubeLegends.BLOCKS.register("trough_block", () -> new TroughBlock("trough_block",
                Block.Properties.create(Material.IRON, MaterialColor.BROWN).hardnessAndResistance(5, 15).harvestTool(ToolType.PICKAXE)
                        .harvestLevel(2).sound(SoundType.ANVIL).setLightLevel(b -> 4).variableOpacity()).noInfoBlock());
        HEATRAN_BLOCK 	= PokecubeLegends.BLOCKS.register("heatran_block", () -> new HeatranBlock("heatran_block",
        		Block.Properties.create(Material.ROCK, MaterialColor.NETHERRACK).hardnessAndResistance(5, 15).harvestTool(ToolType.PICKAXE)
                .harvestLevel(2).sound(SoundType.NETHER_BRICK).setLightLevel(b -> 4).variableOpacity().setEmmisiveRendering((s, r, p) -> true)).noInfoBlock());
        TAO_BLOCK 	= PokecubeLegends.BLOCKS.register("blackwhite_block", () -> new TaoTrioBlock("blackwhite_block",
        		Block.Properties.create(Material.ANVIL, MaterialColor.SNOW).hardnessAndResistance(5, 15).sound(SoundType.FUNGUS)
        				.variableOpacity()).setShape(VoxelShapes.create(0.05, 0, 0.05, 1, 1, 1)).noInfoBlock());
        
        // Regi Cores
        REGISTEEL_CORE = PokecubeLegends.BLOCKS.register("registeel_spawn", () -> new Registeel_Core("registeel_spawn",
                Material.IRON, MaterialColor.WHITE_TERRACOTTA, 15, SoundType.METAL, ToolType.PICKAXE, 2).noInfoBlock());
        REGICE_CORE = PokecubeLegends.BLOCKS.register("regice_spawn", () -> new Regice_Core("regice_spawn",
                Material.PACKED_ICE, MaterialColor.WHITE_TERRACOTTA, 15, SoundType.GLASS, ToolType.PICKAXE, 2).noInfoBlock());
        REGIROCK_CORE = PokecubeLegends.BLOCKS.register("regirock_spawn", () -> new Regirock_Core("regirock_spawn",
                Material.ROCK, MaterialColor.WHITE_TERRACOTTA, 15, SoundType.STONE, ToolType.PICKAXE, 2).noInfoBlock());
        REGIELEKI_CORE = PokecubeLegends.BLOCKS.register("regieleki_spawn", () -> new Regieleki_Core("regieleki_spawn",
                Material.ROCK, MaterialColor.WHITE_TERRACOTTA, 15, SoundType.STONE, ToolType.PICKAXE, 2).noInfoBlock());
        REGIDRAGO_CORE = PokecubeLegends.BLOCKS.register("regidrago_spawn", () -> new Regidrago_Core("regidrago_spawn",
                Material.ROCK, MaterialColor.WHITE_TERRACOTTA, 15, SoundType.STONE, ToolType.PICKAXE, 2).noInfoBlock());
        REGIGIGA_CORE = PokecubeLegends.BLOCKS.register("regigiga_spawn", () -> new Regigigas_Core("regigiga_spawn",
                Material.IRON, MaterialColor.WHITE_TERRACOTTA, 15, SoundType.METAL, ToolType.PICKAXE, 2).noInfoBlock());
        //
        
        // Tapus
        TAPU_KOKO_CORE 	= PokecubeLegends.BLOCKS.register("koko_core", () -> new TapuKokoCore("koko_core",
        		Block.Properties.create(Material.ROCK, MaterialColor.YELLOW_TERRACOTTA).hardnessAndResistance(5, 15)
        		.sound(SoundType.BASALT).variableOpacity()).noInfoBlock());
        TAPU_BULU_CORE 	= PokecubeLegends.BLOCKS.register("bulu_core", () -> new TapuBuluCore("bulu_core",
        		Block.Properties.create(Material.ROCK, MaterialColor.RED_TERRACOTTA).hardnessAndResistance(5, 15)
        		.sound(SoundType.BASALT).variableOpacity()).noInfoBlock());
        TAPU_LELE_CORE 	= PokecubeLegends.BLOCKS.register("lele_core", () -> new TapuLeleCore("lele_core",
        		Block.Properties.create(Material.ROCK, MaterialColor.PURPLE_TERRACOTTA).hardnessAndResistance(5, 15)
        		.sound(SoundType.BASALT).variableOpacity()).noInfoBlock());
        TAPU_FINI_CORE 	= PokecubeLegends.BLOCKS.register("fini_core", () -> new TapuFiniCore("fini_core",
        		Block.Properties.create(Material.ROCK, MaterialColor.PINK_TERRACOTTA).hardnessAndResistance(5, 15)
        		.sound(SoundType.BASALT).variableOpacity()).noInfoBlock());
        //
        
        TIMESPACE_CORE = PokecubeLegends.BLOCKS.register("timerspawn", () -> new SpaceCoreBlock("timerspawn",
                Block.Properties.create(Material.ORGANIC, MaterialColor.STONE).hardnessAndResistance(2000, 2000).sound(SoundType.STONE)
                        .variableOpacity()).setShape(VoxelShapes.create(0.05, 0, 0.05, 1, 2, 1)).noInfoBlock());
        NATURE_CORE = PokecubeLegends.BLOCKS.register("naturespawn", () -> new NatureCoreBlock("naturespawn",
                Block.Properties.create(Material.ROCK, MaterialColor.WHITE_TERRACOTTA).hardnessAndResistance(2000, 2000).sound(SoundType.STONE)
                        .variableOpacity()).setShape(VoxelShapes.create(0.05, 0, 0.05, 1, 2, 1)).noInfoBlock());
        KELDEO_CORE = PokecubeLegends.BLOCKS.register("keldeoblock", () -> new KeldeoBlock("keldeoblock",
                Block.Properties.create(Material.ROCK, MaterialColor.BLUE).hardnessAndResistance(2000, 2000).sound(SoundType.STONE)
                        .variableOpacity()).setShape(VoxelShapes.create(0.05, 0, 0.05, 1, 1, 1)).noInfoBlock());
        VICTINI_CORE = PokecubeLegends.BLOCKS.register("victiniblock", () -> new VictiniBlock("victiniblock",
                Block.Properties.create(Material.IRON, MaterialColor.GOLD).hardnessAndResistance(5, 15).harvestTool(ToolType.PICKAXE)
                        .harvestLevel(2).sound(SoundType.ANVIL).variableOpacity()).setShape(VoxelShapes.create(0.05, 0,
                                0.05, 1, 1, 1)).noInfoBlock());
        YVELTAL_CORE = PokecubeLegends.BLOCKS.register("yveltal_egg", () -> new YveltalEgg("yveltal_egg",
                Block.Properties.create(Material.IRON, MaterialColor.BLACK).hardnessAndResistance(2000, 2000).sound(SoundType.WOOD)
                        .variableOpacity()).setShape(VoxelShapes.create(0.05, 0, 0.05, 1, 2, 1)).noInfoBlock());
        XERNEAS_CORE = PokecubeLegends.BLOCKS.register("xerneas_tree", () -> new XerneasCore("xerneas_tree",
                Block.Properties.create(Material.IRON, MaterialColor.SNOW).hardnessAndResistance(2000, 2000).sound(SoundType.WOOD)
                        .variableOpacity()).setShape(VoxelShapes.create(0.05, 0, 0.05, 1, 2, 1)).noInfoBlock());

        // Ores
        RUBY_ORE = PokecubeLegends.DECORATION_TAB.register("ruby_ore", () -> new BlockBase("ruby_ore", Block.Properties.create(
                Material.ROCK, MaterialColor.STONE).sound(SoundType.STONE).hardnessAndResistance(5, 15).harvestTool(ToolType.PICKAXE)
                .harvestLevel(2)).noInfoBlock());
        SAPPHIRE_ORE = PokecubeLegends.DECORATION_TAB.register("sapphire_ore", () -> new BlockBase("sapphire_ore",
                Block.Properties.create(Material.ROCK, MaterialColor.STONE).sound(SoundType.STONE).hardnessAndResistance(5, 15).harvestTool(
                        ToolType.PICKAXE).harvestLevel(2)).noInfoBlock());
        RUBY_BLOCK = PokecubeLegends.DECORATION_TAB.register("ruby_block", () -> new Block(Block.Properties.create(
                Material.IRON, MaterialColor.RED).hardnessAndResistance(1.5f, 10).sound(SoundType.METAL).harvestTool(ToolType.PICKAXE)));
        SAPPHIRE_BLOCK = PokecubeLegends.DECORATION_TAB.register("sapphire_block", () -> new Block(Block.Properties.create(
                Material.IRON, MaterialColor.BLUE).hardnessAndResistance(1.5f, 10).sound(SoundType.METAL).harvestTool(ToolType.PICKAXE)));
        SPECTRUM_ORE = PokecubeLegends.BLOCKS_TAB.register("spectrum_ore", () -> new BlockBase("spectrum_ore",
                Block.Properties.create(Material.ROCK, MaterialColor.CYAN_TERRACOTTA).sound(SoundType.STONE).hardnessAndResistance(5, 15).harvestTool(
                        ToolType.PICKAXE).harvestLevel(2)).noInfoBlock());
        SPECTRUM_BLOCK = PokecubeLegends.BLOCKS_TAB.register("spectrum_block", () -> new Block(Block.Properties.create(
                Material.IRON, MaterialColor.ADOBE).hardnessAndResistance(5.0f, 7).sound(SoundType.METAL).setLightLevel(b -> 4).harvestTool(
                        ToolType.PICKAXE)));

        // Meteor Ore
        COSMIC_DUST_ORE = PokecubeLegends.BLOCKS_TAB.register("cosmic_dust_ore", () -> new FallingBlock(Block.Properties
                .create(Material.ROCK, MaterialColor.BLUE_TERRACOTTA).sound(SoundType.STONE).hardnessAndResistance(5, 15).harvestTool(ToolType.PICKAXE)
                .harvestLevel(2)));

    }

    public static void init()
    {
        PlantsInit.registry();

        for (final RegistryObject<Block> reg : PokecubeLegends.BLOCKS.getEntries())
            PokecubeLegends.ITEMS.register(reg.getId().getPath(), () -> new BlockItem(reg.get(), new Item.Properties()
                    .group(PokecubeItems.POKECUBEBLOCKS)));

        for (final RegistryObject<Block> reg : PokecubeLegends.BLOCKS_TAB.getEntries())
        {
            // These are registered separately, so skip them.
            if (reg == BlockInit.ULTRA_TORCH1 || reg == BlockInit.ULTRA_TORCH1_WALL) continue;
            PokecubeLegends.ITEMS.register(reg.getId().getPath(), () -> new BlockItem(reg.get(), new Item.Properties()
                    .group(PokecubeLegends.TAB)));
        }
        
        for (final RegistryObject<Block> reg : PokecubeLegends.DECORATION_TAB.getEntries())
            PokecubeLegends.ITEMS.register(reg.getId().getPath(), () -> new BlockItem(reg.get(), new Item.Properties()
                    .group(PokecubeLegends.DECO_TAB)));
    }

    public static void strippableBlocks(final FMLLoadCompleteEvent event)
    {
        // Enqueue this so that it runs on main thread, to prevent concurrency
        // issues.
        event.enqueueWork(() ->
        {
            ItemGenerator.addStrippable(BlockInit.ULTRA_LOGUB03.get(), BlockInit.STRIP_AGED_LOG.get());
            ItemGenerator.addStrippable(BlockInit.AGED_WOOD.get(), BlockInit.STRIP_AGED_WOOD.get());
            ItemGenerator.addStrippable(BlockInit.DISTORTIC_LOG.get(), BlockInit.STRIP_DISTORTIC_LOG.get());
            ItemGenerator.addStrippable(BlockInit.DISTORTIC_WOOD.get(), BlockInit.STRIP_DISTORTIC_WOOD.get());
            ItemGenerator.addStrippable(BlockInit.ULTRA_LOGUB01.get(), BlockInit.STRIP_INVERTED_LOG.get());
            ItemGenerator.addStrippable(BlockInit.INVERTED_WOOD.get(), BlockInit.STRIP_INVERTED_WOOD.get());
            ItemGenerator.addStrippable(BlockInit.ULTRA_LOGUB02.get(), BlockInit.STRIP_TEMPORAL_LOG.get());
            ItemGenerator.addStrippable(BlockInit.TEMPORAL_WOOD.get(), BlockInit.STRIP_TEMPORAL_WOOD.get());
        });
    }
}
