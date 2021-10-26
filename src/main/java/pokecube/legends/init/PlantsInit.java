package pokecube.legends.init;

import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.material.MaterialColor;
import net.minecraftforge.fml.RegistryObject;
import pokecube.legends.PokecubeLegends;
import pokecube.legends.blocks.PlantBase;

public class PlantsInit
{

    // Plants
    public static RegistryObject<Block> DISTORCED_MUSHROOM;
    public static RegistryObject<Block> COMPRECED_MUSHROOM;
    public static RegistryObject<Block> GOLDEN_POPPY;
    public static RegistryObject<Block> INVERTED_ORCHID;

    static
    {
        PlantsInit.DISTORCED_MUSHROOM = PokecubeLegends.BLOCKS_TAB.register("mush_plant1", () -> new PlantBase(Material.PLANT,
        		MaterialColor.COLOR_PURPLE, 0f, 3f, SoundType.GRASS));
        PlantsInit.COMPRECED_MUSHROOM = PokecubeLegends.BLOCKS_TAB.register("mush_plant2", () -> new PlantBase(Material.PLANT,
        		MaterialColor.COLOR_PURPLE, 0f, 3f, SoundType.GRASS));
        PlantsInit.GOLDEN_POPPY = PokecubeLegends.BLOCKS_TAB.register("a1_flower", () -> new PlantBase(Material.PLANT,
        		MaterialColor.COLOR_YELLOW, 0f, 3f, SoundType.CORAL_BLOCK));
        PlantsInit.INVERTED_ORCHID = PokecubeLegends.BLOCKS_TAB.register("b1_flower", () -> new PlantBase(Material.PLANT,
        		MaterialColor.COLOR_PINK, 0f, 3f, SoundType.BAMBOO_SAPLING));
    }

    public static void registry() {

    }
}
