package thut.core.common.world.mobs.data;

import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import com.google.common.collect.Lists;

import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import net.minecraft.core.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import thut.api.ThutCaps;
import thut.api.world.mobs.data.Data;
import thut.api.world.mobs.data.DataSync;
import thut.core.common.world.mobs.data.types.Data_Byte;
import thut.core.common.world.mobs.data.types.Data_Float;
import thut.core.common.world.mobs.data.types.Data_Int;
import thut.core.common.world.mobs.data.types.Data_ItemStack;
import thut.core.common.world.mobs.data.types.Data_String;
import thut.core.common.world.mobs.data.types.Data_UUID;

public class DataSync_Impl implements DataSync, ICapabilityProvider
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

    public Int2ObjectArrayMap<Data<?>>   data   = new Int2ObjectArrayMap<>();
    private final LazyOptional<DataSync> holder = LazyOptional.of(() -> this);

    private final ReadWriteLock lock = new ReentrantReadWriteLock();

    private final Lock r = this.lock.readLock();
    private final Lock w = this.lock.writeLock();

    @Override
    @SuppressWarnings("unchecked")
    public <T> T get(final int key)
    {
        this.r.lock();
        try
        {
            return (T) this.data.get(key).get();
        }
        finally
        {
            this.r.unlock();
        }
    }

    @Override
    public List<Data<?>> getAll()
    {
        List<Data<?>> list = null;
        this.r.lock();
        for (final Data<?> value : this.data.values())
        {
            if (list == null) list = Lists.newArrayList();
            list.add(value);
        }
        this.r.unlock();
        return list;
    }

    @Override
    public <T> LazyOptional<T> getCapability(final Capability<T> capability, final Direction facing)
    {
        return ThutCaps.DATASYNC.orEmpty(capability, this.holder);
    }

    @Override
    public List<Data<?>> getDirty()
    {
        List<Data<?>> list = null;
        this.r.lock();
        for (final Data<?> value : this.data.values())
            if (value.dirty())
            {
                if (list == null) list = Lists.newArrayList();
                list.add(value);
            }
        this.r.unlock();
        return list;
    }

    @Override
    public <T> int register(final Data<T> data, final T value)
    {
        data.set(value);
        final int id = this.data.size();
        data.setID(id);
        // Initialize the UID for this data.
        DataSync_Impl.getID(data);
        this.data.put(id, data);
        return id;
    }

    @Override
    public <T> void set(final int key, final T value)
    {
        this.w.lock();
        @SuppressWarnings("unchecked")
        final Data<T> type = (Data<T>) this.data.get(key);
        type.set(value);
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
        }
        this.w.unlock();
    }

}
