package pokecube.legends.blocks.plants;

import java.util.Collections;
import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.FlowerBlock;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Effects;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.storage.loot.LootContext;
import net.minecraftforge.common.PlantType;
import pokecube.core.PokecubeItems;
import pokecube.legends.Reference;
import pokecube.legends.init.ItemInit;
import pokecube.legends.init.PlantsInit;

public class PlantBase extends FlowerBlock
{	
	public static final Block block = null;
	
	public PlantBase(final String name, final Material material, final float hardness, final float resistance,
            final SoundType sound)
    {
        super(Effects.SATURATION, 0, Block.Properties.create(material).hardnessAndResistance(hardness, resistance)
        		.doesNotBlockMovement().sound(sound).lightValue(2));
        this.initName(name);
    }
	
    private void initName(final String name)
    {
        this.setRegistryName(Reference.ID, name);
        PlantsInit.BLOCKFLOWERS.add(this);
        ItemInit.ITEMS.add(new BlockItem(this, new Item.Properties().group(PokecubeItems.POKECUBEBLOCKS))
                .setRegistryName(this.getRegistryName()));
    }
    
	@SuppressWarnings("deprecation")
	@Override
	public List<ItemStack> getDrops(BlockState state, LootContext.Builder builder) {
		List<ItemStack> dropsOriginal = super.getDrops(state, builder);
		if (!dropsOriginal.isEmpty())
			return dropsOriginal;
		return Collections.singletonList(new ItemStack(this, 1));
	}

	@Override
	public PlantType getPlantType(IBlockReader world, BlockPos pos) {
		return PlantType.Plains;
	}
}
