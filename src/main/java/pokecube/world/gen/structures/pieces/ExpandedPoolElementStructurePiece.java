package pokecube.world.gen.structures.pieces;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.PoolElementStructurePiece;
import net.minecraft.world.level.levelgen.structure.pools.StructurePoolElement;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;

public class ExpandedPoolElementStructurePiece extends PoolElementStructurePiece
{
    public ExpandedPoolElementStructurePiece(StructureTemplateManager manager, StructurePoolElement element, BlockPos pos,
            int y_offset, Rotation rotation, BoundingBox bounds)
    {
        super(manager, element, pos, y_offset, rotation, bounds);
    }

}
