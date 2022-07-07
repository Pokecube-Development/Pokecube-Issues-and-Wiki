package pokecube.world.gen.structures.pieces;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.levelgen.feature.NoiseEffect;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.PoolElementStructurePiece;
import net.minecraft.world.level.levelgen.structure.pools.StructurePoolElement;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureManager;
import pokecube.world.gen.structures.pool_elements.ExpandedJigsawPiece;

public class ExpandedPoolElementStructurePiece extends PoolElementStructurePiece
{
    private final NoiseEffect effect;

    public ExpandedPoolElementStructurePiece(StructureManager manager, StructurePoolElement element, BlockPos pos,
            int y_offset, Rotation rotation, BoundingBox bounds)
    {
        super(manager, element, pos, y_offset, rotation, bounds);
        if (element instanceof ExpandedJigsawPiece p)
        {
            effect = p.no_affect_noise ? NoiseEffect.NONE : NoiseEffect.BEARD;
        }
        else
        {
            effect = super.getNoiseEffect();
        }
    }

    @Override
    public NoiseEffect getNoiseEffect()
    {
        return effect;
    }

}
