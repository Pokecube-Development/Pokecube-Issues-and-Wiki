package pokecube.legends.init;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraftforge.common.ToolType;
import pokecube.legends.blocks.BlockBase;
import pokecube.legends.blocks.GrassJungleBlock;
import pokecube.legends.blocks.GrassMussBlock;
import pokecube.legends.blocks.LegendaryBlock;
import pokecube.legends.blocks.NatureCoreBlock;
import pokecube.legends.blocks.PortalWarp;
import pokecube.legends.blocks.Rotates;
import pokecube.legends.blocks.SandUltraBlock;
import pokecube.legends.blocks.SpaceCoreBlock;
import pokecube.legends.blocks.StoneMagneticBlock;
import pokecube.legends.blocks.UltraSpacePortal;

public class BlockInit
{
    public static List<Block> BLOCKS = new ArrayList<>();

    // Blocks
    public static Block RUBY_BLOCK       = new BlockBase("ruby_block", Material.IRON, 1.5f, 10f, SoundType.METAL).noInfoBlock();
    public static Block SAPPHIRE_BLOCK   = new BlockBase("sapphire_block", Material.IRON, 1.5f, 10f, SoundType.METAL).noInfoBlock();
    public static Block TEMPORAL_CRYSTAL = new BlockBase("temporal_crystal", Material.GLASS, 1.0f, 12f, SoundType.GLASS).noInfoBlock();

    // Decorative_Blocks
    public static Block OCEAN_BRICK   = new BlockBase("oceanbrick", Material.ROCK, 1.5f, 10f, SoundType.STONE).noInfoBlock();
    public static Block SKY_BRICK     = new BlockBase("skybrick", Material.ROCK, 1.5f, 10f, SoundType.STONE).noInfoBlock();
    public static Block SPATIAN_BRICK = new BlockBase("spatianbrick", Material.ROCK, 1.5f, 10f, SoundType.STONE).noInfoBlock();
    public static Block MAGMA_BRICK   = new BlockBase("magmabrick", Material.ROCK, 1.5f, 10f, SoundType.STONE).noInfoBlock();
    public static Block CRYSTAL_BRICK = new BlockBase("crystalbrick", Material.PACKED_ICE, 0.5F, SoundType.GLASS).noInfoBlock();
    public static Block DARKSKY_BRICK = new BlockBase("darkskybrick", Material.ROCK, 1.5f, 10f, SoundType.STONE).noInfoBlock();

    // Dimension and Decoration
    public static Block ULTRA_MAGNETIC  = new BlockBase("ultramagnetic", Material.GLASS, 0.5F, SoundType.GLASS).noInfoBlock();
    public static Block ULTRA_SANDSTONE = new BlockBase("ultrasandstone", Material.SAND, 0.5f, SoundType.SAND).noInfoBlock();
    public static Block ULTRA_COBBLES   = new StoneMagneticBlock("ultracobbles", Material.ROCK).noInfoBlock();
    public static Block ULTRA_GRASSMUSS = new GrassMussBlock("ultragrass1", Material.ORGANIC).noInfoBlock();
    public static Block ULTRA_DIRTMUSS  = new BlockBase("ultradirt1", Material.CLAY, 0.5f, SoundType.GROUND).noInfoBlock();
    public static Block ULTRA_GRASSJUN  = new GrassJungleBlock("ultragrass2", Material.ORGANIC).noInfoBlock().noInfoBlock();
    public static Block ULTRA_DIRTJUN   = new BlockBase("ultradirt2", Material.CLAY, 0.5f, SoundType.GROUND).noInfoBlock();
    public static Block ULTRA_STONE     = new BlockBase("ultrastone", Material.ROCK, 1.5f, 10f, SoundType.STONE).noInfoBlock();
    public static Block ULTRA_METAL     = new BlockBase("ultrablock", Material.IRON, 5.0f, 10f, SoundType.STONE).noInfoBlock();
    public static Block ULTRA_SAND      = new SandUltraBlock("ultrasand", Material.SAND).noInfoBlock();

    // public static Block ULTRA_MUSS1 = new MussPlant("mussplant1",
    // Material.PLANTS);
    // public static Block ULTRA_MUSS2 = new MussPlant1("mussplant2",
    // Material.PLANTS);

    // Portal
    public static Block ULTRASPACE_PORTAL = new UltraSpacePortal("ultraspace_portal", Block.Properties.create(
            Material.GLASS).sound(SoundType.GLASS).hardnessAndResistance(5, 15).harvestTool(ToolType.PICKAXE)
            .harvestLevel(3)).setShape(VoxelShapes.create(0.05, 0, 0.05, 1, 3, 1));
    public static Block BLOCK_PORTALWARP  = new PortalWarp("portal", Block.Properties.create(Material.ROCK).sound(
            SoundType.METAL).hardnessAndResistance(1, 10).harvestTool(ToolType.PICKAXE).harvestLevel(3)).setShape(
                    VoxelShapes.create(0.05, 0, 0.05, 1, 3, 1));
    // public static Block BLOCK_MAXRAID = new
    // MaxRaidSpawnBlock("maxraidspawn", Material.ROCK);

