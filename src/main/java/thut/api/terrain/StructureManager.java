package thut.api.terrain;

import java.util.Collections;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.SectionPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.feature.ConfiguredStructureFeature;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraftforge.event.world.ChunkEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import thut.api.terrain.NamedVolumes.INamedStructure;
import thut.api.terrain.NamedVolumes.NamedStructureWrapper;
import thut.core.common.ThutCore;

public class StructureManager
{
    /**
     * This is a cache of loaded chunks, it is used to prevent thread lock
     * contention when trying to look up a chunk, as it seems that
     * world.chunkExists returning true does not mean that you can just go and
     * ask for the chunk...
     */
    public static Map<GlobalChunkPos, Set<INamedStructure>> map_by_pos = Maps.newHashMap();

    public static Set<INamedStructure> getOrMake(final GlobalChunkPos pos)
    {
        Set<INamedStructure> set = StructureManager.map_by_pos.get(pos);
        if (set == null) StructureManager.map_by_pos.put(pos, set = Sets.newHashSet());
        return set;
    }

    public static Set<INamedStructure> getFor(final ResourceKey<Level> dim, final BlockPos loc, boolean forSubbiome)
    {
        final GlobalChunkPos pos = new GlobalChunkPos(dim, new ChunkPos(loc));
        final Set<INamedStructure> forPos = StructureManager.map_by_pos.getOrDefault(pos, Collections.emptySet());
        if (forPos.isEmpty()) return forPos;
        final Set<INamedStructure> matches = Sets.newHashSet();
        for (final INamedStructure i : forPos) if (i.isIn(loc, forSubbiome)) matches.add(i);
        return matches;
    }

    private static Set<INamedStructure> getNearInt(final ResourceKey<Level> dim, final BlockPos loc, final ChunkPos pos,
            final int distance, boolean forSubbiome)
    {
        final GlobalChunkPos gpos = new GlobalChunkPos(dim, pos);
        final Set<INamedStructure> forPos = StructureManager.map_by_pos.getOrDefault(gpos, Collections.emptySet());
        if (forPos.isEmpty()) return forPos;
        final Set<INamedStructure> matches = Sets.newHashSet();
        for (final INamedStructure i : forPos) if (i.isNear(loc, distance, forSubbiome)) matches.add(i);
        return matches;
    }

    public static Set<INamedStructure> getNear(final ResourceKey<Level> dim, final BlockPos loc, final int distance,
            boolean forSubbiome)
    {
        final Set<INamedStructure> matches = Sets.newHashSet();
        final ChunkPos origin = new ChunkPos(loc);
        int dr = SectionPos.blockToSectionCoord(distance);
        dr = Math.max(dr, 1);
        for (int x = origin.x - dr; x <= origin.x + dr; x++) for (int z = origin.z - dr; z <= origin.z + dr; z++)
            matches.addAll(StructureManager.getNearInt(dim, loc, new ChunkPos(x, z), distance, forSubbiome));
        return matches;
    }

    @SubscribeEvent
    public static void onChunkLoad(final ChunkEvent.Load evt)
    {
        // The world is null when it is loaded off thread during worldgen!
        if (!(evt.getWorld() instanceof ServerLevel w) || evt.getWorld().isClientSide()) return;
        final ResourceKey<Level> dim = w.dimension();
        var reg = w.registryAccess().registryOrThrow(Registry.CONFIGURED_STRUCTURE_FEATURE_REGISTRY);
        for (final Entry<ConfiguredStructureFeature<?, ?>, StructureStart> entry : evt.getChunk().getAllStarts()
                .entrySet())
        {
            String name = reg.getKey(entry.getKey()).toString();
            final NamedStructureWrapper info = new NamedStructureWrapper(w, name, entry);
            if (!info.start.isValid()) continue;

            final BoundingBox b = info.start.getBoundingBox();
            if (b.getXSpan() > 2560 || b.getZSpan() > 2560)
            {
                ThutCore.LOGGER.warn("Warning, too big box for {}: {}", info.getName(), b);
                continue;
            }

            for (int x = b.minX >> 4; x <= b.maxX >> 4; x++) for (int z = b.minZ >> 4; z <= b.maxZ >> 4; z++)
            {
                final ChunkPos p = new ChunkPos(x, z);
                final GlobalChunkPos pos = new GlobalChunkPos(dim, p);
                final Set<INamedStructure> set = StructureManager.getOrMake(pos);
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