package thut.api.terrain;

import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.IWorld;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.world.ChunkEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import thut.api.maths.Vector3;
import thut.api.terrain.CapabilityTerrain.DefaultProvider;

public class TerrainManager
{
    public static final ResourceLocation TERRAINCAP = new ResourceLocation("thutcore", "terrain");
    private static TerrainManager        terrain;

    public static void clear()
    {
    }

    public static TerrainManager getInstance()
    {
        if (TerrainManager.terrain == null) TerrainManager.terrain = new TerrainManager();
        return TerrainManager.terrain;
    }

    public static boolean isAreaLoaded(final IWorld world, final Vector3 centre, final double distance)
    {
        return TerrainManager.isAreaLoaded(world, centre.getPos(), distance);
    }

    public static boolean isAreaLoaded(final IWorld world, final BlockPos blockPos, final double distance)
    {
        if (world.isRemote()) return world.getChunk(blockPos) != null;
        DimensionType dim = null;
        if (world != null && world.getDimension() != null) dim = world.getDimension().getType();
        return TerrainManager.isAreaLoaded(dim, blockPos, distance);
    }

    public static boolean isAreaLoaded(final DimensionType dim, final BlockPos blockPos, final double distance)
    {
        if (dim == null) return false;
        final int r = (int) distance >> 4;
        final int x = blockPos.getX() >> 4;
        final int z = blockPos.getZ() >> 4;
        for (int i = -r; i <= r; i++)
            for (int j = -r; j <= r; j++)
            {
                final ChunkPos pos = new ChunkPos(x + i, z + j);
                if (!TerrainManager.chunkIsReal(dim, pos)) return false;
            }
        return true;
    }

    public static boolean chunkIsReal(final IWorld world, final BlockPos blockPos)
    {
        return TerrainManager.chunkIsReal(world, new ChunkPos(blockPos));
    }

    public static boolean chunkIsReal(final IWorld world, final ChunkPos pos)
    {
        DimensionType dim = null;
        if (world != null && world.getDimension() != null) dim = world.getDimension().getType();
        return TerrainManager.chunkIsReal(dim, pos);
    }

    public static boolean chunkIsReal(final DimensionType dim, final ChunkPos pos)
    {
        if (dim == null) return false;
        return ITerrainProvider.getChunk(dim, pos) != null;
    }

    @SubscribeEvent
    public static void onChunkLoad(final ChunkEvent.Load evt)
    {
        DimensionType dim = null;
        if (evt.getWorld() != null && evt.getWorld().getDimension() != null && !evt.getWorld().isRemote()) dim = evt
                .getWorld().getDimension().getType();
        // This is null when this is loaded off-thread, IE before the chunk is
        // finished
        if (dim != null) ITerrainProvider.addChunk(dim, evt.getChunk());
    }

    @SubscribeEvent
    public static void onChunkUnload(final ChunkEvent.Unload evt)
    {
        DimensionType dim = null;
        if (evt.getWorld() != null && evt.getWorld().getDimension() != null && !evt.getWorld().isRemote()) dim = evt
                .getWorld().getDimension().getType();
        if (dim != null) ITerrainProvider.removeChunk(dim, evt.getChunk().getPos());
    }

    @SubscribeEvent
    public static void onWorldUnload(final WorldEvent.Unload evt)
    {

    }

    @SubscribeEvent
    public static void onCapabilityAttach(final AttachCapabilitiesEvent<Chunk> event)
    {
        if (event.getCapabilities().containsKey(TerrainManager.TERRAINCAP)) return;
        final Chunk chunk = event.getObject();
        final DefaultProvider terrain = new DefaultProvider(chunk);
        event.addCapability(TerrainManager.TERRAINCAP, terrain);
    }

    public ITerrainProvider provider = new ITerrainProvider()
    {
    };

    public TerrainSegment getTerrain(final IWorld world, final BlockPos p)
    {
        return this.provider.getTerrain(world, p);
    }

    public TerrainSegment getTerrain(final IWorld world, final double x, final double y, final double z)
    {
        final BlockPos pos = new BlockPos(x, y, z);
        final TerrainSegment ret = this.getTerrain(world, pos);
        if (world instanceof ServerWorld) ret.initBiomes(world);
        return ret;
    }

    public TerrainSegment getTerrainForEntity(final Entity e)
    {
        if (e == null) return null;
        return this.getTerrain(e.getEntityWorld(), e.getPosX(), e.getPosY(), e.getPosZ());
    }

    public TerrainSegment getTerrian(final IWorld world, final Vector3 v)
    {
        return this.getTerrain(world, v.x, v.y, v.z);
    }
}
