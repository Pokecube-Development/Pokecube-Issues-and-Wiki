package pokecube.world.gen.structures.utils;

import java.util.List;
import java.util.function.BiConsumer;

import net.minecraft.world.level.levelgen.structure.PoolElementStructurePiece;
import net.minecraft.world.level.levelgen.structure.pieces.PieceGenerator;
import net.minecraft.world.level.levelgen.structure.pieces.PieceGenerator.Context;
import pokecube.world.gen.structures.configs.ExpandedJigsawConfiguration;
import pokecube.world.gen.structures.pool_elements.ExpandedJigsawPiece;

public class PostProcessor
        implements BiConsumer<PieceGenerator.Context<ExpandedJigsawConfiguration>, List<PoolElementStructurePiece>>
{
    public static BiConsumer<PieceGenerator.Context<ExpandedJigsawConfiguration>, List<PoolElementStructurePiece>> POSTPROCESS = new PostProcessor();

    @Override
    public void accept(Context<ExpandedJigsawConfiguration> context, List<PoolElementStructurePiece> parts)
    {

        for (final PoolElementStructurePiece part : parts)
            if (part.getElement() instanceof final ExpandedJigsawPiece piece)
        {
            final int dy = piece.y_offset;
            // Check if the part needs a shift.
            part.move(0, -dy, 0);
        }
    }
}