package thut.api.level.structures;

import java.util.List;
import java.util.Map.Entry;

import com.google.common.collect.Lists;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.levelgen.feature.ConfiguredStructureFeature;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.PoolElementStructurePiece;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import pokecube.world.gen.structures.pool_elements.ExpandedJigsawPiece;

public class NamedVolumes
{
    public static interface INamedPart
    {
        String getName();

        BoundingBox getBounds();

        default boolean is(String name)
        {
            return name.equals(this.getName());
        }

        default Object getWrapped()
        {
            return null;
        }
    }

    public static interface INamedStructure
    {
        String getName();

        default boolean is(String name)
        {
            return name.equals(this.getName());
        }

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

        default Object getWrapped()
        {
            return null;
        }
    }

    private static BoundingBox inflate(final BoundingBox other, final int amt)
    {
        return new BoundingBox(other.minX(), other.minY(), other.minZ(), other.maxX(), other.maxY(), other.maxZ())
                .inflatedBy(amt);
    }

    private static boolean insideBox(final BoundingBox b, BlockPos pos, boolean forTerrain)
    {
        // TODO decide if we want to do something special for terrain checks?
        return b.isInside(pos);
    }

    public static class NamedStructureWrapper implements INamedStructure
    {
        List<INamedPart> parts = Lists.newArrayList();
        final String name;
        public ConfiguredStructureFeature<?, ?> feature;
        public StructureStart start;
        final ServerLevel level;

        private int hash = -1;
        private String key;

        public NamedStructureWrapper(ServerLevel level, String name,
                Entry<ConfiguredStructureFeature<?, ?>, StructureStart> entry)
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
            if (!(obj instanceof INamedStructure)) return false;
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
        public boolean is(String name)
        {
            if (INamedStructure.super.is(name)) return true;
            var key = Registry.CONFIGURED_STRUCTURE_FEATURE_REGISTRY;
            var tag = TagKey.create(key, new ResourceLocation(name));
            var registry = level.registryAccess().registryOrThrow(key);
            var opt_holder = registry.getHolder(registry.getId(this.feature));
            return opt_holder.get().is(tag);
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

        @Override
        public Object getWrapped()
        {
            return feature;
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

        @Override
        public Object getWrapped()
        {
            return part;
        }

    }
}
