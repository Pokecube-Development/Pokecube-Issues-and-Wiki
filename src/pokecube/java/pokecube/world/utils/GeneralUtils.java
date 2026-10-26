package pokecube.world.utils;

import it.unimi.dsi.fastutil.longs.LongSet;
import net.minecraft.core.*;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureStart;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.function.Predicate;

///  This is from
/// [...](https://github.com/TelepathicGrunt/RepurposedStructures/blob/1.21-Arch/common/src/main/java/com/telepathicgrunt/repurposedstructures/utils/GeneralUtils.java)

public final class GeneralUtils
{
    private GeneralUtils() {}

    /// ////////////////////////////////////////

    public static List<StructureStart> inboundsValidStartsForAllStructure(WorldGenRegion level, BlockPos position,
            Predicate<Structure> structureMatch)
    {
        StructureManager structureManager = level.getLevel().structureManager();
        SectionPos sectionPos = SectionPos.of(position);

        ChunkAccess chunkAccess = level.getChunk(sectionPos.x(), sectionPos.z(), ChunkStatus.STRUCTURE_REFERENCES);
        if (!chunkAccess.getHighestGeneratedStatus().isOrAfter(ChunkStatus.STRUCTURE_REFERENCES))
        {
            return new ArrayList<>();
        }

        Map<Structure, LongSet> references = chunkAccess.getAllReferences();
        List<StructureStart> list = new ArrayList<>();
        for (Map.Entry<Structure, LongSet> entry : references.entrySet())
        {
            if (structureMatch.test(entry.getKey()))
            {
                fillStartsForStructure(level, structureManager, entry.getKey(), entry.getValue(), position, list::add);
            }
        }
        return list;
    }

    public static void fillStartsForStructure(LevelReader level, StructureManager structureManager, Structure structure,
            LongSet references, BlockPos position, Consumer<StructureStart> consumer)
    {
        for (long ref : references)
        {
            SectionPos sectionPos = SectionPos.of(new ChunkPos(ref), level.getMinSection());
            if (!level.hasChunk(sectionPos.x(), sectionPos.z()))
            {
                continue;
            }
            StructureStart structureStart = structureManager.getStartForStructure(sectionPos, structure,
                    level.getChunk(sectionPos.x(), sectionPos.z(), ChunkStatus.STRUCTURE_STARTS));
            if (structureStart != null && structureStart.isValid() && structureStart.getBoundingBox()
                    .isInside(position))
            {
                consumer.accept(structureStart);
            }
        }
    }

    /// ///////////////////////////////////////////////////////////////////////////////

    private static final ConcurrentHashMap<HeightKey, Integer> CACHED_HEIGHT = new ConcurrentHashMap<>(2048);

    private record HeightKey(ChunkGenerator chunkGenerator, int x, int z) {}

    public static int getCachedFreeHeight(ChunkGenerator chunkGenerator, int x, int z, Heightmap.Types types,
            LevelHeightAccessor levelHeightAccessor, RandomState randomState)
    {
        HeightKey key = new HeightKey(chunkGenerator, x, z);
        Integer y = CACHED_HEIGHT.get(key);

        if (y == null)
        {
            if (CACHED_HEIGHT.size() >= 2048)
            {
                CACHED_HEIGHT.clear();
            }
            y = chunkGenerator.getFirstFreeHeight(x, z, types, levelHeightAccessor, randomState);
            CACHED_HEIGHT.put(key, y);
        }

        return y;
    }
}