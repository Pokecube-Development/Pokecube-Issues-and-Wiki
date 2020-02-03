package pokecube.legends.worldgen.dimension;

import net.minecraft.util.ResourceLocation;
import net.minecraft.world.dimension.DimensionType;
import net.minecraftforge.common.ModDimension;
import net.minecraftforge.registries.ObjectHolder;
import pokecube.legends.Reference;

public class ModDimensions {
	
	public static final ResourceLocation DIMENSION_ID = new ResourceLocation(Reference.ID, "ultraspace");
	
	@ObjectHolder("pokecube_legends:ultraspace")
	public static ModDimension DIMENSION;
	
	public static DimensionType DIMENSION_TYPE;

}
