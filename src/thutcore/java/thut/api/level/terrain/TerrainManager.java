package thut.api.level.terrain;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.chunk.ChunkSource;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.level.ChunkWatchEvent;
import net.neoforged.neoforge.event.level.LevelEvent;
import thut.api.maths.Vector3;
import thut.api.util.PermNodes;
import thut.api.util.PermNodes.DefaultPermissionLevel;
import thut.core.common.ThutCore;
import thut.core.common.network.TerrainUpdate;

@EventBusSubscriber
public class TerrainManager
{
    public static final String EDIT_SUBBIOMES_PERM = "subbiome.can_edit";

    private static TerrainManager terrain;

    public static void init()
    {
        PermNodes.registerBooleanNode(ThutCore.MODID, TerrainManager.EDIT_SUBBIOMES_PERM, DefaultPermissionLevel.OP,
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
        for (int i = -r; i <= r; i++)
            for (int j = -r; j <= r; j++)
            {
                // getChunkNow returns null if the chunk is not a fully loaded
                // chunk, and on the server thread.
                if (source.getChunkNow(x + i, z + j) == null) return false;
            }
        return true;
    }

    @SubscribeEvent
    public static void onChunkWatch(final ChunkWatchEvent.Sent event)
    {
        final ServerPlayer player = event.getPlayer();
        TerrainUpdate.sendTerrainToClient(event.getPos(), player);
    }

    @SubscribeEvent
    public static void onWorldUnload(final LevelEvent.Unload evt)
    {

    }

    public ITerrainProvider provider = new ITerrainProvider()
    {};

    public TerrainSegment getTerrain(final LevelAccessor world, final BlockPos p)
    {
        return this.provider.getTerrain(world, p);
    }

    public TerrainSegment getTerrain(final LevelAccessor world, final double x, final double y, final double z)
    {
        final BlockPos pos = new BlockPos((int) x, (int) y, (int) z);
        final TerrainSegment ret = this.getTerrain(world, pos);
        if (world instanceof ServerLevel) ret.initBiomes(world);
        return ret;
    }

    public TerrainSegment getTerrainForEntity(final Entity e)
    {
        if (e == null) return null;
        final TerrainSegment ret = this.getTerrain(e.level(), e.getOnPos());
        if (e.level() instanceof ServerLevel) ret.initBiomes(e.level());
        return ret;
    }

    public TerrainSegment getTerrian(final LevelAccessor world, final Vector3 v)
    {
        return this.getTerrain(world, v.x, v.y, v.z);
    }
}
