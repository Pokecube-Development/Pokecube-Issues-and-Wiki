package thut.api.terrain;

import java.util.List;
import java.util.Map.Entry;

import com.google.common.collect.Lists;

import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.PoolElementStructurePiece;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import pokecube.world.gen.structures.pool_elements.ExpandedJigsawPiece;
import thut.api.terrain.StructureManager.StructureInfo;

public class NamedVolumeManager
{
    public static interface INamedPart
    {
        String getName();

        BoundingBox getBounds();
    }

    public static interface INamedStructure
    {
        String getName();

        List<INamedPart> getParts();

        BoundingBox getTotalBounds();

        default boolean isIn(final BlockPos pos, boolean forTerrain)
        {
            if (this.getParts().isEmpty()) return false;
            if (!this.getTotalBounds().isInside(pos)) return false;
            synchronized (this.getParts())
            {
                for (var p1 : this.getParts()) if (insideBox(p1.getBounds(), pos, forTerrain)) return true;
            }
            return false;
        }

        default boolean isNear(final BlockPos pos, final int distance, boolean forTerrain)
        {
            if (this.getParts().isEmpty()) return false;
            if (!inflate(this.getTotalBounds(), distance).isInside(pos)) return false;
            synchronized (this.getParts())
            {
                for (var p1 : this.getParts())
                    if (insideBox(inflate(p1.getBounds(), distance), pos, forTerrain)) return true;
            }
            return false;
        }
    }

    private static BoundingBox inflate(final BoundingBox other, final int amt)
    {
        return new BoundingBox(other.minX(), other.minY(), other.minZ(), other.maxX(), other.maxY(), other.maxZ())
                .inflatedBy(amt);
    }

    private static boolean insideBox(final BoundingBox b, BlockPos pos, boolean forTerrain)
    {
        if (!forTerrain) return b.isInside(pos);
        final int x1 = pos.getX();
        final int y1 = pos.getY();
        final int z1 = pos.getZ();
        MutableBlockPos mpos = new MutableBlockPos();
        int s = TerrainSegment.GRIDSIZE;
        int sy = TerrainSegment.YSHIFT;
        int sz = TerrainSegment.ZSHIFT;
        for (int i = 0; i < TerrainSegment.TOTAL; i++)
        {
            int x = x1 + i & s;
            int y = y1 + (i / sy) & s;
            int z = z1 + (i / sz) & s;
            mpos.set(x, y, z);
            if (b.isInside(mpos)) return true;
        }
        return false;
    }

    public static class NamedStructureWrapper implements INamedStructure
    {
        List<INamedPart> parts = Lists.newArrayList();
        final String name;
        public Structure feature;
        public StructureStart start;
        final ServerLevel level;

        private int hash = -1;
        private String key;

        public NamedStructureWrapper(ServerLevel level, String name, Entry<Structure, StructureStart> entry)
        {
            this.feature = entry.getKey();
            this.name = name;
            this.start = entry.getValue();
            this.level = level;
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
            if (this.start.getPieces().isEmpty()) return this.getName();
            if (this.key == null) this.key = this.getName() + " " + this.getTotalBounds();
            this.hash = this.key.hashCode();
            return this.key;
        }

        @Override
        public String getName()
        {
            return name;
        }

        @Override
        public BoundingBox getTotalBounds()
        {
            return start.getBoundingBox();
        }

        @Override
        public List<INamedPart> getParts()
        {
            if (parts.isEmpty())
                start.getPieces().forEach(piece -> this.parts.add(new StructurePiecePart(piece, level)));
            return parts;
        }
    }

    public static class StructurePiecePart implements INamedPart
    {
        final StructurePiece part;
        final String name;

        public StructurePiecePart(StructurePiece part, ServerLevel source)
        {
            this.part = part;
            if (source != null && part instanceof PoolElementStructurePiece p
                    && p.getElement() instanceof ExpandedJigsawPiece exp)
            {
                this.name = exp.name;
            }
            else this.name = "unk_part";
        }

        @Override
        public String getName()
        {
            return name;
        }

        @Override
        public BoundingBox getBounds()
        {
            return part.getBoundingBox();
        }

    }
}
