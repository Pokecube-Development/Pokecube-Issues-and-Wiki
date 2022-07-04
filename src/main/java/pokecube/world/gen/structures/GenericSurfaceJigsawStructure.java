package pokecube.world.gen.structures;

import java.util.Optional;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.feature.StructureFeature;
import net.minecraft.world.level.levelgen.structure.PoolElementStructurePiece;
import net.minecraft.world.level.levelgen.structure.PostPlacementProcessor;
import net.minecraft.world.level.levelgen.structure.pieces.PieceGenerator;
import net.minecraft.world.level.levelgen.structure.pieces.PieceGeneratorSupplier;
import pokecube.core.PokecubeCore;
import pokecube.world.gen.structures.configs.ExpandedJigsawConfiguration;
import pokecube.world.gen.structures.utils.ExpandedJigsawPacement;

public class GenericSurfaceJigsawStructure extends StructureFeature<ExpandedJigsawConfiguration>
{
    public GenericSurfaceJigsawStructure()
    {
        // Create the pieces layout of the structure and give it to the game
        super(ExpandedJigsawConfiguration.CODEC, GenericSurfaceJigsawStructure::createPiecesGenerator,
                PostPlacementProcessor.NONE);
    }

    private static boolean isFeatureChunk(PieceGeneratorSupplier.Context<ExpandedJigsawConfiguration> context)
    {
        return true;
    }

    @Override
    public GenerationStep.Decoration step()
    {
        return GenerationStep.Decoration.SURFACE_STRUCTURES;
    }

    public static Optional<PieceGenerator<ExpandedJigsawConfiguration>> createPiecesGenerator(
            PieceGeneratorSupplier.Context<ExpandedJigsawConfiguration> context)
    {

        // Check if the spot is valid for our structure. This is just as another
        // method for cleanness.
        // Returning an empty optional tells the game to skip this spot as it
        // will not generate the structure.
        if (!GenericSurfaceJigsawStructure.isFeatureChunk(context))
        {
            return Optional.empty();
        }

        // Turns the chunk coordinates into actual coordinates we can use. (Gets
        // center of that chunk)
        BlockPos blockpos = context.chunkPos().getMiddleBlockPosition(0);

        Optional<PieceGenerator<ExpandedJigsawConfiguration>> structurePiecesGenerator = ExpandedJigsawPacement
                .addPieces(context, PoolElementStructurePiece::new, blockpos, false, true);

        /*
         * Note, you are always free to make your own JigsawPlacement class and
         * implementation of how the structure should generate. It is tricky but
         * extremely powerful if you are doing something that vanilla's jigsaw
         * system cannot do. Such as for example, forcing 3 pieces to always
         * spawn every time, limiting how often a piece spawns, or remove the
         * intersection limitation of pieces.
         *
         * An example of a custom JigsawPlacement.addPieces in action can be
         * found here (warning, it is using Mojmap mappings):
         * https://github.com/TelepathicGrunt/RepurposedStructures/blob/1.18.2/
         * src/main/java/com/telepathicgrunt/repurposedstructures/world/
         * structures/pieces/PieceLimitedJigsawManager.java
         */

        if (structurePiecesGenerator.isPresent())
        {
            // I use to debug and quickly find out if the structure is spawning
            // or not and where it is.
            // This is returning the coordinates of the center starting piece.
            PokecubeCore.LOGGER.info("Structure at {}", blockpos);
        }

        // Return the pieces generator that is now set up so that the game runs
        // it when it needs to create the layout of structure pieces.
        return structurePiecesGenerator;
    }
}
