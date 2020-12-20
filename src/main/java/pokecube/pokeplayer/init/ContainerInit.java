package pokecube.pokeplayer.init;

import net.minecraft.inventory.container.ContainerType;
import net.minecraftforge.common.extensions.IForgeContainerType;
import net.minecraftforge.fml.RegistryObject;
import pokecube.pokeplayer.Pokeplayer;
import pokecube.pokeplayer.block.PokeTransformContainer;

public class ContainerInit {

	// Tile
    public static final RegistryObject<ContainerType<PokeTransformContainer>> TRANSFORM_CONTAINER;
    
    static
    {
    	TRANSFORM_CONTAINER = Pokeplayer.CONTAINER.register("pokeplayer_transform", () -> IForgeContainerType.create(
    			PokeTransformContainer::new));
    }
    
    public static void init() {}
}
