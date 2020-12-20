package pokecube.pokeplayer.init;

import net.minecraft.tileentity.TileEntityType;
import net.minecraftforge.fml.RegistryObject;
import pokecube.pokeplayer.Pokeplayer;
import pokecube.pokeplayer.tileentity.TileEntityTransformer;

public class TileEntityInit {

	// Tile
    public static final RegistryObject<TileEntityType<TileEntityTransformer>> TRANSFORM_TILE;
    
    static
    {
    	TRANSFORM_TILE = Pokeplayer.TILES.register("pokeplayer_transform", () -> TileEntityType.Builder.create(
    			TileEntityTransformer::new, BlockInit.TRANSFORM.get()).build(null));
    }
    
    public static void init() {}
}
