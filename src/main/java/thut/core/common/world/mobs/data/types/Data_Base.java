package thut.core.common.world.mobs.data.types;

import io.netty.buffer.ByteBuf;
import thut.api.world.mobs.data.Data;

public abstract class Data_Base<T> implements Data<T>
{
    private int     ID       = -1;
    private int     UID      = -1;
    private boolean dirty    = false;
    private T       lastSent = null;

    @Override
    public boolean dirty()
    {
        if (this.dirty) return true;
        final T value = this.get();
        return this.isDifferent(this.lastSent, value);
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

    protected void initLast(T last)
    {
        this.lastSent = last;
    }

    protected boolean isDifferent(T last, T value)
    {
        return last != null ? !last.equals(value) : value != null;
    }

    @Override
    public void read(ByteBuf buf)
    {
        this.ID = buf.readInt();
    }

    @Override
    public void setDirty(boolean dirty)
    {
        if (!dirty) this.lastSent = this.get();
        else this.dirty = dirty;
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
        this.lastSent = this.get();
        buf.writeInt(this.ID);
    }

}
