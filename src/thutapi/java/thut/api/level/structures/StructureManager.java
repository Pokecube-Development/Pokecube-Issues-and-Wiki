package thut.api.level.structures;

import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.phys.AABB;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.level.ChunkEvent;
import thut.api.level.structures.NamedVolumes.INamedVolume;
import thut.api.level.terrain.GlobalChunkPos;
import thut.api.util.RegHelper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

public class StructureManager
{
    private static final ReentrantLock SET_ADD_LOCK = new ReentrantLock();
    /**
     * This is a cache of loaded chunks, it is used to prevent thread lock contention when trying to look up a chunk, as
     * it seems that world.chunkExists returning true does not mean that you can just go and ask for the chunk...
     */
    private static final Map<GlobalChunkPos, Map<GlobalChunkPos, Set<INamedVolume>>> map_by_rpos = new ConcurrentHashMap<>();

    public static List<INamedVolume> getColliding(ResourceKey<Level> dim, INamedVolume volume)
    {
        return getColliding(dim, volume.getTotalBounds());
    }

    public static List<INamedVolume> getColliding(ResourceKey<Level> dim, BoundingBox ourB)
    {
        var here = forVolume(ourB, dim);
        var opts = new HashSet<INamedVolume>();
        AABB aabbUs = AABB.of(ourB);
        here.forEach(p -> opts.addAll(getFor(dim, p.pos)));
        var ret = opts.stream().filter(b -> {
            var otherB = b.getTotalBounds();
            AABB otherBB = AABB.of(otherB);
            if (!aabbUs.intersects(otherBB)) return false;
            var aabbI = aabbUs.intersect(otherBB);
            return NamedVolumes.computeVolume(aabbI) != 0;
        });
        return new ArrayList<>(ret.toList());
    }

    public static List<INamedVolume> getFor(Level dim, final BlockPos loc)
    {
        return getFor(dim.dimension(), loc);
    }

    public static List<INamedVolume> getFor(Level dim, final BlockPos loc, boolean forTerrain)
    {
        final GlobalChunkPos pos = new GlobalChunkPos(dim.dimension(), new ChunkPos(loc));
        var rPos = new GlobalChunkPos(pos.world, new ChunkPos(pos.pos.getRegionX(), pos.pos.getRegionZ()));
        var rMap = StructureManager.map_by_rpos.get(rPos);
        if ((rMap == null || !rMap.containsKey(pos)) && dim instanceof ServerLevel level)
        {
            if (level.isAreaLoaded(loc, 32))
            {
                var chunk = level.getChunkAt(loc);
                var reg = level.registryAccess().registryOrThrow(RegHelper.STRUCTURE_REGISTRY);
                var starts = level.structureManager().startsForStructure(chunk.getPos(), s -> true);
                starts.forEach(start -> {
                    var structure = start.getStructure();
                    var name = reg.getKey(structure).toString();
                    final NamedVolumes.NamedStructureWrapper info = new NamedVolumes.NamedStructureWrapper(level, name,
                            structure, start);
                    if (!info.start.isValid()) return;
                    addVolume(info, level);
                });
            }
            else
            {
                return Collections.emptyList();
            }
        }
        return getFor(pos.world, loc, forTerrain);
    }

    public static List<INamedVolume> getFor(ResourceKey<Level>  dim, final BlockPos loc)
    {
        return getFor(dim, loc, false);
    }

    public static List<INamedVolume> getFor(final ResourceKey<Level> dim, final BlockPos loc, boolean forTerrain)
    {
        List<INamedVolume> forPos = getFor(dim, new ChunkPos(loc));
        forPos.removeIf(v -> v == null || (forTerrain && !v.affectsMobSpawning()) || !v.isIn(loc));
        return forPos;
    }

    public static List<INamedVolume> getFor(final ResourceKey<Level> dim, final ChunkPos loc)
    {
        final GlobalChunkPos pos = new GlobalChunkPos(dim, loc);
        var rPos = new GlobalChunkPos(pos.world, new ChunkPos(pos.pos.getRegionX(), pos.pos.getRegionZ()));
        final Set<INamedVolume> forPos = StructureManager.map_by_rpos.getOrDefault(rPos, Collections.emptyMap())
                .getOrDefault(pos, Collections.emptySet());
        List<INamedVolume> list;
        SET_ADD_LOCK.lock();
        list = new ArrayList<>(forPos);
        SET_ADD_LOCK.unlock();
        return list;
    }

