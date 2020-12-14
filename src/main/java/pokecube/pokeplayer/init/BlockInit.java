package pokecube.pokeplayer.init;

import net.minecraft.block.Block;
import net.minecraft.block.PressurePlateBlock.Sensitivity;
import net.minecraft.block.material.Material;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraftforge.fml.RegistryObject;
import pokecube.core.PokecubeItems;
import pokecube.pokeplayer.Pokeplayer;
import pokecube.pokeplayer.blocks.PokePlayerBlock;

public class BlockInit {

	public static final RegistryObject<Block> POKEPLAYER_BLOCK;
	
	static 
	{
		POKEPLAYER_BLOCK = Pokeplayer.BLOCKS.register("pokeplayer_transform", () -> 
		new PokePlayerBlock(Sensitivity.MOBS, Block.Properties.create(Material.ROCK).hardnessAndResistance(100)));
	}
	
	public static void init()
    {
        for (final RegistryObject<Block> reg : Pokeplayer.BLOCKS.getEntries())
        	Pokeplayer.ITEMS.register(reg.getId().getPath(), () -> new BlockItem(reg.get(), new Item.Properties()
                   .group(PokecubeItems.POKECUBEBLOCKS)));
    }
}
