package pokecube.legends.init;

import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraftforge.common.ToolType;
import net.minecraftforge.fml.RegistryObject;
import pokecube.core.PokecubeItems;
import pokecube.legends.PokecubeLegends;
import pokecube.legends.blocks.BlockBase;
import pokecube.legends.blocks.GrassJungleBlock;
import pokecube.legends.blocks.GrassMussBlock;
import pokecube.legends.blocks.KeldeoBlock;
import pokecube.legends.blocks.LegendaryBlock;
import pokecube.legends.blocks.NatureCoreBlock;
import pokecube.legends.blocks.PortalWarp;
import pokecube.legends.blocks.RaidSpawnBlock;
import pokecube.legends.blocks.SandUltraBlock;
import pokecube.legends.blocks.SpaceCoreBlock;
import pokecube.legends.blocks.UltraSpacePortal;
import pokecube.legends.blocks.VictiniBlock;
import pokecube.legends.blocks.XerneasCore;
import pokecube.legends.blocks.YveltalEgg;

public class BlockInit
{
    // Blocks
    public static final RegistryObject<Block> RUBY_BLOCK;
    public static final RegistryObject<Block> SAPPHIRE_BLOCK;
    public static final RegistryObject<Block> TEMPORAL_CRYSTAL;
    public static final RegistryObject<Block> RAID_SPAWN;

    // Decorative_Blocks
    public static final RegistryObject<Block> OCEAN_BRICK;
    public static final RegistryObject<Block> SKY_BRICK;
    public static final RegistryObject<Block> SPATIAN_BRICK;
    public static final RegistryObject<Block> MAGMA_BRICK;
    public static final RegistryObject<Block> CRYSTAL_BRICK;
    public static final RegistryObject<Block> DARKSKY_BRICK;

    // Dimension and Decoration
    public static final RegistryObject<Block> ULTRA_MAGNETIC;
    public static final RegistryObject<Block> ULTRA_SANDSTONE;
    public static final RegistryObject<Block> ULTRA_COBBLES;
    public static final RegistryObject<Block> ULTRA_GRASSMUSS;
    public static final RegistryObject<Block> ULTRA_DIRTMUSS;
    public static final RegistryObject<Block> ULTRA_GRASSJUN;
    public static final RegistryObject<Block> ULTRA_DIRTJUN;
    public static final RegistryObject<Block> ULTRA_STONE;
    public static final RegistryObject<Block> ULTRA_METAL;
    public static final RegistryObject<Block> ULTRA_SAND;

    // Portal
    public static final RegistryObject<Block> ULTRASPACE_PORTAL;
    public static final RegistryObject<Block> BLOCK_PORTALWARP;

    // Legendary Spawns
    public static final RegistryObject<Block> LEGENDARY_SPAWN;
    public static final RegistryObject<Block> REGISTEEL_CORE;
    public static final RegistryObject<Block> REGICE_CORE;
    public static final RegistryObject<Block> REGIROCK_CORE;
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

