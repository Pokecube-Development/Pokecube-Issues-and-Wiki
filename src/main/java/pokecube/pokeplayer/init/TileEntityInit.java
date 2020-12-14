package pokecube.pokeplayer.init;

import net.minecraft.tileentity.TileEntityType;
import net.minecraftforge.fml.RegistryObject;
import pokecube.pokeplayer.Pokeplayer;
import pokecube.pokeplayer.blocks.PokePlayerTileEntity;

public class TileEntityInit {
	
	public static final RegistryObject<TileEntityType<PokePlayerTileEntity>> POKEPLAYER_TILE;
	
	static
	{
		POKEPLAYER_TILE = Pokeplayer.TILES.register("pokeplayer_transform", () -> TileEntityType.Builder.create(
				PokePlayerTileEntity::new, BlockInit.POKEPLAYER_BLOCK.get()).build(null));
	}
	
	public static void init() {}
}
