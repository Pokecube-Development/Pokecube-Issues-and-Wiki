package pokecube.legends.init;

import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.registries.RegistryObject;
import pokecube.legends.PokecubeLegends;
import pokecube.legends.tileentity.CustomBarrelContainer;

public class ContainerInit {

	// Tile
    public static final RegistryObject<MenuType<CustomBarrelContainer>> CUSTOM_CHEST_CONTAINER;
    
    static
    {
    	CUSTOM_CHEST_CONTAINER = PokecubeLegends.CONTAINER.register("container_custom_chest", 
    			() -> new MenuType<>(CustomBarrelContainer::threeRows));

    }
    
    public static void init() {}
}