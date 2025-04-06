package thut.core.common.world.mobs.data;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Function;
import java.util.function.Supplier;

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
    public static int getID(final Data<?> data)
    {
        if (data.getUID() != -1) return data.getUID();
        for (final Entry<Integer, Class<? extends Data<?>>> entry : DataSync_Impl.REGISTRY.entrySet())
            if (entry.getValue() == data.getClass())
        {
            data.setUID(entry.getKey());
            return data.getUID();
        }
        throw new NullPointerException("Datatype not found for " + data);
    }

    @SuppressWarnings("unchecked")
    public static <T> T makeData(final int id) throws Exception
    {
        final Class<? extends Data<?>> dataType = DataSync_Impl.REGISTRY.get(id);
        if (dataType == null) throw new NullPointerException("No type registered for ID: " + id);
        final Data<?> data = dataType.getConstructor().newInstance();
        DataSync_Impl.getID(data);
        return (T) data;
    }

    private Int2ObjectArrayMap<Data<?>> data = new Int2ObjectArrayMap<>();
    private Int2ObjectArrayMap<Data<?>> readCache = new Int2ObjectArrayMap<>();

    private final ReadWriteLock lock = new ReentrantReadWriteLock();

    private final Lock r = this.lock.readLock();
    private final Lock w = this.lock.writeLock();

    private long tick;

    private boolean syncNow = false;

    private int offset = ThutCore.newRandom().nextInt();
    protected Provider provider = null;

    @Override
    public void setHolderLookup(Provider provider)
    {
        this.provider = provider;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T get(final int key)
    {
        return (T) this.readCache.get(key).get();
    }

    @Override
    public List<Data<?>> getAll()
    {
        List<Data<?>> list = new ArrayList<Data<?>>();
        this.r.lock();
        list.addAll(this.data.values());
        this.r.unlock();
        syncNow = false;
        return list;
    }

    @Override
    public List<Data<?>> getDirty()
    {
        List<Data<?>> list = null;
        this.r.lock();
        for (final Data<?> value : this.data.values()) if (value.dirty())
        {
            if (list == null) list = Lists.newArrayList();
            list.add(value);
        }
        this.r.unlock();
        syncNow = false;
        return list;
    }

    @Override
    public <T> int register(final Data<T> data, final T value)
    {
        data.setHolderLookup(this.provider);
        data.set(value);
        final int id = this.data.size();
        data.setID(id);
        // Initialize the UID for this data.
        DataSync_Impl.getID(data);
        this.data.put(id, data);
        this.readCache.put(id, data);
        return id;
    }

    @Override
    public <T> void set(final int key, final T value)
    {
        this.w.lock();
        @SuppressWarnings("unchecked")
        final Data<T> type = (Data<T>) this.data.get(key);
        type.set(value);
        if (type.isRealtime() && type.dirty()) syncNow = true;
        this.w.unlock();
    }

    @Override
    public void update(final List<Data<?>> values)
    {
        this.w.lock();
        for (final Data<?> value : values)
        {
            // Only update things we already have. This fixes issues on
            // server/client syncing when both sides have not fully initialized.
            if (!this.data.containsKey(value.getID())) continue;
            final Data<?> old = this.data.get(value.getID());
            final int uid1 = value.getUID();
            final int uid2 = old.getUID();
            // Only update same values, things can go funny on initial syncing
            // if things have not initialized on both sides yet.
            if (uid1 != uid2) continue;
            this.data.put(value.getID(), value);
            this.readCache.put(value.getID(), value);
        }
        this.w.unlock();
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
        Function<IAttachmentHolder, DataSync> func_a = DataSync_Impl::makeProvider;
        var attach_a = AttachmentType.builder(func_a).build();
        TYPE = registry.register(KEY.getPath(), () -> attach_a);
    }

}
