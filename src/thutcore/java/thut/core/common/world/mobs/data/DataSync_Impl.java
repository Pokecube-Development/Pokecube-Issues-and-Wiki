package thut.core.common.world.mobs.data;

import com.google.common.collect.Lists;
import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.attachment.IAttachmentHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import thut.api.world.mobs.data.Data;
import thut.api.world.mobs.data.DataSync;
import thut.core.common.ThutCore;
import thut.core.common.world.mobs.data.types.Data_Byte;
import thut.core.common.world.mobs.data.types.Data_Float;
import thut.core.common.world.mobs.data.types.Data_Int;
import thut.core.common.world.mobs.data.types.Data_ItemStack;
import thut.core.common.world.mobs.data.types.Data_Long;
import thut.core.common.world.mobs.data.types.Data_Seat;
import thut.core.common.world.mobs.data.types.Data_String;
import thut.core.common.world.mobs.data.types.Data_UUID;
import thut.core.common.world.mobs.data.types.Data_Vec3;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Supplier;

public class DataSync_Impl implements DataSync
{
    public static Int2ObjectArrayMap<Class<? extends Data<?>>> REGISTRY = new Int2ObjectArrayMap<>();

    static
    {
        DataSync_Impl.addMapping(Data_Byte.class);
        DataSync_Impl.addMapping(Data_Int.class);
        DataSync_Impl.addMapping(Data_Float.class);
        DataSync_Impl.addMapping(Data_String.class);
        DataSync_Impl.addMapping(Data_UUID.class);
        DataSync_Impl.addMapping(Data_ItemStack.class);
        DataSync_Impl.addMapping(Data_Vec3.class);
        DataSync_Impl.addMapping(Data_Seat.class);
        DataSync_Impl.addMapping(Data_Long.class);
    }

    public static void addMapping(final Class<? extends Data<?>> dataType)
    {
        DataSync_Impl.REGISTRY.put(DataSync_Impl.REGISTRY.size(), dataType);
    }

    @SuppressWarnings("deprecation")
    public static void initID(final Data<?> data)
    {
        if (data.getUID() != -1)
        {
            data.getUID();
            return;
        }
        for (final Entry<Integer, Class<? extends Data<?>>> entry : DataSync_Impl.REGISTRY.entrySet())
            if (entry.getValue() == data.getClass())
            {
                data.setUID(entry.getKey());
                data.getUID();
                return;
            }
        throw new NullPointerException("Datatype not found for " + data);
    }

    @SuppressWarnings("unchecked")
    public static <T> T makeData(String name, int id) throws Exception
    {
        final Class<? extends Data<?>> dataType = DataSync_Impl.REGISTRY.get(id);
        if (dataType == null) throw new NullPointerException("No type registered for ID: " + id);
        final Data<?> data = dataType.getConstructor(String.class).newInstance(name);
        DataSync_Impl.initID(data);
        return (T) data;
    }

    private final List<Data<?>> data = new ArrayList<>();

    private final ReadWriteLock lock = new ReentrantReadWriteLock();

    private long tick;
    private String regTag = "unk";
    private boolean syncNow = false, needInit =false;

    private final int offset = ThutCore.newRandom().nextInt(1024);
    protected Provider provider = null;

    @Override
    public void setHolderLookup(Provider provider)
    {
        this.provider = provider;
    }

    @Override
    public List<Data<?>> getAll()
    {
        List<Data<?>> list = new ArrayList<>(this.data);
        syncNow = false;
        return list;
    }

    @Override
    public List<Data<?>> getDirty()
    {
        List<Data<?>> list = null;
        for (final Data<?> value : this.data)
            if (value.dirty())
            {
                if (list == null) list = Lists.newArrayList();
                list.add(value);
            }
        syncNow = false;
        return list;
    }

    @Override
    public <T> Data<T> register(final Data<T> data)
    {
        data.setHolderLookup(this.provider);
        data.setSync(this);
        data.setTag(this.regTag);
        final int id = this.data.size();
        data.setID(id);
        // Initialize the UID for this data.
        DataSync_Impl.initID(data);
        this.data.add(data);
        needInit = true;
        return data;
    }

    @Override
    public void setRegisterTag(String tag)
    {
        this.regTag = tag;
    }

    @Override
    public void setSyncNow()
    {
        syncNow = true;
    }

    @Override
    public void update(final List<Data<?>> values)
    {
        for (final Data<?> value : values)
        {
            final Data<?> old = this.data.get(value.getID());
            final int uid1 = value.getUID();
            final int uid2 = old.getUID();
            // Only update same values, things can go funny on initial syncing
            // if things have not initialized on both sides yet.
            if (uid1 != uid2) continue;
            old.setRaw(value.get());
        }
    }

    @Override
    public boolean needInit()
    {
        return needInit;
    }

    @Override
    public void clearNeedInit()
    {
        needInit = false;
    }

    @Override
    public void init(List<Data<?>> values)
    {
        this.data.clear();
        values.forEach(data-> {
            this.setRegisterTag(data.getTag());
            this.register(data);
        });
        needInit = false;
    }

    @Override
    public long getTick()
    {
        return tick;
    }

    @Override
    public void setTick(long tick)
    {
        this.tick = tick;
    }

    @Override
    public int tickOffset()
    {
        return offset;
    }

    @Override
    public List<Data<?>> getTagged(String tag)
    {
        List<Data<?>> all = getAll();
        List<Data<?>> list = new ArrayList<>();
        all.forEach(data -> {if (tag.equals(data.getTag())) list.add(data);});
        return list;
    }

    @Override
    public void clearMatching(String tag)
    {
        List<Data<?>> list = getTagged(tag);
        list.forEach(data -> {
            this.data.removeIf(d->d.getName().equals(data.getName()));
        });
    }

    @Override
    public void mapFrom(DataSync other, String tag)
    {
        this.clearMatching(tag);
        this.setRegisterTag(tag);
        List<Data<?>> tagged = other.getTagged(tag);
        other.clearMatching(tag);
        for (var d : tagged) this.register(d);
    }

    @Override
    public boolean syncNow()
    {
        return syncNow;
    }

    public static DataSync makeProvider(final IAttachmentHolder in)
    {
        Provider p = null;
        if (in instanceof Entity e) p = e.registryAccess();
        else if (in instanceof BlockEntity b) p = b.getLevel().registryAccess();
        if (p == null) return null;
        var impl = new DataSync_Impl();
        impl.setHolderLookup(p);
        return impl;
    }

    public static DataSync get(final IAttachmentHolder in)
    {
        return in.getData(TYPE.get());
    }

    public static final ResourceLocation KEY = ResourceLocation.parse("thutcore:data_sync");

    public static Supplier<AttachmentType<DataSync>> TYPE;

    public static void registerAttachment(DeferredRegister<AttachmentType<?>> registry)
    {
        TYPE = registry.register(KEY.getPath(), () -> AttachmentType.builder(DataSync_Impl::makeProvider).build());
    }

}
