package pokecube.legends.init;

import net.minecraft.tileentity.TileEntityType;
import net.minecraftforge.fml.RegistryObject;
import pokecube.legends.PokecubeLegends;
import pokecube.core.tileentity.GenericBookshelfEmptyTile;
import pokecube.core.tileentity.GenericBarrelTile;

public class TileEntityInit 
{
	// Tile
    public static final RegistryObject<TileEntityType<GenericBarrelTile>> GENERIC_BARREL_TILE;
    public static final RegistryObject<TileEntityType<GenericBookshelfEmptyTile>> GENERIC_BOOKSHELF_EMPTY_TILE;

    static
    {
    	GENERIC_BARREL_TILE = PokecubeLegends.TILES.register("generic_barrel", () -> TileEntityType.Builder.of(
    	    GenericBarrelTile::new, BlockInit.AGED_BARREL.get(), BlockInit.CONCRETE_BARREL.get(), BlockInit.INVERTED_BARREL.get(),
			BlockInit.TEMPORAL_BARREL.get(), BlockInit.MIRAGE_BARREL.get(), BlockInit.CORRUPTED_BARREL.get(),
			BlockInit.DISTORTIC_BARREL.get(), BlockInit.DISTORTIC_STONE_BARREL.get()).build(null));
		GENERIC_BOOKSHELF_EMPTY_TILE = PokecubeLegends.TILES.register("generic_bookshelf_empty", () -> TileEntityType.Builder.of(
			GenericBookshelfEmptyTile::new, BlockInit.DISTORTIC_BOOKSHELF_EMPTY.get()).build(null));
    }
    
    public static void init() {}
}
