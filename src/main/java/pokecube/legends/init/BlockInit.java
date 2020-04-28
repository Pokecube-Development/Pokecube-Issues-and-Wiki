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
import pokecube.legends.blocks.KeldeoBlock;
import pokecube.legends.blocks.LegendaryBlock;
import pokecube.legends.blocks.NatureCoreBlock;
import pokecube.legends.blocks.PortalWarp;
import pokecube.legends.blocks.SandUltraBlock;
import pokecube.legends.blocks.SpaceCoreBlock;
import pokecube.legends.blocks.StoneMagneticBlock;
import pokecube.legends.blocks.UltraSpacePortal;
import pokecube.legends.blocks.VictiniBlock;
import pokecube.legends.blocks.XerneasCore;
import pokecube.legends.blocks.YveltalEgg;

public class BlockInit
{
    public static List<Block> BLOCKS = new ArrayList<>();

    // Blocks
    public static Block RUBY_BLOCK       = new BlockBase("ruby_block", Material.IRON, 1.5f, 10f, SoundType.METAL)
            .noInfoBlock();
    public static Block SAPPHIRE_BLOCK   = new BlockBase("sapphire_block", Material.IRON, 1.5f, 10f, SoundType.METAL)
            .noInfoBlock();
    public static Block TEMPORAL_CRYSTAL = new BlockBase("temporal_crystal", Material.GLASS, 1.0f, 12f, SoundType.GLASS)
            .noInfoBlock();
    public static Block RAID_SPAWN = new LegendaryBlock("raidspawn_block", Material.IRON).setInfoBlockName("raidspawn");

    // Decorative_Blocks
    public static Block OCEAN_BRICK   = new BlockBase("oceanbrick", Material.ROCK, 1.5f, 10f, SoundType.STONE)
            .noInfoBlock();
    public static Block SKY_BRICK     = new BlockBase("skybrick", Material.ROCK, 1.5f, 10f, SoundType.STONE)
            .noInfoBlock();
    public static Block SPATIAN_BRICK = new BlockBase("spatianbrick", Material.ROCK, 1.5f, 10f, SoundType.STONE)
            .noInfoBlock();
    public static Block MAGMA_BRICK   = new BlockBase("magmabrick", Material.ROCK, 1.5f, 10f, SoundType.STONE)
            .noInfoBlock();
    public static Block CRYSTAL_BRICK = new BlockBase("crystalbrick", Material.PACKED_ICE, 0.5F, SoundType.GLASS)
            .noInfoBlock();
    public static Block DARKSKY_BRICK = new BlockBase("darkskybrick", Material.ROCK, 1.5f, 10f, SoundType.STONE)
            .noInfoBlock();

    // Dimension and Decoration
    public static Block ULTRA_MAGNETIC  = new BlockBase("ultramagnetic", Material.GLASS, 0.5F, SoundType.GLASS)
            .noInfoBlock();
    public static Block ULTRA_SANDSTONE = new BlockBase("ultrasandstone", Material.SAND, 0.5f, SoundType.SAND)
            .noInfoBlock();
    public static Block ULTRA_COBBLES   = new StoneMagneticBlock("ultracobbles", Material.ROCK).noInfoBlock();
    public static Block ULTRA_GRASSMUSS = new GrassMussBlock("ultragrass1", Material.ORGANIC).noInfoBlock();
    public static Block ULTRA_DIRTMUSS  = new BlockBase("ultradirt1", Material.CLAY, 0.5f, SoundType.GROUND)
            .noInfoBlock();
    public static Block ULTRA_GRASSJUN  = new GrassJungleBlock("ultragrass2", Material.ORGANIC).noInfoBlock()
            .noInfoBlock();
    public static Block ULTRA_DIRTJUN   = new BlockBase("ultradirt2", Material.CLAY, 0.5f, SoundType.GROUND)
            .noInfoBlock();
    public static Block ULTRA_STONE     = new BlockBase("ultrastone", Material.ROCK, 1.5f, 10f, SoundType.STONE)
            .noInfoBlock();
    public static Block ULTRA_METAL     = new BlockBase("ultrablock", Material.IRON, 5.0f, 10f, SoundType.STONE)
            .noInfoBlock();
    public static Block ULTRA_SAND      = new SandUltraBlock("ultrasand", Material.SAND).noInfoBlock();

