package pokecube.world.gen.structures.utils;

import java.util.Random;

import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureFeatureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.PostPlacementProcessor;
import net.minecraft.world.level.levelgen.structure.pieces.PiecesContainer;

public class ExpandedPostPlacementProcessor implements PostPlacementProcessor
{
    public static final PostPlacementProcessor INSTANCE = new ExpandedPostPlacementProcessor();

    @Override
    public void afterPlace(WorldGenLevel level, StructureFeatureManager manager, ChunkGenerator chunkGenerator,
            Random random, BoundingBox box, ChunkPos pos, PiecesContainer pieces)
    {
        
    }
}
