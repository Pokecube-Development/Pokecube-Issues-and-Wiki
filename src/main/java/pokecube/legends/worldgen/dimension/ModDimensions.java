package pokecube.legends.worldgen.dimension;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.gen.Heightmap.Type;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.ModDimension;
import net.minecraftforge.registries.ObjectHolder;
import pokecube.legends.Reference;

public class ModDimensions
{

    public static final ResourceLocation DIMENSION_ULTRASPACE = new ResourceLocation(Reference.ID, "ultraspace");
    public static final ResourceLocation DIMENSION_DISTORTIC = new ResourceLocation(Reference.ID, "distorticw");
    
    @ObjectHolder("pokecube_legends:ultraspace")
    public static ModDimension DIMENSION_U;

    @ObjectHolder("pokecube_legends:distorticw")
    public static ModDimension DIMENSION_D;
    
    public static DimensionType DIMENSION_TYPE_US;
    public static DimensionType DIMENSION_TYPE_DW;

    public static BlockPos getTransferPoint(final ServerPlayerEntity player, final MinecraftServer server,
            final DimensionType targetDim)
    {
        final ServerWorld world = server.getWorld(targetDim);
        // Load the chunk before checking height.
        world.getChunk(player.getPosition());
        // Find height
        final BlockPos top = world.getHeight(Type.MOTION_BLOCKING, player.getPosition());
        return top;
    }
}
