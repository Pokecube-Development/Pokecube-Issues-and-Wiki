package pokecube.legends.init;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.material.MaterialColor;
import net.minecraftforge.fmllegacy.RegistryObject;
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
    	//mush_plant1
        PlantsInit.DISTORCED_MUSHROOM = PokecubeLegends.DIMENSIONS_TAB.register("distorced_mushroom", () -> new PlantBase(Material.PLANT,
        		MaterialColor.COLOR_PURPLE, 0f, 3f, SoundType.GRASS));
        
        //mush_plant2
        PlantsInit.COMPRECED_MUSHROOM = PokecubeLegends.DIMENSIONS_TAB.register("compreced_mushroom", () -> new PlantBase(Material.PLANT,
        		MaterialColor.COLOR_PURPLE, 0f, 3f, SoundType.GRASS));
        
        //a1_flower
        PlantsInit.GOLDEN_POPPY = PokecubeLegends.DIMENSIONS_TAB.register("golden_poppy", () -> new PlantBase(Material.PLANT,
        		MaterialColor.COLOR_YELLOW, 0f, 3f, SoundType.CORAL_BLOCK));
        
        //b1_flower
        PlantsInit.INVERTED_ORCHID = PokecubeLegends.DIMENSIONS_TAB.register("inverted_orchid", () -> new PlantBase(Material.PLANT,
        		MaterialColor.COLOR_PINK, 0f, 3f, SoundType.BAMBOO_SAPLING));
    }

    public static void registry() {

    }
}
