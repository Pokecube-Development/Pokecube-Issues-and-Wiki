package pokecube.legends.init;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.minecraft.world.level.block.state.properties.WoodType;

public class LegendsBlockSetType
{
    public static final BlockSetType AGED_BLOCK_SET = BlockSetType.register(new BlockSetType("aged"));
    public static final BlockSetType AQUAMARINE_BLOCK_SET = BlockSetType.register(new BlockSetType("aquamarine",
            true, SoundType.AMETHYST, SoundEvents.IRON_DOOR_CLOSE, SoundEvents.IRON_DOOR_OPEN,
            SoundEvents.IRON_TRAPDOOR_CLOSE, SoundEvents.IRON_TRAPDOOR_OPEN,
            SoundEvents.STONE_PRESSURE_PLATE_CLICK_OFF, SoundEvents.STONE_PRESSURE_PLATE_CLICK_ON,
            SoundEvents.STONE_BUTTON_CLICK_OFF, SoundEvents.STONE_BUTTON_CLICK_ON));
    public static final BlockSetType CONCRETE_BLOCK_SET = BlockSetType.register(new BlockSetType("concrete",
            true, SoundType.STONE, SoundEvents.IRON_DOOR_CLOSE, SoundEvents.IRON_DOOR_OPEN,
            SoundEvents.IRON_TRAPDOOR_CLOSE, SoundEvents.IRON_TRAPDOOR_OPEN,
            SoundEvents.STONE_PRESSURE_PLATE_CLICK_OFF, SoundEvents.STONE_PRESSURE_PLATE_CLICK_ON,
            SoundEvents.STONE_BUTTON_CLICK_OFF, SoundEvents.STONE_BUTTON_CLICK_ON));
    public static final BlockSetType CONCRETE_DENSE_BLOCK_SET = BlockSetType.register(new BlockSetType("concrete_dense",
            true, SoundType.STONE, SoundEvents.IRON_DOOR_CLOSE, SoundEvents.IRON_DOOR_OPEN,
            SoundEvents.IRON_TRAPDOOR_CLOSE, SoundEvents.IRON_TRAPDOOR_OPEN,
            SoundEvents.STONE_PRESSURE_PLATE_CLICK_OFF, SoundEvents.STONE_PRESSURE_PLATE_CLICK_ON,
            SoundEvents.STONE_BUTTON_CLICK_OFF, SoundEvents.STONE_BUTTON_CLICK_ON));
    public static final BlockSetType CORRUPTED_BLOCK_SET = BlockSetType.register(new BlockSetType("corrupted",
            true, SoundType.NETHER_WOOD, SoundEvents.NETHER_WOOD_DOOR_CLOSE, SoundEvents.NETHER_WOOD_DOOR_OPEN,
            SoundEvents.NETHER_WOOD_TRAPDOOR_CLOSE, SoundEvents.NETHER_WOOD_TRAPDOOR_OPEN,
            SoundEvents.NETHER_WOOD_PRESSURE_PLATE_CLICK_OFF, SoundEvents.NETHER_WOOD_PRESSURE_PLATE_CLICK_ON,
            SoundEvents.NETHER_WOOD_BUTTON_CLICK_OFF, SoundEvents.NETHER_WOOD_BUTTON_CLICK_ON));
    public static final BlockSetType DISTORTIC_BLOCK_SET = BlockSetType.register(new BlockSetType("distortic",
            true, SoundType.NETHER_WOOD, SoundEvents.NETHER_WOOD_DOOR_CLOSE, SoundEvents.NETHER_WOOD_DOOR_OPEN,
            SoundEvents.NETHER_WOOD_TRAPDOOR_CLOSE, SoundEvents.NETHER_WOOD_TRAPDOOR_OPEN,
            SoundEvents.NETHER_WOOD_PRESSURE_PLATE_CLICK_OFF, SoundEvents.NETHER_WOOD_PRESSURE_PLATE_CLICK_ON,
            SoundEvents.NETHER_WOOD_BUTTON_CLICK_OFF, SoundEvents.NETHER_WOOD_BUTTON_CLICK_ON));
    public static final BlockSetType DUSK_DOLERITE_BLOCK_SET = BlockSetType.register(new BlockSetType("dusk_dolerite",
            true, SoundType.DEEPSLATE, SoundEvents.IRON_DOOR_CLOSE, SoundEvents.IRON_DOOR_OPEN,
            SoundEvents.IRON_TRAPDOOR_CLOSE, SoundEvents.IRON_TRAPDOOR_OPEN,
            SoundEvents.STONE_PRESSURE_PLATE_CLICK_OFF, SoundEvents.STONE_PRESSURE_PLATE_CLICK_ON,
            SoundEvents.STONE_BUTTON_CLICK_OFF, SoundEvents.STONE_BUTTON_CLICK_ON));
    public static final BlockSetType INVERTED_BLOCK_SET = BlockSetType.register(new BlockSetType("inverted"));
    public static final BlockSetType MIRAGE_BLOCK_SET = BlockSetType.register(new BlockSetType("mirage"));
    public static final BlockSetType SANDSTONE_BLOCK_SET = BlockSetType.register(new BlockSetType("sandstone",
            true, SoundType.DEEPSLATE, SoundEvents.IRON_DOOR_CLOSE, SoundEvents.IRON_DOOR_OPEN,
            SoundEvents.IRON_TRAPDOOR_CLOSE, SoundEvents.IRON_TRAPDOOR_OPEN,
            SoundEvents.STONE_PRESSURE_PLATE_CLICK_OFF, SoundEvents.STONE_PRESSURE_PLATE_CLICK_ON,
            SoundEvents.STONE_BUTTON_CLICK_OFF, SoundEvents.STONE_BUTTON_CLICK_ON));
    public static final BlockSetType TEMPORAL_BLOCK_SET = BlockSetType.register(new BlockSetType("temporal"));
    public static final BlockSetType ULTRA_DARKSTONE_BLOCK_SET = BlockSetType.register(new BlockSetType("ultra_darkstone",
            true, SoundType.NETHER_BRICKS, SoundEvents.IRON_DOOR_CLOSE, SoundEvents.IRON_DOOR_OPEN,
            SoundEvents.IRON_TRAPDOOR_CLOSE, SoundEvents.IRON_TRAPDOOR_OPEN,
            SoundEvents.STONE_PRESSURE_PLATE_CLICK_OFF, SoundEvents.STONE_PRESSURE_PLATE_CLICK_ON,
            SoundEvents.STONE_BUTTON_CLICK_OFF, SoundEvents.STONE_BUTTON_CLICK_ON));
    public static final BlockSetType ULTRA_STONE_BLOCK_SET = BlockSetType.register(new BlockSetType("ultra_stone",
            true, SoundType.STONE, SoundEvents.IRON_DOOR_CLOSE, SoundEvents.IRON_DOOR_OPEN,
            SoundEvents.IRON_TRAPDOOR_CLOSE, SoundEvents.IRON_TRAPDOOR_OPEN,
            SoundEvents.STONE_PRESSURE_PLATE_CLICK_OFF, SoundEvents.STONE_PRESSURE_PLATE_CLICK_ON,
            SoundEvents.STONE_BUTTON_CLICK_OFF, SoundEvents.STONE_BUTTON_CLICK_ON));
    public static final BlockSetType ULTRA_METAL_BLOCK_SET = BlockSetType.register(new BlockSetType("ultra_metal",
            true, SoundType.NETHERITE_BLOCK, SoundEvents.IRON_DOOR_CLOSE, SoundEvents.IRON_DOOR_OPEN,
            SoundEvents.IRON_TRAPDOOR_CLOSE, SoundEvents.IRON_TRAPDOOR_OPEN,
            SoundEvents.METAL_PRESSURE_PLATE_CLICK_OFF, SoundEvents.METAL_PRESSURE_PLATE_CLICK_ON,
            SoundEvents.STONE_BUTTON_CLICK_OFF, SoundEvents.STONE_BUTTON_CLICK_ON));

