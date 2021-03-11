package thut.api.terrain;

import java.util.Collections;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.google.common.collect.Sets;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.MutableBoundingBox;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.structure.Structure;
import net.minecraft.world.gen.feature.structure.StructurePiece;
import net.minecraft.world.gen.feature.structure.StructureStart;
import net.minecraftforge.event.world.ChunkEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class StructureManager
{
    public static class StructureInfo
    {
        public String         name;
        public StructureStart<?> start;

        private int    hash;
        private String key;

        public StructureInfo()
        {
        }

        public StructureInfo(final Entry<Structure<?>, StructureStart<?>> entry)
        {
            this.name = entry.getKey().getFeatureName();
            this.start = entry.getValue();
            this.key = this.name + " " + this.start.getBoundingBox();
            this.hash = this.key.hashCode();
        }

        public boolean isIn(final BlockPos pos)
        {
            if (!this.start.getBoundingBox().isInside(pos)) return false;
            for (final StructurePiece p1 : this.start.getPieces())
                if (this.isIn(p1.getBoundingBox(), pos)) return true;
            return false;
        }

        private boolean isIn(final MutableBoundingBox b, BlockPos pos)
        {
            final int x1 = pos.getX();
            final int y1 = pos.getY();
            final int z1 = pos.getZ();
            for (int x = x1; x < x1 + TerrainSegment.GRIDSIZE; x++)
                for (int y = y1; y < y1 + TerrainSegment.GRIDSIZE; y++)
                    for (int z = z1; z < z1 + TerrainSegment.GRIDSIZE; z++)
                    {
                        pos = new BlockPos(x, y, z);
                        if (b.isInside(pos)) return true;
                    }
            return false;
        }

        @Override
        public int hashCode()
        {
            return this.hash;
        }

        @Override
        public boolean equals(final Object obj)
        {
            if (!(obj instanceof StructureInfo)) return false;
            return obj.toString().equals(this.toString());
        }

        @Override
        public String toString()
        {
            return this.key;
        }
    }

    /**
     * This is a cache of loaded chunks, it is used to prevent thread lock
     * contention when trying to look up a chunk, as it seems that
     * world.chunkExists returning true does not mean that you can just go and
     * ask for the chunk...
     */
    public static Map<GlobalChunkPos, Set<StructureInfo>> map_by_pos = new Object2ObjectOpenHashMap<>();

    private static Set<StructureInfo> getOrMake(final GlobalChunkPos pos)
    {
        final Set<StructureInfo> set = StructureManager.map_by_pos.getOrDefault(pos, Sets.newHashSet());
        if (!StructureManager.map_by_pos.containsKey(pos)) StructureManager.map_by_pos.put(pos, set);
        return set;
    }

    public static Set<StructureInfo> getFor(final RegistryKey<World> dim, final BlockPos loc)
    {
        final GlobalChunkPos pos = new GlobalChunkPos(dim, new ChunkPos(loc));
        final Set<StructureInfo> forPos = StructureManager.map_by_pos.getOrDefault(pos, Collections.emptySet());
        if (forPos.isEmpty()) return forPos;
        final Set<StructureInfo> matches = Sets.newHashSet();
        for (final StructureInfo i : forPos)
            if (i.isIn(loc)) matches.add(i);
        return matches;
    }

    @SubscribeEvent
    public static void onChunkLoad(final ChunkEvent.Load evt)
    {
        // The world is null when it is loaded off thread during worldgen!
        if (!(evt.getWorld() instanceof World) || evt.getWorld().isClientSide()) return;
        final World w = (World) evt.getWorld();
        final RegistryKey<World> dim = w.dimension();
        for (final Entry<Structure<?>, StructureStart<?>> entry : evt.getChunk().getAllStarts().entrySet())
        {
            final StructureInfo info = new StructureInfo(entry);
            final MutableBoundingBox b = info.start.getBoundingBox();
            for (int x = b.x0 >> 4; x <= b.x1 >> 4; x++)
                for (int z = b.z0 >> 4; z <= b.z1 >> 4; z++)
                {
                    final ChunkPos p = new ChunkPos(x, z);
                    final GlobalChunkPos pos = new GlobalChunkPos(dim, p);
                    final Set<StructureInfo> set = StructureManager.getOrMake(pos);
                    set.add(info);
                }
        }
    }

    @SubscribeEvent
    public static void onChunkUnload(final ChunkEvent.Unload evt)
    {
        if (!(evt.getWorld() instanceof World) || evt.getWorld().isClientSide()) return;
        final World w = (World) evt.getWorld();
        final RegistryKey<World> dim = w.dimension();
        final GlobalChunkPos pos = new GlobalChunkPos(dim, evt.getChunk().getPos());
        StructureManager.map_by_pos.remove(pos);
    }

    public static void clear()
    {
        StructureManager.map_by_pos.clear();
        ITerrainProvider.loadedChunks.clear();
        ITerrainProvider.pendingCache.clear();
    }
}