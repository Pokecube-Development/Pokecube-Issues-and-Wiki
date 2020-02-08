package pokecube.legends.worldgen.dimension;

import java.util.UUID;

import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.dimension.DimensionType;
import net.minecraftforge.common.ModDimension;
import net.minecraftforge.registries.ObjectHolder;
import pokecube.legends.Reference;

public class ModDimensions {
	
	public static final ResourceLocation DIMENSION_ID = new ResourceLocation(Reference.ID, "ultraspace");
	
	@ObjectHolder("pokecube_legends:ultraspace")
	public static ModDimension DIMENSION;
	
	public static DimensionType DIMENSION_TYPE;

	public static BlockPos getSecretBaseLoc(UUID baseOwner, MinecraftServer server, DimensionType targetDim) {
		// TODO Auto-generated method stub
		return null;
	}
}