    static
    {
        // Blocks
        RUBY_BLOCK = PokecubeLegends.BLOCKS.register("ruby_block", () -> new Block(Block.Properties.create(
                Material.IRON).hardnessAndResistance(1.5f, 10).sound(SoundType.METAL)));
        SAPPHIRE_BLOCK = PokecubeLegends.BLOCKS.register("sapphire_block", () -> new Block(Block.Properties.create(
                Material.IRON).hardnessAndResistance(1.5f, 10).sound(SoundType.METAL)));
        TEMPORAL_CRYSTAL = PokecubeLegends.BLOCKS.register("temporal_crystal", () -> new Block(Block.Properties.create(
                Material.GLASS).hardnessAndResistance(1.5f, 10).sound(SoundType.GLASS).notSolid()));
        RAID_SPAWN = PokecubeLegends.BLOCKS.register("raidspawn_block", () -> new RaidSpawnBlock(Material.IRON)
                .setInfoBlockName("raidspawn"));

        // Decorative_Blocks
        OCEAN_BRICK = PokecubeLegends.BLOCKS.register("oceanbrick", () -> new Block(Block.Properties.create(
                Material.ROCK).hardnessAndResistance(1.5f, 10).sound(SoundType.STONE)));
        SKY_BRICK = PokecubeLegends.BLOCKS.register("skybrick", () -> new Block(Block.Properties.create(Material.ROCK)
                .hardnessAndResistance(1.5f, 10).sound(SoundType.STONE)));
        SPATIAN_BRICK = PokecubeLegends.BLOCKS.register("spatianbrick", () -> new Block(Block.Properties.create(
                Material.ROCK).hardnessAndResistance(1.5f, 10).sound(SoundType.STONE)));
        MAGMA_BRICK = PokecubeLegends.BLOCKS.register("magmabrick", () -> new Block(Block.Properties.create(
                Material.ROCK).hardnessAndResistance(1.5f, 10).sound(SoundType.STONE)));
        DARKSKY_BRICK = PokecubeLegends.BLOCKS.register("darkskybrick", () -> new Block(Block.Properties.create(
                Material.ROCK).hardnessAndResistance(1.5f, 10).sound(SoundType.STONE)));

        CRYSTAL_BRICK = PokecubeLegends.BLOCKS.register("crystalbrick", () -> new BlockBase("crystalbrick",
                Material.PACKED_ICE, 0.5F, SoundType.GLASS).noInfoBlock());

        // Dimension and Decoration
        ULTRA_MAGNETIC = PokecubeLegends.BLOCKS.register("ultramagnetic", () -> new BlockBase("ultramagnetic",
                Material.GLASS, 0.5F, SoundType.GLASS).noInfoBlock());
        ULTRA_SANDSTONE = PokecubeLegends.BLOCKS.register("ultrasandstone", () -> new BlockBase("ultrasandstone",
                Material.SAND, 0.5f, SoundType.SAND).noInfoBlock());
        ULTRA_COBBLES = PokecubeLegends.BLOCKS.register("ultracobbles", () -> new Block(Block.Properties.create(
                Material.ROCK).hardnessAndResistance(1.5f, 10).sound(SoundType.STONE)));
        ULTRA_GRASSMUSS = PokecubeLegends.BLOCKS.register("ultragrass1", () -> new GrassMussBlock("ultragrass1",
                Material.ORGANIC).noInfoBlock());
        ULTRA_DIRTMUSS = PokecubeLegends.BLOCKS.register("ultradirt1", () -> new BlockBase("ultradirt1", Material.CLAY,
                0.5f, SoundType.GROUND).noInfoBlock());
        ULTRA_GRASSJUN = PokecubeLegends.BLOCKS.register("ultragrass2", () -> new GrassJungleBlock("ultragrass2",
                Material.ORGANIC).noInfoBlock().noInfoBlock());
        ULTRA_DIRTJUN = PokecubeLegends.BLOCKS.register("ultradirt2", () -> new BlockBase("ultradirt2", Material.CLAY,
                0.5f, SoundType.GROUND).noInfoBlock());
        ULTRA_STONE = PokecubeLegends.BLOCKS.register("ultrastone", () -> new Block(Block.Properties.create(
                Material.ROCK).hardnessAndResistance(1.5f, 10).sound(SoundType.STONE)));
        ULTRA_METAL = PokecubeLegends.BLOCKS.register("ultrablock", () -> new BlockBase("ultrablock", Material.IRON,
                5.0f, 10f, SoundType.STONE).noInfoBlock());
        ULTRA_SAND = PokecubeLegends.BLOCKS.register("ultrasand", () -> new SandUltraBlock("ultrasand", Material.SAND)
                .noInfoBlock());

        // Portal
        ULTRASPACE_PORTAL = PokecubeLegends.BLOCKS.register("ultraspace_portal", () -> new UltraSpacePortal(
                "ultraspace_portal", Block.Properties.create(Material.GLASS).sound(SoundType.GLASS)
                        .hardnessAndResistance(2000, 2000)).setShape(VoxelShapes.create(0.05, 0, 0.05, 1, 3, 1))
                                .setInfoBlockName("ultraportal"));
        BLOCK_PORTALWARP = PokecubeLegends.BLOCKS.register("portal", () -> new PortalWarp("portal", Block.Properties
                .create(Material.ROCK).sound(SoundType.METAL).hardnessAndResistance(2000, 2000)).setShape(VoxelShapes
                        .create(0.05, 0, 0.05, 1, 3, 1)).setInfoBlockName("portalwarp"));

        // Legendary Spawns
        LEGENDARY_SPAWN = PokecubeLegends.BLOCKS.register("legendaryspawn", () -> new LegendaryBlock("legendaryspawn",
                Material.IRON).noInfoBlock());
        REGISTEEL_CORE = PokecubeLegends.BLOCKS.register("registeel_spawn", () -> new BlockBase("registeel_spawn",
                Block.Properties.create(Material.IRON).sound(SoundType.METAL).hardnessAndResistance(5, 15).harvestTool(
                        ToolType.PICKAXE).harvestLevel(2)).noInfoBlock());
        REGICE_CORE = PokecubeLegends.BLOCKS.register("regice_spawn", () -> new BlockBase("regice_spawn",
                Block.Properties.create(Material.PACKED_ICE).sound(SoundType.GLASS).hardnessAndResistance(5, 15)
                        .harvestTool(ToolType.PICKAXE).harvestLevel(2)).noInfoBlock());
        REGIROCK_CORE = PokecubeLegends.BLOCKS.register("regirock_spawn", () -> new BlockBase("regirock_spawn",
                Block.Properties.create(Material.ROCK).sound(SoundType.STONE).hardnessAndResistance(5, 15).harvestTool(
                        ToolType.PICKAXE).harvestLevel(2)).noInfoBlock());
        REGIGIGA_CORE = PokecubeLegends.BLOCKS.register("regigiga_spawn", () -> new BlockBase("regigiga_spawn",
                Block.Properties.create(Material.IRON).sound(SoundType.METAL).hardnessAndResistance(5, 15).harvestTool(
                        ToolType.PICKAXE).harvestLevel(2)).noInfoBlock());
        TIMESPACE_CORE = PokecubeLegends.BLOCKS.register("timerspawn", () -> new SpaceCoreBlock("timerspawn",
                Block.Properties.create(Material.ORGANIC).hardnessAndResistance(2000, 2000).sound(SoundType.STONE)
                        .lightValue(12).variableOpacity()).setShape(VoxelShapes.create(0.05, 0, 0.05, 1, 2, 1))
                                .noInfoBlock());
        NATURE_CORE = PokecubeLegends.BLOCKS.register("naturespawn", () -> new NatureCoreBlock("naturespawn",
                Block.Properties.create(Material.ROCK).hardnessAndResistance(2000, 2000).sound(SoundType.STONE)
                        .lightValue(12).variableOpacity()).setShape(VoxelShapes.create(0.05, 0, 0.05, 1, 2, 1))
                                .noInfoBlock());
        KELDEO_CORE = PokecubeLegends.BLOCKS.register("keldeoblock", () -> new KeldeoBlock("keldeoblock",
                Block.Properties.create(Material.ROCK).hardnessAndResistance(2000, 2000).sound(SoundType.STONE)
                        .lightValue(12).variableOpacity()).setShape(VoxelShapes.create(0.05, 0, 0.05, 1, 1, 1))
                                .noInfoBlock());
        VICTINI_CORE = PokecubeLegends.BLOCKS.register("victiniblock", () -> new VictiniBlock("victiniblock",
                Block.Properties.create(Material.IRON).hardnessAndResistance(5, 15).harvestTool(ToolType.PICKAXE)
                        .harvestLevel(2).sound(SoundType.ANVIL).lightValue(4).variableOpacity()).setShape(VoxelShapes
                                .create(0.05, 0, 0.05, 1, 1, 1)).noInfoBlock());
        YVELTAL_CORE = PokecubeLegends.BLOCKS.register("yveltal_egg", () -> new YveltalEgg("yveltal_egg",
                Block.Properties.create(Material.IRON).hardnessAndResistance(2000, 2000).sound(SoundType.WOOD)
                        .lightValue(2).variableOpacity()).setShape(VoxelShapes.create(0.05, 0, 0.05, 1, 2, 1))
                                .noInfoBlock());
        XERNEAS_CORE = PokecubeLegends.BLOCKS.register("xerneas_tree", () -> new XerneasCore("xerneas_tree",
                Block.Properties.create(Material.IRON).hardnessAndResistance(2000, 2000).sound(SoundType.WOOD)
                        .lightValue(12).variableOpacity()).setShape(VoxelShapes.create(0.05, 0, 0.05, 1, 2, 1))
                                .noInfoBlock());

        // Ores
        RUBY_ORE = PokecubeLegends.BLOCKS.register("ruby_ore", () -> new BlockBase("ruby_ore", Block.Properties.create(
                Material.ROCK).sound(SoundType.STONE).hardnessAndResistance(5, 15).harvestTool(ToolType.PICKAXE)
                .harvestLevel(2)).noInfoBlock());
        SAPPHIRE_ORE = PokecubeLegends.BLOCKS.register("sapphire_ore", () -> new BlockBase("sapphire_ore",
                Block.Properties.create(Material.ROCK).sound(SoundType.STONE).hardnessAndResistance(5, 15).harvestTool(
                        ToolType.PICKAXE).harvestLevel(2)).noInfoBlock());

    }

    public static void init()
    {
        PlantsInit.init();
        for (final RegistryObject<Block> reg : PokecubeLegends.BLOCKS.getEntries())
            PokecubeLegends.ITEMS.register(reg.getId().getPath(), () -> new BlockItem(reg.get(), new Item.Properties()
                    .group(PokecubeItems.POKECUBEBLOCKS)));
    }

}