    public static final WoodType AGED_WOOD_TYPE = WoodType.register(new WoodType("aged", AGED_BLOCK_SET));
    public static final WoodType CONCRETE_WOOD_TYPE = WoodType.register(new WoodType("concrete", CONCRETE_BLOCK_SET,
            SoundType.STONE, SoundType.HANGING_SIGN,
            SoundEvents.FENCE_GATE_CLOSE, SoundEvents.FENCE_GATE_OPEN));
    public static final WoodType CONCRETE_DENSE_WOOD_TYPE = WoodType.register(new WoodType("concrete_dense", CONCRETE_DENSE_BLOCK_SET,
            SoundType.STONE, SoundType.HANGING_SIGN,
            SoundEvents.FENCE_GATE_CLOSE, SoundEvents.FENCE_GATE_OPEN));
    public static final WoodType CORRUPTED_WOOD_TYPE = WoodType.register(new WoodType("corrupted", CORRUPTED_BLOCK_SET,
            SoundType.NETHER_WOOD, SoundType.NETHER_WOOD_HANGING_SIGN,
            SoundEvents.NETHER_WOOD_FENCE_GATE_CLOSE, SoundEvents.NETHER_WOOD_FENCE_GATE_OPEN));
    public static final WoodType DISTORTIC_WOOD_TYPE = WoodType.register(new WoodType("distortic", DISTORTIC_BLOCK_SET,
            SoundType.NETHER_WOOD, SoundType.NETHER_WOOD_HANGING_SIGN,
            SoundEvents.NETHER_WOOD_FENCE_GATE_CLOSE, SoundEvents.NETHER_WOOD_FENCE_GATE_OPEN));
    public static final WoodType INVERTED_WOOD_TYPE = WoodType.register(new WoodType("inverted", INVERTED_BLOCK_SET));
    public static final WoodType MIRAGE_WOOD_TYPE = WoodType.register(new WoodType("mirage", MIRAGE_BLOCK_SET));
    public static final WoodType TEMPORAL_WOOD_TYPE = WoodType.register(new WoodType("temporal", TEMPORAL_BLOCK_SET));
}
