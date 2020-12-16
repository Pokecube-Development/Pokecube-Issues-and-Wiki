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
import pokecube.legends.blocks.customblocks.TroughBlock;
import pokecube.legends.blocks.customblocks.VictiniBlock;
import pokecube.legends.blocks.customblocks.XerneasCore;
import pokecube.legends.blocks.customblocks.YveltalEgg;
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
        RAID_SPAWN = PokecubeLegends.BLOCKS.register("raidspawn_block", () -> new RaidSpawnBlock(Material.IRON)
                .setInfoBlockName("raidspawn"));

        // Decorative_Blocks
        DYNA_LEAVE1 = PokecubeLegends.BLOCKS.register("dyna_leave_1", () -> new LeavesBlock(Block.Properties.create(
                Material.LEAVES).hardnessAndResistance(1f, 5).sound(SoundType.WET_GRASS).noDrops().notSolid()));
        DYNA_LEAVE2 = PokecubeLegends.BLOCKS.register("dyna_leave_2", () -> new LeavesBlock(Block.Properties.create(
                Material.LEAVES).hardnessAndResistance(1f, 5).sound(SoundType.WET_GRASS).noDrops().notSolid()));

        OCEAN_BRICK = PokecubeLegends.BLOCKS.register("oceanbrick", () -> new Block(Block.Properties.create(
                Material.ROCK).hardnessAndResistance(1.5f, 10).sound(SoundType.STONE)));
        SKY_BRICK   = PokecubeLegends.BLOCKS.register("skybrick", () -> new Block(Block.Properties.create(Material.ROCK)
                .hardnessAndResistance(1.5f, 10).sound(SoundType.STONE)));
        SPATIAN_BRICK = PokecubeLegends.BLOCKS.register("spatianbrick", () -> new Block(Block.Properties.create(
                Material.ROCK).hardnessAndResistance(1.5f, 10).sound(SoundType.STONE)));
        MAGMA_BRICK   = PokecubeLegends.BLOCKS.register("magmabrick", () -> new MagmaBlock(Block.Properties.create(
                Material.ROCK).hardnessAndResistance(1.5f, 10).sound(SoundType.NETHERRACK).setLightLevel(b -> 3)
        			.setEmmisiveRendering((s, r, p) -> true)));
        DARKSKY_BRICK = PokecubeLegends.BLOCKS.register("darkskybrick", () -> new Block(Block.Properties.create(
                Material.ROCK).hardnessAndResistance(1.5f, 10).sound(SoundType.STONE)));

        // METEOR_BLOCK = PokecubeLegends.BLOCKS_TAB.register("meteor_block", ()
        // -> new BlockBase("meteor_block",
        // Material.GOURD, 2.5F, SoundType.METAL, ToolType.PICKAXE,
        // 2).noInfoBlock());
        METEOR_BLOCK = PokecubeLegends.BLOCKS_TAB.register("meteor_block", () -> new FallingBlock(Block.Properties
                .create(Material.GOURD).hardnessAndResistance(2.5f).sound(SoundType.METAL).harvestTool(ToolType.PICKAXE)
                .harvestLevel(2)));

        CRYSTAL_BRICK = PokecubeLegends.BLOCKS_TAB.register("crystalbrick", () -> new BlockBase("crystalbrick",
                Material.PACKED_ICE, 0.5F, SoundType.GLASS, ToolType.PICKAXE, 1).noInfoBlock());

        // Dimensions
        SPECTRUM_GLASS = PokecubeLegends.BLOCKS_TAB.register("spectrum_glass", () -> new SpectrumGlass("spectrum_glass",
                Block.Properties.from(Blocks.GLASS).notSolid()));
        ULTRA_DIRTAGED = PokecubeLegends.BLOCKS_TAB.register("ultradirt3", () -> new BlockBase("ultradirt3",
                Material.ORGANIC, 0.5f, SoundType.WET_GRASS, ToolType.SHOVEL, 1).noInfoBlock());
        ULTRA_ROCKDISTOR = PokecubeLegends.BLOCKS_TAB.register("ultradirt4", () -> new BlockBase("ultradirt4",
                Material.ROCK, 0.9f, SoundType.METAL, ToolType.PICKAXE, 1).noInfoBlock());
        ULTRA_MAGNETIC = PokecubeLegends.BLOCKS_TAB.register("ultramagnetic", () -> new MagneticBlock("ultramagnetic",
                Material.GLASS).noInfoBlock());
        ULTRA_SANDSTONE = PokecubeLegends.BLOCKS_TAB.register("ultrasandstone", () -> new BlockBase("ultrasandstone",
                Material.SAND, 0.5f, SoundType.STONE, ToolType.PICKAXE, 1).noInfoBlock());
        ULTRA_DARKSTONE = PokecubeLegends.BLOCKS_TAB.register("ultracobbles", () -> new DarkStoneBlock("ultracobbles",
                Material.ROCK).noInfoBlock());
        ULTRA_DARKCOBBLES = PokecubeLegends.BLOCKS_TAB.register("ultrarock", () -> new BlockBase("ultrarock",
                Material.ROCK, 0.8f, SoundType.STONE, ToolType.PICKAXE, 2).noInfoBlock());
        ULTRA_GRASSMUSS = PokecubeLegends.BLOCKS_TAB.register("ultragrass1", () -> new GrassMussBlock("ultragrass1",
                Material.ORGANIC).noInfoBlock());
        ULTRA_DIRTMUSS = PokecubeLegends.BLOCKS_TAB.register("ultradirt1", () -> new BlockBase("ultradirt1",
                Material.CLAY, 0.5f, SoundType.GROUND, ToolType.SHOVEL, 1).noInfoBlock());
        ULTRA_GRASSJUN = PokecubeLegends.BLOCKS_TAB.register("ultragrass2", () -> new GrassJungleBlock("ultragrass2",
                Material.ORGANIC).noInfoBlock());
        ULTRA_DIRTJUN = PokecubeLegends.BLOCKS_TAB.register("ultradirt2", () -> new BlockBase("ultradirt2",
                Material.GOURD, 0.5f, SoundType.GROUND, ToolType.SHOVEL, 1).noInfoBlock());
        ULTRA_STONE = PokecubeLegends.BLOCKS_TAB.register("ultrastone", () -> new BlockBase("ultrastone", Material.ROCK,
                1.5f, SoundType.STONE, ToolType.PICKAXE, 2).noInfoBlock());
        ULTRA_METAL = PokecubeLegends.BLOCKS_TAB.register("ultrablock", () -> new BlockBase("ultrablock", Material.IRON,
                5.0f, 10f, SoundType.STONE, ToolType.PICKAXE, 2).noInfoBlock());
        ULTRA_SAND = PokecubeLegends.BLOCKS_TAB.register("ultrasand", () -> new SandUltraBlock("ultrasand",
                Material.SAND).noInfoBlock());
        ULTRA_SANDDISTOR = PokecubeLegends.BLOCKS_TAB.register("ultrasand1", () -> new SandDistorBlock("ultrasand1",
                Material.CLAY).noInfoBlock());
        ULTRA_GRASSAGED = PokecubeLegends.BLOCKS_TAB.register("ultragrass3", () -> new GrassAgedBlock("ultragrass3",
                Material.GOURD).noInfoBlock());
        TEMPORAL_CRYSTAL = PokecubeLegends.BLOCKS_TAB.register("temporal_crystal", () -> new BlockBase(
                "temporal_crystal", Material.GLASS, 1.5f, SoundType.GLASS, ToolType.PICKAXE, 1).noInfoBlock());

        // Distortic World
        DISTORTIC_GRASS = PokecubeLegends.BLOCKS_TAB.register("distortic_grass", () -> new GrassDistorticBlock(
                BlockBase.Properties.create(Material.ORGANIC).sound(SoundType.NETHER_WART).hardnessAndResistance(1, 2)
                        .harvestTool(ToolType.SHOVEL).harvestLevel(1)));
        DISTORTIC_STONE = PokecubeLegends.BLOCKS_TAB.register("distortic_stone", () -> new BlockBase("distortic_stone",
                Material.ROCK, 1.5f, SoundType.STONE, ToolType.PICKAXE, 2).noInfoBlock());
        DISTORTIC_MIRROR = PokecubeLegends.BLOCKS_TAB.register("distortic_mirror", () -> new BlockBase(
                "distortic_mirror", Material.GLASS, 2.5f, SoundType.GLASS, ToolType.PICKAXE, 2).noInfoBlock());

        // Torches
        ULTRA_TORCH1 = PokecubeLegends.BLOCKS_TAB.register("ultra_torch1", () -> new UltraTorch1());
        ULTRA_TORCH1_WALL = PokecubeLegends.BLOCKS_TAB.register("ultra_torch1_wall", () -> new UltraTorch1Wall());

        // Plants
        ULTRA_SAPLING_UB01 = PokecubeLegends.BLOCKS_TAB.register("ultra_sapling01", () -> new SaplingBase(
                () -> new Ultra_Tree01(), Block.Properties.from(Blocks.OAK_SAPLING)));
        ULTRA_SAPLING_UB02 = PokecubeLegends.BLOCKS_TAB.register("ultra_sapling02", () -> new SaplingBase(
                () -> new Ultra_Tree02(), Block.Properties.from(Blocks.OAK_SAPLING)));
        ULTRA_SAPLING_UB03 = PokecubeLegends.BLOCKS_TAB.register("ultra_sapling03", () -> new SaplingBase(
                () -> new Ultra_Tree03(), Block.Properties.from(Blocks.OAK_SAPLING)));
        DISTORTIC_SAPLING = PokecubeLegends.BLOCKS_TAB.register("distortic_sapling", () -> new SaplingBase(
                () -> new Distortic_Tree(), Block.Properties.from(Blocks.OAK_SAPLING)));

        // Woods (LOGS/LEAVES/PLANKS)
        ULTRA_LEAVEUB01 = PokecubeLegends.BLOCKS_TAB.register("ultra_leave01", () -> new LeavesBlock(Block.Properties
                .from(Blocks.OAK_LEAVES).notSolid()));
        ULTRA_LOGUB01 = PokecubeLegends.BLOCKS_TAB.register("ultra_log01", () -> Blocks.createLogBlock(
                MaterialColor.PURPLE, MaterialColor.BLUE_TERRACOTTA));
        INVERTED_WOOD = PokecubeLegends.BLOCKS_TAB.register("inverted_wood", () -> Blocks.createLogBlock(
                MaterialColor.PURPLE, MaterialColor.BLUE_TERRACOTTA));
        STRIP_INVERTED_LOG = PokecubeLegends.BLOCKS_TAB.register("stripped_inverted_log", () -> Blocks.createLogBlock(
                MaterialColor.BLUE_TERRACOTTA, MaterialColor.BLUE_TERRACOTTA));
        STRIP_INVERTED_WOOD = PokecubeLegends.BLOCKS_TAB.register("stripped_inverted_wood", () -> Blocks.createLogBlock(
                MaterialColor.BLUE_TERRACOTTA, MaterialColor.BLUE_TERRACOTTA));
        ULTRA_PLANKUB01 = PokecubeLegends.BLOCKS_TAB.register("ultra_plank01", () -> new Block(Block.Properties.from(
                Blocks.OAK_PLANKS)));
        INVERTED_STAIRS = PokecubeLegends.BLOCKS_TAB.register("inverted_stairs",
                () -> new ItemGenerator.GenericWoodStairs(Blocks.OAK_STAIRS.getDefaultState(), Block.Properties.from(
                        Blocks.OAK_PLANKS).sound(SoundType.WOOD).hardnessAndResistance(2.0f, 3.0f)));
        INVERTED_SLAB = PokecubeLegends.BLOCKS_TAB.register("inverted_slab", () -> new SlabBlock(Block.Properties.from(
                Blocks.OAK_SLAB)));
        INVERTED_FENCE = PokecubeLegends.BLOCKS_TAB.register("inverted_fence", () -> new FenceBlock(Block.Properties
                .from(Blocks.OAK_FENCE)));
        INVERTED_FENCE_GATE = PokecubeLegends.BLOCKS_TAB.register("inverted_fence_gate", () -> new FenceGateBlock(
                Block.Properties.from(Blocks.OAK_FENCE_GATE)));
        INVERTED_PR_PLATE = PokecubeLegends.BLOCKS_TAB.register("inverted_pressure_plate",
                () -> new ItemGenerator.GenericPressurePlate(PressurePlateBlock.Sensitivity.EVERYTHING, Block.Properties
                        .create(Material.WOOD).sound(SoundType.WOOD).doesNotBlockMovement().hardnessAndResistance(
                                0.5F)));
        INVERTED_BUTTON = PokecubeLegends.BLOCKS_TAB.register("inverted_button",
                () -> new ItemGenerator.GenericWoodButton(Block.Properties.create(Material.WOOD).sound(SoundType.WOOD)
                        .doesNotBlockMovement().hardnessAndResistance(0.5F)));
        INVERTED_TRAPDOOR = PokecubeLegends.BLOCKS_TAB.register("inverted_trapdoor",
                () -> new ItemGenerator.GenericTrapDoor(Block.Properties.create(Material.WOOD, MaterialColor.WOOD)
                        .sound(SoundType.WOOD).hardnessAndResistance(2.0f, 3.0f).notSolid()));
        INVERTED_DOOR = PokecubeLegends.BLOCKS_TAB.register("inverted_door", () -> new ItemGenerator.GenericDoor(
                Block.Properties.create(Material.WOOD, MaterialColor.WOOD).sound(SoundType.WOOD).hardnessAndResistance(
                        2.0f, 3.0f).notSolid()));

        ULTRA_LEAVEUB02 = PokecubeLegends.BLOCKS_TAB.register("ultra_leave02", () -> new LeavesBlock(Block.Properties
                .from(Blocks.OAK_LEAVES).notSolid()));
        ULTRA_LOGUB02 = PokecubeLegends.BLOCKS_TAB.register("ultra_log02", () -> Blocks.createLogBlock(
                MaterialColor.GREEN, MaterialColor.YELLOW));
        TEMPORAL_WOOD = PokecubeLegends.BLOCKS_TAB.register("temporal_wood", () -> Blocks.createLogBlock(
                MaterialColor.GREEN, MaterialColor.YELLOW));
        STRIP_TEMPORAL_LOG = PokecubeLegends.BLOCKS_TAB.register("stripped_temporal_log", () -> Blocks.createLogBlock(
                MaterialColor.LIME, MaterialColor.LIME));
        STRIP_TEMPORAL_WOOD = PokecubeLegends.BLOCKS_TAB.register("stripped_temporal_wood", () -> Blocks.createLogBlock(
                MaterialColor.LIME, MaterialColor.LIME));
        ULTRA_PLANKUB02 = PokecubeLegends.BLOCKS_TAB.register("ultra_plank02", () -> new Block(Block.Properties.from(
                Blocks.OAK_PLANKS)));
        TEMPORAL_STAIRS = PokecubeLegends.BLOCKS_TAB.register("temporal_stairs",
                () -> new ItemGenerator.GenericWoodStairs(Blocks.OAK_STAIRS.getDefaultState(), Block.Properties.from(
                        Blocks.OAK_PLANKS).sound(SoundType.WOOD).hardnessAndResistance(2.0f, 3.0f)));
        TEMPORAL_SLAB = PokecubeLegends.BLOCKS_TAB.register("temporal_slab", () -> new SlabBlock(Block.Properties.from(
                Blocks.OAK_SLAB)));
        TEMPORAL_FENCE = PokecubeLegends.BLOCKS_TAB.register("temporal_fence", () -> new FenceBlock(Block.Properties
                .from(Blocks.OAK_FENCE)));
        TEMPORAL_FENCE_GATE = PokecubeLegends.BLOCKS_TAB.register("temporal_fence_gate", () -> new FenceGateBlock(
                Block.Properties.from(Blocks.OAK_FENCE_GATE)));
        TEMPORAL_PR_PLATE = PokecubeLegends.BLOCKS_TAB.register("temporal_pressure_plate",
                () -> new ItemGenerator.GenericPressurePlate(PressurePlateBlock.Sensitivity.EVERYTHING, Block.Properties
                        .create(Material.WOOD).sound(SoundType.WOOD).doesNotBlockMovement().hardnessAndResistance(
                                0.5F)));
        TEMPORAL_BUTTON = PokecubeLegends.BLOCKS_TAB.register("temporal_button",
                () -> new ItemGenerator.GenericWoodButton(Block.Properties.create(Material.WOOD).sound(SoundType.WOOD)
                        .doesNotBlockMovement().hardnessAndResistance(0.5F)));
        TEMPORAL_TRAPDOOR = PokecubeLegends.BLOCKS_TAB.register("temporal_trapdoor",
                () -> new ItemGenerator.GenericTrapDoor(Block.Properties.create(Material.WOOD, MaterialColor.WOOD)
                        .sound(SoundType.WOOD).hardnessAndResistance(2.0f, 3.0f).notSolid()));
        TEMPORAL_DOOR = PokecubeLegends.BLOCKS_TAB.register("temporal_door", () -> new ItemGenerator.GenericDoor(
                Block.Properties.create(Material.WOOD, MaterialColor.WOOD).sound(SoundType.WOOD).hardnessAndResistance(
                        2.0f, 3.0f).notSolid()));

        ULTRA_LEAVEUB03 = PokecubeLegends.BLOCKS_TAB.register("ultra_leave03", () -> new LeavesBlock(Block.Properties
                .from(Blocks.OAK_LEAVES).notSolid()));
        ULTRA_LOGUB03 = PokecubeLegends.BLOCKS_TAB.register("ultra_log03", () -> Blocks.createLogBlock(
                MaterialColor.GOLD, MaterialColor.BROWN));
        AGED_WOOD = PokecubeLegends.BLOCKS_TAB.register("aged_wood", () -> Blocks.createLogBlock(MaterialColor.GOLD,
                MaterialColor.BROWN));
        STRIP_AGED_LOG = PokecubeLegends.BLOCKS_TAB.register("stripped_aged_log", () -> Blocks.createLogBlock(
                MaterialColor.GOLD, MaterialColor.BROWN));
        STRIP_AGED_WOOD = PokecubeLegends.BLOCKS_TAB.register("stripped_aged_wood", () -> Blocks.createLogBlock(
                MaterialColor.GOLD, MaterialColor.BROWN));
        ULTRA_PLANKUB03 = PokecubeLegends.BLOCKS_TAB.register("ultra_plank03", () -> new Block(Block.Properties.from(
                Blocks.OAK_PLANKS)));
        AGED_STAIRS = PokecubeLegends.BLOCKS_TAB.register("aged_stairs", () -> new ItemGenerator.GenericWoodStairs(
                Blocks.OAK_STAIRS.getDefaultState(), Block.Properties.from(Blocks.OAK_PLANKS).sound(SoundType.WOOD)
                        .hardnessAndResistance(2.0f, 3.0f)));
        AGED_SLAB = PokecubeLegends.BLOCKS_TAB.register("aged_slab", () -> new SlabBlock(Block.Properties.from(
                Blocks.OAK_SLAB)));
        AGED_FENCE = PokecubeLegends.BLOCKS_TAB.register("aged_fence", () -> new FenceBlock(Block.Properties.from(
                Blocks.OAK_FENCE)));
        AGED_FENCE_GATE = PokecubeLegends.BLOCKS_TAB.register("aged_fence_gate", () -> new FenceGateBlock(
                Block.Properties.from(Blocks.OAK_FENCE_GATE)));
        AGED_PR_PLATE = PokecubeLegends.BLOCKS_TAB.register("aged_pressure_plate",
                () -> new ItemGenerator.GenericPressurePlate(PressurePlateBlock.Sensitivity.EVERYTHING, Block.Properties
                        .create(Material.WOOD).sound(SoundType.WOOD).doesNotBlockMovement().hardnessAndResistance(
                                0.5F)));
        AGED_BUTTON = PokecubeLegends.BLOCKS_TAB.register("aged_button", () -> new ItemGenerator.GenericWoodButton(
                Block.Properties.create(Material.WOOD).sound(SoundType.WOOD).doesNotBlockMovement()
                        .hardnessAndResistance(0.5F)));
        AGED_TRAPDOOR = PokecubeLegends.BLOCKS_TAB.register("aged_trapdoor", () -> new ItemGenerator.GenericTrapDoor(
                Block.Properties.create(Material.WOOD, MaterialColor.WOOD).sound(SoundType.WOOD).hardnessAndResistance(
                        2.0f, 3.0f).notSolid()));
        AGED_DOOR = PokecubeLegends.BLOCKS_TAB.register("aged_door", () -> new ItemGenerator.GenericDoor(
                Block.Properties.create(Material.WOOD, MaterialColor.WOOD).sound(SoundType.WOOD).hardnessAndResistance(
                        2.0f, 3.0f).notSolid()));

        DISTORTIC_LEAVE = PokecubeLegends.BLOCKS_TAB.register("distortic_leave", () -> new LeavesBlock(Block.Properties
                .from(Blocks.JUNGLE_LEAVES).notSolid()));
        DISTORTIC_LOG = PokecubeLegends.BLOCKS_TAB.register("distortic_log", () -> Blocks.createLogBlock(
                MaterialColor.PURPLE, MaterialColor.BLUE));
        DISTORTIC_WOOD = PokecubeLegends.BLOCKS_TAB.register("distortic_wood", () -> Blocks.createLogBlock(
                MaterialColor.PURPLE, MaterialColor.BLUE));
        STRIP_DISTORTIC_LOG = PokecubeLegends.BLOCKS_TAB.register("stripped_distortic_log", () -> Blocks.createLogBlock(
                MaterialColor.PURPLE, MaterialColor.BLUE));
        STRIP_DISTORTIC_WOOD = PokecubeLegends.BLOCKS_TAB.register("stripped_distortic_wood", () -> Blocks
                .createLogBlock(MaterialColor.PURPLE, MaterialColor.BLUE));
        DISTORTIC_PLANK = PokecubeLegends.BLOCKS_TAB.register("distortic_plank", () -> new Block(Block.Properties.from(
                Blocks.OAK_PLANKS)));
        DISTORTIC_STAIRS = PokecubeLegends.BLOCKS_TAB.register("distortic_stairs",
                () -> new ItemGenerator.GenericWoodStairs(Blocks.OAK_STAIRS.getDefaultState(), Block.Properties.from(
                        Blocks.OAK_PLANKS).sound(SoundType.WOOD).hardnessAndResistance(2.0f, 3.0f)));
        DISTORTIC_SLAB = PokecubeLegends.BLOCKS_TAB.register("distortic_slab", () -> new SlabBlock(Block.Properties
                .from(Blocks.OAK_SLAB)));
        DISTORTIC_FENCE = PokecubeLegends.BLOCKS_TAB.register("distortic_fence", () -> new FenceBlock(Block.Properties
                .from(Blocks.OAK_FENCE)));
        DISTORTIC_FENCE_GATE = PokecubeLegends.BLOCKS_TAB.register("distortic_fence_gate", () -> new FenceGateBlock(
                Block.Properties.from(Blocks.OAK_FENCE_GATE)));
        DISTORTIC_PR_PLATE = PokecubeLegends.BLOCKS_TAB.register("distortic_pressure_plate",
                () -> new ItemGenerator.GenericPressurePlate(PressurePlateBlock.Sensitivity.EVERYTHING, Block.Properties
                        .create(Material.WOOD).sound(SoundType.WOOD).doesNotBlockMovement().hardnessAndResistance(
                                0.5F)));
        DISTORTIC_BUTTON = PokecubeLegends.BLOCKS_TAB.register("distortic_button",
                () -> new ItemGenerator.GenericWoodButton(Block.Properties.create(Material.WOOD).sound(SoundType.WOOD)
                        .doesNotBlockMovement().hardnessAndResistance(0.5F)));
        DISTORTIC_TRAPDOOR = PokecubeLegends.BLOCKS_TAB.register("distortic_trapdoor",
                () -> new ItemGenerator.GenericTrapDoor(Block.Properties.create(Material.WOOD, MaterialColor.WOOD)
                        .sound(SoundType.WOOD).hardnessAndResistance(2.0f, 3.0f).notSolid()));
        DISTORTIC_DOOR = PokecubeLegends.BLOCKS_TAB.register("distortic_door", () -> new ItemGenerator.GenericDoor(
                Block.Properties.create(Material.WOOD, MaterialColor.WOOD).sound(SoundType.WOOD).hardnessAndResistance(
                        2.0f, 3.0f).notSolid()));

        // Mirage Spot (Hoopa Ring)
        BLOCK_PORTALWARP = PokecubeLegends.BLOCKS.register("portal", () -> new PortalWarp("portal", Block.Properties
                .create(Material.ROCK).sound(SoundType.METAL).hardnessAndResistance(2000, 2000)).setShape(VoxelShapes
                        .create(0.05, 0, 0.05, 1, 3, 1)).setInfoBlockName("portalwarp"));

        // Legendary Spawns
        GOLEM_STONE = PokecubeLegends.BLOCKS.register("golem_stone", () -> new BlockBase("golem_stone", Material.ROCK,
                5f, SoundType.STONE, ToolType.PICKAXE, 2).noInfoBlock());

        LEGENDARY_SPAWN = PokecubeLegends.BLOCKS.register("legendaryspawn", () -> new LegendaryBlock("legendaryspawn",
                Material.IRON).noInfoBlock());
        TROUGH_BLOCK = PokecubeLegends.BLOCKS.register("trough_block", () -> new TroughBlock("trough_block",
                Block.Properties.create(Material.IRON).hardnessAndResistance(5, 15).harvestTool(ToolType.PICKAXE)
                        .harvestLevel(2).sound(SoundType.ANVIL).setLightLevel(b -> 4).variableOpacity()).noInfoBlock());
        HEATRAN_BLOCK 	= PokecubeLegends.BLOCKS.register("heatran_block", () -> new HeatranBlock("heatran_block",
        		Block.Properties.create(Material.LAVA).hardnessAndResistance(5, 15).harvestTool(ToolType.PICKAXE)
                .harvestLevel(2).sound(SoundType.CORAL).setLightLevel(b -> 4).variableOpacity().setEmmisiveRendering((s, r, p) -> true)).noInfoBlock());
        ///
        REGISTEEL_CORE = PokecubeLegends.BLOCKS.register("registeel_spawn", () -> new Registeel_Core("registeel_spawn",
                Material.IRON, 15, SoundType.METAL, ToolType.PICKAXE, 2).noInfoBlock());
        REGICE_CORE = PokecubeLegends.BLOCKS.register("regice_spawn", () -> new Regice_Core("regice_spawn",
                Material.PACKED_ICE, 15, SoundType.GLASS, ToolType.PICKAXE, 2).noInfoBlock());
        REGIROCK_CORE = PokecubeLegends.BLOCKS.register("regirock_spawn", () -> new Regirock_Core("regirock_spawn",
                Material.ROCK, 15, SoundType.STONE, ToolType.PICKAXE, 2).noInfoBlock());
        REGIELEKI_CORE = PokecubeLegends.BLOCKS.register("regieleki_spawn", () -> new Regieleki_Core("regieleki_spawn",
                Material.ROCK, 15, SoundType.STONE, ToolType.PICKAXE, 2).noInfoBlock());
        REGIDRAGO_CORE = PokecubeLegends.BLOCKS.register("regidrago_spawn", () -> new Regidrago_Core("regidrago_spawn",
                Material.ROCK, 15, SoundType.STONE, ToolType.PICKAXE, 2).noInfoBlock());
        REGIGIGA_CORE = PokecubeLegends.BLOCKS.register("regigiga_spawn", () -> new Regigigas_Core("regigiga_spawn",
                Material.IRON, 15, SoundType.METAL, ToolType.PICKAXE, 2).noInfoBlock());
        ///
        TIMESPACE_CORE = PokecubeLegends.BLOCKS.register("timerspawn", () -> new SpaceCoreBlock("timerspawn",
                Block.Properties.create(Material.ORGANIC).hardnessAndResistance(2000, 2000).sound(SoundType.STONE)
                        .variableOpacity()).setShape(VoxelShapes.create(0.05, 0, 0.05, 1, 2, 1)).noInfoBlock());
        NATURE_CORE = PokecubeLegends.BLOCKS.register("naturespawn", () -> new NatureCoreBlock("naturespawn",
                Block.Properties.create(Material.ROCK).hardnessAndResistance(2000, 2000).sound(SoundType.STONE)
                        .variableOpacity()).setShape(VoxelShapes.create(0.05, 0, 0.05, 1, 2, 1)).noInfoBlock());
        KELDEO_CORE = PokecubeLegends.BLOCKS.register("keldeoblock", () -> new KeldeoBlock("keldeoblock",
                Block.Properties.create(Material.ROCK).hardnessAndResistance(2000, 2000).sound(SoundType.STONE)
                        .variableOpacity()).setShape(VoxelShapes.create(0.05, 0, 0.05, 1, 1, 1)).noInfoBlock());
        VICTINI_CORE = PokecubeLegends.BLOCKS.register("victiniblock", () -> new VictiniBlock("victiniblock",
                Block.Properties.create(Material.IRON).hardnessAndResistance(5, 15).harvestTool(ToolType.PICKAXE)
                        .harvestLevel(2).sound(SoundType.ANVIL).variableOpacity()).setShape(VoxelShapes.create(0.05, 0,
                                0.05, 1, 1, 1)).noInfoBlock());
        YVELTAL_CORE = PokecubeLegends.BLOCKS.register("yveltal_egg", () -> new YveltalEgg("yveltal_egg",
                Block.Properties.create(Material.IRON).hardnessAndResistance(2000, 2000).sound(SoundType.WOOD)
                        .variableOpacity()).setShape(VoxelShapes.create(0.05, 0, 0.05, 1, 2, 1)).noInfoBlock());
        XERNEAS_CORE = PokecubeLegends.BLOCKS.register("xerneas_tree", () -> new XerneasCore("xerneas_tree",
                Block.Properties.create(Material.IRON).hardnessAndResistance(2000, 2000).sound(SoundType.WOOD)
                        .variableOpacity()).setShape(VoxelShapes.create(0.05, 0, 0.05, 1, 2, 1)).noInfoBlock());

        // Ores
        RUBY_ORE = PokecubeLegends.BLOCKS.register("ruby_ore", () -> new BlockBase("ruby_ore", Block.Properties.create(
                Material.ROCK).sound(SoundType.STONE).hardnessAndResistance(5, 15).harvestTool(ToolType.PICKAXE)
                .harvestLevel(2)).noInfoBlock());
        SAPPHIRE_ORE = PokecubeLegends.BLOCKS.register("sapphire_ore", () -> new BlockBase("sapphire_ore",
                Block.Properties.create(Material.ROCK).sound(SoundType.STONE).hardnessAndResistance(5, 15).harvestTool(
                        ToolType.PICKAXE).harvestLevel(2)).noInfoBlock());
        RUBY_BLOCK = PokecubeLegends.BLOCKS.register("ruby_block", () -> new Block(Block.Properties.create(
                Material.IRON).hardnessAndResistance(1.5f, 10).sound(SoundType.METAL).harvestTool(ToolType.PICKAXE)));
        SAPPHIRE_BLOCK = PokecubeLegends.BLOCKS.register("sapphire_block", () -> new Block(Block.Properties.create(
                Material.IRON).hardnessAndResistance(1.5f, 10).sound(SoundType.METAL).harvestTool(ToolType.PICKAXE)));
        SPECTRUM_ORE = PokecubeLegends.BLOCKS_TAB.register("spectrum_ore", () -> new BlockBase("spectrum_ore",
                Block.Properties.create(Material.ROCK).sound(SoundType.STONE).hardnessAndResistance(5, 15).harvestTool(
                        ToolType.PICKAXE).harvestLevel(2)).noInfoBlock());
        SPECTRUM_BLOCK = PokecubeLegends.BLOCKS_TAB.register("spectrum_block", () -> new Block(Block.Properties.create(
                Material.IRON).hardnessAndResistance(5.0f, 7).sound(SoundType.ANVIL).setLightLevel(b -> 4).harvestTool(
                        ToolType.PICKAXE)));

        // COSMIC_DUST_ORE =
        // PokecubeLegends.BLOCKS_TAB.register("cosmic_dust_ore", () -> new
        // BlockBase("cosmic_dust_ore",
        // Block.Properties.create(Material.ROCK).sound(SoundType.STONE).hardnessAndResistance(5,
        // 15).harvestTool(
        // ToolType.PICKAXE).harvestLevel(2)).noInfoBlock());
        COSMIC_DUST_ORE = PokecubeLegends.BLOCKS_TAB.register("cosmic_dust_ore", () -> new FallingBlock(Block.Properties
                .create(Material.ROCK).sound(SoundType.STONE).hardnessAndResistance(5, 15).harvestTool(ToolType.PICKAXE)
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
