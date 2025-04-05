package thut.api.level.structures;

import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;

import com.google.common.collect.Lists;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.attachment.IAttachmentHolder;
import net.neoforged.neoforge.common.util.INBTSerializable;
import net.neoforged.neoforge.registries.DeferredRegister;
import thut.api.level.structures.NamedVolumes.INamedPart;
import thut.api.level.structures.NamedVolumes.INamedStructure;

public class CapabilityWorldStructures implements INBTSerializable<CompoundTag>
{
    public static class Building implements INamedPart, INBTSerializable<CompoundTag>
    {
        String name;
        BoundingBox bounds;

        public Building(HolderLookup.Provider registries, CompoundTag tag)
        {
            this.deserializeNBT(registries, tag);
        }

        public Building(String name, BoundingBox box)
        {
            this.name = name;
            this.bounds = box;
        }

        @Override
        public String getName()
        {
            return name;
        }

        @Override
        public BoundingBox getBounds()
        {
            return bounds;
        }

        @Override
        public CompoundTag serializeNBT(HolderLookup.Provider registries)
        {
            CompoundTag tag = new CompoundTag();
            tag.putString("name", name);
            tag.put("bounds", BoundingBox.CODEC.encodeStart(NbtOps.INSTANCE, this.bounds).getOrThrow());
            return tag;
        }

        @Override
        public void deserializeNBT(HolderLookup.Provider registries, CompoundTag nbt)
        {
            this.name = nbt.getString("name");
            bounds = BoundingBox.CODEC.decode(NbtOps.INSTANCE, nbt.get("bounds")).result().get().getFirst();
        }
    }

    public static class Structure implements INamedStructure, INBTSerializable<CompoundTag>
    {
        String name;
        BoundingBox bounds;
        List<INamedPart> buildings = Lists.newArrayList();

        private int hash = -1;
        private String key;

        public Structure(HolderLookup.Provider registries, CompoundTag tag)
        {
            this.deserializeNBT(registries, tag);
        }

        public Structure(String name, BoundingBox box)
        {
            this.name = name;
            this.bounds = box;
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
            this.key = this.getName() + " " + this.getTotalBounds();
            this.hash = this.key.hashCode();
            return this.key;
        }

        @SuppressWarnings("deprecation")
        public void addBuilding(Building b)
        {
            this.bounds = this.bounds.encapsulate(b.getBounds());
            if (!this.buildings.contains(b)) this.buildings.add(b);
        }

        @Override
        public String getName()
        {
            return name;
        }

        @Override
        public BoundingBox getTotalBounds()
        {
            return bounds;
        }

        @Override
        public List<INamedPart> getParts()
        {
            return buildings;
        }

        @Override
        public CompoundTag serializeNBT(HolderLookup.Provider registries)
        {
            CompoundTag tag = new CompoundTag();
            tag.putString("name", name);
            tag.put("bounds", BoundingBox.CODEC.encodeStart(NbtOps.INSTANCE, this.bounds).getOrThrow());
            ListTag list = new ListTag();
            this.buildings.forEach(b -> {
                if (b instanceof Building building)
                {
                    list.add(building.serializeNBT(registries));
                }
            });
            tag.put("buildings", list);
            return tag;
        }

        @Override
        public void deserializeNBT(HolderLookup.Provider registries, CompoundTag nbt)
        {
            this.name = nbt.getString("name");
            bounds = BoundingBox.CODEC.decode(NbtOps.INSTANCE, nbt.get("bounds")).result().get().getFirst();
            ListTag list = nbt.getList("buildings", Tag.TAG_COMPOUND);
            this.buildings.clear();
            list.forEach(tag -> {
                if (tag instanceof CompoundTag comp)
                {
                    buildings.add(new Building(registries, comp));
                }
            });
        }
    }

    private final List<Structure> structures = Lists.newArrayList();
    private final ServerLevel level;

    public CapabilityWorldStructures(ServerLevel level)
    {
        this.level = level;
    }

    public void addStructure(Structure s)
    {
        if (!this.structures.contains(s)) this.structures.add(s);
        StructureManager.addStructure(level.dimension(), s);
    }

    public void addBuilding(String structure, String building, BoundingBox bounds)
    {
        if (building == null) building = "unk_part";
        Building b = new Building(building, bounds);
        Set<INamedStructure> intersects = StructureManager.getColliding(level.dimension(), bounds);
        Structure s = null;
        if (!intersects.isEmpty())
        {
            for (var s2 : intersects) if (s2 instanceof Structure s1 && s2.getName().equals(structure))
            {
                s = s1;
                break;
            }
        }
        if (s == null) s = new Structure(structure, bounds);
        s.addBuilding(b);
        addStructure(s);
    }

    @Override
    public CompoundTag serializeNBT(HolderLookup.Provider registries)
    {
        CompoundTag tag = new CompoundTag();
        ListTag list = new ListTag();
        this.structures.forEach(b -> list.add(b.serializeNBT(registries)));
        tag.put("structures", list);
        return tag;
    }

    @Override
    public void deserializeNBT(HolderLookup.Provider registries, CompoundTag nbt)
    {
        ListTag list = nbt.getList("structures", Tag.TAG_COMPOUND);
        this.structures.clear();
        list.forEach(tag -> {
            if (tag instanceof CompoundTag comp) this.addStructure(new Structure(registries, comp));
        });
    }

    public static CapabilityWorldStructures makeProvider(final IAttachmentHolder in)
    {
        if (!(in instanceof ServerLevel level)) return null;
        return new CapabilityWorldStructures(level);
    }

    public static CapabilityWorldStructures get(final IAttachmentHolder in)
    {
        return in.getData(TYPE_SAVE.get());
    }

    public static final ResourceLocation LOCSAVEABLE = ResourceLocation.parse("thutcore:world_structures");

    public static Supplier<AttachmentType<CapabilityWorldStructures>> TYPE_SAVE;

    public static void registerAttachment(DeferredRegister<AttachmentType<?>> registry)
    {
        Function<IAttachmentHolder, CapabilityWorldStructures> func_a = CapabilityWorldStructures::makeProvider;
        var attach_a = AttachmentType.serializable(func_a).build();
        TYPE_SAVE = registry.register(LOCSAVEABLE.getPath(), () -> attach_a);
    }
}
