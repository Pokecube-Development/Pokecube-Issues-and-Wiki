package pokecube.gimmicks.builders.builders;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.levelgen.structure.PoolElementStructurePiece;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePiecesBuilder;
import net.minecraft.world.level.levelgen.structure.pools.SinglePoolElement;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraftforge.items.IItemHandlerModifiable;
import pokecube.world.gen.structures.pool_elements.ExpandedJigsawPiece;

public class JigsawBuilder
{
    public List<StructureBuilder> builders = new ArrayList<>();
    StructurePiecesBuilder pieceBuilder;

    public JigsawBuilder(StructurePiecesBuilder pieceBuilder, BlockPos shift, IItemHandlerModifiable itemSource,
            ServerLevel level)
    {
        this.pieceBuilder = pieceBuilder;
        pieceBuilder.build().pieces().forEach(piece -> {
            if (piece instanceof PoolElementStructurePiece pooled)
            {
                if (pooled.getElement() instanceof SinglePoolElement elem)
                {
                    BlockPos origin = pooled.getPosition().offset(shift);
                    var builder = new StructureBuilder(origin, pooled.getRotation(), pooled.getMirror(), itemSource,
                            () -> elem.getTemplate(level.getStructureManager()));

                    // Now make the settings object
                    StructurePlaceSettings settings = null;
                    if (elem instanceof ExpandedJigsawPiece jig)
                        settings = jig.getSettings(pooled.getRotation(), null, false);
                    builder.settings = settings;
                    builders.add(builder);
                }
            }
        });
    }
}
