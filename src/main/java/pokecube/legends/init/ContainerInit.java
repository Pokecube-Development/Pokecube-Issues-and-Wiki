package pokecube.legends.init;

import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.registries.RegistryObject;
import pokecube.legends.PokecubeLegends;
import pokecube.legends.tileentity.GenericBarrelMenu;

public class ContainerInit {

	// Tile
    public static final RegistryObject<MenuType<GenericBarrelMenu>> BARREL_MENU;
    
    static
    {
    	BARREL_MENU = PokecubeLegends.MENU.register("barrel_menu", () -> new MenuType<>(GenericBarrelMenu::threeRows));
    }
    
    public static void init() {}
}