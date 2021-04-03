package pokecube.legends.init;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.FlowingFluidBlock;
import net.minecraft.block.material.Material;
import net.minecraft.block.material.MaterialColor;
import net.minecraft.fluid.FlowingFluid;
import net.minecraft.item.BucketItem;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraftforge.fluids.ForgeFlowingFluid;
import net.minecraftforge.fml.RegistryObject;
import pokecube.legends.PokecubeLegends;
import pokecube.legends.fluids.DistorticWater;

public class FluidInit 
{
	public static RegistryObject<FlowingFluid> DISTORTED_WATER;
	public static RegistryObject<FlowingFluid> DISTORTED_WATER_FLOWING;
	public static RegistryObject<FlowingFluidBlock> DISTORTED_WATER_BLOCK;
	public static RegistryObject<Item> DISTORTED_WATER_BUCKET;

	static 
	{
		DISTORTED_WATER = PokecubeLegends.FLUIDS.register("distortic_water", () ->
    		new ForgeFlowingFluid.Source(DistorticWater.makeProperties().canMultiply()));
		DISTORTED_WATER_FLOWING = PokecubeLegends.FLUIDS.register("distortic_water_flowing", () ->
    		new ForgeFlowingFluid.Flowing(DistorticWater.makeProperties()));
		DISTORTED_WATER_BLOCK = PokecubeLegends.BLOCKS_TAB.register("distortic_water_block", () ->
			new FlowingFluidBlock(DISTORTED_WATER, AbstractBlock.Properties.of(Material.LAVA, MaterialColor.COLOR_LIGHT_BLUE)
						.noCollission().strength(100f).noDrops()));
		DISTORTED_WATER_BUCKET = PokecubeLegends.ITEMS.register("distortic_water_bucket", () ->
			new BucketItem(DISTORTED_WATER, new Item.Properties().craftRemainder(Items.BUCKET).stacksTo(1).tab(PokecubeLegends.DECO_TAB).fireResistant()));
	}
}
