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
import thut.api.level.structures.NamedVolumes.INamedVolume;

public class CapabilityWorldVolumes implements INBTSerializable<CompoundTag>
{
    public static class Building implements INamedPart, INBTSerializable<CompoundTag>
    {
        String name;
        BoundingBox bounds;

        public Building(){}

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
        public String getKey()
        {
            return "thutcore:building_part";
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

    public static class Structure implements INamedVolume, INBTSerializable<CompoundTag>
    {
        String name;
        BoundingBox bounds;
        List<INamedPart> buildings = Lists.newArrayList();

        private int hash = -1;

        public Structure(){}

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
            if (!(obj instanceof INamedVolume)) return false;
            return obj.toString().equals(this.toString());
        }

        @Override
        public String toString()
        {
            String key = this.getName() + " " + this.getTotalBounds();
            this.hash = key.hashCode();
            return key;
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
        public String getKey()
        {
            return "thutcore:structure";
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
                var _tag = NamedVolumes.saveVolumeOrPart(registries, b);
                if(!_tag.isEmpty()) list.add(_tag);
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
                    var part = NamedVolumes.loadPart(registries, comp);
                    if (part != null) this.buildings.add(part);
                }
            });
        }
    }

    static
    {
        NamedVolumes.VOLUMES_FACTORY_REGISTRY.put("thutcore:structure", Structure::new);
        NamedVolumes.PART_FACTORY_REGISTRY.put("thutcore:building_part", Building::new);
    }

    private final List<INamedVolume> volumes = Lists.newArrayList();
    private final ServerLevel level;

    public CapabilityWorldVolumes(ServerLevel level)
    {
        this.level = level;
    }

    public void addStructure(Structure s)
    {
        if (!this.volumes.contains(s)) this.volumes.add(s);
        StructureManager.addStructure(level.dimension(), s);
    }

    public void addBuilding(String structure, String building, BoundingBox bounds)
    {
        if (building == null) building = "unk_part";
        Building b = new Building(building, bounds);
        Set<INamedVolume> intersects = StructureManager.getColliding(level.dimension(), bounds);
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
        this.volumes.forEach(b -> {
            var _tag = NamedVolumes.saveVolumeOrPart(registries, b);
            if(!_tag.isEmpty()) list.add(_tag);
        });
        tag.put("volumes", list);
        return tag;
    }

    @Override
    public void deserializeNBT(HolderLookup.Provider registries, CompoundTag nbt)
    {
        ListTag list = nbt.getList("volumes", Tag.TAG_COMPOUND);;
        //LEGACY Support TODO remove this,
        if(nbt.contains("structures") && !nbt.contains("volumes"))
            list =  nbt.getList("structures", Tag.TAG_COMPOUND);
        this.volumes.clear();
        list.forEach(tag -> {
            if (tag instanceof CompoundTag comp) {
                var volume = NamedVolumes.loadVolume(registries, comp);
                if (volume != null) this.volumes.add(volume);
            }
        });
    }

    public static CapabilityWorldVolumes makeProvider(final IAttachmentHolder in)
    {
        if (!(in instanceof ServerLevel level)) return null;
        return new CapabilityWorldVolumes(level);
    }

    public static CapabilityWorldVolumes get(final IAttachmentHolder in)
    {
        return in.getData(TYPE_SAVE.get());
    }

    public static final ResourceLocation LOCSAVEABLE = ResourceLocation.parse("thutcore:world_structures");

    public static Supplier<AttachmentType<CapabilityWorldVolumes>> TYPE_SAVE;

    public static void registerAttachment(DeferredRegister<AttachmentType<?>> registry)
    {
        Function<IAttachmentHolder, CapabilityWorldVolumes> func_a = CapabilityWorldVolumes::makeProvider;
        var attach_a = AttachmentType.serializable(func_a).build();
        TYPE_SAVE = registry.register(LOCSAVEABLE.getPath(), () -> attach_a);
    }
}