    // Portal
    public static Block ULTRASPACE_PORTAL = new UltraSpacePortal("ultraspace_portal", Block.Properties.create(
            Material.GLASS).sound(SoundType.GLASS).hardnessAndResistance(2000, 2000)).setShape(VoxelShapes.create(0.05,
                    0, 0.05, 1, 3, 1)).setInfoBlockName("ultraportal");
    public static Block BLOCK_PORTALWARP  = new PortalWarp("portal", Block.Properties.create(Material.ROCK).sound(
            SoundType.METAL).hardnessAndResistance(2000, 2000)).setShape(VoxelShapes.create(0.05, 0, 0.05, 1, 3, 1))
                    .setInfoBlockName("portalwarp");

    // Legendary Spawns
    public static Block LEGENDARY_SPAWN = new LegendaryBlock("legendaryspawn", Material.IRON).noInfoBlock();
    public static Block REGISTEEL_CORE  = new BlockBase("registeel_spawn", Block.Properties.create(Material.IRON).sound(
            SoundType.METAL).hardnessAndResistance(5, 15).harvestTool(ToolType.PICKAXE).harvestLevel(2)).noInfoBlock();
    public static Block REGICE_CORE     = new BlockBase("regice_spawn", Block.Properties.create(Material.PACKED_ICE)
            .sound(SoundType.GLASS).hardnessAndResistance(5, 15).harvestTool(ToolType.PICKAXE).harvestLevel(2))
                    .noInfoBlock();
    public static Block REGIROCK_CORE   = new BlockBase("regirock_spawn", Block.Properties.create(Material.ROCK).sound(
            SoundType.STONE).hardnessAndResistance(5, 15).harvestTool(ToolType.PICKAXE).harvestLevel(2)).noInfoBlock();
    public static Block REGIGIGA_CORE   = new BlockBase("regigiga_spawn", Block.Properties.create(Material.IRON).sound(
            SoundType.METAL).hardnessAndResistance(2000, 2000)).noInfoBlock();

    public static Block TIMESPACE_CORE = new SpaceCoreBlock("timerspawn", Block.Properties.create(Material.ORGANIC)
            .hardnessAndResistance(2000, 2000).sound(SoundType.STONE).lightValue(12).variableOpacity()).setShape(
                    VoxelShapes.create(0.05, 0, 0.05, 1, 2, 1)).noInfoBlock();;
    public static Block NATURE_CORE    = new NatureCoreBlock("naturespawn", Block.Properties.create(Material.ROCK)
            .hardnessAndResistance(2000, 2000).sound(SoundType.STONE).lightValue(12).variableOpacity()).setShape(
                    VoxelShapes.create(0.05, 0, 0.05, 1, 2, 1)).noInfoBlock();;

    public static Block KELDEO_CORE = new KeldeoBlock("keldeoblock", Block.Properties.create(Material.ROCK)
            .hardnessAndResistance(2000, 2000).sound(SoundType.STONE).lightValue(12).variableOpacity()).setShape(
                    VoxelShapes.create(0.05, 0, 0.05, 1, 1, 1)).noInfoBlock();;

    public static Block VICTINI_CORE = new VictiniBlock("victiniblock", Block.Properties.create(Material.IRON)
            .hardnessAndResistance(2000, 2000).sound(SoundType.ANVIL).lightValue(4).variableOpacity()).setShape(
                    VoxelShapes.create(0.05, 0, 0.05, 1, 1, 1)).noInfoBlock();;

    public static Block YVELTAL_CORE = new YveltalEgg("yveltal_egg", Block.Properties.create(Material.IRON)
            .hardnessAndResistance(2000, 2000).sound(SoundType.WOOD).lightValue(2).variableOpacity()).setShape(
                    VoxelShapes.create(0.05, 0, 0.05, 1, 2, 1)).noInfoBlock();
    public static Block XERNEAS_CORE = new XerneasCore("xerneas_tree", Block.Properties.create(Material.IRON)
            .hardnessAndResistance(2000, 2000).sound(SoundType.WOOD).lightValue(12).variableOpacity()).setShape(
                    VoxelShapes.create(0.05, 0, 0.05, 1, 2, 1)).noInfoBlock();

    // Ores
    public static Block RUBY_ORE     = new BlockBase("ruby_ore", Block.Properties.create(Material.ROCK).sound(
            SoundType.STONE).hardnessAndResistance(5, 15).harvestTool(ToolType.PICKAXE).harvestLevel(2)).noInfoBlock();
    public static Block SAPPHIRE_ORE = new BlockBase("sapphire_ore", Block.Properties.create(Material.ROCK).sound(
            SoundType.STONE).hardnessAndResistance(5, 15).harvestTool(ToolType.PICKAXE).harvestLevel(2)).noInfoBlock();

    public static void init()
    {

    }

}
