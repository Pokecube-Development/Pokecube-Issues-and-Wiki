package pokecube.legends.init;

import java.util.Map;
import java.util.function.ToIntFunction;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DropExperienceBlock;
import net.minecraft.world.level.block.FenceBlock;
import net.minecraft.world.level.block.FenceGateBlock;
import net.minecraft.world.level.block.LanternBlock;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.MagmaBlock;
import net.minecraft.world.level.block.PressurePlateBlock;
import net.minecraft.world.level.block.RedStoneOreBlock;
import net.minecraft.world.level.block.RootedDirtBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.SandBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.SnowyDirtBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.StainedGlassBlock;
import net.minecraft.world.level.block.StainedGlassPaneBlock;
import net.minecraft.world.level.block.WallBlock;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraftforge.registries.RegistryObject;
import pokecube.core.PokecubeItems;
import pokecube.core.blocks.barrels.GenericBarrel;
import pokecube.core.blocks.bookshelves.GenericBookshelf;
import pokecube.core.blocks.bookshelves.GenericBookshelfEmpty;
import pokecube.core.blocks.hanging_signs.GenericCeilingHangingSign;
import pokecube.core.blocks.hanging_signs.GenericWallHangingSign;
import pokecube.core.blocks.signs.GenericStandingSign;
import pokecube.core.blocks.signs.GenericWallSign;
import pokecube.core.init.ItemGenerator;
import pokecube.core.init.ItemGenerator.GenericStairs;
import pokecube.legends.PokecubeLegends;
import pokecube.legends.Reference;
import pokecube.legends.blocks.BlockBase;
import pokecube.legends.blocks.FaceBlockBase;
import pokecube.legends.blocks.FallingBlockBase;
import pokecube.legends.blocks.FallingSandBlockBase;
import pokecube.legends.blocks.SaplingBase;
import pokecube.legends.blocks.StoneLogBase;
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
import pokecube.legends.blocks.flowing.AshBlock;
import pokecube.legends.blocks.flowing.MoltenMeteorBlock;
import pokecube.legends.blocks.normalblocks.AgedGrassBlock;
import pokecube.legends.blocks.normalblocks.AgedLeavesBlock;
import pokecube.legends.blocks.normalblocks.AquamarineClusterBlock;
import pokecube.legends.blocks.normalblocks.AquamarineCrystalBlock;
import pokecube.legends.blocks.normalblocks.AshOre;
import pokecube.legends.blocks.normalblocks.AzureGrassBlock;
import pokecube.legends.blocks.normalblocks.BuddingAquamarineBlock;
import pokecube.legends.blocks.normalblocks.CorruptedDirtBlock;
import pokecube.legends.blocks.normalblocks.CorruptedGrassBlock;
import pokecube.legends.blocks.normalblocks.CorruptedLeavesBlock;
import pokecube.legends.blocks.normalblocks.CrackedDistorticStone;
import pokecube.legends.blocks.normalblocks.DistorticGrassBlock;
import pokecube.legends.blocks.normalblocks.DistorticStoneBlock;
import pokecube.legends.blocks.normalblocks.FungalNyliumBlock;
import pokecube.legends.blocks.normalblocks.InfectedCampfireBlock;
import pokecube.legends.blocks.normalblocks.InfectedFireBlock;
import pokecube.legends.blocks.normalblocks.InfectedTorch;
import pokecube.legends.blocks.normalblocks.InfectedTorchWall;
import pokecube.legends.blocks.normalblocks.JungleGrassBlock;
import pokecube.legends.blocks.normalblocks.MagneticBlock;
import pokecube.legends.blocks.normalblocks.MirageGlassBlock;
import pokecube.legends.blocks.normalblocks.MirageGlassPaneBlock;
import pokecube.legends.blocks.normalblocks.MirageLeavesBlock;
import pokecube.legends.blocks.normalblocks.MushroomGrassBlock;
import pokecube.legends.blocks.normalblocks.OneWayGlass;
import pokecube.legends.blocks.normalblocks.OneWayLaboratoryGlass;
import pokecube.legends.blocks.normalblocks.OneWayMirageGlass;
import pokecube.legends.blocks.normalblocks.OneWaySpectrumGlass;
import pokecube.legends.blocks.normalblocks.OneWayStainedGlass;
import pokecube.legends.blocks.normalblocks.OneWayTintedGlass;
import pokecube.legends.blocks.normalblocks.PastelPinkDynaLeavesBlock;
import pokecube.legends.blocks.normalblocks.PinkDynaLeavesBlock;
import pokecube.legends.blocks.normalblocks.RedDynaLeavesBlock;
import pokecube.legends.blocks.normalblocks.SpectrumGlassBlock;
import pokecube.legends.blocks.normalblocks.SpectrumGlassPaneBlock;
import pokecube.legends.blocks.normalblocks.UnrefinedAquamarineBlock;
import pokecube.legends.blocks.normalblocks.WallGateBlock;
import pokecube.legends.blocks.plants.BigContaminatedDripleafBlock;
import pokecube.legends.blocks.plants.BigContaminatedDripleafStemBlock;
import pokecube.legends.blocks.plants.CrystallizedBush;
import pokecube.legends.blocks.plants.CrystallizedCactus;
import pokecube.legends.blocks.plants.DistorticSapling;
import pokecube.legends.blocks.plants.DynaShrubBlock;
import pokecube.legends.blocks.plants.MirageSapling;
import pokecube.legends.blocks.plants.PollutingBlossomBlock;
import pokecube.legends.blocks.plants.SmallContaminatedDripleafBlock;
import pokecube.legends.blocks.plants.StringOfPearlsBlock;
import pokecube.legends.blocks.plants.TallCrystallizedBush;
import pokecube.legends.tileentity.RaidSpawn;
import pokecube.legends.tileentity.RingTile;
import pokecube.legends.worldgen.trees.AgedTreeGrower;
import pokecube.legends.worldgen.trees.CorruptedTreeGrower;
import pokecube.legends.worldgen.trees.DistorticTreeGrower;
import pokecube.legends.worldgen.trees.InvertedTreeGrower;
import pokecube.legends.worldgen.trees.MirageTreeGrower;
import pokecube.legends.worldgen.trees.TemporalTreeGrower;
import thut.api.block.flowing.FlowingBlock;
import thut.api.block.flowing.SolidBlock;

@SuppressWarnings("deprecation")
public class BlockInit
{
    // Decorative_Blocks
    public static final RegistryObject<Block> METEORITE_SLAB;
    public static final RegistryObject<Block> METEORITE_STAIRS;

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

    // Meteor and ash blocks
    public static final RegistryObject<FlowingBlock> ASH;
    public static final RegistryObject<FlowingBlock> ASH_BLOCK;

    public static final RegistryObject<FlowingBlock> METEORITE_BLOCK;
    public static final RegistryObject<FlowingBlock> METEORITE_LAYER;

    public static final RegistryObject<FlowingBlock> METEORITE_MOLTEN_BLOCK;
    public static final RegistryObject<FlowingBlock> METEORITE_MOLTEN_LAYER;

    // Unowns
    public static final String[] unowns =
    {
            "unown_stone_a", "unown_stone_b", "unown_stone_c", "unown_stone_d", "unown_stone_e", "unown_stone_f",
            "unown_stone_g", "unown_stone_h", "unown_stone_i", "unown_stone_j", "unown_stone_k", "unown_stone_l",
            "unown_stone_m", "unown_stone_n", "unown_stone_o", "unown_stone_p", "unown_stone_q", "unown_stone_r",
            "unown_stone_s", "unown_stone_t", "unown_stone_u", "unown_stone_v", "unown_stone_w", "unown_stone_x",
            "unown_stone_y", "unown_stone_z", "unown_stone_ex", "unown_stone_in"
    };
    @SuppressWarnings("unchecked")
    public static final RegistryObject<Block>[] UNOWN_STONES = new RegistryObject[unowns.length];

    // Tapus Totems
    public static final RegistryObject<Block> TOTEM_BLOCK;

    private static final Map<String, MapColor> totemColours = Maps.newHashMap();
    public static final String[] totemKeys =
    {
            "_white_totem", "_lightgray_totem", "_gray_totem", "_black_totem", "_brown_totem", "_red_totem",
            "_orange_totem", "_yellow_totem", "_lime_totem", "_green_totem", "_cyan_totem",
            "_lightblue_totem", "_blue_totem", "_purple_totem", "_magenta_totem", "_pink_totem"
    };
    static
    {
        totemColours.put("_white_totem", MapColor.TERRACOTTA_WHITE);
        totemColours.put("_orange_totem", MapColor.TERRACOTTA_ORANGE);
        totemColours.put("_magenta_totem", MapColor.TERRACOTTA_MAGENTA);
        totemColours.put("_lightblue_totem", MapColor.TERRACOTTA_LIGHT_BLUE);
        totemColours.put("_yellow_totem", MapColor.TERRACOTTA_YELLOW);
        totemColours.put("_lime_totem", MapColor.TERRACOTTA_LIGHT_GREEN);
        totemColours.put("_pink_totem", MapColor.TERRACOTTA_PINK);
        totemColours.put("_gray_totem", MapColor.TERRACOTTA_GRAY);
        totemColours.put("_lightgray_totem", MapColor.TERRACOTTA_LIGHT_GRAY);
        totemColours.put("_cyan_totem", MapColor.TERRACOTTA_CYAN);
        totemColours.put("_purple_totem", MapColor.TERRACOTTA_PURPLE);
        totemColours.put("_blue_totem", MapColor.TERRACOTTA_BLUE);
        totemColours.put("_brown_totem", MapColor.TERRACOTTA_BROWN);
        totemColours.put("_green_totem", MapColor.TERRACOTTA_GREEN);
        totemColours.put("_red_totem", MapColor.TERRACOTTA_RED);
        totemColours.put("_black_totem", MapColor.TERRACOTTA_BLACK);
    }

    // Koko Totem
    @SuppressWarnings("unchecked")
    public static final RegistryObject<Block>[] KOKO = new RegistryObject[totemColours.size()];

    // Bulu Totem
    @SuppressWarnings("unchecked")
    public static final RegistryObject<Block>[] BULU = new RegistryObject[totemColours.size()];

    // Lele Totem
    @SuppressWarnings("unchecked")
    public static final RegistryObject<Block>[] LELE = new RegistryObject[totemColours.size()];

    // Fini Totem
    @SuppressWarnings("unchecked")
    public static final RegistryObject<Block>[] FINI = new RegistryObject[totemColours.size()];

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

    // Portal
    public static final RegistryObject<Block> MIRAGE_SPOTS;

    public static final RegistryObject<Block> RAID_SPAWNER;
    public static final RegistryObject<Block> CRAMOMATIC_BLOCK;

    // Dimensions
    // Distortic World
    public static final RegistryObject<Block> DISTORTIC_GRASS_BLOCK;
    public static final RegistryObject<Block> DISTORTIC_STONE;
    public static final RegistryObject<Block> DISTORTIC_STONE_SLAB;
    public static final RegistryObject<Block> DISTORTIC_STONE_STAIRS;
    public static final RegistryObject<Block> CRACKED_DISTORTIC_STONE;
    public static final RegistryObject<Block> DISTORTIC_GLOWSTONE;

    public static final RegistryObject<Block> DISTORTIC_MIRROR;
    public static final RegistryObject<Block> CHISELED_DISTORTIC_MIRROR;

    public static final RegistryObject<Block> DISTORTIC_FRAMED_MIRROR;
    public static final RegistryObject<Block> DISTORTIC_FRAMED_MIRROR_PANE;
    public static final RegistryObject<Block> SPECTRUM_GLASS;
    public static final RegistryObject<Block> SPECTRUM_GLASS_PANE;
    public static final RegistryObject<Block> MIRAGE_GLASS;
    public static final RegistryObject<Block> MIRAGE_GLASS_PANE;

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
    public static final RegistryObject<Block> ONE_WAY_GLASS_TINTED;
    public static final RegistryObject<Block> ONE_WAY_DISTORTIC_FRAMED_MIRROR;

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

    public static final RegistryObject<Block> INFECTED_CAMPFIRE;
    public static final RegistryObject<Block> INFECTED_FIRE;
    public static final RegistryObject<Block> INFECTED_LANTERN;
    public static final RegistryObject<Block> INFECTED_TORCH;
    public static final RegistryObject<Block> INFECTED_TORCH_WALL;

    public static final RegistryObject<Block> AGED_COARSE_DIRT;
    public static final RegistryObject<Block> AGED_DIRT;
    public static final RegistryObject<Block> AGED_GRASS_BLOCK;
    public static final RegistryObject<Block> AGED_PODZOL;
    public static final RegistryObject<Block> AZURE_COARSE_DIRT;
    public static final RegistryObject<Block> AZURE_DIRT;
    public static final RegistryObject<Block> AZURE_GRASS_BLOCK;
    public static final RegistryObject<Block> CORRUPTED_DIRT;
    public static final RegistryObject<Block> CORRUPTED_COARSE_DIRT;
    public static final RegistryObject<Block> CORRUPTED_GRASS_BLOCK;
    public static final RegistryObject<Block> FUNGAL_NYLIUM;
    public static final RegistryObject<Block> JUNGLE_COARSE_DIRT;
    public static final RegistryObject<Block> JUNGLE_DIRT;
    public static final RegistryObject<Block> JUNGLE_GRASS_BLOCK;
    public static final RegistryObject<Block> JUNGLE_PODZOL;
    public static final RegistryObject<Block> MUSHROOM_DIRT;
    public static final RegistryObject<Block> MUSHROOM_COARSE_DIRT;
    public static final RegistryObject<Block> MUSHROOM_GRASS_BLOCK;
    public static final RegistryObject<Block> ROOTED_CORRUPTED_DIRT;
    public static final RegistryObject<Block> ROOTED_MUSHROOM_DIRT;

    public static final RegistryObject<Block> MAGNETIC_STONE;

    // Aquamarine Blocks
    public static final RegistryObject<Block> AQUAMARINE_BLOCK;
    public static final RegistryObject<Block> AQUAMARINE_BUTTON;
    public static final RegistryObject<Block> AQUAMARINE_BRICK_SLAB;
    public static final RegistryObject<Block> AQUAMARINE_BRICK_STAIRS;
    public static final RegistryObject<Block> AQUAMARINE_BRICKS;
    public static final RegistryObject<Block> AQUAMARINE_CLUSTER;
    public static final RegistryObject<Block> AQUAMARINE_CRYSTAL;
    public static final RegistryObject<Block> AQUAMARINE_PR_PLATE;
    public static final RegistryObject<Block> AQUAMARINE_SLAB;
    public static final RegistryObject<Block> AQUAMARINE_STAIRS;
    public static final RegistryObject<Block> BUDDING_AQUAMARINE;
    public static final RegistryObject<Block> LARGE_AQUAMARINE_BUD;
    public static final RegistryObject<Block> MEDIUM_AQUAMARINE_BUD;
    public static final RegistryObject<Block> SMALL_AQUAMARINE_BUD;
    public static final RegistryObject<Block> UNREFINED_AQUAMARINE;
    public static final RegistryObject<Block> UNREFINED_AQUAMARINE_SLAB;
    public static final RegistryObject<Block> UNREFINED_AQUAMARINE_STAIRS;

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
    public static final RegistryObject<Block> ULTRA_DARKSTONE_BRICK_SLAB;
    public static final RegistryObject<Block> ULTRA_DARKSTONE_BRICK_STAIRS;
    public static final RegistryObject<Block> ULTRA_DARK_COBBLESTONE;
    public static final RegistryObject<Block> ULTRA_DARK_COBBLESTONE_SLAB;
    public static final RegistryObject<Block> ULTRA_DARK_COBBLESTONE_STAIRS;
    public static final RegistryObject<Block> ULTRA_DARKSTONE_SLAB;
    public static final RegistryObject<Block> ULTRA_DARKSTONE_STAIRS;
    public static final RegistryObject<Block> ULTRA_DARKSTONE_BUTTON;
    public static final RegistryObject<Block> ULTRA_DARKSTONE_PR_PLATE;

    // Dusk Dolerite Blocks
    public static final RegistryObject<Block> DUSK_DOLERITE;
    public static final RegistryObject<Block> DUSK_DOLERITE_SLAB;
    public static final RegistryObject<Block> DUSK_DOLERITE_STAIRS;
    public static final RegistryObject<Block> DUSK_DOLERITE_BUTTON;
    public static final RegistryObject<Block> DUSK_DOLERITE_PR_PLATE;
    public static final RegistryObject<Block> COBBLED_DUSK_DOLERITE;
    public static final RegistryObject<Block> COBBLED_DUSK_DOLERITE_SLAB;
    public static final RegistryObject<Block> COBBLED_DUSK_DOLERITE_STAIRS;
    public static final RegistryObject<Block> DUSK_DOLERITE_BRICKS;
    public static final RegistryObject<Block> DUSK_DOLERITE_BRICK_SLAB;
    public static final RegistryObject<Block> DUSK_DOLERITE_BRICK_STAIRS;

    public static final RegistryObject<Block> TURQUOISE_GRAVEL;

    // Azure Badlands
    public static final RegistryObject<Block> AZURE_SAND;
    public static final RegistryObject<Block> AZURE_SANDSTONE;
    public static final RegistryObject<Block> AZURE_SANDSTONE_SLAB;
    public static final RegistryObject<Block> AZURE_SANDSTONE_STAIRS;
    public static final RegistryObject<Block> AZURE_SANDSTONE_BRICKS;
    public static final RegistryObject<Block> AZURE_SANDSTONE_BRICK_SLAB;
    public static final RegistryObject<Block> AZURE_SANDSTONE_BRICK_STAIRS;
    public static final RegistryObject<Block> SMOOTH_AZURE_SANDSTONE;
    public static final RegistryObject<Block> SMOOTH_AZURE_SANDSTONE_SLAB;
    public static final RegistryObject<Block> SMOOTH_AZURE_SANDSTONE_STAIRS;
    public static final RegistryObject<Block> AZURE_SANDSTONE_BUTTON;
    public static final RegistryObject<Block> AZURE_SANDSTONE_PR_PLATE;

    // Blackened Beach
    public static final RegistryObject<Block> BLACKENED_SAND;
    public static final RegistryObject<Block> BLACKENED_SANDSTONE;
    public static final RegistryObject<Block> BLACKENED_SANDSTONE_SLAB;
    public static final RegistryObject<Block> BLACKENED_SANDSTONE_STAIRS;
    public static final RegistryObject<Block> BLACKENED_SANDSTONE_BRICKS;
    public static final RegistryObject<Block> BLACKENED_SANDSTONE_BRICK_SLAB;
    public static final RegistryObject<Block> BLACKENED_SANDSTONE_BRICK_STAIRS;
    public static final RegistryObject<Block> SMOOTH_BLACKENED_SANDSTONE;
    public static final RegistryObject<Block> SMOOTH_BLACKENED_SANDSTONE_SLAB;
    public static final RegistryObject<Block> SMOOTH_BLACKENED_SANDSTONE_STAIRS;
    public static final RegistryObject<Block> BLACKENED_SANDSTONE_BUTTON;
    public static final RegistryObject<Block> BLACKENED_SANDSTONE_PR_PLATE;

    // Mirage Desert
    public static final RegistryObject<Block> CRYSTALLIZED_SAND;
    public static final RegistryObject<Block> CRYSTALLIZED_SANDSTONE;
    public static final RegistryObject<Block> CRYS_SANDSTONE_SLAB;
    public static final RegistryObject<Block> CRYS_SANDSTONE_STAIRS;
    public static final RegistryObject<Block> CRYS_SANDSTONE_BRICKS;
    public static final RegistryObject<Block> CRYS_SANDSTONE_BRICK_SLAB;
    public static final RegistryObject<Block> CRYS_SANDSTONE_BRICK_STAIRS;
    public static final RegistryObject<Block> SMOOTH_CRYS_SANDSTONE;
    public static final RegistryObject<Block> SMOOTH_CRYS_SANDSTONE_SLAB;
    public static final RegistryObject<Block> SMOOTH_CRYS_SANDSTONE_STAIRS;
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

    public static final RegistryObject<Block> DYNA_LEAVES_PINK;
    public static final RegistryObject<Block> DYNA_LEAVES_RED;
    public static final RegistryObject<Block> DYNA_LEAVES_PASTEL_PINK;

    // Signs
    public static final RegistryObject<Block> AGED_SIGN;
    public static final RegistryObject<Block> AGED_WALL_SIGN;
    public static final RegistryObject<Block> CONCRETE_SIGN;
    public static final RegistryObject<Block> CONCRETE_WALL_SIGN;
    public static final RegistryObject<Block> CONCRETE_DENSE_SIGN;
    public static final RegistryObject<Block> CONCRETE_DENSE_WALL_SIGN;
    public static final RegistryObject<Block> CORRUPTED_SIGN;
    public static final RegistryObject<Block> CORRUPTED_WALL_SIGN;
    public static final RegistryObject<Block> DISTORTIC_SIGN;
    public static final RegistryObject<Block> DISTORTIC_WALL_SIGN;
    public static final RegistryObject<Block> INVERTED_SIGN;
    public static final RegistryObject<Block> INVERTED_WALL_SIGN;
    public static final RegistryObject<Block> MIRAGE_SIGN;
    public static final RegistryObject<Block> MIRAGE_WALL_SIGN;
    public static final RegistryObject<Block> TEMPORAL_SIGN;
    public static final RegistryObject<Block> TEMPORAL_WALL_SIGN;

    // Hanging Signs
    public static final RegistryObject<Block> AGED_HANGING_SIGN;
    public static final RegistryObject<Block> AGED_WALL_HANGING_SIGN;
    public static final RegistryObject<Block> CONCRETE_HANGING_SIGN;
    public static final RegistryObject<Block> CONCRETE_WALL_HANGING_SIGN;
    public static final RegistryObject<Block> CORRUPTED_HANGING_SIGN;
    public static final RegistryObject<Block> CORRUPTED_WALL_HANGING_SIGN;
    public static final RegistryObject<Block> DISTORTIC_HANGING_SIGN;
    public static final RegistryObject<Block> DISTORTIC_WALL_HANGING_SIGN;
    public static final RegistryObject<Block> INVERTED_HANGING_SIGN;
    public static final RegistryObject<Block> INVERTED_WALL_HANGING_SIGN;
    public static final RegistryObject<Block> MIRAGE_HANGING_SIGN;
    public static final RegistryObject<Block> MIRAGE_WALL_HANGING_SIGN;
    public static final RegistryObject<Block> TEMPORAL_HANGING_SIGN;
    public static final RegistryObject<Block> TEMPORAL_WALL_HANGING_SIGN;

    // Ores
    public static final RegistryObject<Block> DUSK_COAL_ORE;
    public static final RegistryObject<Block> ULTRA_COAL_ORE;

    public static final RegistryObject<Block> DUSK_COPPER_ORE;
    public static final RegistryObject<Block> ULTRA_COPPER_ORE;

    public static final RegistryObject<Block> DUSK_GOLD_ORE;
    public static final RegistryObject<Block> ULTRA_GOLD_ORE;

    public static final RegistryObject<Block> ASH_IRON_ORE;
    public static final RegistryObject<Block> DUSK_IRON_ORE;
    public static final RegistryObject<Block> ULTRA_IRON_ORE;

    public static final RegistryObject<Block> DUSK_DIAMOND_ORE;
    public static final RegistryObject<Block> ULTRA_DIAMOND_ORE;

    public static final RegistryObject<Block> DUSK_EMERALD_ORE;
    public static final RegistryObject<Block> ULTRA_EMERALD_ORE;

    public static final RegistryObject<Block> DUSK_LAPIS_ORE;
    public static final RegistryObject<Block> ULTRA_LAPIS_ORE;

    public static final RegistryObject<Block> ULTRA_REDSTONE_ORE;
    public static final RegistryObject<Block> DUSK_REDSTONE_ORE;

    public static final RegistryObject<Block> ULTRA_FOSSIL_ORE;
    public static final RegistryObject<Block> DUSK_FOSSIL_ORE;

    public static final RegistryObject<Block> RUBY_ORE;
    public static final RegistryObject<Block> DEEPSLATE_RUBY_ORE;
    public static final RegistryObject<Block> DUSK_RUBY_ORE;
    public static final RegistryObject<Block> ULTRA_RUBY_ORE;

    public static final RegistryObject<Block> SAPPHIRE_ORE;
    public static final RegistryObject<Block> DEEPSLATE_SAPPHIRE_ORE;
    public static final RegistryObject<Block> DUSK_SAPPHIRE_ORE;
    public static final RegistryObject<Block> ULTRA_SAPPHIRE_ORE;

    public static final RegistryObject<Block> DUSK_COSMIC_ORE;
    public static final RegistryObject<Block> METEORITE_COSMIC_ORE;
    public static final RegistryObject<Block> ULTRA_COSMIC_ORE;

    public static final RegistryObject<Block> DUSK_SPECTRUM_ORE;
    public static final RegistryObject<Block> ULTRA_SPECTRUM_ORE;

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

    public static final RegistryObject<BlockEntityType<RaidSpawn>> RAID_SPAWN_ENTITY;
    public static final RegistryObject<BlockEntityType<RingTile>> RING_ENTITY;

