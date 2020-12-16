package pokecube.pokeplayer.network;

import java.util.List;

import thut.api.world.mobs.data.Data;
import thut.api.world.mobs.data.DataSync;
import thut.core.common.world.mobs.data.DataSync_Impl;

public class DataSyncWrapper extends DataSync_Impl
{
    public DataSync wrapped = this;

    @Override
    public List<Data<?>> getAll()
    {
        if (wrapped == this) return super.getAll();
        return wrapped.getAll();
    }

    @Override
    public List<Data<?>> getDirty()
    {
        if (wrapped == this) return super.getDirty();
        return wrapped.getDirty();
    }

    @Override
    public <T> T get(int key)
    {
        if (wrapped == this) return super.get(key);
        return wrapped.get(key);
    }

    @Override
    public <T> int register(Data<T> data, T value)
    {
        return super.register(data, value);
    }

    @Override
    public <T> void set(int key, T value)
    {
        if (wrapped == this)
        {
            super.set(key, value);
            return;
        }
        wrapped.set(key, value);
    }

    @Override
    public void update(List<Data<?>> values)
    {
        if (wrapped == this)
        {
            super.update(values);
            return;
        }
        wrapped.update(values);
    }

}
