package pokecube.legends.init;

import net.minecraft.tileentity.TileEntityType;
import net.minecraftforge.fml.RegistryObject;
import pokecube.legends.PokecubeLegends;
import pokecube.legends.tileentity.GenericBookshelfEmptyTile;
import pokecube.legends.tileentity.barrels.AgedPlankBarrelTile;
import pokecube.legends.tileentity.barrels.ConcreteBarrelTile;
import pokecube.legends.tileentity.barrels.CorruptedPlankBarrelTile;
import pokecube.legends.tileentity.barrels.MiragePlankBarrelTile;
import pokecube.legends.tileentity.barrels.DistorticPlankBarrelTile;
import pokecube.legends.tileentity.barrels.DistorticStoneBarrelTile;
import pokecube.legends.tileentity.barrels.InvertedPlankBarrelTile;
import pokecube.legends.tileentity.barrels.TemporalPlankBarrelTile;

public class TileEntityInit 
{
	// Tile
    public static final RegistryObject<TileEntityType<DistorticStoneBarrelTile>> DISTORTIC_STONE_BARREL_TILE;
    public static final RegistryObject<TileEntityType<ConcreteBarrelTile>> CONCRETE_PLANK_BARREL_TILE;
    public static final RegistryObject<TileEntityType<InvertedPlankBarrelTile>> INVERTED_PLANK_BARREL_TILE;
    public static final RegistryObject<TileEntityType<TemporalPlankBarrelTile>> TEMPORAL_PLANK_BARREL_TILE;
    public static final RegistryObject<TileEntityType<AgedPlankBarrelTile>> AGED_PLANK_BARREL_TILE;
    public static final RegistryObject<TileEntityType<MiragePlankBarrelTile>> CRYSTALLIZED_PLANK_BARREL_TILE;
    public static final RegistryObject<TileEntityType<CorruptedPlankBarrelTile>> CORRUPTED_PLANK_BARREL_TILE;
    public static final RegistryObject<TileEntityType<DistorticPlankBarrelTile>> DISTORTIC_PLANK_BARREL_TILE;
    public static final RegistryObject<TileEntityType<GenericBookshelfEmptyTile>> GENERIC_BOOKSHELF_EMPTY_TILE;

    static
    {
    	DISTORTIC_STONE_BARREL_TILE = PokecubeLegends.TILES.register("distortic_stone_barrel", () -> TileEntityType.Builder.of(
    			DistorticStoneBarrelTile::new, BlockInit.DISTORTIC_STONE_BARREL.get()).build(null));
    	CONCRETE_PLANK_BARREL_TILE = PokecubeLegends.TILES.register("concrete_barrel", () -> TileEntityType.Builder.of(
    			ConcreteBarrelTile::new, BlockInit.CONCRETE_BARREL.get()).build(null));
    	INVERTED_PLANK_BARREL_TILE = PokecubeLegends.TILES.register("inverted_barrel", () -> TileEntityType.Builder.of(
    			InvertedPlankBarrelTile::new, BlockInit.INVERTED_BARREL.get()).build(null));
    	TEMPORAL_PLANK_BARREL_TILE = PokecubeLegends.TILES.register("temporal_barrel", () -> TileEntityType.Builder.of(
    			TemporalPlankBarrelTile::new, BlockInit.TEMPORAL_BARREL.get()).build(null));
    	AGED_PLANK_BARREL_TILE = PokecubeLegends.TILES.register("aged_barrel", () -> TileEntityType.Builder.of(
    			AgedPlankBarrelTile::new, BlockInit.AGED_BARREL.get()).build(null));
    	CRYSTALLIZED_PLANK_BARREL_TILE = PokecubeLegends.TILES.register("crystallized_barrel", () -> TileEntityType.Builder.of(
    			MiragePlankBarrelTile::new, BlockInit.MIRAGE_BARREL.get()).build(null));
    	CORRUPTED_PLANK_BARREL_TILE = PokecubeLegends.TILES.register("corrupted_barrel", () -> TileEntityType.Builder.of(
    			CorruptedPlankBarrelTile::new, BlockInit.CORRUPTED_BARREL.get()).build(null));
		DISTORTIC_PLANK_BARREL_TILE = PokecubeLegends.TILES.register("distortic_barrel", () -> TileEntityType.Builder.of(
			DistorticPlankBarrelTile::new, BlockInit.DISTORTIC_BARREL.get()).build(null));
		GENERIC_BOOKSHELF_EMPTY_TILE = PokecubeLegends.TILES.register("generic_bookshelf_empty", () -> TileEntityType.Builder.of(
			GenericBookshelfEmptyTile::new, BlockInit.DISTORTIC_BOOKSHELF_EMPTY.get()).build(null));
    }
    
    public static void init() {}
}
