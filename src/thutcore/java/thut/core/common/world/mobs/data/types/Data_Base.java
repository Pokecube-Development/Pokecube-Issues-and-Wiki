package thut.core.common.world.mobs.data.types;

import io.netty.buffer.ByteBuf;
import net.minecraft.core.HolderLookup.Provider;
import thut.api.world.mobs.data.Data;
import thut.api.world.mobs.data.DataSync;

public abstract class Data_Base<T> implements Data<T>
{
    private int ID = -1;
    private int UID = -1;
    private boolean dirty = false;
    private String tag;
    protected T value;

    private boolean realtime = false;
    protected Provider provider = null;
    private DataSync sync;

    @Override
    public void setSync(DataSync sync)
    {
        this.sync = sync;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void setRaw(Object value)
    {
        this.value = (T) value;
    }

    public String getTag()
    {
        return tag;
    }

    @Override
    public Data<T> setTag(String tag)
    {
        this.tag = tag;
        return this;
    }

    @Override
    public void setHolderLookup(Provider provider)
    {
        this.provider = provider;
    }

    @Override
    public boolean dirty()
    {
        return this.dirty;
    }

    @Override
    public int getID()
    {
        return this.ID;
    }

    @Override
    public int getUID()
    {
        return this.UID;
    }

    @Override
    public void read(ByteBuf buf)
    {
        this.ID = buf.readInt();
    }

    @Override
    public void setDirty(boolean dirty)
    {
        this.dirty = dirty;
        if (this.isRealtime() && dirty) sync.setSyncNow();
    }

    @Override
    public void setID(int id)
    {
        this.ID = id;
    }

    @Override
    public void setUID(int id)
    {
        this.UID = id;
    }

    @Override
    public void write(ByteBuf buf)
    {
        this.dirty = false;
        buf.writeInt(this.ID);
    }

    @Override
    public Data<T> setRealtime()
    {
        realtime = true;
        return this;
    }

    @Override
    public boolean isRealtime()
    {
        return realtime;
    }
}
