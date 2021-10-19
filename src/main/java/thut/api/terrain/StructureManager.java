package thut.api.terrain;

import java.util.Collections;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.feature.StructureFeature;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraftforge.event.world.ChunkEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import thut.core.common.ThutCore;

public class StructureManager
{
    public static class StructureInfo
    {
        public String            name;
        public StructureStart<?> start;

        private int    hash = -1;
        private String key;

        public StructureInfo()
        {
        }

        public StructureInfo(final Entry<StructureFeature<?>, StructureStart<?>> entry)
        {
            this.name = entry.getKey().getFeatureName();
            this.start = entry.getValue();
            if (this.name == null)
            {
                this.name = "unk?";
                ThutCore.LOGGER.warn("Warning, null name for start: {}", this.start);
            }
        }

        private BoundingBox inflate(final BoundingBox other, final int amt)
        {
            return new BoundingBox(other.minX(), other.minY(), other.minZ(), other.maxX(), other.maxY(), other.maxZ())
                    .inflate(amt);
        }

        public boolean isNear(final BlockPos pos, final int distance)
        {
            if (this.start.getPieces().isEmpty()) return false;
            if (!this.inflate(this.start.getBoundingBox(), distance).isInside(pos)) return false;
            synchronized (this.start.getPieces())
            {
                for (final StructurePiece p1 : this.start.getPieces())
                    if (this.isIn(this.inflate(p1.getBoundingBox(), distance), pos)) return true;
            }
            return false;
        }

        public boolean isIn(final BlockPos pos)
        {
            if (this.start.getPieces().isEmpty()) return false;
            if (!this.start.getBoundingBox().isInside(pos)) return false;
            synchronized (this.start.getPieces())
            {
                for (final StructurePiece p1 : this.start.getPieces())
                    if (this.isIn(p1.getBoundingBox(), pos)) return true;
            }
            return false;
        }

        private boolean isIn(final BoundingBox b, BlockPos pos)
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
            if (this.hash == -1) this.toString();
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
            if (this.start.getPieces().isEmpty()) return this.name;
            if (this.key == null) this.key = this.name + " " + this.start.getBoundingBox();
            this.hash = this.key.hashCode();
            return this.key;
        }
    }

    /**
     * This is a cache of loaded chunks, it is used to prevent thread lock
     * contention when trying to look up a chunk, as it seems that
     * world.chunkExists returning true does not mean that you can just go and
     * ask for the chunk...
     */
    public static Map<GlobalChunkPos, Set<StructureInfo>> map_by_pos = Maps.newHashMap();

    private static Set<StructureInfo> getOrMake(final GlobalChunkPos pos)
    {
        Set<StructureInfo> set = StructureManager.map_by_pos.get(pos);
        if (set == null) StructureManager.map_by_pos.put(pos, set = Sets.newHashSet());
        return set;
    }

    public static Set<StructureInfo> getFor(final ResourceKey<Level> dim, final BlockPos loc)
    {
        final GlobalChunkPos pos = new GlobalChunkPos(dim, new ChunkPos(loc));
        final Set<StructureInfo> forPos = StructureManager.map_by_pos.getOrDefault(pos, Collections.emptySet());
        if (forPos.isEmpty()) return forPos;
        final Set<StructureInfo> matches = Sets.newHashSet();
        for (final StructureInfo i : forPos)
            if (i.isIn(loc)) matches.add(i);
        return matches;
    }

    private static Set<StructureInfo> getNearInt(final ResourceKey<Level> dim, final BlockPos loc, final ChunkPos pos,
            final int distance)
    {
        final GlobalChunkPos gpos = new GlobalChunkPos(dim, pos);
        final Set<StructureInfo> forPos = StructureManager.map_by_pos.getOrDefault(gpos, Collections.emptySet());
        if (forPos.isEmpty()) return forPos;
        final Set<StructureInfo> matches = Sets.newHashSet();
        for (final StructureInfo i : forPos)
            if (i.isNear(loc, distance)) matches.add(i);
        return matches;
    }

    public static Set<StructureInfo> getNear(final ResourceKey<Level> dim, final BlockPos loc, final int distance)
    {
        final Set<StructureInfo> matches = Sets.newHashSet();
        final ChunkPos origin = new ChunkPos(loc);
        int dr = SectionPos.blockToSectionCoord(distance);
        dr = Math.max(dr, 1);
        for (int x = origin.x - dr; x <= origin.x + dr; x++)
            for (int z = origin.z - dr; z <= origin.z + dr; z++)
                matches.addAll(StructureManager.getNearInt(dim, loc, new ChunkPos(x, z), distance));
        return matches;
    }

    @SubscribeEvent
    public static void onChunkLoad(final ChunkEvent.Load evt)
    {
        // The world is null when it is loaded off thread during worldgen!
        if (!(evt.getWorld() instanceof Level) || evt.getWorld().isClientSide()) return;
        final Level w = (Level) evt.getWorld();
        final ResourceKey<Level> dim = w.dimension();
        for (final Entry<StructureFeature<?>, StructureStart<?>> entry : evt.getChunk().getAllStarts().entrySet())
        {
            final StructureInfo info = new StructureInfo(entry);
            if (!info.start.isValid()) continue;

            final BoundingBox b = info.start.getBoundingBox();
            if (b.getXSpan() > 2560 || b.getZSpan() > 2560)
            {
                ThutCore.LOGGER.warn("Warning, too big box for {}: {}", info.name, b);
                continue;
            }

            for (int x = b.minX >> 4; x <= b.maxX >> 4; x++)
                for (int z = b.minZ >> 4; z <= b.maxZ >> 4; z++)
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
        if (!(evt.getWorld() instanceof Level) || evt.getWorld().isClientSide()) return;
        final Level w = (Level) evt.getWorld();
        final ResourceKey<Level> dim = w.dimension();
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