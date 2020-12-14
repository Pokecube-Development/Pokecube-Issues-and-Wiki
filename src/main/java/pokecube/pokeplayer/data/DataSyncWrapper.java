package pokecube.pokeplayer.data;

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
        if (this.wrapped == this) return super.getAll();
        return this.wrapped.getAll();
    }

    @Override
    public List<Data<?>> getDirty()
    {
        if (this.wrapped == this) return super.getDirty();
        return this.wrapped.getDirty();
    }

    @Override
    public <T> T get(final int key)
    {
        if (this.wrapped == this) return super.get(key);
        return this.wrapped.get(key);
    }

    @Override
    public <T> int register(final Data<T> data, final T value)
    {
        return super.register(data, value);
    }

    @Override
    public <T> void set(final int key, final T value)
    {
        if (this.wrapped == this)
        {
            super.set(key, value);
            return;
        }
        this.wrapped.set(key, value);
    }

    @Override
    public void update(final List<Data<?>> values)
    {
        if (this.wrapped == this)
        {
            super.update(values);
            return;
        }
        this.wrapped.update(values);
    }
}
