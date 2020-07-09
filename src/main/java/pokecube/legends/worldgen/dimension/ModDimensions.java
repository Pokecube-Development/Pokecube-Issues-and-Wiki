package pokecube.legends.worldgen.dimension;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.DimensionType;
import net.minecraft.world.gen.Heightmap.Type;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.ModDimension;
import net.minecraftforge.registries.ObjectHolder;
import pokecube.legends.Reference;

public class ModDimensions
{

    public static final ResourceLocation DIMENSION_ID = new ResourceLocation(Reference.ID, "ultraspace");

    @ObjectHolder("pokecube_legends:ultraspace")
    public static ModDimension DIMENSION;

    public static DimensionType DIMENSION_TYPE;

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
