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
    public static RegistryObject<Block> MUSH_PLANT1;
    public static RegistryObject<Block> MUSH_PLANT2;
    public static RegistryObject<Block> AGED_FLOWER;
    public static RegistryObject<Block> DIRST_FLOWER;

    static
    {
        PlantsInit.MUSH_PLANT1 = PokecubeLegends.BLOCKS_TAB.register("mush_plant1", () -> new PlantBase(Material.PLANT, 
        		MaterialColor.COLOR_PURPLE, 0f, 3f, SoundType.GRASS));
        PlantsInit.MUSH_PLANT2 = PokecubeLegends.BLOCKS_TAB.register("mush_plant2", () -> new PlantBase(Material.PLANT,
        		MaterialColor.COLOR_PURPLE, 0f, 3f, SoundType.GRASS));
        PlantsInit.AGED_FLOWER = PokecubeLegends.BLOCKS_TAB.register("a1_flower", () -> new PlantBase(Material.PLANT,
        		MaterialColor.COLOR_YELLOW, 0f, 3f, SoundType.CORAL_BLOCK));
        PlantsInit.DIRST_FLOWER = PokecubeLegends.BLOCKS_TAB.register("b1_flower", () -> new PlantBase(Material.PLANT,
        		MaterialColor.COLOR_PINK, 0f, 3f, SoundType.BAMBOO_SAPLING));
    }

    public static void registry() {

    }
}
