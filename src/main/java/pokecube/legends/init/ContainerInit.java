package pokecube.legends.init;

import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.registries.RegistryObject;
import pokecube.legends.PokecubeLegends;
import pokecube.legends.tileentity.CustomBarrelContainer;

public class ContainerInit {

	// Tile
    public static final RegistryObject<MenuType<CustomBarrelContainer>> BARREL_CONTAINER;
    
    static
    {
    	BARREL_CONTAINER = PokecubeLegends.CONTAINER.register("barrel_container",
    			() -> new MenuType<>(CustomBarrelContainer::threeRows));

    }
    
    public static void init() {}
}