    static
    {
        RAID_SPAWN_ENTITY = PokecubeLegends.TILES.register("raid_spot_spawner",
                () -> BlockEntityType.Builder.of(RaidSpawn::new, BlockInit.RAID_SPAWNER.get()).build(null));
        RING_ENTITY = PokecubeLegends.TILES.register("mirage_spot_block",
                () -> BlockEntityType.Builder.of(RingTile::new, BlockInit.MIRAGE_SPOTS.get()).build(null));

        ULTRA_COAL_ORE = PokecubeLegends.BLOCKS.register("ultra_coal_ore",
                () -> new DropExperienceBlock(BlockBehaviour.Properties.of().mapColor(MapColor.TERRACOTTA_CYAN)
                        .sound(SoundType.STONE).instrument(NoteBlockInstrument.BASEDRUM)
                        .strength(3.0F, 3.0F).requiresCorrectToolForDrops(),
                        UniformInt.of(0, 2)));
        DUSK_COAL_ORE = PokecubeLegends.BLOCKS.register("dusk_dolerite_coal_ore",
                () -> new DropExperienceBlock(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_PURPLE)
                        .sound(SoundType.DEEPSLATE).instrument(NoteBlockInstrument.XYLOPHONE)
                        .strength(4.5F, 3.0F).requiresCorrectToolForDrops(),
                        UniformInt.of(0, 2)));
        ULTRA_IRON_ORE = PokecubeLegends.BLOCKS.register("ultra_iron_ore",
                () -> new DropExperienceBlock(BlockBehaviour.Properties.of().mapColor(MapColor.TERRACOTTA_CYAN)
                        .sound(SoundType.STONE).instrument(NoteBlockInstrument.BASEDRUM)
                        .strength(3.0F, 3.0F).requiresCorrectToolForDrops()));
        DUSK_IRON_ORE = PokecubeLegends.BLOCKS.register("dusk_dolerite_iron_ore",
                () -> new DropExperienceBlock(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_PURPLE)
                        .sound(SoundType.DEEPSLATE).instrument(NoteBlockInstrument.XYLOPHONE)
                        .strength(4.5F, 3.0F).requiresCorrectToolForDrops()));

        ULTRA_COPPER_ORE = PokecubeLegends.BLOCKS.register("ultra_copper_ore",
                () -> new DropExperienceBlock(BlockBehaviour.Properties.of().mapColor(MapColor.TERRACOTTA_CYAN)
                        .sound(SoundType.STONE).instrument(NoteBlockInstrument.BASEDRUM)
                        .strength(3.0F, 3.0F).requiresCorrectToolForDrops()));
        DUSK_COPPER_ORE = PokecubeLegends.BLOCKS.register("dusk_dolerite_copper_ore",
                () -> new DropExperienceBlock(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_PURPLE)
                        .sound(SoundType.DEEPSLATE).instrument(NoteBlockInstrument.XYLOPHONE)
                        .strength(4.5F, 3.0F).requiresCorrectToolForDrops()));

        ULTRA_GOLD_ORE = PokecubeLegends.BLOCKS.register("ultra_gold_ore",
                () -> new DropExperienceBlock(BlockBehaviour.Properties.of().mapColor(MapColor.TERRACOTTA_CYAN)
                        .sound(SoundType.STONE).instrument(NoteBlockInstrument.BASEDRUM)
                        .strength(3.0F, 3.0F).requiresCorrectToolForDrops()));
        DUSK_GOLD_ORE = PokecubeLegends.BLOCKS.register("dusk_dolerite_gold_ore",
                () -> new DropExperienceBlock(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_PURPLE)
                        .sound(SoundType.DEEPSLATE).instrument(NoteBlockInstrument.XYLOPHONE)
                        .strength(4.5F, 3.0F).requiresCorrectToolForDrops()));

        ULTRA_REDSTONE_ORE = PokecubeLegends.BLOCKS.register("ultra_redstone_ore",
                () -> new RedStoneOreBlock(BlockBehaviour.Properties.of().mapColor(MapColor.TERRACOTTA_CYAN)
                        .sound(SoundType.STONE).instrument(NoteBlockInstrument.BASEDRUM)
                        .strength(3.0F, 3.0F).requiresCorrectToolForDrops().randomTicks()
                        .lightLevel(BlockInit.litBlockEmission(9))));
        DUSK_REDSTONE_ORE = PokecubeLegends.BLOCKS.register("dusk_dolerite_redstone_ore",
                () -> new RedStoneOreBlock(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_PURPLE)
                        .sound(SoundType.DEEPSLATE).instrument(NoteBlockInstrument.XYLOPHONE)
                        .strength(4.5F, 3.0F).requiresCorrectToolForDrops().randomTicks()
                        .lightLevel(BlockInit.litBlockEmission(9))));

        ULTRA_LAPIS_ORE = PokecubeLegends.BLOCKS.register("ultra_lapis_ore",
                () -> new DropExperienceBlock(BlockBehaviour.Properties.of().mapColor(MapColor.TERRACOTTA_CYAN)
                        .sound(SoundType.STONE).instrument(NoteBlockInstrument.BASEDRUM)
                        .strength(3.0F, 3.0F).requiresCorrectToolForDrops(),
                        UniformInt.of(2, 5)));
        DUSK_LAPIS_ORE = PokecubeLegends.BLOCKS.register("dusk_dolerite_lapis_ore",
                () -> new DropExperienceBlock(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_PURPLE)
                        .sound(SoundType.DEEPSLATE).instrument(NoteBlockInstrument.XYLOPHONE)
                        .strength(4.5F, 3.0F).requiresCorrectToolForDrops(),
                        UniformInt.of(2, 5)));

        ULTRA_EMERALD_ORE = PokecubeLegends.BLOCKS.register("ultra_emerald_ore",
                () -> new DropExperienceBlock(BlockBehaviour.Properties.of().mapColor(MapColor.TERRACOTTA_CYAN)
                        .sound(SoundType.STONE).instrument(NoteBlockInstrument.BASEDRUM)
                        .strength(3.0F, 3.0F).requiresCorrectToolForDrops(),
                        UniformInt.of(3, 7)));
        DUSK_EMERALD_ORE = PokecubeLegends.BLOCKS.register("dusk_dolerite_emerald_ore",
                () -> new DropExperienceBlock(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_PURPLE)
                        .sound(SoundType.DEEPSLATE).instrument(NoteBlockInstrument.XYLOPHONE)
                        .strength(4.5F, 3.0F).requiresCorrectToolForDrops(),
                        UniformInt.of(3, 7)));

        ULTRA_DIAMOND_ORE = PokecubeLegends.BLOCKS.register("ultra_diamond_ore",
                () -> new DropExperienceBlock(BlockBehaviour.Properties.of().mapColor(MapColor.TERRACOTTA_CYAN)
                        .sound(SoundType.STONE).instrument(NoteBlockInstrument.BASEDRUM)
                        .strength(3.0F, 3.0F).requiresCorrectToolForDrops(),
                        UniformInt.of(3, 7)));
        DUSK_DIAMOND_ORE = PokecubeLegends.BLOCKS.register("dusk_dolerite_diamond_ore",
                () -> new DropExperienceBlock(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_PURPLE)
                        .sound(SoundType.DEEPSLATE).instrument(NoteBlockInstrument.XYLOPHONE)
                        .strength(4.5F, 3.0F).requiresCorrectToolForDrops(),
                        UniformInt.of(3, 7)));

        ULTRA_FOSSIL_ORE = PokecubeLegends.BLOCKS.register("ultra_fossil_ore",
                () -> new DropExperienceBlock(BlockBehaviour.Properties.of().mapColor(MapColor.TERRACOTTA_CYAN)
                        .sound(SoundType.STONE).instrument(NoteBlockInstrument.BASEDRUM)
                        .strength(3.0F, 3.0F).requiresCorrectToolForDrops(),
                        UniformInt.of(0, 3)));
        DUSK_FOSSIL_ORE = PokecubeLegends.BLOCKS.register("dusk_dolerite_fossil_ore",
                () -> new DropExperienceBlock(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_PURPLE)
                        .sound(SoundType.DEEPSLATE).instrument(NoteBlockInstrument.XYLOPHONE)
                        .strength(4.5F, 3.0F).requiresCorrectToolForDrops(),
                        UniformInt.of(0, 3)));

        METEORITE_COSMIC_ORE = PokecubeLegends.BLOCKS.register("meteorite_cosmic_ore",
                () -> new DropExperienceBlock(BlockBehaviour.Properties.of().mapColor(MapColor.TERRACOTTA_BLUE)
                        .sound(SoundType.DRIPSTONE_BLOCK).instrument(NoteBlockInstrument.BASEDRUM)
                        .strength(3.0F, 3.0F).requiresCorrectToolForDrops(),
                        UniformInt.of(2, 5)));
        ULTRA_COSMIC_ORE = PokecubeLegends.BLOCKS.register("ultra_cosmic_ore",
                () -> new DropExperienceBlock(BlockBehaviour.Properties.of().mapColor(MapColor.TERRACOTTA_CYAN)
                        .sound(SoundType.STONE).instrument(NoteBlockInstrument.BASEDRUM)
                        .strength(3.0F, 3.0F).requiresCorrectToolForDrops(),
                        UniformInt.of(2, 5)));
        DUSK_COSMIC_ORE = PokecubeLegends.BLOCKS.register("dusk_dolerite_cosmic_ore",
                () -> new DropExperienceBlock(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_PURPLE)
                        .sound(SoundType.DEEPSLATE).instrument(NoteBlockInstrument.XYLOPHONE)
                        .strength(4.5F, 3.0F).requiresCorrectToolForDrops(),
                        UniformInt.of(2, 5)));

        RUBY_ORE = PokecubeLegends.BLOCKS.register("ruby_ore",
                () -> new DropExperienceBlock(BlockBehaviour.Properties.of().mapColor(MapColor.STONE)
                        .sound(SoundType.STONE).instrument(NoteBlockInstrument.BASEDRUM)
                        .strength(3.0F, 3.0F).requiresCorrectToolForDrops(),
                        UniformInt.of(2, 6)));
        DEEPSLATE_RUBY_ORE = PokecubeLegends.BLOCKS.register("deepslate_ruby_ore",
                () -> new DropExperienceBlock(BlockBehaviour.Properties.of().mapColor(MapColor.DEEPSLATE)
                        .sound(SoundType.DEEPSLATE).strength(4.5F, 3.0F).requiresCorrectToolForDrops(),
                        UniformInt.of(2, 6)));
        ULTRA_RUBY_ORE = PokecubeLegends.BLOCKS.register("ultra_ruby_ore",
                () -> new DropExperienceBlock(BlockBehaviour.Properties.of().mapColor(MapColor.TERRACOTTA_CYAN)
                        .sound(SoundType.STONE).instrument(NoteBlockInstrument.BASEDRUM)
                        .strength(3.0F, 3.0F).requiresCorrectToolForDrops(),
                        UniformInt.of(2, 6)));
        DUSK_RUBY_ORE = PokecubeLegends.BLOCKS.register("dusk_dolerite_ruby_ore",
                () -> new DropExperienceBlock(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_PURPLE)
                        .sound(SoundType.DEEPSLATE).instrument(NoteBlockInstrument.XYLOPHONE)
                        .strength(4.5F, 3.0F).requiresCorrectToolForDrops(),
                        UniformInt.of(2, 6)));

        SAPPHIRE_ORE = PokecubeLegends.BLOCKS.register("sapphire_ore",
                () -> new DropExperienceBlock(BlockBehaviour.Properties.of().mapColor(MapColor.STONE)
                        .sound(SoundType.STONE).instrument(NoteBlockInstrument.BASEDRUM)
                        .strength(3.0F, 3.0F).requiresCorrectToolForDrops(),
                        UniformInt.of(2, 6)));
        DEEPSLATE_SAPPHIRE_ORE = PokecubeLegends.BLOCKS.register("deepslate_sapphire_ore",
                () -> new DropExperienceBlock(BlockBehaviour.Properties.of().mapColor(MapColor.DEEPSLATE)
                        .sound(SoundType.DEEPSLATE).strength(4.5F, 3.0F).requiresCorrectToolForDrops(),
                        UniformInt.of(2, 6)));
        ULTRA_SAPPHIRE_ORE = PokecubeLegends.BLOCKS.register("ultra_sapphire_ore",
                () -> new DropExperienceBlock(BlockBehaviour.Properties.of().mapColor(MapColor.TERRACOTTA_CYAN)
                        .sound(SoundType.STONE).instrument(NoteBlockInstrument.BASEDRUM)
                        .strength(3.0F, 3.0F).requiresCorrectToolForDrops(),
                        UniformInt.of(2, 6)));
        DUSK_SAPPHIRE_ORE = PokecubeLegends.BLOCKS.register("dusk_dolerite_sapphire_ore",
                () -> new DropExperienceBlock(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_PURPLE)
                        .sound(SoundType.DEEPSLATE).instrument(NoteBlockInstrument.XYLOPHONE)
                        .strength(4.5F, 3.0F).requiresCorrectToolForDrops(),
                        UniformInt.of(2, 6)));

        ULTRA_SPECTRUM_ORE = PokecubeLegends.BLOCKS.register("spectrum_ore",
                () -> new DropExperienceBlock(BlockBehaviour.Properties.of().mapColor(MapColor.TERRACOTTA_CYAN)
                        .sound(SoundType.STONE).instrument(NoteBlockInstrument.BASEDRUM)
                        .strength(3.0F, 3.0F).requiresCorrectToolForDrops(),
                        UniformInt.of(3, 7)));
        DUSK_SPECTRUM_ORE = PokecubeLegends.BLOCKS.register("dusk_dolerite_spectrum_ore",
                () -> new DropExperienceBlock(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_PURPLE)
                        .sound(SoundType.DEEPSLATE).instrument(NoteBlockInstrument.XYLOPHONE)
                        .strength(4.5F, 3.0F).requiresCorrectToolForDrops(),
                        UniformInt.of(3, 7)));

        // Ultra Stone Blocks
        ULTRA_STONE = PokecubeLegends.BLOCKS.register("ultra_stone",
                () -> new BlockBase(MapColor.TERRACOTTA_CYAN, SoundType.STONE, NoteBlockInstrument.BASEDRUM,
                        true, 1.5F, 7.0F));
        ULTRA_STONE_STAIRS = PokecubeLegends.BLOCKS.register("ultra_stone_stairs",
                () -> new ItemGenerator.GenericStairs(ULTRA_STONE.get().defaultBlockState(),
                        BlockBehaviour.Properties.copy(ULTRA_STONE.get())));
        ULTRA_STONE_SLAB = PokecubeLegends.BLOCKS.register("ultra_stone_slab",
                () -> new SlabBlock(BlockBehaviour.Properties.copy(ULTRA_STONE.get())));
        ULTRA_STONE_BUTTON = PokecubeLegends.BLOCKS.register("ultra_stone_button",
                () -> new ItemGenerator.GenericButton(BlockSetType.STONE, false, 20,
                        BlockBehaviour.Properties.of().sound(SoundType.STONE).pushReaction(PushReaction.DESTROY)
                                .strength(0.7F).noCollission().requiresCorrectToolForDrops()));
        ULTRA_STONE_PR_PLATE = PokecubeLegends.BLOCKS.register("ultra_stone_pressure_plate",
                () -> new ItemGenerator.GenericPressurePlate(PressurePlateBlock.Sensitivity.MOBS,
                        LegendsBlockSetType.ULTRA_STONE_BLOCK_SET, BlockBehaviour.Properties.of()
                        .mapColor(MapColor.COLOR_BLUE).sound(SoundType.STONE).instrument(NoteBlockInstrument.BASEDRUM)
                        .pushReaction(PushReaction.DESTROY).strength(0.7F)
                        .forceSolidOn().noCollission().requiresCorrectToolForDrops()));

        ULTRA_COBBLESTONE = PokecubeLegends.BLOCKS.register("ultra_cobblestone",
                () -> new BlockBase(MapColor.TERRACOTTA_CYAN, SoundType.STONE, NoteBlockInstrument.BASEDRUM,
                        true, 2.0F, 8.0F));
        ULTRA_COBBLESTONE_STAIRS = PokecubeLegends.BLOCKS.register("ultra_cobblestone_stairs",
                () -> new ItemGenerator.GenericStairs(ULTRA_COBBLESTONE.get().defaultBlockState(),
                        BlockBehaviour.Properties.copy(ULTRA_COBBLESTONE.get())));
        ULTRA_COBBLESTONE_SLAB = PokecubeLegends.BLOCKS.register("ultra_cobblestone_slab",
                () -> new SlabBlock(BlockBehaviour.Properties.copy(ULTRA_COBBLESTONE.get())));

        // Darkstone Blocks
        ULTRA_DARKSTONE = PokecubeLegends.BLOCKS.register("ultra_darkstone",
                () -> new BlockBase(MapColor.COLOR_BLACK, SoundType.GILDED_BLACKSTONE, NoteBlockInstrument.BASEDRUM,
                        true, 5.0F, 8.0F));
        ULTRA_DARKSTONE_STAIRS = PokecubeLegends.BLOCKS.register("ultra_darkstone_stairs",
                () -> new ItemGenerator.GenericStairs(ULTRA_DARKSTONE.get().defaultBlockState(),
                        BlockBehaviour.Properties.copy(ULTRA_DARKSTONE.get())));
        ULTRA_DARKSTONE_SLAB = PokecubeLegends.BLOCKS.register("ultra_darkstone_slab",
                () -> new SlabBlock(BlockBehaviour.Properties.copy(ULTRA_DARKSTONE.get())));
        ULTRA_DARKSTONE_BUTTON = PokecubeLegends.BLOCKS.register("ultra_darkstone_button",
                () -> new ItemGenerator.GenericButton(BlockSetType.STONE, false, 20,
                        BlockBehaviour.Properties.of().sound(SoundType.NETHER_BRICKS).pushReaction(PushReaction.DESTROY)
                                .strength(0.5F).noCollission().requiresCorrectToolForDrops()));
        ULTRA_DARKSTONE_PR_PLATE = PokecubeLegends.BLOCKS.register("ultra_darkstone_pressure_plate",
                () -> new ItemGenerator.GenericPressurePlate(PressurePlateBlock.Sensitivity.MOBS,
                        LegendsBlockSetType.ULTRA_DARKSTONE_BLOCK_SET, BlockBehaviour.Properties.of()
                        .mapColor(MapColor.COLOR_BLACK).sound(SoundType.NETHER_BRICKS)
                        .instrument(NoteBlockInstrument.BASEDRUM).pushReaction(PushReaction.DESTROY)
                        .strength(0.7F).noCollission().forceSolidOn().requiresCorrectToolForDrops()));

        ULTRA_DARK_COBBLESTONE = PokecubeLegends.BLOCKS.register("ultra_dark_cobblestone",
                () -> new BlockBase(MapColor.COLOR_BLACK, SoundType.STONE, NoteBlockInstrument.BASEDRUM,
                        true, 0.8f, 10.0F));
        ULTRA_DARK_COBBLESTONE_STAIRS = PokecubeLegends.BLOCKS.register("ultra_dark_cobblestone_stairs",
                () -> new ItemGenerator.GenericStairs(ULTRA_DARK_COBBLESTONE.get().defaultBlockState(),
                        BlockBehaviour.Properties.copy(ULTRA_DARK_COBBLESTONE.get())));
        ULTRA_DARK_COBBLESTONE_SLAB = PokecubeLegends.BLOCKS.register("ultra_dark_cobblestone_slab",
                () -> new SlabBlock(BlockBehaviour.Properties.copy(ULTRA_DARK_COBBLESTONE.get())));

        // Dusk Dolerite Blocks
        DUSK_DOLERITE = PokecubeLegends.BLOCKS.register("dusk_dolerite",
                () -> new RotatedPillarBlock(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_PURPLE)
                        .sound(SoundType.DEEPSLATE).instrument(NoteBlockInstrument.XYLOPHONE)
                        .strength(3.0F, 8.0F).requiresCorrectToolForDrops()));
        DUSK_DOLERITE_STAIRS = PokecubeLegends.BLOCKS.register("dusk_dolerite_stairs",
                () -> new ItemGenerator.GenericStairs(DUSK_DOLERITE.get().defaultBlockState(),
                        BlockBehaviour.Properties.copy(DUSK_DOLERITE.get())));
        DUSK_DOLERITE_SLAB = PokecubeLegends.BLOCKS.register("dusk_dolerite_slab",
                () -> new SlabBlock(BlockBehaviour.Properties.copy(DUSK_DOLERITE.get())));
        DUSK_DOLERITE_BUTTON = PokecubeLegends.BLOCKS.register("dusk_dolerite_button",
                () -> new ItemGenerator.GenericButton(BlockSetType.STONE, false, 15,
                        BlockBehaviour.Properties.of().strength(0.8F).noCollission().requiresCorrectToolForDrops()
                                .sound(SoundType.DEEPSLATE).pushReaction(PushReaction.DESTROY)));
        DUSK_DOLERITE_PR_PLATE = PokecubeLegends.BLOCKS.register("dusk_dolerite_pressure_plate",
                () -> new ItemGenerator.GenericPressurePlate(PressurePlateBlock.Sensitivity.MOBS,
                        LegendsBlockSetType.DUSK_DOLERITE_BLOCK_SET, BlockBehaviour.Properties.of()
                        .mapColor(MapColor.COLOR_PURPLE).sound(SoundType.DEEPSLATE)
                        .instrument(NoteBlockInstrument.XYLOPHONE).pushReaction(PushReaction.DESTROY)
                        .strength(0.8F).noCollission().forceSolidOn().requiresCorrectToolForDrops()));

        COBBLED_DUSK_DOLERITE = PokecubeLegends.BLOCKS.register("cobbled_dusk_dolerite",
                () -> new Block(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_PURPLE)
                                .sound(SoundType.DEEPSLATE).instrument(NoteBlockInstrument.XYLOPHONE)
                                .strength(3.0F, 9.0F).requiresCorrectToolForDrops()));
        COBBLED_DUSK_DOLERITE_STAIRS = PokecubeLegends.BLOCKS.register("cobbled_dusk_dolerite_stairs",
                () -> new ItemGenerator.GenericStairs(COBBLED_DUSK_DOLERITE.get().defaultBlockState(),
                        BlockBehaviour.Properties.copy(COBBLED_DUSK_DOLERITE.get())));
        COBBLED_DUSK_DOLERITE_SLAB = PokecubeLegends.BLOCKS.register("cobbled_dusk_dolerite_slab",
                () -> new SlabBlock(BlockBehaviour.Properties.copy(COBBLED_DUSK_DOLERITE.get())));

        // Ash blocks
        BlockBehaviour.Properties layer_props = BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_GRAY)
                .sound(SoundType.SNOW).instrument(NoteBlockInstrument.PLING).pushReaction(PushReaction.DESTROY)
                .strength(0.1F).speedFactor(0.3F).randomTicks().replaceable().forceSolidOff()
                .isViewBlocking((state, block, pos) -> state.getValue(FlowingBlock.LAYERS) >= 12);

        BlockBehaviour.Properties block_props = BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_GRAY)
                .sound(SoundType.SNOW).instrument(NoteBlockInstrument.PLING)
                .strength(0.2F).speedFactor(0.3F).randomTicks();

        RegistryObject<FlowingBlock>[] regs = AshBlock.makeDust(PokecubeLegends.BLOCKS, Reference.ID, "ash",
                "ash_block", layer_props, block_props);

        ASH = regs[0];
        ASH_BLOCK = regs[1];

        ASH_IRON_ORE = PokecubeLegends.BLOCKS.register("ash_iron_ore",
                () -> new AshOre(3816264, BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_BLACK)
                        .sound(SoundType.SNOW).instrument(NoteBlockInstrument.PLING)
                        .strength(0.2F).speedFactor(0.3F).randomTicks().requiresCorrectToolForDrops()));

        block_props = layer_props = BlockBehaviour.Properties.of().mapColor(MapColor.TERRACOTTA_BLUE)
                .sound(SoundType.DRIPSTONE_BLOCK).instrument(NoteBlockInstrument.PLING)
                .strength(2.5F).requiresCorrectToolForDrops();

        regs = SolidBlock.makeSolid(PokecubeLegends.BLOCKS, Reference.ID, "meteorite_layer", "meteorite_block",
                layer_props, block_props);

        METEORITE_LAYER = regs[0];
        METEORITE_BLOCK = regs[1];

        block_props = layer_props = BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_RED)
                .strength(2.0F).noOcclusion().randomTicks().requiresCorrectToolForDrops().lightLevel(s -> 10);

        ResourceLocation solid_layer = new ResourceLocation(Reference.ID, "meteorite_layer");
        ResourceLocation solid_block = new ResourceLocation(Reference.ID, "meteorite_block");
        regs = MoltenMeteorBlock.makeLava(PokecubeLegends.BLOCKS, Reference.ID, "meteorite_molten_layer",
                "meteorite_molten_block", layer_props, block_props, solid_layer, solid_block);

        METEORITE_MOLTEN_LAYER = regs[0];
        METEORITE_MOLTEN_BLOCK = regs[1];

        // Meteor Blocks
        METEORITE_STAIRS = PokecubeLegends.BLOCKS.register("meteorite_stairs",
                () -> new ItemGenerator.GenericStairs(METEORITE_BLOCK.get().defaultBlockState(),
                        BlockBehaviour.Properties.of().mapColor(MapColor.TERRACOTTA_BLUE)
                                .sound(SoundType.DRIPSTONE_BLOCK).instrument(NoteBlockInstrument.PLING)
                                .strength(2.0F, 3.0F).requiresCorrectToolForDrops()));
        METEORITE_SLAB = PokecubeLegends.BLOCKS.register("meteorite_slab",
                () -> new SlabBlock(BlockBehaviour.Properties.of().mapColor(MapColor.TERRACOTTA_BLUE)
                        .sound(SoundType.DRIPSTONE_BLOCK).instrument(NoteBlockInstrument.PLING)
                        .strength(2.0F, 3.0F).requiresCorrectToolForDrops()));

        // Soils
        AGED_GRASS_BLOCK = PokecubeLegends.BLOCKS.register("aged_grass_block",
                () -> new AgedGrassBlock(BlockBehaviour.Properties.of().mapColor(MapColor.GOLD)
                        .sound(SoundType.GRASS).strength(0.6F).randomTicks()));
        AGED_PODZOL = PokecubeLegends.BLOCKS.register("aged_podzol",
                () -> new SnowyDirtBlock(BlockBehaviour.Properties.of().mapColor(MapColor.GOLD)
                        .sound(SoundType.GRAVEL).strength(0.6F)));
        AGED_DIRT = PokecubeLegends.BLOCKS.register("aged_dirt",
                () -> new BlockBase(MapColor.TERRACOTTA_YELLOW, SoundType.GRAVEL, NoteBlockInstrument.HARP,
                        false, 0.5F, 0.5F));
        AGED_COARSE_DIRT = PokecubeLegends.BLOCKS.register("aged_coarse_dirt",
                () -> new BlockBase(MapColor.TERRACOTTA_YELLOW, SoundType.GRAVEL, NoteBlockInstrument.HARP,
                        false, 0.5F, 0.5F));
        AZURE_GRASS_BLOCK = PokecubeLegends.BLOCKS.register("azure_grass_block",
                () -> new AzureGrassBlock(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_LIGHT_BLUE)
                        .sound(SoundType.GRASS).strength(0.6F).randomTicks()));
        AZURE_DIRT = PokecubeLegends.BLOCKS.register("azure_dirt",
                () -> new BlockBase(MapColor.COLOR_BLUE, SoundType.GRAVEL, NoteBlockInstrument.HARP,
                        false, 0.5F, 0.5F));
        AZURE_COARSE_DIRT = PokecubeLegends.BLOCKS.register("azure_coarse_dirt",
                () -> new BlockBase(MapColor.COLOR_BLUE, SoundType.GRAVEL, NoteBlockInstrument.HARP,
                        false, 0.5F, 0.5F));
        CORRUPTED_GRASS_BLOCK = PokecubeLegends.BLOCKS.register("corrupted_grass_block",
                () -> new CorruptedGrassBlock(BlockBehaviour.Properties.of().mapColor(MapColor.TERRACOTTA_BLUE)
                        .sound(SoundType.NYLIUM).strength(4.0F, 5.0F)
                        .randomTicks().requiresCorrectToolForDrops()));
        CORRUPTED_DIRT = PokecubeLegends.BLOCKS.register("corrupted_dirt",
                () -> new CorruptedDirtBlock(BlockBehaviour.Properties.of().mapColor(MapColor.TERRACOTTA_PURPLE)
                        .sound(SoundType.NETHERRACK).strength(3.0F, 4.0F).requiresCorrectToolForDrops()));
        CORRUPTED_COARSE_DIRT = PokecubeLegends.BLOCKS.register("corrupted_coarse_dirt",
                () -> new CorruptedDirtBlock(BlockBehaviour.Properties.of().mapColor(MapColor.TERRACOTTA_PURPLE)
                        .sound(SoundType.NETHERRACK).strength(3.0F, 4.0F).requiresCorrectToolForDrops()));
        ROOTED_CORRUPTED_DIRT = PokecubeLegends.BLOCKS.register("rooted_corrupted_dirt",
                () -> new RootedDirtBlock(BlockBehaviour.Properties.of().mapColor(MapColor.TERRACOTTA_PURPLE)
                        .sound(SoundType.NETHER_GOLD_ORE).strength(0.9F).requiresCorrectToolForDrops()));
        JUNGLE_GRASS_BLOCK = PokecubeLegends.BLOCKS.register("jungle_grass_block",
                () -> new JungleGrassBlock(BlockBehaviour.Properties.of().mapColor(MapColor.WARPED_NYLIUM)
                        .sound(SoundType.GRASS).strength(0.6F).randomTicks()));
        JUNGLE_PODZOL = PokecubeLegends.BLOCKS.register("jungle_podzol",
                () -> new SnowyDirtBlock(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_BROWN)
                        .sound(SoundType.GRAVEL).strength(0.6F)));
        JUNGLE_DIRT = PokecubeLegends.BLOCKS.register("jungle_dirt",
                () -> new BlockBase(MapColor.COLOR_BROWN, SoundType.GRAVEL, NoteBlockInstrument.HARP,
                        false, 0.5F, 0.5F));
        JUNGLE_COARSE_DIRT = PokecubeLegends.BLOCKS.register("jungle_coarse_dirt",
                () -> new BlockBase(MapColor.COLOR_BROWN, SoundType.GRAVEL, NoteBlockInstrument.HARP,
                        false, 0.5F, 0.5F));
        MUSHROOM_GRASS_BLOCK = PokecubeLegends.BLOCKS.register("mushroom_grass_block",
                () -> new MushroomGrassBlock(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_RED)
                        .sound(SoundType.GRASS).strength(0.6F).randomTicks()));
        FUNGAL_NYLIUM = PokecubeLegends.BLOCKS.register("fungal_nylium",
                () -> new FungalNyliumBlock(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_PINK)
                        .sound(SoundType.NYLIUM).strength(0.6F).randomTicks()));
        MUSHROOM_DIRT = PokecubeLegends.BLOCKS.register("mushroom_dirt",
                () -> new BlockBase(MapColor.COLOR_PURPLE, SoundType.GRAVEL, NoteBlockInstrument.HARP,
                        false, 0.5F, 0.5F));
        MUSHROOM_COARSE_DIRT = PokecubeLegends.BLOCKS.register("mushroom_coarse_dirt",
                () -> new BlockBase(MapColor.COLOR_PURPLE, SoundType.GRAVEL, NoteBlockInstrument.HARP,
                        false, 0.5F, 0.5F));
        ROOTED_MUSHROOM_DIRT = PokecubeLegends.BLOCKS.register("rooted_mushroom_dirt",
                () -> new RootedDirtBlock(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_PURPLE)
                        .sound(SoundType.ROOTED_DIRT).strength(0.9F)));

        TURQUOISE_GRAVEL = PokecubeLegends.BLOCKS.register("turquoise_gravel",
                () -> new FallingBlockBase(2243919, BlockBehaviour.Properties.of()
                        .mapColor(MapColor.COLOR_CYAN).sound(SoundType.GRAVEL).strength(0.6F)));

        // Azure Badlands
        AZURE_SAND = PokecubeLegends.BLOCKS.register("azure_sand",
                () -> new FallingSandBlockBase(1059926, BlockBehaviour.Properties.of()
                        .mapColor(MapColor.COLOR_BLUE).sound(SoundType.SAND).instrument(NoteBlockInstrument.SNARE)
                        .strength(0.6f)));
        AZURE_SANDSTONE = PokecubeLegends.BLOCKS.register("azure_sandstone",
                () -> new BlockBase(MapColor.COLOR_BLUE, SoundType.STONE, NoteBlockInstrument.BASEDRUM,
                        true, 0.8f, 0.8f));
        AZURE_SANDSTONE_STAIRS = PokecubeLegends.BLOCKS.register("azure_sandstone_stairs",
                () -> new ItemGenerator.GenericStairs(AZURE_SANDSTONE.get().defaultBlockState(),
                        BlockBehaviour.Properties.copy(AZURE_SANDSTONE.get())));
        AZURE_SANDSTONE_SLAB = PokecubeLegends.BLOCKS.register("azure_sandstone_slab",
                () -> new SlabBlock(BlockBehaviour.Properties.copy(AZURE_SANDSTONE.get())));
        AZURE_SANDSTONE_BUTTON = PokecubeLegends.BLOCKS.register("azure_sandstone_button",
                    () -> new ItemGenerator.GenericButton(BlockSetType.STONE, false, 20,
                            BlockBehaviour.Properties.of().sound(SoundType.STONE).pushReaction(PushReaction.DESTROY)
                                    .strength(0.5F).noCollission().requiresCorrectToolForDrops()));
        AZURE_SANDSTONE_PR_PLATE = PokecubeLegends.BLOCKS.register("azure_sandstone_pressure_plate",
                () -> new ItemGenerator.GenericPressurePlate(PressurePlateBlock.Sensitivity.MOBS,
                        LegendsBlockSetType.SANDSTONE_BLOCK_SET, BlockBehaviour.Properties.of()
                        .mapColor(MapColor.COLOR_BLUE).sound(SoundType.STONE).instrument(NoteBlockInstrument.BASEDRUM)
                        .pushReaction(PushReaction.DESTROY).strength(0.7F)
                        .noCollission().forceSolidOn().requiresCorrectToolForDrops()));

        SMOOTH_AZURE_SANDSTONE = PokecubeLegends.BLOCKS.register("smooth_azure_sandstone",
                () -> new BlockBase(MapColor.COLOR_BLUE, SoundType.STONE, NoteBlockInstrument.BASEDRUM,
                        true, 2.0F, 6.0F));
        SMOOTH_AZURE_SANDSTONE_STAIRS = PokecubeLegends.BLOCKS.register("smooth_azure_sandstone_stairs",
                () -> new ItemGenerator.GenericStairs(SMOOTH_AZURE_SANDSTONE.get().defaultBlockState(),
                        BlockBehaviour.Properties.copy(SMOOTH_AZURE_SANDSTONE.get())));
        SMOOTH_AZURE_SANDSTONE_SLAB = PokecubeLegends.BLOCKS.register("smooth_azure_sandstone_slab",
                () -> new SlabBlock(BlockBehaviour.Properties.copy(SMOOTH_AZURE_SANDSTONE.get())));

        // Blackened Beach
        BLACKENED_SAND = PokecubeLegends.BLOCKS.register("blackened_sand",
                () -> new FallingSandBlockBase(1447446, BlockBehaviour.Properties.of()
                        .mapColor(MapColor.COLOR_BLACK).sound(SoundType.SAND).instrument(NoteBlockInstrument.SNARE)
                        .strength(0.6f)));
        BLACKENED_SANDSTONE = PokecubeLegends.BLOCKS.register("blackened_sandstone",
                () -> new BlockBase(MapColor.COLOR_BLACK, SoundType.STONE, NoteBlockInstrument.BASEDRUM,
                        true, 0.8f, 0.8f));
        BLACKENED_SANDSTONE_STAIRS = PokecubeLegends.BLOCKS.register("blackened_sandstone_stairs",
                () -> new ItemGenerator.GenericStairs(BLACKENED_SANDSTONE.get().defaultBlockState(),
                        BlockBehaviour.Properties.copy(BLACKENED_SANDSTONE.get())));
        BLACKENED_SANDSTONE_SLAB = PokecubeLegends.BLOCKS.register("blackened_sandstone_slab",
                () -> new SlabBlock(BlockBehaviour.Properties.copy(BLACKENED_SANDSTONE.get())));
        BLACKENED_SANDSTONE_BUTTON = PokecubeLegends.BLOCKS.register("blackened_sandstone_button",
                () -> new ItemGenerator.GenericButton(BlockSetType.STONE, false, 20,
                        BlockBehaviour.Properties.of().sound(SoundType.STONE).pushReaction(PushReaction.DESTROY)
                                .strength(0.5F).noCollission().requiresCorrectToolForDrops()));
        BLACKENED_SANDSTONE_PR_PLATE = PokecubeLegends.BLOCKS.register("blackened_sandstone_pressure_plate",
                () -> new ItemGenerator.GenericPressurePlate(PressurePlateBlock.Sensitivity.MOBS,
                        LegendsBlockSetType.SANDSTONE_BLOCK_SET, BlockBehaviour.Properties.of()
                        .mapColor(MapColor.COLOR_BLACK).sound(SoundType.STONE).instrument(NoteBlockInstrument.BASEDRUM)
                        .pushReaction(PushReaction.DESTROY).strength(0.7F)
                        .noCollission().forceSolidOn().requiresCorrectToolForDrops()));

        SMOOTH_BLACKENED_SANDSTONE = PokecubeLegends.BLOCKS.register("smooth_blackened_sandstone",
                () -> new BlockBase(MapColor.COLOR_BLACK, SoundType.STONE, NoteBlockInstrument.BASEDRUM,
                        true, 2.0F, 6.0F));
        SMOOTH_BLACKENED_SANDSTONE_STAIRS = PokecubeLegends.BLOCKS.register("smooth_blackened_sandstone_stairs",
                () -> new ItemGenerator.GenericStairs(SMOOTH_BLACKENED_SANDSTONE.get().defaultBlockState(),
                        BlockBehaviour.Properties.copy(SMOOTH_BLACKENED_SANDSTONE.get())));
        SMOOTH_BLACKENED_SANDSTONE_SLAB = PokecubeLegends.BLOCKS.register("smooth_blackened_sandstone_slab",
                () -> new SlabBlock(BlockBehaviour.Properties.copy(SMOOTH_BLACKENED_SANDSTONE.get())));

        // Ultra Desert
        CRYSTALLIZED_SAND = PokecubeLegends.BLOCKS.register("crystallized_sand",
                () -> new FallingSandBlockBase(13753318, BlockBehaviour.Properties.of()
                        .mapColor(MapColor.SNOW).sound(SoundType.SAND).instrument(NoteBlockInstrument.SNARE)
                        .strength(0.6f)));
        CRYSTALLIZED_SANDSTONE = PokecubeLegends.BLOCKS.register("crystallized_sandstone",
                () -> new BlockBase(MapColor.SNOW, SoundType.STONE, NoteBlockInstrument.BASEDRUM,
                        true, 1.0F, 1.0F));
        CRYS_SANDSTONE_STAIRS = PokecubeLegends.BLOCKS.register("crystallized_sandstone_stairs",
                () -> new ItemGenerator.GenericStairs(CRYSTALLIZED_SANDSTONE.get().defaultBlockState(),
                        BlockBehaviour.Properties.copy(CRYSTALLIZED_SANDSTONE.get())));
        CRYS_SANDSTONE_SLAB = PokecubeLegends.BLOCKS.register("crystallized_sandstone_slab",
                () -> new SlabBlock(BlockBehaviour.Properties.copy(CRYSTALLIZED_SANDSTONE.get())));
        CRYS_SANDSTONE_BUTTON = PokecubeLegends.BLOCKS.register("crystallized_sandstone_button",
                () -> new ItemGenerator.GenericButton(BlockSetType.STONE, false, 20,
                        BlockBehaviour.Properties.of().sound(SoundType.STONE).pushReaction(PushReaction.DESTROY)
                                .strength(0.5F).noCollission().requiresCorrectToolForDrops()));
        CRYS_SANDSTONE_PR_PLATE = PokecubeLegends.BLOCKS.register("crystallized_sandstone_pressure_plate",
                () -> new ItemGenerator.GenericPressurePlate(PressurePlateBlock.Sensitivity.MOBS,
                        LegendsBlockSetType.SANDSTONE_BLOCK_SET, BlockBehaviour.Properties.of()
                        .mapColor(MapColor.SNOW).sound(SoundType.STONE).instrument(NoteBlockInstrument.BASEDRUM)
                        .pushReaction(PushReaction.DESTROY).strength(0.7F)
                        .noCollission().forceSolidOn().requiresCorrectToolForDrops()));

        SMOOTH_CRYS_SANDSTONE = PokecubeLegends.BLOCKS.register("crystallized_sandstone_smooth",
                () -> new BlockBase(MapColor.SNOW, SoundType.STONE, NoteBlockInstrument.BASEDRUM,
                        true, 2.0F, 6.0F));
        SMOOTH_CRYS_SANDSTONE_STAIRS = PokecubeLegends.BLOCKS.register("crystallized_sandstone_smooth_stairs",
                () -> new ItemGenerator.GenericStairs(SMOOTH_CRYS_SANDSTONE.get().defaultBlockState(),
                        BlockBehaviour.Properties.copy(SMOOTH_CRYS_SANDSTONE.get())));
        SMOOTH_CRYS_SANDSTONE_SLAB = PokecubeLegends.BLOCKS.register("crystallized_sandstone_smooth_slab",
                () -> new SlabBlock(BlockBehaviour.Properties.copy(SMOOTH_CRYS_SANDSTONE.get())));

        // Aquamarine
        AQUAMARINE_CLUSTER = PokecubeLegends.BLOCKS.register("aquamarine_cluster",
                () -> new AquamarineClusterBlock(14, 1, BlockBehaviour.Properties.of()
                        .mapColor(MapColor.COLOR_LIGHT_BLUE).sound(SoundType.AMETHYST_CLUSTER)
                        .strength(1.5F).noOcclusion().randomTicks().requiresCorrectToolForDrops()
                        .forceSolidOn().lightLevel(i -> 5)));
        LARGE_AQUAMARINE_BUD = PokecubeLegends.BLOCKS.register("large_aquamarine_bud",
                () -> new AquamarineClusterBlock(12, 2,
                        BlockBehaviour.Properties.copy(AQUAMARINE_CLUSTER.get())
                                .sound(SoundType.SMALL_AMETHYST_BUD).lightLevel(i -> 4)));
        MEDIUM_AQUAMARINE_BUD = PokecubeLegends.BLOCKS.register("medium_aquamarine_bud",
                () -> new AquamarineClusterBlock(11, 4,
                        BlockBehaviour.Properties.copy(AQUAMARINE_CLUSTER.get())
                                .sound(SoundType.SMALL_AMETHYST_BUD).lightLevel(i -> 2)));
        SMALL_AQUAMARINE_BUD = PokecubeLegends.BLOCKS.register("small_aquamarine_bud",
                () -> new AquamarineClusterBlock(7, 4,
                        BlockBehaviour.Properties.copy(AQUAMARINE_CLUSTER.get())
                                .sound(SoundType.SMALL_AMETHYST_BUD).lightLevel(i -> 1)));
        AQUAMARINE_CRYSTAL = PokecubeLegends.BLOCKS.register("aquamarine_crystal",
                () -> new AquamarineCrystalBlock(BlockBehaviour.Properties.copy(AQUAMARINE_CLUSTER.get())
                        .sound(SoundType.AMETHYST_CLUSTER).dynamicShape().lightLevel(i -> 2)));
        UNREFINED_AQUAMARINE = PokecubeLegends.BLOCKS.register("unrefined_aquamarine",
                () -> new UnrefinedAquamarineBlock(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_LIGHT_BLUE)
                        .sound(SoundType.AMETHYST).strength(1.5F)
                        .requiresCorrectToolForDrops().lightLevel(i -> 6)));
        BUDDING_AQUAMARINE = PokecubeLegends.BLOCKS.register("budding_aquamarine",
                () -> new BuddingAquamarineBlock(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_LIGHT_BLUE)
                        .sound(SoundType.AMETHYST).pushReaction(PushReaction.DESTROY).strength(1.5F)
                        .randomTicks().requiresCorrectToolForDrops().lightLevel(i -> 6)));
        UNREFINED_AQUAMARINE_STAIRS = PokecubeLegends.BLOCKS.register("unrefined_aquamarine_stairs",
                () -> new ItemGenerator.GenericStairs(UNREFINED_AQUAMARINE.get().defaultBlockState(),
                        BlockBehaviour.Properties.copy(UNREFINED_AQUAMARINE.get())));
        UNREFINED_AQUAMARINE_SLAB = PokecubeLegends.BLOCKS.register("unrefined_aquamarine_slab",
                () -> new SlabBlock(BlockBehaviour.Properties.copy(UNREFINED_AQUAMARINE.get())));
        AQUAMARINE_BLOCK = PokecubeLegends.BLOCKS.register("aquamarine_block",
                () -> new Block(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_LIGHT_BLUE)
                        .sound(SoundType.AMETHYST).strength(5.0F, 6.0F)
                        .requiresCorrectToolForDrops().lightLevel(i -> 12)));
        AQUAMARINE_STAIRS = PokecubeLegends.BLOCKS.register("aquamarine_stairs",
                () -> new ItemGenerator.GenericStairs(AQUAMARINE_BLOCK.get().defaultBlockState(),
                        BlockBehaviour.Properties.copy(AQUAMARINE_BLOCK.get())));
        AQUAMARINE_SLAB = PokecubeLegends.BLOCKS.register("aquamarine_slab",
                () -> new SlabBlock(BlockBehaviour.Properties.copy(AQUAMARINE_BLOCK.get())));
        AQUAMARINE_BUTTON = PokecubeLegends.BLOCKS.register("aquamarine_button",
                () -> new ItemGenerator.GenericButton(BlockSetType.STONE, false, 20,
                        BlockBehaviour.Properties.of().sound(SoundType.AMETHYST).pushReaction(PushReaction.DESTROY)
                                .strength(1.0F).noCollission().requiresCorrectToolForDrops().lightLevel(i -> 6)));
        AQUAMARINE_PR_PLATE = PokecubeLegends.BLOCKS.register("aquamarine_pressure_plate",
                () -> new ItemGenerator.GenericPressurePlate(PressurePlateBlock.Sensitivity.MOBS,
                        LegendsBlockSetType.AQUAMARINE_BLOCK_SET, BlockBehaviour.Properties.of()
                        .mapColor(MapColor.COLOR_LIGHT_BLUE).sound(SoundType.AMETHYST)
                        .instrument(NoteBlockInstrument.CHIME).pushReaction(PushReaction.DESTROY).strength(1.0F)
                        .noCollission().forceSolidOn().requiresCorrectToolForDrops().lightLevel(i -> 6)));

        // Distortic World
        DISTORTIC_GRASS_BLOCK = PokecubeLegends.BLOCKS.register("distortic_grass_block",
                () -> new DistorticGrassBlock(BlockBehaviour.Properties.of().mapColor(MapColor.TERRACOTTA_PINK)
                        .sound(SoundType.NYLIUM).instrument(NoteBlockInstrument.BASEDRUM)
                        .strength(1.0F, 2.0F).requiresCorrectToolForDrops().randomTicks()));
        CRACKED_DISTORTIC_STONE = PokecubeLegends.BLOCKS.register("cracked_distortic_stone",
                () -> new CrackedDistorticStone(BlockBehaviour.Properties.of().mapColor(MapColor.TERRACOTTA_BLACK)
                        .sound(SoundType.STONE).instrument(NoteBlockInstrument.BASEDRUM)
                        .strength(1.0F, 2.0F).requiresCorrectToolForDrops()));

        FRACTAL_ORE = PokecubeLegends.BLOCKS.register("fractal_ore",
                () -> new DropExperienceBlock(BlockBehaviour.Properties.of().mapColor(MapColor.TERRACOTTA_BLACK)
                        .sound(SoundType.STONE).instrument(NoteBlockInstrument.BASEDRUM)
                        .strength(3.0F, 3.0F).requiresCorrectToolForDrops(),
                        UniformInt.of(2, 7)));
        DISTORTIC_STONE = PokecubeLegends.BLOCKS.register("distortic_stone",
                () -> new DistorticStoneBlock(BlockBehaviour.Properties.of().mapColor(MapColor.TERRACOTTA_BLACK)
                        .sound(SoundType.STONE).instrument(NoteBlockInstrument.BASEDRUM)
                        .strength(2.5F, 5.0F).requiresCorrectToolForDrops()));
        DISTORTIC_STONE_STAIRS = PokecubeLegends.BLOCKS.register("distortic_stone_stairs",
                () -> new ItemGenerator.GenericStairs(DISTORTIC_STONE.get().defaultBlockState(),
                        BlockBehaviour.Properties.copy(DISTORTIC_STONE.get())));
        DISTORTIC_STONE_SLAB = PokecubeLegends.BLOCKS.register("distortic_stone_slab",
                () -> new SlabBlock(BlockBehaviour.Properties.copy(DISTORTIC_STONE.get())));

        DISTORTIC_MIRROR = PokecubeLegends.BLOCKS.register("distortic_mirror",
                () -> new BlockBase(MapColor.SNOW, SoundType.GLASS, NoteBlockInstrument.HAT,
                        true, 0.3F, 0.3F));

        CHISELED_DISTORTIC_MIRROR = PokecubeLegends.BLOCKS.register("chiseled_distortic_mirror",
                () -> new BlockBase(MapColor.SNOW, SoundType.GLASS, NoteBlockInstrument.HAT,
                        true, 1.5F, 1.5F));

        DISTORTIC_GLOWSTONE = PokecubeLegends.BLOCKS.register("distortic_glowstone",
                () -> new Block(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_LIGHT_BLUE)
                        .sound(SoundType.GLASS).instrument(NoteBlockInstrument.HAT)
                        .strength(1.5F, 1.5F).lightLevel(i -> 10)));

        AGED_LEAVES = PokecubeLegends.BLOCKS.register("aged_leaves",
                () -> new AgedLeavesBlock(20, BlockBehaviour.Properties.of().mapColor(MapColor.GOLD)
                        .sound(SoundType.GRASS).pushReaction(PushReaction.DESTROY)
                        .isSuffocating(PokecubeItems::never).isViewBlocking(PokecubeItems::never)
                        .isRedstoneConductor(PokecubeItems::never).isValidSpawn(PokecubeItems::ocelotOrParrot)
                        .strength(0.2F).ignitedByLava().noOcclusion().randomTicks()));

        AGED_LOG = PokecubeLegends.BLOCKS.register("aged_log",
                () -> Blocks.log(MapColor.TERRACOTTA_GREEN, MapColor.COLOR_BROWN));
        AGED_WOOD = PokecubeLegends.BLOCKS.register("aged_wood",
                () -> Blocks.log(MapColor.COLOR_BROWN, MapColor.COLOR_BROWN));
        STRIP_AGED_LOG = PokecubeLegends.BLOCKS.register("stripped_aged_log",
                () -> Blocks.log(MapColor.TERRACOTTA_GREEN, MapColor.TERRACOTTA_GREEN));
        STRIP_AGED_WOOD = PokecubeLegends.BLOCKS.register("stripped_aged_wood",
                () -> Blocks.log(MapColor.TERRACOTTA_GREEN, MapColor.TERRACOTTA_GREEN));

        AGED_BARREL = PokecubeLegends.BLOCKS.register("aged_barrel",
                () -> new GenericBarrel(BlockBehaviour.Properties.of().mapColor(MapColor.TERRACOTTA_GREEN)
                        .sound(SoundType.WOOD).instrument(NoteBlockInstrument.BASS)
                        .strength(2.5F).ignitedByLava()));
        AGED_BOOKSHELF = PokecubeLegends.BLOCKS.register("aged_bookshelf",
                () -> new GenericBookshelf(BlockBehaviour.Properties.of().mapColor(MapColor.TERRACOTTA_GREEN)
                        .sound(SoundType.WOOD).instrument(NoteBlockInstrument.BASS)
                        .strength(1.5F).ignitedByLava()));
        AGED_BOOKSHELF_EMPTY = PokecubeLegends.BLOCKS.register("aged_bookshelf_empty",
                () -> new GenericBookshelfEmpty(BlockBehaviour.Properties.of().mapColor(MapColor.TERRACOTTA_GREEN)
                        .sound(SoundType.WOOD).instrument(NoteBlockInstrument.BASS)
                        .strength(1.5F).ignitedByLava().requiresCorrectToolForDrops().dynamicShape()));

        AGED_PLANKS = PokecubeLegends.BLOCKS.register("aged_planks",
                () -> new Block(BlockBehaviour.Properties.of().mapColor(MapColor.TERRACOTTA_GREEN)
                        .sound(SoundType.WOOD).instrument(NoteBlockInstrument.BASS)
                        .strength(2.0F, 3.0F).ignitedByLava()));
        AGED_STAIRS = PokecubeLegends.BLOCKS.register("aged_stairs",
                () -> new ItemGenerator.GenericStairs(Blocks.OAK_STAIRS.defaultBlockState(),
                        BlockBehaviour.Properties.copy(AGED_PLANKS.get())));
        AGED_SLAB = PokecubeLegends.BLOCKS.register("aged_slab",
                () -> new SlabBlock(BlockBehaviour.Properties.copy(AGED_PLANKS.get())));
        AGED_FENCE = PokecubeLegends.BLOCKS.register("aged_fence",
                () -> new FenceBlock(BlockBehaviour.Properties.of().mapColor(MapColor.TERRACOTTA_GREEN)
                        .sound(SoundType.WOOD).instrument(NoteBlockInstrument.BASS)
                        .strength(2.0F, 3.0F).forceSolidOn().ignitedByLava()));
        AGED_FENCE_GATE = PokecubeLegends.BLOCKS.register("aged_fence_gate",
                () -> new FenceGateBlock(BlockBehaviour.Properties.of().mapColor(MapColor.TERRACOTTA_GREEN)
                        .sound(SoundType.WOOD).instrument(NoteBlockInstrument.BASS)
                        .strength(2.0F, 3.0F).forceSolidOn().ignitedByLava(), LegendsBlockSetType.AGED_WOOD_TYPE));
        AGED_BUTTON = PokecubeLegends.BLOCKS.register("aged_button",
                () -> new ItemGenerator.GenericButton(LegendsBlockSetType.AGED_BLOCK_SET, true, 30,
                        BlockBehaviour.Properties.of().mapColor(MapColor.TERRACOTTA_GREEN)
                                .sound(SoundType.WOOD).pushReaction(PushReaction.DESTROY)
                                .strength(0.5F).noCollission()));
        AGED_PR_PLATE = PokecubeLegends.BLOCKS.register("aged_pressure_plate",
                () -> new ItemGenerator.GenericPressurePlate(PressurePlateBlock.Sensitivity.EVERYTHING,
                        LegendsBlockSetType.AGED_BLOCK_SET, BlockBehaviour.Properties.of()
                        .mapColor(MapColor.TERRACOTTA_GREEN).sound(SoundType.WOOD)
                        .instrument(NoteBlockInstrument.BASS).pushReaction(PushReaction.DESTROY)
                        .strength(0.5F).noCollission().forceSolidOn().ignitedByLava()));

        AGED_TRAPDOOR = PokecubeLegends.BLOCKS.register("aged_trapdoor",
                () -> new ItemGenerator.GenericTrapDoor(LegendsBlockSetType.AGED_BLOCK_SET,
                        BlockBehaviour.Properties.of().mapColor(MapColor.TERRACOTTA_GREEN).sound(SoundType.WOOD)
                                .instrument(NoteBlockInstrument.BASS).strength(3.0F)
                                .noOcclusion().ignitedByLava().isValidSpawn(PokecubeItems::never)));
        AGED_DOOR = PokecubeLegends.BLOCKS.register("aged_door",
                () -> new ItemGenerator.GenericDoor(LegendsBlockSetType.AGED_BLOCK_SET,
                        BlockBehaviour.Properties.of().mapColor(MapColor.TERRACOTTA_GREEN)
                                .sound(SoundType.WOOD).instrument(NoteBlockInstrument.BASS)
                                .pushReaction(PushReaction.DESTROY).strength(3.0F)
                                .noOcclusion().ignitedByLava()));

        // Corrupted Blocks
        CORRUPTED_LEAVES = PokecubeLegends.BLOCKS.register("corrupted_leaves",
                () -> new CorruptedLeavesBlock(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_GRAY)
                        .sound(SoundType.SOUL_SAND).pushReaction(PushReaction.DESTROY)
                        .isSuffocating(PokecubeItems::never).isViewBlocking(PokecubeItems::never)
                        .isRedstoneConductor(PokecubeItems::never).isValidSpawn(PokecubeItems::ocelotOrParrot)
                        .strength(0.5F).ignitedByLava().noOcclusion().randomTicks()));

        CORRUPTED_LOG = PokecubeLegends.BLOCKS.register("corrupted_log",
                () -> Blocks.log(MapColor.WOOD, MapColor.COLOR_GRAY, SoundType.STEM));
        CORRUPTED_WOOD = PokecubeLegends.BLOCKS.register("corrupted_wood",
                () -> Blocks.log(MapColor.COLOR_GRAY, MapColor.COLOR_GRAY, SoundType.STEM));
        STRIP_CORRUPTED_LOG = PokecubeLegends.BLOCKS.register("stripped_corrupted_log",
                () -> Blocks.log(MapColor.WOOD, MapColor.WOOD, SoundType.STEM));
        STRIP_CORRUPTED_WOOD = PokecubeLegends.BLOCKS.register("stripped_corrupted_wood",
                () -> Blocks.log(MapColor.WOOD, MapColor.WOOD, SoundType.STEM));

        CORRUPTED_BARREL = PokecubeLegends.BLOCKS.register("corrupted_barrel",
                () -> new GenericBarrel(BlockBehaviour.Properties.of().mapColor(MapColor.WOOD)
                        .sound(SoundType.NETHER_WOOD).instrument(NoteBlockInstrument.BASS)
                        .strength(2.5F).ignitedByLava()));
        CORRUPTED_BOOKSHELF = PokecubeLegends.BLOCKS.register("corrupted_bookshelf",
                () -> new GenericBookshelf(BlockBehaviour.Properties.of().mapColor(MapColor.WOOD)
                        .sound(SoundType.NETHER_WOOD).instrument(NoteBlockInstrument.BASS)
                        .strength(1.5F).ignitedByLava()));
        CORRUPTED_BOOKSHELF_EMPTY = PokecubeLegends.BLOCKS.register("corrupted_bookshelf_empty",
                () -> new GenericBookshelfEmpty(BlockBehaviour.Properties.of().mapColor(MapColor.WOOD)
                        .sound(SoundType.NETHER_WOOD).instrument(NoteBlockInstrument.BASS)
                        .strength(1.5F).ignitedByLava().requiresCorrectToolForDrops().dynamicShape()));

        CORRUPTED_PLANKS = PokecubeLegends.BLOCKS.register("corrupted_planks",
                () -> new Block(BlockBehaviour.Properties.of().mapColor(MapColor.WOOD)
                        .sound(SoundType.NETHER_WOOD).instrument(NoteBlockInstrument.BASS)
                        .strength(2.0F, 3.0F).ignitedByLava()));
        CORRUPTED_STAIRS = PokecubeLegends.BLOCKS.register("corrupted_stairs",
                () -> new ItemGenerator.GenericStairs(Blocks.OAK_STAIRS.defaultBlockState(),
                        BlockBehaviour.Properties.copy(CORRUPTED_PLANKS.get())));
        CORRUPTED_SLAB = PokecubeLegends.BLOCKS.register("corrupted_slab",
                () -> new SlabBlock(BlockBehaviour.Properties.copy(CORRUPTED_PLANKS.get())));
        CORRUPTED_FENCE = PokecubeLegends.BLOCKS.register("corrupted_fence",
                () -> new FenceBlock(BlockBehaviour.Properties.of().mapColor(MapColor.WOOD)
                        .sound(SoundType.NETHER_WOOD).instrument(NoteBlockInstrument.BASS)
                        .strength(2.0F, 3.0F).forceSolidOn().ignitedByLava()));
        CORRUPTED_FENCE_GATE = PokecubeLegends.BLOCKS.register("corrupted_fence_gate",
                () -> new FenceGateBlock(BlockBehaviour.Properties.of().mapColor(MapColor.WOOD)
                        .sound(SoundType.NETHER_WOOD).instrument(NoteBlockInstrument.BASS)
                        .strength(2.0F, 3.0F).forceSolidOn().ignitedByLava(), LegendsBlockSetType.CORRUPTED_WOOD_TYPE));
        CORRUPTED_BUTTON = PokecubeLegends.BLOCKS.register("corrupted_button",
                () -> new ItemGenerator.GenericButton(LegendsBlockSetType.CORRUPTED_BLOCK_SET, true, 30,
                        BlockBehaviour.Properties.of().mapColor(MapColor.WOOD)
                                .sound(SoundType.NETHER_WOOD).pushReaction(PushReaction.DESTROY)
                                .strength(0.5F).noCollission()));
        CORRUPTED_PR_PLATE = PokecubeLegends.BLOCKS.register("corrupted_pressure_plate",
                () -> new ItemGenerator.GenericPressurePlate(PressurePlateBlock.Sensitivity.EVERYTHING,
                        LegendsBlockSetType.CORRUPTED_BLOCK_SET, BlockBehaviour.Properties.of().mapColor(MapColor.WOOD)
                        .sound(SoundType.NETHER_WOOD).instrument(NoteBlockInstrument.BASS)
                        .pushReaction(PushReaction.DESTROY).strength(0.5F)
                        .noCollission().forceSolidOn().ignitedByLava()));

        CORRUPTED_TRAPDOOR = PokecubeLegends.BLOCKS.register("corrupted_trapdoor",
                () -> new ItemGenerator.GenericTrapDoor(LegendsBlockSetType.CORRUPTED_BLOCK_SET,
                        BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).sound(SoundType.NETHER_WOOD)
                                .instrument(NoteBlockInstrument.BASS).strength(3.0F)
                                .noOcclusion().ignitedByLava().isValidSpawn(PokecubeItems::never)));
        CORRUPTED_DOOR = PokecubeLegends.BLOCKS.register("corrupted_door",
                () -> new ItemGenerator.GenericDoor(LegendsBlockSetType.CORRUPTED_BLOCK_SET,
                        BlockBehaviour.Properties.of().mapColor(MapColor.WOOD)
                                .sound(SoundType.NETHER_WOOD).instrument(NoteBlockInstrument.BASS)
                                .pushReaction(PushReaction.DESTROY).strength(3.0F)
                                .noOcclusion().ignitedByLava()));

        // Distorted Blocks
        DISTORTIC_LEAVES = PokecubeLegends.BLOCKS.register("distortic_leaves",
                () -> new LeavesBlock(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_PURPLE)
                        .sound(SoundType.GRASS).pushReaction(PushReaction.DESTROY)
                        .isSuffocating(PokecubeItems::never).isViewBlocking(PokecubeItems::never)
                        .isRedstoneConductor(PokecubeItems::never).isValidSpawn(PokecubeItems::ocelotOrParrot)
                        .strength(0.2F).ignitedByLava().noOcclusion().randomTicks()));

        DISTORTIC_LOG = PokecubeLegends.BLOCKS.register("distortic_log",
                () -> Blocks.log(MapColor.COLOR_BLUE, MapColor.COLOR_BLUE, SoundType.STEM));
        DISTORTIC_WOOD = PokecubeLegends.BLOCKS.register("distortic_wood",
                () -> Blocks.log(MapColor.COLOR_BLUE, MapColor.COLOR_BLUE, SoundType.STEM));
        STRIP_DISTORTIC_LOG = PokecubeLegends.BLOCKS.register("stripped_distortic_log",
                () -> Blocks.log(MapColor.COLOR_BLUE, MapColor.COLOR_BLUE, SoundType.STEM));
        STRIP_DISTORTIC_WOOD = PokecubeLegends.BLOCKS.register("stripped_distortic_wood",
                () -> Blocks.log(MapColor.COLOR_BLUE, MapColor.COLOR_BLUE, SoundType.STEM));

        DISTORTIC_BARREL = PokecubeLegends.BLOCKS.register("distortic_barrel",
                () -> new GenericBarrel(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_BLUE)
                        .sound(SoundType.NETHER_WOOD).instrument(NoteBlockInstrument.BASS)
                        .strength(2.5F).ignitedByLava()));
        DISTORTIC_BOOKSHELF = PokecubeLegends.BLOCKS.register("distortic_bookshelf",
                () -> new GenericBookshelf(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_BLUE)
                        .sound(SoundType.NETHER_WOOD).instrument(NoteBlockInstrument.BASS)
                        .strength(1.5F).ignitedByLava()));
        DISTORTIC_BOOKSHELF_EMPTY = PokecubeLegends.BLOCKS.register("distortic_bookshelf_empty",
                () -> new GenericBookshelfEmpty(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_BLUE)
                        .sound(SoundType.NETHER_WOOD).instrument(NoteBlockInstrument.BASS)
                        .strength(1.5F).ignitedByLava().requiresCorrectToolForDrops().dynamicShape()));

        DISTORTIC_PLANKS = PokecubeLegends.BLOCKS.register("distortic_planks",
                () -> new Block(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_BLUE)
                        .sound(SoundType.NETHER_WOOD).instrument(NoteBlockInstrument.BASS)
                        .strength(2.0F, 3.0F).ignitedByLava()));
        DISTORTIC_STAIRS = PokecubeLegends.BLOCKS.register("distortic_stairs",
                () -> new ItemGenerator.GenericStairs(Blocks.OAK_STAIRS.defaultBlockState(),
                        BlockBehaviour.Properties.copy(DISTORTIC_PLANKS.get())));
        DISTORTIC_SLAB = PokecubeLegends.BLOCKS.register("distortic_slab",
                () -> new SlabBlock(BlockBehaviour.Properties.copy(DISTORTIC_PLANKS.get())));
        DISTORTIC_FENCE = PokecubeLegends.BLOCKS.register("distortic_fence",
                () -> new FenceBlock(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_BLUE)
                        .sound(SoundType.WOOD).instrument(NoteBlockInstrument.BASS)
                        .strength(2.0F, 3.0F).forceSolidOn().ignitedByLava()));
        DISTORTIC_FENCE_GATE = PokecubeLegends.BLOCKS.register("distortic_fence_gate",
                () -> new FenceGateBlock(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_BLUE)
                        .sound(SoundType.NETHER_WOOD).instrument(NoteBlockInstrument.BASS)
                        .strength(2.0F, 3.0F).forceSolidOn().ignitedByLava(), LegendsBlockSetType.DISTORTIC_WOOD_TYPE));
        DISTORTIC_BUTTON = PokecubeLegends.BLOCKS.register("distortic_button",
                () -> new ItemGenerator.GenericButton(LegendsBlockSetType.DISTORTIC_BLOCK_SET, true, 30,
                        BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_BLUE)
                                .sound(SoundType.NETHER_WOOD).pushReaction(PushReaction.DESTROY)
                                .strength(0.5F).noCollission()));
        DISTORTIC_PR_PLATE = PokecubeLegends.BLOCKS.register("distortic_pressure_plate",
                () -> new ItemGenerator.GenericPressurePlate(PressurePlateBlock.Sensitivity.EVERYTHING,
                        LegendsBlockSetType.DISTORTIC_BLOCK_SET, BlockBehaviour.Properties.of()
                        .mapColor(MapColor.COLOR_BLUE).sound(SoundType.NETHER_WOOD).instrument(NoteBlockInstrument.BASS)
                        .pushReaction(PushReaction.DESTROY).strength(0.5F)
                        .noCollission().forceSolidOn().ignitedByLava()));

        DISTORTIC_TRAPDOOR = PokecubeLegends.BLOCKS.register("distortic_trapdoor",
                () -> new ItemGenerator.GenericTrapDoor(LegendsBlockSetType.DISTORTIC_BLOCK_SET,
                        BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_BLUE).sound(SoundType.NETHER_WOOD)
                                .instrument(NoteBlockInstrument.BASS).strength(3.0F)
                                .noOcclusion().ignitedByLava().isValidSpawn(PokecubeItems::never)));
        DISTORTIC_DOOR = PokecubeLegends.BLOCKS.register("distortic_door",
                () -> new ItemGenerator.GenericDoor(LegendsBlockSetType.DISTORTIC_BLOCK_SET,
                        BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_BLUE)
                                .sound(SoundType.NETHER_WOOD).instrument(NoteBlockInstrument.BASS)
                                .pushReaction(PushReaction.DESTROY).strength(3.0F)
                                .noOcclusion().ignitedByLava()));

        // Inverted Blocks
        INVERTED_LEAVES = PokecubeLegends.BLOCKS.register("inverted_leaves",
                () -> new LeavesBlock(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_LIGHT_BLUE)
                        .sound(SoundType.GRASS).pushReaction(PushReaction.DESTROY)
                        .isSuffocating(PokecubeItems::never).isViewBlocking(PokecubeItems::never)
                        .isRedstoneConductor(PokecubeItems::never).isValidSpawn(PokecubeItems::ocelotOrParrot)
                        .strength(0.2F).ignitedByLava().noOcclusion().randomTicks()));

        INVERTED_LOG = PokecubeLegends.BLOCKS.register("inverted_log",
                () -> Blocks.log(MapColor.TERRACOTTA_LIGHT_BLUE, MapColor.TERRACOTTA_LIGHT_BLUE));
        INVERTED_WOOD = PokecubeLegends.BLOCKS.register("inverted_wood",
                () -> Blocks.log(MapColor.TERRACOTTA_LIGHT_BLUE, MapColor.TERRACOTTA_LIGHT_BLUE));
        STRIP_INVERTED_LOG = PokecubeLegends.BLOCKS.register("stripped_inverted_log",
                () -> Blocks.log(MapColor.TERRACOTTA_LIGHT_BLUE, MapColor.TERRACOTTA_LIGHT_BLUE));
        STRIP_INVERTED_WOOD = PokecubeLegends.BLOCKS.register("stripped_inverted_wood",
                () -> Blocks.log(MapColor.TERRACOTTA_LIGHT_BLUE, MapColor.TERRACOTTA_LIGHT_BLUE));

        INVERTED_BARREL = PokecubeLegends.BLOCKS.register("inverted_barrel",
                () -> new GenericBarrel(BlockBehaviour.Properties.of().mapColor(MapColor.TERRACOTTA_LIGHT_BLUE)
                        .sound(SoundType.WOOD).instrument(NoteBlockInstrument.BASS)
                        .strength(2.5F).ignitedByLava()));
        INVERTED_BOOKSHELF = PokecubeLegends.BLOCKS.register("inverted_bookshelf",
                () -> new GenericBookshelf(BlockBehaviour.Properties.of().mapColor(MapColor.TERRACOTTA_LIGHT_BLUE)
                        .sound(SoundType.WOOD).instrument(NoteBlockInstrument.BASS)
                        .strength(1.5F).ignitedByLava()));
        INVERTED_BOOKSHELF_EMPTY = PokecubeLegends.BLOCKS.register("inverted_bookshelf_empty",
                () -> new GenericBookshelfEmpty(BlockBehaviour.Properties.of().mapColor(MapColor.TERRACOTTA_LIGHT_BLUE)
                        .sound(SoundType.WOOD).instrument(NoteBlockInstrument.BASS)
                        .strength(1.5F).ignitedByLava().requiresCorrectToolForDrops().dynamicShape()));

        INVERTED_PLANKS = PokecubeLegends.BLOCKS.register("inverted_planks",
                () -> new Block(BlockBehaviour.Properties.of().mapColor(MapColor.TERRACOTTA_LIGHT_BLUE)
                        .sound(SoundType.WOOD).instrument(NoteBlockInstrument.BASS)
                        .strength(2.0F, 3.0F).ignitedByLava()));
        INVERTED_STAIRS = PokecubeLegends.BLOCKS.register("inverted_stairs",
                () -> new ItemGenerator.GenericStairs(Blocks.OAK_STAIRS.defaultBlockState(),
                        BlockBehaviour.Properties.copy(INVERTED_PLANKS.get())));
        INVERTED_SLAB = PokecubeLegends.BLOCKS.register("inverted_slab",
                () -> new SlabBlock(BlockBehaviour.Properties.copy(INVERTED_PLANKS.get())));
        INVERTED_FENCE = PokecubeLegends.BLOCKS.register("inverted_fence",
                () -> new FenceBlock(BlockBehaviour.Properties.of().mapColor(MapColor.TERRACOTTA_LIGHT_BLUE)
                        .sound(SoundType.WOOD).instrument(NoteBlockInstrument.BASS)
                        .strength(2.0F, 3.0F).forceSolidOn().ignitedByLava()));
        INVERTED_FENCE_GATE = PokecubeLegends.BLOCKS.register("inverted_fence_gate",
                () -> new FenceGateBlock(BlockBehaviour.Properties.of().mapColor(MapColor.TERRACOTTA_LIGHT_BLUE)
                        .sound(SoundType.WOOD).instrument(NoteBlockInstrument.BASS)
                        .strength(2.0F, 3.0F).forceSolidOn().ignitedByLava(), LegendsBlockSetType.INVERTED_WOOD_TYPE));
        INVERTED_BUTTON = PokecubeLegends.BLOCKS.register("inverted_button",
                () -> new ItemGenerator.GenericButton(LegendsBlockSetType.INVERTED_BLOCK_SET, true, 30,
                        BlockBehaviour.Properties.of().mapColor(MapColor.TERRACOTTA_LIGHT_BLUE)
                                .sound(SoundType.WOOD).pushReaction(PushReaction.DESTROY)
                                .strength(0.5F).noCollission()));
        INVERTED_PR_PLATE = PokecubeLegends.BLOCKS.register("inverted_pressure_plate",
                () -> new ItemGenerator.GenericPressurePlate(PressurePlateBlock.Sensitivity.EVERYTHING,
                        LegendsBlockSetType.INVERTED_BLOCK_SET, BlockBehaviour.Properties.of()
                        .mapColor(MapColor.TERRACOTTA_LIGHT_BLUE).sound(SoundType.WOOD)
                        .instrument(NoteBlockInstrument.BASS).pushReaction(PushReaction.DESTROY).strength(0.5F)
                        .noCollission().forceSolidOn().ignitedByLava()));

        INVERTED_TRAPDOOR = PokecubeLegends.BLOCKS.register("inverted_trapdoor",
                () -> new ItemGenerator.GenericTrapDoor(LegendsBlockSetType.INVERTED_BLOCK_SET,
                        BlockBehaviour.Properties.of().mapColor(MapColor.TERRACOTTA_LIGHT_BLUE).sound(SoundType.WOOD)
                                .instrument(NoteBlockInstrument.BASS).strength(3.0F)
                                .noOcclusion().ignitedByLava().isValidSpawn(PokecubeItems::never)));
        INVERTED_DOOR = PokecubeLegends.BLOCKS.register("inverted_door",
                () -> new ItemGenerator.GenericDoor(LegendsBlockSetType.INVERTED_BLOCK_SET,
                        BlockBehaviour.Properties.of().mapColor(MapColor.TERRACOTTA_LIGHT_BLUE)
                                .sound(SoundType.WOOD).instrument(NoteBlockInstrument.BASS)
                                .pushReaction(PushReaction.DESTROY).strength(3.0F)
                                .noOcclusion().ignitedByLava()));

        // MIRAGE Blocks
        MIRAGE_LEAVES = PokecubeLegends.BLOCKS.register("mirage_leaves",
                () -> new MirageLeavesBlock(3, BlockBehaviour.Properties.of()
                        .mapColor(MapColor.COLOR_LIGHT_BLUE).sound(SoundType.GRASS).pushReaction(PushReaction.DESTROY)
                        .isSuffocating(PokecubeItems::never).isViewBlocking(PokecubeItems::never)
                        .isRedstoneConductor(PokecubeItems::never).isValidSpawn(PokecubeItems::ocelotOrParrot)
                        .strength(0.2F).ignitedByLava().noOcclusion().randomTicks()));

        MIRAGE_LOG = PokecubeLegends.BLOCKS.register("mirage_log",
                () -> Blocks.log(MapColor.SAND, MapColor.COLOR_LIGHT_BLUE));
        MIRAGE_WOOD = PokecubeLegends.BLOCKS.register("mirage_wood",
                () -> Blocks.log(MapColor.COLOR_LIGHT_BLUE, MapColor.COLOR_LIGHT_BLUE));
        STRIP_MIRAGE_LOG = PokecubeLegends.BLOCKS.register("stripped_mirage_log",
                () -> Blocks.log(MapColor.SAND, MapColor.SNOW));
        STRIP_MIRAGE_WOOD = PokecubeLegends.BLOCKS.register("stripped_mirage_wood",
                () -> Blocks.log(MapColor.SNOW, MapColor.SNOW));

        MIRAGE_BARREL = PokecubeLegends.BLOCKS.register("mirage_barrel",
                () -> new GenericBarrel(BlockBehaviour.Properties.of().mapColor(MapColor.SAND)
                        .sound(SoundType.WOOD).instrument(NoteBlockInstrument.BASS)
                        .strength(2.5F).ignitedByLava()));
        MIRAGE_BOOKSHELF = PokecubeLegends.BLOCKS.register("mirage_bookshelf",
                () -> new GenericBookshelf(BlockBehaviour.Properties.of().mapColor(MapColor.SAND)
                        .sound(SoundType.WOOD).instrument(NoteBlockInstrument.BASS)
                        .strength(1.5F).ignitedByLava()));
        MIRAGE_BOOKSHELF_EMPTY = PokecubeLegends.BLOCKS.register("mirage_bookshelf_empty",
                () -> new GenericBookshelfEmpty(BlockBehaviour.Properties.of().mapColor(MapColor.SAND)
                        .sound(SoundType.WOOD).instrument(NoteBlockInstrument.BASS)
                        .strength(1.5F).ignitedByLava().requiresCorrectToolForDrops().dynamicShape()));

        MIRAGE_PLANKS = PokecubeLegends.BLOCKS.register("mirage_planks",
                () -> new Block(BlockBehaviour.Properties.of().mapColor(MapColor.SAND)
                        .sound(SoundType.WOOD).instrument(NoteBlockInstrument.BASS)
                        .strength(2.0F, 3.0F).ignitedByLava()));
        MIRAGE_STAIRS = PokecubeLegends.BLOCKS.register("mirage_stairs",
                () -> new ItemGenerator.GenericStairs(Blocks.OAK_STAIRS.defaultBlockState(),
                BlockBehaviour.Properties.copy(MIRAGE_PLANKS.get())));
        MIRAGE_SLAB = PokecubeLegends.BLOCKS.register("mirage_slab",
                () -> new SlabBlock(BlockBehaviour.Properties.copy(MIRAGE_PLANKS.get())));
        MIRAGE_FENCE = PokecubeLegends.BLOCKS.register("mirage_fence",
                () -> new FenceBlock(BlockBehaviour.Properties.of().mapColor(MapColor.SAND)
                        .sound(SoundType.WOOD).instrument(NoteBlockInstrument.BASS)
                        .strength(2.0F, 3.0F).forceSolidOn().ignitedByLava()));
        MIRAGE_FENCE_GATE = PokecubeLegends.BLOCKS.register("mirage_fence_gate",
                () -> new FenceGateBlock(BlockBehaviour.Properties.of().mapColor(MapColor.SAND)
                        .sound(SoundType.WOOD).instrument(NoteBlockInstrument.BASS)
                        .strength(2.0F, 3.0F).forceSolidOn().ignitedByLava(), LegendsBlockSetType.MIRAGE_WOOD_TYPE));
        MIRAGE_BUTTON = PokecubeLegends.BLOCKS.register("mirage_button",
                () -> new ItemGenerator.GenericButton(LegendsBlockSetType.MIRAGE_BLOCK_SET, true, 30,
                        BlockBehaviour.Properties.of().mapColor(MapColor.SAND)
                                .sound(SoundType.WOOD).pushReaction(PushReaction.DESTROY)
                                .strength(0.5F).noCollission()));
        MIRAGE_PR_PLATE = PokecubeLegends.BLOCKS.register("mirage_pressure_plate",
                () -> new ItemGenerator.GenericPressurePlate(PressurePlateBlock.Sensitivity.EVERYTHING,
                        LegendsBlockSetType.MIRAGE_BLOCK_SET, BlockBehaviour.Properties.of().mapColor(MapColor.SAND)
                        .sound(SoundType.WOOD).instrument(NoteBlockInstrument.BASS).pushReaction(PushReaction.DESTROY)
                        .strength(0.5F).noCollission().forceSolidOn().ignitedByLava()));

        MIRAGE_TRAPDOOR = PokecubeLegends.BLOCKS.register("mirage_trapdoor",
                () -> new ItemGenerator.GenericTrapDoor(LegendsBlockSetType.MIRAGE_BLOCK_SET,
                        BlockBehaviour.Properties.of().mapColor(MapColor.SAND).sound(SoundType.WOOD)
                                .instrument(NoteBlockInstrument.BASS).strength(3.0F)
                                .noOcclusion().ignitedByLava().isValidSpawn(PokecubeItems::never)));
        MIRAGE_DOOR = PokecubeLegends.BLOCKS.register("mirage_door",
                () -> new ItemGenerator.GenericDoor(LegendsBlockSetType.MIRAGE_BLOCK_SET,
                        BlockBehaviour.Properties.of().mapColor(MapColor.SAND)
                                .sound(SoundType.WOOD).instrument(NoteBlockInstrument.BASS)
                                .pushReaction(PushReaction.DESTROY).strength(3.0F)
                                .noOcclusion().ignitedByLava()));

        // Temporal Blocks
        TEMPORAL_LEAVES = PokecubeLegends.BLOCKS.register("temporal_leaves",
                () -> new LeavesBlock(BlockBehaviour.Properties.of().mapColor(MapColor.WARPED_NYLIUM)
                        .sound(SoundType.GRASS).pushReaction(PushReaction.DESTROY)
                        .isSuffocating(PokecubeItems::never).isViewBlocking(PokecubeItems::never)
                        .isRedstoneConductor(PokecubeItems::never).isValidSpawn(PokecubeItems::ocelotOrParrot)
                        .strength(0.2F).ignitedByLava().noOcclusion().randomTicks()));

        TEMPORAL_LOG = PokecubeLegends.BLOCKS.register("temporal_log",
                () -> Blocks.log(MapColor.WARPED_NYLIUM, MapColor.COLOR_BROWN));
        TEMPORAL_WOOD = PokecubeLegends.BLOCKS.register("temporal_wood",
                () -> Blocks.log(MapColor.WARPED_NYLIUM, MapColor.COLOR_BROWN));
        STRIP_TEMPORAL_LOG = PokecubeLegends.BLOCKS.register("stripped_temporal_log",
                () -> Blocks.log(MapColor.WARPED_NYLIUM, MapColor.WARPED_NYLIUM));
        STRIP_TEMPORAL_WOOD = PokecubeLegends.BLOCKS.register("stripped_temporal_wood",
                () -> Blocks.log(MapColor.WARPED_NYLIUM, MapColor.WARPED_NYLIUM));

        TEMPORAL_BARREL = PokecubeLegends.BLOCKS.register("temporal_barrel",
                () -> new GenericBarrel(BlockBehaviour.Properties.of().mapColor(MapColor.WARPED_NYLIUM)
                        .sound(SoundType.WOOD).instrument(NoteBlockInstrument.BASS)
                        .strength(2.5F).ignitedByLava()));
        TEMPORAL_BOOKSHELF = PokecubeLegends.BLOCKS.register("temporal_bookshelf",
                () -> new GenericBookshelf(BlockBehaviour.Properties.of().mapColor(MapColor.WARPED_NYLIUM)
                        .sound(SoundType.WOOD).instrument(NoteBlockInstrument.BASS)
                        .strength(1.5F).ignitedByLava()));
        TEMPORAL_BOOKSHELF_EMPTY = PokecubeLegends.BLOCKS.register("temporal_bookshelf_empty",
                () -> new GenericBookshelfEmpty(BlockBehaviour.Properties.of().mapColor(MapColor.WARPED_NYLIUM)
                        .sound(SoundType.WOOD).instrument(NoteBlockInstrument.BASS)
                        .strength(1.5F).ignitedByLava().requiresCorrectToolForDrops().dynamicShape()));

        TEMPORAL_PLANKS = PokecubeLegends.BLOCKS.register("temporal_planks",
                () -> new Block(BlockBehaviour.Properties.of().mapColor(MapColor.WARPED_NYLIUM)
                        .sound(SoundType.WOOD).instrument(NoteBlockInstrument.BASS)
                        .strength(2.0F, 3.0F).ignitedByLava()));
        TEMPORAL_STAIRS = PokecubeLegends.BLOCKS.register("temporal_stairs",
                () -> new ItemGenerator.GenericStairs(Blocks.OAK_STAIRS.defaultBlockState(),
                        BlockBehaviour.Properties.copy(TEMPORAL_PLANKS.get())));
        TEMPORAL_SLAB = PokecubeLegends.BLOCKS.register("temporal_slab",
                () -> new SlabBlock(BlockBehaviour.Properties.copy(TEMPORAL_PLANKS.get())));
        TEMPORAL_FENCE = PokecubeLegends.BLOCKS.register("temporal_fence",
                () -> new FenceBlock(BlockBehaviour.Properties.of().mapColor(MapColor.WARPED_NYLIUM)
                        .sound(SoundType.WOOD).instrument(NoteBlockInstrument.BASS)
                        .strength(2.0F, 3.0F).forceSolidOn().ignitedByLava()));
        TEMPORAL_FENCE_GATE = PokecubeLegends.BLOCKS.register("temporal_fence_gate",
                () -> new FenceGateBlock(BlockBehaviour.Properties.of().mapColor(MapColor.WARPED_NYLIUM)
                        .sound(SoundType.WOOD).instrument(NoteBlockInstrument.BASS)
                        .strength(2.0F, 3.0F).forceSolidOn().ignitedByLava(), LegendsBlockSetType.TEMPORAL_WOOD_TYPE));
        TEMPORAL_BUTTON = PokecubeLegends.BLOCKS.register("temporal_button",
                () -> new ItemGenerator.GenericButton(LegendsBlockSetType.TEMPORAL_BLOCK_SET, true, 30,
                        BlockBehaviour.Properties.of().mapColor(MapColor.WARPED_NYLIUM)
                                .sound(SoundType.WOOD).pushReaction(PushReaction.DESTROY)
                                .strength(0.5F).noCollission()));
        TEMPORAL_PR_PLATE = PokecubeLegends.BLOCKS.register("temporal_pressure_plate",
                () -> new ItemGenerator.GenericPressurePlate(PressurePlateBlock.Sensitivity.EVERYTHING,
                        LegendsBlockSetType.TEMPORAL_BLOCK_SET, BlockBehaviour.Properties.of()
                        .mapColor(MapColor.WARPED_NYLIUM).sound(SoundType.WOOD).instrument(NoteBlockInstrument.BASS)
                        .pushReaction(PushReaction.DESTROY).strength(0.5F)
                        .noCollission().forceSolidOn().ignitedByLava()));

        TEMPORAL_TRAPDOOR = PokecubeLegends.BLOCKS.register("temporal_trapdoor",
                () -> new ItemGenerator.GenericTrapDoor(LegendsBlockSetType.TEMPORAL_BLOCK_SET,
                        BlockBehaviour.Properties.of().mapColor(MapColor.WARPED_NYLIUM).sound(SoundType.WOOD)
                                .instrument(NoteBlockInstrument.BASS).strength(3.0F)
                                .noOcclusion().ignitedByLava().isValidSpawn(PokecubeItems::never)));
        TEMPORAL_DOOR = PokecubeLegends.BLOCKS.register("temporal_door",
                () -> new ItemGenerator.GenericDoor(LegendsBlockSetType.TEMPORAL_BLOCK_SET,
                        BlockBehaviour.Properties.of().mapColor(MapColor.WARPED_NYLIUM)
                                .sound(SoundType.WOOD).instrument(NoteBlockInstrument.BASS)
                                .pushReaction(PushReaction.DESTROY).strength(3.0F)
                                .noOcclusion().ignitedByLava()));

        // Dyna Leaves
        DYNA_LEAVES_RED = PokecubeLegends.BLOCKS.register("dyna_leaves_red",
                () -> new RedDynaLeavesBlock(15, BlockBehaviour.Properties.of()
                        .mapColor(MapColor.COLOR_RED).sound(SoundType.AZALEA_LEAVES).pushReaction(PushReaction.DESTROY)
                        .isSuffocating(PokecubeItems::never).isViewBlocking(PokecubeItems::never)
                        .isRedstoneConductor(PokecubeItems::never).isValidSpawn(PokecubeItems::ocelotOrParrot)
                        .strength(0.2F).ignitedByLava().noOcclusion().randomTicks()));
        DYNA_LEAVES_PINK = PokecubeLegends.BLOCKS.register("dyna_leaves_pink",
                () -> new PinkDynaLeavesBlock(15, BlockBehaviour.Properties.of()
                        .mapColor(MapColor.COLOR_MAGENTA).sound(SoundType.AZALEA_LEAVES).pushReaction(PushReaction.DESTROY)
                        .isSuffocating(PokecubeItems::never).isViewBlocking(PokecubeItems::never)
                        .isRedstoneConductor(PokecubeItems::never).isValidSpawn(PokecubeItems::ocelotOrParrot)
                        .strength(0.2F).ignitedByLava().noOcclusion().randomTicks()));
        DYNA_LEAVES_PASTEL_PINK = PokecubeLegends.BLOCKS.register("dyna_leaves_pastel_pink",
                () -> new PastelPinkDynaLeavesBlock(15, BlockBehaviour.Properties.of()
                        .mapColor(MapColor.COLOR_PINK).sound(SoundType.AZALEA_LEAVES).pushReaction(PushReaction.DESTROY)
                        .isSuffocating(PokecubeItems::never).isViewBlocking(PokecubeItems::never)
                        .isRedstoneConductor(PokecubeItems::never).isValidSpawn(PokecubeItems::ocelotOrParrot)
                        .strength(0.2F).ignitedByLava().noOcclusion().randomTicks()));

        // Decorations Creative Tab -
        INFECTED_TORCH = PokecubeLegends.NO_ITEM_BLOCKS.register("infected_torch",
                () -> new InfectedTorch(ParticleTypes.DRAGON_BREATH, ParticleTypes.SMOKE,
                        BlockBehaviour.Properties.of().sound(SoundType.NETHER_WOOD).pushReaction(PushReaction.DESTROY)
                        .noCollission().instabreak().lightLevel(i -> 12)));
        INFECTED_TORCH_WALL = PokecubeLegends.NO_ITEM_BLOCKS.register("infected_torch_wall",
                () -> new InfectedTorchWall(ParticleTypes.DRAGON_BREATH, ParticleTypes.SMOKE,
                        BlockBehaviour.Properties.of().sound(SoundType.NETHER_WOOD).pushReaction(PushReaction.DESTROY)
                        .noCollission().instabreak().lightLevel(i -> 12).lootFrom(INFECTED_TORCH)));

        INFECTED_FIRE = PokecubeLegends.BLOCKS.register("infected_fire",
                () -> new InfectedFireBlock(2.0F, BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_PURPLE)
                        .sound(SoundType.WOOL).pushReaction(PushReaction.DESTROY)
                        .replaceable().noCollission().instabreak().lightLevel(i -> 12)));

        COSMIC_DUST_BLOCK = PokecubeLegends.BLOCKS.register("cosmic_dust_block",
                () -> new SandBlock(2730984, BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_LIGHT_BLUE)
                        .sound(SoundType.SAND).instrument(NoteBlockInstrument.SNARE).strength(0.5F)));

        FRACTAL_BLOCK = PokecubeLegends.BLOCKS.register("fractal_block",
                () -> new BlockBase(MapColor.COLOR_LIGHT_BLUE, SoundType.NETHERITE_BLOCK, NoteBlockInstrument.BANJO,
                        true, 10.0F, 12.0F));

        RUBY_BLOCK = PokecubeLegends.BLOCKS.register("ruby_block",
                () -> new BlockBase(MapColor.COLOR_RED, SoundType.METAL, NoteBlockInstrument.HARP,
                        true, 5.0F, 6.0F));
        RUBY_STAIRS = PokecubeLegends.BLOCKS.register("ruby_stairs",
                () -> new ItemGenerator.GenericStairs(RUBY_BLOCK.get().defaultBlockState(),
                        BlockBehaviour.Properties.copy(RUBY_BLOCK.get())));
        RUBY_SLAB = PokecubeLegends.BLOCKS.register("ruby_slab",
                () -> new SlabBlock(BlockBehaviour.Properties.copy(RUBY_BLOCK.get())));

        SAPPHIRE_BLOCK = PokecubeLegends.BLOCKS.register("sapphire_block",
                () -> new BlockBase(MapColor.COLOR_BLUE, SoundType.METAL, NoteBlockInstrument.HARP,
                        true, 5.0F, 6.0F));
        SAPPHIRE_STAIRS = PokecubeLegends.BLOCKS.register("sapphire_stairs",
                () -> new ItemGenerator.GenericStairs(SAPPHIRE_BLOCK.get().defaultBlockState(),
                        BlockBehaviour.Properties.copy(SAPPHIRE_BLOCK.get())));
        SAPPHIRE_SLAB = PokecubeLegends.BLOCKS.register("sapphire_slab",
                () -> new SlabBlock(BlockBehaviour.Properties.copy(SAPPHIRE_BLOCK.get())));

        SPECTRUM_BLOCK = PokecubeLegends.BLOCKS.register("spectrum_block",
                () -> new BlockBase(MapColor.COLOR_ORANGE, SoundType.METAL, NoteBlockInstrument.HARP,
                        true, 6.0F, 7.0F));
        SPECTRUM_STAIRS = PokecubeLegends.BLOCKS.register("spectrum_stairs",
                () -> new ItemGenerator.GenericStairs(SPECTRUM_BLOCK.get().defaultBlockState(),
                        BlockBehaviour.Properties.copy(SPECTRUM_BLOCK.get())));
        SPECTRUM_SLAB = PokecubeLegends.BLOCKS.register("spectrum_slab",
                () -> new SlabBlock(BlockBehaviour.Properties.copy(SPECTRUM_BLOCK.get())));

        DISTORTIC_TERRACOTTA = PokecubeLegends.BLOCKS.register("distortic_terracotta",
                () -> new BlockBase(MapColor.COLOR_ORANGE, SoundType.STONE, NoteBlockInstrument.BASEDRUM,
                        true, 1.25F, 4.2F));
        DISTORTIC_TERRACOTTA_STAIRS = PokecubeLegends.BLOCKS.register("distortic_terracotta_stairs",
                () -> new ItemGenerator.GenericStairs(DISTORTIC_TERRACOTTA.get().defaultBlockState(),
                        BlockBehaviour.Properties.copy(DISTORTIC_TERRACOTTA.get())));
        DISTORTIC_TERRACOTTA_SLAB = PokecubeLegends.BLOCKS.register("distortic_terracotta_slab",
                () -> new SlabBlock(BlockBehaviour.Properties.copy(DISTORTIC_TERRACOTTA.get())));

        BOOKSHELF_EMPTY = PokecubeLegends.BLOCKS.register("bookshelf_empty",
                () -> new GenericBookshelfEmpty(BlockBehaviour.Properties.of().mapColor(MapColor.WOOD)
                        .sound(SoundType.WOOD).instrument(NoteBlockInstrument.BASS)
                        .strength(1.5F).ignitedByLava().requiresCorrectToolForDrops().dynamicShape()));

        DISTORTIC_OAK_PLANKS = PokecubeLegends.BLOCKS.register("distortic_oak_planks", 
                () -> new Block(BlockBehaviour.Properties.of().mapColor(MapColor.WOOD)
                        .sound(SoundType.WOOD).instrument(NoteBlockInstrument.BASS)
                        .strength(2.0F, 3.0F).ignitedByLava()));
        DISTORTIC_OAK_STAIRS = PokecubeLegends.BLOCKS.register("distortic_oak_stairs",
                () -> new ItemGenerator.GenericStairs(Blocks.OAK_STAIRS.defaultBlockState(),
                        BlockBehaviour.Properties.copy(DISTORTIC_OAK_PLANKS.get())));
        DISTORTIC_OAK_SLAB = PokecubeLegends.BLOCKS.register("distortic_oak_slab",
                () -> new SlabBlock(BlockBehaviour.Properties.copy(DISTORTIC_OAK_PLANKS.get())));

        DISTORTIC_DARK_OAK_PLANKS = PokecubeLegends.BLOCKS.register("distortic_dark_oak_planks",
                () -> new Block(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_BROWN)
                        .sound(SoundType.WOOD).instrument(NoteBlockInstrument.BASS)
                        .strength(2.0F, 3.0F).ignitedByLava()));
        DISTORTIC_DARK_OAK_STAIRS = PokecubeLegends.BLOCKS.register("distortic_dark_oak_stairs",
                () -> new ItemGenerator.GenericStairs(Blocks.OAK_STAIRS.defaultBlockState(),
                        BlockBehaviour.Properties.copy(DISTORTIC_DARK_OAK_PLANKS.get())));
        DISTORTIC_DARK_OAK_SLAB = PokecubeLegends.BLOCKS.register("distortic_dark_oak_slab",
                () -> new SlabBlock(BlockBehaviour.Properties.copy(DISTORTIC_DARK_OAK_PLANKS.get())));

        DISTORTIC_SPRUCE_PLANKS = PokecubeLegends.BLOCKS.register("distortic_spruce_planks",
                () -> new Block(BlockBehaviour.Properties.of().mapColor(MapColor.PODZOL)
                        .sound(SoundType.WOOD).instrument(NoteBlockInstrument.BASS)
                        .strength(2.0F, 3.0F).ignitedByLava()));
        DISTORTIC_SPRUCE_STAIRS = PokecubeLegends.BLOCKS.register("distortic_spruce_stairs",
                () -> new ItemGenerator.GenericStairs(Blocks.OAK_STAIRS.defaultBlockState(),
                        BlockBehaviour.Properties.copy(DISTORTIC_SPRUCE_PLANKS.get())));
        DISTORTIC_SPRUCE_SLAB = PokecubeLegends.BLOCKS.register("distortic_spruce_slab",
                () -> new SlabBlock(BlockBehaviour.Properties.copy(DISTORTIC_SPRUCE_PLANKS.get())));

        DISTORTIC_BIRCH_PLANKS = PokecubeLegends.BLOCKS.register("distortic_birch_planks",
                () -> new Block(BlockBehaviour.Properties.of().mapColor(MapColor.SAND)
                        .sound(SoundType.WOOD).instrument(NoteBlockInstrument.BASS)
                        .strength(2.0F, 3.0F).ignitedByLava()));
        DISTORTIC_BIRCH_STAIRS = PokecubeLegends.BLOCKS.register("distortic_birch_stairs",
                () -> new ItemGenerator.GenericStairs(Blocks.OAK_STAIRS.defaultBlockState(),
                        BlockBehaviour.Properties.copy(DISTORTIC_BIRCH_PLANKS.get())));
        DISTORTIC_BIRCH_SLAB = PokecubeLegends.BLOCKS.register("distortic_birch_slab",
                () -> new SlabBlock(BlockBehaviour.Properties.copy(DISTORTIC_BIRCH_PLANKS.get())));

        DISTORTIC_ACACIA_PLANKS = PokecubeLegends.BLOCKS.register("distortic_acacia_planks",
                () -> new Block(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_ORANGE)
                        .sound(SoundType.WOOD).instrument(NoteBlockInstrument.BASS)
                        .strength(2.0F, 3.0F).ignitedByLava()));
        DISTORTIC_ACACIA_STAIRS = PokecubeLegends.BLOCKS.register("distortic_acacia_stairs",
                () -> new ItemGenerator.GenericStairs(Blocks.OAK_STAIRS.defaultBlockState(),
                        BlockBehaviour.Properties.copy(DISTORTIC_ACACIA_PLANKS.get())));
        DISTORTIC_ACACIA_SLAB = PokecubeLegends.BLOCKS.register("distortic_acacia_slab",
                () -> new SlabBlock(BlockBehaviour.Properties.copy(DISTORTIC_ACACIA_PLANKS.get())));

        DISTORTIC_JUNGLE_PLANKS = PokecubeLegends.BLOCKS.register("distortic_jungle_planks",
                () -> new Block(BlockBehaviour.Properties.of().mapColor(MapColor.DIRT)
                        .sound(SoundType.WOOD).instrument(NoteBlockInstrument.BASS)
                        .strength(2.0F, 3.0F).ignitedByLava()));
        DISTORTIC_JUNGLE_STAIRS = PokecubeLegends.BLOCKS.register("distortic_jungle_stairs",
                () -> new ItemGenerator.GenericStairs(Blocks.OAK_STAIRS.defaultBlockState(),
                        BlockBehaviour.Properties.copy(DISTORTIC_JUNGLE_PLANKS.get())));
        DISTORTIC_JUNGLE_SLAB = PokecubeLegends.BLOCKS.register("distortic_jungle_slab",
                () -> new SlabBlock(BlockBehaviour.Properties.copy(DISTORTIC_JUNGLE_PLANKS.get())));

        // Concrete Blocks
        CONCRETE_LOG = PokecubeLegends.BLOCKS.register("concrete_log",
                () -> StoneLogBase.concreteLog(MapColor.SNOW, MapColor.COLOR_GRAY,
                        SoundType.STONE, NoteBlockInstrument.BASEDRUM, 10.0F, 500.0F));
        CONCRETE_WOOD = PokecubeLegends.BLOCKS.register("concrete_wood",
                () -> StoneLogBase.concreteLog(MapColor.COLOR_GRAY, MapColor.COLOR_GRAY,
                        SoundType.STONE, NoteBlockInstrument.BASEDRUM, 10.0F, 500.0F));
        STRIP_CONCRETE_LOG = PokecubeLegends.BLOCKS.register("stripped_concrete_log",
                () -> StoneLogBase.concreteLog(MapColor.SNOW, MapColor.SNOW,
                        SoundType.STONE, NoteBlockInstrument.BASEDRUM, 10.0F, 500.0F));
        STRIP_CONCRETE_WOOD = PokecubeLegends.BLOCKS.register("stripped_concrete_wood",
                () -> StoneLogBase.concreteLog(MapColor.SNOW, MapColor.SNOW,
                        SoundType.STONE, NoteBlockInstrument.BASEDRUM, 10.0F, 500.0F));

        CONCRETE_BARREL = PokecubeLegends.BLOCKS.register("concrete_barrel",
                () -> new GenericBarrel(BlockBehaviour.Properties.of().mapColor(MapColor.SNOW)
                        .sound(SoundType.STONE).instrument(NoteBlockInstrument.BASEDRUM)
                        .strength(4.5F).requiresCorrectToolForDrops()));
        CONCRETE_BOOKSHELF = PokecubeLegends.BLOCKS.register("concrete_bookshelf",
                () -> new GenericBookshelf(BlockBehaviour.Properties.of().mapColor(MapColor.SNOW)
                        .sound(SoundType.STONE).instrument(NoteBlockInstrument.BASEDRUM)
                        .strength(10.0F, 500.0F).requiresCorrectToolForDrops()));
        CONCRETE_BOOKSHELF_EMPTY = PokecubeLegends.BLOCKS.register("concrete_bookshelf_empty",
                () -> new GenericBookshelfEmpty(BlockBehaviour.Properties.of().mapColor(MapColor.SNOW)
                        .sound(SoundType.STONE).instrument(NoteBlockInstrument.BASEDRUM)
                        .strength(10.0F, 500.0F).requiresCorrectToolForDrops().dynamicShape()));

        CONCRETE_PLANKS = PokecubeLegends.BLOCKS.register("concrete_planks",
                () -> new Block(BlockBehaviour.Properties.of().mapColor(MapColor.SNOW)
                        .sound(SoundType.STONE).instrument(NoteBlockInstrument.BASEDRUM)
                        .strength(10.0F, 500.0F).requiresCorrectToolForDrops()));
        CONCRETE_STAIRS = PokecubeLegends.BLOCKS.register("concrete_stairs",
                () -> new ItemGenerator.GenericStairs(Blocks.OAK_STAIRS.defaultBlockState(),
                        BlockBehaviour.Properties.copy(CONCRETE_PLANKS.get())));
        CONCRETE_SLAB = PokecubeLegends.BLOCKS.register("concrete_slab",
                () -> new SlabBlock(BlockBehaviour.Properties.copy(CONCRETE_PLANKS.get())));
        CONCRETE_FENCE = PokecubeLegends.BLOCKS.register("concrete_fence",
                () -> new FenceBlock(BlockBehaviour.Properties.of().mapColor(MapColor.SNOW)
                        .sound(SoundType.STONE).instrument(NoteBlockInstrument.BASEDRUM)
                        .strength(10.0F, 500.0F).forceSolidOn().requiresCorrectToolForDrops()));
        CONCRETE_FENCE_GATE = PokecubeLegends.BLOCKS.register("concrete_fence_gate",
                () -> new FenceGateBlock(BlockBehaviour.Properties.of().mapColor(MapColor.SNOW)
                        .sound(SoundType.STONE).instrument(NoteBlockInstrument.BASEDRUM).strength(10.0F, 500.0F)
                        .forceSolidOn().requiresCorrectToolForDrops(), LegendsBlockSetType.CONCRETE_WOOD_TYPE));
        CONCRETE_BUTTON = PokecubeLegends.BLOCKS.register("concrete_button",
                () -> new ItemGenerator.GenericButton(BlockSetType.STONE, false, 20,
                        BlockBehaviour.Properties.of().mapColor(MapColor.SNOW).sound(SoundType.STONE)
                                .pushReaction(PushReaction.DESTROY).strength(10.0F, 500.0F)
                                .noCollission().requiresCorrectToolForDrops()));
        CONCRETE_PR_PLATE = PokecubeLegends.BLOCKS.register("concrete_pressure_plate",
                () -> new ItemGenerator.GenericPressurePlate(PressurePlateBlock.Sensitivity.MOBS,
                        LegendsBlockSetType.CONCRETE_BLOCK_SET, BlockBehaviour.Properties.of().mapColor(MapColor.SNOW)
                        .sound(SoundType.STONE).instrument(NoteBlockInstrument.BASEDRUM)
                        .pushReaction(PushReaction.DESTROY).strength(10.0F, 500.0F)
                        .noCollission().forceSolidOn().requiresCorrectToolForDrops()));

        CONCRETE_TRAPDOOR = PokecubeLegends.BLOCKS.register("concrete_trapdoor",
                () -> new ItemGenerator.GenericTrapDoor(LegendsBlockSetType.CONCRETE_BLOCK_SET,
                        BlockBehaviour.Properties.of().mapColor(MapColor.SNOW).sound(SoundType.STONE)
                                .instrument(NoteBlockInstrument.BASEDRUM).pushReaction(PushReaction.BLOCK)
                                .strength(10.0F, 500.0F).noOcclusion().requiresCorrectToolForDrops()
                                .isValidSpawn(PokecubeItems::never)));
        CONCRETE_DOOR = PokecubeLegends.BLOCKS.register("concrete_door",
                () -> new ItemGenerator.GenericDoor(LegendsBlockSetType.CONCRETE_BLOCK_SET,
                        BlockBehaviour.Properties.of().mapColor(MapColor.SNOW)
                                .sound(SoundType.STONE).instrument(NoteBlockInstrument.BASEDRUM)
                                .pushReaction(PushReaction.BLOCK).strength(10.0F, 500.0F)
                                .noOcclusion().requiresCorrectToolForDrops()));

        CONCRETE_DENSE_BARREL = PokecubeLegends.BLOCKS.register("concrete_dense_barrel",
                () -> new GenericBarrel(BlockBehaviour.Properties.of().mapColor(MapColor.SNOW)
                        .sound(SoundType.STONE).instrument(NoteBlockInstrument.BASEDRUM)
                        .strength(20.0F, 1200.0F).requiresCorrectToolForDrops()));
        CONCRETE_DENSE_BOOKSHELF = PokecubeLegends.BLOCKS.register("concrete_dense_bookshelf",
                () -> new GenericBookshelf(BlockBehaviour.Properties.of().mapColor(MapColor.SNOW)
                        .sound(SoundType.STONE).instrument(NoteBlockInstrument.BASEDRUM)
                        .strength(20.0F, 1200.0F).requiresCorrectToolForDrops()));
        CONCRETE_DENSE_BOOKSHELF_EMPTY = PokecubeLegends.BLOCKS.register("concrete_dense_bookshelf_empty",
                () -> new GenericBookshelfEmpty(BlockBehaviour.Properties.of().mapColor(MapColor.SNOW)
                        .sound(SoundType.STONE).instrument(NoteBlockInstrument.BASEDRUM)
                        .strength(20.0F, 1200.0F).requiresCorrectToolForDrops().dynamicShape()));

        CONCRETE_DENSE_PLANKS = PokecubeLegends.BLOCKS.register("concrete_dense_planks",
                () -> new Block(BlockBehaviour.Properties.of().mapColor(MapColor.SNOW)
                        .sound(SoundType.STONE).instrument(NoteBlockInstrument.BASEDRUM)
                        .strength(20.0F, 1200.0F).requiresCorrectToolForDrops()));
        CONCRETE_DENSE_STAIRS = PokecubeLegends.BLOCKS.register("concrete_dense_stairs",
                () -> new ItemGenerator.GenericStairs(Blocks.OAK_STAIRS.defaultBlockState(),
                        BlockBehaviour.Properties.copy(CONCRETE_DENSE_PLANKS.get())));
        CONCRETE_DENSE_SLAB = PokecubeLegends.BLOCKS.register("concrete_dense_slab",
                () -> new SlabBlock(BlockBehaviour.Properties.copy(CONCRETE_DENSE_PLANKS.get())));
        CONCRETE_DENSE_WALL = PokecubeLegends.BLOCKS.register("concrete_dense_wall",
                () -> new WallBlock(BlockBehaviour.Properties.of().mapColor(MapColor.SNOW)
                        .sound(SoundType.STONE).instrument(NoteBlockInstrument.BASEDRUM)
                        .strength(20.0F, 1200.0F).forceSolidOn().requiresCorrectToolForDrops()));
        CONCRETE_DENSE_WALL_GATE = PokecubeLegends.BLOCKS.register("concrete_dense_wall_gate",
                () -> new WallGateBlock(BlockBehaviour.Properties.of().mapColor(MapColor.SNOW)
                        .sound(SoundType.STONE).instrument(NoteBlockInstrument.BASEDRUM).strength(20.0F, 1200.0F)
                        .forceSolidOn().requiresCorrectToolForDrops(), LegendsBlockSetType.CONCRETE_DENSE_WOOD_TYPE));
        CONCRETE_DENSE_BUTTON = PokecubeLegends.BLOCKS.register("concrete_dense_button",
                () -> new ItemGenerator.GenericButton(BlockSetType.STONE, false, 10,
                        BlockBehaviour.Properties.of().mapColor(MapColor.SNOW).sound(SoundType.STONE)
                                .pushReaction(PushReaction.DESTROY).strength(20.0F, 1200.0F)
                                .noCollission().requiresCorrectToolForDrops()));
        CONCRETE_DENSE_PR_PLATE = PokecubeLegends.BLOCKS.register("concrete_dense_pressure_plate",
                () -> new ItemGenerator.GenericPressurePlate(PressurePlateBlock.Sensitivity.MOBS,
                        LegendsBlockSetType.CONCRETE_DENSE_BLOCK_SET, BlockBehaviour.Properties.of()
                        .mapColor(MapColor.SNOW).sound(SoundType.STONE).instrument(NoteBlockInstrument.BASEDRUM)
                        .pushReaction(PushReaction.DESTROY).strength(20.0F, 1200.0F)
                        .noCollission().forceSolidOn().requiresCorrectToolForDrops()));

        // Ultra Metal
        ULTRA_METAL = PokecubeLegends.BLOCKS.register("ultra_metal",
                () -> new BlockBase(MapColor.COLOR_LIGHT_GREEN, SoundType.NETHERITE_BLOCK, NoteBlockInstrument.HARP,
                        true, 5.0F, 10.0F));
        ULTRA_METAL_STAIRS = PokecubeLegends.BLOCKS.register("ultra_metal_stairs",
                () -> new ItemGenerator.GenericStairs(ULTRA_METAL.get().defaultBlockState(),
                        BlockBehaviour.Properties.copy(ULTRA_METAL.get())));
        ULTRA_METAL_SLAB = PokecubeLegends.BLOCKS.register("ultra_metal_slab",
                () -> new SlabBlock(BlockBehaviour.Properties.copy(ULTRA_METAL.get())));
        ULTRA_METAL_BUTTON = PokecubeLegends.BLOCKS.register("ultra_metal_button",
                () -> new ItemGenerator.GenericButton(LegendsBlockSetType.ULTRA_METAL_BLOCK_SET, false, 10,
                        BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_LIGHT_GREEN)
                                .sound(SoundType.NETHERITE_BLOCK).pushReaction(PushReaction.DESTROY)
                                .strength(0.5F).noCollission()));
        ULTRA_METAL_PR_PLATE = PokecubeLegends.BLOCKS.register("ultra_metal_pressure_plate",
                () -> new ItemGenerator.GenericPressurePlate(PressurePlateBlock.Sensitivity.MOBS,
                        LegendsBlockSetType.ULTRA_METAL_BLOCK_SET, BlockBehaviour.Properties.of()
                        .mapColor(MapColor.COLOR_LIGHT_GREEN).sound(SoundType.NETHERITE_BLOCK)
                        .pushReaction(PushReaction.DESTROY).strength(0.5F)
                        .noCollission().forceSolidOn()));

        MAGNETIC_STONE = PokecubeLegends.BLOCKS.register("magnetic_stone",
                () -> new MagneticBlock(MapColor.COLOR_BLUE, SoundType.NETHERITE_BLOCK, NoteBlockInstrument.BASEDRUM,
                        true, 4.0F, 3.0F));

        // Bricks
        ULTRA_STONE_BRICKS = PokecubeLegends.BLOCKS.register("ultra_stone_bricks",
                () -> new BlockBase(MapColor.TERRACOTTA_CYAN, SoundType.STONE, NoteBlockInstrument.BASEDRUM,
                        true, 1.5F, 10.0F));
        ULTRA_STONE_BRICK_STAIRS = PokecubeLegends.BLOCKS.register("ultra_stone_brick_stairs",
                () -> new ItemGenerator.GenericStairs(ULTRA_STONE_BRICKS.get().defaultBlockState(),
                        BlockBehaviour.Properties.copy(ULTRA_STONE_BRICKS.get())));
        ULTRA_STONE_BRICK_SLAB = PokecubeLegends.BLOCKS.register("ultra_stone_brick_slab",
                () -> new SlabBlock(BlockBehaviour.Properties.copy(ULTRA_STONE_BRICKS.get())));

        ULTRA_DARKSTONE_BRICKS = PokecubeLegends.BLOCKS.register("ultra_darkstone_bricks",
                () -> new BlockBase(MapColor.COLOR_BLACK, SoundType.GILDED_BLACKSTONE, NoteBlockInstrument.BASEDRUM,
                        true, 5.0F, 8.0F));
        ULTRA_DARKSTONE_BRICK_STAIRS = PokecubeLegends.BLOCKS.register("ultra_darkstone_brick_stairs",
                () -> new ItemGenerator.GenericStairs(ULTRA_DARKSTONE_BRICKS.get().defaultBlockState(),
                        BlockBehaviour.Properties.copy(ULTRA_DARKSTONE_BRICKS.get())));
        ULTRA_DARKSTONE_BRICK_SLAB = PokecubeLegends.BLOCKS.register("ultra_darkstone_brick_slab",
                () -> new SlabBlock(BlockBehaviour.Properties.copy(ULTRA_DARKSTONE_BRICKS.get())));

        DUSK_DOLERITE_BRICKS = PokecubeLegends.BLOCKS.register("dusk_dolerite_bricks",
                () -> new Block(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_PURPLE)
                        .sound(SoundType.DEEPSLATE_BRICKS).strength(3.0F, 9.0F).requiresCorrectToolForDrops()));
        DUSK_DOLERITE_BRICK_STAIRS = PokecubeLegends.BLOCKS.register("dusk_dolerite_brick_stairs",
                () -> new ItemGenerator.GenericStairs(DUSK_DOLERITE_BRICKS.get().defaultBlockState(),
                        BlockBehaviour.Properties.copy(DUSK_DOLERITE_BRICKS.get())));
        DUSK_DOLERITE_BRICK_SLAB = PokecubeLegends.BLOCKS.register("dusk_dolerite_brick_slab",
                () -> new SlabBlock(BlockBehaviour.Properties.copy(DUSK_DOLERITE_BRICKS.get())));

        AZURE_SANDSTONE_BRICKS = PokecubeLegends.BLOCKS.register("azure_sandstone_bricks",
                () -> new BlockBase(MapColor.COLOR_BLUE, SoundType.STONE, NoteBlockInstrument.BASEDRUM,
                        true, 2.0F, 6.0F));
        AZURE_SANDSTONE_BRICK_STAIRS = PokecubeLegends.BLOCKS.register("azure_sandstone_brick_stairs",
                () -> new ItemGenerator.GenericStairs(AZURE_SANDSTONE_BRICKS.get().defaultBlockState(),
                        BlockBehaviour.Properties.copy(AZURE_SANDSTONE_BRICKS.get())));
        AZURE_SANDSTONE_BRICK_SLAB = PokecubeLegends.BLOCKS.register("azure_sandstone_brick_slab",
                () -> new SlabBlock(BlockBehaviour.Properties.copy(AZURE_SANDSTONE_BRICKS.get())));

        BLACKENED_SANDSTONE_BRICKS = PokecubeLegends.BLOCKS.register("blackened_sandstone_bricks",
                () -> new BlockBase(MapColor.COLOR_BLACK, SoundType.STONE, NoteBlockInstrument.BASEDRUM,
                        true, 2.0F, 6.0F));
        BLACKENED_SANDSTONE_BRICK_STAIRS = PokecubeLegends.BLOCKS.register("blackened_sandstone_brick_stairs",
                () -> new ItemGenerator.GenericStairs(BLACKENED_SANDSTONE_BRICKS.get().defaultBlockState(),
                        BlockBehaviour.Properties.copy(BLACKENED_SANDSTONE_BRICKS.get())));
        BLACKENED_SANDSTONE_BRICK_SLAB = PokecubeLegends.BLOCKS.register("blackened_sandstone_brick_slab",
                () -> new SlabBlock(BlockBehaviour.Properties.copy(BLACKENED_SANDSTONE_BRICKS.get())));

        CRYS_SANDSTONE_BRICKS = PokecubeLegends.BLOCKS.register("crystallized_sandstone_bricks",
                () -> new BlockBase(MapColor.SNOW, SoundType.STONE, NoteBlockInstrument.BASEDRUM,
                        true, 2.0F, 6.0F));
        CRYS_SANDSTONE_BRICK_STAIRS = PokecubeLegends.BLOCKS.register("crystallized_sandstone_brick_stairs",
                () -> new ItemGenerator.GenericStairs(CRYS_SANDSTONE_BRICKS.get().defaultBlockState(),
                        BlockBehaviour.Properties.copy(CRYS_SANDSTONE_BRICKS.get())));
        CRYS_SANDSTONE_BRICK_SLAB = PokecubeLegends.BLOCKS.register("crystallized_sandstone_brick_slab",
                () -> new SlabBlock(BlockBehaviour.Properties.copy(CRYS_SANDSTONE_BRICKS.get())));

        AQUAMARINE_BRICKS = PokecubeLegends.BLOCKS.register("aquamarine_bricks",
                () -> new BlockBase(MapColor.COLOR_LIGHT_BLUE, SoundType.AMETHYST, NoteBlockInstrument.CHIME,
                        true, 1.5F, 1.5F));
        AQUAMARINE_BRICK_STAIRS = PokecubeLegends.BLOCKS.register("aquamarine_brick_stairs",
                () -> new ItemGenerator.GenericStairs(AQUAMARINE_BRICKS.get().defaultBlockState(),
                        BlockBehaviour.Properties.copy(AQUAMARINE_BRICKS.get())));
        AQUAMARINE_BRICK_SLAB = PokecubeLegends.BLOCKS.register("aquamarine_brick_slab",
                () -> new SlabBlock(BlockBehaviour.Properties.copy(AQUAMARINE_BRICKS.get())));

        // Ocean Bricks
        OCEAN_BRICKS = PokecubeLegends.BLOCKS.register("ocean_bricks",
                () -> new BlockBase(MapColor.COLOR_CYAN, SoundType.STONE, NoteBlockInstrument.BASEDRUM,
                        true, 1.5F, 6.0F));
        OCEAN_BRICK_STAIRS = PokecubeLegends.BLOCKS.register("ocean_brick_stairs",
                () -> new GenericStairs(OCEAN_BRICKS.get().defaultBlockState(),
                        BlockBehaviour.Properties.copy(OCEAN_BRICKS.get())));
        OCEAN_BRICK_SLAB = PokecubeLegends.BLOCKS.register("ocean_brick_slab",
                () -> new SlabBlock(BlockBehaviour.Properties.copy(OCEAN_BRICKS.get())));

        // Sky Bricks
        SKY_BRICKS = PokecubeLegends.BLOCKS.register("sky_bricks",
                () -> new BlockBase(MapColor.COLOR_BLUE, SoundType.STONE, NoteBlockInstrument.BASEDRUM, 
                        true, 1.5F, 6.0F));
        SKY_BRICK_STAIRS = PokecubeLegends.BLOCKS.register("sky_brick_stairs",
                () -> new ItemGenerator.GenericStairs(SKY_BRICKS.get().defaultBlockState(),
                        BlockBehaviour.Properties.copy(SKY_BRICKS.get())));
        SKY_BRICK_SLAB = PokecubeLegends.BLOCKS.register("sky_brick_slab",
                () -> new SlabBlock(BlockBehaviour.Properties.copy(SKY_BRICKS.get())));

        // Purpur Bricks
        PURPUR_BRICKS = PokecubeLegends.BLOCKS.register("purpur_bricks",
                () -> new BlockBase(MapColor.COLOR_MAGENTA, SoundType.STONE, NoteBlockInstrument.BASEDRUM,
                        true, 1.5F, 6.0F));
        PURPUR_BRICK_STAIRS = PokecubeLegends.BLOCKS.register("purpur_brick_stairs",
                () -> new ItemGenerator.GenericStairs(PURPUR_BRICKS.get().defaultBlockState(),
                        BlockBehaviour.Properties.copy(PURPUR_BRICKS.get())));
        PURPUR_BRICK_SLAB = PokecubeLegends.BLOCKS.register("purpur_brick_slab",
                () -> new SlabBlock(BlockBehaviour.Properties.copy(PURPUR_BRICKS.get())));

        // Magma Bricks
        MAGMA_BRICKS = PokecubeLegends.BLOCKS.register("magma_bricks",
                () -> new MagmaBlock(BlockBehaviour.Properties.of().mapColor(MapColor.NETHER)
                        .sound(SoundType.NETHER_BRICKS).strength(2.0F, 6.0F)
                        .requiresCorrectToolForDrops().lightLevel(b -> 3).emissiveRendering((s, r, p) -> true)));
        MAGMA_BRICK_STAIRS = PokecubeLegends.BLOCKS.register("magma_brick_stairs",
                () -> new ItemGenerator.GenericStairs(MAGMA_BRICKS.get().defaultBlockState(),
                        BlockBehaviour.Properties.copy(MAGMA_BRICKS.get())));
        MAGMA_BRICK_SLAB = PokecubeLegends.BLOCKS.register("magma_brick_slab",
                () -> new SlabBlock(BlockBehaviour.Properties.copy(MAGMA_BRICKS.get())));

        // Stormy Sky Bricks
        STORMY_SKY_BRICKS = PokecubeLegends.BLOCKS.register("stormy_sky_bricks",
                () -> new BlockBase(MapColor.COLOR_LIGHT_GRAY, SoundType.STONE, NoteBlockInstrument.BASEDRUM,
                        true, 1.5F, 6.0F));
        STORMY_SKY_BRICK_STAIRS = PokecubeLegends.BLOCKS.register("stormy_sky_brick_stairs",
                () -> new ItemGenerator.GenericStairs(STORMY_SKY_BRICKS.get().defaultBlockState(),
                        BlockBehaviour.Properties.copy(STORMY_SKY_BRICKS.get())));
        STORMY_SKY_BRICK_SLAB = PokecubeLegends.BLOCKS.register("stormy_sky_brick_slab",
                () -> new SlabBlock(BlockBehaviour.Properties.copy(STORMY_SKY_BRICKS.get())));

        // Distortic Stone Bricks
        DISTORTIC_STONE_BRICKS = PokecubeLegends.BLOCKS.register("distortic_stone_bricks",
                () -> new BlockBase(MapColor.TERRACOTTA_BLACK, SoundType.DEEPSLATE_BRICKS, NoteBlockInstrument.BASEDRUM,
                        true, 2.5F, 7.0F));
        DISTORTIC_STONE_BRICK_STAIRS = PokecubeLegends.BLOCKS.register("distortic_stone_brick_stairs",
                () -> new ItemGenerator.GenericStairs(DISTORTIC_STONE_BRICKS.get().defaultBlockState(),
                        BlockBehaviour.Properties.copy(DISTORTIC_STONE_BRICKS.get())));
        DISTORTIC_STONE_BRICK_SLAB = PokecubeLegends.BLOCKS.register("distortic_stone_brick_slab",
                () -> new SlabBlock(BlockBehaviour.Properties.copy(DISTORTIC_STONE_BRICKS.get())));

        CHISELED_DISTORTIC_STONE = PokecubeLegends.BLOCKS.register("chiseled_distortic_stone_bricks",
                () -> new BlockBase(MapColor.TERRACOTTA_BLACK, SoundType.DEEPSLATE_BRICKS, NoteBlockInstrument.BASEDRUM,
                        true, 2.5F, 7.0F));
        CHISELED_DISTORTIC_STONE_STAIRS = PokecubeLegends.BLOCKS.register("chiseled_distortic_stone_brick_stairs",
                () -> new ItemGenerator.GenericStairs(CHISELED_DISTORTIC_STONE.get().defaultBlockState(),
                        BlockBehaviour.Properties.copy(CHISELED_DISTORTIC_STONE.get())));
        CHISELED_DISTORTIC_STONE_SLAB = PokecubeLegends.BLOCKS.register("chiseled_distortic_stone_brick_slab",
                () -> new SlabBlock(BlockBehaviour.Properties.copy(CHISELED_DISTORTIC_STONE.get())));

        DISTORTIC_STONE_BARREL = PokecubeLegends.BLOCKS.register("distortic_stone_barrel",
                () -> new GenericBarrel(BlockBehaviour.Properties.of().mapColor(MapColor.TERRACOTTA_BLACK)
                        .sound(SoundType.DEEPSLATE_BRICKS).instrument(NoteBlockInstrument.BASEDRUM)
                        .strength(4.5F).requiresCorrectToolForDrops()));

        // Unown Stones
        for (int i = 0; i < unowns.length; i++)
        {
            UNOWN_STONES[i] = PokecubeLegends.BLOCKS.register(unowns[i],
                    () -> new BlockBase(MapColor.TERRACOTTA_BLACK, SoundType.DEEPSLATE_BRICKS, 
                            NoteBlockInstrument.BASEDRUM, true, 2.5F, 7.0F));
        }

        // Glass
        MIRAGE_GLASS = PokecubeLegends.BLOCKS.register("mirage_glass",
                () -> new MirageGlassBlock(DyeColor.LIGHT_BLUE, BlockBehaviour.Properties.of()
                        .mapColor(MapColor.COLOR_LIGHT_BLUE).sound(SoundType.GLASS).instrument(NoteBlockInstrument.HAT)
                        .strength(0.3F).noOcclusion().isRedstoneConductor(PokecubeItems::never)
                        .isValidSpawn(PokecubeItems::never).isSuffocating(PokecubeItems::never)
                        .isViewBlocking(PokecubeItems::never)));
        MIRAGE_GLASS_PANE = PokecubeLegends.BLOCKS.register("mirage_glass_pane",
                () -> new MirageGlassPaneBlock(DyeColor.LIGHT_BLUE, BlockBehaviour.Properties.of()
                        .mapColor(MapColor.COLOR_LIGHT_BLUE).sound(SoundType.GLASS).instrument(NoteBlockInstrument.HAT)
                        .strength(0.3F).noOcclusion().isRedstoneConductor(PokecubeItems::never)
                        .isValidSpawn(PokecubeItems::never).isSuffocating(PokecubeItems::never)
                        .isViewBlocking(PokecubeItems::never)));
        SPECTRUM_GLASS = PokecubeLegends.BLOCKS.register("spectrum_glass",
                () -> new SpectrumGlassBlock(DyeColor.ORANGE, BlockBehaviour.Properties.of()
                        .mapColor(MapColor.COLOR_ORANGE).sound(SoundType.GLASS).instrument(NoteBlockInstrument.HAT)
                        .strength(0.3F).noOcclusion().isRedstoneConductor(PokecubeItems::never)
                        .isValidSpawn(PokecubeItems::never).isSuffocating(PokecubeItems::never)
                        .isViewBlocking(PokecubeItems::never)));
        SPECTRUM_GLASS_PANE = PokecubeLegends.BLOCKS.register("spectrum_glass_pane",
                () -> new SpectrumGlassPaneBlock(DyeColor.ORANGE, BlockBehaviour.Properties.of()
                        .mapColor(MapColor.COLOR_ORANGE).sound(SoundType.GLASS).instrument(NoteBlockInstrument.HAT)
                        .strength(0.3F).noOcclusion().isRedstoneConductor(PokecubeItems::never)
                        .isValidSpawn(PokecubeItems::never).isSuffocating(PokecubeItems::never)
                        .isViewBlocking(PokecubeItems::never)));
        DISTORTIC_FRAMED_MIRROR = PokecubeLegends.BLOCKS.register("distortic_framed_mirror",
                () -> new StainedGlassBlock(DyeColor.WHITE, BlockBehaviour.Properties.of()
                        .mapColor(MapColor.SNOW).sound(SoundType.GLASS).instrument(NoteBlockInstrument.HAT)
                        .strength(0.3F).noOcclusion().isRedstoneConductor(PokecubeItems::never)
                        .isValidSpawn(PokecubeItems::never).isSuffocating(PokecubeItems::never)
                        .isViewBlocking(PokecubeItems::never)));
        DISTORTIC_FRAMED_MIRROR_PANE = PokecubeLegends.BLOCKS.register("distortic_framed_mirror_pane",
                () -> new StainedGlassPaneBlock(DyeColor.WHITE, BlockBehaviour.Properties.of()
                        .mapColor(MapColor.SNOW).sound(SoundType.GLASS).instrument(NoteBlockInstrument.HAT)
                        .strength(0.3F).noOcclusion().isRedstoneConductor(PokecubeItems::never)
                        .isValidSpawn(PokecubeItems::never).isSuffocating(PokecubeItems::never)
                        .isViewBlocking(PokecubeItems::never)));

        ONE_WAY_GLASS = PokecubeLegends.BLOCKS.register("one_way_glass",
                () -> new OneWayGlass(BlockBehaviour.Properties.of().mapColor(MapColor.SNOW)
                        .sound(SoundType.GLASS).instrument(NoteBlockInstrument.HAT)
                        .strength(0.3F).noOcclusion().isRedstoneConductor(PokecubeItems::never)
                        .isValidSpawn(PokecubeItems::never).isSuffocating(PokecubeItems::never)
                        .isViewBlocking(PokecubeItems::never)));
        ONE_WAY_GLASS_WHITE = PokecubeLegends.BLOCKS.register("one_way_white_stained_glass",
                () -> new OneWayStainedGlass(DyeColor.WHITE, BlockBehaviour.Properties.of().mapColor(MapColor.SNOW)
                        .sound(SoundType.GLASS).instrument(NoteBlockInstrument.HAT)
                        .strength(0.3F).noOcclusion().isRedstoneConductor(PokecubeItems::never)
                        .isValidSpawn(PokecubeItems::never).isSuffocating(PokecubeItems::never)
                        .isViewBlocking(PokecubeItems::never)));
        ONE_WAY_GLASS_ORANGE = PokecubeLegends.BLOCKS.register("one_way_orange_stained_glass",
                () -> new OneWayStainedGlass(DyeColor.ORANGE, BlockBehaviour.Properties.of().mapColor(MapColor.SNOW)
                        .sound(SoundType.GLASS).instrument(NoteBlockInstrument.HAT)
                        .strength(0.3F).noOcclusion().isRedstoneConductor(PokecubeItems::never)
                        .isValidSpawn(PokecubeItems::never).isSuffocating(PokecubeItems::never)
                        .isViewBlocking(PokecubeItems::never)));
        ONE_WAY_GLASS_MAGENTA = PokecubeLegends.BLOCKS.register("one_way_magenta_stained_glass",
                () -> new OneWayStainedGlass(DyeColor.MAGENTA, BlockBehaviour.Properties.of().mapColor(MapColor.SNOW)
                        .sound(SoundType.GLASS).instrument(NoteBlockInstrument.HAT)
                        .strength(0.3F).noOcclusion().isRedstoneConductor(PokecubeItems::never)
                        .isValidSpawn(PokecubeItems::never).isSuffocating(PokecubeItems::never)
                        .isViewBlocking(PokecubeItems::never)));
        ONE_WAY_GLASS_LIGHT_BLUE = PokecubeLegends.BLOCKS.register("one_way_light_blue_stained_glass",
                () -> new OneWayStainedGlass(DyeColor.LIGHT_BLUE, BlockBehaviour.Properties.of()
                        .mapColor(MapColor.SNOW).strength(0.3F).noOcclusion().requiresCorrectToolForDrops()
                        .isRedstoneConductor(PokecubeItems::never).isValidSpawn(PokecubeItems::never)
                        .isSuffocating(PokecubeItems::never).isViewBlocking(PokecubeItems::never)
                        .sound(SoundType.GLASS).instrument(NoteBlockInstrument.HAT)));
        ONE_WAY_GLASS_YELLOW = PokecubeLegends.BLOCKS.register("one_way_yellow_stained_glass",
                () -> new OneWayStainedGlass(DyeColor.YELLOW, BlockBehaviour.Properties.of().mapColor(MapColor.SNOW)
                        .sound(SoundType.GLASS).instrument(NoteBlockInstrument.HAT)
                        .strength(0.3F).noOcclusion().isRedstoneConductor(PokecubeItems::never)
                        .isValidSpawn(PokecubeItems::never).isSuffocating(PokecubeItems::never)
                        .isViewBlocking(PokecubeItems::never)));
        ONE_WAY_GLASS_LIME = PokecubeLegends.BLOCKS.register("one_way_lime_stained_glass",
                () -> new OneWayStainedGlass(DyeColor.LIME, BlockBehaviour.Properties.of().mapColor(MapColor.SNOW)
                        .sound(SoundType.GLASS).instrument(NoteBlockInstrument.HAT)
                        .strength(0.3F).noOcclusion().isRedstoneConductor(PokecubeItems::never)
                        .isValidSpawn(PokecubeItems::never).isSuffocating(PokecubeItems::never)
                        .isViewBlocking(PokecubeItems::never)));
        ONE_WAY_GLASS_PINK = PokecubeLegends.BLOCKS.register("one_way_pink_stained_glass",
                () -> new OneWayStainedGlass(DyeColor.PINK, BlockBehaviour.Properties.of().mapColor(MapColor.SNOW)
                        .sound(SoundType.GLASS).instrument(NoteBlockInstrument.HAT)
                        .strength(0.3F).noOcclusion().isRedstoneConductor(PokecubeItems::never)
                        .isValidSpawn(PokecubeItems::never).isSuffocating(PokecubeItems::never)
                        .isViewBlocking(PokecubeItems::never)));
        ONE_WAY_GLASS_GRAY = PokecubeLegends.BLOCKS.register("one_way_gray_stained_glass",
                () -> new OneWayStainedGlass(DyeColor.GRAY, BlockBehaviour.Properties.of().mapColor(MapColor.SNOW)
                        .sound(SoundType.GLASS).instrument(NoteBlockInstrument.HAT)
                        .strength(0.3F).noOcclusion().isRedstoneConductor(PokecubeItems::never)
                        .isValidSpawn(PokecubeItems::never).isSuffocating(PokecubeItems::never)
                        .isViewBlocking(PokecubeItems::never)));
        ONE_WAY_GLASS_LIGHT_GRAY = PokecubeLegends.BLOCKS.register("one_way_light_gray_stained_glass",
                () -> new OneWayStainedGlass(DyeColor.LIGHT_GRAY, BlockBehaviour.Properties.of().mapColor(MapColor.SNOW)
                        .sound(SoundType.GLASS).instrument(NoteBlockInstrument.HAT)
                        .strength(0.3F).noOcclusion().isRedstoneConductor(PokecubeItems::never)
                        .isValidSpawn(PokecubeItems::never).isSuffocating(PokecubeItems::never)
                        .isViewBlocking(PokecubeItems::never)));
        ONE_WAY_GLASS_CYAN = PokecubeLegends.BLOCKS.register("one_way_cyan_stained_glass",
                () -> new OneWayStainedGlass(DyeColor.CYAN, BlockBehaviour.Properties.of().mapColor(MapColor.SNOW)
                        .sound(SoundType.GLASS).instrument(NoteBlockInstrument.HAT)
                        .strength(0.3F).noOcclusion().isRedstoneConductor(PokecubeItems::never)
                        .isValidSpawn(PokecubeItems::never).isSuffocating(PokecubeItems::never)
                        .isViewBlocking(PokecubeItems::never)));
        ONE_WAY_GLASS_PURPLE = PokecubeLegends.BLOCKS.register("one_way_purple_stained_glass",
                () -> new OneWayStainedGlass(DyeColor.PURPLE, BlockBehaviour.Properties.of().mapColor(MapColor.SNOW)
                        .sound(SoundType.GLASS).instrument(NoteBlockInstrument.HAT)
                        .strength(0.3F).noOcclusion().isRedstoneConductor(PokecubeItems::never)
                        .isValidSpawn(PokecubeItems::never).isSuffocating(PokecubeItems::never)
                        .isViewBlocking(PokecubeItems::never)));
        ONE_WAY_GLASS_BLUE = PokecubeLegends.BLOCKS.register("one_way_blue_stained_glass",
                () -> new OneWayStainedGlass(DyeColor.BLUE, BlockBehaviour.Properties.of().mapColor(MapColor.SNOW)
                        .sound(SoundType.GLASS).instrument(NoteBlockInstrument.HAT)
                        .strength(0.3F).noOcclusion().isRedstoneConductor(PokecubeItems::never)
                        .isValidSpawn(PokecubeItems::never).isSuffocating(PokecubeItems::never)
                        .isViewBlocking(PokecubeItems::never)));
        ONE_WAY_GLASS_BROWN = PokecubeLegends.BLOCKS.register("one_way_brown_stained_glass",
                () -> new OneWayStainedGlass(DyeColor.BROWN, BlockBehaviour.Properties.of().mapColor(MapColor.SNOW)
                        .sound(SoundType.GLASS).instrument(NoteBlockInstrument.HAT)
                        .strength(0.3F).noOcclusion().isRedstoneConductor(PokecubeItems::never)
                        .isValidSpawn(PokecubeItems::never).isSuffocating(PokecubeItems::never)
                        .isViewBlocking(PokecubeItems::never)));
        ONE_WAY_GLASS_GREEN = PokecubeLegends.BLOCKS.register("one_way_green_stained_glass",
                () -> new OneWayStainedGlass(DyeColor.GREEN, BlockBehaviour.Properties.of().mapColor(MapColor.SNOW)
                        .sound(SoundType.GLASS).instrument(NoteBlockInstrument.HAT)
                        .strength(0.3F).noOcclusion().isRedstoneConductor(PokecubeItems::never)
                        .isValidSpawn(PokecubeItems::never).isSuffocating(PokecubeItems::never)
                        .isViewBlocking(PokecubeItems::never)));
        ONE_WAY_GLASS_RED = PokecubeLegends.BLOCKS.register("one_way_red_stained_glass",
                () -> new OneWayStainedGlass(DyeColor.RED, BlockBehaviour.Properties.of().mapColor(MapColor.SNOW)
                        .sound(SoundType.GLASS).instrument(NoteBlockInstrument.HAT)
                        .strength(0.3F).noOcclusion().isRedstoneConductor(PokecubeItems::never)
                        .isValidSpawn(PokecubeItems::never).isSuffocating(PokecubeItems::never)
                        .isViewBlocking(PokecubeItems::never)));
        ONE_WAY_GLASS_BLACK = PokecubeLegends.BLOCKS.register("one_way_black_stained_glass",
                () -> new OneWayStainedGlass(DyeColor.BLACK, BlockBehaviour.Properties.of().mapColor(MapColor.SNOW)
                        .sound(SoundType.GLASS).instrument(NoteBlockInstrument.HAT)
                        .strength(0.3F).noOcclusion().isRedstoneConductor(PokecubeItems::never)
                        .isValidSpawn(PokecubeItems::never).isSuffocating(PokecubeItems::never)
                        .isViewBlocking(PokecubeItems::never)));
        ONE_WAY_GLASS_TINTED = PokecubeLegends.BLOCKS.register("one_way_tinted_glass",
                () -> new OneWayTintedGlass(BlockBehaviour.Properties.of().mapColor(MapColor.SNOW)
                        .sound(SoundType.GLASS).instrument(NoteBlockInstrument.HAT)
                        .strength(0.3F).noOcclusion().isRedstoneConductor(PokecubeItems::never)
                        .isValidSpawn(PokecubeItems::never).isSuffocating(PokecubeItems::never)
                        .isViewBlocking(PokecubeItems::never)));
        ONE_WAY_GLASS_LAB = PokecubeLegends.BLOCKS.register("one_way_laboratory_glass",
                () -> new OneWayLaboratoryGlass(DyeColor.LIGHT_BLUE, BlockBehaviour.Properties.of()
                        .mapColor(MapColor.SNOW).sound(SoundType.GLASS).instrument(NoteBlockInstrument.HAT)
                        .strength(0.3F).noOcclusion().isRedstoneConductor(PokecubeItems::never)
                        .isValidSpawn(PokecubeItems::never).isSuffocating(PokecubeItems::never)
                        .isViewBlocking(PokecubeItems::never)));
        ONE_WAY_GLASS_MIRAGE = PokecubeLegends.BLOCKS.register("one_way_mirage_glass",
                () -> new OneWayMirageGlass(DyeColor.LIGHT_BLUE, BlockBehaviour.Properties.of().mapColor(MapColor.SNOW)
                        .sound(SoundType.GLASS).instrument(NoteBlockInstrument.HAT)
                        .strength(0.3F).noOcclusion().isRedstoneConductor(PokecubeItems::never)
                        .isValidSpawn(PokecubeItems::never).isSuffocating(PokecubeItems::never)
                        .isViewBlocking(PokecubeItems::never)));
        ONE_WAY_GLASS_SPECTRUM = PokecubeLegends.BLOCKS.register("one_way_spectrum_glass",
                () -> new OneWaySpectrumGlass(DyeColor.ORANGE, BlockBehaviour.Properties.of().mapColor(MapColor.SNOW)
                        .sound(SoundType.GLASS).instrument(NoteBlockInstrument.HAT)
                        .strength(0.3F).noOcclusion().isRedstoneConductor(PokecubeItems::never)
                        .isValidSpawn(PokecubeItems::never).isSuffocating(PokecubeItems::never)
                        .isViewBlocking(PokecubeItems::never)));
        ONE_WAY_DISTORTIC_FRAMED_MIRROR = PokecubeLegends.BLOCKS.register("one_way_distortic_framed_mirror",
                () -> new OneWayStainedGlass(DyeColor.WHITE, BlockBehaviour.Properties.of().mapColor(MapColor.SNOW)
                        .sound(SoundType.GLASS).instrument(NoteBlockInstrument.HAT)
                        .strength(0.3F).noOcclusion().isRedstoneConductor(PokecubeItems::never)
                        .isValidSpawn(PokecubeItems::never).isSuffocating(PokecubeItems::never)
                        .isViewBlocking(PokecubeItems::never)));

        // Tapus Totems
        TOTEM_BLOCK = PokecubeLegends.BLOCKS.register("totem_block",
                () -> new BlockBase(MapColor.COLOR_LIGHT_GRAY, SoundType.STONE, NoteBlockInstrument.BASS,
                        true, 3.0F, 6.0F));

        for (int i = 0; i < totemKeys.length; i++)
        {
            String key = totemKeys[i];
            MapColor colour = totemColours.get(key);
            KOKO[i] = PokecubeLegends.BLOCKS.register("koko" + key,
                    () -> new KokoTotem(BlockBehaviour.Properties.of().mapColor(colour).sound(SoundType.WOOD)
                            .instrument(NoteBlockInstrument.BASS).strength(3.0F, 5.0F)
                            .ignitedByLava().dynamicShape()));
            BULU[i] = PokecubeLegends.BLOCKS.register("bulu" + key,
                    () -> new BuluTotem(BlockBehaviour.Properties.of().mapColor(colour).sound(SoundType.WOOD)
                            .instrument(NoteBlockInstrument.BASS).strength(3.0F, 5.0F)
                            .ignitedByLava().dynamicShape()));
            LELE[i] = PokecubeLegends.BLOCKS.register("lele" + key,
                    () -> new LeleTotem(BlockBehaviour.Properties.of().mapColor(colour).sound(SoundType.WOOD)
                            .instrument(NoteBlockInstrument.BASS).strength(3.0F, 5.0F)
                            .ignitedByLava().dynamicShape()));
            FINI[i] = PokecubeLegends.BLOCKS.register("fini" + key,
                    () -> new FiniTotem(BlockBehaviour.Properties.of().mapColor(colour).sound(SoundType.WOOD)
                            .instrument(NoteBlockInstrument.BASS).strength(3.0F, 5.0F)
                            .ignitedByLava().dynamicShape()));
        }

        // Pokecube Blocks Creative Tab - Sorting depends on the order the
        // blocks are listed in
        // Block Raid
        RAID_SPAWNER = PokecubeLegends.BLOCKS.register("raid_spot_spawner",
                () -> new RaidSpawnBlock(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_RED)
                        .sound(SoundType.NETHERITE_BLOCK).strength(2000F, 2000F).randomTicks())
                        .setInfoBlockName("raid_spawner"));
        CRAMOMATIC_BLOCK = PokecubeLegends.BLOCKS.register("cramomatic_block",
                () -> new CramomaticBlock(BlockBehaviour.Properties.of().mapColor(MapColor.TERRACOTTA_RED)
                        .sound(SoundType.NETHERITE_BLOCK).strength(6.0F, 15.0F)
                        .dynamicShape().requiresCorrectToolForDrops()).setToolTip("cramobot"));

        // Mirage Spot (Hoopa Ring)
        MIRAGE_SPOTS = PokecubeLegends.BLOCKS.register("mirage_spot_block",
                () -> new PortalWarp("mirage_spot_block", BlockBehaviour.Properties.of().mapColor(MapColor.GOLD)
                        .sound(SoundType.METAL).strength(2000F, 2000F))
                        .setShape(Shapes.box(0.05, 0, 0.05, 1, 3, 1))
                        .setToolTip("portalwarp"));

        // Legendary Spawners
        // Regi Cores
        GOLEM_STONE = PokecubeLegends.BLOCKS.register("golem_stone",
                () -> new BlockBase(MapColor.TERRACOTTA_WHITE, SoundType.PACKED_MUD,
                        NoteBlockInstrument.BASEDRUM, true, 3.0F, 10.0F));
        REGISTEEL_CORE = PokecubeLegends.BLOCKS.register("registeel_spawn",
                () -> new FaceBlockBase(MapColor.TERRACOTTA_WHITE, Direction.NORTH, SoundType.PACKED_MUD,
                        NoteBlockInstrument.BASEDRUM, true, 3.0F, 10.0F));
        REGICE_CORE = PokecubeLegends.BLOCKS.register("regice_spawn",
                () -> new FaceBlockBase(MapColor.TERRACOTTA_WHITE, Direction.NORTH, SoundType.PACKED_MUD,
                        NoteBlockInstrument.BASEDRUM, true, 3.0F, 10.0F));
        REGIROCK_CORE = PokecubeLegends.BLOCKS.register("regirock_spawn",
                () -> new FaceBlockBase(MapColor.TERRACOTTA_WHITE, Direction.NORTH, SoundType.PACKED_MUD,
                        NoteBlockInstrument.BASEDRUM, true, 3.0F, 10.0F));
        REGIELEKI_CORE = PokecubeLegends.BLOCKS.register("regieleki_spawn",
                () -> new FaceBlockBase(MapColor.TERRACOTTA_WHITE, Direction.NORTH, SoundType.PACKED_MUD,
                        NoteBlockInstrument.BASEDRUM, true, 3.0F, 10.0F));
        REGIDRAGO_CORE = PokecubeLegends.BLOCKS.register("regidrago_spawn",
                () -> new FaceBlockBase(MapColor.TERRACOTTA_WHITE, Direction.NORTH, SoundType.PACKED_MUD,
                        NoteBlockInstrument.BASEDRUM, true, 3.0F, 10.0F));
        REGIGIGA_CORE = PokecubeLegends.BLOCKS.register("regigiga_spawn",
                () -> new FaceBlockBase(MapColor.TERRACOTTA_WHITE, Direction.NORTH, SoundType.PACKED_MUD,
                        NoteBlockInstrument.BASEDRUM, true, 3.0F, 10.0F));

        LEGENDARY_SPAWN = PokecubeLegends.BLOCKS.register("legendary_spawn",
                () -> new BlockBase(MapColor.GOLD, SoundType.NETHERITE_BLOCK,
                        NoteBlockInstrument.BASEDRUM, true, 5F, 15F));

        HEATRAN_BLOCK = PokecubeLegends.BLOCKS.register("heatran_spawn",
                () -> new HeatranBlock(BlockBehaviour.Properties.of().mapColor(MapColor.NETHER)
                        .strength(5.0F, 15.0F).sound(SoundType.NETHER_BRICKS)
                        .requiresCorrectToolForDrops().lightLevel(b -> 4).emissiveRendering((s, r, p) -> true)));

        MAGEARNA_BLOCK = PokecubeLegends.BLOCKS.register("magearna_spawn",
                () -> new MagearnaBlock(BlockBehaviour.Properties.of().mapColor(MapColor.SAND).sound(SoundType.STONE)
                        .strength(5, 15).requiresCorrectToolForDrops()));

        // Tapus
        TAPU_KOKO_CORE = PokecubeLegends.BLOCKS.register("koko_core",
                () -> new TapuKokoCore(BlockBehaviour.Properties.of().mapColor(MapColor.TERRACOTTA_YELLOW)
                        .sound(SoundType.WOOD).instrument(NoteBlockInstrument.BASS)
                        .strength(3.0F, 5.0F).ignitedByLava().dynamicShape()));
        TAPU_BULU_CORE = PokecubeLegends.BLOCKS.register("bulu_core",
                () -> new TapuBuluCore(BlockBehaviour.Properties.of().mapColor(MapColor.TERRACOTTA_RED)
                        .sound(SoundType.WOOD).instrument(NoteBlockInstrument.BASS)
                        .strength(3.0F, 5.0F).ignitedByLava().dynamicShape()));
        TAPU_LELE_CORE = PokecubeLegends.BLOCKS.register("lele_core",
                () -> new TapuLeleCore(BlockBehaviour.Properties.of().mapColor(MapColor.TERRACOTTA_PURPLE)
                        .sound(SoundType.WOOD).instrument(NoteBlockInstrument.BASS)
                        .strength(3.0F, 5.0F).ignitedByLava().dynamicShape()));
        TAPU_FINI_CORE = PokecubeLegends.BLOCKS.register("fini_core",
                () -> new TapuFiniCore(BlockBehaviour.Properties.of().mapColor(MapColor.TERRACOTTA_PINK)
                        .sound(SoundType.WOOD).instrument(NoteBlockInstrument.BASS)
                        .strength(3.0F, 5.0F).ignitedByLava().dynamicShape()));

        YVELTAL_CORE = PokecubeLegends.BLOCKS.register("yveltal_spawn",
                () -> new YveltalEgg(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_BLACK)
                        .sound(SoundType.WOOD).strength(2000F, 2000F).dynamicShape())
                                .setShape(Shapes.box(0.05, 0, 0.05, 1, 2, 1)));

        KELDEO_CORE = PokecubeLegends.BLOCKS.register("keldeo_spawn",
                () -> new KeldeoBlock(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_BLUE)
                        .sound(SoundType.STONE).strength(2000F, 2000F).dynamicShape())
                                .setShape(Shapes.box(0.05, 0, 0.05, 1, 1, 1)));

        TIMESPACE_CORE = PokecubeLegends.BLOCKS.register("timerspace_spawn",
                () -> new TimeSpaceCoreBlock(BlockBehaviour.Properties.of().mapColor(MapColor.STONE)
                        .sound(SoundType.STONE).strength(2000F, 2000F).dynamicShape())
                                .setShape(Shapes.box(0.05, 0, 0.05, 1, 2, 1)));

        NATURE_CORE = PokecubeLegends.BLOCKS.register("nature_spawn",
                () -> new NatureCoreBlock(BlockBehaviour.Properties.of().mapColor(MapColor.TERRACOTTA_WHITE)
                        .sound(SoundType.STONE).strength(2000F, 2000F).dynamicShape())
                                .setShape(Shapes.box(0.05, 0, 0.05, 1, 2, 1)));

        XERNEAS_CORE = PokecubeLegends.BLOCKS.register("xerneas_spawn",
                () -> new XerneasCore(BlockBehaviour.Properties.of().mapColor(MapColor.SNOW)
                        .sound(SoundType.AMETHYST).strength(2000F, 2000F).dynamicShape())
                                .setShape(Shapes.box(0.05, 0, 0.05, 1, 2, 1)));

        TAO_BLOCK = PokecubeLegends.BLOCKS.register("blackwhite_spawn",
                () -> new TaoTrioBlock(BlockBehaviour.Properties.of().mapColor(MapColor.SNOW)
                        .sound(SoundType.STONE).strength(5.0F, 15.0F).dynamicShape())
                                .setShape(Shapes.box(0.05, 0, 0.05, 1, 1, 1)));

        TROUGH_BLOCK = PokecubeLegends.BLOCKS.register("trough_spawn",
                () -> new TroughBlock(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_BROWN)
                        .sound(SoundType.NETHERITE_BLOCK).strength(5.0F, 15.0F).dynamicShape()
                        .requiresCorrectToolForDrops().lightLevel(b -> 4)));

        VICTINI_CORE = PokecubeLegends.BLOCKS.register("victini_spawn",
                () -> new VictiniBlock(BlockBehaviour.Properties.of().mapColor(MapColor.GOLD)
                        .sound(SoundType.NETHERITE_BLOCK).strength(5.0F, 15.0F).dynamicShape().requiresCorrectToolForDrops())
                                .setShape(Shapes.box(0.05, 0, 0.05, 1, 1, 1)));

        INFECTED_CAMPFIRE = PokecubeLegends.BLOCKS.register("infected_campfire",
                () -> new InfectedCampfireBlock(true, 2, BlockBehaviour.Properties.of()
                        .mapColor(MapColor.COLOR_GRAY).sound(SoundType.NETHER_WOOD).instrument(NoteBlockInstrument.BASS)
                        .strength(2.0F).noOcclusion().ignitedByLava().lightLevel(litBlockEmission(12))));

        INFECTED_LANTERN = PokecubeLegends.BLOCKS.register("infected_lantern",
                () -> new LanternBlock(BlockBehaviour.Properties.of().mapColor(MapColor.TERRACOTTA_PURPLE)
                        .sound(SoundType.LANTERN).pushReaction(PushReaction.DESTROY).strength(3.5F)
                        .noOcclusion().forceSolidOn().requiresCorrectToolForDrops().lightLevel(i -> 12)));

        // Signs
        AGED_SIGN = PokecubeLegends.NO_ITEM_BLOCKS.register("aged_sign",
                () -> new GenericStandingSign(LegendsBlockSetType.AGED_WOOD_TYPE,
                        BlockBehaviour.Properties.of().mapColor(MapColor.TERRACOTTA_GREEN)
                                .sound(SoundType.WOOD).instrument(NoteBlockInstrument.BASS)
                                .strength(1.0F).noCollission().forceSolidOn().ignitedByLava()));
        AGED_WALL_SIGN = PokecubeLegends.NO_ITEM_BLOCKS.register("aged_wall_sign",
                () -> new GenericWallSign(LegendsBlockSetType.AGED_WOOD_TYPE,
                        BlockBehaviour.Properties.of().mapColor(MapColor.TERRACOTTA_GREEN)
                                .sound(SoundType.WOOD).instrument(NoteBlockInstrument.BASS)
                                .strength(1.0F).noCollission().forceSolidOn().ignitedByLava()
                                .lootFrom(AGED_SIGN)));
        CONCRETE_SIGN = PokecubeLegends.NO_ITEM_BLOCKS.register("concrete_sign",
                () -> new GenericStandingSign(LegendsBlockSetType.CONCRETE_WOOD_TYPE,
                        BlockBehaviour.Properties.of().mapColor(MapColor.SNOW)
                                .sound(SoundType.STONE).instrument(NoteBlockInstrument.BASEDRUM)
                                .strength(10.0F, 500.0F).noCollission().forceSolidOn()));
        CONCRETE_WALL_SIGN = PokecubeLegends.NO_ITEM_BLOCKS.register("concrete_wall_sign",
                () -> new GenericWallSign(LegendsBlockSetType.CONCRETE_WOOD_TYPE,
                        BlockBehaviour.Properties.of().mapColor(MapColor.SNOW)
                                .sound(SoundType.STONE).instrument(NoteBlockInstrument.BASEDRUM)
                                .strength(10.0F, 500.0F).noCollission().forceSolidOn()
                                .lootFrom(CONCRETE_SIGN)));
        CONCRETE_DENSE_SIGN = PokecubeLegends.NO_ITEM_BLOCKS.register("concrete_dense_sign",
                () -> new GenericStandingSign(LegendsBlockSetType.CONCRETE_DENSE_WOOD_TYPE,
                        BlockBehaviour.Properties.of().mapColor(MapColor.SNOW)
                                .sound(SoundType.STONE).instrument(NoteBlockInstrument.BASEDRUM)
                                .strength(20.0F, 1200.0F).noCollission().forceSolidOn()));
        CONCRETE_DENSE_WALL_SIGN = PokecubeLegends.NO_ITEM_BLOCKS.register("concrete_dense_wall_sign",
                () -> new GenericWallSign(LegendsBlockSetType.CONCRETE_DENSE_WOOD_TYPE,
                        BlockBehaviour.Properties.of().mapColor(MapColor.SNOW)
                                .sound(SoundType.STONE).instrument(NoteBlockInstrument.BASEDRUM)
                                .strength(20.0F, 1200.0F).noCollission().forceSolidOn()
                                .lootFrom(CONCRETE_DENSE_SIGN)));
        CORRUPTED_SIGN = PokecubeLegends.NO_ITEM_BLOCKS.register("corrupted_sign",
                () -> new GenericStandingSign(LegendsBlockSetType.CORRUPTED_WOOD_TYPE,
                        BlockBehaviour.Properties.of().mapColor(MapColor.WOOD)
                                .sound(SoundType.NETHER_WOOD).instrument(NoteBlockInstrument.BASS)
                                .strength(1.0F).noCollission().forceSolidOn().ignitedByLava()));
        CORRUPTED_WALL_SIGN = PokecubeLegends.NO_ITEM_BLOCKS.register("corrupted_wall_sign",
                () -> new GenericWallSign(LegendsBlockSetType.CORRUPTED_WOOD_TYPE,
                        BlockBehaviour.Properties.of().mapColor(MapColor.WOOD)
                                .sound(SoundType.NETHER_WOOD).instrument(NoteBlockInstrument.BASS)
                                .strength(1.0F).noCollission().forceSolidOn().ignitedByLava()
                                .lootFrom(CORRUPTED_SIGN)));
        DISTORTIC_SIGN = PokecubeLegends.NO_ITEM_BLOCKS.register("distortic_sign",
                () -> new GenericStandingSign(LegendsBlockSetType.DISTORTIC_WOOD_TYPE,
                        BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_BLUE)
                                .sound(SoundType.NETHER_WOOD).instrument(NoteBlockInstrument.BASS)
                                .strength(1.0F).noCollission().forceSolidOn().ignitedByLava()));
        DISTORTIC_WALL_SIGN = PokecubeLegends.NO_ITEM_BLOCKS.register("distortic_wall_sign",
                () -> new GenericWallSign(LegendsBlockSetType.DISTORTIC_WOOD_TYPE,
                        BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_BLUE)
                                .sound(SoundType.NETHER_WOOD).instrument(NoteBlockInstrument.BASS)
                                .strength(1.0F).noCollission().forceSolidOn().ignitedByLava()
                                .lootFrom(DISTORTIC_SIGN)));
        INVERTED_SIGN = PokecubeLegends.NO_ITEM_BLOCKS.register("inverted_sign",
                () -> new GenericStandingSign(LegendsBlockSetType.INVERTED_WOOD_TYPE,
                        BlockBehaviour.Properties.of().mapColor(MapColor.TERRACOTTA_LIGHT_BLUE)
                                .sound(SoundType.WOOD).instrument(NoteBlockInstrument.BASS)
                                .strength(1.0F).noCollission().forceSolidOn().ignitedByLava()));
        INVERTED_WALL_SIGN = PokecubeLegends.NO_ITEM_BLOCKS.register("inverted_wall_sign",
                () -> new GenericWallSign(LegendsBlockSetType.INVERTED_WOOD_TYPE,
                        BlockBehaviour.Properties.of().mapColor(MapColor.TERRACOTTA_LIGHT_BLUE)
                                .sound(SoundType.WOOD).instrument(NoteBlockInstrument.BASS)
                                .strength(1.0F).noCollission().forceSolidOn().ignitedByLava()
                                .lootFrom(INVERTED_SIGN)));
        MIRAGE_SIGN = PokecubeLegends.NO_ITEM_BLOCKS.register("mirage_sign",
                () -> new GenericStandingSign(LegendsBlockSetType.MIRAGE_WOOD_TYPE,
                        BlockBehaviour.Properties.of().mapColor(MapColor.SAND)
                                .sound(SoundType.WOOD).instrument(NoteBlockInstrument.BASS)
                                .strength(1.0F).noCollission().forceSolidOn().ignitedByLava()));
        MIRAGE_WALL_SIGN = PokecubeLegends.NO_ITEM_BLOCKS.register("mirage_wall_sign",
                () -> new GenericWallSign(LegendsBlockSetType.MIRAGE_WOOD_TYPE,
                        BlockBehaviour.Properties.of().mapColor(MapColor.SAND)
                                .sound(SoundType.WOOD).instrument(NoteBlockInstrument.BASS)
                                .strength(1.0F).noCollission().forceSolidOn().ignitedByLava()
                                .lootFrom(MIRAGE_SIGN)));
        TEMPORAL_SIGN = PokecubeLegends.NO_ITEM_BLOCKS.register("temporal_sign",
                () -> new GenericStandingSign(LegendsBlockSetType.TEMPORAL_WOOD_TYPE,
                        BlockBehaviour.Properties.of().mapColor(MapColor.WARPED_NYLIUM)
                                .sound(SoundType.WOOD).instrument(NoteBlockInstrument.BASS)
                                .strength(1.0F).noCollission().forceSolidOn().ignitedByLava()));
        TEMPORAL_WALL_SIGN = PokecubeLegends.NO_ITEM_BLOCKS.register("temporal_wall_sign",
                () -> new GenericWallSign(LegendsBlockSetType.TEMPORAL_WOOD_TYPE,
                        BlockBehaviour.Properties.of().mapColor(MapColor.WARPED_NYLIUM)
                                .sound(SoundType.WOOD).instrument(NoteBlockInstrument.BASS)
                                .strength(1.0F).noCollission().forceSolidOn().ignitedByLava()
                                .lootFrom(TEMPORAL_SIGN)));

        ItemGenerator.SIGN_BLOCKS.addAll(Lists.newArrayList(BlockInit.AGED_SIGN, BlockInit.AGED_WALL_SIGN,
                BlockInit.CONCRETE_SIGN, BlockInit.CONCRETE_WALL_SIGN, BlockInit.CONCRETE_DENSE_SIGN,
                BlockInit.CONCRETE_DENSE_WALL_SIGN, BlockInit.CORRUPTED_SIGN, BlockInit.CORRUPTED_WALL_SIGN,
                BlockInit.DISTORTIC_SIGN, BlockInit.DISTORTIC_WALL_SIGN, BlockInit.INVERTED_SIGN,
                BlockInit.INVERTED_WALL_SIGN, BlockInit.MIRAGE_SIGN, BlockInit.MIRAGE_WALL_SIGN,
                BlockInit.TEMPORAL_SIGN, BlockInit.TEMPORAL_WALL_SIGN));

        // Hanging Signs
        AGED_HANGING_SIGN = PokecubeLegends.NO_ITEM_BLOCKS.register("aged_hanging_sign",
                () -> new GenericCeilingHangingSign(LegendsBlockSetType.AGED_WOOD_TYPE,
                        BlockBehaviour.Properties.of().mapColor(MapColor.TERRACOTTA_GREEN)
                                .sound(SoundType.WOOD).instrument(NoteBlockInstrument.BASS)
                                .strength(1.0F).noCollission().forceSolidOn().ignitedByLava()));
        AGED_WALL_HANGING_SIGN = PokecubeLegends.NO_ITEM_BLOCKS.register("aged_wall_hanging_sign",
                () -> new GenericWallHangingSign(LegendsBlockSetType.AGED_WOOD_TYPE,
                        BlockBehaviour.Properties.of().mapColor(MapColor.TERRACOTTA_GREEN)
                                .sound(SoundType.WOOD).instrument(NoteBlockInstrument.BASS)
                                .strength(1.0F).noCollission().forceSolidOn().ignitedByLava()
                                .lootFrom(AGED_HANGING_SIGN)));
        CONCRETE_HANGING_SIGN = PokecubeLegends.NO_ITEM_BLOCKS.register("concrete_hanging_sign",
                () -> new GenericCeilingHangingSign(LegendsBlockSetType.CONCRETE_WOOD_TYPE,
                        BlockBehaviour.Properties.of().mapColor(MapColor.SNOW)
                                .sound(SoundType.STONE).instrument(NoteBlockInstrument.BASS)
                                .strength(10.0F, 500.0F).noCollission().forceSolidOn()));
        CONCRETE_WALL_HANGING_SIGN = PokecubeLegends.NO_ITEM_BLOCKS.register("concrete_wall_hanging_sign",
                () -> new GenericWallHangingSign(LegendsBlockSetType.CONCRETE_WOOD_TYPE,
                        BlockBehaviour.Properties.of().mapColor(MapColor.SNOW)
                                .sound(SoundType.STONE).instrument(NoteBlockInstrument.BASS)
                                .strength(10.0F, 500.0F).noCollission().forceSolidOn()
                                .lootFrom(CONCRETE_HANGING_SIGN)));
        CORRUPTED_HANGING_SIGN = PokecubeLegends.NO_ITEM_BLOCKS.register("corrupted_hanging_sign",
                () -> new GenericCeilingHangingSign(LegendsBlockSetType.CORRUPTED_WOOD_TYPE,
                        BlockBehaviour.Properties.of().mapColor(MapColor.WOOD)
                                .sound(SoundType.NETHER_WOOD).instrument(NoteBlockInstrument.BASS)
                                .strength(1.0F).noCollission().forceSolidOn().ignitedByLava()));
        CORRUPTED_WALL_HANGING_SIGN = PokecubeLegends.NO_ITEM_BLOCKS.register("corrupted_wall_hanging_sign",
                () -> new GenericWallHangingSign(LegendsBlockSetType.CORRUPTED_WOOD_TYPE,
                        BlockBehaviour.Properties.of().mapColor(MapColor.WOOD)
                                .sound(SoundType.NETHER_WOOD).instrument(NoteBlockInstrument.BASS)
                                .strength(1.0F).noCollission().forceSolidOn().ignitedByLava()
                                .lootFrom(CORRUPTED_HANGING_SIGN)));
        DISTORTIC_HANGING_SIGN = PokecubeLegends.NO_ITEM_BLOCKS.register("distortic_hanging_sign",
                () -> new GenericCeilingHangingSign(LegendsBlockSetType.DISTORTIC_WOOD_TYPE,
                        BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_BLUE)
                                .sound(SoundType.NETHER_WOOD).instrument(NoteBlockInstrument.BASS)
                                .strength(1.0F).noCollission().forceSolidOn().ignitedByLava()));
        DISTORTIC_WALL_HANGING_SIGN = PokecubeLegends.NO_ITEM_BLOCKS.register("distortic_wall_hanging_sign",
                () -> new GenericWallHangingSign(LegendsBlockSetType.DISTORTIC_WOOD_TYPE,
                        BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_BLUE)
                                .sound(SoundType.NETHER_WOOD).instrument(NoteBlockInstrument.BASS)
                                .strength(1.0F).noCollission().forceSolidOn().ignitedByLava()
                                .lootFrom(DISTORTIC_HANGING_SIGN)));
        INVERTED_HANGING_SIGN = PokecubeLegends.NO_ITEM_BLOCKS.register("inverted_hanging_sign",
                () -> new GenericCeilingHangingSign(LegendsBlockSetType.INVERTED_WOOD_TYPE,
                        BlockBehaviour.Properties.of().mapColor(MapColor.TERRACOTTA_LIGHT_BLUE)
                                .sound(SoundType.WOOD).instrument(NoteBlockInstrument.BASS)
                                .strength(1.0F, 1.5F).noCollission().forceSolidOn().ignitedByLava()));
        INVERTED_WALL_HANGING_SIGN = PokecubeLegends.NO_ITEM_BLOCKS.register("inverted_wall_hanging_sign",
                () -> new GenericWallHangingSign(LegendsBlockSetType.INVERTED_WOOD_TYPE,
                        BlockBehaviour.Properties.of().mapColor(MapColor.TERRACOTTA_LIGHT_BLUE)
                                .sound(SoundType.WOOD).instrument(NoteBlockInstrument.BASS)
                                .strength(1.0F, 1.5F).noCollission().forceSolidOn().ignitedByLava()
                                .lootFrom(INVERTED_HANGING_SIGN)));
        MIRAGE_HANGING_SIGN = PokecubeLegends.NO_ITEM_BLOCKS.register("mirage_hanging_sign",
                () -> new GenericCeilingHangingSign(LegendsBlockSetType.MIRAGE_WOOD_TYPE,
                        BlockBehaviour.Properties.of().mapColor(MapColor.SAND)
                                .sound(SoundType.WOOD).instrument(NoteBlockInstrument.BASS)
                                .strength(1.0F).noCollission().forceSolidOn().ignitedByLava()));
        MIRAGE_WALL_HANGING_SIGN = PokecubeLegends.NO_ITEM_BLOCKS.register("mirage_wall_hanging_sign",
                () -> new GenericWallHangingSign(LegendsBlockSetType.MIRAGE_WOOD_TYPE,
                        BlockBehaviour.Properties.of().mapColor(MapColor.SAND)
                                .sound(SoundType.WOOD).instrument(NoteBlockInstrument.BASS)
                                .strength(1.0F).noCollission().forceSolidOn().ignitedByLava()
                                .lootFrom(MIRAGE_HANGING_SIGN)));
        TEMPORAL_HANGING_SIGN = PokecubeLegends.NO_ITEM_BLOCKS.register("temporal_hanging_sign",
                () -> new GenericCeilingHangingSign(LegendsBlockSetType.TEMPORAL_WOOD_TYPE,
                        BlockBehaviour.Properties.of().mapColor(MapColor.WARPED_NYLIUM)
                                .sound(SoundType.WOOD).instrument(NoteBlockInstrument.BASS)
                                .strength(1.0F).noCollission().forceSolidOn().ignitedByLava()));
        TEMPORAL_WALL_HANGING_SIGN = PokecubeLegends.NO_ITEM_BLOCKS.register("temporal_wall_hanging_sign",
                () -> new GenericWallHangingSign(LegendsBlockSetType.TEMPORAL_WOOD_TYPE,
                        BlockBehaviour.Properties.of().mapColor(MapColor.WARPED_NYLIUM)
                                .sound(SoundType.WOOD).instrument(NoteBlockInstrument.BASS)
                                .strength(1.0F).noCollission().forceSolidOn().ignitedByLava()
                                .lootFrom(TEMPORAL_HANGING_SIGN)));

        ItemGenerator.HANGING_SIGN_BLOCKS.addAll(Lists.newArrayList(BlockInit.AGED_HANGING_SIGN, BlockInit.AGED_WALL_HANGING_SIGN,
                BlockInit.CONCRETE_HANGING_SIGN, BlockInit.CONCRETE_WALL_HANGING_SIGN, BlockInit.CORRUPTED_HANGING_SIGN,
                BlockInit.CORRUPTED_WALL_HANGING_SIGN, BlockInit.DISTORTIC_HANGING_SIGN, BlockInit.DISTORTIC_WALL_HANGING_SIGN,
                BlockInit.INVERTED_HANGING_SIGN, BlockInit.INVERTED_WALL_HANGING_SIGN, BlockInit.MIRAGE_HANGING_SIGN,
                BlockInit.MIRAGE_WALL_HANGING_SIGN, BlockInit.TEMPORAL_HANGING_SIGN, BlockInit.TEMPORAL_WALL_HANGING_SIGN));
    }

    private static ToIntFunction<BlockState> litBlockEmission(final int i)
    {
        return (state) -> {
            return state.getValue(BlockStateProperties.LIT) ? i : 0;
        };
    }

    public static void init()
    {
        PlantsInit.registry();
        PottedPlantsInit.registry();

        for (final RegistryObject<Block> reg : PokecubeLegends.BLOCKS.getEntries())
        {
            PokecubeLegends.ITEMS.register(reg.getId().getPath(),
                    () -> new BlockItem(reg.get(), new Item.Properties()));
        }
    }
}
