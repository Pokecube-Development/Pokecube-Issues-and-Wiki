package pokecube.api.blocks;

import net.minecraft.resources.ResourceLocation;
import thut.api.level.structures.NamedVolumes;

import java.util.Collections;
import java.util.List;

public interface IRepelledVolume extends NamedVolumes.INamedVolume
{
    public static class ForbidReason
    {
        public static final ForbidReason NONE, REPEL, NEST;

        static
        {
            NONE = new ForbidReason("pokecube:none");
            REPEL = new ForbidReason("pokecube:repel");
            NEST = new ForbidReason("pokecube:nest");
        }

        public final ResourceLocation name;

        public ForbidReason(final String name)
        {
            this.name = ResourceLocation.parse(name);
        }

        @Override
        public String toString()
        {
            return this.name.toString();
        }

        @Override
        public int hashCode()
        {
            return this.name.hashCode();
        }

        @Override
        public boolean equals(final Object obj)
        {
            if (obj instanceof ForbidReason fob) return fob.name.equals(this.name);
            return false;
        }
    }

    ForbidReason getReason();

    @Override
    default List<NamedVolumes.INamedPart> getParts()
    {
        return Collections.emptyList();
    }

    @Override
    default String getName()
    {
        return getReason().name.toString();
    }

    @Override
    default Object getWrapped()
    {
        return getReason();
    }

    @Override
    default String getKey()
    {
        return "pokecube:repelled_volume";
    }
}
