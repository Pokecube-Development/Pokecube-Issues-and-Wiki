package pokecube.legends.init;

import net.minecraft.inventory.container.ContainerType;
import net.minecraftforge.fml.RegistryObject;
import pokecube.legends.PokecubeLegends;
import pokecube.legends.tileentity.CustomBarrelContainer;

public class ContainerInit {

	// Tile
    public static final RegistryObject<ContainerType<CustomBarrelContainer>> CUSTOM_CHEST_CONTAINER;
    
    static
    {
    	CUSTOM_CHEST_CONTAINER = PokecubeLegends.CONTAINER.register("container_custom_chest", 
    			() -> new ContainerType<>(CustomBarrelContainer::threeRows));

    }
    
    public static void init() {}
}