    public static List<GlobalChunkPos> forVolume(INamedVolume volume, ResourceKey<Level> level)
    {
        List<GlobalChunkPos> list = new ArrayList<>();
        var bounds = volume.getTotalBounds();
        bounds.intersectingChunks().forEach(pos -> {
            list.add(new GlobalChunkPos(level, pos));
        });
        return list;
    }

    public static List<GlobalChunkPos> forVolume(BoundingBox bounds, ResourceKey<Level> level)
    {
        List<GlobalChunkPos> list = new ArrayList<>();
        bounds.intersectingChunks().forEach(pos -> {
            list.add(new GlobalChunkPos(level, pos));
        });
        return list;
    }

    public static void addVolume(INamedVolume volume, Level level)
    {
        List<GlobalChunkPos> list = StructureManager.forVolume(volume, level.dimension());
        list.forEach(pos -> {
            var rPos = new GlobalChunkPos(pos.world, new ChunkPos(pos.pos.getRegionX(), pos.pos.getRegionZ()));
            var map = map_by_rpos.computeIfAbsent(rPos, k -> new ConcurrentHashMap<>());
            var set = map.computeIfAbsent(pos, k -> new HashSet<>());
            SET_ADD_LOCK.lock();
            set.add(volume);
            SET_ADD_LOCK.unlock();
        });
    }

    public static void removeVolume(INamedVolume volume, Level level)
    {
        List<GlobalChunkPos> list = forVolume(volume, level.dimension());
        list.forEach(pos-> {
            var rPos = new GlobalChunkPos(pos.world, new ChunkPos(pos.pos.getRegionX(), pos.pos.getRegionZ()));
            if (map_by_rpos.containsKey(rPos))
            {
                var map = map_by_rpos.get(rPos);
                if(map.containsKey(pos))
                {
                    var set = map.get(pos);
                    SET_ADD_LOCK.lock();
                    set.remove(volume);
                    if (set.isEmpty()) map.remove(pos);
                    SET_ADD_LOCK.unlock();
                }
                if(map.isEmpty()) map_by_rpos.remove(rPos);
            }
        });
    }

    public static List<INamedVolume> getNear(final ResourceKey<Level> dim, final BlockPos loc, final int distance,
            boolean forSubbiome)
    {
        final Set<INamedVolume> matches = new HashSet<>();
        final ChunkPos origin = new ChunkPos(loc);
        int dr = SectionPos.blockToSectionCoord(distance);
        dr = Math.max(dr, 1);
        for (int x = origin.x - dr; x <= origin.x + dr; x++)
            for (int z = origin.z - dr; z <= origin.z + dr; z++)
            {
                ChunkPos pos = new ChunkPos(x, z);
                var forChunk = getFor(dim, pos);
                forChunk.removeIf(b -> (forSubbiome && !b.affectsMobSpawning()) || !(b.isNear(loc, distance)));
                matches.addAll(forChunk);
            }
        return new ArrayList<>(matches);
    }

    public static boolean hasVolumes(ResourceKey<Level> dimension, int regionX, int regionZ)
    {
        final GlobalChunkPos gpos = new GlobalChunkPos(dimension, new ChunkPos(regionX, regionZ));
        return map_by_rpos.containsKey(gpos);
    }

    @SubscribeEvent
    public static void onChunkUnload(final ChunkEvent.Unload evt)
    {
        if (!(evt.getLevel() instanceof Level level) || level.isClientSide()) return;
        final ResourceKey<Level> dim = level.dimension();
        final GlobalChunkPos pos = new GlobalChunkPos(dim, evt.getChunk().getPos());
        var rPos = new GlobalChunkPos(pos.world, new ChunkPos(pos.pos.getRegionX(), pos.pos.getRegionZ()));
        map_by_rpos.getOrDefault(rPos, new HashMap<>()).remove(pos);
    }

    public static void clear()
    {
        map_by_rpos.clear();
    }
}