    // Legendary Spawns
    public static Block LEGENDARY_SPAWN = new LegendaryBlock("legendaryspawn", Material.IRON);
    public static Block REGISTEEL_CORE  = new BlockBase("registeel_spawn", Block.Properties.create(Material.IRON).sound(
            SoundType.METAL).hardnessAndResistance(5, 15).harvestTool(ToolType.PICKAXE).harvestLevel(3)).setInfoBlockName("registeelblock");
    public static Block REGICE_CORE     = new BlockBase("regice_spawn", Block.Properties.create(Material.PACKED_ICE)
            .sound(SoundType.GLASS).hardnessAndResistance(5, 15).harvestTool(ToolType.PICKAXE).harvestLevel(3)).setInfoBlockName("regiceblock");
    public static Block REGIROCK_CORE   = new BlockBase("regirock_spawn", Block.Properties.create(Material.ROCK).sound(
            SoundType.STONE).hardnessAndResistance(5, 15).harvestTool(ToolType.PICKAXE).harvestLevel(3)).setInfoBlockName("regirockblock");
    public static Block REGIGIGA_CORE   = new BlockBase("regigiga_spawn", Block.Properties.create(Material.IRON).sound(
            SoundType.METAL).hardnessAndResistance(5, 15).harvestTool(ToolType.PICKAXE).harvestLevel(3)).setInfoBlockName("regigigasblock");

    public static Block TIMESPACE_CORE = new SpaceCoreBlock("timerspawn", Block.Properties.create(Material.ORGANIC)
            .hardnessAndResistance(5, 15).harvestTool(ToolType.PICKAXE).harvestLevel(3).sound(SoundType.STONE)
            .lightValue(12).variableOpacity()).setShape(VoxelShapes.create(0.05, 0, 0.05, 1, 2, 1));
    public static Block NATURE_CORE    = new NatureCoreBlock("naturespawn", Block.Properties.create(Material.ROCK)
            .hardnessAndResistance(5, 15).harvestTool(ToolType.PICKAXE).harvestLevel(3).sound(SoundType.STONE)
            .lightValue(12).variableOpacity()).setShape(VoxelShapes.create(0.05, 0, 0.05, 1, 2, 1));

    public static Block KELDEO_CORE = new BlockBase("keldeoblock", Block.Properties.create(Material.ROCK)
            .hardnessAndResistance(5, 15).harvestTool(ToolType.PICKAXE).harvestLevel(3).sound(SoundType.STONE)
            .lightValue(12).variableOpacity()).setShape(VoxelShapes.create(0.05, 0, 0.05, 1, 1, 1));

    public static Block VICTINI_CORE = new Rotates("victiniblock", Block.Properties.create(Material.IRON)
            .hardnessAndResistance(5, 15).harvestTool(ToolType.PICKAXE).harvestLevel(3).sound(SoundType.ANVIL)
            .lightValue(4).variableOpacity()).setShape(VoxelShapes.create(0.05, 0, 0.05, 1, 1, 1));

    public static Block YVELTAL_CORE = new Rotates("yveltal_egg", Block.Properties.create(Material.IRON)
            .hardnessAndResistance(5, 15).harvestTool(ToolType.AXE).harvestLevel(3).sound(SoundType.WOOD).lightValue(2)
            .variableOpacity()).setShape(VoxelShapes.create(0.05, 0, 0.05, 1, 2, 1)).setInfoBlockName("yveltalblock");
    public static Block XERNEAS_CORE = new Rotates("xerneas_tree", Block.Properties.create(Material.IRON)
            .hardnessAndResistance(5, 15).harvestTool(ToolType.AXE).harvestLevel(3).sound(SoundType.WOOD).lightValue(12)
            .variableOpacity()).setShape(VoxelShapes.create(0.05, 0, 0.05, 1, 2, 1)).setInfoBlockName("xerneasblock");

    // Ores
    public static Block RUBY_ORE     = new BlockBase("ruby_ore", Block.Properties.create(Material.ROCK).sound(
            SoundType.STONE).hardnessAndResistance(5, 15).harvestTool(ToolType.PICKAXE).harvestLevel(2)).noInfoBlock();
    public static Block SAPPHIRE_ORE = new BlockBase("sapphire_ore", Block.Properties.create(Material.ROCK).sound(
            SoundType.STONE).hardnessAndResistance(5, 15).harvestTool(ToolType.PICKAXE).harvestLevel(2)).noInfoBlock();

    public static void init()
    {

    }

}
