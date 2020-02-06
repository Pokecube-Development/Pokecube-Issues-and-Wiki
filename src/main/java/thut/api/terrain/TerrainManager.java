package thut.api.terrain;

import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.event.AttachCapabilitiesEvent;
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
        return this.getTerrain(e.getEntityWorld(), e.posX, e.posY, e.posZ);
    }

    public TerrainSegment getTerrian(final IWorld world, final Vector3 v)
    {
        return this.getTerrain(world, v.x, v.y, v.z);
    }
}
