package thut.api.terrain;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.chunk.ChunkSource;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.world.ChunkEvent;
import net.minecraftforge.event.world.ChunkWatchEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import thut.api.maths.Vector3;
import thut.api.terrain.CapabilityTerrain.DefaultProvider;
import thut.api.util.PermNodes;
import thut.api.util.PermNodes.DefaultPermissionLevel;
import thut.core.common.network.TerrainUpdate;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE)
public class TerrainManager
{
    public static final String EDIT_SUBBIOMES_PERM = "thutcore.subbiome.can_edit";

    public static final ResourceLocation TERRAINCAP = new ResourceLocation("thutcore", "terrain");
    private static TerrainManager terrain;

    public static void init()
    {
        PermNodes.registerNode(TerrainManager.EDIT_SUBBIOMES_PERM, DefaultPermissionLevel.OP,
                "Is the player allowed to edit subbiomes");
    }

    public static void clear()
    {}

    public static TerrainManager getInstance()
    {
        if (TerrainManager.terrain == null) TerrainManager.terrain = new TerrainManager();
        return TerrainManager.terrain;
    }

    public static boolean isAreaLoaded(final LevelAccessor world, final Vector3 centre, final double distance)
    {
        return TerrainManager.isAreaLoaded(world, centre.getPos(), distance);
    }

    public static boolean isAreaLoaded(final LevelAccessor world, final BlockPos blockPos, final double distance)
    {
        if (world.getChunkSource() == null) return false;
        ChunkSource source = world.getChunkSource();
        final int r = (int) distance >> 4;
        final int x = blockPos.getX() >> 4;
        final int z = blockPos.getZ() >> 4;
        for (int i = -r; i <= r; i++) for (int j = -r; j <= r; j++)
        {
            // getChunkNow returns null if the chunk is not a fully loaded
            // chunk, and on the server thread.
            if (source.getChunkNow(x + i, z + j) == null) return false;
        }
        return true;
    }

    @SubscribeEvent
    public static void onChunkLoad(final ChunkEvent.Load evt)
    {
        ResourceKey<Level> dim = null;
        if (evt.getWorld() instanceof Level && !evt.getWorld().isClientSide())
            dim = ((Level) evt.getWorld()).dimension();
        // This is null when this is loaded off-thread, IE before the chunk is
        // finished
        if (dim != null) ITerrainProvider.addChunk(dim, evt.getChunk());
    }

    @SubscribeEvent
    public static void onChunkUnload(final ChunkEvent.Unload evt)
    {
        ResourceKey<Level> dim = null;
        if (evt.getWorld() instanceof Level && !evt.getWorld().isClientSide())
            dim = ((Level) evt.getWorld()).dimension();
        if (dim != null) ITerrainProvider.removeChunk(dim, evt.getChunk().getPos());
    }

    @SubscribeEvent
    public static void onChunkWatch(final ChunkWatchEvent.Watch event)
    {
        final ServerPlayer player = event.getPlayer();
        TerrainUpdate.sendTerrainToClient(event.getPos(), player);
    }

    @SubscribeEvent
    public static void onWorldUnload(final WorldEvent.Unload evt)
    {

    }

    @SubscribeEvent
    public static void onCapabilityAttach(final AttachCapabilitiesEvent<LevelChunk> event)
    {
        if (event.getCapabilities().containsKey(TerrainManager.TERRAINCAP)) return;
        final LevelChunk chunk = event.getObject();
        final DefaultProvider terrain = new DefaultProvider(chunk);
        event.addCapability(TerrainManager.TERRAINCAP, terrain);
    }

    public ITerrainProvider provider = new ITerrainProvider()
    {
    };

    public TerrainSegment getTerrain(final LevelAccessor world, final BlockPos p)
    {
        return this.provider.getTerrain(world, p);
    }

    public TerrainSegment getTerrain(final LevelAccessor world, final double x, final double y, final double z)
    {
        final BlockPos pos = new BlockPos(x, y, z);
        final TerrainSegment ret = this.getTerrain(world, pos);
        if (world instanceof ServerLevel) ret.initBiomes(world);
        return ret;
    }

    public TerrainSegment getTerrainForEntity(final Entity e)
    {
        if (e == null) return null;
        return this.getTerrain(e.getCommandSenderWorld(), e.getX(), e.getY(), e.getZ());
    }

    public TerrainSegment getTerrian(final LevelAccessor world, final Vector3 v)
    {
        return this.getTerrain(world, v.x, v.y, v.z);
    }
}
